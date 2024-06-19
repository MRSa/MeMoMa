package jp.sourceforge.gokigen.memoma.io

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileSavingProcess.ISavingStatusHolder
import java.io.BufferedReader
import java.io.FileReader

/**
 * データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 */
class MeMoMaFileImportCsvProcess(
    private val context: Context,
    resultReceiver: IResultReceiver?,
    fileName: String?
) :
    AsyncTask<MeMoMaObjectHolder?, Int?, String>(), ISavingStatusHolder,
    MeMoMaFileSavingProcess.IResultReceiver {
    private val TAG = toString()
    private var receiver: IResultReceiver? = null

    private var targetFileName: String? = null
    private var fileSavedResult: String? = ""
    private var importingDialog: ProgressDialog? = null

    private var backgroundUri: String? = null
    private var userCheckboxString: String? = null

    /**
     * コンストラクタ
     */
    init {
        receiver = resultReceiver
        targetFileName = fileName

        //  プログレスダイアログ（「データインポート中...」）を表示する。
        importingDialog = ProgressDialog(context)
        importingDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        importingDialog!!.setMessage(context.getString(R.string.dataImporting))
        importingDialog!!.setIndeterminate(true)
        importingDialog!!.setCancelable(false)
        importingDialog!!.show()

        //  設定読み出し用...あらかじめ、UIスレッドで読みだしておく。
        val preferences = PreferenceManager.getDefaultSharedPreferences(
            context
        )
        backgroundUri = preferences.getString("backgroundUri", "")
        userCheckboxString = preferences.getString("userCheckboxString", "")
    }

    /**
     * 非同期処理実施前の前処理
     */
    override fun onPreExecute() {
    }

    /**
     * １レコード分のデータを読み込む。
     */
    private fun readRecord(buf: BufferedReader): String? {
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
    private fun parseRecord(dataLine: String, objectHolder: MeMoMaObjectHolder) {
        var detailIndex = 0
        var userCheckIndexTrue = 0
        var userCheckIndexFalse = 0
        var nextIndex = 0
        var label = ""
        var detail = ""
        var userChecked = false
        try {
            detailIndex = dataLine.indexOf("\",\"")
            if (detailIndex < 0) {
                Log.v(TAG, "parseRecord() : label wrong : $dataLine")
                return
            }
            label = dataLine.substring(1, detailIndex)
            userCheckIndexTrue = dataLine.indexOf("\",True,", detailIndex)
            userCheckIndexFalse = dataLine.indexOf("\",False,", detailIndex)
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
            val datas =
                dataLine.substring(nextIndex).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (datas.size < 6) {
                Log.v(TAG, "parseRecord() : data size wrong : " + datas.size)
                return
            }
            val drawStyle = datas[0].toInt()
            val paintStyle = datas[1]
            val centerX = datas[2].toFloat()
            val centerY = datas[3].toFloat()
            val width = datas[4].toFloat()
            val height = datas[5].toFloat()

            val left = centerX - (width / 2.0f)
            val top = centerY - (height / 2.0f)

            // オブジェクトのデータを作成する
            val pos = objectHolder.createPosition(left, top, drawStyle)
            if (pos == null) {
                Log.v(TAG, "parseRecord() : object create failure.")
                return
            }
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
     *
     */
    protected override fun doInBackground(vararg datas: MeMoMaObjectHolder?): String {
        // ファイル名の設定 ... (拡張子なし)
        val fileName = context.filesDir.toString() + "/exported/" + targetFileName

        // データを読み込む
        val result = importFromCsvFile(fileName, datas[0]!!)

        // データを保存する
        val savingEngine = MeMoMaFileSavingEngine(
            context,
            backgroundUri!!, userCheckboxString!!
        )
        val message = savingEngine.saveObjects(datas[0]!!)

        System.gc()

        return ("$result $message")
    }

    /**
     * 非同期処理の進捗状況の更新
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
            receiver?.onImportedResult("$result  $fileSavedResult")
            fileSavedResult = ""
        } catch (ex: Exception) {
            Log.v(TAG, "MeMoMaFileImportCsvProcess::onPostExecute() : " + ex.message)
        }
        // プログレスダイアログを消す
        importingDialog!!.dismiss()

        return
    }

    override fun onSavedResult(isError: Boolean, detail: String?) {
        fileSavedResult = detail
    }

    override var savingStatus: Boolean
        get() = (false)
        set(isSaving) {
        }

    /**
     * 結果報告用のインタフェース（積極的に使う予定はないけど...）
     */
    interface IResultReceiver {
        /**  保存結果の報告  */
        fun onImportedResult(fileName: String?)
    }
}