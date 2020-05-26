package kr.co.jsh.feature.storage.video

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.GetFileDownloadUseCase
import kr.co.domain.api.usecase.GetVideoResultIUseCase
import timber.log.Timber

class VideoStoragePresenter(override var view: VideoStorageContract.View,
                            var getVideoResultIUseCase: GetVideoResultIUseCase,
                            var getFileDownloadUseCase: GetFileDownloadUseCase)
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
        getFileDownloadUseCase.getFileDownload(objectPid)
            .subscribe({
                view.setVideoResultView(it.file)
            },{
                Timber.e(it.localizedMessage)
            })
    }
}