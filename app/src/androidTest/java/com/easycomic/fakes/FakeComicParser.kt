package com.easycomic.fakes

import android.graphics.Bitmap
import com.easycomic.domain.parser.ComicParser
import com.easycomic.domain.parser.ComicParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class FakeComicParser : ComicParser {
    companion object {
        const val FAKE_PAGE_COUNT = 5
    }

    override fun getPageCount(): Int = FAKE_PAGE_COUNT

    override fun getPageStream(pageIndex: Int): InputStream? {
        if (pageIndex < 0 || pageIndex >= FAKE_PAGE_COUNT) {
            return null
        }
        // Create a dummy 1x1 pixel bitmap and return its stream
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return ByteArrayInputStream(stream.toByteArray())
    }

    override fun close() {
        // No-op
    }
}

