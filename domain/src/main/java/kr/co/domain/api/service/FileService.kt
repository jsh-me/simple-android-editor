package kr.co.domain.api.service

import io.reactivex.Single
import kr.co.data.response.FileResponse
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File

interface FileService{
    @Multipart
    @POST("file/singleUpload.do")
    fun postFileUpload(@Part("file") file: File) : Single<FileResponse>
}