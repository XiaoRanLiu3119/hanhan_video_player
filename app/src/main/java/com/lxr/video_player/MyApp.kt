package com.lxr.video_player

import android.app.Application
import com.drake.brv.utils.BRV
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tencent.mmkv.MMKV
import me.jessyan.autosize.AutoSizeConfig

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/9 09:30.
 * @Description :
 */
class MyApp:Application() {
    companion object {
        lateinit var instance: MyApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        MMKV.initialize(this)
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { _, _ -> MaterialHeader(this) }
        // brv使用,初始化BindingAdapter的默认绑定ID, 如果不使用DataBinding并不需要初始化
        BRV.modelId = BR.m
        //不想让 App 内的字体大小跟随系统设置中字体大小的改变
        AutoSizeConfig.getInstance().isExcludeFontScale = true;
    }
}