package kr.co.jsh.feature.storage.detailVideo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityDetailVideoResultBinding

class VideoDetailActivity : AppCompatActivity(){
    private lateinit var binding : ActivityDetailVideoResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_video_result)
        binding.resultVideo = this@VideoDetailActivity
    }
    private fun initView(){
        val result = intent.getStringExtra(Consts.DETAIL_VIDEO)

    }

    fun backButton(){
        finish()
    }
}