package com.easycomic.data.remote

import java.io.InputStream

/**
 * WebDAV 数据源接口，封装最小能力用于同步：探活、上传/下载与列举。
 */
interface WebDavDataSource {
	suspend fun ping(url: String, username: String, password: String): Boolean
	suspend fun upload(url: String, username: String, password: String, remotePath: String, data: ByteArray): Boolean
	suspend fun download(url: String, username: String, password: String, remotePath: String): ByteArray?
	suspend fun list(url: String, username: String, password: String, remoteDir: String): List<String>
}

