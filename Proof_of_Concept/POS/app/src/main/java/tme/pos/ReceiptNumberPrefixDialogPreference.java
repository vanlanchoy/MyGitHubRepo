package tme.pos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;


/**
 * Created by kchoy on 3/16/2015.
 */
public class ReceiptNumberPrefixDialogPreference extends DialogPreference {
    float COMPANY_PROFILE_TEXT_SIZE = 40;
    View currentView;
    AppSettings myAppSettings;
    public ReceiptNumberPrefixDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        myAppSettings = common.myAppSettings;
        LoadApplicationData();
        setDialogLayoutResource(R.layout.layout_dialog_receipt_number_prefix_ui);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

    }
    private void LoadApplicationData()
    {
        COMPANY_PROFILE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_settings_receipt_number_prefix_text_size);
    }
    protected  void SaveValues()
    {
        myAppSettings.SaveReceiptNumberPrefix(((EditText)currentView.findViewById(R.id.txtPrefix)).getText()+"");
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {

        if (DialogInterface.BUTTON_POSITIVE == which) {
            SaveValues();
        }
    }

    @Override
    protected void onBindDialogView(View view) {

        currentView = view;
        SharedPreferences pref = getSharedPreferences();


        TextView tv = (TextView)view.findViewById(R.id.tvLabel);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        tv.setTextColor(getContext().getResources().getColor(R.color.divider_grey));

        EditText et = (EditText)view.findViewById(R.id.txtPrefix);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        String strValue =pref.getString(AppSettings.PREFERENCE_RECEIPT_NUMBER_PREFIX_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));



        super.onBindDialogView(view);
    }
}
