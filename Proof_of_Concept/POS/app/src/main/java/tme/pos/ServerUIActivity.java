package tme.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.GenericVerticalFlipableTableRow;


/**
 * Created by vanlanchoy on 4/12/2015.
 */
public class ServerUIActivity extends Activity {
    TableLayout tblServer;
    RadioButton rdFemale;
    RadioButton rdMale;
    EditText txtName;
    TextView labelName;
    //TextView labelGender;
    FrameLayout IdPlaceHolder;
    //EditText txtPhoneAreaCode;
    //EditText txtPhoneFirstPart;
    //EditText txtPhoneSecondPart;
    //Button btnAdd;
    //Button btnCancel;
    boolean blnEditMode=false;

    private void EditMode(final Server s)
    {
        blnEditMode = true;
        //btnAdd.setText("Save");
        txtName.setText(s.Name);
        //btnCancel.setVisibility(View.VISIBLE);
        if(s.gender== Enum.ServerGender.male)
        {
            rdMale.setChecked(true);
            rdFemale.setChecked(false);
        }
        else
        {
            rdFemale.setChecked(true);
            rdMale.setChecked(false);
        }

        if(s.PhoneNumber.length()>0)
        {
            if(s.PhoneNumber.length()>10)//11 digits
            {
               /* txtPhoneAreaCode.setText(s.PhoneNumber.substring(0, 3));
                txtPhoneFirstPart.setText(s.PhoneNumber.substring(3,7));
                txtPhoneSecondPart.setText(s.PhoneNumber.substring(7));*/
            }
            else
            {
                //10 digits
                /*txtPhoneAreaCode.setText(s.PhoneNumber.substring(0, 3));
                txtPhoneFirstPart.setText(s.PhoneNumber.substring(3,6));
                txtPhoneSecondPart.setText(s.PhoneNumber.substring(6));*/
            }
        }

       /* btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateServer(s.EmployeeId);
            }
        });*/
    }

  /*  private void CancelEdit()
    {
        btnAdd.setText("Add");
        btnCancel.setVisibility(View.INVISIBLE);
        txtName.setText("");
       *//* txtPhoneAreaCode.setText("");
        txtPhoneFirstPart.setText("");
        txtPhoneSecondPart.setText("");*//*
        blnEditMode=false;
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddServer();
            }
        });
    }*/
    @Override
    protected  void onResume()
    {
        Log.d("Server activity Info", "on resume");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);
    }
    @Override
    protected void onCreate(Bundle savedIntanceState)
    {
        ((POS_Application)getApplication()).setCurrentActivity(this);
        super.onCreate(savedIntanceState);
        setContentView(R.layout.layout_server_ui);


        TextView tv = (TextView)findViewById(R.id.tvName);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

        txtName = (EditText)findViewById(R.id.txtName);
        txtName.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        txtName.setHint(getResources().getString(R.string.hint_server_name).replace("%1$d",
                getResources().getString(R.string.server_name_max_char)));
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter( Integer.parseInt(getResources().getString(R.string.server_name_max_char)));
        txtName.setFilters(filterArray);

       /* txtPhoneAreaCode = (EditText)findViewById(R.id.txtPhoneAreaCode);
        txtPhoneFirstPart = (EditText)findViewById(R.id.txtPhoneFirstPart);
        txtPhoneSecondPart = (EditText)findViewById(R.id.txtPhoneSecondPart);
        SetTextAttribute(findViewById(R.id.tvPhone));
        SetTextAttribute(txtPhoneAreaCode);
        SetTextAttribute(findViewById(R.id.tvPhoneCloseParenthesis));
        SetTextAttribute(txtPhoneFirstPart);
        SetTextAttribute(findViewById(R.id.tvPhoneDash1));
        SetTextAttribute(txtPhoneSecondPart);*/
        //trNoContent = (TableRow)findViewById(R.id.trNoContent);
        tblServer = (TableLayout)findViewById(R.id.tblServer);

       /* btnAdd = (Button)findViewById(R.id.btnAdd);
        btnAdd.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        btnAdd.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //AddServer();
            }
        });
        common.control_events.SetOnTouchEffect(btnAdd);*/

       /* btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CancelEdit();
            }
        });
        common.control_events.SetOnTouchEffect(btnCancel);
*/
        rdMale = (RadioButton)findViewById(R.id.rdMale);
        rdMale.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        rdMale.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        rdMale.setChecked(true);

        rdFemale = (RadioButton)findViewById(R.id.rdFemale);
        rdFemale.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        rdFemale.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);

       /* labelGender = (TextView)findViewById(R.id.labelGender);
        labelGender.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        labelGender.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);*/

        /*labelName = (TextView)findViewById(R.id.labelName);
        labelName.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        labelName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);*/

        //SetTextAttribute(findViewById(R.id.labelContact));

        //IdPlaceHolder = (FrameLayout)findViewById(R.id.idPlaceHolder);

        ReloadServerTable(-1);//new added employee id set to -1 in order not to trigger animation
    }
    public void Close(View v)
    {
        finish();
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
    private boolean IsExist(String strName)
    {
        for(int i=0;i<common.serverList.GetServers().length;i++)
        {
            if(common.serverList.GetServers()[i].Name.equalsIgnoreCase(strName))
            {

                return true;
            }
        }

        return false;
    }
    /*private void UpdateServer(long lngEmployeeId)
    {
        if(!Validate())return;
        Server s=null;
        for(int i=0;i<common.servantList.GetServers().length;i++)
        {
            if(common.servantList.GetServers()[i].EmployeeId==lngEmployeeId)
            {
                s = common.servantList.GetServers()[i];
            }
        }
        if(s!=null) {
            s.Name = txtName.getText().toString().trim();
            //s.PhoneNumber = txtPhoneAreaCode.getText().toString() + txtPhoneFirstPart.getText() + txtPhoneSecondPart.getText() + "";
            if (rdMale.isChecked()) {

                s.gender = Enum.ServerGender.male;
            } else {

                s.gender = Enum.ServerGender.female;
            }
            common.servantList.Update(s);
            ReloadServerTable(lngEmployeeId);
        }
        CancelEdit();
    }*/
    private boolean Validate()
    {
        if(common.serverList.GetServers().length>=common.text_and_length_settings.MAX_SERVER)
        {
            ShowMessage("Add Server","You have reached the maximum of "+common.text_and_length_settings.MAX_SERVER+" servsers.",R.drawable.no_access);
            return false;
        }

        String strName = txtName.getText().toString().trim();

        if(IsExist(strName))
        {
            ShowMessage("Add Server","Name already existed on the list.",R.drawable.no_access);
            return false;
        }
        if(strName.length()==0)
        {
            ShowMessage("Add Server","Please provide a valid name.",R.drawable.no_access);
            return false;
        }
        /*int intNumberLength =txtPhoneAreaCode.getText().length()+txtPhoneFirstPart.getText().length()+txtPhoneSecondPart.getText().length();
        if(intNumberLength>0 && intNumberLength<10)
        {
            ShowMessage("Add Server","Please provide a valid phone number.",R.drawable.no_access);
            return false;
        }*/
        return true;
    }
 /*   private void AddServer()
    {
        if(!Validate()) return;
        String strName = txtName.getText().toString().trim();
        *//*if(common.servantList.GetServers().length>=common.text_and_length_settings.MAX_SERVANT)
        {
            ShowMessage("Add Server","You have reached the maximum of "+common.text_and_length_settings.MAX_SERVANT+" servants.");
            return;
        }

        String strName = txtName.getText().toString().trim();

        if(IsExist(strName))
        {
            ShowMessage("Add Server","Name already existed on the list.");
            return;
        }
        if(strName.length()==0)
        {
            ShowMessage("Add Server","Please provide a valid name.");
            return;
        }
        int intNumberLength =txtPhoneAreaCode.getText().length()+txtPhoneFirstPart.getText().length()+txtPhoneSecondPart.getText().length();
        if(intNumberLength>0 && intNumberLength<10)
        {
            ShowMessage("Add Server","Please provide a valid phone number.");
            return;
        }*//*
        DatabaseHelper helper = new DatabaseHelper(this);
        Server servant = new Server();
        servant.EmployeeId=helper.GenerateNextServerId();
        servant.Name = strName;
        //servant.PhoneNumber = txtPhoneAreaCode.getText().toString()+txtPhoneFirstPart.getText()+txtPhoneSecondPart.getText()+"";
        if(rdMale.isChecked())
        {

            servant.gender = Enum.ServerGender.male;
        }
        else
        {

            servant.gender = Enum.ServerGender.female;
        }

        servant.IsActive= true;

        //ShowMessage("before insert","length="+ common.servantList.GetServers().length);
        long id = common.servantList.Insert(servant);
        //ShowMessage("after insert","length="+ common.servantList.GetServers().length);


        if(id>-1)
        {
            //add the servant on the table

            ReloadServerTable(servant.EmployeeId);
        }
        else {
            ShowMessage("Add Server", "Insert new servant failed.",R.drawable.exclaimation);
        }
    }*/
    private void RemoveServerFromTable(long EmployeeId)
    {
        int index=tblServer.getChildCount()-1;
        for(int i =tblServer.getChildCount()-1;i>=0;i--)
        {
               if(Long.parseLong(tblServer.getChildAt(i).getTag().toString())==EmployeeId)
               {
                   tblServer.removeViewAt(i);
               }
               else
               {
                    //update the id on table
                   ((TextView)((GenericVerticalFlipableTableRow)tblServer.getChildAt(i)).getChildAt(0)).setText(index--+"");
               }
            if((i&1)==0)tblServer.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.very_light_grey));
            else tblServer.getChildAt(i).setBackground(null);
        }

        if(tblServer.getChildCount()==0)
        {
            tblServer.addView(CreateNoContentRow());
        }
    }
    private void ReloadServerTable(long NewEmployeeId)
    {
        //remove all row except the default no content row
        tblServer.removeAllViews();


        for(int i=0;i<common.serverList.GetServers().length;i++)
        {
            GenericVerticalFlipableTableRow row = CreateServerRow(common.serverList.GetServers()[i], Short.parseShort(i + 1 + ""));
            row.setTag(common.serverList.GetServers()[i].EmployeeId);
            tblServer.addView(row);
            if(NewEmployeeId==common.serverList.GetServers()[i].EmployeeId)
            {

                row.setVisibility(View.INVISIBLE);

                SlideIn(!common.myAppSettings.SwipeLeftToDelete(),row);
            }
            /*else
            {

                //tblServer.addView(row);
            }*/
        }
        if(common.serverList.GetServers().length==0)
        {

            tblServer.addView(CreateNoContentRow());
            //trNoContent.setVisibility(View.GONE);
        }



    }
    private void SlideOut(boolean flgSwipeLeftToDelete,final GenericVerticalFlipableTableRow row)
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
                RemoveServerFromTable(Long.parseLong(row.getTag().toString()));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        row.startAnimation(movement);

    }
    public void SlideIn(boolean blnSlideRight,final GenericVerticalFlipableTableRow row)
    {
        //currentView = this;
        //final TableLayout parentTbl = (TableLayout)currentView.getParent();
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
                if((index&1)==0)row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
                else row.setBackground(null);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        row.startAnimation(movement);

    }
    private GenericVerticalFlipableTableRow CreateNoContentRow()
    {
        GenericVerticalFlipableTableRow row = new GenericVerticalFlipableTableRow(this) {
            @Override
            protected void SingleTapped() {

            }

            @Override
            protected void ShowConfirmation() {
            }
        };

        TextView tv = new TextView(this);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tv.setText("No Server");
        //tv.setBackgroundColor(getResources().getColor(R.color.green));

        tv.setGravity(Gravity.CENTER);
        //TableRow.LayoutParams trlp = new TableRow.LayoutParams(labelGender.getLayoutParams().width+labelName.getLayoutParams().width+IdPlaceHolder.getLayoutParams().width, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(labelName.getLayoutParams().width+IdPlaceHolder.getLayoutParams().width, ViewGroup.LayoutParams.WRAP_CONTENT);
        //TableRow.LayoutParams trlp = new TableRow.LayoutParams(500, ViewGroup.LayoutParams.WRAP_CONTENT);
        //trlp.span=3;
        //trlp.column=1;
        tv.setLayoutParams(trlp);
        row.addView(tv);
        return row;
    }
    private void DeleteServer(long serverId)
    {


        //DatabaseHelper helper = new DatabaseHelper(this);
        //int result =helper.DeleteServer(servantId);
        int result =common.serverList.Delete(serverId);
        if(result>0){
            common.serverList.Delete(serverId);
            GenericVerticalFlipableTableRow animeRow=null;
            for(int i=0;i<tblServer.getChildCount();i++)
            {
                if(Long.parseLong(tblServer.getChildAt(i).getTag().toString())==serverId)
                {
                    animeRow =(GenericVerticalFlipableTableRow) tblServer.getChildAt(i);
                }
            }
            if(animeRow!=null){
                SlideOut(!common.myAppSettings.SwipeLeftToDelete(),animeRow);
            }

            //ReloadServerTable(servantId);

        }

    }

    private GenericVerticalFlipableTableRow CreateServerRow(final Server server, final short index)
    {
        GenericVerticalFlipableTableRow row = new GenericVerticalFlipableTableRow(this) {
            @Override
            protected void SingleTapped() {

            }

            @Override
            protected void ShowConfirmation() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Server Option");
                builder.setMessage("What would you like to do?");
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DeleteServer(server.EmployeeId);
                        dialogInterface.dismiss();

                    }
                });
                builder.setNeutralButton("Edit",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditMode(server);
                    }
                });
                builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if((index&1)==0){setBackgroundColor(getResources().getColor(R.color.very_light_grey));}
                        else {setBackground(null);}

                    }
                });
                builder.show();

            }
        };

        TextView tv = new TextView(this);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setText(index + "");//servant.EmployeeId+"");
        tv.setLayoutParams(new TableRow.LayoutParams(80,TableRow.LayoutParams.MATCH_PARENT));
        //tv.getLayoutParams().width = 80;
        tv.setGravity(Gravity.CENTER);

        //tv.setBackgroundColor(getResources().getColor(R.color.green));
        row.addView(tv);


     /*   ImageView iv = new ImageView(this);
        if(servant.gender== Enum.ServerGender.male) {
            iv.setImageResource(R.drawable.male);
        }
        else
        {
            iv.setImageResource(R.drawable.female);
        }


        iv.setBackgroundColor(getResources().getColor(R.color.transparent));
        iv.setLayoutParams(new TableRow.LayoutParams(100,60));

        row.addView(iv);*/

        tv = new TextView(this);
        tv.setText(server.Name.toUpperCase());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(450, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.LEFT);

        row.addView(tv);


      /*  //phone number
        String strFormattedPhoneNumber="(";
        if(servant.PhoneNumber.length()==10) {
            for (int i = 0; i < servant.PhoneNumber.length(); i++) {
                strFormattedPhoneNumber += servant.PhoneNumber.toCharArray()[i];
                if (i == 2) strFormattedPhoneNumber += ")";
                else if (i == 5) strFormattedPhoneNumber += "-";
            }
        }
        else if(servant.PhoneNumber.length()==11)
        {
            for (int i = 0; i < servant.PhoneNumber.length(); i++) {
                strFormattedPhoneNumber += servant.PhoneNumber.toCharArray()[i];
                if (i == 2) strFormattedPhoneNumber += ")";
                else if (i == 6) strFormattedPhoneNumber += "-";
            }
        }
        else{strFormattedPhoneNumber=servant.PhoneNumber;}*/

       /* tv = new TextView(this);
        tv.setText((strFormattedPhoneNumber.length()>0)?strFormattedPhoneNumber:"");
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(200, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        row.addView(tv);*/

        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tllp.topMargin=10;
        row.setLayoutParams(tllp);
        if((index&1)==0)row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
        //row.setTag(servant.EmployeeId);
        return row;
    }
   /* public void SetOnTouchEffect(Button btn)
    {
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    //ShowMessage("On Touch",motionEvent.getAction()+" down");
                    ((Button) view).setTextColor(getResources().getColor(R.color.white));
                    view.setBackgroundColor(getResources().getColor(R.color.green));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    //ShowMessage("On Touch",motionEvent.getAction()+"");
                    ((Button) view).setTextColor(getResources().getColor(R.color.light_green));
                    view.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
                }

                return false;
            }
        });
    }*/
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
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
