package kr.co.jsh.feature.storage.video

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.GetFileDownloadUseCase
import kr.co.domain.api.usecase.GetVideoResultIUseCase
import timber.log.Timber

class VideoStoragePresenter(override var view: VideoStorageContract.View,
                            var getVideoResultIUseCase: GetVideoResultIUseCase)
    :VideoStorageContract.Presenter{
    @SuppressLint("CheckResult")
    override fun getVideoResultFile(objectPid: String) {
        getVideoResultIUseCase.getVideoResult(objectPid)
            .subscribe({
                resultFileDownload(it.datas.resultFile.objectPid)
            },{

            })
    }

    @SuppressLint("CheckResult")
    private fun resultFileDownload(objectPid: String){
        view.setVideoResultView("http://192.168.0.188:8080/file/fileDownload.do?objectPid=${objectPid}")
    }
}