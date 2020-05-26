package kr.co.jsh.feature.storage.video

import java.io.File

interface VideoStorageContract {
    interface View{
        fun setVideoResultView(result: File)
    }
    interface Presenter{
        var view: View
        fun getVideoResultFile(objectPid: String)
    }
}