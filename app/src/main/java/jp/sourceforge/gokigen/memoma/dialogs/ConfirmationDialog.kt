package jp.sourceforge.gokigen.memoma.dialogs

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.sourceforge.gokigen.memoma.R

class ConfirmationDialog(private val myContext: Context) : DialogFragment()
{
    /**
     *    ダイアログを表示する (タイトルやメッセージはリソースからとってくる）
     */
    fun show(titleResId: Int, messageResId: Int, callback: ConfirmationCallback)
    {
        val title = myContext.getString(titleResId)
        val message = myContext.getString(messageResId)
        show(title, message, callback)
    }

    fun show(title: String?, message: String?, callback: ConfirmationCallback)
    {
        val alertDialog = AlertDialog.Builder(myContext)
        alertDialog.setTitle(title)
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert)
        alertDialog.setMessage(message)
        alertDialog.setCancelable(true)
        alertDialog.setPositiveButton(myContext.getString(R.string.confirmYes)) { dialog, _ ->
            callback.acceptConfirmation()
            dialog.dismiss() }

        alertDialog.setNegativeButton(myContext.getString(R.string.confirmNo)) { dialog, _ ->
            callback.cancelConfirmation()
            dialog.cancel() }
        alertDialog.show()
    }

    fun show(iconResId: Int, title: String?, message: String?)
    {
        // 表示イアログの生成
        val alertDialog = AlertDialog.Builder(myContext)
        alertDialog.setTitle(title)
        alertDialog.setIcon(iconResId)
        alertDialog.setMessage(message)
        alertDialog.setCancelable(true)

        // ボタンを設定する（実行ボタン）
        alertDialog.setPositiveButton(myContext.getString(R.string.confirmGo)) { dialog, _ -> dialog.dismiss() }

        // 確認ダイアログを表示する
        alertDialog.show()
    }

    // コールバックインタフェース
    interface ConfirmationCallback
    {
        fun acceptConfirmation()
        fun cancelConfirmation()
    }

    companion object
    {
        fun newInstance(context: Context): ConfirmationDialog
        {
            return (ConfirmationDialog(context))
        }
    }
}
