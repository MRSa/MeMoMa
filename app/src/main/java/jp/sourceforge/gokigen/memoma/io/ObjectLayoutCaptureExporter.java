package jp.sourceforge.gokigen.memoma.io;

import static jp.sourceforge.gokigen.memoma.Main.APP_NAMESPACE;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;

import androidx.appcompat.app.AppCompatActivity;

import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.drawers.MeMoMaCanvasDrawer;
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder;
import jp.sourceforge.gokigen.memoma.holders.PositionObject;

/**
 *  データをファイルに保存するとき用 アクセスラッパ (非同期処理を実行)
 *  Viewの情報を画像形式（png形式）で保存する。
 *  どのViewを保存するのかは、ICaptureExporter.getCaptureTargetView()クラスを使って教えてもらう。
 *  AsyncTask
 *    String       : 実行時に渡すクラス(Param)           : ファイル名をもらう
 *    Integer    : 途中経過を伝えるクラス(Progress)   : 今回は使っていない
 *    String      : 処理結果を伝えるクラス(Result)      : 結果を応答する。
 */
public class ObjectLayoutCaptureExporter extends AsyncTask<String, Integer, String>
{
    private final String TAG = toString();
    private static final boolean dumpLog = false;

    private static final int OUTPUT_EXPORT_SHARE_ID = 1000;
	private static final int OUTPUT_MARGIN = 8;
	private static final int OUTPUT_MARGIN_TOP = 50;
	
	private static final int MINIMUM_WIDTH = 800;
	private static final int MINIMUM_HEIGHT = 600;

	private final ICaptureLayoutExporter receiver;

	private final MeMoMaObjectHolder objectHolder;
	private final MeMoMaCanvasDrawer canvasDrawer;
	private final ProgressDialog savingDialog;
	private float offsetX = 0.0f;
	private float offsetY = 0.0f;
	private int displayWidth;
	private int displayHeight;

    private final Context context;

    private Uri exportedUri = null;

	/**
	 *   コンストラクタ
	 */
	ObjectLayoutCaptureExporter(AppCompatActivity context, MeMoMaObjectHolder holder, MeMoMaCanvasDrawer drawer, ICaptureLayoutExporter resultReceiver)
    {
        this.context = context;
        receiver = resultReceiver;
        objectHolder = holder;
        canvasDrawer = drawer;

        // 現在の画面サイズを取得
        Display display = context.getWindowManager().getDefaultDisplay();
        displayWidth = display.getWidth();
        displayHeight = display.getHeight();

        //  プログレスダイアログ（「保存中...」）を表示する。
    	savingDialog = new ProgressDialog(context);
    	savingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	savingDialog.setMessage(context.getString(R.string.dataSaving));
    	savingDialog.setIndeterminate(true);
    	savingDialog.setCancelable(false);
    	savingDialog.show();

    	// ファイルをバックアップするディレクトリを作成する
    	File dir = new File(context.getFilesDir() + "/exported");
    	if (!dir.mkdir())
        {
            Log.v(TAG, "mkdir is failed.");
        }
    }
	
    /**
     *  非同期処理実施前の前処理
     */
    @Override
    protected void onPreExecute()
    {
        // なにもしない。
    }

    /**
     *    ビットマップデータを(PNG形式で)保管する。
     */
    private String exportToFile(String baseName, Bitmap targetImage)
    {
        String resultMessage = "";
        try
        {
            String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/" + APP_NAMESPACE + "/";
            ContentResolver resolver = context.getContentResolver();
            String fileName = baseName + ".png";

            Uri extStorageUri;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + APP_NAMESPACE);
                values.put(MediaStore.Images.Media.IS_PENDING, true);
                extStorageUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            }
            else
            {
                File path = new File(outputDir);
                values.put(MediaStore.Images.Media.DATA, path.getAbsolutePath() + File.separator + fileName);
                extStorageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            Log.v(TAG, "---------- " + baseName + ".png " + values);
            Uri imageUri = resolver.insert(extStorageUri, values);
            if (imageUri != null)
            {
                ////////////////////////////////////////////////////////////////
                if (dumpLog)
                {
                    try
                    {
                        Cursor cursor = resolver.query(imageUri, null, null, null, null);
                        DatabaseUtils.dumpCursor(cursor);
                        cursor.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        resultMessage = e.getMessage();
                    }
                }
                ////////////////////////////////////////////////////////////////

                OutputStream outputStream = resolver.openOutputStream(imageUri, "wa");
                if (outputStream != null)
                {
                    targetImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    resolver.update(imageUri, values, null, null);
                }
            }
            else
            {
                Log.v(TAG, " cannot get imageUri...");
            }
            exportedUri = imageUri;
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            resultMessage = t.getMessage();
            exportedUri = null;
        }
        return (resultMessage);
    }

    /**
     *    キャンバスの大きさがどれくらい必要か、チェックする。
     */
    private Rect checkCanvasSize()
    {
        Rect canvasSize = new Rect();

        // オブジェクトの配置位置を探る。
    	Enumeration<Integer> keys = objectHolder.getObjectKeys();
        while (keys.hasMoreElements())
        {
            Integer key = keys.nextElement();
            PositionObject pos = objectHolder.getPosition(key);
            RectF posRect = pos.getRect();
            if (canvasSize.left > posRect.left)
            {
            	canvasSize.left = (int) posRect.left;
            }
            if (canvasSize.right < posRect.right)
            {
            	canvasSize.right = (int) posRect.right;
            }
            if (canvasSize.top > posRect.top)
            {
            	canvasSize.top = (int) posRect.top;
            }
            if (canvasSize.bottom < posRect.bottom)
            {
            	canvasSize.bottom = (int) posRect.bottom;
            }
        }
        
        // 描画領域にちょっと余裕を持たせる
        canvasSize.left = canvasSize.left - OUTPUT_MARGIN;
        canvasSize.right = canvasSize.right + OUTPUT_MARGIN;
        canvasSize.top = canvasSize.top - OUTPUT_MARGIN_TOP;
        canvasSize.bottom = canvasSize.bottom + OUTPUT_MARGIN;
        canvasSize.sort();

        // 現在の画面サイズを取得
        if (displayWidth < MINIMUM_WIDTH)
        {
            displayWidth = MINIMUM_WIDTH;
        }
        if (displayHeight < MINIMUM_HEIGHT)
        {
            displayHeight = MINIMUM_HEIGHT;
        }        

        // 出力の最小サイズを(表示画面サイズに)設定
        if (canvasSize.width() < displayWidth)
        {
        	canvasSize.right = canvasSize.left + displayWidth;
        }
        if (canvasSize.height() < displayHeight)
        {
        	canvasSize.bottom = canvasSize.top + displayHeight;
        }

        // 画像位置（キャンバス位置）の調整。。。
        offsetX = 0.0f - canvasSize.left - (OUTPUT_MARGIN);
        offsetY = 0.0f - canvasSize.top - (OUTPUT_MARGIN);

        // 出力する画像データのサイズを表示する
        Log.v(TAG, "ObjectLayoutCaptureExporter::checkCanvasSize() w:" + canvasSize.width() + " , h:" + canvasSize.height() + "  offset :(" + offsetX + "," + offsetY + ")");
        return (canvasSize);
    }    

    /**
     *  非同期処理
     *  （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     */
    @Override
    protected String doInBackground(String... datas)
    {
    	String result = "";
    	try
        {
            Rect canvasSize = checkCanvasSize();
            Bitmap targetBitmap = Bitmap.createBitmap(canvasSize.width(), canvasSize.height(), Bitmap.Config.RGB_565);
            Canvas targetCanvas = new Canvas(targetBitmap);

            // オブジェクトをビットマップの中に書き込む
            canvasDrawer.drawOnBitmapCanvas(targetCanvas, offsetX, offsetY);

            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Calendar.getInstance().getTime()) + "_" + datas[0];

            // データを保管する
            result = exportToFile(fileName, targetBitmap);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
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
            	receiver.onCaptureLayoutExportedResult(exportedUri, result, OUTPUT_EXPORT_SHARE_ID);
            }
    	}
    	catch (Exception ex)
    	{
    		Log.v(TAG, "ViewCaptureExporter::onPostExecute() : " + ex.getMessage());
            ex.printStackTrace();
    	}
    	// プログレスダイアログを消す
    	if (savingDialog != null)
    	{
            savingDialog.dismiss();
    	}
    }     
 
    /**
     *    結果報告用のインタフェース
     */
    public interface ICaptureLayoutExporter
    {
        //  保存結果の報告
        void onCaptureLayoutExportedResult(Uri exportedUri, String detail, int id);
    }
}
