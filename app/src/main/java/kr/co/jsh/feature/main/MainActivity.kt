package kr.co.jsh.feature.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityMainBinding
import kr.co.domain.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.domain.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.domain.globalconst.Consts.Companion.REQUEST_VIDEO_CROPPER
import kr.co.domain.globalconst.Consts.Companion.REQUEST_VIDEO_TRIMMER
import kr.co.jsh.login.LoginAccountDialog
import kr.co.jsh.feature.photoedit.PhotoActivity
import kr.co.jsh.feature.storage.photo.PhotoStorageActivity
import kr.co.jsh.feature.storage.video.VideoStorageActivity
import kr.co.jsh.feature.videoedit.TrimmerActivity
import kr.co.jsh.utils.FileUtils
import kr.co.jsh.utils.setupPermissions


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    var loginCheck : ObservableField<Boolean> = ObservableField(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setupDataBinding()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this@MainActivity
    }

    fun pickFromVideo(intentCode: Int) {
        setupPermissions(this) {
            val intent = Intent()
            intent.setTypeAndNormalize("video/*")
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "label_select_video"), intentCode)
        }
    }

    fun pickFromPicture(intentCode: Int) {
        setupPermissions(this) {
            val intent = Intent()
            intent.setTypeAndNormalize("image/*")
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "label_select_picture"), intentCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_TRIMMER) {
                val selectedUri = data!!.data
                if (selectedUri != null) {
                    startTrimActivity(selectedUri)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "toast_cannot_retrieve_selected_video",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (requestCode == REQUEST_VIDEO_CROPPER) {
                val selectedUri = data!!.data
                if (selectedUri != null) {
                    startPhotoActivity(selectedUri)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "toast_cannot_retrieve_selected_video",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        else if(resultCode == 1000 && requestCode == 1000) {
            binding.accountImg.setImageDrawable(resources.getDrawable(R.drawable.sehee, null))
            loginCheck.set(true)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun accountCircleImage(){
        val intent = Intent(this, LoginAccountDialog::class.java)
        startActivityForResult(intent, 1000)
    }

    fun photoStorageBtn(){
        val intent = Intent(this, PhotoStorageActivity::class.java)
        startActivity(intent)
    }

    fun videoStorageBtn(){
        val intent = Intent(this, VideoStorageActivity::class.java)
        startActivity(intent)
    }

    private fun startTrimActivity(uri: Uri) {
        val intent = Intent(this, TrimmerActivity::class.java)
        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri))
        startActivity(intent)
    }

    private fun startPhotoActivity(uri: Uri) {
        val intent = Intent(this, PhotoActivity::class.java)
        intent.putExtra(EXTRA_PHOTO_PATH, FileUtils.getPath(this, uri))
        startActivity(intent)
    }


}
