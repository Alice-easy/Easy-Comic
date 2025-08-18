# -*- coding: utf-8 -*-
"""
配置管理模块
"""

import json
import os
from pathlib import Path
from typing import Dict, List, Any, Optional


class Config:
    """配置管理类"""
    
    DEFAULT_CONFIG = {
        "test_directories": [
            "src/test",
            "src/androidTest",
            "test",
            "androidTest",
            "src/testDebug",
            "src/testRelease"
        ],
        "test_file_patterns": [
            "*Test.kt",
            "*Test.java",
            "*Tests.kt",
            "*Tests.java",
            "Test*.kt",
            "Test*.java",
            "*Spec.kt",
            "*Spec.java"
        ],
        "obsolete_file_patterns": [
            "*.tmp",
            "*.bak",
            "*.old",
            "*.backup",
            ".DS_Store",
            "Thumbs.db",
            "*.log",
            "*.cache"
        ],
        "unused_resource_patterns": [
            "res/drawable-*dpi/ic_launcher_background.xml",
            "res/drawable/ic_launcher_foreground.xml",
            "res/mipmap-*dpi/ic_launcher.png",
            "res/mipmap-*dpi/ic_launcher_round.png"
        ],
        "protected_directories": [
            "src/main",
            ".git",
            ".gradle",
            "gradle",
            "build"
        ],
        "protected_files": [
            "build.gradle",
            "build.gradle.kts",
            "settings.gradle",
            "settings.gradle.kts",
            "gradle.properties",
            "gradlew",
            "gradlew.bat",
            "AndroidManifest.xml",
            "proguard-rules.pro",
            "README.md",
            "LICENSE"
        ],
        "dependency_analysis": {
            "check_unused_dependencies": True,
            "exclude_dependencies": [
                "androidx.core:core-ktx",
                "androidx.appcompat:appcompat",
                "com.google.android.material:material"
            ]
        },
        "safety_checks": {
            "require_confirmation": True,
            "create_backup": True,
            "backup_directory": ".cleanup_backup",
            "max_files_per_operation": 1000
        },
        "reporting": {
            "generate_html_report": True,
            "include_file_sizes": True,
            "include_dependency_tree": True,
            "include_before_after_comparison": True
        }
    }
    
    def __init__(self, config_file: Optional[str] = None):
        """
        初始化配置
        
        Args:
            config_file: 自定义配置文件路径
        """
        self.config = self.DEFAULT_CONFIG.copy()
        
        if config_file and os.path.exists(config_file):
            self.load_config_file(config_file)
    
    def load_config_file(self, config_file: str):
        """
        加载配置文件
        
        Args:
            config_file: 配置文件路径
        """
        try:
            with open(config_file, 'r', encoding='utf-8') as f:
                custom_config = json.load(f)
                self._merge_config(custom_config)
        except Exception as e:
            raise ValueError(f"加载配置文件失败: {e}")
    
    def _merge_config(self, custom_config: Dict[str, Any]):
        """
        合并自定义配置
        
        Args:
            custom_config: 自定义配置字典
        """
        for key, value in custom_config.items():
            if key in self.config:
                if isinstance(self.config[key], dict) and isinstance(value, dict):
                    self.config[key].update(value)
                else:
                    self.config[key] = value
            else:
                self.config[key] = value
    
    def get(self, key: str, default: Any = None) -> Any:
        """
        获取配置值
        
        Args:
            key: 配置键，支持点号分隔的嵌套键
            default: 默认值
            
        Returns:
            配置值
        """
        keys = key.split('.')
        value = self.config
        
        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default
                
        return value
    
    def get_test_directories(self) -> List[str]:
        """获取测试目录列表"""
        return self.get("test_directories", [])
    
    def get_test_file_patterns(self) -> List[str]:
        """获取测试文件模式列表"""
        return self.get("test_file_patterns", [])
    
    def get_obsolete_file_patterns(self) -> List[str]:
        """获取过期文件模式列表"""
        return self.get("obsolete_file_patterns", [])
    
    def get_protected_directories(self) -> List[str]:
        """获取受保护目录列表"""
        return self.get("protected_directories", [])
    
    def get_protected_files(self) -> List[str]:
        """获取受保护文件列表"""
        return self.get("protected_files", [])
    
    def is_safety_enabled(self) -> bool:
        """检查是否启用安全检查"""
        return self.get("safety_checks.require_confirmation", True)
    
    def should_create_backup(self) -> bool:
        """检查是否应该创建备份"""
        return self.get("safety_checks.create_backup", True)
    
    def get_backup_directory(self) -> str:
        """获取备份目录名"""
        return self.get("safety_checks.backup_directory", ".cleanup_backup")
    
    def save_config(self, file_path: str):
        """
        保存当前配置到文件
        
        Args:
            file_path: 配置文件保存路径
        """
        try:
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(self.config, f, indent=2, ensure_ascii=False)
        except Exception as e:
            raise ValueError(f"保存配置文件失败: {e}")