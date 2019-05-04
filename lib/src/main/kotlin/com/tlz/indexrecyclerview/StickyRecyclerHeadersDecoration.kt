package com.tlz.indexrecyclerview

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Tomlezen.
 * Data: 2018/7/20.
 * Time: 9:52.
 */
class StickyRecyclerHeadersDecoration(
    private val adapter: StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder>,
    private val renderInline: Boolean = false
) : RecyclerView.ItemDecoration() {

    var itemSpace = 0

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        parent.let {
            val position = parent.getChildAdapterPosition(view)
            var headerHeight = 0

            if (position != RecyclerView.NO_POSITION && hasHeader(position) && showHeaderAboveItem(parent, position)) {

                val header = getHeader(parent, position).itemView
                headerHeight = getHeaderHeightForLayout(header) - itemSpace
            }

            outRect.set(0, headerHeight, 0, 0)
        }
    }

    private fun showHeaderAboveItem(parent: RecyclerView, position: Int): Boolean =
            if (isReverseLayout(parent)) {
                val itemCount = parent.layoutManager?.itemCount ?: 0
                position == itemCount - 1 || adapter.getHeaderId(position + 1) != adapter.getHeaderId(position)
            } else {
                position == 0 || adapter.getHeaderId(position - 1) != adapter.getHeaderId(position)
            }

    private fun hasHeader(position: Int): Boolean = adapter.getHeaderId(position) != NO_HEADER_ID

    private fun getHeader(parent: RecyclerView, position: Int): RecyclerView.ViewHolder {
        val holder = adapter.onCreateHeaderViewHolder(parent)
        val header = holder.itemView

        adapter.onBindHeaderViewHolder(holder, position)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                parent.paddingLeft + parent.paddingRight, header.layoutParams.width)
        val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                parent.paddingTop + parent.paddingBottom, header.layoutParams.height)

        header.measure(childWidth, childHeight)
        header.layout(0, 0, header.measuredWidth, header.measuredHeight)

        return holder
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val count = parent.childCount
        var previousHeaderId: Long = -1

        if (isReverseLayout(parent)) {
            for (layoutPos in count - 1 downTo 0) {
                previousHeaderId = calculateHeaderIdAndDrawHeader(canvas, parent, true, layoutPos, previousHeaderId)
            }
        } else {
            for (layoutPos in 0 until count) {
                previousHeaderId = calculateHeaderIdAndDrawHeader(canvas, parent, false, layoutPos, previousHeaderId)
            }
        }
    }

    private fun calculateHeaderIdAndDrawHeader(canvas: Canvas, parent: RecyclerView, isReverseLayout: Boolean,
                                               layoutPos: Int, previousHeaderId: Long): Long {
        var localPreviousHeaderId = previousHeaderId
        val child = parent.getChildAt(layoutPos)
        val adapterPos = parent.getChildAdapterPosition(child)

        if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
            val headerId = adapter.getHeaderId(adapterPos)

            if (headerId != previousHeaderId) {
                localPreviousHeaderId = headerId
                val header = getHeader(parent, adapterPos).itemView
                canvas.save()

                val left = child.left
                val top = getHeaderTop(parent, isReverseLayout, child, header, adapterPos, layoutPos)
                canvas.translate(left.toFloat(), top.toFloat())

                header.translationX = left.toFloat()
                header.translationY = top.toFloat()
                header.draw(canvas)
                canvas.restore()
            }
        }

        return localPreviousHeaderId
    }

    private fun getHeaderTop(parent: RecyclerView, isReverseLayout: Boolean, child: View, header: View, adapterPos: Int, layoutPos: Int): Int {
        val childCount = parent.childCount
        val headerHeight = getHeaderHeightForLayout(header)
        val top = child.y.toInt() - headerHeight
        val currentHeaderId = adapter.getHeaderId(adapterPos)

        if (isReverseLayout && layoutPos == childCount - 1) {
            for (i in childCount - 1 downTo 1) {
                val offset = calculateOffset(parent, headerHeight, currentHeaderId, i)
                if (offset < 0) {
                    return offset
                }
            }
        } else if (!isReverseLayout && layoutPos == 0) {
            for (i in 1 until childCount) {
                val offset = calculateOffset(parent, headerHeight, currentHeaderId, i)
                if (offset < 0) {
                    return offset
                }
            }
        }
        return Math.max(0, top)
    }

    private fun calculateOffset(parent: RecyclerView, headerHeight: Int, currentHeaderId: Long, nextPosition: Int): Int {
        val adapterPosHere = parent.getChildAdapterPosition(parent.getChildAt(nextPosition))
        if (adapterPosHere != RecyclerView.NO_POSITION) {
            val nextId = adapter.getHeaderId(adapterPosHere)
            if (nextId != currentHeaderId && nextId != NO_HEADER_ID) {
                val next = parent.getChildAt(nextPosition)
                return next.y.toInt() - (headerHeight + getHeader(parent, adapterPosHere).itemView.height)
            }
        }
        return 0
    }

    private fun getHeaderHeightForLayout(header: View): Int = if (renderInline) 0 else header.height

    private fun isReverseLayout(parent: RecyclerView): Boolean {
        val layoutManager = parent.layoutManager
        return (layoutManager as? androidx.recyclerview.widget.LinearLayoutManager)?.reverseLayout ?: false
    }

    companion object {
        const val NO_HEADER_ID = -1L
    }

}