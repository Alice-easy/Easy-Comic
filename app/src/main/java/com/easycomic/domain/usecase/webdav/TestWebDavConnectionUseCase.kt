package com.easycomic.domain.usecase.webdav

import com.easycomic.data.repository.WebDavRepository
import javax.inject.Inject

class TestWebDavConnectionUseCase @Inject constructor(
    private val webDavRepository: WebDavRepository
) {
    suspend operator fun invoke(url: String, username: String, password: String): Result<Boolean> =
        webDavRepository.testConnection(url, username, password)
}