package jp.sourceforge.gokigen.memoma.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import jp.sourceforge.gokigen.memoma.fileio.ExternalStorageFileUtility;
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
	private Context context = null;
	private IResultReceiver resultReceiver = null;
	private ExternalStorageFileUtility fileUtility = null;
    private MeMoMaDataFileHolder dataFileHolder = null;
    private String title = null;
    private String fileExtension = null;
    private Dialog dialogRef = null;
    
	/**
	 *    コンストラクタ
	 * 
	 * @param arg
	 */
	public FileSelectionDialog(Context arg, String titleMessage, ExternalStorageFileUtility utility, String extension, IResultReceiver receiver)
	{
	    context = arg;	
	    resultReceiver = receiver;
		title = titleMessage;
        fileUtility = utility;
        fileExtension = extension;
        dataFileHolder = new MeMoMaDataFileHolder(context, android.R.layout.simple_list_item_1, fileUtility, extension);
	}

	/**
	 *   ファイル一覧データをつくる！
	 * 
	 * @param currentFileName
	 * @param extendDirectory
	 */
	public void prepare(String currentFileName, String extendDirectory)
	{
		dataFileHolder.updateFileList(currentFileName, extendDirectory);
	}

    /**
     *   ファイル選択ダイアログを応答する
     *   
     * @return
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.listdialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        final ListView  listView = (ListView)  layout.findViewById(R.id.ListDataFileName);
        listView.setAdapter(dataFileHolder);

        // 表示するデータ（ダイアログタイトル）を準備する
        if (title != null)
        {
            builder.setTitle(title);
        }
        builder.setView(layout);

        // アイテムを選択したときの処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            //@Override
            public void onItemClick(AdapterView<?> parentView, View view, int position, long id)
            {
                ListView listView = (ListView) parentView;
                String fileName = (String) listView.getItemAtPosition(position);

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
            }
        });
        builder.setCancelable(true);
        builder.setNegativeButton(context.getString(R.string.confirmNo), new DialogInterface.OnClickListener()
               {
                   public void onClick(DialogInterface dialog, int id)
                   {
                       dialog.cancel();
                       System.gc();
                   }
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
        public abstract void selectedFileName(String fileName);
    }	
}
