package tme.pos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 9/7/2015.
 */
public class SendReceiptEmailPreference extends DialogPreference {
    View currentView;
    float COMPANY_PROFILE_TEXT_SIZE = 40;
    AppSettings myAppSettings;
    EditText txtEmailToRecipient1;
    EditText txtEmailToRecipient2;
    public SendReceiptEmailPreference(Context context, AttributeSet attrs){
        super(context, attrs);
        myAppSettings = common.myAppSettings;
        LoadApplicationData();
        setDialogLayoutResource(R.layout.layout_dialog_email_receipt_ui);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }
    private void LoadApplicationData()
    {
        COMPANY_PROFILE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_settings_company_profile_text_size);

    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        boolean blnCloseDialog = false;
        if (DialogInterface.BUTTON_POSITIVE == which) {

            if(txtEmailToRecipient1.getText().toString().length()==0 &&
                    txtEmailToRecipient2.getText().toString().length()==0){

                common.myAppSettings.SaveSendReceiptEmail(txtEmailToRecipient1.getText().toString());
                setSummary("Not Set");
                blnCloseDialog = true;

            }
            else if(txtEmailToRecipient1.getText().toString().length()<11)
            {
                common.Utility.ShowMessage("Email","Please provide a valid Gmail address.",getContext(),R.drawable.no_access);

            }
            else if(!txtEmailToRecipient1.getText().toString().substring((txtEmailToRecipient1.getText().length()-10)).equalsIgnoreCase("@gmail.com"))
            {
                common.Utility.ShowMessage("Email","Please provide a valid Gmail address.",getContext(),R.drawable.no_access);
            }
            else if(!txtEmailToRecipient1.getText().toString().equalsIgnoreCase(txtEmailToRecipient2.getText().toString()))
            {
                common.Utility.ShowMessage("Email","Both Gmail addresses didn't match",getContext(),R.drawable.no_access);

            }
            else if(!common.Utility.IsEmailValid(txtEmailToRecipient1.getText().toString()))
            {
                common.Utility.ShowMessage("Email","Invalid email address.",getContext(),R.drawable.no_access);
            }
            else {

                common.myAppSettings.SaveSendReceiptEmail(txtEmailToRecipient1.getText().toString());
                setSummary("Set");
                blnCloseDialog = true;

            }





        }
        else  if (DialogInterface.BUTTON_NEGATIVE == which) {
            blnCloseDialog = true;
        }

        setSummary((myAppSettings.GetSendReceiptEmail().length()>0)?"Set":"No Set");

        try {

            Field f =dialog.getClass().getSuperclass()
                    .getDeclaredField("mShowing");
            f.setAccessible(true);
            f.set(dialog, blnCloseDialog);//set true to close the dialog
        }
        catch (Exception ex){}
    }

    @Override
    protected void onBindDialogView(View view) {

        currentView = view;





        TextView tvEmail1 = (TextView)view.findViewById(R.id.tvEmail1);
        SetTextViewProperties(tvEmail1);


        txtEmailToRecipient1 = (EditText)view.findViewById(R.id.txtEmailToRecipient1);
        SetEditTextProperties(txtEmailToRecipient1);

        TextView tvEmail2 = (TextView)view.findViewById(R.id.tvEmail2);
        SetTextViewProperties(tvEmail2);


        txtEmailToRecipient2 = (EditText)view.findViewById(R.id.txtEmailToRecipient2);
        SetEditTextProperties(txtEmailToRecipient2);


        txtEmailToRecipient1.setText(myAppSettings.GetSendReceiptEmail());
        txtEmailToRecipient2.setText(myAppSettings.GetSendReceiptEmail());

        super.onBindDialogView(view);
    }
    private void SetEditTextProperties(EditText txt)
    {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family));
        txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        txt.setTypeface(tf, Typeface.NORMAL);
    }
    private void SetTextViewProperties(TextView tv)
    {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(tf, Typeface.NORMAL);
    }
}
