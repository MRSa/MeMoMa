package jp.sourceforge.gokigen.memoma.drawers

import android.graphics.Canvas
import android.view.MotionEvent

interface ICanvasDrawer
{
    fun prepareToStart(width: Int, height: Int)
    fun changedScreenProperty(format: Int, width: Int, height: Int)
    fun drawOnCanvas(canvas: Canvas)
    fun onTouchEvent(event: MotionEvent): Boolean
}