package jp.sourceforge.gokigen.memoma.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import jp.sourceforge.gokigen.memoma.R;

/**
 *  テキスト編集のダイアログ
 * 
 * @author MRSa
 *
 */
public class TextEditDialog
{
    private final String TAG = toString();
	private final Context context;
	private ITextEditResultReceiver resultReceiver = null;
	private final int    icon;
	private String title = null;

	/**
	 *   コンストラクタ
	 */
	public TextEditDialog(Context arg, int titleIcon)
	{
		context = arg;
		icon = titleIcon;
	}

	/**
	 *  クラスの準備
	 */
	public void prepare(Dialog layout, ITextEditResultReceiver receiver, String titleMessage, String initialMessage, boolean isSingleLine)
	{
		if (receiver != null)
		{
			resultReceiver = receiver;
		}
        try
        {
            final TextView  editComment = (TextView)  layout.findViewById(R.id.editTextArea);
            if (titleMessage != null)
            {
                layout.setTitle(titleMessage);
                title = titleMessage;
            }

            // テキスト入力エリアの文字を設定する
            if (initialMessage != null)
            {
                editComment.setText(initialMessage);
            }
            else
            {
                editComment.setText("");
            }

            // 入力領域の行数を更新する
            editComment.setSingleLine(isSingleLine);
        }
        catch (Exception ex)
        {
        	// ログだけ吐いて、何もしない
        	Log.v(TAG, "TextEditDialog::prepare() " + ex.toString());
        }
	}
	
    /**
     *   テキスト編集ダイアログを応答する
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.messagedialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final TextView  editComment = (TextView)  layout.findViewById(R.id.editTextArea);

        // 表示するデータ（アイコン、ダイアログタイトル、メッセージ）を準備する
        if (icon != 0)
        {
            builder.setIcon(icon);
        }
        if (title != null)
        {
        	builder.setTitle(title);
        }

        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.confirmYes), (dialog, id) -> {
            if (resultReceiver != null)
            {
                resultReceiver.finishTextEditDialog(editComment.getText().toString());
            }
            dialog.cancel();
            System.gc();
        });
        builder.setNegativeButton(context.getString(R.string.confirmNo), (dialog, id) -> {
            if (resultReceiver != null)
            {
                resultReceiver.cancelTextEditDialog();
            }
            dialog.cancel();
            System.gc();
        });
        return (builder.create());    	
    }

    public interface ITextEditResultReceiver
    {
        void finishTextEditDialog(String message);
        void cancelTextEditDialog();
    }
}
