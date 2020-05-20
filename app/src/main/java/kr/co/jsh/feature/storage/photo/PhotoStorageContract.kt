package kr.co.jsh.feature.storage.photo

interface PhotoStorageContract {
    interface View{

    }
    interface Presenter{
        var view: View
        fun getResultFile(objectPid : String)
    }
}