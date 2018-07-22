package jp.sourceforge.gokigen.memoma.holders;

import java.util.Enumeration;
import java.util.Hashtable;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.Main;


/**
 *   表示オブジェクト間の接続情報を保持するクラス
 * 
 * @author MRSa
 *
 */
public class MeMoMaConnectLineHolder
{
	private final IOperationHistoryHolder historyHolder;
    public static final int ID_NOTSPECIFY = -1;
    private Hashtable<Integer, ObjectConnector>  connectLines;
    private Integer serialNumber = 1;

    public MeMoMaConnectLineHolder(IOperationHistoryHolder historyHolder)
    {
        this.historyHolder = historyHolder;
        connectLines = new Hashtable<>();
        connectLines.clear();
    }

    public Enumeration<Integer> getLineKeys()
    {
        return (connectLines.keys());
    }

    public ObjectConnector getLine(Integer key)
    {
        return (connectLines.get(key));
    }

    public boolean disconnectLines(Integer key)
    {
        connectLines.remove(key);
        Log.v(Main.APP_IDENTIFIER, "DISCONNECT LINES : " + key);
        return (true);
    }

    public void setSerialNumber(int id)
    {
        serialNumber = (id == ID_NOTSPECIFY) ? ++serialNumber : id;
    }

    public int getSerialNumber()
    {
        return (serialNumber);
    }

    public void removeAllLines()
    {
        connectLines.clear();
        serialNumber = 1;
    }

    public void dumpConnectLine(ObjectConnector conn)
    {
        if (conn == null)
        {
            return;
        }
        Log.v(Main.APP_IDENTIFIER, "LINE " + conn.getKey() + " [" + conn.getFromObjectKey() + " -> " + conn.getToObjectKey() + "] ");
    }

    /**
     *    keyToRemove で指定されたobjectの接続をすべて削除する
     *
     * @param keyToRemove\
     */
    public void removeAllConnection(Integer keyToRemove)
    {
        Enumeration<Integer> keys = connectLines.keys();
        while (keys.hasMoreElements())
        {
            Integer key = keys.nextElement();
            ObjectConnector connector = connectLines.get(key);
            if ((connector.getFromObjectKey() == keyToRemove)||(connector.getToObjectKey() == keyToRemove))
            {
                // 削除するキーが見つかった！
                connectLines.remove(key);
            }
        }
    }

    public ObjectConnector createLine(int id)
    {
        ObjectConnector connector = new ObjectConnector(id, 1, 1, LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW, LineStyleHolder.LINESHAPE_NORMAL, LineStyleHolder.LINETHICKNESS_THIN, historyHolder);
        connectLines.put(id, connector);
        return (connector);
    }

    public ObjectConnector setLines(Integer fromKey, Integer toKey, LineStyleHolder lineHolder)
    {
        ObjectConnector connector = new ObjectConnector(serialNumber, fromKey, toKey, lineHolder.getLineStyle(), lineHolder.getLineShape(), lineHolder.getLineThickness(), historyHolder);

        connectLines.put(serialNumber, connector);
        serialNumber++;
        return (connector);
    }
}
