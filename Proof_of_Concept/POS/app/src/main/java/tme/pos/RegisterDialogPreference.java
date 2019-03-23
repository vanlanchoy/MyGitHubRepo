package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.Random;
import java.security.*;

import org.apache.http.concurrent.Cancellable;

import java.lang.reflect.Field;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.common;
import tme.pos.WebService.TMe_POS_WS;

/**
 * Created by vanlanchoy on 9/7/2016.
 */
public class RegisterDialogPreference extends DialogPreference implements TMe_POS_WS_Receiver.OnTMePOSServerListener {
    TMe_POS_WS_Receiver registrationServerMsgReceiver ;
    IntentFilter registrationIntentFilter;
    Intent intent ;

    TextView tvRegister;
    TextView tvExit;
    EditText txtPassword1;
    EditText txtPassword2;
    EditText txtEmail1;
    EditText txtEmail2;
    public RegisterDialogPreference(Context context, AttributeSet attrs)
    {
        super(context,attrs);
        setDialogLayoutResource(R.layout.layout_dialog_register_ui);
        setPositiveButtonText("Register");
        setNegativeButtonText(android.R.string.cancel);
        registrationIntentFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
        registrationIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intent = new Intent(context, TMe_POS_WS.class);
        //setDialogIcon(null);

    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setCancelable(false);
        builder.setNegativeButton(null, null);
        builder.setPositiveButton(null, null);
        builder.setIcon(null);
    }



    @Override
    protected void onBindDialogView(View view) {

        super.onBindDialogView(view);
        SetTextViewProperties((TextView) view.findViewById(R.id.tvPassword1));
        SetTextViewProperties((TextView) view.findViewById(R.id.tvPassword2));
        SetTextViewProperties((TextView) view.findViewById(R.id.tvEmail1));
        SetTextViewProperties((TextView)view.findViewById(R.id.tvEmail2));


        txtPassword1 = (EditText)view.findViewById(R.id.txtPassword1);
        SetEditTextProperties(txtPassword1);

        txtPassword2 = (EditText)view.findViewById(R.id.txtPassword2);
        SetEditTextProperties(txtPassword2);

        txtEmail1 = (EditText)view.findViewById(R.id.txtEmail1);
        SetEditTextProperties(txtEmail1);

        txtEmail2 = (EditText)view.findViewById(R.id.txtEmail2);
        SetEditTextProperties(txtEmail2);




        txtPassword1.setText(common.myAppSettings.GetLockScreenPassword());
        txtPassword2.setText(common.myAppSettings.GetLockScreenPassword());
        txtEmail1.setText(common.myAppSettings.GetLockScreenPasswordEmail());
        txtEmail2.setText(common.myAppSettings.GetLockScreenPasswordEmail());

        tvExit = (TextView)view.findViewById(R.id.tvExit);
        tvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        tvRegister = (TextView)view.findViewById(R.id.tvRegister);
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


               /* String temp1 = common.Utility.HashPassword(1,"12345678",getContext());
                String temp2 = common.Utility.HashPassword(2,"12345678",getContext());
                String temp3 = common.Utility.HashPassword(3,"12345678",getContext());
                String temp4 = common.Utility.HashPassword(4,"12345678",getContext());
                String temp5 = common.Utility.HashPassword(5,"12345678",getContext());
                String temp6 = common.Utility.HashPassword(6,"12345678",getContext());*/
                if(!ValidateInputFields()) return;



                if(!common.Utility.IsConnectedToNetwork(getContext()))
                {
                    common.Utility.LogActivity("register device but no internet");
                    common.Utility.ShowMessage("Internet","You must fist connect to the internet for this operation",getContext(),R.drawable.no_access);
                    return;
                }


                tvExit.setEnabled(false);
                tvExit.setTextColor(getContext().getResources().getColor(R.color.divider_grey));
                tvRegister.setEnabled(false);
                tvRegister.setTextColor(getContext().getResources().getColor(R.color.divider_grey));

                RegisterDevice();
            }
        });

        //don't allowed to register again if already did
        if(IsRegistered())tvRegister.setEnabled(false);
    }
    private void SetEditTextProperties(EditText txt)
    {

        txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.COMPANY_PROFILE_TEXT_SIZE);
        txt.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
    }
    private void SetTextViewProperties(TextView tv)
    {

        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
    }
    private boolean ValidateInputFields()
    {
        boolean blnPass = true;

        if(txtEmail1.getText().toString().length()<11)
        {
            common.Utility.ShowMessage("Email","Please provide a valid GMail address.",getContext(),R.drawable.no_access);
            blnPass = false;
        }
        else if(!txtEmail1.getText().toString().substring((txtEmail1.getText().length()-10)).equalsIgnoreCase("@gmail.com"))
        {
            common.Utility.ShowMessage("Email","Please provide a valid GMail address.",getContext(),R.drawable.no_access);
            blnPass = false;
        }
        else if(!txtEmail1.getText().toString().equalsIgnoreCase(txtEmail2.getText().toString()))
        {
            common.Utility.ShowMessage("Email","Both GMail addresses didn't match",getContext(),R.drawable.no_access);
            blnPass = false;
        }
        else if(txtPassword1.getText().length()<4 ||txtPassword1.getText().length()>12)
        {
            common.Utility.ShowMessage("Password","Password length must consist between 8 to 12 digits",getContext(),R.drawable.no_access);
            blnPass = false;
        }
        else if(txtPassword1.getText().toString().compareTo(txtPassword2.getText().toString())!=0)
        {
            common.Utility.ShowMessage("Password","Both passwords didn't match.",getContext(),R.drawable.no_access);
            blnPass = false;
        }
        return blnPass;
    }
    private void RegisterDevice()
    {

        registrationServerMsgReceiver = new TMe_POS_WS_Receiver(this);
        getContext().registerReceiver(registrationServerMsgReceiver, registrationIntentFilter);

        //start service
        intent.setAction(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
        intent.putExtra(TMe_POS_WS.HASH_METHOD, (new Random().nextInt(5)+1)+"");
        intent.putExtra(TMe_POS_WS.LOGIN_EMAIL_ADDRESS, txtPassword1.getText().toString());
        intent.putExtra(TMe_POS_WS.PASSWORD, txtPassword1.getText().toString());
        getContext().startService(intent);
    }
    private boolean IsRegistered()
    {
        if(common.myAppSettings.GetHashedPassword().length()>0)return true;
        return false;
    }

    @Override
    public void onNotificationReceive(String strMsg, String strMsgId) {

    }

    @Override
    public void onExpirationDateReceive(String strDate) {

    }

    @Override
    public void onRegisterDevice(String strResult,String strEmail,String strHashMethod) {
        getContext().unregisterReceiver(registrationServerMsgReceiver);
        registrationServerMsgReceiver.abortBroadcast();

        try
        {
            if (Integer.parseInt(strResult) > 0) {
                int intHashedMethod = Integer.parseInt(strHashMethod);

                String strHashed = common.Utility.HashPassword(intHashedMethod,txtPassword1.getText().toString(),getContext());
                //save to preference
                common.myAppSettings.SaveLoginEmail(strEmail);
                common.myAppSettings.SaveHashedPassword(strHashed);
                common.myAppSettings.SaveHashedMethod(strHashMethod);

                setSummary("Registered");
                tvExit.callOnClick();//close the dialog

            }
            else
            {
                common.Utility.ShowMessage("Register","Registration failed, please try again later",getContext(),R.drawable.no_access);

            }
        }
        catch(Exception ex)
        {
            common.Utility.LogActivity("Register device failed.");
        }
        finally {
            tvExit.setEnabled(true);
            tvExit.setTextColor(getContext().getResources().getColor(R.color.black));
            tvRegister.setEnabled(true);
            tvRegister.setTextColor(getContext().getResources().getColor(R.color.black));
        }

    }

    @Override
    public void onUpdateGeoLocation(String RowAffected) {

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);
    }
}
