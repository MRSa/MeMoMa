package jp.sourceforge.gokigen.memoma.holders

import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R

/**
 * 線の形状を保持するクラス
 */
class LineStyleHolder(private val activity: AppCompatActivity)
{
    private var currentLineThickness = LINETHICKNESS_THIN
    private var currentLineShape = LINESHAPE_NORMAL
    private var currentLineStyle = LINESTYLE_STRAIGHT_NO_ARROW

    /**
     * ライン形状を読み出す
     *
     */
    fun prepare()
    {
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            currentLineStyle = (preferences.getString("lineStyle", "0")?:"0").toInt()
            currentLineShape = (preferences.getString("lineShape", "1000")?:"1000").toInt()
            currentLineThickness = (preferences.getString("lineThickness", "1")?:"1").toInt()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun changeLineStyle(): Int
    {
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            var value = (preferences.getString("lineStyle", "0")?:"0").toInt()
            value =
                when (value)
                {
                    LINESTYLE_STRAIGHT_NO_ARROW -> LINESTYLE_STRAIGHT_R_ARROW
                    LINESTYLE_STRAIGHT_R_ARROW -> LINESTYLE_TREESTYLE_NO_ARROW
                    LINESTYLE_TREESTYLE_NO_ARROW -> LINESTYLE_TREESTYLE_R_ARROW
                    LINESTYLE_TREESTYLE_R_ARROW -> LINESTYLE_CURVESTYLE_NO_ARROW
                    LINESTYLE_CURVESTYLE_NO_ARROW -> LINESTYLE_CURVESTYLE_R_ARROW
                    LINESTYLE_CURVESTYLE_R_ARROW -> LINESTYLE_STRAIGHT_NO_ARROW
                    LINESTYLE_STRAIGHT_L_ARROW -> LINESTYLE_TREESTYLE_L_ARROW
                    LINESTYLE_TREESTYLE_L_ARROW -> LINESTYLE_CURVESTYLE_L_ARROW
                    LINESTYLE_CURVESTYLE_L_ARROW -> LINESTYLE_STRAIGHT_NO_ARROW
                    else -> LINESTYLE_STRAIGHT_NO_ARROW
                }
            // 文字列としてデータを記録
            val editor = preferences.edit()
            editor.putString("lineStyle", "" + value)
            editor.apply()
            return (value)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (0)
    }

    fun getLineStyle(): Int
    {
        return (currentLineStyle)
    }

    fun setLineStyle(style: Int)
    {
        try
        {
            currentLineStyle = when (style) {
                LINESTYLE_STRAIGHT_NO_ARROW,
                LINESTYLE_TREESTYLE_NO_ARROW,
                LINESTYLE_CURVESTYLE_NO_ARROW,
                LINESTYLE_STRAIGHT_R_ARROW,
                LINESTYLE_TREESTYLE_R_ARROW,
                LINESTYLE_CURVESTYLE_R_ARROW,
                LINESTYLE_STRAIGHT_L_ARROW,
                LINESTYLE_TREESTYLE_L_ARROW,
                LINESTYLE_CURVESTYLE_L_ARROW -> style
                else -> LINESTYLE_STRAIGHT_NO_ARROW
            }

            // 文字列としてデータを記録
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = preferences.edit()
            editor.putString("lineStyle", "" + currentLineStyle)
            editor.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun getLineShape() : Int
    {
        return (currentLineShape)
    }

    fun setLineShape(shape: Int)
    {
        try
        {
            currentLineShape = when (shape) {
                    LINESHAPE_DASH,
                    LINESHAPE_NORMAL -> shape
                    else -> LINESHAPE_NORMAL
            }
            // 文字列としてデータを記録
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = preferences.edit()
            editor.putString("lineShape", "" + currentLineShape)
            editor.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun getLineThickness(): Int
    {
        return (currentLineThickness)
    }

    fun setLineThickness(thickness : Int)
    {
        try
        {
            currentLineThickness = LINETHICKNESS_THIN
            currentLineThickness =
                when (thickness) {
                    LINETHICKNESS_HEAVY, LINETHICKNESS_MIDDLE, LINETHICKNESS_THIN -> thickness
                    else -> LINETHICKNESS_THIN
                }
            // 文字列としてデータを記録
            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val editor = preferences.edit()
            editor.putString("lineThickness", "" + currentLineThickness)
            editor.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object {
        const val LINESTYLE_STRAIGHT_NO_ARROW: Int = 0
        const val LINESTYLE_TREESTYLE_NO_ARROW: Int = 1
        const val LINESTYLE_CURVESTYLE_NO_ARROW: Int = 2
        const val LINESTYLE_STRAIGHT_R_ARROW: Int = 3
        const val LINESTYLE_STRAIGHT_L_ARROW: Int = 4
        const val LINESTYLE_TREESTYLE_R_ARROW: Int = 5
        const val LINESTYLE_TREESTYLE_L_ARROW: Int = 6
        const val LINESTYLE_CURVESTYLE_R_ARROW: Int = 7
        const val LINESTYLE_CURVESTYLE_L_ARROW: Int = 8

        const val LINESHAPE_NORMAL: Int = 1000 // 普通の直線
        const val LINESHAPE_DASH: Int = 1001 // 点線（破線)

        const val LINETHICKNESS_THIN: Int = 0 // 細い線
        const val LINETHICKNESS_MIDDLE: Int = 3 // 中太線
        const val LINETHICKNESS_HEAVY: Int = 6 //  太線

        fun getLineThicknessImageId(thickness: Int): Int
        {
            return (when (thickness) {
                    LINETHICKNESS_HEAVY -> R.drawable.btn_line_heavy
                    LINETHICKNESS_MIDDLE -> R.drawable.btn_line_middle
                    LINETHICKNESS_THIN -> R.drawable.btn_line_thin
                    else -> R.drawable.btn_line_thin
                })
        }

        fun getLineShapeImageId(currentLineStyle: Int, currentLineShape: Int): Int
        {
            var buttonId = R.drawable.btn_straight
            if ((currentLineStyle == LINESTYLE_TREESTYLE_NO_ARROW) &&
                (currentLineShape == LINESHAPE_NORMAL)
            ) {
                buttonId = R.drawable.btn_tree
            } else if ((currentLineStyle == LINESTYLE_CURVESTYLE_NO_ARROW) &&
                (currentLineShape == LINESHAPE_NORMAL)
            ) {
                buttonId = R.drawable.btn_curve
            } else if ((currentLineStyle == LINESTYLE_STRAIGHT_NO_ARROW) &&
                (currentLineShape == LINESHAPE_DASH)
            ) {
                buttonId = R.drawable.btn_straight_dash
            } else if ((currentLineStyle == LINESTYLE_TREESTYLE_NO_ARROW) &&
                (currentLineShape == LINESHAPE_DASH)
            ) {
                buttonId = R.drawable.btn_tree_dash
            } else if ((currentLineStyle == LINESTYLE_CURVESTYLE_NO_ARROW) &&
                (currentLineShape == LINESHAPE_DASH)
            ) {
                buttonId = R.drawable.btn_curve_dash
            } else if ((currentLineStyle == LINESTYLE_STRAIGHT_R_ARROW) &&
                (currentLineShape == LINESHAPE_NORMAL)
            ) {
                buttonId = R.drawable.btn_straight_rarrow
            } else if ((currentLineStyle == LINESTYLE_TREESTYLE_R_ARROW) &&
                (currentLineShape == LINESHAPE_NORMAL)
            ) {
                buttonId = R.drawable.btn_tree_rarrow
            } else if ((currentLineStyle == LINESTYLE_CURVESTYLE_R_ARROW) &&
                (currentLineShape == LINESHAPE_NORMAL)
            ) {
                buttonId = R.drawable.btn_curve_rarrow
            } else if ((currentLineStyle == LINESTYLE_STRAIGHT_R_ARROW) &&
                (currentLineShape == LINESHAPE_DASH)
            ) {
                buttonId = R.drawable.btn_straight_rarrow_dash
            } else if ((currentLineStyle == LINESTYLE_TREESTYLE_R_ARROW) &&
                (currentLineShape == LINESHAPE_DASH)
            ) {
                buttonId = R.drawable.btn_tree_rarrow_dash
            } else if ((currentLineStyle == LINESTYLE_CURVESTYLE_R_ARROW) &&
                (currentLineShape == LINESHAPE_DASH)
            ) {
                buttonId = R.drawable.btn_curve_rarrow_dash
            }
            return (buttonId)
        }
    }
}