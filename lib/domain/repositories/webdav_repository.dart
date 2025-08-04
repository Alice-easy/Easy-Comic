import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';

abstract class IWebdavRepository {
  Future<Either<Failure, Unit>> login(WebDAVConfig config);
  Future<Either<Failure, Unit>> uploadFile({required WebDAVConfig config, required File file, required String remotePath});
  Future<Either<Failure, File>> downloadFile({required WebDAVConfig config, required String remotePath, required String localPath});
}