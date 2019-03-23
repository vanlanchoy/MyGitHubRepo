package tme.pos;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 3/21/2015.
 */
public class AppPaymentDialogPreference extends DialogPreference {
    float COMPANY_PROFILE_TEXT_SIZE = 40;
    View currentView;
    AppSettings myAppSettings;
    public AppPaymentDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        myAppSettings = common.myAppSettings;
        LoadApplicationData();
        setDialogLayoutResource(R.layout.layout_app_payment_ui);
        setTitle("Paypal");
        setPositiveButtonText(null);
        setNegativeButtonText("Cancel");

        setDialogIcon(null);

    }
    private String MakePayment()
    {
        String strPaymentId="";
        String strUsername = ((EditText)currentView.findViewById(R.id.txtUsername)).getText()+"";
        String strPassword = ((EditText)currentView.findViewById(R.id.txtPassword)).getText()+"";
        //check to see whether need to save info
        if(myAppSettings.GetToSavePaypalInfoFlag())
        {
            myAppSettings.SavePaypalUsername(strUsername);
            myAppSettings.SavePaypalPassword(strPassword);
        }
        else
        {
            myAppSettings.SavePaypalUsername("");
            myAppSettings.SavePaypalPassword("");
        }

        return strPaymentId;
    }
    @Override
    protected void onBindDialogView(View view) {

        currentView = view;
        boolean blnSavePaypalCredential = myAppSettings.GetToSavePaypalInfoFlag();


        final TextView tvSubmit = (TextView)view.findViewById(R.id.tvSubmit);
        tvSubmit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch(motionEvent.getAction())
                {
                    case MotionEvent.ACTION_UP:
                        ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.light_green));
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.top_category_item_lost_focus_grey));
                        break;
                    case MotionEvent.ACTION_DOWN:
                        ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.white));
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.green));
                        MakePayment();
                        break;
                }

                return false;
            }
        });
        CheckBox chkAgree = (CheckBox)view.findViewById(R.id.chkAgree);
        chkAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                tvSubmit.setEnabled(b);
                if(b)
                {
                    tvSubmit.setTextColor(getContext().getResources().getColor(R.color.light_green));
                    tvSubmit.setBackgroundColor(getContext().getResources().getColor(R.color.top_category_item_lost_focus_grey));
                }
                else
                {
                    tvSubmit.setTextColor(getContext().getResources().getColor(R.color.white));
                    tvSubmit.setBackgroundColor(getContext().getResources().getColor(R.color.top_category_item_lost_focus_grey));
                }
            }
        });

        CheckBox chkSaveInfo = (CheckBox)view.findViewById(R.id.chkSaveInfo);
        chkSaveInfo.setChecked(blnSavePaypalCredential);
        chkSaveInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                myAppSettings.ToSavePaypalInfo(b);

            }
        });
        if(blnSavePaypalCredential)
        {
            //load saved paypal info
            ((EditText)currentView.findViewById(R.id.txtUsername)).setText(myAppSettings.GetPaypalUsername());
            ((EditText)currentView.findViewById(R.id.txtPassword)).setText(myAppSettings.GetPaypalPassword());
        }
    }
    private void LoadApplicationData()
    {

        COMPANY_PROFILE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_settings_company_profile_text_size);
    }
}
