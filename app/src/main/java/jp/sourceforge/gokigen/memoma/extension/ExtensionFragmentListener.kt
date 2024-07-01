package jp.sourceforge.gokigen.memoma.extension

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.AppSingleton
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.dialogs.FileSelectionDialog
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileExportCsvProcess
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileImportCsvProcess
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileLoadingProcess
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileSavingEngine
import jp.sourceforge.gokigen.memoma.io.SharedIntentInvoker
import jp.sourceforge.gokigen.memoma.listitem.SymbolListArrayAdapter
import jp.sourceforge.gokigen.memoma.listitem.SymbolListArrayItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * リスト形式で表示・エクスポート
 */
class ExtensionFragmentListener(private val parent: AppCompatActivity) : View.OnClickListener,
    MeMoMaFileLoadingProcess.IResultReceiver, MeMoMaFileExportCsvProcess.IResultReceiver,
    FileSelectionDialog.IResultReceiver,
    MeMoMaFileImportCsvProcess.IResultReceiver
{
    private val objectHolder = AppSingleton.objectHolder
    private var isShareExportedData = false
    private var listItems: MutableList<SymbolListArrayItem> = ArrayList()

    private val pickerLauncherForCSV =
        parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            if (result?.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.let { resultData ->
                    try
                    {
                        val uri = resultData.data
                        importObjectFromCsv(uri)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
        }

    private val pickerLauncherForXML =
        parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            if (result?.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.let { resultData ->
                    try
                    {
                        val uri = resultData.data
                        importDataFromXml(uri)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
        }

    fun setDataTitle(title: String)
    {
        var dataTitle = title
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val prefTitleString = preferences.getString("content_data_title", "") ?: ""
            if (prefTitleString.isNotEmpty())
            {
                // Preferenceに タイトル名が記録されていたら、上書きする
                dataTitle = prefTitleString
            }
            // Intentで拾ったデータを読み出す (初期化データ)
            objectHolder.setDataTitle(dataTitle)
        }
        catch (ex: Exception)
        {
            Log.v(TAG, "Exception :" + ex.message)
        }
    }

    private fun setPreferenceString(title: String)
    {
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val editor = preferences.edit()
            editor.putString("content_data_title", title)
            editor.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun prepareListener(myView : View)
    {
        try
        {
            // フィルタ設定ボタン
            myView.findViewById<ImageButton>(R.id.SetFilterButton)?.setOnClickListener(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun prepareToStart()
    {
        try
        {
            Log.v(TAG, "ExtensionActivityListener::prepareToStart() : ${objectHolder.getDataTitle()}")

            //  アクションバーを表示する
            val bar = parent.supportActionBar
            if (bar != null)
            {
                //bar.setIcon(R.drawable.icon1)
                bar.title = objectHolder.getDataTitle()
                bar.show()
            }

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
     * 詳細データを表示する。
     */
    private fun showDetailData(first: String, second: String, third: String)
    {
        Log.v(TAG, "SELECTED: $first $second $third")
    }

    fun shutdown()  { }

    private fun importObjectFromCsv(uri: Uri?)
    {
        try
        {
            if (uri == null)
            {
                Log.v(TAG, "importDataFromXml(): specified Uri is null...")
                return
            }

            val thread = Thread {
                try
                {
                    val importer = ExtensionCsvImport(parent, objectHolder, uri)
                    importer.importFromCsvFile()

                    val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                    val backgroundUri = preferences.getString("backgroundUri", "")?:""
                    val userCheckboxString = preferences.getString("userCheckboxString", "")?:""

                    // データの保管メイン
                    val savingEngine = MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString)
                    val result = savingEngine.saveObjects(objectHolder)

                    parent.runOnUiThread {
                        try
                        {
                            onImportedResult(result)
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

    private fun importDataFromXml(uri: Uri?)
    {
        try
        {
            if (uri == null)
            {
                Log.v(TAG, "importDataFromXml(): specified Uri is null...")
                return
            }

            val thread = Thread {
                val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                val backgroundUri = preferences.getString("backgroundUri", "")?:""
                val userCheckboxString = preferences.getString("userCheckboxString", "")?:""

                // データの保管を実施する (現状)
                val savingEngine = MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString)
                val result0 = savingEngine.saveObjects(objectHolder)
                Log.v(TAG, "Saved : $result0")

                val importer = ExtensionXmlImport(parent, objectHolder, uri)
                val result1 = importer.importFromXmlFile()

                // データの保管を実施する (新規)
                val savingEngine2 = MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString)
                val result = savingEngine2.saveObjects(objectHolder) + " " + result1
                Log.v(TAG, "=== Data Saved: ${objectHolder.getDataTitle()} result: $result1")
                parent.runOnUiThread {
                    try
                    {
                        // 読み込んだファイル名をタイトルに設定する
                        parent.title = objectHolder.getDataTitle()

                        // タイトルバーの更新...
                        val bar = parent.supportActionBar
                        if (bar != null) {
                            bar.setIcon(R.drawable.icon1)
                            bar.title = objectHolder.getDataTitle()
                            bar.show()
                        }
                        onImportedResultXml(result)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
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
     * クリックされたときの処理
     */
    override fun onClick(v: View)
    {
        try
        {
            if (v.id == R.id.SetFilterButton)
            {
                // フィルタ設定ボタンが押された！
                Log.v(TAG, "Selected Filter")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * メニューへのアイテム追加
     */
    fun onCreateOptionsMenu(menu: Menu): Menu
    {
        try {
            var menuItem =
                menu.add(Menu.NONE, MENU_ID_SHARE, Menu.NONE, parent.getString(R.string.export_csv))
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menuItem.setIcon(android.R.drawable.ic_menu_share)

            menuItem = menu.add(
                Menu.NONE,
                MENU_ID_EXPORT,
                Menu.NONE,
                parent.getString(R.string.shareContent)
            )
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            menuItem.setIcon(android.R.drawable.ic_menu_save)

            menuItem = menu.add(
                Menu.NONE,
                MENU_ID_IMPORT,
                Menu.NONE,
                parent.getString(R.string.import_csv)
            )
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.setIcon(android.R.drawable.ic_menu_edit)

            menuItem = menu.add(
                Menu.NONE,
                MENU_ID_EXPORT_XML,
                Menu.NONE,
                parent.getString(R.string.export_xml)
            )
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.setIcon(android.R.drawable.ic_menu_send)

            menuItem = menu.add(
                Menu.NONE,
                MENU_ID_IMPORT_XML,
                Menu.NONE,
                parent.getString(R.string.import_xml)
            )
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.setIcon(android.R.drawable.ic_menu_add)

            menuItem = menu.add(
                Menu.NONE,
                MENU_ID_DELETE,
                Menu.NONE,
                parent.getString(R.string.delete_content)
            )
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
            menuItem.setIcon(android.R.drawable.ic_menu_delete)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (menu)
    }

    /**
     * メニュー表示前の処理
     *
     */
    fun onPrepareOptionsMenu(menu: Menu)
    {
        try
        {
            menu.findItem(MENU_ID_SHARE).setVisible(true)
            menu.findItem(MENU_ID_EXPORT).setVisible(true)
            menu.findItem(MENU_ID_EXPORT_XML).setVisible(true)
            menu.findItem(MENU_ID_IMPORT).setVisible(true)
            menu.findItem(MENU_ID_IMPORT_XML).setVisible(true)
            menu.findItem(MENU_ID_DELETE).setVisible(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * メニューのアイテムが選択されたときの処理
     *
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        val result: Boolean
        when (item.itemId) {
            MENU_ID_EXPORT -> {
                // 表示中データのエクスポート
                exportAsCsv(false)
                result = true
            }

            MENU_ID_EXPORT_XML -> {
                // 表示中データのエクスポート
                exportAsXml()
                result = true
            }

            MENU_ID_SHARE -> {
                exportAsCsv(true)
                result = true
            }

            MENU_ID_IMPORT -> {
                // データのインポート(CSV形式)
                callPickImportObject(PICK_CSV_FILE)
                result = true
            }

            MENU_ID_IMPORT_XML -> {
                // データのインポート(XML形式)
                callPickImportObject(PICK_XML_FILE)
                result = true
            }

            MENU_ID_DELETE -> {
                // データの削除
                deleteContent()
                result = true
            }

            else -> result = false
        }
        return (result)
    }

    /**
     * データを削除する
     */
    private fun deleteContent() {
        try {
            //  データの一覧を取得する
            val dialog =
                FileSelectionDialog(parent, parent.getString(R.string.delete_content),
                    ".xml"
                ) { fileName: String? ->
                    // fileNameのファイルを削除する...
                    val thread = Thread {
                        try
                        {
                            // ファイル削除の実処理
                            val targetFile = parent.filesDir.toString() + "/" + fileName
                            if (!(File(targetFile).delete())) {
                                Log.v(TAG, "Content Delete Failure : $fileName")
                            }
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                    try
                    {
                        // 削除実処理の実行
                        thread.start()
                        parent.runOnUiThread {
                            val outputMessage =
                                parent.getString(R.string.delete_content) + " " + fileName
                            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            dialog.prepare()
            dialog.dialog?.show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * データをインポートする(Intentを呼び出す)
     */
    private fun callPickImportObject(requestCode: Int)
    {
        try
        {
            val intent: Intent
            // ファイル選択のダイアログを取得する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            else
            {
                intent = Intent(Intent.ACTION_GET_CONTENT)
            }
            intent.setType("text/*")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                val path =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + Main.APP_NAMESPACE + "/"
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, path)
            }

            when (requestCode) {
                PICK_CSV_FILE -> {
                    pickerLauncherForCSV.launch(intent)
                }
                PICK_XML_FILE -> {
                    pickerLauncherForXML.launch(intent)
                }
                else -> {
                    Log.v(TAG, "========== requestCode: $requestCode")
                }
            }
        }
        catch (e: Exception)
        {
            val message =
                " " + parent.getString(R.string.intent_call_error) + " " + e.message + " ID: $requestCode"
            Toast.makeText(parent, message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    /**
     * データをCSV形式で出力する
     */
    private fun exportAsCsv(isShare: Boolean)
    {
        isShareExportedData = isShare

        // AsyncTaskを使ってデータをエクスポートする
        val asyncTask = MeMoMaFileExportCsvProcess(parent, this)
        asyncTask.execute(objectHolder)
    }

    /**
     * データをXML形式で出力する
     */
    private fun exportAsXml()
    {
        try
        {
            val thread = Thread {
                try
                {
                    val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                    val backgroundUri = preferences.getString("backgroundUri", "") ?: ""
                    val userCheckboxString = preferences.getString("userCheckboxString", "") ?: ""
                    val exporter = ExtensionXmlExport(parent, objectHolder, backgroundUri, userCheckboxString)
                    val result = exporter.exportToXmlFile()
                    parent.runOnUiThread {
                        try
                        {
                            onExportedResultXml(result)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }
                catch (ee: Exception)
                {
                    ee.printStackTrace()
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
     * ファイルをロードする途中のバックグラウンド処理...
     */
    override fun onLoadingProcess()
    {
        try
        {
            // リストに表示するアイテムを生成する
            listItems = ArrayList()

            // TODO: 何らかの法則に従って並べ替えをする。
            val keys = objectHolder.getObjectKeys()
            if (keys != null)
            {
            while (keys.hasMoreElements())
            {
                val key = keys.nextElement()
                val pos = objectHolder.getPosition(key)

                if (pos != null)
                {
                    // アイコンの決定
                    val objectStyleIcon =
                        MeMoMaObjectHolder.getObjectDrawStyleIcon(pos.getDrawStyle())

                    // ユーザチェックの有無表示
                    val userCheckedIcon =
                        if ((pos.getUserChecked())) R.drawable.btn_checked else R.drawable.btn_notchecked

                    // TODO: アイテム選択時の情報エリアは(ArrayItem側には)用意しているが未使用。
                    val listItem =
                        SymbolListArrayItem(
                            userCheckedIcon,
                            pos.getLabel(),
                            pos.getDetail(),
                            "",
                            objectStyleIcon
                        )

                    listItems.add(listItem)
                }
            }
            }
        }
        catch (ex: Exception)
        {
            // 例外発生...ログを吐く
            Log.v(TAG, "ExtensionActivityListener::onLoadingProcess() : " + ex.message)
            ex.printStackTrace()
        }
    }

    /**
     * ファイルのロード結果を受け取る
     *
     */
    override fun onLoadedResult(isError: Boolean, detail: String)
    {
        try
        {
            Log.v(
                TAG,
                "ExtensionActivityListener::onLoadedResult() '" + objectHolder.getDataTitle() + "' : " + detail
            )

            // 読み込んだファイル名をタイトルに設定する
            parent.title = objectHolder.getDataTitle()

            // オブジェクト一覧を表示する
            updateObjectList()

            // 読み込みしたことを伝達する
            //String outputMessage = parent.getString(R.string.load_data) + " " + objectHolder.getDataTitle() + " " + detail;
            //Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ファイルのエクスポート結果を受け取る
     *
     */
    override fun onExportedResult(documentUri: Uri?, detail: String?)
    {
        try
        {
            Log.v(TAG, "ExtensionActivityListener::onExportedResult() '${objectHolder.getDataTitle()}' : $detail")

            // エクスポートしたことを伝達する
            val outputMessage = parent.getString(R.string.export_csv) + " " + objectHolder.getDataTitle() + " " + detail
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
            if (isShareExportedData)
            {
                // エクスポートしたファイルを共有する
                shareContent(documentUri!!)
            }
            isShareExportedData = false
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクト一覧を更新する
     */
    private fun updateObjectList()
    {
        try
        {
            // リストアダプターを生成し、設定する
            val listView = parent.findViewById<ListView>(R.id.ExtensionView)
            val adapter: ListAdapter = SymbolListArrayAdapter(
                parent, R.layout.listarrayitems,
                listItems
            )
            listView.adapter = adapter

            // アイテムを選択したときの処理
            listView.onItemClickListener =
                AdapterView.OnItemClickListener { parentView: AdapterView<*>, _, position: Int, _ ->
                    val listView1 = parentView as ListView
                    val item = listView1.getItemAtPosition(position) as SymbolListArrayItem

                    /// リストが選択されたときの処理...データを開く
                    showDetailData(
                        item.getTextResource1st(),
                        item.getTextResource2nd(),
                        item.getTextResource3rd()
                    )
                }
            System.gc() // いらない（参照が切れた）クラスを消したい
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * エクスポートしたファイルを共有する
     */
    private fun shareContent(documentUri: Uri) {
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
            SharedIntentInvoker.shareContent(
                parent,
                MENU_ID_SHARE,
                title,
                message,
                documentUri,
                "text/plain"
            )
        }
        catch (ex: Exception)
        {
            Log.v(TAG, "shareContent (fileName : " + objectHolder.getDataTitle() + ")")
            ex.printStackTrace()
        }
    }

    /**
     * ファイルが選択された！
     *
     */
    override fun selectedFileName(fileName: String?)
    {
        // CSVファイルからオブジェクトをロードするクラスを呼び出す。
        Log.v(TAG, "ExtensionActivityListener::selectedFileName() : $fileName")
        val asyncTask = MeMoMaFileImportCsvProcess(parent, this, fileName)
        asyncTask.execute(objectHolder)
    }

    /**
     * インポート結果の受信
     */
    override fun onImportedResult(fileName: String)
    {
        Log.v(TAG, "ExtensionActivityListener::onImportedResult() '${objectHolder.getDataTitle()}' : $fileName")
        try
        {
            // インポートしたことを伝達する
            val outputMessage = "${parent.getString(R.string.import_csv)}  ${objectHolder.getDataTitle()} : $fileName"
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()

            // 一覧のリストを作りなおす
            onLoadingProcess()
            updateObjectList()

            // -----
            Log.v(TAG, "imported : '${objectHolder.getDataTitle()}'.")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onImportedResultXml(detail: String)
    {
        try
        {
            val title = objectHolder.getDataTitle()
            Log.v(TAG, "ExtensionActivityListener::onImportedResultXml() '$title' : $detail")

            // インポート時の注意事項ダイアログを表示する
            parent.runOnUiThread {
                try
                {
                    //  ダイアログで情報を表示する。
                    AlertDialog.Builder(parent)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(parent.getString(R.string.import_xml) + " (" + objectHolder.getDataTitle() + ")")
                        .setMessage(parent.getString(R.string.import_xml_information))
                        .setPositiveButton(parent.getString(R.string.confirmYes), null)
                        .show()

                    // インポートしたことを伝達する
                    val outputMessage =
                        parent.getString(R.string.import_xml) + " " + objectHolder.getDataTitle() + " " + detail
                    Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            // タイトルを更新
            setPreferenceString(title)

            // 一覧のリストを作りなおす
            onLoadingProcess()
            updateObjectList()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onExportedResultXml(detail: String)
    {
        try
        {
            Log.v(TAG, "ExtensionActivityListener::onExportedResultXml() '${objectHolder.getDataTitle()}' : $detail")
            // エクスポートしたことを伝達する
            val outputMessage = parent.getString(R.string.export_xml) + " " + objectHolder.getDataTitle() + " " + detail
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = ExtensionFragmentListener::class.java.simpleName
        private const val PICK_CSV_FILE = 2020
        private const val PICK_XML_FILE = 2030

        private const val MENU_ID_EXPORT = (Menu.FIRST + 1)
        private const val MENU_ID_EXPORT_XML = (Menu.FIRST + 2)
        private const val MENU_ID_SHARE = (Menu.FIRST + 3)
        private const val MENU_ID_IMPORT = (Menu.FIRST + 4)
        private const val MENU_ID_IMPORT_XML = (Menu.FIRST + 5)
        private const val MENU_ID_DELETE = (Menu.FIRST + 6)
    }
}