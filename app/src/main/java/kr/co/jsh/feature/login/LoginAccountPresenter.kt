package kr.co.jsh.feature.login

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.PostLoginUseCase
import kr.co.jsh.singleton.UserObject

class LoginAccountPresenter(override var view: LoginAccountContract.View,
                            var postLoginUseCase: PostLoginUseCase
) : LoginAccountContract.Presenter{
    @SuppressLint("CheckResult")
    override fun getUserData(id: String, passwd: String) {
        postLoginUseCase.postLogin(id, passwd)
            .subscribe({
                if(it.status.toInt() == 200)
                {
                    view.setUserData(it.datas.user.userName)
                    UserObject.loginResponse = it.status.toInt()
                }
                else
                    view.onError("ERROR")
            },{
                view.onError(it.localizedMessage)
            })
    }
}