package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import tme.pos.BusinessLayer.common;

/**
 * Created by vanlanchoy on 3/8/2015.
 */
public class TaxRateOptionPopup {
    interface ITaxRateChangedListener
    {
        void UpdatedRate(String strNewTaxRate);
        void Dismiss();
    }
    Context context;
    View dialogView;

    float ADD_MENU_ITEM_TITLE_TEXT_SIZE = 30;
    float ADD_MENU_ITEM_MODIFIER_TEXT_SIZE=35;
    float ADD_MENU_ITEM_TEXT_SIZE =20;
    String DEFAULT_TAX_PERCENTAGE="00.000";
    ITaxRateChangedListener listener;
    public TaxRateOptionPopup(Context c,ITaxRateChangedListener l)
    {
        this.context = c;
        listener = l;
        LoadApplicationData();
    }
    private void LoadApplicationData()
    {
        ADD_MENU_ITEM_TITLE_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_title);
        ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_modifier_text);
        ADD_MENU_ITEM_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_text);
        DEFAULT_TAX_PERCENTAGE = context.getResources().getString(R.string.label_default_tax_percentage);
    }

    public void ShowPopup(float flTaxPercentage)
    {
        ((MainUIActivity)context).SetPopupShow(true);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((MainUIActivity)context).getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(R.layout.layout_tax_popup_window_ui, null);

        /*//title
        TextView tvTitle =(TextView)dialogView.findViewById(R.id.lblWindowTitle);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TITLE_TEXT_SIZE);
        tvTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);*/

        TextView tvProperties = (TextView)dialogView.findViewById(R.id.lblProperties);
        tvProperties.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE);
        tvProperties.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvProperties.setGravity(Gravity.CENTER);

        TextView tvItem = (TextView)dialogView.findViewById(R.id.lblTaxRate);
        tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT_SIZE);
        tvItem.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)));

        TextView tvSign = (TextView)dialogView.findViewById(R.id.tvPercentageSign);
        tvSign.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT_SIZE);
        tvSign.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)));

        //set para for edit text section
        EditText txtValue = (EditText)dialogView.findViewById(R.id.txtTaxPercentage);
        txtValue.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT_SIZE);
        txtValue.setText(new BigDecimal(flTaxPercentage*100).setScale(3,BigDecimal.ROUND_HALF_UP).toPlainString());
        txtValue.setSelection(txtValue.getText().length());
        txtValue.addTextChangedListener(new PercentageTextWatcher(context,txtValue,true));
       /* txtValue.addTextChangedListener(new TextWatcher() {
            boolean isTaxEditing=false;
            String strPrevious="";
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isTaxEditing) return;
                isTaxEditing = true;

                strPrevious = CheckTextChanged(editable,strPrevious);



                isTaxEditing = false;
            }
        });*/





        builder.setView(dialogView);
        final AlertDialog ad = builder.create();

        ImageButton imgCancel =(ImageButton) dialogView.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(listener!=null)listener.Dismiss();
                //((MainUIActivity)context).SetPopupShow(false);
                ad.dismiss();
            }
        });
        common.control_events.SetOnTouchImageButtonEffect(imgCancel,R.drawable.green_border_outer_glow_cancel,R.drawable.green_border_cancel);

        //save new percentage
        ImageButton imgSave =(ImageButton) dialogView.findViewById(R.id.imgSave);
        common.control_events.SetOnTouchImageButtonEffect(imgSave,R.drawable.green_border_outer_glow_save,R.drawable.green_border_save);
        imgSave.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strNewTaxRate = ((EditText) dialogView.findViewById(R.id.txtTaxPercentage)).getText().toString();
                        if(listener!=null)
                        {
                            listener.UpdatedRate(Float.parseFloat(strNewTaxRate)/100f+"");
                        }
                        //((MainUIActivity) context).SaveTax(strNewTaxRate);
                        //((MainUIActivity) context).SetPopupShow(false);
                        ad.dismiss();



                    }
                });

        ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((MainUIActivity)context).SetPopupShow(false);
            }
        });

        ad.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_Sliding;

        //final View localView = dialogView;
        android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.show();



            }
        },280);
    }
    public String CheckTextChanged(Editable s,String strPrevious)
    {

        if(!s.toString().equals(strPrevious)) {

            String str = s.toString().replaceAll("[%]", "");
            if(str.length()==0)str="0";
            double s1 = Double.parseDouble(str);
            String strInsignificant="";
            if(str.indexOf(".")>-1)
            {strInsignificant=str.substring(str.indexOf(".") + 1);}

            if (str.length() == 0) {
                str = "00.000";
            }
            else if(s1>100)
            {
                common.Utility.ShowMessage("Percentage", "Cannot be greater than 100%",context, R.drawable.no_access);
                str = strPrevious;
            }
            else if(strInsignificant.length()>3)
            {
                common.Utility.ShowMessage("Percentage", "Cannot be more than three decimal point",context, R.drawable.no_access);
                str= strPrevious;
            }

            s.replace(0, s.length(), str.replace("%","")+"%");

            strPrevious = str;
        }
        return strPrevious;
    }
}
