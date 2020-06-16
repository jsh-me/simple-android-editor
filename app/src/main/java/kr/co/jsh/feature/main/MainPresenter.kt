package kr.co.jsh.feature.main

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import kr.co.data.entity.room.ResultFileStorage
import kr.co.data.entity.server.AllVideoResultList
import kr.co.domain.api.usecase.*
import kr.co.domain.globalconst.UrlConst
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class MainPresenter(override var view: MainContract.View,
                    private var insertFileDataBaseUseCase: InsertFileDataBaseUseCase,
                    private var allLoadFileDataBaseUseCase: AllLoadFileDataBaseUseCase,
                    private var allDeleteFileDataBaseUseCase: AllDeleteFileDataBaseUseCase,
                    private var postVideoSearchListUseCase: PostVideoSearchListUseCase,
                    private var postImageSearchListUseCase: PostImageSearchListUseCase
): MainContract.Presenter {
    private val addRoomDBStorage: ArrayList<List<String>> = ArrayList()  //0: url, 1: fileName, 2: fileType
    private val addServerStorage: ArrayList<List<String>> = ArrayList()
    private val mFlag = AtomicInteger(2) //thread control variable
    private var mExpected = 0

    private var mPageSize = 2
    private var mPageNum = 1

    @SuppressLint("CheckResult")
    override fun loadLocalFileStorageDB() {
        allLoadFileDataBaseUseCase.allLoad()
            .subscribe({
                it.map {
                    addRoomDBStorage.add(listOf(it.path, it.filename, it.fileType))
                }
                Timber.e("onComplete")
                view.setFileResult(addRoomDBStorage)
            }, {
                Timber.e("Error getting info from interactor (video)")
            })
      //  view.refreshView(addRoomDBStorage)
    }

    @SuppressLint("CheckResult")
    override fun getServerFileResult() {
        allDeleteStorage()
        loadServerVideoFile()
        loadServerImageFile()
    }

    @SuppressLint("CheckResult")
    private fun loadServerVideoFile() {
        postVideoSearchListUseCase.postVideoSearchList(mPageSize,mPageNum)
            .filter {!it.datas.isNullOrEmpty() }
            .subscribe({
                it.datas.map { video ->
                    video.resultFile?.objectPid?.let { obj ->
                        addServerStorage.add(listOf("${UrlConst.DOWNLOAD_URL}$obj", obj, "video"))
                    }
                }
                compareAndUpdate()
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    private fun loadServerImageFile() {
        postImageSearchListUseCase.postImageSearchList(mPageSize, mPageNum)
            .filter{!it.datas.isNullOrEmpty()}
            .subscribe({
                it.datas.map { image ->
                    image.resultFile?.objectPid?.let { obj ->
                        addServerStorage.add(listOf("${UrlConst.DOWNLOAD_URL}$obj",obj, "image"))
                    }
                }
                compareAndUpdate()
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    private fun compareAndUpdate(){
        mExpected++
        if(mFlag.compareAndSet(mExpected, 2)) {
            view.setFileResult(addServerStorage)
            view.refreshView(addServerStorage)
            insertDataBase(addServerStorage)
            mExpected = 0
        }
    }

  //  all delete db
    private fun allDeleteStorage() {
        allDeleteFileDataBaseUseCase.allDelete()
    }

    //insert db
    private fun insertDataBase(storage: ArrayList<List<String>>) {
        for (i in storage.indices) {
            insertFileDataBaseUseCase.insert(ResultFileStorage(null, storage[i][0], storage[i][1], storage[i][2])) }
    }
}