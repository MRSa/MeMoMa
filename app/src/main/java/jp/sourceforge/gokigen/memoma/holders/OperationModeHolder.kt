package jp.sourceforge.gokigen.memoma.holders

import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import jp.sourceforge.gokigen.memoma.R

class OperationModeHolder(private val activity: AppCompatActivity)
{
    fun changeOperationMode(value: Int)
    {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = preferences.edit()
        editor.putString("operationMode", "" + value)
        editor.apply()
    }

    fun updateOperationMode(buttonId: Int): Int{
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        var operationMode = (preferences.getString("operationMode", "0")?:"0").toInt()

        if (buttonId == R.id.CreateObjectButton)
        {
            operationMode = if (operationMode == OPERATIONMODE_CREATE)
            {
                OPERATIONMODE_MOVE
            }
            else
            {
                OPERATIONMODE_CREATE
            }
        } else if (buttonId == R.id.DeleteObjectButton) {
            operationMode = if (operationMode == OPERATIONMODE_DELETE) {
                OPERATIONMODE_MOVE
            } else {
                OPERATIONMODE_DELETE
            }
        }
        changeOperationMode(operationMode)

        return (operationMode)
    }

    companion object {
        const val OPERATIONMODE_CREATE: Int = 0
        const val OPERATIONMODE_DELETE: Int = 1
        const val OPERATIONMODE_MOVE: Int = 2
    }
}