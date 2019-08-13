package com.tlz.indexrecyclerview

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Tomlezen.
 * Data: 2018/7/5.
 * Time: 17:52.
 */
class IndexRecyclerView(ctx: Context, attrs: AttributeSet) : RecyclerView(ctx, attrs) {

  private val indexLayoutMode: Int

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

  /** 索引字体. */
  var indexBarTextTypeface: Typeface = Typeface.DEFAULT
    set(value) {
      field = value
      if (isIndexBarShowing) {
        postInvalidate()
      }
    }

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
    get() = if (isAutoDismiss) field else 1f

  private val argbEvaluator by lazy { ArgbEvaluator() }

  /** 索引装饰器，可进行索引条和预览框的自定义绘制. */
  var indexDecoration: IndexDecoration = DefIndexDecoration()

  init {
    val ta = resources.obtainAttributes(attrs, R.styleable.IndexRecyclerView)

    indexLayoutMode = ta.getInt(R.styleable.IndexRecyclerView_index_bar_layout_mode, LAYOUT_MODE_FILL)
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
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState != SCROLL_STATE_IDLE) {
          showIndexBar()
        } else {
          sendDismissMessage()
        }
      }

      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (isIndexAdapter && !isTouchedIndexBar) {
          val firstVisiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
          selectedPosition = (adapter as IndexRecyclerViewAdapter<*>).getIndexBarPositionByAdapterPosition(firstVisiblePosition)
        }
      }
    })

    super.setLayoutManager(LinearLayoutManager(context))
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)
    if (changed) {
      // 测量文字宽度.
//            drawnPaint.textSize = indexTextSize.toFloat()
//            val indexWidth = drawnPaint.measureText("宽")
//            indexBarDrawnRectF.set(
//                    r - paddingRight - hPadding * 2 - hMargin - indexWidth,
//                    (t + paddingTop + vMargin).toFloat(),
//                    (r - paddingRight - hMargin).toFloat(),
//                    (b - paddingBottom - vMargin).toFloat()
//            )

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
    if (isIndexAdapter && (isIndexBarShowing || animator.isRunning) && (adapter as IndexRecyclerViewAdapter<*>).getIndexList().isNotEmpty()) {
      c?.run {
        // 判断当前高度是否足够绘制所有索引
        drawnPaint.textSize = indexTextSize.toFloat()
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
      val scrollPosition = (adapter as IndexRecyclerViewAdapter<*>).getAdapterPositionByIndexBarPosition(this.selectedPosition)
      (layoutManager as LinearLayoutManager).scrollToPositionWithOffset(scrollPosition, 0)
      postInvalidate()
    }
  }

  /**
   * 显示索引条.
   */
  private fun showIndexBar() {
    if (isWaitDismiss) {
      handler?.removeCallbacks(dismissAction)
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
      handler?.removeCallbacks(dismissAction)
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
      handler?.postDelayed(dismissAction, autoDismissTime + 500L)
    }
  }

  override fun onDetachedFromWindow() {
    if (isWaitDismiss) {
      handler?.removeCallbacks(dismissAction)
      isWaitDismiss = false
    }
    super.onDetachedFromWindow()
  }

  override fun setAdapter(adapter: Adapter<*>?) {
    isIndexAdapter = adapter is IndexRecyclerViewAdapter<*>
    if (isIndexAdapter) {
      headersDecoration?.let {
        removeItemDecoration(it)
      }

      headersDecoration = StickyRecyclerHeadersDecoration(adapter as StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder>)
      headersDecoration?.let {
        addItemDecoration(it)
      }
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
    drawnPaint.textSize = indexTextSize.toFloat()
    drawnPaint.typeface = indexBarTextTypeface
    val indexWidth = drawnPaint.measureText("宽")
    val maxDrawnHeight = height - vPadding * 2 - vMargin * 2f
    var drawnHeight: Float
    val indexList = (adapter as IndexRecyclerViewAdapter<*>).getIndexList()
    var itemHeight = 0f
    when (indexLayoutMode) {
      LAYOUT_MODE_FILL -> {
        drawnHeight = maxDrawnHeight
        itemHeight = drawnHeight / indexList.size
        if (itemHeight < indexTextSize) {
          itemHeight = indexTextSize.toFloat()
        }
        indexBarDrawnRectF.set(
            right - paddingRight - hPadding * 2 - hMargin - indexWidth,
            (top + paddingTop + vMargin).toFloat(),
            (right - paddingRight - hMargin).toFloat(),
            (bottom - paddingBottom - vMargin).toFloat()
        )
      }
      LAYOUT_MODE_CENTER -> {
        itemHeight = indexTextSize * 2f
        drawnHeight = itemHeight * indexList.size
        // 如果超过最大高度 则重新制定item高度
        if (drawnHeight > maxDrawnHeight) {
          drawnHeight = maxDrawnHeight
          itemHeight = drawnHeight / indexList.size
        }
        indexBarDrawnRectF.set(
            width - paddingRight - hPadding * 2 - hMargin - indexWidth,
            height / 2 - drawnHeight / 2f - vPadding,
            (width - hMargin).toFloat(),
            height / 2 + drawnHeight / 2f + vPadding
        )
      }
      else -> {
        itemHeight = indexTextSize * 2f
        drawnHeight = itemHeight * indexList.size
        // 如果超过最大高度 则重新制定item高度
        if (drawnHeight > maxDrawnHeight) {
          drawnHeight = maxDrawnHeight
          itemHeight = drawnHeight / indexList.size
        }
        indexBarDrawnRectF.set(
            width - paddingRight - hPadding * 2 - hMargin - indexWidth,
            if (indexLayoutMode == LAYOUT_MODE_TOP) (paddingTop + vMargin.toFloat()) else (height - vMargin - paddingBottom - drawnHeight - vPadding * 2),
            (width - hMargin).toFloat(),
            if (indexLayoutMode == LAYOUT_MODE_BOTTOM) (height - vMargin - paddingBottom.toFloat()) else (paddingTop + vMargin + drawnHeight + vPadding * 2)
        )
      }
    }

    val offsetTop = (itemHeight - (drawnPaint.descent() - drawnPaint.ascent())) / 2
    indexList.map { it.index[0].toString() }.forEachIndexed { index, indexStr ->
      drawnPaint.color = getAnimatorColor(if (index == selectedPosition) selectedIndexTextColor else indexTextColor)
      val drawnY = indexBarDrawnRectF.top + vPadding + itemHeight * index + offsetTop - drawnPaint.ascent()
      if (drawnY < indexBarDrawnRectF.bottom - vPadding) {
        cvs.drawText(
            indexStr,
            indexBarDrawnRectF.centerX() - drawnPaint.measureText(indexStr) / 2,
            drawnY,
            drawnPaint
        )
      }
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
      drawnPaint.typeface = indexBarTextTypeface
      val indexStr = indexList[selectedPosition].index[0].toString()
      cvs.drawText(indexStr,
          previewIndexDrawnRectF.centerX() - drawnPaint.measureText(indexStr) / 2,
          previewIndexDrawnRectF.top + (previewIndexDrawnRectF.height() - drawnPaint.descent() + drawnPaint.ascent()) / 2 - drawnPaint.ascent(),
          drawnPaint
      )
    }
  }

  companion object {
    private const val LAYOUT_MODE_FILL = 0x01
    private const val LAYOUT_MODE_CENTER = 0x02
    private const val LAYOUT_MODE_TOP = 0x03
    private const val LAYOUT_MODE_BOTTOM = 0x04
  }
}