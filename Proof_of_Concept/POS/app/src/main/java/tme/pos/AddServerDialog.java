package tme.pos;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.RadioButton;

import android.widget.TextView;

import java.io.File;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.DataAccessLayer.DatabaseHelper;

/**
 * Created by kchoy on 12/28/2015.
 */
public class AddServerDialog extends Dialog {
    //View currentView;
    AppSettings myAppSettings;
    TextView tvRemovePic;
    RadioButton rdFemale;
    RadioButton rdMale;
    EditText txtName;
    EditText txtPhoneAreaCode;
    EditText txtPhoneFirstPart;
    EditText txtPhoneSecondPart;
    EditText txtEmail;
    EditText txtNote;
    EditText txtAddress;
    ImageView imgItemPic;
    ServerUIFragment parentFragment;
    Server server;
    protected static String TemporaryCameraPictureFileName="temp.jpg";
    //TextView tvAdd;
    public AddServerDialog(Context context, ServerUIFragment parent) {
        super(context);
        parentFragment = parent;
    }
    public AddServerDialog(Context context, ServerUIFragment parent, Server s) {
        super(context);

        parentFragment = parent;
        server = s;


    }

    private boolean IsExist(String strName,long lngEmployeeId)
    {

        for(int i=0;i<common.serverList.GetServers().length;i++)
        {
            if(common.serverList.GetServers()[i].Name.equalsIgnoreCase(strName) &&
                    common.serverList.GetServers()[i].EmployeeId!=lngEmployeeId)
            {

                return true;
            }
        }

        return false;
    }

    private void AddServer()
    {
        if(!Validate(-1)) return;


        DatabaseHelper helper = new DatabaseHelper(getContext());
        Server server = new Server();
        server.EmployeeId=helper.GenerateNextServerId();
        server.Name = txtName.getText().toString().trim();
        server.PhoneNumber = txtPhoneAreaCode.getText().toString()+txtPhoneFirstPart.getText()+txtPhoneSecondPart.getText()+"";
        if(rdMale.isChecked())
        {

            server.gender = Enum.ServerGender.male;
        }
        else
        {

            server.gender = Enum.ServerGender.female;
        }
        server.Email = (txtEmail.getText()+"").trim();
        server.PicturePath=imgItemPic.getTag()+"";
        server.Address=(txtAddress.getText()+"").trim();
        server.Note = (txtNote.getText()+"").trim();
        server.IsActive= true;

        //ShowMessage("before insert","length="+ common.servantList.GetServers().length);
        long id = common.serverList.Insert(server);
        //ShowMessage("after insert","length="+ common.servantList.GetServers().length);


        if(id>-1)
        {
            //add the servant on the table
            parentFragment.ReloadServerTable(server.EmployeeId);
        }
        else {
            common.Utility.ShowMessage("Add Server", "Insert new server failed.", getContext(), R.drawable.exclaimation);
        }

        dismiss();
    }

    @Override
    public void dismiss() {
        if(server !=null)
        {
            parentFragment.DisplayServerDetail(server);
            parentFragment.UpdateTableRowName();
        }
        super.dismiss();
    }

    private void UpdateServer()
    {
        if(!Validate(server.EmployeeId))return;
      /*  Server s=null;
        for(int i=0;i<common.servantList.GetServers().length;i++)
        {
            if(common.servantList.GetServers()[i].EmployeeId==lngEmployeeId)
            {
                s = common.servantList.GetServers()[i];
            }
        }*/
        if(server !=null) {
            server.Name = txtName.getText().toString().trim();
            server.PhoneNumber = txtPhoneAreaCode.getText().toString() + txtPhoneFirstPart.getText() + txtPhoneSecondPart.getText() + "";
            if (rdMale.isChecked()) {

                server.gender = Enum.ServerGender.male;
            } else {

                server.gender = Enum.ServerGender.female;
            }

            if(imgItemPic.getTag()!=null) server.PicturePath = imgItemPic.getTag()+"";
            server.Email = txtEmail.getText()+"";
            server.Address = txtAddress.getText()+"";
            server.Note=txtNote.getText()+"";
            common.serverList.Update(server);
            //ReloadServerTable(lngEmployeeId);

        }
        dismiss();
    }
    private boolean Validate(long lngEmployeeId)
    {
        if(common.serverList.GetServers().length>=common.text_and_length_settings.MAX_SERVER)
        {
            common.Utility.ShowMessage("Add Server", "You have reached the maximum of " + common.text_and_length_settings.MAX_SERVER + " servers.",getContext(), R.drawable.no_access);
            return false;
        }
        String strName = txtName.getText().toString().trim();

        if(IsExist(strName,lngEmployeeId))
        {
            common.Utility.ShowMessage("Add Server", "Name already existed on the list.", getContext(), R.drawable.no_access);
            return false;
        }
        if(strName.length()==0)
        {
            common.Utility.ShowMessage("Add Server", "Please provide a valid name.",getContext(), R.drawable.no_access);
            return false;
        }
        int intNumberLength =txtPhoneAreaCode.getText().length()+txtPhoneFirstPart.getText().length()+txtPhoneSecondPart.getText().length();
        if(intNumberLength>0 && intNumberLength<10)
        {
            common.Utility.ShowMessage("Add Server", "Please provide a valid phone number.", getContext(), R.drawable.no_access);
            return false;
        }
        return true;
    }

    public void SavedPictureResult(String strSavedPicPath)
    {
        if(strSavedPicPath.length()==0)return;//saved failed return;
        //delete the existing physical pic if any
        if(imgItemPic.getTag()!=null) {
            ((PhotoFeatureFragment)parentFragment).DeleteExistingItemPic((String) imgItemPic.getTag());
        }
        Bitmap bitmap =((PhotoFeatureFragment)parentFragment).DecodeBitmapFile(strSavedPicPath);
        imgItemPic.setImageBitmap(bitmap);
        imgItemPic.setBackground(null);
        imgItemPic.setTag(strSavedPicPath);

        findViewById(R.id.tvRemovePic).setVisibility(View.VISIBLE);
    }
    private void DisplayServerInfo()
    {
        if(server.PicturePath.length()>0)
        {
            imgItemPic.setImageBitmap((parentFragment).DecodeBitmapFile(server.PicturePath));
            imgItemPic.setTag(server.PicturePath);
            tvRemovePic.setVisibility(View.VISIBLE);
        }
        else
        {
            tvRemovePic.setVisibility(View.INVISIBLE);
        }
        txtName.setText(server.Name);
        if(server.gender== Enum.ServerGender.male)
        {
            rdMale.setChecked(true);
            rdFemale.setChecked(false);
        }
        else
        {
            rdMale.setChecked(false);
            rdFemale.setChecked(true);
        }

        int count=0;
        while(count< server.PhoneNumber.length())
        {
            if(count<3) {
                txtPhoneAreaCode.setText(txtPhoneAreaCode.getText()+ server.PhoneNumber.substring(count++, count));
            }
            else if(count<6 && count>2)
            {
                txtPhoneFirstPart.setText(txtPhoneFirstPart.getText()+ server.PhoneNumber.substring(count++, count));
            }
            else
            {
                txtPhoneSecondPart.setText(txtPhoneSecondPart.getText()+ server.PhoneNumber.substring(count++, count));
            }
        }

        txtEmail.setText(server.Email);
        txtAddress.setText(server.Address);
        txtNote.setText(server.Note);
    }
    private void DeleteItemPicDialog()
    {
        if(imgItemPic.getTag()==null)return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Option");
        builder.setIcon(common.Utility.ReturnMessageBoxSizeIcon(R.drawable.question));
        builder.setMessage("Delete item picture?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                tvRemovePic.setVisibility(View.INVISIBLE);
                ((PhotoFeatureFragment)parentFragment).DeleteExistingItemPic((String) imgItemPic.getTag());
                imgItemPic.setBackground(getContext().getResources().getDrawable(R.drawable.photo_not_available));
                imgItemPic.setImageBitmap(null);
                imgItemPic.setTag(null);//reset
                if(server !=null) server.PicturePath="";

                dialogInterface.dismiss();

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {

            }
        });
        builder.show();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_add_server_dialog);
        //currentView = view;

        imgItemPic = (ImageView)findViewById(R.id.imgItemPic);
        imgItemPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPhoto();
            }
        });

        tvRemovePic = (TextView)findViewById(R.id.tvRemovePic);
        tvRemovePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imgItemPic.getTag() != null) {

                    DeleteItemPicDialog();
                }
            }
        });



        TextView tv = (TextView)findViewById(R.id.tvName);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        txtName = (EditText)findViewById(R.id.txtName);
        txtName.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        txtName.setHint(getContext().getResources().getString(R.string.hint_server_name).replace("%1$d",
                getContext().getResources().getString(R.string.server_name_max_char)));
        txtName.setFilters(common.Utility.CreateMaxLengthFilter(common.text_and_length_settings.SERVER_NAME_MAX_LENGTH));

        txtPhoneAreaCode = (EditText)findViewById(R.id.txtPhoneAreaCode);
        txtPhoneFirstPart = (EditText)findViewById(R.id.txtPhoneFirstPart);
        txtPhoneSecondPart = (EditText)findViewById(R.id.txtPhoneSecondPart);
        SetTextAttribute(findViewById(R.id.tvPhone));
        SetTextAttribute(txtPhoneAreaCode);
        SetTextAttribute(findViewById(R.id.tvPhoneCloseParenthesis));
        SetTextAttribute(txtPhoneFirstPart);
        SetTextAttribute(findViewById(R.id.tvPhoneDash1));
        SetTextAttribute(txtPhoneSecondPart);




        TextView tvEmail = (TextView)findViewById(R.id.tvEmail);
        tvEmail.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvEmail.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtEmail.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        txtEmail.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        txtEmail.setHint(getContext().getResources().getString(R.string.hint_email).replace("%1$d",
                getContext().getResources().getString(R.string.email_address_max_char)));

        TextView tvAddress = (TextView)findViewById(R.id.tvAddress);
        tvAddress.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvAddress.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        txtAddress = (EditText)findViewById(R.id.txtAddress);
        txtAddress.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        txtAddress.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        TextView tvNote = (TextView)findViewById(R.id.tvNote);
        tvNote.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvNote.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        txtNote = (EditText)findViewById(R.id.txtNote);
        txtNote.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        txtNote.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        rdMale = (RadioButton)findViewById(R.id.rdMale);
        rdMale.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        rdMale.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        rdMale.setChecked(true);

        rdFemale = (RadioButton)findViewById(R.id.rdFemale);
        rdFemale.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        rdFemale.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        TextView tvOK = (TextView)findViewById(R.id.tvOK);
        tvOK.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
        tvOK.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (server == null) {
                    AddServer();
                } else {
                    UpdateServer();
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

        if(server !=null)
        DisplayServerInfo();
    }

    protected   void SelectPhoto()
    {
        final CharSequence[] items = { "Take Photo", "Choose From Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Item Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment
                            .getExternalStorageDirectory(), TemporaryCameraPictureFileName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    parentFragment.startActivityForResult(intent, Enum.ChoosePhotoFrom.camera.value);
                } else if (items[item].equals("Choose From Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    parentFragment.startActivityForResult(intent.createChooser(intent, "Select File"), Enum.ChoosePhotoFrom.gallery.value);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }
    private void SetTextAttribute(View v)
    {
        if(v instanceof TextView )
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
}
