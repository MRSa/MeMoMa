package jp.sourceforge.gokigen.memoma.listitem

class SymbolListArrayItem(
    private var iconResource: Int,
    private var textResource1st: String,
    private var textResource2nd: String,
    private var textResource3rd: String,
    private var subIconResource: Int
)
{
    fun getIconResource(): Int {
        return (iconResource)
    }

    fun setIconResource(iconId: Int) {
        iconResource = iconId
    }

    fun getTextResource1st(): String {
        return (textResource1st)
    }

    fun setTextResource1st(textData: String) {
        textResource1st = textData
    }

    fun getTextResource2nd(): String {
        return (textResource2nd)
    }

    fun setTextResource2nd(textData: String) {
        textResource2nd = textData
    }

    fun getTextResource3rd(): String {
        return (textResource3rd)
    }

    fun setTextResource3rd(textData: String) {
        textResource3rd = textData
    }

    fun getSubIconResource(): Int {
        return (subIconResource)
    }

    fun setSubIconResource(iconId: Int) {
        subIconResource = iconId
    }
}