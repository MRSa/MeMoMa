package jp.sourceforge.gokigen.memoma.listitem

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class SymbolListArrayAdapter(
    context: Context,
    private val textViewResourceId: Int,
    private val listItems: List<SymbolListArrayItem>
) : ArrayAdapter<SymbolListArrayItem?>(context, textViewResourceId, listItems)
{
    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
    {
        val view = convertView ?: inflater.inflate(textViewResourceId, null)
        try
        {
            val item = listItems[position]
            val imageView = view.findViewWithTag<ImageView>("icon")
            imageView.setImageResource(item.getIconResource())
            val subIcon = item.getSubIconResource()
            run {
                val subImage =
                    view.findViewWithTag<ImageView>("subIcon")
                subImage.setImageResource(subIcon)
            }

            val titleView = view.findViewWithTag<TextView>("title")
            titleView.setTextColor(Color.LTGRAY)
            titleView.text = item.getTextResource1st()

            val detailView = view.findViewWithTag<TextView>("detail")
            detailView.setTextColor(Color.LTGRAY)
            detailView.text = item.getTextResource2nd()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (view)
    }
}
