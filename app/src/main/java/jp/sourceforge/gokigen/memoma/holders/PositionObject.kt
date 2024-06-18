package jp.sourceforge.gokigen.memoma.holders

import android.graphics.RectF

class PositionObject(
    private val key: Int,
    private var rect: RectF,
    private var drawStyle: Int,
    private var icon: Int,
    private var label: String,
    private var detail: String,
    private var userChecked: Boolean,
    private var labelColor: Int,
    private var objectColor: Int,
    private var paintStyle: String,
    private var strokeWidth: Float,
    private var fontSize: Float,
    private val historyHolder: IOperationHistoryHolder
) {

    /**
     * コンストラクタ (キーを設定する)
     */
    init {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.NEW_OBJECT, this)
    }

    /**
     * オブジェクトのキーを取得する
     *
     */
    fun getKey(): Int {
        return (key)
    }

    fun setRect(rectF: RectF) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect)
        rect = rectF
    }

    fun setRectTop(value: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect)
        rect.top = value
    }

    fun setRectLeft(value: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect)
        rect.left = value
    }

    fun setRectRight(value: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect)
        rect.right = value
    }

    fun setRectBottom(value: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect)
        rect.bottom = value
    }

    fun setDrawStyle(value: Int) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.DRAW_STYLE, drawStyle)
        drawStyle = value
    }

    fun setIcon(value: Int) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.ICON, icon)
        icon = value
    }

    fun setLabel(value: String) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.LABEL, label)
        label = value
    }

    fun setDetail(value: String) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.DETAIL, detail)
        detail = value
    }

    fun setUserChecked(value: Boolean) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.USER_CHECKED, userChecked)
        userChecked = value
    }

    fun setLabelColor(value: Int) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.LABEL_COLOR, labelColor)
        labelColor = value
    }

    fun setObjectColor(value: Int) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.OBJECT_COLOR, objectColor)
        objectColor = value
    }

    fun setPaintStyle(value: String) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.PAINT_STYLE, paintStyle)
        paintStyle = value
    }

    fun setStrokeWidth(value: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.STROKE_WIDTH, strokeWidth)
        strokeWidth = value
    }

    fun setFontSize(value: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.FONT_SIZE, fontSize)
        fontSize = value
    }

    fun setRectOffsetTo(newLeft: Float, newTop: Float) {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect)
        rect.offsetTo(newLeft, newTop)
    }

    fun getRect(): RectF {
        return (rect)
    }

    fun getDrawStyle(): Int {
        return (drawStyle)
    }

    fun getIcon(): Int {
        return (icon)
    }

    fun getLabel(): String {
        return (label)
    }

    fun getDetail(): String {
        return (detail)
    }

    fun getUserChecked(): Boolean {
        return (userChecked)
    }

    fun getLabelColor(): Int {
        return (labelColor)
    }

    fun getObjectColor(): Int {
        return (objectColor)
    }

    fun getPaintStyle(): String {
        return (paintStyle)
    }

    fun getstrokeWidth(): Float {
        return (strokeWidth)
    }

    fun getFontSize(): Float {
        return (fontSize)
    }
}