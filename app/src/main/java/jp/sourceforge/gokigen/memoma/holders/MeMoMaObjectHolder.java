package jp.sourceforge.gokigen.memoma.holders;

import java.util.Enumeration;
import java.util.Hashtable;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import jp.sourceforge.gokigen.memoma.Main;
import jp.sourceforge.gokigen.memoma.R;

/**
 *   表示オブジェクトの情報を保持するクラス
 * 
 * @author MRSa
 *
 */
public class MeMoMaObjectHolder
{
	public static final int ID_NOTSPECIFY = -1;
	
    public static final int DRAWSTYLE_RECTANGLE = 0;
    public static final int DRAWSTYLE_ROUNDRECT = 1;
    public static final int DRAWSTYLE_OVAL = 2;
    public static final int DRAWSTYLE_DIAMOND = 3;
    public static final int DRAWSTYLE_HEXAGONAL = 4;
    public static final int DRAWSTYLE_PARALLELOGRAM = 5;
    public static final int DRAWSTYLE_KEYBOARD = 6;
    public static final int DRAWSTYLE_PAPER = 7;
    public static final int DRAWSTYLE_DRUM = 8;
    public static final int DRAWSTYLE_CIRCLE = 9;
    public static final int DRAWSTYLE_NO_REGION = 10;
    
    public static final int DRAWSTYLE_LOOP_START = 11;
    public static final int DRAWSTYLE_LOOP_END = 12;
    public static final int DRAWSTYLE_LEFT_ARROW = 13;
    public static final int DRAWSTYLE_DOWN_ARROW = 14;
    public static final int DRAWSTYLE_UP_ARROW = 15;
    public static final int DRAWSTYLE_RIGHT_ARROW = 16;

    public static final float ROUNDRECT_CORNER_RX = 8;
    public static final float ROUNDRECT_CORNER_RY = 8;
    
    public static final float STOROKE_BOLD_WIDTH = 3.5f;
    public static final float STOROKE_NORMAL_WIDTH = 0.0f;
    
    public static final float DUPLICATEPOSITION_MARGIN = 15.0f;

    public static final float OBJECTSIZE_DEFAULT_X = 144.0f;
	public static final float OBJECTSIZE_DEFAULT_Y = (OBJECTSIZE_DEFAULT_X / 16.0f * 9.0f);

	public static final float OBJECTSIZE_MINIMUM_X = 48.0f;
	public static final float OBJECTSIZE_MINIMUM_Y = (OBJECTSIZE_MINIMUM_X / 16.0f * 9.0f);
	
	public static final float OBJECTSIZE_MAXIMUM_X = 14400.0f;
	public static final float OBJECTSIZE_MAXIMUM_Y =  (OBJECTSIZE_MAXIMUM_X / 16.0f * 9.0f);

	public static final float OBJECTSIZE_STEP_X = OBJECTSIZE_MINIMUM_X * 1.0f;
	public static final float OBJECTSIZE_STEP_Y = OBJECTSIZE_MINIMUM_Y * 1.0f;
	
	public static final float FONTSIZE_DEFAULT = 12.0f;
	
    private MeMoMaConnectLineHolder connectLineHolder = null;
    
	private Hashtable<Integer, PositionObject> objectPoints = null;
	private Integer serialNumber = 1;
	private String  dataTitle = "";
	private String  background = "";
	private Context parent;
	private final IOperationHistoryHolder historyHolder;

    public MeMoMaObjectHolder(Context context, MeMoMaConnectLineHolder lineHolder, IOperationHistoryHolder historyHolder)
    {
		  objectPoints = new Hashtable<>();
		  connectLineHolder = lineHolder;
		  parent = context;
		  this.historyHolder = historyHolder;
    }

    /**
     *    データの有無を見る (true の場合、データはない。)
     * 
     *
     */
    public boolean isEmpty()
    {
		return (((connectLineHolder == null)||(objectPoints == null))||(objectPoints.isEmpty()));
    }
    
    public MeMoMaConnectLineHolder getConnectLineHolder()
    {
    	return (connectLineHolder);
    }
    
    public void setDataTitle(String title)
    {
    	dataTitle = title;
    }
    
    public String getDataTitle()
    {
    	return (dataTitle);
    }

    public void setBackground(String data)
    {
    	background = data;
    }
    
    public String getBackground()
    {
    	return (background);
    }

    public int getCount()
    {
    	return (objectPoints.size());
    }

    public Enumeration<Integer> getObjectKeys()
    {
    	return (objectPoints.keys());
    }

    public PositionObject getPosition(Integer key)
    {
        return  (objectPoints.get(key));
    }

    public boolean removePosition(Integer key)
    {
        PositionObject removeTarget = objectPoints.remove(key);
        if (removeTarget != null)
        {
            historyHolder.addHistory(key, IOperationHistoryHolder.ChangeKind.DELETE_OBJECT, removeTarget);
        }
    	Log.v(Main.APP_IDENTIFIER, "REMOVE : " + key);
    	return (true);
    }
    
    public void removeAllPositions()
    {
        objectPoints.clear();
        serialNumber = 1;
    }

    public void setSerialNumber(int id)
    {
    	serialNumber = (id == ID_NOTSPECIFY) ? ++serialNumber : id;
    }
    
    public int getSerialNumber()
    {
    	return (serialNumber);
    }

    public void dumpPositionObject(PositionObject position)
    {
    	if (position == null)
    	{
    		return;
    	}
    	RectF posRect = position.getRect();
        Log.v(Main.APP_IDENTIFIER, "[" + posRect.left + "," + posRect.top + "][" + posRect.right + "," + posRect.bottom + "] " + "label : " + position.getLabel() + " detail : " + position.getDetail());
    }
    
    
    /**
     *   オブジェクトを複製する。
     * 
     *
     *
     */
    public PositionObject duplicatePosition(int key)
    {
    	PositionObject orgPosition = objectPoints.get(key);
    	if (orgPosition == null)
    	{
    		// 元のオブジェクトが見つからなかったので、何もせずに戻る
    		return (null);
    	}
    	RectF orgRect = orgPosition.getRect();
    	PositionObject position = new PositionObject(serialNumber,
                new RectF(orgRect.left + DUPLICATEPOSITION_MARGIN, orgRect.top + DUPLICATEPOSITION_MARGIN, orgRect.right + DUPLICATEPOSITION_MARGIN, orgRect.bottom + DUPLICATEPOSITION_MARGIN),
                orgPosition.getDrawStyle(),
                orgPosition.getIcon(),
                orgPosition.getLabel(),
                orgPosition.getDetail(),
                orgPosition.getUserChecked(),
                orgPosition.getLabelColor(),
                orgPosition.getObjectColor(),
                orgPosition.getPaintStyle(),
                orgPosition.getstrokeWidth(),
                orgPosition.getFontSize(),
                historyHolder);
		objectPoints.put(serialNumber, position);
		serialNumber++;
		return (position);
    }

    public PositionObject createPosition(int id)
    {
    	PositionObject position = new PositionObject(id,
                new RectF(0, 0, OBJECTSIZE_DEFAULT_X, OBJECTSIZE_DEFAULT_Y),
                MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE,
                0,
                "",
                "",
                false,
                Color.WHITE,
                Color.WHITE,
                Paint.Style.STROKE.toString(),
                STOROKE_NORMAL_WIDTH,
                FONTSIZE_DEFAULT,
                historyHolder);
		objectPoints.put(id, position);
		return (position);    	
    }
    
    public PositionObject createPosition(float x, float y, int drawStyle)
    {
    	PositionObject position = createPosition(serialNumber);
    	RectF posRect = position.getRect();
    	position.setRectLeft(posRect.left + x);
    	position.setRectRight(posRect.right + x);
    	position.setRectTop(posRect.top + y);
    	position.setRectBottom(posRect.bottom + y);
    	position.setDrawStyle(drawStyle);
		serialNumber++;
		return (position);
    }

    /**
     *   オブジェクトのサイズを拡大する
     *
     */
    public void expandObjectSize(Integer key)
    {
    	PositionObject position = objectPoints.get(key);
    	if (position == null)
    	{
    		// 元のオブジェクトが見つからなかったので、何もせずに戻る
    		return;
    	}
    	RectF posRect = position.getRect();
        float width = posRect.right - posRect.left;
        float height = posRect.bottom - posRect.top;
        if (((width + (OBJECTSIZE_STEP_X * 2.0f)) > OBJECTSIZE_MAXIMUM_X)||((height + (OBJECTSIZE_STEP_Y * 2.0f)) > OBJECTSIZE_MAXIMUM_Y))
        {
            // 拡大リミットだった。。。拡大しない
    		String outputMessage = parent.getString(R.string.object_bigger_limit) + " ";
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
            return;
        }
        position.setRectLeft(posRect.left - OBJECTSIZE_STEP_X);
        position.setRectRight(posRect.right + OBJECTSIZE_STEP_X);
        position.setRectTop(posRect.top - OBJECTSIZE_STEP_Y);
        position.setRectBottom(posRect.bottom + OBJECTSIZE_STEP_Y);
    }

    /**
     *   オブジェクトのサイズを縮小する
     * 
     *
     */
    public void shrinkObjectSize(Integer key)
    {
    	PositionObject position = objectPoints.get(key);
    	if (position == null)
    	{
    		// 元のオブジェクトが見つからなかったので、何もせずに戻る
    		return;
    	}
    	RectF posRect = position.getRect();
        float width = posRect.right - posRect.left;
        float height = posRect.bottom - posRect.top;
        if (((width - (OBJECTSIZE_STEP_X * 2.0f)) < OBJECTSIZE_MINIMUM_X)||((height - (OBJECTSIZE_STEP_Y * 2.0f)) < OBJECTSIZE_MINIMUM_Y))
        {
            // 縮小リミットだった。。。縮小しない
    		String outputMessage = parent.getString(R.string.object_small_limit) + " ";
            Toast.makeText(parent, outputMessage, Toast.LENGTH_SHORT).show();
            return;
        }
        position.setRectLeft(posRect.left + OBJECTSIZE_STEP_X);
        position.setRectRight(posRect.right - OBJECTSIZE_STEP_X);
        position.setRectTop(posRect.top + OBJECTSIZE_STEP_Y);
        position.setRectBottom(posRect.bottom - OBJECTSIZE_STEP_Y);
    }

    public MeMoMaConnectLineHolder getLineHolder()
	{
		return (connectLineHolder);
	}
	
	static public int getObjectDrawStyleIcon(int drawStyle)
	{
		int icon = 0;
    	if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RECTANGLE)
    	{
    		icon  = R.drawable.btn_rectangle;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_ROUNDRECT)
    	{
    		icon = R.drawable.btn_roundrect;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_OVAL)
    	{
    		icon = R.drawable.btn_oval;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DIAMOND)
    	{
    		icon = R.drawable.btn_diamond;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_HEXAGONAL)
    	{
    		icon = R.drawable.btn_hexagonal;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PARALLELOGRAM)
    	{
    		icon = R.drawable.btn_parallelogram;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_KEYBOARD)
    	{
    		icon = R.drawable.btn_keyboard;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_PAPER)
    	{
    		icon = R.drawable.btn_paper;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DRUM)
    	{
    		icon = R.drawable.btn_drum;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_CIRCLE)
    	{
    		icon = R.drawable.btn_circle;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_NO_REGION)
    	{
    		icon = R.drawable.btn_noregion;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_START)
    	{
    		icon = R.drawable.btn_trapezoidy_up;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LOOP_END)
    	{
    		icon = R.drawable.btn_trapezoidy_down;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_LEFT_ARROW)
    	{
    		icon = R.drawable.btn_arrow_left;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_DOWN_ARROW)
    	{
    		icon = R.drawable.btn_arrow_down;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_UP_ARROW)
    	{
    		icon = R.drawable.btn_arrow_up;
    	}
    	else if (drawStyle == MeMoMaObjectHolder.DRAWSTYLE_RIGHT_ARROW)
    	{
    		icon = R.drawable.btn_arrow_right;
    	}
    	return (icon);
	}
}
