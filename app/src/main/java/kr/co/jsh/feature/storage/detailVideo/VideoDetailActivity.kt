package kr.co.jsh.feature.storage.detailVideo

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityDetailVideoResultBinding
import kr.co.jsh.utils.permission.ScopeStorageFileUtil

class VideoDetailActivity : AppCompatActivity(){
    private lateinit var binding : ActivityDetailVideoResultBinding
    private var mplayer: SimpleExoPlayer ?= null
    private var result :String ?= null
    private var playbackPosition = 0L
    private var currentWindow = 0
    private var playWhenReady = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initializePlayer()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_video_result)
        binding.resultVideo = this@VideoDetailActivity
    }
    private fun initializePlayer(){
        result = intent.getStringExtra(Consts.DETAIL_VIDEO)
            mplayer = SimpleExoPlayer.Builder(this).build()
            binding.resultVideoDetail.player = mplayer
            val defaultHttpDataSourceFactory =
                DefaultHttpDataSourceFactory(getString(R.string.app_name))
            val mediaSource = ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory)
                .createMediaSource(Uri.parse(result))
            mplayer!!.apply{
                prepare(mediaSource)
                seekTo(currentWindow, playbackPosition)
                playWhenReady = playWhenReady
            }
    }

    private fun releasePlayer() {
        mplayer?.let {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady
            it.release()
            mplayer = null
        }
    }

    fun saveVideo(){
        val displayName = "${System.currentTimeMillis()}.mp4"
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        ScopeStorageFileUtil.downloadURL(result!!, downloadManager, displayName, this)
        Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onRestart() {
        super.onRestart()
        initializePlayer()
    }


    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    fun backButton(){
        finish()
        releasePlayer()
    }
}