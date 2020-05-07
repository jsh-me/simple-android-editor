package kr.co.jsh.videoedit

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_trimmer.*
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityTrimmerBinding
import kr.co.jsh.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.jsh.interfaces.OnTrimVideoListener
import kr.co.jsh.interfaces.OnVideoListener
import kr.co.jsh.utils.RunOnUiThread
import kr.co.jsh.utils.setupPermissions
import java.io.File


class TrimmerActivity : AppCompatActivity() ,OnTrimVideoListener,OnVideoListener {
    lateinit var binding: ActivityTrimmerBinding

    private val progressDialog: VideoProgressIndeterminateDialog by lazy {
        VideoProgressIndeterminateDialog(
            this,
            "Cropping video. Please wait..."
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trimmer)
        binding.trimmer = this@TrimmerActivity
        Environment.getExternalStorageState()

        setupPermissions(this) {
            val extraIntent = intent
            var path = ""
//            if (extraIntent != null) path =
//                extraIntent.getStringExtra(EXTRA_VIDEO_PATH)

            extraIntent?.let{
                path =  extraIntent.getStringExtra(EXTRA_VIDEO_PATH)
            }
//            videoTrimmer.setTextTimeSelectionTypeface(FontsHelper[this, FontsConstants.SEMI_BOLD])

            videoTrimmer
                .setOnTrimVideoListener(this)
                .setOnVideoListener(this)
                .setVideoURI(Uri.parse(path))
                .setVideoInformationVisibility(true)
//                .setDestinationPath("/document/" + File.separator + "returnable" + File.separator + "Videos" + File.separator)

                .setDestinationPath(Environment.getExternalStorageDirectory().toString() + File.separator + "returnable" + File.separator + "Videos" + File.separator)
        }
    }


    fun back(view: View){
        videoTrimmer.onCancelClicked()
    }

    fun save(view: View) {
        videoTrimmer.onSaveClicked()
    }


    override fun onTrimStarted() {
        RunOnUiThread(this).safely {
            Toast.makeText(this, "Started Trimming", Toast.LENGTH_SHORT).show()
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
        Log.e("ERROR", message)
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
