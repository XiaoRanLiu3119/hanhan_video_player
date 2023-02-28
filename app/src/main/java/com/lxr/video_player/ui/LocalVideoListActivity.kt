package com.lxr.video_player.ui

import android.content.Intent
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.blankj.utilcode.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.drake.brv.annotaion.AnimationType
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.divider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.dyne.myktdemo.base.BaseActivity
import com.hjq.bar.TitleBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnCancelListener
import com.lxj.xpopup.interfaces.OnConfirmListener
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
 * @Author : Liu XiaoRan
 * @Email : 592923276@qq.com
 * @Date : on 2023/1/9 16:25.
 * @Description :
 */
class LocalVideoListActivity : BaseActivity<ActivityVideoListBinding>() {

    /**
     * 当前文件夹id,由文件夹列表传进
     */
    var bucketDisplayName: String? = ""

    override fun initView() {
        binding.titleBar.leftTitle = intent.getStringExtra("title")
        bucketDisplayName = intent.getStringExtra("bucketDisplayName")
        binding.rv.run {

            linear().divider(R.drawable.divider).setup {
                setAnimation(AnimationType.SLIDE_BOTTOM)
                addType<VideoInfo>(R.layout.item_video)
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
                        findView<ProgressBar>(R.id.progressBar).max =
                            getModel<VideoInfo>().duration.toInt()
                    }

                    // 已播放进度时长
                    val progressPlayed = SpUtil.getLong(getModel<VideoInfo>().id.toString())
                    if (progressPlayed != -0L && duration.isNotEmpty()) { // 有进度且有时长都显示
                        findView<TextView>(R.id.tv_duration).text =
                            CommonUtil.stringForTime(progressPlayed!!) + "/" + duration
                        findView<ProgressBar>(R.id.progressBar).progress = progressPlayed.toInt()
                    } else { // 没有进度(也包括没时长,此时是空串,不耽误)
                        findView<TextView>(R.id.tv_duration).text = duration
                    }
                }

                // 监听编辑(切换)模式,当前页面的backPress方法也实现了关闭方法,也会触发
                onToggle { position, toggleMode, end ->
                    // 刷新列表,item的选择按钮根据开关显隐,所以要刷新
                    val model = getModel<VideoInfo>(position)
                    model.checkBoxVisibility = toggleMode
                    //数据变化.通知ui变化
                    model.notifyChange()

                    if (end) {//列表遍历结束
                        // 编辑菜单根据编辑模式的开关显隐
                        binding.llMenu.visibility = if (toggleMode) View.VISIBLE else View.GONE
                        // 如果取消编辑模式则取消全选,目前按返回键和和标题栏关闭多选时触发
                        if (!toggleMode) checkedAll(false)
                    }
                }

                onClick(R.id.item) {
                    if (!toggleMode) {
                        val intent = Intent(this@LocalVideoListActivity, PlayerActivity::class.java)
                        intent.putExtra("videoList", GsonUtils.toJson(models))
                        intent.putExtra("position", modelPosition)
                        startActivity(intent)
                    } else {//编辑模式,设置选择状态
                        setChecked(layoutPosition, !getModel<VideoInfo>().checked)
                    }
                }

                // 监听列表选中
                onChecked { position, isChecked, isAllChecked ->//刷新当前选中条目状态
                    val model = getModel<VideoInfo>(position)
                    model.checked = isChecked
                    model.notifyChange()

                    // 编辑模式中的删除根据是否有选中条目是否启用
                    if (bindingAdapter.checkedCount != 0) {
                        binding.tvDelete.isEnabled = true
                        binding.tvDelete.setTextColor(ColorUtils.getColor(R.color.red))
                    } else {
                        binding.tvDelete.isEnabled = false
                        binding.tvDelete.setTextColor(ColorUtils.getColor(R.color.disable_text))
                    }
                }

                //长按
                onLongClick(R.id.item) {
                    if (!toggleMode) {//开启编辑模式
                        toggle()
                        //并选中当前条
                        setChecked(layoutPosition, true)
                    }
                }
            }
        }

        initEditMode()
    }

    override fun onResume() {
        super.onResume()
        //因为在这更新了数据,所以如果resume时候是编辑模式,更新后数据新的item的checkBoxVisibility默认是false,也就是隐藏状态,会导致开启了编辑(目前仅多选)模式,但是条目的checkBox是隐藏的
        //所以先关闭编辑模式再获取数据(下策,且不该在resume随意刷新数据)
        binding.rv.bindingAdapter.toggle(false)
        updateListData()
    }

    override fun onRightClick(titleBar: TitleBar?) {
        binding.rv.bindingAdapter.toggle() // 点击事件触发切换事件
    }

    override fun onBackPressed() {
        if (binding.rv.bindingAdapter.toggleMode) {//当前是编辑选择模式则关闭
            binding.rv.bindingAdapter.toggle(false)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 初始化编辑模式视图
     */
    private fun initEditMode() {
        val adapter = binding.rv.bindingAdapter

        // 全选
        binding.tvAllChecked.setOnClickListener {
            adapter.checkedAll()
        }

        // 取消选择
        binding.tvCancelChecked.setOnClickListener {
            adapter.checkedAll(false)
        }

        // 删除
        binding.tvDelete.setOnClickListener {
            XPopup.Builder(this)
                .asConfirm(
                    "已选择( ${adapter.checkedCount} / ${adapter.modelCount} )",
                    "确定删除?", "点错了",
                    "确定",
                    object : OnConfirmListener {
                        override fun onConfirm() {
                            showLoading()
                            adapter.getCheckedModels<VideoInfo>().forEach {
                                if (FileUtils.delete(it.path)) {
                                    // 删除缓存的影片时长(部分系统获取不到时长的影片,已在播放的时候缓存)
                                    SPUtils.getInstance()
                                        .remove(it.id.toString())
                                    // 删除缓存的播放进度
                                    SpUtil.removeKey(it.id.toString())
                                    // 文件增删需要通知系统扫描,否则删除文件后还能查出来
                                    // 这个工具类直接传文件路径不知道为啥通知失败,手动获取一下
                                    FileUtils.notifySystemToScan(
                                        FileUtils.getDirName(
                                            it.path
                                        )
                                    )
                                } else {
                                    ToastUtils.showShort("部分文件删除失败")
                                }
                            }
                            EventBus.getDefault().post(SimpleMessage.REFRESH)
                            adapter.toggle(false)
                        }
                    }, object : OnCancelListener {
                        override fun onCancel() {
                            adapter.toggle(false)
                        }
                    }, false
                ).show()
        }
    }


    /**
     * 更新电影列表,和文件夹列表一样全都遍历出来后再筛选出当前文件夹的媒体  todo 后续看看contentResolver 能否按指定文件夹把文件遍历出来
     */
    private fun updateListData() {
        binding.rv.models = Utils.getVideoList().filter { // 筛选出当前文件夹的视频
            it.bucketDisplayName.equals(bucketDisplayName)
        }.toMutableList()
    }

    override fun onSimpleMessage(simpleMessage: String?) {
        if (simpleMessage.equals(SimpleMessage.REFRESH)) {
            Handler().postDelayed({ // 文件变动需要主动让系统扫描,为避免未扫描完毕,加个延迟
                updateListData()
                dismissLoading()
            }, 1000)
        }
    }
}
