package jp.sourceforge.gokigen.memoma.io;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;

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
    private final String TAG = toString();
    private final Context context;
    private IResultReceiver receiver;
    private String exportedFileName = null;

    private ProgressDialog savingDialog;

    /**
     *   コンストラクタ
     */
    public MeMoMaFileExportCsvProcess(Context context,  IResultReceiver resultReceiver)
    {
        this.context = context;
        receiver = resultReceiver;

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
            Log.v(toString(), "mkDir() fail. : " + dir.getAbsolutePath());
        }
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
     */
    private String exportToCsvFile(String fileName, MeMoMaObjectHolder objectHolder)
    {
        String resultMessage = "";
        try
        {
            // エクスポートするファイル名を決定する
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat outFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            exportedFileName = fileName + "_" + outFormat.format(calendar.getTime()) + ".csv";
            FileWriter writer = new FileWriter(new File(exportedFileName));

            //  データのタイトルを出力
            String str = "; label,detail,userChecked,shape,style,centerX,centerY,width,height,;!<_$ (';!<_$' is a record Separator)\r\n";
            writer.write(str);

            // オブジェクトの出力 （保持しているものをすべて表示する）
            Enumeration<Integer> keys = objectHolder.getObjectKeys();
            while (keys.hasMoreElements())
            {
                Integer key = keys.nextElement();
                PositionObject pos = objectHolder.getPosition(key);
                RectF posRect = pos.getRect();

                // TODO:  絞り込み条件がある場合には、その条件に従ってしぼり込む必要あり。

                str = "";
                str = str + "\"" + pos.getLabel() + "\"";
                str = str + ",\"" + pos.getDetail() + "\"";
                if (pos.getUserChecked())
                {
                    str = str + ",True";
                }
                else
                {
                    str = str + ",False";
                }
                str = str + "," + pos.getDrawStyle();   // オブジェクトの形状
                str = str + "," + pos.getPaintStyle();   // オブジェクトの塗りつぶし状態
                str = str + "," + (Math.round(posRect.centerX() * 100.0f) / 100.0f);
                str = str + "," + (Math.round(posRect.centerY() * 100.0f) / 100.0f);
                str = str + "," + (Math.round(posRect.width() * 100.0f) / 100.0f);
                str = str + "," + (Math.round(posRect.height() * 100.0f) / 100.0f);
                str = str + ",;!<_$\r\n";
                writer.write(str);
            }
            writer.flush();
            writer.close();
        }
        catch (Exception e)
        {
            resultMessage = " ERR>" + e.toString();
            Log.v(TAG, resultMessage);
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
        String fileName = context.getFilesDir() + "/exported/" + datas[0].getDataTitle();

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
            Log.v(TAG, "MeMoMaFileExportCsvProcess::onPostExecute() : " + ex.toString());
        }
        // プログレスダイアログを消す
        savingDialog.dismiss();
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
        void onExportedResult(String exportedFileName, String detail);
    }
}
