package com.lxr.video_player.ui

import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.View
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.dyne.myktdemo.base.BaseActivity
import com.google.common.reflect.TypeToken
import com.lxr.video_player.action.OnLongPressUpListener
import com.lxr.video_player.databinding.ActivityPlayerBinding
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.SpUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYStateUiListener
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import me.jessyan.autosize.internal.CancelAdapt


open class PlayerActivity : BaseActivity<ActivityPlayerBinding>(), CancelAdapt {

    var orientationUtils: OrientationUtils? = null

    override fun initBeforeInitView() {
        initFontScale()
    }

    override fun initView() {
        init()
    }

    /**
     * 更改字体大小
     */
    private fun initFontScale() {
        val configuration: Configuration = resources.configuration
        configuration.fontScale = 1F
        //0.85 小, 1 标准大小, 1.15 大，1.3 超大 ，1.45 特大
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        metrics.scaledDensity = configuration.fontScale * metrics.density
        resources.updateConfiguration(configuration, metrics)
    }


    private fun init() {

        val videoListJson = intent.getStringExtra("videoList")
        val position = intent.getIntExtra("position", 0)
        val videoList = GsonUtils.fromJson<List<VideoInfo>>(videoListJson,
            object : TypeToken<List<VideoInfo>>() {}.type
        )

        binding.videoPlayer.setUp(videoList, position)
        //增加title
        binding.videoPlayer.titleTextView.visibility = View.VISIBLE
        //设置返回键
        binding.videoPlayer.backButton.visibility = View.VISIBLE
        //设置旋转
        orientationUtils = OrientationUtils(this, binding.videoPlayer)
        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        binding.videoPlayer.fullscreenButton
            .setOnClickListener { // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
                // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
                orientationUtils!!.resolveByClick()
            }
        //是否可以滑动调整
        binding.videoPlayer.setIsTouchWiget(true)
        binding.videoPlayer.seekRatio = 50F
        //设置返回按键功能
        binding.videoPlayer.backButton.setOnClickListener { onBackPressed() }
        binding.videoPlayer.isReleaseWhenLossAudio = false

        binding.videoPlayer.setOnLongPressListener(object : OnLongPressUpListener {
            //长按监听
            override fun onLongPressIsStart(start: Boolean) {
                if (start) {
                    //todo 添加倍速播放dialog
                    binding.videoPlayer.setSpeedPlaying(2F, false)
                } else {
                    binding.videoPlayer.setSpeedPlaying(1F, false)
                }
            }
        })

        binding.videoPlayer.startPlayLogic()
    }

    override fun onPause() {
        super.onPause()
        binding.videoPlayer.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        binding.videoPlayer.onVideoResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        GSYVideoManager.releaseAllVideos()
        if (orientationUtils != null) orientationUtils!!.releaseListener()
    }

    override fun onBackPressed() {
        binding.videoPlayer.setVideoAllCallBack(null)
        super.onBackPressed()
    }

}