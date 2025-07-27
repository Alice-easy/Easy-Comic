package com.example.easy_comic

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import es.antonborri.home_widget.HomeWidgetPlugin

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        HomeWidgetPlugin.registerWith(flutterEngine.dartExecutor.binaryMessenger)
    }
}
