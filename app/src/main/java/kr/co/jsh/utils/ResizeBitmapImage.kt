package kr.co.jsh.utils

import android.graphics.Bitmap

fun ResizeBitmapImage(b: Bitmap, w: Int, h: Int) : Bitmap{
    return Bitmap.createScaledBitmap(b, w, h, true)
}