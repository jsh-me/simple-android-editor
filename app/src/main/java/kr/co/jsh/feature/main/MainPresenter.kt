package kr.co.jsh.feature.main

import android.annotation.SuppressLint
import kotlinx.atomicfu.AtomicInt
import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.api.usecase.*
import kr.co.domain.globalconst.UrlConst
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger

class MainPresenter(override var view: MainContract.View,
                    private var getAllVideoResultIUseCase: GetAllVideoResultListUseCase,
                    private var getAllImageResultUseCase: GetAllImageResultListUseCase,
                    private var insertFileDataBaseUseCase: InsertFileDataBaseUseCase,
                    private var allLoadFileDataBaseUseCase: AllLoadFileDataBaseUseCase,
                    private var allDeleteFileDataBaseUseCase: AllDeleteFileDataBaseUseCase
): MainContract.Presenter {
    private val addRoomDBStorage: ArrayList<List<String>> =
        ArrayList()  //0: url, 1: fileName, 2: fileType
    private val addServerStorage: ArrayList<List<String>> = ArrayList()
    private val mFlag = AtomicInteger(0)

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
        allDeleteStorage()
        loadServerVideoFile()
        loadServerImageFile()
        view.setFileResult(addServerStorage)
    }

    @SuppressLint("CheckResult")
    private fun loadServerVideoFile() {
        getAllVideoResultIUseCase.getAllVideoResult()
            .subscribe({
                it.datas.list.map { video ->
                    video.resultFile?.objectPid?.let { obj ->
                        Timber.e("11video")
                        addServerStorage.add(listOf("${UrlConst.DOWNLOAD_URL}$obj", obj, "video"))
                        Timber.e("22video")
                    }
                }
                view.refreshView(addServerStorage)
                Timber.e("pass-1")
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    private fun loadServerImageFile() {
        getAllImageResultUseCase.getAllImageResult()
            .subscribe({
                it.datas.list.map { image ->
                    image.resultFile?.objectPid?.let { obj ->
                        Timber.e("11image")
                        addServerStorage.add(listOf("${UrlConst.DOWNLOAD_URL}$obj",obj, "image"))
                        Timber.e("22image")
                    }
                }
                view.refreshView(addServerStorage)
                    insertResultToLocalDB(addServerStorage)
                    Timber.e("pass-2")
            }, {
                Timber.e(it.localizedMessage)
            })
    }

    override fun insertResultToLocalDB(list: ArrayList<List<String>>) {
        insertDataBase(list)
        Timber.e("server storage number: ${list.size}")
    }

    //all delete db
    private fun allDeleteStorage() {
        allDeleteFileDataBaseUseCase.allDelete()
    }

    //insert db
    private fun insertDataBase(storage: ArrayList<List<String>>) {
        for (i in storage.indices) {
            insertFileDataBaseUseCase.insert(ResultFileStorage(null, storage[i][0], storage[i][1], storage[i][2])) }
    }
}