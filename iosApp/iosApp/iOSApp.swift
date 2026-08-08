import SwiftUI
import ComposeApp

// === ENABLE FIREBASE: add these imports (after adding the SPM packages in Xcode) ===
// import FirebaseCore
// import FirebaseAnalytics
// import FirebaseCrashlytics

// === ENABLE FIREBASE: uncomment these two bridge classes ===
// class SwiftAnalyticsBridge: AnalyticsBridge {
//     func logEvent(name: String, params: [String: String]) {
//         Analytics.logEvent(name, parameters: params)
//     }
//     func setUserId(id: String?) {
//         Analytics.setUserID(id)
//     }
// }
//
// class SwiftCrashReporterBridge: CrashReporterBridge {
//     func recordException(message: String, stackTrace: String) {
//         let error = NSError(
//             domain: "AppError",
//             code: 0,
//             userInfo: [NSLocalizedDescriptionKey: message, "stackTrace": stackTrace]
//         )
//         Crashlytics.crashlytics().record(error: error)
//     }
//     func setKey(key: String, value: String) {
//         Crashlytics.crashlytics().setCustomValue(value, forKey: key)
//     }
//     func log(message: String) {
//         Crashlytics.crashlytics().log(message)
//     }
// }

@main
struct iOSApp: App {
    init() {
        // === ENABLE FIREBASE: uncomment these three lines (and add GoogleService-Info.plist) ===
        // FirebaseApp.configure()
        // IosFirebaseBridges.shared.analytics = SwiftAnalyticsBridge()
        // IosFirebaseBridges.shared.crashReporter = SwiftCrashReporterBridge()

        // Must run before any composable resolves a dependency, which is why it's in init()
        // rather than onAppear.
        KoinInitKt.doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
