package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetMangaListUseCase @Inject constructor(
    private val repository: MangaRepository
) {
    operator fun invoke(): Flow<List<Manga>> = repository.observeAll()
}
