package kr.co.jsh.feature.videoedit

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import kr.co.domain.api.usecase.PostFileUploadUseCase
import kr.co.domain.api.usecase.PostVideoPidNumberAndInfoUseCase
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.domain.globalconst.PidClass
import kr.co.jsh.utils.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class TrimmerPresenter(override var view: TrimmerContract.View,
                       private var postFileUploadUseCase: PostFileUploadUseCase,
                       private var postPidNumberAndInfoUseCase: PostVideoPidNumberAndInfoUseCase) : TrimmerContract.Presenter{
   override fun crop(context: Context, cropCount: Int, videoLoader:VideoView,
                          crop_time: ArrayList<Pair<Int, Int>>, recycler: RecyclerView
   ){
        var crop_x1 = 0
       var crop_x2 = 0

        when(cropCount){
            1 -> {
                crop_x1 =
                    ( videoLoader.currentPosition * (recycler.width - ScreenSizeUtil(context).widthPixels)) /  videoLoader.duration

                crop_time.add(Pair(crop_x1,  videoLoader.currentPosition))//2
                crop_time.add(Pair(crop_x1,  videoLoader.currentPosition))//3
                crop_time.add(Pair(recycler.width - ScreenSizeUtil(context).widthPixels,videoLoader.duration)) //4
                Toast.makeText(context, "${crop_x1} and ${videoLoader.currentPosition}", Toast.LENGTH_LONG).show()
            }
             2-> {
                 crop_x2 =
                     ( videoLoader.currentPosition * (recycler.width - ScreenSizeUtil(context).widthPixels)) /  videoLoader.duration
                if(crop_time[1].first > crop_x2) {
                    crop_time[1] = Pair(crop_x2, videoLoader.currentPosition)
                }
                 else crop_time[2] = Pair(crop_x2, videoLoader.currentPosition)
                 Toast.makeText(context, "${crop_x2} and ${videoLoader.currentPosition}", Toast.LENGTH_LONG).show()
             }

            else -> {
                Toast.makeText(context, "두번만 선택 가능", Toast.LENGTH_LONG).show()
            }
        }
       view.setPairList(crop_time)

    }

    //사용자가 자른 동영상이 갤러리와 서버 동시에 저장, 업로드 되는 메소드
    override fun getResultUri(uri: Uri, context: Context) {
        RunOnUiThread(context).safely {
            Toast.makeText(context, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()
            //Todo override 된 함수에 넣어줌 ( 사용자가 자른 동영상 )

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
        }
        uploadFile(uri)

    }

    override fun prepareVideoPath(extraIntent: Intent) {
        var path =""
        extraIntent?.let{
            path =  it.getStringExtra(EXTRA_VIDEO_PATH)
            }
        view.videoPath(path)
    }

    override fun resetCrop(context:Context, crop_time: ArrayList<Pair<Int, Int>>) {
        try {
            crop_time.clear()
            crop_time.add(Pair(0,0))//1
            view.resetCropView()
        } catch (e: Exception) {
            Toast.makeText(context, "잘라진 것이 없어요!", Toast.LENGTH_LONG).show()
        }
    }

    override fun getThumbnailList(mSrc: Uri, context:Context) {
        val thumbnailList = ArrayList<Bitmap>()
                try {
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, mSrc)

                    val videoLengthInMs = (Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))).toLong()
                    //var numThumbs = if(videoLengthInMs-second >=3000) Math.ceil((videoLengthInMs - second) / 3000.0).toInt() else 1
                    val cropHeight = 150 //timelineview에서 한 프레임의 너비 (동적으로 변경되게끔 코드 수정해야함!)
                    val cropWidth = ScreenSizeUtil(context).widthPixels/4 //timelineview에서 한 프레임의 너비

                    //val interval = videoLengthInMs / numThumbs
                    val interval = if(videoLengthInMs< 3000) videoLengthInMs else 3000


                    for (i in 0 .. videoLengthInMs step interval) {
                        var bitmap = mediaMetadataRetriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        if (bitmap != null) {
                            try {
                                bitmap = Bitmap.createScaledBitmap(bitmap, cropWidth, cropHeight, false)
                                //bitmap = Bitmap.createBitmap(bitmap,0,0, cropWidth, cropHeight)
                                Log.i("bitmap111","${bitmap.width}, ${bitmap.height}")


                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            thumbnailList.add(bitmap)
                            Log.i("1:","${thumbnailList.size}")

                        }
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
                }
                Log.i("return 직전:","${thumbnailList.size}")
            view.setThumbnailListView(thumbnailList)
    }

    override fun trimVideo(path: String, context:Context, mSrc: Uri,  start_sec: Int, end_sec: Int) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)

        val file = File(mSrc.path ?: "")

        val root = File(path)
        root.mkdirs()
        val outputFileUri = Uri.fromFile(File(root, "t_${Calendar.getInstance().timeInMillis}_" + file.nameWithoutExtension + ".mp4"))
        val outPutPath = RealPathUtil.realPathFromUriApi19(context, outputFileUri)
            ?: File(root, "t_${Calendar.getInstance().timeInMillis}_" + mSrc.path?.substring(mSrc.path!!.lastIndexOf("/") + 1)).absolutePath
        Log.e("SOURCE", file.path)
        Log.e("DESTINATION", outPutPath)
        val extractor = MediaExtractor()
        var frameRate = 24
        try {
            extractor.setDataSource(file.path)
            val numTracks = extractor.trackCount
            for (i in 0..numTracks) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith("video/")) {
                    if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            extractor.release()
        }
        val duration = java.lang.Long.parseLong(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
        Log.e("FRAME RATE", frameRate.toString())
        Log.e("FRAME COUNT", (duration / 1000 * frameRate).toString())
        VideoOptions(context).trimVideo(TrimVideoUtils.stringForTime(start_sec.toFloat()), TrimVideoUtils.stringForTime(end_sec.toFloat()), file.path, outPutPath, outputFileUri, view)

    }

    //Todo 근데 왜 MediaType 이 video 인데도, 사진 동영상 둘다 왜 되는거지?
    //Todo 동영상과 사진 확장자를 업로드 할 수 있는 메소드
    @SuppressLint("CheckResult")
    override fun uploadFile(uri: Uri) {
        val path = "file://" + uri.toString()
        val request = MultipartBody.Part.createFormData("file", path, RequestBody.create(MediaType.parse("video/*"), Uri.parse(path).toFile() ))
        postFileUploadUseCase.postFile(request)
            .subscribe({
//                if(it.status.toInt() == 200 )
//                {
                    view.uploadSuccess(it.message)
                    PidClass.videoObjectPid = it.datas.objectPid
                //}
                //else view.uploadFailed(it.message)
            },{
                view.uploadFailed(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    override fun uploadMaskFile(bitmap: Bitmap, frameTimeSec:Float, context: Context) {
        val file = BitmapToFileUtil(bitmap, context)
        val path = "file://" + file.toString()
        val request = MultipartBody.Part.createFormData("file", path , RequestBody.create(MediaType.parse("image/*"),Uri.parse(path).toFile()))
        postFileUploadUseCase.postFile(request)
            .subscribe({
                if(it.status.toInt() == 200 ) {
                    view.uploadSuccess(it.message)
                    PidClass.videoMaskObjectPid = it.datas.objectPid
                    resultUriToServerWithInfo(PidClass.videoMaskObjectPid, frameTimeSec, PidClass.videoObjectPid)
                }
                else view.uploadFailed(it.message)
            },{
                view.uploadFailed("로그인 후 가능")
            })
    }


    @SuppressLint("CheckResult")
    fun resultUriToServerWithInfo(maskPid: String, frameSec: Float, videoPid: String) {
        postPidNumberAndInfoUseCase.postPidNumberAndInfo(maskPid, frameSec, Consts.DEL_OBJ ,videoPid, "TEST")
            .subscribe({
                Log.e("pidNum", it.message)
            },{
                it.localizedMessage
            })
    }
}