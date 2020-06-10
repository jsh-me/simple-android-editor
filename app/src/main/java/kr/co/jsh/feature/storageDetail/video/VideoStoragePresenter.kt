package kr.co.jsh.feature.storageDetail.video

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory

class VideoStoragePresenter(override var view: VideoStorageContract.View)
    : VideoStorageContract.Presenter {
    private var mplayer: SimpleExoPlayer ?= null
    private var playbackPosition = 0L
    private var currentWindow = 0
    private var playWhenReady = true

    override fun initPlayer(result: String, context: Context) {
        mplayer = SimpleExoPlayer.Builder(context).build()
        view.setPlayer(mplayer!!)
        val defaultHttpDataSourceFactory =
            DefaultHttpDataSourceFactory("del.it")
        val mediaSource = ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory)
            .createMediaSource(Uri.parse(result))
        mplayer!!.apply{
            prepare(mediaSource)
            seekTo(currentWindow, playbackPosition)
            playWhenReady = playWhenReady
        }
    }

    override fun releasePlayer() {
        mplayer?.let{
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.release()
            mplayer = null
        }
    }
}