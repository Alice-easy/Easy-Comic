package com.easycomic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EasyComicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化全局组件
    }
}