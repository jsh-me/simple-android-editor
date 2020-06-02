package kr.co.jsh.base.storage

//TODO: 공통 메소드 추출
interface BaseStorageView<T> {
    var presenter: T

    fun setFileResult(list: ArrayList<List<String>>)
    fun startAnimation()
    fun stopAnimation()
}