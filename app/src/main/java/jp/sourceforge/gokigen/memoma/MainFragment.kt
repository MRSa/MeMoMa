package jp.sourceforge.gokigen.memoma

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class MainFragment: Fragment()
{
    private lateinit var myView : View
    private lateinit var sceneSelector: IChangeScene
    private lateinit var listener: IListener

    private fun prepare(sceneSelector: IChangeScene, listener: IListener)
    {
        try
        {
            this.sceneSelector = sceneSelector
            this.listener = listener
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        // メニューがあるよ
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        if (::myView.isInitialized)
        {
            // Viewを再利用。。。
            Log.v(TAG, "onCreateView() : called again, so do nothing... : $myView")
            return (myView)
        }
        val view: View = inflater.inflate(R.layout.main, container, false)
        myView = view
        setupMyView(myView)
        return (myView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        try
        {
            if (!listener.commandSelected(item.itemId))
            {
                return (super.onOptionsItemSelected(item))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    override fun onResume()
    {
        super.onResume()
        try
        {
            Log.v(TAG, "MainFragment::onResume()")
            listener.prepareToStart(myView)
            listener.updateContentList()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onPause()
    {
        super.onPause()
        try
        {
            Log.v(TAG, "MainFragment::onPause()")
            listener.shutdown()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setupMyView(view: View)
    {
        try
        {
            if (::listener.isInitialized)
            {
                listener.prepareListener(view)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = MainFragment::class.java.simpleName
        fun newInstance(sceneSelector: IChangeScene, listener: IListener):MainFragment
        {
            val instance = MainFragment()
            instance.prepare(sceneSelector, listener)

            // パラメータはBundleにまとめておく必要はある...
            val arguments = Bundle()
            instance.setArguments(arguments)

            return (instance)
        }
    }
}
