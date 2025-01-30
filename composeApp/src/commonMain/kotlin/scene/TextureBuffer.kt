package scene

import io.ygdrasil.webgpu.*

class TextureBuffer(wgpu: WGPUContext, renderingContext: TextureRenderingContext) {

    val buffer = wgpu.device.createBuffer(BufferDescriptor(
        label = "Compose texture buffer",
        size = (renderingContext.width * renderingContext.height * renderingContext.textureFormat.getBytesPerPixel()).toULong(),
        usage = setOf(BufferUsage.CopyDst, BufferUsage.MapRead),
        mappedAtCreation = false,
    ))

    val bufferArray = ByteArray((renderingContext.width * renderingContext.height * renderingContext.getCurrentTexture().format.getBytesPerPixel()).toInt())

}