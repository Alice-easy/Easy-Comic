# -*- coding: utf-8 -*-
"""
扫描结果数据模型
"""

from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Dict, Set, Optional
from enum import Enum


class FileType(Enum):
    """文件类型枚举"""
    TEST_FILE = "test_file"
    OBSOLETE_FILE = "obsolete_file"
    UNUSED_RESOURCE = "unused_resource"
    BACKUP_FILE = "backup_file"
    TEMP_FILE = "temp_file"
    LOG_FILE = "log_file"


@dataclass
class FileInfo:
    """文件信息"""
    path: Path
    size: int
    file_type: FileType
    reason: str
    last_modified: float
    is_protected: bool = False
    dependencies: List[str] = field(default_factory=list)


@dataclass
class DirectoryInfo:
    """目录信息"""
    path: Path
    size: int
    file_count: int
    directory_type: str
    is_protected: bool = False
    files: List[FileInfo] = field(default_factory=list)


@dataclass
class DependencyInfo:
    """依赖信息"""
    name: str
    version: str
    is_used: bool
    usage_count: int
    file_references: List[str] = field(default_factory=list)
    module: str = ""


@dataclass
class ModuleInfo:
    """模块信息"""
    name: str
    path: Path
    build_file: Optional[Path]
    dependencies: List[DependencyInfo] = field(default_factory=list)
    test_directories: List[DirectoryInfo] = field(default_factory=list)
    source_directories: List[DirectoryInfo] = field(default_factory=list)


@dataclass
class ProjectStats:
    """项目统计信息"""
    total_files: int = 0
    total_size: int = 0
    test_files_count: int = 0
    test_files_size: int = 0
    obsolete_files_count: int = 0
    obsolete_files_size: int = 0
    unused_resources_count: int = 0
    unused_resources_size: int = 0
    unused_dependencies_count: int = 0
    modules_count: int = 0


@dataclass
class ScanResult:
    """扫描结果"""
    project_path: Path
    scan_time: float
    
    # 项目结构信息
    modules: List[ModuleInfo] = field(default_factory=list)
    
    # 待清理文件
    test_files: List[FileInfo] = field(default_factory=list)
    obsolete_files: List[FileInfo] = field(default_factory=list)
    unused_resources: List[FileInfo] = field(default_factory=list)
    backup_files: List[FileInfo] = field(default_factory=list)
    temp_files: List[FileInfo] = field(default_factory=list)
    log_files: List[FileInfo] = field(default_factory=list)
    
    # 待清理目录
    test_directories: List[DirectoryInfo] = field(default_factory=list)
    empty_directories: List[DirectoryInfo] = field(default_factory=list)
    
    # 依赖分析
    unused_dependencies: List[DependencyInfo] = field(default_factory=list)
    
    # 统计信息
    stats: ProjectStats = field(default_factory=ProjectStats)
    
    # 风险评估
    high_risk_files: List[FileInfo] = field(default_factory=list)
    warnings: List[str] = field(default_factory=list)
    
    def get_all_cleanable_files(self) -> List[FileInfo]:
        """获取所有可清理的文件"""
        return (
            self.test_files +
            self.obsolete_files +
            self.unused_resources +
            self.backup_files +
            self.temp_files +
            self.log_files
        )
    
    def get_total_cleanable_size(self) -> int:
        """获取可清理文件的总大小"""
        return sum(file.size for file in self.get_all_cleanable_files())
    
    def get_total_cleanable_count(self) -> int:
        """获取可清理文件的总数量"""
        return len(self.get_all_cleanable_files())
    
    def get_files_by_type(self, file_type: FileType) -> List[FileInfo]:
        """根据类型获取文件列表"""
        type_mapping = {
            FileType.TEST_FILE: self.test_files,
            FileType.OBSOLETE_FILE: self.obsolete_files,
            FileType.UNUSED_RESOURCE: self.unused_resources,
            FileType.BACKUP_FILE: self.backup_files,
            FileType.TEMP_FILE: self.temp_files,
            FileType.LOG_FILE: self.log_files,
        }
        return type_mapping.get(file_type, [])
    
    def add_warning(self, message: str):
        """添加警告信息"""
        if message not in self.warnings:
            self.warnings.append(message)
    
    def update_stats(self):
        """更新统计信息"""
        all_files = self.get_all_cleanable_files()
        
        self.stats.total_files = len(all_files)
        self.stats.total_size = sum(file.size for file in all_files)
        
        self.stats.test_files_count = len(self.test_files)
        self.stats.test_files_size = sum(file.size for file in self.test_files)
        
        self.stats.obsolete_files_count = len(self.obsolete_files)
        self.stats.obsolete_files_size = sum(file.size for file in self.obsolete_files)
        
        self.stats.unused_resources_count = len(self.unused_resources)
        self.stats.unused_resources_size = sum(file.size for file in self.unused_resources)
        
        self.stats.unused_dependencies_count = len(self.unused_dependencies)
        self.stats.modules_count = len(self.modules)


@dataclass
class CleanupResult:
    """清理结果"""
    scan_result: ScanResult
    execution_time: float
    
    # 清理统计
    deleted_files: List[FileInfo] = field(default_factory=list)
    deleted_directories: List[DirectoryInfo] = field(default_factory=list)
    removed_dependencies: List[DependencyInfo] = field(default_factory=list)
    
    # 操作结果
    success_count: int = 0
    failed_count: int = 0
    skipped_count: int = 0
    
    # 错误信息
    errors: List[str] = field(default_factory=list)
    
    # 备份信息
    backup_created: bool = False
    backup_path: Optional[Path] = None
    
    def get_total_freed_size(self) -> int:
        """获取释放的总空间大小"""
        file_size = sum(file.size for file in self.deleted_files)
        dir_size = sum(dir.size for dir in self.deleted_directories)
        return file_size + dir_size
    
    def get_total_deleted_count(self) -> int:
        """获取删除的总文件数量"""
        return len(self.deleted_files) + sum(dir.file_count for dir in self.deleted_directories)
    
    def add_error(self, message: str):
        """添加错误信息"""
        if message not in self.errors:
            self.errors.append(message)
    
    def is_successful(self) -> bool:
        """检查清理是否成功"""
        return self.failed_count == 0 and len(self.errors) == 0