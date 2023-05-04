package jp.sourceforge.gokigen.memoma.io;

import java.io.File;
import java.io.InputStream;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import jp.sourceforge.gokigen.memoma.Main;

/*
 *  画像イメージを読み込む
 */
public class ImageLoader
{
    private static final String TAG = "MeMoMa::ImageLoader";
    /*
	 *  URIから変換
	 */
	public static Uri parseUri(String imageFile)
	{
	    if (imageFile.startsWith("content://"))
	    {
	    	return (Uri.parse(imageFile));
	    }
    	File picFile = new File(imageFile);
    	return (Uri.fromFile(picFile));	
	}

	/**
	 *   URI経由でビットマップデータを取得する
	 * 
	 */
    public static Bitmap getBitmapFromUri(Context context, Uri uri, int width, int height)
    {
        // ファイルの表示方法を若干変更する ⇒ Uri.Parse() から BitmapFactoryを利用する方法へ。
        BitmapFactory.Options opt = new BitmapFactory.Options();

        // OutOfMemoryエラー対策...一度読み込んで画像サイズを取得
        opt.inJustDecodeBounds = true;
        opt.inDither = true;
        opt.inPurgeable = true;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;

        InputStream input = null; 
        try
        {
            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT)
            {
                context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            input = context.getContentResolver().openInputStream(uri);
            if (input != null)
            {
                BitmapFactory.decodeStream(input, null, opt);
                input.close();
            }
        }
        catch (Throwable ex)
        {
        	Log.v(TAG, "Ex(1): " + ex.getMessage() + " URI : " + uri);
        	ex.printStackTrace();
        	if (input != null)
        	{
        		try
        		{
        	        input.close();
        		}
        		catch (Exception e)
        		{
        			//
                    e.printStackTrace();
        		}
        	}
        }
        // 表示サイズに合わせて縮小...表示サイズが取得できなかった場合には、QVGAサイズと仮定する
        if (width < 10)
        {
            width = 320;
        }
        if (height < 10)
        {
        	height = 240;
        }

        // 画像の縮小サイズを決定する (縦幅、横幅の小さいほうにあわせる)
        int widthBounds = opt.outWidth / width;
        int heightBounds = opt.outHeight / height;
        opt.inSampleSize=Math.min(widthBounds, heightBounds);
        opt.inJustDecodeBounds = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        
        // 画像ファイルを応答する
        input = null; 
        Bitmap retBitmap = null;
        try
        { 
            input = context.getContentResolver().openInputStream(uri); 
            retBitmap = BitmapFactory.decodeStream(input, null, opt); 
            input.close();
        }
        catch (Exception ex)
        {
        	Log.v(TAG, "Ex(2): " + ex.toString());
        	if (input != null)
        	{
        		try
        		{
        	        input.close();
        		}
        		catch (Exception e)
        		{
        			//
                    e.printStackTrace();
        		}
        	}
        	ex.printStackTrace();
        }
        return (retBitmap);
    }
}
