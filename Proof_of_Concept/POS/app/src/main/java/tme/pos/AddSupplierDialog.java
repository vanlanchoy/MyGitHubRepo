package tme.pos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import android.widget.TextView;



import tme.pos.BusinessLayer.*;

import tme.pos.DataAccessLayer.DatabaseHelper;

/**
 * Created by kchoy on 1/8/2016.
 */
public class AddSupplierDialog extends Dialog {
    Supplier supplier;
    SupplierUIFragment parentFragment;
    EditText txtName;
    EditText txtPhoneAreaCode;
    EditText txtPhoneFirstPart;
    EditText txtPhoneSecondPart;
    EditText txtEmail;
    EditText txtAddress;
    EditText txtNote;
    public AddSupplierDialog(Context context, SupplierUIFragment parent) {
        super(context);
        parentFragment = parent;
    }
    public AddSupplierDialog(Context context, SupplierUIFragment parent, Supplier s) {
        super(context);

        parentFragment = parent;
        supplier = s;


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_add_supplier_dialog);

        txtName = (EditText)findViewById(R.id.txtName);
        txtPhoneAreaCode = (EditText)findViewById(R.id.txtPhoneAreaCode);
        txtPhoneFirstPart = (EditText)findViewById(R.id.txtPhoneFirstPart);
        txtPhoneSecondPart = (EditText)findViewById(R.id.txtPhoneSecondPart);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtAddress = (EditText)findViewById(R.id.txtAddress);
        txtNote = (EditText)findViewById(R.id.txtNote);

        SetTextAttribute(findViewById(R.id.tvSupplier));
        SetTextAttribute(findViewById(R.id.tvNote));
        SetTextAttribute(txtPhoneAreaCode);
        SetTextAttribute(txtPhoneFirstPart);
        SetTextAttribute(txtPhoneSecondPart);
        SetTextAttribute(txtEmail);
        txtEmail.setHint(getContext().getResources().getString(R.string.hint_email).replace("%1$d",
                getContext().getResources().getString(R.string.email_address_max_char)));
        SetTextAttribute(txtName);
        txtName.setHint(getContext().getResources().getString(R.string.hint_supplier_max_length).replace("%1$d",
                getContext().getResources().getString(R.string.supplier_name_max_char)));
        txtName.setFilters(common.Utility.CreateMaxLengthFilter(common.text_and_length_settings.SUPPLIER_NAME_MAX_LENGTH));
        SetTextAttribute(txtAddress);
        SetTextAttribute(findViewById(R.id.tvPhone));
        SetTextAttribute(findViewById(R.id.tvPhoneCloseParenthesis));
        SetTextAttribute(findViewById(R.id.tvPhoneDash1));
        SetTextAttribute(findViewById(R.id.tvEmail));
        SetTextAttribute(findViewById(R.id.tvAddress));

        TextView tvOK = (TextView)findViewById(R.id.tvOK);
        tvOK.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
        tvOK.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (supplier == null) {
                    AddSupplier();
                } else {
                    UpdateSupplier();
                }
            }
        });

        TextView tvCancel = (TextView)findViewById(R.id.tvCancel);
        tvCancel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
        tvCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if(supplier!=null)DisplaySupplierInfo();
    }
    private void DisplaySupplierInfo()
    {
        txtAddress.setText(supplier.Address);
        txtEmail.setText(supplier.Email);
        txtName.setText(supplier.Name);
        txtNote.setText(supplier.Note);
        int count=0;
        while(count< supplier.PhoneNumber.length())
        {
            if(count<3) {
                txtPhoneAreaCode.setText(txtPhoneAreaCode.getText()+ supplier.PhoneNumber.substring(count++, count));
            }
            else if(count<6 && count>2)
            {
                txtPhoneFirstPart.setText(txtPhoneFirstPart.getText()+ supplier.PhoneNumber.substring(count++, count));
            }
            else
            {
                txtPhoneSecondPart.setText(txtPhoneSecondPart.getText()+ supplier.PhoneNumber.substring(count++, count));
            }
        }
    }
    @Override
    public void dismiss() {
        if(supplier !=null)
        {
            parentFragment.DisplaySupplierDetail(supplier);
            parentFragment.UpdateTableRowName();
        }
        super.dismiss();
    }
    private void UpdateSupplier()
    {
        if(!Validate(supplier.SupplierId))return;

        if(supplier !=null) {
            supplier.Name = txtName.getText().toString().trim();
            supplier.PhoneNumber = txtPhoneAreaCode.getText().toString() + txtPhoneFirstPart.getText() + txtPhoneSecondPart.getText() + "";

            supplier.Email = txtEmail.getText()+"";
            supplier.Address = txtAddress.getText()+"";
            supplier.Note=txtNote.getText()+"";
            common.supplierList.Update(supplier);


        }
        dismiss();
    }
    private void SetTextAttribute(View v)
    {
        if(v instanceof TextView)
        {
            ((TextView)v).setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
            ((TextView)v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        }
        else if(v instanceof EditText)
        {
            ((EditText)v).setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
            ((EditText)v).setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        }
    }
    private void AddSupplier()
    {
        if(!Validate(-1)) return;

        DatabaseHelper helper = new DatabaseHelper(getContext());

        Supplier s = new Supplier();
        s.SupplierId=helper.GenerateNextSupplierId();
        s.Name = txtName.getText().toString();
        s.Address = txtAddress.getText().toString();
        s.Email = txtEmail.getText().toString();
        s.PhoneNumber = txtPhoneAreaCode.getText().toString()+txtPhoneFirstPart.getText()+txtPhoneSecondPart.getText()+"";
        s.IsActive = true;
        s.Note = txtNote.getText().toString();
        long id = common.supplierList.Insert(s);



        if(id>-1)
        {

            //add the supplier in the table
            parentFragment.ReloadSupplierTable(s.SupplierId);
        }
        else {
            common.Utility.ShowMessage("Add Supplier", "Insert new supplier failed.",getContext(), R.drawable.exclaimation);
        }

        dismiss();
    }
    private boolean Validate(long lngSupplierId)
    {
        txtName.setText(txtName.getText().toString().trim());
        txtEmail.setText(txtEmail.getText().toString().trim());
        txtAddress.setText(txtAddress.getText().toString().trim());
        txtNote.setText(txtNote.getText().toString().trim());
        if(common.supplierList.GetSupplier().length>=common.text_and_length_settings.MAX_SUPPLIER)
        {
            common.Utility.ShowMessage("Add Supplier", "You have reached the maximum of " + common.text_and_length_settings.MAX_SUPPLIER + " suppliers.",getContext(), R.drawable.no_access);
            return false;
        }


        String strName = txtName.getText().toString().trim();

        if(IsExist(strName,lngSupplierId))
        {
            common.Utility.ShowMessage("Add Supplier", "Name already existed on the list.",getContext(),R.drawable.no_access);
            return false;
        }
        if(strName.length()==0)
        {
            common.Utility.ShowMessage("Add Supplier", "Please provide a valid name.",getContext(), R.drawable.no_access);
            return false;
        }
        int intNumberLength =txtPhoneAreaCode.getText().length()+txtPhoneFirstPart.getText().length()+txtPhoneSecondPart.getText().length();
        if(intNumberLength>0 && intNumberLength<10)
        {
            common.Utility.ShowMessage("Add Supplier", "Please provide a valid phone number.",getContext(), R.drawable.no_access);
            return false;
        }


        return true;
    }
    private boolean IsExist(String strName,long lngSupplierId)
    {
        for(int i=0;i<common.supplierList.GetSupplier().length;i++)
        {
            if(common.supplierList.GetSupplier()[i].Name.equalsIgnoreCase(strName) &&
                    common.supplierList.GetSupplier()[i].SupplierId!=lngSupplierId)
            {

                return true;
            }
        }

        return false;
    }
}
