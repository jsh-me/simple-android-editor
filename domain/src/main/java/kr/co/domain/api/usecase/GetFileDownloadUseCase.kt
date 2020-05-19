package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.FileDownloadResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.RetrofitRepository

class GetFileDownloadUseCase(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun getFileDownload(objectPid: String) : Single<FileDownloadResponse> = fileService
        .getFileDownload(objectPid)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
