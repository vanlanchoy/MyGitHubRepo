package tme.pos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.Inventory;
import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.Supplier;
import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.GenericVerticalFlipableTableRow;
import tme.pos.DataAccessLayer.DatabaseHelper;
import tme.pos.Interfaces.IDialogActivityListener;
import tme.pos.Interfaces.IItemViewUpdateUnit;

/**
 * Created by kchoy on 5/25/2016.
 */
public class ItemInventoryOptionDialog extends Dialog implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener

{









    ItemObject item;
    FrameLayout InventoryPropertiesPlaceHolder;
    TableLayout tblInventoryRecord;
    boolean blnAnimationStart;
    LinearLayout llInventoryProperties;
    final float propertiesPanelHeight=230;
    final int offset = 5;
    ImageButton imgBtnInventoryPanelCollapse;
    TableLayout tblInventoryLabel;
    ScrollView scrInventoryTable;
    int rowWidth;
    boolean blnEditMode;
    IItemViewUpdateUnit callback;
    Button btnAdd;
    Button btnCancel;
    TextView tvTime;
    TextView tvDate;
    EditText txtCostBasis;
    EditText txtUnit;
    TextView tvUnitAvailable;


    Spinner ddlSupplier;
    IDialogActivityListener listener;
             boolean blnDateDialogShow=false;
             boolean blnTimeDialogShow=false;
    public ItemInventoryOptionDialog(Context context,long itemId,IDialogActivityListener l,IItemViewUpdateUnit callback)
    {
        super(context);

        this.item = common.myMenu.GetLatestItem(itemId);
        listener = l;
        this.callback = callback;
    }
    private String ReturnTimeString(int hour_in_24,int minute)
    {
        return  ((hour_in_24>12)?(hour_in_24-12):hour_in_24)+":"+
                ((minute>9)?minute:"0"+minute)+
                " "+((hour_in_24)>11?"PM":"AM");
    }
    private String ReturnDateString(int month,int day,int year)
    {
        return ((month > 9) ? month : "0" + month) +
                "/" +
                ((day > 9) ? day : "0" + day) + "/" +
                year;
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {

        if(!blnDateDialogShow)return;
        if(blnDateDialogShow)blnDateDialogShow = false;
        tvDate.setText(ReturnDateString(month + 1, day, year));

    }
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(!blnTimeDialogShow)return;
        if(blnTimeDialogShow)blnTimeDialogShow = false;
        tvTime.setText(ReturnTimeString(hourOfDay, minute));

    }
    private boolean Validate()
    {
        int intUnit = Integer.parseInt((txtUnit.getText().toString().length() == 0) ? "0" : txtUnit.getText().toString());

        if(intUnit<1)
        {
            common.Utility.ShowMessage("Inventory","Unit cannot be zero.",getContext(),R.drawable.no_access);
            return false;
        }
        if(ddlSupplier.getSelectedItemPosition()==0)
        {
            common.Utility.ShowMessage("Inventory", "Please select a supplier or create a new one.",getContext(), R.drawable.no_access);
            return false;
        }

        return true;
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
                RemoveInventoryRecordFromTable((Long) row.getTag());

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
        int rowIndex=-1;
        //final TableLayout parentTbl = (TableLayout)currentView.getParent();
        TranslateAnimation movement = new TranslateAnimation(-5000.0f, 0.0f, 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide in to right
            movement = new TranslateAnimation(5000.0f, 0.0f, 0.0f, 0.0f);//move left
        }



        //get row index in table
        for(int i=0;i<tblInventoryRecord.getChildCount();i++)
        {
            if(tblInventoryRecord.getChildAt(i).equals(row))
            {
                rowIndex = i;
                break;
            }
        }
        final int index = rowIndex;
        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                row.setVisibility(View.VISIBLE);
                row.setBackgroundResource(R.drawable.draw_table_row_border);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if ((index & 1) == 0)
                    row.setBackgroundColor(getContext().getResources().getColor(R.color.very_light_grey));
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
        GenericVerticalFlipableTableRow row = new GenericVerticalFlipableTableRow(getContext()) {
            @Override
            protected void SingleTapped() {

            }

            @Override
            protected void ShowConfirmation() {
            }
        };
        TextView tv = new TextView(getContext());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setText("No Record");

        tv.setLayoutParams(new TableRow.LayoutParams(common.Utility.DP2Pixel(rowWidth,getContext()), TableRow.LayoutParams.MATCH_PARENT));
        //tv.getLayoutParams().width = 80;
        tv.setGravity(Gravity.CENTER);

        tv.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        row.addView(tv);
        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tllp.topMargin=10;
        row.setLayoutParams(tllp);
        return row;
    }

    private void ReloadInventoryList(long itemId,long NewInventoryRecordId)
    {
        tblInventoryRecord.removeAllViews();
        ArrayList<Inventory> inventories = common.inventoryList.GetRecords(itemId);

        if(inventories.size()==0)
        {
            tblInventoryRecord.addView(CreateNoContentRow());
        }
        else
        {
            for(int i=0;i<inventories.size();i++) {
                GenericVerticalFlipableTableRow row =CreateInventoryRow(i, inventories.get(i));
                row.setTag(inventories.get(i).lngInventoryId);
                tblInventoryRecord.addView(row);

                if (NewInventoryRecordId == inventories.get(i).lngInventoryId) {

                    row.setVisibility(View.INVISIBLE);

                    SlideIn(!common.myAppSettings.SwipeLeftToDelete(), row);
                }
            }
        }

    }
    private void UpdateInventory(long inventoryId,long itemId)
    {
        if(!Validate())return;
        Inventory inventory=null;
        for(int i=0;i<common.inventoryList.GetRecords(itemId).size();i++)
        {
            if(common.inventoryList.GetRecords(itemId).get(i).lngInventoryId==inventoryId)
            {
                inventory = common.inventoryList.GetRecords(itemId).get(i);
            }
        }
        if(inventory!=null) {
            inventory.CostPrice = new BigDecimal(common.Utility.ConvertCurrencyFormatToBigDecimalString(txtCostBasis.getText().toString()));
            inventory.UnitCount = Integer.parseInt(txtUnit.getText()+"");
            inventory.lngSupplierId=((Supplier)ddlSupplier.getSelectedItem()).SupplierId;
            String strDateTime = tvDate.getText()+" "+tvTime.getText()+"";
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
            try {
                inventory.RecordDate = sdf.parse(strDateTime);
            }
            catch(ParseException ex) {
                common.Utility.ShowMessage("Add","Failed to add record.",getContext(),R.drawable.exclaimation);
                return;
            }
            //ShowMessage("update inventory","inventory id is "+inventory.lngInventoryId);
            common.inventoryList.Update(inventory);
            ReloadInventoryList(itemId, inventoryId);

            tvUnitAvailable.setText(common.Utility.GetAtTheMomentItemCount(itemId)+"");

        }
        CancelEdit(itemId);
    }
    private void EditMode(final Inventory inventory)
    {
        blnEditMode = true;
        btnAdd.setText("Save");
        txtUnit.setText(inventory.UnitCount + "");
        txtCostBasis.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(inventory.CostPrice));
        tvDate.setText(new SimpleDateFormat("MM/dd/yyyy").format(inventory.RecordDate));
        tvTime.setText(new SimpleDateFormat("hh:mm aa").format(inventory.RecordDate));

        for(int i=0;i<ddlSupplier.getAdapter().getCount();i++)
        {

            if(((Supplier)ddlSupplier.getAdapter().getItem(i)).SupplierId==inventory.lngSupplierId)
            {
                ddlSupplier.setSelection(i);
                break;
            }
        }

        btnCancel.setVisibility(View.VISIBLE);




        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateInventory(inventory.lngInventoryId, inventory.lngItemId);
            }
        });
    }
    private GenericVerticalFlipableTableRow CreateInventoryRow(final int index, final Inventory inventory)
    {
        GenericVerticalFlipableTableRow row = new GenericVerticalFlipableTableRow(getContext()) {
            @Override
            protected void SingleTapped() {

            }

            @Override
            protected void ShowConfirmation() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Inventory Option");
                builder.setMessage("What would you like to do?");
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(common.inventoryList.DeleteInventoryRecord(inventory.lngInventoryId,inventory.lngItemId)>0)
                        {
                            //run animation
                            GenericVerticalFlipableTableRow animeRow=null;
                            for(int ii=0;ii<tblInventoryRecord.getChildCount();ii++)
                            {
                                if((Long)tblInventoryRecord.getChildAt(ii).getTag()==inventory.lngInventoryId)
                                {
                                    animeRow =(GenericVerticalFlipableTableRow) tblInventoryRecord.getChildAt(ii);
                                }
                            }
                            if(animeRow!=null){
                                SlideOut(!common.myAppSettings.SwipeLeftToDelete(),animeRow);
                                tvUnitAvailable.setText(common.Utility.GetAtTheMomentItemCount(item.getID()) + "");
                            }


                        }
                        dialogInterface.dismiss();

                    }
                });
                builder.setNeutralButton("Edit",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditMode(inventory);
                        if(Enum.ExpandStatus.collapsed==imgBtnInventoryPanelCollapse.getTag())
                        {
                            imgBtnInventoryPanelCollapse.callOnClick();
                        }

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

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
        TextView tv = new TextView(getContext());
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setText(sdf.format(inventory.RecordDate));
        tv.setLayoutParams(new TableRow.LayoutParams(250, TableRow.LayoutParams.MATCH_PARENT));
        //tv.getLayoutParams().width = 80;
        tv.setGravity(Gravity.CENTER);

        tv.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        row.addView(tv);



        tv = new TextView(getContext());
        tv.setText(inventory.UnitCount+"");
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(100, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        row.addView(tv);




        tv = new TextView(getContext());
        tv.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(inventory.CostPrice));
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(150, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        row.addView(tv);

        String strSupplier = common.supplierList.GetSupplierName(inventory.lngSupplierId);
        if(strSupplier.length()>10)
        {
            strSupplier = strSupplier.substring(0,8)+"...";
        }
        tv = new TextView(getContext());
        tv.setText(strSupplier);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD_ITALIC);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        tv.setLayoutParams(new TableRow.LayoutParams(200, TableRow.LayoutParams.MATCH_PARENT));
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        row.addView(tv);

        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tllp.topMargin=10;
        row.setLayoutParams(tllp);
        if((index&1)==0)row.setBackgroundColor(getContext().getResources().getColor(R.color.very_light_grey));

        return row;
    }
    private void PropertiesPanelAnimation(final boolean blnExpand,final View v)
    {

        int localOffset = 0;//value doesn't matter here, didn't speed up the process
        if(blnExpand)
        {
            if(propertiesPanelHeight<=v.getLayoutParams().height)
            {
                blnAnimationStart = false;
                //expand process completed
                llInventoryProperties.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                v.getLayoutParams().height = (int)propertiesPanelHeight;
                return;
            }

            v.getLayoutParams().height+=offset;
            v.setVisibility(View.VISIBLE);
            tblInventoryLabel.setTop(tblInventoryLabel.getTop()+(offset+localOffset));
            scrInventoryTable.setTop(scrInventoryTable.getTop()+(offset+localOffset));
            scrInventoryTable.getLayoutParams().height-=offset;

        }
        else
        {
            if(0>=v.getLayoutParams().height)
            {
                blnAnimationStart = false;


                return;
            }

            v.getLayoutParams().height-=offset;

            tblInventoryLabel.setTop(tblInventoryLabel.getTop() - (offset+localOffset));
            scrInventoryTable.setTop(scrInventoryTable.getTop()-(offset+localOffset));
            scrInventoryTable.getLayoutParams().height+=offset;



        }
        tblInventoryRecord.setLayoutParams(new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        v.setLayoutParams(v.getLayoutParams());
        scrInventoryTable.setLayoutParams(scrInventoryTable.getLayoutParams());

        //this will have the panel closing immediately with animation
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                PropertiesPanelAnimation(blnExpand, v);
            }
        });

    }
    private void ShowInventoryPropertiesPanel(final boolean blnShow)
    {

        RotateAnimation ra;

        if(blnShow)
        {
            PropertiesPanelAnimation(true,InventoryPropertiesPlaceHolder);

            ra = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imgBtnInventoryPanelCollapse.setBackground(getContext().getResources().getDrawable(R.drawable.collapse));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            imgBtnInventoryPanelCollapse.setTag(Enum.ExpandStatus.expanded);

        }
        else
        {

            InventoryPropertiesPlaceHolder.setVisibility(View.VISIBLE);
            llInventoryProperties.setVisibility(View.GONE);
            PropertiesPanelAnimation(false,InventoryPropertiesPlaceHolder);

            ra = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            ra.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    imgBtnInventoryPanelCollapse.setBackground(getContext().getResources().getDrawable(R.drawable.expand));
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            imgBtnInventoryPanelCollapse.setTag(Enum.ExpandStatus.collapsed);
        }
        ra.setDuration(300);
        ra.setFillAfter(false);
        imgBtnInventoryPanelCollapse.startAnimation(ra);


    }


    @Override
    public void dismiss() {
        super.dismiss();
        if(callback!=null)
        {
            callback.InventoryDialogUpdateUnitCount(Integer.parseInt(tvUnitAvailable.getText()+""));
        }
    }

    public void ReloadSupplierList()
    {
        ddlSupplier.setAdapter(new SpinnerBaseAdapter<Supplier>(common.supplierList.GetSupplier(),getContext()));

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_inventory_popup_window_ui);
        tvUnitAvailable = (TextView)findViewById(R.id.tvUnitAvailable);
       /* setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {




            }
        });*/
        ((TextView)findViewById(R.id.tvItemName)).setText(item.getName());

        TextView lblDateTime = (TextView)findViewById(R.id.lblDateTime);
        lblDateTime.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        lblDateTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);

        TextView lblUnit = (TextView)findViewById(R.id.lblUnit);
        lblUnit.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        lblUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);

        TextView lblCostBasis = (TextView)findViewById(R.id.lblCostBasis);
        lblCostBasis.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        lblCostBasis.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);

        TextView lblSupplier = (TextView)findViewById(R.id.lblSupplier);
        lblSupplier.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);
        lblSupplier.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);

        TextView tvDateTime = (TextView)findViewById(R.id.tvDateTime);
        tvDateTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        tvDateTime.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);

        TextView tvCostBasis = (TextView)findViewById(R.id.tvCostBasis);
        tvCostBasis.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        tvCostBasis.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);

        TextView tvUnit = (TextView)findViewById(R.id.tvUnit);
        tvUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        tvUnit.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);

        TextView tvSupplier = (TextView)findViewById(R.id.tvSupplier);
        tvSupplier.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        tvSupplier.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.NORMAL);

        tblInventoryLabel = (TableLayout)findViewById(R.id.tblInventoryLabel);

        ddlSupplier = (Spinner)findViewById(R.id.ddlSupplier);
        ddlSupplier.setAdapter(new SpinnerBaseAdapter<Supplier>(common.supplierList.GetSupplier(),getContext()));
        ddlSupplier.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                Supplier s= (Supplier)view.findViewById(R.id.textView1).getTag();

                if(s.SupplierId==-2){
                    if(listener!=null)
                    listener.LaunchSupplierOption(ItemInventoryOptionDialog.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ImageView imgItemPic = (ImageView)findViewById(R.id.imgItemPic);
        common.Utility.LoadPicture(imgItemPic, item.getPicturePath(),getContext());

        imgItemPic.setTag(item.getPicturePath());


        tvUnitAvailable.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
        tvUnitAvailable.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);

        tvUnitAvailable.setText(common.Utility.GetAtTheMomentItemCount(item.getID())+"");

        txtUnit = (EditText)findViewById(R.id.txtUnit);
        txtUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        txtUnit.addTextChangedListener(new TextWatcher() {
            boolean isEditing;
            String strPrevious = "";
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (isEditing) return;
                isEditing = true;
                if(editable.toString().length()>0)
                {
                    int unit=Integer.parseInt(editable.toString());
                    if(unit>=10000)
                    {
                        editable.replace(0, editable.length(), strPrevious);
                    }
                }

                strPrevious = editable.toString();
                isEditing = false;
            }
        });

        //barcode
        if(item.getBarcode()>0) {
            ((TextView) findViewById(R.id.tvBarcode)).setText("Barcode: " + (item.getBarcode() == 0 ? "N/A" : item.getBarcode()));
        }
        else
        {
            findViewById(R.id.tvBarcode).setVisibility(View.GONE);
        }

        txtCostBasis = (EditText)findViewById(R.id.txtCostBasis);
        txtCostBasis.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        txtCostBasis.addTextChangedListener(new TextWatcher() {
            boolean isEditing;
            String strPrevious = "";

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing) return;
                isEditing = true;
                strPrevious = common.Utility.CheckMoneyTextChanged(s, strPrevious);

                isEditing = false;
            }
        });


        tvDate = (TextView)findViewById(R.id.tvDate);


        tvDate.setPaintFlags(tvDate.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);

        tvTime = (TextView)findViewById(R.id.tvTime);

        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
        tvTime.setPaintFlags(tvTime.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowTimePickerDialog();
            }
        });
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDatePickerDialog();
            }
        });
        SetCurrentDateTimeForDateTimeInput();

        ImageButton imgBtnCancel =(ImageButton)findViewById(R.id.imgBtnCancel);
        imgBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dismiss();
            }
        });

        btnAdd = (Button)findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSupplyRecord(item.getID());

            }
        });

        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CancelEdit(item.getID());
            }
        });
        tblInventoryRecord = (TableLayout)findViewById(R.id.tblInventoryRecord);
        InventoryPropertiesPlaceHolder = (FrameLayout)findViewById(R.id.InventoryPropertiesPlaceHolder);
        llInventoryProperties = (LinearLayout)findViewById(R.id.llInventoryProperties);

        imgBtnInventoryPanelCollapse = (ImageButton)findViewById(R.id.imgBtnInventoryPanelCollapse);
        imgBtnInventoryPanelCollapse.setTag(Enum.ExpandStatus.expanded);
        imgBtnInventoryPanelCollapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(blnAnimationStart)return;//return if animation still running
                blnAnimationStart = true;
                if ((Enum.ExpandStatus) imgBtnInventoryPanelCollapse.getTag() == Enum.ExpandStatus.expanded) {

                    ShowInventoryPropertiesPanel(false);
                } else {

                    ShowInventoryPropertiesPanel(true);
                }
            }
        });

        scrInventoryTable = (ScrollView)findViewById(R.id.scrInventoryTable);
        rowWidth = scrInventoryTable.getLayoutParams().width;

        ReloadInventoryList(item.getID(),-1);


    }
    private void SetInventoryRowBackgroundColor()
    {
        for(int i =0;i<tblInventoryRecord.getChildCount();i++)
        {
            if((i&1)==0)tblInventoryRecord.getChildAt(i).setBackgroundColor(getContext().getResources().getColor(R.color.very_light_grey));
            else tblInventoryRecord.getChildAt(i).setBackground(null);
        }
    }
    private void RemoveInventoryRecordFromTable(long inventoryId)
    {



        for(int i =tblInventoryRecord.getChildCount()-1;i>=0;i--)
        {

            if((Long)tblInventoryRecord.getChildAt(i).getTag()==inventoryId)
            {
                tblInventoryRecord.removeViewAt(i);
            }


        }

        if(tblInventoryRecord.getChildCount()==0)
        {
            tblInventoryRecord.addView(CreateNoContentRow());
        }

        SetInventoryRowBackgroundColor();
    }
    private void SetCurrentDateTimeForDateTimeInput()
    {
        final Calendar c = Calendar.getInstance();
        int month = (c.get(Calendar.MONTH)+1);
        int day = c.get(Calendar.DAY_OF_MONTH);

        tvDate.setText(ReturnDateString(month, day, c.get(Calendar.YEAR)));
        tvTime.setText(ReturnTimeString(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
    }
    private void ClearAllInputFields()
    {
        txtUnit.setText("");
        txtCostBasis.setText("");
        ddlSupplier.setSelection(0);
    }
    private void CancelEdit(final long itemId)
    {
        btnAdd.setText("Add");
        btnCancel.setVisibility(View.INVISIBLE);
        ClearAllInputFields();

        blnEditMode=false;
        SetCurrentDateTimeForDateTimeInput();
        ClearAllInputFields();
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSupplyRecord(itemId);
            }
        });
    }
    protected void ShowTimePickerDialog()
    {
        blnTimeDialogShow = true;
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        new TimePickerDialog(getContext(), this, hour, minute,
                DateFormat.is24HourFormat(getContext())).show();
    }
    protected void ShowDatePickerDialog()
    {
        blnDateDialogShow = true;
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(getContext(), this, year, month, day).show();
    }
             protected void AddSupplyRecord(long itemId)
             {



                 if(!Validate())return;
                 Inventory inventory = new Inventory();
                 inventory.lngItemId=itemId;
                 inventory.CostPrice = new BigDecimal(common.Utility.ConvertCurrencyFormatToBigDecimalString(txtCostBasis.getText().toString()));
                 inventory.UnitCount = Integer.parseInt(txtUnit.getText()+"");
                 inventory.lngSupplierId=((Supplier)ddlSupplier.getSelectedItem()).SupplierId;
                 inventory.lngInventoryId = new DatabaseHelper(getContext()).GenerateNextInventoryId();
                 String strDateTime = tvDate.getText()+" "+tvTime.getText()+"";
                 SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
                 try {
                     inventory.RecordDate = sdf.parse(strDateTime);
                 }
                 catch(ParseException ex) {
                     common.Utility.ShowMessage("Add","Failed to add record.",getContext(),R.drawable.exclaimation);
                     return;
                 }

                 long id =common.inventoryList.InsertInventory(inventory);
                 if(id>-1)
                 {
                     //play animation to add new record
                     ReloadInventoryList(itemId,id);

                     tvUnitAvailable.setText(common.Utility.GetAtTheMomentItemCount(itemId) + "");//common.inventoryList.GetInventoryCount(itemId) + "");
                     ClearAllInputFields();
                     Toast.makeText(getContext(), "Added", Toast.LENGTH_LONG);
                     common.control_events.HideSoftKeyboard(tvUnitAvailable);
                 }

             }

}
