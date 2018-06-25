package jp.sourceforge.gokigen.memoma;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;

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
public class ObjectLayoutCaptureExporter extends AsyncTask<String, Integer, String>
{
	private static final int OUTPUT_MARGIN = 8;
	private static final int OUTPUT_MARGIN_TOP = 50;
	
	private static final int MINIMUM_WIDTH = 800;
	private static final int MINIMUM_HEIGHT = 600;
	
	private Activity parent = null;
	private ICaptureLayoutExporter receiver = null;
	private ExternalStorageFileUtility fileUtility = null;	
	private String exportedFileName = null;	
	private MeMoMaObjectHolder objectHolder = null;
	private MeMoMaCanvasDrawer canvasDrawer = null;
	private ProgressDialog savingDialog = null;
	private float offsetX = 0.0f;
	private float offsetY = 0.0f;

	/**
	 *   コンストラクタ
	 */
    public ObjectLayoutCaptureExporter(Activity context, ExternalStorageFileUtility utility,  MeMoMaObjectHolder holder, MeMoMaCanvasDrawer drawer, ICaptureLayoutExporter resultReceiver)
    {
    	receiver = resultReceiver;
    	fileUtility = utility;
    	objectHolder = holder;
    	canvasDrawer = drawer;
    	parent = context;

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
        // なにもしない。
    }
    
    /**
     *    ビットマップデータを(PNG形式で)保管する。
     * 
     * @param fileName
     * @param objectHolder
     * @return
     */
    private String exportToFile(String fileName, Bitmap targetBitmap)
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
     *    キャンバスの大きさがどれくらい必要か、チェックする。
     * 
     * @return
     */
    private Rect checkCanvasSize()
    {
        Rect canvasSize = new Rect();

        // オブジェクトの配置位置を探る。
    	Enumeration<Integer> keys = objectHolder.getObjectKeys();
        while (keys.hasMoreElements())
        {
            Integer key = keys.nextElement();
            MeMoMaObjectHolder.PositionObject pos = objectHolder.getPosition(key);
            if (canvasSize.left > pos.rect.left)
            {
            	canvasSize.left = (int) pos.rect.left;
            }
            if (canvasSize.right < pos.rect.right)
            {
            	canvasSize.right = (int) pos.rect.right;
            }
            if (canvasSize.top > pos.rect.top)
            {
            	canvasSize.top = (int) pos.rect.top;
            }
            if (canvasSize.bottom < pos.rect.bottom)
            {
            	canvasSize.bottom = (int) pos.rect.bottom;
            }
        }
        
        // 描画領域にちょっと余裕を持たせる
        canvasSize.left = canvasSize.left - OUTPUT_MARGIN;
        canvasSize.right = canvasSize.right + OUTPUT_MARGIN;
        canvasSize.top = canvasSize.top - OUTPUT_MARGIN_TOP;
        canvasSize.bottom = canvasSize.bottom + OUTPUT_MARGIN;
        canvasSize.sort();

        // 現在の画面サイズを取得
        Display display = parent.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        if (width < MINIMUM_WIDTH)
        {
        	width = MINIMUM_WIDTH;
        }
        if (height < MINIMUM_HEIGHT)
        {
        	height = MINIMUM_HEIGHT;
        }        

        // 出力の最小サイズを(表示画面サイズに)設定
        if (canvasSize.width() < width)
        {
        	canvasSize.right = canvasSize.left + width;
        }
        if (canvasSize.height() < height)
        {
        	canvasSize.bottom = canvasSize.top + height;
        }
        
        
        // 画像位置（キャンバス位置）の調整。。。
        offsetX = 0.0f - canvasSize.left - (OUTPUT_MARGIN);
        offsetY = 0.0f - canvasSize.top - (OUTPUT_MARGIN);

        // 出力する画像データのサイズを表示する
        Log.v(Main.APP_IDENTIFIER, "ObjectLayoutCaptureExporter::checkCanvasSize() w:" + canvasSize.width() + " , h:" + canvasSize.height() + "  offset :(" + offsetX + "," + offsetY + ")");
        return (canvasSize);
    }    

    /**
     *  非同期処理
     *  （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     * 
     */
    @Override
    protected String doInBackground(String... datas)
    {
    	Rect canvasSize = checkCanvasSize();
    	Bitmap targetBitmap = Bitmap.createBitmap(canvasSize.width(), canvasSize.height(), Bitmap.Config.RGB_565);
    	Canvas targetCanvas = new Canvas(targetBitmap);
    	
    	// オブジェクトをビットマップの中に書き込む
    	canvasDrawer.drawOnBitmapCanvas(targetCanvas, offsetX, offsetY);

    	// ファイル名の設定 ... (拡張子なし)
    	String fileName = fileUtility.getGokigenDirectory() + "/exported/" + datas[0];

    	// データを保管する
        String result = exportToFile(fileName, targetBitmap);

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
            	receiver.onCaptureLayoutExportedResult(exportedFileName, result);
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
    public interface ICaptureLayoutExporter
    {
        /**  保存結果の報告 **/
        public abstract void onCaptureLayoutExportedResult(String exportedFileName, String detail);
    }
}
