package tme.pos.BusinessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by vanlan on 7/30/2015.
 */
public class ChartManager {
    Context context;
    HashMap<String,Receipt>receipts;
    HashMap<Long,ArrayList<ItemObject>>itemAndModifierLogTable;
    public ChartManager(Context c)
    {
        context = c;
        receipts = new HashMap<String, Receipt>();
        itemAndModifierLogTable = new HashMap<Long,ArrayList<ItemObject>>();
    }
    public ArrayList<Pair<Integer,Float>>GetMonthlyData(int year)
    {
        ArrayList<Pair<Integer,Float>>data = new ArrayList<Pair<Integer, Float>>();

        DatabaseHelper helper = new DatabaseHelper(context);

        for(int i=0;i<12;i++) {
            Calendar calStart = new GregorianCalendar();//Calendar.getInstance();
            calStart.set(year, Calendar.JANUARY + i, 1, 0, 0, 0);
            Calendar calEnd = new GregorianCalendar();//Calendar.getInstance();
            calEnd.set(year ,Calendar.JANUARY+i,calStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
            String[] args={calStart.getTimeInMillis()+"",calEnd.getTimeInMillis()+""};
           /* String strQuery = "select sum("+Schema.DataTable_Receipt.TOTAL_COLUMN+") 'mySum', sum("+Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN+") 'myTaxSum'"+
                    " from "+Schema.DataTable_Receipt.TABLE_NAME+
                    " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+calStart.getTimeInMillis()+" and "+
                    calEnd.getTimeInMillis() + " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";*/
            String strQuery = "select sum("+Schema.DataTable_Receipt.AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN+") 'mySum', sum("+Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN+") 'myTaxSum'"+
                    " from "+Schema.DataTable_Receipt.TABLE_NAME+
                    " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+calStart.getTimeInMillis()+" and "+
                    calEnd.getTimeInMillis() + " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";
            Cursor cursor=helper.rawQuery(strQuery);
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {



                //data.add(new Pair<Integer, Float>(i+1, cursor.getFloat(0)));
                data.add(new Pair<Integer, Float>(i+1, cursor.getFloat(0)+cursor.getFloat(1)));


                cursor.moveToNext();
            }
        }
        return data;
    }
    public ArrayList<Pair<Integer,Float>> GetYearlyData(int startYear,int endYear)
    {
        ArrayList<Pair<Integer,Float>>data = new ArrayList<Pair<Integer, Float>>();
        int count = endYear-startYear+1;
        DatabaseHelper helper = new DatabaseHelper(context);
        //String strWhere= Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between ? and ? and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=?";
        //String[] strColumns=new String[]{Schema.DataTable_Receipt.TOTAL_COLUMN};
        for(int i=0;i<count;i++)
        {
            Calendar calStart =new GregorianCalendar();//Calendar.getInstance();
            calStart.set(startYear + i, Calendar.JANUARY, 1, 0, 0, 0);
            Calendar calEnd =new GregorianCalendar();//Calendar.getInstance();
            calEnd.set(startYear+i,Calendar.DECEMBER,31,23,59,59);


            /*String strQuery = "select sum("+Schema.DataTable_Receipt.TOTAL_COLUMN+") 'mySum', sum("+Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN+") 'myTaxSum'"+

                              " from "+Schema.DataTable_Receipt.TABLE_NAME+
                                " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+calStart.getTimeInMillis()+" and "+
                    calEnd.getTimeInMillis() + " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";*/
            String strQuery = "select sum("+Schema.DataTable_Receipt.AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN+") 'mySum', sum("+Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN+") 'myTaxSum'"+

                    " from "+Schema.DataTable_Receipt.TABLE_NAME+
                    " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+calStart.getTimeInMillis()+" and "+
                    calEnd.getTimeInMillis() + " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";

            Cursor cursor=helper.rawQuery(strQuery);
            cursor.moveToFirst();
            while(!cursor.isAfterLast()) {


                //data.add(new Pair<Integer, Float>(startYear + i, cursor.getFloat(0)));
                data.add(new Pair<Integer, Float>(startYear + i, cursor.getFloat(0)+cursor.getFloat(1)));


                cursor.moveToNext();
            }

        }

        return data;
    }
    private int ConvertMonthToCalendarMonth(int month)
    {
        switch (month)
        {
            case 1:
                return Calendar.JANUARY;
            case 2:
                return Calendar.FEBRUARY;
            case 3:
                return Calendar.MARCH;
            case 4:
                return Calendar.APRIL;
            case 5:
                return Calendar.MAY;
            case 6:
                return Calendar.JUNE;
            case 7:
                return Calendar.JULY;
            case 8:
                return Calendar.AUGUST;
            case 9:
                return Calendar.SEPTEMBER;
            case 10:
                return Calendar.OCTOBER;
            case 11:
                return Calendar.NOVEMBER;
            default:
                return Calendar.DECEMBER;
        }
    }
    public DailyChartModel GetDailySalesData(int year,int calendarMonth)
    {
        DailyChartModel data = new DailyChartModel();
        HashMap<Integer,Float> DailySalesData = new HashMap<Integer, Float>();
        HashMap<String,Float>GratuityData = new HashMap<String, Float>();
        HashMap<Long,Integer>BestSellingDataByItemId = new HashMap<Long,Integer>();



        DatabaseHelper helper = new DatabaseHelper(context);

        Calendar calStart= new GregorianCalendar();//Calendar.getInstance();
        calStart.set(year,calendarMonth,1,0,0,0);

        Calendar calEnd = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("dd");//"dd/MM/yyyy");
        int intTodayDate =(calEnd.get(Calendar.YEAR)==year && calEnd.get(Calendar.MONTH)==calendarMonth)?
                calEnd.get(Calendar.DAY_OF_MONTH)//Integer.parseInt(dateFormat.format(calEnd.getTime()).substring(0,2))
                :calStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        calEnd = new GregorianCalendar();
        calEnd.set(year, calendarMonth,intTodayDate, 23, 59, 59);
        //calEnd.set(year, calendarMonth, , 23, 59, 59);

        Server[] servers =common.serverList.GetServers();


        String strQuery = "select * "+
                " from "+Schema.DataTable_Receipt.TABLE_NAME+
                " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+calStart.getTimeInMillis()+" and "+
                calEnd.getTimeInMillis() + " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";


        Cursor cursor=helper.rawQuery(strQuery);
        cursor.moveToFirst();
        int intDate = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN);
        int intGratuity = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.GRATUITY_COLUMN);
        int intServer = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.SERVER_ID_COLUMN);
        int intReceiptNum = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN);
        //int intTotal = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TOTAL_COLUMN);
        int intTotal = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN);
        int intTaxAmount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN);
        int intCartItem = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.CART_ITEM_COLUMN);

        int intExistingCount =0;
        int indexStart=0;
        int indexEnd=0;
        while(!cursor.isAfterLast())
        {
            Calendar cal = new GregorianCalendar();//Calendar.getInstance();
            cal.setTimeInMillis(cursor.getLong(intDate));
            int intCurrentDate =Integer.parseInt(dateFormat.format(cal.getTimeInMillis()));
            //int DayOfMonth =cal.get(Calendar.DAY_OF_MONTH);
            float value=0;
            if(DailySalesData.containsKey(intCurrentDate))
            {
                value = DailySalesData.get(intCurrentDate);
            }
            DailySalesData.put(intCurrentDate,value+cursor.getFloat(intTotal)+cursor.getFloat(intTaxAmount));
           /* if(DailySalesData.containsKey(DayOfMonth))
            {
                value = DailySalesData.get(DayOfMonth);
            }
            DailySalesData.put(DayOfMonth,value+cursor.getFloat(intTotal)+cursor.getFloat(intTaxAmount));*/

            //calculate gratuity
             long serverId =cursor.getLong(intServer);
             for(int i=0;i< servers.length;i++)
             {
                 if(servers[i].EmployeeId==serverId)
                 {
                     if(GratuityData.containsKey(servers[i].Name))
                     {
                         GratuityData.put(servers[i].Name, GratuityData.get(servers[i].Name));
                     }
                     else {
                         GratuityData.put(servers[i].Name, cursor.getFloat(intGratuity));
                     }
                     break;
                 }
             }

            //collect item id
            String strItems = cursor.getString(intCartItem);
            if(strItems.length()>0) {
                strItems = strItems.substring(1, strItems.length() - 1);
                String[] items = strItems.split("\\]\\[");
                for (int j = 0; j < items.length; j++) {

                    if(items[j].substring(0,2).equalsIgnoreCase("pa"))continue;//skip if is promotion object
                    Long lnItemId = Long.parseLong(StoreItem.ReturnItemId(items[j]));


                    if (BestSellingDataByItemId.containsKey(lnItemId)) {
                        intExistingCount = BestSellingDataByItemId.get(lnItemId);

                    }
                    BestSellingDataByItemId.put(lnItemId, intExistingCount + Integer.parseInt(StoreItem.ReturnUnitCount(items[j])));
                    intExistingCount = 0;
                }
            }
            cursor.moveToNext();
        }
        //transform raw data into sorted list
        data.BestSellingData= GetTopFiveItem(BestSellingDataByItemId);

        //sort the gratuity data
        ArrayList<Pair<String,Float>> gratuityArry = new ArrayList<Pair<String,Float>>();

        for(String s:GratuityData.keySet())
        {
            gratuityArry.add(new Pair<String,Float>(s,GratuityData.get(s)));
        }
        Collections.sort(gratuityArry, new GratuityDataComparator());

        data.GratuityData = gratuityArry;


        //daily sales figure
        //for(int i=2;i<=31;i++)DailySalesData.put(i,123.45f);
        data.DailySalesData = ReFormatAndSortDailySaleValues(DailySalesData,Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));//intTodayDate);//calStart.getActualMaximum());

        return data;
    }
    private ArrayList<Pair<Integer,Float>>ReFormatAndSortDailySaleValues(HashMap<Integer,Float> data,int maxDay)
    {
        ArrayList<Pair<Integer,Float>>lst = new ArrayList<Pair<Integer, Float>>();
        maxDay=(data.size()==0)?0:maxDay;
        for(int i=1;i<maxDay+1;i++)
        {
            if(!data.containsKey(i))
            {
                lst.add(new Pair<Integer,Float>(i,0f));
            }else
            {
                lst.add(new Pair<Integer,Float>(i,data.get(i)));
            }
        }

        return lst;
    }
    private ArrayList<Pair<String,Integer>>GetTopFiveItem(HashMap<Long,Integer> BestSellingDataByItemId)
    {
        ArrayList<Pair<String,Integer>>data = new ArrayList<Pair<String,Integer>>();
        ArrayList<Pair<Long,Integer>> tempArraylst = new ArrayList<Pair<Long,Integer>>();
        ArrayList<Pair<Long,Integer>> topFive = new ArrayList<Pair<Long,Integer>>();
        //get 5 best selling items
        if(BestSellingDataByItemId.size()>0)
        {
            //get top 5 only, and combining others into one category
            int count=0;
            for(Long l:BestSellingDataByItemId.keySet())
            {
                tempArraylst.add(new Pair<Long,Integer>(l,BestSellingDataByItemId.get(l)));
               /* if(tempArraylst.size()==0)
                {
                    tempArraylst.add(new Pair<Long,Integer>(l,BestSellingDataByItemId.get(l)+count));
                    continue;
                }
                count = BestSellingDataByItemId.get(l);
                for(int i=0;i<tempArraylst.size();i++)
                {
                    if(tempArraylst.get(i).first==l)
                    {
                        count += tempArraylst.get(i).second;
                        tempArraylst.remove(i);
                        tempArraylst.add(new Pair<Long, Integer>(l, count));

                        break;
                    }

                    if(i==tempArraylst.size()-1)
                    {
                        tempArraylst.add(new Pair<Long,Integer>(l,BestSellingDataByItemId.get(l)));
                        break;
                    }
                }*/
                //if(tempArraylst.size()<11)
                //{

                /*}
                else {

                    if(tempArraylst.get(tempArraylst.size()-1).second<count)
                    {
                        tempArraylst.remove(tempArraylst.size()-1);
                        tempArraylst.add(new Pair<Long,Integer>(l,count));
                    }
                    Collections.sort(tempArraylst,new BestSellingItemComparator());
                }*/
            }
            Collections.sort(tempArraylst,new BestSellingItemComparator());
        }
        //collect top 5 item ids

        int end = (tempArraylst.size()>5)?5:tempArraylst.size();
        Long[] tempIds= new Long[end];//tempArraylst.size()];
       /* while(tempArraylst.size()>end)
        {
            if(tempArraylst.get(end-1).second==tempArraylst.get(end).second) {
                end++;
            }
            else
            {
                break;
            }
        }*/
        //int end = (tempArraylst.size()>5)?5:tempArraylst.size();
        for(int i=0;i<end;i++)
        {
            tempIds[i]=tempArraylst.get(i).first;
        }

        HashMap<Long,String>refItem= new MyMenu(context).GetItemName(tempIds);
        int otherCount = 0;

        for(int i=0;i<tempArraylst.size();i++)
        {
            if(i<end) {
                data.add(new Pair<String, Integer>(refItem.get(tempArraylst.get(i).first), tempArraylst.get(i).second));
            }
            else
            {
                otherCount+=tempArraylst.get(i).second;
            }
        }

        if(otherCount>0)
            data.add(new Pair<String, Integer>("Others", otherCount));

        return data;
    }
    private class BestSellingItemComparator implements Comparator<Pair<Long,Integer>>
    {
        @Override
        public int compare(Pair<Long,Integer>p1,Pair<Long,Integer>p2)
        {
            return p1.second.compareTo(p2.second)*-1;
        }
    }
    private class GratuityDataComparator implements Comparator<Pair<String,Float>>
    {
        @Override
        public int compare(Pair<String,Float>p1,Pair<String,Float>p2)
        {
            return p1.second.compareTo(p2.second);
        }
    }
    private  Enum.GetLockResult GetReceiptTableLock(String strReceiptNum,int receiptVersion,SQLiteDatabase db) {
        common.Utility.LogActivity("get locks for receipt table with receipt number ["+strReceiptNum+"]");

        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        contentValues.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());

        String strWhereClause="("+Schema.VERSION_COLUMN+"=? and "
                +Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+"=? and "
                +Schema.LOCK_BY_COLUMN+"=?"+") ";
        String[] args={""+receiptVersion,strReceiptNum,""};

        //where clause to get total count for related record
        String strRawSQLCountWhereClause = Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+"='"+strReceiptNum+"'";

        //version check
        String strRawSQLVersionCheckWhereClause = "Select "+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+","+
                Schema.VERSION_COLUMN+","+
                 " from "+
                Schema.DataTable_Receipt.TABLE_NAME+" where "+Schema.LOCK_BY_COLUMN+"='"+AppSettings.DEVICE_UNIQUE_ID+"'";

        String[] versionCheckParams = {Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN,Schema.VERSION_COLUMN};
        Object[] valuesToCompare = new Object[1];
        valuesToCompare[0]= new ReceiptCompareModel(receiptVersion,strReceiptNum);

        Enum.GetLockResult result =  new DatabaseHelper(context).GetLocksWithOwnDB(contentValues,
                strWhereClause,
                args,
                Schema.DataTable_Receipt.TABLE_NAME,
                strRawSQLCountWhereClause,
                strRawSQLVersionCheckWhereClause,
                versionCheckParams,
                valuesToCompare,
                db,
                Enum.CompareObjectVersionType.Receipt);

        return result;
    }
    private Enum.DBOperationResult DeletePartialPaidReceipts(String strTableId, ArrayList<Receipt> completeReceipts,Receipt targetReceipt)//,  SQLiteDatabase db)
    {

        common.Utility.LogActivity("setting up variables to delete partial paid receipt and  table id ["+strTableId+"]");
        //get lock on all receipts on this table
        int fieldCount = 5;
        String strWhereClause_Orders="";
        int index=0;
/****************************************FOR ORDER TABLE USE***************************************************/
        String[] args_Orders = new String[fieldCount*completeReceipts.size()];

        ContentValues contentValues_Orders = new ContentValues();
        contentValues_Orders.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        contentValues_Orders.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());
        for(int i=0;i<completeReceipts.size();i++)
        {
            strWhereClause_Orders+="("+Schema.VERSION_COLUMN+"=? and "
                    +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                    +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=? and "
                    +Schema.DataTable_Orders.DATE_COLUMN+"=? and "
                    +Schema.LOCK_BY_COLUMN+"=?"+") "+((i==completeReceipts.size()-1)?"":" or ");

            args_Orders[index++]=completeReceipts.get(i).Version+"";
            args_Orders[index++]=completeReceipts.get(i).myCart.tableId+"";
            args_Orders[index++]=completeReceipts.get(i).myCart.receiptIndex+"";
            args_Orders[index++]=completeReceipts.get(i).lngLastUpdateDate+"";
            args_Orders[index++]="";
        }
        //where clause to get total count for related record
        String strRawSQLCountWhereClause_Orders = Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"='"+completeReceipts.get(0).myCart.tableId+"'";
        //version check
        String strRawSQLVersionCheckWhereClause_Orders = "Select "+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+","+
                Schema.VERSION_COLUMN+","+
                Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+","+
                Schema.DataTable_Orders.DATE_COLUMN+" from "+
                Schema.DataTable_Orders.TABLE_NAME+" where "+Schema.LOCK_BY_COLUMN+"='"+AppSettings.DEVICE_UNIQUE_ID+"'";
        String[] versionCheckParams_Orders = {Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN,Schema.VERSION_COLUMN,Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN,Schema.DataTable_Orders.DATE_COLUMN};
        Object[] valuesToCompare_Orders = new Object[completeReceipts.size()];
        for(int i=0;i<completeReceipts.size();i++) {
            Receipt r = completeReceipts.get(i);
            valuesToCompare_Orders[i] = new OrderCompareModel(r.Version,r.myCart.tableId,r.myCart.receiptIndex,r.lngLastUpdateDate);
        }

/*****************************************FOR RECEIPT TABLE USE ****************************************************************/

        ContentValues contentValues_Receipt = new ContentValues();
        contentValues_Receipt.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        contentValues_Receipt.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());

        String strWhereClause_Receipt="("+Schema.VERSION_COLUMN+"=? and "
                +Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+"=? and "
                +Schema.LOCK_BY_COLUMN+"=?"+") ";
        String[] args_Receipt={""+targetReceipt.Version,targetReceipt.receiptNumber,""};

        //where clause to get total count for related record
        String strRawSQLCountWhereClause_Receipt = Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+"='"+targetReceipt.receiptNumber+"'";

        //version check
        String strRawSQLVersionCheckWhereClause_Receipt = "Select "+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+","+
                Schema.VERSION_COLUMN+
                " from "+
                Schema.DataTable_Receipt.TABLE_NAME+" where "+Schema.LOCK_BY_COLUMN+"='"+AppSettings.DEVICE_UNIQUE_ID+"'";

        String[] versionCheckParams_Receipt = {Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN,Schema.VERSION_COLUMN};
        Object[] valuesToCompare_Receipt = new Object[1];
        valuesToCompare_Receipt[0]= new ReceiptCompareModel(targetReceipt.Version,targetReceipt.receiptNumber);

        Enum.DBOperationResult result = new DatabaseHelper(context).DeleteReceiptFromReceiptTableWithTransactions(contentValues_Orders,
                strWhereClause_Orders,
                args_Orders,
                strRawSQLCountWhereClause_Orders,
                strRawSQLVersionCheckWhereClause_Orders,
                versionCheckParams_Orders,
                valuesToCompare_Orders,
                contentValues_Receipt,
                strWhereClause_Receipt,
                args_Receipt,
                strRawSQLCountWhereClause_Receipt,
                strRawSQLVersionCheckWhereClause_Receipt,
                versionCheckParams_Receipt,
                valuesToCompare_Receipt,
                targetReceipt.receiptNumber,
                completeReceipts,
                false);

        //if(result== Enum.DBOperationResult.Success)targetReceipt.Version++;
        /*Enum.GetLockResult result =   new DatabaseHelper(context).GetLocksWithOwnDB(contentValues,
                strWhereClause,
                args,
                Schema.DataTable_Orders.TABLE_NAME,
                strRawSQLCountWhereClause,
                strRawSQLVersionCheckWhereClause,
                versionCheckParams,
                valuesToCompare,
                db,
                Enum.CompareObjectVersionType.Order);*/

        return result;
    }
    public Enum.DBOperationResult UpdateReceiptActiveStatus(boolean blnActive,Receipt receipt,String strReceiptDate,ArrayList<Receipt>receipts)
    {
        //DatabaseHelper helper = new DatabaseHelper(context);
        //receipt.Version++;//increase version number
        //SQLiteDatabase db = helper.getWritableDatabase();

        String[] columns=null;
        String[] values=null;
        int affectRowCount =0;
        Enum.DBOperationResult result = Enum.DBOperationResult.Success;
        //db.beginTransaction();
        try {



            columns = new String[]{Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN, Schema.DataTable_Receipt.CANCEL_DATE_COLUMN};
            values = new String[]{((blnActive) ? "1" : "0"),
                    ((blnActive) ? "" : Calendar.getInstance().getTimeInMillis() + "")};
            String strWhereClause = Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN + " =? and " + Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN + " =?";
            String[] args = new String[]{strReceiptDate, receipt.receiptNumber};


           /**this logic will determine whether the receipt series still in checkout progress or is old archive **/
            //if that is only one receipt for the table id, the whole list of sub receipt already cleared and marked completed
            //regular status update
            if(receipt.strLinkedReceipts.length()>0)
            {
                affectRowCount = new DatabaseHelper(context).Update(Schema.DataTable_Receipt.TABLE_NAME,columns,values,strWhereClause,args);

            }
            else
            {
                /***delete the target record (linked sub receipt index remain the same, no need update)***/
                //get lock for all record in [order table]
                String strTableId = receipts.get(0).myCart.tableId;
                Receipt targetReceipt = null;
                for(Receipt r:receipts) {
                    if(r.receiptNumber.compareTo(receipt.receiptNumber)==0) {
                        targetReceipt = r;
                        break;
                    }
                }
                result = DeletePartialPaidReceipts(strTableId,receipts,targetReceipt);




            }




        }
        catch(Exception ex) {
            common.Utility.LogActivity(ex.getMessage());
            result = Enum.DBOperationResult.Failed;
        }
        finally {
            //db.endTransaction();

        }

        return result;

    }
    private void LoadPromtionLogRecord(long promotionId,int version,HashMap<Long,ArrayList<PromotionObject>>tbl)
    {
        MyPromotionManager mgr = new MyPromotionManager(context);
        PromotionObject po = mgr.Get(promotionId,version);
        if(!tbl.containsKey(promotionId))
        {
            tbl.put(promotionId,new ArrayList<PromotionObject>());
        }
        if(po!=null)
        {
            tbl.get(promotionId).add(po);
        }

    }
    private void LoadItemLogRecord(long lnId,HashMap<Long,ArrayList<ItemObject>> tbl)
    {


        DatabaseHelper helper = new DatabaseHelper(context);


        String strQuery = "select *"+
                " from "+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+
                " where "+Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN+"="+lnId;

        Cursor cursor=helper.rawQuery(strQuery);
        int intDate = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.DATE_COLUMN);
        int intPrice = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN);
        int intName = cursor.getColumnIndexOrThrow(Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN);
        int intVersion = cursor.getColumnIndexOrThrow(Schema.VERSION_COLUMN);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()) {

            //Calendar cal = Calendar.getInstance();
            //cal.setTimeInMillis(cursor.getLong(intDate));
            long lnTime = cursor.getLong(intDate);
            if(!tbl.containsKey(lnId))
            {
                tbl.put(lnId,new ArrayList<ItemObject>());
            }

            ArrayList<ItemObject> records = tbl.get(lnId);

            ItemObject io = new ItemObject(lnId
                    ,cursor.getString(intName)
                    ,-1
                    ,cursor.getString(intPrice)
                    ,"",false,0
                    ,cursor.getInt(intVersion));

            SortInsert(records,  io);


            cursor.moveToNext();
        }
    }
    private void SortInsert(ArrayList<ItemObject> lst,ItemObject io)
    {

        for(int i=0;i<lst.size();i++)
        {
            if(lst.get(i).GetCurrentVersionNumber()>io.GetCurrentVersionNumber())
            {
                lst.add(i,io);
                return;
            }
        }

        lst.add(io);
    }

    public MyCart ConvertItemAndPromotionIds2Object(String strValues)
    {

        /**FORMATTED
         store item [si;unit;item id<space> version;modifier 1 <space> version,modifier 2 <space> version]
         Promotion awarded [pa;unit;promotion object id;sub version;receipt index<space>cart GUID,receipt index<space>cart GUID;item id <space> receipt id <space> unit count,item id <space> receipt id <space> unit count]
         **/

        if(strValues.length()==0)return null;
        strValues = strValues.substring(1,strValues.length()-1);
        String[] strItems = strValues.split("\\]\\[");
        MyMenu mm = new MyMenu(context);
        MyPromotionManager mpm = new MyPromotionManager(context);
        MyCart mc = new MyCart(common.myAppSettings.GetTaxPercentage(),"-1",0);

        for(int i=0;i<strItems.length;i++)
        {

            String[] strDetails = strItems[i].split(";");
            if(strDetails[0].equalsIgnoreCase("si"))
            {
                String[] itemDetails = strDetails[2].split(" ");
                StoreItem si = new StoreItem(mm.GetItem(Long.parseLong(itemDetails[0]),Integer.parseInt(itemDetails[1])));
                si.UnitOrder = Integer.parseInt(strDetails[1]);

                if(strDetails.length>3)
                {
                    String[] modifiers = strDetails[3].split(",");
                    for(int j=0;j<modifiers.length;j++)
                    {
                        String[] modifierDetails = modifiers[j].split(" ");
                        si.modifiers.add(mm.GetModifier(Long.parseLong(modifierDetails[0]),Integer.parseInt(modifierDetails[1])));
                    }
                }
                //later will load promotions
                mc.GetDisplayCartItemList().add(new CartDisplayItem(null,si, Enum.CartItemType.StoreItem));
                mc.GetItems().add(si);
            }
            else if(strDetails[0].equalsIgnoreCase("pa"))
            {
                PromotionObject po = mpm.Get(Long.parseLong(strDetails[2]),Integer.parseInt(strDetails[3]));

                if(po!=null) {
                    PromotionAwarded pa = new PromotionAwarded(po);
                    pa.unit = Integer.parseInt(strDetails[1]);
                    if(strDetails.length>4) {
                        String[] strCartGUIDs = strDetails[4].split(",");
                        for(int k = 0;k<strCartGUIDs.length;k++) {
                            String[] cartInfo =strCartGUIDs[k].split(" ");
                            pa.cart_GUID_Associate_ReceiptID.put(Integer.parseInt(cartInfo[0]),cartInfo[1]);
                        }
                    }

                    if(strDetails.length>5)
                    {
                        String[] strShares = strDetails[5].split(",");
                        for(int k=0;k<strShares.length;k++)
                        {
                            String[] shareDetails = strShares[k].split(" ");
                            long itemId = Long.parseLong(shareDetails[0]);
                            int receiptIndex = Integer.parseInt(shareDetails[1]);
                            int unit = Integer.parseInt(shareDetails[2]);
                            if(!pa.collectedItems.containsKey(itemId))
                            {
                                pa.collectedItems.put(itemId,new HashMap<Integer, Integer>());
                            }
                            pa.collectedItems.get(itemId).put(receiptIndex,unit);
                        }
                    }
                    mc.GetDisplayCartItemList().add(new CartDisplayItem(pa,null, Enum.CartItemType.PromotionAwarded));
                }
            }

        }
        return mc;
    }
    public Receipt GetReceipt(String strReceiptNum)
    {
        if(!receipts.containsKey(strReceiptNum))
        {
            DatabaseHelper helper = new DatabaseHelper(context);


            String strQuery = "select *"+
                    " from "+Schema.DataTable_Receipt.TABLE_NAME+
                    " where "+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+"='"+strReceiptNum+"'";

            Cursor cursor=helper.rawQuery(strQuery);
            cursor.moveToFirst();
            if(cursor.isAfterLast())return null;
            int intDate = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN);
            int intGratuity = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.GRATUITY_COLUMN);
            int intDiscount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.DISCOUNT_COLUMN);
            int intServer = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.SERVER_ID_COLUMN);
            int intReceiptNum = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN);//prefixed with user defined data

            int intLink = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.LINKED_RECEIPT_COLUMN);
            int intTax = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TAX_COLUMN);
            int intCartItem = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.CART_ITEM_COLUMN);
            int intTable = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.DINING_TABLE_COLUMN);
            int intLatitude = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.LATITUDE_COLUMN);
            int intLongitude = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.LONGITUDE_COLUMN);
            int intActive = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN);
            int intPromotionByCash = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.PROMOTION_ID_COLUMN);
            //String strCartItem = cursor.getString(intCartItem);
            MyCart mc=ConvertItemAndPromotionIds2Object(cursor.getString(intCartItem));//ConvertItemIds2CartObject(cursor.getString(intCartItem));
            if(mc==null)return null;
            mc.percentage = cursor.getFloat(intTax);
            mc.tableId = cursor.getString(intTable);
/*********************************************END REPLACEMENT*********************************************8*/
            Server server = common.serverList.GetServer(cursor.getLong(intServer));
            Receipt receipt = new Receipt(mc,
                    mc.percentage,
                    common.companyProfile,
                    common.myAppSettings.GetReceiptHeaderText(),
                    common.myAppSettings.GetReceiptFooterText(),
                    cursor.getString(intTable),
                    cursor.getString(intReceiptNum),
                    common.myAppSettings.GetHeaderNoteCenterAlignment(),
                    common.myAppSettings.GetFooterNoteCenterAlignment(),
                    cursor.getDouble(intLatitude),
                    cursor.getDouble(intLongitude),
                    server,
                    true,
                    1);
            receipt.CashValueForDiscount = cursor.getFloat(intDiscount);
            receipt.CashValueForGratuity = cursor.getFloat(intGratuity);
            Calendar cal = new GregorianCalendar();//Calendar.getInstance();
            cal.setTimeInMillis(cursor.getLong(intDate));
            receipt.receiptDateTime = cal;
            if(cursor.getInt(intActive)==0)receipt.blnActive = false;
            receipt.strLinkedReceipts = cursor.getString(intLink);

            //promotion object
            String strPo = cursor.getString(intPromotionByCash);
            if(strPo.length()>0)
            {
                String[] details = strPo.split(";");
                receipt.myCart.promotionObject =common.myPromotionManager.Get(Long.parseLong(details[0]),Integer.parseInt(details[1]));

            }

            receipts.put(cursor.getString(intReceiptNum),receipt);
            cursor.close();
            helper.close();
        }


        if(receipts.containsKey(strReceiptNum))
            return receipts.get(strReceiptNum);

        return null;
    }
    public ArrayList<Pair<String,String[]>>SearchReceipt(String strKeyword)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        ArrayList<Pair<String,String[]>> data = new ArrayList<Pair<String,String[]>>();
        String strQuery = "select *"+
                " from "+Schema.DataTable_Receipt.TABLE_NAME+
                " where "+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+" like '%"+strKeyword+"%'";
                //" where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" like '%"+strKeyword+"%'";

        Cursor cursor=helper.rawQuery(strQuery);
        int intDate = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN);
        int intReceiptNum = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN);//prefixed with user defined data
        int intTotal = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TOTAL_COLUMN);
        int intTaxAmount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN);
        int intActive = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {

            Calendar cal = new GregorianCalendar();//Calendar.getInstance();
            cal.setTimeInMillis(cursor.getLong(intDate));

            data.add(new Pair<String, String[]>(cursor.getString(intReceiptNum),
                            new String[]{cursor.getString(intDate),common.Utility.ReturnDateString(cal,true),
                                    (cursor.getFloat(intTotal)+cursor.getFloat(intTaxAmount))+"",cursor.getString(intActive)}
                    )
            );

            cursor.moveToNext();
        }

        return data;
    }
    public ArrayList<Pair<String,String[]>>GetReceiptOfParticularServer(long lnStart,long lnEnd,long employeeId)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        ArrayList<Pair<String,String[]>> data = new ArrayList<Pair<String,String[]>>();
        //DateFormat df = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
        String strQuery = "select *"+
                " from "+Schema.DataTable_Receipt.TABLE_NAME+
                " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+lnStart+" and "+
                lnEnd+
                " and "+Schema.DataTable_Receipt.SERVER_ID_COLUMN+"="+employeeId+
            " order by "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN;//+ " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";
        Cursor cursor=helper.rawQuery(strQuery);
        int intDate = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN);
        int intGratuity = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.GRATUITY_COLUMN);
        int intServer = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.SERVER_ID_COLUMN);
        int intDiscount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.DISCOUNT_COLUMN);
        int intReceiptNum = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN);//prefixed with user defined data
        int intTotal = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TOTAL_COLUMN);
        int intTaxAmount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN);
        int intActive = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN);
        //int intCartItem = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.CART_ITEM_COLUMN);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {

            Calendar cal = new GregorianCalendar();//Calendar.getInstance();
            cal.setTimeInMillis(cursor.getLong(intDate));

            data.add(new Pair<String, String[]>(cursor.getString(intReceiptNum),
                            new String[]{cursor.getString(intDate),common.Utility.ReturnDateString(cal,true),
                                    (cursor.getFloat(intGratuity))+"",cursor.getString(intActive)}
                    )
            );

            cursor.moveToNext();
        }

        return data;

    }
    public ArrayList<Pair<Receipt,String>>GetAllReceipts() {
        ArrayList<Pair<Receipt,String>>rs = new ArrayList<Pair<Receipt,String>>();
        DatabaseHelper helper = new DatabaseHelper(context);
        ArrayList<Pair<String,String[]>> data = new ArrayList<Pair<String,String[]>>();
        //DateFormat df = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
        String strQuery = "select *"+
                " from "+Schema.DataTable_Receipt.TABLE_NAME;
        Cursor cursor=helper.rawQuery(strQuery);
        int intV = cursor.getColumnIndexOrThrow(Schema.VERSION_COLUMN);
        int intTable = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.DINING_TABLE_COLUMN);
        int intNum = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN);
        int intLink = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.LINKED_RECEIPT_COLUMN);
        int intActive = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN);
        int intLockBy = cursor.getColumnIndex(Schema.LOCK_BY_COLUMN);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            rs.add(new Pair<Receipt,String>(new Receipt(new MyCart(0f,cursor.getString(intTable),0),0,new CompanyProfile()
            ,"","",cursor.getString(intTable),cursor.getString(intNum), Enum.ReceiptNoteAlignment.center
            , Enum.ReceiptNoteAlignment.center,0,0,null,true,cursor.getInt(intV)),cursor.getString(intLockBy)));
            rs.get(rs.size()-1).first.strLinkedReceipts = cursor.getString(intLink);
            rs.get(rs.size()-1).first.blnActive = cursor.getInt(intActive)>0?true:false;
            cursor.moveToNext();
        }
        return rs;
    }
    public ArrayList<Pair<String,String[]>>GetReceipt(long lnStart,long lnEnd)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        ArrayList<Pair<String,String[]>> data = new ArrayList<Pair<String,String[]>>();
        //DateFormat df = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
        String strQuery = "select *"+
                " from "+Schema.DataTable_Receipt.TABLE_NAME+
                " where "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" between "+lnStart+" and "+
                lnEnd+ " order by "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN;//+ " and "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+"=1";
        Cursor cursor=helper.rawQuery(strQuery);
        int intDate = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN);
        int intGratuity = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.GRATUITY_COLUMN);
        int intServer = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.SERVER_ID_COLUMN);
        int intDiscount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.DISCOUNT_COLUMN);
        int intReceiptNum = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN);//prefixed with user defined data
        int intTotal = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TOTAL_COLUMN);
        int intTaxAmount = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN);
        int intActive = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN);
        //int intCartItem = cursor.getColumnIndexOrThrow(Schema.DataTable_Receipt.CART_ITEM_COLUMN);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {

            Calendar cal = new GregorianCalendar();//Calendar.getInstance();
            cal.setTimeInMillis(cursor.getLong(intDate));

            data.add(new Pair<String, String[]>(cursor.getString(intReceiptNum),
                    new String[]{cursor.getString(intDate),common.Utility.ReturnDateString(cal,true),
                            (cursor.getFloat(intTotal)
                                    +cursor.getFloat(intTaxAmount)
                            +cursor.getFloat(intGratuity)
                            -cursor.getFloat(intDiscount))+"",cursor.getString(intActive)}
                    )
            );

            cursor.moveToNext();
        }

        return data;
    }
}
