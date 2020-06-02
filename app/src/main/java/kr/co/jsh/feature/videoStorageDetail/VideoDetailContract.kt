package kr.co.jsh.feature.videoStorageDetail

import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer

interface VideoDetailContract {
    interface View {
        fun setPlayer(player: SimpleExoPlayer)
    }
    interface Presenter {
        var view: View
        fun initPlayer(result: String, context: Context)
        fun releasePlayer()
    }
}