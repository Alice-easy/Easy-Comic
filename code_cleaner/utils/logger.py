# -*- coding: utf-8 -*-
"""
日志管理模块
"""

import logging
import sys
from pathlib import Path
from datetime import datetime
from typing import Optional


class ColoredFormatter(logging.Formatter):
    """彩色日志格式化器"""
    
    # ANSI颜色代码
    COLORS = {
        'DEBUG': '\033[36m',    # 青色
        'INFO': '\033[32m',     # 绿色
        'WARNING': '\033[33m',  # 黄色
        'ERROR': '\033[31m',    # 红色
        'CRITICAL': '\033[35m', # 紫色
        'RESET': '\033[0m'      # 重置
    }
    
    def format(self, record):
        """格式化日志记录"""
        # 添加颜色
        if record.levelname in self.COLORS:
            record.levelname = (
                f"{self.COLORS[record.levelname]}"
                f"{record.levelname}"
                f"{self.COLORS['RESET']}"
            )
        
        return super().format(record)


def setup_logger(name: str = "code_cleaner", verbose: bool = False) -> logging.Logger:
    """
    设置日志记录器
    
    Args:
        name: 日志记录器名称
        verbose: 是否启用详细模式
        
    Returns:
        配置好的日志记录器
    """
    logger = logging.getLogger(name)
    
    # 避免重复添加处理器
    if logger.handlers:
        return logger
    
    # 设置日志级别
    level = logging.DEBUG if verbose else logging.INFO
    logger.setLevel(level)
    
    # 创建控制台处理器
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(level)
    
    # 设置格式
    if verbose:
        formatter = ColoredFormatter(
            '%(asctime)s - %(name)s - %(levelname)s - %(filename)s:%(lineno)d - %(message)s'
        )
    else:
        formatter = ColoredFormatter(
            '%(levelname)s: %(message)s'
        )
    
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)
    
    # 创建文件处理器（可选）
    log_dir = Path("logs")
    if not log_dir.exists():
        log_dir.mkdir(exist_ok=True)
    
    log_file = log_dir / f"cleanup_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"
    file_handler = logging.FileHandler(log_file, encoding='utf-8')
    file_handler.setLevel(logging.DEBUG)
    
    file_formatter = logging.Formatter(
        '%(asctime)s - %(name)s - %(levelname)s - %(filename)s:%(lineno)d - %(message)s'
    )
    file_handler.setFormatter(file_formatter)
    logger.addHandler(file_handler)
    
    return logger


class ProgressLogger:
    """进度日志记录器"""
    
    def __init__(self, logger: logging.Logger, total: int, prefix: str = "进度"):
        """
        初始化进度记录器
        
        Args:
            logger: 日志记录器
            total: 总数
            prefix: 前缀文本
        """
        self.logger = logger
        self.total = total
        self.prefix = prefix
        self.current = 0
        self.last_percent = -1
    
    def update(self, increment: int = 1, message: str = ""):
        """
        更新进度
        
        Args:
            increment: 增量
            message: 附加消息
        """
        self.current += increment
        percent = int((self.current / self.total) * 100)
        
        # 只在百分比变化时输出
        if percent != self.last_percent:
            progress_bar = self._create_progress_bar(percent)
            msg = f"{self.prefix}: {progress_bar} {percent}%"
            if message:
                msg += f" - {message}"
            
            self.logger.info(msg)
            self.last_percent = percent
    
    def _create_progress_bar(self, percent: int, width: int = 30) -> str:
        """
        创建进度条
        
        Args:
            percent: 百分比
            width: 进度条宽度
            
        Returns:
            进度条字符串
        """
        filled = int(width * percent / 100)
        bar = '█' * filled + '░' * (width - filled)
        return f"[{bar}]"
    
    def finish(self, message: str = "完成"):
        """
        完成进度
        
        Args:
            message: 完成消息
        """
        self.current = self.total
        progress_bar = self._create_progress_bar(100)
        self.logger.info(f"{self.prefix}: {progress_bar} 100% - {message}")


def log_operation(operation_name: str):
    """
    操作日志装饰器
    
    Args:
        operation_name: 操作名称
    """
    def decorator(func):
        def wrapper(*args, **kwargs):
            logger = logging.getLogger("code_cleaner")
            logger.info(f"开始执行: {operation_name}")
            
            try:
                result = func(*args, **kwargs)
                logger.info(f"完成执行: {operation_name}")
                return result
            except Exception as e:
                logger.error(f"执行失败: {operation_name} - {e}")
                raise
                
        return wrapper
    return decorator