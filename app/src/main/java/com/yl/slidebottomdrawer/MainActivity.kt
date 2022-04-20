package com.yl.slidebottomdrawer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var rvList: RecyclerView? = null
    private var slideBottom: SlideBottomDrawer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slideBottom = findViewById(R.id.bottom_container)
        rvList = findViewById(R.id.rv_list)
        val ls = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)
        val adapter = MyAdapter()
        rvList!!.adapter = adapter
        rvList!!.layoutManager = LinearLayoutManager(this)
        adapter.submitList(ls)
//        slideBottom!!.setScrollChild(rvList!!)
        rvList!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition
                    val manager = recyclerView.layoutManager as LinearLayoutManager?
                    val firstVis = manager!!.findFirstCompletelyVisibleItemPosition()
                    slideBottom!!.childStop = firstVis == 0
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                val firstCompletelyVisibleItemPosition =
                    layoutManager!!.findFirstCompletelyVisibleItemPosition()
                slideBottom!!.childStop = firstCompletelyVisibleItemPosition == 0

            }
        })
    }
}