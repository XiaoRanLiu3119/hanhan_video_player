package com.lxr.video_player.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ToastUtils
import com.dyne.myktdemo.base.BaseActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxr.video_player.databinding.ActivityMainBinding
import nl.joery.animatedbottombar.AnimatedBottomBar

class MainActivity : BaseActivity<ActivityMainBinding>() {

    val fragments = listOf<Fragment>(
        MovieFoldersListFragment(),
        SettingFragment()
    )

    override fun initView() {

        binding.vp2.adapter = object : FragmentStateAdapter(this@MainActivity) {
            override fun getItemCount(): Int = fragments.size

            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        binding.bottomBar.setupWithViewPager2(binding.vp2)
    }
}