package jp.sourceforge.gokigen.memoma.io

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 */
class MeMoMaFileExportCsvProcess(
    private val context: Context,
    private val receiver: IResultReceiver?
) :
    AsyncTask<MeMoMaObjectHolder?, Int?, String>() {
    private val TAG = toString()
    private var documentUri: Uri? = null

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
    }

    /**
     * 非同期処理実施前の前処理
     *
     */
    override fun onPreExecute() {
    }

    /**
     * データを(CSV形式で)保管する。
     *
     */
    private fun exportToCsvFile(baseName: String, objectHolder: MeMoMaObjectHolder): String {
        var resultMessage = ""
        try {
            val outputDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + Main.APP_NAMESPACE + "/"
            val resolver = context.contentResolver

            // エクスポートするファイル名を決定する
            val calendar = Calendar.getInstance()
            val outFormat = SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.US)
            val exportedFileName = outFormat.format(calendar.time) + baseName + ".csv"

            val extStorageUri: Uri
            val writer: OutputStreamWriter
            val values = ContentValues()
            values.put(MediaStore.Downloads.TITLE, exportedFileName)
            values.put(MediaStore.Downloads.DISPLAY_NAME, exportedFileName)
            values.put(MediaStore.Downloads.MIME_TYPE, "text/csv") // text/plain or text/csv
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/" + Main.APP_NAMESPACE)
                values.put(MediaStore.Downloads.IS_PENDING, true)
                extStorageUri =
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

                Log.v(TAG, "---------- $exportedFileName $values")

                documentUri = resolver.insert(extStorageUri, values)

                if (documentUri == null) {
                    resultMessage = "documentUri is NULL."
                    return (resultMessage)
                }
                val outputStream = resolver.openOutputStream(documentUri!!, "wa")
                writer = OutputStreamWriter(outputStream)
            } else {
                val path = File(outputDir)
                path.mkdir()
                values.put(
                    MediaStore.Downloads.DATA,
                    path.absolutePath + File.separator + exportedFileName
                )
                val targetFile = File(outputDir + File.separator + exportedFileName)
                val outputStream = FileOutputStream(targetFile)
                writer = OutputStreamWriter(outputStream)
            }

            //  データのタイトルを出力
            var str =
                "; label,detail,userChecked,shape,style,centerX,centerY,width,height,;!<_$ (';!<_$' is a record Separator)\r\n"
            writer.write(str)

            // オブジェクトの出力 （保持しているものをすべて表示する）
            val keys = objectHolder.getObjectKeys()
            while (keys!!.hasMoreElements()) {
                val key = keys.nextElement()
                val pos = objectHolder.getPosition(key)
                val posRect = pos!!.getRect()

                // TODO:  絞り込み条件がある場合には、その条件に従ってしぼり込む必要あり。
                str = ""
                str = str + "\"" + pos.getLabel() + "\""
                str = str + ",\"" + pos.getDetail() + "\""
                str = if (pos.getUserChecked()) {
                    "$str,True"
                } else {
                    "$str,False"
                }
                str = str + "," + pos.getDrawStyle() // オブジェクトの形状
                str = str + "," + pos.getPaintStyle() // オブジェクトの塗りつぶし状態
                str = str + "," + (Math.round(posRect.centerX() * 100.0f) / 100.0f)
                str = str + "," + (Math.round(posRect.centerY() * 100.0f) / 100.0f)
                str = str + "," + (Math.round(posRect.width() * 100.0f) / 100.0f)
                str = str + "," + (Math.round(posRect.height() * 100.0f) / 100.0f)
                str = "$str,;!<_$\r\n"
                writer.write(str)
            }
            writer.flush()
            writer.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Downloads.IS_PENDING, false)
                resolver.update(documentUri!!, values, null, null)
            }
        } catch (e: Exception) {
            resultMessage = " ERR " + e.message + " " + documentUri
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
        // データを保管する
        val result = exportToCsvFile(datas[0]!!.getDataTitle(), datas[0]!!)

        System.gc()
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
            receiver?.onExportedResult(documentUri, result)
        } catch (ex: Exception) {
            Log.v(TAG, "MeMoMaFileExportCsvProcess::onPostExecute() : $ex")
        }
        // プログレスダイアログを消す
        savingDialog.dismiss()
    }

    /**
     * 結果報告用のインタフェース（積極的に使う予定はないけど...）
     *
     * @author MRSa
     */
    interface IResultReceiver {
        /**  保存結果の報告  */
        fun onExportedResult(documentUri: Uri?, detail: String?)
    }
}