package jp.sourceforge.gokigen.memoma.operations

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.LineStyleHolder

/**
 * 接続線の形状を選択するダイアログを表示する
 */
class SelectLineShapeDialog(private val parent: Context, private val lineStyleHolder: LineStyleHolder) : View.OnClickListener
{
    private var lineThickness = LineStyleHolder.LINETHICKNESS_THIN
    private var lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW
    private var lineShape = LineStyleHolder.LINESHAPE_NORMAL
    private lateinit var resultReceiver: IResultReceiver

    private var dialogLayout: View? = null

    fun setResultReceiver(receiver: IResultReceiver)
    {
        resultReceiver = receiver
    }

    fun getDialog(): AlertDialog
    {
            val inflater =
                parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.lineselection, null)
            dialogLayout = layout

            val builder = AlertDialog.Builder(parent)

            //  ダイアログで表示するデータを設定する場所
            lineShape = lineStyleHolder.getLineShape()
            lineStyle = lineStyleHolder.getLineStyle()
            lineThickness = lineStyleHolder.getLineThickness()

            // 線の太さ
            val thin = layout.findViewById<View>(R.id.btnLineThicknessThin) as ImageButton
            thin.setOnClickListener(this)
            val middle = layout.findViewById<View>(R.id.btnLineThicknessMiddle) as ImageButton
            middle.setOnClickListener(this)
            val heavy = layout.findViewById<View>(R.id.btnLineThicknessHeavy) as ImageButton
            heavy.setOnClickListener(this)

            // 線の形状
            val straight = layout.findViewById<View>(R.id.btnLineShapeStraight) as ImageButton
            straight.setOnClickListener(this)
            val tree = layout.findViewById<View>(R.id.btnLineShapeTree) as ImageButton
            tree.setOnClickListener(this)
            val curve = layout.findViewById<View>(R.id.btnLineShapeCurve) as ImageButton
            curve.setOnClickListener(this)
            val straightDash =
                layout.findViewById<View>(R.id.btnLineShapeStraightDash) as ImageButton
            straightDash.setOnClickListener(this)
            val treeDash = layout.findViewById<View>(R.id.btnLineShapeTreeDash) as ImageButton
            treeDash.setOnClickListener(this)
            val curveDash = layout.findViewById<View>(R.id.btnLineShapeCurveDash) as ImageButton
            curveDash.setOnClickListener(this)
            val straightRarrow =
                layout.findViewById<View>(R.id.btnLineShapeStraightRarrow) as ImageButton
            straightRarrow.setOnClickListener(this)
            val treeRarrow = layout.findViewById<View>(R.id.btnLineShapeTreeRarrow) as ImageButton
            treeRarrow.setOnClickListener(this)
            val curveRarrow = layout.findViewById<View>(R.id.btnLineShapeCurveRarrow) as ImageButton
            curveRarrow.setOnClickListener(this)
            val straightRarrowDash =
                layout.findViewById<View>(R.id.btnLineShapeStraightRarrowDash) as ImageButton
            straightRarrowDash.setOnClickListener(this)
            val treeRarrowDash =
                layout.findViewById<View>(R.id.btnLineShapeTreeRarrowDash) as ImageButton
            treeRarrowDash.setOnClickListener(this)
            val curveRarrowDash =
                layout.findViewById<View>(R.id.btnLineShapeCurveRarrowDash) as ImageButton
            curveRarrowDash.setOnClickListener(this)

            builder.setView(layout)
            builder.setTitle(parent.getString(R.string.Title_SelectLineShape))
            builder.setCancelable(false)
            builder.setPositiveButton(
                parent.getString(R.string.confirmYes)
            ) { dialog, _ ->
                val ret = false
                try
                {
                    setLineShape(lineStyle, lineShape, lineThickness)
                    resultReceiver.finishSelectLineShape(lineStyle, lineShape, lineThickness)
                    updateButtonHighlightLineThickness(0)
                    updateButtonHighlightLineShape(0)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }

                if (ret) {
                    dialog.dismiss()
                } else {
                    dialog.cancel()
                }
                System.gc()
            }
            builder.setNegativeButton(
                parent.getString(R.string.confirmNo)
            ) { dialog, _ ->
                val ret = false
                resultReceiver.cancelSelectLineShape()
                updateButtonHighlightLineThickness(0)
                updateButtonHighlightLineShape(0)
                if (ret) {
                    dialog.dismiss()
                } else {
                    dialog.cancel()
                }
                System.gc()
            }
            return (builder.create())
        }

    /**
     * オブジェクト入力用ダイアログの表示を準備する
     * （ダイアログの表示をした時に呼ばれる）
     */
    fun prepareSelectLineShapeDialog(dialog: AlertDialog, objectKey: Int?)
    {
        try
        {
            // 現在の線の形状と種類を取得する
            lineShape = lineStyleHolder.getLineShape()
            lineStyle = lineStyleHolder.getLineStyle()
            lineThickness = lineStyleHolder.getLineThickness()

            // 画面（ダイアログ）で、現在選択中のものをハイライトにする。
            updateButtonHighlightLineThickness(getLineThicknessButtonId(lineThickness))
            updateButtonHighlightLineShape(getLineShapeButtonId(lineStyle, lineShape))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 線の形状を設定する
     */
    private fun setLineShape(toSetLineStyle: Int, toSetLineShape: Int, toSetLineThickness: Int) {
        lineStyleHolder.setLineShape(toSetLineShape)
        lineStyleHolder.setLineStyle(toSetLineStyle)
        lineStyleHolder.setLineThickness(toSetLineThickness)
        Log.v(
            TAG,
            ":::CHANGE LINE :::  shape:$toSetLineShape style:$toSetLineStyle thickness:$toSetLineThickness"
        )
    }

    /**
     *
     */
    private fun setButtonBorder(id: Int, judge: Int) {
        try {
            val button = dialogLayout?.findViewById<View>(id) as ImageButton
            val btnBackgroundShape = button.background as BitmapDrawable
            if (id == judge) {
                btnBackgroundShape.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN)
            } else {
                btnBackgroundShape.setColorFilter(Color.BLACK, PorterDuff.Mode.LIGHTEN)
            }
        } catch (ex: Exception) {
            //
            Log.v(TAG, "setButtonBorder(): $ex")
        }
    }

    /**
     * イメージボタンの選択状態を更新する (接続線の太さ)
     *
     * @param buttonId
     */
    private fun updateLineThickness(buttonId: Int) {
        lineThickness = when (buttonId) {
            R.id.btnLineThicknessMiddle -> LineStyleHolder.LINETHICKNESS_MIDDLE
            R.id.btnLineThicknessHeavy -> LineStyleHolder.LINETHICKNESS_HEAVY
            R.id.btnLineThicknessThin -> LineStyleHolder.LINETHICKNESS_THIN
            else -> LineStyleHolder.LINETHICKNESS_THIN
        }
    }

    /**
     * 線の形状の選択状態を記憶（更新）する
     *
     * @param buttonId
     */
    private fun updateLineStyle(buttonId: Int) {
        when (buttonId) {
            R.id.btnLineShapeTree -> {
                lineStyle = LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }

            R.id.btnLineShapeCurve -> {
                lineStyle = LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }

            R.id.btnLineShapeStraightDash -> {
                lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_DASH
            }

            R.id.btnLineShapeTreeDash -> {
                lineStyle = LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_DASH
            }

            R.id.btnLineShapeCurveDash -> {
                lineStyle = LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_DASH
            }

            R.id.btnLineShapeStraightRarrow -> {
                lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }

            R.id.btnLineShapeTreeRarrow -> {
                lineStyle = LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }

            R.id.btnLineShapeCurveRarrow -> {
                lineStyle = LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }

            R.id.btnLineShapeStraightRarrowDash -> {
                lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW
                lineShape = LineStyleHolder.LINESHAPE_DASH
            }

            R.id.btnLineShapeTreeRarrowDash -> {
                lineStyle = LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW
                lineShape = LineStyleHolder.LINESHAPE_DASH
            }

            R.id.btnLineShapeCurveRarrowDash -> {
                lineStyle = LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW
                lineShape = LineStyleHolder.LINESHAPE_DASH
            }

            R.id.btnLineShapeStraight -> {
                lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }

            else -> {
                lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW
                lineShape = LineStyleHolder.LINESHAPE_NORMAL
            }
        }
    }

    /**
     * 現在の太さを設定する
     *
     * @param thickness
     * @return
     */
    private fun getLineThicknessButtonId(thickness: Int): Int
    {
        val buttonId = when (thickness)
        {
            LineStyleHolder.LINETHICKNESS_HEAVY -> R.id.btnLineThicknessHeavy
            LineStyleHolder.LINETHICKNESS_MIDDLE -> R.id.btnLineThicknessMiddle
            LineStyleHolder.LINETHICKNESS_THIN -> R.id.btnLineThicknessThin
            else -> R.id.btnLineThicknessThin
        }
        return (buttonId)
    }

    /**
     *
     *
     * @param currentLineStyle
     * @param currentLineShape
     * @return
     */
    private fun getLineShapeButtonId(currentLineStyle: Int, currentLineShape: Int): Int {
        var buttonId = R.id.btnLineShapeStraight

        if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_NORMAL)
        ) {
            buttonId = R.id.btnLineShapeTree
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_NORMAL)
        ) {
            buttonId = R.id.btnLineShapeCurve
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_DASH)
        ) {
            buttonId = R.id.btnLineShapeStraightDash
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_DASH)
        ) {
            buttonId = R.id.btnLineShapeTreeDash
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_DASH)
        ) {
            buttonId = R.id.btnLineShapeCurveDash
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_NORMAL)
        ) {
            buttonId = R.id.btnLineShapeStraightRarrow
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_NORMAL)
        ) {
            buttonId = R.id.btnLineShapeTreeRarrow
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_NORMAL)
        ) {
            buttonId = R.id.btnLineShapeCurveRarrow
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_DASH)
        ) {
            buttonId = R.id.btnLineShapeStraightRarrowDash
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_DASH)
        ) {
            buttonId = R.id.btnLineShapeTreeRarrowDash
        } else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW) &&
            (currentLineShape == LineStyleHolder.LINESHAPE_DASH)
        ) {
            buttonId = R.id.btnLineShapeCurveRarrowDash
        }
        /**
         * else  if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW)&&
         * (currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
         * {
         * buttonId = R.id.btnLineShapeStraight;
         * }
         */
        return (buttonId)
    }

    /**
     *
     *
     * @param id
     */
    private fun updateButtonHighlightLineThickness(id: Int) {
        setButtonBorder(R.id.btnLineThicknessThin, id)
        setButtonBorder(R.id.btnLineThicknessMiddle, id)
        setButtonBorder(R.id.btnLineThicknessHeavy, id)
    }

    /**
     *
     *
     * @param id
     */
    private fun updateButtonHighlightLineShape(id: Int) {
        setButtonBorder(R.id.btnLineShapeStraight, id)
        setButtonBorder(R.id.btnLineShapeTree, id)
        setButtonBorder(R.id.btnLineShapeCurve, id)

        setButtonBorder(R.id.btnLineShapeStraightDash, id)
        setButtonBorder(R.id.btnLineShapeTreeDash, id)
        setButtonBorder(R.id.btnLineShapeCurveDash, id)

        setButtonBorder(R.id.btnLineShapeStraightRarrow, id)
        setButtonBorder(R.id.btnLineShapeTreeRarrow, id)
        setButtonBorder(R.id.btnLineShapeCurveRarrow, id)

        setButtonBorder(R.id.btnLineShapeStraightRarrowDash, id)
        setButtonBorder(R.id.btnLineShapeTreeRarrowDash, id)
        setButtonBorder(R.id.btnLineShapeCurveRarrowDash, id)
    }

    /**
     * ボタンが押された時の処理...
     *
     */
    override fun onClick(v: View) {
        val id = v.id

        // 押されたボタンが接続線の太さだった場合...
        if ((id == R.id.btnLineThicknessThin) || (id == R.id.btnLineThicknessMiddle) || (id == R.id.btnLineThicknessHeavy)) {
            updateButtonHighlightLineThickness(id)
            updateLineThickness(id)
            return
        }

        // 線の形状を更新した場合...
        updateButtonHighlightLineShape(id)
        updateLineStyle(id)
    }

    interface IResultReceiver {
        fun finishSelectLineShape(style: Int, shape: Int, thickness: Int)
        fun cancelSelectLineShape()
    }

    companion object {
        private val TAG = SelectLineShapeDialog::class.java.simpleName
    }
}