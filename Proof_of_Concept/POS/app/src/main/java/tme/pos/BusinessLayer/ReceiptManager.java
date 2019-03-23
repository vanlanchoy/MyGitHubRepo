package tme.pos.BusinessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.SyncStateContract;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;
import tme.pos.BusinessLayer.Enum;
/**
 * Created by kchoy on 4/15/2015.
 */
public class ReceiptManager {
    Context context;
    int currentReceiptCountVersion=0;
    //int receiptNumber=0;
    public ReceiptManager(Context c)
    {
        context = c;
    }

   /* private void LoadCurrentMaxReceiptNumber()
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        int count=0;
        Cursor cursor=helper.rawQuery("select count("+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN
                +") from "+Schema.DataTable_Receipt.TABLE_NAME);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            receiptNumber =cursor.getInt(0);
            cursor.moveToNext();
        }

        cursor.close();


    }*/
    private ReceiptCountObj LoadCurrentReceiptNumber() {
        ReceiptCountObj rco = new ReceiptCountObj();
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor c = helper.getReadableDatabase().rawQuery("select "+Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN+
                ","+Schema.VERSION_COLUMN+","+Schema.LOCK_BY_COLUMN+","+Schema.LOCK_TIME_STAMP_COLUMN+ " from "+
                Schema.DataTable_ReceiptCountData.TABLE_NAME,null);
        c.moveToFirst();
        if(!c.isAfterLast()) {
            int indexCol = c.getColumnIndex(Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN);
            int versionCol = c.getColumnIndex(Schema.VERSION_COLUMN);
            int lockByCol = c.getColumnIndex(Schema.LOCK_BY_COLUMN);
            int lockDateCol =c.getColumnIndex(Schema.LOCK_TIME_STAMP_COLUMN);
            rco.LockedDateTime = c.getLong(lockDateCol);
            rco.receiptCount = c.getInt(indexCol);
            rco.Version = c.getInt(versionCol);
            rco.DeviceId = c.getString(lockByCol);
        }
        c.close();
        return rco;
    }
    private int GetNextAvailableReceiptNumber()
    {
        int receiptNum = 0;
        Enum.DBOperationResult result = Enum.DBOperationResult.Success;
        ReceiptCountObj rco = LoadCurrentReceiptNumber();
        //get current receipt index in data table, and increase index count + 1 after query
        if(currentReceiptCountVersion==0) {

            if(rco.receiptCount>0) {
                currentReceiptCountVersion = rco.Version;
            }
        }
        if(currentReceiptCountVersion==0) {
            //insert new record
            result = InsertNewReceiptNumRow();
            receiptNum =(result== Enum.DBOperationResult.Success)?1:0;
        }
        else {
            //set lock, read and update
            receiptNum = GenerateNextReceiptNumber(rco);
        }

        return receiptNum;
    }
    private Enum.GetLockResult LockReceiptNumberRecord(ReceiptCountObj rco) {

        //unlock any expired lock
        UnLockRecords(Schema.DataTable_ReceiptCountData.TABLE_NAME);
        //now lock the record
        String strSql = Schema.VERSION_COLUMN+"=? and "+Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN+"=? and  ("+
                Schema.LOCK_BY_COLUMN+"=? or "+Schema.LOCK_BY_COLUMN+" is null)";
        String[] args = new String[]{rco.Version+"",rco.receiptCount+"",""};
        ContentValues cv = new ContentValues();
        cv.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        cv.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis()+"");
        int count = new DatabaseHelper(context).getWritableDatabase().update(Schema.DataTable_ReceiptCountData.TABLE_NAME,cv,strSql,args);
        if(count>0) {
          return Enum.GetLockResult.Granted;
        }
        return Enum.GetLockResult.TryLater;
    }
    private int GenerateNextReceiptNumber(ReceiptCountObj rco) {
        //get lock
        int intNewReceiptNum = -1;
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase db =helper.getWritableDatabase();


        //get lock
        Enum.GetLockResult result = LockReceiptNumberRecord(rco);
        if(result!= Enum.GetLockResult.Granted)
        {
            return intNewReceiptNum;
        }
        try
        {

            db.beginTransaction();

            String strSql = Schema.VERSION_COLUMN+"=? and "+Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN+"=? and "+
            Schema.LOCK_BY_COLUMN+"=?";
            String[] args = new String[]{rco.Version+"",rco.receiptCount+"",common.myAppSettings.DEVICE_UNIQUE_ID};
            ContentValues cv = new ContentValues();
            cv.put(Schema.ID_COLUMN,common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE);
            cv.put(Schema.VERSION_COLUMN,rco.Version+1);
            cv.put(Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN,rco.receiptCount+1);
            cv.put(Schema.LOCK_TIME_STAMP_COLUMN,"");
            cv.put(Schema.LOCK_BY_COLUMN,"");
            int count = db.update(Schema.DataTable_ReceiptCountData.TABLE_NAME,cv,strSql,args);
            if(count>0) {
                db.setTransactionSuccessful();
                intNewReceiptNum = rco.receiptCount+1;
            }
        }
        catch (Exception ex)
        {
            common.Utility.LogActivity(ex.getMessage());

        }
        finally {
            db.endTransaction();
        }
        return intNewReceiptNum;
    }
    private Enum.DBOperationResult InsertNewReceiptNumRow() {
        Enum.DBOperationResult result = Enum.DBOperationResult.Success;
        DatabaseHelper helper = new DatabaseHelper(context);
        ContentValues cv = new ContentValues();
        cv.put(Schema.ID_COLUMN,common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE);
        cv.put(Schema.VERSION_COLUMN,1);
        cv.put(Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN,1);
        try {
            //id is unique constraint column, so 1st insert 1st win
            helper.getWritableDatabase().insertOrThrow(Schema.DataTable_ReceiptCountData.TABLE_NAME, null, cv);
        }
        catch(Exception ex) {
            result = Enum.DBOperationResult.Existed;
            common.Utility.LogActivity(ex.getMessage());
        }

        return result;
    }
    public void AssignReceiptNumber(Receipt receipt)
    {
        //do not assign new receipt number if already has one
        if(receipt.receiptNumber.length()==0) {
            int retryCount=3;
            int nextReceiptNum = -1;
            //try three times to get the new receipt number
            while(retryCount>0) {
                nextReceiptNum = GetNextAvailableReceiptNumber();
                if(nextReceiptNum>-1)break;
                retryCount--;
            }

            //failed to get new receipt number, return immediately
            if(nextReceiptNum==-1)return;

            //proceed to save
            receipt.receiptNumber = ((common.myAppSettings.GetReceiptNumberPrefix().length()>0)?common.myAppSettings.GetReceiptNumberPrefix()+"-":"")
                    +nextReceiptNum;
        }
    }
    public long SaveReceipt(Receipt receipt,ArrayList<Receipt> receipts)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        receipt.Version++;//increase version if any change on the properties
        receipt.flTotal =receipt.myCart.getAmount().floatValue();
        //do not assign new receipt number if already has one
        if(receipt.receiptNumber.length()==0) {
            int retryCount=3;
            int nextReceiptNum = -1;
            //try three times to get the new receipt number
            while(retryCount>0) {
                nextReceiptNum = GetNextAvailableReceiptNumber();
                if(nextReceiptNum>-1)break;
                retryCount--;
            }

            //failed to get new receipt number, return immediately
            if(nextReceiptNum==-1)return nextReceiptNum;

            //proceed to save
            receipt.receiptNumber = ((common.myAppSettings.GetReceiptNumberPrefix().length()>0)?common.myAppSettings.GetReceiptNumberPrefix()+"-":"")
            +nextReceiptNum;
        }

        //default is cash
        String[] columns= new String[]{
                Schema.DataTable_Receipt.DEVICE_NAME_COLUMN,
                Schema.DataTable_Receipt.PAYMENT_TYPE_ID_COLUMN,
                Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN,
                Schema.DataTable_Receipt.CANCEL_FLAG_COLUMN,
                Schema.DataTable_Receipt.GRATUITY_COLUMN,
                Schema.DataTable_Receipt.DISCOUNT_COLUMN,
                Schema.DataTable_Receipt.TAX_COLUMN,
                Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN,
                Schema.DataTable_Receipt.SERVER_ID_COLUMN,
                Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN,
                Schema.DataTable_Receipt.TOTAL_COLUMN,

                Schema.DataTable_Receipt.DINING_TABLE_COLUMN,
                //Schema.DataTable_Receipt.CREDIT_CARD_NUMBER_COLUMN,
                //Schema.DataTable_Receipt.CREDIT_CARD_CVV_COLUMN,
                //Schema.DataTable_Receipt.CREDIT_CARD_EXP_COLUMN,
                Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Receipt.CART_ITEM_COLUMN,
                Schema.DataTable_Receipt.LATITUDE_COLUMN,
                Schema.DataTable_Receipt.LONGITUDE_COLUMN,
                Schema.DataTable_Receipt.CART_GUID_COLUMN,
                Schema.DataTable_Receipt.PROMOTION_ID_COLUMN,
                Schema.DataTable_Receipt.AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN,
                Schema.DataTable_Receipt.LINKED_RECEIPT_COLUMN,
                Schema.VERSION_COLUMN,
                Schema.LOCK_BY_COLUMN
                //Schema.DataTable_Receipt.TRANSACTION_ID_COLUMN
                };
        String[] values = new String[]{
                                        Build.SERIAL,
                                        receipt.paymentType.value+"",
                                        receipt.receiptDateTime.getTime().getTime()+"",
                                        "0",
                                        receipt.GetCashValueForGratuity()+"",
                                        receipt.CashValueForDiscount+"",
                                        receipt.flTaxRate+"",
                                        receipt.myCart.getTaxAmount().floatValue()+"",
                                        //(receipt.server.EmployeeId==-1)?"NAN":receipt.server.EmployeeId+"",
                                        receipt.server.EmployeeId+"",
                                        receipt.receiptNumber,
                                        receipt.flTotal+"",
                                        //receipt.GetAmountAfterAdditionalDiscount().floatValue()+"",
                                        //(receipt.tableNumber.length()>0)?receipt.tableNumber:"NAN",
                                        (receipt.tableNumber.length()>0)?receipt.tableNumber:"",
                                        //receipt.creditCard.Number,
                                        //receipt.creditCard.CVV,
                                        //receipt.creditCard.ExpDate,
                                        "1",
                                        receipt.ConvertCartItemPromotionToSQLData(),//receipt.ConvertCartItemsToSQLData(),
                                        //((Double.isNaN(receipt.dbLatitude))?"NAN":receipt.dbLatitude+""),
                                        ((Double.isNaN(receipt.dbLatitude))?"-1":receipt.dbLatitude+""),
                                        ((Double.isNaN(receipt.dbLongitude))?"-1":receipt.dbLongitude+""),
                                        //((Double.isNaN(receipt.dbLongitude))?"NAN":receipt.dbLongitude+""),
                                        receipt.myCart.GUID,
                                        receipt.myCart.ReturnPromotionToSQLString(),
                                        receipt.GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount().floatValue()+"",
                                        receipt.strLinkedReceipts,
                                        receipt.Version+"",
                                        ""
                //receipt.myCart.
                                        //receipt.transactionId
                                        };



        //modify for credit card
        if(receipt.paymentType== Enum.PaymentType.credit)
        {
            InsertCreditCardInfo(columns,values,receipt);
        }

        long id = helper.SaveReceiptWithTransaction(columns,values,receipt,receipts);//.Insert(Schema.DataTable_Receipt.TABLE_NAME, columns, values);


        return id;
    }
    /**dirty read**/
    public ArrayList<Receipt> GetOrdersFromDBByTable(String strTableId)
    {
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();

        String strWhereClause=Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=?";
        String[] args = {strTableId};
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor c =helper.query(Schema.DataTable_Orders.TABLE_NAME,Schema.DataTable_Orders.GetColumnNames(),strWhereClause,args,"");
        c.moveToFirst();
        int columnContentIndex = c.getColumnIndex(Schema.DataTable_Orders.GSON_CONTENT_COLUMN);
        while(!c.isAfterLast())
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            receipts.add(gson.fromJson(c.getString(columnContentIndex), Receipt.class));
        }

        return receipts;
    }
    public void RefreshReceipts(String strTableId) {
        ArrayList<Receipt> receipts = GetOrdersFromDb(strTableId,false);
        common.myCartManager.Receipts.put(strTableId,receipts);
    }
    public ArrayList<Receipt> GetOrdersFromDb(String strTableId,boolean blnQueryAllTableIds) {
        ArrayList<Receipt> receipts = new ArrayList<Receipt>();

        String strWhereClause=blnQueryAllTableIds?"":Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=?";
        String[] args = {strTableId};
        if(blnQueryAllTableIds)
        {args = null;}
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor c =helper.query(Schema.DataTable_Orders.TABLE_NAME,Schema.DataTable_Orders.GetColumnNames(),strWhereClause,args,"");
        c.moveToFirst();
        int columnContentIndex = c.getColumnIndex(Schema.DataTable_Orders.GSON_CONTENT_COLUMN);
        int columnVersionIndex = c.getColumnIndex(Schema.VERSION_COLUMN);
        while(!c.isAfterLast())
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            receipts.add(gson.fromJson(c.getString(columnContentIndex), Receipt.class));
            //receipts.get(receipts.size()-1).Version = c.getInt(columnVersionIndex);
            c.moveToNext();
        }

        return receipts;
    }
    public ArrayList<Receipt> GetOrdersFromDB()
    {
        return GetOrdersFromDb("",true);

    }
    private int ReceiptRecordAlreadyExisted(Receipt receipt)
    {
        String strWhereClause=Schema.VERSION_COLUMN+"=? and "
                +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=?";
        String[] args = {receipt.Version+"",receipt.tableNumber+"",receipt.myCart.receiptIndex+""};
        DatabaseHelper helper = new DatabaseHelper(context);
        ContentValues contentValues = new ContentValues();
        int rowAffected = helper.GetLock(contentValues,Schema.DataTable_Orders.TABLE_NAME,args,strWhereClause);
        helper.close();

        return rowAffected;
    }
    public void DeleteAllOrders()
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        helper.DeleteDataTable(Schema.DataTable_Orders.TABLE_NAME);
    }
    public int DeleteOrdersByTableId(String strTableId)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        return helper.DeleteOrdersByTableId(strTableId);
    }
    public int DeleteOrder(ArrayList<Receipt> completeReceipts,ArrayList<OrderCompareModel>recordToDelete)
    {
        common.Utility.LogActivity("Delete order");
        //get lock on all receipts on this table
        String strWhereClause="";
        int index=0;
        int count=0;
        String[] args = new String[4*completeReceipts.size()];
        DatabaseHelper helper = new DatabaseHelper(context);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        for(int i=0;i<completeReceipts.size();i++)
        {
            strWhereClause+="("+Schema.VERSION_COLUMN+"=? and "
                    +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                    +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=? and "
                    +Schema.LOCK_BY_COLUMN+"=?"+") or ";

            args[index++]=completeReceipts.get(i).Version+"";
            args[index++]=completeReceipts.get(i).tableNumber+"";
            args[index++]=completeReceipts.get(i).myCart.receiptIndex+"";
            args[index++]="";
        }
        //where clause to get total count for related record
        String strRawSQLCountWhereClause = Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"="+completeReceipts.get(0).myCart.tableId;
        //version check
        String strRawSQLVersionCheckWhereClause = "Select "+Schema.VERSION_COLUMN+","+Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+" from "+
        Schema.DataTable_Orders.TABLE_NAME+" where "+Schema.LOCK_BY_COLUMN+"="+AppSettings.DEVICE_UNIQUE_ID;
        String[] versionCheckParams = {Schema.VERSION_COLUMN,Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN};
        Object[] valuesToCompare = new Object[completeReceipts.size()];
        for(int i=0;i<completeReceipts.size();i++) {
            Receipt r = completeReceipts.get(i);
            valuesToCompare[i] = new OrderCompareModel(r.Version,r.myCart.tableId,r.myCart.receiptIndex,r.lngLastUpdateDate);
        }
        Enum.GetLockResult result =  helper.GetLocksWithTransaction(contentValues,strWhereClause,args,Schema.DataTable_Orders.TABLE_NAME,strRawSQLCountWhereClause,strRawSQLVersionCheckWhereClause,
        versionCheckParams,valuesToCompare);

        if(result== Enum.GetLockResult.Granted) {
            common.Utility.LogActivity("lock granted, begin delete order");
            count =helper.DeleteOrders(recordToDelete);

        }
        else {
            common.Utility.LogActivity("failed to get lock");
        }

        return  count;
    }
    public Enum.GetLockResult GetLocks(String strTableId,ArrayList<Receipt> completeReceipts)
    {

        common.Utility.LogActivity("get locks for order record with table id ["+strTableId+"]");
        //get lock on all receipts on this table
        int fieldCount = 5;
        String strWhereClause="";
        int index=0;
        int count=0;
        String[] args = new String[fieldCount*completeReceipts.size()];
        DatabaseHelper helper = new DatabaseHelper(context);
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        contentValues.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());
        for(int i=0;i<completeReceipts.size();i++)
        {
            strWhereClause+="("+Schema.VERSION_COLUMN+"=? and "
                    +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                    +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=? and "
                    +Schema.DataTable_Orders.DATE_COLUMN+"=? and "
                    +Schema.LOCK_BY_COLUMN+"=?"+") "+((i==completeReceipts.size()-1)?"":" or ");

            args[index++]=completeReceipts.get(i).Version+"";
            args[index++]=completeReceipts.get(i).myCart.tableId+"";
            args[index++]=completeReceipts.get(i).myCart.receiptIndex+"";
            args[index++]=completeReceipts.get(i).lngLastUpdateDate+"";
            args[index++]="";
        }
        //where clause to get total count for related record
        String strRawSQLCountWhereClause = Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"='"+completeReceipts.get(0).myCart.tableId+"'";
        //version check
        String strRawSQLVersionCheckWhereClause = "Select "+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+","+
                Schema.VERSION_COLUMN+","+
                Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+","+
                Schema.DataTable_Orders.DATE_COLUMN+" from "+
                Schema.DataTable_Orders.TABLE_NAME+" where "+Schema.LOCK_BY_COLUMN+"='"+AppSettings.DEVICE_UNIQUE_ID+"'";
        String[] versionCheckParams = {Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN,Schema.VERSION_COLUMN,Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN,Schema.DataTable_Orders.DATE_COLUMN};
        Object[] valuesToCompare = new Object[completeReceipts.size()];
        for(int i=0;i<completeReceipts.size();i++) {
            Receipt r = completeReceipts.get(i);
            valuesToCompare[i] = new OrderCompareModel(r.Version,r.myCart.tableId,r.myCart.receiptIndex,r.lngLastUpdateDate);
        }
        Enum.GetLockResult result =  helper.GetLocksWithTransaction(contentValues,
                strWhereClause,
                args,
                Schema.DataTable_Orders.TABLE_NAME,
                strRawSQLCountWhereClause,
                strRawSQLVersionCheckWhereClause,
                versionCheckParams,
                valuesToCompare);

        return result;
    }
    public Enum.DBOperationResult SaveOrdersIntoDB(ArrayList<Receipt> receipts, boolean blnGetLocks) {

        String strTableId = receipts.get(0).myCart.tableId;
        Enum.DBOperationResult result = Enum.DBOperationResult.TryLater;
        if(blnGetLocks) {
            //get locks on this table id
            Enum.GetLockResult lockResult = GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
            Enum.DBOperationResult dbOperationResult=common.Utility.ConvertGetLockResult2DBOperationResult(lockResult);
            if(dbOperationResult!= Enum.DBOperationResult.Success){
                common.Utility.LogActivity("failed to get lock");
                return dbOperationResult;
            }
        }
        //check is there at least one item in any of the receipt, else called delete all record
        //based on the table id
        boolean blnAtLeastOneItem = false;
        for(int i=0;i<receipts.size();i++)
        {
            if(receipts.get(i).myCart.GetItems().size()>0)
            {
                blnAtLeastOneItem = true;
                break;
            }
        }

        if(!blnAtLeastOneItem)
        {
            //perform delete operation instead of update if no item left
            int rowAffected = new DatabaseHelper(context).DeleteOrdersByTableId(receipts.get(0).myCart.tableId);
            if(rowAffected>0)
            {
                result = Enum.DBOperationResult.Success;
            }
        }
        else
        {
            //perform regular operation to save and update receipt version
            //increase receipt version by one
            for(int i=0;i<receipts.size();i++) {
                if(!receipts.get(i).blnHasPaid)//not touching already paid receipt
                receipts.get(i).Version++;
            }

            ContentValues[] contentValues = new ContentValues[receipts.size()];
            for(int i=0;i<receipts.size();i++) {
                receipts.get(i).lngLastUpdateDate = Calendar.getInstance().getTimeInMillis();//update date property 1st else will mismatched
                //String strContent = common.Utility.ConvertReceiptToJsonString(receipts.get(i));
                String strContent = new JsonClass().ConvertReceiptToJsonString(receipts.get(i));
                ContentValues cv = new ContentValues();
                cv.put(Schema.VERSION_COLUMN,receipts.get(i).Version);
                cv.put(Schema.DataTable_Orders.DATE_COLUMN,receipts.get(i).lngLastUpdateDate);
                cv.put(Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN,receipts.get(i).myCart.tableId);
                cv.put(Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN,receipts.get(i).myCart.receiptIndex);
                cv.put(Schema.DataTable_Orders.GSON_CONTENT_COLUMN,strContent);
                cv.put(Schema.UPDATED_BY_COLUMN,AppSettings.DEVICE_UNIQUE_ID);
                cv.put(Schema.UPDATED_DATE_COLUMN,Calendar.getInstance().getTimeInMillis());
                cv.put(Schema.LOCK_BY_COLUMN,AppSettings.DEVICE_UNIQUE_ID);//release lock at the end of method
                contentValues[i] = cv;
            }
            DatabaseHelper db = new DatabaseHelper(context);
            result = db.SaveOrdersWithTransaction(contentValues,strTableId);

            //decrease the receipt version count by 1 if failed
            if(result!= Enum.DBOperationResult.Success) {
                for(int i=0;i<receipts.size();i++)
                    receipts.get(i).Version--;
            }
        }


        if(blnGetLocks) {
            UnLockRecords(receipts.get(0).myCart.tableId);
        }

        common.Utility.LogActivity("done writing to database");
        //exclude the last receipt if is empty
        return result;
    }

  /*  public  Enum.DBOperationResult SaveOrderIntoDB(Receipt receipt,boolean blnGetLock)
    {
        long rowAffected;
        common.Utility.LogActivity("saving order to data table ["+receipt.myCart.tableId+"]");
        //get locks on this table id
        if(blnGetLock) {
            Enum.GetLockResult result = GetLocks(Schema.DataTable_Orders.TABLE_NAME, common.myCartManager.GetReceipts(receipt.myCart.tableId));
            Enum.DBOperationResult dbOperationResult = ConvertGetLockResult2DBOperationResult(result);
            if (dbOperationResult != Enum.DBOperationResult.Success) {
                return dbOperationResult;
            }
        }

        String strContent= common.Utility.ConvertReceiptToJsonString(receipt);
        DatabaseHelper helper = new DatabaseHelper(context);

        //check whether there is existing record in db
        String strWhereClause=Schema.VERSION_COLUMN+"=? and "
                +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=?";
        String[] args = {receipt.Version+"",receipt.tableNumber+"",receipt.myCart.receiptIndex+""};

        receipt.Version++;
        *//**//**insert new**//**//*
        if(!helper.CheckRecordExistence(Schema.DataTable_Orders.TABLE_NAME,strWhereClause,args))
        {
            common.Utility.LogActivity("no matching record found, inserting new record to data table "+Schema.DataTable_Orders.TABLE_NAME);
            receipt.lngLastUpdateDate = Calendar.getInstance().getTimeInMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Schema.VERSION_COLUMN,receipt.Version);
            contentValues.put(Schema.DataTable_Orders.DATE_COLUMN,receipt.lngLastUpdateDate);
            contentValues.put(Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN,receipt.tableNumber);
            contentValues.put(Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN,receipt.myCart.receiptIndex);
            contentValues.put(Schema.DataTable_Orders.GSON_CONTENT_COLUMN,strContent);
            contentValues.put(Schema.UPDATED_BY_COLUMN,AppSettings.DEVICE_UNIQUE_ID);
            contentValues.put(Schema.LOCK_BY_COLUMN,AppSettings.DEVICE_UNIQUE_ID);//release lock by at the end of method
            contentValues.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());//release lock time at the end of method

            rowAffected = helper.InsertRecord(Schema.DataTable_Orders.TABLE_NAME,contentValues);
            helper.close();
        }
        *//**//**update**//**//*
        else// if( GetLock(receipt)== Enum.GetLockResult.Granted)
        {
            common.Utility.LogActivity("matching record found, updating record to data table "+Schema.DataTable_Orders.TABLE_NAME);
            strWhereClause=Schema.VERSION_COLUMN+"=? and "
                    +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                    +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=? and "
                    +Schema.LOCK_BY_COLUMN+"=?";
            args = new String[]{receipt.Version+"",receipt.tableNumber+"",receipt.myCart.receiptIndex+"",AppSettings.DEVICE_UNIQUE_ID};

            receipt.lngLastUpdateDate = Calendar.getInstance().getTimeInMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(Schema.VERSION_COLUMN,receipt.Version);
            contentValues.put(Schema.DataTable_Orders.DATE_COLUMN,receipt.lngLastUpdateDate);
            contentValues.put(Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN,receipt.tableNumber);
            contentValues.put(Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN,receipt.myCart.receiptIndex);
            contentValues.put(Schema.DataTable_Orders.GSON_CONTENT_COLUMN,strContent);
            contentValues.put(Schema.UPDATED_BY_COLUMN,AppSettings.DEVICE_UNIQUE_ID);
            contentValues.put(Schema.LOCK_BY_COLUMN,AppSettings.DEVICE_UNIQUE_ID);//release lock by at the end of method
            contentValues.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());//release lock time at the end of method


            rowAffected = helper.UpdateRecord(contentValues,Schema.DataTable_Orders.TABLE_NAME,args,strWhereClause);

            helper.close();
        }
        if(rowAffected==0)receipt.Version--;

        if(blnGetLock) {
            UnLockRecords(receipt.myCart.tableId);
        }
        return Enum.DBOperationResult.Success;
        //return rowAffected;
    }*/
    public void UnLockRecords(String strTableId) {
        common.Utility.LogActivity("unlock lock for table id ["+strTableId+"]");
        String strSQL = "update "+Schema.DataTable_Orders.TABLE_NAME+" set "+Schema.LOCK_BY_COLUMN+"='',"+Schema.LOCK_TIME_STAMP_COLUMN+"=0 where "+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"='"+strTableId+"'";
        new DatabaseHelper(context).getWritableDatabase().execSQL(strSQL);
    }
    /*private Enum.GetLockResult GetLock(Receipt receipt)
    {
        String strWhereClause=Schema.VERSION_COLUMN+"=? and "
                +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=? and "
                +Schema.LOCK_BY_COLUMN+"=?";
        String[] args = {receipt.Version+"",receipt.tableNumber+"",receipt.myCart.receiptIndex+"",""};
        ContentValues contentValues = new ContentValues();
        contentValues.put(Schema.LOCK_BY_COLUMN,android.os.Build.ID);
        DatabaseHelper helper = new DatabaseHelper(context);
        int rowAffected = helper.GetLock(contentValues,Schema.DataTable_Orders.TABLE_NAME,args,strWhereClause);
        *//**failed to get lock**//*
        if(rowAffected==0)
        {
            //get the lock detail
            //check lock time
            strWhereClause=Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                    +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=?";
            args = new String[]{receipt.tableNumber+"",receipt.myCart.receiptIndex+""};
            LockDetails ld= helper.GetLockedDetails(Schema.DataTable_Orders.TABLE_NAME,args,strWhereClause);
            //out of date version, tell user to reload receipt
            if(ld.Version!=receipt.Version) {
                helper.close();
                return Enum.GetLockResult.VersionOutOfDate;
            }

            *//**unlock if more than 1 minutes**//*
            if(Calendar.getInstance().getTimeInMillis()-ld.LockedDateTime>common.myAppSettings.LOCK_RECORD_DURATION)
            {
                strWhereClause=Schema.VERSION_COLUMN+"=? and "
                        +Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=? and "
                        +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+"=? and "
                        +Schema.LOCK_BY_COLUMN+"=?";
                args = new String[]{receipt.Version+"",receipt.tableNumber+"",receipt.myCart.receiptIndex+""};
                rowAffected =helper.UnlockExpiredRecordAndGetLock(contentValues,Schema.DataTable_Orders.TABLE_NAME,args,strWhereClause);
                if(rowAffected==0) {
                    helper.close();;
                    return Enum.GetLockResult.TryLater;
                }

            }
            else
            {
                helper.close();
                return Enum.GetLockResult.TryLater;
            }

        }
        return Enum.GetLockResult.Granted;
    }*/
    private void InsertCreditCardInfo(String[] columns,String[] values,Receipt receipt)
    {
        //update payment info
        values[1] = Enum.PaymentType.credit.value+"";

        //insert credit card info
        //transaction id, credit card holder, card #,ccv,exp date
        String[] newColumns = new String[columns.length+5];
        String[] newValues = new String[newColumns.length];

        //copy over the value
        for(int i=0;i<columns.length;i++)
        {
            newColumns[i] = columns[i];
            newValues[i] = newValues[i];
        }
        //insert new credit info
        int index=0;
        //transaction id
        newColumns[columns.length+index] = Schema.DataTable_Receipt.TRANSACTION_ID_COLUMN;
        newValues[columns.length+index++]= receipt.transactionId;
        //credit card ccv
        newColumns[columns.length+index] = Schema.DataTable_Receipt.CREDIT_CARD_CVV_COLUMN;
        newValues[columns.length+index++]= receipt.creditCard.CVV;
        //credit card number
        newColumns[columns.length+index] = Schema.DataTable_Receipt.CREDIT_CARD_NUMBER_COLUMN;
        newValues[columns.length+index++]= receipt.creditCard.Number;
        //credit card exp date
        newColumns[columns.length+index] = Schema.DataTable_Receipt.CREDIT_CARD_EXP_COLUMN;
        newValues[columns.length+index++]= receipt.creditCard.ExpDate;
        //credit card holder
        newColumns[columns.length+index] = Schema.DataTable_Receipt.CREDIT_CARD_HOLDER_COLUMN;
        newValues[columns.length+index++]= receipt.creditCard.CardHolder;
    }
    /*public class JsonClass{
        public String ConvertReceiptToJsonString(Receipt receipt)
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeSpecialFloatingPointValues();
            Gson gson = gsonBuilder.setPrettyPrinting().create();
            return gson.toJson(receipt,Receipt.class);

        }
    }*/
}
