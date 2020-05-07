package kr.co.jsh.photoedit

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoBinding
import kr.co.jsh.globalconst.BitmapImage
import kr.co.jsh.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.jsh.utils.setupPermissions
import java.io.*


class PhotoActivity : AppCompatActivity() , PhotoContract.View{
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var presenter : PhotoPresenter
    private lateinit var bitmapImage : BitmapImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, kr.co.jsh.R.layout.activity_photo)
        binding.photo = this@PhotoActivity
    }

    private fun initView() {
        val extraIntent = intent
        var path = ""
        presenter = PhotoPresenter(this)
        setupPermissions(this) {
            extraIntent?.let {
                path = extraIntent.getStringExtra(EXTRA_PHOTO_PATH)
                presenter.setImageView(this, "file://"+path)
            }
        }
    }

    override fun displayPhotoView(bitmap: Bitmap) {
        binding.photoview.apply{
            setBackgroundResource(kr.co.jsh.R.color.background_space)
            setBackgroundImage(bitmap, BackgroundType.BITMAP, BackgroundScale.CENTER_INSIDE)
        }
      //  bitmapImage = BitmapImage(binding.photoview.bitma)
    }

//    fun drawButton(v: View){
//
//    }

    fun resetButton(v: View){
        binding.photoview.apply{
            restartDrawing()
        }
        initView()
    }

    //https://codechacha.com/ko/android-mediastore-insert-media-files/
    //Unknown URI: content://media/external_primary/images/media
    fun savePhoto(v: View){
        val saveImage = binding.photoview.createCapture(DrawingCapture.BITMAP)
        val bos = ByteArrayOutputStream()
        (saveImage[0] as Bitmap).compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapData:ByteArray = bos.toByteArray()
        val bs = ByteArrayInputStream(bitmapData)

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "my_image_q.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Images.Media
                .getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val item = contentResolver.insert(collection, values)!!

            contentResolver.openFileDescriptor(item, "w", null).use {
                // write something to OutputStream
                FileOutputStream(it!!.fileDescriptor).use { outputStream ->
                    val imageInputStream = bs
                    while (true) {
                        val data = imageInputStream.read()
                        if (data == -1) {
                            break
                        }
                        outputStream.write(data)
                    }
                    imageInputStream.close()
                    outputStream.close()
                }
            }

            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(item, values, null, null)


        }
        else{
            val inputStream = bs
            val filePath = "$filesDir/my_image.jpg"
            val outputStream = FileOutputStream(filePath)
            while (true) {
                val data = inputStream.read()
                if (data == -1) {
                    break
                }
                outputStream.write(data)
            }
            inputStream.close()
            outputStream.close()

            val values= ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "my_image6.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                put(MediaStore.Images.Media.DATA, filePath)
            }
            val item = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            contentResolver.update(item, values, null, null)

        }
    }
}