package kr.co.jsh.feature.storage.video

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.GetAllVideoResultListUseCase
import timber.log.Timber

class VideoStoragePresenter(override var view: VideoStorageContract.View,
                            private var getAllVideoResultIUseCase: GetAllVideoResultListUseCase)
    :VideoStorageContract.Presenter{
    @SuppressLint("CheckResult")
    override fun getAllVideoResultFile() {
        getAllVideoResultIUseCase.getAllVideoResult()
            .subscribe({
                val resultVideoList = ArrayList<String>()
                val resultVideoName = ArrayList<String>()
                it.datas.list.map{
                    it.resultFile?.objectPid?.let{
                        resultVideoList.add("http://192.168.0.188:8080/file/fileDownload.do?objectPid=${it}")
                        resultVideoName.add(it)
                        }
                    }
                view.setAllVideoResultView(resultVideoList, resultVideoName)
            },{
                Timber.e(it.localizedMessage)
            })
    }



}