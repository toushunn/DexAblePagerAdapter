package com.example.fenrirpageradapter

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private lateinit var tabLayout:TabLayout
    private lateinit var viewpager:ViewPager
    private lateinit var button:Button

    private lateinit var adapter:MineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        viewpager = findViewById(R.id.viewPager)
        button = findViewById(R.id.button)

        adapter = MineAdapter(supportFragmentManager)
        viewpager.adapter = adapter
        tabLayout.setupWithViewPager(viewpager)

        button.setOnClickListener {
            adapter.moveItemTo(5,3)
        }


    }
}
