package com.easycomic.fakes

import com.easycomic.domain.parser.ComicParser
import com.easycomic.domain.parser.ComicParserFactory
import java.io.File

class FakeComicParserFactory : ComicParserFactory {
    override fun create(file: File): ComicParser {
        return FakeComicParser()
    }
}