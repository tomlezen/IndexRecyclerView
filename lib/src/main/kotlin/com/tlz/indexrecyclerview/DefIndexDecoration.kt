package com.tlz.indexrecyclerview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

/**
 * Created by Tomlezen.
 * Data: 2018/7/9.
 * Time: 15:38.
 */
open class DefIndexDecoration: IndexDecoration {

    override fun drawIndexBarBg(cvs: Canvas, drawnPaint: Paint, drawnRectF: RectF, drawnColor: Int) {
        drawnPaint.color = drawnColor
        drawnPaint.style = Paint.Style.FILL
        cvs.drawRoundRect(drawnRectF, drawnRectF.width() / 2, drawnRectF.width() / 2, drawnPaint)
    }

    override fun drawPreviewBg(cvs: Canvas, drawnPaint: Paint, drawnRectF: RectF, drawnColor: Int, radius: Float) {
        drawnPaint.style = Paint.Style.FILL
        drawnPaint.color = drawnColor
        cvs.drawRoundRect(drawnRectF, radius, radius, drawnPaint)
    }

}