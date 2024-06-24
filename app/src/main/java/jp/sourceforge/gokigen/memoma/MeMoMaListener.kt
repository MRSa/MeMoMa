package jp.sourceforge.gokigen.memoma

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.appcompat.app.ActionBar
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
 * @author MRSa
 */
class MeMoMaListener(private val parent: AppCompatActivity, private val dataInOutManager: MeMoMaDataInOutManager) :
    View.OnClickListener, OnTouchListener, View.OnKeyListener,
    IObjectSelectionReceiver, ConfirmationDialog.IResultReceiver,
    ObjectDataInputDialog.IResultReceiver,
    ISelectionItemReceiver, ITextEditResultReceiver, IAlignCallback,
    SelectLineShapeDialog.IResultReceiver
{
    private val editTextDialog: TextEditDialog = TextEditDialog(parent, R.drawable.icon)
    private val objectDrawer: MeMoMaCanvasDrawer
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

    private lateinit var actionBar : ActionBar

    /**
     * コンストラクタ
     *
     */
    init
    {
        lineStyleHolder.prepare()

        // 確認ダイアログ
        confirmationDialog = ConfirmationDialog(parent)
        confirmationDialog.prepare(
            this,
            android.R.drawable.ic_dialog_alert,
            parent.getString(R.string.createnew_title),
            parent.getString(R.string.createnew_message)
        )

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

        // 描画クラスの生成
        objectDrawer = MeMoMaCanvasDrawer(parent, objectHolder, lineStyleHolder, this)
        val colorString = (PreferenceManager.getDefaultSharedPreferences(parent)).getString("backgroundColor", "0xff004000")
        objectDrawer.setBackgroundColor(colorString)
    }

    /**
     * がっつりこのクラスにイベントリスナを接続する
     *
     */
    fun prepareListener()
    {
        // ボタンクリック時のイベントを受信する設定...
        (parent.findViewById<ImageButton>(R.id.HomeButton)).setOnClickListener(this)
        (parent.findViewById<ImageButton>(R.id.ExpandButton)).setOnClickListener(this)
        (parent.findViewById<ImageButton>(R.id.CreateObjectButton)).setOnClickListener(this)
        (parent.findViewById<ImageButton>(R.id.DeleteObjectButton)).setOnClickListener(this)
        (parent.findViewById<ImageButton>(R.id.LineStyleButton)).setOnClickListener(this)
        (parent.findViewById<ImageButton>(R.id.SaveButton)).setOnClickListener(this)
        (parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)).setOnTouchListener(this)

        // スライドバーが動かされた時の処理
        val seekbar = parent.findViewById<SeekBar>(R.id.ZoomInOut)
        seekbar.setOnSeekBarChangeListener(objectDrawer)

        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        val progress = preferences.getInt("zoomProgress", 50)
        seekbar.progress = progress

        // 「実行中」の表示を消す
        parent.setProgressBarIndeterminateVisibility(false)

        // 起動時にデータを読み出す
        prepareMeMoMaInfo()
    }

    /**
     * 終了準備
     */
    fun finishListener()
    {
        // 終了時に状態を保存する
        // saveData(true)
    }

    /**
     * スタート準備
     */
    fun prepareToStart()
    {
        //  設定に記録されているデータを画面に反映させる
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)

        // ラインの形状を取得し、設定する
        setLineStyle()

        // 操作モードを画面に反映させる
        updateButtons((preferences.getString("operationMode", "0")?:"0").toInt())

        // 条件に合わせて、描画クラスを変更する
        (parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)).setCanvasDrawer(objectDrawer)

        // 背景画像（の名前）を設定しておく
        val backgroundString = preferences.getString("backgroundUri", "")
        objectDrawer.setBackgroundUri(backgroundString)
    }

    /**
     *   コンテンツ一覧の更新
     */
    fun updateContentList()
    {
        try
        {
            dataInOutManager.updateFileList(objectHolder.dataTitle, parent.supportActionBar)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    /**
     * 終了準備
     */
    fun shutdown()
    {
        // 保存シーケンスを走らせる
        saveData(true)
    }

    /**
     * 他画面から戻ってきたとき...
     *
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        if ((requestCode == MENU_ID_INSERT_PICTURE) && (resultCode == Activity.RESULT_OK))
        {
            try
            {
                // 取得したuri を preferenceに記録する
                val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                val backgroundUri = data?.data
                if (backgroundUri != null)
                {
                    try
                    {
                        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
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
            catch (ex: Exception)
            {
                Log.v(TAG, "Ex:" + ex.message)
                ex.printStackTrace()
            }
            return
        }
        else if (requestCode == MENU_ID_PREFERENCES)
        {
            // 背景色、背景画像の設定を行う。
            val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
            val colorString = preferences.getString("backgroundColor", "0xff004000")
            objectDrawer.setBackgroundColor(colorString)

            // 背景画像イメージの更新処理
            val backgroundString = preferences.getString("backgroundUri", "")
            updateBackgroundImage(backgroundString)
            Log.v(TAG, "RETURENED PREFERENCES $backgroundString")
        }
        else if (requestCode == MENU_ID_EXTEND)
        {
            // その他...今開いているファイルを読みなおす
            dataInOutManager.loadFile(parent.title as String)
        }
        else
        {
            // 画面表示の準備を実行...
            //prepareToStart();
            return
        }
        // 画面の再描画を指示する
        redrawSurfaceview()
    }

    /**
     * 背景画像イメージの更新処理
     *
     */
    private fun updateBackgroundImage(uri: String?)
    {
        // 背景画像イメージの更新処理
        val graphView = parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)

        // ビットマップを設定する
        objectDrawer.updateBackgroundBitmap(uri, graphView.width, graphView.height)

        // 画面の再描画指示
        graphView.doDraw()
    }

    /**
     * クリックされたときの処理
     */
    override fun onClick(v: View)
    {
        val id = v.id
        when (id)
        {
            R.id.MeMoMaInfo -> {
                showInfoMessageEditDialog()  // テキスト編集ダイアログを表示する
            }
            R.id.LineStyleButton -> {
                selectLineShapeDialog() // ライン形状を変えるダイアログ
            }
            R.id.ExpandButton -> {
                callExtendMenu()      // 拡張メニューを呼び出す
            }
            R.id.DeleteObjectButton, R.id.CreateObjectButton -> {
                // 削除ボタン or 作成ボタンが押された時の処理
                updateButtons(drawModeHolder.updateOperationMode(id))
            }
            R.id.HomeButton -> {
                //  表示位置、表示倍率と並行移動についてリセットする
                objectDrawer.resetScaleAndLocation(parent.findViewById(R.id.ZoomInOut))

                // 画面の再描画を指示する
                redrawSurfaceview()
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
    override fun onTouch(v: View, event: MotionEvent): Boolean
    {
        val id = v.id
        if (id == R.id.GraphicView)
        {
            // 画面をタッチした！
            v.onTouchEvent(event)
            return true
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

    /**
     * メニューへのアイテム追加
     *
     */
    fun onCreateOptionsMenu(menu: Menu): Menu
    {
        // 新規作成
        var menuItem =
            menu.add(Menu.NONE, MENU_ID_NEW, Menu.NONE, parent.getString(R.string.createnew))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_add) // 丸プラス

        // 画像の共有
        menuItem =
            menu.add(Menu.NONE, MENU_ID_SHARE, Menu.NONE, parent.getString(R.string.shareContent))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_share)

        // 画像のキャプチャ
        menuItem =
            menu.add(Menu.NONE, MENU_ID_CAPTURE, Menu.NONE, parent.getString(R.string.capture_data))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_crop) // オブジェクトのキャプチャ

        // 処理のUNDO
        menuItem =
            menu.add(Menu.NONE, MENU_ID_UNDO, Menu.NONE, parent.getString(R.string.undo_operation))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_revert)

        // オブジェクトの整列
        menuItem =
            menu.add(Menu.NONE, MENU_ID_ALIGN, Menu.NONE, parent.getString(R.string.align_data))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_rotate) // オブジェクトの整列

        // タイトルの変更
        menuItem =
            menu.add(Menu.NONE, MENU_ID_RENAME, Menu.NONE, parent.getString(R.string.rename_title))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_edit) // タイトルの変更

        // 壁紙の選択
        menuItem = menu.add(
            Menu.NONE,
            MENU_ID_INSERT_PICTURE,
            Menu.NONE,
            parent.getString(R.string.background_data)
        )
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_gallery) // 壁紙の選択

        // 拡張メニュー
        menuItem =
            menu.add(Menu.NONE, MENU_ID_EXTEND, Menu.NONE, parent.getString(R.string.extend_menu))
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_share) // 拡張メニュー...

        // 設定
        menuItem = menu.add(
            Menu.NONE,
            MENU_ID_PREFERENCES,
            Menu.NONE,
            parent.getString(R.string.preference_name)
        )
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_preferences)

        // クレジット情報の表示
        menuItem = menu.add(
            Menu.NONE,
            MENU_ID_ABOUT_GOKIGEN,
            Menu.NONE,
            parent.getString(R.string.about_gokigen)
        )
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER) /*  for Android 3.1  */
        menuItem.setIcon(android.R.drawable.ic_menu_info_details)

        return menu
    }

    /**
     * メニュー表示前の処理
     *
     *
     */
    fun onPrepareOptionsMenu(menu: Menu)
    {
        menu.findItem(MENU_ID_NEW).isVisible = true
        menu.findItem(MENU_ID_UNDO).isVisible = objectHolder.isHistoryExist
        menu.findItem(MENU_ID_SHARE).isVisible = true
        menu.findItem(MENU_ID_CAPTURE).isVisible = true
        menu.findItem(MENU_ID_ALIGN).isVisible = true
        menu.findItem(MENU_ID_RENAME).isVisible = true
        menu.findItem(MENU_ID_INSERT_PICTURE).isVisible = true
        menu.findItem(MENU_ID_EXTEND).isVisible = true
        menu.findItem(MENU_ID_PREFERENCES).isVisible = true
        menu.findItem(MENU_ID_ABOUT_GOKIGEN).isVisible = true
    }

    /**
     * メニューのアイテムが選択されたときの処理
     *
     *
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        val result: Boolean = when (item.itemId)
        {
            MENU_ID_PREFERENCES -> {
                showPreference()
                true
            }

            MENU_ID_ABOUT_GOKIGEN -> {
                showAboutGokigen()
                true
            }

            MENU_ID_NEW -> {
                createNewScreen()
                true
            }

            MENU_ID_EXTEND -> {
                callExtendMenu()  // 拡張メニューを呼び出す
                true
            }

            MENU_ID_ALIGN -> {
                alignData()  // オブジェクトの整列を行う
                true
            }

            MENU_ID_RENAME, android.R.id.home -> {
                // アイコンが押された時の処理...
                // テキスト編集ダイアログを表示する
                // タイトル名の変更  (テキスト編集ダイアログを表示する)
                showInfoMessageEditDialog()
                true
            }

            MENU_ID_INSERT_PICTURE -> {
                // 背景画像の設定を行う
                insertPicture()
                true
            }

            MENU_ID_CAPTURE -> {
                // 画面キャプチャを指示された場合...
                doCapture(false)
                true
            }

            MENU_ID_SHARE -> {
                // 画面キャプチャ＆共有を指示された場合...
                doCapture(true)
                true
            }
            MENU_ID_UNDO -> {
                undoOperation() // UNDO
            }
            else -> false
        }
        return result
    }

    /**
     * 操作を１つ戻す（Undo 処理）
     */
    private fun undoOperation(): Boolean
    {
        val ret = objectHolder.undo()  // undo処理を実行する
        redrawSurfaceview()  // 画面を再描画する
        return ret
    }

    /**
     * 画像ファイルの挿入 (データファイルの更新)
     */
    private fun insertPicture()
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
        parent.startActivityForResult(intent, MENU_ID_INSERT_PICTURE)
    }

    /**
     * 画面キャプチャの実施
     */
    private fun doCapture(isShare: Boolean)
    {
        // 画面のスクリーンショットをとる処理を実行する
        dataInOutManager.doScreenCapture(
            parent.title as String,
            objectHolder,
            objectDrawer,
            isShare
        )

        // 画面を再描画する
        redrawSurfaceview()
    }

    /**
     * アプリの情報を表示する
     *
     */
    private fun showAboutGokigen()
    {
        // アプリの情報(クレジット)を表示する！
        parent.showDialog(R.id.info_about_gokigen)
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
        intent.apply {
            `package` = parent.packageName
        }

        // データ表示用Activityを起動する
        parent.startActivityForResult(intent, MENU_ID_EXTEND)
        //parent.startActivity(intent);
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
        //  設定に記録されているデータを画面のタイトルに反映させる
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        val memomaInfo = preferences.getString("MeMoMaInfo", parent.getString(R.string.app_name)) ?: parent.getString(R.string.app_name)
        parent.title = memomaInfo

        // アクションバーとファイル名の準備
        val bar = parent.supportActionBar
        if (bar != null)
        {
            actionBar = bar
            dataInOutManager.prepare(objectHolder, bar, memomaInfo)
        }
        //dataInOutManager.loadFile((String) parent.getTitle());
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
        aligner.execute(objectHolder)
    }

    /**
     * メッセージ編集ダイアログを表示する
     */
    private fun showInfoMessageEditDialog()
    {
        parent.showDialog(R.id.editTextArea)
    }

    /**
     * 新規作成が指示されたとき...全部クリアして作りなおして良いか確認する。
     */
    private fun createNewScreen()
    {
        parent.showDialog(R.id.confirmation)
    }

    /**
     * 接続線の設定ダイアログを表示する
     */
    private fun selectLineShapeDialog()
    {
        // 接続線の設定ダイアログを表示する...
        parent.showDialog(R.id.selectline_dialog)
    }

    /**
     * メッセージ編集ダイアログの表示を準備する
     *
     */
    private fun prepareInfoMessageEditDialog(dialog: Dialog)
    {
        val message = parent.title as String
        editTextDialog.prepare(dialog, this, parent.getString(R.string.dataTitle), message, true)
    }

    /**
     * メッセージ編集ダイアログの表示を準備する
     */
    private fun prepareConfirmationDialog(dialog: Dialog)
    {
        // Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::prepareConfirmationDialog() " );
    }

    /**
     * オブジェクト入力用ダイアログの表示を準備する
     */
    private fun prepareObjectInputDialog(dialog: Dialog)
    {
        Log.v(
            TAG,
            "MeMoMaListener::prepareObjectInputDialog(), key: $selectedObjectKey"
        )

        //  ダイアログの準備を行う
        objectDataInputDialog.prepareObjectInputDialog(dialog, selectedObjectKey)
    }

    /**
     * アイテム選択ダイアログの表示を準備する
     */
    private fun prepareItemSelectionDialog(dialog: Dialog)
    {
        // アイテム選択ダイアログの表示設定
        // (動的変更時。。。今回は固定なので何もしない）
    }

    /**
     * 接続線選択用ダイアログの表示を準備する
     *
     */
    private fun prepareLineSelectionDialog(dialog: Dialog)
    {
        Log.v(
            TAG,
            "MeMoMaListener::prepareLineSelectionDialog(), key: $selectedObjectKey"
        )

        //  ダイアログの準備を行う
        lineSelectionDialog.prepareSelectLineShapeDialog(dialog, selectedObjectKey)
    }

    /**
     * 設定画面を表示する処理
     */
    private fun showPreference()
    {
        try
        {
            // 設定画面を呼び出す
            val prefIntent = Intent(parent, Preference::class.java)
            parent.startActivityForResult(prefIntent, MENU_ID_PREFERENCES)
        }
        catch (e: Exception)
        {
            // 例外発生...なにもしない。
            e.printStackTrace()
            //updater.showMessage("ERROR", MainUpdater.SHOWMETHOD_DONTCARE);
        }
    }

    /**
     * 接続線の形状を反映させる
     *
     */
    private fun setLineStyle()
    {
        val buttonId = LineStyleHolder.getLineShapeImageId(
            lineStyleHolder.lineStyle,
            lineStyleHolder.lineShape
        )
        val lineStyleObj = parent.findViewById<ImageButton>(R.id.LineStyleButton)
        lineStyleObj.setImageResource(buttonId)
    }

    /**
     * オブジェクトが生成された！
     */
    override fun objectCreated()
    {
        // ここで動作モードを移動モードに戻す。
        drawModeHolder.changeOperationMode(OperationModeHolder.OPERATIONMODE_MOVE)
        updateButtons(OperationModeHolder.OPERATIONMODE_MOVE)

        // 画面を再描画する
        redrawSurfaceview()
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
        ) { dialog, id -> //  削除モードの時... 確認後削除だけど、今は確認なしで削除を行う。
            objectHolder.removePosition(objectKeyToDelete)

            // 削除するオブジェクトに接続されている線もすべて削除する
            objectHolder.connectLineHolder.removeAllConnection(objectKeyToDelete)

            // ダイアログを閉じる
            dialog.dismiss()

            // ここで動作モードを削除モードから移動モードに戻す。
            drawModeHolder.changeOperationMode(OperationModeHolder.OPERATIONMODE_MOVE)
            updateButtons(OperationModeHolder.OPERATIONMODE_MOVE)


            // 画面を再描画する
            redrawSurfaceview()
        }

        // Cancelボタンの生成
        alertDialogBuilder.setNegativeButton(
            parent.getString(R.string.confirmNo)
        ) { dialog, id -> dialog.cancel() }

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
        redrawSurfaceview()
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
    private fun expandObject(key: Int) {
        // 選択中オブジェクトを拡大する
        objectHolder.expandObjectSize(key)

        // 画面を再描画する
        redrawSurfaceview()
    }

    /**
     * オブジェクトを縮小する
     *
     *
     */
    private fun shrinkObject(key: Int) {
        // 選択中オブジェクトを縮小する
        objectHolder.shrinkObjectSize(key)

        // 画面を再描画する
        redrawSurfaceview()
    }

    private fun setButtonBorder(button: ImageButton, isHighlight: Boolean) {
        try {
            val btnBackgroundShape = button.background as BitmapDrawable
            if (isHighlight) {
//	                 	btnBackgroundShape.setColorFilter(Color.rgb(51, 181, 229), Mode.LIGHTEN);
                btnBackgroundShape.setColorFilter(Color.BLUE, PorterDuff.Mode.LIGHTEN)
            } else {
                btnBackgroundShape.setColorFilter(Color.BLACK, PorterDuff.Mode.LIGHTEN)
            }
        } catch (ex: Exception) {
            //
            Log.v(TAG, "MeMoMaListener::setButtonBorder(): $ex")
        }
    }

    /**
     * ボタンを更新する
     *
     */
    private fun updateButtons(mode: Int) {
        val createObjectButton = parent.findViewById<ImageButton>(R.id.CreateObjectButton)
        val deleteObjectButton = parent.findViewById<ImageButton>(R.id.DeleteObjectButton)
        if (mode == OperationModeHolder.OPERATIONMODE_DELETE) {
            setButtonBorder(createObjectButton, false)
            setButtonBorder(deleteObjectButton, true)
        } else if (mode == OperationModeHolder.OPERATIONMODE_CREATE) {
            setButtonBorder(createObjectButton, true)
            setButtonBorder(deleteObjectButton, false)
        } else  // if (mode == OperationModeHolder.OPERATIONMODE_MOVE)
        {
            setButtonBorder(createObjectButton, false)
            setButtonBorder(deleteObjectButton, false)
        }
    }

    /**
     * オブジェクトが選択された（長押しで！）
     *
     */
    override fun objectSelectedContext(key: Int) {
        Log.v(TAG, "MeMoMaListener::objectSelectedContext(),  key:$key")
        selectedContextKey = key

        // オブジェクトのアイテム選択ダイアログを表示する...
        parent.showDialog(MENU_ID_OPERATION)
    }

    /**
     * オブジェクトが選択された！
     *
     */
    override fun objectSelected(key: Int): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
        val operationMode = preferences.getString("operationMode", "0")!!.toInt()
        if (operationMode == OperationModeHolder.OPERATIONMODE_DELETE) {
            // オブジェクトを削除する
            removeObject(key)
            return true
        }
        //if ((operationMode == OperationModeHolder.OPERATIONMODE_MOVE)||
        //		(operationMode == OperationModeHolder.OPERATIONMODE_CREATE))
        run {

            // 選択されたオブジェクトを記憶する
            selectedObjectKey = key
            Log.v(TAG, "MeMoMaListener::objectSelected() key : $key")

            // オブジェクトの詳細設定ダイアログを表示する...
            parent.showDialog(R.id.objectinput_dialog)
        }
        return true
    }

    /**
     * ダイアログの生成
     *
     */
    fun onCreateDialog(id: Int): Dialog?
    {
        if (id == R.id.info_about_gokigen) {
            // クレジットダイアログを表示
            val dialog = CreditDialog(parent)
            return dialog.dialog
        }
        if (id == R.id.editTextArea) {
            // 変更するテキストを表示
            return editTextDialog.dialog
        }
        if (id == R.id.confirmation) {
            // 確認するメッセージを表示する
            return confirmationDialog.dialog
        }
        if (id == R.id.objectinput_dialog) {
            // オブジェクト入力のダイアログを表示する
            return objectDataInputDialog.dialog
        }
        if (id == MENU_ID_OPERATION) {
            // アイテム選択ダイアログの準備を行う
            return itemSelectionDialog.dialog
        }
        return if (id == R.id.selectline_dialog) {
            // 接続線選択ダイアログの準備を行う
            lineSelectionDialog.dialog
        } else null
    }

    /**
     * ダイアログ表示の準備
     *
     */
    fun onPrepareDialog(id: Int, dialog: Dialog) {
        if (id == R.id.editTextArea) {
            // 変更するデータを表示する
            prepareInfoMessageEditDialog(dialog)
            return
        }
        if (id == R.id.confirmation) {
            // 確認ダイアログを表示する。
            prepareConfirmationDialog(dialog)
            return
        }
        if (id == R.id.objectinput_dialog) {
            // オブジェクト入力のダイアログを表示する
            prepareObjectInputDialog(dialog)
        }
        if (id == MENU_ID_OPERATION) {
            // オブジェクト操作選択のダイアログを表示する
            prepareItemSelectionDialog(dialog)
        }
        if (id == R.id.selectline_dialog) {
            // 接続線選択のダイアログを表示する
            prepareLineSelectionDialog(dialog)
        }
    }

    /**
     * 新規状態に変更する。
     *
     */
    override fun acceptConfirmation() {
        //
        Log.v(TAG, "MeMoMaListener::acceptConfirmation()")

        // 現在のデータを保管する
        saveData(true);

        // オブジェクトデータをクリアする。
        objectHolder.removeAllPositions() // オブジェクトの保持クラス
        objectHolder.connectLineHolder.removeAllLines() // オブジェクト間の接続状態保持クラス

        // 画面の倍率と表示位置を初期状態に戻す
        val zoomBar = parent.findViewById<SeekBar>(R.id.ZoomInOut)
        objectDrawer.resetScaleAndLocation(zoomBar)

        // 画面を再描画する
        redrawSurfaceview()

        // ファイル名選択ダイアログを開く
        showInfoMessageEditDialog()
    }

    /**
     * 画面を再描画する
     *
     */
    private fun redrawSurfaceview() {
        try {
            val surfaceView = parent.findViewById<GokigenSurfaceView>(R.id.GraphicView)
            surfaceView.doDraw()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 不許可。何もしない。
     *
     */
    override fun rejectConfirmation() {
        Log.v(TAG, "MeMoMaListener::rejectConfirmation()")
    }

    /**
     * オブジェクトが整列された時の処理
     *
     */
    override fun objectAligned() {
        // 画面の再描画を指示する
        redrawSurfaceview()
    }

    /**
     * オブジェクト編集ダイアログが閉じられた時の処理
     *
     */
    override fun finishObjectInput() {
        // 画面の再描画を指示する
        redrawSurfaceview()
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
    override fun itemSelected(index: Int, itemValue: String) {
        //
        Log.v(TAG, "MeMoMaListener::itemSelected() : $itemValue [$index]")
        if (index == ObjectOperationCommandHolder.OBJECTOPERATION_DELETE) {
            // オブジェクト削除の確認
            removeObject(selectedContextKey)
        } else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_DUPLICATE) {
            // オブジェクトの複製
            duplicateObject(selectedContextKey)
        } else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_SIZEBIGGER) {
            // オブジェクトの拡大
            expandObject(selectedContextKey)
        } else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_SIZESMALLER) {
            // オブジェクトの縮小
            shrinkObject(selectedContextKey)
        } else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_SHARE) {
            // オブジェクトの共有
            shareObject(selectedContextKey)
        }
    }

    /**
     * (今回未使用)
     *
     */
    override fun itemSelectedMulti(items: Array<String>, status: BooleanArray) {}
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
            Log.v(TAG, " - - - SET TITLE : $message - - -")
            parent.title = message
            objectHolder.dataTitle = message

            // 保存シーケンスを一度走らせる
            saveData(true)

            // ファイル選択リストの更新
            dataInOutManager.updateFileList(objectHolder.dataTitle, parent.supportActionBar)

/**/
            try
            {
                parent.runOnUiThread {
                    if (::actionBar.isInitialized)
                    {
                        actionBar.title = message
                    }
                    parent.supportActionBar?.title = message
                    Log.v(TAG, " Title : ${parent.supportActionBar?.title} (${message}) ${parent.title}")
                }
            }
            catch (ee: Exception)
            {
                ee.printStackTrace()
            }
/**/
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
    override fun finishSelectLineShape(style: Int, shape: Int, thickness: Int) {
        val buttonId = LineStyleHolder.getLineShapeImageId(style, shape)
        val lineStyleObj = parent.findViewById<ImageButton>(R.id.LineStyleButton)
        lineStyleObj.setImageResource(buttonId)
    }

    /**
     *
     */
    override fun cancelSelectLineShape() {}

    companion object {
        private val TAG = MeMoMaListener::class.java.simpleName
        private const val MENU_ID_PREFERENCES = Menu.FIRST + 1 // 設定画面の表示
        private const val MENU_ID_ABOUT_GOKIGEN = Menu.FIRST + 2 // アプリケーションの情報表示
        private const val MENU_ID_NEW = Menu.FIRST + 3 // 新規作成
        private const val MENU_ID_EXTEND = Menu.FIRST + 4 // 拡張機能
        private const val MENU_ID_ALIGN = Menu.FIRST + 5 // オブジェクトの整列
        private const val MENU_ID_INSERT_PICTURE = Menu.FIRST + 6 // 画像の指定
        private const val MENU_ID_OPERATION = Menu.FIRST + 7 // 操作コマンド
        private const val MENU_ID_RENAME = Menu.FIRST + 8 // リネーム
        private const val MENU_ID_CAPTURE = Menu.FIRST + 9 // 画像のキャプチャ
        private const val MENU_ID_SHARE = Menu.FIRST + 10 // 画像の共有
        private const val MENU_ID_UNDO = Menu.FIRST + 11 // 処理の UNDO
    }
}