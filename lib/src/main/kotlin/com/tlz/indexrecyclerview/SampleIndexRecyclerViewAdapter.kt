package com.tlz.indexrecyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * 默认实现时简单的索引适配器，满足大部分需求，如有其他需求可
 * 实现IndexRecyclerViewAdapter.
 * Created by Tomlezen.
 * Data: 2018/7/6.
 * Time: 10:56.
 */
abstract class SampleIndexRecyclerViewAdapter<D : SampleIndex, M : androidx.recyclerview.widget.RecyclerView.ViewHolder, N : androidx.recyclerview.widget.RecyclerView.ViewHolder> : androidx.recyclerview.widget.RecyclerView.Adapter<M>(), IndexRecyclerViewAdapter<N> {

    var data = mutableListOf<D>()
        private set

    /** item数量. */
    private var _itemCount = 0

    override fun getItemCount(): Int = _itemCount

    override fun getIndexList(): List<Index> = data

    final override fun onBindIndexViewHolder(holder: N, position: Int) =
            onBindIndexViewHolder(holder, position, data[getIndexPositionByAdapterPosition(position)])

    final override fun onBindViewHolder(holder: M, position: Int) {
        val (dataPosition, subPosition) = getSubPositionByPosition(position)
        onBindViewHolder(holder, position, data[dataPosition], subPosition)
    }

    abstract fun onBindIndexViewHolder(holder: N, position: Int, item: D)
    abstract fun onBindViewHolder(holder: M, position: Int, item: D, subPosition: Int)

    override fun getAdapterPositionByIndexBarPosition(position: Int): Int {
        // 先找到索引，如果没找到则选择该索引的下一个
        val index = getIndexList()[position].index
        var localPosition = 0
        run Break@{
            data.forEachIndexed { i, item ->
                if (i == data.size - 1 || item.index == index || item.index > index) {
                    localPosition = data.subList(0, i).sumBy { it.getDataCount() }
                    return@Break
                }
            }
        }
        return localPosition
    }

    override fun getIndexPositionByAdapterPosition(position: Int): Int {
        var dataPosition = 0
        val size = data.size - 1
        for (i in (0..size)) {
            dataPosition += data[i].getDataCount()
            if (dataPosition > position) {
                dataPosition = i
                break
            }
        }
        return dataPosition
    }

    override fun getIndexBarPositionByAdapterPosition(position: Int): Int {
        var dataPosition = 0
        val size = data.size - 1
        for (i in (0..size)) {
            dataPosition += data[i].getDataCount()
            if (dataPosition > position) {
                dataPosition = i
                break
            }
        }
        val index = data[dataPosition].index
        val indexList = getIndexList()
        for (i in (0 until indexList.size)) {
            if (indexList[i].index == index) {
                return i
            }
        }
        return dataPosition
    }

    protected open fun getSubPositionByPosition(position: Int): Pair<Int, Int> {
        var dataPosition = 0
        var subPosition = 0
        val size = data.size - 1
        for (i in (0..size)) {
            dataPosition += data[i].getDataCount()
            if (dataPosition > position) {
                subPosition = (position - dataPosition + data[i].getDataCount())
                dataPosition = i
                break
            } else if (i == size) {
                subPosition = (position - dataPosition)
                dataPosition = size
            }
        }
        return Pair(dataPosition, subPosition)
    }

    /**
     * 设置数据.
     * @param data List<D>
     */
    fun setNewData(data: List<D>) {
        this.data.clear()
        this.data.addAll(data)
        _itemCount = this.data.sumBy { it.getDataCount() }
        notifyDataSetChanged()
    }

}