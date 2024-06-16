package jp.sourceforge.gokigen.memoma.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaDataFileHolder

/**
 * ファイル選択ダイアログ
 */
class FileSelectionDialog(private val parent: Context, private val titleMessage: String, private val extension: String, private val receiver: IResultReceiver): DialogFragment()
{
    private val dataFileHolder = MeMoMaDataFileHolder(parent, android.R.layout.simple_list_item_1, extension)
    private lateinit var dialogRef: AlertDialog

    fun prepare()
    {
        dataFileHolder.updateFileList("")
    }

    override fun getDialog(): AlertDialog?
    {
        try
        {
            val inflater =
                parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val layout: View = inflater.inflate(R.layout.listdialog, null)
            val builder = AlertDialog.Builder(parent)
            val listView = layout.findViewById<ListView>(R.id.ListDataFileName)
            listView.adapter = dataFileHolder

            // 表示するデータ（ダイアログタイトル）を準備する
            builder.setTitle(titleMessage)
            builder.setView(layout)

            // アイテムを選択したときの処理
            listView.onItemClickListener =
                OnItemClickListener { parentView: AdapterView<*>, _: View?, position: Int, _: Long ->
                    val listView1 = parentView as ListView
                    val fileName = listView1.getItemAtPosition(position) as String

                    /// リストが選択されたときの処理...データを開く
                    receiver.selectedFileName(fileName + extension)

                    if (::dialogRef.isInitialized)
                    {
                        dialogRef.dismiss()
                    }
                    System.gc()
                }
            builder.setCancelable(true)
            builder.setNegativeButton(
                parent.getString(R.string.confirmNo)
            ) { dialog, _ ->
                dialog.cancel()
                System.gc()
            }
            dialogRef = builder.create()
            return (dialogRef)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (null)
    }

    /**
     * ファイルダイアログのインタフェース
     */
    interface IResultReceiver
    {
        fun selectedFileName(fileName: String?)
    }
    companion object
    {
        fun newInstance(context: Context, titleMessage: String, extension: String, receiver: IResultReceiver): FileSelectionDialog
        {
            return (FileSelectionDialog(context, titleMessage, extension, receiver))
        }
    }
}
