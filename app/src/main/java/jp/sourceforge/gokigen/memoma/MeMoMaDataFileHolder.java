package jp.sourceforge.gokigen.memoma;

import java.io.File;
import java.io.FilenameFilter;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 *    めもまのデータファイル名を保持するクラス　（ArrayAdapterを拡張）
 * 
 * @author MRSa
 *
 */
public class MeMoMaDataFileHolder extends ArrayAdapter<String> implements FilenameFilter
{
	private ExternalStorageFileUtility fileUtility = null;
	private String fileExtension = "";

	/**
	 *    コンストラクタ
	 * 
	 */
    public MeMoMaDataFileHolder(Context context, int textViewRscId, ExternalStorageFileUtility utility, String extension)
    {
    	super(context, textViewRscId);
    	fileUtility = utility;
    	fileExtension = extension;
    }
    
    /**
     *    ファイル一覧を生成する。
     * 
     */
    public int updateFileList(String currentFileName, String extendDirectory)
    {
    	int outputIndex = -1;
    	
    	clear();
        String directory = fileUtility.getGokigenDirectory();
        if (extendDirectory != null)
        {
        	// ディレクトリが指定されていた時には、そのディレクトリを追加する
        	directory = directory + extendDirectory;
        }
    	String[] dirFileList = (new File(directory)).list(this);
        try {
			for (int index = 0; index < dirFileList.length; index++) {
				String fileName = dirFileList[index].substring(0, dirFileList[index].indexOf(fileExtension));
				if (fileName.contentEquals(currentFileName) == true)  // ファイル先頭にない場合は追加する。
				{
					// 選択したインデックスを設定する。
					outputIndex = index;
				}
				add(fileName);
				//Log.v(Main.APP_IDENTIFIER, fileName + ", File : " + dirFileList[index]);
			}
		}
		catch (Exception e)
		{
				e.printStackTrace();
		}
    	System.gc();
    	
    	//Log.v(Main.APP_IDENTIFIER, "::::::: "  + " (" + currentFileName + ") : " + outputIndex);
    	return (outputIndex);
    }

    /**
     *    受け付けるファイル名のフィルタを応答する。
     *    (指定された拡張子を持つなファイルだけ抽出する。)
     * 
     */
    public boolean accept(File dir, String filename)
    {
    	return (filename.endsWith(fileExtension) ? true : false);
    }
}
