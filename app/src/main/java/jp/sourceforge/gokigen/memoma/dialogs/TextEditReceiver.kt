package jp.sourceforge.gokigen.memoma.dialogs

import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.dialogs.TextEditDialog.ITextEditResultReceiver

/**
 * テキストデータの反映
 */
class TextEditReceiver(private val parent: AppCompatActivity, private val textId: String, private val textResId: Int) : ITextEditResultReceiver
{
    /**
     * データの更新
     */
    override fun finishTextEditDialog(message: String)
    {
        if (message.isEmpty())
        {
            // データが入力されていなかったので、何もしない。
            return
        }

        // 文字列を記録
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        val editor = preferences.edit()
        editor.putString(textId, message)
        editor.apply()

        if (textResId != 0)
        {
            // 画面表示の更新
            val infoText = parent.findViewById<TextView>(textResId)
            infoText.text = message
        }
        else
        {
            // リソースIDが指定されていない場合は、タイトルを更新する
            parent.title = message
        }
    }

    /**
     * データを更新しないとき...なにもしない
     */
    override fun cancelTextEditDialog()
    {
        Log.v(TAG,"cancelTextEditDialog()")
    }

    companion object
    {
        private val TAG = TextEditReceiver::class.java.simpleName
    }
}