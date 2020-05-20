package kr.co.jsh.feature.storage.photo

import kr.co.domain.api.usecase.GetFileDownloadUseCase

class PhotoStoragePresenter(override var view: PhotoStorageContract.View,
                            private var getFileDownloadUseCase: GetFileDownloadUseCase)
    :PhotoStorageContract.Presenter{
    override fun getResultFile(objectPid: String) {
        getFileDownloadUseCase.getFileDownload(objectPid)
            .subscribe({

            },{

            })
    }
}