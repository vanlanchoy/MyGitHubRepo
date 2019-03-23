package tme.pos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 9/9/2016.
 */
public class LockScreenUnlockPasswordConfirmationDialog extends Dialog {
    interface ILockScreenUserRespondListerner
    {
        void LockScreen();
    }

    ILockScreenUserRespondListerner listerner;
    TextView txtPassword1,txtPassword2;

    public LockScreenUnlockPasswordConfirmationDialog(Context c, ILockScreenUserRespondListerner l)
    {
        super(c);
        listerner = l;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle("Unlock Password");
        setContentView(R.layout.layout_confirm_unlock_password_dialog);

        txtPassword1 = (TextView)findViewById(R.id.txtPassword1);
        txtPassword2 = (TextView)findViewById(R.id.txtPassword2);

        findViewById(R.id.tvConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //try to hash the password
                if(ValidateInput())
                {
                    String strHashed = common.Utility.HashPassword(Integer.parseInt(common.myAppSettings.GetHashedMethod()),txtPassword1.getText().toString(),getContext());
                    if(strHashed.equalsIgnoreCase(common.myAppSettings.GetHashedPassword()))
                    {
                        if(listerner!=null)listerner.LockScreen();
                        dismiss();
                    }

                }
            }
        });

        findViewById(R.id.tvExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
    private boolean ValidateInput()
    {
        boolean blnPass = true;
        if(txtPassword1.getText().length()<4 ||txtPassword1.getText().length()>12)
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
}
