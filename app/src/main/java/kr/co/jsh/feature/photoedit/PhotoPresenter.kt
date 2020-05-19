package kr.co.jsh.feature.photoedit

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toFile
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.utils.BitmapToFileUtil
import kr.co.jsh.utils.RunOnUiThread
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class PhotoPresenter(override var view: PhotoContract.View,
                     private var postFileUploadUseCase: PostFileUploadUseCase
) : PhotoContract.Presenter {
    @SuppressLint("CheckResult")
    override fun setImageView(context: Context, string: String) {
        val stringToFile = Uri.parse(string).toFile()
        view.displayPhotoView(stringToFile)
    }

    override fun saveImage(context: Context, uri: Uri) {
        RunOnUiThread(context).safely {
            Toast.makeText(context, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()

            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(context, uri)
            val duration =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    .toLong()
            val width =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    .toLong()
            val height =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    .toLong()
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DATA, uri.path)
            values.put(MediaStore.Video.VideoColumns.DURATION, duration)
            values.put(MediaStore.Video.VideoColumns.WIDTH, width)
            values.put(MediaStore.Video.VideoColumns.HEIGHT, height)
            val id = ContentUris.parseId(
                context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            )
            Log.e("VIDEO ID", id.toString())
        }    }

    @SuppressLint("CheckResult")
    override fun uploadFile(uri: String) {
        val path = "file://" + uri
        val request = MultipartBody.Part.createFormData("file", path, RequestBody.create(MediaType.parse("image/*"), Uri.parse(path).toFile()))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 ) {
                    view.uploadSuccess(it.message)
                    PidClass.imageObjectPid = it.datas.objectPid //file pid 저장
                }
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