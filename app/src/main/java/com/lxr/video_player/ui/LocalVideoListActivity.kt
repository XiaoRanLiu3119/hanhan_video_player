package com.lxr.video_player.ui

import android.content.Intent
import android.os.Handler
import android.widget.ProgressBar
import android.widget.TextView
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.dyne.myktdemo.base.BaseActivity
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSelectListener
import com.lxr.video_player.R
import com.lxr.video_player.constants.SimpleMessage
import com.lxr.video_player.databinding.ActivityVideoListBinding
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.SpUtil
import com.lxr.video_player.utils.Utils
import com.shuyu.gsyvideoplayer.utils.CommonUtil
import org.greenrobot.eventbus.EventBus


/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/9 16:25.
 * @Description :
 */
class LocalVideoListActivity : BaseActivity<ActivityVideoListBinding>() {

    /**
     * 当前文件夹id,由文件夹列表传进
     */
    var bucketId:String = ""

    override fun initView() {

        binding.titleBar.leftTitle = intent.getStringExtra("title")
        bucketId = intent.getStringExtra("bucketId").toString()

        binding.rv.run {
            linear().divider(R.drawable.divider).setup {
                setAnimation(AnimationType.SLIDE_BOTTOM)
                addType<VideoInfo>(R.layout.item_video)
                onBind {
                    Glide.with(context).load(getModel<VideoInfo>().path)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.mipmap.iv_video)
                        .centerCrop().into(findView(R.id.iv))
                    //总时长
                    var duration = ""
                    if (getModel<VideoInfo>().duration.toInt() == 0) {//缺失时长/缩略图的,从缓存取(如果有)
                        val cacheDuration =
                            SPUtils.getInstance().getLong(getModel<VideoInfo>().id.toString(),0L)
                        if (cacheDuration != 0L) {//有缓存
                            duration = CommonUtil.stringForTime(cacheDuration)
                            findView<ProgressBar>(R.id.progressBar).max = cacheDuration.toInt()
                        }
                    } else {//正常视频
                        duration = CommonUtil.stringForTime(getModel<VideoInfo>().duration)
                        findView<ProgressBar>(R.id.progressBar).max = getModel<VideoInfo>().duration.toInt()
                    }

                    //已播放进度时长
                    val progressPlayed = SpUtil.getLong(getModel<VideoInfo>().id.toString())
                    if (progressPlayed != -0L && duration.isNotEmpty()){//有进度且有时长都显示
                        findView<TextView>(R.id.tv_duration).text = CommonUtil.stringForTime(progressPlayed!!) +"/"+ duration
                        findView<ProgressBar>(R.id.progressBar).progress = progressPlayed.toInt()
                    }else{//没有进度(也包括没时长,此时是空串,不耽误)
                        findView<TextView>(R.id.tv_duration).text = duration
                    }
                }
                onClick(R.id.item) {
                    val intent = Intent(this@LocalVideoListActivity, PlayerActivity::class.java)
                    intent.putExtra("videoList", GsonUtils.toJson(models))
                    intent.putExtra("position",modelPosition)
                    startActivity(intent)
                }
                onLongClick(R.id.item) {
                    XPopup.Builder(this@LocalVideoListActivity)
                        .asCenterList(
                            getModel<VideoInfo>().title,
                            arrayOf("信息", "删除"),
                            object : OnSelectListener {
                                override fun onSelect(position: Int, text: String?) {
                                    if (position == 0) {

                                    } else {
                                        showLoading()
                                        if (FileUtils.delete(getModel<VideoInfo>().path)) {
                                            //删除缓存的影片时长(部分系统获取不到时长的影片,已在播放的时候缓存)
                                            SPUtils.getInstance()
                                                .remove(getModel<VideoInfo>().id.toString())
                                            //删除缓存的播放进度
                                            SpUtil.removeKey(id.toString())
                                            //文件增删需要通知系统扫描,否则删除文件后还能查出来
                                            //这个工具类直接传文件路径不知道为啥通知失败,手动获取一下
                                            FileUtils.notifySystemToScan(
                                                FileUtils.getDirName(
                                                    getModel<VideoInfo>().path
                                                )
                                            )
                                            EventBus.getDefault().post(SimpleMessage.REFRESH)
                                        } else {
                                            dismissLoading()
                                            ToastUtils.showShort("删除失败,请重试")
                                        }
                                    }
                                }
                            }).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateListData()
    }
    /**
     * 更新电影列表,和文件夹列表一样全都遍历出来后再筛选出当前文件夹的媒体  todo 后续看看contentResolver 能否按指定文件夹把文件遍历出来
     */
    private fun updateListData() {
        binding.rv.models = Utils.getVideoList().filter {//筛选出当前文件夹的视频
            it.bucketId.equals(bucketId)
        }.toMutableList()
    }

    override fun onEvent(simpleMessage: String?) {
        if (simpleMessage.equals(SimpleMessage.REFRESH)) {
            Handler().postDelayed({//文件变动需要主动让系统扫描,为避免未扫描完毕,加个延迟
                updateListData()
                dismissLoading()
            },1000)
        }
    }
}