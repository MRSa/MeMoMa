package jp.sourceforge.gokigen.memoma.operations

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.holders.PositionObject

/**
 * オブジェクトのデータを入力するダイアログを表示する
 */
class ObjectDataInputDialog(private val context: Context, private val objectHolder: MeMoMaObjectHolder) :
    OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener
{
    private lateinit var resultReceiver: IResultReceiver

    private var key = 0
    private var dialogLayout: View? = null
    private var colorBorderAreaView: TextView? = null
    private var borderColorView: SeekBar? = null
    private var fillObjectView: CheckBox? = null
    private var backgroundShape: GradientDrawable? = null
    private var backgroundColor = MeMoMaCanvasDrawer.BACKGROUND_COLOR_DEFAULT
    private var currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE
    private var textFontSize = 6.0f

    fun setResultReceiver(receiver: IResultReceiver)
    {
        resultReceiver = receiver
    }

    fun getDialog(): AlertDialog
    {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.objectinput, null)
        dialogLayout = layout

        val builder = AlertDialog.Builder(context)
        // 背景色を設定する
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val colorString = preferences.getString("backgroundColor", "#ff004000")
            backgroundColor = Color.parseColor(colorString)
        }
        catch (e: Exception)
        {
            backgroundColor = MeMoMaCanvasDrawer.BACKGROUND_COLOR_DEFAULT
        }

        val colorLabel = layout.findViewById<TextView>(R.id.setBorderColorLabel)
        backgroundShape = colorLabel.background as GradientDrawable

        // 入力文字列の色を設定する
        val label = layout.findViewById<View>(R.id.labelInputArea) as EditText
        val detail = layout.findViewById<View>(R.id.descriptionInputArea) as EditText

        borderColorView = layout.findViewById(R.id.borderColorSelectionBar)
        borderColorView?.setOnSeekBarChangeListener(this)

        val userCheckbox = layout.findViewById<CheckBox>(R.id.checkUserCheckbox)
        val boldText = layout.findViewById<CheckBox>(R.id.checkBoldText)
        fillObjectView = layout.findViewById(R.id.checkFillObject)
        fillObjectView?.setOnCheckedChangeListener(this)

        colorBorderAreaView = layout.findViewById(R.id.borderColorArea)
        colorBorderAreaView?.setOnClickListener(this)

        val rect = layout.findViewById<ImageButton>(R.id.btnObjectRectangle)
        rect.setOnClickListener(this)
        val roundRect = layout.findViewById<ImageButton>(R.id.btnObjectRoundRect)
        roundRect.setOnClickListener(this)
        val oval = layout.findViewById<ImageButton>(R.id.btnObjectOval)
        oval.setOnClickListener(this)
        val diamond = layout.findViewById<ImageButton>(R.id.btnObjectDiamond)
        diamond.setOnClickListener(this)
        val hexagonal = layout.findViewById<ImageButton>(R.id.btnObjectHexagonal)
        hexagonal.setOnClickListener(this)
        val parallelogram = layout.findViewById<ImageButton>(R.id.btnObjectParallelogram)
        parallelogram.setOnClickListener(this)
        val keyboard = layout.findViewById<ImageButton>(R.id.btnObjectKeyboard)
        keyboard.setOnClickListener(this)
        val paper = layout.findViewById<ImageButton>(R.id.btnObjectPaper)
        paper.setOnClickListener(this)
        val drum = layout.findViewById<ImageButton>(R.id.btnObjectDrum)
        drum.setOnClickListener(this)
        val circle = layout.findViewById<ImageButton>(R.id.btnObjectCircle)
        circle.setOnClickListener(this)
        val noregion = layout.findViewById<ImageButton>(R.id.btnObjectNoRegion)
        noregion.setOnClickListener(this)
        val loopStart = layout.findViewById<ImageButton>(R.id.btnObjectLoopStart)
        loopStart.setOnClickListener(this)
        val loopEnd = layout.findViewById<ImageButton>(R.id.btnObjectLoopEnd)
        loopEnd.setOnClickListener(this)
        val leftArrow = layout.findViewById<ImageButton>(R.id.btnObjectLeftArrow)
        leftArrow.setOnClickListener(this)
        val downArrow = layout.findViewById<ImageButton>(R.id.btnObjectDownArrow)
        downArrow.setOnClickListener(this)
        val upArrow = layout.findViewById<ImageButton>(R.id.btnObjectUpArrow)
        upArrow.setOnClickListener(this)
        val rightArrow = layout.findViewById<ImageButton>(R.id.btnObjectRightArrow)
        rightArrow.setOnClickListener(this)

        // 背景の色を調整（塗りつぶしの時はオブジェクトの色とする。）
        val targetColor : Int = borderColorView?.progress ?: 0x004000
        var color = convertColor(targetColor)
        colorBorderAreaView?.setBackgroundColor(if ((fillObjectView?.isChecked == true)) color else backgroundColor)

        if (fillObjectView?.isChecked == true)
        {
            // 塗りつぶし時は文字の色を変える。
            color = (color xor 0x00ffffff)
        }
        colorBorderAreaView?.setTextColor(color)
        colorBorderAreaView?.text = context.getString(R.string.labelTextColorSample)

        builder.setView(layout)
        builder.setCancelable(false)
        builder.setPositiveButton(
            context.getString(R.string.confirmYes)
        ) { dialog, _ ->
            try {
                var isUserCheck = false
                if (userCheckbox != null) {
                    isUserCheck = userCheckbox.isChecked
                }
                setObjectData(
                    label.text.toString(),
                    detail.text.toString(),
                    borderColorView?.progress ?: 0,
                    boldText.isChecked,
                    fillObjectView?.isChecked ?: false,
                    isUserCheck,
                    currentObjectDrawStyle
                )
                resultReceiver.finishObjectInput()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            dialog.dismiss()
            System.gc()
        }
        builder.setNegativeButton(
            context.getString(R.string.confirmNo)
        ) { dialog, _ ->
            try
            {
                resultReceiver.cancelObjectInput()
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            dialog.cancel()
            System.gc()
        }
        return (builder.create())
    }

    /**
     * オブジェクト入力用ダイアログの表示を準備する
     */
    fun prepareObjectInputDialog(dialog: AlertDialog, objectKey: Int)
    {
        try
        {
            val position = objectHolder.getPosition(objectKey)
            key = objectKey
            if (position != null)
            {
                // 色を設定する
                val borderColorProgress = dialog.findViewById<SeekBar>(R.id.borderColorSelectionBar)
                borderColorProgress?.progress = convertProgress(position.getObjectColor())

                val boldTextCheck = dialog.findViewById<CheckBox>(R.id.checkBoldText)
                boldTextCheck?.isChecked =
                    position.getstrokeWidth() == MeMoMaObjectHolder.STROKE_BOLD_WIDTH

                val fillObjectCheck = dialog.findViewById<CheckBox>(R.id.checkFillObject)
                fillObjectCheck?.isChecked =
                    Paint.Style.valueOf(position.getPaintStyle()) != Paint.Style.STROKE

                // フォントサイズを設定する
                textFontSize = position.getFontSize() / 2.0f

                // 入力文字列を設定する
                val targetLabel = dialog.findViewById<EditText>(R.id.labelInputArea)
                targetLabel?.setText(position.getLabel())

                val targetDetail = dialog.findViewById<EditText>(R.id.descriptionInputArea)
                targetDetail?.setText(position.getDetail())

                //  設定に記録されているデータを画面に反映させる
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val userCheckboxTitle = preferences.getString("userCheckboxString", "")

                // 描画スタイルを設定する
                currentObjectDrawStyle = position.getDrawStyle()
                updateObjectDrawStyleImageButton(currentObjectDrawStyle)

                // 背景色を設定する
                try {
                    val colorString = preferences.getString("backgroundColor", "#ff004000")
                    backgroundColor = Color.parseColor(colorString)
                } catch (e: Exception) {
                    backgroundColor = MeMoMaCanvasDrawer.BACKGROUND_COLOR_DEFAULT
                }
                setTextColorSample(
                    borderColorProgress?.progress?: 0,
                    textFontSize,
                    fillObjectView?.isChecked?: false
                )
                val userCheckbox = dialog.findViewById<CheckBox>(R.id.checkUserCheckbox)
                userCheckbox?.isEnabled = true
                userCheckbox?.text = userCheckboxTitle
                userCheckbox?.isChecked = position.getUserChecked()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトにデータを設定する
     *
     */
    private fun setObjectData(
        label: String,
        detail: String,
        progress: Int,
        boldText: Boolean,
        fillObject: Boolean,
        userCheck: Boolean,
        drawStyle: Int
    )
    {
        try
        {
            val positionObject = objectHolder.getPosition(key)
            if (positionObject != null) {
                positionObject.setLabel(label)
                positionObject.setDetail(detail)
                var color = convertColor(progress)
                positionObject.setObjectColor(color)
                color = (color xor 0x00ffffff)
                positionObject.setFontSize(textFontSize * 2.0f)
                positionObject.setLabelColor(color)
                positionObject.setStrokeWidth(if ((boldText)) MeMoMaObjectHolder.STROKE_BOLD_WIDTH else MeMoMaObjectHolder.STROKE_NORMAL_WIDTH)
                positionObject.setPaintStyle(
                    (if ((fillObject)) Paint.Style.FILL else Paint.Style.STROKE).toString())
                positionObject.setUserChecked(userCheck)

                val posDrawStyle = positionObject.getDrawStyle()
                if (posDrawStyle != drawStyle) {
                    if ((drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE) ||
                        (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW) ||
                        (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW) ||
                        (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW) ||
                        (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW)
                    ) {
                        // (長方形の形状から)正方形の形状にする場合...
                        setRectToSquare(positionObject)
                    } else if ((posDrawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE) ||
                        (posDrawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW) ||
                        (posDrawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW) ||
                        (posDrawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW) ||
                        (posDrawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW)
                    ) {
                        // 正方形の形状から、長方形の形状にする場合...
                        setRectFromSquare(positionObject)
                    }
                    positionObject.setDrawStyle(drawStyle)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトの領域を長方形から正方形にする
     */
    private fun setRectToSquare(positionObject: PositionObject)
    {
        try
        {
            val posRect = positionObject.getRect()
            val bandWidth = ((posRect.right - posRect.left)) / 2.0f
            val center = posRect.centerY()

            positionObject.setRectTop(center - bandWidth)
            positionObject.setRectBottom(center + bandWidth)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトの領域を正方形から長方形にする
     */
    private fun setRectFromSquare(positionObject: PositionObject)
    {
        try
        {
            val posRect = positionObject.getRect()
            val bandWidth = ((posRect.right - posRect.left) / 16.0f * 9.0f) / 2.0f
            val center = posRect.centerY()

            positionObject.setRectTop(center - bandWidth)
            positionObject.setRectBottom(center + bandWidth)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setButtonBorder(id: Int, isHighlight: Boolean)
    {
        try
        {
            val button = dialogLayout?.findViewById<ImageButton>(id)
            val btnBackgroundShape = button?.background as BitmapDrawable
            if (isHighlight)
            {
                btnBackgroundShape.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN)
            }
            else
            {
                btnBackgroundShape.setColorFilter(Color.BLACK, PorterDuff.Mode.LIGHTEN)
            }
        }
        catch (ex: Exception)
        {
            Log.v(TAG, "setButtonBorder(): $ex")
        }
    }

    /**
     * イメージボタンの選択状態を更新する
     */
    private fun updateObjectDrawStyleImageButton(drawStyle: Int)
    {
        try
        {
            setButtonBorder(
                R.id.btnObjectRectangle,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE)
            )
            setButtonBorder(
                R.id.btnObjectRoundRect,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT)
            )
            setButtonBorder(R.id.btnObjectOval, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_OVAL))
            setButtonBorder(R.id.btnObjectDiamond, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DIAMOND))
            setButtonBorder(
                R.id.btnObjectHexagonal,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL)
            )
            setButtonBorder(
                R.id.btnObjectParallelogram,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM)
            )
            setButtonBorder(
                R.id.btnObjectKeyboard,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD)
            )
            setButtonBorder(R.id.btnObjectPaper, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PAPER))
            setButtonBorder(R.id.btnObjectDrum, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DRUM))
            setButtonBorder(R.id.btnObjectCircle, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE))
            setButtonBorder(
                R.id.btnObjectNoRegion,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_NO_REGION)
            )
            setButtonBorder(
                R.id.btnObjectLoopStart,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_START)
            )
            setButtonBorder(R.id.btnObjectLoopEnd, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_END))
            setButtonBorder(
                R.id.btnObjectLeftArrow,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW)
            )
            setButtonBorder(
                R.id.btnObjectDownArrow,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW)
            )
            setButtonBorder(R.id.btnObjectUpArrow, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW))
            setButtonBorder(
                R.id.btnObjectRightArrow,
                (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW)
            )
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ボタンが押された時の処理...
     */
    override fun onClick(v: View)
    {
        try
        {
            val id = v.id
            if (id == R.id.borderColorArea) {
                // フォントサイズを変更する...
                this.textFontSize = when (this.textFontSize) {
                    FONT_SIZE_MIDDLE -> {
                        FONT_SIZE_LARGE
                    }
                    FONT_SIZE_LARGE -> {
                        FONT_SIZE_SMALL
                    }
                    else // if (textFontSize == FONTSIZE_SMALL)
                    -> {
                        FONT_SIZE_MIDDLE
                    }
                }
                setTextColorSample(borderColorView!!.progress, textFontSize, fillObjectView!!.isChecked)
                return
            }
            currentObjectDrawStyle = when (id) {
                R.id.btnObjectRoundRect -> MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT
                R.id.btnObjectOval -> MeMoMaObjectHolder.DRAWSTYLE_OVAL
                R.id.btnObjectDiamond -> MeMoMaObjectHolder.DRAWSTYLE_DIAMOND
                R.id.btnObjectHexagonal -> MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL
                R.id.btnObjectParallelogram -> MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM
                R.id.btnObjectKeyboard -> MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD
                R.id.btnObjectPaper -> MeMoMaObjectHolder.DRAWSTYLE_PAPER
                R.id.btnObjectDrum -> MeMoMaObjectHolder.DRAWSTYLE_DRUM
                R.id.btnObjectNoRegion -> MeMoMaObjectHolder.DRAWSTYLE_NO_REGION
                R.id.btnObjectCircle -> MeMoMaObjectHolder.DRAWSTYLE_CIRCLE
                R.id.btnObjectLoopStart -> MeMoMaObjectHolder.DRAWSTYLE_LOOP_START
                R.id.btnObjectLoopEnd -> MeMoMaObjectHolder.DRAWSTYLE_LOOP_END
                R.id.btnObjectLeftArrow -> MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW
                R.id.btnObjectDownArrow -> MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW
                R.id.btnObjectUpArrow -> MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW
                R.id.btnObjectRightArrow -> MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW
                R.id.btnObjectRectangle -> MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE
                else -> MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE
            }
            updateObjectDrawStyleImageButton(currentObjectDrawStyle)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 0x00～0x3fの値(R, G, B, それぞれ2ビット)で色を変える
     */
    private fun convertColor(value: Int): Int
    {
        val color: Int
        val r = ((value shr 4) and 0x03) * 85
        val g = ((value shr 2) and 0x03) * 85
        val b = (value and 0x03) * 85
        color = Color.rgb(r, g, b)
        return (color)
    }

    /**
     * 色をプログレスバーの値に変換する
     */
    private fun convertProgress(color: Int): Int
    {
        val r = Color.red(color) / 85
        val g = Color.green(color) / 85
        val b = Color.blue(color) / 85
        return ((r shl 4) + (g shl 2) + b)
    }

    /**
     * 背景色を設定する処理
     */
    private fun setTextColorSample(progress: Int, fontSize: Float, value: Boolean)
    {
        try
        {
            if (colorBorderAreaView != null)
            {
                var color = convertColor(progress)
                val backColor = if ((value)) color else backgroundColor
                colorBorderAreaView?.setBackgroundColor(backColor)
                backgroundShape?.setStroke(2, color)
                if (value)
                {
                    // 塗りつぶし時には色を変える
                    color = (color xor 0x00ffffff)
                }
                colorBorderAreaView?.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize)
                colorBorderAreaView?.setTextColor(color)
                colorBorderAreaView?.text = context.getString(R.string.labelTextColorSample)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * チェックボックスの値が変更された時の処理
     */
    override fun onCheckedChanged(view: CompoundButton, value: Boolean)
    {
        try
        {
            val id = view.id
            if ((id == R.id.checkFillObject) && (borderColorView != null))
            {
                setTextColorSample(borderColorView!!.progress, textFontSize, value)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * プログレスバーで値を変更された時の処理
     */
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean)
    {
        try
        {
            val id = seekBar.id
            if ((id == R.id.borderColorSelectionBar) && (fillObjectView != null))
            {
                setTextColorSample(progress, textFontSize, fillObjectView?.isChecked?: false)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) { }
    override fun onStopTrackingTouch(seekBar: SeekBar) { }

    interface IResultReceiver
    {
        fun finishObjectInput()
        fun cancelObjectInput()
    }

    companion object
    {
        private val TAG = ObjectDataInputDialog::class.java.simpleName

        private const val FONT_SIZE_SMALL = 5.0f
        private const val FONT_SIZE_MIDDLE = 8.0f
        private const val FONT_SIZE_LARGE = 12.0f
    }
}