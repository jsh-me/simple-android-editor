package kr.co.jsh.photoedit

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoBinding
import kr.co.jsh.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.jsh.utils.setupPermissions

class PhotoActivity : AppCompatActivity() , PhotoContract.View{
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var presenter : PhotoPresenter
    private var flag: ObservableField<Boolean> = ObservableField(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo)
        binding.photo = this@PhotoActivity
    }

    private fun initView() {
        val extraIntent = intent
        var path = ""
        presenter = PhotoPresenter(this)
        setupPermissions(this) {
            extraIntent?.let {
                path = extraIntent.getStringExtra(EXTRA_PHOTO_PATH)
                presenter.setImageView(flag, "file://"+path)
            }
        }
    }

    override fun displayImageView(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .placeholder(resources.getDrawable(R.drawable.ic_launcher_background, null))
            .into(binding.imageview)
    }

    override fun displayPhotoView(uri: Uri) {
//        binding.photoview.visibility = View.VISIBLE
//        binding.imageview.visibility = View.INVISIBLE

        Glide.with(this)
            .load(uri)
            .placeholder(resources.getDrawable(R.drawable.ic_launcher_background, null))
            .into(binding.photoview)
    }
}