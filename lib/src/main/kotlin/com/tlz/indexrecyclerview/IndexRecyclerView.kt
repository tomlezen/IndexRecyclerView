package com.tlz.indexrecyclerview

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.CallSuper
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration

/**
 * Created by Tomlezen.
 * Data: 2018/7/5.
 * Time: 17:52.
 */
class IndexRecyclerView(ctx: Context, attrs: AttributeSet) : RecyclerView(ctx, attrs) {

    private val hMargin: Int
    private val vMargin: Int
    private val hPadding: Int
    private val vPadding: Int

    private val indexTextColor: Int
    private val indexTextSize: Int
    private val selectedIndexTextColor: Int

    private val previewIndexTextColor: Int
    private val previewIndexTextSize: Int

    private val indexBarBackground: Int
    private val previewIndexBackground: Int

    private val previewRectSize: Int
    private val previewRectRadius: Int

    private val isAutoDismiss: Boolean
    private val autoDismissTime: Long

    /** 画笔. */
    private val drawnPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 索引条绘制区域. */
    private val indexBarDrawnRectF = RectF()
    /** 选择的索引绘制区域. */
    private val previewIndexDrawnRectF = RectF()

    /** 当前选中的位置. */
    private var selectedPosition = 0
    /** 索引条是否显示. */
    private var isIndexBarShowing = false
        get() = field || !isAutoDismiss
    /** 是否触摸到索引条. */
    private var isTouchedIndexBar = false
        set(value) {
            field = value
            postInvalidate()
        }
    /** 是否是索引适配器. */
    private var isIndexAdapter = false
    /** 当前是否在等待索引条消失. */
    private var isWaitDismiss = false

    private var headersDecoration: StickyRecyclerHeadersDecoration? = null

    private val dismissAction: Runnable by lazy {
        Runnable {
            hideIndexBar()
        }
    }

    private val animator: ValueAnimator by lazy {
        ValueAnimator.ofFloat(0f, 1f)
                .apply {
                    duration = 500L
                    addUpdateListener {
                        val value = it.animatedValue as Float
                        animatorProgress = if (!isIndexBarShowing) (1f - value) else value
                    }
                }
    }
    private var animatorProgress = 0f
        set(value) {
            field = value
            postInvalidate()
        }

    private val argbEvaluator by lazy { ArgbEvaluator() }

    /** 索引装饰器. */
    var indexDecoration: IndexDecoration = DefIndexDecoration()

    init {
        val ta = resources.obtainAttributes(attrs, R.styleable.IndexRecyclerView)

        hMargin = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_margin_horizontal, resources.getDimensionPixelSize(R.dimen.def_index_bar_margin_horizontal))
        vMargin = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_margin_vertical, resources.getDimensionPixelSize(R.dimen.def_index_bar_margin_vertical))
        hPadding = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_padding_horizontal, resources.getDimensionPixelSize(R.dimen.def_index_bar_padding_horizontal))
        vPadding = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_padding_vertical, resources.getDimensionPixelSize(R.dimen.def_index_bar_padding_vertical))
        indexTextSize = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_text_size, resources.getDimensionPixelSize(R.dimen.def_index_bar_text_size))
        previewIndexTextSize = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_preview_text_size, resources.getDimensionPixelSize(R.dimen.def_index_bar_preview_text_size))
        previewRectSize = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_preview_rect_size, resources.getDimensionPixelSize(R.dimen.def_index_bar_preview_rect_size))
        previewRectRadius = ta.getDimensionPixelSize(R.styleable.IndexRecyclerView_index_bar_preview_rect_radius, resources.getDimensionPixelSize(R.dimen.def_index_bar_preview_rect_radius))

        indexTextColor = ta.getColor(R.styleable.IndexRecyclerView_index_bar_text_color, resources.getColor(R.color.def_index_bar_text_color))
        selectedIndexTextColor = ta.getColor(R.styleable.IndexRecyclerView_index_bar_selected_text_color, resources.getColor(R.color.def_index_bar_selected_text_color))
        previewIndexTextColor = ta.getColor(R.styleable.IndexRecyclerView_index_bar_preview_text_color, resources.getColor(R.color.def_index_bar_preview_text_color))
        indexBarBackground = ta.getColor(R.styleable.IndexRecyclerView_index_bar_background, resources.getColor(R.color.def_index_bar_background))
        previewIndexBackground = ta.getColor(R.styleable.IndexRecyclerView_index_bar_preview_rect_background, resources.getColor(R.color.def_index_bar_preview_index_background))

        isAutoDismiss = ta.getBoolean(R.styleable.IndexRecyclerView_index_bar_auto_dismiss, true)
        autoDismissTime = ta.getInteger(R.styleable.IndexRecyclerView_index_bar_auto_dismiss_time, resources.getInteger(R.integer.def_index_bar_auto_dismiss_time)).toLong()

        ta.recycle()

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    showIndexBar()
                } else {
                    sendDismissMessage()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                if (isIndexAdapter && !isTouchedIndexBar) {
                    val firstVisiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    selectedPosition = (adapter as IndexRecyclerViewAdapter<*>).getAdapterPositionByIndexPosition(firstVisiblePosition)
                }
            }
        })

        super.setLayoutManager(LinearLayoutManager(context))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed) {
            // 测量文字宽度.
            drawnPaint.textSize = indexTextSize.toFloat()
            val indexWidth = drawnPaint.measureText("宽")
            indexBarDrawnRectF.set(
                    r - paddingRight - hPadding * 2 - hMargin - indexWidth,
                    (t + paddingTop + vMargin).toFloat(),
                    (r - paddingRight - hMargin).toFloat(),
                    (b - paddingBottom - vMargin).toFloat()
            )

            previewIndexDrawnRectF.set(
                    width / 2 - previewRectSize / 2f,
                    height / 2 - previewRectSize / 2f,
                    width / 2 + previewRectSize / 2f,
                    height / 2 + previewRectSize / 2f
            )
        }
    }

    @CallSuper
    override fun draw(c: Canvas?) {
        super.draw(c)
        if (isIndexAdapter && (isIndexBarShowing || animator.isRunning)) {
            c?.run {
                // 绘制背景
                indexDecoration.drawIndexBarBg(this, drawnPaint, indexBarDrawnRectF, getAnimatorColor(indexBarBackground))

                // 绘制索引文字.
                drawIndexBarText(this)

                if (isTouchedIndexBar) {
                    // 绘制预览框背景
                    indexDecoration.drawPreviewBg(this, drawnPaint, previewIndexDrawnRectF, getAnimatorColor(previewIndexBackground), previewRectRadius.toFloat())

                    // 绘制预览索引文字
                    drawPreviewIndex(this)
                }
            }
        }
    }

    /**
     * 是否在索引条区域.
     * @param e MotionEvent?
     * @return Boolean
     */
    private fun isInIndexBarRect(e: MotionEvent?): Boolean =
            isIndexBarShowing && indexBarDrawnRectF.contains(e?.x ?: 0f, e?.y ?: 0f)

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return isInIndexBarRect(e) || super.onInterceptTouchEvent(e)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        var isIntercept = false
        when (e?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isInIndexBarRect(e)) {
                    isIntercept = true
                    isTouchedIndexBar = true
                    showIndexBar()
                    calculateSelectPosition(e.y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isInIndexBarRect(e) && isIndexBarShowing && isTouchedIndexBar) {
                    isIntercept = true
                    calculateSelectPosition(e.y)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                sendDismissMessage()
                isTouchedIndexBar = false
                sendDismissMessage()
            }
        }
        return if (isIntercept || isTouchedIndexBar) true else super.onTouchEvent(e)
    }

    private fun getAnimatorColor(color: Int) =
        argbEvaluator.evaluate(animatorProgress, Color.TRANSPARENT, color) as Int

    /**
     * 计算选择的位置.
     * @param y Float
     */
    private fun calculateSelectPosition(y: Float) {
        val indexList = (adapter as? IndexRecyclerViewAdapter<*>)?.getIndexList() ?: listOf()
        if (indexList.isNotEmpty() && y > indexBarDrawnRectF.top + vPadding && y < indexBarDrawnRectF.bottom - vPadding) {
            val selectPosition = ((y - indexBarDrawnRectF.top - vPadding) / ((indexBarDrawnRectF.height() - 2 * vPadding) / indexList.size)).toInt()
            this.selectedPosition = if (selectPosition >= indexList.size) indexList.size - 1 else selectPosition
            val scrollPosition = (adapter as IndexRecyclerViewAdapter<*>).getIndexPositionByAdapterPosition(this.selectedPosition)
            (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(scrollPosition, 0)
            postInvalidate()
        }
    }

    /**
     * 显示索引条.
     */
    private fun showIndexBar() {
        if (isWaitDismiss) {
            handler.removeCallbacks(dismissAction)
            isWaitDismiss = false
        }
        if (isAutoDismiss && !isIndexBarShowing) {
            isIndexBarShowing = true
            animator.cancel()
            animator.start()
        }
    }

    /**
     * 隐藏索引条.
     */
    private fun hideIndexBar() {
        if (isWaitDismiss) {
            handler.removeCallbacks(dismissAction)
            isWaitDismiss = false
        }
        if (isAutoDismiss && isIndexBarShowing) {
            isIndexBarShowing = false
            animator.cancel()
            animator.start()
        }
    }

    private fun sendDismissMessage() {
        if (!isWaitDismiss) {
            isWaitDismiss = true
            handler.postDelayed(dismissAction, autoDismissTime + 500L)
        }
    }

    override fun onDetachedFromWindow() {
        if (isWaitDismiss) {
            handler.removeCallbacks(dismissAction)
            isWaitDismiss = false
        }
        super.onDetachedFromWindow()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        isIndexAdapter = adapter is IndexRecyclerViewAdapter<*>
        if (isIndexAdapter) {
            if (headersDecoration != null) {
                removeItemDecoration(headersDecoration)
            }

            headersDecoration = StickyRecyclerHeadersDecoration(adapter as StickyRecyclerHeadersAdapter<*>)
            addItemDecoration(headersDecoration)
        }
        super.setAdapter(adapter)
    }

    override fun setLayoutManager(layout: LayoutManager?) {
    }

    /**
     * 绘制索引条文字.
     * @param cvs Canvas
     */
    private fun drawIndexBarText(cvs: Canvas) {
        val drawnHeight = indexBarDrawnRectF.height() - vPadding * 2
        val indexList = (adapter as IndexRecyclerViewAdapter<*>).getIndexList()
        val itemHeight = drawnHeight / indexList.size

        drawnPaint.textSize = indexTextSize.toFloat()

        val offsetTop = (itemHeight - (drawnPaint.descent() - drawnPaint.ascent())) / 2
        indexList.map { it.index[0].toString() }.forEachIndexed { index, indexStr ->
            drawnPaint.color = getAnimatorColor(if (index == selectedPosition) selectedIndexTextColor else indexTextColor)
            cvs.drawText(indexStr,
                    indexBarDrawnRectF.centerX() - drawnPaint.measureText(indexStr) / 2,
                    indexBarDrawnRectF.top + vPadding + itemHeight * index + offsetTop - drawnPaint.ascent(),
                    drawnPaint
            )
        }
    }

    /**
     * 绘制选中的索引.
     * @param cvs Canvas
     */
    private fun drawPreviewIndex(cvs: Canvas) {
        val indexList = (adapter as IndexRecyclerViewAdapter<*>).getIndexList()

        if (indexList.isNotEmpty()) {
            drawnPaint.color = previewIndexTextColor
            drawnPaint.textSize = previewIndexTextSize.toFloat()
            val indexStr = indexList[selectedPosition].index[0].toString()
            cvs.drawText(indexStr,
                    previewIndexDrawnRectF.centerX() - drawnPaint.measureText(indexStr) / 2,
                    previewIndexDrawnRectF.top + (previewIndexDrawnRectF.height() - drawnPaint.descent() + drawnPaint.ascent()) / 2 - drawnPaint.ascent(),
                    drawnPaint
            )
        }
    }

    companion object {
        private const val ACTION_DISMISS_MESSAGE = 0X01
    }
}