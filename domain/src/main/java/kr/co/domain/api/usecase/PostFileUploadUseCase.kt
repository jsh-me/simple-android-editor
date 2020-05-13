package kr.co.domain.api.usecase

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kr.co.data.response.FileResponse
import kr.co.domain.api.service.FileService
import kr.co.domain.koin.repository.RetrofitRepository
import okhttp3.MultipartBody

class PostFileUploadUseCase(retrofitRepository: RetrofitRepository) {
    private val fileService = retrofitRepository
        .getRetrofit()
        .create(FileService::class.java)

    fun postFile(file: MultipartBody.Part) : Single<FileResponse> = fileService
        .postFileUpload(file)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}
