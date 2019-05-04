package com.tlz.indexrecyclerview

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

/**
 * Created by Tomlezen.
 * Data: 2018/7/20.
 * Time: 9:50.
 */
interface StickyRecyclerHeadersAdapter<VH : androidx.recyclerview.widget.RecyclerView.ViewHolder> {

    fun getHeaderId(position: Int): Long

    fun onCreateHeaderViewHolder(parent: ViewGroup): VH

    fun onBindHeaderViewHolder(holder: VH, position: Int)

}