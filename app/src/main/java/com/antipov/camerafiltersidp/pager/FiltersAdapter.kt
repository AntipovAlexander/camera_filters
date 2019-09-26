package com.antipov.camerafiltersidp.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.lang.RuntimeException

class FiltersAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> FilterOne()
            1 -> FilterTwo()
            2 -> FilterThree()
            else -> throw RuntimeException("add filter")
        }
    }

    override fun getCount(): Int {
        return 3
    }
}