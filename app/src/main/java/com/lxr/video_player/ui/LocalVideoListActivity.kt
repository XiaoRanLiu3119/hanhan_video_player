package com.lxr.video_player.ui

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.dyne.myktdemo.base.BaseActivity
import com.gyf.immersionbar.ktx.fitsTitleBar
import com.gyf.immersionbar.ktx.immersionBar
import com.lxr.video_player.R
import com.lxr.video_player.databinding.ActivityVideoListBinding
import com.lxr.video_player.entity.VideoFolder
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.Utils
import com.lxr.video_player.widget.SpaceItemDecoration


/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/9 16:25.
 * @Description :
 */
class LocalVideoListActivity : BaseActivity<ActivityVideoListBinding>() {

    override fun initView() {
        binding.titleBar.leftTitle = intent.getStringExtra("title")
        val videoFolder= GsonUtils.fromJson(intent.getStringExtra("list"), VideoFolder::class.java)
        binding.rv.run {
            linear().divider(R.drawable.divider).setup {
                setAnimation(AnimationType.SLIDE_BOTTOM)
                addType<VideoInfo>(R.layout.item_video)
                onBind {
                    Glide.with(context).load(getModel<VideoInfo>().path)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.mipmap.iv_video)
                        .centerCrop().into(findView(R.id.iv))
                }
                onClick(R.id.item) {
                    val intent = Intent(this@LocalVideoListActivity, PlayerActivity::class.java)
                    intent.putExtra("id", getModel<VideoInfo>().id.toString())
                    intent.putExtra("path", getModel<VideoInfo>().path)
                    intent.putExtra("title", getModel<VideoInfo>().title)
                    startActivity(intent)
                }
            }.models = videoFolder.videoList
        }
    }
}