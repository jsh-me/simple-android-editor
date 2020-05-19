package kr.co.domain.api.service

import io.reactivex.Single
import kr.co.data.response.FileDownloadResponse
import kr.co.data.response.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.*


interface FileService{
    @Multipart
    @POST("file/singleUpload.do")
    fun postFileUpload(@Part file: MultipartBody.Part) : Single<FileUploadResponse>

    @GET("file/fileDownload.do")
    fun getFileDownload(@Query("objectPid") objectPid : String) : Single<FileDownloadResponse>

}