package kr.co.jsh.main

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityMainBinding
import kr.co.jsh.utils.FileUtils
import kr.co.jsh.videoedit.TrimmerActivity


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this@MainActivity


        //dialog 필요하면 ~
//        val dialogVew = layoutInflater.inflate(R.layout.dialog_layout, null)
//        val builder = AlertDialog.Builder(this)
//        builder.setView(dialogVew)
//            .setPositiveButton("확인") { dialog, which ->  }
//            .setNegativeButton("취소") { dialog, which ->  }
//            .show()
    }

    fun pickFromVideo(intentCode: Int) {
        setupPermissions {
            val intent = Intent()
            intent.setTypeAndNormalize("video/*")
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "label_select_video"), intentCode)
        }
    }

    fun pickFromPicture(intentCode: Int) {
        setupPermissions {
            val intent = Intent()
            intent.setTypeAndNormalize("image/*")
            intent.action = Intent.ACTION_GET_CONTENT
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(Intent.createChooser(intent, "label_select_picture"), intentCode)
            //startActivityForResult : 액티비티로 다시 돌아오기 위해 사용
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                   // startCropActivity(selectedUri)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "toast_cannot_retrieve_selected_video",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun startTrimActivity(uri: Uri) {
        val intent = Intent(this, TrimmerActivity::class.java)
        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri))
        startActivity(intent)
    }

//    private fun startCropActivity(uri: Uri) {
//        val intent = Intent(this, EditImageActivity::class.java)
//        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri))
//        startActivity(intent)
//    }

    companion object {
        const val REQUEST_VIDEO_TRIMMER = 0x01
        const val REQUEST_VIDEO_CROPPER = 0x02
        internal const val EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH"
    }

    lateinit var doThis: () -> Unit
    private fun setupPermissions(doSomething: () -> Unit) {
        val writePermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        doThis = doSomething
        if (writePermission != PackageManager.PERMISSION_GRANTED && readPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                101
            )
        } else doThis()
    }
}
