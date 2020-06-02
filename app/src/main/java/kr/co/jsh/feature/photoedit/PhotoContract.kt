package kr.co.jsh.feature.photoedit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import kr.co.jsh.base.BasePresenter
import kr.co.jsh.base.BaseView
import java.io.File

interface PhotoContract {
    interface View : BaseView<Presenter> {
        fun displayPhotoView(file: File)

    }
    interface Presenter: BasePresenter {
        var view: View
        fun saveImage(context: Context, uri: Uri)

        fun uploadFile(uri: String)
        fun uploadFrameFile(bitmap: Bitmap, context: Context)
    }
}