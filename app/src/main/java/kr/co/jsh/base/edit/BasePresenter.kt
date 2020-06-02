package kr.co.jsh.base.edit

import android.content.Intent

//TODO: 공통 메소드 추출
interface BasePresenter {
    fun preparePath(extraIntent: Intent)
    fun uploadFile(uri: String)

}