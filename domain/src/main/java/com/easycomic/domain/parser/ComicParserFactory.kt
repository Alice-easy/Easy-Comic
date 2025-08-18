package com.easycomic.domain.parser

import java.io.File

interface ComicParserFactory {
    fun create(file: File): ComicParser?
}