package jp.sourceforge.gokigen.memoma.fileio;

import java.io.File;
import java.io.FileReader;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaConnectLineHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;

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
	private Context parent = null;
	private IResultReceiver receiver = null;
	private ExternalStorageFileUtility fileUtility = null;

	 private MeMoMaObjectHolder.PositionObject position = null;
	 private MeMoMaConnectLineHolder.ObjectConnector line = null;

	 private String backgroundUri = "";
     private String userCheckboxString = "";
	
	/**
	 *   コンストラクタ
	 */
    public MeMoMaFileLoadingProcess(Context context, ExternalStorageFileUtility utility, IResultReceiver resultReceiver)
    {
    	parent = context;
    	receiver = resultReceiver;
    	fileUtility = utility;
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
            	position.rect.top = Float.parseFloat(parser.nextText());
            }
            else if ((name.equalsIgnoreCase("bottom"))&&(position != null))
            {
            	position.rect.bottom = Float.parseFloat(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("left"))&&(position != null))
            {
            	position.rect.left = Float.parseFloat(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("right"))&&(position != null))
            {
            	position.rect.right = Float.parseFloat(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("drawStyle"))&&(position != null))
            {
            	position.drawStyle = Integer.parseInt(parser.nextText());
            }
            else if ((name.equalsIgnoreCase("icon"))&&(position != null))
            {
            	position.icon = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("label"))&&(position != null))
            {
            	position.label = parser.nextText();
            }
            else if ((name.equalsIgnoreCase("detail"))&&(position != null))
            {
            	position.detail = parser.nextText();            	
            }
/**
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
            	position.userChecked =(parseData.equalsIgnoreCase("true")) ? true : false;
            }
            else if ((name.equalsIgnoreCase("labelColor"))&&(position != null))
            {
            	position.labelColor = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("objectColor"))&&(position != null))
            {
            	position.objectColor = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("paintStyle"))&&(position != null))
            {
            	position.paintStyle = parser.nextText();
            }
            else if ((name.equalsIgnoreCase("strokeWidth"))&&(position != null))
            {
            	position.strokeWidth = Float.parseFloat(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("fontSize"))&&(position != null))
            {
            	position.fontSize = Float.parseFloat(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("fromObjectKey"))&&(line != null))
            {
            	line.fromObjectKey = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("toObjectKey"))&&(line != null))
            {
            	line.toObjectKey = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("lineStyle"))&&(line != null))
            {
            	line.lineStyle = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("lineShape"))&&(line != null))
            {
            	line.lineShape = Integer.parseInt(parser.nextText());            	
            }
            else if ((name.equalsIgnoreCase("lineThickness"))&&(line != null))
            {
            	line.lineThickness = Integer.parseInt(parser.nextText());            	
            }
/**
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
                position = null;
                position = objectHolder.createPosition(key);
            }
            else if (name.equalsIgnoreCase("line"))
            {
                int key = Integer.parseInt(parser.getAttributeValue(Main.APP_NAMESPACE, "key"));
                //Log.v(Main.APP_IDENTIFIER, "create line, key :" + key);
                line = null;
                line = objectHolder.getConnectLineHolder().createLine(key);        	
            }
    	}
        catch (Exception e)
        {
            Log.v(Main.APP_IDENTIFIER, "ERR>parseStartTag() name:" + name + " " + e.toString());
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
            	if ((position.rect.left > position.rect.right)||(position.rect.top > position.rect.bottom))
            	{
            		Log.v(Main.APP_IDENTIFIER, "RECT IS ILLEGAL. : [" + position.rect.left + "," + position.rect.top + "-[" + position.rect.right + "," + position.rect.bottom + "]");
            		position.rect.right = position.rect.left + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_X;
            		position.rect.bottom = position.rect.top + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_Y;
            	}
            	
            }
            else if (name.equalsIgnoreCase("line"))
            {
                //Log.v(Main.APP_IDENTIFIER, "parseEndTag() : LINE");       
                //objectHolder.getConnectLineHolder().dumpConnectLine(line);
            }
        }
        catch (Exception e)
        {
            Log.v(Main.APP_IDENTIFIER, "ERR>parseEndTag() name:" + name + " " + e.toString());
        }
    }
    
    /**
     *    (XML形式の)データを読みだす。
     * 
     * @param fileName
     * @param objectHolder
     * @return
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
    		 if (inputFile.exists() == false)
    		 {
    			 // ファイルがなかったときには、「ファイルなし」と報告する。
    			 resultMessage = "ERR>File not found.";
    			 return (resultMessage);
    		 }
    		 // ファイルの読み込み
    		 FileReader reader = new FileReader(inputFile);
    		 parser.setInput(reader);

    		 int eventType = parser.getEventType();
             boolean done = false;

             // オブジェクトとラインをすべてクリアする
             objectHolder.removeAllPositions();
             MeMoMaConnectLineHolder lineHolder = objectHolder.getConnectLineHolder();
             if (lineHolder == null)
             {
        		 return ("ERR>lineHolder is null.");            	 
             }
             lineHolder.removeAllLines();
             
             while ((eventType != XmlPullParser.END_DOCUMENT)&&(done != true))
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
         	 resultMessage = " ERR>" + e.toString();
             Log.v(Main.APP_IDENTIFIER, resultMessage);
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
    	String fileName = fileUtility.getGokigenDirectory() + "/" + datas[0].getDataTitle() + ".xml";
    	
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
    		if (result.isEmpty() == true)
    		{
    	    	//  エラーが発生していない場合には、読みだしたデータをPreferenceに設定登録...
    	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("backgroundUri", backgroundUri);
                editor.putString("userCheckboxString", userCheckboxString);
                editor.commit();
    		}

            if (receiver != null)
            {
            	receiver.onLoadedResult(result);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaFileSavingProcess::onPostExecute() : " + ex.toString());
    	}
        return;
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
    	public abstract void onLoadingProcess();
    	
        /**  保存結果の報告 **/
        public abstract void onLoadedResult(String detail);
    }
}
