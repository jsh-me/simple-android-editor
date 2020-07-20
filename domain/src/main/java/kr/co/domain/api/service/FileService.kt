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

    //----------------------------------------------------------

    @POST("cImageEdit/create.do")
    fun postImagePidNumberAndInfo(@Query("maskImg.objectPid") maskImgObjectPid: String,
                                  @Query("reqEditType") reqEditType: String,
                                  @Query("targetImg.objectPid") targetImgObjectPid: String,
                                  @Query("title") title: String) : Single<ImagePidNumberResponse>

    @FormUrlEncoded
    @POST("cImageEdit/search.do")
    fun postImageSearchList(@Field("page.pageSize") pageSize: Int,
                            @Field("page.pageNum") pageNum: Int) : Single<SearchResultImageResponse>

    @POST("cImageEdit/create.do")
    fun postImproveImagePidNumber(@Query("reqEditType") reqEditType: String,
                                  @Query("targetImg.objectPid") imageFileObjectPid: String,
                                  @Query("title") title:String) : Single<ImagePidNumberResponse>

    //-----------------------------------------------------------

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

    //PageSize: 몇개 씩 불러올 것인가, PageNum: 몇 페이지를 보여줄 것인가
    //if response datas is null, it is EOF
    @FormUrlEncoded
    @POST("cVideoEdit/search.do")
    fun postVideoSearchList(@Field("page.pageSize") pageSize: Int,
                            @Field("page.pageNum") pageNum: Int) : Single<SearchResultVideoResponse>

}