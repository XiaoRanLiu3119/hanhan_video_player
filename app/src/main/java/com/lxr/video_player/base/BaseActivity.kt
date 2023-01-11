package com.dyne.myktdemo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.blankj.utilcode.util.LogUtils
import com.gyf.immersionbar.ktx.fitsTitleBar
import com.gyf.immersionbar.ktx.immersionBar
import com.hjq.bar.OnTitleBarListener
import com.hjq.bar.TitleBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.impl.LoadingPopupView
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity(), OnTitleBarListener {

    protected lateinit var binding: T

    protected var loadingPopup: LoadingPopupView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBeforeInitView()
        val type = javaClass.genericSuperclass as ParameterizedType
        val aClass = type.actualTypeArguments[0] as Class<*>
        val method = aClass.getDeclaredMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, layoutInflater) as T
        setContentView(binding.root)


        initTitleStatusBar()
        initView()
        initListener()
    }


    /**
     * 布局初始化之前
     */
    protected open fun initBeforeInitView(){

    }

    /**
     * 初始化布局
     */
    abstract fun initView()

    /**
     * 初始化监听器
     */
    open fun initListener() {

    }

    /**
     * 初始化标题栏和沉浸式状态栏
     */
    private fun initTitleStatusBar(){
        val findTitleBar = findTitleBar(findViewById(Window.ID_ANDROID_CONTENT))
        findTitleBar?.setOnTitleBarListener(this)

        immersionBar {
            if (findTitleBar != null) {
                fitsTitleBar(findTitleBar)
            }
        }
    }


    override fun onLeftClick(titleBar: TitleBar?) {
        super.onLeftClick(titleBar)
        finish()
    }

    override fun onRightClick(titleBar: TitleBar?) {
        super.onRightClick(titleBar)
    }

    override fun onTitleClick(titleBar: TitleBar?) {
        super.onTitleClick(titleBar)
    }

    private fun findTitleBar(group: ViewGroup): TitleBar? {
        for (i in 0 until group.childCount) {
            val view = group.getChildAt(i)
            if (view is TitleBar) {
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

    /**
     * 打开等待框
     */
    protected fun showLoading() {
        if (loadingPopup == null) {
            loadingPopup = XPopup.Builder(this)
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

}