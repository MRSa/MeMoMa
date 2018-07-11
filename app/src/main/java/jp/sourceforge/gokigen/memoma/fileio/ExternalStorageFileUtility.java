package jp.sourceforge.gokigen.memoma.fileio;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import jp.sourceforge.gokigen.memoma.Main;

/**
 *  外部ストレージにデータを記録するために使うユーティリティ
 *  
 * @author MRSa
 */
public class ExternalStorageFileUtility 
{
	private final int COPY_BUFFER_SIZE = 32768;
	private final int BUFFER_MARGIN    = 4;

	private String baseDirectory = "/";

    /**
     *   コンストラクタ
     * 
     */
    public ExternalStorageFileUtility(String offsetDir)
    {
        /** ベースディレクトリの作成 (あれば何もしない) **/
        prepareBaseDirectory(offsetDir);
    }
    
    /**
     *   ベースディレクトリの決定を行う
     * 
     */
    private boolean prepareBaseDirectory(String offsetDir)
    {
        String gokigenDirectory = Environment.getExternalStorageDirectory().getPath() + "/Gokigen";
        try
        {
            File baseDir = new File(gokigenDirectory);
            if (baseDir.exists() == false)
            {
                if (baseDir.mkdirs() == false)
                {
                    // ベースディレクトリ作成失敗...終了する
                    baseDirectory = Environment.getExternalStorageDirectory().getPath();
                    baseDir = null;
                    return (false);
                }
            }
            gokigenDirectory = gokigenDirectory + offsetDir;
            baseDir = null;
            baseDir = new File(gokigenDirectory);
            if (baseDir.exists() == false)
            {
                if (baseDir.mkdirs() == false)
                {
                    // ベースディレクトリ作成失敗...終了する
                    baseDirectory = Environment.getExternalStorageDirectory().getPath() + "/Gokigen";
                    baseDir = null;
                    return (false);                    
                }
            }
            baseDirectory = gokigenDirectory;
            return (true);
        }
        catch (Exception ex)
        {
            Log.v(Main.APP_IDENTIFIER, "prepareBaseDirectory() : " + ex.getMessage());
        }
        baseDirectory = Environment.getExternalStorageDirectory().getPath();
        return (false);
    }

    /**
     *  ベースディレクトリを取得する
     * @return  書き込みを行うアプリ用ベースディレクトリ
     */
    public String getGokigenDirectory()
    {
        return (baseDirectory);
    }    
    
    /**
     *  ディレクトリを作成する
     * 
     * @param dirName
     * @return
     */
    public boolean makeDirectory(String dirName)
    {
        String makeDir = baseDirectory + "/" + dirName;
        try
        {
            File dir = new File(makeDir);
            if (dir.exists() == false)
            {
                return (dir.mkdirs());
            }
            return (true);
        }
        catch (Exception ex)
        {
            Log.v(Main.APP_IDENTIFIER, "makeDirectory() : " + ex.getMessage());
        }
        return (false);
    }

    /**
     *  記録ファイルをオープンする    
     * @param fileName  ファイル名
     * @param isAppend  追記モードでオープンするか？
     * @return  ファイルストリーム
     */
    public FileOutputStream openFileStream(String fileName, boolean isAppend)
    {
        try
        {
            String targetName = baseDirectory + "/" + fileName;
            FileOutputStream fileStream = new FileOutputStream(targetName, isAppend);
            return (fileStream);
        }
        catch (Exception e)
        {
            Log.v(Main.APP_IDENTIFIER, "openFileStream() : " + e.getMessage());
        }
        return (null);
    }    

    /**
     *   日単位でディレクトリを作成し、そこに書き込むためのファイル名を決定する
     * @return ファイル名称
     */
    private String decideFileNameWithSpecifiedDate(String fileName, String year, String month, String date)
    {
        String directory = year;
        
        // 年のディレクトリを掘る
        makeDirectory(year);

        directory = directory + "/" + year + month;

        // 年月のディレクトリを掘る
        makeDirectory(directory);

        directory = directory + "/" + year + month + date;

        // 年月日のディレクトリを掘る
        makeDirectory(directory);
        
        return (directory + "/" + year + month + date + "-" + fileName);
    }

    /**
     *   日単位でディレクトリを作成し、そこに書き込むためのファイル名を決定する
     * @return ファイル名称
     */
    public String decideFileNameWithDate(String fileName)
    {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");

        String year  = yearFormat.format(calendar.getTime());
        String month = monthFormat.format(calendar.getTime());
        String date  = dateFormat.format(calendar.getTime());

        return (decideFileNameWithSpecifiedDate(fileName, year, month, date));
    }

    /**
     *   日単位でディレクトリを作成し、そこに書き込むためのファイル名を決定する
     * @return ファイル名称
     */
    public String decideFileNameWithSpecifiedDate(String fileName, int year, int month, int day)
    {
        String yearStr = year + "";
        String monthStr = "";
        if (month < 10)
        {
            monthStr = "0" + month;	
        }
        else
        {
        	monthStr = month + "";
        }
        String dayStr = "";
        if (day < 10)
        {
            dayStr = "0" + day;
        }
        else
        {
        	dayStr = day + "";
        }
        return (decideFileNameWithSpecifiedDate(fileName, yearStr, monthStr, dayStr));
    }

    /**
     *  日付ディレクトリ名を取得する
     * @param year
     * @param month
     * @param date
     * @return
     */
    public String decideDateDirectory(int year, int month, int date)
    {
        String addMonth = "";
        String addDate  = "";
        String directory = baseDirectory + "/" + year + "/";
        if (month < 10)
        {
            addMonth =  "0";
        }
        if (date < 10)
        {
            addDate = "0";
        }
        directory = directory + year + addMonth + month + "/" + year + addMonth + month + addDate + date + "/";        
        return (directory);
    }

    /**
     * ファイルのコピー (kaniFilerから持ってきた...)
     * 
     * @param destFileName コピー先ファイル (full path)
     * @param srcFileName  コピー元ファイル (full path)
     */
	public boolean copyFile(String destFileName, String srcFileName)
	{
		File srcFile = null;
		File dstFile = null;
		
		boolean     ret = false;
		InputStream   is = null;
		OutputStream  os = null;

		if (destFileName == srcFileName)
		{
			// ファイル名が同じだった場合にはコピーを実行しない
			return (false);
		}
		
		try
		{
			srcFile = new File(srcFileName);
			if (srcFile.exists() != true)
			{
				// ファイルが存在しなかった、、、終了する
				return (false);
			}
			is = new FileInputStream(srcFile);

			long dataFileSize = srcFile.length();
			byte[] buffer = new byte[COPY_BUFFER_SIZE + BUFFER_MARGIN];

			dstFile = new File(destFileName);
			if (dstFile.exists() == true)
			{
				// ファイルが存在した、、、削除して作り直す
				dstFile.delete();
			}

			os = new FileOutputStream(dstFile);
			if ((is != null)&&(os != null))
			{
				while (dataFileSize > 0)
				{
			        int size = is.read(buffer, 0, COPY_BUFFER_SIZE);
			        if (size <= 0)
			        {
			        	break;
			        }
			        os.write(buffer, 0, size);
				}
			}
			os.flush();
			os.close();
			is.close();
			
			dstFile = null;
			srcFile = null;
			buffer = null;
			is = null;
			os = null;
			ret = true;
			System.gc();
		}
		catch (Exception e)
		{
			// 例外発生！！！
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception e2)
			{
				//
			}
				
			try
			{
				if (os != null)
				{
					os.close();
				}
			}
			catch (Exception e2)
			{
				//
			}
			is = null;
			os = null;
			srcFile = null;
			dstFile = null;
			System.gc();

			return (false);
		}
		return (ret);
	}
}
