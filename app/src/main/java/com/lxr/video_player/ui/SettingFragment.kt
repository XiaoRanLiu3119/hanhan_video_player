package com.lxr.video_player.ui

import com.dyne.myktdemo.base.BaseFragment
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSelectListener
import com.lxr.video_player.R
import com.lxr.video_player.databinding.FragmentSettingBinding
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/11 13:52.
 * @Description :
 */
class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    var checkedPlayerManagerPosition: Int = 0

    override fun initView() {
        if (PlayerFactory.getPlayManager() is IjkPlayerManager) {
            binding.tvPlayerManager.text = resources.getString(R.string.player_manager_ijk)
        } else {
            binding.tvPlayerManager.text = resources.getString(R.string.player_manager_exo)
        }
        when (PlayerFactory.getPlayManager()) {
            is IjkPlayerManager -> {
                binding.tvPlayerManager.text = resources.getString(R.string.player_manager_ijk)
                checkedPlayerManagerPosition = 0
            }
            is Exo2PlayerManager -> {
                binding.tvPlayerManager.text = resources.getString(R.string.player_manager_exo)
                checkedPlayerManagerPosition = 1
            }
            is SystemPlayerManager -> {
                binding.tvPlayerManager.text = resources.getString(R.string.player_manager_system)
                checkedPlayerManagerPosition = 2
            }
        }
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
                        0 -> PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
                        1 -> PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
                        2 -> PlayerFactory.setPlayManager(SystemPlayerManager::class.java)
                    }
                }.setCheckedPosition(checkedPlayerManagerPosition).show()

        }
    }

}