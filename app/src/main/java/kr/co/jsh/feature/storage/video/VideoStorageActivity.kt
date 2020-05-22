package kr.co.jsh.feature.storage.video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityVideoStorageBinding

class VideoStorageActivity : AppCompatActivity() {
    private lateinit var binding : ActivityVideoStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_storage)
        binding.videoStorage = this@VideoStorageActivity
    }

    fun backButton(){
        finish()
    }
}