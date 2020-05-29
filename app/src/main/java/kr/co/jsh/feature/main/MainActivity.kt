package kr.co.jsh.feature.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityMainBinding
import kr.co.domain.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.domain.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.domain.globalconst.Consts.Companion.REQUEST_VIDEO_CROPPER
import kr.co.domain.globalconst.Consts.Companion.REQUEST_VIDEO_TRIMMER
import kr.co.jsh.feature.login.LoginAccountDialog
import kr.co.jsh.feature.photoedit.PhotoActivity
import kr.co.jsh.feature.storage.photo.PhotoStorageActivity
import kr.co.jsh.feature.storage.video.VideoStorageActivity
import kr.co.jsh.feature.videoedit.TrimmerActivity
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.permission.FileUtils
import kr.co.jsh.utils.permission.setupPermissions
import timber.log.Timber

//TODO: VIEW를 제외한 모든 로직은 빼자. plz~~~!
//TODO: 더, 더더 쪼개야 하느니라!
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        getMyToken()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this@MainActivity
        Timber.e("${UserObject.loginResponse}")
    }

    private fun getMyToken(){
        Thread(Runnable {
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Timber.i("getInstanceId failed", task.exception)
                            return@OnCompleteListener
                        }
                        val token = task.result?.token
                        Timber.e(token!!)
                    })
        }).start()
    }

    fun pickFromVideo(intentCode: Int) {
        setupPermissions(this) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "video/mp4"
            startActivityForResult(intent, intentCode)

        }
    }

    fun pickFromPicture(intentCode: Int) {
        setupPermissions(this) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, intentCode)
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
            binding.accountImg.apply{
                setImageDrawable(resources.getDrawable(R.drawable.sehee, null))
                isClickable = false
            }
            UserObject.loginResponse = 200

        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun accountCircleImage(){
        if(UserObject.loginResponse == 200) { Toast.makeText(this, "이미 로그인되어 있습니다.", Toast.LENGTH_SHORT).show() }
        else {
            val intent = Intent(this, LoginAccountDialog::class.java)
            startActivityForResult(intent, 1000)
        }
    }

    fun photoStorageBtn(){
        val intent = Intent(this, PhotoStorageActivity::class.java).apply{
            putExtra(Consts.LOGIN_RESPONSE, UserObject.loginResponse)
        }
        startActivity(intent)
    }

    fun videoStorageBtn(){
        val intent = Intent(this, VideoStorageActivity::class.java).apply{
            putExtra(Consts.LOGIN_RESPONSE, UserObject.loginResponse)
        }
        startActivity(intent)
    }

    private fun startTrimActivity(uri: Uri) {
        val intent = Intent(this, TrimmerActivity::class.java).apply{
            putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this@MainActivity, uri))
        }
            startActivity(intent)
    }

    private fun startPhotoActivity(uri: Uri) {
        val intent = Intent(this, PhotoActivity::class.java).apply{
            putExtra(EXTRA_PHOTO_PATH, FileUtils.getPath(this@MainActivity, uri))
        }
        startActivity(intent)
    }
}
