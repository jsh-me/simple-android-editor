package kr.co.jsh.utils.bitmapUtil

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

fun BitmapToFileUtil(bitmap: Bitmap, context:Context): Uri {
    // Get the context wrapper
    val wrapper = ContextWrapper(context)
    val bm = Bitmap.createBitmap(bitmap, 0,0, bitmap.width, bitmap.height)

    // Initialize a new file instance to save bitmap object
    var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
    file = File(file,"${UUID.randomUUID()}.jpg")

    try{
        // Compress the bitmap and save in jpg format
        val stream: OutputStream = FileOutputStream(file)
        bm.compress(Bitmap.CompressFormat.JPEG,100,stream)
        stream.flush()
        stream.close()
    }catch (e: IOException){
        e.printStackTrace()
    }

    // Return the saved bitmap uri
    return Uri.parse(file.absolutePath)
}