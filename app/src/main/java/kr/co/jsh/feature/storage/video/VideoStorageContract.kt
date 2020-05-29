package kr.co.jsh.feature.storage.video

interface VideoStorageContract {
    interface View{
        fun setVideoResult(list: ArrayList<List<String>>)
        fun startAnimation()
        fun stopAnimation()
    }
    interface Presenter{
        var view: View
        fun loadVideoStorage()
        fun getServerVideoResult()
        fun getLocalVideoResult()
    }
}