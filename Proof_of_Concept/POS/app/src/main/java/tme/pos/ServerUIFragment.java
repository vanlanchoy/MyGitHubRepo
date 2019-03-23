package tme.pos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;


/**
 * Created by kchoy on 6/8/2015.
 */
public class ServerUIFragment extends PhotoFeatureFragment {
    TableLayout tblServer;
    ScrollView scrServerTable;
    //FrameLayout IdPlaceHolder;
    ImageView imgEditServer;
    ImageView imgDeleteServer;
    ImageView imgServerPic;
    ImageView imgGratuityChart;
    TextView tvServerName;
    ImageView imgGender;
    TextView tvPhone;
    TextView tvEmail;
    TextView tvAddress;
    TextView tvNote;

    int rowWidth;
    //int intSelectedRowIndex=-1;
    AddServerDialog addServerDialog;
    float propertiesPanelHeight;
    Server selectedServer;


    @Override
    protected void SavedPictureResult(String strSavedPicPath) {

        if(addServerDialog !=null) addServerDialog.SavedPictureResult(strSavedPicPath);

    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle bundle)
    {
        final View rootView =inflater.inflate(R.layout.layout_server_ui,container,false);

        rowWidth = rootView.findViewById(R.id.scrServerTable).getLayoutParams().width;
        propertiesPanelHeight=118;

        imgEditServer = (ImageView)rootView.findViewById(R.id.imgEditServer);
        imgEditServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addServerDialog = new AddServerDialog(getActivity(),ServerUIFragment.this, selectedServer);
                addServerDialog.show();
            }
        });
        imgDeleteServer  = (ImageView)rootView.findViewById(R.id.imgDeleteServer);
        imgDeleteServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDeleteServerDialog();
            }
        });

        imgServerPic = (ImageView)rootView.findViewById(R.id.imgServerPic);
        imgGratuityChart = (ImageView)rootView.findViewById(R.id.imgGratuityChart);
        imgGratuityChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ServerGratuityDetailDialog(getActivity(),selectedServer.EmployeeId).show();
            }
        });
        tvServerName = (TextView)rootView.findViewById(R.id.tvServerName);
        imgGender = (ImageView)rootView.findViewById(R.id.imgGender);
        tvPhone = (TextView)rootView.findViewById(R.id.tvPhone);
        tvEmail = (TextView)rootView.findViewById(R.id.tvEmail);
        tvAddress = (TextView)rootView.findViewById(R.id.tvAddress);
        tvNote = (TextView)rootView.findViewById(R.id.tvNote);

        //ServerPropertiesPlaceHolder = (FrameLayout)rootView.findViewById(R.id.ServerPropertiesPlaceHolder);
        tblServer = (TableLayout)rootView.findViewById(R.id.tblServer);
        //tblLabel = (TableLayout)rootView.findViewById(R.id.tblLabel);
        scrServerTable = (ScrollView)rootView.findViewById(R.id.scrServerTable);

        rootView.findViewById(R.id.imgAddServer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 addServerDialog = new AddServerDialog(getActivity(),ServerUIFragment.this);
                addServerDialog.show();
            }
        });

       /* imgBtnCollapse = (ImageButton)rootView.findViewById(R.id.imgBtnCollapse);
        imgBtnCollapse.setTag(Enum.ExpandStatus.expanded);
        imgBtnCollapse.setBackground(getResources().getDrawable(R.drawable.collapse));
        imgBtnCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blnAnimationStart) return;
                blnAnimationStart = true;
                if ((Enum.ExpandStatus) imgBtnCollapse.getTag() == Enum.ExpandStatus.expanded) {
                    //ShowMessage("clicked","collapse");
                    ShowPropertiesPanel(false);
                } else {
                    //ShowMessage("clicked","expand");
                    ShowPropertiesPanel(true);
                }
            }
        });*/



        //IdPlaceHolder = (FrameLayout)rootView.findViewById(R.id.idPlaceHolder);

        ReloadServerTable(-1);//new added employee id set to -1 in order not to trigger animation

        //hide softkeyboard
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                common.control_events.HideSoftKeyboard(rootView.findViewById(R.id.tblServer));
            }
        }, 1000);

        return rootView;
    }




  /*  private void SetServerRowBackgroundColor()
    {
        for(int i =0;i<tblServer.getChildCount();i++)
        {
            if((i&1)==0)tblServer.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.very_light_grey));
            else tblServer.getChildAt(i).setBackground(null);
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
                ((TextView)((TableRow)tblServer.getChildAt(i)).getChildAt(0)).setText(index-- + "");
            }

        }

        if(tblServer.getChildCount()==0)
        {
            tblServer.addView(CreateNoContentRow());
        }
       RepaintRowBackground();
       //SetServerRowBackgroundColor();
    }
    public void ReloadServerTable(long NewEmployeeId)
    {
        //remove all row except the default no content row
        tblServer.removeAllViews();


        for(int i=0;i<common.serverList.GetServers().length;i++)
        {
            TableRow row = CreateServerRow(common.serverList.GetServers()[i], Short.parseShort(i + ""));
            row.setTag(common.serverList.GetServers()[i].EmployeeId);
            tblServer.addView(row);
            if(NewEmployeeId==common.serverList.GetServers()[i].EmployeeId)
            {

                row.setVisibility(View.INVISIBLE);
                //row.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                SlideIn(!common.myAppSettings.SwipeLeftToDelete(), row);
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
    protected void UpdateTableRowName()
    {
        ReloadServerTable(selectedServer.EmployeeId);
       /* for(int i=0;i<tblServer.getChildCount();i++)
        {
            if(((Long)tblServer.getChildAt(i).getTag())==selectedServer.EmployeeId)
            {
                ((TextView)((TableRow)tblServer.getChildAt(i)).getChildAt(1)).setText(selectedServer.Name.toUpperCase());
                break;
            }
        }*/
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
    public void SlideIn(boolean blnSlideRight,final TableRow row)
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
                index--;
                if(selectedServer==null)
                {
                    if ((index % 2) != 0)
                        row.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
                    else row.setBackground(null);
                }
                else if(selectedServer.EmployeeId!=(Long)row.getTag())
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
    private TableRow CreateNoContentRow()
    {
        TableRow row = new TableRow(getActivity()) ;

        TextView tv = new TextView(getActivity());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tv.setText("No Server");
        tv.setTextColor(getResources().getColor(R.color.common_signin_btn_light_text_disabled));
        //tv.setBackgroundColor(getResources().getColor(R.color.green));

        tv.setGravity(Gravity.CENTER);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(common.Utility.DP2Pixel(rowWidth,getActivity()), ViewGroup.LayoutParams.WRAP_CONTENT);
        //TableRow.LayoutParams trlp = new TableRow.LayoutParams(500, ViewGroup.LayoutParams.WRAP_CONTENT);
        //trlp.span=3;
        //trlp.column=1;
        tv.setLayoutParams(trlp);
        row.addView(tv);
        return row;
    }
    private void ClearServerDetailPanel()
    {
        tvServerName.setText("");
        imgGender.setVisibility(View.INVISIBLE);


        tvPhone.setText("");
        tvEmail.setText("");
        tvAddress.setText("");
        tvNote.setText("");
        imgServerPic.setImageBitmap(null);
        imgServerPic.setTag(null);


        imgDeleteServer.setVisibility(View.INVISIBLE);
        imgEditServer.setVisibility(View.INVISIBLE);
        imgGratuityChart.setVisibility(View.INVISIBLE);
    }
    private void DeleteServer(long serverId)
    {


        //DatabaseHelper helper = new DatabaseHelper(this);

        int result =common.serverList.Delete(serverId);
        if(result>0){
            //common.servantList.Delete(servantId);
            TableRow animeRow=null;
            for(int i=0;i<tblServer.getChildCount();i++)
            {
                if(Long.parseLong(tblServer.getChildAt(i).getTag().toString())==serverId)
                {
                    animeRow =(TableRow) tblServer.getChildAt(i);
                }
            }
            if(animeRow!=null){
                SlideOut(!common.myAppSettings.SwipeLeftToDelete(),animeRow);
            }
            selectedServer = null;
            //intSelectedRowIndex=-1;
            ClearServerDetailPanel();
            //ReloadServerTable(-1);

        }

    }
   /* private void ClearAllInputFields()
    {imgGratuityChart
        txtName.setText("");
        txtPhoneAreaCode.setText("");
        txtPhoneFirstPart.setText("");
        txtPhoneSecondPart.setText("");
    }*/
    protected void DisplayServerDetail(Server server)
    {
        tvServerName.setText(server.Name);
        imgGender.setVisibility(View.VISIBLE);
        if(server.gender== Enum.ServerGender.male)
            imgGender.setBackground(getResources().getDrawable(R.drawable.male));
        else
            imgGender.setBackground(getResources().getDrawable(R.drawable.female));

        tvPhone.setText(common.Utility.ConvertString2PhoneFormat(server.PhoneNumber));
        tvEmail.setText(server.Email);
        tvAddress.setText(server.Address);
        tvNote.setText(server.Note);
        imgServerPic.setImageBitmap(null);
        imgServerPic.setTag(null);
        if(server.PicturePath.length()>0) {
            imgServerPic.setTag(server.PicturePath);
            imgServerPic.setImageBitmap(DecodeBitmapFile(server.PicturePath));
        }

        imgDeleteServer.setVisibility(View.VISIBLE);
        imgEditServer.setVisibility(View.VISIBLE);
        imgGratuityChart.setVisibility(View.VISIBLE);
    }
    private void RepaintRowBackground()
    {
        for(int i=0;i<tblServer.getChildCount();i++)
        {
            if ((i % 2) != 0)
                tblServer.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.very_light_grey));
            else
                tblServer.getChildAt(i).setBackgroundColor(Color.WHITE);
        }
    }
    private TableRow CreateServerRow(final Server server, final short index)
    {
        TableRow tr = new TableRow(getActivity());
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RepaintRowBackground();
               /* if(intSelectedRowIndex>-1)
                {

                    if((index%2)!=0)
                        tblServer.getChildAt(intSelectedRowIndex).setBackgroundColor(getResources().getColor(R.color.very_light_grey));
                    else
                        tblServer.getChildAt(intSelectedRowIndex).setBackgroundColor(Color.WHITE);
                }*/
                //intSelectedRowIndex = index;
                view.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                selectedServer = server;
                DisplayServerDetail(server);
            }
        });


        TextView tv = new TextView(getActivity());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SERVER_FRAGMENT_TEXT_SIZE);
        tv.setText((index+1) + "");
        tv.setLayoutParams(new TableRow.LayoutParams(80, TableRow.LayoutParams.MATCH_PARENT));

        tv.setGravity(Gravity.CENTER);


        tr.addView(tv);


        /*ImageView iv = new ImageView(getActivity());
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

        tv = new TextView(getActivity());
        tv.setText(server.Name.toUpperCase());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SERVER_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(450, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.LEFT);

        tr.addView(tv);


       /* //phone number
        String strFormattedPhoneNumber = common.Utility.ReformatPhoneString(servant.PhoneNumber);
        tv = new TextView(getActivity());
        tv.setText((strFormattedPhoneNumber.length()>0)?strFormattedPhoneNumber:"");
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(200, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        row.addView(tv);*/

        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tllp.topMargin=10;
        tr.setLayoutParams(tllp);
        if((index%2)!=0)tr.setBackgroundColor(getResources().getColor(R.color.very_light_grey));
        //row.setTag(servant.EmployeeId);
        return tr;
    }

    private void ShowDeleteServerDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Option");
        builder.setIcon(common.Utility.ReturnMessageBoxSizeIcon(R.drawable.question));
        builder.setMessage("Delete server?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteServer(selectedServer.EmployeeId);
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
