package jp.sourceforge.gokigen.memoma.drawers

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * 描画するクラス
 */
class GokigenSurfaceView : SurfaceView, SurfaceHolder.Callback
{
    private lateinit var canvasDrawer: ICanvasDrawer

    constructor(context: Context?) : super(context) {
        holder.addCallback(this)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        holder.addCallback(this)
    }

    /**
     * データ書き込みクラスの設定
     */
    fun setCanvasDrawer(drawer: ICanvasDrawer)
    {
        canvasDrawer = drawer
    }

    /**
     * サーフェイス生成イベント
     */
    override fun surfaceCreated(aHolder: SurfaceHolder)
    {
        try
        {
            if (::canvasDrawer.isInitialized)
            {
                canvasDrawer.prepareToStart(width, height)
            }
            doDraw()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * タッチイベント
     */
    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        var ret = false
        try
        {
            if (::canvasDrawer.isInitialized)
            {
                ret = canvasDrawer.onTouchEvent(event)
                if (ret)
                {
                    doDraw()
                }
            }
            else
            {
                super.performClick()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ret)
    }

    override fun performClick(): Boolean {
        return (super.performClick())
    }

    /**
     * サーフェイス変更イベントの処理
     */
    override fun surfaceChanged(aHolder: SurfaceHolder, format: Int, width: Int, height: Int)
    {
        try
        {
            if (::canvasDrawer.isInitialized)
            {
                canvasDrawer.changedScreenProperty(format, width, height)
            }
            doDraw()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * サーフェイス開放イベントの処理
     */
    override fun surfaceDestroyed(aHolder: SurfaceHolder)
    {
        //
    }

    /**
     * グラフィックを描画する
     */
    fun doDraw()
    {
        //Log.v(Main.APP_IDENTIFIER, "GokigenSurfaceView::doDraw()");
        val drawHolder = holder
        try
        {
            val canvas = drawHolder.lockCanvas()
            if (canvas == null)
            {
                // 描画領域が取れないから抜けてしまう
                Log.v(TAG, "GokigenSurfaceView::doDraw()  canvas is null.")
                return
            }
            canvas.save()
            //////////////////////////////////////////////
            if (::canvasDrawer.isInitialized) {
                canvasDrawer.drawOnCanvas(canvas)
            }
            /////////////////////////////////////////////
            canvas.restore()
            drawHolder.unlockCanvasAndPost(canvas)
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    companion object {
        private val TAG = GokigenSurfaceView::class.java.simpleName
    }
}
