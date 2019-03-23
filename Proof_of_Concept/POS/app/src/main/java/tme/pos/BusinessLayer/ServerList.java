package tme.pos.BusinessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;

/**
 * Created by kchoy on 4/13/2015.
 */
public class ServerList {
    Server[] servers;
    Context context;
    public ServerList(Context c)
    {
        context = c;
        ArrayList<Server>list = LoadServer();
        servers = new Server[list.size()];
        if(list.size()>0)InsertIntoArray(list);
    }
    public Server[] GetServers()
    {
        return servers;
    }
    public Server GetServer(long lnServerId)
    {
        for(int i=0;i< servers.length;i++)
        {
            if(servers[i].EmployeeId==lnServerId)return servers[i];
        }
        return null;
    }
    private void InsertIntoArray(List<Server>list)
    {
        for(int i=0;i<list.size();i++)
        {
            servers[i] = list.get(i);
        }

        //sort the array
        QuickSort(0, servers.length-1);
    }
    public int Update(Server s)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        String strWhereClause = Schema.DataTable_Server.ID_COLUMN + " =? ";
        String[] args = new String[]{s.EmployeeId+""};
        String[] columns = new String[]{Schema.DataTable_Server.SERVER_NAME_COLUMN,
                Schema.DataTable_Server.PHONE_COLUMN,Schema.DataTable_Server.GENDER_FLAG_COLUMN};
        String[] values = new String[]{s.Name,s.PhoneNumber,s.gender.value+""};
       /* ContentValues values = new ContentValues();
        values.put(Schema.DataTable_Server.SERVANT_NAME_COLUMN, s.Name);
        values.put(Schema.DataTable_Server.PHONE_COLUMN, s.PhoneNumber);
        values.put(Schema.DataTable_Server.GENDER_FLAG_COLUMN, s.gender.value);*/
        int result =helper.Update(Schema.DataTable_Server.TABLE_NAME,columns, values, strWhereClause, args);
        //sort the array
        QuickSort(0, servers.length-1);
        return result;
    }
    public int Delete(long serverId)
    {

        long lngtime = System.currentTimeMillis();
        String strWhereClause = Schema.DataTable_Server.ID_COLUMN + "=? and "+Schema.DataTable_Server.ACTIVE_FLAG_COLUMN+"=1";
        String[] args = new String[]{serverId + ""};
        ContentValues cValue = new ContentValues();
        cValue.put(Schema.DataTable_Server.ACTIVE_FLAG_COLUMN, 0);
        cValue.put(Schema.DataTable_Server.INACTIVE_DATE_COLUMN, lngtime);

        int result = new DatabaseHelper(context).DeleteServer(cValue, strWhereClause, args);
        if(result>0) {
            Server[] temp = new Server[servers.length - 1];
            int index = 0;
            for (int i = 0; i < servers.length; i++) {
                if (servers[i].EmployeeId != serverId) {
                    temp[index++] = servers[i];
                }
            }

            servers = temp;
        }

        return result;
    }
    public long Insert(Server s)
    {

        DatabaseHelper helper = new DatabaseHelper(context);




        String[] columns= new String[]{Schema.DataTable_Server.SERVER_NAME_COLUMN,
                Schema.DataTable_Server.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Server.GENDER_FLAG_COLUMN,
                Schema.DataTable_Server.PHONE_COLUMN,
                Schema.DataTable_Server.ID_COLUMN,
                Schema.DataTable_Server.NOTE_COLUMN,
                Schema.DataTable_Server.EMAIL_COLUMN,
                Schema.DataTable_Server.PICTURE_PATH_COLUMN,
                Schema.DataTable_Server.ADDRESS_COLUMN};
        String[] values = new String[]{s.Name,"1",s.gender.value+"",s.PhoneNumber,s.EmployeeId+"",s.Note,s.Email,s.PicturePath,s.Address};

        long id =helper.Insert(Schema.DataTable_Server.TABLE_NAME,columns,values);

        if(id!=-1) {
            Server[] temp = new Server[servers.length + 1];
            boolean blnInserted = false;
            int newArrayIndex = 0;
            for (int i = 0; i < servers.length; i++) {
                if (s.compareTo(servers[i]) < 1 && !blnInserted) {
                    temp[newArrayIndex++] = s;
                    temp[newArrayIndex++] = servers[i];
                    blnInserted = true;
                } else {
                    temp[newArrayIndex++] = servers[i];
                }
            }

            if (!blnInserted) temp[newArrayIndex] = s;
            servers = temp;
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
        Server temp;
        for (int i = start; i < end; i++)
        {
            if (servers[i].compareTo(servers[pivotIndex])<1)
            {
                temp = servers[i];
                servers[i] = servers[startingIndex];
                servers[startingIndex] = temp;
                startingIndex++;
            }
        }

        temp = servers[startingIndex];
        servers[startingIndex] = servers[pivotIndex];
        servers[pivotIndex] = temp;
        return startingIndex;
    }
    public ArrayList<Server> LoadServer()
    {

        ArrayList<Server> servers = new ArrayList<Server>();

        DatabaseHelper helper = new DatabaseHelper(context);

        //column names
        String[] columns = {
                Schema.DataTable_Server.ID_COLUMN,
                Schema.DataTable_Server.SERVER_NAME_COLUMN,
                Schema.DataTable_Server.ACTIVE_FLAG_COLUMN,
                Schema.DataTable_Server.GENDER_FLAG_COLUMN,
                Schema.DataTable_Server.INACTIVE_DATE_COLUMN,
                Schema.DataTable_Server.PHONE_COLUMN,
                Schema.DataTable_Server.NOTE_COLUMN,
                Schema.DataTable_Server.PICTURE_PATH_COLUMN,
                Schema.DataTable_Server.EMAIL_COLUMN,
                Schema.DataTable_Server.ADDRESS_COLUMN
        };
        String strSortOrder = Schema.DataTable_Server.ID_COLUMN + " asc";

        String strWhereClause =Schema.DataTable_Server.ACTIVE_FLAG_COLUMN + "=?";
        String[] args = {"1"};

        Cursor c = helper.query(Schema.DataTable_Server.TABLE_NAME, columns, strWhereClause, args, strSortOrder);
        int columnNameIndex = c.getColumnIndex(Schema.DataTable_Server.SERVER_NAME_COLUMN);
        int columnIdIndex = c.getColumnIndex(Schema.DataTable_Server.ID_COLUMN);
        int columnActiveFlagIndex = c.getColumnIndex(Schema.DataTable_Server.ACTIVE_FLAG_COLUMN);
        int columnGenderIndex = c.getColumnIndex(Schema.DataTable_Server.GENDER_FLAG_COLUMN);
        int columnPhoneIndex = c.getColumnIndex(Schema.DataTable_Server.PHONE_COLUMN);
        int columnEmailIndex = c.getColumnIndex(Schema.DataTable_Server.EMAIL_COLUMN);
        int columnAddressIndex = c.getColumnIndex(Schema.DataTable_Server.ADDRESS_COLUMN);
        int columnPictureIndex = c.getColumnIndex(Schema.DataTable_Server.PICTURE_PATH_COLUMN);
        int columnNoteIndex = c.getColumnIndex(Schema.DataTable_Server.NOTE_COLUMN);
        while(!c.isAfterLast())
        {
            Server s = new Server();
            s.Name = c.getString(columnNameIndex);
            s.EmployeeId = c.getLong(columnIdIndex);
            s.gender = (c.getInt(columnGenderIndex)==0)? Enum.ServerGender.male: Enum.ServerGender.female;
            s.IsActive = (c.getInt(columnActiveFlagIndex)==0)?false:true;
            s.PhoneNumber = c.getString(columnPhoneIndex);
            s.Email = c.getString(columnEmailIndex);
            s.Address = c.getString(columnAddressIndex);
            s.PicturePath = c.getString(columnPictureIndex);
            s.Note = c.getString(columnNoteIndex);
            servers.add(s);

            c.moveToNext();
        }

        c.close();
        helper.close();
        return servers;
    }
}
