package jp.sourceforge.gokigen.memoma.holders;

import java.util.Enumeration;
import java.util.Hashtable;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.holders.LineStyleHolder;


/**
 *   表示オブジェクト間の接続情報を保持するクラス
 * 
 * @author MRSa
 *
 */public class MeMoMaConnectLineHolder
{
		public static final int ID_NOTSPECIFY = -1;

		/**
		 *   オブジェクト間を接続するクラス
		 *   
		 * @author MRSa
		 *
		 */
		public class ObjectConnector
		{
			public Integer key;
			public Integer fromObjectKey;
			public Integer toObjectKey;
			public Integer lineStyle;
			public Integer lineShape;
			public Integer lineThickness;
/**
			public int fromShape;
			public int toShape;
			public String fromString;
			public String toString;
**/
		};

		private Hashtable<Integer, ObjectConnector>  connectLines = null;
        private Integer serialNumber = 1;

	    public MeMoMaConnectLineHolder()
	    {
			  connectLines = new Hashtable<Integer, ObjectConnector>();
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
	    	Log.v(Main.APP_IDENTIFIER, "LINE " + conn.key + " [" + conn.fromObjectKey + " -> " + conn.toObjectKey + "] ");
	    }
	    
	    /**
	     *    keyToRemove で指定されたobjectの接続をすべて削除する
	     * 
	     * @param keyToRemove
	     */
	    public void removeAllConnection(Integer keyToRemove)
	    {
	          Enumeration<Integer> keys = connectLines.keys();
			  while (keys.hasMoreElements())
			  {
				  Integer key = keys.nextElement();
				  ObjectConnector connector = connectLines.get(key);
				  if ((connector.fromObjectKey == keyToRemove)||(connector.toObjectKey == keyToRemove))
				  {
					  // 削除するキーが見つかった！
					  connector.key = -1;   // 一応...大丈夫だとは思うけど
					  connectLines.remove(key);
				  }
			  }
	    }

	    public ObjectConnector createLine(int id)
	    {
	    	ObjectConnector connector = new ObjectConnector();
	    	connector.key = id;
	    	connector.fromObjectKey = 1;
	    	connector.toObjectKey = 1;
	    	connector.lineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW;
	    	connector.lineShape = LineStyleHolder.LINESHAPE_NORMAL;
	    	connector.lineThickness = LineStyleHolder.LINETHICKNESS_THIN;
	    	
/**
	    	connector.fromShape = 0;
			connector.toShape = 0;
			connector.fromString = "";
			connector.toString = "";
**/
	    	connectLines.put(id, connector);
	    	return (connector);    	
	    }

	    public ObjectConnector setLines(Integer fromKey, Integer toKey, LineStyleHolder lineHolder)
	    {
	    	ObjectConnector connector = new ObjectConnector();
	    	connector.key = serialNumber;
	    	connector.fromObjectKey = fromKey;
	    	connector.toObjectKey = toKey;
	    	connector.lineStyle = lineHolder.getLineStyle();
	    	connector.lineShape = lineHolder.getLineShape();
	    	connector.lineThickness = lineHolder.getLineThickness();
/**
	    	connector.fromShape = 0;
			connector.toShape = 0;
			connector.fromString = "";
			connector.toString = "";
**/
	    	connectLines.put(serialNumber, connector);
	    	serialNumber++;
	    	return (connector);    	
	    }
}
