import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.tts.TtsScreen

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Phonics TTS") {
        TtsScreen()
    }
}
