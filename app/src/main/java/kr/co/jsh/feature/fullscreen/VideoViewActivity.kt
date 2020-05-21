package kr.co.jsh.feature.fullscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.FullscreenVideoviewBinding

class VideoViewActivity : AppCompatActivity(){
    private lateinit var binding : FullscreenVideoviewBinding
    private lateinit var videoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.fullscreen_videoview)
        binding.fullscreen = this@VideoViewActivity
    }

    private fun initView(){
        if (intent.extras == null || !intent.hasExtra(Consts.VIDEO_URI)) {
            finish()
        }
        videoUrl = intent.getStringExtra(Consts.VIDEO_URI)
        val videoCurrentPosition = intent.getIntExtra(Consts.VIDEO_CURRENT_POSITION, 0)
        binding.fullScreenVideoView.setVideoURI(Uri.parse(videoUrl))
        binding.fullScreenVideoView.seekTo(videoCurrentPosition)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.fullScreenVideoView)
        binding.fullScreenVideoView.setMediaController(mediaController)
    }

    override fun onPause() {
        super.onPause()
        binding.fullScreenVideoView.stopPlayback()
    }

    override fun onResume() {
        super.onResume()
        binding.fullScreenVideoView.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent().apply{
            putExtra(Consts.VIDEO_CURRENT_POSITION, binding.fullScreenVideoView.currentPosition)
        }
        setResult(1000, intent)
    }
}