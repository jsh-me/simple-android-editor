package kr.co.jsh.feature.photoedit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class PhotoPresenter(override var view: PhotoContract.View) : PhotoContract.Presenter {
    @SuppressLint("CheckResult")
    override fun setImageView(context: Context, string: String) {
        val stringToUri = Uri.parse(string)

        Observable.just(stringToUri)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                view.displayPhotoView(BitmapFactory.decodeStream(context.contentResolver.openInputStream(stringToUri)))
            },{
                it.localizedMessage
            })
    }

    override fun saveImage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}