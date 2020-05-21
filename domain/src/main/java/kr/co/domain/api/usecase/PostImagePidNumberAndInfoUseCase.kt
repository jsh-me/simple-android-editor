package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.ImagePidNumberResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.remote.RetrofitRepository

class PostImagePidNumberAndInfoUseCase(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun postImagePidNumberAndInfo(maskPid: String, reqEditType: String, imagePid: String, title: String) : Single<ImagePidNumberResponse> = fileService
        .postImagePidNumberAndInfo(maskPid, reqEditType, imagePid, title)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}