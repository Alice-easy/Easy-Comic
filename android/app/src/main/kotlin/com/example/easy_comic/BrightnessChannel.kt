package com.example.easy_comic

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class BrightnessChannel(private val activity: Activity) : MethodChannel.MethodCallHandler {
    companion object {
        const val CHANNEL = "com.easycomic.brightness"
        const val WRITE_SETTINGS_REQUEST_CODE = 1001
    }

    private var methodChannel: MethodChannel? = null
    private var pendingResult: MethodChannel.Result? = null

    fun initialize(flutterEngine: FlutterEngine) {
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
        methodChannel?.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getSystemBrightness" -> {
                getSystemBrightness(result)
            }
            "setSystemBrightness" -> {
                val brightness = call.argument<Double>("brightness")
                if (brightness != null) {
                    setSystemBrightness(brightness, result)
                } else {
                    result.error("INVALID_ARGUMENT", "Brightness value is required", null)
                }
            }
            "checkWriteSettingsPermission" -> {
                checkWriteSettingsPermission(result)
            }
            "requestWriteSettingsPermission" -> {
                requestWriteSettingsPermission(result)
            }
            "isSupported" -> {
                result.success(true)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getSystemBrightness(result: MethodChannel.Result) {
        try {
            val brightness = Settings.System.getInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            ) / 255.0
            result.success(brightness)
        } catch (e: Settings.SettingNotFoundException) {
            // Fall back to window brightness if system brightness not available
            try {
                val windowBrightness = activity.window.attributes.screenBrightness
                if (windowBrightness >= 0) {
                    result.success(windowBrightness.toDouble())
                } else {
                    result.success(1.0) // Default brightness
                }
            } catch (ex: Exception) {
                result.error("BRIGHTNESS_ERROR", "Unable to get system brightness: ${ex.message}", null)
            }
        } catch (e: Exception) {
            result.error("BRIGHTNESS_ERROR", "Unable to get system brightness: ${e.message}", null)
        }
    }

    private fun setSystemBrightness(brightness: Double, result: MethodChannel.Result) {
        try {
            val clampedBrightness = brightness.coerceIn(0.0, 1.0)
            
            // First try to set window brightness (doesn't require permission)
            setWindowBrightness(clampedBrightness)
            
            // Then try to set system brightness (requires WRITE_SETTINGS permission)
            if (canWriteSystemSettings()) {
                setSystemBrightnessInternal(clampedBrightness)
                result.success(null)
            } else {
                // Window brightness set successfully, but system setting requires permission
                result.success(null)
            }
        } catch (e: Exception) {
            result.error("BRIGHTNESS_ERROR", "Unable to set brightness: ${e.message}", null)
        }
    }

    private fun setWindowBrightness(brightness: Double) {
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = brightness.toFloat()
        activity.window.attributes = layoutParams
    }

    private fun setSystemBrightnessInternal(brightness: Double) {
        try {
            val brightnessValue = (brightness * 255).toInt()
            Settings.System.putInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightnessValue
            )
            
            // Also set the brightness mode to manual
            Settings.System.putInt(
                activity.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } catch (e: SecurityException) {
            // Permission denied - this is expected if WRITE_SETTINGS permission not granted
            throw e
        }
    }

    private fun checkWriteSettingsPermission(result: MethodChannel.Result) {
        val hasPermission = canWriteSystemSettings()
        result.success(hasPermission)
    }

    private fun requestWriteSettingsPermission(result: MethodChannel.Result) {
        if (canWriteSystemSettings()) {
            result.success(true)
            return
        }

        try {
            pendingResult = result
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + activity.packageName)
            activity.startActivityForResult(intent, WRITE_SETTINGS_REQUEST_CODE)
        } catch (e: Exception) {
            result.error("PERMISSION_ERROR", "Unable to request write settings permission: ${e.message}", null)
        }
    }

    private fun canWriteSystemSettings(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(activity)
        } else {
            true // Permission not required on older versions
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == WRITE_SETTINGS_REQUEST_CODE) {
            val hasPermission = canWriteSystemSettings()
            pendingResult?.success(hasPermission)
            pendingResult = null
        }
    }

    fun dispose() {
        methodChannel?.setMethodCallHandler(null)
        methodChannel = null
        pendingResult = null
    }
}