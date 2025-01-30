import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.ygdrasil.webgpu.canvasContextRenderer
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    MainScope().launch {
        val canvasContext = canvasContextRenderer(
            deferredRendering = true,
            width = 512,
            height = 512
        )

        ComposeViewport(document.body!!) {
            App(canvasContext.wgpuContext)
        }
    }
}