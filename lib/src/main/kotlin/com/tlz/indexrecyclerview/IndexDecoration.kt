package com.tlz.indexrecyclerview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Created by Tomlezen.
 * Data: 2018/7/9.
 * Time: 15:37.
 */
interface IndexDecoration {

    /**
     * 绘制索引条背景.
     * @param cvs Canvas
     * @param drawnPaint Paint
     * @param drawnRectF RectF
     * @param drawnColor Int
     */
    fun drawIndexBarBg(cvs: Canvas, drawnPaint: Paint, drawnRectF: RectF, drawnColor: Int)

    /**
     * 获取选中索引的背景.
     * @param cvs Canvas
     * @param drawnPaint Paint
     * @param drawnRectF RectF
     * @param drawnColor Int
     * @param radius Int
     */
    fun drawPreviewBg(cvs: Canvas, drawnPaint: Paint, drawnRectF: RectF, drawnColor: Int, radius: Float)

}