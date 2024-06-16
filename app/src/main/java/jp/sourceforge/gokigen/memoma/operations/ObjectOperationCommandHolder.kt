package jp.sourceforge.gokigen.memoma.operations

import android.content.Context
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.dialogs.ItemSelectionDialog.ISelectionItemHolder

class ObjectOperationCommandHolder(private val parent: Context) : ISelectionItemHolder
{
    override fun isMultipleSelection(): Boolean
    {
        return (false)
    }

    override fun getItems(): Array<String?>
    {
        val ret = arrayOfNulls<String>(5)
        ret[0] = parent.getString(R.string.object_delete)
        ret[1] = parent.getString(R.string.object_duplicate)
        ret[2] = parent.getString(R.string.object_bigger)
        ret[3] = parent.getString(R.string.object_smaller)
        ret[4] = parent.getString(R.string.object_share)
        return (ret)
    }

    override fun getItem(index: Int): String
    {
        var message = ""
        when (index) {
            OBJECTOPERATION_DELETE -> message = parent.getString(R.string.object_delete)
            OBJECTOPERATION_DUPLICATE -> message = parent.getString(R.string.object_duplicate)
            OBJECTOPERATION_SIZEBIGGER -> message = parent.getString(R.string.object_bigger)
            OBJECTOPERATION_SIZESMALLER -> message = parent.getString(R.string.object_smaller)
            OBJECTOPERATION_SHARE -> message = parent.getString(R.string.object_share)
            else -> {}
        }
        return (message)
    }

    override fun getSelectionStatus(): BooleanArray
    {
        return (booleanArrayOf())
    }

    override fun setSelectionStatus(index: Int, isSelected: Boolean)
    {
        // なにもしない
    }

    companion object
    {
        const val OBJECTOPERATION_DELETE: Int = 0
        const val OBJECTOPERATION_DUPLICATE: Int = 1
        const val OBJECTOPERATION_SIZEBIGGER: Int = 2
        const val OBJECTOPERATION_SIZESMALLER: Int = 3
        const val OBJECTOPERATION_SHARE: Int = 4
    }
}
