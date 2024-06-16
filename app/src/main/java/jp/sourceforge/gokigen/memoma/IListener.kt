package jp.sourceforge.gokigen.memoma

import android.view.View

interface IListener
{

    fun prepareListener(view: View)
    fun prepareToStart(view: View)
    fun updateContentList()
    fun shutdown()
    fun commandSelected(menuId: Int) : Boolean

}