package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tme.pos.BusinessLayer.CategoryObject;
import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.AddNewCategoryItemView;
import tme.pos.CustomViewCtr.MyCategoryItemView;
import tme.pos.CustomViewCtr.MyGridViewCategoryItemView;

/**
 * Created by vanlanchoy on 11/2/2014.
 */
public class MyCategoryItemViewBaseAdapter extends BaseAdapter {
    Context context;
    String strSelectedTag;
    ArrayList<CategoryObject> categories;
    public MyCategoryItemViewBaseAdapter(Context context,ArrayList<CategoryObject> categories,String strSelectedTag ) {
        super();
        this.context = context;
        this.categories = categories;
        this.strSelectedTag = strSelectedTag;
    }
    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View currentView, ViewGroup parent) {

        //create a new view each time, because passing the existing view might create duplicated view
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        MyGridViewCategoryItemView catItem=null;
        CategoryObject co  = categories.get(position);


            if(co.getID()==common.text_and_length_settings.PROMOTION_CATEGORY_ID)
            {
                catItem = (MyGridViewCategoryItemView) inflater.inflate(R.layout.layout_gridview_item_category_ui, parent, false);
                catItem.setText(co.getName());
                catItem.setTag(co.getID());
                catItem.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)));
                catItem.setTextColor(context.getResources().getColor(R.color.green));
                if ((co.getID() + "").compareTo(strSelectedTag) == 0) {

                    catItem.setBackground(context.getResources().getDrawable(R.drawable.draw_two_round_rect));

                    catItem.setTextColor(Color.BLACK);

                }
            }
            else if(co.getID()==common.text_and_length_settings.TAP_TO_ADD_CATEGORY_ID)
            {
                //add 'TAP TO ADD' category item view
                AddNewCategoryItemView catItem2 = (AddNewCategoryItemView) inflater.inflate(R.layout.layout_add_item_category_ui,
                        parent ,false);
                catItem2.setText(co.getName());
                catItem2.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)),Typeface.BOLD);
                return catItem2;
            }
            else
            {

                    catItem = (MyGridViewCategoryItemView) inflater.inflate(R.layout.layout_gridview_item_category_ui, parent, false);
                    catItem.setText(co.getName());
                    catItem.setTag(co.getID());
                    catItem.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)));
                    catItem.setTextColor(context.getResources().getColor(R.color.green));
                    if ((co.getID() + "").compareTo(strSelectedTag) == 0) {

                        catItem.setBackground(context.getResources().getDrawable(R.drawable.draw_two_round_rect));

                        catItem.setTextColor(Color.BLACK);

                    }

            }

        return catItem;
    }
    public ArrayList<CategoryObject> GetCategories()
    {
        return categories;
    }
    public String GetSelectedCategoryTag(){return strSelectedTag;}
    /*private  void ShowMessageBox(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(strTitle);
        messageBox.setMessage(strMsg);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(context.getResources().getDrawable(iconId),context.getResources(),36,36));
        }
        messageBox.show();
    }*/
}
