package jp.sourceforge.gokigen.memoma;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import jp.sourceforge.gokigen.memoma.drawers.ICanvasDrawer;

/**
 *  描画するくらす
 * 
 * @author MRSa
 *
 */
public class GokigenSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
	ICanvasDrawer canvasDrawer = null;
	
	/**
     *  コンストラクタ
     *
     */
	public GokigenSurfaceView(Context context)
    {
    	super(context);
    	initializeSelf();
    }

	/**
	 *  コンストラクタ
     *
	 */
	public GokigenSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initializeSelf();
	}

    /**
     *   クラスの初期化処理
     *
     */
    private void initializeSelf()
    {
    	SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    /**
     *  データ書き込みクラスの設定
     *
     *
     */
    public void setCanvasDrawer(ICanvasDrawer drawer)
    {
        canvasDrawer = drawer;
    }

    /**
     *   サーフェイス生成イベントの処理
     * 
     */
    public void surfaceCreated(SurfaceHolder aHolder)
    {
        try
        {
        	if (canvasDrawer != null)
        	{
        	    canvasDrawer.prepareToStart(getWidth(), getHeight());
        	}
        	doDraw();
        }
        catch (Exception ex)
        {
            //
        }
    }
    

    /**
     *   タッチイベント
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean ret = false;
    	if (canvasDrawer != null)
    	{
    	    ret = canvasDrawer.onTouchEvent(event);
    	    if (ret)
    	    {
    	    	doDraw();
    	    }
    	}
    	else
        {
            super.performClick();
        }
        return (ret);
    }

    @Override
    public boolean performClick()
    {
        return (super.performClick());
    }

    /**
     *  サーフェイス変更イベントの処理
     * 
     */
    public void surfaceChanged(SurfaceHolder aHolder, int format, int width, int height)
    {
        try
        {
        	if (canvasDrawer != null)
        	{
        	    canvasDrawer.changedScreenProperty(format, width, height);
        	}
        	doDraw();
        }
        catch (Exception ex)
        {
            //
            //
            //
        }
    }

    /**
     *  サーフェイス開放イベントの処理
     * 
     */
    public void surfaceDestroyed(SurfaceHolder aHolder)
    {
        //
        //
        //
    }

    /**
     *  グラフィックを描画する
     */
    public void doDraw()
    {
		//Log.v(Main.APP_IDENTIFIER, "GokigenSurfaceView::doDraw()");

		SurfaceHolder drawHolder = getHolder();
    	try
    	{
            Canvas canvas = drawHolder.lockCanvas();
    		if (canvas == null)
    		{
    			// 描画領域が取れないから抜けてしまう
        		Log.v(Main.APP_IDENTIFIER, "GokigenSurfaceView::doDraw()  canvas is null." );
    			return;
    		}
            canvas.save();
            //////////////////////////////////////////////
            if (canvasDrawer != null)
            {
            	canvasDrawer.drawOnCanvas(canvas);
            }
            /////////////////////////////////////////////
            canvas.restore();
            drawHolder.unlockCanvasAndPost(canvas);
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "ex.(doDraw())>" +  ex.toString() + " " + ex.getMessage());
    	}
    }

}
