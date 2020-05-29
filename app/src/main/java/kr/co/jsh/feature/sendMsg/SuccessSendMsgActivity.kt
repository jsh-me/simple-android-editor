package kr.co.jsh.feature.sendMsg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import kr.co.jsh.R
import kr.co.jsh.databinding.ActivitySuccessSendMsgBinding

class SuccessSendMsgActivity: AppCompatActivity(){
    private lateinit var binding: ActivitySuccessSendMsgBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initView()
    }

    private fun setupDataBinding(){
        binding = DataBindingUtil.setContentView(this, R.layout.activity_success_send_msg)
        binding.sendServerSuccess = this@SuccessSendMsgActivity
    }

    private fun initView(){
        binding.lottieAnimationView.playAnimation()
    }
}