package kr.co.jsh.feature.storage.photo

interface PhotoStorageContract {
    interface View{
        fun setImageResult(list: ArrayList<List<String>>)

    }
    interface Presenter{
        var view: View
        fun getServerImageResult()
        fun getLocalImageResult()
    }
}