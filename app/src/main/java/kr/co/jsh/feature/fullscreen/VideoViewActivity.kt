package kr.co.jsh.feature.fullscreen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityFullscreenVideoViewBinding

class VideoViewActivity : AppCompatActivity(){
    private lateinit var binding : ActivityFullscreenVideoViewBinding
    private lateinit var videoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fullscreen_video_view)
        binding.fullscreen = this@VideoViewActivity
    }

    private fun initView(){
        if (intent.extras == null || !intent.hasExtra(Consts.VIDEO_URI)) {
            finish()
        }
        videoUrl = intent.getStringExtra(Consts.VIDEO_URI)
        val videoCurrentPosition = intent.getIntExtra(Consts.VIDEO_CURRENT_POSITION, 0)
        binding.videoFullScreen.setVideoURI(Uri.parse(videoUrl))
        binding.videoFullScreen.seekTo(videoCurrentPosition)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoFullScreen)
        binding.videoFullScreen.setMediaController(mediaController)
    }

    override fun onPause() {
        super.onPause()
        binding.videoFullScreen.stopPlayback()
    }

    override fun onResume() {
        super.onResume()
        binding.videoFullScreen.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent().apply{
            putExtra(Consts.VIDEO_CURRENT_POSITION, binding.videoFullScreen.currentPosition)
        }
        setResult(1000, intent)
    }
}