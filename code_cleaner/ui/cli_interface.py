# -*- coding: utf-8 -*-
"""
命令行界面模块
"""

import os
import sys
import time
from pathlib import Path
from typing import List, Dict, Optional, Any

from models.scan_result import ScanResult, CleanupResult, FileInfo, DirectoryInfo
from utils.logger import ProgressLogger
import logging


class CLIInterface:
    """命令行界面"""
    
    def __init__(self):
        """初始化CLI界面"""
        self.logger = logging.getLogger("code_cleaner")
        
        # ANSI颜色代码
        self.colors = {
            'HEADER': '\033[95m',
            'BLUE': '\033[94m',
            'CYAN': '\033[96m',
            'GREEN': '\033[92m',
            'YELLOW': '\033[93m',
            'RED': '\033[91m',
            'BOLD': '\033[1m',
            'UNDERLINE': '\033[4m',
            'RESET': '\033[0m'
        }
        
        # 检查是否支持颜色输出
        self.use_colors = self._supports_color()
    
    def _supports_color(self) -> bool:
        """检查终端是否支持颜色输出"""
        return (
            hasattr(sys.stdout, "isatty") and sys.stdout.isatty() and
            os.environ.get("TERM") != "dumb" and
            sys.platform != "win32"  # Windows CMD默认不支持ANSI颜色
        )
    
    def _colorize(self, text: str, color: str) -> str:
        """给文本添加颜色"""
        if not self.use_colors:
            return text
        return f"{self.colors.get(color, '')}{text}{self.colors['RESET']}"
    
    def show_welcome(self):
        """显示欢迎界面"""
        welcome_art = """
╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║    🧹 Android项目代码瘦身优化工具 v1.0                        ║
║                                                              ║
║    ✨ 智能清理测试文件、废弃代码和未使用依赖                    ║
║    🛡️  安全备份机制，支持一键恢复                             ║
║    📊 详细的清理报告和统计分析                                 ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
        """
        
        print(self._colorize(welcome_art, 'CYAN'))
        print()
    
    def confirm_project_path(self, project_path: str) -> bool:
        """
        确认项目路径
        
        Args:
            project_path: 项目路径
            
        Returns:
            用户确认结果
        """
        print(self._colorize("📁 项目信息", 'BOLD'))
        print(f"项目路径: {self._colorize(project_path, 'BLUE')}")
        print()
        
        # 显示项目基本信息
        path = Path(project_path)
        if path.exists():
            # 统计基本信息
            total_files = sum(1 for _ in path.rglob("*") if _.is_file())
            total_size = sum(f.stat().st_size for f in path.rglob("*") if f.is_file() and f.exists())
            size_mb = total_size / (1024 * 1024)
            
            print(f"总文件数: {self._colorize(str(total_files), 'GREEN')}")
            print(f"项目大小: {self._colorize(f'{size_mb:.1f} MB', 'GREEN')}")
            print()
        
        return self._ask_yes_no("确认要对此项目进行清理分析吗？", default=True)
    
    def show_scanning_progress(self):
        """显示扫描进度"""
        print(self._colorize("🔍 正在扫描项目...", 'YELLOW'))
        print()
        
        # 显示扫描动画
        self._show_spinner("扫描中", duration=2)
    
    def show_scan_results(self, scan_result: ScanResult):
        """
        显示扫描结果
        
        Args:
            scan_result: 扫描结果
        """
        print(self._colorize("📊 扫描结果", 'BOLD'))
        print("=" * 60)
        
        # 显示统计信息
        stats = scan_result.stats
        
        print(f"🔍 发现的模块数量: {self._colorize(str(stats.modules_count), 'BLUE')}")
        print()
        
        # 显示可清理文件统计
        self._show_file_stats("🗑️  测试文件", len(scan_result.test_files), 
                             sum(f.size for f in scan_result.test_files))
        
        self._show_file_stats("📄 过期文件", len(scan_result.obsolete_files), 
                             sum(f.size for f in scan_result.obsolete_files))
        
        self._show_file_stats("💾 备份文件", len(scan_result.backup_files), 
                             sum(f.size for f in scan_result.backup_files))
        
        self._show_file_stats("🗂️  临时文件", len(scan_result.temp_files), 
                             sum(f.size for f in scan_result.temp_files))
        
        self._show_file_stats("📝 日志文件", len(scan_result.log_files), 
                             sum(f.size for f in scan_result.log_files))
        
        self._show_file_stats("🖼️  未使用资源", len(scan_result.unused_resources), 
                             sum(f.size for f in scan_result.unused_resources))
        
        print()
        self._show_file_stats("📁 测试目录", len(scan_result.test_directories), 
                             sum(d.size for d in scan_result.test_directories))
        
        self._show_file_stats("📂 空目录", len(scan_result.empty_directories), 
                             sum(d.size for d in scan_result.empty_directories))
        
        print()
        print(f"📦 未使用依赖: {self._colorize(str(len(scan_result.unused_dependencies)), 'YELLOW')}")
        
        # 显示总计
        total_size = scan_result.get_total_cleanable_size()
        total_count = scan_result.get_total_cleanable_count()
        size_mb = total_size / (1024 * 1024)
        
        print()
        print("=" * 60)
        print(f"📊 总计: {self._colorize(str(total_count), 'BOLD')} 个项目")
        print(f"💾 可释放空间: {self._colorize(f'{size_mb:.1f} MB', 'BOLD')}")
        
        # 显示警告信息
        if scan_result.warnings:
            print()
            print(self._colorize("⚠️  警告信息:", 'YELLOW'))
            for warning in scan_result.warnings:
                print(f"  • {warning}")
        
        # 显示高风险文件
        if scan_result.high_risk_files:
            print()
            print(self._colorize("🚨 高风险文件:", 'RED'))
            for risk_file in scan_result.high_risk_files[:5]:  # 只显示前5个
                size_mb = risk_file.size / (1024 * 1024)
                print(f"  • {risk_file.path} ({size_mb:.1f} MB)")
            
            if len(scan_result.high_risk_files) > 5:
                print(f"  ... 还有 {len(scan_result.high_risk_files) - 5} 个高风险文件")
        
        print("=" * 60)
        print()
    
    def confirm_cleanup(self, scan_result: ScanResult) -> bool:
        """
        确认清理操作
        
        Args:
            scan_result: 扫描结果
            
        Returns:
            用户确认结果
        """
        total_count = scan_result.get_total_cleanable_count()
        total_size = scan_result.get_total_cleanable_size()
        size_mb = total_size / (1024 * 1024)
        
        print(self._colorize("🚀 准备执行清理操作", 'BOLD'))
        print()
        print(f"将要删除 {self._colorize(str(total_count), 'YELLOW')} 个项目")
        print(f"预计释放 {self._colorize(f'{size_mb:.1f} MB', 'YELLOW')} 空间")
        print()
        
        if scan_result.high_risk_files:
            print(self._colorize("⚠️  注意：发现高风险文件，请仔细确认！", 'RED'))
            print()
        
        print(self._colorize("💾 将自动创建备份，可用于恢复", 'GREEN'))
        print()
        
        return self._ask_yes_no("确认执行清理操作吗？", default=False)
    
    def show_cleanup_progress(self):
        """显示清理进度"""
        print(self._colorize("🧹 正在执行清理操作...", 'YELLOW'))
        print()
    
    def show_completion(self, cleanup_result: CleanupResult, report_path: Path):
        """
        显示完成信息
        
        Args:
            cleanup_result: 清理结果
            report_path: 报告文件路径
        """
        print()
        print(self._colorize("✅ 清理操作完成！", 'GREEN'))
        print("=" * 60)
        
        # 显示操作统计
        print(f"✅ 成功操作: {self._colorize(str(cleanup_result.success_count), 'GREEN')}")
        print(f"❌ 失败操作: {self._colorize(str(cleanup_result.failed_count), 'RED')}")
        print(f"⏭️  跳过操作: {self._colorize(str(cleanup_result.skipped_count), 'YELLOW')}")
        print()
        
        # 显示清理统计
        print(f"🗑️  删除文件: {self._colorize(str(len(cleanup_result.deleted_files)), 'BLUE')}")
        print(f"📁 删除目录: {self._colorize(str(len(cleanup_result.deleted_directories)), 'BLUE')}")
        print(f"📦 移除依赖: {self._colorize(str(len(cleanup_result.removed_dependencies)), 'BLUE')}")
        print()
        
        # 显示释放空间
        freed_size = cleanup_result.get_total_freed_size()
        freed_mb = freed_size / (1024 * 1024)
        print(f"💾 释放空间: {self._colorize(f'{freed_mb:.1f} MB', 'BOLD')}")
        
        # 显示执行时间
        print(f"⏱️  执行时间: {self._colorize(f'{cleanup_result.execution_time:.2f} 秒', 'CYAN')}")
        
        # 显示备份信息
        if cleanup_result.backup_created:
            print()
            print(f"💾 备份位置: {self._colorize(str(cleanup_result.backup_path), 'GREEN')}")
        
        # 显示报告信息
        print()
        print(f"📊 详细报告: {self._colorize(str(report_path), 'BLUE')}")
        
        # 显示错误信息
        if cleanup_result.errors:
            print()
            print(self._colorize("❌ 错误信息:", 'RED'))
            for error in cleanup_result.errors[:3]:  # 只显示前3个错误
                print(f"  • {error}")
            
            if len(cleanup_result.errors) > 3:
                print(f"  ... 还有 {len(cleanup_result.errors) - 3} 个错误，详见报告")
        
        print("=" * 60)
        
        # 显示成功提示
        if cleanup_result.is_successful():
            print(self._colorize("🎉 项目清理成功完成！", 'GREEN'))
        else:
            print(self._colorize("⚠️  清理过程中遇到一些问题，请查看详细报告", 'YELLOW'))
        
        print()
    
    def _show_file_stats(self, label: str, count: int, size: int):
        """显示文件统计信息"""
        if count > 0:
            size_mb = size / (1024 * 1024)
            print(f"{label}: {self._colorize(str(count), 'YELLOW')} 个 "
                  f"({self._colorize(f'{size_mb:.1f} MB', 'CYAN')})")
        else:
            print(f"{label}: {self._colorize('0', 'GREEN')} 个")
    
    def _ask_yes_no(self, question: str, default: bool = True) -> bool:
        """
        询问是/否问题
        
        Args:
            question: 问题文本
            default: 默认值
            
        Returns:
            用户选择结果
        """
        default_text = "Y/n" if default else "y/N"
        prompt = f"{question} [{default_text}]: "
        
        while True:
            try:
                response = input(self._colorize(prompt, 'BOLD')).strip().lower()
                
                if not response:
                    return default
                
                if response in ['y', 'yes', '是', 'y']:
                    return True
                elif response in ['n', 'no', '否', 'n']:
                    return False
                else:
                    print(self._colorize("请输入 y/yes 或 n/no", 'RED'))
                    
            except KeyboardInterrupt:
                print()
                print(self._colorize("操作已取消", 'YELLOW'))
                return False
            except EOFError:
                return default
    
    def _show_spinner(self, message: str, duration: float = 1.0):
        """
        显示旋转动画
        
        Args:
            message: 显示消息
            duration: 持续时间（秒）
        """
        if not self.use_colors:
            print(f"{message}...")
            time.sleep(duration)
            return
        
        spinner_chars = ['⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏']
        start_time = time.time()
        i = 0
        
        try:
            while time.time() - start_time < duration:
                char = spinner_chars[i % len(spinner_chars)]
                print(f"\r{self._colorize(char, 'CYAN')} {message}...", end='', flush=True)
                time.sleep(0.1)
                i += 1
            
            print(f"\r{self._colorize('✓', 'GREEN')} {message}完成")
            
        except KeyboardInterrupt:
            print(f"\r{self._colorize('✗', 'RED')} {message}已取消")
            raise
    
    def show_detailed_file_list(self, title: str, files: List[FileInfo], max_display: int = 10):
        """
        显示详细文件列表
        
        Args:
            title: 列表标题
            files: 文件列表
            max_display: 最大显示数量
        """
        if not files:
            return
        
        print(self._colorize(f"\n{title}:", 'BOLD'))
        print("-" * 50)
        
        for i, file_info in enumerate(files[:max_display]):
            size_mb = file_info.size / (1024 * 1024)
            print(f"{i+1:2d}. {file_info.path}")
            print(f"     大小: {size_mb:.2f} MB | 类型: {file_info.file_type.value}")
            print(f"     原因: {file_info.reason}")
            print()
        
        if len(files) > max_display:
            remaining = len(files) - max_display
            print(f"... 还有 {self._colorize(str(remaining), 'YELLOW')} 个文件")
        
        print("-" * 50)
    
    def show_module_info(self, scan_result: ScanResult):
        """
        显示模块信息
        
        Args:
            scan_result: 扫描结果
        """
        if not scan_result.modules:
            return
        
        print(self._colorize("\n📦 项目模块信息:", 'BOLD'))
        print("-" * 50)
        
        for module in scan_result.modules:
            print(f"模块: {self._colorize(module.name, 'BLUE')}")
            print(f"路径: {module.path}")
            
            if module.build_file:
                print(f"构建文件: {module.build_file.name}")
            
            if module.dependencies:
                print(f"依赖数量: {len(module.dependencies)}")
            
            test_dirs = len(module.test_directories)
            if test_dirs > 0:
                print(f"测试目录: {self._colorize(str(test_dirs), 'YELLOW')} 个")
            
            print()
        
        print("-" * 50)
    
    def ask_cleanup_options(self) -> Dict[str, bool]:
        """
        询问清理选项
        
        Returns:
            清理选项字典
        """
        print(self._colorize("🔧 清理选项配置:", 'BOLD'))
        print()
        
        options = {}
        
        options['clean_test_files'] = self._ask_yes_no(
            "清理测试文件？", default=True
        )
        
        options['clean_obsolete_files'] = self._ask_yes_no(
            "清理过期文件？", default=True
        )
        
        options['clean_unused_resources'] = self._ask_yes_no(
            "清理未使用资源？", default=True
        )
        
        options['clean_dependencies'] = self._ask_yes_no(
            "清理未使用依赖？", default=False
        )
        
        options['create_backup'] = self._ask_yes_no(
            "创建备份？", default=True
        )
        
        print()
        return options