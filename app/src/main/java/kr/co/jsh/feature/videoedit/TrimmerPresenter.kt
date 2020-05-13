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
import kr.co.domain.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.jsh.utils.*
import java.io.File
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList

class TrimmerPresenter(override var view: TrimmerContract.View,
                       private var postFileUploadUseCase: PostFileUploadUseCase) : TrimmerContract.Presenter{
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

    override fun getResultUri(uri: Uri, context: Context) {
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
        }

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

        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    //val threshold = 10
                    val mediaMetadataRetriever = MediaMetadataRetriever()
                    mediaMetadataRetriever.setDataSource(context, mSrc)
                    //동영상의 총 길이

                    //frameWidth/Height = video_loader 의 너비,높이
                    //initialBitmap = 불러온 비디오 프레임의 bitmap
                    //cropWidth/height = timelineview에 하나씩 붙일 , 리사이즈된 프레임의 너비 높이
                    //numThumbs = 썸네일 보여줄 갯수

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
                                //frameWidth, frameHeight 이 0으로 나오는 오류가 발생함.
//                                bitmap = Bitmap.createScaledBitmap(bitmap, frameWidth, frameHeight, false)
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
            }
        })
            view.setThumbnailListView(thumbnailList)
    }

    override fun saveVideo(path: String, context:Context, mSrc: Uri,  start_sec: Int, end_sec: Int) {
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

    @SuppressLint("CheckResult")
    override fun uploadFile(uri: Uri) {
        val path = "file://" + uri.toString()
        postFileUploadUseCase.postFile(Uri.parse(path).toFile())
            .subscribe({
                if(it.status.toInt() == 200 )
                    view.uploadSuccess(it.message)
                else view.uploadFailed(it.message)
            },{
                view.uploadFailed(it.localizedMessage)
            })

    }
}