package jp.sourceforge.gokigen.memoma;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 *   共有Intentを発行するクラス。
 * 
 * @author MRSa
 *
 */
public class SharedIntentInvoker
{
	private static final String  IDENTIFIER = "Gokigen";
	
    /**
     *    メール送信用のIntentを発行する処理。
     * @param parent   呼び出し元Activity
     * @param id          Intentが呼び出し元Activityに戻った時に、呼ばれていたのは何か識別するID
     * @param mailTitle         共有データタイトル
     * @param mailMessage   共有データ本文
     * @param fileName         添付データファイル名称
     * @param fileType           添付データファイルの形 (text/plain とか  image/* とか ...)
     */
    static public void shareContent(Activity parent, int id, String mailTitle, String mailMessage, String fileName, String fileType)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        try
        {
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, mailTitle);
            intent.putExtra(Intent.EXTRA_TEXT, mailMessage);
            try
            {
            	if ((fileName.isEmpty() == false)&&(fileType.isEmpty() == false))
            	{
                	// ファイルを添付する
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); 
                    intent.setType(fileType);
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileName));
                    Log.v(IDENTIFIER, "Attached :" + fileName);
            	}
            }
            catch (Exception ee)
            {
            	// 
                Log.v(IDENTIFIER, "attach failure : " + fileName + "  " + ee.toString() + " " + ee.getMessage());
            }
            parent.startActivityForResult(intent, id);          	
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            Toast.makeText(parent, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.v(IDENTIFIER, "android.content.ActivityNotFoundException : " + ex.toString() + " " + ex.getMessage());
        }
        catch (Exception e)
        {
            Log.v(IDENTIFIER, "xxx : " + e.toString() + " " + e.getMessage());
        }
    }
}
