package jp.sourceforge.gokigen.memoma

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import jp.sourceforge.gokigen.memoma.extension.ExtensionFragment
import jp.sourceforge.gokigen.memoma.io.MeMoMaDataInOutManager
import jp.sourceforge.gokigen.memoma.preference.PreferenceFragment

class ChangeScene(private val parent: AppCompatActivity) : IChangeScene
{
    private lateinit var mainFragment: MainFragment
    private lateinit var preferenceFragment: PreferenceFragment
    private lateinit var extentionFragment: ExtensionFragment
    private lateinit var listener: MeMoMaListener

    fun prepare() {
        if (!::listener.isInitialized)
        {
            listener = MeMoMaListener(parent, MeMoMaDataInOutManager(parent), this)
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
            if (!::extentionFragment.isInitialized)
            {
                extentionFragment = ExtensionFragment.newInstance(parent)
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
                preferenceFragment = PreferenceFragment.newInstance()
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