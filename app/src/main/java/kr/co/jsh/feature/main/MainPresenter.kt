package kr.co.jsh.feature.main

import android.annotation.SuppressLint
import io.reactivex.schedulers.Schedulers
import kr.co.data.entity.room.ResultFileStorage
import kr.co.domain.api.usecase.*
import timber.log.Timber

class MainPresenter(override var view: MainContract.View,
                    private var getAllVideoResultIUseCase: GetAllVideoResultListUseCase,
                    private var insertFileDataBaseUseCase: InsertFileDataBaseUseCase,
                    private var allLoadFileDataBaseUseCase: AllLoadFileDataBaseUseCase,
                    private var allDeleteFileDataBaseUseCase: AllDeleteFileDataBaseUseCase
): MainContract.Presenter{
    private val addRoomDBStorage : ArrayList<List<String>> = ArrayList()  //0: url, 1: fileName, 2: fileType
    private val addServerStorage : ArrayList<List<String>> = ArrayList()
    private val resultList = ArrayList<String>()

    @SuppressLint("CheckResult")
    override fun loadLocalFileStorageDB() {
        allLoadFileDataBaseUseCase.allLoad()
            .subscribe({
                it.map {
                    Timber.e("11")
                    resultList.clear()
                    resultList.add(it.path)
                    resultList.add(it.filename)
                    resultList.add(it.fileType)
                    addRoomDBStorage.add(resultList) //set 함수
                    Timber.e("22")
                }
                //  view.successLoadDB()
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

        getAllVideoResultIUseCase.getAllVideoResult()
            .subscribe({
                it.datas.list.map{
                    it.resultFile?.objectPid?.let {
                        resultList.clear()
                        resultList.add("http://192.168.0.188:8080/file/fileDownload.do?objectPid=${it}")
                        resultList.add(it)
                    }
                    
                    it.videoFile.fileType.let{
                        resultList.add(it)
                    }

                    addServerStorage.add(resultList)
                    insertDataBase(addServerStorage)
                }
                view.setFileResult(addServerStorage)
            },{
                Timber.e(it.localizedMessage)
            })
    }

    override fun getLocalFileResult() {
        view.startAnimation()
        view.setFileResult(addRoomDBStorage)
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