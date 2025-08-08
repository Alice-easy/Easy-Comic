package com.easycomic.data.repository

import com.easycomic.data.remote.WebDavDataSource
import javax.inject.Inject

class WebDavRepository @Inject constructor(
	private val dataSource: WebDavDataSource
) {
	suspend fun testConnection(url: String, username: String, password: String): Boolean =
		dataSource.ping(url, username, password)
}

