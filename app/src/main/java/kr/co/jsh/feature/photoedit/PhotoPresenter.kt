package kr.co.jsh.feature.photoedit

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.domain.api.usecase.PostImagePidNumberAndInfoUseCase
import kr.co.domain.api.usecase.PostImproveImagePidNumber
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.PidClass
import kr.co.domain.utils.addFile
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.BitmapUtil.bitmapToFileUtil
import kr.co.jsh.utils.RunOnUiThread
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PhotoPresenter(override var view: PhotoContract.View,
                     private var postFileUploadUseCase: PostFileUploadUseCase,
                     private var postImagePidNumberAndInfoUseCase: PostImagePidNumberAndInfoUseCase,
                     private var postImproveImagePidNumber: PostImproveImagePidNumber
) : PhotoContract.Presenter {

    override fun preparePath(extraIntent: Intent) {
        var path =""
        extraIntent?.let{
            path =  it.getStringExtra(Consts.EXTRA_PHOTO_PATH)?:""
        }
        view.setPhotoView(Uri.parse(path.addFile()).toFile())
    }

    override fun saveImage(context: Context, uri: Uri) {
        RunOnUiThread(context).safely {
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
            Timber.e("IMAGE ID: $id")
        }    }

    @SuppressLint("CheckResult")
    override fun uploadFile(uri: String, type: String) {
        val path = uri.addFile()
        val request = MultipartBody.Part.createFormData("file", path, RequestBody.create(MediaType.parse("image/*"), Uri.parse(path).toFile()))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 ) {
                    view.uploadSuccess(it.message)
                    PidClass.imageObjectPid = it.datas.objectPid //file pid 저장
                    UserObject.ResponseCode = it.status.toInt()
                    if(type == Consts.SUPER_RESOL) requestImproveImage()
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
    private fun requestImproveImage() {
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val curTime = dateFormat.format(Date(time))

        postImproveImagePidNumber.postImproveImagePidNumber(Consts.SUPER_RESOL, PidClass.imageObjectPid, curTime)
            .subscribe({
                if (it.status.toInt() == 200) {
                    Timber.d("Complete Image Improve Request")
                    view.stopAnimation()
                } else Timber.e("ERROR ${it.status}")
            }, {
                it.localizedMessage
            })
    }

    //mask file 전송
    @SuppressLint("CheckResult")
    override fun uploadFrameFile(bitmap: Bitmap, context: Context) {
        val file = bitmapToFileUtil(bitmap, context)
        val path = file.toString().addFile()
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
    @SuppressLint("CheckResult", "SimpleDateFormat")
    private fun sendImageResultToServerWithInfo(maskPid: String, imagePid: String){
        //title 구분을 위해 현재 시간을 넣음
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val curTime = dateFormat.format(Date(time))

        postImagePidNumberAndInfoUseCase.postImagePidNumberAndInfo(maskPid, Consts.DEL_OBJ, imagePid, curTime)
            .subscribe({
                Timber.e("Image Send Result: ${it.message}")
                view.stopAnimation()
                PidClass.topImageObjectPid.add(it.datas.objectPid)
            },{
                it.localizedMessage
            })
    }

}