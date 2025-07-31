import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
    private var brightnessChannel: BrightnessChannel?
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)
        
        // Register brightness channel
        guard let controller = window?.rootViewController as? FlutterViewController else {
            fatalError("rootViewController is not type FlutterViewController")
        }
        
        let registrar = self.registrar(forPlugin: "BrightnessChannel")!
        BrightnessChannel.register(with: registrar)
        
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    override func applicationWillTerminate(_ application: UIApplication) {
        // Restore original brightness when app terminates
        brightnessChannel?.restoreOriginalBrightness()
        super.applicationWillTerminate(application)
    }
}
