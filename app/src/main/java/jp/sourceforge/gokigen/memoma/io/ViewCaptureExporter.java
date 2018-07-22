package jp.sourceforge.gokigen.memoma.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;

/**
 *  データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 *  Viewの情報を画像形式（png形式）で保存する。
 *  どのViewを保存するのかは、ICaptureExporter.getCaptureTargetView()クラスを使って教えてもらう。
 *  
 *  AsyncTask
 *    String       : 実行時に渡すクラス(Param)           : ファイル名をもらう
 *    Integer    : 途中経過を伝えるクラス(Progress)   : 今回は使っていない
 *    String      : 処理結果を伝えるクラス(Result)      : 結果を応答する。
 *    
 * @author MRSa
 *
 */
public class ViewCaptureExporter extends AsyncTask<String, Integer, String>
{
	private ICaptureExporter receiver = null;
	private ExternalStorageFileUtility fileUtility = null;
	private String exportedFileName = null;

	private ProgressDialog savingDialog = null;
	
	private Bitmap targetBitmap = null;

	/**
	 *   コンストラクタ
	 */
    public ViewCaptureExporter(Context context, ExternalStorageFileUtility utility,  ICaptureExporter resultReceiver)
    {
    	receiver = resultReceiver;
    	fileUtility = utility;

        //  プログレスダイアログ（「保存中...」）を表示する。
    	savingDialog = new ProgressDialog(context);
    	savingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	savingDialog.setMessage(context.getString(R.string.dataSaving));
    	savingDialog.setIndeterminate(true);
    	savingDialog.setCancelable(false);
    	savingDialog.show();

    	/** ファイルをバックアップするディレクトリを作成する **/
    	File dir = new File(fileUtility.getGokigenDirectory() + "/exported");
    	dir.mkdir();
    }
	
    /**
     *  非同期処理実施前の前処理
     * 
     */
    @Override
    protected void onPreExecute()
    {
    	try
    	{
        	targetBitmap = null;
            if (receiver != null)
            {
            	// 画面のキャプチャを実施する
            	View targetView = receiver.getCaptureTargetView();
            	targetView.setDrawingCacheEnabled(false);
            	targetView.setDrawingCacheEnabled(true);
            	targetBitmap = Bitmap.createBitmap(targetView.getDrawingCache());
            	targetView.setDrawingCacheEnabled(false);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "ViewCaptureExporter::onPreExecute() : " + ex.toString());
    	}
    }
    
    /**
     *    ビットマップデータを(PNG形式で)保管する。
     *
	 *
     */
    private String exportToFile(String fileName)
    {
    	String resultMessage = "";
        try
        {
        	if (targetBitmap == null)
        	{
        		// ビットマップが取れないため、ここで折り返す。
        		return ("SCREEN DATA GET FAILURE...");
        	}
        	
        	// エクスポートするファイル名を決定する
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            exportedFileName = fileName + "_" + outFormat.format(calendar.getTime()) + ".png";

            // PNG形式でファイル出力を行う。
            OutputStream out = new FileOutputStream(exportedFileName);
            targetBitmap.compress(CompressFormat.PNG, 100, out);
            out.flush();
            out.close();            
        }
        catch (Exception e)
        {
        	resultMessage = " ERR(png)>" + e.toString();
            Log.v(Main.APP_IDENTIFIER, resultMessage);
            e.printStackTrace();
        } 
        return (resultMessage);
    }

    /**
     *  非同期処理
     *  （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     * 
     */
    @Override
    protected String doInBackground(String... datas)
    {
        // ファイル名の設定 ... (拡張子なし)
    	String fileName = fileUtility.getGokigenDirectory() + "/exported/" + datas[0];

    	// データを保管する
        String result = exportToFile(fileName);

        System.gc();

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
            	receiver.onCaptureExportedResult(exportedFileName, result);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "ViewCaptureExporter::onPostExecute() : " + ex.toString());
    	}
    	// プログレスダイアログを消す
    	if (savingDialog != null)
    	{
            savingDialog.dismiss();
    	}
    	return;
    }     
 
    /**
     *    結果報告用のインタフェース
     *    
     * @author MRSa
     *
     */
    public interface ICaptureExporter
    {
    	/** データをキャプチャする Viewを取得する **/
    	public abstract View getCaptureTargetView();
    	
        /**  保存結果の報告 **/
        public abstract void onCaptureExportedResult(String exportedFileName, String detail);
    }
}
