package kr.co.jsh.utils

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

object BitmapUtil{
    fun bitmapToFileUtil(bitmap: Bitmap, context: Context): Uri {
        // Get the context wrapper
        val wrapper = ContextWrapper(context)
        val bm = Bitmap.createBitmap(bitmap, 0,0, bitmap.width, bitmap.height)

        // Initialize a new file instance to save bitmap object
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.png")

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

    fun createBinaryMask(b: Bitmap): Bitmap {
        for(i in 0 until b.width){
            for(j in 0 until b.height ){
                val rgb= b.getPixel(i,j)
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

    fun cropBitmapImage(b: Bitmap, w: Int, h: Int) : Bitmap {
        return Bitmap.createBitmap(b, 0,0, w, h )
    }

    fun fileToBitmapSize(f: File) : ArrayList<Int> {
        val path = f.path
        val bitmap = BitmapFactory.decodeFile(path)
        val sizeArray = ArrayList<Int>()
        sizeArray.add(bitmap.width)
        sizeArray.add(bitmap.height)

        return sizeArray
    }

    fun resizeBitmapImage(b: Bitmap, w: Int, h: Int) : Bitmap{
        return Bitmap.createScaledBitmap(b, w, h, true)
    }
}