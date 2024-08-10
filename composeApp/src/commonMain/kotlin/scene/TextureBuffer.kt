package scene

import io.ygdrasil.wgpu.*

class TextureBuffer(wgpu: WGPUContext, renderingContext: TextureRenderingContext) {

    val buffer = wgpu.device.createBuffer(BufferDescriptor(
        label = "Compose texture buffer",
        size = (renderingContext.width * renderingContext.height * renderingContext.textureFormat.getBytesPerPixel()).toLong(),
        usage = setOf(BufferUsage.copydst, BufferUsage.mapread),
        mappedAtCreation = false,
    ))

    val bufferArray = ByteArray(renderingContext.width * renderingContext.height * renderingContext.getCurrentTexture().format.getBytesPerPixel())

}