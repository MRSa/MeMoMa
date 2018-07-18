package jp.sourceforge.gokigen.memoma;

import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import jp.sourceforge.gokigen.memoma.fileio.MeMoMaDataInOutManager;

import static android.os.Build.VERSION_CODES.KITKAT;

/**
 *   メイン画面の処理
 * 
 * @author MRSa
 *
 */
public class Main extends  AppCompatActivity
{
    public static final String APP_IDENTIFIER = "Gokigen";
    public static final String APP_BASEDIR = "/MeMoMa";
	public static final String APP_NAMESPACE = "gokigen";

    private MeMoMaListener listener = null;  // イベント処理クラス

    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        final int REQUEST_NEED_PERMISSIONS = 1010;

        super.onCreate(savedInstanceState);

        // データ保存・展開クラス を生成する
        // リスナクラスを生成する
        listener = new MeMoMaListener(this, new MeMoMaDataInOutManager(this));

        // タイトルにプログレスバーを出せるようにする
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // タイトルバーにアクションバーを出す
//       requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        // レイアウトを設定する **/
        setContentView(R.layout.main);

        // 外部メモリアクセス権のオプトイン
        if (((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED))||
            (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_DOCUMENTS) != PackageManager.PERMISSION_GRANTED))
        {
            if (Build.VERSION.SDK_INT >= KITKAT)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.MANAGE_DOCUMENTS,
                        },
                        REQUEST_NEED_PERMISSIONS);
            }
            else
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        },
                        REQUEST_NEED_PERMISSIONS);
            }
        }

        try
        {
            // リスナクラスの準備
            listener.prepareListener();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  メニューの生成
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        try
        {
            menu = listener.onCreateOptionsMenu(menu);
            return (super.onCreateOptionsMenu(menu));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
    
    /**
     *  メニューアイテムの選択
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        try
        {
            return (listener.onOptionsItemSelected(item));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
    
    /**
     *  メニュー表示前の処理
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        try
        {
            listener.onPrepareOptionsMenu(menu);
            return (super.onPrepareOptionsMenu(menu));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    /**
     *  画面が裏に回ったときの処理
     */
    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            // 動作を止めるようイベント処理クラスに指示する
        	listener.shutdown();        	
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     *  画面が表に出てきたときの処理
     */
    @Override
    public void onResume()
    {
        super.onResume();
        try
        {
        	// 動作準備するようイベント処理クラスに指示する
        	listener.prepareToStart();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     *   終了時の処理
     * 
     */
    @Override
    protected void onDestroy()
    {
        try
        {
            listener.finishListener();
            super.onDestroy();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    @Override
    protected void onStart()
    {
        super.onStart();
    }

    /**
     * 
     */
    @Override
    protected void onStop()
    {
        super.onStop();
    }

    /**
     * 
     */
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        try
        {
            super.onSaveInstanceState(outState);
            if (listener != null)
            {
                // ここでActivityの情報を覚える
                listener.onSaveInstanceState(outState);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        try
        {
            if (listener != null)
            {
                // ここでActivityの情報を展開する
                listener.onRestoreInstanceState(savedInstanceState);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  ダイアログ表示の準備
     * 
     */
    @Override
    protected Dialog onCreateDialog(int id)
    {
        try
        {
            return (listener.onCreateDialog(id));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    /**
     *  ダイアログ表示の準備
     * 
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog)
    {
        try
        {
            listener.onPrepareDialog(id, dialog);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     *  子画面から応答をもらったときの処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        try
        {
            // 子画面からもらった情報の応答処理をイベント処理クラスに依頼する
        	listener.onActivityResult(requestCode, resultCode, data);
        }
        catch (Exception ex)
        {
           ex.printStackTrace();
        }
    }    
}
