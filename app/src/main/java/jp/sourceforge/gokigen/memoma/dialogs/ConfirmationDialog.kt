package jp.sourceforge.gokigen.memoma.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import jp.sourceforge.gokigen.memoma.R;

/**
 *   はい か いいえ を入力するダイアログを準備する
 * 
 * @author MRSa
 *
 */
public class ConfirmationDialog
{
	private final Context context;
	private IResultReceiver resultReceiver = null;
    private String  message = "";
	private String  title = "";
	private int    icon = 0;

	public ConfirmationDialog(Context arg)
	{
		context = arg;
	}

	/**
	 *  クラスの準備
     *
	 */
	public void prepare(IResultReceiver receiver, int titleIcon, String titleMessage, String confirmMessage)
	{
		if (receiver != null)
		{
			resultReceiver = receiver;
		}
		icon = titleIcon;
		title = titleMessage;
        message = confirmMessage;		
	}

    /**
     *   確認ダイアログを応答する
     *
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	if (inflater == null)
        {
            return (null);
        }
        final View layout = inflater.inflate(R.layout.confirmationdialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final TextView  textView = layout.findViewById(R.id.confirm_message);

        // 表示するデータ（アイコン、ダイアログタイトル、メッセージ）を準備する
        if (icon != 0)
        {
            builder.setIcon(icon);
        }
        if (title != null)
        {
            builder.setTitle(title);
        }
        if (message != null)
        {
        	textView.setText(message);
        }
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.confirmYes), (dialog, id) -> {
            if (resultReceiver != null)
            {
                resultReceiver.acceptConfirmation();
            }
            dialog.cancel();
            System.gc();
        });
        builder.setNegativeButton(context.getString(R.string.confirmNo), (dialog, id) -> {
            if (resultReceiver != null)
            {
                resultReceiver.rejectConfirmation();
            }
            dialog.cancel();
            System.gc();
        });
        return (builder.create());    	
    }

    public interface IResultReceiver
    {
        void acceptConfirmation();
        void rejectConfirmation();
    }
}
