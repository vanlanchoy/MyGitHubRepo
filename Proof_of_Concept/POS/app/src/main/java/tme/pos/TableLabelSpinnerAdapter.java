package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tme.pos.BusinessLayer.Duple;
import tme.pos.BusinessLayer.MyCart;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 5/2/2015.
 */
public class TableLabelSpinnerAdapter<T> extends SpinnerBaseAdapter<T> {
    public TableLabelSpinnerAdapter(T[]myItems,Context c)
    {
        super(myItems,c);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            v =inflater.inflate(R.layout.layout_spinner_string_item_ui, null);

        }
        TextView tv = (TextView) v.findViewById(R.id.textView1);
        tv.setTypeface(FONT_ABEL);
        T item = strItems[position];
       //if(item instanceof Duple)
        //{
            //Duple<String,String>d = (Duple<String,String>)item;

        Duple<String,Duple<String,Boolean>>d = (Duple<String,Duple<String,Boolean>>)item;
//ShowMessage("spinner get view","id "+d.GetFirst()+ " label "+d.GetSecond().GetFirst());
        tv.setText(d.GetSecond().GetFirst());
        v.setTag(d.GetFirst());

        v.setBackgroundColor(context.getResources().getColor(R.color.divider_grey));
            //change to black color if there is already existing item in cart
            if(d.GetFirst().length()==0)
            {
                //default
                tv.setTextColor(context.getResources().getColor(R.color.black));
            }
            else
            {
                MyCart cart = common.myCartManager.GetCart(d.GetFirst(),0);
                if(cart!=null && cart.GetItems().size()>0) {

                    tv.setTextColor(context.getResources().getColor(R.color.black));
                }
                else {
                        tv.setTextColor(context.getResources().getColor(R.color.very_light_grey));

                }
            }

        //}
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
        return v;
    }

    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(context.getResources().getDrawable(iconId),context.getResources(),36,36));
        }
        messageBox.show();
    }
}
