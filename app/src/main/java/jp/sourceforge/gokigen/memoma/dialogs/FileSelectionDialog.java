package jp.sourceforge.gokigen.memoma.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import jp.sourceforge.gokigen.memoma.holders.MeMoMaDataFileHolder;
import jp.sourceforge.gokigen.memoma.R;

/**
 *    ファイル選択ダイアログ
 * 
 * @author MRSa
 *
 */
public class FileSelectionDialog
{
	private final Context context;
	private final IResultReceiver resultReceiver;
    private final MeMoMaDataFileHolder dataFileHolder;
    private final String title;
    private final String fileExtension;
    private Dialog dialogRef;
    
	/**
	 *    コンストラクタ
	 *
	 */
	public FileSelectionDialog(Context arg, String titleMessage, String extension, IResultReceiver receiver)
    {
        context = arg;
        resultReceiver = receiver;
        title = titleMessage;
        fileExtension = extension;
        dataFileHolder = new MeMoMaDataFileHolder(context, android.R.layout.simple_list_item_1, extension);
    }

	/**
	 *   ファイル一覧データをつくる！
	 *
	 */
	public void prepare()
	{
		dataFileHolder.updateFileList("");
	}

    /**
     *   ファイル選択ダイアログを応答する
     *
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.listdialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        final ListView  listView = layout.findViewById(R.id.ListDataFileName);
        listView.setAdapter(dataFileHolder);

        // 表示するデータ（ダイアログタイトル）を準備する
        if (title != null)
        {
            builder.setTitle(title);
        }
        builder.setView(layout);

        // アイテムを選択したときの処理
        listView.setOnItemClickListener((parentView, view, position, id) -> {
            ListView listView1 = (ListView) parentView;
            String fileName = (String) listView1.getItemAtPosition(position);

            /// リストが選択されたときの処理...データを開く
            if (resultReceiver != null)
           {
               resultReceiver.selectedFileName(fileName + fileExtension);
           }
            if (dialogRef != null)
            {
                dialogRef.dismiss();
               dialogRef = null;
            }
           System.gc();
        });
        builder.setCancelable(true);
        builder.setNegativeButton(context.getString(R.string.confirmNo), (dialog, id) -> {
            dialog.cancel();
            System.gc();
        });
        dialogRef = builder.create();
        return (dialogRef);    	
    }

    /**
     *   ファイルダイアログのインタフェース
     *   
     * @author MRSa
     *
     */
    public interface IResultReceiver
    {
    	/**
    	 *    ファイルが選択された！
    	 *    
    	 */
        void selectedFileName(String fileName);
    }	
}
