package com.easycomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.easycomic.ui.AppNavigation
import com.easycomic.ui.theme.EasyComicTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyComicTheme {
                AppNavigation()
            }
        }
    }
}