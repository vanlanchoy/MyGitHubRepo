package tme.pos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import tme.pos.BusinessLayer.*;

/**
 * Created by kchoy on 6/9/2015.
 */
public class SupplierUIFragment extends Fragment {
    TextView tvPhone;
    TextView tvName;
    TextView tvAddress;
    TextView tvEmail;
    TextView tvNote;
    ImageView imgEdit;
    ImageView imgDelete;
    //TextView tvAdd;
    //TextView tvCancel;
    //Button btnCancel;
    TableLayout tblSupplier;
    //ScrollView scrSupplierTable;
    //TableLayout tblSupplierLabel;
    //ImageButton imgSupplierBtnCollapse;
    //FrameLayout SupplierPropertiesPlaceHolder;
    //boolean blnAnimationStart = false;
    //int rowWidth;
    //boolean blnEditMode=false;
    //final int offset = 5;
    //final float propertiesPanelHeight=250;
    AddSupplierDialog addSupplierDialog;
    //int intSelectedRowIndex;
    Supplier selectedSupplier;
    public void ShowAddSupplierDialog()
    {
        addSupplierDialog = new AddSupplierDialog(getActivity(), SupplierUIFragment.this);
        addSupplierDialog.show();
    }
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle bundle) {
        final View rootView =inflater.inflate(R.layout.layout_supplier_ui,container,false);



        tblSupplier = (TableLayout)rootView.findViewById(R.id.tblSupplier);

        rootView.findViewById(R.id.imgAddSupplier).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSupplierDialog = new AddSupplierDialog(getActivity(), SupplierUIFragment.this);
                addSupplierDialog.show();
            }
        });


        tvName = (TextView)rootView.findViewById(R.id.tvName);
        tvName.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        tvEmail = (TextView)rootView.findViewById(R.id.tvEmail);
        tvEmail.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvEmail.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        tvAddress = (TextView)rootView.findViewById(R.id.tvAddress);
        tvAddress.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvAddress.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        tvPhone = (TextView)rootView.findViewById(R.id.tvPhone);
        SetTextAttribute(tvPhone);

        tvNote = (TextView)rootView.findViewById(R.id.tvNote);
        SetTextAttribute(tvNote);

        imgEdit = (ImageView)rootView.findViewById(R.id.imgEdit);
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSupplierDialog = new AddSupplierDialog(getActivity(), SupplierUIFragment.this,selectedSupplier);
                addSupplierDialog.show();
            }
        });
        imgDelete = (ImageView)rootView.findViewById(R.id.imgDelete);
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDeleteSupplierDialog();
            }
        });

       /* TextView label = (TextView)rootView.findViewById(R.id.labelSupplier);
        label.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);


        label = (TextView)rootView.findViewById(R.id.labelAddress);
        label.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);


        label = (TextView)rootView.findViewById(R.id.labelContact);
        label.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);*/




        ReloadSupplierTable(-1);

        //hide softkeyboard
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                common.control_events.HideSoftKeyboard(rootView);
            }
        }, 1000);

        return rootView;
    }
    private void ShowDeleteSupplierDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Option");
        builder.setIcon(common.Utility.ReturnMessageBoxSizeIcon(R.drawable.question));
        builder.setMessage("Delete supplier?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteSupplier(selectedSupplier.SupplierId);
                dialogInterface.dismiss();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }
    protected void DisplaySupplierDetail(Supplier s)
    {
        tvName.setText(s.Name);
        tvPhone.setText(common.Utility.ReformatPhoneString(s.PhoneNumber));
        tvAddress.setText(s.Address);
        tvNote.setText(s.Note);
        tvEmail.setText(s.Email);
        imgDelete.setVisibility(View.VISIBLE);
        imgEdit.setVisibility(View.VISIBLE);
    }
    private void SetTextAttribute(TextView tv)
    {
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
    }
    private void SlideOut(boolean flgSwipeLeftToDelete,final TableRow row)
    {

        TranslateAnimation movement = new TranslateAnimation(0.0f, 5000.0f, 0.0f, 0.0f);//move right
        if(flgSwipeLeftToDelete) {
            movement = new TranslateAnimation(0.0f, -5000.0f, 0.0f, 0.0f);//move left
        }
        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                RemoveSupplierFromTable(Long.parseLong(row.getTag().toString()));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        row.startAnimation(movement);

    }
    /*private void SetSupplierRowBackgroundColor()
    {
        for(int i=0;i<tblSupplier.getChildCount();i++)
        {
            if((i&1)==0)tblSupplier.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.very_light_grey));
            else tblSupplier.getChildAt(i).setBackground(null);
        }
    }*/
    private void RemoveSupplierFromTable(long SupplierId)
    {
        int index=tblSupplier.getChildCount()-1;
        for(int i =tblSupplier.getChildCount()-1;i>=0;i--)
        {
            if(Long.parseLong(tblSupplier.getChildAt(i).getTag().toString())==SupplierId)
            {
                tblSupplier.removeViewAt(i);
            }
            else
            {
                //update the id on table
                ((TextView)((TableRow)tblSupplier.getChildAt(i)).getChildAt(0)).setText(index--+"");
                //
            }

        }

        if(tblSupplier.getChildCount()==0)
        {
            tblSupplier.addView(CreateNoContentRow());
        }

        RepaintRowBackground();
        //SetSupplierRowBackgroundColor();
    }
    protected void UpdateTableRowName()
    {
        ReloadSupplierTable(selectedSupplier.SupplierId);
        /*for(int i=0;i<tblSupplier.getChildCount();i++)
        {
            if(((Long)tblSupplier.getChildAt(i).getTag())==selectedSupplier.SupplierId)
            {
                ((TextView)((TableRow)tblSupplier.getChildAt(i)).getChildAt(1)).setText(selectedSupplier.Name.toUpperCase());
                break;
            }
        }*/
        /*if(intSelectedRowIndex>-1)
        {
            TableRow tr =(TableRow)tblSupplier.getChildAt(intSelectedRowIndex);
            ((TextView)tr.getChildAt(1)).setText(selectedSupplier.Name);
        }*/
    }
    private void ClearSupplierDetailPanel()
    {
        tvName.setText("");

        tvPhone.setText("");
        tvEmail.setText("");
        tvAddress.setText("");
        tvNote.setText("");



        imgDelete.setVisibility(View.INVISIBLE);
        imgEdit.setVisibility(View.INVISIBLE);

    }
    private void DeleteSupplier(long supplierId)
    {



        int result =common.supplierList.Delete(supplierId);
        if(result>0){
            //common.supplierList.Delete(supplierId);
           TableRow animeRow=null;
            for(int i=0;i<tblSupplier.getChildCount();i++)
            {
                if(Long.parseLong(tblSupplier.getChildAt(i).getTag().toString())==supplierId)
                {
                    animeRow =(TableRow) tblSupplier.getChildAt(i);
                }
            }
            if(animeRow!=null){
                SlideOut(!common.myAppSettings.SwipeLeftToDelete(),animeRow);
            }



            selectedSupplier = null;
            //intSelectedRowIndex=-1;
            ClearSupplierDetailPanel();
        }

    }
    public void SlideIn(boolean blnSlideRight,final TableRow row)
    {

        TranslateAnimation movement = new TranslateAnimation(-5000.0f, 0.0f, 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide in to right
            movement = new TranslateAnimation(5000.0f, 0.0f, 0.0f, 0.0f);//move left
        }





        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                row.setVisibility(View.VISIBLE);
                row.setBackgroundResource(R.drawable.draw_table_row_border);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                short index = Short.parseShort(((TextView)row.getChildAt(0)).getText().toString());
                index--;
                if(selectedSupplier==null)
                {
                    if ((index % 2) != 0)
                        row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
                    else row.setBackground(null);
                }
                else if(selectedSupplier.SupplierId!=(Long)row.getTag())
                {
                    if ((index % 2) != 0)
                        row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
                    else row.setBackground(null);
                }
                else
                {
                    row.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                    //row.setBackground(null);
                }


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        row.startAnimation(movement);

    }

   /* private boolean Validate(long lngSupplierId)
    {
        txtName.setText(txtName.getText().toString().trim());
        txtEmail.setText(txtEmail.getText().toString().trim());
        txtAddress.setText(txtAddress.getText().toString().trim());

        if(common.supplierList.GetSupplier().length>=common.text_and_length_settings.MAX_SUPPLIER)
        {
            ShowMessage("Add Supplier","You have reached the maximum of "+common.text_and_length_settings.MAX_SUPPLIER+" suppliers.",R.drawable.no_access);
            return false;
        }


        String strName = txtName.getText().toString().trim();

        if(IsExist(strName,lngSupplierId))
        {
            ShowMessage("Add Supplier","Name already existed on the list.",R.drawable.no_access);
            return false;
        }
        if(strName.length()==0)
        {
            ShowMessage("Add Supplier","Please provide a valid name.",R.drawable.no_access);
            return false;
        }
        int intNumberLength =txtPhoneAreaCode.getText().length()+txtPhoneFirstPart.getText().length()+txtPhoneSecondPart.getText().length();
        if(intNumberLength>0 && intNumberLength<10)
        {
            ShowMessage("Add Supplier","Please provide a valid phone number.",R.drawable.no_access);
            return false;
        }


        return true;
    }*/
    private TableRow CreateNoContentRow()
    {
        TableRow row = new TableRow(getActivity());

        TextView tv = new TextView(getActivity());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tv.setText("No Supplier");
        tv.setTextColor(getResources().getColor(R.color.common_signin_btn_light_text_disabled));
        //tv.setBackgroundColor(getResources().getColor(R.color.green));

        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(common.Utility.DP2Pixel(500,getActivity()), ViewGroup.LayoutParams.WRAP_CONTENT);

        tv.setLayoutParams(trlp);
        row.addView(tv);
        row.setBackgroundColor(getResources().getColor(R.color.transparent));
        return row;
    }
    protected void ReloadSupplierTable(long NewSupplierId)
    {
        //remove all row except the default no content row
        tblSupplier.removeAllViews();


        for(int i=0;i<common.supplierList.GetSupplier().length;i++)
        {
            TableRow row = CreateSupplierRow(common.supplierList.GetSupplier()[i], Short.parseShort(i + ""));
            row.setTag(common.supplierList.GetSupplier()[i].SupplierId);
            tblSupplier.addView(row);
            if(NewSupplierId==common.supplierList.GetSupplier()[i].SupplierId)
            {

                row.setVisibility(View.INVISIBLE);
                //row.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                SlideIn(!common.myAppSettings.SwipeLeftToDelete(),row);
            }

        }
        if(common.supplierList.GetSupplier().length==0)
        {

            tblSupplier.addView(CreateNoContentRow());

        }



    }
   /* private void EditMode(final Supplier s)
    {
        blnEditMode = true;
        tvAdd.setText("Save");
        //btnAdd.setText("Save");
        txtName.setText(s.Name);
        tvCancel.setVisibility(View.VISIBLE);
        txtEmail.setText(s.Email);
        txtAddress.setText(s.Address);

        if(s.PhoneNumber.length()>0)
        {
            if(s.PhoneNumber.length()>10)//11 digits
            {
                txtPhoneAreaCode.setText(s.PhoneNumber.substring(0, 3));
                txtPhoneFirstPart.setText(s.PhoneNumber.substring(3,7));
                txtPhoneSecondPart.setText(s.PhoneNumber.substring(7));
            }
            else
            {
                //10 digits
                txtPhoneAreaCode.setText(s.PhoneNumber.substring(0, 3));
                txtPhoneFirstPart.setText(s.PhoneNumber.substring(3,6));
                txtPhoneSecondPart.setText(s.PhoneNumber.substring(6));
            }
        }

        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateSupplier(s.SupplierId);
                common.control_events.HideSoftKeyboard(view);
            }
        });

    }*/
   /* private void UpdateSupplier(long lngSupplierId)
    {
        if(!Validate(lngSupplierId))return;
        Supplier s=null;
        for(int i=0;i<common.supplierList.GetSupplier().length;i++)
        {
            if(common.supplierList.GetSupplier()[i].SupplierId==lngSupplierId)
            {
                s = common.supplierList.GetSupplier()[i];
            }
        }
        if(s!=null) {
            s.Name = txtName.getText().toString().trim();
            s.PhoneNumber = txtPhoneAreaCode.getText().toString() + txtPhoneFirstPart.getText() + txtPhoneSecondPart.getText() + "";
            s.Address = txtAddress.getText().toString();
            s.Email = txtEmail.getText().toString();
            common.supplierList.Update(s);
            ReloadSupplierTable(lngSupplierId);
            ClearAllInputFields();
        }
        CancelEdit();
    }
    private void CancelEdit()
    {
        //btnAdd.setText("Add");
        tvAdd.setText("Add");
        tvCancel.setVisibility(View.INVISIBLE);
        ClearAllInputFields();
        //txtName.setText("");
        //txtPhoneAreaCode.setText("");
        //txtPhoneFirstPart.setText("");
        //txtPhoneSecondPart.setText("");
        //txtEmail.setText("");
        //txtAddress.setText("");
        blnEditMode=false;
        tvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSupplier();
            }
        });
        *//*btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSupplier();
            }
        });*//*
    }
    private void ClearAllInputFields()
    {
        txtName.setText("");
        txtPhoneAreaCode.setText("");
        txtPhoneFirstPart.setText("");
        txtPhoneSecondPart.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
    }
    private void AddSupplier()
    {
        if(!Validate(-1)) return;

        DatabaseHelper helper = new DatabaseHelper(getActivity());

        Supplier s = new Supplier();
        s.SupplierId=helper.GenerateNextSupplierId();
        s.Name = txtName.getText().toString();
        s.Address = txtAddress.getText().toString();
        s.Email = txtEmail.getText().toString();
        s.PhoneNumber = txtPhoneAreaCode.getText().toString()+txtPhoneFirstPart.getText()+txtPhoneSecondPart.getText()+"";
        s.IsActive = true;
        long id = common.supplierList.Insert(s);



        if(id>-1)
        {

            //add the supplier in the table
            ClearAllInputFields();
            ReloadSupplierTable(s.SupplierId);
        }
        else {
            ShowMessage("Add Supplier", "Insert new supplier failed.",R.drawable.exclaimation);
        }
    }*/
    private void RepaintRowBackground()
    {
        for(int i=0;i<tblSupplier.getChildCount();i++)
        {
            if ((i % 2) != 0)
                tblSupplier.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.very_light_grey));
            else
                tblSupplier.getChildAt(i).setBackgroundColor(Color.WHITE);
        }
    }
    private TableRow CreateSupplierRow(final Supplier s,final int index)
    {
        TableRow row = new TableRow(getActivity());
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepaintRowBackground();

                view.setBackgroundColor(getResources().getColor(R.color.selected_row_green));

                selectedSupplier = s;
                DisplaySupplierDetail(s);
            }
        });

        TextView tv = new TextView(getActivity());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SERVER_FRAGMENT_TEXT_SIZE);
        tv.setText((index+1) + "");
        tv.setLayoutParams(new TableRow.LayoutParams(80,TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        row.addView(tv);

        tv = new TextView(getActivity());
        tv.setText(s.Name.toUpperCase());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.SERVER_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(220, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.LEFT);
        row.addView(tv);



        if((index%2)!=0)row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));

        TableLayout.LayoutParams tllp =new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tllp.setMargins(0,10,0,0);
        row.setLayoutParams(tllp);

        return row;
    }
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getActivity());
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
        messageBox.show();
    }

}
