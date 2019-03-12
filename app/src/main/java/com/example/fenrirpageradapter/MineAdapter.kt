package com.example.fenrirpageradapter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager

class MineAdapter(fm:FragmentManager):FenrirPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        if (mFragments.size > position && mFragments[position] != null) {
            return mFragments[position]!!
        }
        var fragment = TestFragment()
        var bundle = Bundle()
        bundle.putString("text","position " + position)
        bundle.putInt("position", position)

        fragment.arguments = bundle
        return fragment
    }

    override fun getCount(): Int {
        return 10
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return "$position"
    }

}