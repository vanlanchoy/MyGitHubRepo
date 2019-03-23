package tme.pos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 3/15/2015.
 */
public class SettingsPreferenceActivity extends PreferenceActivity {

    //AppSettings myAppSettings;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if(resultCode==RESULT_OK) {

                if(common.myAppSettings.INTENT_PICK_LOGO==requestCode) {


                    String strFilePath =data.getExtras().getString(AppSettings.EXTRA_LOGO_PATH);
                    if(strFilePath!=null && common.companyProfileDialogPreference!=null )
                    common.companyProfileDialogPreference.SelectedCompanyLogoFile( strFilePath);
                }
            else
                {
                    Toast.makeText(this,"intent pick logo "+common.myAppSettings.INTENT_PICK_LOGO+", request code "+requestCode,
                            Toast.LENGTH_LONG).show();
                }


        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((POS_Application)getApplication()).setCurrentActivity(this);
        //myAppSettings = common.myAppSettings;
        super.onCreate(savedInstanceState);
        ArrayList<Header>items = new ArrayList<Header>();

        Header header;
        //receipt settings header
        header = new Header();
        header.summary = "Setup your receipt in here.";
        header.title="Receipt";
        header.fragment="tme.pos.ReceiptFragment";
        header.id=0;
        items.add(header);

       /* //pos printer settings header
        header = new Header();
        header.summary = "Status: ";
        header.title="Printer";
        header.fragment="tme.pos.PrinterFragment";
        header.id=1;
        items.add(header);*/

        /*//application status
        header = new Header();
        String strExpDate = common.myAppSettings.GetExpirationDate();
        String strSummary="We couldn't recognize your device.";
        if(strExpDate.length()>0)
        {
            try {

                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
                Date currentDate = new Date();//sdf.format(new Date());
                Date expDate = new SimpleDateFormat("M/d/yyyy").parse(strExpDate);
                strSummary="This application is expiring on "+new SimpleDateFormat("EEE MMM d yyyy").format(expDate)+" ";
                long diff = expDate.getTime()-currentDate.getTime();// expDate.compareTo(currentDate);
                long dayDiff =  diff/(60l*60l*24l*1000l);
                if(dayDiff<0)
                {
                    strSummary+="(Your application has expired)";
                }
                else if(dayDiff<2)
                {
                    strSummary+="("+dayDiff+" day left)";
                }
                else{
                   *//* strSummary+="("+dayDiff+" days left)"+",  expDate is ["+expDate.getTime()+
                    "], currentDate is ["+currentDate.getTime()+"]";*//*
                }
            }
            catch (ParseException ex){}

        }
        header.summary =strSummary;
        header.title="Expiration";
        header.fragment="tme.pos.AppPaymentFragment";
        header.id=2;
        items.add(header);*/

        //my account
        header = new Header();
        header.summary ="Your POS account Info";
        header.title="My Account";
        header.fragment="tme.pos.MyAccountFragment";
        header.id=1;
        items.add(header);

        //about
        header = new Header();
        header.title="About POS";
        header.summary="";
        header.fragment="tme.pos.AboutFragment";
        header.id=2;
        items.add(header);

        setListAdapter(new CustomPreferenceHeaderListItemAdapter(this, R.layout.layout_preference_header_ui, items));


    }

    @Override
    protected  void onResume()
    {
        Log.d("setting activity Info", "onBuildHeaders");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);
    }

    @Override
    public void onBuildHeaders(List<PreferenceActivity.Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.headers_preference, target);
    }
    @Override
    protected boolean isValidFragment (String fragmentName) {
        if(ReceiptFragment.class.getName().equals(fragmentName)) return true;
        //if(PrinterFragment.class.getName().equals(fragmentName)) return true;
        //if(AppPaymentFragment.class.getName().equals(fragmentName)) return true;
        if(AboutFragment.class.getName().equals(fragmentName)) return true;
        if(MyAccountFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

}
