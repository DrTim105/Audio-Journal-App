package com.salihutimothy.myaudiojournalapp.Adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.salihutimothy.myaudiojournalapp.Fragments.FileViewerFragment
import com.salihutimothy.myaudiojournalapp.Fragments.RecordFragment

class MyTabAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private var titles = arrayOf("Record", "Saved Recording")

    override fun getCount(): Int {
        return titles.size
    }

    override fun getItem(position: Int): Fragment {

        val fragment : Fragment = when(position) {
            0 -> RecordFragment.newInstance()
            1 -> FileViewerFragment.newInstance()
            else -> RecordFragment.newInstance()
        }

        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}