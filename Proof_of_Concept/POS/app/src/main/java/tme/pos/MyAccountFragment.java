package tme.pos;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
/**
 * Created by vanlanchoy on 8/30/2015.
 */
public class MyAccountFragment extends PreferenceFragment {
    AppSettings myAppSettings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.my_account_settings);
        myAppSettings = common.myAppSettings;
        //device serial
        Preference p = getPreferenceScreen().getPreference(0);
        p.setSummary(Build.SERIAL);

        p = getPreferenceScreen().getPreference(1);
        p.setSummary((myAppSettings.GetDeviceRegisterId().length()>0)?myAppSettings.GetLoginEmail():"unknown");

        /*p = getPreferenceScreen().getPreference(2);
        p.setSummary((myAppSettings.GetLockScreenPassword().length()>0 &&
        myAppSettings.GetLockScreenPasswordEmail().length()>0)?"Set":"Not set");*/

        p = getPreferenceScreen().getPreference(2);
        p.setSummary((myAppSettings.GetSendReceiptEmail().length()>0)?"Set":"Not set");



    }


}
