package kr.co.jsh.base.edit

//TODO: 공통 메소드 추출
interface BaseView<T> {
    var presenter: T

    fun cancelJob()
    fun startAnimation()
    fun stopAnimation()
    fun onError(message: String)
    fun cancelAction()
    fun uploadSuccess(msg: String)
    fun uploadFailed(msg: String)
}