package com.lxr.video_player.action

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/12 15:35.
 * @Description : 长按抬起监听
 */
interface OnLongPressUpListener {
    /**
     * start 回调 true 开始  回调 false 结束
     */
    fun onLongPressIsStart(start :Boolean)
}