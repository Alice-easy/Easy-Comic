import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/webdav_repository.dart';
import 'package:webdav_client/webdav_client.dart' as webdav;

class WebdavRepositoryImpl implements IWebdavRepository {
  webdav.Client _newClient(WebDAVConfig config) {
    return webdav.newClient(
      config.serverUrl,
      user: config.username,
      password: config.password,
    );
  }

  @override
  Future<Either<Failure, Unit>> login(WebDAVConfig config) async {
    try {
      final client = _newClient(config);
      await client.ping();
      return const Right(unit);
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, File>> downloadFile({required WebDAVConfig config, required String remotePath, required String localPath}) async {
    try {
      final client = _newClient(config);
      final file = File(localPath);
      await client.read2File(remotePath, file.path);
      return Right(file);
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Unit>> uploadFile({required WebDAVConfig config, required File file, required String remotePath}) async {
    try {
      final client = _newClient(config);
      await client.writeFromFile(file.path, remotePath);
      return const Right(unit);
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }
}