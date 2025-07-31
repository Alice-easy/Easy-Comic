package com.example.easy_comic

import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity: FlutterActivity() {
    private var brightnessChannel: BrightnessChannel? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Initialize brightness channel
        brightnessChannel = BrightnessChannel(this)
        brightnessChannel?.initialize(flutterEngine)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        brightnessChannel?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        brightnessChannel?.dispose()
        brightnessChannel = null
        super.onDestroy()
    }
}