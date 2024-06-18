package jp.sourceforge.gokigen.memoma.holders

import android.graphics.RectF
import android.util.Log
import jp.sourceforge.gokigen.memoma.holders.IOperationHistoryHolder.ChangeKind

class OperationHistoryHolder(private val objectHolder: MeMoMaObjectHolder) : IOperationHistoryHolder
{
    private var previousRect: RectF? = null
    private var previousKey = -1

    override fun addHistory(key: Int, kind: ChangeKind?, `object`: Any?)
    {
        try
        {
            if (kind == ChangeKind.RECTANGLE)
            {
                // オブジェクトが移動したとき、１つだけ記録する
                previousKey = key
                previousRect = `object` as RectF?
            }
            else
            {
                previousKey = -1
                previousRect = null
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun reset()
    {
        Log.v(TAG, "Histry Reset() ")
        previousKey = -1
        previousRect = null
    }

    override fun undo(): Boolean
    {
        var ret = false
        Log.v(TAG, "undo() ")
        try
        {
            val pos = objectHolder.getPosition(previousKey)
            if ((previousRect != null)&&(pos != null))
            {
                // 移動したオブジェクトを戻す
                pos.setRect(previousRect!!)

                // undo を実行したら、履歴を消す
                previousKey = -1
                previousRect = null
                ret = true
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (ret)
    }

    override fun isHistoryExist(): Boolean
    {
        return (previousKey != -1)
    }

    companion object
    {
        private val TAG = OperationHistoryHolder::class.java.simpleName
    }
}