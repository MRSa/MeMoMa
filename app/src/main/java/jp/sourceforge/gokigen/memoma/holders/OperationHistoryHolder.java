package jp.sourceforge.gokigen.memoma.holders;

import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;

public class OperationHistoryHolder implements IOperationHistoryHolder
{
    private final String TAG = toString();
    private final MeMoMaObjectHolder objectHolder;

    private RectF previousRect = null;
    private int previousKey = -1;

    public OperationHistoryHolder(@NonNull MeMoMaObjectHolder objectHolder)
    {
        this.objectHolder = objectHolder;
    }

    @Override
    public void addHistory(int key, ChangeKind kind, Object object)
    {
        Log.v(TAG, "addHistory() KEY : " + key + " KIND : " + kind.toString() + " OBJ : " + object.toString());

        try
        {
            if (kind == ChangeKind.RECTANGLE)
            {
                // オブジェクトが移動したとき、１つだけ記録する
                previousKey = key;
                previousRect = (RectF) object;
                Log.v(TAG, " id : " + previousKey + "(" + previousRect.left + "," + previousRect.top + ")-(" + previousRect.right + "," + previousRect.bottom + ")");
            }
            else
            {
                previousKey = -1;
                previousRect = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void reset()
    {
        Log.v(TAG, "Histry Reset() ");
        previousKey = -1;
        previousRect = null;
    }

    @Override
    public boolean undo()
    {
        boolean ret = false;
        Log.v(TAG, "undo() ");
        try
        {
            PositionObject pos = objectHolder.getPosition(previousKey);
            if (pos != null)
            {
                // 移動したオブジェクトを戻す
                pos.setRect(previousRect);

                // undo を実行したら、履歴を消す
                previousKey = -1;
                previousRect = null;
                ret = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (ret);
    }

    @Override
    public boolean isHistoryExist()
    {
        return (previousKey != -1);
    }
}
