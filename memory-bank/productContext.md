# Product Context — Phase 2

愿景与范围（仅第二阶段）

- 文件解析：支持 ZIP/CBZ、RAR/CBR；自然序排序；编码/路径兼容（UTF-8/CP437 启发式）；封面提取；流式单页读取；可读 ≥2GB 归档（不全量解压）。
- 阅读器：Compose 基础 UI；双指缩放；点击/滑动翻页；阅读进度保存（300ms 防抖）；书签唯一（mangaId,page）。
- 书架：自适应 2–4 列；搜索/筛选（标题/作者/标签/格式/状态/评分）；SAF 导入与可选目录扫描。

架构剖面与模块图（文字版）

- 分层：presentation → domain → data（含 fileparser、repository、database、utils、image）。
- 模块：
  - presentation: Jetpack Compose 层（Reader、Bookshelf、Navigation）。
  - domain: 用例聚合（ScanAndImportManga、OpenManga、SaveReadingProgress、ToggleBookmark、QueryMangaList）。
  - data: Repository 实现（MangaRepositoryImpl、BookmarkRepositoryImpl、HistoryRepositoryImpl、FileRepositoryImpl）。
  - fileparser: ArchiveParser 接口 + ZipCbzParser / RarCbrParser（引擎可插拔），ImageSorter、CoverExtractor。
  - database: Room（History UPSERT 唯一 mangaId；Bookmark UPSERT 唯一 mangaId+page；必要索引与 DAO）。
  - utils: NaturalComparator、ZipList、FileUtils、ProbeBuilder、ImageUtils。
  - image: Coil 内存 RGB_565 + 区域解码 + 磁盘缓存。
- 依赖约束：presentation 仅依赖 domain；domain 仅依赖接口；data 依赖 fileparser/Room/Coil/SAF/utils。

KPI / 性能门槛

- 冷启动 < 2s（P50）；主线程首帧 < 500ms；IO 初始化后台化。
- 翻页 P95 < 100ms（同页缓存命中）；未命中 < 300ms（区域解码）。
- 搜索 < 500ms（本地索引 + Flow 去抖）。
- 内存：RGB_565，按需区域解码；图片缓存由 Coil 控制（内存/磁盘双层）。

术语表

- ArchiveParser：归档格式解析抽象，支持 ZIP/CBZ 与 RAR/CBR。
- SAF：Storage Access Framework，Android 官方存储访问框架。
- 自然序排序：按人类直观序号排序（Image 2 < Image 10）。
- 封面：首张有效图片或显式标记文件，用于书架缩略图。
- 进度：用户阅读至某页的状态（带时间戳）。
- 书签唯一性：同一漫画同一页仅一个书签（UPSERT）。
- ChangeLog：跨阶段同步所需的变更日志（PROGRESS/BOOKMARK）。

约束 / 边界

- 离线优先；不引入网络与私密信息。
- 兼容 ≥2GB 归档，严格采用流式读取，禁止全量解压到磁盘。
- 路径与编码兼容：UTF-8/CP437 启发；遇异常文件名以替代字符回退并记录告警。
- EXIF 方向与大图：读取 EXIF 旋转，采用区域解码以控峰值内存。
- RAR 引擎许可与体积需评估（Phase 2 可集成开源引擎占位，Phase 3 评审）。
- DI：Hilt 统一装配；解析器引擎选择策略可配置（扩展至 Phase 3）。

更新日志

- [2025-08-08 21:32:10] 初始化 Phase 2 产品上下文。