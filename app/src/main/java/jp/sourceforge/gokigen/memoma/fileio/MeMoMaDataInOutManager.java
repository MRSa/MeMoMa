package jp.sourceforge.gokigen.memoma.fileio;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import jp.sourceforge.gokigen.memoma.GokigenSurfaceView;
import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.MeMoMaListener;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.SharedIntentInvoker;
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaDataFileHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;

public class MeMoMaDataInOutManager implements MeMoMaFileSavingProcess.ISavingStatusHolder, MeMoMaFileSavingProcess.IResultReceiver, MeMoMaFileLoadingProcess.IResultReceiver,  ActionBar.OnNavigationListener, ObjectLayoutCaptureExporter.ICaptureLayoutExporter
{
	private Activity parent = null;
	private MeMoMaObjectHolder objectHolder = null;
	private ExternalStorageFileUtility fileUtility = null;
    private MeMoMaDataFileHolder dataFileHolder = null;
	
	private boolean isSaving = false;	
	private boolean isShareExportedData = false;
	
	/**
	 *    コンストラクタ
	 * 
	 */
	public MeMoMaDataInOutManager(Activity activity)
	{
	    parent = activity;
        fileUtility = new ExternalStorageFileUtility(Main.APP_BASEDIR);
	}

	/**
	 *
	 *
	 */
	public void prepare(MeMoMaObjectHolder objectHolder, ActionBar bar, String fileName)
	{
        this.objectHolder = objectHolder;
        //this.lineHolder = lineHolder;
        
    	// データファイルフォルダを更新する
        dataFileHolder = new MeMoMaDataFileHolder(parent, android.R.layout.simple_spinner_dropdown_item, fileUtility, ".xml");
        int index = dataFileHolder.updateFileList(fileName, null);

        try
		{
			// アクションバーを設定する
			prepareActionBar(bar);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

        // タイトルの設定を変更する
        if ((bar != null)&&(index >= 0))
        {
            bar.setSelectedNavigationItem(index);  // 実験...
        }
	}

	/**
	 *   データファイル一覧を更新し、アクションバーに反映させる
	 *
	 */
	public void updateFileList(String titleName, ActionBar bar)
	{
		if (dataFileHolder != null)
		{
			// データファイル一覧を更新する
            int index = dataFileHolder.updateFileList(titleName, null);

            // タイトルをオブジェクトフォルダに記憶させる
    		objectHolder.setDataTitle(titleName);

    		// タイトルの設定を変更する
            if ((bar != null)&&(index >= 0))
            {
                bar.setSelectedNavigationItem(index);  // 実験...
            }
		}
	}

    /**
     *   データの保存を行う (同名のファイルが存在していた場合、 *.BAKにリネーム（上書き）してから保存する)
     *   
     *   
     *   @param forceOverwrite  trueの時は、ファイル名が確定していたときは（確認せずに）上書き保存を自動で行う。
     *   
     */
	public void saveFile(String dataTitle, boolean forceOverwrite)
	{
		if (objectHolder == null)
		{
			Log.e(Main.APP_IDENTIFIER, "ERR>MeMoMaDataInOutManager::saveFile() : "  + dataTitle);
			return;
		}

		// タイトルをオブジェクトフォルダに記憶させる
		objectHolder.setDataTitle(dataTitle);
		Log.v(Main.APP_IDENTIFIER, "MeMoMaDataInOutManager::saveFile() : "  + dataTitle);

		// 同期型でファイルを保存する。。。
		String message = saveFileSynchronous();
		onSavedResult(message);
	}

	/**
	 *    データファイルのフルパスを応答する
	 * 
	 * @param dataTitle
	 * @return
	 */
	public String getDataFileFullPath(String dataTitle, String extension)
	{
		return (fileUtility.getGokigenDirectory() + "/" + dataTitle + extension);
	}
	
	/**  保存中状態を設定する **/
    public void setSavingStatus(boolean isSaving)
    {
    	this.isSaving = isSaving;
    }
    
    /** 保存中状態を取得する **/
    public boolean getSavingStatus()
    {
    	return (isSaving);
    }

	/**
	 *    保存終了時の処理
	 */
    public  void onSavedResult(String detail)
    {
        // 保存したことを伝達する
		String outputMessage = parent.getString(R.string.save_data) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();    	

		// ファイルリスト更新 ... (ここでやっちゃあ、AsyncTaskにしている意味ないなあ...)
        dataFileHolder.updateFileList(objectHolder.getDataTitle(), null);
    }

    /**
	 *    読み込み終了時の処理
	 */
    public  void onLoadedResult(String detail)
    {
        // 読み込みしたことを伝達する
		String outputMessage = parent.getString(R.string.load_data) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();

    	// 画面を再描画する
    	final GokigenSurfaceView surfaceview = (GokigenSurfaceView) parent.findViewById(R.id.GraphicView);
    	surfaceview.doDraw();
    }

    /**
     *    ファイルをロードする途中のバックグラウンド処理...
     * 
     */
	public void onLoadingProcess()
	{
        // 何もしない...
	}

    /**
     *    ファイルからデータを読み込む。
     * 
     * @param dataTitle
     */
    public void loadFile(String dataTitle)
    {
        loadFileWithName(dataTitle);
    }
    
    
    /**
     *   ファイルからのデータ読み込み処理
     * 
     * @param dataTitle
     */
	private void loadFileWithName(String dataTitle)
	{
        if (objectHolder == null)
		{
			Log.e(Main.APP_IDENTIFIER, "ERR>MeMoMaDataInOutManager::loadFile() : "  + dataTitle);
			return;
		}

		// タイトルをオブジェクトフォルダに記憶させる
		objectHolder.setDataTitle(dataTitle);
		Log.v(Main.APP_IDENTIFIER, "MeMoMaDataInOutManager::loadFile() : "  + dataTitle);

		// AsyncTaskを使ってデータを読み込む
		MeMoMaFileLoadingProcess asyncTask = new MeMoMaFileLoadingProcess(parent, fileUtility, this);
        asyncTask.execute(objectHolder);
	}

	/**
	 *    アクションバーを更新する...
	 * 
	 * @param bar
	 */
	private void prepareActionBar(ActionBar bar)
	{
		try {
			bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);  // リストを入れる
			bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);   // タイトルの表示をマスクする
			bar.setListNavigationCallbacks(dataFileHolder, this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 *    ファイルを保存する...同期型で。
	 * 
	 * @return
	 */
	private String saveFileSynchronous()
	{
		// 同期型でファイルを保存する。。。
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
    	String backgroundUri = preferences.getString("backgroundUri","");
    	String userCheckboxString = preferences.getString("userCheckboxString","");
    	MeMoMaFileSavingEngine saveEngine = new MeMoMaFileSavingEngine(fileUtility, backgroundUri, userCheckboxString);
    	String message = saveEngine.saveObjects(objectHolder);
        return (message);		
	}
	
	
	/**
	 * 
	 * 
	 */
	public boolean onNavigationItemSelected(int itemPosition, long itemId)
	{
		String data = dataFileHolder.getItem(itemPosition);
		Log.v(Main.APP_IDENTIFIER, "onNavigationItemSelected(" + itemPosition + "," + itemId + ") : " + data);

		// 同期型で現在のファイルを保存する。。。
		String message = saveFileSynchronous();
		if (message.length() != 0)
		{
            onSavedResult(message);
		}
		
    	// 選択したファイル名をタイトルに反映し、またPreferenceにも記憶する
        parent.setTitle(data);
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("MeMoMaInfo", data);
        editor.commit();

		// 選択したアイテムをロードする！
        loadFileWithName(data);

		 return (true);
	}

	/**
	 *    スクリーンキャプチャを実施する
	 * 
	 */
	public void doScreenCapture(String title, MeMoMaObjectHolder holder, MeMoMaCanvasDrawer drawer, boolean isShare)
	{
		isShareExportedData = isShare;
		
    	// AsyncTaskを使ってデータをエクスポートする
		ObjectLayoutCaptureExporter asyncTask = new ObjectLayoutCaptureExporter(parent, fileUtility, holder, drawer, this);
        asyncTask.execute(title);
	}
	
    /**
     *    ファイルのエクスポート結果を受け取る
     * 
     */
	public void onCaptureLayoutExportedResult(String exportedFileName, String detail)
    {
		Log.v(Main.APP_IDENTIFIER, "MeMoMaDataInOutManager::onCaptureExportedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);

		// エクスポートしたことを伝達する
		String outputMessage = parent.getString(R.string.capture_data) + " " + objectHolder.getDataTitle() + " " + detail;
        Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
        
        if (isShareExportedData == true)
        {
        	// エクスポートしたファイルを共有する
        	shareContent(exportedFileName);
        }
    	isShareExportedData = false;
    }

    /**
     *    エクスポートしたファイルを共有する
     * 
     * @param fileName
     */
    private void shareContent(String fileName)
    {
    	String message = "";
        try
        {
        	// 現在の時刻を取得する
            Calendar calendar = Calendar.getInstance();
    		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String date =  outFormat.format(calendar.getTime());

            // メールタイトル
            String title = parent.getString(R.string.app_name) + " | "+ objectHolder.getDataTitle() + " | " + date;

            // メールの本文を構築する
            message = message + "Name : " + objectHolder.getDataTitle() + "\n";
            message = message + "exported : " + date + "\n";
            message = message + "number of objects : " + objectHolder.getCount() + "\n";

            // Share Intentを発行する。
            SharedIntentInvoker.shareContent(parent, MeMoMaListener.MENU_ID_SHARE, title, message,  fileName, "image/png");
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }

}
