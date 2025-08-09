# Active Context — Phase 2

当前目标清单（DoD 与进度位）

- 文件解析器（ZIP/CBZ、RAR/CBR、自然序、编码兼容、封面、流式单页、≥2GB）：DoD = 全部样本通过；状态 = Pending。
- 阅读器（双指缩放、点击/滑动翻页、进度保存防抖 300ms、书签唯一）：DoD = 交互流畅度达标；状态 = Pending。
- 书架（2–4 列、自适应、搜索筛选、SAF 导入/目录扫描）：DoD = 查询 P95 < 500ms；状态 = Pending。
- 非功能（冷启动 < 2s、翻页 P95 < 100ms、内存控制）：DoD = 监控指标稳定；状态 = Pending。

决策记录

1) 解析器分层
- 抽象：ArchiveParser 接口，ZipCbzParser / RarCbrParser 引擎可插拔（按文件签名与扩展名选择）。
- 边界：解析层不触达 UI/DB，仅暴露条目元数据与按需 InputStream。

2) 流式读取
- 策略：单页按需读取；顺序预取下一页；禁止全量解压；大文件分块缓冲。
- 跳转：支持自然序快速跳转，必要时重建归档句柄以控内存。

3) 唯一约束
- Room 索引：History 唯一(mangaId) UPSERT；Bookmark 唯一(mangaId,page) UPSERT；必要索引覆盖查询。

4) 封面缓存策略
- 提取：首张有效图片或显式封面；失败回退次序。
- 缓存：交由 Coil 内存/磁盘缓存；Key 组合(mangaId, entryPath, mtime/hash)；不持久化大图至 DB，仅存元信息。

风险与缓解

- 超大归档（≥2GB）：仅流式 + 分块；限制并发；预读窗口可配置。
- 损坏文件：解析失败标记并跳过；书架展示占位图；记录告警。
- 编码问题（UTF-8/CP437）：启发式 + 手动回退；无法判定时使用替代字符。
- EXIF 方向与大图：读取 EXIF 旋转；区域解码；RGB_565 降内存。
- 内存峰值：严格避免 ByteArray 聚合；使用流式管道与池化缓冲。
- RAR 引擎许可：Phase 2 先用可接受的开源实现，占位封装；Phase 3 决策商用许可。

开放问题

- RAR 引擎选型与许可方案。
- 目录扫描深度与频率（首次/增量/手动触发）。
- 标签存储与索引方案（多值、查询性能、迁移）。

更新日志

- [2025-08-08 21:36:40] 初始化 Active Context（Phase 2）。
* [2025-08-08 22:06:50] 已补齐 domain 接口与模型：新增 [MangaProbe.kt](app/src/main/java/com/easycomic/domain/model/MangaProbe.kt)、[Bookmark.kt](app/src/main/java/com/easycomic/domain/model/Bookmark.kt)、[ReadingHistory.kt](app/src/main/java/com/easycomic/domain/model/ReadingHistory.kt)，以及 [FileRepository.kt](app/src/main/java/com/easycomic/domain/repository/FileRepository.kt)、[BookmarkRepository.kt](app/src/main/java/com/easycomic/domain/repository/BookmarkRepository.kt)、[HistoryRepository.kt](app/src/main/java/com/easycomic/domain/repository/HistoryRepository.kt)。边界：纯 Kotlin（无 Android 依赖）、字段与 DAO 对齐、进度仍由 [MangaRepository.updateProgress()](app/src/main/java/com/easycomic/domain/repository/MangaRepository.kt:12) 管理。
* [2025-08-08 22:30:30] - 完成 domain 去重与模型补齐：新增 [kotlin.data class ReadingHistory()](app/src/main/java/com/easycomic/domain/model/ReadingHistory.kt:11)；删除 [kotlin.interface HistoryRepository()](app/src/main/java/com/easycomic/domain/repository/Repositories.kt:51) 的重复定义，保留专用文件 [kotlin.interface HistoryRepository()](app/src/main/java/com/easycomic/domain/repository/HistoryRepository.kt:14)；当前 [Repositories.kt](app/src/main/java/com/easycomic/domain/repository/Repositories.kt) 仅包含 FileRepository 与 BookmarkRepository。