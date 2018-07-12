package jp.sourceforge.gokigen.memoma.drawers;

import java.util.Enumeration;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import 	android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.widget.SeekBar;
import android.widget.TextView;

import jp.sourceforge.gokigen.memoma.GokigenSurfaceView;
import jp.sourceforge.gokigen.memoma.IObjectSelectionReceiver;
import jp.sourceforge.gokigen.memoma.fileio.ImageLoader;
import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.holders.LineStyleHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaConnectLineHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.OperationModeHolder;

/**
 *    メモまの描画クラス
 *    
 * @author MRSa
 *
 */
public class MeMoMaCanvasDrawer implements ICanvasDrawer,  GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener, SeekBar.OnSeekBarChangeListener
{
	public static final float OBJECTLABEL_MARGIN = 8.0f;
	public static final float OBJECTLABEL_MARGIN_WIDTH = 24.0f;

	public static final int BACKGROUND_COLOR_DEFAULT = 0xff004000;
	private int backgroundColor = BACKGROUND_COLOR_DEFAULT;
	
	private MeMoMaObjectHolder.PositionObject selectedPosition = null;
	private float tempPosX = Float.MIN_VALUE;
	private float tempPosY = Float.MIN_VALUE;
	private float downPosX = Float.MIN_VALUE;
	private float downPosY = Float.MIN_VALUE;
	
	// 以下の値は、MeMoMaListenerで初期値を設定する
	private int objectStyle = MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE;
	
	private LineStyleHolder lineStyleHolder = null;

	private float drawScale = 1.0f;    // 表示の倍率
	private float drawTransX  = 0.0f;   // 並行移動距離 (X)
	private float drawTransY  = 0.0f;   // 並行移動距離 (Y)
	private boolean onScaling = false;  // ピンチイン・ピンチアウト操作しているかどうかを示す
	private int currentScaleBar= 50;  // 現在のピンチイン・ピンチアウト倍率
	
	private boolean onGestureProcessed = false;   // 長押し時の処理を行なっているかどうかを示す。
	
	private float screenWidth = 0.0f;  // 表示領域の幅
	private float screenHeight = 0.0f; // 表示領域の高さ

	private int displayObjectInformation = 1;  // オブジェクトラベルの表示
	
	private String backgroundBitmapUri = null;
	private Bitmap backgroundBitmap = null;

	private MeMoMaObjectHolder objectHolder = null;
	private MeMoMaConnectLineHolder lineHolder = null;
	private IObjectSelectionReceiver selectionReceiver = null;

	private GestureDetector gestureDetector = null;
	private ScaleGestureDetector scaleGestureDetector = null;

	private Activity parent = null;
	
	/**
      *   コンストラクタ
      *   
      */
	  public MeMoMaCanvasDrawer(Activity argument, MeMoMaObjectHolder object, LineStyleHolder styleHolder, IObjectSelectionReceiver receiver)
	  {
		  objectHolder = object;
		  lineHolder = objectHolder.getConnectLineHolder();
		  selectionReceiver = receiver;
		  lineStyleHolder = styleHolder;
		  parent = argument;

		  // ジェスチャを検出するクラスを生成する
		  gestureDetector = new GestureDetector(argument, this);
		  scaleGestureDetector = new ScaleGestureDetector(argument, this);

		  // ズーム倍率を展開する
		  restoreTranslateAndZoomScale();
	  }

	  /**
	   *   オブジェクトの形状を変更する
	   *   (ここで指定された形状のチェックを行っておく。)
	   * 
	   * @param style
	   */
	  public void setObjectStyle(int style)
	  {
		  switch (style)
		  {
		    case MeMoMaObjectHolder.DRAWSTYLE_OVAL:
		    case MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT:
		    case MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE:
		    case MeMoMaObjectHolder.DRAWSTYLE_DIAMOND:
		    case MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD:
		    case MeMoMaObjectHolder.DRAWSTYLE_PAPER:
		    case MeMoMaObjectHolder.DRAWSTYLE_DRUM:
		    case MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM:
		    case MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL:
		    case MeMoMaObjectHolder.DRAWSTYLE_CIRCLE:
		    case MeMoMaObjectHolder.DRAWSTYLE_NO_REGION:
		    case MeMoMaObjectHolder.DRAWSTYLE_LOOP_START:
		    case MeMoMaObjectHolder.DRAWSTYLE_LOOP_END:
		    case MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW:
		    case MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW:
		    case MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW:
		    case MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW:
		    	objectStyle = style;
			    break;

		    default:
		    	objectStyle = MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE;
			    break;
		  }	
	  }

	  /**
	   * 
	   * @param uri
	   */
	  public void updateBackgroundBitmap(String uri, int width, int height)
	  {
		  // 背景画像の文字列を記憶する
		  backgroundBitmapUri = uri;

		  // とりあえず、背景画像をクリアしてガベコレする。
		  backgroundBitmap = null;
		  System.gc();
		  if (uri.isEmpty() == true)
		  {
			  // 背景画像の指定がなかったので、ここでリターンする。
			  return;
		  }
		  try
		  {
			  // とりあえず設定する情報をログに残してみる
			  Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::updateBackgroundBitmap() : w:" + width + " , h:"+ height + " " + uri);

			  // 背景画像を取得して設定する。
			  backgroundBitmap = ImageLoader.getBitmapFromUri(parent, ImageLoader.parseUri(uri), width, height);
		  }
		  catch (Exception ex)
		  {
			  Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::updateBackgroundBitmap() : " + uri + " , "+ ex.toString());
			  ex.printStackTrace();
			  backgroundBitmap = null;
			  backgroundBitmapUri = "";
			  System.gc();
		  }	
		  return;
	  }	  
	  
	  /**
	   *   背景画像を設定する
	   *   
	   * @param uri
	   */
	  public void setBackgroundUri(String uri)
	  {
		  backgroundBitmapUri = uri;
	  }
	  
	  /**
	   *   背景色を(文字列で)設定する
	   * 
	   * @param colorString
	   */
	  public void setBackgroundColor(String colorString)
	  {
		  try
		  {
			  backgroundColor = Color.parseColor(colorString);
			  return;
		  }
		  catch (Exception ex)
		  {
			  //
              //Log.v(Main.APP_IDENTIFIER, "Ex:" + ex.toString() + " " + ex.getMessage());
		  }
		  backgroundColor = BACKGROUND_COLOR_DEFAULT;
	  }

	  /**
	   *    初期化ロジックを設定する
	   * 
	   */
	  public void prepareToStart(int width, int height)
	  {
          Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::prepareToStart() " + "x:" + width + " , " + "y:" + height);

          // 背景画像を更新する
		  //updateBackgroundBitmap(backgroundBitmapUri, width, height);

		  // Preferenceを読み出す
		  SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
		  displayObjectInformation = Integer.parseInt(preferences.getString("objectPrintData", "1"));
	  }	

	  /**
	   *    画面の大きさが変わってしまった場合...
	   * 
	   */
	  public  void changedScreenProperty(int format, int width, int height)
	  {
		  // 背景画像を更新する
		  updateBackgroundBitmap(backgroundBitmapUri, width, height);
		  
		  // 表示画面サイズを覚える
		  screenWidth = width;
		  screenHeight = height;

          Log.v(Main.APP_IDENTIFIER, "changedScreenProperty() " + "x:" + width + " , " + "y:" + height);
	  }


	  /**
	   *    キャンバスにオブジェクト（と接続線）を表示する
	   * 
	   */
	  public void drawOnCanvas(Canvas canvas)
	  {
	    	//Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::drawOnCanvas()");
	    	try
	    	{
	    		// 画面全体をクリアする
	    		//canvas.drawColor(Color.argb(backgroundColorAlfa, backgroundColorRed, backgroundColorGreen, backgroundColorBlue), Mode.CLEAR);
	    		canvas.drawColor(backgroundColor);	    			
	    		
	    		// 背景画像が設定されていた場合は、背景画像を描画する
	    		if (backgroundBitmap != null)
	    		{
	    		    canvas.drawBitmap(backgroundBitmap, 0, 0, new Paint());
	    		}

	    		// 表示位置を移動させる
	    		canvas.translate(drawTransX, drawTransY);

	    		// 画面の表示領域を拡大・縮小する
	    		canvas.scale(drawScale, drawScale);

	    		// オブジェクト間の接続線をすべて表示する
	    		drawConnectionLines(canvas, 0.0f, 0.0f);
	    		
                // オブジェクトをすべて表示
	    		drawObjects(canvas, 0.0f, 0.0f);

                /**  移動中かどうかのチェックを行う。 **/
	    		if (isFlicking(canvas) == true)
	    		{
                    // 移動中の場合、フリック時の軌跡と現在位置を表示する
		            drawTrackAndPositions(canvas);
	    		}
	    	}
	    	catch (Exception ex)
	    	{
	    		// 例外発生...でもそのときには何もしない
	    		Log.v(Main.APP_IDENTIFIER, "drawOnCanvas() ex: " + ex.getMessage());
	    	}
	  }

	  /**
	   *    オブジェクトをBitmapCanvas上に描く
	   * 
	   */
	  public void drawOnBitmapCanvas(Canvas canvas, float offsetX, float offsetY)
	  {
	    	try
	    	{
	    		Paint paint = new Paint();

	    		// 画面全体をクリアする
	    		canvas.drawColor(backgroundColor);	    			
	    		
	    		// 背景画像が設定されていた場合は、背景画像を描画する
	    		if (backgroundBitmap != null)
	    		{
	    		    canvas.drawBitmap(backgroundBitmap, offsetX, offsetY, paint);
	    		}

	    		// オブジェクト間の接続線をすべて表示する
	    		drawConnectionLines(canvas, offsetX, offsetY);
	    		
                // オブジェクトをすべて表示
	    		drawObjects(canvas, offsetX, offsetY);
	    		
	    		// タイトルとめもまのアイコンを表示する : 文字の色は黒でいいのかな...
	    		Bitmap bitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.icon1);
	    		canvas.drawBitmap(bitmap, 2.0f, 2.0f, paint);

                // 文字をライトグレー、ちょっと影付きにする
                paint.setColor(Color.LTGRAY);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setShadowLayer(0.5f, 0.5f, 0.5f, Color.DKGRAY);

	    		canvas.drawText(objectHolder.getDataTitle(), (bitmap.getWidth() + 10.0f), 32.0f, paint);

	    	}
	    	catch (Exception ex)
	    	{
	    		// 例外発生...でもそのときには何もしない
	    		Log.v(Main.APP_IDENTIFIER, "drawOnBitmapCanvas() ex: " + ex.toString() + " " + ex.getMessage());
	    	}
	  }

	  /**
	   *    オブジェクト間の接続線を表示する
	   * 
	   * @param canvas
	   */
	  private void drawConnectionLines(Canvas canvas, float offsetX, float offsetY)
	  {
	        // オブジェクトの色と枠線を設定する （連続線用）
	    	Paint paint = new Paint();
	        paint.setColor(Color.WHITE);
	    	paint.setStyle(Paint.Style.STROKE);

	        // オブジェクトの色と枠線を設定する  （点線用）
	    	Paint dashLinePaint = new Paint();
	    	dashLinePaint.setColor(Color.WHITE);
	    	dashLinePaint.setStyle(Paint.Style.STROKE);
	    	dashLinePaint.setPathEffect(new DashPathEffect(new float[]{ 5.0f, 5.0f }, 0));	    	
	    	
	    	// オブジェクトの描画 （保持しているものはすべて表示する）
	    	Enumeration<Integer> keys = lineHolder.getLineKeys();
	        while (keys.hasMoreElements())
	        {
	            Integer key = keys.nextElement();
	            MeMoMaConnectLineHolder.ObjectConnector line = lineHolder.getLine(key);
	            if (line.key > 0)
	            {
                    // 実際にラインを引く
	            	drawLine(canvas, paint, dashLinePaint, line, offsetX, offsetY);
	            }
	            else
	            {
	            	// ここは呼ばれないはず。。。消したはずのものが残っている
	            	Log.v(Main.APP_IDENTIFIER, "DETECTED DELETED LINE");
	            }
	        }
	  }

	  /** 
	   *    接続する線を引く
       *
	   * @param canvas
	   * @param paint
	   * @param line
	   */
	  public void drawLine(Canvas canvas, Paint paint, Paint dashPaint, MeMoMaConnectLineHolder.ObjectConnector line, float offsetX, float offsetY)
	  {
		  try
		  {
			  if ((objectHolder == null)||(canvas == null))
			  {
				  // なにもしない
				  return;
			  }

			  MeMoMaObjectHolder.PositionObject from = objectHolder.getPosition(line.fromObjectKey);
			  MeMoMaObjectHolder.PositionObject to = objectHolder.getPosition(line.toObjectKey);
			  if ((from == null)||(to == null))
			  {
				  // なにもしない
				  return;
			  }

			  // ラインの太さを設定する。
			  paint.setStrokeWidth((float) line.lineThickness);

			  // ラインの太さを設定する。
			  dashPaint.setStrokeWidth((float) line.lineThickness);

			  // ラインのスタイル(連続線 or 点線)を設定する
			  Paint linePaint = (line.lineShape == LineStyleHolder.LINESHAPE_DASH) ? dashPaint : paint;
			  
			  // 初期値として、各オブジェクトの中心座標を設定する
			  float startX = from.rect.centerX() + offsetX;
			  float endX = to.rect.centerX() + offsetX;
			  float startY = from.rect.centerY() + offsetY;
			  float endY = to.rect.centerY() + offsetY;
			  
			  // Y座標の線の位置を補正する
			  if (from.rect.bottom < to.rect.top)
			  {
                  startY = from.rect.bottom + offsetY;
                  endY = to.rect.top + offsetY;
			  }
			  else if (from.rect.top > to.rect.bottom)
			  {
                  startY = from.rect.top + offsetY;
                  endY = to.rect.bottom + offsetY;
			  }

			  // X座標の線の位置を補正する (Y座標が補正されていないとき)
              if ((startY != (from.rect.top + offsetY))&&(startY != (from.rect.bottom + offsetY)))
			  {
    			  if (from.rect.right < to.rect.left)
    			  {
                      startX = from.rect.right + offsetX;
                      endX = to.rect.left + offsetX;
    			  }
    			  else if (from.rect.left > to.rect.right)
    			  {
                      startX = from.rect.left + offsetX;
                      endX = to.rect.right + offsetX; 
    			  }
			  }

			  if ((line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW)||
					  (line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW)||
					  (line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW))
			  {
				  // ツリー形式のように接続する ... 
				  if (startX == (from.rect.centerX() + offsetX))
				  {
					  float middleY = (startY + endY) / 2;
				      canvas.drawLine(startX, startY, startX, middleY, linePaint);
				      canvas.drawLine(startX, middleY, endX, middleY, linePaint);
				      canvas.drawLine(endX, middleY, endX, endY, linePaint);
				      
				      /**  やじるしをつける処理 **/
				      if (line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW)
				      {
				    	  // 始点に矢印をつける
				    	  ObjectShapeDrawer.drawArrowTree(canvas, paint, startX,startY, middleY, false);
				      }
				      else if (line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW)
				      {
				    	  // 終点に矢印をつける
				    	  ObjectShapeDrawer.drawArrowTree(canvas, paint, endX, endY, middleY, false);
				      }
				  }
				  else
				  {
					  float middleX = (startX + endX) / 2;
				      canvas.drawLine(startX, startY, middleX, startY, linePaint);
				      canvas.drawLine(middleX, startY, middleX, endY, linePaint);
				      canvas.drawLine(middleX, endY, endX, endY, linePaint);

				      /**  やじるし(三角形)をつける処理 **/
				      if (line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW)
				      {
				    	  // 始点に矢印をつける
				    	  ObjectShapeDrawer.drawArrowTree(canvas, paint, startX, startY, middleX, true);
				      }
				      else if (line.lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW)
				      {
				    	  // 終点に矢印をつける
				    	  ObjectShapeDrawer.drawArrowTree(canvas, paint, endX,endY, middleX, true);
				      }
				  }
			  }
			  else if ((line.lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW)||
					  (line.lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_L_ARROW)||
					  (line.lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW))
			  {
                  // 曲線で接続する
				  float middleX = (startX + endX) / 2;
				  float middleY = (startY + endY) / 2;
				  float x1 = (startX + middleX) / 2;
				  float y1 = middleY;
				  float x2 = (middleX + endX) / 2;
				  float y2 = middleY;
				  
			      Path pathLine = new Path();
			      pathLine.moveTo(startX, startY);
			      pathLine.cubicTo(x1, y1, x2, y2, endX, endY);
			      canvas.drawPath(pathLine, linePaint);

			      /**  やじるしをつける処理 **/
			      if (line.lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_L_ARROW)
			      {
			    	  // 始点に矢印をつける
			    	  ObjectShapeDrawer.drawArrow(canvas, paint, startX, startY, endX, endY);
			      }
			      else if (line.lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW)
			      {
			    	  // 終点に矢印をつける
			    	  ObjectShapeDrawer.drawArrow(canvas, paint, endX, endY, startX, startY);
			      }
			  }
              else  // if (line.lineStyle == MeMoMaConnectLineHolder.LINESTYLE_STRAIGHT)
			  {
			      // 直線で接続する
			      canvas.drawLine(startX, startY, endX, endY, linePaint);
			      
			      /**  やじるしをつける処理 **/
			      if (line.lineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_L_ARROW)
			      {
			    	  // 始点に矢印をつける
			    	  ObjectShapeDrawer.drawArrow(canvas, paint, startX, startY, endX, endY);
			      }
			      else if (line.lineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW)
			      {
			    	  // 終点に矢印をつける
			    	  ObjectShapeDrawer.drawArrow(canvas, paint, endX, endY, startX, startY);
			      }
			  }
		  }
		  catch (Exception ex)
		  {
			  // なにもしない
			  Log.v(Main.APP_IDENTIFIER, "EXCEPTION :" + ex.toString());
		  }
	  }	  
	  
    /**
     *   オブジェクトを動かしている最中かどうかの判定を行う。
     * 
     * @param canvas
     * @return  trueなら、動かしている最中
     */
    private boolean isFlicking(Canvas canvas)
    {
        //int width = canvas.getWidth();
        //int height = canvas.getHeight();
        if ((tempPosX == Float.MIN_VALUE)||(tempPosY == Float.MIN_VALUE))
        {
            return (false);
        }
        return (true);
    }

    /**
     *   フリック時の軌跡と現在地点を表示する
     * 
     * @param canvas
     */
    private void drawTrackAndPositions(Canvas canvas)
    {
        // フリック時の軌跡を表示する
    	float x = (tempPosX - drawTransX) / drawScale;
    	float y = (tempPosY - drawTransY) / drawScale;

    	Paint paint = new Paint();
    	paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GRAY);
        if (selectedPosition != null)
        {
        	float objX = (selectedPosition.rect.right - selectedPosition.rect.left) / 2;
        	float objY = (selectedPosition.rect.bottom - selectedPosition.rect.top) / 2;
    	    canvas.drawLine(selectedPosition.rect.centerX(), selectedPosition.rect.centerY(), x, y, paint);
            canvas.drawRect((x - objX), (y - objY), (x + objX), (y + objY), paint);

            // 現在地点の表示
            drawObject(canvas, selectedPosition, true, 0.0f, 0.0f);
        }
        else   // オブジェクト非選択時の表示
        {
    		int data = selectionReceiver.touchedVacantArea();
    		if (data == OperationModeHolder.OPERATIONMODE_MOVE)
    		{
                // 移動モードのとき... （表示領域を移動させる）
    			drawTransX = (tempPosX - downPosX);
    			drawTransY = (tempPosY - downPosY);

    			//  表示領域の移動を記憶する
    	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
    	        SharedPreferences.Editor editor = preferences.edit();
    	        editor.putFloat("drawTransX", drawTransX);
    	        editor.putFloat("drawTransY", drawTransY);
    	        editor.commit();    		
    		}
    		else
    		{
    			// 移動モード以外
            	paint.setColor(Color.YELLOW);
        	    canvas.drawLine(((downPosX) / drawScale), ((downPosY) / drawScale), x,  y, paint);
    		}
        }
    }

    /**
     *    オブジェクト（１個）を表示する
     *
     */
    private void drawObject(Canvas canvas, MeMoMaObjectHolder.PositionObject object, boolean isMoving, float offsetX, float offsetY)
    {
    	float label_offsetX = OBJECTLABEL_MARGIN;
    	float label_offsetY = 0.0f;

        // オブジェクトの色と枠線を設定する
    	Paint paint = new Paint();
    	if (isMoving == true)
    	{
            paint.setColor(Color.YELLOW);
        	paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(object.strokeWidth);
    	}
    	else
    	{
            paint.setColor(object.objectColor);
            paint.setStyle(Paint.Style.valueOf(object.paintStyle));
            paint.setStrokeWidth(object.strokeWidth);
    	}
 
       // 図形の形状に合わせて描画する
    	RectF objectShape = new RectF(object.rect);
    	objectShape.left = objectShape.left + offsetX;
    	objectShape.right = objectShape.right + offsetX;
    	objectShape.top = objectShape.top + offsetY;
    	objectShape.bottom = objectShape.bottom + offsetY;
    	
    	if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_OVAL)
		{
			// 楕円形の描画
    		label_offsetY = ObjectShapeDrawer.drawObjectOval(canvas, objectShape, paint);
		}
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT)
		{
			// 丸角四角形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectRoundRect(canvas, objectShape, paint);
		}
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DIAMOND)
		{
			// 菱形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectDiamond(canvas, objectShape, paint);
            label_offsetX = OBJECTLABEL_MARGIN;
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD)
		{
			// 台形(キーボード型)の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectKeyboard(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM)
		{
			// 平行四辺形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectParallelogram(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL)
		{
			// 六角形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectHexagonal(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PAPER)
		{
			// 書類の形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectPaper(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DRUM)
		{
			// 円柱の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectDrum(canvas, objectShape, paint, Paint.Style.valueOf(object.paintStyle));
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE)
		{
			// 円を描画する
	   		label_offsetY = ObjectShapeDrawer.drawObjectCircle(canvas, objectShape, paint);
		}
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_NO_REGION)
		{
			  // 枠なしを描画（？）する ... なにもしない
			  if (object.label.length() == 0)
			  {
				  // 何も表示しないとわからないので、ラベルが無いときには枠を表示する
				  ObjectShapeDrawer.drawObjectNoRegion(canvas, objectShape, paint);
			  }
		}
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_START)
		{
			// ループ開始図形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectLoopStart(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_END)
		{
			// ループ終了図形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectLoopEnd(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW)
		{
			// 左側矢印図形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectLeftArrow(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW)
		{
			// 下側矢印図形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectDownArrow(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW)
		{
			// 上側矢印図形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectUpArrow(canvas, objectShape, paint);
        }
		else if (object.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW)
		{
			// 右側矢印図形の描画
	   		label_offsetY = ObjectShapeDrawer.drawObjectRightArrow(canvas, objectShape, paint);
        }
		else // if (pos.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE)
        {
            // 四角形を描画する
            label_offsetY = ObjectShapeDrawer.drawObjectRect(canvas, objectShape, paint);
        }

        // 文字サイズを設定する。
        paint.setTextSize(object.fontSize);

        // 文字ラベルを表示する
       	ObjectShapeDrawer.drawTextLabel(canvas, paint, object, objectShape, displayObjectInformation, label_offsetX, label_offsetY);
    }

    /**
     *   オブジェクトをすべて表示する
     *
     * @param canvas
     */
    private void drawObjects(Canvas canvas , float offsetX, float offsetY)
    {
    	// オブジェクトの描画 （保持しているものはすべて表示する）
    	Enumeration<Integer> keys = objectHolder.getObjectKeys();
        while (keys.hasMoreElements())
        {
            Integer key = keys.nextElement();
            MeMoMaObjectHolder.PositionObject pos = objectHolder.getPosition(key);
            drawObject(canvas, pos, false, offsetX, offsetY);
        }
    }

    /**
     *   タッチされたタイミングでの処理
     * @param event
     * @return
     */
    private boolean onTouchDown(MotionEvent event)
    {
    	// タッチ位置を記憶する
    	downPosX = event.getX() - drawTransX;
    	downPosY = event.getY() - drawTransY;

    	// タッチ位置をオブジェクト画像の座標に変換する
    	float x = downPosX / drawScale;
    	float y = downPosY / drawScale;

    	// タッチ位置にオブジェクトが存在するか確認する
    	selectedPosition = checkSelectedObject(x, y);
    	if (selectedPosition == null)
    	{
    		// 最初にタップしたときの位置を selectedPositionに設定する
    		int data = selectionReceiver.touchedVacantArea();
    		if (data == OperationModeHolder.OPERATIONMODE_CREATE)
    		{
    			// オブジェクト作成モードのとき...オブジェクトを生成する
        		selectedPosition = objectHolder.createPosition(x, y, objectStyle);
        		
        		// オブジェクトが生成されたことを通知する
        		selectionReceiver.objectCreated();        		
    		}
    		else if (data ==OperationModeHolder.OPERATIONMODE_MOVE)
    		{
    			// 移動モードのとき
    		}
	        else // if (data ==ChangeDrawMode.OPERATIONMODE_DELETE)
	        {
	        	// 削除モードのとき...何もしない
	        }
    	}
        return (false);
    }

    /**
     *   タッチが離されたタイミングでの処理
     * @param event
     * @return
     */
    private boolean onTouchUp(MotionEvent event)
    {
    	boolean longPress = false;
        if (onGestureProcessed == true)
        {
        	// ロングタッチ中だった場合...フラグを落とす
        	onGestureProcessed = false;
        	longPress = true;
        }

        // タッチ位置をオブジェクト画像の座標に変換する
    	float x = (event.getX() - drawTransX) / drawScale;
    	float y = (event.getY() - drawTransY) / drawScale;

    	if (selectedPosition == null)
        {
        	int data = selectionReceiver.touchUppedVacantArea();
        	if (data == OperationModeHolder.OPERATIONMODE_DELETE)
        	{
                if ((tempPosX == Float.MIN_VALUE)||(tempPosY == Float.MIN_VALUE)||(downPosX == Float.MIN_VALUE)||(downPosY == Float.MIN_VALUE))
                {
                	// タッチが２つ揃っていないので、何もしない。
            		Log.v(Main.APP_IDENTIFIER, "onTouchUp : (" + downPosX + "," + downPosY + ") [" + drawScale + "] (" + tempPosX + "," + tempPosY + ") [" + drawTransX + "," + drawTransY + "]");
                    return (false);	
                }

        		// タッチが離された位置にオブジェクトがおらず、オブジェクトが非選択だった場合...オブジェクトが繋がっているラインを切断する
                disconnectObjects((downPosX / drawScale) , (downPosY / drawScale), ((tempPosX - drawTransX) / drawScale), ((tempPosY - drawTransY) / drawScale));
                
    			// 移動位置をクリアする
    			tempPosX = Float.MIN_VALUE;
	        	tempPosY = Float.MIN_VALUE;
	        	downPosX = Float.MIN_VALUE;
	        	downPosY = Float.MIN_VALUE;
                return (true);
        	}

        	// 移動位置をクリアする
			tempPosX = Float.MIN_VALUE;
        	tempPosY = Float.MIN_VALUE;
        	downPosX = Float.MIN_VALUE;
        	downPosY = Float.MIN_VALUE;
        	return (true);
        }

        if (selectedPosition.rect.contains(x, y) == true)
    	{
        	//  タッチが離された位置がタッチしたオブジェクトと同じ位置だった場合......

        	// タッチが離された位置を認識する
        	float diffX = Math.abs(event.getX() - drawTransX - downPosX);
        	float diffY = Math.abs(event.getY() - drawTransY - downPosY);    	

    		// タッチが離された位置が動いていた場合、オブジェクト位置の微調整と判定する。
    		if (((diffX > 2.0f)||(diffY > 2.0f))||(longPress == true))
    		{
    	        // タッチが離された場所にはオブジェクトがなかった場合...オブジェクトをその位置に移動させる
    	    	Log.v(Main.APP_IDENTIFIER, "MOVE OBJECT : (" + diffX + "," + diffY + ")");
    			moveObjectPosition(x, y);
    			return (true);
    		}
    		
            //  タッチが押された位置と離された位置が同じ位置だった場合......アイテムが選択された、と認識する。        	
    		Log.v(Main.APP_IDENTIFIER, " ITEM SELECTED :" + x + "," + y);
    		if (selectionReceiver != null)
    		{
    			// アイテムが選択されたよ！と教える
    			boolean isDraw = selectionReceiver.objectSelected(selectedPosition.getKey());

    			// 移動位置をクリアする
    			tempPosX = Float.MIN_VALUE;
	        	tempPosY = Float.MIN_VALUE;
        		return (isDraw);
    		}
    	}

    	// タッチが離された位置にオブジェクトがいるかどうかのチェック
    	MeMoMaObjectHolder.PositionObject position = checkSelectedObject(x, y);
        if ((position != null)&&(longPress == false))
        {
        	// 他のオブジェクトと重なるように操作した、この場合は、オブジェクト間を線をつなげる
        	// （ただし、ボタンを長押ししていなかったとき。）
        	lineHolder.setLines(selectedPosition.getKey(), position.getKey(), lineStyleHolder);
        	tempPosX = Float.MIN_VALUE;
        	tempPosY = Float.MIN_VALUE;
        	return (true);
        }
        
        // タッチが離された場所にはオブジェクトがなかった場合...オブジェクトをその位置に移動させる
        moveObjectPosition(x, y);
/*
        tempPosX = Float.MIN_VALUE;
    	tempPosY = Float.MIN_VALUE;
    	float positionX = alignPosition(x, (objectSizeX / 2) * (-1));
    	float positionY = alignPosition(y, (objectSizeY / 2) * (-1));
    	selectedPosition.rect = new  android.graphics.RectF(positionX, positionY, (positionX + objectSizeX), (positionY + objectSizeY));
    	// selectedPosition.drawStyle = objectStyle;   // 不要、最初に生成するときだけ必要
*/
    	return (true);
    }
    
    /**
     *   オブジェクトの位置を移動させる
     * 
     * @param x
     * @param y
     */
    private void moveObjectPosition(float x, float y)
    {
        tempPosX = Float.MIN_VALUE;
    	tempPosY = Float.MIN_VALUE;
    	float sizeX = selectedPosition.rect.right - selectedPosition.rect.left;
    	float sizeY = selectedPosition.rect.bottom - selectedPosition.rect.top;
    	
    	float positionX = alignPosition(x, (sizeX / 2) * (-1));
    	float positionY = alignPosition(y, (sizeY / 2) * (-1));
    	selectedPosition.rect = new  android.graphics.RectF(positionX, positionY, (positionX + sizeX), (positionY + sizeY));
    	
    	return;
    }
    
	  /**
	   *    onTouchEvent : 画面がタッチした時のイベント処理
	   *    (true なら、画面描画を実施する)
	   */
	  public boolean onTouchEvent(MotionEvent event)
	  {
            boolean isDraw = false;

            /** スケールジェスチャ(マルチタッチのジェスチャ)を拾う **/
            isDraw = scaleGestureDetector.onTouchEvent(event);
        	if ((onScaling == true)||(scaleGestureDetector.isInProgress() == true))
        	{
        		//  マルチタッチ操作中...
        		return (true);
        	}
        	
	        /**  先にジェスチャーを拾ってみよう...   **/
            isDraw = gestureDetector.onTouchEvent(event);
            if (isDraw == true)
            {
            	Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onTouchEvent() : isDraw == true");
            	return (isDraw);
            }

	    	int action = event.getAction();
	    	
	    	//Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onTouchEvent() : " + action);
	        if (action == MotionEvent.ACTION_DOWN)
	        {
	        	// タッチされたとき
	        	isDraw = onTouchDown(event);
	        }
	        else if (action == MotionEvent.ACTION_MOVE)
	        {
	        	// タッチされたまま動かされたときの処理
	        	tempPosX = event.getX();
                tempPosY = event.getY();
                isDraw = true;
	        }
	        else if (action == MotionEvent.ACTION_UP)
	        {
	        	// タッチが離されたときの処理...
	            isDraw = onTouchUp(event);
	        }

	        return (isDraw);
	  }

	  /**
	   *   タテヨコ位置を合わせられるよう、調整する。
	   * @param pos
	   * @return
	   */
	  private float alignPosition(float pos, float offset)
	  {
		  // 位置を調整する。
		  return (pos + offset);
	  }	

	  /**
	   *    位置から判定し、選択したオブジェクトを応答する
	   *    （オブジェクトが選択されていない場合には、nullを応答する）
	   * @param x
	   * @param y
	   * @return
	   */
	  private MeMoMaObjectHolder.PositionObject checkSelectedObject(float x, float y)
	  {
          Enumeration<Integer> keys = objectHolder.getObjectKeys();
	      //Log.v(Main.APP_IDENTIFIER, "CHECK POS "  + x + "," + y);
		  while (keys.hasMoreElements())
		  {
			  Integer key = keys.nextElement();
			  MeMoMaObjectHolder.PositionObject pos = objectHolder.getPosition(key);
			  if (pos.rect.contains(x, y) == true)
			  {
				      Log.v(Main.APP_IDENTIFIER, "SELECTED :"  + pos.rect.centerX() + "," + pos.rect.centerY() +  " KEY :" + key);
				      return (pos);
			  }
			  //Log.v(Main.APP_IDENTIFIER, "NOT MATCH :"   + pos.rect.centerX() + "," + pos.rect.centerY());
		  }
		  //Log.v(Main.APP_IDENTIFIER, "RETURN NULL...");
		  return (null);
	  }	

	  /**
	   *   線と交差するオブジェクト接続線をすべて削除する
	   * 
	   * @param startX
	   * @param startY
	   * @param endX
	   * @param endY
	   */
      private void disconnectObjects(float startX, float startY, float endX, float endY)
      {
    	    Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::disconnectObjects() [" + startX + "," + startY +"]-[" + endX + "," + endY + "]");
		    try
		    {
		    	Enumeration<Integer> keys = lineHolder.getLineKeys();
		        while (keys.hasMoreElements())
		        {
		            Integer key = keys.nextElement();
		            MeMoMaConnectLineHolder.ObjectConnector line = lineHolder.getLine(key);
		            if (line.key > 0)
		            {
		            	    // 線の始点と終点を取り出す
		    			    MeMoMaObjectHolder.PositionObject from = objectHolder.getPosition(line.fromObjectKey);
		    			    MeMoMaObjectHolder.PositionObject to = objectHolder.getPosition(line.toObjectKey);

		    			    // 線が交差しているかチェックする
		    			    if (checkIntersection(startX, startY, endX, endY, from.rect.centerX(),  from.rect.centerY(),  to.rect.centerX(), to.rect.centerY()) == true)		    			    
		    			    {
                                // 線が交差していた！ 線を切る！
		    			    	//Log.v(Main.APP_IDENTIFIER, "CUT LINE [" +  from.rect.centerX() + "," +  from.rect.centerY() +"]-[" + to.rect.centerX() + "," + to.rect.centerY() + "]");
			    			    lineHolder.disconnectLines(line.key);
		    			    }		    			    
		    		 }
		        }
		    }
		    catch (Exception ex)
		    {
		    	// 例外発生、でもなにもしない
		    }
      }
      
      /**
       *    線が交差しているかチェックする
       * 
       * @param x1  線１の始点 (X座標)
       * @param y1  線１の始点 (Y座標)
       * @param x2  線１の終点 (X座標)
       * @param y2  線１の終点 (Y座標)
       * @param x3  線２の始点 (X座標)
       * @param y3  線２の始点 (Y座標)
       * @param x4  線２の終点 (X座標)
       * @param y4  線２の終点 (Y座標)
       * @return  true なら線が交差している
       */
      private boolean checkIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
      {
          // 線が平行かどうかのチェックを行う
    	  float denominator = (x2 - x1) * (y4 - y3) - (y2 - y1) * (x4 - x3);
    	  if (Math.abs(denominator) < 0.00001)
    	  {
    		  // 線が平行と認識、交差しない
    		  return (false);
    	  }

          float tempX = x3 - x1;
          float tempY = y3 - y1;
          float dR = (((y4 - y3) * tempX) - ((x4 - x3) * tempY)) / denominator;
          float dS = (((y2 - y1) * tempX) - ((x2 - x1) * tempY)) / denominator;
 
    	  // ２直線の交点を求める
    	  //float crossX, crossY;
          //crossX = x1 + dR * (x2 - x1);
          //crossY = y1 + dR * (y2 - y1);

          // 交点が線分内にあるかどうかをチェックする
          if ((dR >= 0)&&(dR <= 1)&&(dS >= 0)&&(dS <= 1))
          {
        	  return (true);
          }
          return (false);
      }

      /**
       *   並行移動・ズームのサイズをリセットする
       * 
       */
      public void resetScaleAndLocation(SeekBar zoomBar)
      {
    	    // 並行移動をリセットする
    	    drawTransX = 0.0f;
    	    drawTransY = 0.0f;
    	    
            // プログレスバーの位置をリセットする
    	    drawScale = 1.0f;
    	    zoomBar.setProgress(50);

    	    // preferenceに状態を記録する
  	        recordTranslateAndZoomScale(50);
      }

      /**
       *    スライドバーを変更された時の処理
       */
      public void zoomScaleChanged(int progress)
      {
    	  float val = ((float) progress - 50.0f) / 50.0f;

    	  // 前の表示領域サイズを取得
    	  float prevSizeWidth = screenWidth * drawScale;
    	  float prevSizeHeight = screenHeight * drawScale;

    	  //  表示倍率を変更し、倍率を画面に表示する
    	  drawScale = (float) Math.round(Math.pow(10.0, val) * 10.0) / 10.0f;
    	  TextView  textview = (TextView) parent.findViewById(R.id.ZoomRate);
    	  textview.setText("x" + drawScale);

    	  // 現在の表示領域サイズを取得
    	  float showSizeWidth = screenWidth * drawScale;
    	  float showSizeHeight = screenHeight * drawScale;

    	  // 倍率にあわせて並行移動する場所を調整する
    	  drawTransX = (prevSizeWidth - showSizeWidth) / 2.0f  + drawTransX;
    	  drawTransY = (prevSizeHeight - showSizeHeight) / 2.0f + drawTransY;
          
	      // preferenceに状態を記録する
	      recordTranslateAndZoomScale(progress);
      }

      /**
       *    平行移動状態と倍率の状態を記憶する
       * 
       */
      private void recordTranslateAndZoomScale(int progress)
      {
    	  //Log.v(Main.APP_IDENTIFIER, "recordTranslateAndZoomScale() : x" + drawScale + " X:" + drawTransX + " Y: " + drawTransY);
          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
          SharedPreferences.Editor editor = preferences.edit();
          editor.putFloat("drawScale", drawScale);
          editor.putFloat("drawTransX", drawTransX);
          editor.putFloat("drawTransY", drawTransY);
          editor.putInt("zoomProgress", progress);
          editor.commit();    	  
      }

      /**
       *    平行移動状態と倍率の状態を記憶する
       * 
       */
      private void restoreTranslateAndZoomScale()
      {
          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
          drawScale = preferences.getFloat("drawScale", 1.0f);
          drawTransX = preferences.getFloat("drawTransX", 0.0f);
          drawTransY = preferences.getFloat("drawTransY", 0.0f);
    	  Log.v(Main.APP_IDENTIFIER, "restoreTranslateAndZoomScale() : x" + drawScale + " X:" + drawTransX + " Y: " + drawTransY);
      }

      /**
       *    GestureDetector.OnGestureListener の実装
       */
      public boolean onDown(MotionEvent event)
      {
          //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onDown() "  + event.getX()  + "," + event.getY());    	  
          return (false);    	  
      }

      /**
       *    GestureDetector.OnGestureListener の実装
       */
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
      {
          //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onFling() "  + velocityX  + "," + velocityY);    	  
          return (false);    	  
      }

      /**
       *    GestureDetector.OnGestureListener の実装
       */
      public void onLongPress(MotionEvent event)
      {
    	  Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onLongPress() "  + event.getX()  + "," + event.getY());   

    	  // タッチ位置をオブジェクト画像の座標に変換する
    	  float x = (event.getX() - drawTransX) / drawScale;
      	  float y = (event.getY() - drawTransY) / drawScale;

    	  // タッチ位置にオブジェクトが存在するか確認する
          MeMoMaObjectHolder.PositionObject  position = checkSelectedObject(x, y);
      	  if (position != null)
      	  {
      		  // 長押し処理を実施していることを記憶する
      	      onGestureProcessed = true;

      		  // タッチした場所にオブジェクトが存在した！！
      	      selectionReceiver.objectSelectedContext(position.getKey());
      	  }          
      }

      /**
       *    GestureDetector.OnGestureListener の実装
       */
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
      {
          //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onScroll() "  + distanceX  + "," + distanceY);    	  
          return (false);    	  
      }

      /**
       *    GestureDetector.OnGestureListener の実装
       */
      public void onShowPress(MotionEvent event)
      {
         //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onShowPress() "  + event.getX()  + "," + event.getY());    	  
      }

      /**
       *    GestureDetector.OnGestureListener の実装
       */
      public boolean onSingleTapUp(MotionEvent event)
      {
            //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onSingleTapUp() "  + event.getX()  + "," + event.getY());
            return (false);
      }

      /**
       *    スライドバーを変更された時の処理
       *    (SeekBar.OnSeekBarChangeListener の実装)
       */
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
      {	
    	    // 画面描画の倍率を変更する
    	    zoomScaleChanged(progress);

    	    // 画面描画クラスに再描画を指示する
	        final GokigenSurfaceView surfaceView = (GokigenSurfaceView) parent.findViewById(R.id.GraphicView);
	        surfaceView.doDraw();
      }

      /**
       *    SeekBar.OnSeekBarChangeListener の実装
       */
      public void onStartTrackingTouch(SeekBar seekBar)
      {
           // 何もしない
      }

      /**
       *    SeekBar.OnSeekBarChangeListener の実装
       */
      public void onStopTrackingTouch(SeekBar seekBar)
      {
    	   // 何もしない 
      }

      /**
       *   （ScaleGestureDetector.OnScaleGestureListener の実装）
       * 
       * @param detector
       * @return
       */
      public boolean onScale(ScaleGestureDetector detector)
      {
          float scaleFactor = detector.getScaleFactor();
          //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onScale() : " + scaleFactor + " (" + currentScaleBar + ")");

          /** 画面表示の倍率が変更された！　x < 1 : 縮小、 1 < x : 拡大 **/
          if (scaleFactor < 1.0f)
          {
        	  currentScaleBar = (currentScaleBar == 0) ? 0 : currentScaleBar - 1;
          }
          else if (scaleFactor > 1.0f)
          {
        	  currentScaleBar = (currentScaleBar == 100) ? 100 : currentScaleBar + 1;
          }
          zoomScaleChanged(currentScaleBar);

          return (onScaling);
      }

      /**
       *   （ScaleGestureDetector.OnScaleGestureListener の実装）
       *   
       * 
       */
      public  boolean	 onScaleBegin(ScaleGestureDetector detector)
      {
          //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onScaleBegin() ");
          SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
          currentScaleBar = preferences.getInt("zoomProgress", 50);
    	  onScaling = true;
    	  return (onScaling);
      }

      /**
       *   （ScaleGestureDetector.OnScaleGestureListener の実装）
       *   
       */
      public void	 onScaleEnd(ScaleGestureDetector detector)
      {
          //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onScaleEnd() " + currentScaleBar);
    	  onScaling = false;
    	  
    	  // シークバーを設定し、値を記憶する
	      final SeekBar seekbar = (SeekBar) parent.findViewById(R.id.ZoomInOut);
	      seekbar.setProgress(currentScaleBar);	        
	      zoomScaleChanged(currentScaleBar);
      }
 }
