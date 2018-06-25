package jp.sourceforge.gokigen.memoma;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SymbolListArrayAdapter extends ArrayAdapter<SymbolListArrayItem>
{
    private LayoutInflater inflater = null;
    private int textViewResourceId = 0;
    private List<SymbolListArrayItem> listItems = null;
    
    /**
     * コンストラクタ
     */
    public SymbolListArrayAdapter(Context context, int textId, List<SymbolListArrayItem> items)
    {
        super(context, textId, items);

        // リソースIDと表示アイテム
        textViewResourceId = textId;
        listItems = items;

        // ContextからLayoutInflaterを取得
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    /**
     * 
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;
        if(convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = inflater.inflate(textViewResourceId, null);
        }

        SymbolListArrayItem item = listItems.get(position);
        
        ImageView imageView = (ImageView) view.findViewWithTag("icon");
        imageView.setImageResource(item.getIconResource());

        int subIcon = item.getSubIconResource();
        //if (subIcon != 0)
        {
            ImageView subImage = (ImageView) view.findViewWithTag("subIcon");
            subImage.setImageResource(subIcon);
        }            

        TextView titleView = (TextView)view.findViewWithTag("title");
        titleView.setText(item.getTextResource1st());

        TextView detailView = (TextView)view.findViewWithTag("detail");
        detailView.setText(item.getTextResource2nd());

        /**
        TextView optionView = (TextView)view.findViewWithTag("option");
        optionView.setText(item.getTextResource3rd());
        **/
        return (view);
    }
}
