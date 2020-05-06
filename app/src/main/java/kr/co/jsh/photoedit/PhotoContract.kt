package kr.co.jsh.photoedit

import android.content.Context
import android.graphics.Bitmap

interface PhotoContract {
    interface View{
        fun displayPhotoView(bitmap: Bitmap)
    }
    interface Presenter{
        var view: View
        fun setImageView(context: Context, string: String)
        fun saveImage()
    }
}