package com.example.fenrirpageradapter

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import java.util.ArrayList

abstract class FenrirPagerAdapter(fm: FragmentManager) : PagerAdapter() {

    private val mFragmentManager: FragmentManager = fm

    private val TAG = "FenrirPagerAdapter"
    private val DEBUG = false

    private var mCurTransaction: FragmentTransaction? = null

    val mSavedState = ArrayList<Fragment.SavedState?>()
    val mFragments = ArrayList<Fragment?>()
    private var mCurrentPrimaryItem: Fragment? = null

    private var activePosition = ArrayList<Int>()

    /**
     * Return the Fragment associated with a specified position.
     */
    abstract fun getItem(position: Int): Fragment

    override fun startUpdate(container: ViewGroup) {
        if (container.id == View.NO_ID) {
            throw IllegalStateException(
                "ViewPager with adapter " + this
                        + " requires a view id"
            )
        }
    }

    private var needChangePosition = ArrayList<Int>()

    override fun getItemPosition(`object`: Any): Int {
        var position = mFragments.indexOf(`object`as Fragment) // 有效果 不知道原因，点击会更新
        Log.e("pagerAdapter","getItemPosition " + position + " object ${`object`.toString()}" )

//        if (activePosition.any { needChangePosition.contains(it) }) {
//            return PagerAdapter.POSITION_NONE
//        }
        if (needChangePosition.contains(position)) {
            return PagerAdapter.POSITION_NONE
        }
        return PagerAdapter.POSITION_UNCHANGED
    }

    fun moveItemTo(itemPosition: Int, targetPosition: Int) {
        // stateList have delay
//        mSavedState.add(targetPosition, getItemAndRemove(itemPosition, mSavedState))
//        mFragments.add(targetPosition, getItemAndRemove(itemPosition, mFragments))
        var temp: Fragment?
        if (itemPosition < mFragments.size && targetPosition < mFragments.size) {
            temp = getItemAndRemove(itemPosition, mFragments)
            mFragments.add(targetPosition, temp)
        } else {
            while (mFragments.size <= targetPosition) {
                mFragments.add(null)
            }
            mFragments[targetPosition] = null
            // 更换位置后，无法正常index
        }

        var tempState: Fragment.SavedState?
        if (itemPosition < mSavedState.size && targetPosition < mSavedState.size) {
            tempState = getItemAndRemove(itemPosition, mSavedState)
            mSavedState.add(targetPosition, tempState)
        } else {
            while (mSavedState.size <= targetPosition) {
                mSavedState.add(null)
            }
            mSavedState[targetPosition] = null
        }
        var index = mFragments.indexOf(mCurrentPrimaryItem)
        Log.e("pagerAdapter","index $index")
        when (index) {
            itemPosition, targetPosition -> {
                if (activePosition.contains(itemPosition) || activePosition.contains(itemPosition)) {
                    makeNeedChangePositionList(itemPosition, targetPosition)
                    notifyDataSetChanged()
                    Log.e("pagerAdapter","afterNotify")
                    needChangePosition.clear()
                }
            }
        }
    }

    private fun makeNeedChangePositionList(itemPosition: Int, targetPosition: Int) {
        needChangePosition.addAll(itemPosition.rangeTo(targetPosition).toList())
    }

    private fun <T> getItemAndRemove(index: Int, list: ArrayList<T>): T {
        var temp = list[index]
        list.remove(temp)
        return temp
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // If we already have this item instantiated, there is nothing
        // to do.  This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        Log.e("pagerAdapter","instantiateItem position $position")

        activePosition.add(position)

        if (mFragments.size > position) {
            val f = mFragments[position]
            if (f != null) {
                return f
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction()
        }

        val fragment = getItem(position)
        if (DEBUG) Log.v(TAG, "Adding item #$position: f=$fragment")
        if (mSavedState.size > position) {
            val fss = mSavedState[position]
            if (fss != null) {
                fragment.setInitialSavedState(fss)
                Log.e("pagerAdapter","in instantiateItem set state fragment ${fragment.arguments.toString() } hash" + fragment.hashCode())
            }
        }
        while (mFragments.size <= position) {
            mFragments.add(null)
        }
        fragment.setMenuVisibility(false)
        fragment.userVisibleHint = false
        mFragments[position] = fragment
        mCurTransaction!!.add(container.id, fragment)

        Log.e("pagerAdapter","over instantiateItem fragment ${fragment.arguments.toString() } hash" + fragment.hashCode())
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        activePosition.remove(position)
        Log.e("pagerAdapter","destroyItem")

        val fragment = `object` as Fragment

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction()
        }

        while (mSavedState.size <= position) {
            mSavedState.add(null)
        }
        if (fragment.isAdded) {
            mSavedState[position] = mFragmentManager.saveFragmentInstanceState(fragment)
        } else {
            mSavedState[position] = null
        }

        mFragments[position] = null

        mCurTransaction!!.remove(fragment)
        Log.e("pagerAdapter","over destroyItem")
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, item: Any) {
        Log.e("pagerAdapter","setPrimaryItem　position $position item ${item.toString()}")

        val fragment = item as Fragment
        if (fragment !== mCurrentPrimaryItem) {
            if (mCurrentPrimaryItem != null) {
                mCurrentPrimaryItem!!.setMenuVisibility(false)
                mCurrentPrimaryItem!!.userVisibleHint = false
            }

            fragment.setMenuVisibility(true)
            fragment.userVisibleHint = true

            mCurrentPrimaryItem = fragment
        }
    }

    override fun finishUpdate(container: ViewGroup) {

        if (mCurTransaction != null) {
            mCurTransaction!!.commitNowAllowingStateLoss()
            mCurTransaction = null
        }
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return (`object` as Fragment).view === view
    }

    override fun saveState(): Parcelable? {
        Log.e("pagerAdapter","saveState")

        var state: Bundle? = null
        if (mSavedState.size > 0) {
            state = Bundle()
            val fss = arrayOfNulls<Fragment.SavedState>(mSavedState.size)
            state.putParcelableArray("states", fss)
        }
        for (i in mFragments.indices) {
            val f = mFragments[i]
            if (f != null && f.isAdded) {
                if (state == null) {
                    state = Bundle()
                }
                val key = "f$i"
                mFragmentManager.putFragment(state, key, f)
            }
        }
        return state
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        Log.e("pagerAdapter","restoreState")

        if (state != null) {
            val bundle = state as Bundle?
            bundle!!.classLoader = loader
            val fss = bundle.getParcelableArray("states")
            mSavedState.clear()
            mFragments.clear()
            if (fss != null) {
                for (i in fss.indices) {
                    mSavedState.add(fss[i] as Fragment.SavedState)
                }
            }
            val keys = bundle.keySet()
            for (key in keys) {
                if (key.startsWith("f")) {
                    val index = Integer.parseInt(key.substring(1))
                    val f = mFragmentManager.getFragment(bundle, key)
                    if (f != null) {
                        while (mFragments.size <= index) {
                            mFragments.add(null)
                        }
                        f.setMenuVisibility(false)
                        mFragments[index] = f
                    } else {
                        Log.w(TAG, "Bad fragment at key $key")
                    }
                }
            }
        }
    }
}