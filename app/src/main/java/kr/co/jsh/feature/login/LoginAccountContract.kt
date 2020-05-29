package kr.co.jsh.feature.login

interface LoginAccountContract {
    interface View{
        fun setUserData(name: String)
        fun onError(error: String)
    }
    interface Presenter {
        var view:View
        fun getUserData(id:String, passwd:String)
    }
}