package kr.co.jsh.photoedit

import android.net.Uri
import androidx.databinding.ObservableField

class PhotoPresenter(override var view: PhotoContract.View) : PhotoContract.Presenter {
    override fun setImageView(flag: ObservableField<Boolean>,  string: String) {
        val stringToUri = Uri.parse(string)
        if(flag.get() == true) view.displayImageView(stringToUri)
        else view.displayPhotoView(stringToUri)
    }
}