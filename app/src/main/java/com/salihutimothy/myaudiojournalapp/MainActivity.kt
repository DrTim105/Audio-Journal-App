package com.salihutimothy.myaudiojournalapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager.widget.ViewPager
import com.salihutimothy.myaudiojournalapp.fragments.FileViewerFragment

class MainActivity : AppCompatActivity() {


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

//    override fun onBackPressed() {
//        val fragment =
//            this.supportFragmentManager.findFragmentById(R.id.fragment_container) as? NavHostFragment
//        val currentFragment =
//            fragment?.childFragmentManager?.fragments?.get(0) as? FileViewerFragment
//        if (currentFragment != null) {
//            currentFragment.onBackPressed().takeIf { !it }?.let { super.onBackPressed() }
//        } else {
//            super.onBackPressed()
//        }
//    }
}