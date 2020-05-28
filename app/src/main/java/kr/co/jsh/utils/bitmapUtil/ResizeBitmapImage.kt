package kr.co.jsh.utils.bitmapUtil

import android.graphics.Bitmap

fun ResizeBitmapImage(b: Bitmap, w: Int, h: Int) : Bitmap{
    return Bitmap.createScaledBitmap(b, w, h, true)
}