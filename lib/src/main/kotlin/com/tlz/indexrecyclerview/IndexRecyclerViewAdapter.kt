package com.tlz.indexrecyclerview

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Created by Tomlezen.
 * Data: 2018/7/6.
 * Time: 9:37.
 */
interface IndexRecyclerViewAdapter<T : RecyclerView.ViewHolder> : StickyRecyclerHeadersAdapter<T> {

    @Deprecated("", ReplaceWith("getAdapterPositionByIndexPosition(position).toLong()"))
    override fun getHeaderId(position: Int): Long = getIndexPositionByAdapterPosition(position).toLong()

    @Deprecated("", ReplaceWith("onBindIndexViewHolder(holder, position)"))
    override fun onBindHeaderViewHolder(holder: T, position: Int) = onBindIndexViewHolder(holder, position)

    fun onBindIndexViewHolder(holder: T, position: Int)

    @Deprecated("", ReplaceWith("onCreateIndexViewHolder(parent)"))
    override fun onCreateHeaderViewHolder(parent: ViewGroup): T = onCreateIndexViewHolder(parent)

    fun onCreateIndexViewHolder(parent: ViewGroup): T

    /** 获取索引列表. */
    fun getIndexList(): List<Index>

    /**
     * 根据适配器位置获取索引位置
     * @param position Int
     * @return Int
     */
    fun getIndexPositionByAdapterPosition(position: Int): Int

    @Deprecated("方法名之前命名错误，现废弃使用", ReplaceWith("getAdapterPositionByIndexBarPosition(position)"))
    fun getAdapterPositionByIndexPosition(position: Int): Int = getAdapterPositionByIndexBarPosition(position)

    /**
     * 根据索引条position获取适配器位置.
     * @param position Int
     * @return Int
     */
    fun getAdapterPositionByIndexBarPosition(position: Int): Int

    /**
     * 根据适配器位置获取索引条选中的位置
     * @param position Int
     * @return Int
     */
    fun getIndexBarPositionByAdapterPosition(position: Int): Int


}