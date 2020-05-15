package kr.co.jsh.dialog

import android.annotation.SuppressLint
import kr.co.domain.api.usecase.PostLoginUseCase

class LoginAccountPresenter(override var view: LoginAccountContract.View,
                            var postLoginUseCase: PostLoginUseCase
) : LoginAccountContract.Presenter{
    @SuppressLint("CheckResult")
    override fun getUserData(id: String, passwd: String) {
        postLoginUseCase.postLogin(id, passwd)
            .subscribe({
                if(it.status.toInt() == 200)
                    view.setUserData(it.datas.user.userName)
                else
                    view.onError("ERROR")
            },{
                view.onError(it.localizedMessage)
            })
    }
}