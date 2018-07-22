package jp.sourceforge.gokigen.memoma.operations;


import java.util.Enumeration;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.PositionObject;

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
	private ProgressDialog executingDialog;
	private IAlignCallback  receiver;
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
            PositionObject pos = objectHolder.getPosition(key);
            RectF posRect = pos.getRect();
            float newLeft = (float) Math.floor((posRect.left + 15.0f)/ 30.0) * 30.0f;
            float newTop = (float) Math.floor((posRect.top + 15.0f)/ 30.0) * 30.0f;
            pos.setRectOffsetTo(newLeft, newTop);
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
    }     
    
    /**
     *    並べ変えたことを通知する
     * 
     * @author MRSa
     *
     */
    public interface IAlignCallback
    {
        void objectAligned();
    }
}
