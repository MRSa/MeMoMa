package jp.sourceforge.gokigen.memoma.dialogs

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.sourceforge.gokigen.memoma.R

class TextEditDialog(private val parent: Context, private val titleIcon: Int) : DialogFragment()
{
    private lateinit var resultReceiver: ITextEditResultReceiver
    private var title: String = ""

    fun prepare(
        layout: AlertDialog,
        receiver: ITextEditResultReceiver,
        titleMessage: String?,
        initialMessage: String?,
        isSingleLine: Boolean
    )
    {
        resultReceiver = receiver

        try
        {
            val editComment = layout.findViewById<View>(R.id.editTextArea) as TextView
            if (titleMessage != null)
            {
                layout.setTitle(titleMessage)
                title = titleMessage
            }

            // テキスト入力エリアの文字を設定する
            if (initialMessage != null)
            {
                editComment.text = initialMessage
            }
            else
            {
                editComment.text = parent.getString(R.string.blank)
            }

            // 入力領域の行数を更新する
            editComment.isSingleLine = isSingleLine
        }
        catch (ex: Exception)
        {
            // ログだけ吐いて、何もしない
            ex.printStackTrace()
        }
    }

    /**
     * テキスト編集ダイアログを応答する
     */
    override fun getDialog(): AlertDialog
    {
        val inflater =
            parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.messagedialog, null)
        val builder = AlertDialog.Builder(parent)
        val editComment = layout.findViewById<View>(R.id.editTextArea) as TextView

        // 表示するデータ（アイコン、ダイアログタイトル、メッセージ）を準備する
        if (titleIcon != 0) {
            builder.setIcon(titleIcon)
        }
        builder.setTitle(title)
        builder.setView(layout)
        builder.setCancelable(false)
        builder.setPositiveButton(
            parent.getString(R.string.confirmYes)
        ) { dialog, _ ->
            resultReceiver.finishTextEditDialog(editComment.text.toString())
            Log.v(TAG, "ENTER TEXT : ${editComment.text}")
            dialog.dismiss()
            System.gc()
        }
        builder.setNegativeButton(parent.getString(R.string.confirmNo))
        { dialog, _ ->
            resultReceiver.cancelTextEditDialog()
            dialog.cancel()
            System.gc()
        }
        return (builder.create())
    }

    interface ITextEditResultReceiver
    {
        fun finishTextEditDialog(message: String)
        fun cancelTextEditDialog()
    }

    companion object
    {
        private val TAG = TextEditDialog::class.java.simpleName
        fun newInstance(context: Context, iconResId: Int): TextEditDialog
        {
            return (TextEditDialog(context, iconResId))
        }
    }
}
