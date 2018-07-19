package com.tlz.indexrecyclerview.example

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import com.tlz.indexrecyclerview.SampleIndex
import com.tlz.indexrecyclerview.SampleIndexRecyclerViewAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_view.view.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = MyViewAdapter()
        rv.adapter = adapter
        // 准备数据
        adapter.setNewData((0 until 26).mapTo(mutableListOf(), {
            val count = Random().nextInt(8) + 1
            ItemData( (0 until count).mapTo(mutableListOf(), { it.toString() }), "${('A'.toInt() + it).toChar()}, $count")
        }))
    }

    class ItemData(val data: List<String>, index: String) : SampleIndex(index) {

        override fun getDataCount(): Int = data.size

    }

    class MyViewAdapter : SampleIndexRecyclerViewAdapter<ItemData, RecyclerView.ViewHolder, RecyclerView.ViewHolder>() {

        override fun onBindIndexViewHolder(holder:  RecyclerView.ViewHolder, position: Int, item: ItemData) {
            holder.itemView.setBackgroundColor(Color.GRAY)
            holder.itemView.tv_item.setTextColor(Color.BLACK)
            holder.itemView.tv_item.text = item.index
        }

        override fun onBindViewHolder(holder:  RecyclerView.ViewHolder, position: Int, item: ItemData, subPosition: Int) {
            holder.itemView.tv_item.text = item.data[subPosition]
        }

        override fun onCreateIndexViewHolder(parent: ViewGroup):  RecyclerView.ViewHolder =
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_header_view, parent, false))

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder =
                ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false))
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
