package jp.sourceforge.gokigen.memoma.extension

import android.content.Context
import android.graphics.RectF
import android.net.Uri
import android.util.Log
import android.util.Xml
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import jp.sourceforge.gokigen.memoma.holders.ObjectConnector
import jp.sourceforge.gokigen.memoma.holders.PositionObject
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class ExtensionXmlImport(private val context: Context, private val objectHolder: MeMoMaObjectHolder, private val importUri: Uri)
{
    private var position: PositionObject? = null
    private var line: ObjectConnector? = null
    private var backgroundUri = ""
    private var userCheckboxString = ""

    fun importFromXmlFile() : String
    {
        var resultMessage = ""
        val parser = Xml.newPullParser()
        try
        {
            val inputStream: InputStream? = context.contentResolver.openInputStream(importUri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            parser.setInput(reader)
            var eventType = parser.eventType

            // オブジェクトとラインをすべてクリアする
            objectHolder.removeAllPositions()
            val lineHolder = objectHolder.getConnectLineHolder()
            lineHolder.removeAllLines()
            while (eventType != XmlPullParser.END_DOCUMENT)
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
            resultMessage = " ERR " + e.message
            Log.v(TAG, resultMessage)
            e.printStackTrace()
        }
        return resultMessage
    }

    private fun parseStartTag(
        name: String,
        parser: XmlPullParser,
        objectHolder: MeMoMaObjectHolder?
    )
    {
        try
        {
            if (name.equals("top", ignoreCase = true) && position != null) {
                position?.setRectTop(parser.nextText().toFloat())
            } else if (name.equals("bottom", ignoreCase = true) && position != null) {
                position?.setRectBottom(parser.nextText().toFloat())
            } else if (name.equals("left", ignoreCase = true) && position != null) {
                position?.setRectLeft(parser.nextText().toFloat())
            } else if (name.equals("right", ignoreCase = true) && position != null) {
                position?.setRectRight(parser.nextText().toFloat())
            } else if (name.equals("drawStyle", ignoreCase = true) && position != null) {
                position?.setDrawStyle(parser.nextText().toInt())
            } else if (name.equals("icon", ignoreCase = true) && position != null) {
                position?.setIcon(parser.nextText().toInt())
            } else if (name.equals("label", ignoreCase = true) && position != null) {
                position?.setLabel(parser.nextText())
            } else if (name.equals("detail", ignoreCase = true) && position != null) {
                position?.setDetail(parser.nextText())
            } else if (name.equals("userChecked", ignoreCase = true) && position != null) {
                val parseData = parser.nextText()
                position?.setUserChecked(parseData.equals("true", ignoreCase = true))
            } else if (name.equals("labelColor", ignoreCase = true) && position != null) {
                position?.setLabelColor(parser.nextText().toInt())
            } else if (name.equals("objectColor", ignoreCase = true) && position != null) {
                position?.setObjectColor(parser.nextText().toInt())
            } else if (name.equals("paintStyle", ignoreCase = true) && position != null) {
                position?.setPaintStyle(parser.nextText())
            } else if (name.equals("strokeWidth", ignoreCase = true) && position != null) {
                position?.setStrokeWidth(parser.nextText().toFloat())
            } else if (name.equals("fontSize", ignoreCase = true) && position != null) {
                position?.setFontSize(parser.nextText().toFloat())
            } else if (name.equals("fromObjectKey", ignoreCase = true) && line != null) {
                line?.setFromObjectKey(parser.nextText().toInt())
            } else if (name.equals("toObjectKey", ignoreCase = true) && line != null) {
                line?.setToObjectKey(parser.nextText().toInt())
            } else if (name.equals("lineStyle", ignoreCase = true) && line != null) {
                line?.setLineStyle(parser.nextText().toInt())
            } else if (name.equals("lineShape", ignoreCase = true) && line != null) {
                line?.setLineShape(parser.nextText().toInt())
            } else if (name.equals("lineThickness", ignoreCase = true) && line != null) {
                line?.setLineThickness(parser.nextText().toInt())
            } else if (name.equals("title", ignoreCase = true) && objectHolder != null) {
                objectHolder.setDataTitle(parser.nextText())
            } else if (name.equals("background", ignoreCase = true) && objectHolder != null) {
                objectHolder.setBackground(parser.nextText())
            } else if (name.equals("backgroundUri", ignoreCase = true) && objectHolder != null) {
                backgroundUri = parser.nextText()
            } else if (name.equals(
                    "userCheckboxString",
                    ignoreCase = true
                ) && objectHolder != null
            ) {
                userCheckboxString = parser.nextText()
            } else if (name.equals("objserial", ignoreCase = true) && objectHolder != null) {
                objectHolder.setSerialNumber(parser.nextText().toInt())
                //Log.v(Main.APP_IDENTIFIER, "objSerial : " + objectHolder.getSerialNumber());
            } else if (name.equals("lineserial", ignoreCase = true) && objectHolder != null) {
                objectHolder.getConnectLineHolder().setSerialNumber(parser.nextText().toInt())
                //Log.v(Main.APP_IDENTIFIER, "lineSerial : " + objectHolder.getSerialNumber());
            } else if (name.equals("object", ignoreCase = true)) {
                val key = parser.getAttributeValue(Main.APP_NAMESPACE, "key").toInt()
                //Log.v(Main.APP_IDENTIFIER, "create object, key :" + key);
                if (objectHolder != null) {
                    position = objectHolder.createPosition(key)
                }
            } else if (name.equals("line", ignoreCase = true)) {
                val key = parser.getAttributeValue(Main.APP_NAMESPACE, "key").toInt()
                //Log.v(Main.APP_IDENTIFIER, "create line, key :" + key);
                line = null
                if (objectHolder != null) {
                    line = objectHolder.getConnectLineHolder().createLine(key)
                }
            }
        } catch (e: java.lang.Exception) {
            Log.v(TAG, "ERR>parseStartTag() name:$name $e")
        }
    }
    private fun parseEndTag(name: String)
    {
        try
        {
            if (name.equals("object", ignoreCase = true))
            {
                // 領域サイズがおかしい場合には、オブジェクトサイズを補正する (ふつーありえないはずなんだけど...)
                val posRect: RectF? = position?.getRect()
                if (posRect != null)
                {
                    if (posRect.left > posRect.right || posRect.top > posRect.bottom)
                    {
                        Log.v(
                            TAG,
                            "RECT IS ILLEGAL. : [" + posRect.left + "," + posRect.top + "-[" + posRect.right + "," + posRect.bottom + "]"
                        )
                        position?.setRectRight(posRect.left + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_X)
                        position?.setRectBottom(posRect.top + MeMoMaObjectHolder.OBJECTSIZE_DEFAULT_Y)
                    }
                }
            }
        }
        catch (e: Exception)
        {
            Log.v(TAG, "ERR>parseEndTag() name:$name $e")
        }
    }

    companion object {
        private val TAG = ExtensionXmlImport::class.java.simpleName
    }
}
