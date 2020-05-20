package kr.co.jsh.utils

import android.graphics.Bitmap
import android.graphics.Color

fun CreateBinaryMask(b: Bitmap): Bitmap {
    var rgb = 0

    for(i in 0 until b.width){
        for(j in 0 until b.height ){
            rgb= b.getPixel(i,j)
            val R = Color.red(rgb)
            val G = Color.green(rgb)
            val B = Color.blue(rgb)

            if(R == 0 && G == 0 && B == 0){
                b.setPixel(i, j, Color.WHITE)
            } else b.setPixel(i, j, 0)
        }
    }
    return b
}