package jp.sourceforge.gokigen.memoma.operations

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import kotlin.math.floor

/**
 * オブジェクトの位置を整列するクラス (非同期処理を実行)
 * AsyncTask
 * MeMoMaObjectHolder : 実行時に渡すクラス(Param)
 * Integer    : 途中経過を伝えるクラス(Progress)
 * String     : 処理結果を伝えるクラス(Result)
 */
class ObjectAligner(context: Context, private val receiver: IAlignCallback?) :
    AsyncTask<MeMoMaObjectHolder?, Int?, String>() {
    private val TAG = toString()

    //  プログレスダイアログ（「保存中...」）を表示する。
    private val executingDialog = ProgressDialog(context)

    /**
     * コンストラクタ
     */
    init {
        executingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        executingDialog.setMessage(context.getString(R.string.dataAligning))
        executingDialog.isIndeterminate = true
        executingDialog.setCancelable(false)
        executingDialog.show()
    }

    /**
     * 非同期処理実施前の前処理
     *
     */
    override fun onPreExecute()
    {
        //
    }

    /**
     * 非同期処理
     * （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     *
     */
    override fun doInBackground(vararg datas: MeMoMaObjectHolder?): String
    {
        try
        {
            val objectHolder = datas[0]
            // オブジェクトの出力 （保持しているものはすべて表示する）
            if (objectHolder != null)
            {
                val keys = objectHolder.objectKeys
                while (keys.hasMoreElements())
                {
                    val key = keys.nextElement()
                    val pos = objectHolder.getPosition(key)
                    val posRect = pos.rect
                    val newLeft = floor((posRect.left + 15.0f) / 30.0)
                        .toFloat() * 30.0f
                    val newTop = floor((posRect.top + 15.0f) / 30.0)
                        .toFloat() * 30.0f
                    pos.setRectOffsetTo(newLeft, newTop)
                }
            }
            System.gc()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    /**
     * 非同期処理の進捗状況の更新
     */
    override fun onProgressUpdate(vararg values: Int?)
    {
        // 今回は何もしない
    }

    /**
     * 非同期処理の後処理
     * (結果を応答する)
     */
    override fun onPostExecute(result: String)
    {
        try
        {
            receiver?.objectAligned()
        }
        catch (ex: Exception)
        {
            Log.v(TAG, "ObjectAligner::onPostExecute() : $ex")
        }

        // プログレスダイアログを消す
        executingDialog.dismiss()
    }

    /**
     * 並べ変えたことを通知する
     */
    interface IAlignCallback
    {
        fun objectAligned()
    }
}