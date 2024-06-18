package jp.sourceforge.gokigen.memoma.holders

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.widget.Toast
import jp.sourceforge.gokigen.memoma.R
import java.util.Enumeration
import java.util.Hashtable

/**
 * 表示オブジェクトの情報を保持するクラス
 */
class MeMoMaObjectHolder(private val parent: Context)
{
    private val connectLineHolder: MeMoMaConnectLineHolder
    private val objectPoints: Hashtable<Int, PositionObject>?
    private var serialNumber = 1
    private var dataTitle: String = ""
    fun getDataTitle() : String
    {
        return (dataTitle)
    }
    fun setDataTitle(value: String)
    {
        dataTitle = value
    }

    private var background: String = ""
    fun getBackground(): String
    {
        return (background)
    }
    fun setBackground(value: String)
    {
        background = value
    }

    private val historyHolder: IOperationHistoryHolder = OperationHistoryHolder(this)

    init
    {
        connectLineHolder = MeMoMaConnectLineHolder(historyHolder)
        objectPoints = Hashtable()
    }

    fun isEmpty(): Boolean
    {
        return (((objectPoints == null)) || (objectPoints.isEmpty))
    }

    fun isHistoryExist(): Boolean
    {
        return (historyHolder.isHistoryExist())
    }

    /**
     * 「ひとつ戻す」処理
     */
    fun undo(): Boolean
    {
        return (historyHolder.undo())
    }

    fun getConnectLineHolder(): MeMoMaConnectLineHolder
    {
        return (connectLineHolder)
    }

    fun getCount(): Int
    {
        return (objectPoints?.size?:0)
    }

    fun getObjectKeys(): Enumeration<Int>?
    {
        if (objectPoints == null)
        {
            return (null)
        }
        return (objectPoints.keys())
    }

    fun getPosition(key: Int): PositionObject?
    {
        if (objectPoints == null)
        {
            return (null)
        }
        return (objectPoints[key])
    }

    fun removePosition(key: Int): Boolean
    {
        val removeTarget = objectPoints?.remove(key)
        if (removeTarget != null)
        {
            historyHolder.addHistory(
                key,
                IOperationHistoryHolder.ChangeKind.DELETE_OBJECT,
                removeTarget
            )
        }
        Log.v(TAG, "REMOVE : $key")
        return (true)
    }

    fun removeAllPositions()
    {
        objectPoints?.clear()
        serialNumber = 1

        // 操作履歴をクリアする
        historyHolder.reset()
    }

    fun setSerialNumber(id: Int)
    {
        serialNumber = if ((id == ID_NOTSPECIFY)) ++serialNumber else id
    }

    fun getSerialNumber(): Int
    {
        return (serialNumber)
    }

    /**
     * オブジェクトを共有する
     */
    fun shareObject(key: Int) {
        Log.v(TAG, " shareObject $key")
        if (objectPoints == null)
        {
            return
        }
        val targetPosition = objectPoints[key]
        val title = targetPosition?.getLabel()
        val detail = targetPosition?.getDetail()

        try
        {
            val intent = Intent()
            intent.setAction(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TITLE, title)
            intent.putExtra(Intent.EXTRA_SUBJECT, title)
            intent.putExtra(Intent.EXTRA_TEXT, detail)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            parent.startActivity(intent)
            Log.v(TAG, "<<< SEND INTENT >>> : $title")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトを複製する。
     */
    fun duplicatePosition(key: Int): PositionObject?
    {
        if (objectPoints == null)
        {
            // 元のオブジェクトが見つからなかったので、何もせずに戻る
            return (null)
        }
        val orgPosition = objectPoints[key] ?: return (null)
        val orgRect = orgPosition.getRect()
        val position = PositionObject(
            serialNumber,
            RectF(
                orgRect.left + DUPLICATEPOSITION_MARGIN,
                orgRect.top + DUPLICATEPOSITION_MARGIN,
                orgRect.right + DUPLICATEPOSITION_MARGIN,
                orgRect.bottom + DUPLICATEPOSITION_MARGIN
            ),
            orgPosition.getDrawStyle(),
            orgPosition.getIcon(),
            orgPosition.getLabel(),
            orgPosition.getDetail(),
            orgPosition.getUserChecked(),
            orgPosition.getLabelColor(),
            orgPosition.getObjectColor(),
            orgPosition.getPaintStyle(),
            orgPosition.getstrokeWidth(),
            orgPosition.getFontSize(),
            historyHolder
        )
        objectPoints[serialNumber] = position
        serialNumber++
        return (position)
    }

    fun createPosition(id: Int): PositionObject
    {
        val position = PositionObject(
            id,
            RectF(0f, 0f, OBJECTSIZE_DEFAULT_X, OBJECTSIZE_DEFAULT_Y),
            DRAWSTYLE_RECTANGLE,
            0,
            "",
            "",
            false,
            Color.WHITE,
            Color.WHITE,
            Paint.Style.STROKE.toString(),
            STROKE_NORMAL_WIDTH,
            FONTSIZE_DEFAULT,
            historyHolder
        )
        objectPoints!![id] = position
        return (position)
    }

    fun createPosition(x: Float, y: Float, drawStyle: Int): PositionObject
    {
        val position = createPosition(serialNumber)
        val posRect = position.getRect()
        position.setRectLeft(posRect.left + x)
        position.setRectRight(posRect.right + x)
        position.setRectTop(posRect.top + y)
        position.setRectBottom(posRect.bottom + y)
        position.setDrawStyle(drawStyle)
        serialNumber++
        return (position)
    }

    /**
     * オブジェクトのサイズを拡大する
     *
     */
    fun expandObjectSize(key: Int)
    {
        if (objectPoints == null)
        {
            // 元のオブジェクトが見つからなかったので、何もせずに戻る
            return
        }
        val position = objectPoints[key] ?: return
        val posRect = position.getRect()
        val width = posRect.right - posRect.left
        val height = posRect.bottom - posRect.top
        if (((width + (OBJECTSIZE_STEP_X * 2.0f)) > OBJECTSIZE_MAXIMUM_X) || ((height + (OBJECTSIZE_STEP_Y * 2.0f)) > OBJECTSIZE_MAXIMUM_Y)) {
            // 拡大リミットだった。。。拡大しない
            val outputMessage = parent.getString(R.string.object_bigger_limit) + " "
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
            return
        }
        position.setRectLeft(posRect.left - OBJECTSIZE_STEP_X)
        position.setRectRight(posRect.right + OBJECTSIZE_STEP_X)
        position.setRectTop(posRect.top - OBJECTSIZE_STEP_Y)
        position.setRectBottom(posRect.bottom + OBJECTSIZE_STEP_Y)
    }

    /**
     * オブジェクトのサイズを縮小する
     */
    fun shrinkObjectSize(key: Int)
    {
        if (objectPoints == null)
        {
            // 元のオブジェクトが見つからなかったので、何もせずに戻る
            return
        }
        val position = objectPoints[key] ?: return
        val posRect = position.getRect()
        val width = posRect.right - posRect.left
        val height = posRect.bottom - posRect.top
        if (((width - (OBJECTSIZE_STEP_X * 2.0f)) < OBJECTSIZE_MINIMUM_X) || ((height - (OBJECTSIZE_STEP_Y * 2.0f)) < OBJECTSIZE_MINIMUM_Y))
        {
            // 縮小リミットだった。。。縮小しない
            val outputMessage = parent.getString(R.string.object_small_limit) + " "
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
            return
        }
        position.setRectLeft(posRect.left + OBJECTSIZE_STEP_X)
        position.setRectRight(posRect.right - OBJECTSIZE_STEP_X)
        position.setRectTop(posRect.top + OBJECTSIZE_STEP_Y)
        position.setRectBottom(posRect.bottom - OBJECTSIZE_STEP_Y)
    }

    companion object
    {
        private val TAG = MeMoMaObjectHolder::class.java.simpleName

        const val ID_NOTSPECIFY: Int = -1
        const val DRAWSTYLE_RECTANGLE: Int = 0
        const val DRAWSTYLE_ROUNDRECT: Int = 1
        const val DRAWSTYLE_OVAL: Int = 2
        const val DRAWSTYLE_DIAMOND: Int = 3
        const val DRAWSTYLE_HEXAGONAL: Int = 4
        const val DRAWSTYLE_PARALLELOGRAM: Int = 5
        const val DRAWSTYLE_KEYBOARD: Int = 6
        const val DRAWSTYLE_PAPER: Int = 7
        const val DRAWSTYLE_DRUM: Int = 8
        const val DRAWSTYLE_CIRCLE: Int = 9
        const val DRAWSTYLE_NO_REGION: Int = 10

        const val DRAWSTYLE_LOOP_START: Int = 11
        const val DRAWSTYLE_LOOP_END: Int = 12
        const val DRAWSTYLE_LEFT_ARROW: Int = 13
        const val DRAWSTYLE_DOWN_ARROW: Int = 14
        const val DRAWSTYLE_UP_ARROW: Int = 15
        const val DRAWSTYLE_RIGHT_ARROW: Int = 16

        const val ROUNDRECT_CORNER_RX: Float = 8f
        const val ROUNDRECT_CORNER_RY: Float = 8f

        const val STROKE_BOLD_WIDTH: Float = 3.5f
        const val STROKE_NORMAL_WIDTH: Float = 0.0f

        const val DUPLICATEPOSITION_MARGIN: Float = 15.0f

        const val OBJECTSIZE_DEFAULT_X: Float = 198.0f
        const val OBJECTSIZE_DEFAULT_Y: Float = (OBJECTSIZE_DEFAULT_X / 16.0f * 10.0f)

        const val OBJECTSIZE_MINIMUM_X: Float = 90.0f
        const val OBJECTSIZE_MINIMUM_Y: Float = (OBJECTSIZE_MINIMUM_X / 16.0f * 10.0f)

        const val OBJECTSIZE_MAXIMUM_X: Float = 25600.0f
        const val OBJECTSIZE_MAXIMUM_Y: Float = (OBJECTSIZE_MAXIMUM_X / 16.0f * 10.0f)

        const val OBJECTSIZE_STEP_X: Float = OBJECTSIZE_MINIMUM_X
        const val OBJECTSIZE_STEP_Y: Float = OBJECTSIZE_MINIMUM_Y

        const val FONTSIZE_DEFAULT: Float = 20.0f

        fun getObjectDrawStyleIcon(drawStyle: Int): Int {
            var icon = 0
            when (drawStyle) {
                DRAWSTYLE_RECTANGLE -> {
                    icon = R.drawable.btn_rectangle
                }
                DRAWSTYLE_ROUNDRECT -> {
                    icon = R.drawable.btn_roundrect
                }
                DRAWSTYLE_OVAL -> {
                    icon = R.drawable.btn_oval
                }
                DRAWSTYLE_DIAMOND -> {
                    icon = R.drawable.btn_diamond
                }
                DRAWSTYLE_HEXAGONAL -> {
                    icon = R.drawable.btn_hexagonal
                }
                DRAWSTYLE_PARALLELOGRAM -> {
                    icon = R.drawable.btn_parallelogram
                }
                DRAWSTYLE_KEYBOARD -> {
                    icon = R.drawable.btn_keyboard
                }
                DRAWSTYLE_PAPER -> {
                    icon = R.drawable.btn_paper
                }
                DRAWSTYLE_DRUM -> {
                    icon = R.drawable.btn_drum
                }
                DRAWSTYLE_CIRCLE -> {
                    icon = R.drawable.btn_circle
                }
                DRAWSTYLE_NO_REGION -> {
                    icon = R.drawable.btn_noregion
                }
                DRAWSTYLE_LOOP_START -> {
                    icon = R.drawable.btn_trapezoidy_up
                }
                DRAWSTYLE_LOOP_END -> {
                    icon = R.drawable.btn_trapezoidy_down
                }
                DRAWSTYLE_LEFT_ARROW -> {
                    icon = R.drawable.btn_arrow_left
                }
                DRAWSTYLE_DOWN_ARROW -> {
                    icon = R.drawable.btn_arrow_down
                }
                DRAWSTYLE_UP_ARROW -> {
                    icon = R.drawable.btn_arrow_up
                }
                DRAWSTYLE_RIGHT_ARROW -> {
                    icon = R.drawable.btn_arrow_right
                }
            }
            return (icon)
        }
    }
}