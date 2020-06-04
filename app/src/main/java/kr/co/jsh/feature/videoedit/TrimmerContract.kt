package kr.co.jsh.feature.videoedit

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.SimpleExoPlayer
import kr.co.jsh.base.edit.BasePresenter
import kr.co.jsh.base.edit.BaseView

interface TrimmerContract {
    interface View: BaseView<Presenter> {
        fun onVideoPrepared() //OnVideoListener
        fun onTrimStarted()
        fun setVideoPath(path: String)
        fun resetCropView()
        fun setThumbnailListView(thumbnailList: ArrayList<Bitmap>)
        fun setPairList(list:  ArrayList<Pair<Long, Long>>)
        fun getResult(uri:Uri)

        fun setPlayer(player: SimpleExoPlayer)
        fun setVideoPlayFlag(whenReady: Boolean)
        fun setVideoDuration(duration: Long)
        fun setDrawBitmap(bitmap: Bitmap)
    }

    interface Presenter: BasePresenter {
        var view: View
        fun getCropArrayList(context:Context, trimVideoTimeList:  ArrayList<Pair<Long, Long>>)
        fun setCuttingVideo(context: Context, cropCount: Int, trimVideoTimeList:  ArrayList<Pair<Long, Long>>, recycler: RecyclerView)
        fun getThumbnailList(mSrc: Uri, context: Context)
        fun trimVideo(path: String, context:Context, mSrc: Uri, start_sec: Int, end_sec: Int)
        fun getResultUri(uri:Uri, context:Context, option: String)
        fun uploadMaskFile(bitmap: Bitmap, frameTimeSec: Float, context: Context)

        //prepare video
        fun initPlayer(uri: Uri, context: Context)
        fun releasePlayer()
        fun isVideoPlay(whenReady: Boolean)
        fun getVideoCurrentPosition(): Float
        fun getVideoDuration()
        fun setVideoSeekTo(currentPosition: Long)
        fun getFrameBitmap(sec: Long)

    }
}