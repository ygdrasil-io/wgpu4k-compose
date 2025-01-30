package layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ygdrasil.webgpu.*
import org.koin.compose.koinInject
import scene.SceneViewModel
import scene.TextureBuffer


@Composable
fun CurrentScene(
    sceneViewModel: SceneViewModel = koinInject(),
    renderingContext: TextureRenderingContext = koinInject(),
    textureBuffer: TextureBuffer = koinInject()
) {

    val texture = renderingContext.getCurrentTexture()

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            text = "Scene X",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        val testImage  = textureBuffer
            .bufferArray.toImageBitmap(texture.width, texture.height)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = testImage,
                //painter = customPainter,
                contentScale = ContentScale.Fit,
                modifier = Modifier.wrapContentSize().background(Color.Green),
                contentDescription = "WebGPU Canvas"
            )
        }
    }
}

expect internal fun ByteArray.toImageBitmap(width: GPUIntegerCoordinateOut, height: GPUIntegerCoordinateOut): ImageBitmap
