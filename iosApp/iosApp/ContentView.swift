import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            // Compose draws the full window and handles insets itself via WindowInsets, so SwiftUI
            // must not also inset it — doing both double-pads the top of every screen. Remove this
            // only if the app deliberately wants SwiftUI chrome outside the Compose surface.
            .ignoresSafeArea(.all)
    }
}
