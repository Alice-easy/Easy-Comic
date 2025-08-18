# -*- coding: utf-8 -*-
"""
文件分析器模块
"""

import os
import re
import fnmatch
from pathlib import Path
from typing import List, Set, Dict, Optional, Tuple
import time

from models.scan_result import FileInfo, FileType, DirectoryInfo
from config.settings import Config
from utils.logger import log_operation


class FileAnalyzer:
    """文件分析器"""
    
    def __init__(self, config: Config):
        """
        初始化文件分析器
        
        Args:
            config: 配置对象
        """
        self.config = config
        self.protected_dirs = set(config.get_protected_directories())
        self.protected_files = set(config.get_protected_files())
    
    @log_operation("分析测试文件")
    def analyze_test_files(self, project_path: Path) -> List[FileInfo]:
        """
        分析测试文件
        
        Args:
            project_path: 项目根路径
            
        Returns:
            测试文件列表
        """
        test_files = []
        test_patterns = self.config.get_test_file_patterns()
        test_directories = self.config.get_test_directories()
        
        # 扫描测试目录
        for test_dir in test_directories:
            test_path = project_path / test_dir
            if test_path.exists() and test_path.is_dir():
                test_files.extend(self._scan_directory_for_files(
                    test_path, test_patterns, FileType.TEST_FILE, f"测试目录: {test_dir}"
                ))
        
        # 扫描整个项目中的测试文件
        for file_path in self._walk_project(project_path):
            if self._is_protected_path(file_path, project_path):
                continue
                
            if self._matches_patterns(file_path.name, test_patterns):
                if not any(file.path == file_path for file in test_files):
                    file_info = self._create_file_info(
                        file_path, FileType.TEST_FILE, "测试文件模式匹配"
                    )
                    test_files.append(file_info)
        
        return test_files
    
    @log_operation("分析过期文件")
    def analyze_obsolete_files(self, project_path: Path) -> List[FileInfo]:
        """
        分析过期文件
        
        Args:
            project_path: 项目根路径
            
        Returns:
            过期文件列表
        """
        obsolete_files = []
        obsolete_patterns = self.config.get_obsolete_file_patterns()
        
        for file_path in self._walk_project(project_path):
            if self._is_protected_path(file_path, project_path):
                continue
            
            # 检查文件模式
            if self._matches_patterns(file_path.name, obsolete_patterns):
                file_type = self._determine_obsolete_file_type(file_path)
                reason = f"匹配过期文件模式: {file_path.suffix}"
                
                file_info = self._create_file_info(file_path, file_type, reason)
                obsolete_files.append(file_info)
        
        return obsolete_files
    
    @log_operation("分析未使用资源")
    def analyze_unused_resources(self, project_path: Path) -> List[FileInfo]:
        """
        分析未使用的资源文件
        
        Args:
            project_path: 项目根路径
            
        Returns:
            未使用资源文件列表
        """
        unused_resources = []
        resource_patterns = self.config.get("unused_resource_patterns", [])
        
        # 扫描资源目录
        for module_path in self._find_android_modules(project_path):
            res_path = module_path / "src" / "main" / "res"
            if res_path.exists():
                unused_resources.extend(self._analyze_module_resources(res_path, resource_patterns))
        
        return unused_resources
    
    @log_operation("分析测试目录")
    def analyze_test_directories(self, project_path: Path) -> List[DirectoryInfo]:
        """
        分析测试目录
        
        Args:
            project_path: 项目根路径
            
        Returns:
            测试目录列表
        """
        test_directories = []
        test_dir_patterns = self.config.get_test_directories()
        
        for pattern in test_dir_patterns:
            for module_path in self._find_android_modules(project_path):
                test_path = module_path / pattern
                if test_path.exists() and test_path.is_dir():
                    dir_info = self._create_directory_info(test_path, "测试目录")
                    test_directories.append(dir_info)
        
        return test_directories
    
    @log_operation("分析空目录")
    def analyze_empty_directories(self, project_path: Path) -> List[DirectoryInfo]:
        """
        分析空目录
        
        Args:
            project_path: 项目根路径
            
        Returns:
            空目录列表
        """
        empty_directories = []
        
        for dir_path in self._walk_directories(project_path):
            if self._is_protected_path(dir_path, project_path):
                continue
            
            if self._is_empty_directory(dir_path):
                dir_info = self._create_directory_info(dir_path, "空目录")
                empty_directories.append(dir_info)
        
        return empty_directories
    
    def _walk_project(self, project_path: Path):
        """遍历项目文件"""
        for root, dirs, files in os.walk(project_path):
            root_path = Path(root)
            
            # 跳过受保护的目录
            dirs[:] = [d for d in dirs if not self._is_protected_directory(root_path / d, project_path)]
            
            for file in files:
                file_path = root_path / file
                if not self._is_protected_path(file_path, project_path):
                    yield file_path
    
    def _walk_directories(self, project_path: Path):
        """遍历项目目录"""
        for root, dirs, files in os.walk(project_path):
            root_path = Path(root)
            
            for dir_name in dirs:
                dir_path = root_path / dir_name
                if not self._is_protected_path(dir_path, project_path):
                    yield dir_path
    
    def _find_android_modules(self, project_path: Path) -> List[Path]:
        """查找Android模块"""
        modules = []
        
        # 检查根目录是否是模块
        if (project_path / "build.gradle").exists() or (project_path / "build.gradle.kts").exists():
            modules.append(project_path)
        
        # 查找子模块
        for item in project_path.iterdir():
            if item.is_dir() and not item.name.startswith('.'):
                if (item / "build.gradle").exists() or (item / "build.gradle.kts").exists():
                    modules.append(item)
        
        return modules
    
    def _scan_directory_for_files(self, directory: Path, patterns: List[str], 
                                 file_type: FileType, reason: str) -> List[FileInfo]:
        """扫描目录中的文件"""
        files = []
        
        for file_path in directory.rglob("*"):
            if file_path.is_file() and not self._is_protected_path(file_path, directory.parent):
                if not patterns or self._matches_patterns(file_path.name, patterns):
                    file_info = self._create_file_info(file_path, file_type, reason)
                    files.append(file_info)
        
        return files
    
    def _analyze_module_resources(self, res_path: Path, patterns: List[str]) -> List[FileInfo]:
        """分析模块资源"""
        unused_resources = []
        
        for pattern in patterns:
            for file_path in res_path.rglob(pattern):
                if file_path.is_file():
                    file_info = self._create_file_info(
                        file_path, FileType.UNUSED_RESOURCE, f"未使用资源: {pattern}"
                    )
                    unused_resources.append(file_info)
        
        return unused_resources
    
    def _matches_patterns(self, filename: str, patterns: List[str]) -> bool:
        """检查文件名是否匹配模式"""
        return any(fnmatch.fnmatch(filename, pattern) for pattern in patterns)
    
    def _is_protected_path(self, path: Path, project_path: Path) -> bool:
        """检查路径是否受保护"""
        try:
            relative_path = path.relative_to(project_path)
            
            # 检查文件名
            if path.name in self.protected_files:
                return True
            
            # 检查目录路径
            for protected_dir in self.protected_dirs:
                if str(relative_path).startswith(protected_dir):
                    return True
            
            return False
        except ValueError:
            return True
    
    def _is_protected_directory(self, dir_path: Path, project_path: Path) -> bool:
        """检查目录是否受保护"""
        try:
            relative_path = dir_path.relative_to(project_path)
            return any(str(relative_path).startswith(protected) for protected in self.protected_dirs)
        except ValueError:
            return True
    
    def _is_empty_directory(self, dir_path: Path) -> bool:
        """检查目录是否为空"""
        try:
            return not any(dir_path.iterdir())
        except (OSError, PermissionError):
            return False
    
    def _determine_obsolete_file_type(self, file_path: Path) -> FileType:
        """确定过期文件类型"""
        suffix = file_path.suffix.lower()
        name = file_path.name.lower()
        
        if suffix in ['.bak', '.backup', '.old'] or 'backup' in name:
            return FileType.BACKUP_FILE
        elif suffix in ['.tmp', '.temp', '.cache'] or 'temp' in name:
            return FileType.TEMP_FILE
        elif suffix in ['.log'] or 'log' in name:
            return FileType.LOG_FILE
        else:
            return FileType.OBSOLETE_FILE
    
    def _create_file_info(self, file_path: Path, file_type: FileType, reason: str) -> FileInfo:
        """创建文件信息对象"""
        try:
            stat = file_path.stat()
            return FileInfo(
                path=file_path,
                size=stat.st_size,
                file_type=file_type,
                reason=reason,
                last_modified=stat.st_mtime,
                is_protected=False
            )
        except (OSError, PermissionError):
            return FileInfo(
                path=file_path,
                size=0,
                file_type=file_type,
                reason=f"{reason} (无法访问文件信息)",
                last_modified=time.time(),
                is_protected=False
            )
    
    def _create_directory_info(self, dir_path: Path, directory_type: str) -> DirectoryInfo:
        """创建目录信息对象"""
        try:
            total_size = 0
            file_count = 0
            files = []
            
            for file_path in dir_path.rglob("*"):
                if file_path.is_file():
                    try:
                        size = file_path.stat().st_size
                        total_size += size
                        file_count += 1
                        
                        file_info = self._create_file_info(
                            file_path, FileType.TEST_FILE, f"位于{directory_type}中"
                        )
                        files.append(file_info)
                    except (OSError, PermissionError):
                        continue
            
            return DirectoryInfo(
                path=dir_path,
                size=total_size,
                file_count=file_count,
                directory_type=directory_type,
                is_protected=False,
                files=files
            )
        except (OSError, PermissionError):
            return DirectoryInfo(
                path=dir_path,
                size=0,
                file_count=0,
                directory_type=f"{directory_type} (无法访问)",
                is_protected=False,
                files=[]
            )