package jp.sourceforge.gokigen.memoma

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import jp.sourceforge.gokigen.memoma.io.MeMoMaDataInOutManager

/**
 * メイン画面の処理
 */
class Main : AppCompatActivity()
{
    private lateinit var listener: MeMoMaListener // イベント処理クラス

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // リスナクラスを生成する
        listener = MeMoMaListener(this, MeMoMaDataInOutManager(this))

        // タイトルにプログレスバーを出せるようにする
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)

        // タイトルバーにアクションバーを出す
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)

        // レイアウトを設定する **/
        setContentView(R.layout.main)

        try
        {
            // リスナクラスの準備
            listener.prepareListener()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        Log.v(TAG, " START MEMOMA...");
    }

    /**
     * メニューの生成
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean
    {
        try
        {
            val menu0 = listener.onCreateOptionsMenu(menu)
            return (super.onCreateOptionsMenu(menu0))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    /**
     * メニューアイテムの選択
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        try
        {
            return listener.onOptionsItemSelected(item)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    /**
     * メニュー表示前の処理
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean
    {
        try
        {
            listener.onPrepareOptionsMenu(menu)
            return (super.onPrepareOptionsMenu(menu))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 画面が裏に回ったときの処理
     */
    public override fun onPause()
    {
        super.onPause()
        try
        {
            // 動作を止めるようイベント処理クラスに指示する
            listener.shutdown()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * 画面が表に出てきたときの処理
     */
    public override fun onResume()
    {
        super.onResume()
        try
        {
            // 動作準備するようイベント処理クラスに指示する
            listener.prepareToStart()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * 終了時の処理
     */
    override fun onDestroy()
    {
        try
        {
            listener.finishListener()
            super.onDestroy()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     */
    override fun onSaveInstanceState(outState: Bundle)
    {
        try
        {
            super.onSaveInstanceState(outState)
            if (::listener.isInitialized)
            {
                // ここでActivityの情報を覚える
                listener.onSaveInstanceState(outState)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle)
    {
        super.onRestoreInstanceState(savedInstanceState)
        try
        {
            if (::listener.isInitialized)
            {
                // ここでActivityの情報を展開する
                listener.onRestoreInstanceState(savedInstanceState)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ダイアログ表示の準備
     *
     */
    override fun onCreateDialog(id: Int): Dialog
    {
        return listener.onCreateDialog(id)
    }

    /**
     * ダイアログ表示の準備
     *
     */
    override fun onPrepareDialog(id: Int, dialog: Dialog)
    {
        try
        {
            listener.onPrepareDialog(id, dialog)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 子画面から応答をもらったときの処理
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        try
        {
            // 子画面からもらった情報の応答処理をイベント処理クラスに依頼する
            listener.onActivityResult(requestCode, resultCode, data)
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    companion object {
        const val APP_NAMESPACE = "gokigen"
        private val TAG = Main::class.java.simpleName
    }
}
