package jp.sourceforge.gokigen.memoma.io;

import java.io.BufferedReader;
import java.io.FileReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.PositionObject;

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
public class MeMoMaFileImportCsvProcess extends AsyncTask<MeMoMaObjectHolder, Integer, String> implements MeMoMaFileSavingProcess.ISavingStatusHolder, MeMoMaFileSavingProcess.IResultReceiver
{	
	private Context parent = null;
	private IResultReceiver receiver = null;
	private ExternalStorageFileUtility fileUtility = null;
	private String targetFileName = null;
    private String fileSavedResult = "";
	private ProgressDialog importingDialog = null;

	private String backgroundUri = null;
	private String userCheckboxString = null;
	
	/**
	 *   コンストラクタ
	 */
    public MeMoMaFileImportCsvProcess(Context context, ExternalStorageFileUtility utility,  IResultReceiver resultReceiver, String fileName)
    {
    	parent = context;
    	receiver = resultReceiver;
    	fileUtility = utility;
    	targetFileName = fileName;

        //  プログレスダイアログ（「データインポート中...」）を表示する。
    	importingDialog = new ProgressDialog(context);
    	importingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	importingDialog.setMessage(context.getString(R.string.dataImporting));
    	importingDialog.setIndeterminate(true);
    	importingDialog.setCancelable(false);
    	importingDialog.show();

    	//  設定読み出し用...あらかじめ、UIスレッドで読みだしておく。   	
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(parent);
    	backgroundUri = preferences.getString("backgroundUri","");
    	userCheckboxString = preferences.getString("userCheckboxString","");
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
     *    １レコード分のデータを読み込む。 
     * 
     * @param buf
     * @return
     */
    private String readRecord(BufferedReader buf )
    {
    	String oneRecord = null;
    	try
    	{
    		String oneLine = buf.readLine();
            while (oneLine != null)
            {
            	oneRecord = (oneRecord == null) ? oneLine : oneRecord + oneLine;
            	if (oneRecord.indexOf(",;!<_$") > 0)
            	{
            		// レコード末尾が見つかったので break する。
            		break;
            	}
            	// 次の行を読みだす。
            	oneLine = buf.readLine();
            }
    	}
    	catch (Exception ex)
    	{
            //
    		Log.v(Main.APP_IDENTIFIER, "CSV:readRecord() ex : " + ex.toString());
    		oneRecord = null;
    	}
    	return (oneRecord);
    }

    /**
     *   1レコード分のデータを区切る
     * 
     * 
     * @param dataLine
     */
    private void parseRecord(String dataLine,  MeMoMaObjectHolder objectHolder)
    {
        int detailIndex = 0;
        int userCheckIndexTrue = 0;
        int userCheckIndexFalse = 0;
        int nextIndex = 0;
        String label = "";
        String detail = "";
        boolean userChecked = false;
        try
        {
            detailIndex = dataLine.indexOf("\",\"");
            if (detailIndex < 0)
            {
                Log.v(Main.APP_IDENTIFIER, "parseRecord() : label wrong : " + dataLine);
            	return;
            }
            label = dataLine.substring(1, detailIndex);
            userCheckIndexTrue = dataLine.indexOf("\",True,", detailIndex);
            userCheckIndexFalse = dataLine.indexOf("\",False,", detailIndex);
            if (userCheckIndexFalse > detailIndex)
            {
                //
                detail = dataLine.substring(detailIndex + 3, userCheckIndexFalse);
            	userChecked = false;
            	nextIndex = userCheckIndexFalse + 8; // 8は、 ",False, を足した数
            }
            else if (userCheckIndexTrue > detailIndex)
            {
                //
                detail = dataLine.substring(detailIndex + 3, userCheckIndexTrue);
            	userChecked = true;
            	nextIndex = userCheckIndexTrue + 7; // 7は、 ",True,  を足した数
            }
            else // if ((userCheckIndexTrue <= detailIndex)&&(userCheckIndexFalse <= detailIndex))
            {
                Log.v(Main.APP_IDENTIFIER, "parseRecord() : detail wrong : " + dataLine);
            	return;            	
            }
            
            //  残りのデータを切り出す。
            String[] datas = (dataLine.substring(nextIndex)).split(",");
            if (datas.length < 6)
            {
            	Log.v(Main.APP_IDENTIFIER, "parseRecord() : data size wrong : " + datas.length);
            	return;
            }
            int drawStyle = Integer.parseInt(datas[0]);
            String paintStyle = datas[1];
            float centerX = Float.parseFloat(datas[2]);
            float centerY = Float.parseFloat(datas[3]);
            float width = Float.parseFloat(datas[4]);
            float height = Float.parseFloat(datas[5]);

            float left = centerX - (width / 2.0f);
            float top = centerY - (height / 2.0f);

            // オブジェクトのデータを作成する
            PositionObject pos = objectHolder.createPosition(left, top, drawStyle);
            if (pos == null)
            {
                Log.v(Main.APP_IDENTIFIER, "parseRecord() : object create failure.");
            	return;            	
            }
            pos.setRectRight(left + width);
            pos.setRectBottom(top + height);
            pos.setLabel(label);
            pos.setDetail(detail);
            pos.setPaintStyle(paintStyle);
            pos.setUserChecked(userChecked);
            Log.v(Main.APP_IDENTIFIER, "OBJECT CREATED: " + label + "(" + left + "," + top + ") [" +drawStyle + "]");
        }
        catch (Exception ex)
        {
        	Log.v(Main.APP_IDENTIFIER, "parseRecord() " + ex.toString());
        }
    	
    }

    
    /**
     *    (CSV形式の)データを読み込んで格納する。
     * 
     * @param fileName
     * @param objectHolder
     * @return
     */
    private String importFromCsvFile(String fileName, MeMoMaObjectHolder objectHolder)
    {
    	String resultMessage = "";
        try
        {
            Log.v(Main.APP_IDENTIFIER, "CSV(import)>> " + fileName);        		
        	BufferedReader buf = new BufferedReader(new FileReader(fileName));
            String dataLine = readRecord(buf);
            while (dataLine != null)
            {
        		if (dataLine.startsWith(";") != true)
        		{
        			// データ行だった。ログに出力する！
                    parseRecord(dataLine, objectHolder);
        		}
                // 次のデータ行を読み出す
        		dataLine = readRecord(buf);
            }
        }
        catch (Exception e)
        {
        	resultMessage = " ERR(import)>" + e.toString();
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
    	String fileName = fileUtility.getGokigenDirectory() + "/exported/" + targetFileName;

    	// データを読み込む
        String result = importFromCsvFile(fileName, datas[0]);

        // データを保存する
    	MeMoMaFileSavingEngine savingEngine = new MeMoMaFileSavingEngine(fileUtility, backgroundUri, userCheckboxString);
    	String message = savingEngine.saveObjects(datas[0]);

        System.gc();

		return (result + " " + message);
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
            	receiver.onImportedResult(result + "  " + fileSavedResult);
            }
            fileSavedResult = "";
    	}
    	catch (Exception ex)
    	{
    		Log.v(Main.APP_IDENTIFIER, "MeMoMaFileImportCsvProcess::onPostExecute() : " + ex.toString());
    	}
    	// プログレスダイアログを消す
    	importingDialog.dismiss();

    	return;
    }
    
    public void onSavedResult(String detail)
    {
        fileSavedResult = detail;
    }

    public void setSavingStatus(boolean isSaving)
    {
    	
    }

    public boolean getSavingStatus()
    {
        return (false);
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
        public abstract void onImportedResult(String detail);
    }
}
