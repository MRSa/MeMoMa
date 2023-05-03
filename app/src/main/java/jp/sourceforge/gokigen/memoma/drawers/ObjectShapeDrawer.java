package jp.sourceforge.gokigen.memoma.drawers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.PositionObject;

/**
 *   めもまのオブジェクト・ライン・ラベルを描画するメソッド群
 *   (現在のところ、MeMoMaCanvasDrawerクラスから追い出してきただけ...)
 * 
 * @author MRSa
 *
 */
public class ObjectShapeDrawer
{

    static public float drawObjectOval(Canvas canvas, RectF objectShape, Paint paint)
    {
        // 楕円形の描画
        canvas.drawOval(objectShape, paint);

        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2);
    }

    static public float drawObjectRect(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 四角形を描画する
        canvas.drawRect(objectShape, paint);
        return (0.0f);
    }

    static public float drawObjectRoundRect(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 丸角四角形の描画
        canvas.drawRoundRect(objectShape, MeMoMaObjectHolder.ROUNDRECT_CORNER_RX, MeMoMaObjectHolder.ROUNDRECT_CORNER_RY, paint);

        return (0.0f);
    }

    static public float drawObjectDiamond(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 菱形の描画
        Path path = new Path();
        path.moveTo(objectShape.centerX(), objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY());
        path.lineTo(objectShape.centerX(), objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY());
        path.lineTo(objectShape.centerX(), objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2);
    }

    static public float drawObjectKeyboard(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 台形(キーボード型)の描画
        Path path = new Path();
        path.moveTo(objectShape.left, objectShape.centerY() - MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN);
        path.lineTo(objectShape.left, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY() - MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN);
    }

    static public float drawObjectParallelogram(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 平行四辺形の描画
        Path path = new Path();
        path.moveTo(objectShape.left + MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN, objectShape.top);
        path.lineTo(objectShape.left, objectShape.bottom);
        path.lineTo(objectShape.right - MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.top);
        path.lineTo(objectShape.left + MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN, objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectHexagonal(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 六角形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 2;
        path.moveTo(objectShape.left + margin, objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY());
        path.lineTo(objectShape.left + margin, objectShape.bottom);
        path.lineTo(objectShape.right - margin, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY());
        path.lineTo(objectShape.right - margin, objectShape.top);
        path.lineTo(objectShape.left + margin, objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectPaper(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 書類の形の描画
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 3.0f;
        Path path = new Path();
        path.moveTo(objectShape.left, objectShape.top);
        path.lineTo(objectShape.left, objectShape.bottom - margin);
        path.cubicTo((objectShape.left + objectShape.centerX()) / 2.0f , objectShape.bottom, (objectShape.right + objectShape.centerX()) / 2.0f, objectShape.bottom - margin, objectShape.right, objectShape.bottom - margin);
        path.lineTo(objectShape.right, objectShape.top);
        path.lineTo(objectShape.left, objectShape.top);
        canvas.drawPath(path, paint);
        return ( - MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN);
    }

    static public float drawObjectDrum(Canvas canvas, RectF objectShape, Paint paint, Paint.Style paintStyle)
    {
		float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN;
		// 円柱の描画
        Path path = new Path();
        path.moveTo(objectShape.left, objectShape.top);
        path.arcTo(new RectF(objectShape.left, objectShape.top,objectShape.right, objectShape.top + margin), 180.0f, 359.999f, true);
        path.lineTo(objectShape.left, objectShape.bottom - (margin / 2.0f));
        path.arcTo(new RectF(objectShape.left, objectShape.bottom - margin, objectShape.right, objectShape.bottom), 180.0f, -180.0f, true);
        path.lineTo(objectShape.right, objectShape.top + (margin / 2.0f));
        if (paintStyle != Paint.Style.STROKE)
        {
        	// 塗りつぶし用に線の領域を追加する
            path.arcTo(new RectF(objectShape.left, objectShape.top, objectShape.right, objectShape.top + margin), 180.0f, 180.0f, true);
            path.lineTo(objectShape.left, objectShape.bottom - (margin / 2.0f));
            path.arcTo(new RectF(objectShape.left, objectShape.bottom - margin, objectShape.right, objectShape.bottom), 180.0f, -180.0f, true);
        }
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectCircle(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 円を描画する
        canvas.drawCircle(objectShape.centerX(), objectShape.centerY(), ((objectShape.right - objectShape.left)/ 2.0f), paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public void drawObjectNoRegion(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 何も表示しないとわからないので、ラベルが無いときには枠を表示する
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(objectShape, paint);
        paint.setColor(Color.WHITE);
    }

    static public float drawObjectLoopStart(Canvas canvas, RectF objectShape, Paint paint)
    {
		// ループ開始図形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 2;
        path.moveTo(objectShape.left + margin, objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY());
        path.lineTo(objectShape.left, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY());
        path.lineTo(objectShape.right - margin, objectShape.top);
        path.lineTo(objectShape.left + margin, objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectLoopEnd(Canvas canvas, RectF objectShape, Paint paint)
    {
		// ループ終了図形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 2;
        path.moveTo(objectShape.left, objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY());
        path.lineTo(objectShape.left + margin, objectShape.bottom);
        path.lineTo(objectShape.right - margin, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY());
        path.lineTo(objectShape.right, objectShape.top);
        path.lineTo(objectShape.left, objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectLeftArrow(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 左側矢印図形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 4.0f;
        path.moveTo(objectShape.left + margin, objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY());
        path.lineTo(objectShape.left + margin, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.top);
        path.lineTo(objectShape.left + margin, objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectDownArrow(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 下側矢印図形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 2;
        path.moveTo(objectShape.left, objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY() + margin);
        path.lineTo(objectShape.centerX(), objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY() + margin);
        path.lineTo(objectShape.right, objectShape.top);
        path.lineTo(objectShape.left, objectShape.top);
        canvas.drawPath(path, paint);
        return (- MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN);
    }

    static public float drawObjectUpArrow(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 上側矢印図形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 2.0f;
        path.moveTo(objectShape.centerX(), objectShape.top);
        path.lineTo(objectShape.left, objectShape.centerY() - margin);
        path.lineTo(objectShape.left, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY() - margin);
        path.lineTo(objectShape.centerX(), objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

    static public float drawObjectRightArrow(Canvas canvas, RectF objectShape, Paint paint)
    {
		// 右側矢印図形の描画
        Path path = new Path();
        float margin = MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 4.0f;
        path.moveTo(objectShape.left, objectShape.top);
        path.lineTo(objectShape.left, objectShape.bottom);
        path.lineTo(objectShape.right - margin, objectShape.bottom);
        path.lineTo(objectShape.right, objectShape.centerY());
        path.lineTo(objectShape.right - margin, objectShape.top);
        path.lineTo(objectShape.left, objectShape.top);
        canvas.drawPath(path, paint);
        return (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN / 2.0f);
    }

	  /**
	   *    矢印を描画する処理 (ツリー表示時)
	   *
	   */
	static public void drawArrowTree(Canvas canvas, Paint paint, float x1, float y1, float checkValue, boolean isXaxis)
    {
		float margin = 8.0f;
	  	float direction;
	  	if (isXaxis)
        {
	  	    direction = (checkValue < x1) ? -1.0f : 1.0f;
            canvas.drawLine(x1, y1, (x1 + direction * margin), (y1 - margin), paint);
	        canvas.drawLine(x1, y1, (x1 + direction * margin), (y1 + margin), paint);
	  	}
	  	else
	  	{
	  	    direction = (checkValue < y1) ? -1.0f : 1.0f;
	        canvas.drawLine(x1, y1, (x1 - margin), (y1 + direction * margin), paint);
	        canvas.drawLine(x1, y1, (x1 + margin), (y1 + direction * margin), paint);    		  
	  	}
    }
	  /**
	   *    矢印を描画する処理
	   *
	   */
	  static public void drawArrow(Canvas canvas, Paint paint, float x1, float y1, float x2, float y2)
	  {
          // 矢印線の長さ
		  float moveX = 14.0f;
		  
		  // 接続線の傾きが、どれくらいの角度で入っているか？
		  float centerDegree = (float) (Math.atan2((y2 - y1) , (x2 - x1))  * 180.0d / Math.PI);

		  // x1, y1 に x2, y2 方向から来た線に合わせた矢印を描画する
		  // (2本、１本づつ引いて、それを回転行列で回転させている)

		  // 回転行列の準備
		  Matrix matrix1 = new Matrix();
		  matrix1.setRotate((centerDegree + 30), x1, y1);
	      Matrix matrix2 = new Matrix();
		  matrix2.setRotate((centerDegree - 30), x1, y1);

		  // 線分を引いた後、回転行列で回転させる
		  Path pathLine1 = new Path();
	      pathLine1.moveTo(x1, y1);
	      pathLine1.lineTo(x1 + moveX, y1);
	      pathLine1.transform(matrix1);
	      canvas.drawPath(pathLine1, paint);

		  Path pathLine2 = new Path();
	      pathLine2.moveTo(x1, y1);
	      pathLine2.lineTo(x1 + moveX, y1);
	      pathLine2.transform(matrix2);
	      canvas.drawPath(pathLine2, paint);
    }

	  /**
	     *    オブジェクトのラベルを表示する
	     *
	     */
      public static void drawTextLabel(Canvas canvas, Paint paint, PositionObject pos, RectF region, int displayObjectInformation, float offsetX, float offsetY)
      {
          // タイトルの先頭部分を表示する場合...
          String labelToShow = pos.getLabel();
          if (displayObjectInformation == 0)
          {
              float width = region.width() - MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN_WIDTH;
              int textLen = paint.breakText(pos.getLabel(), true, width, null);  // 省略文字を追加するから、そのぶん減らす
              labelToShow = labelToShow.substring(0, textLen);
              if (!labelToShow.equals(pos.getLabel()))
              {
                  // truncate した場合には、省略文字を出す。
                  labelToShow = labelToShow + "...";
              }
          }

          if (Paint.Style.valueOf(pos.getPaintStyle()) != Paint.Style.STROKE)
          {
              // オブジェクトを塗りつぶすのときは、文字の色を設定する
              paint.setColor(pos.getLabelColor());
          }

          // 文字をちょっと影付きにする
          paint.setStyle(Paint.Style.FILL_AND_STROKE);
          paint.setShadowLayer(0.7f, 0.7f, 0.7f, Color.DKGRAY);

          // ユーザチェックの描画
          if (pos.getUserChecked())
          {
              canvas.drawText("*", region.centerX(), region.top + (MeMoMaCanvasDrawer.OBJECTLABEL_MARGIN * 2.0f), paint);
          }

          // 強調表示
          if (pos.getstrokeWidth() != 0.0f)
          {
              // そのまま表示すると、読めないので、太さを調整し、アンダーラインを引くことにする
              paint.setStrokeWidth(0.0f);
              paint.setSubpixelText(true);
              paint.setUnderlineText (true);
          }

          if (displayObjectInformation == 0)
          {
              // １行分しか表示しない場合...そのまま表示して終了する
              canvas.drawText(labelToShow, (region.left + offsetX),   (region.centerY() + offsetY), paint);
              return;
          }

          float tall = paint.getFontMetrics().top + 1.0f;
          float posX = (region.left + offsetX);
          float posY =  (region.centerY() + offsetY);
          float width = region.right - region.left - 12.0f;  // 幅

          int startChar  = 0;
          int endChar = pos.getLabel().length();
          do
          {
              int textLen = paint.breakText(pos.getLabel(), startChar, endChar, true, width, null);
              canvas.drawText(labelToShow, startChar, (startChar +textLen), posX, posY, paint);

              posY = posY - tall;
              startChar = startChar + textLen;
          } while (startChar < endChar);
      }
}
