package jp.sourceforge.gokigen.memoma.holders;

import java.io.File;
import java.io.FilenameFilter;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 *    めもまのデータファイル名を保持するクラス　（ArrayAdapterを拡張）
 * 
 * @author MRSa
 *
 */
public class MeMoMaDataFileHolder extends ArrayAdapter<String> implements FilenameFilter
{
	private final String TAG = toString();
	private final Context context;
	private final String fileExtension;

	/**
	 *    コンストラクタ
	 * 
	 */
    public MeMoMaDataFileHolder(Context context, int textViewRscId, String extension)
    {
    	super(context, textViewRscId);
		this.context = context;
		fileExtension = extension;
    }
    
    /**
     *    ファイル一覧を生成する。
     * 
     */
    public int updateFileList(String currentFileName)
    {
		int matchedIndex = 0;
    	int outputIndex = -1;
    	clear();
    	String[] dirFileList = context.fileList();
		try
		{
			for (String fileName : dirFileList)
			{
				int position = fileName.indexOf(fileExtension);
				if (position >= 0)
				{
					String fileBaseName = fileName.substring(0, position);
					if (fileBaseName.contentEquals(currentFileName))
					{
						// 選択したインデックスを設定する。
						outputIndex = matchedIndex;
					}
					add(fileBaseName);
					matchedIndex++;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (getCount() == 0)
		{
			add("No Title");
			outputIndex = 0;
		}
    	System.gc();
    	
    	Log.v(TAG, "::::::: "  + " (" + currentFileName + ") : " + outputIndex + " <" + getCount() + ">");
    	return (outputIndex);
    }

    /**
     *    受け付けるファイル名のフィルタを応答する。
     *    (指定された拡張子を持つなファイルだけ抽出する。)
     */
    public boolean accept(File dir, String filename)
    {
    	return (filename.endsWith(fileExtension));
    }
}
