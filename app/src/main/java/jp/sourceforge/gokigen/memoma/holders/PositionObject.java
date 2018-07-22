package jp.sourceforge.gokigen.memoma.holders;

import android.graphics.RectF;
import android.support.annotation.NonNull;

public class PositionObject
{
    private final Integer key;            // オブジェクト識別子 (変更不可）
    private final IOperationHistoryHolder historyHolder;  // 履歴を保持するところ

    private RectF rect;                    // オブジェクトの大きさ
    private int drawStyle;               // オブジェクトの形状

    private int icon;                     // オブジェクトのアイコン
    private String label;                 // オブジェクトの表示ラベル
    private String detail;                // オブジェクトの説明
    //public String backgroundUri;         // オブジェクトの背景画像
    //public String otherInfoUri;          // 補足（写真とかへのURI）
    //public String objectStatus;          // オブジェクトの状態
    private boolean userChecked;        // ユーザチェックボックス

    private int labelColor;              // オブジェクト内に表示する色
    private int objectColor;             // オブジェクトの色
    private String paintStyle;           // オブジェクトの表示方法 （枠線のみ、塗りつぶし、塗りつぶしと枠線）
    private float strokeWidth;          // 枠線の太さ
    private float fontSize;             // フォントサイズ

    /**
     *    コンストラクタ (キーを設定する)
     *
     */
    public PositionObject(int id, RectF rect, int drawStyle, int icon, String label, String detail, boolean userChecked, int labelColor, int objectColor, String paintStyle, float strokeWidth, float fontSize, @NonNull IOperationHistoryHolder historyHolder)
    {
        key = id;
        this.rect = rect;
        this.drawStyle = drawStyle;
        this.icon = icon;
        this.label = label;
        this.detail = detail;
        this.userChecked = userChecked;
        this.labelColor = labelColor;
        this.objectColor = objectColor;
        this.paintStyle = paintStyle;
        this.strokeWidth = strokeWidth;
        this.fontSize = fontSize;
        this.historyHolder = historyHolder;
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.NEW_OBJECT, this);
    }

    /**
     *    オブジェクトのキーを取得する
     *
     */
    public Integer getKey()
    {
        return (key);
    }

    public void setRect(RectF rectF)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect);
        rect = rectF;
    }

    public void setRectTop(float value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect);
        rect.top = value;
    }

    public void setRectLeft(float value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect);
        rect.left = value;
    }

    public void setRectRight(float value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect);
        rect.right = value;
    }

    public void setRectBottom(float value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect);
        rect.bottom = value;
    }

    public void setDrawStyle(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.DRAW_STYLE, drawStyle);
        drawStyle = value;
    }

    public void setIcon(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.ICON, icon);
        icon = value;
    }

    public void setLabel(String value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.LABEL, label);
        label = value;
    }

    public void setDetail(String value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.DETAIL, detail);
        detail = value;
    }

    public void setUserChecked(boolean value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.USER_CHECKED, userChecked);
        userChecked = value;
    }

    public void setLabelColor(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.LABEL_COLOR, labelColor);
        labelColor = value;
    }

    public void setObjectColor(int value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.OBJECT_COLOR, objectColor);
        objectColor = value;
    }

    public void setPaintStyle(String value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.PAINT_STYLE, paintStyle);
        paintStyle = value;
    }

    public void setStrokeWidth(float value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.STROKE_WIDTH, strokeWidth);
        strokeWidth = value;
    }

    public void setFontSize(float value)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.FONT_SIZE, fontSize);
        fontSize = value;
    }

    public void setRectOffsetTo(float newLeft, float newTop)
    {
        historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.RECTANGLE, rect);
        rect.offsetTo(newLeft, newTop);
    }

    public RectF getRect()
    {
        return (rect);
    }

    public int getDrawStyle()
    {
        return (drawStyle);
    }

    public int getIcon()
    {
        return (icon);
    }

    public String getLabel()
    {
        return (label);
    }

    public String getDetail()
    {
        return (detail);
    }

    public boolean getUserChecked()
    {
        return (userChecked);
    }

    public int getLabelColor()
    {
        return (labelColor);
    }

    public int getObjectColor()
    {
        return (objectColor);
    }

    public String getPaintStyle()
    {
        return (paintStyle);
    }

    public float getstrokeWidth()
    {
        return (strokeWidth);
    }

    public float getFontSize()
    {
        return (fontSize);
    }
}
