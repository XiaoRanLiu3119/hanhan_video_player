package com.lxr.video_player.ui

import com.blankj.utilcode.util.SPUtils
import com.dyne.myktdemo.base.BaseFragment
import com.github.gzuliyujiang.filepicker.ExplorerConfig
import com.github.gzuliyujiang.filepicker.FilePicker
import com.github.gzuliyujiang.filepicker.annotation.ExplorerMode
import com.github.gzuliyujiang.filepicker.contract.OnFilePickedListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnConfirmListener
import com.lxr.video_player.R
import com.lxr.video_player.constants.Constants
import com.lxr.video_player.constants.SimpleMessage
import com.lxr.video_player.databinding.FragmentSettingBinding
import com.lxr.video_player.utils.SpUtil
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager
import org.greenrobot.eventbus.EventBus
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import java.io.File

/**
 * @Author      : Liu XiaoRan
 * @Email       : 592923276@qq.com
 * @Date        : on 2023/1/11 13:52.
 * @Description :
 */
class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    override fun initView() {

        binding.tvSubtitlePath.text = SpUtil.getString(Constants.K_DEFAULT_PATH_4_FIND_SUBTITLE)

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

        binding.rlSubtitlePath.setOnClickListener {
            val config = ExplorerConfig(context)
            config.rootDir = File(SpUtil.getString(Constants.K_DEFAULT_PATH_4_FIND_SUBTITLE)!!)
            config.explorerMode = ExplorerMode.DIRECTORY
            config.setOnFilePickedListener(object :OnFilePickedListener{
                override fun onFilePicked(file: File) {
                    binding.tvSubtitlePath.text = file.absolutePath
                    SpUtil.put(Constants.K_DEFAULT_PATH_4_FIND_SUBTITLE, file.absolutePath)
                }
            })
            val picker = FilePicker(activity)
            picker.setExplorerConfig(config)
            picker.okView.text = "就用这个目录"
            picker.show()
        }
    }

}