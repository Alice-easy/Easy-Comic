package com.easycomic.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.GetCoverUseCase

class MangaCoverFetcher(
    private val manga: Manga,
    private val options: Options,
    private val getCoverUseCase: GetCoverUseCase
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val coverBitmap: Bitmap = getCoverUseCase(manga) ?: return null
        return FetchResult(
            drawable = BitmapDrawable(options.context.resources, coverBitmap),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory(private val getCoverUseCase: GetCoverUseCase) : Fetcher.Factory<Manga> {
        override fun create(data: Manga, options: Options, imageLoader: ImageLoader): Fetcher {
            return MangaCoverFetcher(data, options, getCoverUseCase)
        }
    }
}