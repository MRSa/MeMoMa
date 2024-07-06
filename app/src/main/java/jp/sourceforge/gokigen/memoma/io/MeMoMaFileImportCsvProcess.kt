package jp.sourceforge.gokigen.memoma.io

import android.app.ProgressDialog
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileSavingProcess.ISavingStatusHolder
import java.io.BufferedReader
import java.io.FileReader

/**
 * データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 */
class MeMoMaFileImportCsvProcess(
    private val parent: AppCompatActivity,
    private val resultReceiver: IResultReceiver?,
    private val targetFileName: String
) : ISavingStatusHolder, MeMoMaFileSavingProcess.IResultReceiver
{
    private var fileSavedResult: String? = ""
    private var importingDialog: ProgressDialog = ProgressDialog(parent)

    private var backgroundUri: String
    private var userCheckboxString: String

    /**
     * コンストラクタ
     */
    init
    {
        //  設定読み出し用...あらかじめ、UIスレッドで読みだしておく。
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        backgroundUri = preferences.getString("backgroundUri", "") ?: ""
        userCheckboxString = preferences.getString("userCheckboxString", "") ?: ""
    }

    private fun prepareExecute()
    {
        try
        {
            //  プログレスダイアログ（「データインポート中...」）を表示する。
            importingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            importingDialog.setMessage(parent.getString(R.string.dataImporting))
            importingDialog.setIndeterminate(true)
            importingDialog.setCancelable(false)
            importingDialog.show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun execute(data: MeMoMaObjectHolder)
    {
        try
        {
            val thread = Thread {
                try
                {
                    parent.runOnUiThread {
                        prepareExecute()
                    }
                    val result = doInBackground(data)
                    parent.runOnUiThread {
                        finishExecute(result)
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun finishExecute(result: String)
    {
        try
        {
            try
            {
                resultReceiver?.onImportedResult("$result $fileSavedResult")
                fileSavedResult = ""
            }
            catch (ex: Exception)
            {
                Log.v(TAG, "MeMoMaFileImportCsvProcess::onPostExecute() : " + ex.message)
            }
            // プログレスダイアログを消す
            importingDialog.dismiss()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * １レコード分のデータを読み込む。
     */
    private fun readRecord(buf: BufferedReader): String?
    {
        var oneRecord: String? = null
        try {
            var oneLine = buf.readLine()
            while (oneLine != null) {
                oneRecord = if ((oneRecord == null)) oneLine else oneRecord + oneLine
                if (oneRecord.indexOf(",;!<_$") > 0) {
                    // レコード末尾が見つかったので break する。
                    break
                }
                // 次の行を読みだす。
                oneLine = buf.readLine()
            }
        } catch (ex: Exception) {
            //
            Log.v(TAG, "CSV:readRecord() ex : $ex")
            oneRecord = null
        }
        return (oneRecord)
    }

    /**
     * 1レコード分のデータを区切る
     */
    private fun parseRecord(dataLine: String, objectHolder: MeMoMaObjectHolder)
    {
        val nextIndex: Int
        val detail : String
        val userChecked: Boolean
        try
        {
            val detailIndex = dataLine.indexOf("\",\"")
            if (detailIndex < 0) {
                Log.v(TAG, "parseRecord() : label wrong : $dataLine")
                return
            }
            val label = dataLine.substring(1, detailIndex)
            val userCheckIndexTrue = dataLine.indexOf("\",True,", detailIndex)
            val userCheckIndexFalse = dataLine.indexOf("\",False,", detailIndex)
            if (userCheckIndexFalse > detailIndex) {
                //
                detail = dataLine.substring(detailIndex + 3, userCheckIndexFalse)
                userChecked = false
                nextIndex = userCheckIndexFalse + 8 // 8は、 ",False, を足した数
            } else if (userCheckIndexTrue > detailIndex) {
                //
                detail = dataLine.substring(detailIndex + 3, userCheckIndexTrue)
                userChecked = true
                nextIndex = userCheckIndexTrue + 7 // 7は、 ",True,  を足した数
            } else  // if ((userCheckIndexTrue <= detailIndex)&&(userCheckIndexFalse <= detailIndex))
            {
                Log.v(TAG, "parseRecord() : detail wrong : $dataLine")
                return
            }

            //  残りのデータを切り出す。
            val data =
                dataLine.substring(nextIndex).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (data.size < 6) {
                Log.v(TAG, "parseRecord() : data size wrong : " + data.size)
                return
            }
            val drawStyle = data[0].toInt()
            val paintStyle = data[1]
            val centerX = data[2].toFloat()
            val centerY = data[3].toFloat()
            val width = data[4].toFloat()
            val height = data[5].toFloat()

            val left = centerX - (width / 2.0f)
            val top = centerY - (height / 2.0f)

            // オブジェクトのデータを作成する
            val pos = objectHolder.createPosition(left, top, drawStyle)
            pos.setRectRight(left + width)
            pos.setRectBottom(top + height)
            pos.setLabel(label)
            pos.setDetail(detail)
            pos.setPaintStyle(paintStyle)
            pos.setUserChecked(userChecked)
            Log.v(TAG, "OBJECT CREATED: $label($left,$top) [$drawStyle]")
        } catch (ex: Exception) {
            Log.v(TAG, "parseRecord() $ex")
        }
    }

    /**
     * (CSV形式の)データを読み込んで格納する。
     */
    private fun importFromCsvFile(fileName: String, objectHolder: MeMoMaObjectHolder): String {
        var resultMessage = ""
        try {
            Log.v(TAG, "CSV(import)>> $fileName")
            val buf = BufferedReader(FileReader(fileName))
            var dataLine = readRecord(buf)
            while (dataLine != null) {
                if (!dataLine.startsWith(";")) {
                    // データ行だった。ログに出力する！
                    parseRecord(dataLine, objectHolder)
                }
                // 次のデータ行を読み出す
                dataLine = readRecord(buf)
            }
        } catch (e: Exception) {
            resultMessage = " ERR(import) " + e.message
            Log.v(TAG, resultMessage)
            e.printStackTrace()
        }
        return (resultMessage)
    }

    /**
     * 非同期処理
     * （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     */
    private fun doInBackground(data: MeMoMaObjectHolder): String
    {
        var resultString = ""
        try
        {
            // ファイル名の設定 ... (拡張子なし)
            val fileName = parent.filesDir.toString() + "/exported/" + targetFileName

            // データを読み込む
            val result = importFromCsvFile(fileName, data)

            // データを保存する
            val savingEngine = MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString)
            val message = savingEngine.saveObjects(data)
            System.gc()
            resultString = "$result $message"
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            resultString = " IMPORT ERRROR "
        }
        return (resultString)
    }

    override fun onSavedResult(isError: Boolean, detail: String?)
    {
        fileSavedResult = detail
    }

    private var savingStatus = false
    override fun getSavingStatus(): Boolean { return false }
    override fun setSavingStatus(isSaving: Boolean) { savingStatus = isSaving }

    /**
     * 結果報告用のインタフェース（積極的に使う予定はないけど...）
     */
    interface IResultReceiver
    {
        fun onImportedResult(fileName: String) // 保存結果の報告
    }
    companion object
    {
        private val TAG = MeMoMaFileImportCsvProcess::class.java.simpleName
    }
}