package kr.co.jsh.feature.photoedit

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.domain.api.usecase.PostImagePidNumberAndInfoUseCase
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.bitmapUtil.BitmapToFileUtil
import kr.co.jsh.utils.RunOnUiThread
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

class PhotoPresenter(override var view: PhotoContract.View,
                     private var postFileUploadUseCase: PostFileUploadUseCase,
                     private var postImagePidNumberAndInfoUseCase: PostImagePidNumberAndInfoUseCase
) : PhotoContract.Presenter {
    @SuppressLint("CheckResult")
    override fun setImageView(context: Context, string: String) {
        val stringToFile = Uri.parse(string).toFile()
        view.displayPhotoView(stringToFile)
    }

    override fun saveImage(context: Context, uri: Uri) {
        RunOnUiThread(context).safely {
          //  Toast.makeText(context, "Image saved at ${uri.path}", Toast.LENGTH_SHORT).show()

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
            values.put(MediaStore.Images.Media.DATA, uri.path)
            values.put(MediaStore.Images.ImageColumns.DURATION, duration)
            values.put(MediaStore.Images.ImageColumns.WIDTH, width)
            values.put(MediaStore.Images.ImageColumns.HEIGHT, height)
            val id = ContentUris.parseId(
                context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            )
            Log.e("IMAGE ID", id.toString())
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
                    UserObject.ResponseCode = it.status.toInt()

                }
                else {
                    view.uploadFailed(it.message)
                    UserObject.ResponseCode = it.status.toInt()
                }
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
                if(it.status.toInt() == 200 ) {
                    view.uploadSuccess(it.message)
                    UserObject.ResponseCode = it.status.toInt()
                    PidClass.imageMaskObjectPid = it.datas.objectPid
                    sendImageResultToServerWithInfo(PidClass.imageMaskObjectPid, PidClass.imageObjectPid)
                }
                else {
                    view.uploadFailed(it.message)
                    UserObject.ResponseCode = it.status.toInt()
                }
            },{
                view.uploadFailed("로그인 후 가능")
                view.cancelJob()

            })
    }
    @SuppressLint("CheckResult")
    private fun sendImageResultToServerWithInfo(maskPid: String, imagePid: String){
        //title 구분을 위해 현재 시간을 넣음
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-mm-dd hh:mm:ss")
        val curTime = dateFormat.format(Date(time))

        postImagePidNumberAndInfoUseCase.postImagePidNumberAndInfo(maskPid, Consts.DEL_OBJ, imagePid, curTime)
            .subscribe({
                Log.e("Image Send Result", it.message)
                PidClass.topImageObjectPid.add(it.datas.objectPid)
            },{
                it.localizedMessage
            })
    }

}