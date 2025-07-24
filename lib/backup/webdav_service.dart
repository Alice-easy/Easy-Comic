import 'dart:io' as io;
import 'package:webdav_client/webdav_client.dart';

class WebdavService {
  late final Client _client;

  WebdavService(String host, String user, String password) {
    _client = newClient(host, user: user, password: password);
  }

  Future<void> uploadFile(String localPath, String remotePath) async {
    final file = io.File(localPath);
    final bytes = await file.readAsBytes();
    await _client.write(remotePath, bytes);
  }

  Future<void> downloadFile(String remotePath, String localPath) async {
    final bytes = await _client.read(remotePath);
    final file = io.File(localPath);
    await file.writeAsBytes(bytes);
  }
}
