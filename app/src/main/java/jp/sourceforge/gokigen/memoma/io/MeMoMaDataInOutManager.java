package jp.sourceforge.gokigen.memoma.io;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.net.Uri;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import jp.sourceforge.gokigen.memoma.drawers.GokigenSurfaceView;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaDataFileHolder;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;

public class MeMoMaDataInOutManager implements MeMoMaFileSavingProcess.ISavingStatusHolder, MeMoMaFileSavingProcess.IResultReceiver, MeMoMaFileLoadingProcess.IResultReceiver,  ActionBar.OnNavigationListener, ObjectLayoutCaptureExporter.ICaptureLayoutExporter
{
	private final String TAG = toString();
	private final AppCompatActivity parent;
	private MeMoMaObjectHolder objectHolder = null;

    private MeMoMaDataFileHolder dataFileHolder = null;
	
	private boolean isSaving = false;	
	private boolean isShareExportedData = false;
	
	/**
	 *    コンストラクタ
	 * 
	 */
	public MeMoMaDataInOutManager(AppCompatActivity activity)
	{
	    parent = activity;
	}

	/**
	 *
	 *
	 */
	public void prepare(MeMoMaObjectHolder objectHolder, ActionBar bar, String fileName)
	{
        this.objectHolder = objectHolder;

    	// データファイルフォルダを更新する
        dataFileHolder = new MeMoMaDataFileHolder(parent, android.R.layout.simple_spinner_dropdown_item, ".xml");
        int index = dataFileHolder.updateFileList(fileName);

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
            int index = dataFileHolder.updateFileList(titleName);

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
     */
	public void saveFile(String dataTitle, boolean forceOverwrite)
	{
		if (objectHolder == null)
		{
			Log.e(TAG, "ERR>MeMoMaDataInOutManager::saveFile() : "  + dataTitle);
			return;
		}

		// タイトルをオブジェクトフォルダに記憶させる
		objectHolder.setDataTitle(dataTitle);
		Log.v(TAG, "MeMoMaDataInOutManager::saveFile() : "  + dataTitle);

		// 同期型でファイルを保存する。。。
		String message = saveFileSynchronous();
		onSavedResult((message.length() != 0), message);
	}

	/**
	 *    データファイルのフルパスを応答する
	 */
	public String getDataFileFullPath(String dataTitle, String extension)
	{
		return (parent.getFilesDir() + "/" + dataTitle + extension);
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
    public  void onSavedResult(boolean isError, String detail)
    {
        // 保存したことを伝達する
		String outputMessage = parent.getString(R.string.save_data) + " " + objectHolder.getDataTitle() + " " + detail;
		if (isError)
		{
			Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
		}
		Log.v(TAG, outputMessage);

		// ファイルリスト更新 ... (ここでやっちゃあ、AsyncTaskにしている意味ないなあ...)
        dataFileHolder.updateFileList(objectHolder.getDataTitle());
    }

    /**
	 *    読み込み終了時の処理
	 */
    public  void onLoadedResult(boolean isError, String detail)
    {
        // 読み込みしたことを伝達する
		String outputMessage = parent.getString(R.string.load_data) + " " + objectHolder.getDataTitle() + " " + detail;
		if (isError)
		{
			Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
		}
		Log.v(TAG, outputMessage);

    	// 画面を再描画する
    	final GokigenSurfaceView surfaceview = parent.findViewById(R.id.GraphicView);
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
     */
    public void loadFile(String dataTitle)
    {
        loadFileWithName(dataTitle);
    }
    
    
    /**
     *   ファイルからのデータ読み込み処理
     */
	private void loadFileWithName(String dataTitle)
	{
		if (objectHolder == null)
		{
			Log.e(TAG, "ERR>MeMoMaDataInOutManager::loadFile() : "  + dataTitle);
			return;
		}

		// タイトルをオブジェクトフォルダに記憶させる
		objectHolder.setDataTitle(dataTitle);
		Log.v(TAG, "MeMoMaDataInOutManager::loadFile() : "  + dataTitle);

		// AsyncTaskを使ってデータを読み込む
		MeMoMaFileLoadingProcess asyncTask = new MeMoMaFileLoadingProcess(parent, this);
        asyncTask.execute(objectHolder);
	}

	/**
	 *    アクションバーを更新する...
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
	 */
	private String saveFileSynchronous()
	{
		// 同期型でファイルを保存する。。。
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
    	String backgroundUri = preferences.getString("backgroundUri","");
    	String userCheckboxString = preferences.getString("userCheckboxString","");
    	MeMoMaFileSavingEngine saveEngine = new MeMoMaFileSavingEngine(parent, backgroundUri, userCheckboxString);
    	return (saveEngine.saveObjects(objectHolder));
	}

	/**
	 * 
	 * 
	 */
	public boolean onNavigationItemSelected(int itemPosition, long itemId)
	{
		String data = dataFileHolder.getItem(itemPosition);
		Log.v(TAG, "onNavigationItemSelected(" + itemPosition + "," + itemId + ") : " + data);

		// 同期型で現在のファイルを保存する。。。
		String message = saveFileSynchronous();
		onSavedResult((message.length() != 0), message);

		// 選択したファイル名をタイトルに反映し、またPreferenceにも記憶する
		parent.setTitle(data);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("MeMoMaInfo", data);
		editor.apply();

		// 選択したアイテムをロードする！
		loadFileWithName(data);

		return (true);
	}

	/**
	 *    スクリーンキャプチャを実施する
	 */
	public void doScreenCapture(String title, MeMoMaObjectHolder holder, MeMoMaCanvasDrawer drawer, boolean isShare)
	{
		isShareExportedData = isShare;
		
    	// AsyncTaskを使ってデータをエクスポートする
		ObjectLayoutCaptureExporter asyncTask = new ObjectLayoutCaptureExporter(parent, holder, drawer, this);
        asyncTask.execute(title);
	}
	
    /**
     *    ファイルのエクスポート結果を受け取る
     */
	public void onCaptureLayoutExportedResult(Uri exportedUri, String detail, int id)
	{
		Log.v(TAG, "MeMoMaDataInOutManager::onCaptureExportedResult() '"  + objectHolder.getDataTitle() +"' : " + detail);
		try
		{
			// エクスポートしたことを伝達する
			String outputMessage = parent.getString(R.string.capture_data) + " " + objectHolder.getDataTitle() + " " + detail;
			if ((exportedUri == null)&&(isShareExportedData))
			{
				// エクスポートはできない
				isShareExportedData = false;
				outputMessage = parent.getString(R.string.exported_picture_not_shared) + " : " + objectHolder.getDataTitle() + " " + detail;
			}
			Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
			if (isShareExportedData)
			{
				// ギャラリーに受信したファイルを登録し、エクスポートしたファイルを共有する
				shareContent(exportedUri, id);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		isShareExportedData = false;
	}

    /**
     *    エクスポートしたファイルを共有する
     */
    private void shareContent(Uri imageName, int id)
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
            SharedIntentInvoker.shareContent(parent, id, title, message,  imageName, "image/png");
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }

}
