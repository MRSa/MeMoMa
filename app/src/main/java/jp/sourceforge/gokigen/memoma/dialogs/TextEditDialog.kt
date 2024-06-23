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
    private var editTextString: String = ""
    private var isSingleLine = false

    fun prepare(
        receiver: ITextEditResultReceiver,
        titleMessage: String?,
        initialMessage: String?,
        isSingleLine: Boolean
    )
    {
        try
        {
            this.resultReceiver = receiver
            this.isSingleLine = isSingleLine  // 1行表示かどうか
            this.title = titleMessage ?: "" // ダイアログのタイトル
            this.editTextString = initialMessage ?: "" // テキスト入力エリアの文字
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * テキスト編集ダイアログを応答する
     */
    override fun getDialog(): AlertDialog
    {
        val builder = AlertDialog.Builder(parent)
        try
        {
            val inflater = parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout = inflater.inflate(R.layout.messagedialog, null)

            val editComment = layout.findViewById<View>(R.id.editTextArea) as TextView
            editComment.text = editTextString
            editComment.isSingleLine = isSingleLine

            // 表示するデータ（アイコン、ダイアログタイトル、メッセージ）を準備する
            if (titleIcon != 0) {
                builder.setIcon(titleIcon)
            }
            builder.setView(layout)
            builder.setTitle(title)
            builder.setCancelable(false)
            builder.setPositiveButton(
                parent.getString(R.string.confirmYes)
            ) { dialog, _ ->
                resultReceiver.finishTextEditDialog(editComment.text.toString())
                Log.v(TAG, "$title --- ENTER TEXT : ${editComment.text}")
                dialog.dismiss()
                System.gc()
            }
            builder.setNegativeButton(parent.getString(R.string.confirmNo))
            { dialog, _ ->
                resultReceiver.cancelTextEditDialog()
                dialog.cancel()
                System.gc()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
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
