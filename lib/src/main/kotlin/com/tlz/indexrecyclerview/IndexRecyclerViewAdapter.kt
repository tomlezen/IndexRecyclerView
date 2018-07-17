package com.tlz.indexrecyclerview

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter

/**
 * Created by Tomlezen.
 * Data: 2018/7/6.
 * Time: 9:37.
 */
interface IndexRecyclerViewAdapter<T : RecyclerView.ViewHolder> : StickyRecyclerHeadersAdapter<T> {

    @Deprecated("", ReplaceWith("getAdapterPositionByIndexPosition(position).toLong()"))
    override fun getHeaderId(position: Int): Long = getAdapterPositionByIndexPosition(position).toLong()

    @Deprecated("", ReplaceWith("onBindIndexViewHolder(holder, position)"))
    override fun onBindHeaderViewHolder(holder: T, position: Int) = onBindIndexViewHolder(holder, position)

    fun onBindIndexViewHolder(holder: T, position: Int)

    @Deprecated("", ReplaceWith("onCreateIndexViewHolder(parent)"))
    override fun onCreateHeaderViewHolder(parent: ViewGroup): T = onCreateIndexViewHolder(parent)

    fun onCreateIndexViewHolder(parent: ViewGroup): T

    /** 获取索引列表. */
    fun getIndexList(): List<Index>

    /**
     * 根据索引position获取适配器位置.
     * @param position Int
     * @return Int
     */
    fun getIndexPositionByAdapterPosition(position: Int): Int

    /**
     * 根据适配器位置获取索引位置.
     * @param position Int
     * @return Int
     */
    fun getAdapterPositionByIndexPosition(position: Int): Int

}