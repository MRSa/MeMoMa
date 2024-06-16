package jp.sourceforge.gokigen.memoma.dialogs

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import jp.sourceforge.gokigen.memoma.R

class ItemSelectionDialog(private val parent: Context) : DialogFragment()
{
    private lateinit var resultReceiver: ISelectionItemReceiver
    private lateinit var dataHolder: ISelectionItemHolder
    private lateinit var title : String

    fun prepare(
        receiver: ISelectionItemReceiver,
        holder: ISelectionItemHolder,
        titleMessage: String
    ) {
        title = titleMessage
        resultReceiver = receiver
        dataHolder = holder
    }

    override fun getDialog(): AlertDialog
    {
        val builder = AlertDialog.Builder(parent)

        // 表示するデータ（ダイアログタイトル、メッセージ）を準備する
        builder.setTitle(title)
        builder.setCancelable(false)
        if (!dataHolder.isMultipleSelection()) {
            builder.setItems(dataHolder.getItems()) { dialog, id ->
                resultReceiver.itemSelected(id, dataHolder.getItem(id))
                dialog.cancel()
                System.gc()
            }
        } else {
            //  複数選択の選択肢を準備する
            builder.setMultiChoiceItems(
                dataHolder.getItems(), dataHolder.getSelectionStatus()
            ) { _, which, _ ->
                resultReceiver.itemSelected(which, dataHolder.getItem(which))
            }

            //  複数選択時には、OKボタンを押したときに選択を確定させる。  感謝
            builder.setPositiveButton(
                parent.getString(R.string.confirmYes)
            ) { dialog, _ ->
                resultReceiver.itemSelectedMulti(
                    dataHolder.getItems(),
                    dataHolder.getSelectionStatus()
                )
                dialog.cancel()
                System.gc()
            }
        }

        builder.setNegativeButton(parent.getString(R.string.confirmNo)) { dialog, _ ->
            resultReceiver.canceledSelection()
            dialog.cancel()
            System.gc()
        }
        Log.v(TAG, "Create Multi-selection Dialog")
        return (builder.create())
    }


    interface ISelectionItemHolder
    {
        fun isMultipleSelection(): Boolean
        fun getItems() : Array<String?>
        fun getItem(index: Int): String
        fun getSelectionStatus(): BooleanArray
        fun setSelectionStatus(index: Int, isSelected: Boolean)
    }

    interface ISelectionItemReceiver
    {
        fun itemSelected(index: Int, itemValue: String)
        fun itemSelectedMulti(items: Array<String?>, status: BooleanArray)
        fun canceledSelection()
    }

    companion object
    {
        private val TAG = ItemSelectionDialog::class.java.simpleName
        fun newInstance(context: Context): ItemSelectionDialog
        {
            return (ItemSelectionDialog(context))
        }
    }
}