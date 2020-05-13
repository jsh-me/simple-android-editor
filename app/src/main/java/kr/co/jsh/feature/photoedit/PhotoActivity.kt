package kr.co.jsh.feature.photoedit

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import kr.co.jsh.databinding.ActivityPhotoBinding
import kr.co.jsh.localclass.BitmapImage
import kr.co.domain.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.jsh.R
import kr.co.jsh.utils.setupPermissions
import org.koin.android.ext.android.get



class PhotoActivity : AppCompatActivity() , PhotoContract.View{
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var presenter : PhotoPresenter
    var path = ""

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

        presenter = PhotoPresenter(this, get())
        setupPermissions(this) {
            extraIntent?.let {
                path = extraIntent.getStringExtra(EXTRA_PHOTO_PATH)
                presenter.setImageView(this, "file://"+path)
            }
        }
    }

    override fun displayPhotoView(bitmap: Bitmap) {
        binding.photoview.apply{
            setBackgroundResource(R.color.background_space)
            setBackgroundImage(bitmap, BackgroundType.BITMAP, BackgroundScale.CENTER_INSIDE)
        }
    }


    fun resetButton(v: View){
        binding.photoview.apply{
            restartDrawing()
        }
        initView()
    }

    fun uploadServer(){
        presenter.uploadFile("file://"+path)
    }

    //https://codechacha.com/ko/android-mediastore-insert-media-files/
    //Unknown URI: content://media/external_primary/images/media
    //오른쪽 위 아이콘
    fun savePhoto(v: View){
        val saveImage = binding.photoview.createCapture(DrawingCapture.BITMAP)
        presenter.uploadFrameFile(saveImage[0] as Bitmap, this)
    }



    override fun uploadSuccess(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }

    override fun uploadFailed(msg: String) {
        Toast.makeText(this, "$msg", Toast.LENGTH_SHORT).show()
    }
}