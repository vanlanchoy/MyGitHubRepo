package tme.pos.BusinessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by vanlanchoy on 11/24/2016.
 */

public class FloorPlanManager {
    Context context;
    int currentRecordVersion =0;
    public FloorPlanManager(Context c) {
        context = c;
    }
    public String LoadFloorPlanData() {
        String strObj ="";
        Cursor c = new DatabaseHelper(context).getReadableDatabase().rawQuery("select "+Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN+" from "+Schema.DataTable_FloorPlanData.TABLE_NAME+
        " where "+Schema.ID_COLUMN+"="+common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE,null);

        c.moveToFirst();
        if(!c.isAfterLast()) {
            strObj = c.getString(0);
        }

        c.close();

        return strObj;


    }
    public Enum.DBOperationResult Save(Serializable serializableObj)
    {

        Enum.DBOperationResult result = Enum.DBOperationResult.Failed;
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        //load record info
        LockDetails ld =GetCurrentLockDetailInfo();
        if (currentRecordVersion==0) {
            if(ld.Version>0) {
                currentRecordVersion = ld.Version;
            }
        }

        if(currentRecordVersion==0) {
            //insert new record
            result = InsertNewFloorPlanRow(ObjectToString(serializableObj));
        }
        else
        {
            //get lock
            int retryCount=3;
            Enum.GetLockResult lockResult = Enum.GetLockResult.TryLater;
            while(retryCount>0) {
                lockResult = LockFloorPlanRecord(ld);
                if (lockResult == Enum.GetLockResult.Granted) {
                    break;
                }
                retryCount--;

            }
            if(lockResult== Enum.GetLockResult.Granted) {
                //update record
                result = UpdateFloorPlanRecord(ObjectToString(serializableObj),ld);
            }
        }

        return result;

    }
    private Enum.DBOperationResult UpdateFloorPlanRecord(String strObj,LockDetails ld)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        SQLiteDatabase db =helper.getWritableDatabase();
        String strSql = Schema.VERSION_COLUMN+"=? and "+Schema.ID_COLUMN+"=? and "+
                Schema.LOCK_BY_COLUMN+"=?";
        String[] args = new String[]{ld.Version+"",common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE+"",common.myAppSettings.DEVICE_UNIQUE_ID};
        ContentValues cv = new ContentValues();

        cv.put(Schema.VERSION_COLUMN,ld.Version+1);
        cv.put(Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN,strObj);
        cv.put(Schema.LOCK_TIME_STAMP_COLUMN,"");
        cv.put(Schema.LOCK_BY_COLUMN,"");
        int count = db.update(Schema.DataTable_FloorPlanData.TABLE_NAME,cv,strSql,args);
        if(count>0) {
           return Enum.DBOperationResult.Success;
        }
        else {
            return Enum.DBOperationResult.Failed;
        }
    }
    private Enum.DBOperationResult InsertNewFloorPlanRow(String strObj) {
        Enum.DBOperationResult result = Enum.DBOperationResult.Success;
        DatabaseHelper helper = new DatabaseHelper(context);
        ContentValues cv = new ContentValues();
        cv.put(Schema.ID_COLUMN,common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE);
        cv.put(Schema.VERSION_COLUMN,1);
        cv.put(Schema.DataTable_FloorPlanData.FLOOR_PLAN_COLUMN,strObj);
        try {
            //id is unique constraint column, so 1st insert 1st win
            helper.getWritableDatabase().insertOrThrow(Schema.DataTable_FloorPlanData.TABLE_NAME, null, cv);
        }
        catch(Exception ex) {
            result = Enum.DBOperationResult.Existed;
            common.Utility.LogActivity(ex.getMessage());
        }

        return result;
    }
    private void UnLockRecords() {
        common.Utility.LogActivity("unlock lock for floor plan record");
        String strSQL = "update "+Schema.DataTable_FloorPlanData.TABLE_NAME+" set "+Schema.LOCK_BY_COLUMN+"='',"+Schema.LOCK_TIME_STAMP_COLUMN+"=0 where "+Schema.ID_COLUMN+"='"+common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE+"'";
        new DatabaseHelper(context).getWritableDatabase().execSQL(strSQL);
    }
    private Enum.GetLockResult LockFloorPlanRecord(LockDetails ld) {
        //unlock any expired lock
        UnLockRecords();
        //now lock the record
        String strSql = Schema.VERSION_COLUMN+"=?  and  ("+
                Schema.LOCK_BY_COLUMN+"=? or "+Schema.LOCK_BY_COLUMN+" is null) and "+Schema.ID_COLUMN+"="+common.myAppSettings.RECEIPT_NUM_ID_COLUMN_VALUE;
        String[] args = new String[]{ld.Version+"",""};
        ContentValues cv = new ContentValues();
        cv.put(Schema.LOCK_BY_COLUMN,common.myAppSettings.DEVICE_UNIQUE_ID);
        cv.put(Schema.LOCK_TIME_STAMP_COLUMN, Calendar.getInstance().getTimeInMillis()+"");
        int count = new DatabaseHelper(context).getWritableDatabase().update(Schema.DataTable_FloorPlanData.TABLE_NAME,cv,strSql,args);
        if(count>0) {
            return Enum.GetLockResult.Granted;
        }
        return Enum.GetLockResult.TryLater;
    }
    public LockDetails GetCurrentLockDetailInfo() {
        LockDetails ld = new LockDetails();
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor c = helper.getReadableDatabase().rawQuery("select *  from "+
                Schema.DataTable_FloorPlanData.TABLE_NAME,null);
        c.moveToFirst();
        if(!c.isAfterLast()) {

            int versionCol = c.getColumnIndex(Schema.VERSION_COLUMN);
            int lockByCol = c.getColumnIndex(Schema.LOCK_BY_COLUMN);
            int lockDateCol =c.getColumnIndex(Schema.LOCK_TIME_STAMP_COLUMN);
            int idCol = c.getColumnIndex(Schema.ID_COLUMN);
            int id = c.getInt(idCol);
            ld.LockedDateTime = c.getLong(lockDateCol);
            ld.Version = c.getInt(versionCol);
            ld.DeviceId = c.getString(lockByCol);
        }
        c.close();
        return ld;
    }

    public  String ObjectToString(Serializable obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(
                    new Base64OutputStream(baos, Base64.NO_PADDING
                            | Base64.NO_WRAP));
            oos.writeObject(obj);
            oos.close();
            return baos.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public  Object StringToObject(String str) {
        try {
            return new ObjectInputStream(new Base64InputStream(
                    new ByteArrayInputStream(str.getBytes()), Base64.NO_PADDING
                    | Base64.NO_WRAP)).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
