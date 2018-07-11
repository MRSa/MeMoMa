package jp.sourceforge.gokigen.memoma.fileio;

import java.io.File;
import java.io.InputStream;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;

/**
 *  画像イメージを読み込む
 * 
 * @author MRSa
 *
 */
public class ImageLoader
{
    ProgressDialog loadingDialog = null;
    Context  parent = null;

    // 画像を表示する
    String imageFile = null;
	Bitmap imageBitmap = null;
	int imageWidth     = 1;
	int imageHeight    = 1;
	ImageView imageView = null;
    
    public ImageLoader(Context context)
    {
        loadingDialog = new ProgressDialog(context);
        parent = context;
    }
    
    /**
     *  イメージファイルの一覧を取得する
     *  (一度も動かしたことのないコードなので注意！)
     * @param activity
     * @return イメージファイル名の一覧
     */
    /*
    public static String[] getImageFileList(Activity activity)
    {
        try
        {
        	HashSet<String> list = new HashSet<String>();
        	
        	Cursor c = activity.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        	while(c.moveToNext())
        	{
                String imagefile = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
                File file = new File(imagefile);
                list.add(file.getParent());
        	}
            return (String[]) list.toArray(new String[list.size()]);
        }
        catch (Exception ex)
        {
            //
        }
        return (null);
    }
    */

    /**
	 *  URIから変換
	 * 
	 * @param imageFile
	 * @return
	 */
	public static Uri parseUri(String imageFile)
	{
	    if (imageFile.startsWith("content://") == true)
	    {
	    	return (Uri.parse(imageFile));
	    }

    	File picFile = new File(imageFile);
    	return (Uri.fromFile(picFile));	
	}

	/**
	 *   画面にイメージを表示する 
	 *
	 */
	public static void setImage(Context context, ImageView view, String imageFile)
    {
    	// 画像を表示する
		Bitmap bitmap = null;
		int width = view.getWidth();
		int height = view.getHeight();
        if (imageFile.startsWith("content://") == true)
        {
        	// URIから画像を設定する...OutOfMemory対策付き
        	bitmap = getBitmapFromUri(context, Uri.parse(imageFile), width, height);
        }
        else
        {
        	// OutOfMemory対策付き...ビットマップのサイズを圧縮して表示
        	bitmap = getBitmap(imageFile, view.getWidth(), view.getHeight());
        }
    	view.setScaleType(ImageView.ScaleType.FIT_XY);
        view.setImageBitmap(bitmap);
    }

	/**
	 *   画面にイメージを表示する (ロード中ダイアログ表示つき)
	 * 
	 */
	public void setImage(ImageView view, String targetFile)
    {

        //  プログレスダイアログ（「ロード中...」）を表示する。
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loadingDialog.setMessage(parent.getString(R.string.dataLoading));
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        imageFile = targetFile;
        imageView = view;
        imageBitmap = null;
        imageWidth = view.getWidth();
        imageHeight = view.getHeight();        
        
        /**
         *  ダイアログ表示中の処理
         * 
         */
        Thread thread = new Thread(new Runnable()
        {  
            public void run()
            {
            	try
            	{            		
            		if (imageFile.startsWith("content://") == true)
                    {
                    	// URIから画像を設定する...OutOfMemory対策付き
                    	imageBitmap = getBitmapFromUri(parent, Uri.parse(imageFile), imageWidth, imageHeight);
                    }
                    else
                    {
                    	// OutOfMemory対策付き...ビットマップのサイズを圧縮して表示
                    	imageBitmap = getBitmap(imageFile, imageWidth, imageHeight);
                    }
            		handler.sendEmptyMessage(0);
            	}
                catch (Exception ex)
            	{
            		handler.sendEmptyMessage(0);
            	}
            }

            /**
             *   画面の更新
             */
            private final Handler handler = new Handler()
            {
                @Override
                public void handleMessage(Message msg)
                {
                	if ((imageBitmap != null)&&(imageView != null))
                	{
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setImageBitmap(imageBitmap);
                	}
                    imageFile = null;
                    imageView = null;
                    imageBitmap = null;
                    imageWidth = 1;
                    imageHeight = 1;

                	loadingDialog.dismiss();
                
                }
            };   
        });
        try
        {
            thread.start();
        }
        catch (Exception ex)
        {

        }
    }
	
	/**
	 *   URI経由でビットマップデータを取得する
	 * 
	 * @param context
	 * @param uri
	 * @param width
	 * @param height
	 * @return
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
            input = context.getContentResolver().openInputStream(uri); 
            BitmapFactory.decodeStream(input, null, opt); 
            input.close();
        }
        catch (Exception ex)
        {
        	Log.v(Main.APP_IDENTIFIER, "Ex(1): " + ex.toString());
        	if (input != null)
        	{
        		try
        		{
        	        input.close();
        		}
        		catch (Exception e)
        		{
        			//
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
        	Log.v(Main.APP_IDENTIFIER, "Ex(2): " + ex.toString());
        	if (input != null)
        	{
        		try
        		{
        	        input.close();
        		}
        		catch (Exception e)
        		{
        			//
        		}
        	}
        }
        return (retBitmap);
    }        
    
    /**
     *   ビットマップデータを取得する
     * 
     * @param pictureString
     * @param width
     * @param height
     * @return ビットマップデータ
     */
    public static Bitmap getBitmap(String pictureString, int width, int height)
    {

        // ファイルの表示方法を若干変更する ⇒ Uri.Parse() から BitmapFactoryを利用する方法へ。
        BitmapFactory.Options opt = new BitmapFactory.Options();

        // OutOfMemoryエラー対策...一度読み込んで画像サイズを取得
        opt.inJustDecodeBounds = true;
        opt.inDither = true;
        BitmapFactory.decodeFile(pictureString, opt);

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
        
        // 画像ファイルを応答する
    	return (BitmapFactory.decodeFile(pictureString, opt));
    }    
}
