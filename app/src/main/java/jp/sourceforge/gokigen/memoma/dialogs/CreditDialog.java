package jp.sourceforge.gokigen.memoma.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import jp.sourceforge.gokigen.memoma.R;

/**
 *  クレジットを表示する
 * 
 * @author MRSa
 *
 */
public class CreditDialog
{
	private Activity context;

	/**
	 *   コンストラクタ
	 *
	 */
	public CreditDialog(Activity arg)
	{
		context = arg;
	}

    /**
     *   ダイアログを応答する
     *
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	if (inflater == null)
		{
			return (null);
		}
    	View layout = inflater.inflate(R.layout.creditdialog, null);  //  ?? http://www.mail-archive.com/android-developers@googlegroups.com/msg162003.html より
    	//View layout = inflater.inflate(R.layout.creditdialog,  context.findViewById(R.id.layout_root));

    	TextView text = layout.findViewById(R.id.creditmessage);
    	text.setText(context.getString(R.string.app_credit));
 //   	ImageView image = (ImageView) layout.findViewById(R.id.crediticon);
 //   	image.setImageResource(R.drawable.icon);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.app_name));
        builder.setIcon(R.drawable.icon);
        builder.setView(layout);
        builder.setCancelable(true);
        return (builder.create());
    }
}
