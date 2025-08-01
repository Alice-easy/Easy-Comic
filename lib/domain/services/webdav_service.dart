import 'dart:typed_data';

import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/error/failures.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';

abstract class WebDAVService {
  Future<Either<Failure, Unit>> backup({
    required WebDAVConfig config,
    required String fileName,
    required Uint8List data,
  });

  Future<Either<Failure, Uint8List>> restore({
    required WebDAVConfig config,
    required String fileName,
  });
}