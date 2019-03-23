package tme.pos;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

import tme.pos.BusinessLayer.Duple;
import tme.pos.BusinessLayer.common;
import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by vanlanchoy on 6/18/2016.
 */
public class CustomListManager {
    HashMap<Integer,Duple<String,ArrayList<Long>>> customListPages;
    Context context;
    public CustomListManager(Context c){
        customListPages = new HashMap<Integer,Duple<String,ArrayList<Long>>>();
        context = c;
        LoadCustomLists();
    }
    public Duple<String,ArrayList<Long>>GetCustomList(int pageIndex)
    {
        if(!customListPages.containsKey(pageIndex))
        {
            customListPages.put(pageIndex,new Duple<String, ArrayList<Long>>("List "+pageIndex,new ArrayList<Long>()));
        }
        return customListPages.get(pageIndex);
    }
    public void Add(int pageIndex,String strListName,ArrayList<Long>list)
    {

        customListPages.put(pageIndex,new Duple<String, ArrayList<Long>>(strListName,list));
        InsertIntoDb(pageIndex,strListName,list);
    }
    public void Update(int pageIndex,String strListName,ArrayList<Long>list)
    {
        customListPages.put(pageIndex,new Duple<String, ArrayList<Long>>(strListName,list));
        DeleteFromDb(pageIndex);
        InsertIntoDb(pageIndex,strListName,list);
    }
    private void InsertIntoDb(int pageIndex,String strListName,ArrayList<Long>list)
    {
        String strIds = "";
        for(int i=0;i<list.size();i++)
        {
            strIds+=list.get(i)+",";
        }
        strIds=(strIds.length()>0)?strIds.substring(0,strIds.length()-1):strIds;

        DatabaseHelper helper = new DatabaseHelper(context);
        String[] columns= new String[]{Schema.DataTable_CustomList.ID_COLUMN
                ,Schema.DataTable_CustomList.PAGE_TITLE_COLUMN,
                Schema.DataTable_CustomList.PAGE_CONTENT_COLUMN};

        String[] values = new String[]{pageIndex+"",strListName,strIds};
        long id =helper.Insert(Schema.DataTable_CustomList.TABLE_NAME, columns, values);
    }
    public void Delete(int pageIndex)
    {
        customListPages.remove(pageIndex);
        DeleteFromDb(pageIndex);
    }
    private void DeleteFromDb(int index)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String strSql = "delete from "+Schema.DataTable_CustomList.TABLE_NAME+" where "
                +Schema.DataTable_CustomList.ID_COLUMN+"="+index;

        helper.getWritableDatabase().execSQL(strSql);
    }
    private void LoadCustomLists()
    {
        customListPages =new HashMap<Integer,Duple<String,ArrayList<Long>>>();

        DatabaseHelper helper = new DatabaseHelper(context);
        //column names
        String[] columns={
                Schema.DataTable_CustomList.ID_COLUMN,
                Schema.DataTable_CustomList.PAGE_TITLE_COLUMN,
                Schema.DataTable_CustomList.PAGE_CONTENT_COLUMN

        };

        String strSortOrder = Schema.DataTable_CustomList.ID_COLUMN+" asc";

        Cursor c = helper.query(Schema.DataTable_CustomList.TABLE_NAME, columns, "",new String[]{}, strSortOrder);
        if(c==null)return;//error occurred
        c.moveToFirst();

        int columnTitleIndex=c.getColumnIndex(Schema.DataTable_CustomList.PAGE_TITLE_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_CustomList.ID_COLUMN);
        int columnPageContentIndex=c.getColumnIndex(Schema.DataTable_CustomList.PAGE_CONTENT_COLUMN);


        while(!c.isAfterLast())
        {
            String strItems = c.getString(columnPageContentIndex);
            ArrayList<Long>lst = new ArrayList<Long>();



            if(strItems.length()>0) {
                String[] items = strItems.split(",");
                for (int i = 0; i < items.length; i++) {
                    lst.add(Long.parseLong(items[i] + ""));
                }
            }
            customListPages.put(c.getInt(columnIdIndex),
                    new Duple<String, ArrayList<Long>>(c.getString(columnTitleIndex)
            ,lst));

            c.moveToNext();
        }
        c.close();
        helper.close();
    }

}
