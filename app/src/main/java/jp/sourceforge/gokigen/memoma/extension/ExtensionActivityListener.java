package jp.sourceforge.gokigen.memoma.extension;

import static jp.sourceforge.gokigen.memoma.Main.APP_NAMESPACE;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import jp.sourceforge.gokigen.memoma.holders.PositionObject;
import jp.sourceforge.gokigen.memoma.dialogs.FileSelectionDialog;
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileExportCsvProcess;
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileImportCsvProcess;
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileLoadingProcess;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.io.MeMoMaFileSavingEngine;
import jp.sourceforge.gokigen.memoma.io.SharedIntentInvoker;
import jp.sourceforge.gokigen.memoma.listitem.SymbolListArrayAdapter;
import jp.sourceforge.gokigen.memoma.listitem.SymbolListArrayItem;

/**
 *    リスト形式で表示・エクスポート
 */
public class ExtensionActivityListener  implements OnClickListener, MeMoMaFileLoadingProcess.IResultReceiver, MeMoMaFileExportCsvProcess.IResultReceiver, FileSelectionDialog.IResultReceiver, MeMoMaFileImportCsvProcess.IResultReceiver
{
    private final String TAG = toString();
    private static final int PICK_CSV_FILE = 2020;
    private static final int PICK_XML_FILE = 2030;

    private final int MENU_ID_EXPORT= (Menu.FIRST + 1);
    private final int MENU_ID_EXPORT_XML = (Menu.FIRST + 2);
    private final int MENU_ID_SHARE = (Menu.FIRST + 3);
    private final int MENU_ID_IMPORT = (Menu.FIRST + 4);
    private final int MENU_ID_IMPORT_XML = (Menu.FIRST + 5);
    private final int MENU_ID_DELETE = (Menu.FIRST + 6);

	private final MeMoMaObjectHolder objectHolder;
	//private FileSelectionDialog fileSelectionDialog = null;
	private boolean isShareExportedData = false;
	private List<SymbolListArrayItem> listItems = null;
    private final AppCompatActivity parent;  // 親分

	/**
     *  コンストラクタ
     */
	ExtensionActivityListener(AppCompatActivity argument)
    {
        parent = argument;
        objectHolder = new MeMoMaObjectHolder(parent);
    }
    /**
     *  起動時にデータを準備する
     */
    public void prepareExtraDatas(Intent myIntent)
    {
        try
        {
            String dataTitle = myIntent.getStringExtra(ExtensionActivity.MEMOMA_EXTENSION_DATA_TITLE);

            SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(parent);
            String prefTitleString = preferences.getString("content_data_title", "");
            if (prefTitleString.length() > 0)
            {
                // Preferenceに タイトル名が記録されていたら、上書きする
                dataTitle = prefTitleString;
            }
            // Intentで拾ったデータを読み出す (初期化データ)
            objectHolder.setDataTitle(dataTitle);
        }
        catch (Exception ex)
        {
            Log.v(TAG, "Exception :" + ex.getMessage());
        }
    }

    private void setPreferenceString(String title)
    {
        try
        {
            SharedPreferences preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(parent);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("content_data_title", title);
            editor.apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *  がっつりこのクラスにイベントリスナを接続する
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
        //setPreferenceString("");
    }

    /**
     *  スタート準備
     */
    public void prepareToStart()
    {
        try
        {
            Log.v(TAG, "ExtensionActivityListener::prepareToStart() : "  + objectHolder.getDataTitle());

            //  アクションバーを表示する
            ActionBar bar = parent.getSupportActionBar();
            if (bar != null)
            {
                bar.setIcon(R.drawable.icon1);
                bar.setTitle(objectHolder.getDataTitle());
                bar.show();
            }

            // ファイルをロードする！
            // (AsyncTaskを使ってデータを読み込む)
            MeMoMaFileLoadingProcess asyncTask = new MeMoMaFileLoadingProcess(parent, this);
            asyncTask.execute(objectHolder);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *    詳細データを表示する。
     */
    private void showDetailData(String first, String second, String third)
    {
        Log.v(TAG, "SELECTED: " + first + " " + second + " " + third);
    }

    /**
     *  終了準備
     */
    public void shutdown()
    {
        //setPreferenceString("");
    }

    private void imporObjectFromCsv(final Uri uri)
    {
        try
        {
            // Perform operations on the document using its URI.
            Thread thread = new Thread(() -> {
                ExtensionCsvImport importer = new ExtensionCsvImport(parent, objectHolder, uri);
                importer.importFromCsvFile();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
                String backgroundUri = preferences.getString("backgroundUri","");
                String userCheckboxString = preferences.getString("userCheckboxString","");

                // データの保管メイン
                MeMoMaFileSavingEngine savingEngine = new MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString);
                String result = savingEngine.saveObjects(objectHolder);
                parent.runOnUiThread(() -> {
                    try
                    {
                        onImportedResult(result);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void importDataFromXml(final Uri uri)
    {
        try
        {
            // Perform operations on the document using its URI.
            Thread thread = new Thread(() -> {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
                String backgroundUri = preferences.getString("backgroundUri","");
                String userCheckboxString = preferences.getString("userCheckboxString","");

                // データの保管を実施する (現状)
                MeMoMaFileSavingEngine savingEngine = new MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString);
                String result0 = savingEngine.saveObjects(objectHolder);
                Log.v(TAG, "Saved : " + result0);

                //
                ExtensionXmlImport importer = new ExtensionXmlImport(parent, objectHolder, uri);
                String result1 = importer.importFromXmlFile();

                // データの保管を実施する (新規)
                MeMoMaFileSavingEngine savingEngine2 = new MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString);
                String result = savingEngine2.saveObjects(objectHolder) + " " + result1;
                Log.v(TAG, "=== Data Saved : " + objectHolder.getDataTitle() + " " + result + " " + result1);
                parent.runOnUiThread(() -> {
                    try
                    {
                        // 読み込んだファイル名をタイトルに設定する
                        parent.setTitle(objectHolder.getDataTitle());

                        // タイトルバーの更新...
                        ActionBar bar = parent.getSupportActionBar();
                        if (bar != null)
                        {
                            bar.setIcon(R.drawable.icon1);
                            bar.setTitle(objectHolder.getDataTitle());
                            bar.show();
                        }
                        onImportedResultXml(result);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                });
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  他画面から戻ってきたとき...
     */
    public void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {
        try
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (resultData != null)
                {
                    Uri uri = resultData.getData();
                    if (requestCode == PICK_CSV_FILE)
                    {
                        imporObjectFromCsv(uri);
                    }
                    else if (requestCode == PICK_XML_FILE)
                    {
                        importDataFromXml(uri);
                    }
                    else
                    {
                        Log.v(TAG, "========== rc: " + requestCode + " rs: " + resultCode + " uri: "  + uri.toString());
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // Log.v(TAG, "rc: " + requestCode + " rs: " + resultCode + " it: "  + resultData.getDataString());
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
            Log.v(TAG, "Selected Filter");
        }
    }

    /**
     *   メニューへのアイテム追加
     *
     */
    public Menu onCreateOptionsMenu(Menu menu)
    {
        try
        {
            MenuItem menuItem = menu.add(Menu.NONE, MENU_ID_SHARE, Menu.NONE, parent.getString(R.string.export_csv));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menuItem.setIcon(android.R.drawable.ic_menu_share);

            menuItem = menu.add(Menu.NONE, MENU_ID_EXPORT, Menu.NONE, parent.getString(R.string.shareContent));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menuItem.setIcon(android.R.drawable.ic_menu_save);

            menuItem = menu.add(Menu.NONE, MENU_ID_IMPORT, Menu.NONE, parent.getString(R.string.import_csv));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menuItem.setIcon(android.R.drawable.ic_menu_edit);

            menuItem = menu.add(Menu.NONE, MENU_ID_EXPORT_XML, Menu.NONE, parent.getString(R.string.export_xml));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menuItem.setIcon(android.R.drawable.ic_menu_send);

            menuItem = menu.add(Menu.NONE, MENU_ID_IMPORT_XML, Menu.NONE, parent.getString(R.string.import_xml));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menuItem.setIcon(android.R.drawable.ic_menu_add);

            menuItem = menu.add(Menu.NONE, MENU_ID_DELETE, Menu.NONE, parent.getString(R.string.delete_content));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            menuItem.setIcon(android.R.drawable.ic_menu_delete);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (menu);
    }
    
    /**
     *   メニュー表示前の処理
     *
     */
    public void onPrepareOptionsMenu(Menu menu)
    {
        try
        {
            menu.findItem(MENU_ID_SHARE).setVisible(true);
            menu.findItem(MENU_ID_EXPORT).setVisible(true);
            menu.findItem(MENU_ID_EXPORT_XML).setVisible(true);
            menu.findItem(MENU_ID_IMPORT).setVisible(true);
            menu.findItem(MENU_ID_IMPORT_XML).setVisible(true);
            menu.findItem(MENU_ID_DELETE).setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
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
            case MENU_ID_EXPORT -> {
                // 表示中データのエクスポート
                export_as_csv(false);
                result = true;
            }
            case MENU_ID_EXPORT_XML -> {
                // 表示中データのエクスポート
                export_as_xml();
                result = true;
            }
            case MENU_ID_SHARE -> {
                export_as_csv(true);
                result = true;
            }
            case MENU_ID_IMPORT -> {
                // データのインポート
                importObjectFromCsv();
                result = true;
            }
            case MENU_ID_IMPORT_XML -> {
                // データのインポート(XML形式)
                importObjectFromXml();
                result = true;
            }
            case MENU_ID_DELETE -> {
                // データの削除
                deleteContent();
                result = true;
            }
            default -> result = false;
        }
        return (result);
    }

    /**
     *   XML形式でデータをインポートする
     */
    private void importObjectFromXml()
    {
        try
        {
            // ファイル選択のダイアログを取得する
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + APP_NAMESPACE + "/";
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, path);
                }
                parent.startActivityForResult(intent, PICK_XML_FILE);
            }
            else
            {
                // 旧バージョンの Android での処理... （File Picker ってあったっけ？）


            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   データを削除する
     */
    private void deleteContent()
    {
        try
        {
            //  データの一覧を取得する
            FileSelectionDialog dialog = new FileSelectionDialog(parent, parent.getString(R.string.delete_content), ".xml", fileName -> {
                // fileNameのファイルを削除する...
                Thread thread = new Thread(() -> {
                    // ファイル削除の実処理
                    String targetFile = parent.getFilesDir() + "/" + fileName;
                    if (!(new File(targetFile).delete()))
                    {
                        Log.v(TAG, "Content Delete Failure : " + fileName);
                    }
                });
                try
                {
                    // 削除実処理の実行
                    thread.start();
                    parent.runOnUiThread(() -> {
                        String outputMessage = parent.getString(R.string.delete_content) + " " + fileName;

                        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
            dialog.prepare();
            dialog.getDialog().show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *   CSV形式でデータをインポートする
     */
    private void importObjectFromCsv()
    {
        // ファイル選択のダイアログを取得する
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
        {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + APP_NAMESPACE + "/";
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, path);
            }
            parent.startActivityForResult(intent, PICK_CSV_FILE);
        }
        else
        {
            // 旧バージョンの Android での処理... （File Picker ってあったっけ？）


        }
    }

    /**
     *   データをCSV形式で出力する
     */
    private void export_as_csv(boolean isShare)
    {
    	isShareExportedData = isShare;

    	// AsyncTaskを使ってデータをエクスポートする
    	MeMoMaFileExportCsvProcess asyncTask = new MeMoMaFileExportCsvProcess(parent, this);
        asyncTask.execute(objectHolder);
    }

    /**
     *   データをXML形式で出力する
     */
    private void export_as_xml()
    {
        try
        {
            Thread thread = new Thread(() -> {
                try
                {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
                    String backgroundUri = preferences.getString("backgroundUri", "");
                    String userCheckboxString = preferences.getString("userCheckboxString", "");
                    ExtensionXmlExport exporter = new ExtensionXmlExport(parent, objectHolder, backgroundUri, userCheckboxString);
                    final String result = exporter.exportToXmlFile();
                    parent.runOnUiThread(() -> {
                        try
                        {
                            onExportedResultXml(result);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    });
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
	            PositionObject pos = objectHolder.getPosition(key);

	            // アイコンの決定
	            int objectStyleIcon = MeMoMaObjectHolder .getObjectDrawStyleIcon(pos.getDrawStyle());
	            
	            // ユーザチェックの有無表示
	            int userCheckedIcon = (pos.getUserChecked()) ? R.drawable.btn_checked : R.drawable.btn_notchecked;

	            // TODO: アイテム選択時の情報エリアは(ArrayItem側には)用意しているが未使用。
	            SymbolListArrayItem listItem = new SymbolListArrayItem(userCheckedIcon, pos.getLabel(), pos.getDetail(), "", objectStyleIcon);

	            listItems.add(listItem);
	        }
	    }
        catch (Exception ex)
	    {
	        // 例外発生...ログを吐く
	    	Log.v(TAG, "ExtensionActivityListener::onLoadingProcess() : " + ex.getMessage());
            ex.printStackTrace();
	    }	
	}

    /**
     *    ファイルのロード結果を受け取る
     * 
     */
    public void onLoadedResult(boolean isError, String detail)
    {
		Log.v(TAG, "ExtensionActivityListener::onLoadedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

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
    public void onExportedResult(Uri documentUri, String detail)
    {
        Log.v(TAG, "ExtensionActivityListener::onExportedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

        // エクスポートしたことを伝達する
        String outputMessage = parent.getString(R.string.export_csv) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();

        if (isShareExportedData)
        {
            // エクスポートしたファイルを共有する
            shareContent(documentUri);
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
            listView.setOnItemClickListener((parentView, view, position, id) -> {
                ListView listView1 = (ListView) parentView;
                SymbolListArrayItem item = (SymbolListArrayItem) listView1.getItemAtPosition(position);

                /// リストが選択されたときの処理...データを開く
                showDetailData(item.getTextResource1st(), item.getTextResource2nd(), item.getTextResource3rd());
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
     */
    private void shareContent(Uri documentUri)
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
            SharedIntentInvoker.shareContent(parent, MENU_ID_SHARE, title, message, documentUri, "text/plain");
        }
        catch (Exception ex)
        {
            Log.v(TAG, "shareContent (fileName : " + objectHolder.getDataTitle() + ")");
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
        Log.v(TAG, "ExtensionActivityListener::selectedFileName() : " + fileName);
        MeMoMaFileImportCsvProcess asyncTask = new MeMoMaFileImportCsvProcess(parent, this, fileName);
        asyncTask.execute(objectHolder);
    }

    /**
     *    インポート結果の受信
     *
     */
    public void onImportedResult(String detail)
    {
		Log.v(TAG, "ExtensionActivityListener::onImportedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

		// インポートしたことを伝達する
		String outputMessage = parent.getString(R.string.import_csv) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();

        // 一覧のリストを作りなおす
        onLoadingProcess();
        updateObjectList();
    }

    private void onImportedResultXml(String detail)
    {
        String title = objectHolder.getDataTitle();
        Log.v(TAG, "ExtensionActivityListener::onImportedResultXml() '"  + title + "' : " + detail);

        // インポート時の注意事項ダイアログを表示する
        parent.runOnUiThread(() -> {
            //  ダイアログで情報を表示する。
            new AlertDialog.Builder(parent)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(parent.getString(R.string.import_xml) + " (" + objectHolder.getDataTitle() + ")")
                    .setMessage(parent.getString(R.string.import_xml_information))
                    .setPositiveButton(parent.getString(R.string.confirmYes), null)
                    .show();

            // インポートしたことを伝達する
            String outputMessage = parent.getString(R.string.import_xml) + " " + objectHolder.getDataTitle() + " " + detail;
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
        });

        // タイトルを更新
        setPreferenceString(title);

        // 一覧のリストを作りなおす
        onLoadingProcess();
        updateObjectList();
    }
    private void onExportedResultXml(String detail)
    {
        Log.v(TAG, "ExtensionActivityListener::onExportedResultXml() '"  + objectHolder.getDataTitle() +"' : " + detail);

        // エクスポートしたことを伝達する
        String outputMessage = parent.getString(R.string.export_xml) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
    }

}
