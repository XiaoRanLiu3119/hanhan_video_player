package com.lxr.video_player.ui

import android.content.res.Configuration
import android.util.DisplayMetrics
import android.view.View
import com.dyne.myktdemo.base.BaseActivity
import com.lxr.video_player.action.OnLongPressUpListener
import com.lxr.video_player.databinding.ActivityPlayerBinding
import com.lxr.video_player.utils.SpUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYStateUiListener
import com.shuyu.gsyvideoplayer.utils.OrientationUtils
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView
import me.jessyan.autosize.internal.CancelAdapt


open class PlayerActivity : BaseActivity<ActivityPlayerBinding>(), CancelAdapt {
    /**
     * 当前播放影片的id,仅用于存取播放进度
     */
    var id: String? = ""

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
        id = intent.getStringExtra("id")
        val path = intent.getStringExtra("path")
        val title = intent.getStringExtra("title")

//        videoPlayer.setUp("file://"+ path, false, title);
        //外部辅助的旋转，帮助全屏
        binding.videoPlayer.setUp("file://$path", false, title)
        binding.videoPlayer.gsyStateUiListener
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
        binding.videoPlayer.gsyStateUiListener = object : GSYStateUiListener {
            override fun onStateChanged(state: Int) {
                when (state) {
                    GSYVideoView.CURRENT_STATE_PAUSE, GSYVideoView.CURRENT_STATE_ERROR -> {
                        //直接home退出/暂停/返回,存储当前影片的播放进度
                        id?.let { SpUtil.put(it, binding.videoPlayer.currentPositionWhenPlaying) }
                    }
                    GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> id?.let {//自动播放完成完成清空该影片缓存的进度
                        SpUtil.removeKey(it)
                    }
                }
            }
        }
        binding.videoPlayer.setOnLongPressListener(object : OnLongPressUpListener {//长按监听
            override fun onLongPressIsStart(start: Boolean) {
                if (start){
                    //todo 添加倍速播放dialog
                    binding.videoPlayer.setSpeedPlaying(2F, false)
                }else{
                    binding.videoPlayer.setSpeedPlaying(1F, false)
                }
            }
        })
        if (id != null) {//设置进度再播放
            val playPosition = SpUtil.getLong(id!!)
            if (playPosition != null) {//设置上次播放的位置
                binding.videoPlayer.seekOnStart = playPosition
            }
        }
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