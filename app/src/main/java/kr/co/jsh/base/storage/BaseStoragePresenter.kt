package kr.co.jsh.base.storage


//TODO: 공통 메소드 추출
interface BaseStoragePresenter {

    fun loadLocalFileStorageDB()
    fun getServerFileResult()
    fun getLocalFileResult()
}