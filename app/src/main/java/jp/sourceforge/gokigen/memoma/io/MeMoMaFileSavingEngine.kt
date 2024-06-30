package jp.sourceforge.gokigen.memoma.io

import android.content.Context
import android.util.Log
import android.util.Xml
import jp.sourceforge.gokigen.memoma.Main
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder
import java.io.File
import java.io.FileWriter

/**
 * データをファイルに保存するエンジン部分
 */
class MeMoMaFileSavingEngine//  設定データ読み出し用...。

/**
 * コンストラクタ
 */(
    private val context: Context,
    private val backgroundUri: String,
    private val userCheckboxString: String
) {
    private val TAG = toString()

    /**
     * データを(XML形式で)保管する。
     *
     */
    private fun storeToXmlFile(fileName: String, objectHolder: MeMoMaObjectHolder): String {
        var resultMessage = ""
        try {
            val writer = FileWriter("$fileName.xml")
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
            while (keys!!.hasMoreElements()) {
                val key = keys.nextElement()
                val pos = objectHolder.getPosition(key)
                val posRect = pos!!.getRect()
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
                /*
                serializer.startTag(Main.APP_NAMESPACE, "otherInfoUri");
                serializer.text(pos.otherInfoUri);
                serializer. endTag(Main.APP_NAMESPACE, "otherInfoUri");

                serializer.startTag(Main.APP_NAMESPACE, "backgroundUri");
                serializer.text(pos.backgroundUri);
                serializer. endTag(Main.APP_NAMESPACE, "backgroundUri");

                serializer.startTag(Main.APP_NAMESPACE, "objectStatus");
                serializer.text(pos.objectStatus);
                serializer. endTag(Main.APP_NAMESPACE, "objectStatus");
*/
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

            // 接続線の出力 （保持しているものはすべて表示する）
            val lineKeys = objectHolder.getConnectLineHolder().lineKeys
            while (lineKeys.hasMoreElements()) {
                val key = lineKeys.nextElement()
                val line = objectHolder.getConnectLineHolder().getLine(key)
                serializer.startTag(Main.APP_NAMESPACE, "line")
                serializer.attribute(Main.APP_NAMESPACE, "key", key.toString())

                serializer.startTag(Main.APP_NAMESPACE, "fromObjectKey")
                serializer.text(line!!.getFromObjectKey().toString())
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
                /*
                serializer.startTag(Main.APP_NAMESPACE, "fromShape");
                serializer.text(Integer.toString(line.fromShape));
                serializer.endTag(Main.APP_NAMESPACE, "fromShape");

                serializer.startTag(Main.APP_NAMESPACE, "toShape");
                serializer.text(Integer.toString(line.toShape));
                serializer.endTag(Main.APP_NAMESPACE, "toShape");

                serializer.startTag(Main.APP_NAMESPACE, "fromString");
                serializer.text(line.fromString);
                serializer.endTag(Main.APP_NAMESPACE, "fromString");

                serializer.startTag(Main.APP_NAMESPACE, "toString");
                serializer.text(line.toString);
                serializer.endTag(Main.APP_NAMESPACE, "toString");
*/
                serializer.endTag(Main.APP_NAMESPACE, "line")
            }

            serializer.endTag(Main.APP_NAMESPACE, "memoma")
            serializer.endDocument()
            serializer.flush()
            writer.close()
        } catch (e: Exception) {
            resultMessage = " " + e.message
            Log.v(TAG, resultMessage)
            e.printStackTrace()
        }
        return (resultMessage)
    }

    /**
     * オブジェクトを保存する
     */
    fun saveObjects(objectHolder: MeMoMaObjectHolder): String
    {
        // データタイトルがない場合...保存処理は行わない。
        if (objectHolder.getDataTitle().isEmpty())
        {
            Log.v(
                TAG,
                "MeMoMaFileSavingEngine::saveObjects() : specified file name is illegal, save aborted. : " + objectHolder.getDataTitle()
            )
            return ("")
        }

        if (objectHolder.isEmpty())
        {
            try
            {
                // ファイルの存在を確認
                val fileName =
                    context.filesDir.toString() + "/" + objectHolder.getDataTitle() + ".xml"
                val checkFile = File(fileName)
                if (!checkFile.exists()) {
                    // ファイルが存在しない場合、新規ファイルを作成する
                    if (checkFile.createNewFile()) {
                        Log.v(TAG, "create New File.")
                        return (context.getString(R.string.createnew))
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            Log.v(TAG, "MeMoMaFileSavingEngine::saveObjects() EMPTY : ${objectHolder.getDataTitle()}")

            // データがない場合は保存しない
            return (context.getString(R.string.none_object))
        }
        Log.v(TAG, "MeMoMaFileSavingEngine::saveObjects() DONE : ${objectHolder.getDataTitle()}")

        // データを保管する （ファイル名の設定は、拡張子なし
        return (storeToXmlFile(
            context.filesDir.toString() + "/" + objectHolder.getDataTitle(),
            objectHolder
        ))
    }
}