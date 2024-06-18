package jp.sourceforge.gokigen.memoma.holders

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import java.io.File
import java.io.FilenameFilter

/**
 * めもまのデータファイル名を保持するクラス　（ArrayAdapterを拡張）
 */
class MeMoMaDataFileHolder(private val context: Context, textViewRscId: Int, private val fileExtension: String) : ArrayAdapter<String?>(context, textViewRscId), FilenameFilter
{
    /**
     * ファイル一覧を生成する。
     *
     */
    fun updateFileList(currentFileName: String): Int
    {
        var outputIndex = -1
        try
        {
            var matchedIndex = 0
            clear()
            val dirFileList = context.fileList()
            for (fileName in dirFileList)
            {
                val position = fileName.indexOf(fileExtension)
                if (position >= 0)
                {
                    val fileBaseName = fileName.substring(0, position)
                    if (fileBaseName.contentEquals(currentFileName))
                    {
                        // 選択したインデックスを設定する。
                        outputIndex = matchedIndex
                    }
                    add(fileBaseName)
                    matchedIndex++
                }
            }
            if (count == 0)
            {
                add("No Title")
                outputIndex = 0
            }
            System.gc()
            Log.v(TAG, ":::::::  ($currentFileName) : $outputIndex <$count>")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (outputIndex)
    }

    /**
     * 受け付けるファイル名のフィルタを応答する。
     * (指定された拡張子を持つなファイルだけ抽出する。)
     */
    override fun accept(dir: File, filename: String): Boolean
    {
        return (filename.endsWith(fileExtension))
    }

    companion object
    {
        private val TAG = MeMoMaDataFileHolder::class.java.simpleName
    }
}
