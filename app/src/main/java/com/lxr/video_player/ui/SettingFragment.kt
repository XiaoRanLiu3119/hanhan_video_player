package com.lxr.video_player.ui

import com.blankj.utilcode.util.SPUtils
import com.dyne.myktdemo.base.BaseFragment
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxr.video_player.R
import com.lxr.video_player.constants.SimpleMessage
import com.lxr.video_player.databinding.FragmentSettingBinding
import com.lxr.video_player.utils.SpUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager
import org.greenrobot.eventbus.EventBus
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/11 13:52.
 * @Description :
 */
class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    override fun initView() {
        binding.rlPlayerManager.setOnClickListener {
            XPopup.Builder(this@SettingFragment.context)
                .asCenterList(
                    null,
                    arrayOf(
                        resources.getString(R.string.player_manager_ijk),
                        resources.getString(R.string.player_manager_exo),
                        resources.getString(R.string.player_manager_system)
                    )
                ) { position, _ ->
                    when (position) {
                        0 -> {
                            PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
                            binding.tvPlayerManager.text = resources.getString(R.string.player_manager_ijk)
                        }
                        1 -> {
                            PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
                            binding.tvPlayerManager.text = resources.getString(R.string.player_manager_exo)
                        }
                        2 -> {
                            PlayerFactory.setPlayManager(SystemPlayerManager::class.java)
                            binding.tvPlayerManager.text = resources.getString(R.string.player_manager_system)
                        }
                    }
                }.show()
        }

        binding.rlClearCache.setOnClickListener {
            XPopup.Builder(this@SettingFragment.context)
                .asConfirm("提示","确认清理缓存?",object :OnConfirmListener{
                    override fun onConfirm() {
                        SpUtil.clearAll()
                        SPUtils.getInstance().clear()
                        GSYVideoManager.instance().clearAllDefaultCache(this@SettingFragment.context)
                        //通知视频列表等,清楚通过缓存获取的状态(播放记录及播放时间等)
                        EventBus.getDefault().post(SimpleMessage.REFRESH)
                    }
                }).show()
        }
    }

}