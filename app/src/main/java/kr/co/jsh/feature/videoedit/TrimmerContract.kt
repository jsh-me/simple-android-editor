package kr.co.jsh.feature.videoedit

import android.content.Context
import android.net.Uri

interface TrimmerContract {
    interface View{
        fun onVideoPrepared() //OnVideoListener
        fun onError(message: String)
        fun cancelAction()
        fun onTrimStarted()


    }
    interface Presenter{
        var view: View
        fun getResult(progressDialog: VideoProgressIndeterminateDialog, context: Context,  uri: Uri) //OnTrimVideoListener
    }
}