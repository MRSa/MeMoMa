package jp.sourceforge.gokigen.memoma.holders;

import androidx.annotation.NonNull;

/**
 *   オブジェクト間を接続するクラス
 *
 * @author MRSa
 *
 */
public class ObjectConnector
{
    private final IOperationHistoryHolder historyHolder;
    private final Integer key;
    private Integer fromObjectKey;
    private Integer toObjectKey;
    private Integer lineStyle;
    private Integer lineShape;
    private Integer lineThickness;

    ObjectConnector(int key, int fromObjectKey, int toObjectkey, int lineStyle, int lineShape, int lineThickness, @NonNull IOperationHistoryHolder historyHolder)
    {
        this.key = key;
        this.fromObjectKey = fromObjectKey;
        this.toObjectKey = toObjectkey;
        this.lineStyle = lineStyle;
        this.lineShape = lineShape;
        this.lineThickness = lineThickness;
        this.historyHolder = historyHolder;
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.NEW_CONNECT_LINE, this);
    }

    public int getKey()
    {
        return (key);
    }

    public int getFromObjectKey()
    {
        return (fromObjectKey);
    }

    public int getToObjectKey()
    {
        return (toObjectKey);
    }

    public int getLineStyle()
    {
        return (lineStyle);
    }

    public int getLineShape()
    {
        return (lineShape);
    }

    public int getLineThickness()
    {
        return (lineThickness);
    }

    public void setFromObjectKey(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_FROM_KEY, fromObjectKey);
        fromObjectKey = value;
    }

    public void setToObjectKey(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_TO_KEY, toObjectKey);
        toObjectKey = value;
    }

    public void setLineStyle(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_STYLE, lineStyle);
        lineStyle = value;
    }

    public void setLineShape(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_SHAPE, lineShape);
        lineShape = value;
    }

    public void setLineThickness(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.CONNECT_LINE_THICKNESS, lineThickness);
        lineThickness = value;
    }
}
