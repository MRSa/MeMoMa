package jp.sourceforge.gokigen.memoma;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton;

/**
 *   オブジェクトのデータを入力するダイアログを表示する
 * 
 * @author MRSa
 *
 */
public class ObjectDataInputDialog implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, ImageButton.OnClickListener
{
	private final float FONTSIZE_SMALL = 4.0f;
	private final float FONTSIZE_MIDDLE = 6.0f;
	private final float FONTSIZE_LARGE = 10.0f;
	
	private Context context = null;	
	private IResultReceiver resultReceiver = null;
	private MeMoMaObjectHolder objectHolder = null;
	private Integer key = 0;
	
	private View dialogLayout = null;
	private TextView  colorBorderAreaView = null;
	private SeekBar borderColorView = null;
    private CheckBox  fillObjectView = null;
    private GradientDrawable backgroundShape = null;
    private int backgroundColor = MeMoMaCanvasDrawer.BACKGROUND_COLOR_DEFAULT;	
    private int currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE;
    private float textFontSize = 6.0f;
	
	public ObjectDataInputDialog(Context arg, MeMoMaObjectHolder holder)
	{
		context = arg;
		objectHolder = holder;
	}

	public void setResultReceiver(IResultReceiver receiver)
	{
		resultReceiver = receiver;
	}
	
    /**
     *   確認ダイアログを応答する
     * @return
     */
    public Dialog getDialog()
    {
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = inflater.inflate(R.layout.objectinput, null);
        dialogLayout = layout;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //  ダイアログで表示するデータを設定する場所
        
        // 背景色を設定する
        try
        {
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String colorString = preferences.getString("backgroundColor", "0xff004000");
            backgroundColor = Color.parseColor(colorString);
        }
        catch (Exception e)
        {
            backgroundColor = MeMoMaCanvasDrawer.BACKGROUND_COLOR_DEFAULT;
        }

        final TextView colorLabel = (TextView) layout.findViewById(R.id.setBorderColorLabel);
        backgroundShape = (GradientDrawable)colorLabel.getBackground();

        // 入力文字列の色を設定する
        final EditText label = (EditText) layout.findViewById(R.id.labelInputArea);
       // label.setTextColor(Color.LTGRAY);

        final EditText detail = (EditText) layout.findViewById(R.id.descriptionInputArea);
        //detail.setTextColor(Color.LTGRAY);

        borderColorView = (SeekBar) layout.findViewById(R.id.borderColorSelectionBar);
        borderColorView.setOnSeekBarChangeListener(this);        
        
        final CheckBox userCheckbox = (CheckBox) layout.findViewById(R.id.checkUserCheckbox);
        final CheckBox boldText = (CheckBox) layout.findViewById(R.id.checkBoldText);
        fillObjectView = (CheckBox) layout.findViewById(R.id.checkFillObject);
        fillObjectView.setOnCheckedChangeListener(this);

        colorBorderAreaView = (TextView) layout.findViewById(R.id.borderColorArea);
        colorBorderAreaView.setOnClickListener(this);
        
        final ImageButton rect = (ImageButton) layout.findViewById(R.id.btnObjectRectangle);
        rect.setOnClickListener(this);
        final ImageButton roundRect = (ImageButton) layout.findViewById(R.id.btnObjectRoundRect);
        roundRect.setOnClickListener(this);
        final ImageButton oval = (ImageButton) layout.findViewById(R.id.btnObjectOval);
        oval.setOnClickListener(this);
        final ImageButton diamond = (ImageButton) layout.findViewById(R.id.btnObjectDiamond);
        diamond.setOnClickListener(this);
        final ImageButton hexagonal = (ImageButton) layout.findViewById(R.id.btnObjectHexagonal);
        hexagonal.setOnClickListener(this);
        final ImageButton parallelogram = (ImageButton) layout.findViewById(R.id.btnObjectParallelogram);
        parallelogram.setOnClickListener(this);
        final ImageButton keyboard = (ImageButton) layout.findViewById(R.id.btnObjectKeyboard);
        keyboard.setOnClickListener(this);
        final ImageButton paper = (ImageButton) layout.findViewById(R.id.btnObjectPaper);
        paper.setOnClickListener(this);
        final ImageButton drum = (ImageButton) layout.findViewById(R.id.btnObjectDrum);
        drum.setOnClickListener(this);
        final ImageButton circle = (ImageButton) layout.findViewById(R.id.btnObjectCircle);
        circle.setOnClickListener(this);
        final ImageButton noregion = (ImageButton) layout.findViewById(R.id.btnObjectNoRegion);
        noregion.setOnClickListener(this);

        final ImageButton loopStart = (ImageButton) layout.findViewById(R.id.btnObjectLoopStart);
        loopStart.setOnClickListener(this);
        final ImageButton loopEnd = (ImageButton) layout.findViewById(R.id.btnObjectLoopEnd);
        loopEnd.setOnClickListener(this);
        final ImageButton leftArrow = (ImageButton) layout.findViewById(R.id.btnObjectLeftArrow);
        leftArrow.setOnClickListener(this);
        final ImageButton downArrow = (ImageButton) layout.findViewById(R.id.btnObjectDownArrow);
        downArrow.setOnClickListener(this);
        final ImageButton upArrow = (ImageButton) layout.findViewById(R.id.btnObjectUpArrow);
        upArrow.setOnClickListener(this);
        final ImageButton rightArrow = (ImageButton) layout.findViewById(R.id.btnObjectRightArrow);
        rightArrow.setOnClickListener(this);

        // 背景の色を調整（塗りつぶしの時はオブジェクトの色とする。）
        int color = convertColor(borderColorView.getProgress());
		colorBorderAreaView.setBackgroundColor((fillObjectView.isChecked() == true) ? color : backgroundColor);
		//backgroundShape.setStroke(2, color);

		if (fillObjectView.isChecked() == true)
		{
			// 塗りつぶし時は文字の色を変える。
            color = (color ^ 0x00ffffff);
		}
		colorBorderAreaView.setTextColor(color);
		colorBorderAreaView.setText(context.getString(R.string.labelTextColorSample));
		
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.confirmYes), new DialogInterface.OnClickListener()
              {
                   public void onClick(DialogInterface dialog, int id)
                   {
                	   boolean ret = false;
                	   boolean isUserCheck = false;
                	   if (userCheckbox != null)
                	   {
                		   isUserCheck = userCheckbox.isChecked();
                	   }
                	   setObjectData(label.getText().toString(), detail.getText().toString(), borderColorView.getProgress(), boldText.isChecked(), fillObjectView.isChecked(), isUserCheck, currentObjectDrawStyle);
                	   if (resultReceiver != null)
                	   {
                	       resultReceiver.finishObjectInput();
                	   }
                       if (ret == true)
                       {
                    	   dialog.dismiss();
                       }
                       else
                       {
                           dialog.cancel();
                       }
                       System.gc();
                   }
               });
        builder.setNegativeButton(context.getString(R.string.confirmNo), new DialogInterface.OnClickListener()
               {
                   public void onClick(DialogInterface dialog, int id)
                   {
                	   boolean ret = false;
                	   if (resultReceiver != null)
                	   {
                	       resultReceiver.cancelObjectInput();
                	   }
                       if (ret == true)
                       {
                    	   dialog.dismiss();
                       }
                       else
                       {
                           dialog.cancel();
                       }
                       System.gc();
                   }
               });
        return (builder.create());    	
    }

    /**
     *    オブジェクト入力用ダイアログの表示を準備する
     * 
     */
    public void prepareObjectInputDialog(Dialog dialog, Integer objectKey)
    {
        MeMoMaObjectHolder.PositionObject position = objectHolder.getPosition(objectKey);
    	key = objectKey;
        if (position != null)
        {
        	// 色を設定する
            final SeekBar borderColorProgess = (SeekBar) dialog.findViewById(R.id.borderColorSelectionBar);
            borderColorProgess.setProgress(convertProgress(position.objectColor));

            final CheckBox  boldTextCheck = (CheckBox) dialog.findViewById(R.id.checkBoldText);
            boldTextCheck.setChecked(position.strokeWidth == MeMoMaObjectHolder.STOROKE_BOLD_WIDTH);

            final CheckBox  fillObjectCheck = (CheckBox) dialog.findViewById(R.id.checkFillObject);
            fillObjectCheck.setChecked(Paint.Style.valueOf(position.paintStyle) != Paint.Style.STROKE);

            // フォントサイズを設定する
            textFontSize = position.fontSize / 2.0f;
            
        	// 入力文字列を設定する
            final EditText targetLabel = (EditText) dialog.findViewById(R.id.labelInputArea);
            targetLabel.setText(position.label);

            final EditText targetDetail = (EditText) dialog.findViewById(R.id.descriptionInputArea);
            targetDetail.setText(position.detail);
            
        	//  設定に記録されているデータを画面に反映させる
        	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String userCheckboxTitle = preferences.getString("userCheckboxString", "");

            // 描画スタイルを設定する
            currentObjectDrawStyle = position.drawStyle;
        	updateObjectDrawStyleImageButton(currentObjectDrawStyle);
            
            // 背景色を設定する
            try
            {
                String colorString = preferences.getString("backgroundColor", "0xff004000");
                backgroundColor = Color.parseColor(colorString);
            }
            catch (Exception e)
            {
                backgroundColor = MeMoMaCanvasDrawer.BACKGROUND_COLOR_DEFAULT;
            }
            setTextColorSample(borderColorProgess.getProgress(), textFontSize, fillObjectView.isChecked());

            final CheckBox  userCheckbox = (CheckBox) dialog.findViewById(R.id.checkUserCheckbox);
            userCheckbox.setEnabled(true);
            userCheckbox.setText(userCheckboxTitle);
            userCheckbox.setChecked(position.userChecked);
        }
    }
    
    /**
     *    オブジェクトにデータを設定する
     * @param label
     * @param detail
     * @param labelColor
     * @param borderColor
     * @param size
     */
    public void setObjectData(String label, String detail, int progress, boolean boldText, boolean fillObject, boolean userCheck, int drawStyle)
    {
    	MeMoMaObjectHolder.PositionObject positionObject = objectHolder.getPosition(key);
    	if (positionObject != null)
    	{
            positionObject.label = label;
            positionObject.detail = detail;
    		int color = convertColor(progress);
            positionObject.objectColor = color;
    		color = (color ^ 0x00ffffff);
        	positionObject.fontSize = textFontSize * 2.0f;
            positionObject.labelColor = color;
        	positionObject.strokeWidth = (boldText == true) ? MeMoMaObjectHolder.STOROKE_BOLD_WIDTH : MeMoMaObjectHolder.STOROKE_NORMAL_WIDTH;
        	positionObject.paintStyle = ((fillObject == true) ? Paint.Style.FILL : Paint.Style.STROKE).toString();
        	positionObject.userChecked = userCheck;
        	
        	if (positionObject.drawStyle != drawStyle)
        	{
        	    if ((drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE)||
        	    	(drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_LEFT_ARROW)||
        	    	(drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_DOWN_ARROW)||
        	    	(drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_UP_ARROW)||
        	    	(drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_RIGHT_ARROW))
        	    {
        	    	// (長方形の形状から)正方形の形状にする場合...
        	    	setRectToSquare(positionObject);        	    	
        	    }
        	    else if ((positionObject.drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE)||
                            (positionObject.drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_LEFT_ARROW)||
                            (positionObject.drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_DOWN_ARROW)||
                            (positionObject.drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_UP_ARROW)||
                            (positionObject.drawStyle == MeMoMaObjectHolder.	DRAWSTYLE_RIGHT_ARROW))
        	    {
        	    	// 正方形の形状から、長方形の形状にする場合...
        	    	setRectFromSquare(positionObject);
        	    }  	
        	    positionObject.drawStyle = drawStyle;
        	}
    	}
    	positionObject = null;
    }

    /**
     *   オブジェクトの領域を長方形から正方形にする
     */
    private void setRectToSquare(MeMoMaObjectHolder.PositionObject positionObject)
    {
           float bandWidth = ((positionObject.rect.right -  positionObject.rect.left)) / 2.0f;
           float center = positionObject.rect.centerY();
           
           positionObject.rect.top = center - bandWidth;
           positionObject.rect.bottom = center + bandWidth;
    }

    /**
     *   オブジェクトの領域を正方形から長方形にする
     */
    private void setRectFromSquare(MeMoMaObjectHolder.PositionObject positionObject)
    {
        float bandWidth = ((positionObject.rect.right -  positionObject.rect.left) / 16.0f * 9.0f) / 2.0f;
        float center = positionObject.rect.centerY();
        
        positionObject.rect.top = center - bandWidth;
        positionObject.rect.bottom = center + bandWidth;
    }

    private void setButtonBorder(int id, boolean isHighlight)
    {
    	try
    	{
            ImageButton button = (ImageButton) dialogLayout.findViewById(id);
            //GradientDrawable btnBackgroundShape = (GradientDrawable)button.getBackground();
            BitmapDrawable btnBackgroundShape = (BitmapDrawable)button.getBackground();
            if (isHighlight == true)
            {
//               	btnBackgroundShape.setColorFilter(Color.rgb(51, 181, 229), Mode.LIGHTEN);
            	btnBackgroundShape.setColorFilter(Color.BLUE, Mode.LIGHTEN);
            }
            else
            {
            	btnBackgroundShape.setColorFilter(Color.BLACK, Mode.LIGHTEN);
            } 
    	}
    	catch (Exception ex)
    	{
    		// 
    		Log.v(Main.APP_IDENTIFIER, "setButtonBorder(): " + ex.toString());
    	}
    	
    }
    
    /**
     *    イメージボタンの選択状態を更新する
     * 
     * @param drawStyle
     */
    private void updateObjectDrawStyleImageButton(int drawStyle)
    {
    	setButtonBorder(R.id.btnObjectRectangle, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE));
    	setButtonBorder(R.id.btnObjectRoundRect, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT));
    	setButtonBorder(R.id.btnObjectOval, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_OVAL));
    	setButtonBorder(R.id.btnObjectDiamond, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DIAMOND));
    	setButtonBorder(R.id.btnObjectHexagonal, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL));
    	setButtonBorder(R.id.btnObjectParallelogram, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM));
    	setButtonBorder(R.id.btnObjectKeyboard, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD));
    	setButtonBorder(R.id.btnObjectPaper, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PAPER));
    	setButtonBorder(R.id.btnObjectDrum, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DRUM));
    	setButtonBorder(R.id.btnObjectCircle, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE));
    	setButtonBorder(R.id.btnObjectNoRegion, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_NO_REGION));
    	setButtonBorder(R.id.btnObjectLoopStart, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_START));
    	setButtonBorder(R.id.btnObjectLoopEnd, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_END));
    	setButtonBorder(R.id.btnObjectLeftArrow, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW));
    	setButtonBorder(R.id.btnObjectDownArrow, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW));
    	setButtonBorder(R.id.btnObjectUpArrow, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW));
    	setButtonBorder(R.id.btnObjectRightArrow, (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW));
     }
    
    /**
     *    ボタンが押された時の処理...
     * 
     */
    public void onClick(View v)
    {
    	int id = v.getId();
    	if (id == R.id.borderColorArea)
    	{
    		// フォントサイズを変更する...
    		if (textFontSize == FONTSIZE_MIDDLE)
    		{
    			textFontSize = FONTSIZE_LARGE;
    		}
    		else if (textFontSize == FONTSIZE_LARGE)
    		{
    			textFontSize = FONTSIZE_SMALL;
    		}
    		else // if (textFontSize == FONTSIZE_SMALL)
    		{
    			textFontSize = FONTSIZE_MIDDLE;
    		}
            setTextColorSample(borderColorView.getProgress(), textFontSize, fillObjectView.isChecked());
    		return;
    	}
    	
    	switch (id)
    	{
    	  case R.id.btnObjectRoundRect:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT;
        	  break;
    	  case R.id.btnObjectOval:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_OVAL;
        	  break;
    	  case R.id.btnObjectDiamond:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_DIAMOND;
        	  break;
    	  case R.id.btnObjectHexagonal:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL;
        	  break;
    	  case R.id.btnObjectParallelogram:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM;
        	  break;
    	  case R.id.btnObjectKeyboard:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD;
        	  break;
    	  case R.id.btnObjectPaper:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_PAPER;
        	  break;
    	  case R.id.btnObjectDrum:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_DRUM;
        	  break;
    	  case R.id.btnObjectNoRegion:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_NO_REGION;
        	  break;
    	  case R.id.btnObjectCircle:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_CIRCLE;
        	  break;
    	  case R.id.btnObjectLoopStart:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_LOOP_START;
        	  break;
    	  case R.id.btnObjectLoopEnd:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_LOOP_END;
        	  break;
    	  case R.id.btnObjectLeftArrow:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW;
        	  break;
    	  case R.id.btnObjectDownArrow:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW;
        	  break;
    	  case R.id.btnObjectUpArrow:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW;
        	  break;
    	  case R.id.btnObjectRightArrow:
    		  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW;
        	  break;
    	  case R.id.btnObjectRectangle:	
          default:
        	  currentObjectDrawStyle = MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE;
        	  break;
    	}
    	updateObjectDrawStyleImageButton(currentObjectDrawStyle);
    }

    /**
     *   0x00～0x3fの値(R, G, B, それぞれ2ビット)で色を変える
     * 
     * @param value
     * @return
     */
    private int convertColor(int value)
    {
    	int color = 0;	
    	int r = ((value >> 4 ) & 0x03) * 85;
    	int g = ((value >> 2) & 0x03) * 85;
    	int b = (value & 0x03)  * 85;
        color = Color.rgb(r, g, b);
        //Log.v(Main.APP_IDENTIFIER, "convertColor()  R : " + r + ", G : " + g + ", B : " + b + "color : " + color);
        return (color);
    }

    /**
     *    色をプログレスバーの値に変換する
     *    
     * @param color
     * @return
     */
    private int convertProgress(int color)
    {
    	int r = Color.red(color) / 85;
    	int g = Color.green(color) / 85;
    	int b = Color.blue(color) / 85;
    	
    	return ((r << 4) + (g << 2) + b);    	
    }
    
    /**
     *   背景色を設定する処理
     *   
     * @param progress
     * @param fontSize
     * @param value
     */
    private void setTextColorSample(int progress, float fontSize, boolean value)
    {
    	if (colorBorderAreaView != null)
    	{
    		int color = convertColor(progress);
    		int backColor = (value == true) ? color : backgroundColor;
    		colorBorderAreaView.setBackgroundColor(backColor);
    		
    		backgroundShape.setStroke(2, color);
    		
    		if (value == true)
    		{
    			// 塗りつぶし時には色を変える
    		    color = (color ^ 0x00ffffff);
    		}
    		colorBorderAreaView.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSize);
    		colorBorderAreaView.setTextColor(color);
    		colorBorderAreaView.setText(context.getString(R.string.labelTextColorSample));
    	}
    }
    
    /**
     *   チェックボックスの値が変更された時の処理
     * 
     */
    public void onCheckedChanged(CompoundButton view, boolean value)
    {
    	int id = view.getId();
    	if ((id == R.id.checkFillObject)&&(borderColorView != null))
    	{
    		setTextColorSample(borderColorView.getProgress(), textFontSize, value);
    	}    	
    }
    /**
     *    プログレスバーで値を変更された時の処理
     * 
     */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {
    	int id = seekBar.getId();
        //Log.v(Main.APP_IDENTIFIER, "onProgressChanged() : " + progress + " id : " + id);
        
    	if ((id == R.id.borderColorSelectionBar)&&(fillObjectView != null))
    	{
    		setTextColorSample(progress, textFontSize, fillObjectView.isChecked());
    	}
    }
    
    public void onStartTrackingTouch(SeekBar seekBar)
    {
        //	
    }

    public void onStopTrackingTouch(SeekBar seekBar)
    {
        //
    }    
    
    public interface IResultReceiver
    {
        public abstract void finishObjectInput();
        public abstract void cancelObjectInput();
    }
}
