package com.easycomic.data.parser

import com.easycomic.domain.parser.ComicParser
import com.easycomic.domain.parser.ComicParserFactory
import java.io.File

class ComicParserFactoryImpl : ComicParserFactory {
    override fun create(file: File): ComicParser? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "zip", "cbz" -> ZipComicParser(file)
            "rar", "cbr" -> RarComicParser(file)
            else -> null
        }
    }
}