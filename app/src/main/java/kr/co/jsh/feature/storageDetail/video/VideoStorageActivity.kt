package kr.co.jsh.feature.storageDetail.video

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import kr.co.domain.globalconst.Consts
import kr.co.domain.utils.toastShort
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityDetailVideoResultBinding
import kr.co.jsh.utils.permission.ScopeStorageFileUtil

class VideoStorageActivity : AppCompatActivity(),
    VideoStorageContract.View {
    private lateinit var binding : ActivityDetailVideoResultBinding
    private lateinit var presenterVideo : VideoStoragePresenter
    private var videoUri :String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initPresenter()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_video_result)
        binding.resultVideo = this@VideoStorageActivity
    }

    private fun initPresenter(){
        presenterVideo = VideoStoragePresenter(this)
        videoUri = intent.getStringExtra(Consts.DETAIL_VIDEO)?:""
    }

    override fun setPlayer(player: SimpleExoPlayer) {
        binding.videoDetailView.player = player
    }


    fun saveBtn(){
        val displayName = "${System.currentTimeMillis()}.mp4"
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        ScopeStorageFileUtil.downloadURL(videoUri!!, downloadManager, displayName, this)
        this.toastShort("저장 완료")
    }

    override fun onResume() {
        super.onResume()
        presenterVideo.initPlayer(videoUri!!, this)
    }

    override fun onRestart() {
        super.onRestart()
        presenterVideo.initPlayer(videoUri!!, this)
    }


    override fun onStop() {
        super.onStop()
        presenterVideo.releasePlayer()
    }

    fun backBtn(){
        finish()
        presenterVideo.releasePlayer()
    }
}