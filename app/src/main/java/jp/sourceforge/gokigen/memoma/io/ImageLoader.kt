package jp.sourceforge.gokigen.memoma.io

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.InputStream
import kotlin.math.min

/*
*  画像イメージを読み込む
*/
class ImageLoader
{
    fun parseUri(imageFile: String): Uri
    {
        if (imageFile.startsWith("content://"))
        {
            return (Uri.parse(imageFile))
        }
        val picFile = File(imageFile)
        return (Uri.fromFile(picFile))
    }

    /**
     * URI経由でビットマップデータを取得する
     *
     */
    fun getBitmapFromUri(context: Context, uri: Uri, iWidth: Int, iHeight: Int): Bitmap?
    {
        var retBitmap: Bitmap? = null
        try
        {
            // ファイルの表示方法を若干変更する ⇒ Uri.Parse() から BitmapFactoryを利用する方法へ。
            var width = iWidth
            var height = iHeight
            val opt = BitmapFactory.Options()

            // OutOfMemoryエラー対策...一度読み込んで画像サイズを取得
            opt.inJustDecodeBounds = true
            opt.inDither = true
            opt.inPurgeable = true
            opt.inPreferredConfig = Bitmap.Config.RGB_565

            var input: InputStream? = null
            try
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
                input = context.contentResolver.openInputStream(uri)
                if (input != null)
                {
                    BitmapFactory.decodeStream(input, null, opt)
                    input.close()
                }
            }
            catch (ex: Throwable)
            {
                Log.v(TAG, "Ex(1): " + ex.message + " URI : " + uri)
                ex.printStackTrace()
                if (input != null)
                {
                    try
                    {
                        input.close()
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
            }
            // 表示サイズに合わせて縮小...表示サイズが取得できなかった場合には、QVGAサイズと仮定する
            if (width < 10)
            {
                width = 320
            }
            if (height < 10)
            {
                height = 240
            }

            // 画像の縮小サイズを決定する (縦幅、横幅の小さいほうにあわせる)
            val widthBounds = opt.outWidth / width
            val heightBounds = opt.outHeight / height
            opt.inSampleSize = min(widthBounds.toDouble(), heightBounds.toDouble()).toInt()
            opt.inJustDecodeBounds = false
            opt.inPreferredConfig = Bitmap.Config.RGB_565

            try
            {
                input = context.contentResolver.openInputStream(uri)
                retBitmap = BitmapFactory.decodeStream(input, null, opt)
                input?.close()
            }
            catch (ex: Exception)
            {
                Log.v(TAG, "Ex(2): $ex")
                ex.printStackTrace()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (retBitmap)
    }

    companion object
    {
        private val TAG = ImageLoader::class.java.simpleName
    }
}
