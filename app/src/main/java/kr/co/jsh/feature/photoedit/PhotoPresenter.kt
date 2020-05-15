package kr.co.jsh.feature.photoedit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toFile
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.jsh.utils.BitmapToFileUtil
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PhotoPresenter(override var view: PhotoContract.View,
                     private var postFileUploadUseCase: PostFileUploadUseCase
) : PhotoContract.Presenter {
    @SuppressLint("CheckResult")
    override fun setImageView(context: Context, string: String) {
        val stringToFile = Uri.parse(string).toFile()
        view.displayPhotoView(stringToFile)
    }

    override fun saveImage() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @SuppressLint("CheckResult")
    override fun uploadFile(uri: String) {
        val path = "file://" + uri
        val request = MultipartBody.Part.createFormData("file", path, RequestBody.create(MediaType.parse("image/*"), Uri.parse(path).toFile()))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 )
                    view.uploadSuccess(it.message)
                else view.uploadFailed(it.message)
            },{
                view.uploadFailed("로그인 후 가능")
            })
    }

    @SuppressLint("CheckResult")
    override fun uploadFrameFile(bitmap: Bitmap, context: Context) {
        val file = BitmapToFileUtil(bitmap, context)
        val path = "file://" + file.toString()
        val request = MultipartBody.Part.createFormData("file", path , RequestBody.create(MediaType.parse("image/*"),Uri.parse(path).toFile()))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 )
                    view.uploadSuccess(it.message)
                else view.uploadFailed(it.message)
            },{
                view.uploadFailed("로그인 후 가능")
            })
    }
}