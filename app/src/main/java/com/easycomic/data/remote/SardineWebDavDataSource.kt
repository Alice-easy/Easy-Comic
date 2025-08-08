package com.easycomic.data.remote

import com.github.sardine.SardineFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import javax.inject.Inject

class SardineWebDavDataSource @Inject constructor() : WebDavDataSource {
    override suspend fun ping(url: String, username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val sardine = SardineFactory.begin(username, password)
            sardine.exists(url)
        }.getOrElse { false }
    }

    override suspend fun upload(url: String, username: String, password: String, remotePath: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val sardine = SardineFactory.begin(username, password)
            val full = if (remotePath.startsWith("http")) remotePath else url.trimEnd('/') + "/" + remotePath.trimStart('/')
            sardine.put(full, data)
            true
        }.getOrElse { false }
    }

    override suspend fun download(url: String, username: String, password: String, remotePath: String): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            val sardine = SardineFactory.begin(username, password)
            val full = if (remotePath.startsWith("http")) remotePath else url.trimEnd('/') + "/" + remotePath.trimStart('/')
            sardine.get(full).use { input ->
                input.readBytes()
            }
        }.getOrNull()
    }

    override suspend fun list(url: String, username: String, password: String, remoteDir: String): List<String> = withContext(Dispatchers.IO) {
        runCatching {
            val sardine = SardineFactory.begin(username, password)
            val full = if (remoteDir.startsWith("http")) remoteDir else url.trimEnd('/') + "/" + remoteDir.trimStart('/')
            sardine.list(full).mapNotNull { it.name }
        }.getOrElse { emptyList() }
    }
}
