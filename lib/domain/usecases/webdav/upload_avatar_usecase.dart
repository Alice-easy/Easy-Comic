import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/webdav_repository.dart';
import 'package:equatable/equatable.dart';

class UploadAvatarUseCase implements UseCase<Unit, UploadAvatarParams> {
  final IWebdavRepository repository;

  UploadAvatarUseCase(this.repository);

  @override
  Future<Either<Failure, Unit>> call(UploadAvatarParams params) async {
    return await repository.uploadFile(config: params.config, file: params.file, remotePath: params.remotePath);
  }
}

class UploadAvatarParams extends Equatable {
  final WebDAVConfig config;
  final File file;
  final String remotePath;

  const UploadAvatarParams({required this.config, required this.file, required this.remotePath});

  @override
  List<Object?> get props => [config, file, remotePath];
}