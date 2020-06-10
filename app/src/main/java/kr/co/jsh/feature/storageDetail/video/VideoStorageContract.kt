package kr.co.jsh.feature.storageDetail.video

import android.content.Context
import com.google.android.exoplayer2.SimpleExoPlayer

interface VideoStorageContract {
    interface View {
        fun setPlayer(player: SimpleExoPlayer)
    }
    interface Presenter {
        var view: View
        fun initPlayer(result: String, context: Context)
        fun releasePlayer()
    }
}