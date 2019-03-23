package tme.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.text.Html;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 3/1/2015.
 */
public class CompanyProfileDialogPreference extends DialogPreference {
    float COMPANY_PROFILE_TEXT_SIZE = 40;
    View currentView;
    AppSettings myAppSettings;
    Context context;
    ImageView imgLogo;
    TextView tvRemovePic;

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        common.companyProfileDialogPreference = null;//remove it
    }

    @Override
    protected View onCreateDialogView() {
        //Add to common
        common.companyProfileDialogPreference = this;
        return super.onCreateDialogView();

    }

    public CompanyProfileDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        myAppSettings = common.myAppSettings;
        LoadApplicationData();
        setDialogLayoutResource(R.layout.layout_dialog_company_profile_ui);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

    }
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getContext().getResources().getDrawable(iconId), getContext().getResources(), 36, 36));
        }
        messageBox.show();
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
        //SharedPreferences pref = getSharedPreferences();


        TextView tv = (TextView)view.findViewById(R.id.tvCompanyName);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        EditText et = (EditText)view.findViewById(R.id.txtCompanyName);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        String strValue =myAppSettings.GetCompanyProfile_CompanyName();// pref.getString(AppSettings.PREFERENCE_COMPANY_NAME_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvStreet);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtStreet);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyAddressStreet();//pref.getString(AppSettings.PREFERENCE_COMPANY_ADDRESS_STREET_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvCity);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtCity);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyAddressCity();//pref.getString(AppSettings.PREFERENCE_COMPANY_ADDRESS_CITY_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvState);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtState);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyAddressState();//pref.getString(AppSettings.PREFERENCE_COMPANY_ADDRESS_STATE_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvZipcode);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtZipcode);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetSaveCompanyProfile_CompanyAddressZipCode();//pref.getString(AppSettings.PREFERENCE_COMPANY_ADDRESS_ZIPCODE_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvPhone);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtPhoneAreaCode);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyPhoneAreaCode();//pref.getString(AppSettings.PREFERENCE_COMPANY_PHONE_AREA_CODE_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvPhoneCloseParenthesis);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtPhoneFirstPart);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyPhoneFirstPart();//pref.getString(AppSettings.PREFERENCE_COMPANY_PHONE_FIRST_PART_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvPhoneDash1);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtPhoneSecondPart);
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyPhoneSecondPart();//pref.getString(AppSettings.PREFERENCE_COMPANY_PHONE_SECOND_PART_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv = (TextView)view.findViewById(R.id.tvEmail);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        et = (EditText)view.findViewById(R.id.txtEmail);
        et.setFilters(common.Utility.CreateMaxLengthFilter(common.text_and_length_settings.EMAIL_ADDRESS_MAX_LENGTH));
        et.setHint(getContext().getString(R.string.hint_server_name).replace("%1$d",
                common.text_and_length_settings.EMAIL_ADDRESS_MAX_LENGTH + ""));
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        et.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);
        strValue =myAppSettings.GetCompanyProfile_CompanyEmail();//pref.getString(AppSettings.PREFERENCE_COMPANY_EMAIL_SETTING_KEY,"");
        et.setText(((strValue.length()>0)?strValue:""));

        tv=(TextView)view.findViewById(R.id.tvLogo);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, COMPANY_PROFILE_TEXT_SIZE);
        tv.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        imgLogo = (ImageView)view.findViewById(R.id.imgLogo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ((Activity)context).startActivityForResult(new Intent(context, FileDirectoryBrowserActivity.class), common.myAppSettings.INTENT_PICK_LOGO);

            }
        });

        tvRemovePic = (TextView)view.findViewById(R.id.tvRemovePic);
        tvRemovePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.myAppSettings.DeleteCompanyProfileLogo();
                //clear the imageview
                imgLogo.setImageBitmap(null);
                imgLogo.setImageResource(R.drawable.photo_not_available);
                tvRemovePic.setVisibility(View.INVISIBLE);
            }
        });
        LoadLogo();
        super.onBindDialogView(view);
    }
    private void LoadLogo()
    {
        String strFilePath = common.myAppSettings.GetCompanyProfileLogoFilePath();
        if(strFilePath.length()>0) {
            imgLogo.setImageBitmap(BitmapFactory.decodeFile(strFilePath));
            tvRemovePic.setVisibility(View.VISIBLE);
        }

    }
    public void SelectedCompanyLogoFile(String strLogo)
    {
        int lastDot = strLogo.lastIndexOf(".");
        if (lastDot>0) {
            String extension = strLogo.substring(strLogo.lastIndexOf(".") + 1);

            if (extension == null) {
                Toast.makeText(context, "File type not supported", Toast.LENGTH_SHORT).show();
            } else if (extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("png")) {
                //imgLogo.setImageURI(Uri.fromFile(new File(strLogo)));
                imgLogo.setImageBitmap(BitmapFactory.decodeFile(strLogo));
                tvRemovePic.setVisibility(View.VISIBLE);
                //delete any existing file
                common.myAppSettings.DeleteCompanyProfileLogo();

                //save to folder
                File srcFile = new File(strLogo);

                File destination = new File(context.getFilesDir(),common.myAppSettings.COMPANY_LOGO_FILENAME+"."+extension);
                try {
                    destination.createNewFile();
                    InputStream in = new FileInputStream(srcFile);
                    OutputStream out = new FileOutputStream(destination);
                    byte[] imgBytes = common.Utility.ReadBytes(in);
                    out.write(imgBytes);
                   /* // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }*/
                    in.close();
                    out.close();

                    //save the by array to the companyProfile object also
                    common.companyProfile.Logo = imgBytes;
                }
                catch(IOException ex)
                {
                    Toast.makeText(context, "Error saving logo file.", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(context, "File type not supported", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(context, "File type not supported", Toast.LENGTH_SHORT).show();
        }
    }
    public void SaveValues()
    {
        if(currentView==null)return;



        EditText et = (EditText)currentView.findViewById(R.id.txtCompanyName);
        myAppSettings.SaveCompanyProfile_CompanyName(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtStreet);
        myAppSettings.SaveCompanyProfile_CompanyAddressStreet(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtCity);
        myAppSettings.SaveCompanyProfile_CompanyAddressCity(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtState);
        myAppSettings.SaveCompanyProfile_CompanyAddressState(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtZipcode);
        myAppSettings.SaveCompanyProfile_CompanyAddressZipCode(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtPhoneAreaCode);
        myAppSettings.SaveCompanyProfile_CompanyPhoneAreaCode(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtPhoneFirstPart);
        myAppSettings.SaveCompanyProfile_CompanyPhoneFirstPart(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtPhoneSecondPart);
        myAppSettings.SaveCompanyProfile_CompanyPhoneSecondPart(et.getText()+"");

        et = (EditText)currentView.findViewById(R.id.txtEmail);
        myAppSettings.SaveCompanyProfile_CompanyEmail(et.getText()+"");


        common.UpdateCompanyProfile();//update the singleton
        getDialog().dismiss();
    }
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state

        } else {
            // Set default state from the XML attribute

        }
    }
    private void LoadApplicationData()
    {

        COMPANY_PROFILE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_settings_company_profile_text_size);
    }
}
