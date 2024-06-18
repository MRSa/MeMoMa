package jp.sourceforge.gokigen.memoma

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.dialogs.ConfirmationDialog
import jp.sourceforge.gokigen.memoma.dialogs.CreditDialog
import jp.sourceforge.gokigen.memoma.dialogs.ItemSelectionDialog
import jp.sourceforge.gokigen.memoma.dialogs.ItemSelectionDialog.ISelectionItemReceiver
import jp.sourceforge.gokigen.memoma.dialogs.TextEditDialog
import jp.sourceforge.gokigen.memoma.dialogs.TextEditDialog.ITextEditResultReceiver
import jp.sourceforge.gokigen.memoma.drawers.GokigenSurfaceView
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer
import jp.sourceforge.gokigen.memoma.extension.ExtensionActivity
import jp.sourceforge.gokigen.memoma.holders.LineStyleHolder
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.holders.OperationModeHolder
import jp.sourceforge.gokigen.memoma.io.MeMoMaDataInOutManager
import jp.sourceforge.gokigen.memoma.operations.IObjectSelectionReceiver
import jp.sourceforge.gokigen.memoma.operations.ObjectAligner
import jp.sourceforge.gokigen.memoma.operations.ObjectAligner.IAlignCallback
import jp.sourceforge.gokigen.memoma.operations.ObjectDataInputDialog
import jp.sourceforge.gokigen.memoma.operations.ObjectOperationCommandHolder
import jp.sourceforge.gokigen.memoma.operations.SelectLineShapeDialog
import jp.sourceforge.gokigen.memoma.preference.Preference

/**
 * メモま！ のメイン画面処理
 *
 */
class MeMoMaListener internal constructor(private val parent: AppCompatActivity, private val dataInOutManager: MeMoMaDataInOutManager) :
    View.OnClickListener, OnTouchListener, View.OnKeyListener,
    IObjectSelectionReceiver, ConfirmationDialog.ConfirmationCallback,
    ObjectDataInputDialog.IResultReceiver,
    ISelectionItemReceiver, ITextEditResultReceiver, IAlignCallback,
    SelectLineShapeDialog.IResultReceiver,
    IListener
{
    private lateinit var objectDrawer: MeMoMaCanvasDrawer

    private val editTextDialog: TextEditDialog = TextEditDialog(parent, R.drawable.icon)
    private val objectHolder: MeMoMaObjectHolder = MeMoMaObjectHolder(parent)
    private val drawModeHolder: OperationModeHolder = OperationModeHolder(parent)
    private val lineStyleHolder: LineStyleHolder = LineStyleHolder(parent)
    private val confirmationDialog: ConfirmationDialog
    private val objectDataInputDialog: ObjectDataInputDialog
    private val lineSelectionDialog: SelectLineShapeDialog
    private val itemSelectionDialog: ItemSelectionDialog
    private var selectedObjectKey = 0
    private var objectKeyToDelete = 0
    private var selectedContextKey = 0

    /**
     * コンストラクタ
     *
     */
    init
    {
        lineStyleHolder.prepare()

        // 確認ダイアログ
        confirmationDialog = ConfirmationDialog.newInstance(parent)

        // オブジェクトのデータ入力ダイアログを生成
        objectDataInputDialog = ObjectDataInputDialog(parent, objectHolder)
        objectDataInputDialog.setResultReceiver(this)

        // 接続線の形状と太さを選択するダイアログを生成
        lineSelectionDialog = SelectLineShapeDialog(parent, lineStyleHolder)
        lineSelectionDialog.setResultReceiver(this)

        // アイテム選択ダイアログを生成
        val commandHolder = ObjectOperationCommandHolder(parent)
        itemSelectionDialog = ItemSelectionDialog(parent)
        itemSelectionDialog.prepare(
            this,
            commandHolder,
            parent.getString(R.string.object_operation)
        )

    }

    // イベントリスナの登録
    override fun prepareListener(view: View)
    {
        // ボタンクリック時のイベントを受信する設定...
        (view.findViewById<ImageButton>(R.id.HomeButton)).setOnClickListener(this)
        (view.findViewById<ImageButton>(R.id.ExpandButton)).setOnClickListener(this)
        (view.findViewById<ImageButton>(R.id.CreateObjectButton)).setOnClickListener(this)
        (view.findViewById<ImageButton>(R.id.DeleteObjectButton)).setOnClickListener(this)
        (view.findViewById<ImageButton>(R.id.LineStyleButton)).setOnClickListener(this)
        (view.findViewById<ImageButton>(R.id.SaveButton)).setOnClickListener(this)
        (view.findViewById<GokigenSurfaceView>(R.id.GraphicView)).setOnTouchListener(this)

        prepareObjectDrawer()

        // スライドバーが動かされた時の処理
        val seekbar = view.findViewById<SeekBar>(R.id.ZoomInOut)
        seekbar.setOnSeekBarChangeListener(objectDrawer)

        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        val progress = preferences.getInt("zoomProgress", 50)
        seekbar.progress = progress

        // 起動時にデータを読み出す
        prepareMeMoMaInfo()

    }

    private fun prepareObjectDrawer()
    {
        try
        {
            if (!::objectDrawer.isInitialized)
            {
                // 描画クラスの生成
                objectDrawer = MeMoMaCanvasDrawer(parent, objectHolder, lineStyleHolder, this)
                val colorString = (PreferenceManager.getDefaultSharedPreferences(parent)).getString("backgroundColor", "#ff004000") ?: "#ff004000"
                objectDrawer.setBackgroundColor(colorString)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 終了準備
     */
    fun finishListener()
    {
        // 終了時に状態を保存する
        saveData(true)
    }

    /**
     * スタート準備
     */
    override fun prepareToStart(view: View)
    {
        prepareObjectDrawer()

        //  設定に記録されているデータを画面に反映させる
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)

        // ラインの形状を取得し、設定する
        setLineStyle()

        // 操作モードを画面に反映させる
        updateButtons((preferences.getString("operationMode", "0")?:"0").toInt())

        // 条件に合わせて、描画クラスを変更する
        (view.findViewById<GokigenSurfaceView>(R.id.GraphicView)).setCanvasDrawer(objectDrawer)

        // 背景画像（の名前）を設定しておく
        val backgroundString = preferences.getString("backgroundUri", "") ?: ""
        objectDrawer.setBackgroundUri(backgroundString)
    }

    /**
     *   コンテンツ一覧の更新
     */
    override fun updateContentList()
    {
        try
        {
            dataInOutManager.updateFileList(objectHolder.getDataTitle(), parent.supportActionBar)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 終了準備
     */
    override fun shutdown()
    {
        // 保存シーケンスを走らせる
        saveData(true)
    }



    /**
     * 背景画像イメージの更新処理
     *
     */
    private fun updateBackgroundImage(uri: String)
    {
        try
        {
            // 背景画像イメージの更新処理
            val graphView = parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)

            // ビットマップを設定する
            objectDrawer.updateBackgroundBitmap(uri, graphView.width, graphView.height)

            // 画面の再描画指示
            graphView.doDraw()
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
        Log.v(TAG, "onClick()")
        when (val id = v.id)
        {
            R.id.MeMoMaInfo -> {
                // テキスト編集ダイアログを表示する
                showInfoMessageEditDialog()
            }
            R.id.LineStyleButton -> {
                // ライン形状を変えるダイアログ
                selectLineShapeDialog()
            }
            R.id.ExpandButton -> {
                // 拡張メニューを呼び出す
                callExtendMenu()
            }
            R.id.DeleteObjectButton, R.id.CreateObjectButton -> {
                // 削除ボタン or 作成ボタンが押された時の処理
                updateButtons(drawModeHolder.updateOperationMode(id))
            }
            R.id.HomeButton -> {
                //  表示位置、表示倍率と並行移動についてリセットする
                objectDrawer.resetScaleAndLocation(parent.findViewById(R.id.ZoomInOut))

                // 画面の再描画を指示する
                redrawSurfaceView()
            }
            R.id.SaveButton -> {
                // データ保存が指示された！
                saveData(true)
            }
        }
    }

    /**
     * 触られたときの処理
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean
    {
        try
        {
            if (v.id == R.id.GraphicView)
            {
                // 画面をタッチした！
                v.onTouchEvent(event)
                return true
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    /**
     * キーを押したときの操作
     */
    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean
    {
/*
        val action = event.action
        if (action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
        {
            //Log.v(TAG, "KEY ENTER")
        }
        //Log.v(TAG, "MeMoMaListener::onKey() ")
*/
        return false
    }

    override fun commandSelected(menuId: Int): Boolean
    {
        var ret: Boolean
        try
        {
            ret = when (menuId) {
                R.id.action_add -> {
                    createNewScreen()
                    true
                }
                R.id.action_share -> {
                    doCapture(false)
                    true
                }
                R.id.action_capture -> {
                    doCapture(true)
                    true
                }
                R.id.action_undo -> {
                    undoOperation()
                    true
                }
                R.id.action_align -> {
                    alignData()
                    true
                }
                R.id.action_rename -> {
                    showInfoMessageEditDialog()
                    true
                }
                R.id.action_select_wallpaper -> {
                    insertPicture()
                    true
                }
                R.id.action_extension -> {
                    callExtendMenu()
                    true
                }
                R.id.action_preference -> {
                    showPreference()
                    true
                }
                R.id.action_about_gokigen -> {
                    showAboutGokigen()
                    true
                }
                else -> false
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            ret = false
        }
        return ret
    }

    /**
     * 操作を１つ戻す（Undo 処理）
     */
    private fun undoOperation(): Boolean
    {
        try
        {
            val ret = objectHolder.undo()  // undo処理を実行する
            redrawSurfaceView()  // 画面を再描画する
            return ret
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 画像ファイルの挿入 (データファイルの更新)
     */
    private fun insertPicture()
    {
        try
        {
            val intent: Intent
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                //intent.setAction(Intent.ACTION_GET_CONTENT);
            }
            else
            {
                intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
            }
            val startForResult =
                parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                    if (result?.resultCode == Activity.RESULT_OK) {
                        result.data?.let { data: Intent ->
                            updateBackgroundUri(data)
                        }
                    }
                }
            startForResult.launch(intent)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun updateBackgroundUri(data: Intent)
    {
        try
        {
            // ---- 取得したuri を preferenceに記録する
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val backgroundUri = data.data
            if (backgroundUri != null)
            {
                try
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    {
                        parent.contentResolver.takePersistableUriPermission(
                            backgroundUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                    val editor = preferences.edit()
                    editor.putString("backgroundUri", backgroundUri.toString())
                    editor.apply()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
                // 背景画像イメージの更新処理
                updateBackgroundImage(backgroundUri.toString())
            }
            System.gc()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 画面キャプチャの実施
     */
    private fun doCapture(isShare: Boolean)
    {
        try
        {
            // 画面のスクリーンショットをとる処理を実行する
            dataInOutManager.doScreenCapture(
                parent.title as String,
                objectHolder,
                objectDrawer,
                isShare
            )
            // 画面を再描画する
            redrawSurfaceView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * アプリの情報を表示する
     *
     */
    private fun showAboutGokigen()
    {
        try
        {
            // アプリの情報(クレジット)を表示する！
            val aboutDialog = CreditDialog.newInstance(parent)
            aboutDialog.show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 拡張メニューを呼び出す
     *
     */
    private fun callExtendMenu()
    {
        val dataTitle = parent.title as String

        // 現在表示中のデータをファイルに保存する
        dataInOutManager.saveFile(dataTitle, true)

        // Preferenceにもデータタイトル名称を記録する
        setPreferenceDataTitle(dataTitle)

        // 現在読み込んでいるファイルのファイル名を生成する
        val fullPath = dataInOutManager.getDataFileFullPath(dataTitle, ".xml")

        //  ここで拡張メニューを呼び出す
        // (渡すデータを作って Intentとする)
        val intent = Intent()
        intent.action = ExtensionActivity.MEMOMA_EXTENSION_LAUNCH_ACTIVITY
        intent.putExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_FULLPATH, fullPath)
        intent.putExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_TITLE, dataTitle)

        val launcher =
            parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                if (result?.resultCode == Activity.RESULT_OK) {
                    result.data?.let { _ ->
                        try
                        {
                            dataInOutManager.loadFile(parent.title as String)
                        }
                        catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                }
            }
        launcher.launch(intent)
    }

    private fun setPreferenceDataTitle(title: String)
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

    /**
     * データの読み込みを行う
     *
     */
    private fun prepareMeMoMaInfo()
    {
        try
        {
            //  設定に記録されているデータを画面のタイトルに反映させる
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val memomaInfo = preferences.getString("MeMoMaInfo", parent.getString(R.string.app_name)) ?: parent.getString(R.string.app_name)
            parent.title = memomaInfo

            // アクションバーとファイル名の準備
            val bar = parent.supportActionBar
            if (bar != null)
            {
                dataInOutManager.prepare(objectHolder, bar, memomaInfo)
            }
            //dataInOutManager.loadFile((String) parent.getTitle());
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * データの保存を行う
     *
     *
     * @param forceOverwrite  trueの時は、ファイル名が確定していたときは（確認せずに）上書き保存を自動で行う。
     */
    private fun saveData(forceOverwrite: Boolean)
    {
        try
        {
            dataInOutManager.saveFile(parent.title as String, forceOverwrite)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * データの整列を行う
     */
    private fun alignData()
    {
        val aligner = ObjectAligner(parent, this)
        Thread {
            try
            {
                aligner.doAlignObject(objectHolder)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            parent.runOnUiThread {
                try
                {
                    aligner.finishAlignObject()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
        }.start()

        // aligner.execute(objectHolder)
    }

    /**
     * メッセージ編集ダイアログを表示する
     */
    private fun showInfoMessageEditDialog()
    {
        try
        {
            val dialog = editTextDialog.dialog
            prepareInfoMessageEditDialog(dialog)
            dialog.show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 新規作成が指示されたとき...全部クリアして作りなおして良いか確認する。
     */
    private fun createNewScreen()
    {
        try
        {
            confirmationDialog.show(parent.getString(R.string.createnew_title), parent.getString(R.string.createnew_message), this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 接続線の設定ダイアログを表示する
     */
    private fun selectLineShapeDialog()
    {
        try
        {
            // 接続線の設定ダイアログを表示する...
            val dialog = lineSelectionDialog.getDialog()
            lineSelectionDialog.prepareSelectLineShapeDialog(dialog, selectedObjectKey)
            dialog.show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * メッセージ編集ダイアログの表示を準備する
     *
     */
    private fun prepareInfoMessageEditDialog(dialog: AlertDialog)
    {
        try
        {
            val message = parent.title as String
            editTextDialog.prepare(dialog, this, parent.getString(R.string.dataTitle), message, true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 設定画面を表示する処理
     */
    private fun showPreference()
    {
        try
        {
            // 設定画面を呼び出す
            val intent = Intent(parent, Preference::class.java)

            val launcher =
                parent.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
                    if (result?.resultCode == Activity.RESULT_OK) {
                        result.data?.let { data: Intent ->
                            try
                            {
                                // 背景色、背景画像の設定を行う。
                                val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                                val colorString = preferences.getString("backgroundColor", "#ff004000") ?: "#ff004000"
                                objectDrawer.setBackgroundColor(colorString)

                                // 背景画像イメージの更新処理
                                val backgroundString = preferences.getString("backgroundUri", "") ?: ""
                                updateBackgroundImage(backgroundString)
                                Log.v(TAG, "RETURNED PREFERENCES $backgroundString")
                            }
                            catch (e: Exception)
                            {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            launcher.launch(intent)
        }
        catch (e: Exception)
        {
            // 例外発生...なにもしない。
            e.printStackTrace()
        }
    }

    /**
     * 接続線の形状を反映させる
     *
     */
    private fun setLineStyle()
    {
        try
        {
            val buttonId = LineStyleHolder.getLineShapeImageId(
                lineStyleHolder.getLineStyle(),
                lineStyleHolder.getLineShape()
            )
            val lineStyleObj = parent.findViewById<ImageButton>(R.id.LineStyleButton)
            lineStyleObj.setImageResource(buttonId)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトが生成された！
     */
    override fun objectCreated()
    {
        try
        {
            // ここで動作モードを移動モードに戻す。
            drawModeHolder.changeOperationMode(OperationModeHolder.OPERATIONMODE_MOVE)
            updateButtons(OperationModeHolder.OPERATIONMODE_MOVE)

            // 画面を再描画する
            redrawSurfaceView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 空き領域がタッチされた！
     */
    override fun touchedVacantArea(): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        return preferences.getString("operationMode", "0")!!.toInt()
    }

    /**
     * 空き領域でタッチが離された！
     */
    override fun touchUppedVacantArea(): Int
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        return preferences.getString("operationMode", "0")!!.toInt()
    }

    /**
     * オブジェクトを本当に削除して良いか確認した後に、オブジェクトを削除する。
     */
    private fun removeObject(key: Int) {
        // 本当に消して良いか、確認をするダイアログを表示して、OKが押されたら消す。
        val alertDialogBuilder = AlertDialog.Builder(parent)
        alertDialogBuilder.setTitle(parent.getString(R.string.deleteconfirm_title))
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert)
        alertDialogBuilder.setMessage(parent.getString(R.string.deleteconfirm_message))

        // 削除するオブジェクトのキーを覚えこむ。
        objectKeyToDelete = key

        // OKボタンの生成
        alertDialogBuilder.setPositiveButton(
            parent.getString(R.string.confirmYes)
        ) { dialog, _ -> //  削除モードの時... 確認後削除だけど、今は確認なしで削除を行う。
            objectHolder.removePosition(objectKeyToDelete)

            // 削除するオブジェクトに接続されている線もすべて削除する
            objectHolder.getConnectLineHolder().removeAllConnection(objectKeyToDelete)

            // ダイアログを閉じる
            dialog.dismiss()

            // ここで動作モードを削除モードから移動モードに戻す。
            drawModeHolder.changeOperationMode(OperationModeHolder.OPERATIONMODE_MOVE)
            updateButtons(OperationModeHolder.OPERATIONMODE_MOVE)


            // 画面を再描画する
            redrawSurfaceView()
        }

        // Cancelボタンの生成
        alertDialogBuilder.setNegativeButton(
            parent.getString(R.string.confirmNo)
        ) { dialog, _ -> dialog.cancel() }

        // ダイアログはキャンセル可能に設定する
        alertDialogBuilder.setCancelable(true)

        // ダイアログを表示する
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    /**
     * オブジェクトを複製する
     *
     *
     */
    private fun duplicateObject(key: Int) {
        // 選択中オブジェクトを複製する
        objectHolder.duplicatePosition(key)

        // 画面を再描画する
        redrawSurfaceView()
    }

    /**
     * オブジェクトを共有する
     *
     *
     */
    private fun shareObject(key: Int)
    {
        // 選択中オブジェクトを共有する
        objectHolder.shareObject(key)
    }


    /**
     * オブジェクトを拡大する
     *
     *
     */
    private fun expandObject(key: Int)
    {
        try
        {
            // 選択中オブジェクトを拡大する
            objectHolder.expandObjectSize(key)

            // 画面を再描画する
            redrawSurfaceView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトを縮小する
     *
     *
     */
    private fun shrinkObject(key: Int)
    {
        try
        {
            // 選択中オブジェクトを縮小する
            objectHolder.shrinkObjectSize(key)

            // 画面を再描画する
            redrawSurfaceView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setButtonBorder(button: ImageButton, isHighlight: Boolean)
    {
        try
        {
            val btnBackgroundShape = button.background as BitmapDrawable
            if (isHighlight)
            {
                btnBackgroundShape.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN)
            }
            else
            {
                btnBackgroundShape.setColorFilter(Color.BLACK, PorterDuff.Mode.LIGHTEN)
            }
        }
        catch (ex: Exception)
        {
            Log.v(TAG, "MeMoMaListener::setButtonBorder(): $ex")
        }
    }

    /**
     * ボタンを更新する
     *
     */
    private fun updateButtons(mode: Int)
    {
        try
        {
            val createObjectButton = parent.findViewById<ImageButton>(R.id.CreateObjectButton)
            val deleteObjectButton = parent.findViewById<ImageButton>(R.id.DeleteObjectButton)
            when (mode)
            {
                OperationModeHolder.OPERATIONMODE_DELETE -> {
                    setButtonBorder(createObjectButton, false)
                    setButtonBorder(deleteObjectButton, true)
                }
                OperationModeHolder.OPERATIONMODE_CREATE -> {
                    setButtonBorder(createObjectButton, true)
                    setButtonBorder(deleteObjectButton, false)
                }
                else  // if (mode == OperationModeHolder.OPERATIONMODE_MOVE)
                -> {
                    setButtonBorder(createObjectButton, false)
                    setButtonBorder(deleteObjectButton, false)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトが選択された（長押しで！）
     *
     */
    override fun objectSelectedContext(key: Int?)
    {
        try
        {
            Log.v(TAG, "MeMoMaListener::objectSelectedContext(),  key:$key")
            selectedContextKey = key?: 0

            //  ダイアログの準備を行う
            val dialog = objectDataInputDialog.getDialog()
            objectDataInputDialog.prepareObjectInputDialog(dialog, selectedObjectKey)
            dialog.show()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクトが選択された！
     *
     */
    override fun objectSelected(key: Int?): Boolean
    {
        try
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val operationMode = preferences.getString("operationMode", "0")?.toInt()
            if ((operationMode == OperationModeHolder.OPERATIONMODE_DELETE)&&(key != null))
            {
                // オブジェクトを削除する
                removeObject(key)
                return true
            }
            run {
                try
                {
                    if (key != null)
                    {
                        // 選択されたオブジェクトを記憶する
                        selectedObjectKey = key
                        Log.v(TAG, "MeMoMaListener::objectSelected() key : $key")

                        // オブジェクトの詳細設定ダイアログを表示する...
                        val dialog = objectDataInputDialog.getDialog()
                        objectDataInputDialog.prepareObjectInputDialog(dialog, selectedObjectKey)
                        dialog.show()
                    }
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
        return true
    }

    /**
     * 新規状態に変更する。
     *
     */
    override fun acceptConfirmation()
    {
        Log.v(TAG, "MeMoMaListener::acceptConfirmation()")
        try
        {
            // 現在のデータを保管する
            saveData(true);

            // オブジェクトデータをクリアする。
            objectHolder.removeAllPositions() // オブジェクトの保持クラス
            objectHolder.getConnectLineHolder().removeAllLines() // オブジェクト間の接続状態保持クラス

            // 画面の倍率と表示位置を初期状態に戻す
            val zoomBar = parent.findViewById<SeekBar>(R.id.ZoomInOut)
            objectDrawer.resetScaleAndLocation(zoomBar)

            // 画面を再描画する
            redrawSurfaceView()

            // ファイル名選択ダイアログを開く
            showInfoMessageEditDialog()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 画面を再描画する
     *
     */
    private fun redrawSurfaceView()
    {
        try
        {
            val surfaceView = parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)
            surfaceView.doDraw()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 不許可。何もしない。
     *
     */
    override fun cancelConfirmation() {
        Log.v(TAG, "MeMoMaListener::cancelConfirmation()")
    }

    /**
     * オブジェクトが整列された時の処理
     *
     */
    override fun objectAligned() {
        try
        {
            // 画面の再描画を指示する
            redrawSurfaceView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクト編集ダイアログが閉じられた時の処理
     *
     */
    override fun finishObjectInput()
    {
        try
        {
            // 画面の再描画を指示する
            redrawSurfaceView()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * オブジェクト編集ダイアログが閉じられた時の処理
     *
     */
    override fun cancelObjectInput() {
        // 何もしない
    }

    /**
     * アイテムが選択された！
     *
     */
    override fun itemSelected(index: Int, itemValue: String)
    {
        try
        {
            Log.v(TAG, "MeMoMaListener::itemSelected() : $itemValue [$index]")
            when (index) {
                ObjectOperationCommandHolder.OBJECTOPERATION_DELETE -> {
                    // オブジェクト削除の確認
                    removeObject(selectedContextKey)
                }
                ObjectOperationCommandHolder.OBJECTOPERATION_DUPLICATE -> {
                    // オブジェクトの複製
                    duplicateObject(selectedContextKey)
                }
                ObjectOperationCommandHolder.OBJECTOPERATION_SIZEBIGGER -> {
                    // オブジェクトの拡大
                    expandObject(selectedContextKey)
                }
                ObjectOperationCommandHolder.OBJECTOPERATION_SIZESMALLER -> {
                    // オブジェクトの縮小
                    shrinkObject(selectedContextKey)
                }
                ObjectOperationCommandHolder.OBJECTOPERATION_SHARE -> {
                    // オブジェクトの共有
                    shareObject(selectedContextKey)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * (今回未使用)
     *
     */
    override fun itemSelectedMulti(items: Array<String?>, status: BooleanArray) {}
    override fun canceledSelection() {}
    fun onSaveInstanceState(outState: Bundle) {
        /* ここで状態を保存 */
        Log.v(TAG, "MeMoMaListener::onSaveInstanceState() : $outState")
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        /* ここで状態を復元 */
        Log.v(TAG, "MeMoMaListener::onRestoreInstanceState() : $savedInstanceState")
    }

    override fun finishTextEditDialog(message: String) {
        if (message.isEmpty())
        {
            // データが入力されていなかったので、何もしない。
            return
        }
        try
        {
            // 文字列を記録
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val editor = preferences.edit()
            editor.putString("MeMoMaInfo", message)
            editor.apply()

            // タイトルに設定
            parent.title = message

            // 保存シーケンスを一度走らせる
            saveData(true)

            // ファイル選択リストの更新
            dataInOutManager.updateFileList(message, parent.supportActionBar)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun cancelTextEditDialog() {}

    /**
     * 接続線
     *
     */
    override fun finishSelectLineShape(style: Int, shape: Int, thickness: Int)
    {
        try
        {
            val buttonId = LineStyleHolder.getLineShapeImageId(style, shape)
            val lineStyleObj = parent.findViewById<ImageButton>(R.id.LineStyleButton)
            lineStyleObj.setImageResource(buttonId)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     */
    override fun cancelSelectLineShape() {}

    companion object {
        private val TAG = MeMoMaListener::class.java.simpleName
    }
}