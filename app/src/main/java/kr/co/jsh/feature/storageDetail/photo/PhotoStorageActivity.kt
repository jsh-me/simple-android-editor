package kr.co.jsh.feature.storageDetail.photo

import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.domain.globalconst.Consts
import kr.co.domain.utils.loadUrl
import kr.co.domain.utils.toastShort
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityDetailPhotoResultBinding
import kr.co.jsh.utils.permission.ScopeStorageFileUtil

class PhotoStorageActivity :AppCompatActivity(),
    PhotoStorageContract.View {
    private lateinit var binding : ActivityDetailPhotoResultBinding
    private lateinit var presenter : PhotoStoragePresenter
    private var resourceBitmap: Bitmap ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initPresenter()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_photo_result)
        binding.resultPhoto = this@PhotoStorageActivity
    }

    private fun initPresenter(){
        val result = intent.getStringExtra(Consts.DETAIL_PHOTO)?:""
        presenter = PhotoStoragePresenter(this)
        binding.imageDetailView.loadUrl(result)
    }

    fun saveBtn(){
        val displayName = "${System.currentTimeMillis()}.jpg"
        val mimeType = "image/jpeg"
        val compressFormat = Bitmap.CompressFormat.JPEG
        resourceBitmap?.let{ ScopeStorageFileUtil.addPhotoAlbum(resourceBitmap!!, displayName, mimeType, compressFormat, this) }
        this.toastShort("저장 완료")
    }

    fun backBtn(){
        finish()
    }

}