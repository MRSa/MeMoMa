package jp.sourceforge.gokigen.memoma.fileio;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;

/**
 *  データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 *  
 *  AsyncTask
 *    MeMoMaObjectHolder : 実行時に渡すクラス(Param)
 *    Integer    : 途中経過を伝えるクラス(Progress)
 *    String     : 処理結果を伝えるクラス(Result)
 *    
 * @author MRSa
 *
 */
public class MeMoMaFileSavingProcess extends AsyncTask<MeMoMaObjectHolder, Integer, String>
{	
	private IResultReceiver receiver = null;
	private ExternalStorageFileUtility fileUtility = null;
	private ISavingStatusHolder statusHolder = null;
	
	private String backgroundUri = null;
	private String userCheckboxString = null;
	private ProgressDialog savingDialog = null;
	
	/**
	 *   コンストラクタ
	 */
    public MeMoMaFileSavingProcess(Context context, ISavingStatusHolder holder, ExternalStorageFileUtility utility,  IResultReceiver resultReceiver)
    {
    	receiver = resultReceiver;
    	fileUtility = utility;
    	statusHolder = holder;

        //  プログレスダイアログ（「保存中...」）を表示する。
    	savingDialog = new ProgressDialog(context);
    	savingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	savingDialog.setMessage(context.getString(R.string.dataSaving));
    	savingDialog.setIndeterminate(true);
    	savingDialog.setCancelable(false);
    	savingDialog.show();

    	//  設定読み出し用...あらかじめ、UIスレッドで読みだしておく。
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
    	backgroundUri = preferences.getString("backgroundUri","");
    	userCheckboxString = preferences.getString("userCheckboxString","");
    	    	
    	// 未保管状態にリセットする
    	statusHolder.setSavingStatus(false);
    }
	
    /**
     *  非同期処理実施前の前処理
     * 
     */
    @Override
    protected void onPreExecute()
    {
    	// 未保管状態にリセットする
    	statusHolder.setSavingStatus(false);
    }

    /**
     *  非同期処理
     *  （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     * 
     */
    @Override
    protected String doInBackground(MeMoMaObjectHolder... datas)
    {
    	// 保管中状態を設定する
    	statusHolder.setSavingStatus(true);

    	// データの保管メイン
    	MeMoMaFileSavingEngine savingEngine = new MeMoMaFileSavingEngine(fileUtility, backgroundUri, userCheckboxString);
    	String result = savingEngine.saveObjects(datas[0]);

        System.gc();
		
    	// 未保管状態にリセットする
    	statusHolder.setSavingStatus(false);

		return (result);
    }

    /**
     *  非同期処理の進捗状況の更新
     * 
     */
	@Override
	protected void onProgressUpdate(Integer... values)
	{
        // 今回は何もしない
	}

    /**
     *  非同期処理の後処理
     *  (結果を応答する)
     */
    @Override
    protected void onPostExecute(String result)
    {
    	try
    	{
            if (receiver != null)
            {
            	receiver.onSavedResult(result);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaFileSavingProcess::onPostExecute() : " + ex.toString());
    	}
    	// プログレスダイアログを消す
    	savingDialog.dismiss();

    	// 未保管状態にセットする
    	statusHolder.setSavingStatus(false);
        return;
    }     
    
    /**
     *    結果報告用のインタフェース（積極的に使う予定はないけど...）
     *    
     * @author MRSa
     *
     */
    public interface IResultReceiver
    {
        /**  保存結果の報告 **/
        public abstract void onSavedResult(String detail);
    }

    /**
     *     ファイル保存実施状態を記憶するインタフェースクラス
     *     
     * @author MRSa
     *
     */
    public interface ISavingStatusHolder
    {
    	/**  保存中状態を設定する **/
        public abstract void setSavingStatus(boolean isSaving);
        
        /** 保存中状態を取得する **/
        public abstract boolean getSavingStatus();
    }

}
