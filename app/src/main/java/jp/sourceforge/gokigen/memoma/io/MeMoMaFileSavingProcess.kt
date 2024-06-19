package jp.sourceforge.gokigen.memoma.io

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder

/**
 * データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 * AsyncTask
 * MeMoMaObjectHolder : 実行時に渡すクラス(Param)
 * Integer    : 途中経過を伝えるクラス(Progress)
 * String     : 処理結果を伝えるクラス(Result)
 *
 * @author MRSa
 */
class MeMoMaFileSavingProcess(
    private val context: Context,
    private val statusHolder: ISavingStatusHolder,
    private val receiver: IResultReceiver?
) :
    AsyncTask<MeMoMaObjectHolder?, Int?, String>() {

    private val backgroundUri: String?
    private val userCheckboxString: String?

    //  プログレスダイアログ（「保存中...」）を表示する。
    private val savingDialog = ProgressDialog(context)

    /**
     * コンストラクタ
     */
    init {
        savingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        savingDialog.setMessage(context.getString(R.string.dataSaving))
        savingDialog.isIndeterminate = true
        savingDialog.setCancelable(false)
        savingDialog.show()

        //  設定読み出し用...あらかじめ、UIスレッドで読みだしておく。
        val preferences = PreferenceManager.getDefaultSharedPreferences(
            context
        )
        backgroundUri = preferences.getString("backgroundUri", "")
        userCheckboxString = preferences.getString("userCheckboxString", "")


        // 未保管状態にリセットする
        statusHolder.setSavingStatus(false)
    }

    /**
     * 非同期処理実施前の前処理
     *
     */
    override fun onPreExecute() {
        // 未保管状態にリセットする
        statusHolder.setSavingStatus(false)
    }

    /**
     * 非同期処理
     * （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     */
    protected override fun doInBackground(vararg datas: MeMoMaObjectHolder?): String {
        // 保管中状態を設定する
        statusHolder.setSavingStatus(true)

        // データの保管メイン
        val savingEngine = MeMoMaFileSavingEngine(context, backgroundUri!!, userCheckboxString!!)
        val result = savingEngine.saveObjects(datas[0]!!)

        System.gc()

        // 未保管状態にリセットする
        statusHolder.setSavingStatus(false)

        return (result)
    }

    /**
     * 非同期処理の進捗状況の更新
     *
     */
    protected override fun onProgressUpdate(vararg values: Int?) {
        // 今回は何もしない
    }

    /**
     * 非同期処理の後処理
     * (結果を応答する)
     */
    override fun onPostExecute(result: String) {
        try {
            receiver?.onSavedResult(!(result.isEmpty()), result)
        } catch (ex: Exception) {
            Log.v(TAG, "MeMoMaFileSavingProcess::onPostExecute() : " + ex.message)
        }
        // プログレスダイアログを消す
        savingDialog.dismiss()

        // 未保管状態にセットする
        statusHolder.setSavingStatus(false)
        return
    }

    /**
     * 結果報告用のインタフェース（積極的に使う予定はないけど...）
     */
    interface IResultReceiver {
        // 保存結果の報告
        fun onSavedResult(isError: Boolean, detail: String?)
    }

    /**
     * ファイル保存実施状態を記憶するインタフェースクラス
     */
    interface ISavingStatusHolder {
        fun getSavingStatus(): Boolean
        fun setSavingStatus(isSaving: Boolean)
    }
    companion object
    {
        private val TAG = MeMoMaFileSavingProcess::class.java.simpleName
    }

}