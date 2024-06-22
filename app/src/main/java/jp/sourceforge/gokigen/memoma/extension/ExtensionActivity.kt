package jp.sourceforge.gokigen.memoma.extension

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ExtensionActivity : Fragment()
{
    private var listener: ExtensionActivityListener? = null
    init {
        val activity = this.activity as AppCompatActivity
        listener = ExtensionActivityListener(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        try
        {
            Log.v(TAG, "ExtensionActivity::onCreate()")

            //// レイアウトを設定する
            //setContentView(R.layout.extensionview)

            // リスナクラスの準備
            val intent = this.activity?.intent
            if (intent != null)
            {
                listener?.prepareExtraDatas(intent)
            }
            listener?.prepareListener()

            // メニューがあるよ
            setHasOptionsMenu(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * メニューの生成
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        try
        {
            listener?.onCreateOptionsMenu(menu)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (super.onCreateOptionsMenu(menu, inflater))
    }

    /**
     * メニューアイテムの選択
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        if (listener != null)
        {
            return (listener?.onOptionsItemSelected(item)?: false)
        }
        return (false)
    }

    /**
     * メニュー表示前の処理
     */
    override fun onPrepareOptionsMenu(menu: Menu)
    {
        try
        {
            super.onPrepareOptionsMenu(menu)
            listener?.onPrepareOptionsMenu(menu)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 画面が裏に回ったときの処理
     */
    public override fun onPause()
    {
        try
        {
            super.onPause()

            // 動作を止めるようイベント処理クラスに指示する
            listener?.shutdown()
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
        try
        {
            super.onResume()
            listener?.prepareToStart()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * 終了時の処理
     *
     */
    override fun onDestroy()
    {
        try
        {
            //listener?.finishListener()
            super.onDestroy()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = ExtensionActivity::class.java.simpleName

        // 起動コード
        const val MEMOMA_EXTENSION_LAUNCH_ACTIVITY: String = "jp.sfjp.gokigen.memoma.extension.activity"

        // データ識別子(表示中データの保存ファイルへのフルパス)
        const val MEMOMA_EXTENSION_DATA_FULLPATH: String = "jp.sfjp.gokigen.memoma.extension.data.fullpath"
        const val MEMOMA_EXTENSION_DATA_TITLE: String = "jp.sfjp.gokigen.memoma.extension.data.title"
    }
}