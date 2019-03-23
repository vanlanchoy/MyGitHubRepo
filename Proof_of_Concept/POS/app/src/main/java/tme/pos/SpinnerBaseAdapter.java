package tme.pos;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;

/**
 * Created by kchoy on 3/23/2015.
 */
public class SpinnerBaseAdapter<T> extends BaseAdapter  {
    T[] strItems;
    float TEXT_SIZE=25;
    Typeface FONT_ABEL;
    Context context;
    boolean blnCustomTextSize=false;
    public SpinnerBaseAdapter(T[]myItems,Context c,int intTextSize)
    {
        Instantiate(myItems,c);
        blnCustomTextSize = true;
        TEXT_SIZE = intTextSize;
    }
    public SpinnerBaseAdapter(T[]myItems,Context c)
    {
       Instantiate(myItems,c);
    }
    void Instantiate(T[]myItems,Context c)
    {
        /**order does matter, always check for child type before parent, because instanceof will return true if parent type present**/
        //insert default item if is servant type
        if(myItems instanceof Server[])
        {
            Server s = new Server();
            if(myItems.length>0)
            {
                s.Name="Server Option...";
                s.EmployeeId=-1;
                s.gender = Enum.ServerGender.male;
            }
            else
            {

                s.Name="No Server";
                s.EmployeeId=-1;
                s.gender = Enum.ServerGender.male;
            }
            Server[] temp = new Server[myItems.length+2];
            temp[0]=s;
            for(int i=0;i<myItems.length;i++)temp[i+1] =(Server)myItems[i];

            //add create new entry option
            s =new Server();
            s.Name = "Create New...";
            s.EmployeeId=-2;
            s.gender = Enum.ServerGender.male;
            temp[myItems.length+1] = s;
            myItems = (T[])temp;
        }
        else if(myItems instanceof Supplier[])
        {
            Supplier s = new Supplier();
            if(myItems.length>0)
            {
                s.SupplierId=-1;
                s.Name = "Supplier Option...";

            }
            else
            {
                s.Name="No Supplier";
                s.SupplierId=-1;
            }
            Supplier[] temp = new Supplier[myItems.length+2];
            temp[0]=s;
            for(int i=0;i<myItems.length;i++)temp[i+1] =(Supplier)myItems[i];
            s = new Supplier();
            s.SupplierId=-2;
            s.Name="Create New...";
            temp[myItems.length+1] = s;
            myItems = (T[])temp;

        }
        else if(myItems instanceof ItemObject[])
        {
            if(myItems.length==0)
            {
                ItemObject itemObject = new ItemObject(-1,"No Item",-1,"0","",false,-1,1);
                ItemObject[] temp = new ItemObject[]{itemObject};
                myItems = (T[])temp;
            }
        }
        else if(myItems instanceof CategoryObject[])
        {
            if(myItems.length==0)
            {
                CategoryObject categoryObject = new CategoryObject(-1,"No Category");
                CategoryObject[] temp = new CategoryObject[]{categoryObject};
                myItems = (T[])temp;
            }
        }
        strItems = myItems;
        context = c;
        LoadApplication();
    }
    private void LoadApplication()
    {
        TEXT_SIZE = (blnCustomTextSize)?TEXT_SIZE:context.getResources().getDimension(R.dimen.dp_checkout_panel_fragment_text_size);
        FONT_ABEL = Typeface.createFromAsset(context.getResources().getAssets(), context.getResources().getString(R.string.app_font_family));
    }
    @Override
    public int getCount() {
        return strItems.length;
    }

    @Override
    public Object getItem(int i) {
        return strItems[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
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
        if(item instanceof Server)
        {
            Server s = (Server)item;
            tv.setText(s.Name);
            tv.setTag(s);
        }
        else if(item instanceof String) {
            tv.setText(item.toString());
            //tv.setText(strItems[position]);
        }
        else if(item instanceof Supplier){
            Supplier s = (Supplier)item;
            tv.setText(s.Name);
            tv.setTag(s);
        }
        else if(item instanceof CategoryObject)
        {
            CategoryObject c = (CategoryObject)item;
            tv.setText(c.getName());
            tv.setTag(c);
        }
        else if(item instanceof ItemObject)
        {
            ItemObject i = (ItemObject)item;
            tv.setText(i.getName());
            tv.setTag(i);
        }

        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
        return v;
    }
}
