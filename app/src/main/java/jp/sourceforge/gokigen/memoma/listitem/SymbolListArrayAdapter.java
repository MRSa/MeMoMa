package jp.sourceforge.gokigen.memoma.listitem;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SymbolListArrayAdapter extends ArrayAdapter<SymbolListArrayItem>
{
    private final LayoutInflater inflater;
    private final int textViewResourceId;
    private final List<SymbolListArrayItem> listItems;
    
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
    public @NonNull View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        View view;
        if(convertView != null)
        {
            view = convertView;
        }
        else
        {
            view = inflater.inflate(textViewResourceId, null);
        }

        SymbolListArrayItem item = listItems.get(position);
        
        ImageView imageView = view.findViewWithTag("icon");
        imageView.setImageResource(item.getIconResource());

        int subIcon = item.getSubIconResource();
        //if (subIcon != 0)
        {
            ImageView subImage = view.findViewWithTag("subIcon");
            subImage.setImageResource(subIcon);
        }            

        TextView titleView = view.findViewWithTag("title");
        titleView.setTextColor(Color.LTGRAY);
        titleView.setText(item.getTextResource1st());

        TextView detailView = view.findViewWithTag("detail");
        detailView.setTextColor(Color.LTGRAY);
        detailView.setText(item.getTextResource2nd());

        /*
        TextView optionView = view.findViewWithTag("option");
        optionView.setText(item.getTextResource3rd());
        */
        return (view);
    }
}
