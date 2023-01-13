package com.lxr.video_player.entity

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/11 15:41.
 * @Description :装载视频的文件夹对象
 */
data class VideoFolder(var name:String? = "", var videoList: MutableList<VideoInfo>)