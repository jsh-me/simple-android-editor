package kr.co.jsh.feature.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivityLoginBinding
import kr.co.jsh.feature.main.MainActivity
import org.koin.android.ext.android.get

class LoginActivity :AppCompatActivity() , LoginContract.View {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var presenter : LoginPresenter
    private var inputId = ""
    private var inputPasswd = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDataBinding()
        initPresenter()
    }
    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.login = this@LoginActivity
    }

    private fun initPresenter(){
        presenter = LoginPresenter(this, get())
    }

    fun loginBtn(){
        inputId = binding.idText.text.toString()
        inputPasswd = binding.passwdText.text.toString()
//        Toast.makeText(this, "$inputId and $inputPasswd", Toast.LENGTH_LONG).show()
        presenter.getUserData(inputId, inputPasswd)
    }

    override fun setUserData(name: String) {
        Toast.makeText(this, "$name 님 안녕하세요.", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onError(error: String) {
        binding.result.text = error
    }
}
