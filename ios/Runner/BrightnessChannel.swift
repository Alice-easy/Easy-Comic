import Flutter
import UIKit

@objc class BrightnessChannel: NSObject, FlutterPlugin {
    static let channelName = "com.easycomic.brightness"
    private var methodChannel: FlutterMethodChannel?
    private var originalBrightness: CGFloat = 1.0
    
    static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: channelName, binaryMessenger: registrar.messenger())
        let instance = BrightnessChannel()
        instance.methodChannel = channel
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        switch call.method {
        case "getSystemBrightness":
            getSystemBrightness(result: result)
        case "setSystemBrightness":
            if let args = call.arguments as? [String: Any],
               let brightness = args["brightness"] as? Double {
                setSystemBrightness(brightness: brightness, result: result)
            } else {
                result(FlutterError(code: "INVALID_ARGUMENT", message: "Brightness value is required", details: nil))
            }
        case "checkWriteSettingsPermission":
            // iOS doesn't require special permissions for brightness control
            result(true)
        case "requestWriteSettingsPermission":
            // iOS doesn't require special permissions for brightness control
            result(true)
        case "isSupported":
            result(true)
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    private func getSystemBrightness(result: @escaping FlutterResult) {
        DispatchQueue.main.async {
            let brightness = UIScreen.main.brightness
            result(Double(brightness))
        }
    }
    
    private func setSystemBrightness(brightness: Double, result: @escaping FlutterResult) {
        DispatchQueue.main.async {
            let clampedBrightness = min(max(brightness, 0.0), 1.0)
            
            // Store original brightness on first call
            if self.originalBrightness == 1.0 {
                self.originalBrightness = UIScreen.main.brightness
            }
            
            UIScreen.main.brightness = CGFloat(clampedBrightness)
            result(nil)
        }
    }
    
    // Helper method to restore original brightness
    func restoreOriginalBrightness() {
        DispatchQueue.main.async {
            UIScreen.main.brightness = self.originalBrightness
        }
    }
}