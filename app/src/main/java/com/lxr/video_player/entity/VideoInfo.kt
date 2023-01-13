package com.lxr.video_player.entity

import android.graphics.Bitmap

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/9 09:42.
 * @Description : 视频信息
 */
data class VideoInfo(
     var id :Int = 0,//视频id
     var path: String? = null,//文件路径
     var size: Long = 0,//大小
     var displayName: String? = null,//视频名字,不带后缀
     var title: String? = null,//视频标题,带后缀
     var duration: Long = 0,//时长,部分视频损坏/其他原因没有
     var resolution: String? = null,//分辨率
     var isPrivate:Int  = 0,//私密?
     var bucketId: String? = null,//装载(文件夹)id
     var bucketDisplayName: String? = null,//装载文件夹名字
     var thumbnail: Bitmap? = null,//缩略图
     var bookmark: String? = null,//书签,上次播放的位置

)
