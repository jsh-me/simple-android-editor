package kr.co.jsh.utils

import android.graphics.BitmapFactory
import java.io.File

fun FileToBitmapSize(f: File) : ArrayList<Int> {
    val path = f.path
    val bitmap = BitmapFactory.decodeFile(path)
    val sizeArray = ArrayList<Int>()
    sizeArray.add(bitmap.width)
    sizeArray.add(bitmap.height)

    return sizeArray
}