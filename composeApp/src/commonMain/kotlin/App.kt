import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.ygdrasil.webgpu.TextureRenderingContext
import io.ygdrasil.webgpu.WGPUContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import layout.Main
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import scene.SceneViewModel
import scene.TextureBuffer

@Composable
@Preview
fun App(wgpu: WGPUContext? = null) {
    if (wgpu == null) return

    KoinApplication(application = {
        modules(
            module {
                single { wgpu }
                single { TextureBuffer(wgpu, get()) }
                single { (wgpu.renderingContext as? TextureRenderingContext) ?: error("expected texture rendering context") }
                single { SceneViewModel(wgpu, get(), get()).apply { MainScope().launch { initScene() } } }
            }
        )
    }) {

        MaterialTheme(
            colorScheme = darkColorScheme(
                primaryContainer = Color(red = 32, green = 38, blue = 52, alpha = 255),
                secondary = Color.White,
                secondaryContainer = Color(red = 41, green = 48, blue = 66, alpha = 255)
            )
        ) {
            Main()
        }
    }
}