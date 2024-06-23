package jp.sourceforge.gokigen.memoma

interface IChangeScene
{
    fun changeSceneToMain()
    fun changeSceneToExtension(title: String)
    fun changeSceneToPreference()
    fun exitApplication()
}
