package com.lxr.video_player.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.widget.ImageView
import androidx.annotation.NonNull
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.VideoDecoder.FRAME_OPTION
import com.bumptech.glide.request.RequestOptions
import com.lxr.video_player.MyApp
import com.lxr.video_player.entity.VideoInfo
import java.security.MessageDigest


/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/9 09:39.
 * @Description :
 */
object Utils {

     fun getVideoList(): List<VideoInfo> {
       val videoList: List<VideoInfo> = mutableListOf()

        val cursor = MyApp.instance.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, arrayOf(//查询内容
                MediaStore.Video.Media._ID,  // 视频id
                MediaStore.Video.Media.DATA,  // 视频路径
                MediaStore.Video.Media.SIZE,  // 视频字节大小
                MediaStore.Video.Media.DISPLAY_NAME,  // 视频名称 xxx.mp4
                MediaStore.Video.Media.TITLE,  // 视频标题
                MediaStore.Video.Media.DURATION,  // 视频时长
                MediaStore.Video.Media.RESOLUTION,  // 视频分辨率 X x Y格式
                MediaStore.Video.Media.IS_PRIVATE,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.BOOKMARK // 上次视频播放的位置
            ),
            null, null, null
        )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val videoInfo = VideoInfo()
                videoInfo.id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                videoInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                videoInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE))
                videoInfo.displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                videoInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE))
                videoInfo.duration = parseTime((cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)) / 1000).toInt())
                videoInfo.resolution = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION))
                videoInfo.isPrivate = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.IS_PRIVATE))
                videoInfo.bucketId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID))
                videoInfo.bucketDisplayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                videoInfo.bookmark = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BOOKMARK))

                (videoList as MutableList<VideoInfo>).add(videoInfo)
            } while (cursor.moveToNext())
            cursor.close()
        }
        return videoList
    }

    /**
     * 将秒转为 00:00显示模式
     */
    fun parseTime(seconds: Int): String {
        val stringBuilder = StringBuilder()
        val min = seconds / 60
        val sec = seconds % 60
        val minHeigh = min / 10
        val minLow = min % 10
        val secHeight = sec / 10
        val secLow = sec % 10
        stringBuilder.append(minHeigh).append(minLow).append(":").append(secHeight).append(secLow)
        return stringBuilder.toString()
    }
}