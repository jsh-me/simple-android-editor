package kr.co.jsh.feature.storage.video

interface VideoStorageContract {
    interface View{
        fun setVideoResult(list: ArrayList<List<String>>)
      //  fun setLocalViewResult(list: MutableLiveData<List<VideoStorage>>)
    }
    interface Presenter{
        var view: View
        fun getServerVideoResult()
        fun getLocalVideoResult()
    }
}