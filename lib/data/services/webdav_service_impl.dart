import 'dart:typed_data';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/services/webdav_service.dart';
import 'package:webdav_client/webdav_client.dart' as webdav;

class WebDAVServiceImpl implements WebDAVService {
  @override
  Future<Either<Failure, Unit>> backup({
    required WebDAVConfig config,
    required String fileName,
    required Uint8List data,
  }) async {
    try {
      final client = webdav.newClient(
        config.uri,
        user: config.username,
        password: config.password,
      );
      await client.write(fileName, data);
      return const Right(unit);
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }

  @override
  Future<Either<Failure, Uint8List>> restore({
    required WebDAVConfig config,
    required String fileName,
  }) async {
    try {
      final client = webdav.newClient(
        config.uri,
        user: config.username,
        password: config.password,
      );
      final data = await client.read(fileName);
      return Right(Uint8List.fromList(data));
    } catch (e) {
      return Left(ServerFailure(e.toString()));
    }
  }
}