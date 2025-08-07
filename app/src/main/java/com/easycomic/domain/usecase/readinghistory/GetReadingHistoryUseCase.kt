package com.easycomic.domain.usecase.readinghistory

import com.easycomic.data.repository.ReadingHistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadingHistoryUseCase @Inject constructor(
    private val readingHistoryRepository: ReadingHistoryRepository
) {
    operator fun invoke(): Flow<List<com.easycomic.core.database.ReadingHistory>> = 
        readingHistoryRepository.getAllReadingHistory()
    
    fun getInProgressHistory(): Flow<List<com.easycomic.core.database.ReadingHistory>> = 
        readingHistoryRepository.getInProgressReadingHistory()
    
    fun getCompletedHistory(): Flow<List<com.easycomic.core.database.ReadingHistory>> = 
        readingHistoryRepository.getCompletedReadingHistory()
    
    suspend fun getHistoryForManga(mangaId: Long): com.easycomic.core.database.ReadingHistory? = 
        readingHistoryRepository.getReadingHistoryForManga(mangaId)
}