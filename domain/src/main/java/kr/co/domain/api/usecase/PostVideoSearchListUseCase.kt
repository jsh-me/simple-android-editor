package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.AllVideoResultResponse
import kr.co.data.response.SearchResultVideoResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.remote.RetrofitRepository

class PostVideoSearchListUseCase(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun postVideoSearchList(pageSize: Int, pageNum: Int) : Single<SearchResultVideoResponse> = fileService
        .postVideoSearchList(pageSize, pageNum)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}