package tme.pos.DataAccessLayer;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.util.Pair;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Duple;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.LockDetails;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.OrderCompareModel;
import tme.pos.BusinessLayer.PromotionObject;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.ReceiptCompareModel;
import tme.pos.BusinessLayer.JsonClass;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 10/15/2014.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION=1;
    private static final String DB_NAME="TME_POS_DB";

    private final Context context;


    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, VERSION);
        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CreateCategoryTable());
        sqLiteDatabase.execSQL(CreateItemTable());
        sqLiteDatabase.execSQL(CreateModifierTable());
        sqLiteDatabase.execSQL(CreatePaymentTable());
        sqLiteDatabase.execSQL(CreateReceiptTable());
        sqLiteDatabase.execSQL(CreateServerTable());
        sqLiteDatabase.execSQL(CreateInventoryTable());
        sqLiteDatabase.execSQL(CreateSupplierTable());
        sqLiteDatabase.execSQL(CreateItemAndModifierUpdateTable());
        sqLiteDatabase.execSQL(CreatePromotionTable());
        sqLiteDatabase.execSQL(CreatePromotionLogTable());
        sqLiteDatabase.execSQL(CreateCustomListTable());
        sqLiteDatabase.execSQL(CreateOrdersTable());
        sqLiteDatabase.execSQL(CreateReceiptCountDataTable());
        sqLiteDatabase.execSQL(CreateFloorPlanDataTable());
        InsertPaymentType(sqLiteDatabase);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(oldVersion==1 && newVersion==2)
        {
            //sqLiteDatabase.execSQL(CreateOrdersTable());
            //AddSchemaSupplierTableNoteColumn(sqLiteDatabase);
        }
    }
    private boolean IsColumnExist(String strColumnName,Cursor c)
    {
        //table structure
        //http://stackoverflow.com/questions/11753871/getting-the-type-of-a-column-in-sqlite
        c.moveToFirst();
        int nameIdx = c.getColumnIndexOrThrow("name");

        while(!c.isAfterLast())
        {
            String strName =c.getString(nameIdx);
            if(strColumnName.equalsIgnoreCase(strName))return true;
            c.moveToNext();
        }
        return false;
    }
    //keeping this method for reference
    private void AddSchemaSupplierTableNoteColumn(SQLiteDatabase db)
    {
        String strCheckColumnSql = "PRAGMA table_info('"+Schema.DataTable_Supplier.TABLE_NAME+"')";
        Cursor c = db.rawQuery(strCheckColumnSql, null);


        if(!IsColumnExist(Schema.DataTable_Supplier.NOTE_COLUMN,c)) {
            String strSQL = "Alter table " + Schema.DataTable_Supplier.TABLE_NAME + " add column " + Schema.DataTable_Supplier.NOTE_COLUMN +
                    " " + Schema.DataTable_Supplier.NOTE_COLUMN_TYPE;
            db.rawQuery(strSQL,null);
        }
    }
    private void AddSchemaItemTableParentColumn(SQLiteDatabase db)
    {
        String strCheckColumnSql = "PRAGMA table_info('"+Schema.DataTable_Item.TABLE_NAME+"')";
        Cursor c = db.rawQuery(strCheckColumnSql, null);


        if(!IsColumnExist(Schema.DataTable_Item.PARENT_ID_COLUMN,c)) {
            String strSQL = "Alter table " + Schema.DataTable_Item.TABLE_NAME + " add column " + Schema.DataTable_Item.PARENT_ID_COLUMN +
                    " " + Schema.DataTable_Item.PARENT_ID_COLUMN_TYPE;
            db.rawQuery(strSQL, null);
        }
    }
    private String CreateFloorPlanDataTable() {
        return "create table "+Schema.DataTable_FloorPlanData.TABLE_NAME+
                "("+Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN+" "+Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN_TYPE+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN+","+
                Schema.ID_COLUMN+" "+Schema.ID_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+");"+
                "Create unique index "+Schema.ID_COLUMN+"_index on "+Schema.DataTable_FloorPlanData.TABLE_NAME+"("+Schema.ID_COLUMN+");";
    }
    private String CreateReceiptCountDataTable() {
    return "create table "+Schema.DataTable_ReceiptCountData.TABLE_NAME+
            "("+Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN+" "+Schema.DataTable_ReceiptCountData.CURRENT_RECEIPT_INDEX_COLUMN_TYPE+
            Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
            Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
            Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN+","+
            Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN+","+
            Schema.ID_COLUMN+" "+Schema.ID_COLUMN_TYPE+","+
            Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+");"+
            "Create unique index "+Schema.ID_COLUMN+"_index on "+Schema.DataTable_ReceiptCountData.TABLE_NAME+"("+Schema.ID_COLUMN+");";
    }
    private String CreateCustomListTable()
    {
        return "create table "+Schema.DataTable_CustomList.TABLE_NAME+
                "("+Schema.DataTable_CustomList.ID_COLUMN+" "+Schema.DataTable_CustomList.ID_COLUMN_TYPE+","+
                Schema.DataTable_CustomList.PAGE_TITLE_COLUMN+" "+Schema.DataTable_CustomList.PAGE_TITLE_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.DataTable_CustomList.PAGE_CONTENT_COLUMN+" "+Schema.DataTable_CustomList.PAGE_CONTENT_COLUMN_TYPE+");";//+
                //"Create unique index "+Schema.DataTable_CustomList.ID_COLUMN+"_index on "+Schema.DataTable_CustomList.TABLE_NAME+"("+Schema.DataTable_CustomList.ID_COLUMN+");";
    }
    private String CreatePromotionLogTable()
    {
        return "create table "+Schema.DataTable_PromotionUpdateLog.TABLE_NAME+

                "("+Schema.DataTable_PromotionUpdateLog.ID_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.ID_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.TITLE_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.TITLE_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.DAY_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.DAY_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.REPEAT_EVERY_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.REPEAT_EVERY_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.RULE_TYPE_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.RULE_TYPE_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.RULE_ITEM_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.RULE_ITEM_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.RULE_ABOVE_AMOUNT_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.RULE_ABOVE_AMOUNT_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.RULE_TO_AMOUNT_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.RULE_TO_AMOUNT_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.RULE_AMOUNT_NO_LIMIT_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.RULE_AMOUNT_NO_LIMIT_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.DISCOUNT_BY_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.DISCOUNT_BY_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.DISCOUNT_VALUE_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.DISCOUNT_VALUE_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.BY_DAY_OF_WEEK_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.BY_DAY_OF_WEEK_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_MONTHS_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_MONTHS_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_DAY_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_DAY_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.EXPIRATION_DATE_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.EXPIRATION_DATE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.DataTable_PromotionUpdateLog.DATE_COLUMN+" "+Schema.DataTable_PromotionUpdateLog.DATE_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Promotion.ID_COLUMN+"_index on "+Schema.DataTable_PromotionUpdateLog.TABLE_NAME+"("+Schema.DataTable_Promotion.ID_COLUMN+");";
    }
    private String CreatePromotionTable()
    {
        return "create table "+Schema.DataTable_Promotion.TABLE_NAME+

                "("+Schema.DataTable_Promotion.ID_COLUMN+" "+Schema.DataTable_Promotion.ID_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.TITLE_COLUMN+" "+Schema.DataTable_Promotion.TITLE_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+" "+Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN+" "+Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.DAY_COLUMN+" "+Schema.DataTable_Promotion.DAY_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.REPEAT_EVERY_COLUMN+" "+Schema.DataTable_Promotion.REPEAT_EVERY_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.RULE_TYPE_COLUMN+" "+Schema.DataTable_Promotion.RULE_TYPE_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.RULE_ITEM_COLUMN+" "+Schema.DataTable_Promotion.RULE_ITEM_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.RULE_ABOVE_AMOUNT_COLUMN+" "+Schema.DataTable_Promotion.RULE_ABOVE_AMOUNT_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.RULE_TO_AMOUNT_COLUMN+" "+Schema.DataTable_Promotion.RULE_TO_AMOUNT_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.RULE_AMOUNT_NO_LIMIT_COLUMN+" "+Schema.DataTable_Promotion.RULE_AMOUNT_NO_LIMIT_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.DISCOUNT_BY_COLUMN+" "+Schema.DataTable_Promotion.DISCOUNT_BY_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Promotion.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.DISCOUNT_VALUE_COLUMN+" "+Schema.DataTable_Promotion.DISCOUNT_VALUE_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.BY_DAY_OF_WEEK_COLUMN+" "+Schema.DataTable_Promotion.BY_DAY_OF_WEEK_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_MONTHS_COLUMN+" "+Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_MONTHS_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_DAY_COLUMN+" "+Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_DAY_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.EXPIRATION_DATE_COLUMN+" "+Schema.DataTable_Promotion.EXPIRATION_DATE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.PROMOTION_CREATED_DATE_COLUMN+" "+Schema.DataTable_Promotion.PROMOTION_CREATED_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Promotion.COLOR_COLUMN+" "+Schema.DataTable_Promotion.COLOR_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Promotion.ID_COLUMN+"_index on "+Schema.DataTable_Promotion.TABLE_NAME+"("+Schema.DataTable_Promotion.ID_COLUMN+");";
    }
    private void InsertPaymentType(SQLiteDatabase db)
    {


        long id =db.insertOrThrow(Schema.DataTable_PaymentType.TABLE_NAME, null, CreatePaymentTypeCV(10,"CASH"));
        id =db.insertOrThrow(Schema.DataTable_PaymentType.TABLE_NAME, null, CreatePaymentTypeCV(11,"VISA"));
        id =db.insertOrThrow(Schema.DataTable_PaymentType.TABLE_NAME, null, CreatePaymentTypeCV(12,"MASTER"));
        id =db.insertOrThrow(Schema.DataTable_PaymentType.TABLE_NAME, null, CreatePaymentTypeCV(13,"PAYPAL"));

    }
    private ContentValues CreatePaymentTypeCV(long Id,String strPaymentType)
    {
        ContentValues cValue = new ContentValues();
        cValue.put(Schema.DataTable_PaymentType.ID_COLUMN,Id);
        cValue.put(Schema.DataTable_PaymentType.PAYMENT_NAME_COLUMN, strPaymentType);

        return cValue;
    }
    private String CreateReceiptTable()
    {
        //create unique index data can be null during insertion while primary key not allowed

        return "create table "+Schema.DataTable_Receipt.TABLE_NAME+
                //"("+Schema.DataTable_Receipt.ID_COLUMN+" "+Schema.DataTable_Receipt.ID_COLUMN_TYPE+","+
                "("+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+" "+Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CART_ITEM_COLUMN+" "+Schema.DataTable_Receipt.CART_ITEM_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.DEVICE_NAME_COLUMN+" "+Schema.DataTable_Receipt.DEVICE_NAME_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CANCEL_DATE_COLUMN+" "+Schema.DataTable_Receipt.CANCEL_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CANCEL_FLAG_COLUMN+" "+Schema.DataTable_Receipt.CANCEL_FLAG_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.TAX_COLUMN+" "+Schema.DataTable_Receipt.TAX_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN+" "+Schema.DataTable_Receipt.TAX_AMOUNT_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.GRATUITY_COLUMN+" "+Schema.DataTable_Receipt.GRATUITY_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.SERVER_ID_COLUMN+" "+Schema.DataTable_Receipt.SERVER_ID_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.DISCOUNT_COLUMN+" "+Schema.DataTable_Receipt.DISCOUNT_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+" "+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN_TYPE+","+
                //Schema.DataTable_Receipt.SUB_RECEIPT_NUMBER_COLUMN+" "+Schema.DataTable_Receipt.SUB_RECEIPT_NUMBER_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.TOTAL_COLUMN+" "+Schema.DataTable_Receipt.TOTAL_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.DINING_TABLE_COLUMN+" "+Schema.DataTable_Receipt.DINING_TABLE_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CREDIT_CARD_NUMBER_COLUMN+" "+Schema.DataTable_Receipt.CREDIT_CARD_NUMBER_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CREDIT_CARD_CVV_COLUMN+" "+Schema.DataTable_Receipt.CREDIT_CARD_CVV_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CREDIT_CARD_EXP_COLUMN+" "+Schema.DataTable_Receipt.CREDIT_CARD_EXP_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CREDIT_CARD_HOLDER_COLUMN+" "+Schema.DataTable_Receipt.CREDIT_CARD_HOLDER_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Receipt.ACTIVE_FLAG_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.TRANSACTION_ID_COLUMN+" "+Schema.DataTable_Receipt.TRANSACTION_ID_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.LATITUDE_COLUMN+" "+Schema.DataTable_Receipt.LATITUDE_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.LONGITUDE_COLUMN+" "+Schema.DataTable_Receipt.LONGITUDE_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.CART_GUID_COLUMN+" "+Schema.DataTable_Receipt.CART_GUID_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.PROMOTION_ID_COLUMN+" "+Schema.DataTable_Receipt.PROMOTION_ID_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.LINKED_RECEIPT_COLUMN+" "+Schema.DataTable_Receipt.LINKED_RECEIPT_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.DataTable_Receipt.AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN+" "+Schema.DataTable_Receipt.AMOUNT_WITH_PROMOTION_AND_ADDITIONAL_DISCOUNT_COLUMN_Type+","+
                Schema.DataTable_Receipt.PAYMENT_TYPE_ID_COLUMN+" "+Schema.DataTable_Receipt.PAYMENT_TYPE_ID_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+"_index on "+Schema.DataTable_Receipt.TABLE_NAME+"("+Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+");";//+
              /*  "Create unique index "+Schema.DataTable_Receipt.ID_COLUMN+"_index on "+Schema.DataTable_Receipt.TABLE_NAME+"("+Schema.DataTable_Receipt.ID_COLUMN+","+
        Schema.DataTable_Receipt.RECEIPT_DATE_COLUMN+","+Schema.DataTable_Receipt.DEVICE_NAME_COLUMN+");";*/
    }
    private String CreateServerTable()
    {
        Log.d("Info","returning create server table string");
        return "create table "+Schema.DataTable_Server.TABLE_NAME+
                "("+Schema.DataTable_Server.ID_COLUMN+" "+Schema.DataTable_Server.ID_COLUMN_TYPE+","+
                Schema.DataTable_Server.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Server.ACTIVE_FLAG_COLUMN_TYPE+","+
                Schema.DataTable_Server.GENDER_FLAG_COLUMN+" "+Schema.DataTable_Server.GENDER_FLAG_COLUMN_TYPE+","+
                Schema.DataTable_Server.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Server.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Server.PHONE_COLUMN+" "+Schema.DataTable_Server.PHONE_COLUMN_TYPE+","+
                Schema.DataTable_Server.ADDRESS_COLUMN+" "+Schema.DataTable_Server.ADDRESS_COLUMN_TYPE+","+
                Schema.DataTable_Server.PICTURE_PATH_COLUMN+" "+Schema.DataTable_Server.PICTURE_PATH_COLUMN_TYPE+","+
                Schema.DataTable_Server.NOTE_COLUMN+" "+Schema.DataTable_Server.NOTE_COLUMN_TYPE+","+
                Schema.DataTable_Server.EMAIL_COLUMN+" "+Schema.DataTable_Server.EMAIL_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.DataTable_Server.SERVER_NAME_COLUMN+" "+Schema.DataTable_Server.SERVER_NAME_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Server.ID_COLUMN+"_index on "+Schema.DataTable_Server.TABLE_NAME+"("+Schema.DataTable_Server.ID_COLUMN+");";
    }
    private String CreatePaymentTable()
    {
        //create unique index data can be null during insertion while primary key not allowed
        Log.d("Info","returning create payment table string");
        return "create table "+Schema.DataTable_PaymentType.TABLE_NAME+
                "("+Schema.DataTable_PaymentType.ID_COLUMN+" "+Schema.DataTable_PaymentType.ID_COLUMN_TYPE+","+
                Schema.DataTable_PaymentType.PAYMENT_NAME_COLUMN+" "+Schema.DataTable_PaymentType.PAYMENT_NAME_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Modifier.ID_COLUMN+"_index on "+Schema.DataTable_Modifier.TABLE_NAME+"("+Schema.DataTable_Modifier.ID_COLUMN+");";
    }
    private String CreateOrdersTable()
    {
        Log.d("Info","returning create Orders table string");
        return "create table "+Schema.DataTable_Orders.TABLE_NAME+
                "("+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+" "+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN_TYPE+","+
                Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+" "+Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Orders.DATE_COLUMN+" "+Schema.DataTable_Orders.DATE_COLUMN_TYPE+","+
                Schema.DataTable_Orders.GSON_CONTENT_COLUMN+" "+Schema.DataTable_Orders.GSON_CONTENT_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+");"+
                "Create unique index Id_index on "+Schema.DataTable_Orders.TABLE_NAME+"("+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+","
                +Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+","+Schema.VERSION_COLUMN+");";
    }
    private String CreateModifierTable()
    {
        Log.d("Info","returning create item table string");
        return "create table "+Schema.DataTable_Modifier.TABLE_NAME+
                "("+Schema.DataTable_Modifier.ID_COLUMN+" "+Schema.DataTable_Modifier.ID_COLUMN_TYPE+","+
                Schema.DataTable_Modifier.ITEM_NAME_COLUMN+" "+Schema.DataTable_Modifier.ITEM_NAME_COLUMN_TYPE+","+
                Schema.DataTable_Modifier.PARENT_ID_COLUMN+" "+Schema.DataTable_Modifier.PARENT_ID_COLUMN_TYPE+","+
                Schema.DataTable_Modifier.ITEM_PRICE_COLUMN+" "+Schema.DataTable_Modifier.ITEM_PRICE_COLUMN_TYPE+","+
                Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN+" "+Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN_TYPE+","+
                Schema.DataTable_Modifier.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Modifier.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Modifier.ID_COLUMN+"_index on "+Schema.DataTable_Modifier.TABLE_NAME+"("+Schema.DataTable_Modifier.ID_COLUMN+");";
    }
    private String CreateItemTable()
    {
        Log.d("Info","returning create item table string");
        return "create table "+Schema.DataTable_Item.TABLE_NAME+"("+Schema.DataTable_Item.ID_COLUMN+" "+Schema.DataTable_Item.ID_COLUMN_TYPE+","+
                Schema.DataTable_Item.ITEM_NAME_COLUMN+" "+Schema.DataTable_Item.ITEM_NAME_COLUMN_TYPE+","+
                Schema.DataTable_Item.PARENT_ID_COLUMN+" "+Schema.DataTable_Item.PARENT_ID_COLUMN_TYPE+","+
                Schema.DataTable_Item.ITEM_PRICE_COLUMN+" "+Schema.DataTable_Item.ITEM_PRICE_COLUMN_TYPE+","+
                Schema.DataTable_Item.PICTURE_PATH_COLUMN+" "+Schema.DataTable_Item.PICTURE_PATH_COLUMN_TYPE+","+
                Schema.DataTable_Item.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Item.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Item.DO_NOT_TRACK_COLUMN+" "+Schema.DataTable_Item.DO_NOT_TRACK_COLUMN_TYPE+","+
                Schema.DataTable_Item.BARCODE_COLUMN+" "+Schema.DataTable_Item.BARCODE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.DataTable_Item.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Item.ACTIVE_FLAG_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Item.ID_COLUMN+"_index on "+Schema.DataTable_Item.TABLE_NAME+"("+Schema.DataTable_Item.ID_COLUMN+");";
    }
    private String CreateItemAndModifierUpdateTable()
    {
        Log.d("Info","returning create item table string");
        return "create table "+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+"("+Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN+" "+Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN_TYPE+","+
                Schema.DataTable_ItemAndModifierUpdateLog.DATE_COLUMN+" "+Schema.DataTable_ItemAndModifierUpdateLog.DATE_COLUMN_TYPE+","+
                Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN+" "+Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN+" "+Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN_TYPE+ ");";

    }
    private String CreateSupplierTable()
    {

        return "create table "+Schema.DataTable_Supplier.TABLE_NAME+"("+Schema.DataTable_Supplier.ID_COLUMN+" "+Schema.DataTable_Supplier.ID_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.NAME_COLUMN+" "+Schema.DataTable_Supplier.NAME_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.ADDRESS_COLUMN+" "+Schema.DataTable_Supplier.ADDRESS_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.PHONE_COLUMN+" "+Schema.DataTable_Supplier.PHONE_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.EMAIL_COLUMN+" "+Schema.DataTable_Supplier.EMAIL_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Supplier.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.NOTE_COLUMN+" "+Schema.DataTable_Supplier.NOTE_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Supplier.ID_COLUMN+"_index on "+Schema.DataTable_Supplier.TABLE_NAME+"("+Schema.DataTable_Supplier.ID_COLUMN+");";
    }
    private String CreateInventoryTable()
    {
        Log.d("Info","returning create inventory table string");
        return "create table "+Schema.DataTable_Inventory.TABLE_NAME+"("+Schema.DataTable_Inventory.ID_COLUMN+" "+Schema.DataTable_Inventory.ID_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.SUPPLIER_COLUMN+" "+Schema.DataTable_Inventory.SUPPLIER_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.ITEM_COLUMN+" "+Schema.DataTable_Inventory.ITEM_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.RECORD_DATE_COLUMN+" "+Schema.DataTable_Inventory.RECORD_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.UNIT_COLUMN+" "+Schema.DataTable_Inventory.UNIT_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Inventory.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.DataTable_Inventory.COST_PRICE_COLUMN+" "+Schema.DataTable_Inventory.COST_PRICE_COLUMN_TYPE+");"+
                "Create unique index "+Schema.DataTable_Inventory.ID_COLUMN+"_index on "+Schema.DataTable_Inventory.TABLE_NAME+"("+Schema.DataTable_Inventory.ID_COLUMN+");";
    }
    private String CreateCategoryTable()
    {
        Log.d("Info","returning create category table string");
        return "create table "+Schema.DataTable_Category.TABLE_NAME+"("+Schema.DataTable_Category.ID_COLUMN+" "+Schema.DataTable_Category.ID_COLUMN_TYPE+","+
                Schema.DataTable_Category.CATEGORY_NAME_COLUMN+" "+Schema.DataTable_Category.CATEGORY_NAME_COLUMN_TYPE+","+
                Schema.DataTable_Category.INACTIVE_DATE_COLUMN+" "+Schema.DataTable_Category.INACTIVE_DATE_COLUMN_TYPE+","+
                Schema.UPDATED_BY_COLUMN+" "+Schema.UPDATED_BY_COLUMN_TYPE+","+
                Schema.UPDATED_DATE_COLUMN+" "+Schema.UPDATED_DATE_COLUMN_TYPE+","+
                Schema.LOCK_BY_COLUMN+" "+Schema.LOCK_BY_COLUMN_TYPE+","+
                Schema.LOCK_TIME_STAMP_COLUMN+" "+Schema.LOCK_TIME_STAMP_COLUMN_TYPE+","+
                Schema.VERSION_COLUMN+" "+Schema.VERSION_COLUMN_TYPE+","+
                Schema.DataTable_Category.ACTIVE_FLAG_COLUMN+" "+Schema.DataTable_Category.ACTIVE_FLAG_COLUMN_TYPE+");"+
        "Create unique index "+Schema.DataTable_Category.ID_COLUMN+"_index on "+Schema.DataTable_Category.TABLE_NAME+"("+Schema.DataTable_Category.ID_COLUMN+");";
    }
   /* public int UpdateCustomList(int index,String strTitle,ArrayList<Long>list)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean blnUpdateFlag = false;
        //check if any existing record
        String strSql="select count(*) from "+Schema.DataTable_CustomList.TABLE_NAME+" where "+Schema.DataTable_CustomList.ID_COLUMN+"="+index;
        Cursor c=db.query(Schema.DataTable_CustomList.TABLE_NAME,new String[]{Schema.DataTable_CustomList.ID_COLUMN},Schema.DataTable_CustomList.ID_COLUMN+"=?"
        ,new String[]{index+""},"","","");

        c.moveToFirst();
        if(!c.isAfterLast())
        {
            blnUpdateFlag=true;
        }
        c.close();

        String strWhereClause;

        String strItems ="";
        for(int i=0;i<list.size();i++)
        {
            strItems+=list.get(i).toString()+",";
        }
        strItems=(strItems.length()>0)?strItems.substring(0,strItems.length()-1):strItems;
        strWhereClause = Schema.DataTable_CustomList.ID_COLUMN+"=?";
        String[] args = new String[]{index+""};

        ContentValues cValue = new ContentValues();
        cValue.put(Schema.DataTable_CustomList.PAGE_TITLE_COLUMN, strTitle);
        cValue.put(Schema.DataTable_CustomList.PAGE_CONTENT_COLUMN, strItems);
        return db.update(Schema.DataTable_CustomList.TABLE_NAME, cValue, strWhereClause, args);
    }*/
    public int DeleteCategory(long CategoryId,ArrayList<Long>ItemIds)
    {
        long lngtime = System.currentTimeMillis();
        int rowAffected=0;
        int itemRowAffected=0;
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Long> results = new ArrayList<Long>();
        String strWhereClause="";
        String[] args=null;
        ContentValues cValue=null;

        try
        {
            db.beginTransaction();

            //delete category
            strWhereClause = Schema.DataTable_Category.ID_COLUMN+"=?";
            args = new String[]{CategoryId+""};

            cValue = new ContentValues();
            cValue.put(Schema.DataTable_Category.ACTIVE_FLAG_COLUMN, 0);
            cValue.put(Schema.DataTable_Category.INACTIVE_DATE_COLUMN, lngtime);

            rowAffected = db.update(Schema.DataTable_Category.TABLE_NAME, cValue, strWhereClause, args);

            //then delete child item if any
            if(rowAffected>0)
            {
                //update active only, need to keep track of inactive timestamp
                strWhereClause = Schema.DataTable_Item.PARENT_ID_COLUMN+"=? and "+Schema.DataTable_Item.ACTIVE_FLAG_COLUMN+"=1";
                args = new String[]{CategoryId+""};

                cValue = new ContentValues();
                cValue.put(Schema.DataTable_Item.ACTIVE_FLAG_COLUMN, 0);
                cValue.put(Schema.DataTable_Item.INACTIVE_DATE_COLUMN, lngtime);

                itemRowAffected = db.update(Schema.DataTable_Item.TABLE_NAME, cValue, strWhereClause, args);

                if(itemRowAffected>0)
                {
                    //delete item child if any
                    for(long id:ItemIds) {
                        //update active only, need to keep track of inactive timestamp
                        strWhereClause = Schema.DataTable_Modifier.PARENT_ID_COLUMN + "=? and "+Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN+"=1";
                        args = new String[]{id + ""};

                        cValue = new ContentValues();
                        cValue.put(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN, 0);
                        cValue.put(Schema.DataTable_Modifier.INACTIVE_DATE_COLUMN, lngtime);

                        itemRowAffected = db.update(Schema.DataTable_Modifier.TABLE_NAME, cValue, strWhereClause, args);
                    }
                }
            }

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            return rowAffected;
        }

    }
    public int DeleteSupplier(ContentValues cValue,String strWhereClause,String[] args)
    {
        int result=0;

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {

            result = db.update(Schema.DataTable_Supplier.TABLE_NAME, cValue, strWhereClause, args);

            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            return result;
        }
    }
    public int DeleteServer(ContentValues cValue,String strWhereClause,String[] args)
    {
        int result=0;
        //long lngtime = System.currentTimeMillis();
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
/*
            String strWhereClause = Schema.DataTable_Server.ID_COLUMN + "=? and "+Schema.DataTable_Server.ACTIVE_FLAG_COLUMN+"=1";
            String[] args = new String[]{ServerId + ""};
            ContentValues cValue = new ContentValues();
            cValue.put(Schema.DataTable_Server.ACTIVE_FLAG_COLUMN, 0);
            cValue.put(Schema.DataTable_Server.INACTIVE_DATE_COLUMN, lngtime);
*/
            result = db.update(Schema.DataTable_Server.TABLE_NAME, cValue, strWhereClause, args);




            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            return result;
        }
    }

    public int DeleteItem(long ItemId)
    {
        int result=0,modifierResult=0;
        long lngtime = System.currentTimeMillis();
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            //item
            String strWhereClause = Schema.DataTable_Item.ID_COLUMN + "=? and "+Schema.DataTable_Item.ACTIVE_FLAG_COLUMN+"=1";
            String[] args = new String[]{ItemId + ""};
            ContentValues cValue = new ContentValues();
            cValue.put(Schema.DataTable_Item.ACTIVE_FLAG_COLUMN, 0);
            cValue.put(Schema.DataTable_Item.INACTIVE_DATE_COLUMN, lngtime);

            result = db.update(Schema.DataTable_Item.TABLE_NAME, cValue, strWhereClause, args);

            //modifier
            strWhereClause = Schema.DataTable_Modifier.PARENT_ID_COLUMN + "=? and " + Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN + "=?";
            args = new String[]{ItemId + "", "1"};
            cValue = new ContentValues();
            cValue.put(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN, 0);
            cValue.put(Schema.DataTable_Modifier.INACTIVE_DATE_COLUMN, lngtime);
            modifierResult = db.update(Schema.DataTable_Modifier.TABLE_NAME, cValue, strWhereClause, args);


            db.setTransactionSuccessful();
         }
         finally {
            db.endTransaction();
            return result;
         }

    }
    public int DeleteInventory(long InventoryId)
    {
        ContentValues cValue = new ContentValues();
        cValue.put(Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN, 0);
        cValue.put(Schema.DataTable_Inventory.INACTIVE_DATE_COLUMN, System.currentTimeMillis());
        String strWhereClause = Schema.DataTable_Inventory.ID_COLUMN + "=? and "+Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN+"=1";
        String[] args = new String[]{InventoryId + ""};
        return this.getWritableDatabase().update(Schema.DataTable_Inventory.TABLE_NAME, cValue, strWhereClause, args);
    }
    public int UpdateInventoryItemId(long oldId,long newId,SQLiteDatabase db)
    {
        String strWhereClause=Schema.DataTable_Inventory.ITEM_COLUMN+"=? and "+Schema.DataTable_Inventory.ACTIVE_FLAG_COLUMN+"=?";
        String[] args=new String[]{oldId+"","1"};
        ContentValues cValue = new ContentValues();
        cValue.put(Schema.DataTable_Inventory.ITEM_COLUMN, newId);

        int result = db.update(Schema.DataTable_Inventory.TABLE_NAME, cValue, strWhereClause, args);

        return result;
    }
    public LockDetails GetLockedDetails(String strTableName,String[] args,String strWhereClause)
    {
        LockDetails ld = new LockDetails();
        //every table are having the same column names
        String[] columns={Schema.LOCK_BY_COLUMN,Schema.LOCK_TIME_STAMP_COLUMN,Schema.VERSION_COLUMN};
        Cursor c = query(strTableName, columns, strWhereClause, args, "");
        if(c==null)return ld;

        c.moveToFirst();
        int columnDeviceIdIndex = c.getColumnIndex(Schema.LOCK_BY_COLUMN);
        int columnDateTimeIndex = c.getColumnIndex(Schema.LOCK_TIME_STAMP_COLUMN);
        int columnVersionIndex = c.getColumnIndex(Schema.VERSION_COLUMN);
        while(!c.isAfterLast())
        {
            ld.DeviceId=c.getString(columnDeviceIdIndex);
            ld.LockedDateTime=c.getLong(columnDateTimeIndex);
            ld.Version = c.getInt(columnVersionIndex);


            c.moveToNext();
        }
        c.close();
        close();
        return ld;
    }
   /* public int UnlockExpiredRecordAndGetLock(ContentValues cValues,String strTableName,String[] args,String strWhereClause)
    {
        //add current timestamp to update
        cValues.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());
        return getWritableDatabase().update(strTableName,cValues,strWhereClause,args);
    }*/
    public boolean CheckRecordExistence(String strDataTableName,String strWhereClause,String[] args)
    {
        common.Utility.LogActivity("Check record exist in data table "+strDataTableName);
        boolean blnIsExist = false;
        Cursor c = getReadableDatabase().rawQuery("select * from "+ strDataTableName+ " where "+strWhereClause,args);
        /*int columnVersion = c.getColumnIndex(Schema.VERSION_COLUMN);
        int columnReceiptIndex = c.getColumnIndex(Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN);
        int columnTableId = c.getColumnIndex(Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN);*/
        c.moveToFirst();
        while(!c.isAfterLast())
        {
            /*String strV = c.getString(columnVersion);
            String strR = c.getString(columnReceiptIndex);
            String strT = c.getString(columnTableId);*/
            blnIsExist=true;
            break;
        }
        c.close();
        close();
        return blnIsExist;
    }
    public long InsertRecord(String strTableName,ContentValues contentValues)
    {
        return getWritableDatabase().insert(strTableName,null,contentValues);
    }
    public int UpdateRecord(ContentValues cValues,String strTableName,String[] args,String strWhereClause)
    {
        //add current timestamp to update
        cValues.put(Schema.LOCK_TIME_STAMP_COLUMN,Calendar.getInstance().getTimeInMillis());
        return getWritableDatabase().update(strTableName,cValues,strWhereClause,args);
    }
    public long SaveReceiptWithTransaction(String[] columns, String[] values,Receipt receipt,
                                           ArrayList<Receipt>receipts) {
        long id=-1;
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues cValues = new ContentValues();
            for(int i=0;i<columns.length;i++) {
                if(!values[i].equalsIgnoreCase("nan")) {

                    cValues.put(columns[i], values[i]);
                }
            }


            id= db.insertOrThrow(Schema.DataTable_Receipt.TABLE_NAME, null, cValues);
            if(id<=0)
            {
                throw new Exception("failed to insert receipt into table");
            }

            //check is there any unpaid receipt still
            receipt.blnHasPaid = true;
            receipt.myCart.blnIsLock = true;

            boolean blnNotPaidYet = false;
            for(Receipt r:receipts)
            {
                if(!r.blnHasPaid)
                {
                    blnNotPaidYet = true;
                    break;
                }
            }

            if(!blnNotPaidYet)
            {
                //insert linked receipt indexes
                String strLinkedReceipts="";
                for(Receipt r:receipts) {
                    strLinkedReceipts+=","+r.receiptNumber;
                }
                strLinkedReceipts=(strLinkedReceipts.length()>0)?strLinkedReceipts.substring(1,strLinkedReceipts.length()):strLinkedReceipts;

                String strArg="'"+strLinkedReceipts.replace(",","','")+"'";
                ContentValues cv = new ContentValues();

                cv.put(Schema.DataTable_Receipt.LINKED_RECEIPT_COLUMN,strLinkedReceipts);
                int rowAffected=db.update(Schema.DataTable_Receipt.TABLE_NAME,cv,Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+" in ("+strArg+")",new String[]{});
            }

            db.setTransactionSuccessful();

        }
            catch(Exception ex) {
                common.Utility.LogActivity("save receipt with transaction error-"+ex.getMessage());
                id=-1;
            }
            finally {
                db.endTransaction();
                return id;
            }
    }
    public Enum.DBOperationResult DeleteReceiptFromReceiptTableWithTransactions(ContentValues contentValues_Orders,
                                                                                String strWhereClause_Orders,
                                                                                String[] args_Orders,
                                                                                String strRawSQLCountWhereClause_Orders,
                                                                                String strRawSQLVersionCheckWhereClause_Orders,
                                                                                String[] versionCheckParams_Orders,
                                                                                Object[] valuesToCompare_Orders,
                                                                                ContentValues contentValues_Receipt,
                                                                                String strWhereClause_Receipt,
                                                                                String[] args_Receipt,
                                                                                String strRawSQLCountWhereClause_Receipt,
                                                                                String strRawSQLVersionCheckWhereClause_Receipt,
                                                                                String[] versionCheckParams_Receipt,
                                                                                Object[] valuesToCompare_Receipt,
                                                                                String strReceiptNumbers,
                                                                                ArrayList<Receipt> receipts,
                                                                                boolean blnDeleteAll)
    {
        Enum.DBOperationResult result = Enum.DBOperationResult.Success;
        SQLiteDatabase db = getWritableDatabase();
        try {

            db.beginTransaction();
            Enum.GetLockResult lockResult = GetLocksWithOwnDB(contentValues_Orders,strWhereClause_Orders,args_Orders,Schema.DataTable_Orders.TABLE_NAME,strRawSQLCountWhereClause_Orders,
            strRawSQLVersionCheckWhereClause_Orders,versionCheckParams_Orders,valuesToCompare_Orders,db, Enum.CompareObjectVersionType.Order);
            if(lockResult!= Enum.GetLockResult.Granted) {
                result = Enum.DBOperationResult.TryLater;
            }
            else {
                lockResult = GetLocksWithOwnDB(contentValues_Receipt,strWhereClause_Receipt,args_Receipt,Schema.DataTable_Receipt.TABLE_NAME,strRawSQLCountWhereClause_Receipt,
                        strRawSQLVersionCheckWhereClause_Receipt,versionCheckParams_Receipt,valuesToCompare_Receipt,db, Enum.CompareObjectVersionType.Receipt);

                if(lockResult!= Enum.GetLockResult.Granted) {
                    result = Enum.DBOperationResult.TryLater;
                }
                else
                {

                    //start to delete the entry from receipt table
                    result = DeleteReceiptRecordFromDataTable(strReceiptNumbers,db);

                    if(result== Enum.DBOperationResult.Success)
                    {
                        boolean blnHasPaidReceipt = false;
                        for (int i = 0; i < receipts.size(); i++)
                        {
                            if(blnDeleteAll || (receipts.get(i).receiptNumber.compareTo(strReceiptNumbers)==0)) {
                                receipts.get(i).blnHasPaid = false;
                                receipts.get(i).myCart.blnIsLock = false;
                                receipts.get(i).Version++;
                            }
                            blnHasPaidReceipt = receipts.get(i).blnHasPaid?true:blnHasPaidReceipt;


                        }
                        if(!blnHasPaidReceipt) {
                            for (int i = 0; i < receipts.size(); i++) {
                                receipts.get(i).myCart.blnIsLock = false;
                            }
                        }

                        //update order table
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

                        //result =SaveOrdersWithTransaction(contentValues,Schema.DataTable_Orders.TABLE_NAME);
                        result =SaveOrdersWithTransaction(contentValues,receipts.get(0).myCart.tableId);

                        if(result== Enum.DBOperationResult.Success)
                        {
                            db.setTransactionSuccessful();
                        }
                    }

                }
            }
        }
        catch(Exception ex) {
            common.Utility.LogActivity(ex.getMessage());
            result= Enum.DBOperationResult.Failed;
        }
        finally {
            db.endTransaction();
        }

        return  result;
    }
    private Enum.DBOperationResult DeleteReceiptRecordFromDataTable(String strReceiptNumber,SQLiteDatabase db)
    {

        int row = db.delete(Schema.DataTable_Receipt.TABLE_NAME,Schema.DataTable_Receipt.RECEIPT_NUMBER_COLUMN+" in ("+strReceiptNumber+")",
                new String[]{});

        if(row>0)
        {
            return Enum.DBOperationResult.Success;
        }
        else
        {
            return Enum.DBOperationResult.Failed;
        }
    }
    /*private Enum.DBOperationResult SaveOrdersWithTransactionOwnDB(String strTableId,SQLiteDatabase db)
    {

        int row = db.delete(Schema.DataTable_Orders.TABLE_NAME,Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"='"+strTableId+"'",
                new String[]{});

        if(row>0)
        {
            return Enum.DBOperationResult.Success;
        }
        else
        {
            return Enum.DBOperationResult.Failed;
        }
    }*/
    public Enum.DBOperationResult SaveFloorPlan(String serialized)
    {
        long result = -1;
        ContentValues cv = new ContentValues();
        //cv.put(Schema.LOCK_BY_COLUMN,
        //GetLock(
        SQLiteDatabase db = getWritableDatabase();
        Cursor c= db.rawQuery("select count(*) from "+Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN,null);
        boolean blnInsert =(!c.isAfterLast())?true:false;
        c.close();
        //ContentValues cv = new ContentValues();
        cv.put(Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN,serialized);
        if(blnInsert)
        {
            result = db.insertOrThrow(Schema.DataTable_FloorPlanData.TABLE_NAME,null,cv);
        }
        else
        {
            result = db.update(Schema.DataTable_FloorPlanData.TABLE_NAME,cv,"",null);
        }
        return Enum.DBOperationResult.Failed;
    }
    public Enum.DBOperationResult SaveOrdersWithTransaction(ContentValues[] contentValues,String strTableId) {
        SQLiteDatabase db = getWritableDatabase();
        Enum.DBOperationResult result = Enum.DBOperationResult.Success;
        db.beginTransaction();
        try {
            //delete all existing record
            common.Utility.LogActivity("delete all existing records before inserting new.");
            /*String strSqlDelete = "delete from "+Schema.DataTable_Orders.TABLE_NAME+" where "+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"='"+strTableId+"'";

            db.execSQL(strSqlDelete);*/
            int row = db.delete(Schema.DataTable_Orders.TABLE_NAME,Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"='"+strTableId+"'",
                    new String[]{});
            //now reinsert all
            for(ContentValues cv:contentValues)
                db.insertOrThrow(Schema.DataTable_Orders.TABLE_NAME,null,cv);

            db.setTransactionSuccessful();
        }
        catch(Exception ex) {
            common.Utility.LogActivity("save orders with transaction error-"+ex.getMessage());
            result = Enum.DBOperationResult.Failed;
        }
        finally {
            db.endTransaction();
            return result;
        }

    }
    public Enum.GetLockResult GetLocksWithTransaction(ContentValues lockContentValues,String strWhereClause,
                                                      String[] args,String strTableName,String strRawSQLRecordCountWhereClause,
                                                      String strRawSQLVersionCheckWhereClause,
                                                      String[] argsVersionCompareNames,
                                                      Object[] valuesToCompare)
    {
        //unlock expired locks before begin
        UnlockExpiredRecord(strTableName);

        Enum.GetLockResult result = Enum.GetLockResult.TryLater;
        int count=0;
        boolean blnFound=false;
        SQLiteDatabase db = getWritableDatabase();
        try
        {
            db.beginTransaction();
            //get total record count to lock before that
            //String strSQL = "select * from "+strTableName+" where "+strRawSQLRecordCountWhereClause ;
            String strSQL = "select count(*) from "+strTableName+" where "+strRawSQLRecordCountWhereClause ;
            Cursor c = getReadableDatabase().rawQuery(strSQL,null);
            int intVersion = c.getColumnIndex(Schema.VERSION_COLUMN);
            int intLockBy = c.getColumnIndex(Schema.LOCK_BY_COLUMN);
            int intCustomerTableId = c.getColumnIndex(Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN);
            int intReceiptIndex = c.getColumnIndex(Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN);
            int intDateUpdatedId = c.getColumnIndex(Schema.DataTable_Orders.DATE_COLUMN);
            c.moveToFirst();
           /* while(!c.isAfterLast()) {
                String strTemp = c.getString(intVersion)+","+c.getString(intLockBy)+","+c.getString(intCustomerTableId)+","+c.getString(intReceiptIndex)+
                        ","+c.getLong(intDateUpdatedId);
                c.moveToNext();
                count++;
            }*/
            if(!c.isAfterLast())
            {
                count = c.getInt(0);
            }
            c.close();

            //now update all the record and make sure the updated record count is a match with previous count
            //int tempCount = db.update(strTableName,lockContentValues,strWhereClause,args);
            if(count==0) {
                result = Enum.GetLockResult.NoExistingRecord;
            }
            else {
            int countUpdate = db.update(strTableName, lockContentValues, strWhereClause, args);
                //if (count == db.update(strTableName, lockContentValues, strWhereClause, args))
                if(count==countUpdate)
                {
                    //now perform record version checking
                    c = getReadableDatabase().rawQuery(strRawSQLVersionCheckWhereClause, null);
                    int[] indexes = new int[argsVersionCompareNames.length];
                    for (int i = 0; i < argsVersionCompareNames.length; i++) {
                        indexes[i] = c.getColumnIndex(argsVersionCompareNames[i]);
                    }
                    c.moveToFirst();
                    while (!c.isAfterLast()) {

                        blnFound = RecordVersionTheSame(c, valuesToCompare, indexes, Enum.CompareObjectVersionType.Order);
                        if (!blnFound) break;
                        c.moveToNext();
                    }
                    if (blnFound) {
                        result = Enum.GetLockResult.Granted;
                        db.setTransactionSuccessful();
                    } else {
                        result = Enum.GetLockResult.VersionOutOfDate;
                    }


                } else {
                    result = Enum.GetLockResult.RecordCountMismatch;
                }
            }


        }
        catch(Exception ex)
        {
            //String strError = ex.getMessage();
            common.Utility.LogActivity(ex.getMessage());
        }
        finally {

            db.endTransaction();
            return result;
        }


    }
    public Enum.GetLockResult GetLocksWithOwnDB(ContentValues lockContentValues,String strWhereClause,
                                                      String[] args,String strTableName,String strRawSQLRecordCountWhereClause,
                                                      String strRawSQLVersionCheckWhereClause,
                                                      String[] argsVersionCompareNames,
                                                      Object[] valuesToCompare,
                                                      SQLiteDatabase db,
                                                      Enum.CompareObjectVersionType compareType)
    {
        //unlock expired locks before begin
        UnlockExpiredRecordWithOwnDb(strTableName,db);

        Enum.GetLockResult result = Enum.GetLockResult.TryLater;
        int count=0;
        boolean blnFound=false;

        try
        {

            //get total record count to lock before that

            String strSQL = "select count(*) from "+strTableName+" where "+strRawSQLRecordCountWhereClause ;

            //Cursor c = getReadableDatabase().rawQuery(strSQL,null);
            Cursor c = db.rawQuery(strSQL,null);

            c.moveToFirst();
            /*******/
           /* Cursor c=null;
            if(compareType== Enum.CompareObjectVersionType.Receipt)
            {
                String strSQL = "select * from "+strTableName+" where "+strRawSQLRecordCountWhereClause ;
                c = db.rawQuery(strSQL,null);
                c.moveToFirst();
                int int1 = c.getColumnIndex(Schema.VERSION_COLUMN);
                if(!c.isAfterLast())
                {
                    int version = c.getInt(int1);
                    count = c.getInt(0);
                }
                c.close();
            }
            else
            {
                String strSQL = "select count(*) from "+strTableName+" where "+strRawSQLRecordCountWhereClause ;
                c = db.rawQuery(strSQL,null);
                c.moveToFirst();
                if(!c.isAfterLast())
                {
                    count = c.getInt(0);
                }
                c.close();
            }*/
            /******/
            if(!c.isAfterLast())
            {
                count = c.getInt(0);
            }
            c.close();

            //now update all the record and make sure the updated record count is a match with previous count
            //int tempCount = db.update(strTableName,lockContentValues,strWhereClause,args);
            if(count==0) {
                result = Enum.GetLockResult.NoExistingRecord;
            }
            else {
                int countUpdate = db.update(strTableName, lockContentValues, strWhereClause, args);

                if(count==countUpdate)
                {
                    //now perform record version checking
                    //c = getReadableDatabase().rawQuery(strRawSQLVersionCheckWhereClause, null);
                    c = db.rawQuery(strRawSQLVersionCheckWhereClause, null);
                    int[] indexes = new int[argsVersionCompareNames.length];
                    for (int i = 0; i < argsVersionCompareNames.length; i++) {
                        indexes[i] = c.getColumnIndex(argsVersionCompareNames[i]);
                    }
                    c.moveToFirst();
                    while (!c.isAfterLast()) {

                        blnFound = RecordVersionTheSame(c, valuesToCompare, indexes, compareType);
                        if (!blnFound) break;
                        c.moveToNext();
                    }
                    if (blnFound) {
                        result = Enum.GetLockResult.Granted;

                    } else {
                        result = Enum.GetLockResult.VersionOutOfDate;
                    }


                } else {
                    result = Enum.GetLockResult.RecordCountMismatch;
                }
            }


        }
        catch(Exception ex)
        {

            common.Utility.LogActivity(ex.getMessage());
        }
        finally {


            return result;
        }


    }
    private void UnlockExpiredRecordWithOwnDb(String strDbTable,SQLiteDatabase db)
    {
        String strNowTimeInMilli = (Calendar.getInstance().getTimeInMillis()-AppSettings.LOCK_RECORD_DURATION)+"";
        /*Cursor c = getReadableDatabase().rawQuery("select "+Schema.LOCK_TIME_STAMP_COLUMN+","+Schema.LOCK_BY_COLUMN+" from "+Schema.DataTable_Orders.TABLE_NAME,null);
        c.moveToFirst();
        long lockedTime =c.getLong(0);
        String strName = c.getString(1);
        c.close();
        String strSQL = "update "+strDbTable+" set "+Schema.LOCK_BY_COLUMN+"='' where ("+
                Schema.LOCK_TIME_STAMP_COLUMN+")<="+strNowTimeInMilli;*/
        ContentValues cv = new ContentValues();
        cv.put(Schema.LOCK_BY_COLUMN,"");
        cv.put(Schema.LOCK_TIME_STAMP_COLUMN,0);
        //int count =getWritableDatabase().update(Schema.DataTable_Orders.TABLE_NAME,cv,Schema.LOCK_TIME_STAMP_COLUMN+"<=? or "+Schema.LOCK_TIME_STAMP_COLUMN+" is null" +
        int count =db.update(strDbTable,cv,Schema.LOCK_TIME_STAMP_COLUMN+"<=? or "+Schema.LOCK_TIME_STAMP_COLUMN+" is null" +
                "",new String[]{strNowTimeInMilli});

        //getWritableDatabase().execSQL(strSQL);
    }
    public void UnlockExpiredRecord(String strDbTable)
    {
        String strNowTimeInMilli = (Calendar.getInstance().getTimeInMillis()-AppSettings.LOCK_RECORD_DURATION)+"";
        /*Cursor c = getReadableDatabase().rawQuery("select "+Schema.LOCK_TIME_STAMP_COLUMN+","+Schema.LOCK_BY_COLUMN+" from "+Schema.DataTable_Orders.TABLE_NAME,null);
        c.moveToFirst();
        long lockedTime =c.getLong(0);
        String strName = c.getString(1);
        c.close();
        String strSQL = "update "+strDbTable+" set "+Schema.LOCK_BY_COLUMN+"='' where ("+
                Schema.LOCK_TIME_STAMP_COLUMN+")<="+strNowTimeInMilli;*/
        ContentValues cv = new ContentValues();
        cv.put(Schema.LOCK_BY_COLUMN,"");
        cv.put(Schema.LOCK_TIME_STAMP_COLUMN,0);
        //int count =getWritableDatabase().update(Schema.DataTable_Orders.TABLE_NAME,cv,Schema.LOCK_TIME_STAMP_COLUMN+"<=? or "+Schema.LOCK_TIME_STAMP_COLUMN+" is null" +
        int count =getWritableDatabase().update(strDbTable,cv,Schema.LOCK_TIME_STAMP_COLUMN+"<=? or "+Schema.LOCK_TIME_STAMP_COLUMN+" is null" +
                "",new String[]{strNowTimeInMilli});

        //getWritableDatabase().execSQL(strSQL);
    }
    private boolean RecordVersionTheSame(Cursor c,Object[] valueToCompare,int[] indexes,Enum.CompareObjectVersionType compareType) {

        if(compareType== Enum.CompareObjectVersionType.Order) {

            for(int i=0;i<valueToCompare.length;i++) {
                OrderCompareModel ocm = (OrderCompareModel) valueToCompare[i];
                //String sV = c.getString(indexes[1]);String sT = c.getString(indexes[0]);String sR = c.getString(indexes[2]);long lngUpdateDate = c.getLong(indexes[3]);
                if(ocm.Version==c.getInt(indexes[1]) &&
                        ocm.TableId.compareToIgnoreCase(c.getString(indexes[0]))==0 &&
                        ocm.ReceiptIndex==c.getInt(indexes[2]) &&
                        ocm.LastUpdateDate == c.getLong(indexes[3])) {
                    return true;
                }
            }
        }
        else if(compareType == Enum.CompareObjectVersionType.Receipt) {
            for(int i=0;i<valueToCompare.length;i++) {
                ReceiptCompareModel rcm = (ReceiptCompareModel) valueToCompare[i];
                int tempV = c.getInt(indexes[1]);
                String tempRN = c.getString(indexes[0]);
                if (rcm.Version == c.getInt(indexes[1]) &&
                        rcm.ReceiptNum.compareToIgnoreCase(c.getString(indexes[0]))==0)
                {
                    return true;

                }
            }
        }

        return false;
    }
    public int GetLock(ContentValues cValues,String strTableName,String[] args,String strWhereClause)
    {

        return getWritableDatabase().update(strTableName,cValues,strWhereClause,args);

    }
    public int DeleteOrdersByTableId(String strTableId)
    {
        String strWhereClause = Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"=?";
        String[] args = new String[]{strTableId};
        return getWritableDatabase().delete(Schema.DataTable_Orders.TABLE_NAME,strWhereClause,args);
    }
    public int DeleteOrders(ArrayList<OrderCompareModel> orderToDelete) {
        String strWhereClause=Schema.DataTable_Orders.RECEIPT_INDEX_COLUMN+" in(";
        for(int i=0;i<orderToDelete.size();i++) {
            strWhereClause+=orderToDelete.get(i).ReceiptIndex+((i==orderToDelete.size()-1)?")":",");
        }
        strWhereClause+=" and "+Schema.DataTable_Orders.CUSTOMER_TABLE_ID_COLUMN+"="+orderToDelete.get(0).TableId;
        return getWritableDatabase().delete(Schema.DataTable_Orders.TABLE_NAME,strWhereClause,null);
    }
    public boolean BulkUpdateItemAndModifiers(ItemObject item,long lngItemId,
                                           ArrayList<ModifierObject>InactiveModifiers,ArrayList<ModifierObject>NewModifiers)
    {
        long lngtime = System.currentTimeMillis();
        long lngNewId;
        SQLiteDatabase db = this.getWritableDatabase();
        int result;
        boolean blnSuccess = false;

        String strWhereClause;
        String[] args;
        ContentValues cValue;
        //String strTemp="";
        db.beginTransaction();
        try
        {
            if(item!=null)
            {
                //update item (deletion not performing here)
                LogItem(item.getID(),db);//log the original value
                strWhereClause = Schema.DataTable_Item.ID_COLUMN + " =? and "+Schema.DataTable_Item.ACTIVE_FLAG_COLUMN+"=1";
                args = new String[]{item.getID()+""};
                ContentValues values = new ContentValues();
                values.put(Schema.DataTable_Item.ITEM_NAME_COLUMN, item.getName());
                values.put(Schema.DataTable_Item.ITEM_PRICE_COLUMN, item.getPrice().toPlainString());
                values.put(Schema.DataTable_Item.PICTURE_PATH_COLUMN,item.getPicturePath());
                values.put(Schema.DataTable_Item.BARCODE_COLUMN,item.getBarcode());
                //increase version number
                item.SetVersion(item.GetCurrentVersionNumber()+1);
                values.put(Schema.VERSION_COLUMN,item.GetCurrentVersionNumber()+"");
                result = db.update(Schema.DataTable_Item.TABLE_NAME,values,strWhereClause,args);

                if(result==0)return blnSuccess;
            }

            //delete modifiers
            if(InactiveModifiers.size()>0) {

                //"in" range won't work for arg array
                //executing each by each in order to log
                strWhereClause = Schema.DataTable_Modifier.PARENT_ID_COLUMN + "=? and " + Schema.DataTable_Modifier.ID_COLUMN + " =? and " + Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN + "=1";
                for (int i = 0; i < InactiveModifiers.size(); i++) {

                    args = new String[]{lngItemId + "", InactiveModifiers.get(i).getID()+""};
                    cValue = new ContentValues();
                    cValue.put(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN, 0);
                    cValue.put(Schema.DataTable_Modifier.INACTIVE_DATE_COLUMN, lngtime);
                    result = db.update(Schema.DataTable_Modifier.TABLE_NAME, cValue, strWhereClause, args);
                    //revert if updated row not the same as the list size
                    if (result ==0) return blnSuccess;


                }


            }
            //update/insert modifier
            if(NewModifiers.size()>0) {
                for (int i = 0; i < NewModifiers.size(); i++) {
                    ModifierObject mo = NewModifiers.get(i);
                    if (mo.getID() >0) {
                        //perform update
                        LogModifier(mo.getID(),db);//log the original value
                        strWhereClause = Schema.DataTable_Modifier.PARENT_ID_COLUMN + "=? and " + Schema.DataTable_Modifier.ID_COLUMN + "=?" + " and " + Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN + "=1";
                        args = new String[]{lngItemId + "", mo.getID() + ""};
                        cValue = new ContentValues();
                        cValue.put(Schema.DataTable_Modifier.ITEM_PRICE_COLUMN, mo.getPrice().toPlainString());
                        cValue.put(Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN, mo.getMutualGroup());
                        cValue.put(Schema.DataTable_Modifier.ITEM_NAME_COLUMN, mo.getName());
                        mo.SetVersion(mo.GetCurrentVersionNumber()+1);
                        cValue.put(Schema.VERSION_COLUMN, mo.GetCurrentVersionNumber());
                        if (db.update(Schema.DataTable_Modifier.TABLE_NAME, cValue, strWhereClause, args) == 0) {
                            return blnSuccess;
                        }
                    } else {
                        //new insert
                        cValue = new ContentValues();
                        cValue.put(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN, 1);
                        cValue.put(Schema.DataTable_Modifier.ITEM_NAME_COLUMN, mo.getName());
                        cValue.put(Schema.DataTable_Modifier.PARENT_ID_COLUMN, lngItemId);
                        cValue.put(Schema.DataTable_Modifier.ITEM_PRICE_COLUMN, mo.getPrice().toPlainString());
                        cValue.put(Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN, mo.getMutualGroup());
                        cValue.put(Schema.VERSION_COLUMN, mo.GetCurrentVersionNumber());
                        lngNewId = GenerateNextModifierRecordId();
                        cValue.put(Schema.DataTable_Modifier.ID_COLUMN, lngNewId);

                        //revert if insert failed
                        if (db.insert(Schema.DataTable_Modifier.TABLE_NAME, null, cValue) <= 0) {
                            return blnSuccess;
                        }
                        else{
                            mo.setId(lngNewId);
                        }
                    }
                }
            }
            blnSuccess=true;
            db.setTransactionSuccessful();
        }

        finally {
            db.endTransaction();
            return blnSuccess;
        }
    }
    public void DeleteDataTable(String strTableName)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String strSql = "Delete from "+strTableName ;
        db.execSQL(strSql);
    }
    public boolean RestoreDatabase(ArrayList<Pair<String,ArrayList<ContentValues>>> sqlTasks,boolean blnOverwriteTable)
    {
        boolean blnSuccess = false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try{
            for(int i=0;i<sqlTasks.size();i++)
            {

                if(blnOverwriteTable) {
                    String strSql = "Delete from "+sqlTasks.get(i).first;
                    db.execSQL(strSql);
                }

                for(int j=0;j<sqlTasks.get(i).second.size();j++) {
                    long result =db.insert(sqlTasks.get(i).first, null, sqlTasks.get(i).second.get(j));
                    if(result==0)
                    {
                        common.Utility.LogActivity("failed to insert new record");
                        throw new Exception("failed to insert new record");
                    }
                }
            }

            blnSuccess = true;
            db.setTransactionSuccessful();
        }
        catch(Exception ex)
        {

        }
        finally {
            db.endTransaction();
            return blnSuccess;
        }

    }
    public boolean UpdatePromotionObjectWithTransaction(PromotionObject po)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean blnSuccess = false;
        try
        {

            Date d1 = new Date(po.GetStartDateTime());
            Date d2 = new Date(po.GetEndDateTime());
            //log promotion object detail before updating
            LogPromotion(po.GetId(),db);
            //increase the version number by 1 after logged into log table
            po.IncreaseCurrentVersionNumber();
            ContentValues values = new ContentValues();
            values.put(Schema.DataTable_Promotion.TITLE_COLUMN,po.GetTitle());
            values.put(Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN,po.GetStartDateTime());
            values.put(Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN,po.GetEndDateTime());
            values.put(Schema.DataTable_Promotion.DAY_COLUMN,po.GetOccurDay());
            values.put(Schema.DataTable_Promotion.REPEAT_EVERY_COLUMN,po.GetOccurrence().value);
            values.put(Schema.DataTable_Promotion.RULE_TYPE_COLUMN,po.GetRule().value);
            values.put(Schema.DataTable_Promotion.RULE_ITEM_COLUMN,po.GetAllRuleItemString());
            values.put(Schema.DataTable_Promotion.RULE_ABOVE_AMOUNT_COLUMN,po.GetStartingAmount().toPlainString());
            values.put(Schema.DataTable_Promotion.RULE_TO_AMOUNT_COLUMN,po.GetUpperLimitAmount().toPlainString());
            values.put(Schema.DataTable_Promotion.RULE_AMOUNT_NO_LIMIT_COLUMN,po.GetUpperLimitFlag()?1:0);
            values.put(Schema.DataTable_Promotion.DISCOUNT_BY_COLUMN,po.GetDiscountType().value);
            values.put(Schema.DataTable_Promotion.DISCOUNT_VALUE_COLUMN,po.GetDiscountValue());
            values.put(Schema.DataTable_Promotion.COLOR_COLUMN,po.GetDiscountColor().value);
            values.put(Schema.DataTable_Promotion.ACTIVE_FLAG_COLUMN,po.IsActive()?1:0);
            values.put(Schema.DataTable_Promotion.INACTIVE_DATE_COLUMN,po.GetInactiveDate());
            values.put(Schema.DataTable_Promotion.BY_DAY_OF_WEEK_COLUMN,po.GetPromotionDateOption().value);
            values.put(Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_MONTHS_COLUMN,po.GetOccurMonth());
            values.put(Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_DAY_COLUMN,po.GetDayOfMonth());
            values.put(Schema.DataTable_Promotion.EXPIRATION_DATE_COLUMN,po.GetExpirationDate());
            values.put(Schema.VERSION_COLUMN,po.GetCurrentVersionNumber());


            int rowCount=db.update(Schema.DataTable_Promotion.TABLE_NAME
            ,values,Schema.DataTable_Promotion.ID_COLUMN+"=?",new String[]{po.GetId()+""});

            if(rowCount>0)
            {

                common.Utility.LogActivity("updated promotion "+po.GetId());
            }

            blnSuccess = true;
            db.setTransactionSuccessful();
        }

        finally {
            db.endTransaction();
            return blnSuccess;
        }
    }
    public boolean BulkUpdateModifiers(ArrayList<ModifierObject> InactiveList,ArrayList<ModifierObject> NewList)
    {

        long lngTime = System.currentTimeMillis();
        ArrayList<Long> results = new ArrayList<Long>();
        int result=0;
        boolean blnSuccess = false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try
        {

            //delete
            if(InactiveList.size()>0) {
                for(ModifierObject mo:InactiveList) {
                    //set inactive
                    String strWhereClause = Schema.DataTable_Modifier.ID_COLUMN + " =? and "+Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN+"=1";

                    ContentValues values = new ContentValues();
                    values.put(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN, 0);
                    values.put(Schema.DataTable_Modifier.INACTIVE_DATE_COLUMN, lngTime);


                    String[] args = new String[]{mo.getID()+""};
                    result = db.update(Schema.DataTable_Modifier.TABLE_NAME, values, strWhereClause, args);
                }
            }

            //update and insert new
            if(NewList.size()>0)
            {

                //check which one is new and which one is performing update
                for(ModifierObject mo:NewList)
                {

                    //insertion
                    if(mo.getID()<0) {
                        ContentValues cValue = new ContentValues();
                        cValue.put(Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN, 1);
                        cValue.put(Schema.DataTable_Modifier.ITEM_NAME_COLUMN, mo.getName());
                        cValue.put(Schema.DataTable_Modifier.PARENT_ID_COLUMN, mo.getParentID());
                        cValue.put(Schema.DataTable_Modifier.ITEM_PRICE_COLUMN, mo.getPrice().toPlainString());
                        cValue.put(Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN, mo.getMutualGroup());
                        cValue.put(Schema.DataTable_Modifier.ID_COLUMN, GenerateNextModifierRecordId());
                        cValue.put(Schema.VERSION_COLUMN, mo.GetCurrentVersionNumber());

                        results.add(db.insert(Schema.DataTable_Modifier.TABLE_NAME, null, cValue));
                    }
                    else
                    {
                        //update
                        //log to database before update
                        LogModifier(mo.getID(),db);
                        String strWhereClause = Schema.DataTable_Modifier.ID_COLUMN + " =? and "+Schema.DataTable_Modifier.ACTIVE_FLAG_COLUMN+"=1";

                        ContentValues values = new ContentValues();
                        values.put(Schema.DataTable_Modifier.ITEM_NAME_COLUMN, mo.getName());
                        values.put(Schema.DataTable_Modifier.ITEM_PRICE_COLUMN, mo.getPrice().toPlainString());
                        values.put(Schema.DataTable_Modifier.MUTUAL_GROUP_COLUMN,mo.getMutualGroup());
                        mo.SetVersion(mo.GetCurrentVersionNumber()+1);
                        values.put(Schema.VERSION_COLUMN,mo.GetCurrentVersionNumber());

                        String[] args = new String[]{mo.getID()+""};
                        result = db.update(Schema.DataTable_Modifier.TABLE_NAME, values, strWhereClause, args);
                    }
                }
            }
            blnSuccess = true;
           db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
            return blnSuccess;
        }



    }

    private void LogPromotion(long id,SQLiteDatabase db)
    {
        String strSql = "insert into "+Schema.DataTable_PromotionUpdateLog.TABLE_NAME+"("
                +Schema.DataTable_PromotionUpdateLog.ID_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.TITLE_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.FROM_DATE_TIME_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.TO_DATE_TIME_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.DAY_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.REPEAT_EVERY_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.RULE_TYPE_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.RULE_ITEM_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.RULE_ABOVE_AMOUNT_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.RULE_TO_AMOUNT_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.RULE_AMOUNT_NO_LIMIT_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.DISCOUNT_BY_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.DISCOUNT_VALUE_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.BY_DAY_OF_WEEK_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_MONTHS_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.OCCURRENCE_CALENDAR_DAY_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.EXPIRATION_DATE_COLUMN+","
                +Schema.VERSION_COLUMN+","
                +Schema.DataTable_PromotionUpdateLog.DATE_COLUMN+")"
                +" select "
                +Schema.DataTable_Promotion.ID_COLUMN+","
                +Schema.DataTable_Promotion.TITLE_COLUMN+","
                +Schema.DataTable_Promotion.FROM_DATE_TIME_COLUMN+","
                +Schema.DataTable_Promotion.TO_DATE_TIME_COLUMN+","
                +Schema.DataTable_Promotion.DAY_COLUMN+","
                +Schema.DataTable_Promotion.REPEAT_EVERY_COLUMN+","
                +Schema.DataTable_Promotion.RULE_TYPE_COLUMN+","
                +Schema.DataTable_Promotion.RULE_ITEM_COLUMN+","
                +Schema.DataTable_Promotion.RULE_ABOVE_AMOUNT_COLUMN+","
                +Schema.DataTable_Promotion.RULE_TO_AMOUNT_COLUMN+","
                +Schema.DataTable_Promotion.RULE_AMOUNT_NO_LIMIT_COLUMN+","
                +Schema.DataTable_Promotion.DISCOUNT_BY_COLUMN+","
                +Schema.DataTable_Promotion.DISCOUNT_VALUE_COLUMN+","
                +Schema.DataTable_Promotion.BY_DAY_OF_WEEK_COLUMN+","
                +Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_MONTHS_COLUMN+","
                +Schema.DataTable_Promotion.OCCURRENCE_CALENDAR_DAY_COLUMN+","
                +Schema.DataTable_Promotion.EXPIRATION_DATE_COLUMN+","
                +Schema.VERSION_COLUMN+","
                +Calendar.getInstance().getTimeInMillis()
                +" from "+Schema.DataTable_Promotion.TABLE_NAME
                +" where "+Schema.DataTable_Promotion.ID_COLUMN +"="+id;

        db.execSQL(strSql);
    }
    private void LogItem(long lnId,SQLiteDatabase db)
    {
        String strSql = "insert into "+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+"("
                +Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN+","
                +Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN+","
                +Schema.DataTable_ItemAndModifierUpdateLog.DATE_COLUMN+","
                +Schema.VERSION_COLUMN+","
                +Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN+")"
                +" select "
                +Schema.DataTable_Item.ITEM_PRICE_COLUMN+","
                +Schema.DataTable_Item.ITEM_NAME_COLUMN+","
                +Calendar.getInstance().getTimeInMillis()+","
                +Schema.VERSION_COLUMN+","
                +Schema.DataTable_Item.ID_COLUMN
                +" from "+Schema.DataTable_Item.TABLE_NAME
                +" where "+Schema.DataTable_Item.ID_COLUMN+"="+lnId;

        db.execSQL(strSql);

    }
    private void LogModifier(long lnId,SQLiteDatabase db)
    {
        String strSql = "insert into "+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+"("
                +Schema.DataTable_ItemAndModifierUpdateLog.PRICE_COLUMN+","
                +Schema.DataTable_ItemAndModifierUpdateLog.NAME_COLUMN+","
                +Schema.DataTable_ItemAndModifierUpdateLog.DATE_COLUMN+","
                +Schema.VERSION_COLUMN+","
                +Schema.DataTable_ItemAndModifierUpdateLog.ID_COLUMN+")"
                +" select "
                +Schema.DataTable_Modifier.ITEM_PRICE_COLUMN+","
                +Schema.DataTable_Modifier.ITEM_NAME_COLUMN+","
                +Calendar.getInstance().getTimeInMillis()+","
                +Schema.VERSION_COLUMN+","
                +Schema.DataTable_Modifier.ID_COLUMN
                +" from "+Schema.DataTable_Modifier.TABLE_NAME
                +" where "+Schema.DataTable_Modifier.ID_COLUMN+"="+lnId;

        db.execSQL(strSql);

    }
    public long Insert(String strTableName,String[] columns,String[] values){
        //try {

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cValues = new ContentValues();
            for(int i=0;i<columns.length;i++) {
                if(!values[i].equalsIgnoreCase("nan")) {
                    //Log.d("insert into database", "Table name:" + strTableName + " ," + columns[i] + ":" + values[i]);
                    cValues.put(columns[i], values[i]);
                }
            }


        return db.insertOrThrow(strTableName, null, cValues);

    }
    public int Update(String strTableName,String[] columns,String[] values,String strWhereClause,String[] args)
    {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues cValues = new ContentValues();
            for (int i = 0; i < columns.length; i++) {
                common.Utility.LogActivity("update database Table name:" + strTableName + " ," + columns[i] + ":" + values[i]);
                cValues.put(columns[i], values[i]);
            }
            return db.update(strTableName,cValues,strWhereClause,args);
        }
        catch(Exception ex)
        {
            ShowErrorMessageBox("Update",ex);
        }
        return -1;
    }

    public Cursor query(String strTableName,String[] columns,String strSelections,String[] strSelectionArgs,String strSortOrder)
    {
        //try {
            SQLiteDatabase db = this.getReadableDatabase();
            //Cursor c = db.query(Schema.DataTable_Category.TABLE_NAME,columns,null,null,null,null,null);
            Cursor c = db.query(
                    strTableName,
                    columns,
                    strSelections,
                    strSelectionArgs,
                    null,
                    null,
                    strSortOrder
            );
            //ShowMessageBox("row count","row found "+c.getCount());
            c.moveToFirst();
            return c;
        //}
        //catch(Exception ex)
        //{
            //ShowErrorMessageBox("Query",ex);
        //}
        //return null;
    }
    public Cursor rawQuery(String strSQL)
    {
        ContextWrapper cw = new ContextWrapper(context);
        File f = cw.getDatabasePath(Schema.DataTable_Category.TABLE_NAME);

        //ShowMessageBox("test",f.getAbsolutePath());

        return getReadableDatabase().rawQuery(strSQL,new String[]{});
    }

    private void ShowErrorMessageBox(String strMethodName,Exception ex)
    {

        common.Utility.LogActivity("EXCEPTION: " + strMethodName+ " " + ex.getMessage() + ", Cause " + ex.getCause().toString());

        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(strMethodName);
        messageBox.setMessage(ex.getMessage()+", Cause "+ex.getCause().toString());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }

    public long GenerateNextInventoryId()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    public long GenerateNextSupplierId()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    public long GenerateNextServerId()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    public long GenerateNextPromotionId()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    public long GenerateNextCategoryRecordId()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    public long GenerateNextItemRecordId()
    {
        return Calendar.getInstance().getTimeInMillis();
    }
    public long GenerateNextModifierRecordId()
    {
        //need to run query to check for next available id, worry the system creating modifier within split second and making two modifiers have identical ID
        String strQuery = "select * from "+Schema.DataTable_Modifier.TABLE_NAME + " where "+Schema.DataTable_Modifier.ID_COLUMN+"=";
        long nextAvailableId =1449779418273l;// Calendar.getInstance().getTimeInMillis();
        Cursor cursor = rawQuery(strQuery + nextAvailableId);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            cursor.close();
            nextAvailableId++;
            cursor = rawQuery(strQuery + nextAvailableId);
            cursor.moveToFirst();
        }
        cursor.close();
        return nextAvailableId;
    }



    public Cursor GetAllModifierRecords()
    {
        return GetAllRecords(Schema.DataTable_Modifier.TABLE_NAME);
    }
    public Cursor GetAllPaymentTypeRecords()
    {
        return GetAllRecords(Schema.DataTable_PaymentType.TABLE_NAME);
    }
    public Cursor GetAllServerRecords()
    {
        return GetAllRecords(Schema.DataTable_Server.TABLE_NAME);
    }
    public Cursor GetAllReceiptRecords()
    {
        return GetAllRecords(Schema.DataTable_Receipt.TABLE_NAME);
    }
    public Cursor GetAllSupplierRecords()
    {
        return GetAllRecords(Schema.DataTable_Supplier.TABLE_NAME);
    }
    public Cursor GetAllItemRecords()
    {
        return GetAllRecords(Schema.DataTable_Item.TABLE_NAME);
    }
    public Cursor GetAllCategoryRecords()
    {
        return GetAllRecords(Schema.DataTable_Category.TABLE_NAME);
    }
    public Cursor GetAllInventoryRecords(){return GetAllRecords(Schema.DataTable_Inventory.TABLE_NAME);}
    public Cursor GetAllRecords(String strTableName)
    {
        return getReadableDatabase().rawQuery("SELECT * FROM "+strTableName,null);

    }
}
