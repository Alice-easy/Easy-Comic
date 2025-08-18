# -*- coding: utf-8 -*-
"""
依赖分析器模块
"""

import re
import logging
from pathlib import Path
from typing import List, Dict, Set, Optional, Tuple

from models.scan_result import DependencyInfo
from config.settings import Config
from utils.logger import log_operation


class DependencyAnalyzer:
    """依赖分析器"""
    
    def __init__(self, config: Config):
        """
        初始化依赖分析器
        
        Args:
            config: 配置对象
        """
        self.config = config
        self.logger = logging.getLogger("code_cleaner")
        self.exclude_dependencies = set(config.get("dependency_analysis.exclude_dependencies", []))
    
    @log_operation("分析未使用依赖")
    def analyze_unused_dependencies(self, project_path: Path) -> List[DependencyInfo]:
        """
        分析未使用的依赖
        
        Args:
            project_path: 项目根路径
            
        Returns:
            未使用依赖列表
        """
        unused_dependencies = []
        
        # 查找所有模块的构建文件
        build_files = self._find_build_files(project_path)
        
        for build_file in build_files:
            module_name = build_file.parent.name
            
            # 解析依赖
            declared_deps = self._parse_dependencies(build_file)
            
            # 分析使用情况
            for dep in declared_deps:
                if dep.name not in self.exclude_dependencies:
                    usage_count = self._count_dependency_usage(build_file.parent, dep.name)
                    
                    if usage_count == 0:
                        dep.is_used = False
                        dep.usage_count = 0
                        dep.module = module_name
                        unused_dependencies.append(dep)
                    else:
                        dep.is_used = True
                        dep.usage_count = usage_count
        
        return unused_dependencies
    
    def analyze_module_dependencies(self, build_file: Path) -> List[DependencyInfo]:
        """
        分析模块依赖
        
        Args:
            build_file: 构建文件路径
            
        Returns:
            依赖信息列表
        """
        return self._parse_dependencies(build_file)
    
    def _find_build_files(self, project_path: Path) -> List[Path]:
        """查找所有构建文件"""
        build_files = []
        
        # 查找根目录构建文件
        for build_name in ["build.gradle.kts", "build.gradle"]:
            build_file = project_path / build_name
            if build_file.exists():
                build_files.append(build_file)
        
        # 查找子模块构建文件
        for item in project_path.iterdir():
            if item.is_dir() and not item.name.startswith('.'):
                for build_name in ["build.gradle.kts", "build.gradle"]:
                    build_file = item / build_name
                    if build_file.exists():
                        build_files.append(build_file)
        
        return build_files
    
    def _parse_dependencies(self, build_file: Path) -> List[DependencyInfo]:
        """
        解析构建文件中的依赖
        
        Args:
            build_file: 构建文件路径
            
        Returns:
            依赖信息列表
        """
        dependencies = []
        
        try:
            content = build_file.read_text(encoding='utf-8')
            
            # 根据文件类型选择解析方法
            if build_file.name.endswith('.kts'):
                dependencies = self._parse_kotlin_dependencies(content)
            else:
                dependencies = self._parse_groovy_dependencies(content)
                
        except Exception as e:
            self.logger.warning(f"解析构建文件 {build_file} 失败: {e}")
        
        return dependencies
    
    def _parse_kotlin_dependencies(self, content: str) -> List[DependencyInfo]:
        """解析Kotlin DSL依赖"""
        dependencies = []
        
        # 匹配依赖声明的正则表达式
        patterns = [
            r'implementation\s*\(\s*"([^"]+)"\s*\)',
            r'api\s*\(\s*"([^"]+)"\s*\)',
            r'testImplementation\s*\(\s*"([^"]+)"\s*\)',
            r'androidTestImplementation\s*\(\s*"([^"]+)"\s*\)',
            r'compileOnly\s*\(\s*"([^"]+)"\s*\)',
            r'runtimeOnly\s*\(\s*"([^"]+)"\s*\)',
        ]
        
        for pattern in patterns:
            matches = re.findall(pattern, content, re.MULTILINE)
            for match in matches:
                dep_info = self._parse_dependency_string(match)
                if dep_info:
                    dependencies.append(dep_info)
        
        return dependencies
    
    def _parse_groovy_dependencies(self, content: str) -> List[DependencyInfo]:
        """解析Groovy DSL依赖"""
        dependencies = []
        
        # 匹配依赖声明的正则表达式
        patterns = [
            r"implementation\s+['\"]([^'\"]+)['\"]",
            r"api\s+['\"]([^'\"]+)['\"]",
            r"testImplementation\s+['\"]([^'\"]+)['\"]",
            r"androidTestImplementation\s+['\"]([^'\"]+)['\"]",
            r"compileOnly\s+['\"]([^'\"]+)['\"]",
            r"runtimeOnly\s+['\"]([^'\"]+)['\"]",
        ]
        
        for pattern in patterns:
            matches = re.findall(pattern, content, re.MULTILINE)
            for match in matches:
                dep_info = self._parse_dependency_string(match)
                if dep_info:
                    dependencies.append(dep_info)
        
        return dependencies
    
    def _parse_dependency_string(self, dep_string: str) -> Optional[DependencyInfo]:
        """
        解析依赖字符串
        
        Args:
            dep_string: 依赖字符串，如 "androidx.core:core-ktx:1.8.0"
            
        Returns:
            依赖信息对象
        """
        try:
            parts = dep_string.split(':')
            if len(parts) >= 2:
                group_id = parts[0]
                artifact_id = parts[1]
                version = parts[2] if len(parts) > 2 else "unknown"
                
                name = f"{group_id}:{artifact_id}"
                
                return DependencyInfo(
                    name=name,
                    version=version,
                    is_used=True,  # 默认认为已使用，后续分析会更新
                    usage_count=0
                )
        except Exception as e:
            self.logger.debug(f"解析依赖字符串失败: {dep_string} - {e}")
        
        return None
    
    def _count_dependency_usage(self, module_path: Path, dependency_name: str) -> int:
        """
        统计依赖在模块中的使用次数
        
        Args:
            module_path: 模块路径
            dependency_name: 依赖名称
            
        Returns:
            使用次数
        """
        usage_count = 0
        
        # 提取包名用于搜索
        package_name = self._extract_package_name(dependency_name)
        if not package_name:
            return 0
        
        # 搜索源码文件中的import语句
        src_dirs = [
            module_path / "src" / "main" / "java",
            module_path / "src" / "main" / "kotlin",
        ]
        
        for src_dir in src_dirs:
            if src_dir.exists():
                usage_count += self._search_imports_in_directory(src_dir, package_name)
        
        return usage_count
    
    def _extract_package_name(self, dependency_name: str) -> Optional[str]:
        """
        从依赖名称提取包名
        
        Args:
            dependency_name: 依赖名称，如 "androidx.core:core-ktx"
            
        Returns:
            包名，如 "androidx.core"
        """
        try:
            # 简单的包名映射
            package_mappings = {
                "androidx.core:core-ktx": "androidx.core",
                "androidx.appcompat:appcompat": "androidx.appcompat",
                "com.google.android.material:material": "com.google.android.material",
                "androidx.lifecycle:lifecycle": "androidx.lifecycle",
                "androidx.navigation:navigation": "androidx.navigation",
                "androidx.room:room": "androidx.room",
                "androidx.compose:compose": "androidx.compose",
            }
            
            # 直接映射
            if dependency_name in package_mappings:
                return package_mappings[dependency_name]
            
            # 提取group id作为包名
            parts = dependency_name.split(':')
            if len(parts) >= 1:
                return parts[0]
                
        except Exception as e:
            self.logger.debug(f"提取包名失败: {dependency_name} - {e}")
        
        return None
    
    def _search_imports_in_directory(self, directory: Path, package_name: str) -> int:
        """
        在目录中搜索import语句
        
        Args:
            directory: 搜索目录
            package_name: 包名
            
        Returns:
            找到的import语句数量
        """
        count = 0
        
        try:
            for file_path in directory.rglob("*.kt"):
                count += self._search_imports_in_file(file_path, package_name)
            
            for file_path in directory.rglob("*.java"):
                count += self._search_imports_in_file(file_path, package_name)
                
        except Exception as e:
            self.logger.debug(f"搜索目录 {directory} 失败: {e}")
        
        return count
    
    def _search_imports_in_file(self, file_path: Path, package_name: str) -> int:
        """
        在文件中搜索import语句
        
        Args:
            file_path: 文件路径
            package_name: 包名
            
        Returns:
            找到的import语句数量
        """
        count = 0
        
        try:
            content = file_path.read_text(encoding='utf-8')
            
            # 搜索import语句
            import_pattern = rf'import\s+{re.escape(package_name)}'
            matches = re.findall(import_pattern, content, re.MULTILINE)
            count = len(matches)
            
        except Exception as e:
            self.logger.debug(f"搜索文件 {file_path} 失败: {e}")
        
        return count