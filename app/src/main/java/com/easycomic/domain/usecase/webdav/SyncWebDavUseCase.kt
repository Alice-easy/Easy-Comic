package com.easycomic.domain.usecase.webdav

import com.easycomic.data.repository.MangaRepository
import com.easycomic.data.repository.WebDavRepository
import javax.inject.Inject

class SyncWebDavUseCase @Inject constructor(
    private val mangaRepository: MangaRepository,
    private val webDavRepository: WebDavRepository
) {
    suspend operator fun invoke(): Result<Unit> = mangaRepository.syncWithWebDAV()
    
    suspend fun syncFromWebDAV(): Result<List<com.easycomic.core.database.Manga>> = 
        webDavRepository.syncFromWebDAV()
}