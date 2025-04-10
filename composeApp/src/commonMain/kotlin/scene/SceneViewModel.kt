package scene

import RotatingCubeScene
import androidx.compose.runtime.mutableStateOf
import io.ygdrasil.webgpu.*
import kotlinx.atomicfu.atomic


class SceneViewModel(
    val wgpu: WGPUContext,
    val renderingContext: TextureRenderingContext
) {

    private var rendering = atomic(false)
    private var swapStatus = SwapStatus.Empty

    private enum class SwapStatus {
        Empty,
        Buffer1,
        Buffer2,
    }

    private val textureBuffer1 = TextureBuffer(wgpu, renderingContext)
    private val textureBuffer2 = TextureBuffer(wgpu, renderingContext)

    val color = mutableStateOf(2)
    val scene = mutableStateOf<RotatingCubeScene?>(null)
    val swapBuffer = mutableStateOf<TextureBuffer?>(null)

    suspend fun initScene() {
        scene.value = RotatingCubeScene(wgpu).apply {
            initialize()
        }
        render()
    }

    suspend fun render() {
        scene.value?.apply {
            frame += 1
            // We drop the frame if we cannot get the lock
            if (rendering.compareAndSet(expect = false, update = true)) {
                autoClosableContext {
                    render()
                }
                copyRenderingToBuffer()
                swapBuffers()
                rendering.value = false
            }
        }
    }

    suspend fun copyRenderingToBuffer() {
        autoClosableContext {
            val textureBuffer = getCurrentSwapBuffer()
            val commandEncoder = wgpu.device.createCommandEncoder().bind()
            commandEncoder.copyTextureToBuffer(
                TexelCopyTextureInfo(
                    texture = renderingContext.getCurrentTexture(),
                    mipLevel = 0u,
                    origin = Origin3D(),
                    aspect = GPUTextureAspect.All,
                ),
                TexelCopyBufferInfo(
                    buffer = textureBuffer.buffer,
                    offset = 0u,
                    // This needs to be a multiple of 256. Normally we would need to pad
                    // it but we here know it will work out anyways.
                    bytesPerRow = renderingContext.width * 4u,
                    rowsPerImage = renderingContext.height,
                ),
                Extent3D(
                    width = renderingContext.width,
                    height = renderingContext.height
                )
            )

            wgpu.device.queue.submit(listOf(commandEncoder.finish()))
            textureBuffer.buffer.map(setOf(GPUMapMode.Read))
            // Complete async work
            wgpu.device.poll()
            textureBuffer.buffer.mapInto(buffer = textureBuffer.bufferArray, offset = 0u)
            textureBuffer.buffer.unmap()
        }
    }

    fun getCurrentSwapBuffer(): TextureBuffer = when (swapStatus) {
        SwapStatus.Empty, SwapStatus.Buffer1 -> textureBuffer1
        else -> textureBuffer2
    }

    fun swapBuffers() = when (swapStatus) {
        SwapStatus.Empty, SwapStatus.Buffer1 -> {
            swapStatus = SwapStatus.Buffer2
            swapBuffer.value = textureBuffer1
        }
        else -> {
            swapStatus = SwapStatus.Buffer1
            swapBuffer.value = textureBuffer2
        }
    }
}