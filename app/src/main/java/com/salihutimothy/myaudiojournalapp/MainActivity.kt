package com.salihutimothy.myaudiojournalapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val pref = sp.getBoolean("theme", false)

        if (pref){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
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

    override fun onDestroy() {
        Log.d("BUG - activity", "stop service")

        super.onDestroy()
    }

    override fun onStop() {
        Log.d("BUG - onstop", "stop service")

        super.onStop()
    }
}