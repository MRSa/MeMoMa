package jp.sourceforge.gokigen.memoma.io;

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
     * @param contentUri         添付データファイルのURI
     * @param fileType           添付データファイルの形 (text/plain とか  image/* とか ...)
     */
    static public void shareContent(Activity parent, int id, String mailTitle, String mailMessage, Uri contentUri, String fileType)
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        try
        {
            Log.v(IDENTIFIER, "Share Content... " + contentUri);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, mailTitle);
            intent.putExtra(Intent.EXTRA_TEXT, mailMessage);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try
            {
            	if ((contentUri != null)&&(!fileType.isEmpty()))
            	{
                	// ファイル類を添付する
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setType(fileType);
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    Log.v(IDENTIFIER, "Attached :" + contentUri);
            	}
            }
            catch (Exception ee)
            {
            	// 
                Log.v(IDENTIFIER, "attach failure : " + contentUri + "  " + ee.toString() + " " + ee.getMessage());
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
            e.printStackTrace();
        }
    }
}
