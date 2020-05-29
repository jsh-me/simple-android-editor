package kr.co.jsh.feature.storage.photo

interface PhotoStorageContract {
    interface View{
        fun setImageResult(list: ArrayList<List<String>>)
        fun startAnimation()
        fun stopAnimation()
    }
    interface Presenter{
        var view: View
        fun loadImageStorage()
        fun getServerImageResult()
        fun getLocalImageResult()
    }
}