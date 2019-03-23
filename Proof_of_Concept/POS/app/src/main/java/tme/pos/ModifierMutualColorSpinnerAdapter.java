package tme.pos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;

/**
 * Created by kchoy on 1/13/2015.
 */
public class ModifierMutualColorSpinnerAdapter extends ArrayAdapter {
    Activity myActivity;
    ArrayList myList;
    LayoutInflater inflater;
    int myColorViewResourceId;
    public ModifierMutualColorSpinnerAdapter(Activity activity,int ColorLayoutResourceId,ArrayList aryList)
    {
        super(activity,ColorLayoutResourceId,aryList);
        myActivity = activity;
        myList = aryList;
        inflater = (LayoutInflater)myActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myColorViewResourceId = ColorLayoutResourceId;
    }
    public View getCustomView(int position,View convertView,ViewGroup parent)
    {
        View row = inflater.inflate(myColorViewResourceId,parent,false);//R.layout.layout_modifier_mutual_color_ui,parent,false);
        TextView tv = (TextView)row.findViewById(R.id.tv);
        if(Integer.parseInt(myList.get(position).toString())== Enum.MutualGroupColor.white.group)
        {
            //tv.setTextColor(Enum.MutualGroupColor.white.value);
            //tv.setBackgroundColor(Enum.MutualGroupColor.white.value);
            tv.setBackgroundColor(getContext().getResources().getColor(Enum.MutualGroupColor.white.value));

        }
        else if(Integer.parseInt(myList.get(position).toString())==Enum.MutualGroupColor.mutual_dark_brown.group)
        {

            tv.setBackgroundColor(getContext().getResources().getColor(Enum.MutualGroupColor.mutual_dark_brown.value));

        }
        else if(Integer.parseInt(myList.get(position).toString())==Enum.MutualGroupColor.mutual_dark_orange.group)
        {

            tv.setBackgroundColor(getContext().getResources().getColor(Enum.MutualGroupColor.mutual_dark_orange.value));

        }
        else if(Integer.parseInt(myList.get(position).toString())==Enum.MutualGroupColor.mutual_dark_indigo.group)
        {

            tv.setBackgroundColor(getContext().getResources().getColor(Enum.MutualGroupColor.mutual_dark_indigo.value));

        }
        else if(Integer.parseInt(myList.get(position).toString())==Enum.MutualGroupColor.mutual_dark_red.group)
        {

            tv.setBackgroundColor(getContext().getResources().getColor(Enum.MutualGroupColor.mutual_dark_red.value));

        }
        else if(Integer.parseInt(myList.get(position).toString())==Enum.MutualGroupColor.mutual_dark_navy.group)
        {

            tv.setBackgroundColor(getContext().getResources().getColor(Enum.MutualGroupColor.mutual_dark_navy.value));

        }


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
}
