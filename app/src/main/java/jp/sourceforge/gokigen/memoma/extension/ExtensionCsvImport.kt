package jp.sourceforge.gokigen.memoma.extension

import android.content.Context
import android.net.Uri
import android.util.Log
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class ExtensionCsvImport(private val context: Context, private val objectHolder: MeMoMaObjectHolder, private val importUri: Uri)
{
    fun importFromCsvFile()
    {
        try
        {
            val inputStream: InputStream? = context.contentResolver.openInputStream(importUri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var dataLine: String? = readRecord(reader)
            while (dataLine != null)
            {
                if (dataLine.startsWith(";") != true)
                {
                    // データ行だった。ログに出力する！
                    parseRecord(dataLine, objectHolder)
                }
                // 次のデータ行を読み出す
                dataLine = readRecord(reader)
            }
            inputStream?.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun readRecord(buf: BufferedReader): String?
    {
        var oneRecord: String? = null
        try
        {
            var oneLine = buf.readLine()
            while (oneLine != null)
            {
                oneRecord = if (oneRecord == null) oneLine else oneRecord + oneLine
                if (oneRecord.indexOf(",;!<_$") > 0)
                {
                    // レコード末尾が見つかったので break する。
                    break
                }
                // 次の行を読みだす。
                oneLine = buf.readLine()
            }
        }
        catch (ex: java.lang.Exception)
        {
            //
            Log.v(TAG, "CSV:readRecord() ex : $ex")
            oneRecord = null
        }
        return (oneRecord)
    }
    private fun parseRecord(dataLine: String, objectHolder: MeMoMaObjectHolder)
    {
        val detailIndex: Int
        val userCheckIndexTrue: Int
        val userCheckIndexFalse: Int
        val nextIndex: Int
        val label: String
        val detail: String
        val userChecked: Boolean
        try
        {
            detailIndex = dataLine.indexOf("\",\"")
            if (detailIndex < 0)
            {
                Log.v(TAG, "parseRecord() : label wrong : $dataLine")
                return
            }
            label = dataLine.substring(1, detailIndex)
            userCheckIndexTrue = dataLine.indexOf("\",True,", detailIndex)
            userCheckIndexFalse = dataLine.indexOf("\",False,", detailIndex)
            if (userCheckIndexFalse > detailIndex)
            {
                detail = dataLine.substring(detailIndex + 3, userCheckIndexFalse)
                userChecked = false
                nextIndex = userCheckIndexFalse + 8 // 8は、 ",False, を足した数
            }
            else if (userCheckIndexTrue > detailIndex)
            {
                detail = dataLine.substring(detailIndex + 3, userCheckIndexTrue)
                userChecked = true
                nextIndex = userCheckIndexTrue + 7 // 7は、 ",True,  を足した数
            }
            else  // if ((userCheckIndexTrue <= detailIndex)&&(userCheckIndexFalse <= detailIndex))
            {
                Log.v(TAG, "parseRecord() : detail wrong : $dataLine")
                return
            }

            //  残りのデータを切り出す。
            val datas =
                dataLine.substring(nextIndex).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (datas.size < 6)
            {
                Log.v(TAG, "parseRecord() : data size wrong : " + datas.size)
                return
            }
            val drawStyle = datas[0].toInt()
            val paintStyle = datas[1]
            val centerX = datas[2].toFloat()
            val centerY = datas[3].toFloat()
            val width = datas[4].toFloat()
            val height = datas[5].toFloat()
            val left = centerX - width / 2.0f
            val top = centerY - height / 2.0f

            // オブジェクトのデータを作成する
            val pos = objectHolder.createPosition(left, top, drawStyle)
            if (pos == null)
            {
                Log.v(TAG, "parseRecord() : object create failure.")
                return
            }
            pos.setRectRight(left + width)
            pos.setRectBottom(top + height)
            pos.label = label
            pos.detail = detail
            pos.paintStyle = paintStyle
            pos.userChecked = userChecked
            Log.v(TAG, "OBJECT CREATED: $label($left,$top) [$drawStyle]")
        }
        catch (ex: Exception)
        {
            Log.v(TAG, "parseRecord() $ex")
            ex.printStackTrace()
        }
    }

    companion object {
        private val TAG = ExtensionCsvImport::class.java.simpleName
    }

}