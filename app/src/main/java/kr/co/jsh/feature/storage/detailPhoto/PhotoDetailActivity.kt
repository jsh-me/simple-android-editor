package kr.co.jsh.feature.storage.detailPhoto

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityDetailPhotoResultBinding

class PhotoDetailActivity :AppCompatActivity(){
    private lateinit var binding : ActivityDetailPhotoResultBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_photo_result)
        binding.resultPhoto = this@PhotoDetailActivity
    }

    private fun initView(){
        val result = intent.getStringExtra(Consts.DETAIL_PHOTO)
        Glide.with(this).asBitmap().load(result).into(binding.resultImageDetail)
    }

    fun backButton(){
        finish()
    }

}