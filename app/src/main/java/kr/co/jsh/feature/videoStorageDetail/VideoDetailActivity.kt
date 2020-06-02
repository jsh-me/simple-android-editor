package kr.co.jsh.feature.videoStorageDetail

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.exoplayer2.SimpleExoPlayer
import kr.co.domain.globalconst.Consts
import kr.co.domain.utils.toastShort
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityDetailVideoResultBinding
import kr.co.jsh.utils.permission.ScopeStorageFileUtil

class VideoDetailActivity : AppCompatActivity(), VideoDetailContract.View{
    private lateinit var binding : ActivityDetailVideoResultBinding
    private lateinit var presenter : VideoDetailPresenter
    private var videoUri :String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initPresenter()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_video_result)
        binding.resultVideo = this@VideoDetailActivity
    }

    private fun initPresenter(){
        presenter = VideoDetailPresenter(this)
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
        presenter.initPlayer(videoUri!!, this)
    }

    override fun onRestart() {
        super.onRestart()
        presenter.initPlayer(videoUri!!, this)
    }


    override fun onStop() {
        super.onStop()
        presenter.releasePlayer()
    }

    fun backBtn(){
        finish()
        presenter.releasePlayer()
    }
}