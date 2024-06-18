package jp.sourceforge.gokigen.memoma.drawers

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.holders.PositionObject
import kotlin.math.atan2

/**
 * めもまのオブジェクト・ライン・ラベルを描画するメソッド群
 * (現在のところ、MeMoMaCanvasDrawerクラスから追い出してきただけ...)
 *
 * @author MRSa
 */
object ObjectShapeDrawer {
    fun drawObjectOval(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 楕円形の描画
        canvas.drawOval(objectShape, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2)
    }

    fun drawObjectRect(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 四角形を描画する
        canvas.drawRect(objectShape, paint)
        return (0.0f)
    }

    fun drawObjectRoundRect(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 丸角四角形の描画
        canvas.drawRoundRect(
            objectShape,
            MeMoMaObjectHolder.ROUNDRECT_CORNER_RX,
            MeMoMaObjectHolder.ROUNDRECT_CORNER_RY,
            paint
        )
        return (0.0f)
    }

    fun drawObjectDiamond(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 菱形の描画
        val path = Path()
        path.moveTo(objectShape.centerX(), objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY())
        path.lineTo(objectShape.centerX(), objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY())
        path.lineTo(objectShape.centerX(), objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2)
    }

    fun drawObjectKeyboard(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 台形(キーボード型)の描画
        val path = Path()
        path.moveTo(objectShape.left, objectShape.centerY() - MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN)
        path.lineTo(objectShape.left, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY() - MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN)
    }

    fun drawObjectParallelogram(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 平行四辺形の描画
        val path = Path()
        path.moveTo(objectShape.left + MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN, objectShape.top)
        path.lineTo(objectShape.left, objectShape.bottom)
        path.lineTo(objectShape.right - MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.top)
        path.lineTo(objectShape.left + MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN, objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectHexagonal(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 六角形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 2
        path.moveTo(objectShape.left + margin, objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY())
        path.lineTo(objectShape.left + margin, objectShape.bottom)
        path.lineTo(objectShape.right - margin, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY())
        path.lineTo(objectShape.right - margin, objectShape.top)
        path.lineTo(objectShape.left + margin, objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectPaper(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 書類の形の描画
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 3.0f
        val path = Path()
        path.moveTo(objectShape.left, objectShape.top)
        path.lineTo(objectShape.left, objectShape.bottom - margin)
        path.cubicTo(
            (objectShape.left + objectShape.centerX()) / 2.0f,
            objectShape.bottom,
            (objectShape.right + objectShape.centerX()) / 2.0f,
            objectShape.bottom - margin,
            objectShape.right,
            objectShape.bottom - margin
        )
        path.lineTo(objectShape.right, objectShape.top)
        path.lineTo(objectShape.left, objectShape.top)
        canvas.drawPath(path, paint)
        return (-MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN)
    }

    fun drawObjectDrum(
        canvas: Canvas,
        objectShape: RectF,
        paint: Paint,
        paintStyle: Paint.Style
    ): Float {
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN
        // 円柱の描画
        val path = Path()
        path.moveTo(objectShape.left, objectShape.top)
        path.arcTo(
            RectF(
                objectShape.left,
                objectShape.top,
                objectShape.right,
                objectShape.top + margin
            ), 180.0f, 359.999f, true
        )
        path.lineTo(objectShape.left, objectShape.bottom - (margin / 2.0f))
        path.arcTo(
            RectF(
                objectShape.left,
                objectShape.bottom - margin,
                objectShape.right,
                objectShape.bottom
            ), 180.0f, -180.0f, true
        )
        path.lineTo(objectShape.right, objectShape.top + (margin / 2.0f))
        if (paintStyle != Paint.Style.STROKE) {
            // 塗りつぶし用に線の領域を追加する
            path.arcTo(
                RectF(
                    objectShape.left,
                    objectShape.top,
                    objectShape.right,
                    objectShape.top + margin
                ), 180.0f, 180.0f, true
            )
            path.lineTo(objectShape.left, objectShape.bottom - (margin / 2.0f))
            path.arcTo(
                RectF(
                    objectShape.left,
                    objectShape.bottom - margin,
                    objectShape.right,
                    objectShape.bottom
                ), 180.0f, -180.0f, true
            )
        }
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectCircle(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 円を描画する
        canvas.drawCircle(
            objectShape.centerX(),
            objectShape.centerY(),
            ((objectShape.right - objectShape.left) / 2.0f),
            paint
        )
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectNoRegion(canvas: Canvas, objectShape: RectF, paint: Paint) {
        // 何も表示しないとわからないので、ラベルが無いときには枠を表示する
        paint.color = Color.DKGRAY
        canvas.drawRect(objectShape, paint)
        paint.color = Color.WHITE
    }

    fun drawObjectLoopStart(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // ループ開始図形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 2
        path.moveTo(objectShape.left + margin, objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY())
        path.lineTo(objectShape.left, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY())
        path.lineTo(objectShape.right - margin, objectShape.top)
        path.lineTo(objectShape.left + margin, objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectLoopEnd(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // ループ終了図形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 2
        path.moveTo(objectShape.left, objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY())
        path.lineTo(objectShape.left + margin, objectShape.bottom)
        path.lineTo(objectShape.right - margin, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY())
        path.lineTo(objectShape.right, objectShape.top)
        path.lineTo(objectShape.left, objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectLeftArrow(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 左側矢印図形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 4.0f
        path.moveTo(objectShape.left + margin, objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY())
        path.lineTo(objectShape.left + margin, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.top)
        path.lineTo(objectShape.left + margin, objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectDownArrow(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 下側矢印図形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 2
        path.moveTo(objectShape.left, objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY() + margin)
        path.lineTo(objectShape.centerX(), objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY() + margin)
        path.lineTo(objectShape.right, objectShape.top)
        path.lineTo(objectShape.left, objectShape.top)
        canvas.drawPath(path, paint)
        return (-MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN)
    }

    fun drawObjectUpArrow(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 上側矢印図形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 2.0f
        path.moveTo(objectShape.centerX(), objectShape.top)
        path.lineTo(objectShape.left, objectShape.centerY() - margin)
        path.lineTo(objectShape.left, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY() - margin)
        path.lineTo(objectShape.centerX(), objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    fun drawObjectRightArrow(canvas: Canvas, objectShape: RectF, paint: Paint): Float {
        // 右側矢印図形の描画
        val path = Path()
        val margin = MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 4.0f
        path.moveTo(objectShape.left, objectShape.top)
        path.lineTo(objectShape.left, objectShape.bottom)
        path.lineTo(objectShape.right - margin, objectShape.bottom)
        path.lineTo(objectShape.right, objectShape.centerY())
        path.lineTo(objectShape.right - margin, objectShape.top)
        path.lineTo(objectShape.left, objectShape.top)
        canvas.drawPath(path, paint)
        return (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN / 2.0f)
    }

    /**
     * 矢印を描画する処理 (ツリー表示時)
     *
     */
    fun drawArrowTree(
        canvas: Canvas,
        paint: Paint,
        x1: Float,
        y1: Float,
        checkValue: Float,
        isXaxis: Boolean
    ) {
        val margin = 8.0f
        val direction: Float
        if (isXaxis) {
            direction = if ((checkValue < x1)) -1.0f else 1.0f
            canvas.drawLine(
                x1, y1, (x1 + direction * margin), (y1 - margin),
                paint
            )
            canvas.drawLine(
                x1, y1, (x1 + direction * margin), (y1 + margin),
                paint
            )
        } else {
            direction = if ((checkValue < y1)) -1.0f else 1.0f
            canvas.drawLine(
                x1, y1, (x1 - margin), (y1 + direction * margin),
                paint
            )
            canvas.drawLine(
                x1, y1, (x1 + margin), (y1 + direction * margin),
                paint
            )
        }
    }

    /**
     * 矢印を描画する処理
     *
     */
    fun drawArrow(canvas: Canvas, paint: Paint, x1: Float, y1: Float, x2: Float, y2: Float) {
        // 矢印線の長さ
        val moveX = 14.0f

        // 接続線の傾きが、どれくらいの角度で入っているか？
        val centerDegree =
            (atan2((y2 - y1).toDouble(), (x2 - x1).toDouble()) * 180.0 / Math.PI).toFloat()

        // x1, y1 に x2, y2 方向から来た線に合わせた矢印を描画する
        // (2本、１本づつ引いて、それを回転行列で回転させている)

        // 回転行列の準備
        val matrix1 = Matrix()
        matrix1.setRotate((centerDegree + 30), x1, y1)
        val matrix2 = Matrix()
        matrix2.setRotate((centerDegree - 30), x1, y1)

        // 線分を引いた後、回転行列で回転させる
        val pathLine1 = Path()
        pathLine1.moveTo(x1, y1)
        pathLine1.lineTo(x1 + moveX, y1)
        pathLine1.transform(matrix1)
        canvas.drawPath(pathLine1, paint)

        val pathLine2 = Path()
        pathLine2.moveTo(x1, y1)
        pathLine2.lineTo(x1 + moveX, y1)
        pathLine2.transform(matrix2)
        canvas.drawPath(pathLine2, paint)
    }

    /**
     * オブジェクトのラベルを表示する
     *
     */
    fun drawTextLabel(
        canvas: Canvas,
        paint: Paint,
        pos: PositionObject,
        region: RectF,
        displayObjectInformation: Int,
        offsetX: Float,
        offsetY: Float
    ) {
        // タイトルの先頭部分を表示する場合...
        var labelToShow = pos.getLabel()
        if (displayObjectInformation == 0) {
            val width = region.width() - MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN_WIDTH
            val textLen = paint.breakText(pos.getLabel(), true, width, null) // 省略文字を追加するから、そのぶん減らす
            labelToShow = labelToShow.substring(0, textLen)
            if (labelToShow != pos.getLabel()) {
                // truncate した場合には、省略文字を出す。
                labelToShow = "$labelToShow..."
            }
        }

        if (Paint.Style.valueOf(pos.getPaintStyle()) != Paint.Style.STROKE) {
            // オブジェクトを塗りつぶすのときは、文字の色を設定する
            paint.color = pos.getLabelColor()
        }

        // 文字をちょっと影付きにする
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.setShadowLayer(0.7f, 0.7f, 0.7f, Color.DKGRAY)

        // ユーザチェックの描画
        if (pos.getUserChecked()) {
            canvas.drawText(
                "*",
                region.centerX(),
                region.top + (MeMoMaCanvasDrawer.OBJECT_LABEL_MARGIN * 2.0f),
                paint
            )
        }

        // 強調表示
        if (pos.getstrokeWidth() != 0.0f) {
            // そのまま表示すると、読めないので、太さを調整し、アンダーラインを引くことにする
            paint.strokeWidth = 0.0f
            paint.isSubpixelText = true
            paint.isUnderlineText = true
        }

        if (displayObjectInformation == 0) {
            // １行分しか表示しない場合...そのまま表示して終了する
            canvas.drawText(
                labelToShow,
                (region.left + offsetX),
                (region.centerY() + offsetY), paint
            )
            return
        }

        val tall = paint.fontMetrics.top + 1.0f
        val posX = (region.left + offsetX)
        var posY: Float = (region.centerY() + offsetY)
        val width = region.right - region.left - 12.0f // 幅

        var startChar = 0
        val endChar = pos.getLabel().length
        do {
            val textLen = paint.breakText(pos.getLabel(), startChar, endChar, true, width, null)
            canvas.drawText(labelToShow, startChar, (startChar + textLen), posX, posY, paint)

            posY -= tall
            startChar += textLen
        } while (startChar < endChar)
    }
}