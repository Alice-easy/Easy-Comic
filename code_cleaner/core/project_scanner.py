# -*- coding: utf-8 -*-
"""
项目扫描器模块
"""

import time
import logging
from pathlib import Path
from typing import List, Dict, Optional

from models.scan_result import ScanResult, ModuleInfo, ProjectStats
from core.file_analyzer import FileAnalyzer
from core.dependency_analyzer import DependencyAnalyzer
from config.settings import Config
from utils.logger import log_operation, ProgressLogger


class ProjectScanner:
    """项目扫描器"""
    
    def __init__(self, project_path: Path, config: Config):
        """
        初始化项目扫描器
        
        Args:
            project_path: 项目根路径
            config: 配置对象
        """
        self.project_path = project_path
        self.config = config
        self.logger = logging.getLogger("code_cleaner")
        
        # 初始化分析器
        self.file_analyzer = FileAnalyzer(config)
        self.dependency_analyzer = DependencyAnalyzer(config)
    
    @log_operation("扫描项目")
    def scan_project(self) -> ScanResult:
        """
        扫描整个项目
        
        Returns:
            扫描结果
        """
        start_time = time.time()
        
        # 创建扫描结果对象
        scan_result = ScanResult(
            project_path=self.project_path,
            scan_time=start_time
        )
        
        try:
            # 验证项目结构
            self._validate_project_structure()
            
            # 扫描模块信息
            self.logger.info("扫描项目模块...")
            scan_result.modules = self._scan_modules()
            
            # 分析文件
            self.logger.info("分析项目文件...")
            self._analyze_files(scan_result)
            
            # 分析依赖
            if self.config.get("dependency_analysis.check_unused_dependencies", True):
                self.logger.info("分析项目依赖...")
                scan_result.unused_dependencies = self.dependency_analyzer.analyze_unused_dependencies(
                    self.project_path
                )
            
            # 风险评估
            self.logger.info("执行风险评估...")
            self._perform_risk_assessment(scan_result)
            
            # 更新统计信息
            scan_result.update_stats()
            
            # 记录扫描完成时间
            scan_result.scan_time = time.time() - start_time
            
            self.logger.info(f"项目扫描完成，耗时 {scan_result.scan_time:.2f} 秒")
            self._log_scan_summary(scan_result)
            
            return scan_result
            
        except Exception as e:
            self.logger.error(f"项目扫描失败: {e}")
            raise
    
    def _validate_project_structure(self):
        """验证项目结构"""
        if not self.project_path.exists():
            raise ValueError(f"项目路径不存在: {self.project_path}")
        
        if not self.project_path.is_dir():
            raise ValueError(f"项目路径不是目录: {self.project_path}")
        
        # 检查是否是Android项目
        android_indicators = [
            "build.gradle",
            "build.gradle.kts",
            "settings.gradle",
            "settings.gradle.kts",
            "gradlew"
        ]
        
        has_android_files = any(
            (self.project_path / indicator).exists() 
            for indicator in android_indicators
        )
        
        if not has_android_files:
            self.logger.warning("未检测到Android项目标识文件，可能不是标准的Android项目")
    
    def _scan_modules(self) -> List[ModuleInfo]:
        """扫描项目模块"""
        modules = []
        
        # 扫描根模块
        root_module = self._scan_single_module(self.project_path, "root")
        if root_module:
            modules.append(root_module)
        
        # 扫描子模块
        for item in self.project_path.iterdir():
            if item.is_dir() and not item.name.startswith('.'):
                # 检查是否是Android模块
                if self._is_android_module(item):
                    module = self._scan_single_module(item, item.name)
                    if module:
                        modules.append(module)
        
        self.logger.info(f"发现 {len(modules)} 个模块")
        return modules
    
    def _scan_single_module(self, module_path: Path, module_name: str) -> Optional[ModuleInfo]:
        """扫描单个模块"""
        try:
            # 查找构建文件
            build_file = None
            for build_name in ["build.gradle.kts", "build.gradle"]:
                build_path = module_path / build_name
                if build_path.exists():
                    build_file = build_path
                    break
            
            # 扫描测试目录
            test_directories = []
            for test_dir_pattern in self.config.get_test_directories():
                test_path = module_path / test_dir_pattern
                if test_path.exists() and test_path.is_dir():
                    dir_info = self.file_analyzer._create_directory_info(test_path, "测试目录")
                    test_directories.append(dir_info)
            
            # 扫描源码目录
            source_directories = []
            src_main_path = module_path / "src" / "main"
            if src_main_path.exists() and src_main_path.is_dir():
                dir_info = self.file_analyzer._create_directory_info(src_main_path, "源码目录")
                source_directories.append(dir_info)
            
            # 分析依赖（如果有构建文件）
            dependencies = []
            if build_file:
                dependencies = self.dependency_analyzer.analyze_module_dependencies(build_file)
            
            return ModuleInfo(
                name=module_name,
                path=module_path,
                build_file=build_file,
                dependencies=dependencies,
                test_directories=test_directories,
                source_directories=source_directories
            )
            
        except Exception as e:
            self.logger.warning(f"扫描模块 {module_name} 时出错: {e}")
            return None
    
    def _is_android_module(self, path: Path) -> bool:
        """检查是否是Android模块"""
        build_files = ["build.gradle", "build.gradle.kts"]
        return any((path / build_file).exists() for build_file in build_files)
    
    def _analyze_files(self, scan_result: ScanResult):
        """分析项目文件"""
        progress = ProgressLogger(self.logger, 6, "文件分析")
        
        # 分析测试文件
        progress.update(1, "分析测试文件")
        scan_result.test_files = self.file_analyzer.analyze_test_files(self.project_path)
        
        # 分析过期文件
        progress.update(1, "分析过期文件")
        obsolete_files = self.file_analyzer.analyze_obsolete_files(self.project_path)
        
        # 按类型分类过期文件
        for file_info in obsolete_files:
            if file_info.file_type.value == "backup_file":
                scan_result.backup_files.append(file_info)
            elif file_info.file_type.value == "temp_file":
                scan_result.temp_files.append(file_info)
            elif file_info.file_type.value == "log_file":
                scan_result.log_files.append(file_info)
            else:
                scan_result.obsolete_files.append(file_info)
        
        # 分析未使用资源
        progress.update(1, "分析未使用资源")
        scan_result.unused_resources = self.file_analyzer.analyze_unused_resources(self.project_path)
        
        # 分析测试目录
        progress.update(1, "分析测试目录")
        scan_result.test_directories = self.file_analyzer.analyze_test_directories(self.project_path)
        
        # 分析空目录
        progress.update(1, "分析空目录")
        scan_result.empty_directories = self.file_analyzer.analyze_empty_directories(self.project_path)
        
        progress.finish("文件分析完成")
    
    def _perform_risk_assessment(self, scan_result: ScanResult):
        """执行风险评估"""
        # 检查高风险文件
        for file_info in scan_result.get_all_cleanable_files():
            if self._is_high_risk_file(file_info):
                scan_result.high_risk_files.append(file_info)
        
        # 添加警告信息
        if scan_result.high_risk_files:
            scan_result.add_warning(f"发现 {len(scan_result.high_risk_files)} 个高风险文件")
        
        # 检查大量文件删除
        total_files = scan_result.get_total_cleanable_count()
        max_files = self.config.get("safety_checks.max_files_per_operation", 1000)
        
        if total_files > max_files:
            scan_result.add_warning(f"待删除文件数量 ({total_files}) 超过安全阈值 ({max_files})")
        
        # 检查重要目录
        important_dirs = ["src/main", "app/src/main"]
        for test_dir in scan_result.test_directories:
            for important_dir in important_dirs:
                if important_dir in str(test_dir.path):
                    scan_result.add_warning(f"测试目录 {test_dir.path} 可能影响重要源码目录")
    
    def _is_high_risk_file(self, file_info) -> bool:
        """检查是否是高风险文件"""
        # 检查文件大小（大于10MB认为是高风险）
        if file_info.size > 10 * 1024 * 1024:
            return True
        
        # 检查文件路径中是否包含重要关键词
        path_str = str(file_info.path).lower()
        risk_keywords = ["main", "src", "important", "config", "key"]
        
        return any(keyword in path_str for keyword in risk_keywords)
    
    def _log_scan_summary(self, scan_result: ScanResult):
        """记录扫描摘要"""
        self.logger.info("=" * 50)
        self.logger.info("项目扫描摘要:")
        self.logger.info(f"  模块数量: {len(scan_result.modules)}")
        self.logger.info(f"  测试文件: {len(scan_result.test_files)} 个")
        self.logger.info(f"  过期文件: {len(scan_result.obsolete_files)} 个")
        self.logger.info(f"  备份文件: {len(scan_result.backup_files)} 个")
        self.logger.info(f"  临时文件: {len(scan_result.temp_files)} 个")
        self.logger.info(f"  日志文件: {len(scan_result.log_files)} 个")
        self.logger.info(f"  未使用资源: {len(scan_result.unused_resources)} 个")
        self.logger.info(f"  测试目录: {len(scan_result.test_directories)} 个")
        self.logger.info(f"  空目录: {len(scan_result.empty_directories)} 个")
        self.logger.info(f"  未使用依赖: {len(scan_result.unused_dependencies)} 个")
        
        total_size = scan_result.get_total_cleanable_size()
        size_mb = total_size / (1024 * 1024)
        self.logger.info(f"  可释放空间: {size_mb:.2f} MB")
        
        if scan_result.warnings:
            self.logger.warning(f"  警告数量: {len(scan_result.warnings)}")
            for warning in scan_result.warnings:
                self.logger.warning(f"    - {warning}")
        
        self.logger.info("=" * 50)
