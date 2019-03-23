package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.widget.Toast;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 3/15/2015.
 */
public class ReceiptFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.receipt_settings);
        PreferenceScreen ps = getPreferenceScreen();

        //receipt barcode switch mode
        SwitchPreference swt = (SwitchPreference)ps.getPreference(1);
        swt.setKey(AppSettings.PREFERENCE_RECEIPT_BARCODE_SETTING_KEY);
        swt.setChecked(GetBarcodeSetting());

        //header alignment switch mode
        swt = (SwitchPreference)ps.getPreference(3);
        swt.setKey(AppSettings.PREFERENCE_RECEIPT_HEADER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY);
        swt.setChecked(GetHeaderNoteCenterAlignment());


        //footer alignment switch mode
        swt = (SwitchPreference)ps.getPreference(5);
        swt.setKey(AppSettings.PREFERENCE_RECEIPT_FOOTER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY);
        swt.setChecked(GetFooterNoteCenterAlignment());
    }
  /*  public void SelectedCompanyLogoFile(String strLogoFile)
    {
        CompanyProfileDialogPreference profile = (CompanyProfileDialogPreference)getPreferenceScreen().getPreference(5);
        profile.SelectedCompanyLogoFile(strLogoFile);
    }*/
    protected  void ShowMessage(String strTitle,String strMessage,Context context,int iconId)
    {
        AlertDialog.Builder da =new AlertDialog.Builder(context);
                da.setTitle(strTitle);
                da.setMessage(strMessage);

                da.setIcon(android.R.drawable.ic_dialog_alert);
        if(iconId>-1)
        {
            da.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
                da.show();

    }
    private boolean GetHeaderNoteCenterAlignment()
    {

        return getPreferenceManager().getSharedPreferences().getBoolean(AppSettings.PREFERENCE_RECEIPT_HEADER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY,false);

    }
    private boolean GetFooterNoteCenterAlignment()
    {
        return getPreferenceManager().getSharedPreferences().getBoolean(AppSettings.PREFERENCE_RECEIPT_FOOTER_NOTE_TEXT_CENTER_ALIGNMENT_SETTING_KEY,false);

    }
    private boolean GetBarcodeSetting()
    {
        return getPreferenceManager().getSharedPreferences().getBoolean(AppSettings.PREFERENCE_RECEIPT_BARCODE_SETTING_KEY,false);
    }
}
