package com.lxr.video_player.ui

import android.content.Intent
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.dyne.myktdemo.base.BaseFragment
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnCancelListener
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxr.video_player.R
import com.lxr.video_player.constants.SimpleMessage
import com.lxr.video_player.databinding.FragmentMovieFolderListBinding
import com.lxr.video_player.entity.VideoFolder
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.SpUtil
import com.lxr.video_player.utils.Utils
import com.shuyu.gsyvideoplayer.utils.CommonUtil

/**
 * @Author : Liu XiaoRan
 * @Email : 592923276@qq.com
 * @Date : on 2023/1/11 13:52.
 * @Description :
 */
class MovieFoldersListFragment : BaseFragment<FragmentMovieFolderListBinding>() {
    /**
     * 文件夹集合
     */
    private val folderList = mutableListOf<VideoFolder>()

    override fun initView() {
        binding.rvPlayHistory.run { // 初始化历史记录列表
            linear(orientation = LinearLayoutManager.HORIZONTAL).setup {
                addType<VideoInfo>(R.layout.item_play_history)
                onBind {
                    Glide.with(context).load(getModel<VideoInfo>().path)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.mipmap.iv_video)
                        .centerCrop().into(findView(R.id.iv))
                    // 总时长
                    var duration = ""
                    if (getModel<VideoInfo>().duration.toInt() == 0) { // 缺失时长/缩略图的,从缓存取(如果有)
                        val cacheDuration =
                            SPUtils.getInstance().getLong(getModel<VideoInfo>().id.toString(), 0L)
                        if (cacheDuration != 0L) { // 有缓存
                            duration = CommonUtil.stringForTime(cacheDuration)
                            findView<ProgressBar>(R.id.progressBar).max = cacheDuration.toInt()
                        }
                    } else { // 正常视频
                        duration = CommonUtil.stringForTime(getModel<VideoInfo>().duration)
                        findView<ProgressBar>(R.id.progressBar).max = getModel<VideoInfo>().duration.toInt()
                    }
                    // 已播放进度时长
                    val progressPlayed = SpUtil.getLong(getModel<VideoInfo>().id.toString())
                    if (progressPlayed != -0L && duration.isNotEmpty()) { // 有进度且有时长都显示
                        findView<TextView>(R.id.tv_duration).text = CommonUtil.stringForTime(progressPlayed!!) + "/" + duration
                        findView<ProgressBar>(R.id.progressBar).progress = progressPlayed.toInt()
                    } else { // 没有进度(也包括没时长,此时是空串,不耽误)
                        findView<TextView>(R.id.tv_duration).text = duration
                    }
                }
                onClick(R.id.item) {
                    val intent = Intent(this@MovieFoldersListFragment.context, PlayerActivity::class.java)
                    intent.putExtra("videoList", GsonUtils.toJson(models))
                    intent.putExtra("position", modelPosition)
                    startActivity(intent)
                }
            }
        }
        binding.rv.run { // 初始化文件夹列表
            linear().divider(R.drawable.divider).setup {
                setAnimation(AnimationType.SLIDE_BOTTOM)
                addType<VideoFolder>(R.layout.item_folder)
                onBind {
                    Glide.with(context)
                        .load(getModel<VideoFolder>().videoList[0].path) // 第一个视频做封面
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.mipmap.iv_video)
                        .centerCrop().into(findView(R.id.iv))
                }
                onClick(R.id.item) {
                    val intent = Intent(
                        this@MovieFoldersListFragment.context,
                        LocalVideoListActivity::class.java
                    )
                    intent.putExtra("title", getModel<VideoFolder>().name)
                    intent.putExtra("bucketDisplayName", getModel<VideoFolder>().videoList[0].bucketDisplayName)
                    startActivity(intent)
                }
            }
        }
        showPermissionTipPopup()
    }

    override fun onResume() {
        super.onResume()
        this@MovieFoldersListFragment.context?.let {
            if (XXPermissions.isGranted(it, Permission.MANAGE_EXTERNAL_STORAGE)) {
                updateListData()
            }
        }
    }

    private fun showPermissionTipPopup() {
        this@MovieFoldersListFragment.context?.let {
            if (!XXPermissions.isGranted(it, Permission.MANAGE_EXTERNAL_STORAGE)) {
                XPopup.Builder(context)
                    .dismissOnBackPressed(false)
                    .dismissOnTouchOutside(false)
                    .asConfirm(
                        "提示",
                        "为了播放视频、音频、获取字幕,我们需要访问您设备文件的权限",
                        "就不给",
                        "好哒",
                        object : OnConfirmListener {
                            override fun onConfirm() {
                                getPermission2getData()
                            }
                        },
                        object : OnCancelListener {
                            override fun onCancel() {
                                ActivityUtils.finishAllActivities(true)
                            }
                        },
                        false
                    )
                    .show()
            } else {
                updateListData()
            }
        }
    }

    private fun getPermission2getData() {
        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        ToastUtils.showLong("部分权限未正常授予,请授权")
                        return
                    }
                    updateListData()
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        ToastUtils.showLong("读写文件权限被永久拒绝，请手动授权")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(
                            this@MovieFoldersListFragment,
                            permissions
                        )
                    } else {
                        showPermissionTipPopup()
                        ToastUtils.showShort("获取权限失败")
                    }
                }
            })
    }

    private fun updateListData() {
        folderList.clear()
        // 全部视频
        val videoList = Utils.getVideoList()
        val groupByBucketIdMap = videoList.groupBy { // 按文件夹分组
            it.bucketDisplayName
        }
        for ((k, v) in groupByBucketIdMap) { // 按文件夹名字区分,一样的名字装在一起,每个分组的k为文件夹名字,值为所有包含当前key的对象的集合,设置到文件夹对象并装文件夹集合
            folderList.add(VideoFolder(k, v as MutableList<VideoInfo>))
        }
        // 给列表设置数据
        binding.rv.models = folderList

        // 有历史记录的
        val mutablePlayHistoryList = videoList.filter { // 过滤出有历史播放进度和总时长的
            SpUtil.getLong(it.id.toString()) != 0L && (
                SPUtils.getInstance()
                    .getLong(it.id.toString(), 0L) != 0L || it.duration != 0L
                )
        }.toMutableList()
        if (mutablePlayHistoryList.size != 0) {
            binding.llPlayHistoryContainer.isVisible = true
            binding.rvPlayHistory.models = mutablePlayHistoryList
        }
    }

    override fun onEvent(simpleMessage: String?) {
        if (simpleMessage.equals(SimpleMessage.REFRESH)) {
            updateListData()
        }
    }
}
