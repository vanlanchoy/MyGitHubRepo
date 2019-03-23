package tme.pos;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import tme.pos.BusinessLayer.ChartManager;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 1/6/2016.
 */


public class ServerGratuityDetailDialog extends Dialog implements DatePickerDialog.OnDateSetListener{
    boolean blnDateDialogShow = false;
    TextView tvSelectedReceiptFromDate;
    TextView tvSelectedReceiptToDate;
    TextView tvSelectedTvDate;
    ImageView imgSearchReceipt;
    TableLayout tblReceiptList;
    boolean blnLoading;
    long employeeId;
    public ServerGratuityDetailDialog(Context context,long serverId) {
        super(context);
        employeeId = serverId;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_server_gratuity_detail_dialog);

        tvSelectedReceiptFromDate = (TextView)findViewById(R.id.tvSelectedReceiptFromDate);
        tvSelectedReceiptFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tvSelectedTvDate = tvSelectedReceiptFromDate;
                ShowDatePickerDialog();
            }
        });

        tvSelectedReceiptToDate = (TextView)findViewById(R.id.tvSelectedReceiptToDate);
        tvSelectedReceiptToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tvSelectedTvDate = tvSelectedReceiptToDate;
                ShowDatePickerDialog();
            }
        });

        imgSearchReceipt = (ImageView)findViewById(R.id.imgSearchReceipt);
        imgSearchReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SearchReceiptByDate();
            }
        });

        tblReceiptList = (TableLayout)findViewById(R.id.tblReceiptList);
        tblReceiptList.addView(CreateNoRecordTableRow());

        //set control position
        findViewById(R.id.imgCancelStamp).setY(common.Utility.DP2Pixel(130,getContext()));
        findViewById(R.id.imgCancelStamp).setX(common.Utility.DP2Pixel(620, getContext()));

        SetCurrentDateForDateInput();

    }
    private void SearchReceiptByDate()
    {
        if(blnLoading)return;
        blnLoading = true;
        String strFromDate = tvSelectedReceiptFromDate.getText()+"";
        Calendar cal  = new GregorianCalendar();//Calendar.getInstance();
        cal.set(Integer.parseInt(strFromDate.substring(6))
                , Integer.parseInt(strFromDate.substring(0, 2)) - 1//jan = 0
                , Integer.parseInt(strFromDate.substring(3, 5))
                , 0, 0, 0);
        long value = cal.getTimeInMillis();

        String strToDate = tvSelectedReceiptToDate.getText()+"";
        cal.set(Integer.parseInt(strToDate.substring(6))
                , Integer.parseInt(strToDate.substring(0, 2)) - 1//jan = 0
                , Integer.parseInt(strToDate.substring(3, 5))
                , 23, 59, 59);


        ArrayList<Pair<String,String[]>> data =(new ChartManager(getContext())).GetReceiptOfParticularServer(value, cal.getTimeInMillis(),employeeId);

        float total =0;
        for(Pair p:data)
        {
            total +=Float.parseFloat(((String[])p.second)[2]);
        }
        ((TextView)findViewById(R.id.tvTotalGratuity)).setText(
                Html.fromHtml("Total gratuity earned during this period <b>"+common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(total))+"</b>"));
        ConstructReceiptListTable(data);

        //reset flag
        blnLoading=false;
    }
    private void ShowReceiptPanel(boolean isActive)
    {

        //show gui panel
        findViewById(R.id.llReceiptGUIPanel).setVisibility(View.VISIBLE);

        //hide receipt icon
        findViewById(R.id.ReceiptIcon).setVisibility(View.GONE);

        //receipt options
        //findViewById(R.id.llReceiptOptions).setVisibility(View.VISIBLE);

        findViewById(R.id.imgCancelStamp).setVisibility(((!isActive) ? View.VISIBLE : View.GONE));
        //findViewById(R.id.imgActiveReceipt).setVisibility(((isActive) ? View.GONE : View.VISIBLE));
        //findViewById(R.id.imgCancelReceipt).setVisibility(((!isActive)?View.GONE:View.VISIBLE));

    }
    private void UpdateReceiptSummary(Receipt receipt)
    {
        //Receipt receipt = common.myCartManager.GetReceipt(strReceiptId,intMiniReceiptIndex);

        //table label
        ((TextView)findViewById(R.id.tvTableLabel)).setText(receipt.tableNumber);

        //gratuity
        TextView tvReceiptGratuity = (TextView)findViewById(R.id.tvReceiptGratuity);
        tvReceiptGratuity.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity())));

        //item count
        TextView tvReceiptItemCount= (TextView)findViewById(R.id.tvReceiptItemCount);
        tvReceiptItemCount.setText("Total item: " + receipt.myCart.GetItems().size());

        //server name if any
        if(receipt.server !=null)
        {
            ((TextView)findViewById(R.id.tvServerName)).setText("Server: "+receipt.server.Name);
        }

        //amount
        TextView tvReceiptAmount = (TextView)findViewById(R.id.tvReceiptAmount);
        tvReceiptAmount.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.getAmount()));

        //tax
        TextView tvReceiptTax = (TextView)findViewById(R.id.tvReceiptTax);
        tvReceiptTax.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.getTaxAmount()));

        //discount
        TextView tvReceiptDiscount = (TextView)findViewById(R.id.tvReceiptDiscount);
        tvReceiptDiscount.setText("-" + common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForDiscount())));


        TextView tvReceiptTotal = (TextView)findViewById(R.id.tvReceiptTotal);
        tvReceiptTotal.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()));


    }
    private void DisplayReceipt(String strReceiptNum)
    {

        TableLayout tblReceipt = (TableLayout)findViewById(R.id.tblReceipt);
        tblReceipt.removeAllViews();
        Receipt receipt = new ChartManager(getContext()).GetReceipt(strReceiptNum);

        if(receipt==null)
        {
            common.Utility.ShowMessage("Load Receipt","Couldn't load selected receipt, data not available.",getContext(), R.drawable.exclaimation);
        }
        else
        {
            //save selected receipt object
            findViewById(R.id.llReceiptGUIPanel).setTag(receipt);

            ShowReceiptPanel(receipt.blnActive);
            UpdateReceiptSummary(receipt);
            //TableLayout tblReceipt = (TableLayout)findViewById(R.id.tblReceipt);

            for(StoreItem si:receipt.myCart.GetItems())
            {
                //create table column for item name
                TextView tvItemName = CreateItemNameTextView(si.UnitOrder,
                        si.item.getName(),
                        common.text_and_length_settings.TYPE_FACE_ABEL_FONT,
                        common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH);

                //create table column for unit price
                TextView tvUnit =  CreateUnitPriceTextView(si.item.getPrice(), common.text_and_length_settings.TYPE_FACE_ABEL_FONT);


                //create table column for price
                TextView tvPrice =CreateTotalPriceTextView(si.UnitOrder, si.item.getPrice(), common.text_and_length_settings.TYPE_FACE_ABEL_FONT);

                TableRow trItem = new TableRow(getContext());
                trItem.addView(tvItemName);
                trItem.addView(tvUnit);
                trItem.addView(tvPrice);
                tblReceipt.addView(trItem);
                for(ModifierObject mo: si.modifiers)
                {
                    //create table column for Item
                    TextView tvSubName = CreateSubItemNameTextView(mo.getName(),
                            common.text_and_length_settings.TYPE_FACE_ABEL_FONT,
                            common.text_and_length_settings.SUB_ITEM_NAME_MAX_LENGTH, si.UnitOrder);
                    tvSubName.setPadding(20,0,0,0);
                    tvSubName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f));

                    ((TableRow.LayoutParams)tvSubName.getLayoutParams()).setMargins(0, common.text_and_length_settings.MODIFIER_TABLE_RECEIPT_ROW_NAME_TOP_MARGIN, 0, 0);


                    //create table column for unit price
                    TextView tvSubUnitPrice =  CreateSubUnitPriceTextView(mo.getPrice(),
                            common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
                    tvSubUnitPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f));


                    //create table column for price
                    TextView tvSubTotalPrice =  CreateSubTotalPriceTextView(mo.getPrice(),
                            common.text_and_length_settings.TYPE_FACE_ABEL_FONT, si.UnitOrder);


                    tvSubTotalPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f));
                    TableRow trSubItem = new TableRow(getContext());

                    trSubItem.addView(tvSubName);
                    trSubItem.addView(tvSubUnitPrice);
                    trSubItem.addView(tvSubTotalPrice);
                    tblReceipt.addView(trSubItem);
                }
            }
        }
    }
    public TextView CreateItemNameTextView(int Unit,String strName,Typeface tf,int intMaxChar)
    {
        //create table column for item name
        TextView txtName = new TextView(getContext());
        txtName.setTypeface(tf);
        txtName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, common.text_and_length_settings.INVOICE_ITEM_NAME_WIDTH_WEIGHT));
        txtName.setText(Unit + "x " + ((strName.length() > intMaxChar) ? strName.substring(0, intMaxChar) : strName));

        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtName.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW, common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW, common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW);

        return txtName;
    }
    public TextView CreateUnitPriceTextView(BigDecimal price,Typeface tf)
    {
        //create table column for unit
        TextView txtUnit = new TextView(getContext());
        txtUnit.setTypeface(tf);
        txtUnit.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, common.text_and_length_settings.INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT));
        txtUnit.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price));
        //txtUnit.setText("x" + Integer.toString(intCount));
        txtUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);
        //txtUnit.setBackgroundColor(Color.CYAN);
        txtUnit.setGravity(Gravity.RIGHT);
        txtUnit.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW, common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtUnit;
    }
    public TextView CreateTotalPriceTextView(int unit,BigDecimal price,Typeface tf)
    {
        //create table column for price
        TextView txtPrice = new TextView(getContext());
        txtPrice.setTypeface(tf);
        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, common.text_and_length_settings.INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT));
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price.multiply(new BigDecimal(unit))));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW,common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtPrice;
    }
    public TextView CreateSubItemNameTextView(String strName,Typeface tf,int intMaxChar,int intUnit)
    {


        //create table column for item name
        TextView txtName = new TextView(getContext());
        txtName.setTypeface(tf);
        txtName.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, common.text_and_length_settings.INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT));

        txtName.setText((strName.length() > intMaxChar) ? strName.substring(0, intMaxChar) : strName);
        txtName.setText(intUnit + "x " + txtName.getText());
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtName.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW, common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW,
                common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW, common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW);
        //txtName.setBackgroundColor(Color.YELLOW);
        return txtName;
    }
    public TextView CreateSubUnitPriceTextView(BigDecimal price,Typeface tf)
    {

        //create table column for price
        TextView txtPrice = new TextView(getContext());
        txtPrice.setTypeface(tf);

        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, common.text_and_length_settings.INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT));
        //txtPrice.setText(NumberFormat.getCurrencyInstance(java.util.Locale.US).format(price));
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW,
                common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW);

        return txtPrice;
    }
    public TextView CreateSubTotalPriceTextView(BigDecimal price,Typeface tf,int intUnit)
    {

        //create table column for price
        TextView txtPrice = new TextView(getContext());
        txtPrice.setTypeface(tf);

        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, common.text_and_length_settings.INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT));
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price.multiply(new BigDecimal(intUnit))));
        //txtPrice.setText(NumberFormat.getCurrencyInstance(java.util.Locale.US).format(price.multiply(new BigDecimal(intUnit))));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW,
                common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW, common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW);
        //txtPrice.setBackgroundColor(Color.GREEN);
        return txtPrice;
    }
    private void ConstructReceiptListTable(ArrayList<Pair<String,String[]>> data)
    {

        tblReceiptList.removeAllViews();
        int index=1;



        for (Pair<String, String[]> p : data) {
            TableRow tr = new TableRow(getContext());

            TextView tvIndex = new TextView(getContext());
            tvIndex.setText(index++ + ".");
            tvIndex.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
            tvIndex.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
            TableRow.LayoutParams trlp1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            trlp1.gravity = Gravity.LEFT;

            //Receipt number
            final String strReceiptNum = p.first;
            TextView tvReceiptNum = new TextView(getContext());
            //tvReceiptNum.setText(Html.fromHtml("<u>#" + p.second[0] + "</u>< " + p.second[1].substring(p.second[1].indexOf(" ")) + " " + common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(p.second[2])) + ""));
            if(p.second[3].equalsIgnoreCase("1")) {
                tvReceiptNum.setText(Html.fromHtml("<font color='#50C108'><u>#" + strReceiptNum + "</u></font> "));
            }
            else
            {
                tvReceiptNum.setText(Html.fromHtml("<font color='#ff0000'><u>#" + strReceiptNum + "</u></font> "));
            }
            //tvReceipt.setTextColor(getResources().getColor(R.color.green));
            tvReceiptNum.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
            tvReceiptNum.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
            common.control_events.CreateClickEffect(tvReceiptNum);
            tvReceiptNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DisplayReceipt(strReceiptNum);
                }
            });

            TableRow.LayoutParams trlp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            trlp2.bottomMargin = common.Utility.DP2Pixel(20, getContext());

            //timestamp
            TextView tvTime = new TextView(getContext());
            tvTime.setText(Html.fromHtml("  " + p.second[1].substring(p.second[1].indexOf(" "))));
            tvTime.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
            tvTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);



            //total
            TextView tvTotal = new TextView(getContext());
            tvTotal.setText(Html.fromHtml("  " + common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(p.second[2]))));
            tvTotal.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
            tvTotal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
            TableRow.LayoutParams trlp3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            trlp3.leftMargin = common.Utility.DP2Pixel(20, getContext());

            tr.addView(tvIndex, trlp1);
            tr.addView(tvReceiptNum, trlp2);
            tr.addView(tvTime);
            tr.addView(tvTotal,trlp3);
            TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tllp.bottomMargin=3;
            tblReceiptList.addView(tr,tllp);
        }

        if(tblReceiptList.getChildCount()==0)
        {
            tblReceiptList.addView(CreateNoRecordTableRow());
        }
    }
    private TableRow CreateNoRecordTableRow()
    {
        TableRow tr = new TableRow(getContext());
        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(tllp);
        tr.setGravity(Gravity.CENTER);

        TextView tv = new TextView(getContext());
        tv.setText("No Data");
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tv.setTextColor(getContext().getResources().getColor(R.color.common_signin_btn_light_text_disabled));

        tr.addView(tv);
        return tr;
    }
    protected void ShowDatePickerDialog()
    {
        if(blnDateDialogShow)return;

        blnDateDialogShow = true;
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(getContext(), this, year, month, day).show();
    }
    private void SetCurrentDateForDateInput()
    {
        final Calendar c = Calendar.getInstance();
        int month = (c.get(Calendar.MONTH)+1);
        int day = c.get(Calendar.DAY_OF_MONTH);

        tvSelectedReceiptToDate.setText(Html.fromHtml("<u>" + ReturnDateString(month, day, c.get(Calendar.YEAR)) + "</u>"));
        tvSelectedReceiptFromDate.setText(Html.fromHtml("<u>" + ReturnDateString(month, day, c.get(Calendar.YEAR)) + "</u>"));

    }
    private String ReturnDateString(int month,int day,int year)
    {
        return ((month > 9) ? month : "0" + month) +
                "/" +
                ((day > 9) ? day : "0" + day) + "/" +
                year;
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {

        //set flag preventing double dialog box for date picker on screen
        blnDateDialogShow = false;
        Calendar cal = new GregorianCalendar();//Calendar.getInstance();
        cal.set(year, month, day,0,0,0);

        tvSelectedTvDate.setText(Html.fromHtml("<u>" + ReturnDateString(month + 1, day, year) + "</u>"));
        tvSelectedTvDate.setTag(cal.getTimeInMillis());



    }
}
