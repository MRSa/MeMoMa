package jp.sourceforge.gokigen.memoma.extension;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import jp.sourceforge.gokigen.memoma.fileio.ExternalStorageFileUtility;
import jp.sourceforge.gokigen.memoma.dialogs.FileSelectionDialog;
import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaConnectLineHolder;
import jp.sourceforge.gokigen.memoma.fileio.MeMoMaFileExportCsvProcess;
import jp.sourceforge.gokigen.memoma.fileio.MeMoMaFileImportCsvProcess;
import jp.sourceforge.gokigen.memoma.fileio.MeMoMaFileLoadingProcess;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.SharedIntentInvoker;
import jp.sourceforge.gokigen.memoma.listitem.SymbolListArrayAdapter;
import jp.sourceforge.gokigen.memoma.listitem.SymbolListArrayItem;

/**
 *    リスト形式で表示・エクスポート
 * 
 * @author MRSa
 *
 */
public class ExtensionActivityListener  implements OnClickListener, MeMoMaFileLoadingProcess.IResultReceiver, MeMoMaFileExportCsvProcess.IResultReceiver, FileSelectionDialog.IResultReceiver, MeMoMaFileImportCsvProcess.IResultReceiver
{
    private final int MENU_ID_EXPORT= (Menu.FIRST + 1);
    private final int MENU_ID_SHARE = (Menu.FIRST + 2);
    private final int MENU_ID_IMPORT = (Menu.FIRST + 3);

    private static final String EXTENSION_DIRECTORY = "/exported";
    
    private ExternalStorageFileUtility fileUtility;
	private MeMoMaObjectHolder objectHolder;
	private FileSelectionDialog fileSelectionDialog = null;
	
	private boolean isShareExportedData = false;

	private List<SymbolListArrayItem> listItems = null;
    
    private final Activity parent;  // 親分
	
	/**
     *  コンストラクタ
     * @param argument parent activity
     */
	ExtensionActivityListener(Activity argument)
    {
        parent = argument;
        fileUtility = new ExternalStorageFileUtility(Main.APP_BASEDIR);
        objectHolder = new MeMoMaObjectHolder(parent, new MeMoMaConnectLineHolder());
    }
    /**
     *  起動時にデータを準備する
     * 
     * @param myIntent intent information
     */
    public void prepareExtraDatas(Intent myIntent)
    {
        try
        {
            // Intentで拾ったデータを読み出す (初期化データ)
        	//fullPath = myIntent.getStringExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_FULLPATH);
        	objectHolder.setDataTitle(myIntent.getStringExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_TITLE));

            // Preferenceに記憶されたデータがあればそれを取得する
            // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
         }
        catch (Exception ex)
        {
            Log.v(Main.APP_IDENTIFIER, "Exception :" + ex.toString());
        }        
    }

    /**
     *  がっつりこのクラスにイベントリスナを接続する
     * 
     */
    public void prepareListener()
    {
        // フィルタ設定ボタン
        final ImageButton filterButton = parent.findViewById(R.id.SetFilterButton);
        filterButton.setOnClickListener(this);

    }

    /**
     *  終了準備
     */
    public void finishListener()
    {

    }

    /**
     *  スタート準備
     */
    public void prepareToStart()
    {
		Log.v(Main.APP_IDENTIFIER, "ExtensionActivityListener::prepareToStart() : "  + objectHolder.getDataTitle());

		//  アクションバーを表示する
        ActionBar bar = parent.getActionBar();
        if (bar != null)
        {
            bar.show();
            bar.setTitle(objectHolder.getDataTitle());
        }

        // ファイルをロードする！
		// (AsyncTaskを使ってデータを読み込む)
		MeMoMaFileLoadingProcess asyncTask = new MeMoMaFileLoadingProcess(parent, fileUtility, this);
        asyncTask.execute(objectHolder);
    }

    /**
     *    詳細データを表示する。
     *
     */
    private void showDetailData(String first, String second, String third)
    {
        Log.v(Main.APP_IDENTIFIER, "SELECTED: " + first + " " + second + " " + third);
    }

    /**
     *  終了準備
     */
    public void shutdown()
    {
    	
    }
    
    /**
     *  他画面から戻ってきたとき...
     *
     *
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // なにもしない...
        Log.v(Main.APP_IDENTIFIER, "rc: " + requestCode + " rs: " + resultCode + " it: "  + data.getDataString());
    }

    /**
     *   クリックされたときの処理
     */
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.SetFilterButton)
        {
        	 // フィルタ設定ボタンが押された！
            Log.v(Main.APP_IDENTIFIER, "Selected Filter");
        }
    }
    
/*
    **
     *   触られたときの処理
     *
     *
    public boolean onTouch(View v, MotionEvent event)
    {
        Log.v(Main.APP_IDENTIFIER, " " + v.toString() + " " + event.toString());
        // int id = v.getId();
        // int action = event.getAction();

        return (false);
    }
*/

    /**
     *   メニューへのアイテム追加
     *
     */
    public Menu onCreateOptionsMenu(Menu menu)
    {
    	MenuItem menuItem = menu.add(Menu.NONE, MENU_ID_SHARE, Menu.NONE, parent.getString(R.string.export_csv));
    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);   /*  for Android 3.1  */
    	menuItem.setIcon(android.R.drawable.ic_menu_share);

    	menuItem = menu.add(Menu.NONE, MENU_ID_EXPORT, Menu.NONE, parent.getString(R.string.shareContent));
    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);   /*  for Android 3.1  */
    	menuItem.setIcon(android.R.drawable.ic_menu_save);

    	menuItem = menu.add(Menu.NONE, MENU_ID_IMPORT, Menu.NONE, parent.getString(R.string.import_csv));
    	menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);   /*  for Android 3.1  */
    	menuItem.setIcon(android.R.drawable.ic_menu_edit);

    	return (menu);
    }
    
    /**
     *   メニュー表示前の処理
     *
     */
    public void onPrepareOptionsMenu(Menu menu)
    {
    	menu.findItem(MENU_ID_SHARE).setVisible(true);
    	menu.findItem(MENU_ID_EXPORT).setVisible(true);
    	menu.findItem(MENU_ID_IMPORT).setVisible(true);
    }

    /**
     *   メニューのアイテムが選択されたときの処理
     *
     */
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	boolean result;
    	switch (item.getItemId())
    	{
          case MENU_ID_EXPORT:
            // 表示中データのエクスポート
  		    export_as_csv(false);
    		result = true;
    		break;

          case MENU_ID_SHARE:
        	export_as_csv(true);
        	result = true;
        	break;

          case MENU_ID_IMPORT:
        	// データのインポート
        	importObjectFromCsv();
        	result = true;
        	break;

    	  default:
    		result = false;
    		break;
    	}
    	return (result);
    }


    /**
     *   CSV形式でデータをインポートする
     * 
     */
    private void importObjectFromCsv()
    {
    	// データのインポート
    	parent.showDialog(R.id.listdialog);    	
    }

    /**
     *   データをCSV形式で出力する
     * 
     */
    private void export_as_csv(boolean isShare)
    {
    	isShareExportedData = isShare;

    	// AsyncTaskを使ってデータをエクスポートする
    	MeMoMaFileExportCsvProcess asyncTask = new MeMoMaFileExportCsvProcess(parent, fileUtility, this);
        asyncTask.execute(objectHolder);
    }
    
    /**
     *  ダイアログの生成
     * 
     */
    public Dialog onCreateDialog(int id)
    {
    	if (id == R.id.listdialog)
    	{
    		fileSelectionDialog = new FileSelectionDialog(parent, parent.getString(R.string.dialogtitle_selectcsv), fileUtility, ".csv",  this);
    		return (fileSelectionDialog.getDialog());
    	}

      /*
    	if (id == R.id.info_about_gokigen)
    	{
    		CreditDialog dialog = new CreditDialog(parent);
    		return (dialog.getDialog());
    	}
      */
   	    return (null);
    }

    /**
     *    ファイル選択ダイアログの表示を準備する
     * 
     */
    private void prepareFileSelectionDialog(Dialog dialog)
    {
        try
        {
            Log.v(Main.APP_IDENTIFIER, " " + dialog.toString());
            fileSelectionDialog.prepare("", EXTENSION_DIRECTORY);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  ダイアログ表示の準備
     * 
     */
    public void onPrepareDialog(int id, Dialog dialog)
    {
        if (id == R.id.listdialog)
        {
        	// CSVインポートダイアログを準備する
        	prepareFileSelectionDialog(dialog);
        }
    }

    /**
     *    ファイルをロードする途中のバックグラウンド処理...
     * 
     */
	public void onLoadingProcess()
	{
		try
		{
	        // リストに表示するアイテムを生成する
	        listItems = null;
	        listItems = new ArrayList<>();

	        // TODO: 何らかの法則に従って並べ替えをする。

	        Enumeration<Integer> keys = objectHolder.getObjectKeys();
	        while (keys.hasMoreElements())
	        {
	            Integer key = keys.nextElement();
	            MeMoMaObjectHolder.PositionObject pos = objectHolder.getPosition(key);

	            // アイコンの決定
	            int objectStyleIcon = MeMoMaObjectHolder .getObjectDrawStyleIcon(pos.drawStyle);
	            
	            // ユーザチェックの有無表示
	            int userCheckedIcon = (pos.userChecked) ? R.drawable.btn_checked : R.drawable.btn_notchecked;

	            // TODO: アイテム選択時の情報エリアは(ArrayItem側には)用意しているが未使用。
	            SymbolListArrayItem listItem = new SymbolListArrayItem(userCheckedIcon, pos.label, pos.detail, "", objectStyleIcon);

	            listItems.add(listItem);
	        }
	    } catch (Exception ex)
	    {
	        // 例外発生...ログを吐く
	    	Log.v(Main.APP_IDENTIFIER, "ExtensionActivityListener::onLoadingProcess() : " + ex.toString());
	    }	
	}

    /**
     *    ファイルのロード結果を受け取る
     * 
     */
    public void onLoadedResult(String detail)
    {
		Log.v(Main.APP_IDENTIFIER, "ExtensionActivityListener::onLoadedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

		// 読み込んだファイル名をタイトルに設定する
		parent.setTitle(objectHolder.getDataTitle());
		
		// オブジェクト一覧を表示する
		updateObjectList();
		
		// 読み込みしたことを伝達する
		//String outputMessage = parent.getString(R.string.load_data) + " " + objectHolder.getDataTitle() + " " + detail;
        //Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();    	
    }

    /**
     *    ファイルのエクスポート結果を受け取る
     * 
     */
    public void onExportedResult(String exportedFileName, String detail)
    {
		Log.v(Main.APP_IDENTIFIER, "ExtensionActivityListener::onExportedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

		// エクスポートしたことを伝達する
		String outputMessage = parent.getString(R.string.export_csv) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
        
        if (isShareExportedData)
        {
        	// エクスポートしたファイルを共有する
            shareContent(exportedFileName);
        }
    	isShareExportedData = false;
    }
    
    /**
     *    オブジェクト一覧を更新する
     */
    private void updateObjectList()
    {
    	try
    	{
    		// リストアダプターを生成し、設定する
            ListView listView = parent.findViewById(R.id.ExtensionView);
            ListAdapter adapter = new SymbolListArrayAdapter(parent,  R.layout.listarrayitems, listItems);
            listView.setAdapter(adapter);

            // アイテムを選択したときの処理
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                //@Override
                public void onItemClick(AdapterView<?> parentView, View view, int position, long id)
                {
                    ListView listView = (ListView) parentView;
                    SymbolListArrayItem item = (SymbolListArrayItem) listView.getItemAtPosition(position);

                    /// リストが選択されたときの処理...データを開く
                    showDetailData(item.getTextResource1st(), item.getTextResource2nd(), item.getTextResource3rd());
                }
            });
            System.gc();   // いらない（参照が切れた）クラスを消したい
    	}
    	catch (Exception ex)
    	{
    		// 何もしない。
    	}
    }
    
    /**
     *    エクスポートしたファイルを共有する
     * 
     *
     */
    private void shareContent(String fileName)
    {
    	String message = "";
        try
        {
        	// 現在の時刻を取得する
            Calendar calendar = Calendar.getInstance();
    		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            String date =  outFormat.format(calendar.getTime());

            // メールタイトル
            String title = parent.getString(R.string.app_name) + " | "+ objectHolder.getDataTitle() + " | " + date;

            // メールの本文を構築する
            message = message + "Name : " + objectHolder.getDataTitle() + "\n";
            message = message + "exported : " + date + "\n";
            message = message + "number of objects : " + objectHolder.getCount() + "\n";

            // Share Intentを発行する。
            Uri fileURI = FileProvider.getUriForFile(parent,"jp.sourceforge.gokigen.memoma.fileprovider", new File(fileName));
            SharedIntentInvoker.shareContent(parent, MENU_ID_SHARE, title, message, fileURI, "text/plain");
        }
        catch (Exception ex)
        {
            Log.v(Main.APP_IDENTIFIER, "shareContent (fileName : " + fileName + ")");
            ex.printStackTrace();
        }
    }
    
    /**
     *   ファイルが選択された！
     * 
     */
    public void selectedFileName(String fileName)
    {
    	// CSVファイルからオブジェクトをロードするクラスを呼び出す。
        Log.v(Main.APP_IDENTIFIER, "ExtensionActivityListener::selectedFileName() : " + fileName);
        MeMoMaFileImportCsvProcess asyncTask = new MeMoMaFileImportCsvProcess(parent, fileUtility, this, fileName);
        asyncTask.execute(objectHolder);
    }

    /**
     *    インポート結果の受信
     *
     */
    public void onImportedResult(String detail)
    {
		Log.v(Main.APP_IDENTIFIER, "ExtensionActivityListener::onImportedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

		// インポートしたことを伝達する
		String outputMessage = parent.getString(R.string.import_csv) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();

        // 一覧のリストを作りなおす
        onLoadingProcess();
        updateObjectList();
    }    
}
