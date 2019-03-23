package tme.pos.BusinessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by kchoy on 6/10/2015.
 */
public class SupplierList {
    Supplier[] suppliers;
    Context context;
    public SupplierList(Context c)
    {
        context = c;
        ArrayList<Supplier> list = LoadSupplier();
        suppliers = new Supplier[list.size()];
        if(list.size()>0)InsertIntoArray(list);
    }
    public Supplier[] GetSupplier()
    {
        return suppliers;
    }
    public String GetSupplierName(long supplierId)
    {


        for(Supplier s:suppliers)
        {
            if(s!=null)
            if(s.SupplierId==supplierId)return s.Name;
        }

        return "";
    }
    private void InsertIntoArray(List<Supplier> list)
    {
        for(int i=0;i<list.size();i++)
        {
            suppliers[i] = list.get(i);
        }

        //sort the array
        QuickSort(0,suppliers.length-1);
    }
    public int Update(Supplier s)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String strWhereClause = Schema.DataTable_Supplier.ID_COLUMN + " =? ";
        String[] args = new String[]{s.SupplierId+""};
        String[] columns = new String[]{Schema.DataTable_Supplier.NAME_COLUMN,
                Schema.DataTable_Supplier.PHONE_COLUMN,
                Schema.DataTable_Supplier.EMAIL_COLUMN,
                Schema.DataTable_Supplier.ADDRESS_COLUMN,
                Schema.DataTable_Supplier.NOTE_COLUMN};
        String[] values = new String[]{s.Name,s.PhoneNumber,s.Email,s.Address,s.Note};

        int result =helper.Update(Schema.DataTable_Supplier.TABLE_NAME,columns, values, strWhereClause, args);
        QuickSort(0,suppliers.length-1);
        return result;
    }
    public int Delete(long supplierId)
    {

        long lngtime = System.currentTimeMillis();
        String strWhereClause = Schema.DataTable_Supplier.ID_COLUMN + "=? and "+Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN+"=1";
        String[] args = new String[]{supplierId + ""};
        ContentValues cValue = new ContentValues();
        cValue.put(Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN, 0);
        cValue.put(Schema.DataTable_Supplier.INACTIVE_DATE_COLUMN, lngtime);

        int result = new DatabaseHelper(context).DeleteSupplier(cValue,strWhereClause,args);
        if(result>0) {
            Supplier[] temp = new Supplier[suppliers.length - 1];
            int index = 0;
            for (int i = 0; i < suppliers.length; i++) {
                if (suppliers[i].SupplierId != supplierId) {
                    temp[index++] = suppliers[i];
                }
            }

            suppliers = temp;
        }

        return result;
    }
    public long Insert(Supplier s)
    {

        DatabaseHelper helper = new DatabaseHelper(context);




        String[] columns= new String[]{Schema.DataTable_Supplier.NAME_COLUMN,
                Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Supplier.EMAIL_COLUMN,
                Schema.DataTable_Supplier.PHONE_COLUMN,
                Schema.DataTable_Supplier.ADDRESS_COLUMN,
                Schema.DataTable_Supplier.ID_COLUMN,
                Schema.DataTable_Supplier.NOTE_COLUMN};
        String[] values = new String[]{s.Name,"1",s.Email+"",s.PhoneNumber,s.Address,s.SupplierId+"",s.Note};

        long id =helper.Insert(Schema.DataTable_Supplier.TABLE_NAME,columns,values);

        if(id!=-1) {
            Supplier[] temp = new Supplier[suppliers.length + 1];
            boolean blnInserted = false;
            int newArrayIndex = 0;
            for (int i = 0; i < suppliers.length; i++) {
                if (s.compareTo(suppliers[i]) < 1 && !blnInserted) {
                    temp[newArrayIndex++] = s;
                    temp[newArrayIndex++] = suppliers[i];
                    blnInserted = true;
                } else {
                    temp[newArrayIndex++] = suppliers[i];
                }
            }

            if (!blnInserted) temp[newArrayIndex] = s;
            suppliers = temp;
        }
        return id;
    }
    private void QuickSort(int start,int end)
    {
        if (start < end)
        {
            int p = Partition( start, end);
            QuickSort( start, p - 1);
            QuickSort( p + 1, end);
        }
    }
    private int Partition(int start,int end)
    {
        int pivotIndex = end;
        int startingIndex = start;
        Supplier temp;
        for (int i = start; i < end; i++)
        {
            if (suppliers[i].compareTo(suppliers[pivotIndex])<1)
            {
                temp = suppliers[i];
                suppliers[i] = suppliers[startingIndex];
                suppliers[startingIndex] = temp;
                startingIndex++;
            }
        }

        temp = suppliers[startingIndex];
        suppliers[startingIndex] = suppliers[pivotIndex];
        suppliers[pivotIndex] = temp;
        return startingIndex;
    }
    public ArrayList<Supplier> LoadSupplier()
    {

        ArrayList<Supplier> suppliers = new ArrayList<Supplier>();

        DatabaseHelper helper = new DatabaseHelper(context);

        //column names
        String[] columns = {
                Schema.DataTable_Supplier.ID_COLUMN,
                Schema.DataTable_Supplier.NAME_COLUMN,
                Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Supplier.ADDRESS_COLUMN,
                Schema.DataTable_Supplier.EMAIL_COLUMN,
                Schema.DataTable_Supplier.INACTIVE_DATE_COLUMN,
                Schema.DataTable_Supplier.PHONE_COLUMN,
                Schema.DataTable_Supplier.NOTE_COLUMN
        };
        String strSortOrder = Schema.DataTable_Supplier.ID_COLUMN + " asc";

        String strWhereClause =Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN + "=?";
        String[] args = {"1"};

        Cursor c = helper.query(Schema.DataTable_Supplier.TABLE_NAME, columns, strWhereClause, args, strSortOrder);
        int columnNameIndex = c.getColumnIndex(Schema.DataTable_Supplier.NAME_COLUMN);
        int columnIdIndex = c.getColumnIndex(Schema.DataTable_Supplier.ID_COLUMN);
        int columnActiveFlagIndex = c.getColumnIndex(Schema.DataTable_Supplier.ACTIVE_FLAG_COLUMN);
        int columnEmailIndex = c.getColumnIndex(Schema.DataTable_Supplier.EMAIL_COLUMN);
        int columnPhoneIndex = c.getColumnIndex(Schema.DataTable_Supplier.PHONE_COLUMN);
        int columnAddressIndex = c.getColumnIndex(Schema.DataTable_Supplier.ADDRESS_COLUMN);
        int columnNoteIndex = c.getColumnIndex(Schema.DataTable_Supplier.NOTE_COLUMN);
        while(!c.isAfterLast())
        {
            Supplier s = new Supplier();
            s.Name = c.getString(columnNameIndex);
            s.SupplierId = c.getLong(columnIdIndex);
            s.Address = c.getString(columnAddressIndex);
            s.IsActive = (c.getInt(columnActiveFlagIndex)==0)?false:true;
            s.PhoneNumber = c.getString(columnPhoneIndex);
            s.Email = c.getString(columnEmailIndex);
            s.Note = c.getString(columnNoteIndex);
            suppliers.add(s);

            c.moveToNext();
        }

        c.close();
        helper.close();
        return suppliers;
    }
}
