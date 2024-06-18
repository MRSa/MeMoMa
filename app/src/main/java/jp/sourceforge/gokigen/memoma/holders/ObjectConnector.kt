package jp.sourceforge.gokigen.memoma.holders

/**
 * オブジェクト間を接続するクラス
 *
 * @author MRSa
 */
class ObjectConnector(
    private val key: Int,
    private var fromObjectKey: Int,
    private var toObjectKey: Int,
    private var lineStyle: Int,
    private var lineShape: Int,
    private var lineThickness: Int,
    private val historyHolder: IOperationHistoryHolder
) {
    init {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.NEW_CONNECT_LINE, this)
    }

    fun getKey(): Int {
        return (key)
    }

    fun getFromObjectKey(): Int {
        return (fromObjectKey)
    }

    fun getToObjectKey(): Int {
        return (toObjectKey)
    }

    fun getLineStyle(): Int {
        return (lineStyle)
    }

    fun getLineShape(): Int {
        return (lineShape)
    }

    fun getLineThickness(): Int {
        return (lineThickness)
    }

    fun setFromObjectKey(value: Int) {
        historyHolder.addHistory(
            key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_FROM_KEY,
            fromObjectKey
        )
        fromObjectKey = value
    }

    fun setToObjectKey(value: Int) {
        historyHolder.addHistory(
            key,
            IOperationHistoryHolder.ChangeKind.CONNECT_LINE_TO_KEY,
            toObjectKey
        )
        toObjectKey = value
    }

    fun setLineStyle(value: Int) {
        historyHolder.addHistory(
            key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_STYLE,
            lineStyle
        )
        lineStyle = value
    }

    fun setLineShape(value: Int) {
        historyHolder.addHistory(
            key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_SHAPE,
            lineShape
        )
        lineShape = value
    }

    fun setLineThickness(value: Int) {
        historyHolder.addHistory(
            key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_THICKNESS,
            lineThickness
        )
        lineThickness = value
    }
}