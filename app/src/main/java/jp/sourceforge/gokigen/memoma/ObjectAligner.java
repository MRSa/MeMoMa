package jp.sourceforge.gokigen.memoma;


import java.util.Enumeration;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;

/**
 *  オブジェクトの位置を整列するクラス (非同期処理を実行)
 *  
 *  AsyncTask
 *    MeMoMaObjectHolder : 実行時に渡すクラス(Param)
 *    Integer    : 途中経過を伝えるクラス(Progress)
 *    String     : 処理結果を伝えるクラス(Result)
 *    
 * @author MRSa
 *
 */
public class ObjectAligner extends AsyncTask<MeMoMaObjectHolder, Integer, String>
{
	ProgressDialog executingDialog = null;
	IAlignCallback  receiver = null;
	/**
	 *   コンストラクタ
	 */
    public ObjectAligner(Context context, IAlignCallback client)
    {
        receiver = client;
    	
    	//  プログレスダイアログ（「保存中...」）を表示する。
    	executingDialog = new ProgressDialog(context);
    	executingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	executingDialog.setMessage(context.getString(R.string.dataAligning));
    	executingDialog.setIndeterminate(true);
    	executingDialog.setCancelable(false);
    	executingDialog.show();
    }
	
    /**
     *  非同期処理実施前の前処理
     * 
     */
    @Override
    protected void onPreExecute()
    {
    	
    }

    /**
     *  非同期処理
     *  （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     * 
     */
    @Override
    protected String doInBackground(MeMoMaObjectHolder... datas)
    {
    	MeMoMaObjectHolder objectHolder = datas[0];

    	// オブジェクトの出力 （保持しているものはすべて表示する）
    	Enumeration<Integer> keys = objectHolder.getObjectKeys();
        while (keys.hasMoreElements())
        {
            Integer key = keys.nextElement();
            MeMoMaObjectHolder.PositionObject pos = objectHolder.getPosition(key);
            
            float newLeft = (float) Math.floor((pos.rect.left + 15.0f)/ 30.0) * 30.0f;
            float newTop = (float) Math.floor((pos.rect.top + 15.0f)/ 30.0) * 30.0f;
            pos.rect.offsetTo(newLeft, newTop);
        }
        System.gc();
		
		return ("");
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
    			// 並べ変えたことを通知する
    			receiver.objectAligned();
    		}
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "ObjectAligner::onPostExecute() : " + ex.toString());
    	}

    	// プログレスダイアログを消す
    	executingDialog.dismiss();
        return;
    }     
    
    /**
     *    並べ変えたことを通知する
     * 
     * @author MRSa
     *
     */
    public interface IAlignCallback
    {
        public abstract void objectAligned();
    }
}
