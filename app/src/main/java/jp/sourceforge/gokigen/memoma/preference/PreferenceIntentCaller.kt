package jp.sourceforge.gokigen.memoma.preference

import android.app.Activity
import android.content.Intent

class PreferenceIntentCaller private constructor(private val activity: Activity) :
    IPreferenceIntentCaller {
    override fun selectBackgroundImageFileFromGallery(code: Int): Boolean {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        activity.startActivityForResult(intent, code)
        return (false)
    }

    companion object {
        fun newInstance(activity: Activity): IPreferenceIntentCaller {
            return (PreferenceIntentCaller(activity))
        }
    }
}