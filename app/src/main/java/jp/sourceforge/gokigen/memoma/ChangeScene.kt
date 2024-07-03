package jp.sourceforge.gokigen.memoma

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import jp.sourceforge.gokigen.memoma.extension.ExtensionFragment
import jp.sourceforge.gokigen.memoma.io.MeMoMaDataInOutManager
import jp.sourceforge.gokigen.memoma.preference.PreferenceFragment

class ChangeScene(private val parent: AppCompatActivity) : IChangeScene
{
    private val inOutManager = MeMoMaDataInOutManager(parent)
    private lateinit var mainFragment: MainFragment
    private lateinit var preferenceFragment: PreferenceFragment
    private lateinit var extentionFragment: ExtensionFragment
    private lateinit var listener: MeMoMaListener

    fun prepare() {
        Log.v(TAG, "ChangeScene::prepare()")

        if (!::listener.isInitialized)
        {
            listener = MeMoMaListener(parent, inOutManager, this)
        }
        if (!::extentionFragment.isInitialized)
        {
            extentionFragment = ExtensionFragment.newInstance(parent, inOutManager)
        }
        if (!::preferenceFragment.isInitialized)
        {
            preferenceFragment = PreferenceFragment.newInstance(parent)
        }
        if (!::mainFragment.isInitialized)
        {
            mainFragment = MainFragment.newInstance(this, listener)
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
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun changeSceneToExtension(title: String)
    {
        try
        {
            // ----- オブジェクト一覧画面に遷移させる
            Log.v(TAG, "ChangeScene::changeSceneToExtension()")
            if (!::extentionFragment.isInitialized)
            {
                extentionFragment = ExtensionFragment.newInstance(parent, inOutManager)
            }
            extentionFragment.setDataTitle(title)
            val transaction: FragmentTransaction = parent.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, extentionFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun changeSceneToPreference()
    {
        try
        {
            // ----- 設定画面に遷移させる
            if (!::preferenceFragment.isInitialized)
            {
                preferenceFragment = PreferenceFragment.newInstance(parent)
            }
            val transaction: FragmentTransaction = parent.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment1, preferenceFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
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
