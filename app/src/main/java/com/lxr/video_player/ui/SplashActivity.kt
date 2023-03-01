package com.lxr.video_player.ui

import android.content.Intent
import android.os.Handler
import com.dyne.myktdemo.base.BaseActivity
import com.lxr.video_player.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    override fun initView() {
        Handler().postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        },1200)
    }
}