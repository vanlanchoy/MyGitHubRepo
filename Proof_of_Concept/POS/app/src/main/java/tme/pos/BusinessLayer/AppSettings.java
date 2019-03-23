package tme.pos.BusinessLayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import tme.pos.MainUIActivity;
import tme.pos.R;


/**
 * Created by kchoy on 10/9/2014.
 */
public class AppSettings{
    public static String DEVICE_UNIQUE_ID = Build.SERIAL.length()==0?java.util.UUID.randomUUID().toString():Build.SERIAL;
    public static String DEFAULT_SHARED_PREFERENCES_FILE_NAME="tme.pos_preferences";
    public static String APPLICATION_CRASHED_FLAG="tme_pos_app_crashed";
    public static String APPLICATION_MYCARTMANAGER_OBJECT_JSONSTRING="tme_mycart_in_json_string";
    //public static String PREFERENCE_LEFT_HANDED_SETTING_KEY="tme_pos_app_pref_right_handed";
    public static String PREFERENCE_TAX_RATE_SETTING_KEY="tme_pos_app_pref_tax_rate";
    public static String PREFERENCE_CATEGORY_ORDER_KEY="tme_pos_app_pref_category_order";
    public static String TAG_ADD_NEW_MENU_ITEM_FRAGMENT="TagAddNewMenuItemFragment";
    public static String TAG_CHECKOUT_PANEL_FRAGMENT="TagCheckoutPanelFragment";
    public static String PREFERENCE_COMPANY_NAME_SETTING_KEY="tme_pos_app_pref_company_name";
    public static String PREFERENCE_COMPANY_ADDRESS_STREET_SETTING_KEY="tme_pos_app_pref_company_address_street";
    public static String PREFERENCE_COMPANY_ADDRESS_CITY_SETTING_KEY="tme_pos_app_pref_company_address_city";
    public static String PREFERENCE_COMPANY_ADDRESS_STATE_SETTING_KEY="tme_pos_app_pref_company_address_state";
    public static String PREFERENCE_COMPANY_ADDRESS_ZIPCODE_SETTING_KEY="tme_pos_app_pref_company_address_zipcode";
    public static String PREFERENCE_COMPANY_PHONE_AREA_CODE_SETTING_KEY="tme_pos_app_pref_company_phone_area_code";
    public static String PREFERENCE_COMPANY_PHONE_FIRST_PART_SETTING_KEY="tme_pos_app_pref_company_phone_first_part";
    public static String PREFERENCE_COMPANY_PHONE_SECOND_PART_SETTING_KEY="tme_pos_app_pref_company_phone_second_part";
    public static String PREFERENCE_COMPANY_EMAIL_SETTING_KEY="tme_pos_app_pref_company_email";
    public static String PREFERENCE_RECEIPT_NUMBER_PREFIX_SETTING_KEY="tme_pos_app_pref_receipt_prefix";
    public static String PREFERENCE_RECEIPT_HEADER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY = "tme_pos_app_pref_receipt_header_note_text_center_alignment";
    public static String PREFERENCE_RECEIPT_FOOTER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY = "tme_pos_app_pref_receipt_footer_note_text_center_alignment";
    public static String PREFERENCE_PRINTER_NAME_SETTING_KEY = "tme_pos_app_pref_printer_name";
    public static String PREFERENCE_RECEIPT_BARCODE_SETTING_KEY = "tme_pos_app_pref_receipt_barcode";


    public static String SERVER_MESSAGE_SETTING_KEY="tme_pos_app_pref_server_message";
    public static String SERVER_MESSAGE_TIMESTAMP_SETTING_KEY="tme_pos_app_pref_server_message_timestamp";
    public static String SERVER_MESSAGE_CODE_SETTING_KEY="tme_pos_app_pref_server_message_code";
    public static String EXPIRATION_DATE_KEY="tme_pos_app_expiration_date";
    public static String EXPIRATION_DATE_ERROR_CODE_KEY="tme_pos_app_expiration_error_code";
    public static String REGISTERED_DEVICE_ID_KEY="tme_pos_app_registered_device_id";
    //public static String REGISTERED_DEVICE_ID_2_KEY="tme_pos_app_registered_device_id_2";
    public static String APP_INITIAL_RUN_DATE="tme_pos_app_initial_run_date";
    public static String HASH_METHOD="tme_pos_app_hash_method";
    public static String LOGIN_EMAIL="tme_pos_app_login_email_address";
    public static String HASHED_PASS ="tme_pos_app_hashed_password";

    public static String RECEIPT_HEADER_TEXT_KEY="tme_pos_receipt_header_text";
    public static String RECEIPT_FOOTER_TEXT_KEY="tme_pos_receipt_footer_text";
    public static String QR_CODE_URL_TEXT_KEY="tme_pos_qr_code_url_text";

    /**PAYPAL**/
    public static String PAYPAL_USERNAME_KEY="tme_pos_paypal_username";
    public static String PAYPAL_PASSWORD_KEY="tme_pos_paypal_password";
    public static String SAVE_PAYPAL_CREDENTIAL_KEY="tme_pos_save_paypal_credential";

    /**LOCK SCREEN**/
    public static String LOCK_SCREEN_PASSWORD_KEY="tme_pos_lock_screen_pass";
    public static String LOCK_SCREEN_FORGOT_PASSWORD_EMAIL_KEY="tme_pos_lock_screen_email";
    public static String APP_IS_LOCKED_KEY="tme_pos_app_is_locked";


    /**floor plan background image**/
    public static String FLOOR_PLAN_BACKGROUND_PICTURE_KEY="tme_pos_floor_plan_background_pic_key";

    /**key store password**/
    //debug
    public static String KEYSTORE_PASSWORD="android";
    public static String KEYSTORE_SHA1="d7:49:e4:18:1a:4f:0a:4e:ae:02:a3:af:0c:27:45:2e:7b:05:4c:2f";
    public static String NMBOR_CLIENT_ID="847860680830-ptcmpcf3cvqf347hbo2b47pjuhqaoqk8.apps.googleusercontent.com";
    public static String Scope ="audience:server:client_id:9414861317621.apps.googleusercontent.com";

    /**send email receipt to customer**/
    public static String SEND_RECEIPT_EMAIL="tme_pos_send_receipt_email";

    /**Bluetooth related variables**/
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String PRINTER_BRAND="printer_brand";
    public static String PRINTER_MODEL="printer_model";
    public static String PRINTER_PAPER_WIDTH="printer_paper_width";
    public static UUID MY_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//default UUID for all bluetooth

    /**select company logo variable**/
    public static String EXTRA_LOGO_PATH = "logo_path";
    public static String COMPANY_LOGO_FILENAME="CompanyLogo";

    /**receipt queue**/
    public static String EXTRA_DELETED_RECEIPT_INDEX = "receipt_index";
    public static String EXTRA_RECEIPT_QUEUE = "receipt_queue";

    /**intent value**/
    public final static int LAUNCH_WINDOW_ACTIVITY_ID=0;
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int  INTENT_PICK_LOGO=3;
    public static final int  INTENT_RECEIPT_QUEUE=4;

    /**File export folder location**/
    public static final String FILE_EXPORT_PUBLIC_LOCATION="/sdcard/TMePOS/";
    public String FILE_EXPORT_PRIVATE_LOCATION;
    public String FILE_EXPORT_EXTERNAL_STORAGE_LOCATION;

    /**limitation**/
    public static float AMOUNT_LIMIT=1000000;
    public static int UNIT_LIMIT=999;
    public static int TOTAL_UNIT_LIMIT=9999;
    public static int ORDER_ITEM_PER_LOAD=50;
    /**DB record lock duration**/
    public static long LOCK_RECORD_DURATION=60000;
    /**receipt number id column value**/
    public static int RECEIPT_NUM_ID_COLUMN_VALUE = 1;

    /**messages**/
    public static String MESSAGE_APPLICATION_BUSY_TITLE="Busy";
    public static String MESSAGE_APPLICATION_BUSY="Application is currently busy, please try again later.";

    /**initial time**/
    public static long INITIAL_RECEIPT_TIME=0;

    Context context;
    public enum PanelDesign{
        Left_Handed,Right_Handed
    }

    public AppSettings(Context c)
    {
        context = c;
        FILE_EXPORT_PRIVATE_LOCATION=context.getFilesDir().getAbsolutePath()+"/working_folder/";
        FILE_EXPORT_EXTERNAL_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getAbsolutePath()+"/TMePOS/";
        INITIAL_RECEIPT_TIME = new GregorianCalendar(2016,1,1).getTimeInMillis();
    }
    public String GetHashedPassword(){return GetPreference(HASHED_PASS);}
    public void SaveHashedPassword(String strValue){SavePreference(HASHED_PASS,strValue);}
    public String GetHashedMethod(){return GetPreference(HASH_METHOD);}
    public void SaveHashedMethod(String strValue){SavePreference(HASH_METHOD,strValue);}
    public String GetLoginEmail(){return GetPreference(LOGIN_EMAIL);}
    public void SaveLoginEmail(String strValue){SavePreference(LOGIN_EMAIL,strValue);}
    public String GetSendReceiptEmail(){return GetPreference(SEND_RECEIPT_EMAIL);}
    public void SaveSendReceiptEmail(String strValue){SavePreference(SEND_RECEIPT_EMAIL,strValue);}
    public String GetFloorPlanBackgroundPic(){return GetPreference(FLOOR_PLAN_BACKGROUND_PICTURE_KEY);}
    public void SetFloorPlanBackgroundPic(String strValue){SavePreference(FLOOR_PLAN_BACKGROUND_PICTURE_KEY,strValue);}
    public boolean GetAppIsLockedFlag(){return GetBooleanPreference(APP_IS_LOCKED_KEY);}
    public void SetAppIsLockedFlag(boolean blnValue){SaveBooleanPreference(APP_IS_LOCKED_KEY, blnValue);}

    public String GetLockScreenPassword(){return GetPreference(LOCK_SCREEN_PASSWORD_KEY);}
    public void SaveLockScreenPassword(String strValue){SavePreference(LOCK_SCREEN_PASSWORD_KEY, strValue);}
    public String GetLockScreenPasswordEmail(){return GetPreference(LOCK_SCREEN_FORGOT_PASSWORD_EMAIL_KEY);}
    public void SaveLockScreenPasswordEmail(String strValue){SavePreference(LOCK_SCREEN_FORGOT_PASSWORD_EMAIL_KEY,strValue);}
    public String GetReceiptHeaderText(){return GetPreference(RECEIPT_HEADER_TEXT_KEY);}
    public String GetReceiptFooterText(){return GetPreference(RECEIPT_FOOTER_TEXT_KEY);}
    public String GetReceiptQRCodeUrlText(){return GetPreference(QR_CODE_URL_TEXT_KEY);}
    public String GetPaypalUsername()
    {
        return GetPreference(PAYPAL_USERNAME_KEY);
    }
    public String GetPaypalPassword()
    {
        return GetPreference(PAYPAL_PASSWORD_KEY);
    }
    public void SavePaypalUsername(String strUsername)
    {
        SavePreference(PAYPAL_USERNAME_KEY,strUsername);
    }
    public void SavePaypalPassword(String strPassword)
    {
        SavePreference(PAYPAL_PASSWORD_KEY,strPassword);
    }
    public boolean GetPrintBarcodeFlag()
    {
        return GetBooleanPreference(PREFERENCE_RECEIPT_BARCODE_SETTING_KEY);
    }
    public boolean GetToSavePaypalInfoFlag()
    {
        return GetBooleanPreference(SAVE_PAYPAL_CREDENTIAL_KEY);
    }
    public void ToSavePaypalInfo(boolean flag)
    {
        SaveBooleanPreference(SAVE_PAYPAL_CREDENTIAL_KEY, flag);
    }
    public boolean GetApplicationCrashFlag()
    {
        //reset the flag after reading

        boolean flag = GetBooleanPreference(APPLICATION_CRASHED_FLAG);
        SetApplicationCrashFlag(false);
        return flag;
    }
    public void SetApplicationCrashFlag(boolean flag)
    {
        SaveBooleanPreference(APPLICATION_CRASHED_FLAG, flag);
    }
    private void SaveBooleanPreference(String strKey,boolean flag)
    {
        SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(strKey,flag);
        editor.commit();
    }
    private boolean GetBooleanPreference(String strKey)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(strKey, false);
    }
    public boolean SwipeLeftToDelete()
    {

        boolean blnRightHanded = true;//GetBooleanPreference(PREFERENCE_LEFT_HANDED_SETTING_KEY);//sp.getBoolean(PREFERENCE_LEFT_HANDED_SETTING_KEY, true);
        return !blnRightHanded;

    }
    public float GetTaxPercentage()
    {

        String strTaxRate = GetPreference(PREFERENCE_TAX_RATE_SETTING_KEY);
        strTaxRate = (strTaxRate.length()>0)?strTaxRate:"0.08753";
        return Float.parseFloat(strTaxRate);

    }
    public  void SavePercentage(String strTaxRate)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor= sp.edit();
        editor.putString(PREFERENCE_TAX_RATE_SETTING_KEY,strTaxRate);
        editor.commit();
    }
    public void SaveCategoryOrder(String strOrder)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor =  sp.edit();
        editor.putString(PREFERENCE_CATEGORY_ORDER_KEY,strOrder);
        editor.commit();
    }
    public void AppendCategoryOrder(String strNewKey)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String strOrder = sp.getString(PREFERENCE_CATEGORY_ORDER_KEY, "");
        if(strOrder.length()>0)
        {
            strOrder +=","+strNewKey;
        }
        else
        {
            strOrder = strNewKey;
        }
        SharedPreferences.Editor editor =  sp.edit();
        editor.putString(PREFERENCE_CATEGORY_ORDER_KEY, strOrder);
        editor.commit();
    }
    public void SaveMyCartManagerObjectInJsonString(String strJson){
        //common.Utility.ShowMessage("save","save receipt",context,R.drawable.message);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor =  sp.edit();
        editor.putString(APPLICATION_MYCARTMANAGER_OBJECT_JSONSTRING,strJson);
        editor.commit();
    }
    public String GetMyCartManagerObjectInJsonString(){return GetPreference(APPLICATION_MYCARTMANAGER_OBJECT_JSONSTRING);}
    public String GetPrinterName()
    {
        return GetPreference(PREFERENCE_PRINTER_NAME_SETTING_KEY);
    }
    public String GetCategoryOrder()
    {

        return GetPreference(PREFERENCE_CATEGORY_ORDER_KEY);
    }
    public String GetDeviceRegisterId()
    {
        return GetPreference(REGISTERED_DEVICE_ID_KEY);
    }
    public void SaveDeviceRegisterId(String strId){SavePreference(REGISTERED_DEVICE_ID_KEY,strId);}
    /*public String GetDeviceRegisterId2()
    {
        return GetPreference(REGISTERED_DEVICE_ID_2_KEY);
    }*/

    public String GetServerMessageTimestamp()
    {
        return GetPreference(SERVER_MESSAGE_TIMESTAMP_SETTING_KEY);
    }
    public String GetServerMessageCode()
    {

        return GetPreference(SERVER_MESSAGE_CODE_SETTING_KEY);
    }
    public String GetServerMessage()
    {

        return GetPreference(SERVER_MESSAGE_SETTING_KEY);
    }
    public String GetReceiptNumberPrefix()
    {
        return GetPreference(PREFERENCE_RECEIPT_NUMBER_PREFIX_SETTING_KEY);
    }
    public void SaveReceiptNumberPrefix(String strPrefix)
    {
        SavePreference(PREFERENCE_RECEIPT_NUMBER_PREFIX_SETTING_KEY,strPrefix);
    }
    public byte[] GetCompanyProfile_Logo()
    {
        //byte[] logoBytes=new byte[0];
        InputStream in =null;

        String strFilePath=GetCompanyProfileLogoFilePath();
        if(strFilePath.length()>0)
        {
            File existingLogo = new File(strFilePath);
            if (existingLogo.exists())
            {
                return ReadFileSource(existingLogo);
            }
        }


        return null;
    }
    private byte[] ReadFileSource(File file)
    {
        byte[] logoBytes=new byte[0];
        InputStream in =null;
        try
        {
            in =new FileInputStream(file);
            logoBytes = common.Utility.ReadBytes(in);
            in.close();
        }
        catch(IOException ex)
        {
            common.Utility.ShowMessage("Load Logo",ex.getMessage(),context,R.drawable.exclaimation);
        }
        return logoBytes;
    }
    public String GetCompanyProfileLogoFilePath()
    {
        String strFilePath ="";
        //jpeg
        File exitingLogo = new File(context.getFilesDir(),common.myAppSettings.COMPANY_LOGO_FILENAME+".jpeg");
        if(exitingLogo.exists()) {strFilePath= exitingLogo.getAbsolutePath();}
        //jpg
        exitingLogo = new File(context.getFilesDir(),common.myAppSettings.COMPANY_LOGO_FILENAME+".jpg");
        if(exitingLogo.exists()) {strFilePath= exitingLogo.getAbsolutePath();}
        //png
        exitingLogo = new File(context.getFilesDir(),common.myAppSettings.COMPANY_LOGO_FILENAME+".png");
        if(exitingLogo.exists()) {strFilePath= exitingLogo.getAbsolutePath();}


        return strFilePath;
    }
    public void DeleteCompanyProfileLogo()
    {
        String strFilePath = GetCompanyProfileLogoFilePath();
        if(strFilePath.length()>0)
        {
            File exitingLogo = new File(strFilePath);
            if(exitingLogo.exists()){
                exitingLogo.delete();
                common.companyProfile.Logo=null;//reset
            }
        }

    }
    public String GetCompanyProfile_CompanyName()
    {
        return GetPreference(PREFERENCE_COMPANY_NAME_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyName(String strName)
    {
        SavePreference(PREFERENCE_COMPANY_NAME_SETTING_KEY,strName);

    }
    public String GetCompanyProfile_CompanyAddressStreet()
    {
        return GetPreference(PREFERENCE_COMPANY_ADDRESS_STREET_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyAddressStreet(String strAddress)
    {
        SavePreference(PREFERENCE_COMPANY_ADDRESS_STREET_SETTING_KEY,strAddress);

    }
    public String GetCompanyProfile_CompanyAddressCity()
    {
        return GetPreference(PREFERENCE_COMPANY_ADDRESS_CITY_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyAddressCity(String strCity)
    {
        SavePreference(PREFERENCE_COMPANY_ADDRESS_CITY_SETTING_KEY,strCity);

    }
    public String GetCompanyProfile_CompanyAddressState()
    {
        return GetPreference(PREFERENCE_COMPANY_ADDRESS_STATE_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyAddressState(String strState)
    {
        SavePreference(PREFERENCE_COMPANY_ADDRESS_STATE_SETTING_KEY,strState);

    }
    public String GetSaveCompanyProfile_CompanyAddressZipCode()
    {
        return GetPreference(PREFERENCE_COMPANY_ADDRESS_ZIPCODE_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyAddressZipCode(String strZip)
    {
        SavePreference(PREFERENCE_COMPANY_ADDRESS_ZIPCODE_SETTING_KEY,strZip);

    }
    public String GetCompanyProfile_CompanyPhoneAreaCode()
    {
        return GetPreference(PREFERENCE_COMPANY_PHONE_AREA_CODE_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyPhoneAreaCode(String strCode)
    {
        SavePreference(PREFERENCE_COMPANY_PHONE_AREA_CODE_SETTING_KEY,strCode);

    }
    public String GetCompanyProfile_CompanyPhoneFirstPart()
    {
        return GetPreference(PREFERENCE_COMPANY_PHONE_FIRST_PART_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyPhoneFirstPart(String strFirst)
    {
        SavePreference(PREFERENCE_COMPANY_PHONE_FIRST_PART_SETTING_KEY,strFirst);

    }
    public String GetCompanyProfile_CompanyPhoneSecondPart()
    {
        return GetPreference(PREFERENCE_COMPANY_PHONE_SECOND_PART_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyPhoneSecondPart(String strSecond)
    {
        SavePreference(PREFERENCE_COMPANY_PHONE_SECOND_PART_SETTING_KEY,strSecond);

    }
    public String GetCompanyProfile_CompanyEmail()
    {
        return GetPreference(PREFERENCE_COMPANY_EMAIL_SETTING_KEY);
    }
    public void SaveCompanyProfile_CompanyEmail(String strEmail)
    {

        SavePreference(PREFERENCE_COMPANY_EMAIL_SETTING_KEY,strEmail);

    }
    public void SaveServerMessage(String strMessage,String strMsgCode)
    {
        SavePreference(SERVER_MESSAGE_SETTING_KEY,strMessage);
        SavePreference(SERVER_MESSAGE_CODE_SETTING_KEY,strMsgCode);

        DateFormat df = new SimpleDateFormat("dd/mm/yyyy HH:mm:ss");
        SavePreference(SERVER_MESSAGE_TIMESTAMP_SETTING_KEY, df.format(new Date()));
    }
    /*public void SaveDeviceRegisteredIds(String strId1,String strId2)
    {
        SavePreference(REGISTERED_DEVICE_ID_1_KEY,strId1);
        SavePreference(REGISTERED_DEVICE_ID_2_KEY,strId2);
    }*/
    public void SaveExpirationDate(String strDate)
    {
        SavePreference(EXPIRATION_DATE_KEY,strDate);

    }
    public String GetExpirationDate()
    {
        return GetPreference(EXPIRATION_DATE_KEY);
    }
    public void SaveAppInitialRunDate(String strDate)
    {
        SavePreference(APP_INITIAL_RUN_DATE,strDate);

    }
    public String GetAppInitialRunDate()
    {
        return GetPreference(APP_INITIAL_RUN_DATE);
    }
    private void SavePreference(String strKey,String strContent)
    {
        SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(strKey,strContent);
        editor.commit();
    }
    private String GetPreference(String strKey)
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String strMsg = sp.getString(strKey, "");
        return strMsg;
    }
    public  Enum.ReceiptNoteAlignment GetHeaderNoteCenterAlignment()
    {
        if(context.getSharedPreferences(common.myAppSettings.DEFAULT_SHARED_PREFERENCES_FILE_NAME,0).getBoolean(AppSettings.PREFERENCE_RECEIPT_HEADER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY,false))
        {
            return Enum.ReceiptNoteAlignment.center;
        }
        return Enum.ReceiptNoteAlignment.left;

    }
    public Enum.ReceiptNoteAlignment GetFooterNoteCenterAlignment()
    {
        if(context.getSharedPreferences(common.myAppSettings.DEFAULT_SHARED_PREFERENCES_FILE_NAME,0).getBoolean(AppSettings.PREFERENCE_RECEIPT_FOOTER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY,false))
        {
            return Enum.ReceiptNoteAlignment.center;
        }
        return Enum.ReceiptNoteAlignment.left;

    }
}
