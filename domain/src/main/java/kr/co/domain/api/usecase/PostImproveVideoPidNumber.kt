package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.ImagePidNumberResponse
import kr.co.data.response.VideoPidNumberResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.remote.RetrofitRepository

class PostImproveVideoPidNumber(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun postImproveVideoPidNumber(reqEditType: String, videoFileObjectPid : String, title: String) : Single<VideoPidNumberResponse> = fileService
        .postImproveVideoPidNumber(reqEditType, videoFileObjectPid, title)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}