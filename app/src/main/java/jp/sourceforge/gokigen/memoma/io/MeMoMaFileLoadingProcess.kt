package jp.sourceforge.gokigen.memoma.io

import android.content.Context
import android.graphics.RectF
import android.util.Log
import android.util.Xml
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.holders.ObjectConnector
import jp.sourceforge.gokigen.memoma.holders.PositionObject
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileReader

/**
 * データをファイルに保存するとき用 アクセスラッパ (非同期処理を前提)
 * MeMoMaObjectHolder : 実行時に渡すクラス(Param)
 * Integer    : 途中経過を伝えるクラス(Progress)
 * String     : 処理結果を伝えるクラス(Result)
 */
class MeMoMaFileLoadingProcess(private val parent: Context, private val receiver: IResultReceiver)
{
    private var position: PositionObject? = null
    private var line: ObjectConnector? = null
    private var backgroundUri = ""
    private var userCheckboxString = ""

    private fun parseStartTag(name: String, parser: XmlPullParser, objectHolder: MeMoMaObjectHolder)
    {
        try
        {
            if ((name.equals("top", ignoreCase = true)) && (position != null)) {
                position?.setRectTop(parser.nextText().toFloat())
            } else if ((name.equals("bottom", ignoreCase = true)) && (position != null)) {
                position?.setRectBottom(parser.nextText().toFloat())
            } else if ((name.equals("left", ignoreCase = true)) && (position != null)) {
                position?.setRectLeft(parser.nextText().toFloat())
            } else if ((name.equals("right", ignoreCase = true)) && (position != null)) {
                position?.setRectRight(parser.nextText().toFloat())
            } else if ((name.equals("drawStyle", ignoreCase = true)) && (position != null)) {
                position?.setDrawStyle(parser.nextText().toInt())
            } else if ((name.equals("icon", ignoreCase = true)) && (position != null)) {
                position?.setIcon(parser.nextText().toInt())
            } else if ((name.equals("label", ignoreCase = true)) && (position != null)) {
                position?.setLabel(parser.nextText())
            } else if ((name.equals("detail", ignoreCase = true)) && (position != null)) {
                position?.setDetail(parser.nextText())
            } else if ((name.equals("userChecked", ignoreCase = true)) && (position != null)) {
                val parseData = parser.nextText()
                position?.setUserChecked(parseData.equals("true", ignoreCase = true))
            } else if ((name.equals("labelColor", ignoreCase = true)) && (position != null)) {
                position?.setLabelColor(parser.nextText().toInt())
            } else if ((name.equals("objectColor", ignoreCase = true)) && (position != null)) {
                position?.setObjectColor(parser.nextText().toInt())
            } else if ((name.equals("paintStyle", ignoreCase = true)) && (position != null)) {
                position?.setPaintStyle(parser.nextText())
            } else if ((name.equals("strokeWidth", ignoreCase = true)) && (position != null)) {
                position?.setStrokeWidth(parser.nextText().toFloat())
            } else if ((name.equals("fontSize", ignoreCase = true)) && (position != null)) {
                position?.setFontSize(parser.nextText().toFloat())
            } else if ((name.equals("fromObjectKey", ignoreCase = true)) && (line != null)) {
                line?.setFromObjectKey(parser.nextText().toInt())
            } else if ((name.equals("toObjectKey", ignoreCase = true)) && (line != null)) {
                line?.setToObjectKey(parser.nextText().toInt())
            } else if ((name.equals("lineStyle", ignoreCase = true)) && (line != null)) {
                line?.setLineStyle(parser.nextText().toInt())
            } else if ((name.equals("lineShape", ignoreCase = true)) && (line != null)) {
                line?.setLineShape(parser.nextText().toInt())
            } else if ((name.equals("lineThickness", ignoreCase = true)) && (line != null)) {
                line?.setLineThickness(parser.nextText().toInt())
            } else if (name.equals("title", ignoreCase = true)) {
                objectHolder.setDataTitle(parser.nextText())
            } else if (name.equals("background", ignoreCase = true)) {
                objectHolder.setBackground(parser.nextText())
            } else if (name.equals("backgroundUri", ignoreCase = true)) {
                backgroundUri = parser.nextText()
            } else if (name.equals("userCheckboxString", ignoreCase = true)) {
                userCheckboxString = parser.nextText()
            } else if (name.equals("objserial", ignoreCase = true)) {
                objectHolder.setSerialNumber(parser.nextText().toInt())
            } else if (name.equals("lineserial", ignoreCase = true)) {
                objectHolder.getConnectLineHolder().setSerialNumber(parser.nextText().toInt())
            } else if (name.equals("object", ignoreCase = true)) {
                val key = parser.getAttributeValue(Main.APP_NAMESPACE, "key").toInt()
                position = objectHolder.createPosition(key)
            } else if (name.equals("line", ignoreCase = true)) {
                val key = parser.getAttributeValue(Main.APP_NAMESPACE, "key").toInt()
                line = objectHolder.getConnectLineHolder().createLine(key)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseEndTag(name: String)
    {
        try
        {
            if (position == null)
            {
                Log.v(TAG, "Specified position is null...")
                return
            }

            if (name.equals("object", ignoreCase = true))
            {
                // 領域サイズがおかしい場合には、オブジェクトサイズを補正する (ふつーありえないはずなんだけど...)
                val posRect : RectF = position?.getRect() ?: RectF()
                if ((posRect.left > posRect.right) || (posRect.top > posRect.bottom)) {
                    Log.v(
                        TAG,
                        "RECT IS ILLEGAL. : [" + posRect.left + "," + posRect.top + "-[" + posRect.right + "," + posRect.bottom + "]"
                    )
                    position?.setRectRight(posRect.left + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_X)
                    position?.setRectBottom(posRect.top + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_Y)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * (XML形式の)データを読みだす。
     */
    private fun restoreFromXmlFile(fileName: String, objectHolder: MeMoMaObjectHolder): String
    {
        var resultMessage = ""
        try
        {
            val parser = Xml.newPullParser()
            val inputFile = File(fileName)
            if (!inputFile.exists())
            {
                // ファイルが見つからないときは、存在しないファイルを生成する
                if (!inputFile.createNewFile())
                {
                    // ファイルの新規作成が失敗したときには、「ファイルなし」と報告する。
                    resultMessage = "ERR>File not found."
                    return (resultMessage)
                }
            }
            // ファイルの読み込み
            val reader = FileReader(inputFile)
            parser.setInput(reader)

            var eventType = parser.eventType

            // オブジェクトとラインをすべてクリアする
            objectHolder.removeAllPositions()
            val lineHolder = objectHolder.getConnectLineHolder()
            lineHolder.removeAllLines()

            while ((eventType != XmlPullParser.END_DOCUMENT))
            {
                when (eventType)
                {
                    XmlPullParser.START_DOCUMENT -> {}
                    XmlPullParser.START_TAG -> parseStartTag(parser.name, parser, objectHolder)
                    XmlPullParser.END_TAG -> parseEndTag(parser.name)
                    else -> {}
                }
                eventType = parser.next()
            }
            reader.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (resultMessage)
    }

    /**
     * 非同期処理 （バックグラウンドで実行する(このメソッドは、UIスレッドと別のところで実行する)）
     */
    fun parseObjectFromXml(data: MeMoMaObjectHolder): String
    {
        var result = ""
        try
        {
            // ファイル名の設定 ... (拡張子あり...保存時とは違う)
            val fileName = "${parent.filesDir.toString()}/${data.getDataTitle()}.xml"

            // データを読みだす。
            result = restoreFromXmlFile(fileName, data)

            //何か必要な場合、 非同期処理をここで実効
            receiver.onLoadingProcess()
            System.gc()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (result)
    }

    /**
     * 非同期処理の後処理 (結果を応答する)
     */
    fun onFinishProcess(result: String)
    {
        try
        {
            Log.v(TAG, "onFinishProcess(): $result")
            if (result.isEmpty())
            {
                //  エラーが発生していない場合には、読みだしたデータをPreferenceに設定登録...
                val preferences = PreferenceManager.getDefaultSharedPreferences(parent)
                val editor = preferences.edit()
                editor.putString("backgroundUri", backgroundUri)
                editor.putString("userCheckboxString", userCheckboxString)
                editor.apply()
            }
            receiver.onLoadedResult(result.isNotEmpty(), result)
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    /**
     * 結果報告用のインタフェース（積極的に使う予定はないけど...）
     */
    interface IResultReceiver
    {
        fun onLoadingProcess()  // 処理中の処理
        fun onLoadedResult(isError: Boolean, detail: String) // 保存結果の報告
    }

    companion object
    {
        private val TAG = MeMoMaFileLoadingProcess::class.java.simpleName
    }
}
