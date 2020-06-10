package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.AllImageResultResponse
import kr.co.data.response.ImagePidNumberResponse
import kr.co.data.response.SearchResultImageResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.remote.RetrofitRepository

class PostImageSearchListUseCase(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun postImageSearchList(pageSize: Int, pageNum: Int) : Single<SearchResultImageResponse> = fileService
        .postImageSearchList(pageSize, pageNum)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}