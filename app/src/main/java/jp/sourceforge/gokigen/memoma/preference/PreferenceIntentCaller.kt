package jp.sourceforge.gokigen.memoma.preference

import android.content.Intent
import androidx.fragment.app.FragmentActivity

class PreferenceIntentCaller private constructor(private val activity: FragmentActivity?) :
    IPreferenceIntentCaller {
    override fun selectBackgroundImageFileFromGallery(code: Int): Boolean {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType("image/*")
        activity?.startActivityForResult(intent, code)
        return (false)
    }

    companion object {
        fun newInstance(activity: FragmentActivity?): IPreferenceIntentCaller {
            return (PreferenceIntentCaller(activity))
        }
    }
}