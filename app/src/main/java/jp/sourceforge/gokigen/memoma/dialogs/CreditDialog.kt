package jp.sourceforge.gokigen.memoma.dialogs

import android.content.Context
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.sourceforge.gokigen.memoma.R

class CreditDialog(private val myContext: Context) : DialogFragment()
{
    fun show()
    {
        val alertDialog = AlertDialog.Builder(myContext)
        alertDialog.setIcon(R.drawable.icon)
        alertDialog.setTitle(myContext.getString(R.string.app_name))
        alertDialog.setMessage(myContext.getString(R.string.app_credit))
        alertDialog.setCancelable(true)
        val alert = alertDialog.create()
        alert.show()
        (alert.findViewById<TextView>(android.R.id.message))?.movementMethod =
            LinkMovementMethod.getInstance()
    }

    companion object
    {
        fun newInstance(context: Context): CreditDialog
        {
            return (CreditDialog(context))
        }
    }
}
