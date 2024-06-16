package jp.sourceforge.gokigen.memoma

import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity

/**
 * メイン画面の処理
 */
class Main : AppCompatActivity()
{
    private val sceneChanger = ChangeScene(this)

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // タイトルバーにアクションバーを出す
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)

        // レイアウトを設定する
        setContentView(R.layout.activity_main)

        try
        {
            // ----- 最初の画面を開く
            sceneChanger.prepare()
            sceneChanger.changeSceneToMain()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

/*     //  たぶん今は使っていない、、と、思う
        try
        {
            if (!allPermissionsGranted())
            {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
*/
        Log.v(TAG, " START MeMoMa...")
    }

    /**
     * メニューの生成
     */
/*
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
*/
    /**
     * メニューアイテムの選択
     */
/*
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
*/
    /**
     * メニュー表示前の処理
     */
/*
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
*/
/*
    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    param
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                // Permission Denied
                if ((param == Manifest.permission.READ_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN))
                {
                    // この場合は権限付与がされているかどうかの判断を除外 (デバイスが JELLY_BEAN よりも古く、READ_EXTERNAL_STORAGE がない場合）
                }
                //else if ((param == Manifest.permission.ACCESS_MEDIA_LOCATION)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
                //{
                //    //　この場合は権限付与がされているかどうかの判断を除外 (デバイスが (10) よりも古く、ACCESS_MEDIA_LOCATION がない場合）
                //}
                else
                {
                    result = false
                }
            }
        }
        return (result)
    }
*/
/*
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
*/
    /**
     * 画面が裏に回ったときの処理
     */
/*
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
*/
    /**
     * 画面が表に出てきたときの処理
     */
/*
    public override fun onResume()
    {
        super.onResume()
        try
        {
            // 動作準備するようイベント処理クラスに指示する
            listener.prepareToStart()
            listener.updateContentList()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }
*/
    /**
     * 終了時の処理
     */
/*
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
*/
    /**
     *
     */
/*
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
*/
    /**
     *
     */
/*
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
*/
    /**
     * ダイアログ表示の準備
     *
     */
    //override fun onCreateDialog(id: Int): Dialog?
    //{
    //    return listener.onCreateDialog(id)
    //}

    /**
     * ダイアログ表示の準備
     *
     */
    //override fun onPrepareDialog(id: Int, dialog: Dialog)
    // {
    //    try
    //    {
    //        listener.onPrepareDialog(id, dialog)
    //    }
    //    catch (e: Exception)
    //    {
    //        e.printStackTrace()
    //    }
    //}

    /**
     * 子画面から応答をもらったときの処理
     */
    //override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    //{
    //    super.onActivityResult(requestCode, resultCode, data)
    //    try
    //    {
    //        // 子画面からもらった情報の応答処理をイベント処理クラスに依頼する
    //        listener.onActivityResult(requestCode, resultCode, data)
    //    }
    //    catch (ex: Exception)
    //    {
    //        ex.printStackTrace()
    //    }
    //}

/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS)
        {
            if (!allPermissionsGranted())
            {
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
            }
        }
    }
*/
    companion object {
        const val APP_NAMESPACE = "gokigen"
        private val TAG = Main::class.java.simpleName
        //private const val REQUEST_CODE_PERMISSIONS = 10
        //private val REQUIRED_PERMISSIONS = arrayOf(
        //    Manifest.permission.READ_EXTERNAL_STORAGE,
        //    Manifest.permission.WRITE_EXTERNAL_STORAGE,
        //)
    }
}
