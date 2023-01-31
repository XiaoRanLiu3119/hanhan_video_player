package com.lxr.video_player.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.blankj.utilcode.util.LogUtils
import com.lxr.video_player.constants.Constants
import com.lxr.video_player.constants.MessageEvent
import org.greenrobot.eventbus.EventBus


/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/29 11:45.
 * @Description :
 */
class BatteryReceiver :BroadcastReceiver() {

    var currentBattery:Int = -1
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BATTERY_CHANGED == intent!!.action) {
            val level = intent.getIntExtra("level", 0)
            if (currentBattery!=level){//电量变化
                currentBattery = level
                EventBus.getDefault().post(MessageEvent(Constants.MSG_TYPE_BATTERY,currentBattery.toString()))
            }
        }
    }
}