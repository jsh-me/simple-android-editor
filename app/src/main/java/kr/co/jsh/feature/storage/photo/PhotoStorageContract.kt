package kr.co.jsh.feature.storage.photo

interface PhotoStorageContract {
    interface View{
        fun setAllImageResultView(url: ArrayList<String>, name: ArrayList<String>)

    }
    interface Presenter{
        var view: View
        fun getAllVideoResultFile()
    }
}