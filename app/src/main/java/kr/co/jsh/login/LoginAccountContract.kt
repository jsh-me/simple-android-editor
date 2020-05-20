package kr.co.jsh.login

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