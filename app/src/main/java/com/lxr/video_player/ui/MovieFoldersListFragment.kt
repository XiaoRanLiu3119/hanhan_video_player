package com.lxr.video_player.ui

import android.content.Intent
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
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
import com.lxr.video_player.R
import com.lxr.video_player.databinding.FragmentMovieFolderListBinding
import com.lxr.video_player.entity.VideoFolder
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.Utils
import com.lxr.video_player.widget.SpaceItemDecoration

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/11 13:52.
 * @Description :
 */
class MovieFoldersListFragment : BaseFragment<FragmentMovieFolderListBinding>() {
    //文件夹集合
    val folderList = mutableListOf<VideoFolder>()

    override fun initView() {
        getPermission()
    }

    private fun getPermission() {

        binding.rv.run {//初始化列表
            linear().divider(R.drawable.divider).setup {
                setAnimation(AnimationType.SLIDE_BOTTOM)
                addType<VideoFolder>(R.layout.item_folder)
                onBind {
                    Glide.with(context)
                        .load(getModel<VideoFolder>().videoList?.get(0)?.path)//第一个视频做封面
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
                    intent.putExtra("list", GsonUtils.toJson(getModel<VideoFolder>()))
                    startActivity(intent)
                }
            }
        }

        XXPermissions.with(this)
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        ToastUtils.showLong("部分权限未正常授予,请授权")
                        return
                    }
                    //有权限,获取所有视频
                    var groupByBucketIdMap = Utils.getVideoList().groupBy {//根据视频所在文件夹分组
                        it.bucketDisplayName
                    }
                    for ((k, v) in groupByBucketIdMap) {//每个分组的k为文件夹名字,值为所有包含当前key的对象的集合,设置到文件夹对象并装文件夹集合
                        folderList.add(VideoFolder(k, v))
                    }
                    //给列表设置数据
                    binding.rv.models = folderList
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
                        ToastUtils.showShort("获取权限失败")
                    }
                }
            })
    }

}