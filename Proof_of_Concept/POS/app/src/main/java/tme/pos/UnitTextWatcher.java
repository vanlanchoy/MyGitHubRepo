package tme.pos;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.util.Set;

import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by kchoy on 3/8/2016.
 */
public class UnitTextWatcher implements TextWatcher {
    public interface IUnitChangedListener
    {
        void UnitChanged(int newUnit,int oldUnit);
    }
    boolean isEditing;
    double price;
    double price_limit;
    Context context;
    EditText txtUnit;
    IUnitChangedListener listener;
    int previousUnit=0;

    public UnitTextWatcher(EditText et,double price,double price_limit,Context c,IUnitChangedListener l)
    {
        Settings(et,price,price_limit,c,l);

    }
    public UnitTextWatcher(EditText et,double price,Context c,IUnitChangedListener l)
    {
        Settings(et,price,0,c,l);

    }
    public UnitTextWatcher(EditText et,Context c,IUnitChangedListener l)
    {
        Settings(et,0,0,c,l);

    }
    private void Settings(EditText et,double price,double price_limit,Context c,IUnitChangedListener l)
    {
        this.txtUnit = et;
        this.price_limit = price_limit;
        this.price = price;
        this.context = c;
        this.listener = l;
        previousUnit = (et.getText().length()>0)?Integer.parseInt(et.getText().toString()):previousUnit;
    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
       /* if(s.length()==0)
        {
            //unit=0;
        }
        else{unit = Integer.parseInt(s.toString());}*/
    }

    @Override
    public void afterTextChanged(final Editable s) {
        if (s.toString().trim().length() == 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (txtUnit.getText().toString().trim().length() == 0) {
                        txtUnit.setText("1");
                    }
                }
            }, 1000);
           //do nothing
            return;
        }
        if (isEditing) return;
        isEditing = true;

        int intUnit = Integer.parseInt(s.toString());

        if (intUnit > common.text_and_length_settings.UNIT_LIMIT) {
            common.Utility.ShowMessage("Unit Limit", "Please keep the unit under 1000.", context, R.drawable.no_access);
            s.replace(0, s.length(), "1");
        } else if (intUnit == 0) {
            common.Utility.ShowMessage("Unit Limit", "Unit value minimum is 1.",context, R.drawable.no_access);
            s.replace(0, s.length(), "1");
        } else if (price>0 && price_limit>0 && IsGreaterPriceLimit(intUnit)) {
            common.Utility.ShowMessage("Price Limit", "Please keep the value under <b><i>million</i></b>.",context, R.drawable.no_access);
            s.replace(0, s.length(), "1");
        }



        if(listener!=null)
        {
            listener.UnitChanged(Integer.parseInt(s.toString()),previousUnit);
        }
        //update previous variable
        previousUnit = Integer.parseInt(s.toString());
        isEditing = false;
    }
    private boolean IsGreaterPriceLimit(int intUnit)
    {

        if(Math.abs(price*intUnit)>price_limit)return true;
        return false;
    }
}
