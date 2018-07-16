package jp.sourceforge.gokigen.memoma.operations;

import android.content.Context;

import jp.sourceforge.gokigen.memoma.R;
import jp.sourceforge.gokigen.memoma.dialogs.ItemSelectionDialog;

/**
 * 
 * 
 * @author MRSa
 *
 */
public class ObjectOperationCommandHolder implements ItemSelectionDialog.ISelectionItemHolder
{
    public static final int OBJECTOPERATION_DELETE = 0;
    public static final int OBJECTOPERATION_DUPLICATE =1;
    public static final int OBJECTOPERATION_SIZEBIGGER = 2;
    public static final int OBJECTOPERATION_SIZESMALLER = 3;
//    public static final int OBJECTOPERATION_BRINGTOP = 4;
	
	private Context parent = null;
	
	/**
	 *    コンストラクタ
	 * 
	 */
    public ObjectOperationCommandHolder(Context context)
    {
    	parent = context;
    }	

    /**
     * 
     * 
     */
    public boolean isMultipleSelection()
	{
		return (false);
		
	}

    /**
     * 
     * 
     */
	public String[] getItems()
	{
        String[] ret = new String[4];
        ret[0] = parent.getString(R.string.object_delete);
        ret[1] = parent.getString(R.string.object_duplicate);
        ret[2] = parent.getString(R.string.object_bigger);
        ret[3] = parent.getString(R.string.object_smaller);
        //ret[4] = parent.getString(R.string.object_bringtop);

        return (ret);
	}
	
	/**
	 * 
	 * 
	 */
    public String  getItem(int index)
    {
    	String message = "";
    	switch (index)
    	{
          case OBJECTOPERATION_DELETE:
    		message = parent.getString(R.string.object_delete);
    		break;
          case OBJECTOPERATION_DUPLICATE:
      		message = parent.getString(R.string.object_duplicate);
      		break;
          case OBJECTOPERATION_SIZEBIGGER:
        	message = parent.getString(R.string.object_bigger);
        	break;
          case OBJECTOPERATION_SIZESMALLER:
        	message = parent.getString(R.string.object_smaller);
         	break;
/*
          case OBJECTOPERATION_BRINGTOP:
        	message = parent.getString(R.string.object_bringtop);
         	break;
*/
          default:
         	break;
    	}
    	return (message);
    }

    /** 複数選択時に使用する **/
    
    /**
     * 
     */
    public boolean[] getSelectionStatus()
    {
    	return (null);
    }
    
    /**
     * 
     */
    public void setSelectionStatus(int index, boolean isSelected)
    {
    	  // なにもしない
    }
}
