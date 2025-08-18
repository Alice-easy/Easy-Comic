# -*- coding: utf-8 -*-
"""
清理执行引擎模块
"""

import os
import shutil
import time
import logging
from pathlib import Path
from typing import List, Dict, Optional, Tuple
import json
from datetime import datetime

from models.scan_result import ScanResult, CleanupResult, FileInfo, DirectoryInfo, DependencyInfo
from config.settings import Config
from utils.logger import log_operation, ProgressLogger


class CleanerEngine:
    """清理执行引擎"""
    
    def __init__(self, project_path: Path, config: Config):
        """
        初始化清理引擎
        
        Args:
            project_path: 项目根路径
            config: 配置对象
        """
        self.project_path = project_path
        self.config = config
        self.logger = logging.getLogger("code_cleaner")
        
        # 安全检查配置
        self.require_confirmation = config.is_safety_enabled()
        self.create_backup = config.should_create_backup()
        self.backup_dir = config.get_backup_directory()
        self.max_files_per_operation = config.get("safety_checks.max_files_per_operation", 1000)
    
    @log_operation("执行清理操作")
    def execute_cleanup(self, scan_result: ScanResult) -> CleanupResult:
        """
        执行清理操作
        
        Args:
            scan_result: 扫描结果
            
        Returns:
            清理结果
        """
        start_time = time.time()
        
        # 创建清理结果对象
        cleanup_result = CleanupResult(
            scan_result=scan_result,
            execution_time=0
        )
        
        try:
            # 安全检查
            self._perform_safety_checks(scan_result, cleanup_result)
            
            # 创建备份
            if self.create_backup:
                self._create_backup(scan_result, cleanup_result)
            
            # 执行清理操作
            self._execute_file_cleanup(scan_result, cleanup_result)
            self._execute_directory_cleanup(scan_result, cleanup_result)
            self._execute_dependency_cleanup(scan_result, cleanup_result)
            
            # 记录执行时间
            cleanup_result.execution_time = time.time() - start_time
            
            self.logger.info(f"清理操作完成，耗时 {cleanup_result.execution_time:.2f} 秒")
            self._log_cleanup_summary(cleanup_result)
            
            return cleanup_result
            
        except Exception as e:
            cleanup_result.add_error(f"清理操作失败: {e}")
            cleanup_result.execution_time = time.time() - start_time
            self.logger.error(f"清理操作失败: {e}")
            raise
    
    @log_operation("干运行模式")
    def dry_run(self, scan_result: ScanResult) -> CleanupResult:
        """
        干运行模式，仅模拟清理操作
        
        Args:
            scan_result: 扫描结果
            
        Returns:
            清理结果（模拟）
        """
        start_time = time.time()
        
        cleanup_result = CleanupResult(
            scan_result=scan_result,
            execution_time=0
        )
        
        # 模拟文件删除
        all_files = scan_result.get_all_cleanable_files()
        for file_info in all_files:
            if file_info.path.exists():
                cleanup_result.deleted_files.append(file_info)
                cleanup_result.success_count += 1
            else:
                cleanup_result.skipped_count += 1
        
        # 模拟目录删除
        for dir_info in scan_result.test_directories + scan_result.empty_directories:
            if dir_info.path.exists():
                cleanup_result.deleted_directories.append(dir_info)
                cleanup_result.success_count += 1
            else:
                cleanup_result.skipped_count += 1
        
        # 模拟依赖清理
        cleanup_result.removed_dependencies = scan_result.unused_dependencies.copy()
        
        cleanup_result.execution_time = time.time() - start_time
        
        self.logger.info("干运行模式完成，未执行实际删除操作")
        self._log_cleanup_summary(cleanup_result)
        
        return cleanup_result
    
    def _perform_safety_checks(self, scan_result: ScanResult, cleanup_result: CleanupResult):
        """执行安全检查"""
        total_files = scan_result.get_total_cleanable_count()
        
        # 检查文件数量限制
        if total_files > self.max_files_per_operation:
            error_msg = f"待删除文件数量 ({total_files}) 超过安全阈值 ({self.max_files_per_operation})"
            cleanup_result.add_error(error_msg)
            raise ValueError(error_msg)
        
        # 检查高风险文件
        if scan_result.high_risk_files:
            self.logger.warning(f"发现 {len(scan_result.high_risk_files)} 个高风险文件")
            for risk_file in scan_result.high_risk_files:
                self.logger.warning(f"  高风险文件: {risk_file.path}")
        
        # 检查项目完整性
        self._check_project_integrity(scan_result, cleanup_result)
    
    def _check_project_integrity(self, scan_result: ScanResult, cleanup_result: CleanupResult):
        """检查项目完整性"""
        # 检查是否会删除重要的构建文件
        important_files = ["build.gradle.kts", "build.gradle", "settings.gradle.kts", "settings.gradle"]
        
        for file_info in scan_result.get_all_cleanable_files():
            if file_info.path.name in important_files:
                error_msg = f"尝试删除重要构建文件: {file_info.path}"
                cleanup_result.add_error(error_msg)
                self.logger.error(error_msg)
        
        # 检查是否会删除整个src/main目录
        for dir_info in scan_result.test_directories:
            if "src/main" in str(dir_info.path):
                error_msg = f"尝试删除主要源码目录: {dir_info.path}"
                cleanup_result.add_error(error_msg)
                self.logger.error(error_msg)
    
    def _create_backup(self, scan_result: ScanResult, cleanup_result: CleanupResult):
        """创建备份"""
        try:
            backup_path = self.project_path / self.backup_dir
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            backup_path = backup_path / f"backup_{timestamp}"
            
            if backup_path.exists():
                shutil.rmtree(backup_path)
            
            backup_path.mkdir(parents=True, exist_ok=True)
            
            self.logger.info(f"创建备份到: {backup_path}")
            
            # 备份待删除的文件
            backup_count = 0
            all_files = scan_result.get_all_cleanable_files()
            
            progress = ProgressLogger(self.logger, len(all_files), "备份文件")
            
            for file_info in all_files:
                if file_info.path.exists():
                    try:
                        # 计算相对路径
                        rel_path = file_info.path.relative_to(self.project_path)
                        backup_file_path = backup_path / rel_path
                        
                        # 创建目录
                        backup_file_path.parent.mkdir(parents=True, exist_ok=True)
                        
                        # 复制文件
                        shutil.copy2(file_info.path, backup_file_path)
                        backup_count += 1
                        
                        progress.update(1, f"备份: {file_info.path.name}")
                        
                    except Exception as e:
                        self.logger.warning(f"备份文件失败 {file_info.path}: {e}")
                        progress.update(1)
            
            progress.finish(f"备份完成，共备份 {backup_count} 个文件")
            
            # 备份目录结构信息
            self._save_backup_manifest(backup_path, scan_result)
            
            cleanup_result.backup_created = True
            cleanup_result.backup_path = backup_path
            
        except Exception as e:
            error_msg = f"创建备份失败: {e}"
            cleanup_result.add_error(error_msg)
            self.logger.error(error_msg)
    
    def _save_backup_manifest(self, backup_path: Path, scan_result: ScanResult):
        """保存备份清单"""
        try:
            manifest = {
                "backup_time": datetime.now().isoformat(),
                "project_path": str(self.project_path),
                "total_files": scan_result.get_total_cleanable_count(),
                "total_size": scan_result.get_total_cleanable_size(),
                "files": []
            }
            
            for file_info in scan_result.get_all_cleanable_files():
                manifest["files"].append({
                    "path": str(file_info.path.relative_to(self.project_path)),
                    "size": file_info.size,
                    "type": file_info.file_type.value,
                    "reason": file_info.reason
                })
            
            manifest_file = backup_path / "backup_manifest.json"
            with open(manifest_file, 'w', encoding='utf-8') as f:
                json.dump(manifest, f, indent=2, ensure_ascii=False)
                
        except Exception as e:
            self.logger.warning(f"保存备份清单失败: {e}")
    
    def _execute_file_cleanup(self, scan_result: ScanResult, cleanup_result: CleanupResult):
        """执行文件清理"""
        all_files = scan_result.get_all_cleanable_files()
        
        if not all_files:
            self.logger.info("没有需要清理的文件")
            return
        
        self.logger.info(f"开始清理 {len(all_files)} 个文件")
        progress = ProgressLogger(self.logger, len(all_files), "清理文件")
        
        for file_info in all_files:
            try:
                if file_info.path.exists():
                    # 删除文件
                    file_info.path.unlink()
                    cleanup_result.deleted_files.append(file_info)
                    cleanup_result.success_count += 1
                    
                    progress.update(1, f"删除: {file_info.path.name}")
                else:
                    cleanup_result.skipped_count += 1
                    progress.update(1, f"跳过: {file_info.path.name} (不存在)")
                    
            except Exception as e:
                error_msg = f"删除文件失败 {file_info.path}: {e}"
                cleanup_result.add_error(error_msg)
                cleanup_result.failed_count += 1
                self.logger.error(error_msg)
                progress.update(1, f"失败: {file_info.path.name}")
        
        progress.finish("文件清理完成")
    
    def _execute_directory_cleanup(self, scan_result: ScanResult, cleanup_result: CleanupResult):
        """执行目录清理"""
        all_dirs = scan_result.test_directories + scan_result.empty_directories
        
        if not all_dirs:
            self.logger.info("没有需要清理的目录")
            return
        
        self.logger.info(f"开始清理 {len(all_dirs)} 个目录")
        progress = ProgressLogger(self.logger, len(all_dirs), "清理目录")
        
        for dir_info in all_dirs:
            try:
                if dir_info.path.exists() and dir_info.path.is_dir():
                    # 删除目录及其内容
                    shutil.rmtree(dir_info.path)
                    cleanup_result.deleted_directories.append(dir_info)
                    cleanup_result.success_count += 1
                    
                    progress.update(1, f"删除: {dir_info.path.name}")
                else:
                    cleanup_result.skipped_count += 1
                    progress.update(1, f"跳过: {dir_info.path.name} (不存在)")
                    
            except Exception as e:
                error_msg = f"删除目录失败 {dir_info.path}: {e}"
                cleanup_result.add_error(error_msg)
                cleanup_result.failed_count += 1
                self.logger.error(error_msg)
                progress.update(1, f"失败: {dir_info.path.name}")
        
        progress.finish("目录清理完成")
    
    def _execute_dependency_cleanup(self, scan_result: ScanResult, cleanup_result: CleanupResult):
        """执行依赖清理"""
        if not scan_result.unused_dependencies:
            self.logger.info("没有需要清理的依赖")
            return
        
        self.logger.info(f"开始清理 {len(scan_result.unused_dependencies)} 个未使用依赖")
        
        # 按模块分组依赖
        deps_by_module = {}
        for dep in scan_result.unused_dependencies:
            module = dep.module or "root"
            if module not in deps_by_module:
                deps_by_module[module] = []
            deps_by_module[module].append(dep)
        
        # 清理每个模块的依赖
        for module_name, deps in deps_by_module.items():
            try:
                self._clean_module_dependencies(module_name, deps, cleanup_result)
            except Exception as e:
                error_msg = f"清理模块 {module_name} 依赖失败: {e}"
                cleanup_result.add_error(error_msg)
                self.logger.error(error_msg)
    
    def _clean_module_dependencies(self, module_name: str, dependencies: List[DependencyInfo], 
                                 cleanup_result: CleanupResult):
        """清理模块依赖"""
        # 查找构建文件
        if module_name == "root":
            module_path = self.project_path
        else:
            module_path = self.project_path / module_name
        
        build_file = None
        for build_name in ["build.gradle.kts", "build.gradle"]:
            build_path = module_path / build_name
            if build_path.exists():
                build_file = build_path
                break
        
        if not build_file:
            self.logger.warning(f"未找到模块 {module_name} 的构建文件")
            return
        
        try:
            # 读取构建文件内容
            content = build_file.read_text(encoding='utf-8')
            original_content = content
            
            # 移除未使用的依赖
            for dep in dependencies:
                content = self._remove_dependency_from_content(content, dep)
                cleanup_result.removed_dependencies.append(dep)
                cleanup_result.success_count += 1
            
            # 如果内容有变化，写回文件
            if content != original_content:
                build_file.write_text(content, encoding='utf-8')
                self.logger.info(f"已更新构建文件: {build_file}")
            
        except Exception as e:
            error_msg = f"更新构建文件失败 {build_file}: {e}"
            cleanup_result.add_error(error_msg)
            cleanup_result.failed_count += len(dependencies)
            raise
    
    def _remove_dependency_from_content(self, content: str, dependency: DependencyInfo) -> str:
        """从构建文件内容中移除依赖"""
        # 构建依赖匹配模式
        dep_patterns = [
            # Kotlin DSL
            rf'implementation\s*\(\s*"{re.escape(dependency.name)}[^"]*"\s*\)\s*\n?',
            rf'api\s*\(\s*"{re.escape(dependency.name)}[^"]*"\s*\)\s*\n?',
            rf'testImplementation\s*\(\s*"{re.escape(dependency.name)}[^"]*"\s*\)\s*\n?',
            rf'androidTestImplementation\s*\(\s*"{re.escape(dependency.name)}[^"]*"\s*\)\s*\n?',
            
            # Groovy DSL
            rf"implementation\s+['\"]" + re.escape(dependency.name) + r"[^'\"]*['\"].*\n?",
            rf"api\s+['\"]" + re.escape(dependency.name) + r"[^'\"]*['\"].*\n?",
            rf"testImplementation\s+['\"]" + re.escape(dependency.name) + r"[^'\"]*['\"].*\n?",
            rf"androidTestImplementation\s+['\"]" + re.escape(dependency.name) + r"[^'\"]*['\"].*\n?",
        ]
        
        import re
        for pattern in dep_patterns:
            content = re.sub(pattern, '', content, flags=re.MULTILINE)
        
        return content
    
    def _log_cleanup_summary(self, cleanup_result: CleanupResult):
        """记录清理摘要"""
        self.logger.info("=" * 50)
        self.logger.info("清理操作摘要:")
        self.logger.info(f"  成功操作: {cleanup_result.success_count}")
        self.logger.info(f"  失败操作: {cleanup_result.failed_count}")
        self.logger.info(f"  跳过操作: {cleanup_result.skipped_count}")
        self.logger.info(f"  删除文件: {len(cleanup_result.deleted_files)} 个")
        self.logger.info(f"  删除目录: {len(cleanup_result.deleted_directories)} 个")
        self.logger.info(f"  移除依赖: {len(cleanup_result.removed_dependencies)} 个")
        
        freed_size = cleanup_result.get_total_freed_size()
        freed_mb = freed_size / (1024 * 1024)
        self.logger.info(f"  释放空间: {freed_mb:.2f} MB")
        
        if cleanup_result.backup_created:
            self.logger.info(f"  备份位置: {cleanup_result.backup_path}")
        
        if cleanup_result.errors:
            self.logger.error(f"  错误数量: {len(cleanup_result.errors)}")
            for error in cleanup_result.errors:
                self.logger.error(f"    - {error}")
        
        self.logger.info("=" * 50)
    
    def generate_report(self, cleanup_result: CleanupResult, report_path: Path):
        """
        生成清理报告
        
        Args:
            cleanup_result: 清理结果
            report_path: 报告文件路径
        """
        try:
            html_content = self._generate_html_report(cleanup_result)
            report_path.write_text(html_content, encoding='utf-8')
            self.logger.info(f"清理报告已生成: {report_path}")
        except Exception as e:
            self.logger.error(f"生成清理报告失败: {e}")
    
    def _generate_html_report(self, cleanup_result: CleanupResult) -> str:
        """生成HTML格式的清理报告"""
        scan_result = cleanup_result.scan_result
        
        html_template = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Android项目清理报告</title>
    <style>
        body {{ font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }}
        .container {{ max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }}
        h1 {{ color: #2c3e50; text-align: center; margin-bottom: 30px; }}
        h2 {{ color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 10px; }}
        .summary {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }}
        .summary-card {{ background: #ecf0f1; padding: 20px; border-radius: 8px; text-align: center; }}
        .summary-card h3 {{ margin: 0 0 10px 0; color: #2c3e50; }}
        .summary-card .value {{ font-size: 2em; font-weight: bold; color: #3498db; }}
        .file-list {{ margin: 20px 0; }}
        .file-item {{ background: #f8f9fa; margin: 5px 0; padding: 10px; border-left: 4px solid #3498db; }}
        .file-path {{ font-family: monospace; color: #2c3e50; }}
        .file-size {{ color: #7f8c8d; font-size: 0.9em; }}
        .error {{ color: #e74c3c; background: #fdf2f2; padding: 10px; border-left: 4px solid #e74c3c; margin: 5px 0; }}
        .success {{ color: #27ae60; }}
        .warning {{ color: #f39c12; }}
        table {{ width: 100%; border-collapse: collapse; margin: 20px 0; }}
        th, td {{ padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }}
        th {{ background-color: #3498db; color: white; }}
        .progress-bar {{ width: 100%; height: 20px; background-color: #ecf0f1; border-radius: 10px; overflow: hidden; }}
        .progress-fill {{ height: 100%; background-color: #3498db; transition: width 0.3s ease; }}
    </style>
</head>
<body>
    <div class="container">
        <h1>🧹 Android项目清理报告</h1>
        
        <div class="summary">
            <div class="summary-card">
                <h3>删除文件</h3>
                <div class="value">{deleted_files_count}</div>
            </div>
            <div class="summary-card">
                <h3>删除目录</h3>
                <div class="value">{deleted_dirs_count}</div>
            </div>
            <div class="summary-card">
                <h3>移除依赖</h3>
                <div class="value">{removed_deps_count}</div>
            </div>
            <div class="summary-card">
                <h3>释放空间</h3>
                <div class="value">{freed_space_mb:.1f} MB</div>
            </div>
        </div>
        
        <h2>📊 执行统计</h2>
        <table>
            <tr><th>项目</th><th>数量</th><th>状态</th></tr>
            <tr><td>成功操作</td><td>{success_count}</td><td class="success">✓</td></tr>
            <tr><td>失败操作</td><td>{failed_count}</td><td class="{'error' if cleanup_result.failed_count > 0 else 'success'}">{'✗' if cleanup_result.failed_count > 0 else '✓'}</td></tr>
            <tr><td>跳过操作</td><td>{skipped_count}</td><td class="warning">⚠</td></tr>
            <tr><td>执行时间</td><td>{execution_time:.2f} 秒</td><td class="success">⏱</td></tr>
        </table>
        
        {deleted_files_section}
        {deleted_dirs_section}
        {removed_deps_section}
        {errors_section}
        {backup_section}
        
        <div style="text-align: center; margin-top: 30px; color: #7f8c8d;">
            <p>报告生成时间: {report_time}</p>
            <p>项目路径: {project_path}</p>
        </div>
    </div>
</body>
</html>
        """
        
        # 生成各个部分的HTML
        deleted_files_section = self._generate_files_section("🗑️ 已删除文件", cleanup_result.deleted_files)
        deleted_dirs_section = self._generate_dirs_section("📁 已删除目录", cleanup_result.deleted_directories)
        removed_deps_section = self._generate_deps_section("📦 已移除依赖", cleanup_result.removed_dependencies)
        errors_section = self._generate_errors_section(cleanup_result.errors)
        backup_section = self._generate_backup_section(cleanup_result)
        
        return html_template.format(
            deleted_files_count=len(cleanup_result.deleted_files),
            deleted_dirs_count=len(cleanup_result.deleted_directories),
            removed_deps_count=len(cleanup_result.removed_dependencies),
            freed_space_mb=cleanup_result.get_total_freed_size() / (1024 * 1024),
            success_count=cleanup_result.success_count,
            failed_count=cleanup_result.failed_count,
            skipped_count=cleanup_result.skipped_count,
            execution_time=cleanup_result.execution_time,
            deleted_files_section=deleted_files_section,
            deleted_dirs_section=deleted_dirs_section,
            removed_deps_section=removed_deps_section,
            errors_section=errors_section,
            backup_section=backup_section,
            report_time=datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            project_path=self.project_path,
            cleanup_result=cleanup_result
        )
    
    def _generate_files_section(self, title: str, files: List[FileInfo]) -> str:
        """生成文件列表部分"""
        if not files:
            return f"<h2>{title}</h2><p>无文件被删除</p>"
        
        html = f"<h2>{title}</h2><div class='file-list'>"
        for file_info in files:
            size_mb = file_info.size / (1024 * 1024)
            html += f"""
            <div class='file-item'>
                <div class='file-path'>{file_info.path}</div>
                <div class='file-size'>大小: {size_mb:.2f} MB | 类型: {file_info.file_type.value} | 原因: {file_info.reason}</div>
            </div>
            """
        html += "</div>"
        return html
    
    def _generate_dirs_section(self, title: str, directories: List[DirectoryInfo]) -> str:
        """生成目录列表部分"""
        if not directories:
            return f"<h2>{title}</h2><p>无目录被删除</p>"
        
        html = f"<h2>{title}</h2><div class='file-list'>"
        for dir_info in directories:
            size_mb = dir_info.size / (1024 * 1024)
            html += f"""
            <div class='file-item'>
                <div class='file-path'>{dir_info.path}</div>
                <div class='file-size'>大小: {size_mb:.2f} MB | 文件数: {dir_info.file_count} | 类型: {dir_info.directory_type}</div>
            </div>
            """
        html += "</div>"
        return html
    
    def _generate_deps_section(self, title: str, dependencies: List[DependencyInfo]) -> str:
        """生成依赖列表部分"""
        if not dependencies:
            return f"<h2>{title}</h2><p>无依赖被移除</p>"
        
        html = f"<h2>{title}</h2><table><tr><th>依赖名称</th><th>版本</th><th>模块</th></tr>"
        for dep in dependencies:
            html += f"<tr><td>{dep.name}</td><td>{dep.version}</td><td>{dep.module}</td></tr>"
        html += "</table>"
        return html
    
    def _generate_errors_section(self, errors: List[str]) -> str:
        """生成错误列表部分"""
        if not errors:
            return ""
        
        html = "<h2>❌ 错误信息</h2>"
        for error in errors:
            html += f"<div class='error'>{error}</div>"
        return html
    
    def _generate_backup_section(self, cleanup_result: CleanupResult) -> str:
        """生成备份信息部分"""
        if not cleanup_result.backup_created:
            return ""
        
        return f"""
        <h2>💾 备份信息</h2>
        <div class='file-item'>
            <div class='file-path'>备份路径: {cleanup_result.backup_path}</div>
            <div class='file-size'>备份已创建，可用于恢复删除的文件</div>
        </div>
        """