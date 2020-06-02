package kr.co.jsh.feature.login

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import kr.co.domain.utils.toastShort
import kr.co.jsh.R
import kr.co.jsh.databinding.DialogLayoutBinding
import org.koin.android.ext.android.get
import timber.log.Timber


class LoginAccountDialog: Activity(), LoginAccountContract.View{
    lateinit var presenter : LoginAccountPresenter
    lateinit var mBinding: DialogLayoutBinding
    private var inputId = ""
    private var inputPasswd = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setUpDataBinding()
        initPresenter()
    }

    private fun setUpDataBinding() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.dialog_layout)
        mBinding.loginDialog = this@LoginAccountDialog
    }

    private fun initPresenter(){
        presenter = LoginAccountPresenter(this,get())
    }

    fun loginBtn(){
        inputId = mBinding.idText.text.toString()
        inputPasswd = mBinding.passwdText.text.toString()
        presenter.getUserData(inputId, inputPasswd)
    }

    fun closeBtn(){
        finish()
    }

    override fun setUserData(name: String) {
       this.toastShort("$name 님 안녕하세요.")
        setResult(1000)
        finish()
    }

    override fun onError(error: String) {
        Timber.e(error)
    }

}