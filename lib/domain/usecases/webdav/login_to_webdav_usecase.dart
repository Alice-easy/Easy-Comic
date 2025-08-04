import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/webdav_repository.dart';

class LoginToWebdavUseCase implements UseCase<Unit, WebDAVConfig> {
  final IWebdavRepository repository;

  LoginToWebdavUseCase(this.repository);

  @override
  Future<Either<Failure, Unit>> call(WebDAVConfig params) async {
    return await repository.login(params);
  }
}