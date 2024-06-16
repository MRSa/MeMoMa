package jp.sourceforge.gokigen.memoma.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import jp.sourceforge.gokigen.memoma.R;


/**
 *   アイテムを選択するダイアログを準備する
 * 
 * @author MRSa
 *
 */
public class ItemSelectionDialog
{
	private final Context context;
	private ISelectionItemReceiver resultReceiver = null;
	private ISelectionItemHolder dataHolder = null;
	private String  title = "";

	public ItemSelectionDialog(Context arg)
	{
		context = arg;
	}

	/**
	 *  クラスの準備
     *
	 */
	public void prepare(ISelectionItemReceiver receiver, ISelectionItemHolder holder,  String titleMessage)
	{
		title = titleMessage;
		resultReceiver = receiver;
		dataHolder = holder;
	}
	
	/**
     *   確認ダイアログを応答する
     */
    public Dialog getDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        // 表示するデータ（ダイアログタイトル、メッセージ）を準備する
        if (title != null)
        {
            builder.setTitle(title);
        }
        builder.setCancelable(false);
        if (dataHolder != null)
        {
        	if (!dataHolder.isMultipleSelection())
        	{
                builder.setItems(dataHolder.getItems(), (dialog, id) -> {
                   if (resultReceiver != null)
                   {
                       resultReceiver.itemSelected(id, dataHolder.getItem(id));
                   }
                   dialog.cancel();
                   System.gc();
                });
        	}
            else
            {
            	//  複数選択の選択肢を準備する
                builder.setMultiChoiceItems(dataHolder.getItems(), dataHolder.getSelectionStatus(), (dialog, which, isChecked) -> {
                      if (resultReceiver != null)
                    {
                        resultReceiver.itemSelected(which, dataHolder.getItem(which));
                    }
                });

                //  複数選択時には、OKボタンを押したときに選択を確定させる。
                builder.setPositiveButton(context.getString(R.string.confirmYes), (dialog, id) -> {
                    if (resultReceiver != null)
                    {
                        resultReceiver.itemSelectedMulti(dataHolder.getItems(), dataHolder.getSelectionStatus());
                    }
                    dialog.cancel();
                    System.gc();
                });
            }
        }

        builder.setNegativeButton(context.getString(R.string.confirmNo), (dialog, id) -> {
            if (resultReceiver != null)
            {
                resultReceiver.canceledSelection();
            }
            dialog.cancel();
            System.gc();
        });
        return (builder.create());    	
    }

    public interface ISelectionItemHolder
    {
    	boolean isMultipleSelection();

    	String[] getItems();
        String  getItem(int index);

        /** 複数選択時に使用する **/
        boolean[] getSelectionStatus();
        void setSelectionStatus(int index, boolean isSelected);
    }
    
    public interface ISelectionItemReceiver
    {
        void itemSelected(int index, String itemValue);
        void itemSelectedMulti(String[] items, boolean[] status);
        void canceledSelection();
    }
}
