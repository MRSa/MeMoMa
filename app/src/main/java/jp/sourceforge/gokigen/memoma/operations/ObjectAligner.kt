package jp.sourceforge.gokigen.memoma.operations

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import kotlin.math.floor

/**
 * オブジェクトの位置を整列するクラス (非同期処理を実行)
 */
class ObjectAligner(private val parent: AppCompatActivity, private val receiver: IAlignCallback?)
{
    //  プログレスダイアログ（「保存中...」）を表示する。
    private val executingDialog = ProgressDialog(parent)

    /**
     * コンストラクタ
     */
    init {
        executingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        executingDialog.setMessage(parent.getString(R.string.dataAligning))
        executingDialog.isIndeterminate = true
        executingDialog.setCancelable(false)
        executingDialog.show()
    }

    fun doAlignObject(objectHolder: MeMoMaObjectHolder?)
    {
        try
        {
            // オブジェクトの出力 （保持しているものはすべて表示する）
            if (objectHolder != null)
            {
                val keys = objectHolder.getObjectKeys()
                if (keys != null)
                {
                    while (keys.hasMoreElements())
                    {
                        val key = keys.nextElement()
                        val pos = objectHolder.getPosition(key)
                        if (pos != null)
                        {
                            val posRect = pos.getRect()
                            val newLeft = floor((posRect.left + 15.0f) / 30.0)
                                .toFloat() * 30.0f
                            val newTop = floor((posRect.top + 15.0f) / 30.0)
                                .toFloat() * 30.0f
                            pos.setRectOffsetTo(newLeft, newTop)
                        }
                    }
                }
            }
            parent.runOnUiThread {
                try
                {
                    executingDialog.dismiss()
                    receiver?.objectAligned()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            System.gc()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 並べ変えたことを通知する
     */
    interface IAlignCallback
    {
        fun objectAligned()
    }

    companion object
    {
        private val TAG = ObjectAligner::class.java.simpleName
    }
}
