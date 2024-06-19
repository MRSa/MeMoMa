package jp.sourceforge.gokigen.memoma.preference

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.provider.MediaStore
import android.util.Log
import jp.sourceforge.gokigen.memoma.R

/**
 * Androidの設定画面
 *
 * @author MRSa
 */
class Preference : PreferenceActivity(), OnSharedPreferenceChangeListener,
    Preference.OnPreferenceClickListener {
    private val TAG = toString()
    private val intentCaller = PreferenceIntentCaller.newInstance(this)
    private val SELECT_BACKGROUND_IMAGE = 100

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.layout.preference)

        // findPreference("backgroundUri").setOnPreferenceClickListener(this);
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * 設定値が変更されたときの処理
     */
    override fun onSharedPreferenceChanged(shardPref: SharedPreferences, key: String?) {
        //
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAG, "onActivityResult() : start")
        try {
            when (resultCode) {
                RESULT_OK -> {
                    var filePath = ""
                    val projection = arrayOf(MediaStore.MediaColumns.DATA)
                    try {
                        val cursor =
                            contentResolver.query(data.data!!, projection, null, null, null)
                        if (cursor != null) {
                            if (cursor.count > 0) {
                                cursor.moveToNext()
                                filePath = cursor.getString(0)
                            }
                            cursor.close()
                        }
                        setActivityResultValue(requestCode, filePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.v(TAG, "onActivityResult() : end")
    }

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
        findPreference(key).summary = fileName
        val editor = preferences.edit()
        editor.putString(key, filePath)
        editor.apply()
        Log.v(TAG, " key : $key image File : $fileName ($filePath)")
        //Toast.makeText(getContext(), "Selected :" + fileName, Toast.LENGTH_SHORT).show();
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
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
}