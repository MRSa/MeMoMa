package jp.sourceforge.gokigen.memoma.preference;

import android.app.Activity;
import android.content.Intent;

public class PreferenceIntentCaller implements IPreferenceIntentCaller
{
    private final Activity activity;

    public static IPreferenceIntentCaller newInstance(Activity activity)
    {
        return (new PreferenceIntentCaller(activity));
    }

    private PreferenceIntentCaller(Activity activity)
    {
        this.activity = activity;
    }


    @Override
    public boolean selectBackgroundImageFileFromGallery(int code)
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, code);
        return (false);
    }
}
