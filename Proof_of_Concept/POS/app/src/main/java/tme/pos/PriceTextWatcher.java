package tme.pos;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.NumberFormat;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/9/2016.
 */
public class PriceTextWatcher implements TextWatcher {
    public interface IPriceTextWatcherListener
    {
        void AfterPriceTextChanged(String strNewText);
    }
    boolean isEditing;
    boolean isNegateMode;
    String strPrevious = "";
    EditText et;
    Context context;
    IPriceTextWatcherListener listener;
    public PriceTextWatcher(Context c,EditText et,boolean blnNegative)
    {
        this.et = et;
        this.context = c;
        this.isNegateMode= blnNegative;
        this.listener = null;
    }
    public PriceTextWatcher(Context c,EditText et,boolean blnNegative,IPriceTextWatcherListener l)
    {
        this.et = et;
        this.context = c;
        this.isNegateMode= blnNegative;
        this.listener = l;
    }
    @Override
    public void afterTextChanged(Editable editable) {
        if (isEditing) return;
        isEditing = true;
        strPrevious = CheckTextChanged(editable, strPrevious);
        if(listener!=null)listener.AfterPriceTextChanged(strPrevious);
        isEditing = false;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {

    }
    private String CheckTextChanged(Editable s,String strPrevious)
    {
        //boolean isNegative=false;
        //if(s.toString().indexOf("-")>-1)isNegative = true;
        if(!s.toString().equals(strPrevious)) {
            String str = s.toString().replaceAll("[$,.%-]", "");

            if (str.length() == 0) {
                str = "0";
            } /*else if (str.equals("-")) {
                str = "0";
            }*/
            double s1 = Double.parseDouble(str);

            String strFormatted = NumberFormat.getCurrencyInstance().format((s1 / 100));
            double s2= ((s1 / 100.0));
            if(Math.abs(s2)>1000000)
            {
                common.Utility.ShowMessage("Price", "Please keep the value under <b><i>million</i></b>.",context, R.drawable.no_access);
                strFormatted = NumberFormat.getCurrencyInstance().format((0 / 100));
            }

            //if(isNegative){strFormatted="-"+strFormatted;}
            strFormatted=strFormatted.replace("$","");
            s.replace(0, s.length(), strFormatted);

            strPrevious = strFormatted;
        }
        return strPrevious;
    }
}
