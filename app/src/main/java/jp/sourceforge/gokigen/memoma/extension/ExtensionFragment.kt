package jp.sourceforge.gokigen.memoma.extension

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import jp.sourceforge.gokigen.memoma.R
import jp.sourceforge.gokigen.memoma.io.MeMoMaDataInOutManager

class ExtensionFragment : Fragment()
{
    private lateinit var parent : AppCompatActivity
    private lateinit var inOutManager: MeMoMaDataInOutManager
    private lateinit var myView : View
    private lateinit var listener: ExtensionFragmentListener

    private fun prepare(parent : AppCompatActivity, inOutManager: MeMoMaDataInOutManager)
    {
        try
        {
            Log.v(TAG, "ExtensionFragment::prepare() : ${parent.title}")
            try
            {
                this.inOutManager = inOutManager
                this.parent = parent
                if (!::listener.isInitialized)
                {
                    listener = ExtensionFragmentListener(parent, inOutManager)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun setDataTitle(title: String)
    {
        try
        {
            if (::listener.isInitialized)
            {
                listener.setDataTitle(title)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        try
        {
            Log.v(TAG, "ExtensionFragment::onCreate()")
            try
            {
                if (!::listener.isInitialized)
                {
                    listener = ExtensionFragmentListener(parent, inOutManager)
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            // メニューがあるよ
            setHasOptionsMenu(true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
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
        val view: View = inflater.inflate(R.layout.extensionview, container, false)
        myView = view
        setupMyView()
        return (myView)
    }

    private fun setupMyView()
    {
        try
        {
            if ((::listener.isInitialized)&&(::myView.isInitialized))
            {
                listener.prepareListener(myView)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)
    {
        try
        {
            listener.onCreateOptionsMenu(menu)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (super.onCreateOptionsMenu(menu, inflater))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return (listener.onOptionsItemSelected(item))
    }

    override fun onPrepareOptionsMenu(menu: Menu)
    {
        try
        {
            super.onPrepareOptionsMenu(menu)
            listener.onPrepareOptionsMenu(menu)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onPause()
    {
        try
        {
            super.onPause()

            // 動作を止めるようイベント処理クラスに指示する
            listener.shutdown()
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    override fun onResume()
    {
        try
        {
            super.onResume()
            if (::listener.isInitialized)
            {
                if (::myView.isInitialized)
                {
                    listener.prepareListener(myView)
                }
                listener.prepareToStart()
            }
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = ExtensionFragment::class.java.simpleName

        fun newInstance(activity: AppCompatActivity, inOutManager: MeMoMaDataInOutManager):ExtensionFragment
        {
            val instance = ExtensionFragment()
            instance.prepare(activity, inOutManager)
            return (instance)
        }
    }
}
