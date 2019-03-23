package tme.pos;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CalendarView;
import android.widget.EditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Calendar;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/9/2016.
 */
public class PercentageTextWatcher implements TextWatcher {
    public interface IPercentageTextWatcherListener
    {
        void AfterPercentageTextChanged(String strNewText);
    }
    //boolean isEditing = false;
    //String strBeforeChange="";
    String strPrevious = "";
    EditText et;
    Context context;
    boolean blnThreeDecimalPoint;
    IPercentageTextWatcherListener listener;
    int b_start,b_replaced_char_count,b_new_inserted_length;
    String strTest="";
    int counter=0;
    public PercentageTextWatcher(Context c,EditText et,boolean blnThreeDecimalPoints)
    {
        this.et = et;
        this.context = c;
        this.blnThreeDecimalPoint = blnThreeDecimalPoints;
        this.listener=null;
    }

    public PercentageTextWatcher(Context c,EditText et,boolean blnThreeDecimalPoints,IPercentageTextWatcherListener l)
    {
        this.et = et;
        this.context = c;
        this.blnThreeDecimalPoint = blnThreeDecimalPoints;
        this.listener=l;
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int start_position, int replaced_char_count, int new_char_inserted_count) {

        if(counter++==0) {
            b_start = start_position;b_replaced_char_count=replaced_char_count;b_new_inserted_length = new_char_inserted_count;
            //strTest = "[" + charSequence + "]" + start_position + "," + replaced_char_count + "," + new_char_inserted_count;
            strPrevious = charSequence.toString();
        }
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int length_before, int replaced_char_count) {

        if(counter++==1)
        strTest +=" ; ["+charSequence+"]"+start+","+length_before+","+replaced_char_count;
    }

    @Override
    public void afterTextChanged(Editable s) {

        if(counter++>=3)return;
        boolean blnReverted=false;
        int decimalPtLength=blnThreeDecimalPoint?3:2;
        String strTemp = s.toString().replaceAll("[$%,]","");
        int denominator = (int)Math.pow(10,decimalPtLength);

        /**empty string case**/
        if(strTemp.length()==0)
        {

            strTemp=(blnThreeDecimalPoint)?"0.000":"0.00";
            s.replace(0, s.length(), strTemp);
            strTest+="|1";
            et.setSelection(s.length());
        }
        /**insert at the end**/
        else if(b_start==strPrevious.length() && b_replaced_char_count==0)
        {

            strTemp = strTemp.replace(".","");
            strTemp = strTemp.substring(0,strTemp.length()-decimalPtLength)+"."+strTemp.substring(strTemp.length()-decimalPtLength);
            float flTemp = Float.parseFloat(strTemp);
            if(flTemp>100)
            {
                common.Utility.ShowMessage("Percentage", "Please keep the value no greater than 100%",context, R.drawable.no_access);
                s.replace(0,s.length(),strPrevious);

            }
            else
            {
                //update with latest float value
                s.replace(0,s.length(),new BigDecimal(strTemp).setScale(decimalPtLength,RoundingMode.FLOOR).toPlainString());//no rounding + string ctor+removing leading zeros
            }
            et.setSelection(s.length());

        }
        /**replacing whole string**/
        else if(b_start==0 && b_replaced_char_count==strPrevious.length())
        {
            float flTemp = Float.parseFloat(strTemp);
            float remainder = flTemp-new BigDecimal(strTemp).setScale(decimalPtLength,RoundingMode.FLOOR).floatValue();//no rounding + string ctor
            //check the newly inserted value has insignificant else divide 1->0.001
            if(remainder==0)
            {
                flTemp/=denominator;
            }

            if(flTemp>100)
            {
                //common.Utility.ShowMessage("Percentage", "Please keep the value no greater than 100%",context, R.drawable.no_access);
                s.replace(0,s.length(),strPrevious);
            }
            else
            {
                //strTemp = new BigDecimal(flTemp).setScale(decimalPtLength,BigDecimal.ROUND_FLOOR).toPlainString();
                //update with latest float value
                s.replace(0,s.length(),new BigDecimal(flTemp+"").setScale(decimalPtLength,RoundingMode.FLOOR).toPlainString());//no rounding + string ctor+removing leading zeros
            }

            et.setSelection(s.length());
        }
        /**copy and paste **/
        else// if( b_replaced_char_count>0)
        {

            //user hit backspace and removed decimal point
            //hence manually delete a character in front of decimal pt
            if(blnThreeDecimalPoint)
            {
                if(b_replaced_char_count==1 && b_new_inserted_length==0) {
                    if (strTemp.indexOf(".") == -1 && strTemp.length() == 5)//format [xxxxx]
                    {
                        strTemp = strTemp.substring(0, 1) + "" + strTemp.substring(2);
                    } else if (strTemp.indexOf(".") == -1 && strTemp.length() == 4) //format [xxxx]
                    {
                        strTemp = strTemp.substring(1);
                    }
                }
            }
            else {

                if(b_replaced_char_count==1 && b_new_inserted_length==0)
                {
                    if (strTemp.indexOf(".") == -1 && strTemp.length() == 4) {
                        strTemp = strTemp.substring(0, 1) + "" + strTemp.substring(2);
                    } else if (strTemp.indexOf(".") == -1 && strTemp.length() == 3) {
                        strTemp = strTemp.substring(1);
                    }
                }
            }
            strTemp = strTemp.replace(".","");
            //beware of user only replacing the whole string with one character only
            //need minimum 3 (4 for sale tax) characters to perform the operation below
            strTemp = strTemp.substring(0,strTemp.length()-decimalPtLength)+"."+strTemp.substring(strTemp.length()-decimalPtLength);
            float flTemp = Float.parseFloat(strTemp);
            if(flTemp>100)
            {
                common.Utility.ShowMessage("Percentage", "Please keep the value no greater than 100%",context, R.drawable.no_access);
                s.replace(0,s.length(),strPrevious);
                blnReverted = true;
            }
            else
            {
                //update with latest float value
                s.replace(0,s.length(),new BigDecimal(flTemp+"").setScale(decimalPtLength,RoundingMode.FLOOR).toPlainString());//no rounding + string ctor+removing leading zeros
            }
            strTemp = s.toString();
            //calculate pointer position
            int decimalPtPosition = strPrevious.indexOf(".");
            int selectionIndex;// s.length()-b_start-1;

            //reverted
            if(blnReverted)
            {
                selectionIndex = b_start;
            }
            //insert at front
            else if(b_start==0)
            {
                selectionIndex = b_new_inserted_length;
            }
            //overwriting existing character, please the selection index after the new string
            else if(b_replaced_char_count>0 && b_new_inserted_length>0)
            {

                int diffLength = strTemp.length()-strPrevious.length();
                if(diffLength==0)
                {
                    selectionIndex = b_start+b_replaced_char_count;
                }
                else if(diffLength>0)
                {
                    selectionIndex = b_start+strTemp.length();
                }
                else
                {
                    selectionIndex = (b_start+diffLength+strTemp.length());
                }
                //selectionIndex = (b_start+b_new_inserted_length);
            }
            //back space hit
            else if(b_replaced_char_count>0 && b_new_inserted_length==0)
            {
                int length = strPrevious.length()-(b_start+b_replaced_char_count);

                selectionIndex = strTemp.length()-length;
            }
            //behind decimal pt
            else if(b_start==decimalPtPosition+1)
            {
                selectionIndex = s.toString().indexOf(".")+1;
            }
            //in front decimal pt
            else if(b_start==decimalPtPosition)
            {
                selectionIndex = s.toString().indexOf(".");
            }
            else
            {
                selectionIndex=b_start;
                if(strTemp.length()==5)
                {
                    selectionIndex++;
                }


            }
            et.setSelection(selectionIndex);
        }

        //if(listener!=null)listener.AfterPercentageTextChanged(strTest);

        if(listener!=null)listener.AfterPercentageTextChanged(s.toString());
        counter=0;//reset


    }

}
