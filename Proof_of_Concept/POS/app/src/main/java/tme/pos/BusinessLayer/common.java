package tme.pos.BusinessLayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.security.*;

import printer.PrinterManager;
import printer.StarMicronics_TSP650II_BTI_Thermal_Printer;
import tme.pos.CompanyProfileDialogPreference;
import tme.pos.CustomListManager;
import tme.pos.R;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * Created by kchoy on 10/14/2014.
 */
public class common {
    public static AppSettings myAppSettings;
    public static CompanyProfile companyProfile;
    public static TextLengthAndSize text_and_length_settings;
    private static StarMicronics_TSP650II_BTI_Thermal_Printer myPrinter;
    public static Control_Events control_events;
    public static ServerList serverList;
    public static SupplierList supplierList;
    public static ReceiptManager receiptManager;
    public static FloorPlan floorPlan;
    public static MyCartManager myCartManager;
    public static MyLocationService myLocationService;
    public static InventoryList inventoryList;
    public static PrinterManager myPrinterManager;
    public static CompanyProfileDialogPreference companyProfileDialogPreference;
    public static MathLib mathLib;
    public static MyMenu myMenu;
    public static CustomListManager customListManager;
    public static MyPromotionManager myPromotionManager;
    public static ServerCommunicator PosCenter;
    private static String strDebugDirectory = Environment.getExternalStorageDirectory().toString();//"/sdcard";//case sensitive!!
    private static ArrayList<Pair<String,String>> ActivityLog = new ArrayList<Pair<String, String>>();
    public static void UpdateCompanyProfile()
    {
        if(companyProfile==null ||myAppSettings==null)return;

        common.companyProfile.City = myAppSettings.GetCompanyProfile_CompanyAddressCity();
        common.companyProfile.CompanyName = myAppSettings.GetCompanyProfile_CompanyName();
        common.companyProfile.Email = myAppSettings.GetCompanyProfile_CompanyEmail();
        common.companyProfile.Phone = myAppSettings.GetCompanyProfile_CompanyPhoneAreaCode()+"-"+
                myAppSettings.GetCompanyProfile_CompanyPhoneFirstPart()+"-"+
                myAppSettings.GetCompanyProfile_CompanyPhoneSecondPart();
        common.companyProfile.Phone =(common.companyProfile.Phone.equalsIgnoreCase("--"))?"":common.companyProfile.Phone;
        common.companyProfile.State = myAppSettings.GetCompanyProfile_CompanyAddressState();
        common.companyProfile.Street = myAppSettings.GetCompanyProfile_CompanyAddressStreet();
        common.companyProfile.Zipcode = myAppSettings.GetSaveCompanyProfile_CompanyAddressZipCode();
        common.companyProfile.Logo = myAppSettings.GetCompanyProfile_Logo();
    }
    public static class Utility{

        public static String ReturnDateTimeString(long time)
        {
        Date d = new Date(time);

            return new SimpleDateFormat("MM-dd-yyyy HH:mm:ss a").format(d);
        }
        public static Calendar GetFirstDayOfWeekDate(Calendar c)
        {
            int firstDay = c.getFirstDayOfWeek();
            while(true) {
                if (c.get(Calendar.DAY_OF_WEEK) == firstDay) {
                    return c;
                } else {
                    c.add(Calendar.DATE, -1);
                }
            }

        }
        public static boolean IsConnectedToNetwork(Context context)
        {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();

            for(NetworkInfo ni :netInfo)
            {
                if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnected())
                        return true;
                if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnected())
                        return true;
            }

            return false;
        }

        public static void LogActivity(String strMsg)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
            if(ActivityLog.size()>99)ActivityLog.remove(0);
            String strDate = dateFormat.format(new Date());
            ActivityLog.add(new Pair<String, String>(strDate,strMsg));
        }
        public static String ReturnActivityLogString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Activity Log:");
            sb.append('\n');
            for(int i =0;i<ActivityLog.size();i++)
            {
                sb.append(ActivityLog.get(i).first +" "+ActivityLog.get(i).second);
                sb.append('\n');
            }

            return sb.toString();
        }
        public static int GetEndIndex(String strRemaining,int maxChar)
        {
            int intEndIndex=-1;
            if(strRemaining.length()>maxChar) {
                intEndIndex = maxChar;
                //check if the current word has been cut off
                if(!strRemaining.substring(intEndIndex,intEndIndex+1).equalsIgnoreCase(" ")
                        && !strRemaining.substring(intEndIndex+1,intEndIndex+2).equalsIgnoreCase(" "))
                {
                    //find the starting index of the cut off word, and insert it to next line
                    int intStartIndex=-1;
                    for(int i=intEndIndex-1;i>=0;i--)
                    {
                        if(strRemaining.substring(i,i+1).equalsIgnoreCase(" "))
                        {
                            intStartIndex=i;
                            break;
                        }
                    }

                    if(intStartIndex>-1)
                    {
                        intEndIndex = intStartIndex;
                    }
                }
            }
            else
            {
                intEndIndex = strRemaining.length();
            }


            return intEndIndex;
        }
        public static String ConvertString2PhoneFormat(String strValue)
        {
            if(strValue.length()==0)return strValue;
            int count=0;
            String strFormatted="(";
            while(count<strValue.length())
            {
                strFormatted+=strValue.substring(count++,count);
                if(count==3)strFormatted+=")";
                else if(count==6)strFormatted+="-";
            }
            return strFormatted;

        }
        public static int CalculateOrderedItem(long itemId)
        {
            int ordered =0;

            for(String strKey:common.myCartManager.Receipts.keySet())
            {
                ArrayList<Receipt> lstReceipt =common.myCartManager.Receipts.get(strKey);
                for(int i=0;i<lstReceipt.size();i++)
                {

                    //skip paid receipt this is because it has been taken into account while calculating sold unit in database
                    if(lstReceipt.get(i).blnHasPaid)continue;

                    MyCart mc = lstReceipt.get(i).myCart;
                    for(int j=0;j<mc.GetItems().size();j++)
                    {
                        if(itemId==mc.GetItems().get(j).item.getID())
                        {
                            ordered+=mc.GetItems().get(j).UnitOrder;
                        }
                    }
                }

            }
            return ordered;
        }


        public static String ReturnDateString(Calendar cal,boolean TwelveHrFormat)
        {

            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            if(TwelveHrFormat)
                df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

            return df.format(cal.getTime());
        }
        public static Drawable ReturnMessageBoxSizeIcon(int iconId)
        {
            return ResizeDrawable(common.myAppSettings.context.getResources().getDrawable(iconId), common.myAppSettings.context.getResources(), 36, 36);
        }
        public static  Drawable ResizeDrawable(Drawable image,Resources res,int width,int height) {
            Bitmap b = ((BitmapDrawable)image).getBitmap();
            Bitmap bitmapResized = Bitmap.createScaledBitmap(b,width, height, false);
            return new BitmapDrawable(res, bitmapResized);
        }
       /* public static void SaveReceiptsObjectIntoJson(Context context)
        {
            if(myCartManager!=null)
            {
                if(myCartManager.Receipts!=null)
                {
                    //myCartManager.RemoveEmptySplitReceipt();
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.serializeSpecialFloatingPointValues();
                    Gson gson = gsonBuilder.setPrettyPrinting().create();
                    //String jsonString = gson.toJson(myCartManager,MyCartManager.class);
                    myAppSettings.SaveMyCartManagerObjectInJsonString(gson.toJson(myCartManager,MyCartManager.class));//jsonString);
                    LogActivity("save cart manager");
                    //WriteToFile(jsonString,myAppSettings.context.getString(R.string.crash_filename),context);

                }
            }
        }*/
        public static String ConvertReceiptToJsonString(Receipt receipt)
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeSpecialFloatingPointValues();
            Gson gson = gsonBuilder.setPrettyPrinting().create();
            return gson.toJson(receipt,Receipt.class);

        }
       /* public static MyCartManager ReloadLastStateFile(Context context)
        {
            //Get the text file
           return LoadCartManager(myAppSettings.context.getString(R.string.crash_filename),context);

        }*/
        public static int GetModifierGroupColor(int groupId)
        {
            if(groupId== Enum.MutualGroupColor.mutual_dark_orange.group)
            {
                return R.color.mutual_dark_orange;

            }
            else if(groupId== Enum.MutualGroupColor.mutual_dark_brown.group)
            {
                return R.color.mutual_dark_brown;

            }
            else if(groupId== Enum.MutualGroupColor.mutual_dark_red.group)
            {
                return R.color.mutual_dark_red;

            }
            else if(groupId== Enum.MutualGroupColor.mutual_dark_indigo.group)
            {
                return R.color.mutual_dark_indigo;

            }
            else if(groupId== Enum.MutualGroupColor.mutual_dark_navy.group)
            {
                return R.color.mutual_dark_navy;

            }
            else
            {
                //white
                return R.color.black;
            }
        }
        public static String HashPassword(int hashMethodIndex,String strPassword,Context context)
        {
            String strHashed="";

            String strMethod="";
            if(Enum.HashMethod.MD5.value==hashMethodIndex)
            {
                strMethod="MD5";
            }
            else if(Enum.HashMethod.SHA1.value==hashMethodIndex)
            {
                strMethod="SHA-1";
            }
            else if(Enum.HashMethod.SHA256.value==hashMethodIndex)
            {
                strMethod="SHA-256";
            }
            else if(Enum.HashMethod.SHA384.value==hashMethodIndex)
            {
                strMethod="SHA-384";
            }
            else
            {
                strMethod = "SHA-512";
            }
            try {
                MessageDigest md = MessageDigest.getInstance(strMethod);
                byte[] thedigest = md.digest(strPassword.getBytes());
                BigInteger bigInt = new BigInteger(1, thedigest);
                strHashed = bigInt.toString();
            }
            catch(NoSuchAlgorithmException ex){
                ShowMessage("Encryption", "Failed to encode password.",context,R.drawable.no_access);
            }

            return strHashed;
        }
        public static Activity FindActivity(Context cont) {
            if (cont == null)
                return null;
            else if (cont instanceof Activity)
                return (Activity)cont;
            else if (cont instanceof ContextWrapper)
                return FindActivity(((ContextWrapper)cont).getBaseContext());

            return null;
        }
       /* public static MyCartManager LoadLastSavedState(Context context)
        {
            //load last saved file
            MyCartManager mc = LoadCartManager(myAppSettings.context.getString(R.string.save_filename),context);
            //delete the saved file after loaded
            DeleteFile(myAppSettings.context.getString(R.string.save_filename),context);
            return mc;

        }*/
       /* public static MyCartManager LoadCartManager()
        {
            MyCartManager mcm;// = new MyCartManager();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            mcm = gson.fromJson(myAppSettings.GetMyCartManagerObjectInJsonString(), MyCartManager.class);

            return mcm;
        }*/
     /*   private static MyCartManager LoadCartManager(String strFilename,Context context)
        {
            //String strDir= strDebugDirectory+myAppSettings.context.getString(R.string.app_directory);
            String strDir = context.getFilesDir().getAbsolutePath()+"/";
            File file = new File(strDir+strFilename);
            MyCartManager mcm = new MyCartManager();
            if(file.exists())
            {
                try {
                    //Read text from file
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                        sb.append('\n');
                    }
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gson = gsonBuilder.create();
                    mcm = gson.fromJson(sb.toString(), MyCartManager.class);
                    br.close();
                } catch (IOException e) {
                    //You'll need to add proper error handling here
                    Log.d("Load saved state ", e.getMessage() + "");
                }
            }
            return mcm;
        }*/
        public static void DeleteFile(String strFilename,Context context)
        {
            //File file = new File(Environment.getExternalStorageDirectory().toString()+myAppSettings.context.getString(R.string.app_directory),strFilename);
            //String strDir= strDebugDirectory+myAppSettings.context.getString(R.string.app_directory);
            String strDir= context.getFilesDir().getAbsolutePath()+"/";
            File file = new File(strDir+strFilename);
            if(file.exists())file.delete();
        }
        /*public static void SaveCartsBeforeExit(Context context)
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.serializeSpecialFloatingPointValues();
            Gson gson = gsonBuilder.setPrettyPrinting().create();
            String jsonString = gson.toJson(myCartManager,MyCartManager.class);
            WriteToFile(jsonString,myAppSettings.context.getString(R.string.save_filename),context);
        }*/
        /*private  static void WriteToFile(String strContent,String strFilename,Context context)
        {
            try
            {

                //String strDir= Environment.getExternalStorageDirectory().toString()+myAppSettings.context.getString(R.string.app_directory);


                String strDir=context.getFilesDir().getAbsolutePath()+"/";// strDebugDirectory+myAppSettings.context.getString(R.string.app_directory);
                //check directory
                File TMePosDir = new File(strDir);
                if(!TMePosDir.exists())TMePosDir.mkdir();

                //delete the existing file if any
                File file = new File(strDir+strFilename);
                if(file.exists())file.delete();

                //write to new file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(strContent.getBytes());
                fos.close();
                file.setReadable(true,false);

            }
            catch(IOException ex)
            {
                String temp = ex.getMessage();
            }

        }*/
        public static void LoadPicture(ImageView imageView, String strPath,Context c)
        {
            if(strPath.isEmpty()) {
                imageView.setBackground(c.getResources().getDrawable(R.drawable.photo_not_available));
            }
            else
            {
                File imgFile = new  File(strPath);

                if(imgFile.exists()){

                    imageView.setBackground(null);
                    imageView.setImageBitmap(DecodeBitmapFile(strPath));




                }
            }
        }
        public static Bitmap DecodeBitmapFile(String strPath)
        {
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            // Calculate inSampleSize
            options.inSampleSize =4;//2;// common.Utility.CalculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            return BitmapFactory.decodeFile(strPath, options);
        }
        public static int GetAtTheMomentItemCount(long itemId)
        {
            int unitAvailable = inventoryList.GetInventoryCount(itemId);
            unitAvailable -= CalculateOrderedItem(itemId);
            return unitAvailable;
        }
        public static Enum.DBOperationResult ConvertGetLockResult2DBOperationResult(Enum.GetLockResult lockResult) {
            //return if didn't get locks
            if(lockResult== Enum.GetLockResult.RecordCountMismatch) {
                common.Utility.LogActivity("Save order to db, receipt record count mismatched");
                return Enum.DBOperationResult.VersionOutOfDate;
            }
            if(lockResult== Enum.GetLockResult.VersionOutOfDate) {
                common.Utility.LogActivity("Save order to db, receipt record version mismatched");
                return Enum.DBOperationResult.VersionOutOfDate;
            }
            if(lockResult== Enum.GetLockResult.TryLater) {
                common.Utility.LogActivity("Save order to db, receipt record in used");
                return Enum.DBOperationResult.TryLater;
            }
            return Enum.DBOperationResult.Success;
        }
        public static void DispatchTouchEvent(View v)
        {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            float x = 0.0f;
            float y = 0.0f;
// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
            int metaState = 0;
            MotionEvent motionEvent = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_UP,
                    x,
                    y,
                    metaState
            );

// Dispatch touch event to view
            v.dispatchTouchEvent(motionEvent);
        }

        public static Receipt CreateNewReceiptObject(String tableId)
        {
            int receiptIndex = myCartManager.GetReceipts(tableId).size();
           return new Receipt(new MyCart(common.myAppSettings.GetTaxPercentage(),tableId,receiptIndex),
                   common.myAppSettings.GetTaxPercentage(),
                   common.companyProfile,
                   "",
                   "",
                   "",
                   "",//Calendar.getInstance().getTimeInMillis()+"",
                   Enum.ReceiptNoteAlignment.left,
                   Enum.ReceiptNoteAlignment.left,
                   0,
                   0,
                   new Server(), false,1);
        }
        public static void FillInReceiptProperties(Receipt receipt,String strTableLabel)//,int receiptSubId)
        {
            receipt.flTaxRate=common.myAppSettings.GetTaxPercentage();
            receipt.companyProfile= common.companyProfile;
            receipt.HeaderNote =common.myAppSettings.GetReceiptHeaderText();
            receipt.FooterNote=common.myAppSettings.GetReceiptFooterText();
            receipt.tableNumber = strTableLabel;
            receipt.HeaderNoteAlignment=common.myAppSettings.GetHeaderNoteCenterAlignment();
            receipt.FooterNoteAlignment= common.myAppSettings.GetFooterNoteCenterAlignment();
            //receipt.splitId = receiptSubId;
            double[] location = new double[2];
            myLocationService.GetLocationPts(location);
            receipt.dbLatitude =location[0];
            receipt.dbLongitude = location[1];
            receipt.receiptDateTime = Calendar.getInstance();
            //will append numeric number during savereceipt()
            //String strReceiptNumber =(common.myAppSettings.GetReceiptNumberPrefix().length()>0)?common.myAppSettings.GetReceiptNumberPrefix()+"-":"";//user defined prefix
            //strReceiptNumber += receipt.receiptDateTime.getTime().getTime();
            //receipt.receiptNumber = strReceiptNumber;
        }
        public static float Pixel2DP(float px,Context context)
        {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float dp =(float)(px/metrics.density);
            return dp;
        }

        public static StarMicronics_TSP650II_BTI_Thermal_Printer PrinterGetInstance(Context c)
        {
            if(myPrinter==null)myPrinter = new StarMicronics_TSP650II_BTI_Thermal_Printer(c,common.myAppSettings.GetPrinterName(),80,null,null);
            return myPrinter;
        }
        public static void DestroyPrinterObject()
        {
            if(myPrinter!=null)
            {
                myPrinter=null;
            }
        }
        public static int DP2Pixel(float dp,Context context)
        {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) (metrics.density * dp + 0.5f);
        }
        public static String ConvertCurrencyFormatToBigDecimalString(String strCurrency)
        {
            if(strCurrency.length()==0)strCurrency="0";
            return strCurrency.replaceAll("[^0-9.-]+","");//.replace("$","").replace(",","");
        }
        public static String ConvertBigDecimalToCurrencyFormat(BigDecimal price)
        {
            //boolean blnRoundUp=false;
            float temp= price.floatValue();
            String strTemp=price.toPlainString();//temp+"";
            int decimalPt = strTemp.indexOf(".");
            if(decimalPt>-1 && strTemp.length()-1-decimalPt>2)
            {
                //check the 3rd decimal pt don't add a penny if is zero
                int lastDigit = Integer.parseInt(strTemp.substring(decimalPt+3,decimalPt+4));
                if(lastDigit>0) {
                    strTemp = strTemp.substring(0, decimalPt + 3);
                    temp = Float.parseFloat(strTemp);

                }
                else
                {
                    temp = Float.parseFloat(strTemp);
                }
            }
            else
            {
                temp = Float.parseFloat(strTemp);
            }


            strTemp = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(temp);
            if(strTemp.contains("("))
            {
                //negative value
                strTemp = strTemp.replace("(","");
                strTemp = strTemp.replace(")","");
                strTemp ="-"+strTemp;
            }


            return strTemp;
        }
        public static boolean IsEmailValid(String strEmail)
        {
            Pattern ptr = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)");
            return ptr.matcher(strEmail).matches();

        }
        public static boolean CheckValueFormatValidForMoney(String strValue)
        {
            boolean blnFlag = true;
            int decimalIndex =strValue.lastIndexOf('.');
            if(decimalIndex+2<strValue.length()-1)
            {
                blnFlag  =false;
            }

            return blnFlag;
        }
        public static String CheckPercentageTextChanged(Editable s,String strPrevious,boolean blnThreeDecimalPoint)
        {
            boolean isNegative=false;
            double denominator = blnThreeDecimalPoint?1000.0:100.0;
            if(s.toString().indexOf("-")>-1)isNegative = true;
            if(!s.toString().equals(strPrevious)) {
                String str = s.toString().replaceAll("[$,.%]", "");

                if (str.length() == 0) {
                    str = "0";
                } else if (str.equals("-")) {
                    str = "0";
                }
                double s1 = Double.parseDouble(str);
                NumberFormat nf =NumberFormat.getPercentInstance();
                nf.setMinimumFractionDigits(blnThreeDecimalPoint?3:2);
                String strFormatted = nf.format((s1 / denominator));//up to 00.00%
                double s2= ((s1 / denominator));
                if(s2>100)
                {

                    strFormatted = nf.format((0 / denominator));
                }

                if(isNegative && s1>0){strFormatted="-"+strFormatted;}
                s.replace(0, s.length(), strFormatted);

                strPrevious = strFormatted.replaceAll("[$,%-]", "");
            }
            return strPrevious;
        }
        public static int CalculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }
        public static InputFilter[] CreateMaxLengthFilter(int length)
        {
            InputFilter[] filters = new InputFilter[1];
            filters[0] = new InputFilter.LengthFilter(length);
            return filters;
        }
        private static String GenericMoneyTextChanged(Editable s,String strPrevious,int limit)
        {
            boolean isNegative=false;
            if(s.toString().indexOf("-")>-1)isNegative = true;
            if(!s.toString().equals(strPrevious)) {
                String str = s.toString().replaceAll("[$%,.-]", "");

                if (str.length() == 0) {
                    str = "0";
                } else if (str.equals("-")) {
                    str = "0";
                }
                double s1 = Double.parseDouble(str);

                String strFormatted = NumberFormat.getCurrencyInstance().format((s1 / 100));
                double s2= ((s1 / 100.0));
                if(s2>limit)
                {

                    strFormatted = NumberFormat.getCurrencyInstance().format((0 / 100));
                }


                if(isNegative && s1>0){strFormatted="-"+strFormatted;}
                s.replace(0, s.length(), strFormatted);

                strPrevious = strFormatted;
            }
            return strPrevious;
        }
        public static String CheckPaymentMoneyTextChanged(Editable s,String strPrevious)
        {
            return GenericMoneyTextChanged(s,strPrevious,1200000);
        }
        public static String CheckMoneyTextChanged(Editable s,String strPrevious)
        {
            return GenericMoneyTextChanged(s,strPrevious,1000000);
        }
        public static String ReformatPhoneString(String strInput)
        {
            String strFormattedPhoneNumber="(";
            if(strInput.length()==10) {
                for (int i = 0; i < strInput.length(); i++) {
                    strFormattedPhoneNumber += strInput.toCharArray()[i];
                    if (i == 2) strFormattedPhoneNumber += ")";
                    else if (i == 5) strFormattedPhoneNumber += "-";
                }
            }
            else if(strInput.length()==11)
            {
                for (int i = 0; i < strInput.length(); i++) {
                    strFormattedPhoneNumber += strInput.toCharArray()[i];
                    if (i == 2) strFormattedPhoneNumber += ")";
                    else if (i == 6) strFormattedPhoneNumber += "-";
                }
            }
            else{strFormattedPhoneNumber=strInput;}

            return strFormattedPhoneNumber;
        }

        public static void ShowMessage(String strTitle,String strMsg,Context context,int iconId)
        {
            if(context==null)context = myAppSettings.context;
            if(context==null)return;
            AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
            messageBox.setTitle(strTitle);
            messageBox.setMessage(Html.fromHtml(strMsg));
            messageBox.setCancelable(false);
            messageBox.setNeutralButton("OK", null);
            if(iconId>-1)
            {
                messageBox.setIcon(common.Utility.ResizeDrawable(context.getResources().getDrawable(iconId),context.getResources(),36,36));
            }
            messageBox.show();
        }
        public static byte[] ReadBytes(InputStream inputStream) {
            // this dynamically extends to take the bytes you read
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

            // this is storage overwritten on each iteration with bytes
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            // we need to know how may bytes were read to write them to the byteBuffer
            int len = 0;
            try {
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
            }
            catch(Exception ex){}
            // and then we can return your byte array.
            return byteBuffer.toByteArray();
        }
    }


}
