package com.salihutimothy.myaudiojournalapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.astuetz.PagerSlidingTabStrip
import com.salihutimothy.myaudiojournalapp.adapters.MyTabAdapter
import com.salihutimothy.myaudiojournalapp.fragments.RecordFragment

class MainActivity : AppCompatActivity() {
    private lateinit var tabs : PagerSlidingTabStrip
    private lateinit var viewPager : ViewPager
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

//    override fun onBackPressed() {
//
//        if (!searchView.isIconified) {
//            searchView.onActionViewCollapsed()
//        } else {
//            super.onBackPressed()
//        }
//    }
}