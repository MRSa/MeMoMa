package jp.sourceforge.gokigen.memoma.drawers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.LineStyleHolder
import jp.sourceforge.gokigen.memoma.holders.MeMoMaConnectLineHolder
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.holders.ObjectConnector
import jp.sourceforge.gokigen.memoma.holders.OperationModeHolder
import jp.sourceforge.gokigen.memoma.holders.PositionObject
import jp.sourceforge.gokigen.memoma.io.ImageLoader
import jp.sourceforge.gokigen.memoma.operations.IObjectSelectionReceiver
import kotlin.math.abs
import kotlin.math.pow

/**
 * メモまの描画クラス
 */
class MeMoMaCanvasDrawer(
    private val parent: AppCompatActivity,
    private val objectHolder: MeMoMaObjectHolder,
    private val lineStyleHolder: LineStyleHolder,
    private val selectionReceiver: IObjectSelectionReceiver
) :
    ICanvasDrawer, GestureDetector.OnGestureListener, ScaleGestureDetector.OnScaleGestureListener,
    SeekBar.OnSeekBarChangeListener
{

    private var backgroundColor = BACKGROUND_COLOR_DEFAULT

    private var selectedPosition: PositionObject? = null
    private var tempPosX = Float.MIN_VALUE
    private var tempPosY = Float.MIN_VALUE
    private var downPosX = Float.MIN_VALUE
    private var downPosY = Float.MIN_VALUE

    // 以下の値は、MeMoMaListenerで初期値を設定する
    private var objectStyle = MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE

    private var drawScale = 1.0f // 表示の倍率
    private var drawTransX = 0.0f // 並行移動距離 (X)
    private var drawTransY = 0.0f // 並行移動距離 (Y)
    private var onScaling = false // ピンチイン・ピンチアウト操作しているかどうかを示す
    private var currentScaleBar = 50 // 現在のピンチイン・ピンチアウト倍率

    private var onGestureProcessed = false // 長押し時の処理を行なっているかどうかを示す。

    private var screenWidth = 0.0f // 表示領域の幅
    private var screenHeight = 0.0f // 表示領域の高さ

    private var displayObjectInformation = 1 // オブジェクトラベルの表示

    private var backgroundBitmapUri: String = ""
    private var backgroundBitmap: Bitmap? = null
    private val lineHolder: MeMoMaConnectLineHolder = objectHolder.getConnectLineHolder()

    // ジェスチャを検出するクラスを生成する
    private val gestureDetector = GestureDetector(parent, this)
    private val scaleGestureDetector =
        ScaleGestureDetector(parent, this)

    /**
     * コンストラクタ
     *
     */
    init {
        // ズーム倍率を展開する
        restoreTranslateAndZoomScale()
    }

    /**
     * オブジェクトの形状を変更する
     * (ここで指定された形状のチェックを行っておく。)
     */
    fun setObjectStyle(style: Int) {
        objectStyle =
            when (style) {
                MeMoMaObjectHolder.DRAWSTYLE_OVAL, MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT, MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE, MeMoMaObjectHolder.DRAWSTYLE_DIAMOND, MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD, MeMoMaObjectHolder.DRAWSTYLE_PAPER, MeMoMaObjectHolder.DRAWSTYLE_DRUM, MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM, MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL, MeMoMaObjectHolder.DRAWSTYLE_CIRCLE, MeMoMaObjectHolder.DRAWSTYLE_NO_REGION, MeMoMaObjectHolder.DRAWSTYLE_LOOP_START, MeMoMaObjectHolder.DRAWSTYLE_LOOP_END, MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW, MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW, MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW, MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW -> style
                else -> MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE
            }
    }

    /**
     *
     *
     */
    fun updateBackgroundBitmap(uri: String, width: Int, height: Int)
    {
        // 背景画像の文字列を記憶する
        backgroundBitmapUri = uri

        // とりあえず、背景画像をクリアしてガベコレする。
        System.gc()
        if (uri.isEmpty())
        {
            // 背景画像の指定がなかったので、ここでリターンする。
            return
        }
        try
        {
            // とりあえず設定する情報をログに残してみる
            Log.v(
                TAG,
                "MeMoMaCanvasDrawer::updateBackgroundBitmap() : w:$width , h:$height $uri"
            )

            // 背景画像を取得して設定する。
            backgroundBitmap =
                ImageLoader().getBitmapFromUri(parent, ImageLoader().parseUri(uri), width, height)
        }
        catch (ex: Throwable)
        {
            Log.v(TAG, "MeMoMaCanvasDrawer::updateBackgroundBitmap() : " + uri + " , " + ex.message)
            ex.printStackTrace()
            backgroundBitmap
            backgroundBitmapUri = ""
            System.gc()
        }
    }

    /**
     * 背景画像を設定する
     */
    fun setBackgroundUri(uri: String)
    {
        backgroundBitmapUri = uri
    }

    /**
     * 背景色を(文字列で)設定する
     */
    fun setBackgroundColor(colorString: String)
    {
        try
        {
            Log.v(TAG, "background Color: $colorString")
            backgroundColor = Color.parseColor(colorString)
            return
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
        backgroundColor = BACKGROUND_COLOR_DEFAULT
    }

    /**
     * 初期化ロジックを設定する
     */
    override fun prepareToStart(width: Int, height: Int)
    {
        Log.v(TAG, "MeMoMaCanvasDrawer::prepareToStart() x:$width , y:$height")
        try
        {
            // 背景画像を更新する
            //updateBackgroundBitmap(backgroundBitmapUri, width, height);

            // Preferenceを読み出す
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            displayObjectInformation = (preferences.getString("objectPrintData", "1")?:"1").toInt()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 画面の大きさが変わってしまった場合...
     *
     */
    override fun changedScreenProperty(format: Int, width: Int, height: Int)
    {
        // 背景画像を更新する
        updateBackgroundBitmap(backgroundBitmapUri, width, height)

        // 表示画面サイズを覚える
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()

        Log.v(TAG, "changedScreenProperty() x:$screenWidth , y:$screenHeight")
    }


    /**
     * キャンバスにオブジェクト（と接続線）を表示する
     *
     */
    override fun drawOnCanvas(canvas: Canvas)
    {
        //Log.v(TAG, "MeMoMaCanvasDrawer::drawOnCanvas()");
        try
        {
            // 画面全体をクリアする
            //canvas.drawColor(Color.argb(backgroundColorAlfa, backgroundColorRed, backgroundColorGreen, backgroundColorBlue), Mode.CLEAR);
            canvas.drawColor(backgroundColor)

            // 背景画像が設定されていた場合は、背景画像を描画する
            if (backgroundBitmap != null)
            {
                canvas.drawBitmap(backgroundBitmap!!, 0f, 0f, Paint())
            }

            // 表示位置を移動させる
            canvas.translate(drawTransX, drawTransY)

            // 画面の表示領域を拡大・縮小する
            canvas.scale(drawScale, drawScale)

            // オブジェクト間の接続線をすべて表示する
            drawConnectionLines(canvas, 0.0f, 0.0f)

            // オブジェクトをすべて表示
            drawObjects(canvas, 0.0f, 0.0f)

            //  移動中かどうかのチェックを行う。
            if (isFlicking()) {
                // 移動中の場合、フリック時の軌跡と現在位置を表示する
                drawTrackAndPositions(canvas)
            }
        } catch (ex: Exception) {
            // 例外発生...でもそのときには何もしない
            Log.v(TAG, "drawOnCanvas() ex: " + ex.message)
        }
    }

    /**
     * オブジェクトをBitmapCanvas上に描く
     */
    fun drawOnBitmapCanvas(canvas: Canvas, offsetX: Float, offsetY: Float)
    {
        try
        {
            val paint = Paint()

            // 画面全体をクリアする
            canvas.drawColor(backgroundColor)

            // 背景画像が設定されていた場合は、背景画像を描画する
            if (backgroundBitmap != null)
            {
                canvas.drawBitmap(backgroundBitmap!!, offsetX, offsetY, paint)
            }

            // オブジェクト間の接続線をすべて表示する
            drawConnectionLines(canvas, offsetX, offsetY)

            // オブジェクトをすべて表示
            drawObjects(canvas, offsetX, offsetY)

            // タイトルとめもまのアイコンを表示する : 文字の色は黒でいいのかな...
            val bitmap = BitmapFactory.decodeResource(parent.resources, R.drawable.icon1)
            canvas.drawBitmap(bitmap, 2.0f, 2.0f, paint)

            // 文字をライトグレー、ちょっと影付きにする
            paint.color = Color.LTGRAY
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.setShadowLayer(0.5f, 0.5f, 0.5f, Color.DKGRAY)
            paint.textSize = DATA_LABEL_TEXT_SIZE.toFloat()
            canvas.drawText(objectHolder.getDataTitle(), (bitmap.width + 10.0f), 32.0f, paint)
        }
        catch (ex: Exception)
        {
            // 例外発生...でもそのときには何もしない
            Log.v(TAG, "drawOnBitmapCanvas() ex: " + " " + ex.message)
            ex.printStackTrace()
        }
    }

    /**
     * オブジェクト間の接続線を表示する
     */
    private fun drawConnectionLines(canvas: Canvas, offsetX: Float, offsetY: Float) {
        // オブジェクトの色と枠線を設定する （連続線用）
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE

        // オブジェクトの色と枠線を設定する  （点線用）
        val dashLinePaint = Paint()
        dashLinePaint.color = Color.WHITE
        dashLinePaint.style = Paint.Style.STROKE
        dashLinePaint.setPathEffect(DashPathEffect(floatArrayOf(5.0f, 5.0f), 0f))


        // オブジェクトの描画 （保持しているものはすべて表示する）
        val keys = lineHolder.lineKeys
        while (keys.hasMoreElements()) {
            val key = keys.nextElement()
            val line = lineHolder.getLine(key)
            if (line != null)
            {
                if (line.getKey() > 0) {
                    // 実際にラインを引く
                    drawLine(canvas, paint, dashLinePaint, line, offsetX, offsetY)
                } else {
                    // ここは呼ばれないはず。。。消したはずのものが残っている
                    Log.v(TAG, "DETECTED DELETED LINE")
                }
            }
        }
    }

    /**
     * 接続する線を引く
     *
     */
    private fun drawLine(
        canvas: Canvas?,
        paint: Paint,
        dashPaint: Paint,
        line: ObjectConnector?,
        offsetX: Float,
        offsetY: Float
    )
    {
        try
        {
            if (canvas == null)
            {
                // なにもしない
                return
            }
            if (line == null)
            {
                // なにもしない
                return
            }

            val from = objectHolder.getPosition(line.getFromObjectKey())
            val to = objectHolder.getPosition(line.getToObjectKey())
            if ((from == null) || (to == null))
            {
                // なにもしない
                return
            }

            // ラインの太さを設定する。
            paint.strokeWidth = line.getLineThickness().toFloat()

            // ラインの太さを設定する。
            dashPaint.strokeWidth = line.getLineThickness().toFloat()

            // ラインのスタイル(連続線 or 点線)を設定する
            val linePaint =
                if ((line.getLineShape() == LineStyleHolder.LINESHAPE_DASH)) dashPaint else paint

            // 初期値として、各オブジェクトの中心座標を設定する
            val fromRect = from.getRect()
            val toRect = to.getRect()
            var startX = fromRect.centerX() + offsetX
            var endX = toRect.centerX() + offsetX
            var startY = fromRect.centerY() + offsetY
            var endY = toRect.centerY() + offsetY

            // Y座標の線の位置を補正する
            if (fromRect.bottom < toRect.top) {
                startY = fromRect.bottom + offsetY
                endY = toRect.top + offsetY
            } else if (fromRect.top > toRect.bottom) {
                startY = fromRect.top + offsetY
                endY = toRect.bottom + offsetY
            }

            // X座標の線の位置を補正する (Y座標が補正されていないとき)
            if ((startY != (fromRect.top + offsetY)) && (startY != (fromRect.bottom + offsetY))) {
                if (fromRect.right < toRect.left) {
                    startX = fromRect.right + offsetX
                    endX = toRect.left + offsetX
                } else if (fromRect.left > toRect.right) {
                    startX = fromRect.left + offsetX
                    endX = toRect.right + offsetX
                }
            }

            val lineStyle = line.getLineStyle()
            if ((lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW) ||
                (lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW) ||
                (lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW)
            ) {
                // ツリー形式のように接続する ...
                if (startX == (fromRect.centerX() + offsetX)) {
                    val middleY = (startY + endY) / 2
                    canvas.drawLine(startX, startY, startX, middleY, linePaint)
                    canvas.drawLine(startX, middleY, endX, middleY, linePaint)
                    canvas.drawLine(endX, middleY, endX, endY, linePaint)


                    // やじるしをつける処理
                    if (lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW) {
                        // 始点に矢印をつける
                        ObjectShapeDrawer.drawArrowTree(
                            canvas,
                            paint,
                            startX,
                            startY,
                            middleY,
                            false
                        )
                    } else if (lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW) {
                        // 終点に矢印をつける
                        ObjectShapeDrawer.drawArrowTree(canvas, paint, endX, endY, middleY, false)
                    }
                } else {
                    val middleX = (startX + endX) / 2
                    canvas.drawLine(startX, startY, middleX, startY, linePaint)
                    canvas.drawLine(middleX, startY, middleX, endY, linePaint)
                    canvas.drawLine(middleX, endY, endX, endY, linePaint)

                    // やじるし(三角形)をつける処理
                    if (lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW) {
                        // 始点に矢印をつける
                        ObjectShapeDrawer.drawArrowTree(
                            canvas,
                            paint,
                            startX,
                            startY,
                            middleX,
                            true
                        )
                    } else if (lineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW) {
                        // 終点に矢印をつける
                        ObjectShapeDrawer.drawArrowTree(canvas, paint, endX, endY, middleX, true)
                    }
                }
            } else if ((lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW) ||
                (lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_L_ARROW) ||
                (lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW)
            ) {
                // 曲線で接続する
                val middleX = (startX + endX) / 2
                val middleY = (startY + endY) / 2
                val x1 = (startX + middleX) / 2
                val x2 = (middleX + endX) / 2

                val pathLine = Path()
                pathLine.moveTo(startX, startY)
                pathLine.cubicTo(x1, middleY, x2, middleY, endX, endY)
                canvas.drawPath(pathLine, linePaint)

                //  やじるしをつける処理
                if (lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_L_ARROW) {
                    // 始点に矢印をつける
                    ObjectShapeDrawer.drawArrow(canvas, paint, startX, startY, endX, endY)
                } else if (lineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW) {
                    // 終点に矢印をつける
                    ObjectShapeDrawer.drawArrow(canvas, paint, endX, endY, startX, startY)
                }
            } else  // if (line.lineStyle == MeMoMaConnectLineHolder.LINESTYLE_STRAIGHT)
            {
                // 直線で接続する
                canvas.drawLine(startX, startY, endX, endY, linePaint)


                //  やじるしをつける処理
                if (lineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_L_ARROW) {
                    // 始点に矢印をつける
                    ObjectShapeDrawer.drawArrow(canvas, paint, startX, startY, endX, endY)
                } else if (lineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW) {
                    // 終点に矢印をつける
                    ObjectShapeDrawer.drawArrow(canvas, paint, endX, endY, startX, startY)
                }
            }
        } catch (ex: Exception) {
            // なにもしない
            Log.v(TAG, "EXCEPTION :" + ex.message)
        }
    }

    /**
     * オブジェクトを動かしている最中かどうかの判定を行う。
     * @return  trueなら、動かしている最中
     */
    private fun isFlicking(): Boolean
    {
        return (!((tempPosX == Float.MIN_VALUE) || (tempPosY == Float.MIN_VALUE)))
    }

    /**
     * フリック時の軌跡と現在地点を表示する
     */
    private fun drawTrackAndPositions(canvas: Canvas) {
        // フリック時の軌跡を表示する
        val x = (tempPosX - drawTransX) / drawScale
        val y = (tempPosY - drawTransY) / drawScale

        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = Color.GRAY
        if (selectedPosition != null)
        {
            val objRect = selectedPosition!!.getRect()
            val objX = (objRect.right - objRect.left) / 2
            val objY = (objRect.bottom - objRect.top) / 2
            canvas.drawLine(objRect.centerX(), objRect.centerY(), x, y, paint)
            canvas.drawRect((x - objX), (y - objY), (x + objX), (y + objY), paint)

            // 現在地点の表示
            drawObject(canvas, selectedPosition!!, true, 0.0f, 0.0f)
        } else  // オブジェクト非選択時の表示
        {
            val data = selectionReceiver.touchedVacantArea()
            if (data == OperationModeHolder.OPERATIONMODE_MOVE) {
                // 移動モードのとき... （表示領域を移動させる）
                drawTransX = (tempPosX - downPosX)
                drawTransY = (tempPosY - downPosY)

                //  表示領域の移動を記憶する
                val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                val editor = preferences.edit()
                editor.putFloat("drawTransX", drawTransX)
                editor.putFloat("drawTransY", drawTransY)
                editor.apply()
            } else {
                // 移動モード以外
                paint.color = Color.YELLOW
                canvas.drawLine(((downPosX) / drawScale), ((downPosY) / drawScale), x, y, paint)
            }
        }
    }

    /**
     * オブジェクト（１個）を表示する
     *
     */
    private fun drawObject(
        canvas: Canvas,
        `object`: PositionObject,
        isMoving: Boolean,
        offsetX: Float,
        offsetY: Float
    ) {
        var labelOffsetX = OBJECT_LABEL_MARGIN
        var labelOffsetY = 0.0f

        // オブジェクトの色と枠線を設定する
        val paint = Paint()
        if (isMoving) {
            paint.color = Color.YELLOW
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = `object`.getstrokeWidth()
        } else {
            paint.color = `object`.getObjectColor()
            paint.style = Paint.Style.valueOf(`object`.getPaintStyle())
            paint.strokeWidth = `object`.getstrokeWidth()
        }

        // 図形の形状に合わせて描画する
        val objectShape = RectF(`object`.getRect())
        objectShape.left += offsetX
        objectShape.right += offsetX
        objectShape.top += offsetY
        objectShape.bottom += offsetY

        val drawStyle = `object`.getDrawStyle()
        if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_OVAL) {
            // 楕円形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectOval(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT) {
            // 丸角四角形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectRoundRect(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DIAMOND) {
            // 菱形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectDiamond(canvas, objectShape, paint)
            labelOffsetX = OBJECT_LABEL_MARGIN
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD) {
            // 台形(キーボード型)の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectKeyboard(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM) {
            // 平行四辺形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectParallelogram(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL) {
            // 六角形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectHexagonal(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PAPER) {
            // 書類の形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectPaper(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DRUM) {
            // 円柱の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectDrum(
                canvas,
                objectShape,
                paint,
                Paint.Style.valueOf(`object`.getPaintStyle())
            )
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE) {
            // 円を描画する
            labelOffsetY = ObjectShapeDrawer.drawObjectCircle(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_NO_REGION) {
            // 枠なしを描画（？）する ... なにもしない
            if (`object`.getLabel().isEmpty()) {
                // 何も表示しないとわからないので、ラベルが無いときには枠を表示する
                ObjectShapeDrawer.drawObjectNoRegion(canvas, objectShape, paint)
            }
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_START) {
            // ループ開始図形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectLoopStart(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_END) {
            // ループ終了図形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectLoopEnd(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW) {
            // 左側矢印図形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectLeftArrow(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW) {
            // 下側矢印図形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectDownArrow(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW) {
            // 上側矢印図形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectUpArrow(canvas, objectShape, paint)
        } else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW) {
            // 右側矢印図形の描画
            labelOffsetY = ObjectShapeDrawer.drawObjectRightArrow(canvas, objectShape, paint)
        } else  // if (pos.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE)
        {
            // 四角形を描画する
            labelOffsetY = ObjectShapeDrawer.drawObjectRect(canvas, objectShape, paint)
        }

        // 文字サイズを設定する。
        paint.textSize = `object`.getFontSize()

        // 文字ラベルを表示する
        ObjectShapeDrawer.drawTextLabel(
            canvas,
            paint,
            `object`,
            objectShape,
            displayObjectInformation,
            labelOffsetX,
            labelOffsetY
        )
    }

    /**
     * オブジェクトをすべて表示する
     */
    private fun drawObjects(canvas: Canvas, offsetX: Float, offsetY: Float)
    {
        try
        {
            // オブジェクトの描画 （保持しているものはすべて表示する）
            val keys = objectHolder.getObjectKeys()
            if (keys != null)
            {
                while (keys.hasMoreElements()) {
                    val key = keys.nextElement()
                    val pos = objectHolder.getPosition(key)
                    if (pos != null)
                    {
                        drawObject(canvas, pos, false, offsetX, offsetY)
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * タッチされたタイミングでの処理
     *
     */
    private fun onTouchDown(event: MotionEvent): Boolean
    {
        try
        {
            // タッチ位置を記憶する
            downPosX = event.x - drawTransX
            downPosY = event.y - drawTransY

            // タッチ位置をオブジェクト画像の座標に変換する
            val x = downPosX / drawScale
            val y = downPosY / drawScale

            // タッチ位置にオブジェクトが存在するか確認する
            selectedPosition = checkSelectedObject(x, y)
            if (selectedPosition == null)
            {
                // 最初にタップしたときの位置を selectedPositionに設定する
                val data = selectionReceiver.touchedVacantArea()
                if (data == OperationModeHolder.OPERATIONMODE_CREATE) {
                    // オブジェクト作成モードのとき...オブジェクトを生成する
                    selectedPosition = objectHolder.createPosition(x, y, objectStyle)

                    // オブジェクトが生成されたことを通知する
                    selectionReceiver.objectCreated()
                }
                /*
                else if (data ==OperationModeHolder.OPERATIONMODE_MOVE)
                {
                   // 移動モードのとき
                }
                else // if (data ==ChangeDrawMode.OPERATIONMODE_DELETE)
                {
                   // 削除モードのとき...何もしない
                }
                */
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    /**
     * タッチが離されたタイミングでの処理
     */
    private fun onTouchUp(event: MotionEvent): Boolean
    {
        try
        {
            var longPress = false
            if (onGestureProcessed)
            {
                // ロングタッチ中だった場合...フラグを落とす
                onGestureProcessed = false
                longPress = true
            }

            // タッチ位置をオブジェクト画像の座標に変換する
            val x = (event.x - drawTransX) / drawScale
            val y = (event.y - drawTransY) / drawScale

            if (selectedPosition == null)
            {
                val data = selectionReceiver.touchUppedVacantArea()
                if (data == OperationModeHolder.OPERATIONMODE_DELETE)
                {
                    if ((tempPosX == Float.MIN_VALUE) || (tempPosY == Float.MIN_VALUE) || (downPosX == Float.MIN_VALUE) || (downPosY == Float.MIN_VALUE)) {
                        // タッチが２つ揃っていないので、何もしない。
                        Log.v(
                            TAG,
                            "onTouchUp : ($downPosX,$downPosY) [$drawScale] ($tempPosX,$tempPosY) [$drawTransX,$drawTransY]"
                        )
                        return (false)
                    }

                    // タッチが離された位置にオブジェクトがおらず、オブジェクトが非選択だった場合...オブジェクトが繋がっているラインを切断する
                    disconnectObjects(
                        (downPosX / drawScale),
                        (downPosY / drawScale),
                        ((tempPosX - drawTransX) / drawScale),
                        ((tempPosY - drawTransY) / drawScale)
                    )

                    // 移動位置をクリアする
                    tempPosX = Float.MIN_VALUE
                    tempPosY = Float.MIN_VALUE
                    downPosX = Float.MIN_VALUE
                    downPosY = Float.MIN_VALUE
                    return (true)
                }

                // 移動位置をクリアする
                tempPosX = Float.MIN_VALUE
                tempPosY = Float.MIN_VALUE
                downPosX = Float.MIN_VALUE
                downPosY = Float.MIN_VALUE
                return (true)
            }

            val selectedRect = selectedPosition?.getRect()
            if ((selectedRect != null)&&(selectedRect.contains(x, y)))
            {
                //  タッチが離された位置がタッチしたオブジェクトと同じ位置だった場合......

                // タッチが離された位置を認識する
                val diffX =
                    abs((event.x - drawTransX - downPosX).toDouble()).toFloat()
                val diffY =
                    abs((event.y - drawTransY - downPosY).toDouble()).toFloat()

                // タッチが離された位置が動いていた場合、オブジェクト位置の微調整と判定する。
                if (((diffX > 2.0f) || (diffY > 2.0f)) || (longPress)) {
                    // タッチが離された場所にはオブジェクトがなかった場合...オブジェクトをその位置に移動させる
                    Log.v(TAG, "MOVE OBJECT : ($diffX,$diffY)")
                    moveObjectPosition(x, y)
                    return (true)
                }

                //  タッチが押された位置と離された位置が同じ位置だった場合......アイテムが選択された、と認識する。
                Log.v(TAG, " ITEM SELECTED :$x,$y")
                // アイテムが選択されたよ！と教える
                val isDraw = selectionReceiver.objectSelected(selectedPosition?.getKey())

                // 移動位置をクリアする
                tempPosX = Float.MIN_VALUE
                tempPosY = Float.MIN_VALUE
                return (isDraw)
            }

            // タッチが離された位置にオブジェクトがいるかどうかのチェック
            val position = checkSelectedObject(x, y)
            if ((position != null) && (!longPress))
            {
                // 他のオブジェクトと重なるように操作した、この場合は、オブジェクト間を線をつなげる
                // （ただし、ボタンを長押ししていなかったとき。）
                val key = selectedPosition?.getKey()
                if (key != null)
                {
                    lineHolder.setLines(key, position.getKey(), lineStyleHolder)
                    tempPosX = Float.MIN_VALUE
                    tempPosY = Float.MIN_VALUE
                }
                return (true)
            }

            // タッチが離された場所にはオブジェクトがなかった場合...オブジェクトをその位置に移動させる
            moveObjectPosition(x, y)

            /*
            tempPosX = Float.MIN_VALUE;
            tempPosY = Float.MIN_VALUE;
            float positionX = alignPosition(x, (objectSizeX / 2) * (-1));
            float positionY = alignPosition(y, (objectSizeY / 2) * (-1));
            selectedPosition.rect = new  android.graphics.RectF(positionX, positionY, (positionX + objectSizeX), (positionY + objectSizeY));
            // selectedPosition.drawStyle = objectStyle;   // 不要、最初に生成するときだけ必要
            */
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    /**
     * オブジェクトの位置を移動させる
     *
     */
    private fun moveObjectPosition(x: Float, y: Float)
    {
        try
        {
            val curRect = selectedPosition?.getRect()
            if (curRect != null)
            {
                tempPosX = Float.MIN_VALUE
                tempPosY = Float.MIN_VALUE
                val sizeX = curRect.right - curRect.left
                val sizeY = curRect.bottom - curRect.top
                val positionX = alignPosition(x, (sizeX / 2) * (-1))
                val positionY = alignPosition(y, (sizeY / 2) * (-1))
                selectedPosition?.setRect(
                    RectF(positionX, positionY, (positionX + sizeX), (positionY + sizeY))
                )
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * onTouchEvent : 画面がタッチした時のイベント処理
     * (true なら、画面描画を実施する)
     */
    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        var isDraw: Boolean
        try
        {
            // スケールジェスチャ(マルチタッチのジェスチャ)を拾う
            //isDraw = scaleGestureDetector.onTouchEvent(event);
            if ((onScaling) || (scaleGestureDetector.isInProgress)) {
                //  マルチタッチ操作中...
                return (true)
            }

            //  先にジェスチャーを拾ってみよう...
            isDraw = gestureDetector.onTouchEvent(event)
            if (isDraw) {
                Log.v(TAG, "MeMoMaCanvasDrawer::onTouchEvent() : isDraw == true")
                return (true)
            }

            val action = event.action

            //Log.v(Main.APP_IDENTIFIER, "MeMoMaCanvasDrawer::onTouchEvent() : " + action);
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    // タッチされたとき
                    isDraw = onTouchDown(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    // タッチされたまま動かされたときの処理
                    tempPosX = event.x
                    tempPosY = event.y
                    isDraw = true
                }
                MotionEvent.ACTION_UP -> {
                    // タッチが離されたときの処理...
                    isDraw = onTouchUp(event)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            isDraw = true
        }
        return (isDraw)
    }

    /**
     * タテヨコ位置を合わせられるよう、調整する。
     *
     */
    private fun alignPosition(pos: Float, offset: Float): Float
    {
        // 位置を調整する。
        return (pos + offset)
    }

    /**
     * 位置から判定し、選択したオブジェクトを応答する
     * （オブジェクトが選択されていない場合には、nullを応答する）
     */
    private fun checkSelectedObject(x: Float, y: Float): PositionObject?
    {
        try
        {
            val keys = objectHolder.getObjectKeys()
            //Log.v(TAG, "CHECK POS "  + x + "," + y);
            if (keys != null) {
                while (keys.hasMoreElements()) {
                    val key = keys.nextElement()
                    val pos = objectHolder.getPosition(key)
                    if (pos != null) {
                        val posRect = pos.getRect()
                        if (posRect.contains(x, y)) {
                            Log.v(
                                TAG,
                                "SELECTED :" + posRect.centerX() + "," + posRect.centerY() + " KEY :" + key
                            )
                            return (pos)
                        }
                    }
                    //Log.v(TAG, "NOT MATCH :"   + pos.rect.centerX() + "," + pos.rect.centerY());
                }
            }
            //Log.v(TAG, "RETURN NULL...");
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (null)
    }

    /**
     * 線と交差するオブジェクト接続線をすべて削除する
     *
     */
    private fun disconnectObjects(startX: Float, startY: Float, endX: Float, endY: Float) {
        Log.v(
            TAG,
            "MeMoMaCanvasDrawer::disconnectObjects() [$startX,$startY]-[$endX,$endY]"
        )
        try {
            val keys = lineHolder.lineKeys
            while (keys.hasMoreElements()) {
                val key = keys.nextElement()
                val line = lineHolder.getLine(key)
                if (line != null) {
                    if (line.getKey() > 0) {
                        // 線の始点と終点を取り出す
                        val fromRect = objectHolder.getPosition(line.getFromObjectKey())?.getRect()
                        val toRect = objectHolder.getPosition(line.getToObjectKey())?.getRect()
                        if ((fromRect != null)&&(toRect != null)) {
                            // 線が交差しているかチェックする
                            if (checkIntersection(
                                    startX,
                                    startY,
                                    endX,
                                    endY,
                                    fromRect.centerX(),
                                    fromRect.centerY(),
                                    toRect.centerX(),
                                    toRect.centerY()
                                )
                            ) {
                                // 線が交差していた！ 線を切る！
                                //Log.v(TAG, "CUT LINE [" +  from.rect.centerX() + "," +  from.rect.centerY() +"]-[" + to.rect.centerX() + "," + to.rect.centerY() + "]");
                                lineHolder.disconnectLines(line.getKey())
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            // 例外発生、でもなにもしない
        }
    }

    /**
     * 線が交差しているかチェックする
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
    private fun checkIntersection(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        x4: Float,
        y4: Float
    ): Boolean {
        // 線が平行かどうかのチェックを行う
        val denominator = (x2 - x1) * (y4 - y3) - (y2 - y1) * (x4 - x3)
        if (abs(denominator.toDouble()) < 0.00001) {
            // 線が平行と認識、交差しない
            return (false)
        }

        val tempX = x3 - x1
        val tempY = y3 - y1
        val dR = (((y4 - y3) * tempX) - ((x4 - x3) * tempY)) / denominator
        val dS = (((y2 - y1) * tempX) - ((x2 - x1) * tempY)) / denominator

        // ２直線の交点を求める
        //float crossX, crossY;
        //crossX = x1 + dR * (x2 - x1);
        //crossY = y1 + dR * (y2 - y1);

        // 交点が線分内にあるかどうかをチェックする
        return ((dR >= 0) && (dR <= 1) && (dS >= 0) && (dS <= 1))
    }

    /**
     * 並行移動・ズームのサイズをリセットする
     *
     */
    fun resetScaleAndLocation(zoomBar: SeekBar)
    {
        try
        {
            // 並行移動をリセットする
            drawTransX = 0.0f
            drawTransY = 0.0f

            // プログレスバーの位置をリセットする
            drawScale = 1.0f
            zoomBar.progress = 50

            // preferenceに状態を記録する
            recordTranslateAndZoomScale(50)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * スライドバーを変更された時の処理
     */
    private fun zoomScaleChanged(progress: Int)
    {
        try
        {
            val value = (progress.toFloat() - 50.0f) / 50.0f

            // 前の表示領域サイズを取得
            val prevSizeWidth = screenWidth * drawScale
            val prevSizeHeight = screenHeight * drawScale

            //  表示倍率を変更し、倍率を画面に表示する
            drawScale = Math.round(10.0.pow(value.toDouble()) * 10.0).toFloat() / 10.0f
            val textview = parent.findViewById<TextView>(R.id.ZoomRate)
            val showText = "x$drawScale"
            showText.also { textview.text = it }

            // 現在の表示領域サイズを取得
            val showSizeWidth = screenWidth * drawScale
            val showSizeHeight = screenHeight * drawScale

            // 倍率にあわせて並行移動する場所を調整する
            drawTransX += (prevSizeWidth - showSizeWidth) / 2.0f
            drawTransY += (prevSizeHeight - showSizeHeight) / 2.0f

            // preferenceに状態を記録する
            recordTranslateAndZoomScale(progress)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 平行移動状態と倍率の状態を記憶する
     *
     */
    private fun recordTranslateAndZoomScale(progress: Int)
    {
        try
        {
            //Log.v(TAG, "recordTranslateAndZoomScale() : x" + drawScale + " X:" + drawTransX + " Y: " + drawTransY);
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val editor = preferences.edit()
            editor.putFloat("drawScale", drawScale)
            editor.putFloat("drawTransX", drawTransX)
            editor.putFloat("drawTransY", drawTransY)
            editor.putInt("zoomProgress", progress)
            editor.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 平行移動状態と倍率の状態を記憶する
     *
     */
    private fun restoreTranslateAndZoomScale()
    {
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            drawScale = preferences.getFloat("drawScale", 1.0f)
            drawTransX = preferences.getFloat("drawTransX", 0.0f)
            drawTransY = preferences.getFloat("drawTransY", 0.0f)
            Log.v(
                TAG,
                "restoreTranslateAndZoomScale() : x$drawScale X:$drawTransX Y: $drawTransY"
            )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * GestureDetector.OnGestureListener の実装
     */
    override fun onDown(event: MotionEvent): Boolean {
        //Log.v(TAG, "MeMoMaCanvasDrawer::onDown() "  + event.getX()  + "," + event.getY());
        return (false)
    }

    /**
     * GestureDetector.OnGestureListener の実装
     */
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        //Log.v(TAG, "MeMoMaCanvasDrawer::onFling() "  + velocityX  + "," + velocityY);
        return (false)
    }

    /**
     * GestureDetector.OnGestureListener の実装
     */
    override fun onLongPress(event: MotionEvent)
    {
        try
        {
            Log.v(TAG, "MeMoMaCanvasDrawer::onLongPress() " + event.x + "," + event.y)

            // タッチ位置をオブジェクト画像の座標に変換する
            val x = (event.x - drawTransX) / drawScale
            val y = (event.y - drawTransY) / drawScale

            // タッチ位置にオブジェクトが存在するか確認する
            val position = checkSelectedObject(x, y)
            if (position != null) {
                // 長押し処理を実施していることを記憶する
                onGestureProcessed = true
                // タッチした場所にオブジェクトが存在した！！
                selectionReceiver.objectSelectedContext(position.getKey())
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * GestureDetector.OnGestureListener の実装
     */
    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //Log.v(TAG, "MeMoMaCanvasDrawer::onScroll() "  + distanceX  + "," + distanceY);
        return (false)
    }

    /**
     * GestureDetector.OnGestureListener の実装
     */
    override fun onShowPress(event: MotionEvent) {
        //Log.v(TAG, "MeMoMaCanvasDrawer::onShowPress() "  + event.getX()  + "," + event.getY());
    }

    /**
     * GestureDetector.OnGestureListener の実装
     */
    override fun onSingleTapUp(event: MotionEvent): Boolean {
        //Log.v(TAG, "MeMoMaCanvasDrawer::onSingleTapUp() "  + event.getX()  + "," + event.getY());
        return (false)
    }

    /**
     * スライドバーを変更された時の処理
     * (SeekBar.OnSeekBarChangeListener の実装)
     */
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
    {
        try
        {
            // Log.v(TAG, "SeekBar::onProgressChanged() : $progress")

            // 画面描画の倍率を変更する
            zoomScaleChanged(progress)

            // 画面描画クラスに再描画を指示する
            val surfaceView = parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)
            surfaceView.doDraw()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {} // SeekBar.OnSeekBarChangeListener の実装
    override fun onStopTrackingTouch(seekBar: SeekBar) {} // SeekBar.OnSeekBarChangeListener の実装

    /**
     * （ScaleGestureDetector.OnScaleGestureListener の実装）
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean
    {
        try
        {
            val scaleFactor = detector.scaleFactor

            //Log.v(TAG, "MeMoMaCanvasDrawer::onScale() : " + scaleFactor + " (" + currentScaleBar + ")");

            // 画面表示の倍率が変更された！　x < 1 : 縮小、 1 < x : 拡大
            if (scaleFactor < 1.0f) {
                currentScaleBar = if ((currentScaleBar == 0)) 0 else currentScaleBar - 1
            } else if (scaleFactor > 1.0f) {
                currentScaleBar = if ((currentScaleBar == 100)) 100 else currentScaleBar + 1
            }
            zoomScaleChanged(currentScaleBar)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (onScaling)
    }

    /**
     * （ScaleGestureDetector.OnScaleGestureListener の実装）
     */
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean
    {
        //Log.v(TAG, "MeMoMaCanvasDrawer::onScaleBegin() ");
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        currentScaleBar = preferences.getInt("zoomProgress", 50)
        onScaling = true
        return (true)
    }

    /**
     * （ScaleGestureDetector.OnScaleGestureListener の実装）
     */
    override fun onScaleEnd(detector: ScaleGestureDetector)
    {
        try
        {
            //Log.v(TAG, "MeMoMaCanvasDrawer::onScaleEnd() " + currentScaleBar);
            onScaling = false

            // シークバーを設定し、値を記憶する
            val seekbar = parent.findViewById<SeekBar>(R.id.ZoomInOut)
            seekbar.progress = currentScaleBar
            zoomScaleChanged(currentScaleBar)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = MeMoMaCanvasDrawer::class.java.simpleName
        const val OBJECT_LABEL_MARGIN: Float = 8.0f
        const val OBJECT_LABEL_MARGIN_WIDTH: Float = 24.0f
        const val DATA_LABEL_TEXT_SIZE: Int = 30
        const val BACKGROUND_COLOR_DEFAULT: Int = -0xffc000
    }
}
