import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/webdav_repository.dart';
import 'package:equatable/equatable.dart';

class DownloadAvatarUseCase implements UseCase<File, DownloadAvatarParams> {
  final IWebdavRepository repository;

  DownloadAvatarUseCase(this.repository);

  @override
  Future<Either<Failure, File>> call(DownloadAvatarParams params) async {
    return await repository.downloadFile(config: params.config, remotePath: params.remotePath, localPath: params.localPath);
  }
}

class DownloadAvatarParams extends Equatable {
  final WebDAVConfig config;
  final String remotePath;
  final String localPath;

  const DownloadAvatarParams({required this.config, required this.remotePath, required this.localPath});

  @override
  List<Object?> get props => [config, remotePath, localPath];
}