package tme.pos;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import printer.StarMicronics_TSP650II_BTI_Thermal_Printer;
import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/18/2015.
 */
public class PrinterFragment extends PreferenceFragment  implements SharedPreferences.OnSharedPreferenceChangeListener{
    String strPrinterDefaultName="<Not Set>";
    String strDefaultPrinterModel="Unknown";
    String strDefaultPrinterFirmware="Unknown";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.printer_settings);

        PreferenceScreen ps = getPreferenceScreen();


        //setup printer name field
        EditTextPreference etp = (EditTextPreference)ps.getPreference(0);
        etp.setKey(AppSettings.PREFERENCE_PRINTER_NAME_SETTING_KEY);
        etp.setTitle("Printer Name: "+GetPrinterName());
        etp.setText(GetPrinterName());

        //get printer model
        Preference printerModel = ps.getPreference(1);
        printerModel.setSummary(GetPrinterModel());


        //get printer model
        Preference printerFirmware = ps.getPreference(2);
        printerFirmware.setSummary(GetPrinterFirmwareVersion());
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(AppSettings.PREFERENCE_PRINTER_NAME_SETTING_KEY)) {

            //call to recreate printer object
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    RecreatePrinterObject();
                }
            });

            Preference pref = findPreference(key);
            pref.setTitle("Printer Name: "+GetPrinterName());


        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }
    private void RecreatePrinterObject()
    {
        //set the printer object to null
        common.Utility.DestroyPrinterObject();
        //call get instance to recreate the object
        common.Utility.PrinterGetInstance(getActivity());

        //update the printer detail
        PreferenceScreen ps = getPreferenceScreen();

        //get printer model
        Preference printerModel = ps.getPreference(1);
        printerModel.setSummary(GetPrinterModel());


        //get printer model
        Preference printerFirmware = ps.getPreference(2);
        printerFirmware.setSummary(GetPrinterFirmwareVersion());
    }
    private String GetPrinterName()
    {
        String strName =getPreferenceManager().getSharedPreferences().getString(AppSettings.PREFERENCE_PRINTER_NAME_SETTING_KEY, strPrinterDefaultName);
        return (strName.trim().length()>0)?strName:strPrinterDefaultName;
    }

    private String GetPrinterModel()
    {
        StarMicronics_TSP650II_BTI_Thermal_Printer printer =common.Utility.PrinterGetInstance(getActivity());
        if(printer==null)return strDefaultPrinterModel;
        return printer.GetPrinterModel();
    }
    private String GetPrinterFirmwareVersion()
    {
        StarMicronics_TSP650II_BTI_Thermal_Printer printer =common.Utility.PrinterGetInstance(getActivity());
        if(printer==null)return strDefaultPrinterFirmware;
        return printer.GetFirmwareVersion();
    }
   /* private String GetPrinterPin()
    {
        String strPin =getPreferenceManager().getSharedPreferences().getString(AppSettings.PREFERENCE_PRINTER_PIN_SETTING_KEY, "");
        return (strPin.trim().length()>0)?strPin:"";
    }*/
    /*private boolean GetPrinterMode()
    {
        return getPreferenceManager().getSharedPreferences().getBoolean(AppSettings.PREFERENCE_PRINTER_PAIRING_MODE_SETTING_KEY,false);
    }*/
}