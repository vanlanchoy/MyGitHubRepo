package tme.pos;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 8/30/2015.
 */
public class LockScreenDialogPreference extends DialogPreference {
    View currentView;
    //float COMPANY_PROFILE_TEXT_SIZE = 40;
    AppSettings myAppSettings;
    EditText txtPassword1;
    EditText txtPassword2;
    EditText txtEmail1;
    EditText txtEmail2;

    public LockScreenDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        myAppSettings = common.myAppSettings;
        LoadApplicationData();
        setDialogLayoutResource(R.layout.layout_dialog_lock_screen_ui);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);



        setDialogIcon(null);

    }
    private void LoadApplicationData()
    {
        //COMPANY_PROFILE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_settings_company_profile_text_size);

    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        boolean blnCloseDialog = false;
        if (DialogInterface.BUTTON_POSITIVE == which) {


            if(txtEmail1.getText().toString().length()<11)
            {
                common.Utility.ShowMessage("Email","Please provide a valid Gmail address.",getContext(),R.drawable.no_access);

            }
            else if(!txtEmail1.getText().toString().substring((txtEmail1.getText().length()-10)).equalsIgnoreCase("@gmail.com"))
            {
                common.Utility.ShowMessage("Email","Please provide a valid Gmail address.",getContext(),R.drawable.no_access);
            }
            else if(!txtEmail1.getText().toString().equalsIgnoreCase(txtEmail2.getText().toString()))
            {
                common.Utility.ShowMessage("Email","Both Gmail addresses didn't match",getContext(),R.drawable.no_access);

            }
            else if(txtPassword1.getText().length()!=4)
            {
                common.Utility.ShowMessage("Password","Password length must consist of four digits",getContext(),R.drawable.no_access);

            }
            else if(txtPassword1.getText().toString().compareTo(txtPassword2.getText().toString())!=0)
            {
                common.Utility.ShowMessage("Password","Both passwords didn't match.",getContext(),R.drawable.no_access);

            }
            else {
                common.myAppSettings.SaveLockScreenPassword(txtPassword1.getText().toString());
                common.myAppSettings.SaveLockScreenPasswordEmail(txtEmail1.getText().toString());
                //common.myAppSettings.SaveLockScreenEmailAccPass(txtEmailAccPass1.getText().toString());
                blnCloseDialog = true;

            }




        }
        else  if (DialogInterface.BUTTON_NEGATIVE == which) {
            blnCloseDialog = true;
        }

        setSummary((myAppSettings.GetLockScreenPasswordEmail().length()>0&&
                myAppSettings.GetLockScreenPassword().length()>0)?"Set":"No Set");

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
        //SharedPreferences pref = getSharedPreferences();
        //Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family));


        TextView tvPassword1 = (TextView)view.findViewById(R.id.tvPassword1);
        SetTextViewProperties((TextView) view.findViewById(R.id.tvPassword1));


        txtPassword1 = (EditText)view.findViewById(R.id.txtPassword1);
        SetEditTextProperties(txtPassword1);

        TextView tvPassword2 = (TextView)view.findViewById(R.id.tvPassword2);
        SetTextViewProperties((TextView) view.findViewById(R.id.tvPassword2));

        txtPassword2 = (EditText)view.findViewById(R.id.txtPassword2);
        SetEditTextProperties(txtPassword2);


        TextView tvEmail1 = (TextView)view.findViewById(R.id.tvEmail1);
        SetTextViewProperties((TextView) view.findViewById(R.id.tvEmail1));


        txtEmail1 = (EditText)view.findViewById(R.id.txtEmail1);
        SetEditTextProperties(txtEmail1);

        TextView tvEmail2 = (TextView)view.findViewById(R.id.tvEmail2);
        SetTextViewProperties((TextView) view.findViewById(R.id.tvEmail2));


        txtEmail2 = (EditText)view.findViewById(R.id.txtEmail2);
        SetEditTextProperties(txtEmail2);



        /*SetTextViewProperties((TextView) view.findViewById(R.id.tvEmailAccPass1));

        txtEmailAccPass1 = (EditText) view.findViewById(R.id.txtEmailAccPass1);
        SetEditTextProperties(txtEmailAccPass1);


        SetTextViewProperties((TextView) view.findViewById(R.id.tvEmailAccPass2));


        txtEmailAccPass2 = (EditText) view.findViewById(R.id.txtEmailAccPass2);
        SetEditTextProperties(txtEmailAccPass2);*/


        txtPassword1.setText(myAppSettings.GetLockScreenPassword());
        txtPassword2.setText(myAppSettings.GetLockScreenPassword());
        txtEmail1.setText(myAppSettings.GetLockScreenPasswordEmail());
        txtEmail2.setText(myAppSettings.GetLockScreenPasswordEmail());
        //txtEmailAccPass1.setText(myAppSettings.GetLockScreenEmailAccPass());
        //txtEmailAccPass2.setText(myAppSettings.GetLockScreenEmailAccPass());
        super.onBindDialogView(view);
    }
    private void SetEditTextProperties(EditText txt)
    {

        txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.COMPANY_PROFILE_TEXT_SIZE);
        txt.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
    }
    private void SetTextViewProperties(TextView tv)
    {
        //Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
    }
}
