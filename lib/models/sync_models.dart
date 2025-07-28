class SyncResult {
  SyncResult({
    required this.uploaded,
    required this.downloaded,
    required this.conflicts,
    required this.errors,
  });

  final int uploaded;
  final int downloaded;
  final int conflicts;
  final List<String> errors;

  @override
  String toString() =>
      'SyncResult(uploaded: $uploaded, downloaded: $downloaded, conflicts: $conflicts, errors: ${errors.length})';
}

class SyncDataItem {
  SyncDataItem({
    required this.fileHash,
    required this.currentPage,
    required this.totalPages,
    required this.updatedAt,
    this.localEtag,
    this.remoteEtag,
  });

  factory SyncDataItem.fromJson(Map<String, dynamic> json) => SyncDataItem(
    fileHash: json['fileHash'] as String,
    currentPage: json['currentPage'] as int,
    totalPages: json['totalPages'] as int,
    updatedAt: DateTime.parse(json['updatedAt'] as String),
  );

  final String fileHash;
  final int currentPage;
  final int totalPages;
  final DateTime updatedAt;
  final String? localEtag;
  final String? remoteEtag;

  Map<String, dynamic> toJson() => {
    'fileHash': fileHash,
    'currentPage': currentPage,
    'totalPages': totalPages,
    'updatedAt': updatedAt.toIso8601String(),
  };
}
