package com.tlz.indexrecyclerview

import android.support.v7.widget.RecyclerView

/**
 * Created by Tomlezen.
 * Data: 2018/7/6.
 * Time: 10:56.
 */
abstract class SampleIndexRecyclerViewAdapter<D : SampleIndex, M : RecyclerView.ViewHolder, N : RecyclerView.ViewHolder> : RecyclerView.Adapter<M>(), IndexRecyclerViewAdapter<N> {

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

    override fun getAdapterPositionByIndexPosition(position: Int): Int =
            if (position == 0) 0 else data.subList(0, position).sumBy { it.getDataCount() }

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