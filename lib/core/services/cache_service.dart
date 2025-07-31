// lib/core/services/cache_service.dart
import 'dart:typed_data';

abstract class ICacheService {
  Future<Uint8List?> getImage(String key);
  Future<void> setImage(String key, Uint8List image);
  Future<void> clearCache();
}

class CacheService implements ICacheService {
  // TODO: Implement LRU cache logic
  
  @override
  Future<Uint8List?> getImage(String key) async {
    // TODO: Implement getImage
    return null;
  }

  @override
  Future<void> setImage(String key, Uint8List image) async {
    // TODO: Implement setImage
  }

  @override
  Future<void> clearCache() async {
    // TODO: Implement clearCache
  }
}