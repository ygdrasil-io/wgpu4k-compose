package layout

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo

internal actual fun ByteArray.toImageBitmap(width: Int, height: Int): ImageBitmap {

    val info = ImageInfo(
        width,
        height,
        ColorType.BGRA_8888,
        ColorAlphaType.PREMUL,
        ColorSpace.sRGB
    )

    val image = Image.makeRaster(
        info, this, info.width * 4
    )

    return Bitmap.makeFromImage(image).asComposeImageBitmap()
}