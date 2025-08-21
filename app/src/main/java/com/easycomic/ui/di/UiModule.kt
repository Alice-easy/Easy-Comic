package com.easycomic.ui.di

import com.easycomic.ui.theme.ThemeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * UI层依赖注入模块
 */
val uiModule = module {
    // ViewModels
    viewModel { ThemeViewModel(get(), get()) }
}
