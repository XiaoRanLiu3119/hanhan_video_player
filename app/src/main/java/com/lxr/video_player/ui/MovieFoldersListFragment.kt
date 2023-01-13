package com.lxr.video_player.ui

import android.content.Intent
import android.util.Log
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
import com.lxr.video_player.constants.SimpleMessage
import com.lxr.video_player.databinding.FragmentMovieFolderListBinding
import com.lxr.video_player.entity.VideoFolder
import com.lxr.video_player.entity.VideoInfo
import com.lxr.video_player.utils.Utils

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
        binding.rv.run {//初始化列表
            linear().divider(R.drawable.divider).setup {
                setAnimation(AnimationType.SLIDE_BOTTOM)
                addType<VideoFolder>(R.layout.item_folder)
                onBind {
                    Glide.with(context)
                        .load(getModel<VideoFolder>().videoList[0].path)//第一个视频做封面
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
                    //todo 用文件夹id传入,详情页再按需筛选,这页面是用 bucketDisplayName给列表分组的 但可能有重复文件夹,暂时搁置 遇见问题解决
                    intent.putExtra("bucketId", getModel<VideoFolder>().videoList[0].bucketId)
                    startActivity(intent)
                }
            }
        }
        getPermission()
    }

    override fun onResume() {
        super.onResume()
        this@MovieFoldersListFragment.context?.let {
            if (XXPermissions.isGranted(it, Permission.MANAGE_EXTERNAL_STORAGE)) {
                updateListData()
            }
        }
    }

    private fun getPermission() {

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
                        ToastUtils.showShort("获取权限失败")
                    }
                }
            })
    }

    private fun updateListData() {
        folderList.clear()
        val groupByBucketIdMap = Utils.getVideoList().groupBy {//根据视频所在文件夹分组
            it.bucketDisplayName
        }
        for ((k, v) in groupByBucketIdMap) {//每个分组的k为文件夹名字,值为所有包含当前key的对象的集合,设置到文件夹对象并装文件夹集合
            folderList.add(VideoFolder(k, v as MutableList<VideoInfo>))
        }
        //给列表设置数据
        binding.rv.models = folderList
    }

    override fun onEvent(simpleMessage: String?) {
        if (simpleMessage.equals(SimpleMessage.REFRESH)) {
            updateListData()
        }
    }
}