package jp.sourceforge.gokigen.memoma.holders

import android.util.Log
import java.util.Enumeration
import java.util.Hashtable

/**
 * 表示オブジェクト間の接続情報を保持するクラス
 */
class MeMoMaConnectLineHolder(private val historyHolder: IOperationHistoryHolder)
{
    private val connectLines = Hashtable<Int, ObjectConnector>()
    private var serialNumber = 1

    init
    {
        connectLines.clear()
    }

    val lineKeys: Enumeration<Int>
        get() = (connectLines.keys())

    fun getLine(key: Int): ObjectConnector?
    {
        return (connectLines[key])
    }

    fun disconnectLines(key: Int): Boolean
    {
        try
        {
            val removeTarget = connectLines.remove(key)
            if (removeTarget != null)
            {
                historyHolder.addHistory(
                    key,
                    IOperationHistoryHolder.ChangeKind.DELETE_CONNECT_LINE,
                    removeTarget
                )
            }
            Log.v(TAG, "DISCONNECT LINES : $key")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    fun setSerialNumber(id: Int)
    {
        serialNumber = if ((id == ID_NOTSPECIFY)) ++serialNumber else id
    }

    fun getSerialNumber(): Int
    {
        return (serialNumber)
    }

    fun removeAllLines()
    {
        connectLines.clear()
        serialNumber = 1
    }

    fun dumpConnectLine(conn: ObjectConnector?)
    {
        if (conn == null) {
            return
        }
        Log.v(
            TAG,
            "LINE " + conn.getKey() + " [" + conn.getFromObjectKey() + " -> " + conn.getToObjectKey() + "] "
        )
    }

    fun removeAllConnection(keyToRemove: Int)
    {
        try
        {
            val keys = connectLines.keys()
            while (keys.hasMoreElements())
            {
                val key = keys.nextElement()
                val connector = connectLines[key]
                if ((connector != null)&&((connector.getFromObjectKey() == keyToRemove) || (connector.getToObjectKey() == keyToRemove)))
                {
                    // 削除するキーが見つかった！
                    connectLines.remove(key)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun createLine(id: Int): ObjectConnector
    {
        val connector = ObjectConnector(
            id,
            1,
            1,
            LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW,
            LineStyleHolder.LINESHAPE_NORMAL,
            LineStyleHolder.LINETHICKNESS_THIN,
            historyHolder
        )
        connectLines[id] = connector
        return (connector)
    }

    fun setLines(fromKey: Int, toKey: Int, lineHolder: LineStyleHolder): ObjectConnector
    {
        val connector = ObjectConnector(
            this.serialNumber,
            fromKey,
            toKey,
            lineHolder.getLineStyle(),
            lineHolder.getLineShape(),
            lineHolder.getLineThickness(),
            this.historyHolder
        )
        try
        {
            this.connectLines[this.serialNumber] = connector
            this.serialNumber++
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (connector)
    }

    companion object
    {
        const val ID_NOTSPECIFY: Int = -1
        private val TAG = MeMoMaConnectLineHolder::class.java.simpleName
    }
}