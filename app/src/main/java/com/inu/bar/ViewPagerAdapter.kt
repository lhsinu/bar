package com.inu.bar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter (fragment : FragmentActivity) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DrivingHistoryFragment()
            1 -> HealthHistoryFragment()
            2 -> AccidentHistoryFragment()
            3 -> CallSettingFragment()
            else -> DrivingHistoryFragment()
        }
    }
}