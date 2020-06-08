package kr.co.jsh.feature.main

import android.annotation.SuppressLint
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.api.usecase.*
import kr.co.domain.globalconst.Consts
import kr.co.domain.globalconst.UrlConst
import timber.log.Timber

class MainPresenter(override var view: MainContract.View,
                    private var getAllVideoResultIUseCase: GetAllVideoResultListUseCase,
                    private var getAllImageResultUseCase: GetAllImageResultListUseCase,
                    private var insertFileDataBaseUseCase: InsertFileDataBaseUseCase,
                    private var allLoadFileDataBaseUseCase: AllLoadFileDataBaseUseCase,
                    private var allDeleteFileDataBaseUseCase: AllDeleteFileDataBaseUseCase
): MainContract.Presenter{
    private val addRoomDBStorage : ArrayList<List<String>> = ArrayList()  //0: url, 1: fileName, 2: fileType
    private val addServerStorage : ArrayList<List<String>> = ArrayList()
    private val resultVideoList = ArrayList<String>()
    private val resultImageList = ArrayList<String>()
    private val localResultList = ArrayList<String>()

    @SuppressLint("CheckResult")
    override fun loadLocalFileStorageDB() {
        view.startAnimation()
        allLoadFileDataBaseUseCase.allLoad()
            .subscribe({
                it.map {
                    Timber.e("11local")
                    localResultList.clear()
                    localResultList.add(it.path)
                    localResultList.add(it.filename)
                    localResultList.add(it.fileType)
                    addRoomDBStorage.add(localResultList) //set 함수
                    Timber.e("22local")
                }
                Timber.e("onComplete")
                view.setFileResult(addRoomDBStorage)
            }, {
                Timber.e("Error getting info from interactor (video)")
            })

    }

    @SuppressLint("CheckResult")
    override fun getServerFileResult() {
        allDeleteStorage()
        view.startAnimation()
        loadServerVideoFile()
        loadServerImageFile()
        view.setFileResult(addServerStorage)
       // insertDataBase(addServerStorage)
    }

    @SuppressLint("CheckResult")
    private fun loadServerVideoFile(){
        getAllVideoResultIUseCase.getAllVideoResult()
            .subscribe({
                it.datas.list.map{ video ->
                    video.resultFile?.objectPid?.let {obj ->
                        Timber.e("11video")
                        resultVideoList.clear()
                        resultVideoList.add("${UrlConst.DOWNLOAD_URL}$obj")
                        resultVideoList.add(obj)
                        resultVideoList.add("video")
                        addServerStorage.add(resultVideoList)
                        //insertDataBase(addServerStorage)
                        Timber.e("22video")
                    }
                }
                view.refreshView(addServerStorage)
            },{
                Timber.e(it.localizedMessage)
            })
    }

    @SuppressLint("CheckResult")
    private fun loadServerImageFile(){
        getAllImageResultUseCase.getAllImageResult()
            .subscribe({
                it.datas.list.map{ image ->
                    image.resultFile?.objectPid?.let{obj ->
                        Timber.e("11image")
                        resultImageList.clear()
                        resultImageList.add("${UrlConst.DOWNLOAD_URL}$obj")
                        resultImageList.add(obj)
                        resultImageList.add("image")
                        addServerStorage.add(resultImageList)
                       // insertDataBase(addServerStorage)
                        Timber.e("22image")
                    }
                }
                view.refreshView(addServerStorage)
            },{
                Timber.e(it.localizedMessage)
            })
    }

    override fun getLocalFileResult() {
        view.startAnimation()
//        view.setFileResult(addRoomDBStorage)
    }

    //all delete db
    private fun allDeleteStorage(){
        allDeleteFileDataBaseUseCase.allDelete()
    }

    //insert db
    private fun insertDataBase(storage: ArrayList<List<String>>){
        insertFileDataBaseUseCase.insert(ResultFileStorage(null, storage[storage.size-1][0], storage[storage.size-1][1], storage[storage.size-1][2]))
    }
}