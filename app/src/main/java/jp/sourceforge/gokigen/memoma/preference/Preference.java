package jp.sourceforge.gokigen.memoma.preference;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.R;

/**
 *    Androidの設定画面
 * 
 * @author MRSa
 *
 */
public class Preference extends PreferenceActivity implements OnSharedPreferenceChangeListener, android.preference.Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private final IPreferenceIntentCaller intentCaller;
    private final int SELECT_BACKGROUND_IMAGE = 100;

    /**
     *  コンストラクタ
     *
     **/
    public Preference()
    {
        intentCaller = PreferenceIntentCaller.newInstance(this);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.preference);

       // findPreference("backgroundUri").setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     *  設定値が変更されたときの処理
     */
    public void onSharedPreferenceChanged(SharedPreferences shardPref, String key)
    {
        //
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v(TAG, "onActivityResult() : start");
        try
        {
            switch (resultCode)
            {
                case Activity.RESULT_OK:
                    String filePath = "";
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    try
                    {
                        Cursor cursor = getContentResolver().query(data.getData(), projection, null, null, null);
                        if (cursor != null) {
                            if (cursor.getCount() > 0) {
                                cursor.moveToNext();
                                filePath = cursor.getString(0);
                            }
                            cursor.close();
                        }
                        setActivityResultValue(requestCode, filePath);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    break;

                default:
                    break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "onActivityResult() : end");
    }

    private void setActivityResultValue(int requestCode, String filePath)
    {
        String key;
        if (requestCode == SELECT_BACKGROUND_IMAGE)
        {
            key = "backgroundUri";
        }
        else
        {
            // 何もしない
            return;
        }
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        findPreference(key).setSummary(fileName);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, filePath);
        editor.apply();
        Log.v(TAG, " key : " + key + " image File : " + fileName + " (" + filePath + ")");
        //Toast.makeText(getContext(), "Selected :" + fileName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onPreferenceClick(android.preference.Preference preference)
    {
        try
        {
            if (!preference.hasKey())
            {
                return (false);
            }
            String key = preference.getKey();
            return (key.contains("backgroundUri") && (intentCaller.selectBackgroundImageFileFromGallery(SELECT_BACKGROUND_IMAGE)));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
}
