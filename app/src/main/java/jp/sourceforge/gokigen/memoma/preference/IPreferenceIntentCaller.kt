package jp.sourceforge.gokigen.memoma.preference

interface IPreferenceIntentCaller {
    fun selectBackgroundImageFileFromGallery(code: Int): Boolean
}