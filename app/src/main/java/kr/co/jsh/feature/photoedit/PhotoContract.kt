package kr.co.jsh.feature.photoedit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File

interface PhotoContract {
    interface View{
        fun displayPhotoView(file: File)

        //uploadFile result
        fun uploadSuccess(msg: String)
        fun uploadFailed(msg: String)
    }
    interface Presenter{
        var view: View
        fun setImageView(context: Context, string: String)
        fun saveImage()

        fun uploadFile(uri: String)

        fun uploadFrameFile(bitmap: Bitmap, context: Context)
    }
}