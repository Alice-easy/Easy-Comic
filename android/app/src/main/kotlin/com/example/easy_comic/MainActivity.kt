package com.example.easy_comic

import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.flutter.embedding.android.FlutterActivity

class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val crashlytics = FirebaseCrashlytics.getInstance()
        
        // 设置自定义键
        crashlytics.setCustomKey("userId", "12345")
        crashlytics.setCustomKey("fileHash", "abcdef123456")
    }
}
