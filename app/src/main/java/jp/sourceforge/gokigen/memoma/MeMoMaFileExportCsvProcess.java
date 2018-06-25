package jp.sourceforge.gokigen.memoma;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

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
public class MeMoMaFileExportCsvProcess extends AsyncTask<MeMoMaObjectHolder, Integer, String>
{	
	private IResultReceiver receiver = null;
	private ExternalStorageFileUtility fileUtility = null;	
	private String exportedFileName = null;

	ProgressDialog savingDialog = null;
	
	/**
	 *   コンストラクタ
	 */
    public MeMoMaFileExportCsvProcess(Context context, ExternalStorageFileUtility utility,  IResultReceiver resultReceiver)
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
    }
    
    /**
     *    データを(CSV形式で)保管する。
     * 
     * @param fileName
     * @param objectHolder
     * @return
     */
    private String exportToCsvFile(String fileName, MeMoMaObjectHolder objectHolder)
    {
    	String resultMessage = "";
        try
        {
        	// エクスポートするファイル名を決定する
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            exportedFileName = fileName + "_" + outFormat.format(calendar.getTime()) + ".csv";
        	FileWriter writer = new FileWriter(new File(exportedFileName));    	
            
        	//  データのタイトルを出力
        	String str = "";
        	str = "; label,detail,userChecked,shape,style,centerX,centerY,width,height,;!<_$ (';!<_$' is a record Separator)\r\n";
            writer.write(str);
        	
        	// オブジェクトの出力 （保持しているものをすべて表示する）
        	Enumeration<Integer> keys = objectHolder.getObjectKeys();
            while (keys.hasMoreElements())
            {
                Integer key = keys.nextElement();
                MeMoMaObjectHolder.PositionObject pos = objectHolder.getPosition(key);

                // TODO:  絞り込み条件がある場合には、その条件に従ってしぼり込む必要あり。

                str = "";
                str = str + "\"" + pos.label + "\"";
                str = str + ",\"" + pos.detail + "\"";
                if (pos.userChecked == true)
                {
                	str = str + ",True";
                }
                else
                {
                	str = str + ",False";
                }
                str = str + "," + pos.drawStyle;   // オブジェクトの形状
                str = str + "," + pos.paintStyle;   // オブジェクトの塗りつぶし状態
                str = str + "," + (Math.round(pos.rect.centerX() * 100.0f) / 100.0f);
                str = str + "," + (Math.round(pos.rect.centerY() * 100.0f) / 100.0f);
                str = str + "," + (Math.round(pos.rect.width() * 100.0f) / 100.0f);
                str = str + "," + (Math.round(pos.rect.height() * 100.0f) / 100.0f);
                str = str + ",;!<_$\r\n";
                writer.write(str);
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
        	resultMessage = " ERR>" + e.toString();
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
    protected String doInBackground(MeMoMaObjectHolder... datas)
    {    	
        // ファイル名の設定 ... (拡張子なし)
    	String fileName = fileUtility.getGokigenDirectory() + "/exported/" + datas[0].getDataTitle();

    	// データを保管する
        String result = exportToCsvFile(fileName, datas[0]);

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
            	receiver.onExportedResult(exportedFileName, result);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaFileExportCsvProcess::onPostExecute() : " + ex.toString());
    	}
    	// プログレスダイアログを消す
    	savingDialog.dismiss();

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
        public abstract void onExportedResult(String exportedFileName, String detail);
    }
}
