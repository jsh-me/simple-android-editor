package kr.co.jsh.feature.videoedit

import android.content.ContentUris
import android.content.ContentValues
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_trimmer.*
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityTrimmerBinding
import kr.co.jsh.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.jsh.interfaces.OnTrimVideoListener
import kr.co.jsh.interfaces.OnVideoListener
import kr.co.jsh.utils.RunOnUiThread
import kr.co.jsh.utils.setupPermissions
import timber.log.Timber
import java.io.File


class TrimmerActivity : AppCompatActivity() ,OnTrimVideoListener,OnVideoListener {
    private lateinit var binding: ActivityTrimmerBinding
    private lateinit var progressDialog : VideoProgressIndeterminateDialog
   // private lateinit var presenter : TrimmerPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Environment.getExternalStorageState()
        setupDatabinding()
        initView()
    }

    private fun initView(){
        setupPermissions(this) {
            val extraIntent = intent
            var path = ""

            extraIntent?.let{
                path =  extraIntent.getStringExtra(EXTRA_VIDEO_PATH)
            }
            videoTrimmer
                .setOnTrimVideoListener(this)
                .setOnVideoListener(this)
                .setVideoURI(Uri.parse(path))
                .setVideoInformationVisibility(true)
//                .setDestinationPath("/document/" + File.separator + "returnable" + File.separator + "Videos" + File.separator)

                .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator)
        }
    }

    private fun setupDatabinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trimmer)
        binding.trimmer = this@TrimmerActivity
        //presenter = TrimmerPresenter(this)
    }


    fun back(v: View){
        videoTrimmer.onCancelClicked()
    }

    fun save(v: View) {
        videoTrimmer.onSaveClicked()
        //presenter.getResult(progressDialog, this, uri = Uri )
    }


    override fun onTrimStarted() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "Started Trimming", Toast.LENGTH_SHORT).show()
            progressDialog = VideoProgressIndeterminateDialog(this, "Cropping Video. Please Wait...")
            progressDialog.show()
        }
    }

    override fun getResult(uri: Uri) {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "Video saved at ${uri.path}", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            val mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(this, uri)
            val duration =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    .toLong()
            val width =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    .toLong()
            val height =
                mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    .toLong()
            val values = ContentValues()
            values.put(MediaStore.Video.Media.DATA, uri.path)
            values.put(MediaStore.Video.VideoColumns.DURATION, duration)
            values.put(MediaStore.Video.VideoColumns.WIDTH, width)
            values.put(MediaStore.Video.VideoColumns.HEIGHT, height)
            val id = ContentUris.parseId(
                contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )
            )
            Log.e("VIDEO ID", id.toString())
        }
    }

    override fun cancelAction() {
        RunOnUiThread(this).safely {
            videoTrimmer.destroy()
            finish()
        }
    }

    override fun onError(message: String) {
        Timber.e(message)
    }

    override fun onVideoPrepared() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "onVideoPrepared", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
