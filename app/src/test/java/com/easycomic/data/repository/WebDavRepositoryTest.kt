package com.easycomic.data.repository

import com.easycomic.core.database.Manga
import com.easycomic.data.remote.WebDavDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

/**
 * Unit tests for WebDavRepository
 * Verifies the repository handles WebDAV operations correctly
 */
class WebDavRepositoryTest {
    
    private lateinit var webDavDataSource: WebDavDataSource
    private lateinit var webDavRepository: WebDavRepository
    
    @Before
    fun setUp() {
        webDavDataSource = mock(WebDavDataSource::class.java)
        webDavRepository = WebDavRepository(webDavDataSource)
    }
    
    @Test
    fun `testConnection should return success when data source returns true`() = runTest {
        // Given
        val url = "https://example.com/webdav"
        val username = "user"
        val password = "pass"
        
        `when`(webDavDataSource.testConnection(url, username, password))
            .thenReturn(Result.success(true))
        
        // When
        val result = webDavRepository.testConnection(url, username, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        verify(webDavDataSource).testConnection(url, username, password)
    }
    
    @Test
    fun `testConnection should return failure when data source throws exception`() = runTest {
        // Given
        val url = "https://example.com/webdav"
        val username = "user"
        val password = "pass"
        val exception = Exception("Network error")
        
        `when`(webDavDataSource.testConnection(url, username, password))
            .thenReturn(Result.failure(exception))
        
        // When
        val result = webDavRepository.testConnection(url, username, password)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(webDavDataSource).testConnection(url, username, password)
    }
    
    @Test
    fun `saveCredentials should call data source`() = runTest {
        // Given
        val url = "https://example.com/webdav"
        val username = "user"
        val password = "pass"
        
        `when`(webDavDataSource.saveCredentials(url, username, password))
            .thenReturn(Result.success(true))
        
        // When
        val result = webDavRepository.saveCredentials(url, username, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        verify(webDavDataSource).saveCredentials(url, username, password)
    }
    
    @Test
    fun `getCredentials should call data source`() {
        // Given
        val expectedUrl = "https://example.com/webdav"
        val expectedUsername = "user"
        val expectedPassword = "pass"
        
        `when`(webDavDataSource.getCredentials())
            .thenReturn(Triple(expectedUrl, expectedUsername, expectedPassword))
        
        // When
        val (url, username, password) = webDavRepository.getCredentials()
        
        // Then
        assertEquals(expectedUrl, url)
        assertEquals(expectedUsername, username)
        assertEquals(expectedPassword, password)
        verify(webDavDataSource).getCredentials()
    }
    
    @Test
    fun `clearCredentials should call data source`() = runTest {
        // Given
        `when`(webDavDataSource.clearCredentials())
            .thenReturn(Result.success(true))
        
        // When
        val result = webDavRepository.clearCredentials()
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        verify(webDavDataSource).clearCredentials()
    }
    
    @Test
    fun `uploadManga should call data source`() = runTest {
        // Given
        val manga = Manga(
            id = 1,
            title = "Test Manga",
            filePath = "/path/to/manga.cbz",
            totalPages = 10
        )
        
        `when`(webDavDataSource.uploadMangaData(manga))
            .thenReturn(Result.success(true))
        
        // When
        val result = webDavRepository.uploadManga(manga)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        verify(webDavDataSource).uploadMangaData(manga)
    }
    
    @Test
    fun `downloadMangaList should call data source`() = runTest {
        // Given
        val expectedMangaList = listOf(
            Manga(id = 1, title = "Manga 1", filePath = "/path/1", totalPages = 10),
            Manga(id = 2, title = "Manga 2", filePath = "/path/2", totalPages = 15)
        )
        
        `when`(webDavDataSource.downloadMangaList())
            .thenReturn(Result.success(expectedMangaList))
        
        // When
        val result = webDavRepository.downloadMangaList()
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMangaList, result.getOrNull())
        verify(webDavDataSource).downloadMangaList()
    }
    
    @Test
    fun `isConfigured should return true when credentials are present`() {
        // Given
        `when`(webDavDataSource.getCredentials())
            .thenReturn(Triple("https://example.com/webdav", "user", "pass"))
        
        // When
        val result = webDavRepository.isConfigured()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isConfigured should return false when credentials are missing`() {
        // Given
        `when`(webDavDataSource.getCredentials())
            .thenReturn(Triple(null, null, null))
        
        // When
        val result = webDavRepository.isConfigured()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getServerUrl should return url from credentials`() {
        // Given
        val expectedUrl = "https://example.com/webdav"
        `when`(webDavDataSource.getCredentials())
            .thenReturn(Triple(expectedUrl, "user", "pass"))
        
        // When
        val result = webDavRepository.getServerUrl()
        
        // Then
        assertEquals(expectedUrl, result)
    }
    
    @Test
    fun `getServerUrl should return null when credentials are missing`() {
        // Given
        `when`(webDavDataSource.getCredentials())
            .thenReturn(Triple(null, "user", "pass"))
        
        // When
        val result = webDavRepository.getServerUrl()
        
        // Then
        assertNull(result)
    }
}