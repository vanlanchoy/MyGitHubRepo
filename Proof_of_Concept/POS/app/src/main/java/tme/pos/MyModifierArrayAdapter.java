package tme.pos;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import tme.pos.BusinessLayer.Enum.MutualGroupColor;
import java.util.ArrayList;

import tme.pos.BusinessLayer.*;

/**
 * Created by kchoy on 3/10/2015.
 */
public class MyModifierArrayAdapter extends ArrayAdapter {
    Activity myActivity;
    ArrayList myList;
    LayoutInflater inflater;
    int mySpinnerXMLResourceId;
    float DP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE;
    public MyModifierArrayAdapter(Activity activity,int layoutId,ArrayList<ModifierObject> aryList)
    {
        super(activity,layoutId,aryList);
        myActivity = activity;
        myList = aryList;
        inflater = (LayoutInflater)myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mySpinnerXMLResourceId = layoutId;
        DP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE = activity.getResources().getDimension(R.dimen.dp_menu_item_popup_active_page_modifier_text_size);
    }
    public View getCustomView(int position,View convertView,ViewGroup parent)
    {
        ModifierObject mo =(ModifierObject) myList.get(position);
        View row = inflater.inflate(mySpinnerXMLResourceId,parent,false);//R.layout.layout_modifier_mutual_color_ui,parent,false);
        TextView tvName = (TextView)row.findViewById(R.id.tvName);
        TextView tvPrice = (TextView)row.findViewById(R.id.tvPrice);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE);
        tvName.setText(mo.getName());

        if(position!=0)
        tvPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(mo.getPrice()));

        int colorId = common.Utility.GetModifierGroupColor(mo.getMutualGroup());
        tvName.setTextColor(getContext().getResources().getColor(colorId));
        tvPrice.setTextColor(getContext().getResources().getColor(colorId));


        row.setTag(new ModifierObject(mo.getID(),mo.getName(),mo.getParentID(),mo.getPrice().toPlainString(),mo.getMutualGroup(),mo.getIsActive(),mo.GetCurrentVersionNumber()));
        return row;
    }
    @Override
    public View getDropDownView(int position, View convertView,ViewGroup parent)
    {
        return getCustomView(position,convertView,parent);
    }
    @Override
    public View getView(int position, View convertView,ViewGroup parent)
    {
        return getCustomView(position,convertView,parent);
    }
    @Override
    public int getCount()
    {
        return myList.size();
    }
}
