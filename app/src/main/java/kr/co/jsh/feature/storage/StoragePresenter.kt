package kr.co.jsh.feature.storage

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.*
import kr.co.domain.globalconst.UrlConst
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger


class StoragePresenter(override var view: StorageContract.View,
                            private var insertFileDataBaseUseCase: InsertFileDataBaseUseCase,
                            private var allLoadFileDataBaseUseCase: AllLoadFileDataBaseUseCase,
                            private var allDeleteFileDataBaseUseCase: AllDeleteFileDataBaseUseCase,
                            private var postVideoSearchListUseCase: PostVideoSearchListUseCase,
                            private var postImageSearchListUseCase: PostImageSearchListUseCase)
    :StorageContract.Presenter{
    private val addRoomDBStorage: ArrayList<List<String>> = ArrayList()  //0: url, 1: fileName, 2: fileType
    private val addServerStorage: ArrayList<List<String>> = ArrayList()
    private val mFlag = AtomicInteger(2) //thread control variable
    private var mExpected = 0

    private var mPageSize = 4 //몇 개씩 반영?
    private var mPageNum = 1 // 몇 페이지 씩 반영?

    private var isEndVideoResult = false
    private var isEndImageResult = false

    private var isFirstAttached = true

    @SuppressLint("CheckResult")
    override fun loadLocalFileStorageDB() {
        view.startAnimation()
        allLoadFileDataBaseUseCase.allLoad()
            .subscribe({
                it.map {
                    Timber.e("11local")
                    addRoomDBStorage.add(listOf(it.path, it.filename, it.fileType))
                    Timber.e("22local")
                }
                Timber.e("onComplete")
                view.setFileResult(addRoomDBStorage)
            }, {
                Timber.e("Error getting info from interactor (video)")
            })
        view.refreshView(addRoomDBStorage)
    }

    @SuppressLint("CheckResult")
    override fun getServerFileResult() {
        view.startAnimation()
//        allDeleteStorage()
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
                mExpected++
                if(mFlag.compareAndSet(mExpected, 2)) {
                    Timber.e("pass-1")

                    if(isFirstAttached) view.setFileResult(addServerStorage)
                    view.refreshView(addServerStorage)
                    mPageNum++
                    mExpected = 0
                    isFirstAttached = false
                    Timber.e("pageNum is $mPageNum")
                }
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
                // insertResultToLocalDB(addServerStorage)
                mExpected++
                if(mFlag.compareAndSet(mExpected, 2)) {
                    Timber.e("pass-2")

                    if(isFirstAttached) view.setFileResult(addServerStorage)
                    view.refreshView(addServerStorage)
                    mPageNum++
                    mExpected = 0
                    isFirstAttached = false
                    Timber.e("pageNum is $mPageNum")
                }
            }, {
                Timber.e(it.localizedMessage)
                view.onError()
            })
    }

    @SuppressLint("CheckResult")
    override fun isAnyMoreNoData() {
      postVideoSearchListUseCase.postVideoSearchList(mPageSize, mPageNum+1)
            .filter{ it.datas.isNullOrEmpty() }
            .subscribe{  isEndVideoResult = true
                Timber.e("loading 판단 video: $mPageNum and $isEndVideoResult")}

      postImageSearchListUseCase.postImageSearchList(mPageSize, mPageNum+1)
            .filter{ it.datas.isNullOrEmpty() }
            .subscribe { isEndImageResult = true
                Timber.e("loading 판단 image: $mPageNum and $isEndVideoResult")}

        if(isEndImageResult && isEndVideoResult) view.stopAnimation()
        view.isEnd(isEndImageResult && isEndVideoResult)
    }

    //    override fun insertResultToLocalDB(list: ArrayList<List<String>>) {
//        insertDataBase(list)
//        Timber.e("server storage number: ${list.size}")
//    }


    //  all delete db
    private fun allDeleteStorage() {
        allDeleteFileDataBaseUseCase.allDelete()
    }

//    //insert db
//    private fun insertDataBase(storage: ArrayList<List<String>>) {
//        for (i in storage.indices) {
//            insertFileDataBaseUseCase.insert(ResultFileStorage(null, storage[i][0], storage[i][1], storage[i][2])) }
//    }
}