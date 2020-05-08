package kr.co.jsh.feature.videoedit

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.LongSparseArray
import android.widget.VideoView
import kr.co.jsh.customview.TimeLineView

interface TrimmerContract {
    interface View{
        fun onVideoPrepared() //OnVideoListener
        fun onError(message: String)
        fun cancelAction()
        fun onTrimStarted()
        fun initialBordering(crop_time:  ArrayList<Pair<Int, Int>>)
        fun videoPath(path : String)

        fun resetCropView()

        fun setThumbnailListView(thumbnailList :  LongSparseArray<Bitmap>)

    }
    interface Presenter{
        var view: View
        fun getResult(progressDialog: VideoProgressIndeterminateDialog, context: Context,  uri: Uri) //OnTrimVideoListener
        fun crop(context: Context, crop_time: ArrayList<Pair<Int, Int>>, cropCount: Int, mBitmaps: LongSparseArray<Bitmap>, timeLineView: TimeLineView, videoLoader: VideoView)
        fun prepareVideoPath(extraIntent: Intent)
        fun resetCrop(context:Context, crop_time: ArrayList<Pair<Int, Int>>)

        fun getThumbnailList(mSrc: Uri, context: Context)
    }
}