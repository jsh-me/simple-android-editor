package kr.co.jsh.feature.storage.photo

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.GetAllImageResultListUseCase
import timber.log.Timber

class PhotoStoragePresenter(override var view: PhotoStorageContract.View,
                            private var getAllImageResultListUseCase: GetAllImageResultListUseCase)
    :PhotoStorageContract.Presenter{

    @SuppressLint("CheckResult")
    override fun getAllVideoResultFile() {
        val resultImageList = ArrayList<String>()
        val resultImageName = ArrayList<String>()
        getAllImageResultListUseCase.getAllImageResult()
            .subscribe({
                it.datas.list.map{
                    it.resultFile?.objectPid?.let{
                        resultImageList.add("http://192.168.0.188:8080/file/fileDownload.do?objectPid=${it}")
                        resultImageName.add(it)
                    }
                }
                view.setAllImageResultView(resultImageList, resultImageName)
            },{
                Timber.e(it.localizedMessage)
            })
    }
}