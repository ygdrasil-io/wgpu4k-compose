package scene

import RotatingCubeScene
import androidx.compose.runtime.mutableStateOf
import io.ygdrasil.wgpu.*


class SceneViewModel(
    val wgpu: WGPUContext,
    val textureBuffer: TextureBuffer,
    val renderingContext: TextureRenderingContext
) {

    val color = mutableStateOf(2)
    val scene = mutableStateOf<RotatingCubeScene?>(null)


    suspend fun initScene() {
        scene.value = RotatingCubeScene(wgpu).apply {
            initialize()
            autoClosableContext {
                render()
            }
            copyRenderingToBuffer()
        }
    }

    suspend fun copyRenderingToBuffer() {
        autoClosableContext {
            val commandEncoder = wgpu.device.createCommandEncoder().bind()
            commandEncoder.copyTextureToBuffer(
                ImageCopyTexture(
                    texture = renderingContext.getCurrentTexture(),
                    mipLevel = 0,
                    origin = Origin3D.Zero,
                    aspect = TextureAspect.all,
                ),
                ImageCopyBuffer(
                    buffer = textureBuffer.buffer,
                    offset = 0,
                    // This needs to be a multiple of 256. Normally we would need to pad
                    // it but we here know it will work out anyways.
                    bytesPerRow = renderingContext.width * 4,
                    rowsPerImage = renderingContext.height,
                ),
                Size3D(
                    width = renderingContext.width,
                    height = renderingContext.height
                )
            )

            wgpu.device.queue.submit(listOf(commandEncoder.finish()))
            textureBuffer.buffer.map(setOf(MapMode.read))
            // Complete async work
            wgpu.device.poll()
            textureBuffer.buffer.mapInto(buffer = textureBuffer.bufferArray, offset = 0)
        }
    }

}