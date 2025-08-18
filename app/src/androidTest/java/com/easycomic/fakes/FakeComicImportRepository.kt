package com.easycomic.fakes

import com.easycomic.domain.repository.ComicImportRepository
import java.io.File

class FakeComicImportRepository : ComicImportRepository {
    override suspend fun importComics(directory: File) {
        // This is a fake implementation.
        // In a real test, you might want to add some logic here to simulate
        // the import process, e.g., by adding a manga to a fake database.
    }
}