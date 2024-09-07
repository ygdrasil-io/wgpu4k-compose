package layout

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ygdrasil.wgpu.GPUIntegerCoordinateOut


internal actual fun ByteArray.toImageBitmap(width: GPUIntegerCoordinateOut, height: GPUIntegerCoordinateOut): ImageBitmap {
    val options = BitmapFactory.Options()
    options.inMutable = true
    val bmp: Bitmap = BitmapFactory.decodeByteArray(this, 0, this.size, options)
    return bmp.asImageBitmap()
}