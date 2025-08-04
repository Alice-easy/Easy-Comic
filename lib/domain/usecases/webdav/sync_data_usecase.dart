import 'package:dartz/dartz.dart';
import 'package:easy_comic/core/failures/failures.dart';
import 'package:easy_comic/core/usecases/usecase.dart';
import 'package:easy_comic/domain/entities/webdav_config.dart';
import 'package:easy_comic/domain/repositories/webdav_repository.dart';

// TODO: Implement full sync logic in a future task
class SyncDataUseCase implements UseCase<Unit, WebDAVConfig> {
  final IWebdavRepository repository;

  SyncDataUseCase(this.repository);

  @override
  Future<Either<Failure, Unit>> call(WebDAVConfig params) async {
    // Placeholder implementation
    print("Syncing data...");
    await Future.delayed(const Duration(seconds: 2));
    print("Sync complete.");
    return const Right(unit);
  }
}