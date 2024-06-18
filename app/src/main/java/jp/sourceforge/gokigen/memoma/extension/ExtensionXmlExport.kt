package jp.sourceforge.gokigen.memoma.extension

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Xml
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExtensionXmlExport(private val context: Context, private val objectHolder: MeMoMaObjectHolder, private val backgroundUri: String, private val userCheckboxString: String)
{
    fun exportToXmlFile(): String
    {
        var resultMessage = ""
        try
        {
            val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/" + Main.APP_NAMESPACE + "/"
            val resolver = context.contentResolver

            // エクスポートするファイル名を決定する
            val calendar = Calendar.getInstance()
            val outFormat = SimpleDateFormat("yyyyMMdd_HHmmss_", Locale.US)
            val exportedFileName = outFormat.format(calendar.time) + objectHolder.getDataTitle() + ".xml"
            val extStorageUri: Uri
            val documentUri: Uri?
            val writer: OutputStreamWriter
            val values = ContentValues()
            values.put(MediaStore.Downloads.TITLE, exportedFileName)
            values.put(MediaStore.Downloads.DISPLAY_NAME, exportedFileName)
            values.put(MediaStore.Downloads.MIME_TYPE, "text/xml") // text/plain or text/xml
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                values.put(MediaStore.Downloads.RELATIVE_PATH, "Download/" + Main.APP_NAMESPACE)
                values.put(MediaStore.Downloads.IS_PENDING, true)
                extStorageUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                Log.v(TAG, "---------- $exportedFileName $values")
                documentUri = resolver.insert(extStorageUri, values)
                if (documentUri == null)
                {
                    resultMessage = "documentUri is NULL."
                    return resultMessage
                }
                val outputStream = resolver.openOutputStream(documentUri, "wa")
                writer = OutputStreamWriter(outputStream)
            }
            else
            {
                documentUri = null
                val path = File(outputDir)
                path.mkdir()
                values.put(MediaStore.Downloads.DATA, path.absolutePath + File.separator + exportedFileName)
                val targetFile = File(outputDir + File.separator + exportedFileName)
                val outputStream = FileOutputStream(targetFile)
                writer = OutputStreamWriter(outputStream)
            }

            val serializer = Xml.newSerializer()
            serializer.setOutput(writer)
            serializer.startDocument("UTF-8", true)
            serializer.startTag(Main.APP_NAMESPACE, "memoma")

            // タイトルの出力
            serializer.startTag(Main.APP_NAMESPACE, "title")
            serializer.text(objectHolder.getDataTitle())
            serializer.endTag(Main.APP_NAMESPACE, "title")

            // 背景情報の出力
            serializer.startTag(Main.APP_NAMESPACE, "background")
            serializer.text(objectHolder.getBackground())
            serializer.endTag(Main.APP_NAMESPACE, "background")

            // 背景画像URIの出力
            serializer.startTag(Main.APP_NAMESPACE, "backgroundUri")
            serializer.text(backgroundUri)
            serializer.endTag(Main.APP_NAMESPACE, "backgroundUri")

            // ユーザチェックボックス名の出力
            serializer.startTag(Main.APP_NAMESPACE, "userCheckboxString")
            serializer.text(userCheckboxString)
            serializer.endTag(Main.APP_NAMESPACE, "userCheckboxString")

            serializer.startTag(Main.APP_NAMESPACE, "objserial")
            serializer.text(objectHolder.getSerialNumber().toString())
            serializer.endTag(Main.APP_NAMESPACE, "objserial")

            serializer.startTag(Main.APP_NAMESPACE, "lineserial")
            serializer.text(objectHolder.getConnectLineHolder().getSerialNumber().toString())
            serializer.endTag(Main.APP_NAMESPACE, "lineserial")

            // オブジェクトの出力 （保持しているものはすべて表示する）
            val keys = objectHolder.getObjectKeys()
            if (keys != null) {
                while (keys.hasMoreElements()) {
                    val key = keys.nextElement()
                    val pos = objectHolder.getPosition(key)
                    if (pos != null) {
                        val posRect = pos.getRect()
                        serializer.startTag(Main.APP_NAMESPACE, "object")
                        serializer.attribute(Main.APP_NAMESPACE, "key", key.toString())
                        serializer.startTag(Main.APP_NAMESPACE, "rect")
                        serializer.startTag(Main.APP_NAMESPACE, "top")
                        serializer.text(posRect.top.toString())
                        serializer.endTag(Main.APP_NAMESPACE, "top")
                        serializer.startTag(Main.APP_NAMESPACE, "left")
                        serializer.text(posRect.left.toString())
                        serializer.endTag(Main.APP_NAMESPACE, "left")
                        serializer.startTag(Main.APP_NAMESPACE, "right")
                        serializer.text(posRect.right.toString())
                        serializer.endTag(Main.APP_NAMESPACE, "right")
                        serializer.startTag(Main.APP_NAMESPACE, "bottom")
                        serializer.text(posRect.bottom.toString())
                        serializer.endTag(Main.APP_NAMESPACE, "bottom")
                        serializer.endTag(Main.APP_NAMESPACE, "rect")
                        serializer.startTag(Main.APP_NAMESPACE, "drawStyle")
                        serializer.text(pos.getDrawStyle().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "drawStyle")
                        serializer.startTag(Main.APP_NAMESPACE, "icon")
                        serializer.text(pos.getIcon().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "icon")
                        serializer.startTag(Main.APP_NAMESPACE, "label")
                        serializer.text(pos.getLabel())
                        serializer.endTag(Main.APP_NAMESPACE, "label")
                        serializer.startTag(Main.APP_NAMESPACE, "detail")
                        serializer.text(pos.getDetail())
                        serializer.endTag(Main.APP_NAMESPACE, "detail")
                        serializer.startTag(Main.APP_NAMESPACE, "userChecked")
                        serializer.text(pos.getUserChecked().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "userChecked")
                        serializer.startTag(Main.APP_NAMESPACE, "labelColor")
                        serializer.text(pos.getLabelColor().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "labelColor")
                        serializer.startTag(Main.APP_NAMESPACE, "objectColor")
                        serializer.text(pos.getObjectColor().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "objectColor")
                        serializer.startTag(Main.APP_NAMESPACE, "paintStyle")
                        serializer.text(pos.getPaintStyle())
                        serializer.endTag(Main.APP_NAMESPACE, "paintStyle")
                        serializer.startTag(Main.APP_NAMESPACE, "strokeWidth")
                        serializer.text(pos.getstrokeWidth().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "strokeWidth")
                        serializer.startTag(Main.APP_NAMESPACE, "fontSize")
                        serializer.text(pos.getFontSize().toString())
                        serializer.endTag(Main.APP_NAMESPACE, "fontSize")
                        serializer.endTag(Main.APP_NAMESPACE, "object")
                    }
                }
            }

            // 接続線の出力 （保持しているものはすべて表示する）
            val lineKeys = objectHolder.getConnectLineHolder().lineKeys
            while (lineKeys.hasMoreElements())
            {
                val key = lineKeys.nextElement()
                val line = objectHolder.getConnectLineHolder().getLine(key)
                if (line != null) {
                    serializer.startTag(Main.APP_NAMESPACE, "line")
                    serializer.attribute(Main.APP_NAMESPACE, "key", key.toString())
                    serializer.startTag(Main.APP_NAMESPACE, "fromObjectKey")
                    serializer.text(line.getFromObjectKey().toString())
                    serializer.endTag(Main.APP_NAMESPACE, "fromObjectKey")
                    serializer.startTag(Main.APP_NAMESPACE, "toObjectKey")
                    serializer.text(line.getToObjectKey().toString())
                    serializer.endTag(Main.APP_NAMESPACE, "toObjectKey")
                    serializer.startTag(Main.APP_NAMESPACE, "lineStyle")
                    serializer.text(line.getLineStyle().toString())
                    serializer.endTag(Main.APP_NAMESPACE, "lineStyle")
                    serializer.startTag(Main.APP_NAMESPACE, "lineShape")
                    serializer.text(line.getLineShape().toString())
                    serializer.endTag(Main.APP_NAMESPACE, "lineShape")
                    serializer.startTag(Main.APP_NAMESPACE, "lineThickness")
                    serializer.text(line.getLineThickness().toString())
                    serializer.endTag(Main.APP_NAMESPACE, "lineThickness")
                    serializer.endTag(Main.APP_NAMESPACE, "line")
                }
            }

            serializer.endTag(Main.APP_NAMESPACE, "memoma")
            serializer.endDocument()
            writer.flush()
            writer.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                values.put(MediaStore.Downloads.IS_PENDING, false)
                if (documentUri != null)
                {
                    resolver.update(documentUri, values, null, null)
                }
            }
        }
        catch (e: Exception)
        {
            resultMessage = " ERR " + e.message
            Log.v(TAG, resultMessage)
            e.printStackTrace()
        }
        return resultMessage
    }

    companion object {
        private val TAG = ExtensionXmlExport::class.java.simpleName
    }
}
