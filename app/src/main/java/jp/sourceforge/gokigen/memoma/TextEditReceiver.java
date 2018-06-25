package jp.sourceforge.gokigen.memoma;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.TextView;

/**
 *   テキストデータの反映
 * 
 * @author MRSa
 *
 */
public class TextEditReceiver implements TextEditDialog.ITextEditResultReceiver
{
	Activity parent = null;
	String textId = null;
	int     textResId = -1;
	
    /**
     *    コンストラクタ
     * 
     */
	public TextEditReceiver(Activity argument, String prefId, int resId)
    {
        textId = prefId;
        parent = argument;
        textResId = resId;
    }
	
	/**
	 *   データの更新
	 * 
	 */
    public boolean finishTextEditDialog(String message)
    {
    	if ((message == null)||(message.length() == 0))
    	{
            // データが入力されていなかったので、何もしない。
    		return (false);
    	}
    	
    	// 文字列を記録
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(textId, message);
        editor.commit();

        if (textResId != 0)
        {
            // 画面表示の更新
        	final TextView infoText = (TextView) parent.findViewById(textResId);
        	infoText.setText(message);
        }
        else
        {
        	// リソースIDが指定されていない場合は、タイトルを更新する
        	parent.setTitle(message);
        }
        
        return (true);
    }

    /**
     *   データを更新しないとき...
     */
    public boolean cancelTextEditDialog()
    {
        return (false);
    }
}
