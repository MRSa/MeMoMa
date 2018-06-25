package jp.sourceforge.gokigen.memoma;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 *    線の形状を保持するクラス
 * 
 * @author MRSa
 *
 */
public class LineStyleHolder
{
	private Activity activity = null;

	public static final int LINESTYLE_STRAIGHT_NO_ARROW = 0;
    public static final int LINESTYLE_TREESTYLE_NO_ARROW = 1;
    public static final int LINESTYLE_CURVESTYLE_NO_ARROW = 2;
	public static final int LINESTYLE_STRAIGHT_R_ARROW = 3;
	public static final int LINESTYLE_STRAIGHT_L_ARROW = 4;
    public static final int LINESTYLE_TREESTYLE_R_ARROW = 5;
    public static final int LINESTYLE_TREESTYLE_L_ARROW = 6;
    public static final int LINESTYLE_CURVESTYLE_R_ARROW = 7;
    public static final int LINESTYLE_CURVESTYLE_L_ARROW = 8;

    public static final int LINESHAPE_NORMAL = 1000;       // 普通の直線
    public static final int LINESHAPE_DASH =  1001;      // 点線（破線)

    public static final int LINETHICKNESS_THIN = 0;      // 細い線
	public static final int LINETHICKNESS_MIDDLE = 3;  // 中太線
	public static final int LINETHICKNESS_HEAVY = 6;    //  太線
    
	private int currentLineThickness = LINETHICKNESS_THIN;
	private int currentLineShape = LINESHAPE_NORMAL;
	private int currentLineStyle = LINESTYLE_STRAIGHT_NO_ARROW;
	
	/**
	 *    コンストラクタ
	 * 
	 * @param arg
	 */
	public LineStyleHolder(Activity arg)
	{
		activity = arg;
	}

	/**
	 *    ライン形状を読み出す
	 * 
	 */
	public void prepare()
	{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        currentLineStyle = Integer.parseInt(preferences.getString("lineStyle", "0"));
        currentLineShape = Integer.parseInt(preferences.getString("lineShape", "1000"));
        currentLineThickness = Integer.parseInt(preferences.getString("lineThickness", "1"));
		
	}
	
	public int changeLineStyle()
	{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        int value = Integer.parseInt(preferences.getString("lineStyle", "0"));
        switch (value)
        {
          case LINESTYLE_STRAIGHT_NO_ARROW:
              value = LINESTYLE_STRAIGHT_R_ARROW;
            break;
          case LINESTYLE_STRAIGHT_R_ARROW:
              value = LINESTYLE_TREESTYLE_NO_ARROW;
            break;
          case LINESTYLE_TREESTYLE_NO_ARROW:
              value = LINESTYLE_TREESTYLE_R_ARROW;
              break;
          case LINESTYLE_TREESTYLE_R_ARROW:
              value = LINESTYLE_CURVESTYLE_NO_ARROW;
            break;
          case LINESTYLE_CURVESTYLE_NO_ARROW:
              value = LINESTYLE_CURVESTYLE_R_ARROW;
              break;
          case LINESTYLE_CURVESTYLE_R_ARROW:
        	  // value = LINESTYLE_STRAIGHT_L_ARROW;  // 左側矢印を作成する場合
        	  value = LINESTYLE_STRAIGHT_NO_ARROW;   // 左側矢印を作成しない場合
        	  break;
          case LINESTYLE_STRAIGHT_L_ARROW:
              value = LINESTYLE_TREESTYLE_L_ARROW;
              break;
          case LINESTYLE_TREESTYLE_L_ARROW:
              value = LINESTYLE_CURVESTYLE_L_ARROW;
              break;
          case LINESTYLE_CURVESTYLE_L_ARROW:
          default:
              value = LINESTYLE_STRAIGHT_NO_ARROW;
              break;
        }

        // 文字列としてデータを記録
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lineStyle", "" + value);
        editor.commit();

        return (value);
	}

	  /**
	   *   接続する線の形状を指定する
	   *   
	   * @param style
	   */
	  public void setLineStyle(int style)
	  {
		  currentLineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW;
		  switch (style)
		  {
		    case LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW:
		    case LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW:
		    case LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW:
		    case LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW:
		    case LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW:
		    case LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW:
		    case LineStyleHolder.LINESTYLE_STRAIGHT_L_ARROW:
		    case LineStyleHolder.LINESTYLE_TREESTYLE_L_ARROW:
		    case LineStyleHolder.LINESTYLE_CURVESTYLE_L_ARROW:
		    	currentLineStyle = style;
			    break;

		    default:
		    	currentLineStyle = LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW;
			    break;
		  }	

		  // 文字列としてデータを記録
	      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
	      SharedPreferences.Editor editor = preferences.edit();
	      editor.putString("lineStyle", "" + currentLineStyle);
	      editor.commit();
	  }
	  /**
	   *    接続する線の種類（点線 or 実線) を設定する
	   * 
	   * @param shape
	   */
	  public void setLineShape(int shape)
	  {
		  currentLineShape = LineStyleHolder.LINESHAPE_NORMAL;
		  switch (shape)
		  {
		    case LineStyleHolder.LINESHAPE_DASH:
		    case LineStyleHolder.LINESHAPE_NORMAL:
		    	currentLineShape = shape;
			    break;

		    default:
		    	currentLineShape = LineStyleHolder.LINESHAPE_NORMAL;
			    break;
		  }

		  // 文字列としてデータを記録
	      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
	      SharedPreferences.Editor editor = preferences.edit();
	      editor.putString("lineShape", "" + currentLineShape);
	      editor.commit();
	  }	

	  /**
	   *    接続する線の太さ を設定する
	   * 
	   * @param shape
	   */
	  public void setLineThickness(int thickness)
	  {
		  currentLineThickness = LineStyleHolder.LINETHICKNESS_THIN;
		  switch (thickness)
		  {
		    case LineStyleHolder.LINETHICKNESS_HEAVY:
		    case LineStyleHolder.LINETHICKNESS_MIDDLE:
		    case LineStyleHolder.LINETHICKNESS_THIN:
		    	currentLineThickness = thickness;
			    break;

		    default:
		    	currentLineThickness = LineStyleHolder.LINETHICKNESS_THIN;
			    break;
		  }

		  // 文字列としてデータを記録
	      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
	      SharedPreferences.Editor editor = preferences.edit();
	      editor.putString("lineThickness", "" + currentLineThickness);
	      editor.commit();
	  }	

	  /**
	   *    接続する線の形状を応答する
	   * 
	   * @return 接続する線の形状
	   */
	  public int getLineStyle()
	  {
		  return (currentLineStyle);
	  }
	  
	  /**
	   *   接続する線の種類（点線 or 実線) を応答する
	   * 
	   * @return 接続する線の種類
	   */
	  public int getLineShape()
	  {
		  return (currentLineShape);
	  }

	  /**
	   *   接続する線の太さを応答する
	   * 
	   * @return 接続する線の太さ
	   */
	  public int getLineThickness()
	  {
		  return (currentLineThickness);
	  }

	    /**
	     *    現在の太さのイメージのIDを応答する
	     * 
	     * @param thickness
	     * @return
	     */
	    public static int getLineThicknessImageId(int thickness)
	    {
	        int buttonId = R.drawable.btn_line_thin;
	    	switch (thickness)
	        {
	  	      case LineStyleHolder.LINETHICKNESS_HEAVY:
	            buttonId = R.drawable.btn_line_heavy;
	            break;

	    	  case LineStyleHolder.LINETHICKNESS_MIDDLE:
	              buttonId = R.drawable.btn_line_middle;
	              break;

	    	  case LineStyleHolder.LINETHICKNESS_THIN:
	          default:
	        	  buttonId = R.drawable.btn_line_thin;
	        	  break;
	        }
	        return (buttonId);
	    }    
	    
	    /**
	     * 
	     * 
	     * @param currentLineStyle
	     * @param currentLineShape
	     * @return
	     */
	    public static int getLineShapeImageId(int currentLineStyle, int currentLineShape)
	    {
	        int buttonId = R.drawable.btn_straight;
	    	
	        if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
	        {
	        	buttonId = R.drawable.btn_tree;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
	        {
	        	buttonId = R.drawable.btn_curve;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_DASH))
	        {
	        	buttonId = R.drawable.btn_straight_dash;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_NO_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_DASH))
	        {
	        	buttonId = R.drawable.btn_tree_dash;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_NO_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_DASH))
	        {
	        	buttonId = R.drawable.btn_curve_dash;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
	        {
	        	buttonId = R.drawable.btn_straight_rarrow;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
	        {
	        	buttonId = R.drawable.btn_tree_rarrow;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
	        {
	        	buttonId = R.drawable.btn_curve_rarrow;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_R_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_DASH))
	        {
	        	buttonId = R.drawable.btn_straight_rarrow_dash;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_TREESTYLE_R_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_DASH))
	        {
	        	buttonId = R.drawable.btn_tree_rarrow_dash;
	        }
	        else if ((currentLineStyle == LineStyleHolder.LINESTYLE_CURVESTYLE_R_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_DASH))
	        {
	        	buttonId = R.drawable.btn_curve_rarrow_dash;
	        }
	        /**
	        else  if ((currentLineStyle == LineStyleHolder.LINESTYLE_STRAIGHT_NO_ARROW)&&
	        		(currentLineShape == LineStyleHolder.LINESHAPE_NORMAL))
	        {
	        	buttonId = R.drawable.btn_straight;
	        }
	        **/
	        return (buttonId);
	    }
}
