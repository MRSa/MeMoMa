package jp.sourceforge.gokigen.memoma.drawers;

import android.graphics.Canvas;
import android.view.MotionEvent;

public interface ICanvasDrawer
{	
    void prepareToStart(int width, int height);
    void changedScreenProperty(int format, int width, int height);
    void drawOnCanvas(Canvas canvas);
    boolean onTouchEvent(MotionEvent event);
}
