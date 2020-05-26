package kr.co.domain.api.usecase


import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.VideoResultResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.remote.RetrofitRepository

class GetVideoResultIUseCase(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun getVideoResult(objectPid: String) : Single<VideoResultResponse> = fileService
        .getVideoResult(objectPid)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
