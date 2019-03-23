package tme.pos.BusinessLayer;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;

import tme.pos.CustomViewCtr.MyCategoryItemView;
import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;
import tme.pos.MainUIActivity;
import tme.pos.POS_Application;
import tme.pos.R;

/**
 * Created by vanlanchoy on 10/14/2014.
 */
public class MyMenu {
    Hashtable<Long,CategoryObject> Categories;
    private Hashtable<Long,ArrayList<ItemObject>>AllItemsTable;
    private Hashtable<Long,ArrayList<ModifierObject>>AllModifiersTable;
    ArrayList<ItemObject>Items;
    ArrayList<ModifierObject>Modifiers;
    Context context;
    AppSettings myAppSettings;
//    String FILE_EXPORT_LOCATION="/sdcard/TMePOS/";
    public MyMenu(Context context)
    {
        this.context = context;
        Categories = new Hashtable<Long, CategoryObject>();
        this.myAppSettings = common.myAppSettings;

        LoadCategories();

    }

    public int SaveItemPicPath(long itemId,String strPath)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String[] columns=null;
        String[] values=null;

        columns = new String[]{Schema.DataTable_Item.PICTURE_PATH_COLUMN};
        values = new String[]{strPath};
        String strWhereClause = Schema.DataTable_Item.ID_COLUMN + " =?";
        String[] args = new String[]{itemId+""};
        return helper.Update(Schema.DataTable_Item.TABLE_NAME,columns,values,strWhereClause,args);
    }
    public ItemObject SearchItemByBarcode(String strBarcode)
    {
        ItemObject io = null;
        String[] args = new String[]{strBarcode,"1"};
        String strWhereClause = Schema.DataTable_Item.BARCODE_COLUMN+"=? and "+Schema.DataTable_Item.ACTIVE_FLAG_COLUMN
                +"=?";

        DatabaseHelper helper = new DatabaseHelper(context);
        //column names
        String[] columns={
                Schema.DataTable_Item.ID_COLUMN,
                Schema.DataTable_Item.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Item.ITEM_NAME_COLUMN,
                Schema.DataTable_Item.ITEM_PRICE_COLUMN,
                Schema.DataTable_Item.PARENT_ID_COLUMN,
                Schema.DataTable_Item.PICTURE_PATH_COLUMN,
                Schema.DataTable_Item.BARCODE_COLUMN,
                Schema.DataTable_Item.DO_NOT_TRACK_COLUMN,
                Schema.DataTable_Item.INACTIVE_DATE_COLUMN,
                Schema.VERSION_COLUMN

        };

        String strSortOrder = Schema.DataTable_Modifier.ITEM_NAME_COLUMN+" asc";

        Cursor c = helper.query(Schema.DataTable_Item.TABLE_NAME, columns, strWhereClause, args, strSortOrder);
        if(c==null)return io;


        c.moveToFirst();

        int columnNameIndex=c.getColumnIndex(Schema.DataTable_Item.ITEM_NAME_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_Item.ID_COLUMN);
        int columnParentIdIndex=c.getColumnIndex(Schema.DataTable_Item.PARENT_ID_COLUMN);
        int columnPriceIndex=c.getColumnIndex(Schema.DataTable_Item.ITEM_PRICE_COLUMN);
        int columnVersionIndex =c.getColumnIndex(Schema.VERSION_COLUMN);
        int columnPictureIndex =c.getColumnIndex(Schema.DataTable_Item.PICTURE_PATH_COLUMN);
        int columnBarcodeIndex =c.getColumnIndex(Schema.DataTable_Item.BARCODE_COLUMN);
        int columnDoNotTrackIndex =c.getColumnIndex(Schema.DataTable_Item.DO_NOT_TRACK_COLUMN);


        while(!c.isAfterLast())
        {
            io = new ItemObject(c.getLong(columnIdIndex)
            ,c.getString(columnNameIndex)
            ,c.getLong(columnParentIdIndex)
            ,c.getString(columnPriceIndex)
            ,c.getString(columnPictureIndex)
            ,c.getInt(columnDoNotTrackIndex) == 1 ? true : false
            ,c.getLong(columnBarcodeIndex)
                    ,c.getInt(columnVersionIndex));


            c.moveToNext();
        }
        c.close();
        helper.close();
        return io;
    }
    public ArrayList<ModifierObject> GetModifiers(Enum.ModifierType ModifierTypeToLoad,long ParentId)
    {
        String strWhereClause="";
        String[] args=null;
        if(ModifierTypeToLoad== Enum.ModifierType.global) {
            strWhereClause = Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN + "=? and (" + Schema.DataTable_Modifier.PARENT_ID_COLUMN + "=?)";
            args = new String[]{"1", "-1"};//-1 is global
        }
        else if(ModifierTypeToLoad==Enum.ModifierType.individual)
        {
            strWhereClause = Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN + "=? and (" + Schema.DataTable_Modifier.PARENT_ID_COLUMN + "=?)";
            args = new String[]{"1", ParentId+""};
        }
        else
        {
            strWhereClause = Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN+"=? and ("+Schema.DataTable_Modifier.PARENT_ID_COLUMN+"=? or "+
                    Schema.DataTable_Modifier.PARENT_ID_COLUMN+"=?)";

            args=new String[]{"1",ParentId+"","-1"};//-1 is global
        }
        LoadModifiers(strWhereClause, args);

        return Modifiers;
    }
    public void ReloadModifierTable()
    {
        AllModifiersTable = new Hashtable<Long, ArrayList<ModifierObject>>();
        LoadAllModifiersIntoTable(AllModifiersTable);
    }
    public ModifierObject GetModifier(long id,int version)
    {
        if(AllModifiersTable==null)
        {
            AllModifiersTable = new Hashtable<Long, ArrayList<ModifierObject>>();
            LoadAllModifiersIntoTable(AllModifiersTable);
        }
        if(AllModifiersTable.containsKey(id))
        {
            ArrayList<ModifierObject> mos = AllModifiersTable.get(id);
            for(ModifierObject mo:mos)
            {
                if(mo.GetCurrentVersionNumber()==version)return mo;
            }

        }

        ModifierObject mo = GetModifierObjectFromLogTable(id,version);
        if(mo!=null)
        {
            AllModifiersTable.get(id).add(mo);
        }

        return mo;
    }

    public ModifierObject GetModifier(long lnId)
    {
        if(AllModifiersTable==null)
        {
            AllModifiersTable = new Hashtable<Long, ArrayList<ModifierObject>>();
            LoadAllModifiersIntoTable(AllModifiersTable);
        }
        if(AllModifiersTable.containsKey(lnId))
        {
            ModifierObject moLatest=null;
            ArrayList<ModifierObject> mos = AllModifiersTable.get(lnId);
            for(ModifierObject mo:mos)
            {
                if(moLatest==null)
                {
                    moLatest = mo;
                }
                else
                {
                    if(moLatest.GetCurrentVersionNumber()<mo.GetCurrentVersionNumber())
                    {
                        moLatest = mo;
                    }
                }
            }
            return moLatest;
        }
        return null;
    }
    public ItemObject GetItem(long id,int version)
    {
        if(AllItemsTable==null)
        {
            AllItemsTable = new Hashtable<Long, ArrayList<ItemObject>>();
            LoadAllItemsIntoTable(AllItemsTable);
        }
        //get from current table
        if(AllItemsTable.containsKey(id))
        {
            ArrayList<ItemObject>ios =AllItemsTable.get(id);
            for(ItemObject io:ios)
            {
                if(io.GetCurrentVersionNumber()==version)return io;
            }

        }

        //query log table
        ItemObject io = GetItemObjectFromLogTable(id,version);
        if(io!=null)
        {
            AllItemsTable.get(id).add(io);
        }

        return io;


    }
    public ItemObject GetLatestItem(long lnId)
    {
        if(AllItemsTable==null)
        {
            AllItemsTable = new Hashtable<Long, ArrayList<ItemObject>>();
            LoadAllItemsIntoTable(AllItemsTable);
        }

        if(AllItemsTable.containsKey(lnId))
        {
            ArrayList<ItemObject>ios = AllItemsTable.get(lnId);
            ItemObject ioLatest=null;
            for(ItemObject io:ios)
            {
                if(ioLatest==null) {
                    ioLatest = io;
                }
                else
                {
                    if(ioLatest.GetCurrentVersionNumber()<io.GetCurrentVersionNumber())
                    {
                        ioLatest = io;
                    }
                }

            }
            return ioLatest;

        }
        return null;
    }
    private ModifierObject GetModifierObjectFromLogTable(long id,int version)
    {
        ModifierObject mo=null;
        DatabaseHelper helper = new DatabaseHelper(context);
        String strQuery = "select * from "+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+
                " where "+Schema.VERSION_COLUMN+"="+version+
                " and "+Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN+"="+id;
        Cursor cursor=helper.rawQuery(strQuery);

        int intPrice = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN);
        int intName = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN);


        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {



            mo = new ModifierObject(id
                    ,cursor.getString(intName)
                    ,-1
                    ,cursor.getString(intPrice)
                    ,0
                    ,0
                    ,version);



            cursor.moveToNext();
        }

        return mo;
    }
    private ItemObject GetItemObjectFromLogTable(long id,int version)
    {
        ItemObject io=null;
        DatabaseHelper helper = new DatabaseHelper(context);
        String strQuery = "select * from "+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+
                " where "+Schema.VERSION_COLUMN+"="+version+
                " and "+Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN+"="+id;
        Cursor cursor=helper.rawQuery(strQuery);

        int intPrice = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN);
        int intName = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN);


        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {



            io = new ItemObject(id
                    ,cursor.getString(intName)
                    ,-1
                    ,cursor.getString(intPrice)
                    ,"",false,0,version);



            cursor.moveToNext();
        }

        return io;
    }
    private void LoadAllModifiersIntoTable(Hashtable<Long, ArrayList<ModifierObject>> table)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String strQuery = "select * from "+Schema.DataTable_Modifier.TABLE_NAME;
        Cursor cursor = helper.rawQuery(strQuery);
        int intId = cursor.getColumnIndexOrThrow(Schema.DataTable_Modifier.ID_COLUMN);
        int intName = cursor.getColumnIndexOrThrow(Schema.DataTable_Modifier.ITEM_NAME_COLUMN);
        int intParentId = cursor.getColumnIndexOrThrow(Schema.DataTable_Modifier.PARENT_ID_COLUMN);
        int intPrice = cursor.getColumnIndexOrThrow(Schema.DataTable_Modifier.ITEM_PRICE_COLUMN);
        int intGroup = cursor.getColumnIndexOrThrow(Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN);
        int intActive = cursor.getColumnIndexOrThrow(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN);
        int intVersion =cursor.getColumnIndex(Schema.VERSION_COLUMN);
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            long id = cursor.getLong(intId);
            if(!table.containsKey(id))
            {
                table.put(id,new ArrayList<ModifierObject>());
            }
            table.get(id).add(new ModifierObject(cursor.getLong(intId),
                            cursor.getString(intName),
                            cursor.getLong(intParentId),
                            cursor.getString(intPrice),
                            cursor.getInt(intGroup),
                            cursor.getInt(intActive)
                    ,cursor.getInt(intVersion))
            );
            cursor.moveToNext();
        }
        cursor.close();
        helper.close();
    }
    private void LoadAllItemsIntoTable(Hashtable<Long, ArrayList<ItemObject>> table)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String strQuery = "select * from "+Schema.DataTable_Item.TABLE_NAME+" where "+Schema.DataTable_Item.ACTIVE_FLAG_COLUMN+"=1";
        Cursor cursor = helper.rawQuery(strQuery);
        int intId = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.ID_COLUMN);
        int intName = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.ITEM_NAME_COLUMN);
        int intParentId = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.PARENT_ID_COLUMN);
        int intPrice = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.ITEM_PRICE_COLUMN);
        int intPicture = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.PICTURE_PATH_COLUMN);
        int intDoNotTrack = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.DO_NOT_TRACK_COLUMN);
        int intBarcode = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.BARCODE_COLUMN);
        int intVersion =cursor.getColumnIndex(Schema.VERSION_COLUMN);
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            long id = cursor.getLong(intId);
            if(!table.containsKey(id))
            {
                table.put(id,new ArrayList<ItemObject>());
            }

            table.get(id).add(new ItemObject(cursor.getLong(intId),
                            cursor.getString(intName),
                            cursor.getLong(intParentId),
                            cursor.getString(intPrice),
                            cursor.getString(intPicture),
                    cursor.getInt(intDoNotTrack)==1?true:false,
                            cursor.getLong(intBarcode),
                    cursor.getInt(intVersion))
            );
            cursor.moveToNext();
        }
        cursor.close();
        helper.close();
    }
    protected void LoadModifiers(String strWhereClause , String[] args)
    {
        Modifiers = new ArrayList<ModifierObject>();

        DatabaseHelper helper = new DatabaseHelper(context);
        //column names
        String[] columns={
                Schema.DataTable_Modifier.ID_COLUMN,
                Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Modifier.ITEM_NAME_COLUMN,
                Schema.DataTable_Modifier.ITEM_PRICE_COLUMN,
                Schema.DataTable_Modifier.PARENT_ID_COLUMN,
                Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN,
                Schema.VERSION_COLUMN
        };

        String strSortOrder = Schema.DataTable_Modifier.ITEM_NAME_COLUMN+" asc";

        Cursor c = helper.query(Schema.DataTable_Modifier.TABLE_NAME, columns, strWhereClause, args, strSortOrder);
        if(c==null)return;//error occurred
        c.moveToFirst();

        int columnNameIndex=c.getColumnIndex(Schema.DataTable_Modifier.ITEM_NAME_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_Modifier.ID_COLUMN);
        int columnParentIdIndex=c.getColumnIndex(Schema.DataTable_Modifier.PARENT_ID_COLUMN);
        int columnPriceIndex=c.getColumnIndex(Schema.DataTable_Modifier.ITEM_PRICE_COLUMN);
        int columnGroupIndex =c.getColumnIndex(Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN);
        int columnVersionIndex =c.getColumnIndex(Schema.VERSION_COLUMN);
        while(!c.isAfterLast())
        {
            Modifiers.add(new ModifierObject(
                    c.getLong(columnIdIndex),
                    c.getString(columnNameIndex),
                    c.getLong(columnParentIdIndex),
                    c.getString(columnPriceIndex),
                    c.getInt(columnGroupIndex),
                    1,
                    c.getInt(columnVersionIndex)
            ));
            c.moveToNext();
        }
        c.close();
        helper.close();
    }

    public ArrayList<ItemObject> GetCategoryItems()
    {
        if(Items==null)Items = new ArrayList<ItemObject>();
        return Items;
    }
    public ArrayList<ItemObject> GetCategoryItems(long ParentID,boolean blnReload)
    {
        if(blnReload)
        {
            LoadItems(ParentID);
        }
        else {
            if (Items == null) {
                LoadItems(ParentID);
            } else {
                if (Items.size() == 0) {
                    LoadItems(ParentID);
                } else if (((ItemObject) Items.get(0)).ParentID != ParentID) {
                    LoadItems(ParentID);
                }
            }
        }
        return Items;
    }
    private ArrayList<ItemObject> LoadItemsIntoArrayList(long ParentId)
    {
        ArrayList<ItemObject> tempItems = new ArrayList<ItemObject>();

        DatabaseHelper helper = new DatabaseHelper(context);
        //column names
        String[] columns={
                Schema.DataTable_Item.ID_COLUMN,
                Schema.DataTable_Item.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Item.ITEM_NAME_COLUMN,
                Schema.DataTable_Item.ITEM_PRICE_COLUMN,
                Schema.DataTable_Item.PARENT_ID_COLUMN,
                Schema.DataTable_Item.PICTURE_PATH_COLUMN,
                Schema.DataTable_Item.DO_NOT_TRACK_COLUMN,
                Schema.DataTable_Item.BARCODE_COLUMN,
                Schema.VERSION_COLUMN
        };

        String strSortOrder = Schema.DataTable_Item.ITEM_NAME_COLUMN+" asc";

        String strWhereClause = Schema.DataTable_Item.ACTIVE_FLAG_COLUMN+"=? and "+Schema.DataTable_Item.PARENT_ID_COLUMN+"=?";
        String[] args={"1",ParentId+""};

        Cursor c = helper.query(Schema.DataTable_Item.TABLE_NAME, columns, strWhereClause, args, strSortOrder);
        if(c==null)return tempItems;//error occurred
        c.moveToFirst();

        int columnNameIndex=c.getColumnIndex(Schema.DataTable_Item.ITEM_NAME_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_Item.ID_COLUMN);
        int columnParentIdIndex=c.getColumnIndex(Schema.DataTable_Item.PARENT_ID_COLUMN);
        int columnPriceIndex=c.getColumnIndex(Schema.DataTable_Item.ITEM_PRICE_COLUMN);
        int columnPictureIndex=c.getColumnIndex(Schema.DataTable_Item.PICTURE_PATH_COLUMN);
        int columnDoNotTrackIndex=c.getColumnIndex(Schema.DataTable_Item.DO_NOT_TRACK_COLUMN);
        int columnBarcodeIndex = c.getColumnIndexOrThrow(Schema.DataTable_Item.BARCODE_COLUMN);
        int columnVersionIndex =c.getColumnIndex(Schema.VERSION_COLUMN);

        while(!c.isAfterLast())
        {
            tempItems.add(new ItemObject(
                    c.getLong(columnIdIndex),
                    c.getString(columnNameIndex),
                    c.getLong(columnParentIdIndex),
                    c.getString(columnPriceIndex),
                    c.getString(columnPictureIndex),
                    c.getInt(columnDoNotTrackIndex)==1?true:false,
                    c.getLong(columnBarcodeIndex),
                    c.getInt(columnVersionIndex)
            ));
            c.moveToNext();
        }
        c.close();
        helper.close();
        return tempItems;
    }
    protected void LoadItems(long ParentID)
    {
        Items = LoadItemsIntoArrayList(ParentID);

    }
    protected void LoadCategories()
    {
        common.Utility.LogActivity("load category list");
        Categories.clear();
        DatabaseHelper helper = new DatabaseHelper(context);

        //column names
        String[] columns = {
                Schema.DataTable_Category.ID_COLUMN,
                Schema.DataTable_Category.CATEGORY_NAME_COLUMN,
                Schema.DataTable_Category.ACTIVE_FLAG_COLUMN
        };
        String strSortOrder=Schema.DataTable_Category.ID_COLUMN+ " asc";

        String strWhereClause=Schema.DataTable_Category.ACTIVE_FLAG_COLUMN+"=?";
        String[] args = {"1"};

        Cursor c = helper.query(Schema.DataTable_Category.TABLE_NAME, columns, strWhereClause, args, strSortOrder);
        int columnNameIndex=c.getColumnIndex(Schema.DataTable_Category.CATEGORY_NAME_COLUMN);
        int columnIdIndex=c.getColumnIndex(Schema.DataTable_Category.ID_COLUMN);
        //int columnFlagIndex = c.getColumnIndex(Schema.DataTable_Category.ACTIVE_FLAG_COLUMN);
        while(!c.isAfterLast())
        {

            Categories.put(c.getLong(columnIdIndex), new CategoryObject(c.getLong(columnIdIndex), c.getString(columnNameIndex)));
            c.moveToNext();
        }
        c.close();
        helper.close();
    }

    public ArrayList<CategoryObject> GetCategoryList()
    {
        common.Utility.LogActivity("get category order list");

        //construct a new array list to return
        ArrayList<CategoryObject>al = new ArrayList<CategoryObject>();
        ArrayList<CategoryObject> al_clone = new ArrayList<CategoryObject>(Categories.values());
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String strOrder =myAppSettings.GetCategoryOrder();// sp.getString(MainUIActivity.PREFERRENCE_CATEGORY_ORDER_KEY, "");


        //skip if is empty string
        if(strOrder.length()>0)
        {
            String[] strKeyOrders = strOrder.split(",");
            for (int i = 0; i < strKeyOrders.length; i++) {
                for (int j = al_clone.size() - 1; j >= 0; j--) {
                    if (strKeyOrders[i].compareTo(al_clone.get(j).getID() + "") == 0) {
                        al.add(al_clone.get(j));
                        al_clone.remove(j);
                    }
                }
            }
        }
        else
        {
            //create a new order list and save it
            for(int i =0;i<al_clone.size();i++)
            {
                strOrder +=al_clone.get(i).getID()+",";
            }
            if(strOrder.length()>0)
            {
                //removing the last comma
                strOrder=strOrder.substring(0,strOrder.length()-1);
                myAppSettings.SaveCategoryOrder(strOrder);
                //SharedPreferences.Editor editor =  sp.edit();
                //editor.putString(MainUIActivity.PREFERRENCE_CATEGORY_ORDER_KEY,strOrder);
                //editor.commit();
            }
        }
        //insert remaining into the list
        while(al_clone.size()>0)
        {
            al.add(al_clone.get(0));
            al_clone.remove(0);
        }
        return al;
        //return new ArrayList<CategoryObject>(Categories.values());
    }

    public long AddItem(String strItemName,BigDecimal bdPrice,long lgParentId,String strPicturePath
            ,boolean blnDoNotTrack,long lngBarcode)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String[] columns=null;
        String[] values=null;
        long lnNewInsertedId = helper.GenerateNextItemRecordId();

            columns = new String[]{Schema.DataTable_Item.ITEM_NAME_COLUMN,
                    Schema.DataTable_Item.ACTIVE_FLAG_COLUMN,
                    Schema.DataTable_Item.ITEM_PRICE_COLUMN,
                    Schema.DataTable_Item.PARENT_ID_COLUMN,
                    Schema.DataTable_Item.ID_COLUMN,
                    Schema.DataTable_Item.PICTURE_PATH_COLUMN,
                    Schema.DataTable_Item.DO_NOT_TRACK_COLUMN,
                    Schema.DataTable_Item.BARCODE_COLUMN,
            Schema.VERSION_COLUMN};
            values = new String[]{strItemName, "1", bdPrice.toPlainString(), lgParentId + ""
                    , lnNewInsertedId + "", strPicturePath,(blnDoNotTrack)?"1":"0"
                    ,lngBarcode+"","1"};

        long id =helper.Insert(Schema.DataTable_Item.TABLE_NAME, columns, values);

        if(id>0) {
            LoadItems(lgParentId);
            if(AllItemsTable==null){
                AllItemsTable = new Hashtable<Long, ArrayList<ItemObject>>();
                LoadAllItemsIntoTable(AllItemsTable);
            }
            if(!AllItemsTable.containsKey(lnNewInsertedId))
            {
                AllItemsTable.put(lnNewInsertedId,new ArrayList<ItemObject>());
            }


            AllItemsTable.get(lnNewInsertedId).add(new ItemObject(lnNewInsertedId,strItemName,lgParentId,bdPrice.toPlainString(),strPicturePath,blnDoNotTrack,lngBarcode,1
            ));


        }
        return lnNewInsertedId;
    }
    public long AddCategory(String  strNewCategory)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        long lngId=helper.GenerateNextCategoryRecordId();
        String[] columns= new String[]{Schema.DataTable_Category.CATEGORY_NAME_COLUMN,
                Schema.DataTable_Category.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Category.ID_COLUMN};
        String[] values = new String[]{strNewCategory,"1",lngId+""};

        long id =helper.Insert(Schema.DataTable_Category.TABLE_NAME, columns, values);
        if(id>-1)
        {
            CategoryObject co = new CategoryObject(lngId,strNewCategory);//id,strNewCategory);
            Categories.put(lngId,co);//id,co);
            myAppSettings.AppendCategoryOrder(lngId + "");//id+"");
            id = lngId;
        }
        return id;

    }

    public int DeleteItem(long ItemId)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        int result = helper.DeleteItem(ItemId);
        if(result>0)
        {
            //remove it from table
            AllItemsTable.remove(ItemId);

            //remove item from invetory list
            if(common.inventoryList.inventories.containsKey(ItemId))
            {
                common.inventoryList.inventories.remove(ItemId);
            }
        }
        return result;
    }
    public int DeleteCategory(MyCategoryItemView item,ArrayList<Long>AffectedItemIds)
    {

       /* String[] columns = new String[]{Schema.DataTable_Category.ACTIVE_FLAG_COLUMN};
        String[] values = new String[]{"0"};
        String strWhereClause =Schema.DataTable_Category.ACTIVE_FLAG_COLUMN+"=? and "+Schema.DataTable_Category.ID_COLUMN+"=?";
        String[] args = new String[]{"1",item.getTag().toString()};*/
        long CategoryId = Long.parseLong(item.getTag().toString());
        ArrayList<ItemObject>tempList = LoadItemsIntoArrayList(CategoryId);
        ArrayList<Long> Ids = new ArrayList<Long>();
        for(ItemObject io:tempList)
        {
            Ids.add(io.getID());
            AffectedItemIds.add(io.getID());
        }
        DatabaseHelper helper = new DatabaseHelper(context);
        int result =helper.DeleteCategory(CategoryId, Ids);//helper.Update(Schema.DataTable_Category.TABLE_NAME,columns,values,strWhereClause,args);
        if(result>0){

            for(Long id:Ids)
            {AllItemsTable.remove(id);}

            LoadCategories();

        }
        return result;

    }
    public boolean UpdateItemAndModifiers(ItemObject item,long lngItemId,ArrayList<ModifierObject>InactiveModifiers,ArrayList<ModifierObject>NewModifiers)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        boolean blnSuccess = helper.BulkUpdateItemAndModifiers(item, lngItemId, InactiveModifiers, NewModifiers);
        if(blnSuccess)
        {
            //update inventory record
            if(common.inventoryList.inventories.containsKey(lngItemId))
            {
                common.inventoryList.inventories.remove(lngItemId);
            }

            //reload item if it has been updated, if only modifier need not to re-query
            if(item!=null)
                LoadItems(item.getParentID());

            AllItemsTable = new Hashtable<Long, ArrayList<ItemObject>>();
            LoadAllItemsIntoTable(AllItemsTable);
        }



        return blnSuccess;
    }
    /*public void UpdateItemAndModifiers(String strNewItemName,BigDecimal bgPrice,long lngParentId,long lngItemId,ArrayList<ModifierObject>InactiveModifiers,ArrayList<ModifierObject>NewModifiers)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        helper.BulkUpdateItemAndModifiers(strNewItemName, bgPrice,lngParentId,lngItemId,InactiveModifiers,NewModifiers);
        LoadItems(lngParentId);//here

    }*/
    public boolean UpdateModifiers(ArrayList<ModifierObject>InactiveList,ArrayList<ModifierObject>NewList)
    {
        DatabaseHelper helper = new DatabaseHelper(context);

        return helper.BulkUpdateModifiers(InactiveList, NewList);


    }
    public int UpdateCategoryName(String strNewName,String strId)
    {
        String[] columns = new String[]{Schema.DataTable_Category.CATEGORY_NAME_COLUMN};
        String[] values = new String[]{strNewName};
        String strWhereClause =Schema.DataTable_Category.ACTIVE_FLAG_COLUMN+"=? and "+Schema.DataTable_Category.ID_COLUMN+"=?";
        String[] args = new String[]{"1",strId};
        DatabaseHelper helper = new DatabaseHelper(context);
        int rowAffectedCount= helper.Update(Schema.DataTable_Category.TABLE_NAME, columns, values, strWhereClause, args);
        if(rowAffectedCount!=-1){LoadCategories();}//reload categories list
        return rowAffectedCount;
    }
    public CategoryObject GetCategory(long key)
    {
        return Categories.get(key);
    }
    public void MoveCategory(String key1,String key2)
    {

            int FromIndex = -1;
            int ToIndex = -1;
            int count=0;
            //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String strOrder =myAppSettings.GetCategoryOrder();// sp.getString(MainUIActivity.PREFERRENCE_CATEGORY_ORDER_KEY, "");
            //Log.d("current category order",strOrder);
            String[] strKeyOrders = strOrder.split(",");

            for (int i = 0; i < strKeyOrders.length; i++) {
                if (strKeyOrders[i].compareTo(key1 ) == 0) {
                    FromIndex=i;
                    count++;
                } else if (strKeyOrders[i].compareTo(key2) == 0) {
                    ToIndex = i;
                    count++;
                }
                if(count>1)break;
            }

            strKeyOrders[ToIndex] = key1;
            strKeyOrders[FromIndex] = key2;
            String strNewOrder ="";
            for(int i=0;i<strKeyOrders.length;i++)
            {

                    strNewOrder +=strKeyOrders[i]+",";

            }
            if(strNewOrder.length()>0)
            {
                strNewOrder = strNewOrder.substring(0,strNewOrder.length()-1);
            }
            //Log.d("new category order",strNewOrder);
            myAppSettings.SaveCategoryOrder(strNewOrder);
            //SharedPreferences.Editor editor = sp.edit();
            //editor.putString(MainUIActivity.PREFERRENCE_CATEGORY_ORDER_KEY, strNewOrder);
            //editor.commit();

    }
    public void SwapCategory(String key1,String key2) {

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String strOrder =myAppSettings.GetCategoryOrder();// sp.getString(MainUIActivity.PREFERRENCE_CATEGORY_ORDER_KEY, "");
            //Log.d("current category order",strOrder);
            String[] strKeyOrders = strOrder.split(",");
            int count = 0;
            for (int i = 0; i < strKeyOrders.length; i++) {
                if (strKeyOrders[i].compareTo(key1 ) == 0) {
                    strKeyOrders[i] = key2;
                    count++;
                } else if (strKeyOrders[i].compareTo(key2) == 0) {
                    strKeyOrders[i] = key1;
                    count++;
                }
                if (count == 2) break;
            }

            SharedPreferences.Editor editor = sp.edit();
            strOrder = "";
            for (int i = 0; i < strKeyOrders.length; i++) {
                strOrder += strKeyOrders[i] + ",";
            }

            //remove the last pile
            //Log.d("new category order",strOrder);
            strOrder = strOrder.substring(0, strOrder.length() - 1);
            myAppSettings.SaveCategoryOrder(strOrder);
            //editor.putString(MainUIActivity.PREFERRENCE_CATEGORY_ORDER_KEY, strOrder);
            //editor.commit();

    }
    public HashMap<Long,String> GetItemName(Long[] IDs)
    {
        HashMap<Long,String>data = new HashMap<Long, String>();
        String strArgs ="";
        for(int i=0;i<IDs.length;i++)
        {
            strArgs+=""+IDs[i];
            if(i!=IDs.length-1)
            {
                strArgs+=",";
            }

        }
        String strSql ="select * from "+Schema.DataTable_Item.TABLE_NAME+
                       " where "+Schema.DataTable_Item.ID_COLUMN+ " in ("+strArgs+")";
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor cursor=helper.rawQuery(strSql);
        cursor.moveToFirst();
        int itemIdIndex = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.ID_COLUMN);
        int itemNameIndex = cursor.getColumnIndexOrThrow(Schema.DataTable_Item.ITEM_NAME_COLUMN);
        while(!cursor.isAfterLast())
        {
            data.put(cursor.getLong(itemIdIndex),cursor.getString(itemNameIndex));
            cursor.moveToNext();
        }
        return data;
    }
    protected void ShowErrorMessageBox(String strMethodName,Exception ex)
    {
        //Log.d("EXCEPTION: " + strMethodName, "" + ex.getMessage());

        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(strMethodName);
        messageBox.setMessage(ex.getMessage());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }


}
