package jp.sourceforge.gokigen.memoma.preference

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R

/**
 * Androidの設定画面
 */
class PreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener,
    PreferenceManager.OnPreferenceTreeClickListener
{
    private val intentCaller = PreferenceIntentCaller.newInstance(this.activity)
    private val SELECT_BACKGROUND_IMAGE = 100
    private lateinit var parent: AppCompatActivity

    private fun prepare(parent: AppCompatActivity)
    {
        this.parent = parent
    }

    override fun onResume()
    {
        super.onResume()
        try
        {
            if (::parent.isInitialized)
            {
                //parent.supportActionBar?.title = parent.getString(R.string.preference_name)
                parent.supportActionBar?.hide()
            }
            preferenceManager.preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onPause()
    {
        super.onPause()
        try
        {
            if (::parent.isInitialized)
            {
                parent.supportActionBar?.show()
            }
            preferenceManager.preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        try
        {
            addPreferencesFromResource(R.xml.preference)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * 設定値が変更されたときの処理
     */
    override fun onSharedPreferenceChanged(shardPref: SharedPreferences, key: String?)
    {
        val value = if (key != null) { shardPref.getString(key, "") } else { "(???)" }
        Log.v(TAG, "onSharedPreferenceChanged($key) : $value")
    }
/*
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAG, "onActivityResult() : start")
        try {
            when (resultCode)
            {
                RESULT_OK -> {
                    var filePath = ""
                    val projection = arrayOf(MediaStore.MediaColumns.DATA)
                    try
                    {
                        val uriData = data.data
                        val activity = activity
                        if ((activity != null)&&(uriData != null)) {
                            val cursor =
                                activity.contentResolver?.query(
                                    uriData,
                                    projection,
                                    null,
                                    null,
                                    null
                                )
                            if (cursor != null)
                            {
                                if (cursor.count > 0) {
                                    cursor.moveToNext()
                                    filePath = cursor.getString(0)
                                }
                                cursor.close()
                            }
                            setActivityResultValue(requestCode, filePath)
                        }
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
                else -> {}
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        Log.v(TAG, "onActivityResult() : end")
    }
*/
/*
    private fun setActivityResultValue(requestCode: Int, filePath: String) {
        val key: String
        if (requestCode == SELECT_BACKGROUND_IMAGE) {
            key = "backgroundUri"
        } else {
            // 何もしない
            return
        }
        val preferences = preferenceScreen.sharedPreferences
        val fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
        findPreference<Preference>(key)?.summary = fileName
        val editor = preferences?.edit()
        editor?.putString(key, filePath)
        editor?.apply()
        Log.v(TAG, " key : $key image File : $fileName ($filePath)")
        //Toast.makeText(getContext(), "Selected :" + fileName, Toast.LENGTH_SHORT).show();
    }
*/

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        try {
            if (!preference.hasKey()) {
                return (false)
            }
            val key = preference.key
            return (key.contains("backgroundUri") && (intentCaller.selectBackgroundImageFileFromGallery(
                SELECT_BACKGROUND_IMAGE
            )))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (false)
    }

    companion object
    {
        private val TAG = PreferenceFragment::class.java.simpleName
        fun newInstance(activity: AppCompatActivity): PreferenceFragment
        {
            val instance = PreferenceFragment()
            val arguments = Bundle()
            instance.setArguments(arguments)
            instance.prepare(activity)
            return (instance)
        }

    }
}
