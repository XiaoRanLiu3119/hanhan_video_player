package com.lxr.video_player.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.lxr.video_player.R
import com.lxr.video_player.action.OnLongPressUpListener
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.SpUtil
import com.shuyu.gsyvideoplayer.listener.GSYStateUiListener
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.shuyu.gsyvideoplayer.video.base.GSYVideoView


/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/12 14:31.
 * @Description : 自定义播放器,添加:倍速播放,下一集,缓存进度和一些丢失时长信息的视频
 */
class LongPressPlayer: StandardGSYVideoPlayer {
    /**
     * 长按倍速标识,仅长按时开启,长按结束的MotionEvent.ACTION_UP时再关闭,避免点击时也触发倍速播放
     */
    private var idLongPressSpeed = false

    /**
     * 长按/抬起监听器
     */
    private var pressUpListener: OnLongPressUpListener? = null

    constructor(context: Context?, fullFlag: Boolean?) : super(context, fullFlag)
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    protected var uriList: List<VideoInfo> = ArrayList()

    override fun getLayoutId(): Int {
        return R.layout.widget_custom_video
    }

    override fun init(context: Context?) {
        super.init(context)
        post {
            gestureDetector = GestureDetector(
                getContext().applicationContext,
                object : SimpleOnGestureListener() {
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        touchDoubleUp(e)
                        return super.onDoubleTap(e)
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        if (!mChangePosition && !mChangeVolume && !mBrightness) {
                            onClickUiToggle(e)
                        }
                        return super.onSingleTapConfirmed(e)
                    }

                    override fun onLongPress(e: MotionEvent) {
                        idLongPressSpeed = true
                        pressUpListener?.onLongPressIsStart(true)
                    }
                })
        }

        findViewById<ImageView>(R.id.iv_next).setOnClickListener {
            playNext()
        }
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url,这里用集合,以供播放下一集等使用
     * @param cacheWithPlay 是否边播边缓存
     * @param position      需要播放的位置
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param mapHeadData   http header
     * @param changeState   切换的时候释放surface
     * @return
     */
    fun setUp(
        urls: List<VideoInfo>,
        position: Int,
    ): Boolean {
        uriList = urls
        mPlayPosition = position
        val videoModel = urls[position]
        val set =
            setUp(videoModel.path, false, videoModel.title)
        if (!TextUtils.isEmpty(videoModel.title) && mTitleTextView != null) {
            mTitleTextView.text = videoModel.title
        }

        //设置当前视频id,缓存当前播放进度和时长
        val currentVideoId = videoModel.id.toString()
        //监听播放状态通过视频id缓存播放进度和时长
        gsyStateUiListener = GSYStateUiListener { state ->
            when (state) {
                GSYVideoView.CURRENT_STATE_PAUSE, GSYVideoView.CURRENT_STATE_ERROR -> {
                    currentVideoId.let {
                        //直接home退出/暂停/返回,播放下一集,存储当前影片的播放进度
                        SpUtil.put(it, currentPositionWhenPlaying)
                        //部分资源没有时长(部分媒体文件用几种系统api都获取不到),在缓存中记录时长 注:这里用sp存时长,,而不是自己的(用id存播放进度了..后续可改用数据库记录每个电影的播放进度.总时长.缩略图.帧图等)
                        if (videoModel.duration == 0L){//视频没有时长,自己缓存,用于下次显示
                            SPUtils.getInstance().put(it,duration)
                        }
                    }
                }
                GSYVideoView.CURRENT_STATE_AUTO_COMPLETE -> currentVideoId.let {//自动播放完成完成清空该影片缓存的进度
                    SpUtil.removeKey(it)
                }
            }
        }

        //设置播放进度
        val playPosition = SpUtil.getLong(currentVideoId)
        if (playPosition != null) {//设置播放位置
            seekOnStart = playPosition
        }

        return set
    }

    /**
     * 设置长按/抬起监听器
     */
    fun setOnLongPressListener(onLongPressUpListener: OnLongPressUpListener){
        pressUpListener = onLongPressUpListener
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (idLongPressSpeed && event?.action  == MotionEvent.ACTION_UP){
            idLongPressSpeed = false
            pressUpListener?.onLongPressIsStart(false)
        }
       return super.onTouch(v, event)
    }

    /**
     * 播放下一集
     *
     * @return true表示还有下一集
     */
    private fun playNext(): Boolean {
        if (mPlayPosition < uriList.size - 1) {
            //直接home退出/暂停/返回,播放下一集,存储当前影片的播放进度
            SpUtil.put(uriList[mPlayPosition].id.toString(), currentPositionWhenPlaying)
            //记录下一集播放位置
            mPlayPosition += 1
            val nextVideoModel: VideoInfo = uriList[mPlayPosition]
            mSaveChangeViewTIme = 0
            setUp(uriList,mPlayPosition)
            if (!TextUtils.isEmpty(nextVideoModel.title) && mTitleTextView != null) {
                mTitleTextView.text = nextVideoModel.title
            }
            startPlayLogic()
            return true
        }else{
            ToastUtils.showShort("最后一集了")
        }
        return false
    }
}
