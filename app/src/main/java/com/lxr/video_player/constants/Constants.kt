package com.lxr.video_player.constants

import com.drake.brv.listener.ItemDifferCallback

/**
 * @Author : Liu XiaoRan
 * @Email : 592923276@qq.com
 * @Date : on 2023/1/13 09:24.
 * @Description :
 */
object Constants {

    /**
     * 消息类型  电量
     */
    const val MSG_TYPE_BATTERY = "msg_type_battery"

    /**
     * 支持(筛选)的字幕类型
     */
    @JvmField
    val SUPPORT_SUBTITLE_TYPE = arrayOf(".srt", ".ass")

    /**
     * 查找字幕文件的默认路径key
     */
    const val K_DEFAULT_PATH_4_FIND_SUBTITLE = "default_path_4_find_subtitle"

    /**
     * 查找字幕文件的默认路径value
     */
    const val V_DEFAULT_PATH_4_FIND_SUBTITLE = "sdcard"
}
