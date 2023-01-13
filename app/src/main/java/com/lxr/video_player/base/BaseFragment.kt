package com.dyne.myktdemo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils
import com.gyf.immersionbar.ktx.fitsTitleBar
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.ParameterizedType

/**
 * @Author      : xia chuanqi
 * @Email       : 751528989@qq.com
 * @Date        : on 2022/6/9 17:08.
 * @Description :
 */
abstract class BaseFragment<T : ViewBinding> : Fragment(),OnTitleBarListener{
    val TAG = javaClass.name
    protected lateinit var binding: T
    protected var loadingPopup: LoadingPopupView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val type = javaClass.genericSuperclass as ParameterizedType
        val aClass = type.actualTypeArguments[0] as Class<*>
        val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java,ViewGroup::class.java,Boolean::class.java)
        binding = method.invoke(null,layoutInflater,container,false) as T
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initTitleStatusBar()
        initData()
        initListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onEvent(simpleMessage: String?) {
    }

    /**
     * 初始化布局
     */
    open fun initView(){

    }

    /**
     * 页面初始化后初始化数据
     */
    open fun initData() {

    }

    /**
     * 初始化监听器
     */
    open fun initListener() {

    }


    /**
     * 打开等待框
     */
    protected fun showLoading() {
        if (loadingPopup == null) {
            loadingPopup = XPopup.Builder(context)
                .dismissOnBackPressed(false)
                .isLightNavigationBar(true)
                .hasShadowBg(false)
                .asLoading()
                .show() as LoadingPopupView
        }
        loadingPopup?.show()
    }

    /**
     * 关闭等待框
     */
    protected fun dismissLoading() {
        loadingPopup?.dismiss()
    }


    private fun initTitleStatusBar(){
        val findTitleBar = findTitleBar((view as ViewGroup?)!!)
        if (findTitleBar is TitleBar){
            findTitleBar.setOnTitleBarListener(this)
        }

        immersionBar {
            if (findTitleBar != null) {//沉浸式适配出标题栏的颜色
                fitsTitleBar(findTitleBar)
            }
        }
    }

    /**
     * 遍历出标题栏
     */
    private fun findTitleBar(group: ViewGroup): View? {
        for (i in 0 until group.childCount) {
            val view = group.getChildAt(i)
            if (view is TitleBar || view is Toolbar) {
                return view
            } else if (view is ViewGroup) {
                val titleBar = findTitleBar(view)
                if (titleBar != null) {
                    return titleBar
                }
            }
        }
        return null
    }

    override fun onLeftClick(titleBar: TitleBar?) {
        super.onLeftClick(titleBar)
    }

    override fun onRightClick(titleBar: TitleBar?) {
        super.onRightClick(titleBar)
    }

    override fun onTitleClick(titleBar: TitleBar?) {
        super.onTitleClick(titleBar)
    }
}