package jp.sourceforge.gokigen.memoma.io;

import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.graphics.RectF;
import android.util.Log;
import android.util.Xml;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.ObjectConnector;
import jp.sourceforge.gokigen.memoma.holders.PositionObject;

/**
 *  データをファイルに保存するエンジン部分
 */
public class MeMoMaFileSavingEngine
{
    private final String TAG = toString();
    private final Context context;
	private final String backgroundUri;
	private final String userCheckboxString;
	
	/**
	 *   コンストラクタ
	 */
    public MeMoMaFileSavingEngine(Context context, String bgUri, String checkBoxLabel)
    {
        this.context = context;

        //  設定データ読み出し用...。
        backgroundUri = bgUri;
        userCheckboxString = checkBoxLabel;
    }

    /**
     *    データを(XML形式で)保管する。
     *
     */
    private String storeToXmlFile(String fileName, MeMoMaObjectHolder objectHolder)
    {
    	String resultMessage = "";
        try
        {
            FileWriter writer = new FileWriter(fileName + ".xml");
            XmlSerializer serializer = Xml.newSerializer();

            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.startTag(Main.APP_NAMESPACE, "memoma");
            
            // タイトルの出力
            serializer.startTag(Main.APP_NAMESPACE, "title");
            serializer.text(objectHolder.getDataTitle());
            serializer. endTag(Main.APP_NAMESPACE, "title");

            // 背景情報の出力
            serializer.startTag(Main.APP_NAMESPACE, "background");
            serializer.text(objectHolder.getBackground());
            serializer. endTag(Main.APP_NAMESPACE, "background");

            // 背景画像URIの出力
            serializer.startTag(Main.APP_NAMESPACE, "backgroundUri");
            serializer.text(backgroundUri);
            serializer. endTag(Main.APP_NAMESPACE, "backgroundUri");
            
            // ユーザチェックボックス名の出力
            serializer.startTag(Main.APP_NAMESPACE, "userCheckboxString");
            serializer.text(userCheckboxString);
            serializer. endTag(Main.APP_NAMESPACE, "userCheckboxString");
            
            serializer.startTag(Main.APP_NAMESPACE, "objserial");
            serializer.text(Integer.toString(objectHolder.getSerialNumber()));
            serializer.endTag(Main.APP_NAMESPACE, "objserial");

            serializer.startTag(Main.APP_NAMESPACE, "lineserial");
            serializer.text(Integer.toString(objectHolder.getConnectLineHolder().getSerialNumber()));
            serializer.endTag(Main.APP_NAMESPACE, "lineserial");
            
            
        	// オブジェクトの出力 （保持しているものはすべて表示する）
            int objectCount = 0;
        	Enumeration<Integer> keys = objectHolder.getObjectKeys();
            while (keys.hasMoreElements())
            {
                Integer key = keys.nextElement();
                PositionObject pos = objectHolder.getPosition(key);
                RectF posRect = pos.getRect();
                serializer.startTag(Main.APP_NAMESPACE, "object");

                serializer.attribute(Main.APP_NAMESPACE, "key", Integer.toString(key));

                serializer.startTag(Main.APP_NAMESPACE, "rect");
                serializer.startTag(Main.APP_NAMESPACE, "top");
                serializer.text(Float.toString(posRect.top));
                serializer. endTag(Main.APP_NAMESPACE, "top");
                serializer.startTag(Main.APP_NAMESPACE, "left");
                serializer.text(Float.toString(posRect.left));
                serializer. endTag(Main.APP_NAMESPACE, "left");
                serializer.startTag(Main.APP_NAMESPACE, "right");
                serializer.text(Float.toString(posRect.right));
                serializer. endTag(Main.APP_NAMESPACE, "right");
                serializer.startTag(Main.APP_NAMESPACE, "bottom");
                serializer.text(Float.toString(posRect.bottom));
                serializer. endTag(Main.APP_NAMESPACE, "bottom");
                serializer. endTag(Main.APP_NAMESPACE, "rect");

                serializer.startTag(Main.APP_NAMESPACE, "drawStyle");
                serializer.text(Integer.toString(pos.getDrawStyle()));
                serializer. endTag(Main.APP_NAMESPACE, "drawStyle");

                serializer.startTag(Main.APP_NAMESPACE, "icon");
                serializer.text(Integer.toString(pos.getIcon()));
                serializer. endTag(Main.APP_NAMESPACE, "icon");

                serializer.startTag(Main.APP_NAMESPACE, "label");
                serializer.text(pos.getLabel());
                serializer. endTag(Main.APP_NAMESPACE, "label");

                serializer.startTag(Main.APP_NAMESPACE, "detail");
                serializer.text(pos.getDetail());
                serializer. endTag(Main.APP_NAMESPACE, "detail");
/*
                serializer.startTag(Main.APP_NAMESPACE, "otherInfoUri");
                serializer.text(pos.otherInfoUri);
                serializer. endTag(Main.APP_NAMESPACE, "otherInfoUri");

                serializer.startTag(Main.APP_NAMESPACE, "backgroundUri");
                serializer.text(pos.backgroundUri);
                serializer. endTag(Main.APP_NAMESPACE, "backgroundUri");

                serializer.startTag(Main.APP_NAMESPACE, "objectStatus");
                serializer.text(pos.objectStatus);
                serializer. endTag(Main.APP_NAMESPACE, "objectStatus");
*/
                serializer.startTag(Main.APP_NAMESPACE, "userChecked");
                serializer.text(Boolean.toString(pos.getUserChecked()));
                serializer. endTag(Main.APP_NAMESPACE, "userChecked");
                
                serializer.startTag(Main.APP_NAMESPACE, "labelColor");
                serializer.text(Integer.toString(pos.getLabelColor()));
                serializer. endTag(Main.APP_NAMESPACE, "labelColor");

                serializer.startTag(Main.APP_NAMESPACE, "objectColor");
                serializer.text(Integer.toString(pos.getObjectColor()));
                serializer. endTag(Main.APP_NAMESPACE, "objectColor");

                serializer.startTag(Main.APP_NAMESPACE, "paintStyle");
                serializer.text(pos.getPaintStyle());
                serializer. endTag(Main.APP_NAMESPACE, "paintStyle");
               
                serializer.startTag(Main.APP_NAMESPACE, "strokeWidth");
                serializer.text(Float.toString(pos.getstrokeWidth()));
                serializer. endTag(Main.APP_NAMESPACE, "strokeWidth");

                serializer.startTag(Main.APP_NAMESPACE, "fontSize");
                serializer.text(Float.toString(pos.getFontSize()));
                serializer. endTag(Main.APP_NAMESPACE, "fontSize");

                serializer.endTag(Main.APP_NAMESPACE, "object");
                objectCount++;
            }

            // 接続線の出力 （保持しているものはすべて表示する）
        	Enumeration<Integer> lineKeys = objectHolder.getConnectLineHolder().getLineKeys();
            while (lineKeys.hasMoreElements())
            {
                Integer key = lineKeys.nextElement();
                ObjectConnector line = objectHolder.getConnectLineHolder().getLine(key);
                serializer.startTag(Main.APP_NAMESPACE, "line");
                serializer.attribute(Main.APP_NAMESPACE, "key", Integer.toString(key));

                serializer.startTag(Main.APP_NAMESPACE, "fromObjectKey");
                serializer.text(Integer.toString(line.getFromObjectKey()));
                serializer.endTag(Main.APP_NAMESPACE, "fromObjectKey");

                serializer.startTag(Main.APP_NAMESPACE, "toObjectKey");
                serializer.text(Integer.toString(line.getToObjectKey()));
                serializer.endTag(Main.APP_NAMESPACE, "toObjectKey");

                serializer.startTag(Main.APP_NAMESPACE, "lineStyle");
                serializer.text(Integer.toString(line.getLineStyle()));
                serializer.endTag(Main.APP_NAMESPACE, "lineStyle");

                serializer.startTag(Main.APP_NAMESPACE, "lineShape");
                serializer.text(Integer.toString(line.getLineShape()));
                serializer.endTag(Main.APP_NAMESPACE, "lineShape");

                serializer.startTag(Main.APP_NAMESPACE, "lineThickness");
                serializer.text(Integer.toString(line.getLineThickness()));
                serializer.endTag(Main.APP_NAMESPACE, "lineThickness");
/*
                serializer.startTag(Main.APP_NAMESPACE, "fromShape");
                serializer.text(Integer.toString(line.fromShape));
                serializer.endTag(Main.APP_NAMESPACE, "fromShape");

                serializer.startTag(Main.APP_NAMESPACE, "toShape");
                serializer.text(Integer.toString(line.toShape));
                serializer.endTag(Main.APP_NAMESPACE, "toShape");
                
                serializer.startTag(Main.APP_NAMESPACE, "fromString");
                serializer.text(line.fromString);
                serializer.endTag(Main.APP_NAMESPACE, "fromString");

                serializer.startTag(Main.APP_NAMESPACE, "toString");
                serializer.text(line.toString);
                serializer.endTag(Main.APP_NAMESPACE, "toString");
*/
                serializer.endTag(Main.APP_NAMESPACE, "line");
                objectCount++;
            }

            serializer.endTag(Main.APP_NAMESPACE, "memoma");
            serializer.endDocument();
            serializer.flush();
            writer.close();

            Log.v(TAG, "SAVE XML... objects: " + objectCount);
        }
        catch (Exception e)
        {
            resultMessage = " " + e.getMessage();
            Log.v(TAG, resultMessage);
            e.printStackTrace();
        }

        return (resultMessage);
    }

    /**
     *    オブジェクトを保存する
     */
    public String saveObjects(MeMoMaObjectHolder objectHolder)
    {
		// データタイトルがない場合...保存処理は行わない。
    	if (objectHolder.getDataTitle().isEmpty())
        {
    		Log.v(TAG, "MeMoMaFileSavingEngine::saveObjects() : specified file name is illegal, save aborted. : " + objectHolder.getDataTitle() );
    		return ("");
        }

        if (objectHolder.isEmpty())
        {
            try
            {
                // ファイルの存在を確認
                String fileName = context.getFilesDir() + "/" + objectHolder.getDataTitle() + ".xml";
                File checkFile = new File(fileName);
                if (!checkFile.exists())
                {
                    // ファイルが存在しない場合、新規ファイルを作成する
                    if (checkFile.createNewFile())
                    {
                        Log.v(TAG, "create New File.");
                        return (context.getString(R.string.createnew));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // データがない場合は保存しない
            return(context.getString(R.string.none_object));
        }

    	// データを保管する （ファイル名の設定は、拡張子なし
        return (storeToXmlFile(context.getFilesDir() + "/" + objectHolder.getDataTitle(), objectHolder));
    }
}
