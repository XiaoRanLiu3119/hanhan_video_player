package com.lxr.video_player.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.lxr.video_player.action.OnLongPressUpListener
import com.shuyu.gsyvideoplayer.utils.Debuger
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer


/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/12 14:31.
 * @Description : 2倍速播放器
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


}
