package kr.co.jsh.feature.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityMainBinding
import kr.co.domain.globalconst.Consts.Companion.EXTRA_PHOTO_PATH
import kr.co.domain.globalconst.Consts.Companion.EXTRA_VIDEO_PATH
import kr.co.domain.globalconst.Consts.Companion.REQUEST_VIDEO_CROPPER
import kr.co.domain.globalconst.Consts.Companion.REQUEST_VIDEO_TRIMMER
import kr.co.domain.utils.loadDrawable
import kr.co.domain.utils.toastShort
import kr.co.jsh.feature.login.LoginAccountDialog
import kr.co.jsh.feature.photoStorageDetail.PhotoDetailActivity
import kr.co.jsh.feature.photoedit.PhotoActivity
import kr.co.jsh.feature.videoStorageDetail.VideoDetailActivity
import kr.co.jsh.feature.videoedit.TrimmerActivity
import kr.co.jsh.singleton.UserObject
import kr.co.jsh.utils.permission.FileUtils
import kr.co.jsh.utils.permission.setupPermissions
import timber.log.Timber
import org.koin.android.ext.android.get


//1. Non-public, non-static field names start with m.
//2. Static field names start with s.
//3. Other fields start with a lower case letter.
//4. Public static final fields (constants) are ALL_CAPS_WITH_UNDERSCORES.

class MainActivity : AppCompatActivity(), MainContract.View {
    private lateinit var binding: ActivityMainBinding
    override lateinit var presenter: MainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initView()
        getMyToken()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.main = this@MainActivity
        binding.videoButton.setBackgroundResource(R.drawable.main_video)
        binding.photoButton.setBackgroundResource(R.drawable.main_photo)
        binding.mainImageView.loadDrawable(resources.getDrawable(R.drawable.main_view, null))
    }

    private fun initView(){
        presenter = MainPresenter(this, get(), get(), get(), get(), get())
        // when response 500
        presenter.loadLocalFileStorageDB()
    }

    override fun setFileResult(list: ArrayList<List<String>>) {
        Timber.e("OnResume : ${UserObject.loginResponse}")
        binding.mainResultRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, true)
            adapter = MainAdapter(click(), list, context)
            scrollToPosition(list.size-1)
        }
        stopAnimation()
    }

    override fun refreshView(list: ArrayList<List<String>>) {
        binding.mainResultRecycler.apply{
            adapter?.notifyDataSetChanged()
            scrollToPosition(list.size-1)
        }

        presenter.insertResultToLocalDB(list)
    }

    override fun startAnimation() {
        binding.loadingAnimation.playAnimation()
        binding.blockingView.visibility = View.VISIBLE
        binding.loadingAnimation.visibility = View.VISIBLE
    }

    override fun stopAnimation() {
        binding.loadingAnimation.cancelAnimation()
        binding.blockingView.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
    }

    private fun click() = { _: Int, url: String, type: String ->
       val intent = if(type == "video") {
            Intent(this, VideoDetailActivity::class.java).apply {
                putExtra(Consts.DETAIL_VIDEO, url)
            }
        } else {
            Intent(this, PhotoDetailActivity::class.java).apply{
                putExtra(Consts.DETAIL_PHOTO, url)
            }
        }
        startActivity(intent)
    }

    private fun getMyToken(){
        CoroutineScope(Dispatchers.Default).async {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.i("getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }
                    val token = task.result?.token
                    Timber.e(token!!)
                })
        }
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
                selectedUri?.let{
                    startTrimActivity(selectedUri)
                }?:run {
                    this.toastShort("toast_cannot_retrieve_selected_video")
                }
            } else if (requestCode == REQUEST_VIDEO_CROPPER) {
                val selectedUri = data!!.data
                selectedUri?.let{
                    startPhotoActivity(selectedUri)
                } ?:run {
                    this.toastShort(  "toast_cannot_retrieve_selected_video")
                }
            }
        }
        else if(resultCode == 1000 && requestCode == 1000) {
            binding.accountImg.apply{
                setImageDrawable(resources.getDrawable(R.drawable.sehee, null))
                isClickable = false
            }
            UserObject.loginResponse = 200
            presenter.getServerFileResult()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun accountCircleImage(){
        if(UserObject.loginResponse == 200) {this.toastShort( "이미 로그인되어 있습니다.") }
        else {
            val intent = Intent(this, LoginAccountDialog::class.java)
            startActivityForResult(intent, 1000)
        }
    }


    private fun startTrimActivity(uri: Uri?) {
        uri?.let{this.toastShort("이용할 수 없는 비디오 입니다.")}?:run {
            val intent = Intent(this, TrimmerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this@MainActivity, uri!!))
            }
            startActivity(intent)
        }
    }

    private fun startPhotoActivity(uri: Uri?) {
        uri?.let{this.toastShort("이용할 수 없는 사진 입니다.")}?:run {
            val intent = Intent(this, PhotoActivity::class.java).apply {
                putExtra(EXTRA_PHOTO_PATH, FileUtils.getPath(this@MainActivity, uri!!))
            }
            startActivity(intent)
        }
    }
}
