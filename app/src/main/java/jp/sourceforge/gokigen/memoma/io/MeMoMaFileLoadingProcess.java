package jp.sourceforge.gokigen.memoma.io;

import java.io.File;
import java.io.FileReader;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaConnectLineHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.ObjectConnector;
import jp.sourceforge.gokigen.memoma.holders.PositionObject;

/**
 *  データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 *  
 *  AsyncTask
 *    MeMoMaObjectHolder : 実行時に渡すクラス(Param)
 *    Integer    : 途中経過を伝えるクラス(Progress)
 *    String     : 処理結果を伝えるクラス(Result)
 *    
 * @author MRSa
 *
 */
public class MeMoMaFileLoadingProcess extends AsyncTask<MeMoMaObjectHolder, Integer, String>
{
    private final String TAG = toString();
	private final Context parent;
	private IResultReceiver receiver;

	 private PositionObject position = null;
	 private ObjectConnector line = null;

	 private String backgroundUri = "";
     private String userCheckboxString = "";
	
	/**
	 *   コンストラクタ
	 */
    public MeMoMaFileLoadingProcess(Context context, IResultReceiver resultReceiver)
    {
    	parent = context;
    	receiver = resultReceiver;
    }

    /**
     *  非同期処理実施前の前処理
     * 
     */
    @Override
    protected void onPreExecute()
    {
         // 今回は何もしない
    }

    private void parseStartTag(String name, XmlPullParser parser, MeMoMaObjectHolder objectHolder)
    {
    	try
    	{
	    	//Log.v(Main.APP_IDENTIFIER, "parseStartTag() name = " + name);
            if ((name.equalsIgnoreCase("top"))&&(position != null))
            {
            	position.setRectTop(Float.parseFloat(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("bottom"))&&(position != null))
            {
            	position.setRectBottom(Float.parseFloat(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("left"))&&(position != null))
            {
            	position.setRectLeft(Float.parseFloat(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("right"))&&(position != null))
            {
            	position.setRectRight(Float.parseFloat(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("drawStyle"))&&(position != null))
            {
            	position.setDrawStyle(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("icon"))&&(position != null))
            {
            	position.setIcon(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("label"))&&(position != null))
            {
            	position.setLabel(parser.nextText());
            }
            else if ((name.equalsIgnoreCase("detail"))&&(position != null))
            {
            	position.setDetail(parser.nextText());
            }
/*
            else if ((name.equalsIgnoreCase("backgroundUri"))&&(position != null))
            {
            	position.backgroundUri = parser.nextText();            	            	
            }
            else if ((name.equalsIgnoreCase("otherInfoUri"))&&(position != null))
            {
            	position.otherInfoUri = parser.nextText();            	            	
            }
            else if ((name.equalsIgnoreCase("objectStatus"))&&(position != null))
            {
            	position.objectStatus = parser.nextText();            	
            }
**/
            else if ((name.equalsIgnoreCase("userChecked"))&&(position != null))
            {
            	String parseData = parser.nextText();
            	position.setUserChecked((parseData.equalsIgnoreCase("true")));
            }
            else if ((name.equalsIgnoreCase("labelColor"))&&(position != null))
            {
            	position.setLabelColor(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("objectColor"))&&(position != null))
            {
            	position.setObjectColor(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("paintStyle"))&&(position != null))
            {
            	position.setPaintStyle(parser.nextText());
            }
            else if ((name.equalsIgnoreCase("strokeWidth"))&&(position != null))
            {
            	position.setStrokeWidth(Float.parseFloat(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("fontSize"))&&(position != null))
            {
            	position.setFontSize(Float.parseFloat(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("fromObjectKey"))&&(line != null))
            {
            	line.setFromObjectKey(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("toObjectKey"))&&(line != null))
            {
            	line.setToObjectKey(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("lineStyle"))&&(line != null))
            {
            	line.setLineStyle(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("lineShape"))&&(line != null))
            {
            	line.setLineShape(Integer.parseInt(parser.nextText()));
            }
            else if ((name.equalsIgnoreCase("lineThickness"))&&(line != null))
            {
            	line.setLineThickness(Integer.parseInt(parser.nextText()));
            }
/*
            else if ((name.equalsIgnoreCase("fromShape"))&&(line != null))
            {
            	line.fromShape = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("toShape"))&&(line != null))
            {
            	line.toShape = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("fromString"))&&(line != null))
            {
            	line.fromString = parser.nextText();            	
            }
            else if ((name.equalsIgnoreCase("toString"))&&(line != null))
            {
            	line.toString = parser.nextText();            	
            }
**/
            else if ((name.equalsIgnoreCase("title"))&&(objectHolder != null))
            {
            	objectHolder.setDataTitle(parser.nextText());
            }
            else if ((name.equalsIgnoreCase("background"))&&(objectHolder != null))
            {
            	objectHolder.setBackground(parser.nextText());
            }
            else if ((name.equalsIgnoreCase("backgroundUri"))&&(objectHolder != null))
            {
                backgroundUri = parser.nextText();
            }
            else if ((name.equalsIgnoreCase("userCheckboxString"))&&(objectHolder != null))
            {
                userCheckboxString = parser.nextText();
            }
            else if ((name.equalsIgnoreCase("objserial"))&&(objectHolder != null))
            {
            	objectHolder.setSerialNumber(Integer.parseInt(parser.nextText()));
            	//Log.v(Main.APP_IDENTIFIER, "objSerial : " + objectHolder.getSerialNumber());
            }
            else if ((name.equalsIgnoreCase("lineserial"))&&(objectHolder != null))
            {
            	objectHolder.getConnectLineHolder().setSerialNumber(Integer.parseInt(parser.nextText()));            	
            	//Log.v(Main.APP_IDENTIFIER, "lineSerial : " + objectHolder.getSerialNumber());
            }
            else if (name.equalsIgnoreCase("object"))
            {
                int key = Integer.parseInt(parser.getAttributeValue(Main.APP_NAMESPACE, "key"));
                //Log.v(Main.APP_IDENTIFIER, "create object, key :" + key);
                if (objectHolder != null)
                {
                    position = objectHolder.createPosition(key);
                }
            }
            else if (name.equalsIgnoreCase("line"))
            {
                int key = Integer.parseInt(parser.getAttributeValue(Main.APP_NAMESPACE, "key"));
                //Log.v(Main.APP_IDENTIFIER, "create line, key :" + key);
                line = null;
                if (objectHolder != null)
                {
                    line = objectHolder.getConnectLineHolder().createLine(key);
                }
            }
    	}
        catch (Exception e)
        {
            Log.v(TAG, "ERR>parseStartTag() name:" + name + " " + e.toString());
        }
    }
    
    private void parseEndTag(String name, XmlPullParser parser, MeMoMaObjectHolder objectHolder)
    {
    	try
    	{
            if (name.equalsIgnoreCase("object"))
            {
                //Log.v(Main.APP_IDENTIFIER, "parseEndTag() : OBJECT");
                //objectHolder.dumpPositionObject(position);

            	// 領域サイズがおかしい場合には、オブジェクトサイズを補正する (ふつーありえないはずなんだけど...)
                RectF posRect = position.getRect();
            	if ((posRect.left > posRect.right)||(posRect.top > posRect.bottom))
            	{
            		Log.v(TAG, "RECT IS ILLEGAL. : [" + posRect.left + "," + posRect.top + "-[" + posRect.right + "," + posRect.bottom + "]");
            		position.setRectRight(posRect.left + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_X);
            		position.setRectBottom(posRect.top + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_Y);
            	}
            }
/*
            else if (name.equalsIgnoreCase("line"))
            {
                //
                //Log.v(Main.APP_IDENTIFIER, "parseEndTag() : LINE");       
                //objectHolder.getConnectLineHolder().dumpConnectLine(line);
            }
*/
        }
        catch (Exception e)
        {
            Log.v(TAG, "ERR>parseEndTag() name:" + name + " " + e.toString());
        }
    }
    
    /**
     *    (XML形式の)データを読みだす。
     *
     */
    private String restoreToXmlFile(String fileName, MeMoMaObjectHolder objectHolder)
    {
    	String resultMessage = "";
    	 XmlPullParser parser = Xml.newPullParser();
    	 
    	 if (objectHolder == null)
    	 {
    		 return ("ERR>objectHolder is null.");
    	 }
    	 
    	 try
    	 {
    		 File inputFile = new File(fileName);
    		 if (!inputFile.exists())
    		 {
    			 // ファイルがなかったときには、「ファイルなし」と報告する。
    			 resultMessage = "ERR>File not found.";
    			 return (resultMessage);
    		 }
    		 // ファイルの読み込み
    		 FileReader reader = new FileReader(inputFile);
    		 parser.setInput(reader);

    		 int eventType = parser.getEventType();

             // オブジェクトとラインをすべてクリアする
             objectHolder.removeAllPositions();
             MeMoMaConnectLineHolder lineHolder = objectHolder.getConnectLineHolder();
             if (lineHolder == null)
             {
        		 return ("ERR>lineHolder is null.");            	 
             }
             lineHolder.removeAllLines();
             
             while ((eventType != XmlPullParser.END_DOCUMENT))
             {
                 switch (eventType)
                 {
                     case XmlPullParser.START_DOCUMENT:
                         break;

                     case XmlPullParser.START_TAG:
                         parseStartTag(parser.getName(), parser, objectHolder);
                         break;

                     case XmlPullParser.END_TAG:
                    	 parseEndTag(parser.getName(), parser, objectHolder);
                         break;

                     default:
                    	 // 省略...
                    	 break;
                 }
                 eventType = parser.next();
             }
             reader.close();
    	 }
    	 catch (Exception e)
    	 {
         	 resultMessage = " ERR>" + e.getMessage();
             Log.v(TAG, resultMessage);
         	 e.printStackTrace();
    	 }
    	return (resultMessage);
    }

    /**
     *  非同期処理
     *  （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     * 
     */
    @Override
    protected String doInBackground(MeMoMaObjectHolder... datas)
    {
        // ファイル名の設定 ... (拡張子あり...保存時とは違う)
    	String fileName = parent.getFilesDir() + "/" + datas[0].getDataTitle() + ".xml";
    	
    	// データを読みだす。
        String result = restoreToXmlFile(fileName, datas[0]);

        //何か必要な場合、 非同期処理をここで実効
        if (receiver != null)
        {
        	receiver.onLoadingProcess();
        }

		System.gc();
		return (result);
    }
    /**
     *  非同期処理の進捗状況の更新
     * 
     */
	@Override
	protected void onProgressUpdate(Integer... values)
	{
        // 今回は何もしない
	}

    /**
     *  非同期処理の後処理
     *  (結果を応答する)
     */
    @Override
    protected void onPostExecute(String result)
    {
    	try
    	{
    		if (result.isEmpty())
    		{
    	    	//  エラーが発生していない場合には、読みだしたデータをPreferenceに設定登録...
    	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("backgroundUri", backgroundUri);
                editor.putString("userCheckboxString", userCheckboxString);
                editor.apply();
    		}

            if (receiver != null)
            {
            	receiver.onLoadedResult(result);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(TAG, "MeMoMaFileSavingProcess::onPostExecute() : " + ex.getMessage());
    	}
    }     
	
    /**
     *    結果報告用のインタフェース（積極的に使う予定はないけど...）
     *    
     * @author MRSa
     *
     */
    public interface IResultReceiver
    {
        /**   処理中の処理   **/
    	void onLoadingProcess();
    	
        /**  保存結果の報告 **/
        void onLoadedResult(String detail);
    }
}
