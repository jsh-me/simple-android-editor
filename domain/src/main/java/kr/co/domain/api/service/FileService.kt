package kr.co.domain.api.service

import io.reactivex.Single
import kr.co.data.response.*
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

    @POST("cVideoEdit/create.do")
    fun postImproveVideoPidNumber(@Query("reqEditType") reqEditType: String,
                                  @Query("videoFile.objectPid") videoFileObjectPid : String,
                                  @Query("title") title: String): Single<VideoPidNumberResponse>

    @POST("cImageEdit/create.do")
    fun postImagePidNumberAndInfo(@Query("maskImg.objectPid") maskImgObjectPid: String,
                                  @Query("reqEditType") reqEditType: String,
                                  @Query("targetImg.objectPid") targetImgObjectPid: String,
                                  @Query("title") title: String) : Single<ImagePidNumberResponse>

    @GET("cVideoEdit/get.do")
    fun getVideoResult(@Query("objectPid") objectPid: String) : Single<VideoResultResponse>

    @GET("cVideoEdit/list.do")
    fun getAllVideoResultList() : Single<AllVideoResultResponse>

    @GET("cImageEdit/list.do")
    fun getAllImageResultList() : Single<AllImageResultResponse>
}