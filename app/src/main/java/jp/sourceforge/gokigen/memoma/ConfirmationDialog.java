package jp.sourceforge.gokigen.memoma;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 *   はい か いいえ を入力するダイアログを準備する
 * 
 * @author MRSa
 *
 */
public class ConfirmationDialog
{
	private Context context = null;
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
	 * @param receiver
	 * @param initialMessage
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
     * @return
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.confirmationdialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final TextView  textView = (TextView)  layout.findViewById(R.id.confirm_message);

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
        builder.setPositiveButton(context.getString(R.string.confirmYes), new DialogInterface.OnClickListener()
              {
                   public void onClick(DialogInterface dialog, int id)
                   {
                	   boolean ret = false;
                	   if (resultReceiver != null)
                	   {
                	       resultReceiver.acceptConfirmation();
                	   }
                       if (ret == true)
                       {
                    	   dialog.dismiss();
                       }
                       else
                       {
                           dialog.cancel();
                       }
                       System.gc();
                   }
               });
        builder.setNegativeButton(context.getString(R.string.confirmNo), new DialogInterface.OnClickListener()
               {
                   public void onClick(DialogInterface dialog, int id)
                   {
                	   boolean ret = false;
                	   if (resultReceiver != null)
                	   {
                	       resultReceiver.rejectConfirmation();
                	   }
                       if (ret == true)
                       {
                    	   dialog.dismiss();
                       }
                       else
                       {
                           dialog.cancel();
                       }
                       System.gc();
                   }
               });
        return (builder.create());    	
    }

    public interface IResultReceiver
    {
        public abstract void acceptConfirmation();
        public abstract void rejectConfirmation();
    }
}
