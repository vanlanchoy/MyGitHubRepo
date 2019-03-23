package tme.pos.BusinessLayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.DataAccessLayer.Schema;
import tme.pos.R;

/**
 * Created by kchoy on 2/1/2016.
 */
public class BackupManager {
    Context context;
    //String strPrivateTempFolder;
    String strTempZipFilename="temp.zip";
    String strTempGunzipFilename =strTempZipFilename+".gzip";
    static int TOTAL_TABLE_FILE=11;
    int TOTAL_FILE_TO_PROCESS=TOTAL_TABLE_FILE;
    int PROCESSED_FILE_COUNT=0;
    public interface IProgressListener{
        void Progress(float percentage);
    }
    IProgressListener listener;
    public BackupManager(Context context,IProgressListener l)
    {
        this.context = context;
        listener = l;
        CalculateTotalFileToProcess();
        //strPrivateTempFolder = context.getFilesDir().getAbsolutePath()+"/working_folder/";
    }
    private void CalculateTotalFileToProcess()
    {
        TOTAL_FILE_TO_PROCESS= TOTAL_TABLE_FILE*2;//to export to csv and zip

        //get item profile picture count
        File f = new File(context.getFilesDir().getAbsolutePath());
        TOTAL_FILE_TO_PROCESS +=f.listFiles().length;
    }
    private String[] GetSchemaTableColumn(String strTableName)
    {
        ArrayList<String> columns = new ArrayList<String>();
        strTableName = strTableName.toLowerCase();
        if(strTableName.compareTo(Schema.DataTable_PaymentType.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_PaymentType.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Server.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Server.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Receipt.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Receipt.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Category.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Category.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Item.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Item.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Modifier.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Modifier.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Inventory.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Inventory.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_Supplier.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_Supplier.GetColumnNames();
        }
        if(strTableName.compareTo(Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME.toLowerCase())==0)
        {
            return Schema.DataTable_ItemAndModifierUpdateLog.GetColumnNames();
        }
        return (String[])columns.toArray();
    }
    private boolean CompareDataTableAndFileColumns(String strTableName,String strFilePath)
    {
        //load schema table column into array
        String[] tableColumns = GetSchemaTableColumn(strTableName);

        try {
            File file = new File(strFilePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

           if((line = br.readLine().toLowerCase()) != null) {
                //we just need the 1st line
                String fileColumns[] = line.split(",");//assume user input comma will be encoded in [comma]
                boolean blnFound;
                for(int i=0;i<fileColumns.length;i++)
                {
                    blnFound = false;
                    String currentFileColumn =(fileColumns[i].substring(0,fileColumns[i].length()-1)).substring(1);
                    for(int j=tableColumns.length-1;i>=0;j--)
                    {
                        String currentTableColumn =tableColumns[j];
                        if(currentFileColumn.equalsIgnoreCase(currentTableColumn))
                        {
                            blnFound = true;
                            break;
                        }
                    }
                    if(blnFound==false)return false;
                }

            }
            else
            {
               return false;
            }
            br.close();
        }
        catch (IOException e) {
            common.Utility.LogActivity(e.getMessage());
            common.Utility.ShowMessage("Restore Database",e.getMessage(),context,R.drawable.exclaimation);
            return false;
        }
        return true;
    }
    public void RestoreDatabase(ArrayList<Pair<String,String>> pairs)
    {

        ArrayList<Pair<String,ArrayList<ContentValues>>> sqlTasks = new ArrayList<Pair<String, ArrayList<ContentValues>>>();
        for(int i=0;i<pairs.size();i++)
        {
            String strTableName = pairs.get(i).first;
            String strFilePath = pairs.get(i).second;

            common.Utility.LogActivity("Restoring data table " + strTableName + " with file " + strFilePath);


            if(CompareDataTableAndFileColumns(strTableName,strFilePath)) {

                //String[] columns = Schema.GetTableColumns(strTableName);
                ContentValues params;// = new ContentValues();
                try {
                    File file = new File(strFilePath);
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    int index = 0;
                    String[] headers=null;
                    ArrayList<ContentValues> sqlValues = new ArrayList<ContentValues>();
                    while((line = br.readLine()) != null) {

                        if(index==0)//column headers
                        {
                            headers = line.split(",");
                            index++;
                        }
                        else
                        {
                            String[] values = line.split(",");
                            params = new ContentValues();
                            for(int j=0;j<values.length;j++) {
                                params.put((headers[j].substring(0, headers[j].length() - 1)).substring(1), (values[j].substring(0, values[j].length() - 1)).substring(1));

                            }
                            sqlValues.add(params);
                        }
                    }
                    sqlTasks.add(new Pair<String, ArrayList<ContentValues>>(strTableName,sqlValues));
                    br.close();
                }
                catch (Exception ex)
                {
                    common.Utility.LogActivity("Failed to read file");
                    return;
                }
            }
            else
            {
                common.Utility.LogActivity("Restoring failed column unmatched, table "+strTableName+", file path "+strFilePath);
                return;

            }


        }

        //update everything in one shot
        new DatabaseHelper(context).RestoreDatabase(sqlTasks, true);

    }

    public void CleanUpTempFiles()
    {
        File dir = new File(common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION);
        //byte[] buffer = new byte[4096];
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = files.length-1;i>=0 ;i--)
            {
                File file = files[i];
                if(!file.getName().equalsIgnoreCase(strTempGunzipFilename)){file.delete();}

            }
        }
    }
    public void UnzipFile(String strPath,String strFilename)throws Exception
    {
        UnGunZip(strPath,strFilename);

        //wait for a second to let the files be ready
        Thread.sleep(1000);
        try{
            InputStream is = new FileInputStream(strPath+strFilename.replace(".gzip",""));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry mZipEntry;
            byte[] buffer = new byte[1024];
            int count;

            while ((mZipEntry = zis.getNextEntry()) != null) {
                // zapis do souboru
                String strTempFilename = mZipEntry.getName();



                FileOutputStream fout = new FileOutputStream(strPath+ strTempFilename);


                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();

            }

            zis.close();


        }
        catch(IOException ex)
        {
            common.Utility.LogActivity("Unzip file "+ex.getMessage());
            throw ex;
        }

    }
    private void UnGunZip(String strPath,String strGunZipFilename)throws Exception
    {
        try {
            FileInputStream fis = new FileInputStream(strPath+"/"+strGunZipFilename);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(strPath+"/"+strGunZipFilename.replace(".gzip",""));//.replace(".zip","new.zip"));
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }
            //close resources
            fos.close();
            gis.close();
        } catch (Exception ex) {
            common.Utility.LogActivity("UnGunzip file "+ex.getMessage());
            throw ex;
        }
    }
    public boolean GunzipFile(String strPath)
    {
        try {
            FileInputStream fis = new FileInputStream(strPath);
            FileOutputStream fos = new FileOutputStream(strPath+".gzip");
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            File f = new File(strPath);
            long size =f.length();

            byte[] buffer = new byte[4096];
            int len;
            while((len=fis.read(buffer)) != -1){
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fos.close();
            fis.close();
            return true;
        } catch (Exception ex) {
            common.Utility.LogActivity("Gunzip file " + ex.getMessage());
            return false;
        }
    }
    private boolean IsZipFileReadyToBeGunzip(String strZipPath)
    {
        common.Utility.LogActivity("waiting for created zip file to be ready to perform gun zip operation");
        try {
            File f = new File(strZipPath);
            long size =f.length();
            Thread.sleep(200);
            while(size!=f.length()){Thread.sleep(1000);}
            if(!GunzipFile(strZipPath))return false;

            CleanUpTempFiles();
            return true;
        }
        catch (Exception ex)
        {
            common.Utility.LogActivity(ex.getMessage());

            return false;
        }
    }
    private boolean ZipFolderFiles(ZipOutputStream zos,String strDir,String strSkipped)
    {
        try {
            File dir = new File(strDir);
            byte[] buffer = new byte[4096];
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; ++i) {
                    //update listener
                    CallBackToListener(1);
                    File file = files[i];
                    if (file.isDirectory()) {
                        //do nothing if is a dir
                    } else {
                        //zip the file,but skipping the currently creating zip file
                        if (!file.getName().equalsIgnoreCase(strSkipped)) {

                            ZipEntry ze = new ZipEntry(file.getName());
                            zos.putNextEntry(ze);

                            //ByteArrayOutputStream ous = new ByteArrayOutputStream();
                            InputStream ios = new FileInputStream(file);
                            int read;
                            while ((read = ios.read(buffer)) != -1) {

                                //write content into zip file
                                zos.write(buffer, 0, read);
                            }


                            //close after writing
                            ios.close();


                        }
                    }
                }
            }
            else {
                common.Utility.LogActivity("Zip dir " + strDir + " does not existed");
            }
        }
        catch (Exception ex)
        {
            common.Utility.LogActivity(ex.getMessage());
            //common.Utility.ShowMessage("Zip file",ex.getMessage(),context,R.drawable.exclaimation);
            return false;
        }
        return true;
    }
    public boolean ZipFiles(String strDir,String strFileName) throws Exception
    {
        ZipOutputStream zos=null;
        final String strZipPath = strDir+strFileName;
        common.Utility.LogActivity("zip files dir " + strDir + ", zip filename is " + strFileName);
        //byte[] buffer = new byte[4096];
        try {
            OutputStream os = new FileOutputStream(strZipPath);
            zos = new ZipOutputStream(new BufferedOutputStream(os));
            if(!ZipFolderFiles(zos,strDir,strFileName))return false;


            //now zip the item picture dir
            if(!ZipFolderFiles(zos,context.getFilesDir().getAbsolutePath(),""))return false;



            zos.closeEntry();

            /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run(){

                  IsZipFileReadyToBeGunzip(strZipPath);

                }
                }, 1000);*/
           return  IsZipFileReadyToBeGunzip(strZipPath);
        }
        catch(Exception ex)
        {
            common.Utility.LogActivity(ex.getMessage());
            return false;

        }
        finally {
            if(zos!=null)zos.close();
        }
    }
    private boolean ExportTableRecords(String strTableName,String strFilename)
    {
        DatabaseHelper helper = new DatabaseHelper(context);
        Cursor c = null;
        c = helper.GetAllRecords(strTableName);
        boolean blnSuccess=false;
        //update listener
        CallBackToListener(1);

        try
        {
            StringBuilder sb = new StringBuilder();

            //get all column names
            for(String strName:c.getColumnNames())
            {

                sb.append("\""+strName+"\",");
            }
            c.moveToFirst();
            while(!c.isAfterLast()) {

                sb.append(System.getProperty("line.separator"));
                for(int i=0;i<c.getColumnCount();i++)
                {
                    String strValue =c.getString(i);

                    if(strValue==null)strValue="";
                    strValue=strValue.replace(",","[comma]");
                    sb.append("\""+strValue+"\",");
                    //sb.append("\""+c.getString(i)+"\",");
                }
                c.moveToNext();
            }

            //check directory
            //File TMePosDir = new File(common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION);//common.myAppSettings.FILE_EXPORT_PUBLIC_LOCATION);
            File TMePosDir = new File(common.myAppSettings.FILE_EXPORT_PUBLIC_LOCATION);
            //File TMePosDir = new File(common.myAppSettings.FILE_EXPORT_EXTERNAL_STORAGE_LOCATION);
            if(!TMePosDir.exists())TMePosDir.mkdir();

            //delete the existing file if any
            //File csvFile = new File(common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION,strFilename);// common.myAppSettings.FILE_EXPORT_PUBLIC_LOCATION,strFilename);
            File csvFile = new File(common.myAppSettings.FILE_EXPORT_PUBLIC_LOCATION,strFilename);
            //File csvFile = new File(common.myAppSettings.FILE_EXPORT_EXTERNAL_STORAGE_LOCATION,strFilename);
            if(csvFile.exists())csvFile.delete();

            //write to new file
            FileOutputStream fos = new FileOutputStream(csvFile);
            fos.write(sb.toString().getBytes());
            fos.close();
            csvFile.setReadable(true, false);
            blnSuccess = true;

        }
        catch(IOException ex)
        {
            //new ServerCommunicator(context).SendError(ex);
            common.Utility.LogActivity(ex.getMessage());
            //common.Utility.ShowMessage("Export file","Error exporting data.",context,R.drawable.exclaimation);
            blnSuccess=false;
        }
        finally {
            if(c!=null)
                c.close();

            helper.close();
            return blnSuccess;
        }
    }

    public boolean BeginBackup()
    {
        common.Utility.LogActivity("begin backup");
        boolean blnSuccess=false;
        PROCESSED_FILE_COUNT=0;
        /**do a clean up before that**/
        File dir = new File(common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION);
        //File dir = new File(common.myAppSettings.FILE_EXPORT_PUBLIC_LOCATION);
        if(dir.exists())
        {
            for(File f : dir.listFiles()){
                f.delete();
            }

        }
        /**export all table to csv**/
        if(ExportAllTableDataToCSV())
        {
            try {

                ZipFiles(common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION,strTempZipFilename);
                //ZipFiles(common.myAppSettings.FILE_EXPORT_PUBLIC_LOCATION,strTempZipFilename);

                blnSuccess=true;
            }
            catch (Exception ex)
            {
                common.Utility.LogActivity(ex.getMessage());
                blnSuccess= false;
            }

        }
        else
        {
            blnSuccess= false;
        }

        return blnSuccess;
    }
    private void CallBackToListener(int processedFile)
    {
        if(listener!=null) {
            PROCESSED_FILE_COUNT+=processedFile;
            final float percentage = (float)PROCESSED_FILE_COUNT / TOTAL_FILE_TO_PROCESS;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    listener.Progress(percentage*100);
                }
            });

        }
    }
    private boolean ExportAllTableDataToCSV()
    {
        common.Utility.LogActivity("exporting payment type table");
        if(!ExportPaymentTypeRecords())return false;
        common.Utility.LogActivity("exporting category records table");
        if(!ExportCategoryRecords())return false;
        common.Utility.LogActivity("exporting payment type table");
        if(!ExportItemRecords())return false;
        common.Utility.LogActivity("exporting modifiers table");
        if(!ExportModifierRecords())return false;
        common.Utility.LogActivity("exporting server table");
        if(!ExportServerRecords())return false;
        common.Utility.LogActivity("exporting receipt table");
        if(!ExportReceiptRecords())return false;
        common.Utility.LogActivity("exporting supplier table");
        if(!ExportSupplierRecords())return false;
        common.Utility.LogActivity("exporting inventory table");
        if(!ExportInventoryRecords())return false;
        common.Utility.LogActivity("exporting item and modifier change log table");
        if(!ExportItemModifierUpdateRecords())return false;
        common.Utility.LogActivity("exporting promotion table");
        if(!ExportPromotionRecords())return false;
        common.Utility.LogActivity("exporting promotion change log table");
        if(!ExportPromotionChangeLogRecords())return false;
        common.Utility.LogActivity("exporting custom list table");
        if(!ExportCustomListRecords())return false;

        return true;
    }
    public boolean ExportItemModifierUpdateRecords()
    {
        return ExportTableRecords(Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME, Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+".csv");
    }
    public boolean ExportReceiptCountDataRecord() {
        return ExportTableRecords(Schema.DataTable_ReceiptCountData.TABLE_NAME,Schema.DataTable_ReceiptCountData.TABLE_NAME+".csv");
    }
    public boolean ExportOrdersRecord()
    {
        return ExportTableRecords(Schema.DataTable_Orders.TABLE_NAME,Schema.DataTable_Orders.TABLE_NAME+".csv");
    }
    public boolean ExportPromotionChangeLogRecords()
    {
        return ExportTableRecords(Schema.DataTable_PromotionUpdateLog.TABLE_NAME,Schema.DataTable_PromotionUpdateLog.TABLE_NAME+".csv");
    }
    public boolean ExportReceiptRecords()
    {
        return ExportTableRecords(Schema.DataTable_Receipt.TABLE_NAME,Schema.DataTable_Receipt.TABLE_NAME+".csv");
    }
    public boolean ExportCategoryRecords()
    {
        return ExportTableRecords(Schema.DataTable_Category.TABLE_NAME, Schema.DataTable_Category.TABLE_NAME + ".csv");
    }
    public boolean ExportItemRecords()
    {
        return ExportTableRecords(Schema.DataTable_Item.TABLE_NAME,Schema.DataTable_Item.TABLE_NAME+".csv");
    }
    public boolean ExportModifierRecords()
    {
        return ExportTableRecords(Schema.DataTable_Modifier.TABLE_NAME, Schema.DataTable_Modifier.TABLE_NAME + ".csv");
    }
    public boolean ExportPaymentTypeRecords()
    {
        return ExportTableRecords(Schema.DataTable_PaymentType.TABLE_NAME,Schema.DataTable_PaymentType.TABLE_NAME+".csv");
    }
    public boolean ExportServerRecords()
    {
        return ExportTableRecords(Schema.DataTable_Server.TABLE_NAME,Schema.DataTable_Server.TABLE_NAME+".csv");
    }
    public boolean ExportSupplierRecords()
    {
        return ExportTableRecords(Schema.DataTable_Supplier.TABLE_NAME, Schema.DataTable_Supplier.TABLE_NAME + ".csv");
    }
    public boolean ExportInventoryRecords()
    {
        return ExportTableRecords(Schema.DataTable_Inventory.TABLE_NAME,Schema.DataTable_Inventory.TABLE_NAME+".csv");
    }
    public boolean ExportPromotionRecords()
    {
        return ExportTableRecords(Schema.DataTable_Promotion.TABLE_NAME,Schema.DataTable_Promotion.TABLE_NAME+".csv");
    }
    public boolean ExportCustomListRecords()
    {
        return ExportTableRecords(Schema.DataTable_CustomList.TABLE_NAME,Schema.DataTable_CustomList.TABLE_NAME+".csv");
    }

}
