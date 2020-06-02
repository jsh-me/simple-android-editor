package kr.co.jsh.feature.photoedit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kr.co.jsh.base.edit.BasePresenter
import kr.co.jsh.base.edit.BaseView
import java.io.File

interface PhotoContract {
    interface View : BaseView<Presenter> {
        fun setPhotoView(file: File)

    }
    interface Presenter: BasePresenter {
        var view: View
        fun saveImage(context: Context, uri: Uri)
        fun uploadFrameFile(bitmap: Bitmap, context: Context)
    }
}