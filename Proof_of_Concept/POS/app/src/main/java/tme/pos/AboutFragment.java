package tme.pos;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceFragment;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/20/2015.
 */
public class AboutFragment extends PreferenceFragment {
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    AppSettings myAppSettings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_settings);
        myAppSettings =common.myAppSettings;
        //server message
        Preference p = getPreferenceScreen().getPreference(0);
        if(myAppSettings.GetServerMessageCode().compareTo("-1")==0) {
            p.setSummary(GetDefaultServerMessage());
        }
        else if(myAppSettings.GetServerMessageCode().length()==0)
        {
            p.setSummary(GetDefaultServerMessage());
        }
        else
        {
            p.setSummary(myAppSettings.GetServerMessageTimestamp()+" "+myAppSettings.GetServerMessage());
        }


        //build version
        p = getPreferenceScreen().getPreference(1);
        PackageManager pm  =  getActivity().getPackageManager();
        try {
            PackageInfo pInfo = pm.getPackageInfo(getActivity().getApplication().getPackageName(), 0);
            p.setSummary(pInfo.packageName+"  Version "+pInfo.versionName);
        }
        catch(PackageManager.NameNotFoundException ex)
        {
            p.setSummary("TMe POS  Version 1.0.0 Alpha");
        }


    }
    private String GetDefaultServerMessage()
    {
        return df.format(new Date())+" Welcome to TMe POS, we hope you enjoy using this app. We are trying to build the most convenient system to grow your business.";
    }

}
