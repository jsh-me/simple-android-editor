package kr.co.domain.api.service

import io.reactivex.Single
import kr.co.data.response.FileDownloadResponse
import kr.co.data.response.FileUploadResponse
import kr.co.data.response.ImagePidNumberResponse
import kr.co.data.response.VideoPidNumberResponse
import okhttp3.MultipartBody
import retrofit2.http.*


interface FileService{
    @Multipart
    @POST("file/singleUpload.do")
    fun postFileUpload(@Part file: MultipartBody.Part) : Single<FileUploadResponse>

    @GET("file/fileDownload.do")
    fun getFileDownload(@Query("objectPid") objectPid : String) : Single<FileDownloadResponse>

    @POST("cVideoEdit/create.do")
    fun postVideoPidNumberAndInfo(@Query("maskImg.objectPid") maskImgObjectPid : String,
                           @Query("frameTimeSec") frameTimeSec : Float,
                           @Query("reqEditType") reqEditType: String,
                           @Query("videoFile.objectPid") videoFileObjectPid : String,
                             @Query("title") title: String): Single<VideoPidNumberResponse>

    @POST("cImageEdit/create.do")
    fun postImagePidNumberAndInfo(@Query("maskImg.objectPid") maskImgObjectPid: String,
                                  @Query("reqEditType") reqEditType: String,
                                  @Query("targetImg.objectPid") targetImgObjectPid: String,
                                  @Query("title") title: String) : Single<ImagePidNumberResponse>
}