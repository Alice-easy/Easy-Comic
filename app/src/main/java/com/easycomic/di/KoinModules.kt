package com.easycomic.di

import android.content.Context
import com.easycomic.data.di.dataModule
import com.easycomic.domain.di.domainModule
import com.easycomic.ui.di.uiModule
// import com.easycomic.ui_bookshelf.di.bookshelfModule
// import com.easycomic.ui_reader.di.readerModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

object KoinModules {
    fun getAllModules() = listOf(
        dataModule,
        domainModule,
        uiModule
        // TODO: 待feature模块修复后重新启用
        // bookshelfModule,
        // readerModule
    )

    fun initializeKoin(context: Context) {
        startKoin {
            androidContext(context)
            modules(getAllModules())
        }
    }
}