package kr.co.jsh.photoedit

import android.net.Uri
import androidx.databinding.ObservableField

interface PhotoContract {
    interface View{
        fun displayImageView(uri: Uri)
        fun displayPhotoView(uri: Uri)
    }
    interface Presenter{
        var view: View
        fun setImageView(flag: ObservableField<Boolean>, string: String)
    }
}