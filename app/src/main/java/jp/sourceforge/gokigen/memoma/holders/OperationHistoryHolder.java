package jp.sourceforge.gokigen.memoma.holders;

import android.content.Context;
import android.util.Log;

public class OperationHistoryHolder implements IOperationHistoryHolder
{
    private final String TAG = toString();

    public OperationHistoryHolder()
    {

    }

    public void addHistory(int key, ChangeKind kind, Object object)
    {
        Log.v(TAG, "addHistory() KEY : " + key + " KIND : " + kind.toString() + " OBJ : " + object.toString());
    }

    public void undo()
    {
        Log.v(TAG, "undo() ");

    }
}
