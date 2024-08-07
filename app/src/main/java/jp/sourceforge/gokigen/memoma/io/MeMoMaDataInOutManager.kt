package jp.sourceforge.gokigen.memoma.io

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.drawers.GokigenSurfaceView
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer
import jp.sourceforge.gokigen.memoma.holders.MeMoMaDataFileHolder
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileSavingProcess.ISavingStatusHolder
import jp.sourceforge.gokigen.memoma.io.ObjectLayoutCaptureExporter.ICaptureLayoutExporter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MeMoMaDataInOutManager(private val parent: AppCompatActivity) : ISavingStatusHolder,
    MeMoMaFileSavingProcess.IResultReceiver, MeMoMaFileLoadingProcess.IResultReceiver,
    ActionBar.OnNavigationListener, ICaptureLayoutExporter
{
    private lateinit var objectHolder: MeMoMaObjectHolder
    private lateinit var dataFileHolder: MeMoMaDataFileHolder

    private var isSaving = false
    private var isShareExportedData = false
    private var loadResultReceiver : ILoadResultReceiver? = null

    fun prepare(objectHolder: MeMoMaObjectHolder, bar: ActionBar?, fileName: String)
    {
        this.objectHolder = objectHolder

        // データファイルフォルダを更新する
        this.dataFileHolder = MeMoMaDataFileHolder(parent, android.R.layout.simple_spinner_dropdown_item, ".xml")
        val index = dataFileHolder.updateFileList(fileName)

        try
        {
            // アクションバーを設定する
            prepareActionBar(bar)

            // タイトルの設定を変更する
            if ((bar != null) && (index >= 0))
            {
                bar.setSelectedNavigationItem(index)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * データファイル一覧を更新し、アクションバーに反映させる
     *
     */
    fun updateFileList(titleName: String, bar: ActionBar?) {
        try
        {
            if (::dataFileHolder.isInitialized)
            {
                parent.runOnUiThread {
                    try
                    {
                        // データファイル一覧を更新する
                        val index = dataFileHolder.updateFileList(titleName)

                        // タイトルをオブジェクトフォルダに記憶させる
                        objectHolder.setDataTitle(titleName)

                        // タイトルの設定を変更する
                        if ((bar != null) && (index >= 0))
                        {
                            bar.setSelectedNavigationItem(index) // 実験...
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * データの保存を行う (同名のファイルが存在していた場合、 *.BAKにリネーム（上書き）してから保存する)
     */
    fun saveFile(dataTitle: String, forceOverwrite: Boolean, saveResult: ISaveResultReceiver? = null)
    {
        try
        {
            if (!::objectHolder.isInitialized)
            {
                Log.e(TAG, "ERR>MeMoMaDataInOutManager::saveFile() : $dataTitle")
                return
            }

            // タイトルをオブジェクトフォルダに記憶させる
            objectHolder.setDataTitle(dataTitle)
            val myObjectHolder = objectHolder
            val thread = Thread {
                // スレッドでファイルを保存する。。。
                Log.v(TAG, "MeMoMaDataInOutManager::saveFile() : '$dataTitle'")
                val message = saveFileSynchronous(myObjectHolder)
                parent.runOnUiThread {
                    onSavedResult((message.isNotEmpty()), message)
                }
                saveResult?.onSaved()
                Log.v(TAG, "MeMoMaDataInOutManager::saveFile() : $dataTitle : DONE.")
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * データファイルのフルパスを応答する
     */
    fun getDataFileFullPath(dataTitle: String, extension: String): String {
        return (parent.filesDir.toString() + "/" + dataTitle + extension)
    }

    /**  保存中状態を設定する  */
    override fun setSavingStatus(isSaving: Boolean) {
        this.isSaving = isSaving
    }

    /** 保存中状態を取得する  */
    override fun getSavingStatus(): Boolean {
        return (isSaving)
    }

    fun setLoadResultReceiver(receiver: ILoadResultReceiver?)
    {
        loadResultReceiver = receiver
    }

    /**
     * 保存終了時の処理
     */
    override fun onSavedResult(isError: Boolean, detail: String?)
    {
        try
        {
            // 保存したことを伝達する
            val outputMessage = "${parent.getString(R.string.save_data)} ${objectHolder.getDataTitle()} $detail"
            if (isError)
            {
                parent.runOnUiThread {
                    Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
                }
            }
            Log.v(TAG, outputMessage)

            // ファイルリスト更新 ... (ここでやっちゃあ、AsyncTaskにしている意味ないなあ...)
            dataFileHolder.updateFileList(objectHolder.getDataTitle())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 読み込み終了時の処理
     */
    override fun onLoadedResult(isError: Boolean, detail: String)
    {
        try
        {
            // 読み込みしたことを伝達する
            val outputMessage =
                parent.getString(R.string.load_data) + " " + objectHolder.getDataTitle() + " " + detail
            if (isError) {
                Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
            }
            Log.v(TAG, outputMessage)

            // 画面を再描画する
            val surfaceView : GokigenSurfaceView? = parent.findViewById(R.id.GraphicView)
            surfaceView?.doDraw()

            if (surfaceView == null)
            {
                loadResultReceiver?.onLoaded()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ファイルをロードする途中のバックグラウンド処理...
     */
    override fun onLoadingProcess() { }

    /**
     * ファイルからのデータ読み込み処理
     */
    private fun loadFileWithName(dataTitle: String)
    {
        try
        {
            // タイトルをオブジェクトフォルダに記憶させる
            objectHolder.setDataTitle(dataTitle)
            Log.v(TAG, "MeMoMaDataInOutManager::loadFile() : $dataTitle")

            // ファイルをロードする！
            val thread = Thread {
                try
                {
                    val fileLoader = MeMoMaFileLoadingProcess(parent, this)
                    val result = fileLoader.parseObjectFromXml(objectHolder)

                    // ----- Finish
                    parent.runOnUiThread {
                        try
                        {
                            fileLoader.onFinishProcess(result)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
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

    /**
     * アクションバーを更新する...
     */
    private fun prepareActionBar(bar: ActionBar?) {
        try
        {
            if (bar != null)
            {
                bar.navigationMode = ActionBar.NAVIGATION_MODE_LIST // リストを入れる
                bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE) // タイトルの表示をマスクする
                bar.setListNavigationCallbacks(dataFileHolder, this)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ファイルを保存する...同期型で。
     */
    private fun saveFileSynchronous(copyObjectHolder: MeMoMaObjectHolder): String
    {
        try
        {
            val myObjectHolder = copyObjectHolder
            // 同期型でファイルを保存する。。。
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val backgroundUri = preferences.getString("backgroundUri", "")?: ""
            val userCheckboxString = preferences.getString("userCheckboxString", "")?: ""
            val saveEngine = MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString)
            return (saveEngine.saveObjects(myObjectHolder))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return ("")
    }

    /**
     *
     *
     */
    override fun onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean
    {
        val data = dataFileHolder.getItem(itemPosition) ?: ""
        Log.v(TAG, "onNavigationItemSelected($itemPosition,$itemId) : $data")

        // 同期型で現在のファイルを保存する。。。
        val message = saveFileSynchronous(objectHolder)
        onSavedResult((message.isNotEmpty()), message)

        // 選択したファイル名をタイトルに反映し、またPreferenceにも記憶する
        parent.title = data
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        val editor = preferences.edit()
        editor.putString("MeMoMaInfo", data)
        editor.apply()

        // 選択したアイテムをロードする！
        loadFileWithName(data)

        return (true)
    }

    /**
     * スクリーンキャプチャを実施する
     */
    fun doScreenCapture(
        title: String?,
        holder: MeMoMaObjectHolder,
        drawer: MeMoMaCanvasDrawer,
        isShare: Boolean
    ) {
        isShareExportedData = isShare


        // AsyncTaskを使ってデータをエクスポートする
        val asyncTask = ObjectLayoutCaptureExporter(parent, holder, drawer, this)
        asyncTask.execute(title)
    }

    /**
     * ファイルのエクスポート結果を受け取る
     */
    override fun onCaptureLayoutExportedResult(exportedUri: Uri?, detail: String?, id: Int) {
        Log.v(
            TAG,
            "onCaptureLayoutExportedResult() '${objectHolder.getDataTitle()}' : $detail (${exportedUri}) $isShareExportedData"
        )
        try
        {
            // エクスポートしたことを伝達する
            val outputMessage = "${parent.getString(R.string.capture_data)} ${objectHolder.getDataTitle()} $detail"
/*
            if (isShareExportedData)
            {
                // エクスポートはできない、しない
                isShareExportedData = false
                outputMessage =
                    parent.getString(R.string.exported_picture_not_shared) + " : " + objectHolder.getDataTitle() + " " + detail
            }
*/
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
            if ((isShareExportedData)&&(exportedUri != null))
            {
                // ギャラリーに受信したファイルを登録し、エクスポートしたファイルを共有する
                shareContent(exportedUri, id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isShareExportedData = false
    }

    /**
     * エクスポートしたファイルを共有する
     */
    private fun shareContent(imageName: Uri, id: Int) {
        var message = ""
        try {
            // 現在の時刻を取得する
            val calendar = Calendar.getInstance()
            val outFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val date = outFormat.format(calendar.time)

            // メールタイトル
            val title =
                parent.getString(R.string.app_name) + " | " + objectHolder.getDataTitle() + " | " + date

            // メールの本文を構築する
            message = message + "Name : " + objectHolder.getDataTitle() + "\n"
            message = message + "exported : " + date + "\n"
            message = message + "number of objects : " + objectHolder.getCount() + "\n"

            // Share Intentを発行する。
            SharedIntentInvoker.shareContent(parent, id, title, message, imageName, "image/png")
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    interface ISaveResultReceiver
    {
        fun onSaved()
    }

    interface ILoadResultReceiver
    {
        fun onLoaded()
    }

    companion object
    {
        private val TAG = MeMoMaDataInOutManager::class.java.simpleName
    }
}
