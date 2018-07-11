package jp.sourceforge.gokigen.memoma;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.SeekBar;

import jp.sourceforge.gokigen.memoma.dialogs.ConfirmationDialog;
import jp.sourceforge.gokigen.memoma.dialogs.CreditDialog;
import jp.sourceforge.gokigen.memoma.dialogs.TextEditDialog;
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer;
import jp.sourceforge.gokigen.memoma.extension.ExtensionActivity;
import jp.sourceforge.gokigen.memoma.fileio.MeMoMaDataInOutManager;
import jp.sourceforge.gokigen.memoma.holders.LineStyleHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaConnectLineHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.OperationModeHolder;

/**
 *   メモま！ のメイン画面処理
 *   
 * @author MRSa
 */
public class MeMoMaListener implements OnClickListener, OnTouchListener, OnKeyListener, IObjectSelectionReceiver, ConfirmationDialog.IResultReceiver, ObjectDataInputDialog.IResultReceiver, ItemSelectionDialog.ISelectionItemReceiver, TextEditDialog.ITextEditResultReceiver, ObjectAligner.IAlignCallback, SelectLineShapeDialog.IResultReceiver
{
	    public static final int MENU_ID_PREFERENCES = (Menu.FIRST + 1);    // 設定画面の表示
	    public static final int MENU_ID_ABOUT_GOKIGEN = (Menu.FIRST + 2);  // アプリケーションの情報表示
	    public static final int MENU_ID_NEW = (Menu.FIRST + 3);                     // 新規作成
	    public static final int MENU_ID_EXTEND= (Menu.FIRST + 4);                   // 拡張機能
	    public static final int MENU_ID_ALIGN = (Menu.FIRST + 5);                     // オブジェクトの整列
	    public static final int MENU_ID_INSERT_PICTURE = (Menu.FIRST + 6);   // 画像の指定
	    public static final int MENU_ID_OPERATION = (Menu.FIRST + 7);           // 操作コマンド
	    public static final int MENU_ID_RENAME = (Menu.FIRST + 8);                // リネーム
	    public static final int MENU_ID_CAPTURE = (Menu.FIRST + 9);              // 画像のキャプチャ
	    public static final int MENU_ID_SHARE = (Menu.FIRST + 10);              // 画像の共有
	    

	    private AppCompatActivity parent = null;  // 親分
	    private TextEditDialog editTextDialog = null;   // テキスト編集用ダイアログ
	    private MeMoMaCanvasDrawer objectDrawer = null; // 画像の表示
	    private MeMoMaObjectHolder objectHolder = null;  // オブジェクトの保持クラス
	    private MeMoMaConnectLineHolder lineHolder =null;  // オブジェクト間の接続状態保持クラス
	    //private SelectFeatureListener featureListener = null;  // 機能選択用のリスナ
	    
	    private MeMoMaDataInOutManager dataInOutManager = null;
	    
	    private OperationModeHolder drawModeHolder = null;
	    private LineStyleHolder lineStyleHolder = null;
	    
	    private ConfirmationDialog confirmationDialog = null;
	    
	    private ObjectDataInputDialog objectDataInputDialog = null;
	    
	    private SelectLineShapeDialog lineSelectionDialog = null;
	    
	    private ItemSelectionDialog itemSelectionDialog = null;
	    private ObjectOperationCommandHolder commandHolder = null;
	    
	    private boolean isEditing = false;
	    private Integer  selectedObjectKey = 0;
	    private Integer  objectKeyToDelete = 0;
	    private Integer selectedContextKey = 0;

	    /**
	     *  コンストラクタ
	     * @param argument
	     */
	    public MeMoMaListener(AppCompatActivity argument, MeMoMaDataInOutManager inoutManager)
	    {
	        parent = argument;
	        dataInOutManager = inoutManager;
	        lineHolder = new MeMoMaConnectLineHolder();
	        objectHolder = new MeMoMaObjectHolder(argument, lineHolder);
	        editTextDialog = new TextEditDialog(parent, R.drawable.icon);
	        //lineHolder = new MeMoMaConnectLineHolder();
	        //featureListener = new SelectFeatureListener(parent);
	        drawModeHolder = new OperationModeHolder(parent);

	        lineStyleHolder = new LineStyleHolder(parent);
	        lineStyleHolder.prepare();

	        // 新規作成時の確認ダイアログについて
	        confirmationDialog = new ConfirmationDialog(argument);	    	
            confirmationDialog.prepare(this, android.R.drawable.ic_dialog_alert, parent.getString(R.string.createnew_title), parent.getString(R.string.createnew_message));	    	

            // オブジェクトのデータ入力ダイアログを生成
            objectDataInputDialog = new ObjectDataInputDialog(argument, objectHolder);
            objectDataInputDialog.setResultReceiver(this);
            
            // 接続線の形状と太さを選択するダイアログを生成
            lineSelectionDialog = new SelectLineShapeDialog(argument, lineStyleHolder);
            lineSelectionDialog.setResultReceiver(this);
            
            // アイテム選択ダイアログを生成
            commandHolder = new ObjectOperationCommandHolder(argument);
            itemSelectionDialog = new ItemSelectionDialog(argument);
            itemSelectionDialog.prepare(this,  commandHolder, parent.getString(R.string.object_operation));
            
	        // 描画クラスの生成
	        objectDrawer = new MeMoMaCanvasDrawer(argument, objectHolder, lineStyleHolder, this);

	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
        	String colorString = preferences.getString("backgroundColor", "0xff004000");
        	objectDrawer.setBackgroundColor(colorString);

	    }

	    /**
	     *  がっつりこのクラスにイベントリスナを接続する
	     * 
	     */
	    public void prepareListener()
	    {
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);

	        // 表示位置リセットボタン
	        final ImageButton homeButton = (ImageButton) parent.findViewById(R.id.HomeButton);
	        homeButton.setOnClickListener(this);

	        // 拡張ボタン
	        final ImageButton expandButton = (ImageButton) parent.findViewById(R.id.ExpandButton);
	        expandButton.setOnClickListener(this);

	        // 作成ボタン
	        final ImageButton createObjectButton = (ImageButton) parent.findViewById(R.id.CreateObjectButton);
	        createObjectButton.setOnClickListener(this);

	        final ImageButton deleteObjectButton = (ImageButton) parent.findViewById(R.id.DeleteObjectButton);
            deleteObjectButton.setOnClickListener(this);

	        // 線の形状切り替えボタン
	        final ImageButton lineStyleButton = (ImageButton) parent.findViewById(R.id.LineStyleButton);
	        lineStyleButton.setOnClickListener(this);

	        // データ保存ボタン
	        final ImageButton saveButton = (ImageButton) parent.findViewById(R.id.SaveButton);
	        saveButton.setOnClickListener(this);

	        // 画面描画クラス
	        final GokigenSurfaceView surfaceView = (GokigenSurfaceView) parent.findViewById(R.id.GraphicView);
	        surfaceView.setOnTouchListener(this);
	    		    	
	        // スライドバーが動かされた時の処理
	        final SeekBar seekbar = (SeekBar) parent.findViewById(R.id.ZoomInOut);
	        seekbar.setOnSeekBarChangeListener(objectDrawer);
	        int progress = preferences.getInt("zoomProgress", 50);
	        seekbar.setProgress(progress);

	        // 「実行中」の表示を消す
	    	parent.setProgressBarIndeterminateVisibility(false);
	    	
            //// 起動時にデータを読み出す	    	
	    	prepareMeMoMaInfo();
	    }

	    /**
	     *  終了準備
	     */
	    public void finishListener()
	    {
	    	// 終了時に状態を保存する
            saveData(true);
	    }

	    /**
	     *  スタート準備
	     */
	    public void prepareToStart()
	    {
	    	//  設定に記録されているデータを画面に反映させる
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);

	    	// 描画オブジェクトの形状を設定する
	        //int objectStyle = Integer.parseInt(preferences.getString("drawStyle", "0"));

	        // ラインの形状を取得し、設定する
	    	setLineStyle();

	        // 操作モードを画面に反映させる
	        updateButtons(Integer.parseInt(preferences.getString("operationMode", "0")));

	        // 条件に合わせて、描画クラスを変更する
	        final GokigenSurfaceView surfaceView = (GokigenSurfaceView) parent.findViewById(R.id.GraphicView);
	        surfaceView.setCanvasDrawer(objectDrawer);

	        // 背景画像（の名前）を設定しておく
	        String backgroundString = preferences.getString("backgroundUri", "");
            objectDrawer.setBackgroundUri(backgroundString);
	    }

	    /**
	     *  終了準備
	     */
	    public void shutdown()
	    {
	    	
	    }
	    
	    /**
	     *  他画面から戻ってきたとき...
	     * @param requestCode
	     * @param resultCode
	     * @param data
	     */
	    public void onActivityResult(int requestCode, int resultCode, Intent data)
	    {
	    	if ((requestCode == MENU_ID_INSERT_PICTURE)&&(resultCode == Activity.RESULT_OK))
	    	{
	            try
	            {
	            	// 取得したuri を preferenceに記録する
	    	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
	                Uri backgroundUri = data.getData();
	                SharedPreferences.Editor editor = preferences.edit();
	                editor.putString("backgroundUri", backgroundUri.toString());
	                editor.commit();
	                
	                // 背景画像イメージの更新処理
	            	updateBackgroundImage(backgroundUri.toString());
	                
	          	    System.gc();
	            }
	            catch (Exception ex)
	            {
	                Log.v(Main.APP_IDENTIFIER, "Ex:" + ex.toString() + " " + ex.getMessage());
	            }
	            return;
	    	}
	    	else if (requestCode == MENU_ID_PREFERENCES)
            {
	    		// 背景色、背景画像の設定を行う。
    	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
            	String colorString = preferences.getString("backgroundColor", "0xff004000");
            	objectDrawer.setBackgroundColor(colorString);

            	// 背景画像イメージの更新処理
            	String backgroundString = preferences.getString("backgroundUri", "");
            	updateBackgroundImage(backgroundString);

            	Log.v(Main.APP_IDENTIFIER, "RETURENED PREFERENCES " + backgroundString);

            }
	    	else if (requestCode == MENU_ID_EXTEND)
	    	{
	    		// その他...今開いているファイルを読みなおす
	            dataInOutManager.loadFile((String) parent.getTitle());
	    	}
	    	else
	    	{
		    	// 画面表示の準備を実行...
		    	//prepareToStart();
		    	return;
	    	}
            // 画面の再描画を指示する
       	    redrawSurfaceview();
	    }

	    /**
	     *    背景画像イメージの更新処理
	     * 
	     */
	    private void updateBackgroundImage(String uri)
	    {
             // 背景画像イメージの更新処理	    	
            GokigenSurfaceView graphView = (GokigenSurfaceView) parent.findViewById(R.id.GraphicView);

            // ビットマップを設定する
            objectDrawer.updateBackgroundBitmap(uri, graphView.getWidth(), graphView.getHeight());

            // 画面の再描画指示
            graphView.doDraw();
	    }
	    
	    
	    /**
	     *   クリックされたときの処理
	     */
	    public void onClick(View v)
	    {
	         int id = v.getId();

	         //Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::onClick() " + id);
	         if (id == R.id.MeMoMaInfo)
	         {
	        	 // テキスト編集ダイアログを表示する
                 showInfoMessageEditDialog();
	         }
	         else if (id == R.id.LineStyleButton)
	         {
	        	 // ライン形状を変えるダイアログで変更するように変更する
	        	 selectLineShapeDialog();
	         }
	         else if (id == R.id.ExpandButton)
	         {
	        	 // 拡張メニューを呼び出す
	        	 callExtendMenu();
	         }
	         else if ((id == R.id.DeleteObjectButton)||(id == R.id.CreateObjectButton))
	         {
	        	 // 削除ボタン or 作成ボタンが押された時の処理
	        	 updateButtons(drawModeHolder.updateOperationMode(id));
	         }
	         else if (id == R.id.HomeButton)
	         {
	        	 /**  表示位置をリセットする **/
	        	 // 表示倍率と並行移動についてリセットする
	        	 objectDrawer.resetScaleAndLocation((SeekBar) parent.findViewById(R.id.ZoomInOut));
	        	 
	        	 // 画面の再描画を指示する
	        	 redrawSurfaceview();
            }
	        else if (id == R.id.SaveButton)
	        {
	        	// データ保存が指示された！
	        	saveData(true);
	        }
	    }

	    /**
	     *   触られたときの処理
	     * 
	     */
	    public boolean onTouch(View v, MotionEvent event)
	    {
	        int id = v.getId();
	        // int action = event.getAction();

	        //Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::onTouch() " + id);

	        if (id == R.id.GraphicView)
	        {
	        	// 画面をタッチした！
	            ((GokigenSurfaceView) v).onTouchEvent(event);
	            return (true);
	        }
	        return (false);
	    }

	    /**
	     *  キーを押したときの操作
	     */
	    public boolean onKey(View v, int keyCode, KeyEvent event)
	    {
	        int action = event.getAction();
	        if ((action == KeyEvent.ACTION_DOWN)&&(keyCode == KeyEvent.KEYCODE_DPAD_CENTER))
	        {
	        	//
	        }

	        Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::onKey() ");
	        return (false);
	    }

	    /**
	     *   メニューへのアイテム追加
	     * @param menu
	     * @return
	     */
	    public Menu onCreateOptionsMenu(Menu menu)
	    {
            // 新規作成
	    	MenuItem menuItem = menu.add(Menu.NONE, MENU_ID_NEW, Menu.NONE, parent.getString(R.string.createnew));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_add);    // 丸プラス

	        // 画像の共有
	    	menuItem = menu.add(Menu.NONE, MENU_ID_SHARE, Menu.NONE, parent.getString(R.string.shareContent));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);   /*  for Android 3.1  */
	    	menuItem.setIcon(android.R.drawable.ic_menu_share);

	    	// 画像のキャプチャ
	    	menuItem = menu.add(Menu.NONE, MENU_ID_CAPTURE, Menu.NONE, parent.getString(R.string.capture_data));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_crop);    // オブジェクトのキャプチャ

	        // オブジェクトの整列
	    	menuItem = menu.add(Menu.NONE, MENU_ID_ALIGN, Menu.NONE, parent.getString(R.string.align_data));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_rotate);    // オブジェクトの整列

	        // タイトルの変更
	    	menuItem = menu.add(Menu.NONE, MENU_ID_RENAME, Menu.NONE, parent.getString(R.string.rename_title));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_edit);    // タイトルの変更

	        // 壁紙の選択
	    	menuItem = menu.add(Menu.NONE, MENU_ID_INSERT_PICTURE, Menu.NONE, parent.getString(R.string.background_data));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_gallery);    // 壁紙の選択

	        // 拡張メニュー
	    	menuItem = menu.add(Menu.NONE, MENU_ID_EXTEND, Menu.NONE, parent.getString(R.string.extend_menu));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_share);    // 拡張メニュー...

	        // 設定
	        menuItem = menu.add(Menu.NONE, MENU_ID_PREFERENCES, Menu.NONE, parent.getString(R.string.preference_name));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
	    	menuItem.setIcon(android.R.drawable.ic_menu_preferences);

            // クレジット情報の表示
	        menuItem = menu.add(Menu.NONE, MENU_ID_ABOUT_GOKIGEN, Menu.NONE, parent.getString(R.string.about_gokigen));
	    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
	        menuItem.setIcon(android.R.drawable.ic_menu_info_details);

	        return (menu);
	    }

	    /**
	     *   メニュー表示前の処理
	     * @param menu
	     * @return
	     */
	    public void onPrepareOptionsMenu(Menu menu)
	    {
	    	menu.findItem(MENU_ID_NEW).setVisible(true);
	    	menu.findItem(MENU_ID_SHARE).setVisible(true);
	    	menu.findItem(MENU_ID_CAPTURE).setVisible(true);
	    	menu.findItem(MENU_ID_ALIGN).setVisible(true);
	    	menu.findItem(MENU_ID_RENAME).setVisible(true);
	    	menu.findItem(MENU_ID_INSERT_PICTURE).setVisible(true);
	    	menu.findItem(MENU_ID_EXTEND).setVisible(true);
	    	menu.findItem(MENU_ID_PREFERENCES).setVisible(true);
	    	menu.findItem(MENU_ID_ABOUT_GOKIGEN).setVisible(true);
	    	return;
	    }

	    /**
	     *   メニューのアイテムが選択されたときの処理
	     * @param item
	     * @return
	     */
	    public boolean onOptionsItemSelected(MenuItem item)
	    {
	    	boolean result = false;
	    	switch (item.getItemId())
	    	{
	    	  case MENU_ID_PREFERENCES:
	    	    showPreference();
	    		result = true;
	    		break;

	    	  case MENU_ID_ABOUT_GOKIGEN:
	    		showAboutGokigen();
		    	result = true;
		        break;

	    	  case MENU_ID_NEW:
	    		createNewScreen();
	    		result = true;
	    		break;

	    	  case MENU_ID_EXTEND:
	    		// 拡張メニューを呼び出す
	    		callExtendMenu();
	    		result = true;
	    		break;

	    	  case MENU_ID_ALIGN:
                // オブジェクトの整列を行う
	    		alignData();
                result = true;
		    	break;

	    	  case MENU_ID_RENAME:
	    		// タイトル名の変更  (テキスト編集ダイアログを表示する)
	            showInfoMessageEditDialog();
	    		result = true;
	    		break;

	    	  case MENU_ID_INSERT_PICTURE:
	            // 背景画像の設定を行う
		    	insertPicture();
	            result = true;
			    break;

	    	  case MENU_ID_CAPTURE:
	    		// 画面キャプチャを指示された場合...
	    		doCapture(false);
	    		result = true;
	    		break;

	    	  case MENU_ID_SHARE:
	    		// 画面キャプチャ＆共有を指示された場合...
	    		doCapture(true);
	    		result = true;
	    		break;

	    	  case android.R.id.home:
	    		/** アイコンが押された時の処理... **/
		        // テキスト編集ダイアログを表示する
	            showInfoMessageEditDialog();
			    result = true;
			    break;

	    	  default:
	    		result = false;
	    		break;
	    	}
	    	return (result);
	    }

	    /**
	     *  画像ファイルの挿入 (データファイルの更新)
	     * 
	     */
	    private void insertPicture()
	    {
	    	Intent intent = new Intent();
	    	intent.setType("image/*");
	    	intent.setAction(Intent.ACTION_GET_CONTENT);
	        parent.startActivityForResult(intent, MENU_ID_INSERT_PICTURE);
	    }

	    /**
	     *    画面キャプチャの実施
	     * 
	     * @param isShare
	     */
	    private void doCapture(boolean isShare)
	    {
	    	// 画面のスクリーンショットをとる処理を実行する
	    	dataInOutManager.doScreenCapture((String) parent.getTitle(), objectHolder, objectDrawer, isShare);

        	// 画面を再描画する
            redrawSurfaceview();
	    }

	    /**
	     *   アプリの情報を表示する
	     * 
	     */
	    private void showAboutGokigen()
	    {
	        // アプリの情報(クレジット)を表示する！
	    	parent.showDialog(R.id.info_about_gokigen);
	    }

	    /**
	     *   拡張メニューを呼び出す
	     * 
	     */
	    private void callExtendMenu()
	    {
	    	// 現在表示中のデータをファイルに保存する
	    	dataInOutManager.saveFile((String) parent.getTitle(), true);
	    	
	    	// 現在読み込んでいるファイルのファイル名を生成する
	    	String fullPath = dataInOutManager.getDataFileFullPath((String) parent.getTitle(), ".xml");
	    	
            //  ここで拡張メニューを呼び出す	    	
	        // (渡すデータを作って Intentとする)
	        Intent intent = new Intent();
	        
	        intent.setAction(ExtensionActivity.MEMOMA_EXTENSION_LAUNCH_ACTIVITY);
	        intent.putExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_FULLPATH, fullPath);
	        intent.putExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_TITLE, (String) parent.getTitle());

	        // データ表示用Activityを起動する
	        parent.startActivityForResult(intent, MENU_ID_EXTEND);
	    }

	    /**
	     *    データの読み込みを行う
	     * 
	     */
	    private void prepareMeMoMaInfo()
	    {
	    	//  設定に記録されているデータを画面のタイトルに反映させる
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
	    	String memomaInfo = preferences.getString("MeMoMaInfo", parent.getString(R.string.app_name));
	    	parent.setTitle(memomaInfo);

	        // アクションバーとファイル名の準備
			android.support.v7.app.ActionBar bar = parent.getSupportActionBar();
	        if (bar != null)
	        {
				dataInOutManager.prepare(objectHolder, bar, memomaInfo);
			}

            //dataInOutManager.loadFile((String) parent.getTitle());
	    }

	    /**
	     *   データの保存を行う
	     *   
	     *   
	     *   @param forceOverwrite  trueの時は、ファイル名が確定していたときは（確認せずに）上書き保存を自動で行う。
	     *   
	     */
	    private void saveData(boolean forceOverwrite)
	    {
            dataInOutManager.saveFile((String) parent.getTitle(), forceOverwrite);
	    }

	    /**
	     *   データの整列を行う
	     * 
	     */
	    private void alignData()
	    {
	    	ObjectAligner aligner = new ObjectAligner(parent, this);
	    	aligner.execute(objectHolder);
	    }
	    
	    /**
	     *    メッセージ編集ダイアログを表示する
	     * 
	     */
	    private void showInfoMessageEditDialog()
	    {
	    	parent.showDialog(R.id.editTextArea);        	    	
	    }

	    /**
	     *   新規作成が指示されたとき...全部クリアして作りなおして良いか確認する。
	     * 
	     */
	    private void createNewScreen()
	    {
	    	parent.showDialog(R.id.confirmation);
	    }
	    
	    /**
	     *    接続線の設定ダイアログを表示する
	     */
	    private void selectLineShapeDialog()
	    {
	    	// 接続線の設定ダイアログを表示する...
	    	parent.showDialog(R.id.selectline_dialog);
	    }

	    /**
	     *    メッセージ編集ダイアログの表示を準備する
	     * 
	     */
	    private void prepareInfoMessageEditDialog(Dialog dialog)
	    {
	    	String message = (String) parent.getTitle();
         	editTextDialog.prepare(dialog, this, parent.getString(R.string.dataTitle), message, true);
	    }

	    /**
	     *    メッセージ編集ダイアログの表示を準備する
	     * 
	     */
	    private void prepareConfirmationDialog(Dialog dialog)
	    {
    		// Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::prepareConfirmationDialog() " );
	    }

	    /**
	     *    オブジェクト入力用ダイアログの表示を準備する
	     * 
	     */
	    private void prepareObjectInputDialog(Dialog dialog)
	    {
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::prepareObjectInputDialog(), key: " + selectedObjectKey);
    		
    		//  ダイアログの準備を行う
    		objectDataInputDialog.prepareObjectInputDialog(dialog, selectedObjectKey);
    		
	    }

	    /**
	     *   アイテム選択ダイアログの表示を準備する
	     * 
	     * @param dialog
	     */
	    private void prepareItemSelectionDialog(Dialog dialog)
	    {
	    	// アイテム選択ダイアログの表示設定
	    	// (動的変更時。。。今回は固定なので何もしない）
	    }

	    /**
	     *    接続線選択用ダイアログの表示を準備する
	     * 
	     */
	    private void prepareLineSelectionDialog(Dialog dialog)
	    {
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::prepareLineSelectionDialog(), key: " + selectedObjectKey);
    		
    		//  ダイアログの準備を行う
    		lineSelectionDialog.prepareSelectLineShapeDialog(dialog, selectedObjectKey);
	    }

	    /**
	     *  設定画面を表示する処理
	     */
	    private void showPreference()
	    {
	        try
	        {
	            // 設定画面を呼び出す
	            Intent prefIntent = new Intent(parent,jp.sourceforge.gokigen.memoma.Preference.class);
	            parent.startActivityForResult(prefIntent, MENU_ID_PREFERENCES);
	        }
	        catch (Exception e)
	        {
	             // 例外発生...なにもしない。
	        	 //updater.showMessage("ERROR", MainUpdater.SHOWMETHOD_DONTCARE);
	        }
	    }

        /**
         *    接続線の形状を反映させる
         * 
         */
	    private void setLineStyle()
	    {
	    	int buttonId = LineStyleHolder.getLineShapeImageId(lineStyleHolder.getLineStyle(), lineStyleHolder.getLineShape());
	        final ImageButton lineStyleObj = (ImageButton) parent.findViewById(R.id.LineStyleButton);
	        lineStyleObj.setImageResource(buttonId);	
	    }

	    /**
	     *    オブジェクトが生成された！
	     * 
	     */
	    public void objectCreated()
	    {
      	     // ここで動作モードを移動モードに戻す。
    		 drawModeHolder.changeOperationMode(OperationModeHolder.OPERATIONMODE_MOVE);
    		 updateButtons(OperationModeHolder.OPERATIONMODE_MOVE);

       	   // 画面を再描画する
       	   redrawSurfaceview();
	    }

	    /**
	     *    空き領域がタッチされた！
	     * 
	     */
	    public int touchedVacantArea()
	    {
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
	    	return (Integer.parseInt(preferences.getString("operationMode", "0")));
	    }

	    /**
	     *    空き領域でタッチが離された！
	     * 
	     */
	    public int touchUppedVacantArea()
	    {
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
	    	return (Integer.parseInt(preferences.getString("operationMode", "0")));
	    }

	    /**
	     *    オブジェクトを本当に削除して良いか確認した後に、オブジェクトを削除する。
	     * 
	     * @param key
	     */
	    private void removeObject(Integer key)
	    {
	    	// 本当に消して良いか、確認をするダイアログを表示して、OKが押されたら消す。
	    	 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parent);
	    	 alertDialogBuilder.setTitle(parent.getString(R.string.deleteconfirm_title));
	    	 alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
	    	 alertDialogBuilder.setMessage(parent.getString(R.string.deleteconfirm_message));

	    	 // 削除するオブジェクトのキーを覚えこむ。
	    	 objectKeyToDelete = key;

	    	 // OKボタンの生成
	    	 alertDialogBuilder.setPositiveButton(parent.getString(R.string.confirmYes), new DialogInterface.OnClickListener()
             {
                  public void onClick(DialogInterface dialog, int id)
                  {
               		    //  削除モードの時... 確認後削除だけど、今は確認なしで削除を行う。
                		objectHolder.removePosition(objectKeyToDelete);
                		
                		// 削除するオブジェクトに接続されている線もすべて削除する
                		objectHolder.getConnectLineHolder().removeAllConnection(objectKeyToDelete);
                		
                		// ダイアログを閉じる
                 	   dialog.dismiss();

                 	   // ここで動作モードを削除モードから移動モードに戻す。
       	    		   drawModeHolder.changeOperationMode(OperationModeHolder.OPERATIONMODE_MOVE);
       	    		   updateButtons(OperationModeHolder.OPERATIONMODE_MOVE);
       	    		

                 	   // 画面を再描画する
                 	   redrawSurfaceview();
                  }
              });
	    	 
	    	 // Cancelボタンの生成
	    	 alertDialogBuilder.setNegativeButton(parent.getString(R.string.confirmNo), new DialogInterface.OnClickListener()
             {
                 public void onClick(DialogInterface dialog, int id)
                 {
                	 dialog.cancel();
                 }
             });
    		
	         // ダイアログはキャンセル可能に設定する
	         alertDialogBuilder.setCancelable(true);

	         // ダイアログを表示する
	         AlertDialog alertDialog = alertDialogBuilder.create();
	         alertDialog.show();

	         return;
	    }

	    /**
	     *    オブジェクトを複製する
	     * 
	     * @param key
	     */
	    private void duplicateObject(Integer key)
	    {
	    	// 選択中オブジェクトを複製する
	        objectHolder.duplicatePosition(key);

	        // 画面を再描画する
      	   redrawSurfaceview();
	    }	    

	    /**
	     *    オブジェクトを拡大する
	     * 
	     * @param key
	     */
	    private void expandObject(Integer key)
	    {
	    	// 選択中オブジェクトを拡大する
	        objectHolder.expandObjectSize(key);

	        // 画面を再描画する
      	   redrawSurfaceview();
	    }	    
	    /**
	     *    オブジェクトを縮小する
	     * 
	     * @param key
	     */
	    private void shrinkObject(Integer key)
	    {
	    	// 選択中オブジェクトを縮小する
	        objectHolder.shrinkObjectSize(key);

	        // 画面を再描画する
      	   redrawSurfaceview();
	    }

	     private void setButtonBorder(ImageButton button, boolean isHighlight)
	      {
	      	try
	      	{
	              BitmapDrawable btnBackgroundShape = (BitmapDrawable)button.getBackground();
	              if (isHighlight == true)
	              {
//	                 	btnBackgroundShape.setColorFilter(Color.rgb(51, 181, 229), Mode.LIGHTEN);
	              	btnBackgroundShape.setColorFilter(Color.BLUE, Mode.LIGHTEN);
	              }
	              else
	              {
	              	btnBackgroundShape.setColorFilter(Color.BLACK, Mode.LIGHTEN);
	              } 
	      	}
	      	catch (Exception ex)
	      	{
	      		// 
	      		Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::setButtonBorder(): " + ex.toString());
	      	}
	      }	     

	     /**
	     *   ボタンを更新する
	     * 
	     */
	    private void updateButtons(int mode)
	    {
	        final ImageButton createObjectButton = (ImageButton) parent.findViewById(R.id.CreateObjectButton);
	        final ImageButton deleteObjectButton = (ImageButton) parent.findViewById(R.id.DeleteObjectButton);

	        if (mode == OperationModeHolder.OPERATIONMODE_DELETE)
	        {
	        	setButtonBorder(createObjectButton, false);
	        	setButtonBorder(deleteObjectButton, true);
	        }
	        else if (mode == OperationModeHolder.OPERATIONMODE_CREATE)
	        {
	        	setButtonBorder(createObjectButton, true);
	        	setButtonBorder(deleteObjectButton, false);	    		
	        }
	        else // if (mode == OperationModeHolder.OPERATIONMODE_MOVE)
	    	{
	        	setButtonBorder(createObjectButton, false);
	        	setButtonBorder(deleteObjectButton, false);	    		
	    	}
	    }

	    
	    /**
	     *   オブジェクトが選択された（長押しで！）
	     * 
	     */
	    public void objectSelectedContext(Integer key)
	    {
	    	Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::objectSelectedContext(),  key:" + key);
	    	selectedContextKey = key;

	    	// オブジェクトのアイテム選択ダイアログを表示する...
	    	parent.showDialog(MENU_ID_OPERATION);

	    }
	    
	    
	    /**
	     *   オブジェクトが選択された！
	     * 
	     */
	    public boolean objectSelected(Integer key)
	    {
	        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
	    	int operationMode = Integer.parseInt(preferences.getString("operationMode", "0"));
	    	if (operationMode == OperationModeHolder.OPERATIONMODE_DELETE)
	    	{
	    		// オブジェクトを削除する
	    		removeObject(key);
	    		
	    		return (true);
	    	}
	    	//if ((operationMode == OperationModeHolder.OPERATIONMODE_MOVE)||
	    	//		(operationMode == OperationModeHolder.OPERATIONMODE_CREATE))
	    	{
	    		// 選択されたオブジェクトを記憶する
		    	selectedObjectKey = key;
		    	Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::objectSelected() key : " + key);

		    	// オブジェクトの詳細設定ダイアログを表示する...
		    	parent.showDialog(R.id.objectinput_dialog);
	    	}
	    	return (true);
	    }
	    
	    /**
	     *  ダイアログの生成
	     * 
	     */
	    public Dialog onCreateDialog(int id)
	    {
            if (id == R.id.info_about_gokigen)
    	    {
            	// クレジットダイアログを表示
    		    CreditDialog dialog = new CreditDialog(parent);
    		    return (dialog.getDialog());
    	    }
            if (id == R.id.editTextArea)
            {
        		// 変更するテキストを表示
                return (editTextDialog.getDialog());
            }
            if (id == R.id.confirmation)
            {
            	// 確認するメッセージを表示する
            	return (confirmationDialog.getDialog());
            }
            if (id == R.id.objectinput_dialog)
            {
            	// オブジェクト入力のダイアログを表示する
            	return (objectDataInputDialog.getDialog());
            }
            if (id == MENU_ID_OPERATION)
            {
            	// アイテム選択ダイアログの準備を行う
            	return (itemSelectionDialog.getDialog());
            }
            if (id == R.id.selectline_dialog)
            {
            	// 接続線選択ダイアログの準備を行う
            	return (lineSelectionDialog.getDialog());
            }
	    	return (null);
	    }

	    /**
	     *  ダイアログ表示の準備
	     * 
	     */
	    public void onPrepareDialog(int id, Dialog dialog)
	    {
            if (id == R.id.editTextArea)
            {
            	// 変更するデータを表示する
            	prepareInfoMessageEditDialog(dialog);
            	return;
            }
            if (id == R.id.confirmation)
            {
            	// 確認ダイアログを表示する。
            	prepareConfirmationDialog(dialog);
            	return;
            }
            if (id == R.id.objectinput_dialog)
            {
            	// オブジェクト入力のダイアログを表示する
            	prepareObjectInputDialog(dialog);
            }
            if (id == MENU_ID_OPERATION)
            {
            	// オブジェクト操作選択のダイアログを表示する
            	prepareItemSelectionDialog(dialog);
            }
            if (id == R.id.selectline_dialog)
            {
            	// 接続線選択のダイアログを表示する
            	prepareLineSelectionDialog(dialog);
            }
	    }

        /**
         *    新規状態に変更する。
         * 
         */
        public void acceptConfirmation()
        {
            //
        	Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::acceptConfirmation()");
        	
        	// オブジェクトデータをクリアする。
    	    objectHolder.removeAllPositions();  // オブジェクトの保持クラス
    	    objectHolder.getConnectLineHolder().removeAllLines();  // オブジェクト間の接続状態保持クラス

        	// 画面の倍率と表示位置を初期状態に戻す
        	if (objectDrawer != null)
        	{
    	        final SeekBar zoomBar = (SeekBar) parent.findViewById(R.id.ZoomInOut);
        		objectDrawer.resetScaleAndLocation(zoomBar);
        	}

        	/**
        	// 題名を "無題"に変更し、関係情報をクリアする
        	String newName = parent.getString(R.string.no_name);
        	parent.setTitle(newName);
	    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MeMoMaInfo", newName);
            editor.commit();
            **/
            
        	// 画面を再描画する
            redrawSurfaceview();

            // ファイル名選択ダイアログを開く
            showInfoMessageEditDialog();

        }

        /**
         *   画面を再描画する
         * 
         */
        private void redrawSurfaceview()
        {
        	final GokigenSurfaceView surfaceview = (GokigenSurfaceView) parent.findViewById(R.id.GraphicView);
        	surfaceview.doDraw();        	
        }
        
        /**
         *    不許可。何もしない。
         * 
         */
        public  void rejectConfirmation()
        {
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::rejectConfirmation()");
        }

        /**
         *   オブジェクトが整列された時の処理
         * 
         */
        public void objectAligned()
        {
            // 画面の再描画を指示する
       	    redrawSurfaceview();        	
        }
        
        /**
         *   オブジェクト編集ダイアログが閉じられた時の処理
         * 
         */
        public void finishObjectInput()
        {
            // 画面の再描画を指示する
       	    redrawSurfaceview();
        }
        
        /**
         *   オブジェクト編集ダイアログが閉じられた時の処理
         * 
         */
        public void cancelObjectInput()
        {
            // 何もしない	
        }
        
        
        /**
         *   現在編集中かどうかを知る
         * 
         * @return
         */
        public boolean isEditing()
        {
        	return (isEditing);
        }

        /**
         *   現在編集中のフラグを更新する
         * 
         * @param value
         */
        public void setIsEditing(boolean value)
        {
        	isEditing = value;
        }

        /**
         *   アイテムが選択された！
         * 
         */
        public void itemSelected(int index, String itemValue)
        {
            //
        	Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::itemSelected() : " + itemValue + " [" + index + "]");
        	
        	if (index == ObjectOperationCommandHolder.OBJECTOPERATION_DELETE)
        	{
        		// オブジェクト削除の確認
	    		removeObject(selectedContextKey);
        	}
        	else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_DUPLICATE)
        	{
        		// オブジェクトの複製
        		duplicateObject(selectedContextKey);        		
        	}
        	else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_SIZEBIGGER)
        	{
        		// オブジェクトの拡大
        		expandObject(selectedContextKey);
        	}
        	else if (index == ObjectOperationCommandHolder.OBJECTOPERATION_SIZESMALLER)
        	{
        		// オブジェクトの縮小
        		shrinkObject(selectedContextKey);
        	}
        }

        /**
         *    (今回未使用)
         * 
         */
        public void itemSelectedMulti(String[] items, boolean[] status)
        {
        	
        }
        public void canceledSelection()
        {
        	
        }
        
        public void onSaveInstanceState(Bundle outState)
        {
    	    /* ここで状態を保存 */ 
    	    //Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::onSaveInstanceState()");
        }
        
        public void onRestoreInstanceState(Bundle savedInstanceState)
        {
        	/* ここで状態を復元 */
    	    Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::onRestoreInstanceState()");
        }

        public boolean finishTextEditDialog(String message)
        {
        	if ((message == null)||(message.length() == 0))
        	{
                // データが入力されていなかったので、何もしない。
        		return (false);
        	}
        	
        	// 文字列を記録
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("MeMoMaInfo", message);
            editor.commit();

            // タイトルに設定
            parent.setTitle(message);

            // 保存シーケンスを一度走らせる 
            saveData(true);

            // ファイル選択リストの更新
            dataInOutManager.updateFileList(message, parent.getSupportActionBar());

            return (true);
        }
        public boolean cancelTextEditDialog()
        {
            return (false);
        }

        /**
         *    接続線
         * 
         */
        public void finishSelectLineShape(int style, int shape, int thickness)
        {
        	int buttonId = LineStyleHolder.getLineShapeImageId(style, shape);
            final ImageButton lineStyleObj = (ImageButton) parent.findViewById(R.id.LineStyleButton);
            lineStyleObj.setImageResource(buttonId);	
            //Log.v(Main.APP_IDENTIFIER, "MeMoMaListener::finishSelectLineShape() buttonId:" + buttonId);
        }

        /**
         * 
         * 
         */
        public void cancelSelectLineShape()
        {
        	
        }
}
