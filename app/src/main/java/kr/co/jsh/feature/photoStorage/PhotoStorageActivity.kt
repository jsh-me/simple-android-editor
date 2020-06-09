package kr.co.jsh.feature.photoStorage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.domain.globalconst.Consts
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityPhotoStorageBinding
import kr.co.jsh.feature.photoStorageDetail.PhotoDetailActivity
import kr.co.jsh.feature.photoedit.PhotoContract
import org.koin.android.ext.android.get


class PhotoStorageActivity : AppCompatActivity(){
    lateinit var binding : ActivityPhotoStorageBinding
   // override lateinit var presenter : PhotoStorageContract.Presenter
    private var response = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
       // initPresenter()
    }
    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_photo_storage)
        binding.photoStorage = this@PhotoStorageActivity
    }
//
//    private fun initPresenter(){
//        presenter = PhotoStoragePresenter(this, get(), get(), get(), get())
////        presenter.loadImageStorage()
//        response = intent.getIntExtra(Consts.LOGIN_RESPONSE, -1)
//        when(response){
//            200 -> {presenter.getServerFileResult()}
//            500 -> {
//                presenter.loadLocalFileStorageDB()
//                presenter.getLocalFileResult()
//            }
//        }
//    }
//
//    override fun setFileResult(list: ArrayList<List<String>>) {
//        if (list.isNullOrEmpty()) {
//            binding.noResultText.visibility = View.VISIBLE
//        } else {
//            binding.noResultText.visibility = View.GONE
//            binding.photoStorageRecycler.apply {
//                layoutManager = GridLayoutManager(this@PhotoStorageActivity, 2)
//                adapter = PhotoStorageAdapter(click(), list, context)
//            }
//        }
//        stopAnimation()
//    }
//
//    private fun click() = { _:Int, url: String ->
//        val intent = Intent(this, PhotoDetailActivity::class.java).apply{
//            putExtra(Consts.DETAIL_PHOTO, url)
//        }
//        startActivity(intent)
//    }
//
//    override fun startAnimation(){
//        binding.loadingAnimation.playAnimation()
//        binding.blockingView.visibility = View.VISIBLE
//        binding.loadingAnimation.visibility = View.VISIBLE
//    }
//
//    override fun stopAnimation(){
//        binding.loadingAnimation.cancelAnimation()
//        binding.blockingView.visibility = View.GONE
//        binding.loadingAnimation.visibility = View.GONE
//
//    }
//
    fun backBtn(){
        finish()
    }
}
