package jp.sourceforge.gokigen.memoma.io

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * 共有Intentを発行するクラス。
 *
 * @author MRSa
 */
object SharedIntentInvoker {
    private const val IDENTIFIER = "Gokigen"

    /**
     * メール送信用のIntentを発行する処理。
     * @param parent   呼び出し元Activity
     * @param id          Intentが呼び出し元Activityに戻った時に、呼ばれていたのは何か識別するID
     * @param mailTitle         共有データタイトル
     * @param mailMessage   共有データ本文
     * @param contentUri         添付データファイルのURI
     * @param fileType           添付データファイルの形 (text/plain とか  image/ * とか ...)
     */
    fun shareContent(
        parent: AppCompatActivity,
        id: Int,
        mailTitle: String?,
        mailMessage: String?,
        contentUri: Uri?,
        fileType: String
    ) {
        val intent = Intent()
        intent.setAction(Intent.ACTION_SEND)
        try {
            Log.v(
                IDENTIFIER,
                "Share Content... $contentUri"
            )
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_SUBJECT, mailTitle)
            intent.putExtra(Intent.EXTRA_TEXT, mailMessage)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try
            {
                if ((contentUri != null) && (fileType.isNotEmpty()))
                {
                    // ファイル類を添付する
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setType(fileType)
                    intent.putExtra(Intent.EXTRA_STREAM, contentUri)
                    Log.v(
                        IDENTIFIER,
                        "Attached :$contentUri"
                    )
                }
            }
            catch (ee: Exception)
            {
                Log.v(IDENTIFIER, "attach failure : " + contentUri + "  " + ee.message)
                ee.printStackTrace()
            }
            if (intent.resolveActivity(parent.applicationContext.packageManager) != null) {
                Log.v(IDENTIFIER, "----- START ACTIVITY -----")
                parent.startActivity(intent)
            }
        }
        catch (ex: ActivityNotFoundException)
        {
            Toast.makeText(parent, "" + ex.message, Toast.LENGTH_SHORT).show()
            Log.v(IDENTIFIER, "android.content.ActivityNotFoundException : " + ex.message)
            ex.printStackTrace()
        }
        catch (e: Throwable)
        {
            Log.v(IDENTIFIER, "xxx : " + e.message)
            e.printStackTrace()
        }
    }
}