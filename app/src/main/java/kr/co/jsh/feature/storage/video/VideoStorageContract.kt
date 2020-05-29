package kr.co.jsh.feature.storage.video

interface VideoStorageContract {
    interface View{
        fun setVideoResult(list: ArrayList<List<String>>)
        fun startAnimation()
        fun stopAnimation()
      //  fun successLoadDB()
    }
    interface Presenter{
        var view: View
        fun loadLocalVideoStorageDB()
        fun getServerVideoResult()
        fun getLocalVideoResult()
    }
}