package jp.sourceforge.gokigen.memoma

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * メイン画面の処理
 */
class Main : AppCompatActivity()
{
    private val sceneChanger = ChangeScene(this)

    public override fun onStart()
    {
        super.onStart()
        try
        {
            Log.v(TAG, "Main::onStart()")
            sceneChanger.prepare()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // タイトルバーにアクションバーを出す
        //supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)

        // レイアウトを設定する
        setContentView(R.layout.activity_main)

        // ツールバーの指定を変える
        try
        {
            setSupportActionBar(findViewById(R.id.toolbar))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        try
        {
            setupWindowInset(findViewById(R.id.base_layout))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        try
        {
            // ----- 最初の画面を開く
            sceneChanger.changeSceneToMain()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        if (allPermissionsGranted())
        {
            Log.v(TAG, "allPermissionsGranted() : true")
            // ----- 最初の画面を開く
            sceneChanger.prepare()
            sceneChanger.changeSceneToMain()
        }
        else
        {
            Log.v(TAG, "====== REQUEST PERMISSIONS ======")
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        Log.v(TAG, " START MeMoMa...")
    }

    private fun setupWindowInset(view: View)
    {
        try
        {
            // Display cutout insets
            //   https://developer.android.com/develop/ui/views/layout/edge-to-edge
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    left = bars.left,
                    top = bars.top,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                WindowInsetsCompat.CONSUMED
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(
                    baseContext,
                    param
                ) != PackageManager.PERMISSION_GRANTED
            )
            {
                // Permission Denied
                if ((param == Manifest.permission.READ_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN))
                {
                    // この場合は権限付与の判断を除外 (デバイスが JELLY_BEAN よりも古く、READ_EXTERNAL_STORAGE がない場合)
                }
                else if ((param == Manifest.permission.ACCESS_MEDIA_LOCATION)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (10) よりも古く、ACCESS_MEDIA_LOCATION がない場合)
                }
                else if ((param == Manifest.permission.READ_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 34以上はエラーになる...)
                }
                else if ((param == Manifest.permission.WRITE_EXTERNAL_STORAGE)&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
                {
                    // この場合は、権限付与の判断を除外 (SDK: 34以上はエラーになる...)
                }
                else
                {
                    Log.v(TAG, " Permission: $param : ${Build.VERSION.SDK_INT}")
                    result = false
                }
            }
        }
        return (result)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v(TAG, "------------------------- onRequestPermissionsResult() ")
        if (requestCode == REQUEST_CODE_PERMISSIONS)
        {
            if (allPermissionsGranted())
            {
                // ----- 権限が有効だった、最初の画面を開く
                Log.v(TAG, "onRequestPermissionsResult()")
                sceneChanger.prepare()
                sceneChanger.changeSceneToMain()
            }
            else
            {
                Log.v(TAG, "----- onRequestPermissionsResult() : false")
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object
    {
        const val APP_NAMESPACE = "gokigen"
        private val TAG = Main::class.java.simpleName
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }
}
