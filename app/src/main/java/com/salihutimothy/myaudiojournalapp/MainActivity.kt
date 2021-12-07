package com.salihutimothy.myaudiojournalapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.astuetz.PagerSlidingTabStrip

class MainActivity : AppCompatActivity() {

    private lateinit var tabs : PagerSlidingTabStrip
    private lateinit var viewPager : ViewPager
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabs = findViewById(R.id.tabs)
        viewPager = findViewById(R.id.pager)
        toolbar = findViewById(R.id.toolbar)

        viewPager.adapter = MyTabAdapter(supportFragmentManager)
        tabs.setViewPager(viewPager)
        setSupportActionBar(toolbar)

    }
}