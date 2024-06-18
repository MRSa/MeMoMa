package jp.sourceforge.gokigen.memoma.io

import android.app.ProgressDialog
import android.content.ContentValues
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 * Viewの情報を画像形式（png形式）で保存する。
 * どのViewを保存するのかは、ICaptureExporter.getCaptureTargetView()クラスを使って教えてもらう。
 * AsyncTask
 * String       : 実行時に渡すクラス(Param)           : ファイル名をもらう
 * Integer    : 途中経過を伝えるクラス(Progress)   : 今回は使っていない
 * String      : 処理結果を伝えるクラス(Result)      : 結果を応答する。
 */
class ObjectLayoutCaptureExporter(
    private val context: AppCompatActivity,
    holder: MeMoMaObjectHolder,
    drawer: MeMoMaCanvasDrawer,
    resultReceiver: ICaptureLayoutExporter?
) :
    AsyncTask<String?, Int?, String?>() {
    private val TAG = toString()
    private val receiver = resultReceiver

    private val objectHolder = holder
    private val canvasDrawer = drawer
    private val savingDialog: ProgressDialog?
    private var offsetX = 0.0f
    private var offsetY = 0.0f
    private var displayWidth: Int
    private var displayHeight: Int
    private var exportedUri: Uri? = null

    /**
     * コンストラクタ
     */
    init {
        // 現在の画面サイズを取得
        val display = context.windowManager.defaultDisplay
        displayWidth = display.width
        displayHeight = display.height

        //  プログレスダイアログ（「保存中...」）を表示する。
        savingDialog = ProgressDialog(context)
        savingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        savingDialog.setMessage(context.getString(R.string.dataSaving))
        savingDialog.isIndeterminate = true
        savingDialog.setCancelable(false)
        savingDialog.show()

        // ファイルをバックアップするディレクトリを作成する
        val dir = File(context.filesDir.toString() + "/exported")
        if (!dir.mkdir()) {
            Log.v(TAG, "mkdir is failed.")
        }
    }

    /**
     * 非同期処理実施前の前処理
     */
    override fun onPreExecute() {
        // なにもしない。
    }

    /**
     * ビットマップデータを(PNG形式で)保管する。
     */
    private fun exportToFile(baseName: String, targetImage: Bitmap): String {
        var resultMessage = ""
        try {
            val outputDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + "/" + Main.APP_NAMESPACE + "/"
            val resolver = context.contentResolver
            val fileName = "$baseName.png"

            var outputStream: OutputStream? = null
            val extStorageUri: Uri
            var imageUri: Uri? = null
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + Main.APP_NAMESPACE)
                values.put(MediaStore.Images.Media.IS_PENDING, true)
                extStorageUri =
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                Log.v(TAG, "---------- $baseName.png $values")
                imageUri = resolver.insert(extStorageUri, values)
                if (imageUri != null) {
                    ////////////////////////////////////////////////////////////////
                    if (dumpLog) {
                        try {
                            val cursor = resolver.query(imageUri, null, null, null, null)
                            DatabaseUtils.dumpCursor(cursor)
                            cursor!!.close()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            resultMessage = e.message ?:""
                        }
                    }
                    ////////////////////////////////////////////////////////////////
                    try {
                        outputStream = resolver.openOutputStream(imageUri, "wa")
                    } catch (ee: Exception) {
                        ee.printStackTrace()
                    }
                } else {
                    Log.v(TAG, " cannot get imageUri...")
                }
            } else {
                val path = File(outputDir)
                if (!path.mkdir()) {
                    Log.v(TAG, " mkdir fail: $outputDir")
                }
                values.put(
                    MediaStore.Images.Media.DATA,
                    path.absolutePath + File.separator + fileName
                )
                val targetPath = File(outputDir + File.separator + fileName)
                try {
                    outputStream = FileOutputStream(targetPath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (outputStream != null) {
                targetImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                if (imageUri != null) {
                    resolver.update(imageUri, values, null, null)
                    exportedUri = imageUri
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            resultMessage = t.message?: ""
            exportedUri = null
        }
        return (resultMessage)
    }

    /**
     * キャンバスの大きさがどれくらい必要か、チェックする。
     */
    private fun checkCanvasSize(): Rect {
        val canvasSize = Rect()

        // オブジェクトの配置位置を探る。
        val keys = objectHolder.getObjectKeys()
        if (keys != null)
        {
            while (keys.hasMoreElements()) {
                val key = keys.nextElement()
                val pos = objectHolder.getPosition(key)
                if (pos != null)
                {
                    val posRect = pos.getRect()
                    if (canvasSize.left > posRect.left)
                    {
                        canvasSize.left = posRect.left.toInt()
                    }
                    if (canvasSize.right < posRect.right)
                    {
                        canvasSize.right = posRect.right.toInt()
                    }
                    if (canvasSize.top > posRect.top)
                    {
                        canvasSize.top = posRect.top.toInt()
                    }
                    if (canvasSize.bottom < posRect.bottom)
                    {
                        canvasSize.bottom = posRect.bottom.toInt()
                    }
                }
            }
        }


        // 描画領域にちょっと余裕を持たせる
        canvasSize.left = canvasSize.left - OUTPUT_MARGIN
        canvasSize.right = canvasSize.right + OUTPUT_MARGIN
        canvasSize.top = canvasSize.top - OUTPUT_MARGIN_TOP
        canvasSize.bottom = canvasSize.bottom + OUTPUT_MARGIN
        canvasSize.sort()

        // 現在の画面サイズを取得
        if (displayWidth < MINIMUM_WIDTH) {
            displayWidth = MINIMUM_WIDTH
        }
        if (displayHeight < MINIMUM_HEIGHT) {
            displayHeight = MINIMUM_HEIGHT
        }


        // 出力の最小サイズを(表示画面サイズに)設定
        if (canvasSize.width() < displayWidth) {
            canvasSize.right = canvasSize.left + displayWidth
        }
        if (canvasSize.height() < displayHeight) {
            canvasSize.bottom = canvasSize.top + displayHeight
        }

        // 画像位置（キャンバス位置）の調整。。。
        offsetX = 0.0f - canvasSize.left - (OUTPUT_MARGIN)
        offsetY = 0.0f - canvasSize.top - (OUTPUT_MARGIN)

        // 出力する画像データのサイズを表示する
        Log.v(
            TAG,
            "ObjectLayoutCaptureExporter::checkCanvasSize() w:" + canvasSize.width() + " , h:" + canvasSize.height() + "  offset :(" + offsetX + "," + offsetY + ")"
        )
        return (canvasSize)
    }

    /**
     * 非同期処理
     * （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     */
    protected override fun doInBackground(vararg datas: String?): String? {
        var result: String? = ""
        try {
            val canvasSize = checkCanvasSize()
            val targetBitmap =
                Bitmap.createBitmap(canvasSize.width(), canvasSize.height(), Bitmap.Config.RGB_565)
            val targetCanvas = Canvas(targetBitmap)

            // オブジェクトをビットマップの中に書き込む
            canvasDrawer.drawOnBitmapCanvas(targetCanvas, offsetX, offsetY)

            val fileName = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(Calendar.getInstance().time) + "_" + datas[0]

            // データを保管する
            result = exportToFile(fileName, targetBitmap)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
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
    override fun onPostExecute(result: String?) {
        try {
            receiver?.onCaptureLayoutExportedResult(exportedUri, result, OUTPUT_EXPORT_SHARE_ID)
        } catch (ex: Exception) {
            Log.v(TAG, "ViewCaptureExporter::onPostExecute() : " + ex.message)
            ex.printStackTrace()
        }
        // プログレスダイアログを消す
        savingDialog?.dismiss()
    }

    /**
     * 結果報告用のインタフェース
     */
    interface ICaptureLayoutExporter {
        //  保存結果の報告
        fun onCaptureLayoutExportedResult(exportedUri: Uri?, detail: String?, id: Int)
    }

    companion object {
        private const val dumpLog = false

        private const val OUTPUT_EXPORT_SHARE_ID = 1000
        private const val OUTPUT_MARGIN = 8
        private const val OUTPUT_MARGIN_TOP = 50

        private const val MINIMUM_WIDTH = 800
        private const val MINIMUM_HEIGHT = 600
    }
}