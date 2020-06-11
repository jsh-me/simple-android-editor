package kr.co.jsh.feature.storage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.Consts.Companion.LOGIN_RESPONSE
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityResultStorageBinding
import kr.co.jsh.feature.storageDetail.photo.PhotoStorageActivity
import kr.co.jsh.feature.storageDetail.video.VideoStorageActivity
import org.koin.android.ext.android.get
import timber.log.Timber


class StorageActivity : AppCompatActivity(), StorageContract.View {
    private lateinit var binding : ActivityResultStorageBinding
    override lateinit var presenter : StorageContract.Presenter
    private var response = 0
    private var mContentSize = 0
    private var mContentList = ArrayList<List<String>>()
    private var isEnd = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initPresenter()
        initVIew()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_result_storage)
        binding.storage = this@StorageActivity
    }

    private fun initPresenter(){
        presenter = StoragePresenter(this, get(), get(), get(), get(), get())
        response = intent.getIntExtra(LOGIN_RESPONSE, -1)
    }

    private fun initVIew(){
        presenter.getServerFileResult()
        presenter.isAnyMoreNoData()
    }

    override fun setFileResult(list: ArrayList<List<String>>) {
        if(list.isNullOrEmpty()){
            binding.noResultText.visibility = View.VISIBLE
        } else {
            binding.noResultText.visibility = View.GONE
            binding.storageRecycler.apply {
                layoutManager = GridLayoutManager(this@StorageActivity, 2)
                adapter = StorageAdapter(click(), mContentList)
            }
            val animator : RecyclerView.ItemAnimator? = binding.storageRecycler.itemAnimator
            (animator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private val recyclerViewScrollListener : RecyclerView.OnScrollListener =
        object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0 && mContentSize > 0) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    if(layoutManager.findLastCompletelyVisibleItemPosition() == mContentSize -1 && !isEnd.value!!){
                        Timber.d("mContentSize: ${mContentSize -1}")
                        Timber.d("find last..: ${layoutManager.findLastCompletelyVisibleItemPosition()}")
                        presenter.getServerFileResult()
                        presenter.isAnyMoreNoData()
                    }
                }
            }
        }

    override fun isEnd(b: Boolean) {
        isEnd.value = b
        Timber.d("isEnd is ${isEnd.value}")
    }

    private fun click() = { _: Int, url: String, type: String ->
        val intent = if (type == "video") {
            Intent(this, VideoStorageActivity::class.java).apply {
                putExtra(Consts.DETAIL_VIDEO, url)
            }
        } else {
            Intent(this, PhotoStorageActivity::class.java).apply {
                putExtra(Consts.DETAIL_PHOTO, url)
            }
        }
        startActivity(intent)
    }

    override fun refreshView(list: ArrayList<List<String>>) {
        Timber.d("last recycler view size is ${list.size}")
        mContentSize = list.size
        mContentList.clear()
        mContentList.addAll(list)

        binding.storageRecycler.apply{
            adapter?.notifyDataSetChanged()
            addOnScrollListener(recyclerViewScrollListener)
        }

        stopAnimation()
    }

    override fun startAnimation(){
        binding.loadingAnimation.playAnimation()
        binding.blockingView.visibility = View.VISIBLE
        binding.loadingAnimation.visibility = View.VISIBLE
    }

    override fun stopAnimation(){
        binding.loadingAnimation.cancelAnimation()
        binding.blockingView.visibility = View.GONE
        binding.loadingAnimation.visibility = View.GONE
    }

    fun backBtn(){
        finish()
    }
}