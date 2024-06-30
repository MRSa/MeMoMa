package jp.sourceforge.gokigen.memoma

import android.app.Application
import android.util.Log
import jp.sourceforge.gokigen.memoma.holders.MeMoMaObjectHolder

class AppSingleton: Application()
{
    override fun onCreate()
    {
        super.onCreate()

        objectHolder = MeMoMaObjectHolder(applicationContext)

        Log.v(TAG, "AppSingleton::create()")
    }

    companion object
    {
        private val TAG = AppSingleton::class.java.simpleName
        lateinit var objectHolder : MeMoMaObjectHolder
    }
}
