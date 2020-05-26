package kr.co.jsh.feature.storage.video

interface VideoStorageContract {
    interface View{
        fun setAllVideoResultView(url: ArrayList<String>, name: ArrayList<String>)
    }
    interface Presenter{
        var view: View
       // fun getVideoResultFile(objectPid: String)
        fun getAllVideoResultFile()
    }
}