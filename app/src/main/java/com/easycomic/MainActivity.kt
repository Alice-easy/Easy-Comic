package com.easycomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.easycomic.ui.AppNavigation
import com.easycomic.ui.theme.EasyComicTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            
            EasyComicTheme {
                AppNavigation()
            }
            
            // 首帧渲染完成后的日志
            LaunchedEffect(Unit) {
                Timber.d("首帧渲染完成")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Timber.d("应用恢复")
    }
    
    override fun onPause() {
        super.onPause()
        Timber.d("应用暂停")
    }
}