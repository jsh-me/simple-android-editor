package kr.co.jsh.feature.videoedit

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.LongSparseArray
import android.widget.Toast
import android.widget.VideoView
import kr.co.jsh.customview.TimeLineView
import kr.co.jsh.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.jsh.utils.BackgroundExecutor
import kr.co.jsh.utils.RunOnUiThread
import kr.co.jsh.utils.ScreenSizeUtil
import timber.log.Timber
import java.io.File

class TrimmerPresenter(override var view: TrimmerContract.View) : TrimmerContract.Presenter{

    override fun getResult(progressDialog: VideoProgressIndeterminateDialog, context: Context, uri: Uri) {
        RunOnUiThread(context).safely {
            Timber.i("Video saved at ${uri.path}")
            progressDialog.dismiss()
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
            Timber.e(id.toString())
        }
    }

    override fun crop(context: Context, crop_time: ArrayList<Pair<Int, Int>>, cropCount: Int, mBitmaps: LongSparseArray<Bitmap>, timeLineView: TimeLineView, videoLoader:VideoView) {
        var bitmapArrayList : LongSparseArray<Bitmap> = mBitmaps
        var crop_x1 = 0
        var crop_x2 = 0
        when (cropCount) {
            1 -> {
                bitmapArrayList = mBitmaps
                crop_x1 =
                    ( videoLoader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels)) /  videoLoader.duration
                //미리 두번 넣고 추후에 수정하자.
                crop_time.add(Pair(crop_x1,  videoLoader.currentPosition))//2
                Log.i("size:", "${crop_time.size}")
                crop_time.add(Pair(crop_x1,  videoLoader.currentPosition))//3
                Log.i("size:", "${crop_time.size}")

                timeLineView.cropView(bitmapArrayList, crop_x1, crop_x2, cropCount)
            }
            2 -> {
                crop_x2 =
                    ( videoLoader.currentPosition * (timeLineView.width - ScreenSizeUtil(context).widthPixels)) /  videoLoader.duration

                //요렇게하면 crop_time은 좌표값이 작은 순부터 큰 순으로 자동정렬 되겠지
                if (crop_x1 < crop_x2) {
                    crop_time[2] = Pair(crop_x2,  videoLoader.currentPosition)
                } else {
                    crop_time[1] = Pair(crop_x2,  videoLoader.currentPosition)
                }

                crop_time.add(
                    Pair(
                        timeLineView.width - ScreenSizeUtil(context).widthPixels,
                        videoLoader.duration
                    )
                ) //4
                timeLineView.cropView(bitmapArrayList, crop_x1, crop_x2, cropCount)
                view.initialBordering(crop_time)
            }
            else -> {
                Toast.makeText(context, "CROP은 두 번만 가능 !", Toast.LENGTH_LONG).show()
            }
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
            crop_time.add(Pair(0, 0))//1
            view.resetCropView()
        } catch (e: Exception) {
            Toast.makeText(context, "잘라진 것이 없어요!", Toast.LENGTH_LONG).show()
        }
    }

    override fun getThumbnailList(mSrc: Uri, context:Context) {
        val thumbnailList = LongSparseArray<Bitmap>()

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
                    var numThumbs = if(videoLengthInMs>=3000) Math.ceil(videoLengthInMs / 3000.0).toInt() else 1
                    val cropHeight = 150 //timelineview에서 한 프레임의 너비 (동적으로 변경되게끔 코드 수정해야함!)
                    val cropWidth = ScreenSizeUtil(context).widthPixels/4 //timelineview에서 한 프레임의 너비

                    //val interval = videoLengthInMs / numThumbs
                    val interval = if(videoLengthInMs< 3000) videoLengthInMs*1000 else 3000*1000

                    for (i in 0 until numThumbs) {
                        var bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
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
                            thumbnailList.put(i.toLong(), bitmap)
                            Log.i("1:","${thumbnailList.size()}")

                        }
                    }
                    mediaMetadataRetriever.release()
                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e)
                }
                Log.i("return 직전:","${thumbnailList.size()}")
            }
        })
        view.setThumbnailListView(thumbnailList)
    }

}