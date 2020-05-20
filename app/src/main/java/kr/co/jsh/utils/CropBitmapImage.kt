package kr.co.jsh.utils

import android.graphics.Bitmap

fun CropBitmapImage(b: Bitmap, w: Int, h: Int) : Bitmap {
    return Bitmap.createBitmap(b, 0,0, w, h )
}
