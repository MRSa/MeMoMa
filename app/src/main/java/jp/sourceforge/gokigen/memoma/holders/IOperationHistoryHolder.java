package jp.sourceforge.gokigen.memoma.holders;

interface IOperationHistoryHolder
{
    enum ChangeKind
    {
        RECTANGLE,
        DRAW_STYLE,
        ICON,
        LABEL,
        DETAIL,
        USER_CHECKED,
        LABEL_COLOR,
        OBJECT_COLOR,
        PAINT_STYLE,
        STROKE_WIDTH,
        FONT_SIZE,
        NEW_OBJECT,
        DELETE_OBJECT,
        NEW_CONNECT_LINE,
        DELETE_CONNECT_LINE,
        CONNECT_LINE_FROM_KEY,
        CONNECT_LINE_TO_KEY,
        CONNECT_LINE_STYLE,
        CONNECT_LINE_SHAPE,
        CONNECT_LINE_THICKNESS,
    };


    void addHistory(int key, ChangeKind kind, Object object);
    void reset();
    void undo();

}
