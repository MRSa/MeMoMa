package jp.sourceforge.gokigen.memoma;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 
 * @author MRSa
 *
 */
public class OperationModeHolder 
{
	private Activity activity = null;

    public static final int OPERATIONMODE_CREATE = 0;
    public static final int OPERATIONMODE_DELETE = 1;
    public static final int OPERATIONMODE_MOVE = 2;

	public OperationModeHolder(Activity arg)
	{
		activity = arg;
	}

	public void changeOperationMode(int value)
	{
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("operationMode", "" + value);
        editor.commit();
	}

    public int updateOperationMode(int buttonId)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    	int operationMode = Integer.parseInt(preferences.getString("operationMode", "0"));
   	 
    	if (buttonId == R.id.CreateObjectButton)
    	{
    		if (operationMode == OPERATIONMODE_CREATE)
    		{
    			operationMode = OPERATIONMODE_MOVE;
    		}
    		else
    		{
    			operationMode = OPERATIONMODE_CREATE;
    		}
    	}
    	else if (buttonId == R.id.DeleteObjectButton)
    	{
    		if (operationMode == OPERATIONMODE_DELETE)
    		{
    			operationMode = OPERATIONMODE_MOVE;
    		}
    		else
    		{
    			operationMode = OPERATIONMODE_DELETE;
    		}
    	}
    	changeOperationMode(operationMode);
    	
    	return (operationMode);
    }
}
