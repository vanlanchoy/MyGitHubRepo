package tme.pos.BusinessLayer;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Pair;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by kchoy on 6/18/2015.
 */
public class InventoryList {
    HashMap<Long,Pair<Integer,ArrayList<Inventory>>> inventories;
    Context context;

    public InventoryList(Context c)
    {
        inventories =  new HashMap<Long, Pair<Integer, ArrayList<Inventory>>>();
        context = c;
    }

    public void RemoveInventoryCount(long itemId)
    {
        if(inventories.containsKey(itemId))
        {
            inventories.remove(itemId);
            GetInventoryCount(itemId);
        }
    }
    public ArrayList<Inventory> GetRecords(long itemId)
    {
        if(!inventories.containsKey(itemId))
        {
            //load inventory list
            inventories.put(itemId,new Pair<Integer, ArrayList<Inventory>>(0,LoadInventoryList(itemId)));
            CalculateInventory(itemId);
        }

        return inventories.get(itemId).second;
    }
    public int GetInventoryCount(long itemId)
    {

        if(!inventories.containsKey(itemId))
        {
            //load inventory list
            inventories.put(itemId,new Pair<Integer, ArrayList<Inventory>>(0,LoadInventoryList(itemId)));
            CalculateInventory(itemId);
        }

        //common.Utility.ShowMessage("item Id "+itemId,"inventory count "+inventories.get(itemId).first,null);
        return inventories.get(itemId).first;
    }
    public void UpdateOrder(long itemId,int unitOrder)
    {
        //ignore if item id doesn't existed
        if(inventories.containsKey(itemId))
        {
           //if(inventories.get(itemId).first>0) {

               inventories.put(itemId,
                       new Pair<Integer, ArrayList<Inventory>>(inventories.get(itemId).first - unitOrder,
                               inventories.get(itemId).second));
           //}
        }
    }
    private void CalculateInventory(long itemId)
    {
        if(!inventories.containsKey(itemId))return;
        int total=0;
        Date startDate= new Date(AppSettings.INITIAL_RECEIPT_TIME);//Calendar.getInstance().getTime();
        ArrayList<Inventory> inventoryList = inventories.get(itemId).second;
        for(Inventory i: inventoryList)
        {
            //common.Utility.ShowMessage("calculate",""+i.UnitCount,context);
            if(i.RecordDate.before(startDate))startDate = i.RecordDate;//use for calculate sold unit
            total +=i.UnitCount;
        }

        //calculate already sold unit
        total -=GetSoldUnit(itemId,startDate);

        inventories.put(itemId,new Pair<Integer, ArrayList<Inventory>>(total,inventories.get(itemId).second));


    }
    public int DeleteInventoryRecord(long inventoryId,long itemId)
    {

        int rowAffected = new DatabaseHelper(context).DeleteInventory(inventoryId);

        //update the list manually if delete is a success
        if(rowAffected>0)
        {
            Pair<Integer,ArrayList<Inventory>> tempPair = inventories.get(itemId);
            for(int i=0;i<tempPair.second.size();i++)
            {
                if(tempPair.second.get(i).lngInventoryId==inventoryId)
                {
                    tempPair.second.remove(i);

                    break;
                }
            }
            //inventories.put(inventory.lngItemId,new Pair<Integer, ArrayList<Inventory>>(0,tempPair.second));
            CalculateInventory(itemId);
        }
       /* if(rowAffected>0)
        {
            inventories.remove(inventoryId);
            CalculateInventory(itemId);
        }*/
        return rowAffected;

    }
    public int Update(Inventory inventory)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String strWhereClause = Schema.DataTable_Inventory.ID_COLUMN + " =? and " +Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN+"=?";
        String[] args = new String[]{inventory.lngInventoryId+"","1"};
        String[] columns = new String[]{Schema.DataTable_Inventory.UNIT_COLUMN,
                Schema.DataTable_Inventory.COST_PRICE_COLUMN,
                Schema.DataTable_Inventory.SUPPLIER_COLUMN,
                Schema.DataTable_Inventory.RECORD_DATE_COLUMN};
        String[] values = new String[]{inventory.UnitCount+"",inventory.CostPrice.toPlainString(),inventory.lngSupplierId+"",inventory.RecordDate.getTime()+""};

        int affectedRow =helper.Update(Schema.DataTable_Inventory.TABLE_NAME,columns, values, strWhereClause, args);
        if(affectedRow>0)
        {
            Pair<Integer,ArrayList<Inventory>> tempPair = inventories.get(inventory.lngItemId);
            for(int i=0;i<tempPair.second.size();i++)
            {
                if(tempPair.second.get(i).lngInventoryId==inventory.lngInventoryId)
                {
                    tempPair.second.remove(i);
                    tempPair.second.add(inventory);
                    break;
                }
            }
            inventories.put(inventory.lngItemId,new Pair<Integer, ArrayList<Inventory>>(0,tempPair.second));
            CalculateInventory(inventory.lngItemId);
        }
        //common.Utility.ShowMessage("Affected row count",affectedRow+"",context);
        return affectedRow;
    }
    public long  InsertInventory(Inventory inventory)
    {
        String[] columns=new String[]{Schema.DataTable_Inventory.UNIT_COLUMN,
                                      Schema.DataTable_Inventory.RECORD_DATE_COLUMN,
                                      Schema.DataTable_Inventory.COST_PRICE_COLUMN,
                                      Schema.DataTable_Inventory.ITEM_COLUMN,
                                      Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN,
                                      Schema.DataTable_Inventory.SUPPLIER_COLUMN,
                                      Schema.DataTable_Inventory.ID_COLUMN};

        String[] values = new String[]{inventory.UnitCount+"",
                                       inventory.RecordDate.getTime()+"",
                                       inventory.CostPrice.toPlainString(),
                                       inventory.lngItemId+"",
                                       "1",
                                       inventory.lngSupplierId+"",
                                       inventory.lngInventoryId+""};

        long id =new DatabaseHelper(context).Insert(Schema.DataTable_Inventory.TABLE_NAME,columns,values);
        //manually update the list and the total inventory count if success
        if(id>-1)
        {
            inventories.get(inventory.lngItemId).second.add(inventory);
            CalculateInventory(inventory.lngItemId);

        }
        return id;
    }
    private int GetSoldUnit(long itemId,Date startDate)
    {
        int total = 0;
        String[] columns ={ Schema.DataTable_Receipt.CART_ITEM_COLUMN};
        String strWhereClause = Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=? and "+
                Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+">=?";

        String[] args={"1",startDate.getTime()+""};

        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor c = helper.query(Schema.DataTable_Receipt.TABLE_NAME,columns,strWhereClause,args,"");
        int columnCartItemIndex = c.getColumnIndex(Schema.DataTable_Receipt.CART_ITEM_COLUMN);
        while(!c.isAfterLast())
        {
            String[] strItems = c.getString(columnCartItemIndex).split("\\]\\[");
            for(String s:strItems)
            {
                s = s.replace("[", "");
                s = s.replace("]","");
                String[] strDetails = s.split(";");

                //we only interested in Stored Item string
                /**store item [si;unit;item id<space> version;modifier 1 <space> version,modifier 2 <space> version]**/
                if(strDetails[0].compareToIgnoreCase("si")==0 && Long.parseLong(strDetails[2].split(" ")[0])==itemId)
                {
                    total+=Integer.parseInt(strDetails[1]);
                }

            }
            c.moveToNext();
        }

        return total;
    }
    private ArrayList<Inventory> LoadInventoryList(long itemId)
    {
        ArrayList<Inventory> inventoryList = new ArrayList<Inventory>();

        DatabaseHelper helper = new DatabaseHelper(context);

        //column names
        String[] columns = {
                Schema.DataTable_Inventory.ID_COLUMN,
                Schema.DataTable_Inventory.SUPPLIER_COLUMN,
                Schema.DataTable_Inventory.ITEM_COLUMN,
                Schema.DataTable_Inventory.COST_PRICE_COLUMN,
                Schema.DataTable_Inventory.RECORD_DATE_COLUMN,
                Schema.DataTable_Inventory.UNIT_COLUMN
        };
        String strSortOrder = Schema.DataTable_Inventory.ID_COLUMN + " asc";

        String strWhereClause =Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN + "=? and "+Schema.DataTable_Inventory.ITEM_COLUMN+"=?";
        String[] args = {"1",itemId+""};

        Cursor c = helper.query(Schema.DataTable_Inventory.TABLE_NAME, columns, strWhereClause, args, strSortOrder);

        int columnIdIndex = c.getColumnIndex(Schema.DataTable_Inventory.ID_COLUMN);
        int columnSupplierIndex = c.getColumnIndex(Schema.DataTable_Inventory.SUPPLIER_COLUMN);
        int columnItemIndex = c.getColumnIndex(Schema.DataTable_Inventory.ITEM_COLUMN);
        int columnCostIndex = c.getColumnIndex(Schema.DataTable_Inventory.COST_PRICE_COLUMN);
        int columnRecordDateIndex = c.getColumnIndex(Schema.DataTable_Inventory.RECORD_DATE_COLUMN);
        int columnUnitIndex = c.getColumnIndex(Schema.DataTable_Inventory.UNIT_COLUMN);
        while(!c.isAfterLast())
        {
            Inventory inventory = new Inventory();
            inventory.lngInventoryId = c.getLong(columnIdIndex);
            inventory.lngSupplierId = c.getLong(columnSupplierIndex);
            inventory.lngItemId = c.getLong(columnItemIndex);
            inventory.CostPrice =new BigDecimal(c.getString(columnCostIndex));
            inventory.RecordDate = new Date(c.getLong(columnRecordDateIndex));
            inventory.UnitCount = c.getInt(columnUnitIndex);
            inventoryList.add(inventory);

            c.moveToNext();
        }

        c.close();
        helper.close();
        return inventoryList;
    }
}
