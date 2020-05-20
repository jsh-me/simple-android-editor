package kr.co.jsh.dialog

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import kr.co.jsh.R
import kr.co.jsh.databinding.ProgressLoadingBinding

class DialogActivity : Activity() {
    private lateinit var binding: ProgressLoadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataBinding()
        initView()
    }

    private fun setupDataBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.progress_loading)
        binding.dialog = this@DialogActivity
    }

    private fun initView(){
        val handler = Handler()
        for ( i in 0.. 1000) {
            handler.postDelayed({
                binding.pieProgress.setProgress(i.toFloat())
            }, 10 * i.toLong())
        }
        //finish()
    }
}