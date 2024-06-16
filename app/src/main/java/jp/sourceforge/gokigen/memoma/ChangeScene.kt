package jp.sourceforge.gokigen.memoma

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import jp.sourceforge.gokigen.memoma.io.MeMoMaDataInOutManager

class ChangeScene(private val parent: AppCompatActivity) : IChangeScene
{
    private lateinit var mainFragment : MainFragment
    private lateinit var listener: MeMoMaListener // イベント処理クラス

    fun prepare()
    {
        if (!::listener.isInitialized)
        {
            listener = MeMoMaListener(parent, MeMoMaDataInOutManager(parent))
        }
    }

    override fun changeSceneToMain()
    {
        try
        {
            // 念のための初期化
            prepare()

            // ----- メイン画面に遷移させる
            if (!::mainFragment.isInitialized)
            {
                mainFragment = MainFragment.newInstance(this, listener)
            }
            val transaction: FragmentTransaction = parent.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, mainFragment)
            transaction.commitAllowingStateLoss()

            // --- backstackに追加してcommit
            //transaction.addToBackStack(null)
            //transaction.commit()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun changeSceneToExtension() {
        TODO("Not yet implemented")
    }

    override fun changeSceneToPreference() {
        TODO("Not yet implemented")
    }

    override fun exitApplication()
    {
        // -----  アプリケーションの終了
        Log.v(TAG, "exitApplication()")
        try
        {
            parent.finish()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = ChangeScene::class.java.simpleName
    }
}
