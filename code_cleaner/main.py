#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Android项目代码瘦身优化工具
主入口文件
"""

import sys
import os
import argparse
from pathlib import Path
from typing import Optional

from core.project_scanner import ProjectScanner
from core.cleaner_engine import CleanerEngine
from ui.cli_interface import CLIInterface
from utils.logger import setup_logger
from config.settings import Config


def main():
    """主函数"""
    parser = argparse.ArgumentParser(
        description="Android项目代码瘦身优化工具",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
使用示例:
  python main.py --project-path /path/to/android/project
  python main.py --project-path . --dry-run
  python main.py --config custom_config.json
        """
    )
    
    parser.add_argument(
        "--project-path", "-p",
        type=str,
        default=".",
        help="Android项目根目录路径 (默认: 当前目录)"
    )
    
    parser.add_argument(
        "--config", "-c",
        type=str,
        help="自定义配置文件路径"
    )
    
    parser.add_argument(
        "--dry-run", "-d",
        action="store_true",
        help="仅分析不执行删除操作"
    )
    
    parser.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="详细输出模式"
    )
    
    parser.add_argument(
        "--output", "-o",
        type=str,
        default="cleanup_report.html",
        help="清理报告输出文件名 (默认: cleanup_report.html)"
    )
    
    args = parser.parse_args()
    
    # 设置日志
    logger = setup_logger(verbose=args.verbose)
    
    try:
        # 验证项目路径
        project_path = Path(args.project_path).resolve()
        if not project_path.exists():
            logger.error(f"项目路径不存在: {project_path}")
            sys.exit(1)
            
        # 加载配置
        config = Config(args.config)
        
        # 初始化CLI界面
        cli = CLIInterface()
        cli.show_welcome()
        
        # 确认项目路径
        if not cli.confirm_project_path(str(project_path)):
            logger.info("用户取消操作")
            sys.exit(0)
            
        # 初始化项目扫描器
        scanner = ProjectScanner(project_path, config)
        
        # 扫描项目
        cli.show_scanning_progress()
        scan_result = scanner.scan_project()
        
        # 显示扫描结果
        cli.show_scan_results(scan_result)
        
        # 用户确认清理操作
        if not args.dry_run and not cli.confirm_cleanup(scan_result):
            logger.info("用户取消清理操作")
            sys.exit(0)
            
        # 执行清理
        cleaner = CleanerEngine(project_path, config)
        
        if args.dry_run:
            logger.info("干运行模式，不执行实际删除操作")
            cleanup_result = cleaner.dry_run(scan_result)
        else:
            cli.show_cleanup_progress()
            cleanup_result = cleaner.execute_cleanup(scan_result)
            
        # 生成报告
        report_path = project_path / args.output
        cleaner.generate_report(cleanup_result, report_path)
        
        # 显示完成信息
        cli.show_completion(cleanup_result, report_path)
        
    except KeyboardInterrupt:
        logger.info("用户中断操作")
        sys.exit(0)
    except Exception as e:
        logger.error(f"执行过程中发生错误: {e}")
        if args.verbose:
            import traceback
            traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()