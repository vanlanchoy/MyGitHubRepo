package tme.pos;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Set;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.FlowLayout;
import tme.pos.CustomViewCtr.PromotionRuleItemRow;
import tme.pos.Interfaces.ICreatePromotionActivityListener;
import tme.pos.Interfaces.IPromotionRuleItemRowListener;

/**
 * Created by kchoy on 4/6/2016.
 */
public class AddPromotionDialog extends Dialog implements DatePickerDialog.OnDateSetListener
        ,TimePickerDialog.OnTimeSetListener,ICreatePromotionActivityListener
,IPromotionRuleItemRowListener{
    int MAX_TYPE_OF_ITEM=10;
    PromotionUIActivity parent;
    PromotionObject originalPromotionObject;
    PromotionObject copiedPromotionObject;
    TextView tvSelectedPromotionDateMode;
    TextView tvSelectedDiscountType;
    Enum.PromotionDateOption selectedPromotionDateOption;
    LinearLayout selectedInputPromotionDateRangePanel;
    LinearLayout selectedInputPromotionByTypePanel;
    boolean blnDateDialogShow=false;
    boolean blnUpdateMode=false;
    boolean blnDismissDialog=true;
    boolean blnDoNotReset=false;
    Enum.CallDateDialogFrom dialogFor = Enum.CallDateDialogFrom.from;
    Enum.PromotionByType selectedPromotionByType;
    Enum.DiscountType selectedDiscountType;
    RadioButton[] rdsRepeat;
    TextView[] dayViews;
    TextView[] monthViews;
    TextView[] daysOfMonth;
    View[] colorViews;
    PriceTextWatcher priceTextWatcher;
    PercentageTextWatcher percentageTextWatcher;
    TextView tvSelectedPromotionType;
    Enum.DiscountColor selectedDiscountColor= Enum.DiscountColor.discount_blue;
    int dynamicId=100;

    CheckBox chkNoUpperLimit;
    EditText txtAmountFrom;
    EditText txtAmountLimit;
    EditText txtDiscountAmount;
    LinearLayout llItems;
    RadioButton rdMonthly;
    TextView tvTimeStart;
    TextView tvTimeEnd;
    TextView tvEndOn;
    EditText txtTitle;
    TextView tvFromDate;
    TextView tvFromTime;
    TextView tvToDate;
    TextView tvToTime;
    ImageView imgCancel;
    ImageView imgSave;
    RadioButton rdWeekly;
    RadioButton rd2Week;
    RadioButton rd3Week;
    TextView tvDollarSignLabel;
    TextView tvPercentageSignLabel;
    TextView tvDiscountLabel;

    //boolean blnRemovePromotionInParentUINCache=false;
    public AddPromotionDialog(Context context, PromotionUIActivity parent) {
        super(context);
       /* this.parent = parent;
        promotionObject = new PromotionObject();*/
        Instantiate(parent,new PromotionObject());
    }
    public AddPromotionDialog(Context context, PromotionUIActivity parent, PromotionObject po) {
        super(context);
        Instantiate(parent,po);
       /* this.parent = parent;
        promotionObject = po;*/


    }
    private void Instantiate(PromotionUIActivity parent,PromotionObject po)
    {

        this.parent = parent;
        originalPromotionObject = po;
        copiedPromotionObject = new PromotionObject(po);
        MAX_TYPE_OF_ITEM = Integer.parseInt(getContext().getResources().getString(R.string.MAX_PROMOTION_ITEM_KIND_ALLOWED));
    }

   /* private String ConstructItemString(HashMap<Long,Integer>hashMap)
    {
        String strItems="(";
        ItemObject io = null;

        for(long key:hashMap.keySet())
        {
            io=common.myMenu.GetItem(key);
            strItems+=hashMap.get(key)+"X "+io.getName()+" | ";
        }
        if(strItems.length()>1) {
            strItems = strItems.substring(0,strItems.length()-3);
            strItems += ")";
        }

        return strItems;
    }*/
    private TextView ReturnCurrentDateTextLabel()
    {
        TextView tv=null;
        if(dialogFor== Enum.CallDateDialogFrom.to || Enum.CallDateDialogFrom.day_month_expiration_end_time ==dialogFor)
        {
            tv = tvToDate;
        }

        else if(dialogFor== Enum.CallDateDialogFrom.endOn)
        {
            tv = tvEndOn;
        }
        else if(dialogFor== Enum.CallDateDialogFrom.from || Enum.CallDateDialogFrom.day_month_expiration_start_time ==dialogFor)
        {
            tv = tvFromDate;
        }
        else
        {


        }
        return tv;
    }
    protected void ShowDatePickerDialog()
    {
        blnDateDialogShow = true;
        TextView tv=ReturnCurrentDateTextLabel();//tvFromDate;
        final Calendar c=Calendar.getInstance();


        c.setTimeInMillis((Long)tv.getTag());
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(getContext(), this, year, month, day).show();
    }
    private void ShowPanelAnimation(final int ShowPanelId, final Enum.PromotionActivityPanel activityPanel)
    {

        //final LinearLayout tempPanel = panel;
        final float flPanelWidth = 600;
        TranslateAnimation movementSlideOut = new TranslateAnimation(0.0f,common.Utility.DP2Pixel(flPanelWidth, getContext()),  0.0f, 0.0f);//move left
        movementSlideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LinearLayout tempPanel = null;
                switch (activityPanel) {
                    case input_promotion_date_range_panel:
                        tempPanel = selectedInputPromotionDateRangePanel;
                        break;
                    case input_promotion_by_panel:
                        tempPanel = selectedInputPromotionByTypePanel;
                        break;

                    default:
                }
                tempPanel.setVisibility(View.GONE);
                tempPanel = (LinearLayout) findViewById(ShowPanelId);
                switch (activityPanel) {
                    case input_promotion_date_range_panel:
                        selectedInputPromotionDateRangePanel=tempPanel;
                        break;
                    case input_promotion_by_panel:
                        selectedInputPromotionByTypePanel =tempPanel;
                        break;

                    default:
                }
                tempPanel.setVisibility(View.VISIBLE);

                TranslateAnimation movementSlideIn = new TranslateAnimation(-flPanelWidth, 0.0f, 0.0f, 0.0f);//move right


                movementSlideIn.setDuration(200);
                movementSlideIn.setFillAfter(true);


                tempPanel.startAnimation(movementSlideIn);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movementSlideOut.setDuration(200);
        LinearLayout tempPanel=null;
        switch (activityPanel) {
            case input_promotion_date_range_panel:
                tempPanel = selectedInputPromotionDateRangePanel;
                break;
            case input_promotion_by_panel:
                tempPanel = selectedInputPromotionByTypePanel;
                break;

            default:
        }
        tempPanel.startAnimation(movementSlideOut);
    }
    private void ShowPanel(int ShowPanelId,Enum.PromotionActivityPanel panelType)
    {
        LinearLayout panel = null;


        switch(panelType)
        {

            case input_promotion_date_range_panel:
                panel=selectedInputPromotionDateRangePanel;

                break;
            case input_promotion_by_panel:
                panel = selectedInputPromotionByTypePanel;

                break;
            default:
        }
        if(panel!=null) {

            ShowPanelAnimation(ShowPanelId,panelType);

        }
        else {
            panel = (LinearLayout) findViewById(ShowPanelId);
            panel.setVisibility(View.VISIBLE);
            switch(panelType)
            {

                case input_promotion_date_range_panel:
                    selectedInputPromotionDateRangePanel = panel;

                    break;
                case input_promotion_by_panel:
                    selectedInputPromotionByTypePanel=panel;

                    break;
                default:
            }
        }

    }
    private boolean PromotionDateOptionClicked(View v)
    {
        if(v==tvSelectedPromotionDateMode)return false;
        v.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));

        if (tvSelectedPromotionDateMode != null) {

            tvSelectedPromotionDateMode.setTextColor(getContext().getResources().getColor(R.color.divider_grey));
            tvSelectedPromotionDateMode.setBackground(null);
        }
        tvSelectedPromotionDateMode = (TextView) v;

        tvSelectedPromotionDateMode.setTextColor(getContext().getResources().getColor(R.color.green));
        return true;
    }
    private void CreateClickEffectForRoundedBoarder(View v,final Enum.PromotionDateOption pdo)
    {

        final LinearLayout dayPanel = (LinearLayout) findViewById(R.id.dayOfWeekPanel);

        final LinearLayout expPanel = (LinearLayout) findViewById(R.id.expireOptionPanel);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View innerView) {

                selectedPromotionDateOption = pdo;
                if (PromotionDateOptionClicked(innerView)) {
                    switch (pdo) {
                        case day:
                            common.Utility.LogActivity("day promotion clicked");
                            ShowPanel(R.id.dayOfWeekPanel, Enum.PromotionActivityPanel.input_promotion_date_range_panel);

                            expPanel.setVisibility(View.VISIBLE);
                            break;
                        case once:
                            common.Utility.LogActivity("once promotion clicked");
                            ShowPanel(R.id.onceOptionPanel, Enum.PromotionActivityPanel.input_promotion_date_range_panel);

                            expPanel.setVisibility(View.GONE);
                            break;
                        case month:
                            common.Utility.LogActivity("month promotion clicked");
                            ShowPanel(R.id.dayOfMonthPanel, Enum.PromotionActivityPanel.input_promotion_date_range_panel);

                            expPanel.setVisibility(View.VISIBLE);
                            break;
                        default:


                    }
                }
            }
        });
    }
    protected void RadioButtonConfiguration(RadioButton rd)
    {
        for(RadioButton r:rdsRepeat)
        {
            r.setChecked(false);
        }
        rd.setChecked(true);
    }
    protected void ShowTimePickerDialog(long timeInMili)
    {
        blnDateDialogShow = true;

        Date date = new Date(timeInMili);
        int hr = Integer.parseInt(new SimpleDateFormat("HH").format(date));
        int minute = Integer.parseInt(new SimpleDateFormat("mm").format(date));
        new TimePickerDialog(getContext(),this,hr,minute,false).show();
    }
    public void SetTextLabelProperties(TextView tv)
    {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_PROMOTION_FIELD_LABEL_TEXT_SIZE);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
    }
    private void SetCurrentTimeForTimeInput(TextView tvTime,Date date)
    {

        tvTime.setText(Html.fromHtml("<u>" + new SimpleDateFormat("hh:mm aa").format(date) + "</u>"));

    }
    private void SetCurrentDateForDateInput(TextView tvDate,TextView tvTime,Enum.CallDateDialogFrom callDateDialogFrom)
    {
        final Calendar c = Calendar.getInstance();
        Date date = new Date(c.getTimeInMillis());
        tvDate.setTag(date.getTime());

        //check if is the date label in calendar

        if(callDateDialogFrom== Enum.CallDateDialogFrom.from || callDateDialogFrom== Enum.CallDateDialogFrom.to)
        {
            tvDate.setText(Html.fromHtml("<u>" + new SimpleDateFormat("MM/dd/yyyy").format(date) + "</u>"));

            SetCurrentTimeForTimeInput(tvTime, date);
        }
        else if(callDateDialogFrom== Enum.CallDateDialogFrom.endOn)
        {

            tvDate.setTag(date.getTime());
            tvDate.setText(Html.fromHtml("<u>" + new SimpleDateFormat("MM/dd/yyyy").format(date) + "</u>"));
        }

    }
    private void ConfigureDayOfWeekClick()
    {
        for(int i=0;i<dayViews.length;i++)
        {
            dayViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SetTextViewButtonOnClickProperties((TextView)view);
                }
            });
        }
    }
    private void SetTextViewButtonOnClickProperties(TextView tv)
    {
        int colorId = -1;

        if(tv.getTag()==null)
        {
            //select
            tv.setTag(true);
            colorId = R.color.white;
            tv.setBackgroundColor(getContext().getResources().getColor(R.color.green));
        }
        else
        {
            //deselect
            tv.setTag(null);
            colorId = R.color.green;
            tv.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
        }

        tv.setTextColor(getContext().getResources().getColor(colorId));

    }
    private void ConfigureDataInputPanel()
    {


        //title
        SetTextLabelProperties((TextView) findViewById(R.id.tvTitle));
        txtTitle = (EditText)findViewById(R.id.txtTitle);
        txtTitle.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_PROMOTION_FIELD_LABEL_TEXT_SIZE);
        txtTitle.setHint((txtTitle.getHint() + "").replace("%1$d", common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH + ""));
        InputFilter[] filters =new InputFilter[1];
        filters[0]= new InputFilter.LengthFilter(common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH);
        txtTitle.setFilters(filters);

        CreateClickEffectForRoundedBoarder((TextView) findViewById(R.id.tvOptionOnce), Enum.PromotionDateOption.once);
        CreateClickEffectForRoundedBoarder((TextView) findViewById(R.id.tvOptionDay), Enum.PromotionDateOption.day);
        CreateClickEffectForRoundedBoarder((TextView) findViewById(R.id.tvOptionMonth), Enum.PromotionDateOption.month);
        ((TextView) findViewById(R.id.tvOptionOnce)).callOnClick();

        //from date
        SetTextLabelProperties((TextView) findViewById(R.id.tvFromDateLabel));
        tvFromDate = (TextView)findViewById(R.id.tvFromDate);
        SetTextLabelProperties(tvFromDate);

        tvFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set flag preventing double dialog box for date picker on screen
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("from date label clicked");
                dialogFor = Enum.CallDateDialogFrom.from;
                ShowDatePickerDialog();
            }
        });

        tvFromTime = (TextView)findViewById(R.id.tvFromTime);
        SetTextLabelProperties(tvFromTime);

        tvFromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("from time display label clicked");
                dialogFor = Enum.CallDateDialogFrom.from;
                ShowTimePickerDialog((Long) tvFromDate.getTag());
            }
        });
        SetCurrentDateForDateInput(tvFromDate, tvFromTime, Enum.CallDateDialogFrom.from);


        //to date
        SetTextLabelProperties((TextView) findViewById(R.id.tvToDateLabel));
        tvToDate = (TextView)findViewById(R.id.tvToDate);
        SetTextLabelProperties(tvToDate);

        tvToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set flag preventing double dialog box for date picker on screen
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("to date label clicked");
                dialogFor = Enum.CallDateDialogFrom.to;
                ShowDatePickerDialog();
            }
        });

        tvToTime = (TextView)findViewById(R.id.tvToTime);
        SetTextLabelProperties(tvToTime);
        tvToTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("to time label clicked");
                dialogFor = Enum.CallDateDialogFrom.to;
                ShowTimePickerDialog((Long) tvToDate.getTag());
            }
        });
        SetCurrentDateForDateInput(tvToDate, tvToTime, Enum.CallDateDialogFrom.to);

        //'once' option start time and end time
        SetTextLabelProperties((TextView)findViewById(R.id.tvFromTimeLabel));
        SetTextLabelProperties((TextView)findViewById(R.id.tvToTimeLabel));

        //promotion start time
        Calendar tempCal = new GregorianCalendar();
        tempCal.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        tvTimeStart = (TextView)findViewById(R.id.tvTimeStart);
        SetTextLabelProperties(tvTimeStart);
        tvTimeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("day/month expiration  start time clicked");
                dialogFor = Enum.CallDateDialogFrom.day_month_expiration_start_time;
                ShowTimePickerDialog((Long) tvTimeStart.getTag());
            }
        });
        SetCurrentTimeForTimeInput(tvTimeStart, new Date(tempCal.getTimeInMillis()));
        tvTimeStart.setTag(tempCal.getTimeInMillis());
        //promotion end time
        tvTimeEnd = (TextView)findViewById(R.id.tvTimeEnd);
        //tempCal.add(Calendar.HOUR_OF_DAY, 1);
        SetTextLabelProperties(tvTimeEnd);
        tvTimeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("day/month expiration  end time clicked");
                dialogFor = Enum.CallDateDialogFrom.day_month_expiration_end_time;
                ShowTimePickerDialog((Long) tvTimeEnd.getTag());
            }
        });
        SetCurrentTimeForTimeInput(tvTimeEnd, new Date(tempCal.getTimeInMillis()));
        tvTimeEnd.setTag(tempCal.getTimeInMillis());

        //day of week selections
        dayViews = new TextView[7];
        TextView tvDay = (TextView)findViewById(R.id.tvDayOfWeekMon);

        dayViews[0]=tvDay;
        tvDay = (TextView)findViewById(R.id.tvDayOfWeekTue);

        dayViews[1]=tvDay;
        tvDay = (TextView)findViewById(R.id.tvDayOfWeekWed);

        dayViews[2]=tvDay;
        tvDay = (TextView)findViewById(R.id.tvDayOfWeekThu);

        dayViews[3]=tvDay;
        tvDay = (TextView)findViewById(R.id.tvDayOfWeekFri);

        dayViews[4]=tvDay;
        tvDay = (TextView)findViewById(R.id.tvDayOfWeekSat);

        dayViews[5]=tvDay;
        tvDay = (TextView)findViewById(R.id.tvDayOfWeekSun);

        dayViews[6]=tvDay;
        ConfigureDayOfWeekClick();

        //repeat
        SetTextLabelProperties((TextView) findViewById(R.id.tvRepeatLabel));


        //radio button
        rdWeekly = (RadioButton)findViewById(R.id.rdWeekly);
        rdWeekly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButtonConfiguration((RadioButton) v);
            }
        });
        rd2Week = (RadioButton)findViewById(R.id.rd2Week);
        rd2Week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButtonConfiguration((RadioButton)v);
            }
        });
        rd3Week = (RadioButton)findViewById(R.id.rd3Week);
        rd3Week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButtonConfiguration((RadioButton)v);
            }
        });
        rdMonthly = (RadioButton)findViewById(R.id.rdMonthly);
        rdMonthly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioButtonConfiguration((RadioButton)v);
            }
        });
        rdsRepeat =new RadioButton[]{rdWeekly,rd2Week,rd3Week,rdMonthly};


        //months
        monthViews = new TextView[12];
        TableRow tr = null;
        TableLayout tbl=(TableLayout)findViewById(R.id.tblMonths);
        for(int i=0;i<12;i++)
        {
            if(i%7==0)
            {
                //new row
                tr = new TableRow(getContext());
                tbl.addView(tr);
            }
            final TextView tvMonth = new TextView(getContext());
            tvMonth.setGravity(Gravity.CENTER);
            String strMonthName="";
            if (i == 0) {strMonthName="Jan"; }
            else if (i == 1) {strMonthName="Feb"; }
            else if (i == 2) {strMonthName="Mar"; }
            else if (i == 3) {strMonthName="Apr"; }
            else if (i == 4) {strMonthName="May"; }
            else if (i == 5) {strMonthName="Jun"; }
            else if (i == 6) {strMonthName="Jul"; }
            else if (i == 7) {strMonthName="Aug"; }
            else if (i == 8) {strMonthName="Sep"; }
            else if (i == 9) {strMonthName="Oct"; }
            else if (i == 10) {strMonthName="Nov"; }
            else if (i == 11) {strMonthName="Dec"; }
            monthViews[i]=tvMonth;
            tvMonth.setText(strMonthName);
            tvMonth.setTextColor(getContext().getResources().getColor(R.color.green));
            tvMonth.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
            tvMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SetTextViewButtonOnClickProperties(tvMonth);
                }
            });

            TableRow.LayoutParams trlp = new TableRow.LayoutParams(common.Utility.DP2Pixel(40,getContext()),common.Utility.DP2Pixel(40,getContext()));
            trlp.setMargins(0,0,1,1);
            tr.addView(tvMonth,trlp);
        }

        //day of month
        daysOfMonth = new TextView[31];
        tr = null;
        tbl=(TableLayout)findViewById(R.id.tblCalendar);

        for(int i=0;i<31;i++)
        {
            if(i%7==0)
            {
                //new row
                tr = new TableRow(getContext());
                tbl.addView(tr);
            }
            final TextView tvDayOfMonth = new TextView(getContext());
            tvDayOfMonth.setGravity(Gravity.CENTER);
            tvDayOfMonth.setText((i + 1) + "");
            tvDayOfMonth.setTextColor(getContext().getResources().getColor(R.color.green));
            tvDayOfMonth.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
            tvDayOfMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SetTextViewButtonOnClickProperties(tvDayOfMonth);
                }
            });

            TableRow.LayoutParams trlp = new TableRow.LayoutParams(common.Utility.DP2Pixel(40,getContext()),common.Utility.DP2Pixel(40,getContext()));
            trlp.setMargins(0,0,1,1);
            tr.addView(tvDayOfMonth,trlp);
            daysOfMonth[i]=tvDayOfMonth;
        }

        //time label
        SetTextLabelProperties((TextView)findViewById(R.id.tvTimeLabel1));
        SetTextLabelProperties((TextView)findViewById(R.id.tvTimeLabel2));

        //ends on
        SetTextLabelProperties((TextView)findViewById(R.id.tvEndLabel));

        tvEndOn = (TextView)findViewById(R.id.tvEndOn);
        SetTextLabelProperties(tvEndOn);
        SetCurrentDateForDateInput(tvEndOn, null, Enum.CallDateDialogFrom.endOn);
        tvEndOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set flag preventing double dialog box for date picker on screen
                if (blnDateDialogShow) return;
                blnDateDialogShow = true;
                common.Utility.LogActivity("end on date label clicked");
                dialogFor = Enum.CallDateDialogFrom.endOn;
                ShowDatePickerDialog();
            }
        });



        //discount by rule item
        CreateClickEffectForRoundedBoarder(findViewById(R.id.tvOptionByItem), Enum.PromotionByType.item);
        CreateClickEffectForRoundedBoarder(findViewById(R.id.tvOptionByTotal), Enum.PromotionByType.total);
        findViewById(R.id.tvOptionByItem).callOnClick();

        //rule panel
        //rule by item
        llItems = (LinearLayout) findViewById(R.id.llItems);
        //add item
        TextView tvAddItem = (TextView)findViewById(R.id.tvAddItem);
        tvAddItem.setText(Html.fromHtml("<u>Add Item</u>"));
        SetTextLabelProperties(tvAddItem);
        tvAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("show by item add item dialog");
                if (llItems.getChildCount()/2 >= MAX_TYPE_OF_ITEM) {
                    common.Utility.ShowMessage("Add item", "You have reached the maximum " + MAX_TYPE_OF_ITEM + " kind of item", getContext(), R.drawable.no_access);
                    return;
                }
                SelectPromotionItemRowDialog d = new SelectPromotionItemRowDialog(getContext(), AddPromotionDialog.this,copiedPromotionObject,true);
                d.show();
            }
        });
        //rule by amount
        txtAmountFrom = (EditText)findViewById(R.id.txtAmountFrom);
        txtAmountLimit = (EditText)findViewById(R.id.txtAmountLimit);
        txtAmountFrom.addTextChangedListener(new PriceTextWatcher(getContext(), txtAmountFrom,true));
        txtAmountLimit.addTextChangedListener(new PriceTextWatcher(getContext(), txtAmountLimit, true));
        chkNoUpperLimit = (CheckBox)findViewById(R.id.chkNoUpperLimit);
        chkNoUpperLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                txtAmountLimit.setEnabled(!b);
                txtAmountLimit.setText("$0.00");
            }
        });



        //discount
        tvPercentageSignLabel = (TextView)findViewById(R.id.tvPercentageSignLabel);
        tvDollarSignLabel = (TextView)findViewById(R.id.tvDollarSignLabel);
        txtDiscountAmount = (EditText)findViewById(R.id.txtDiscountAmount);
        tvDiscountLabel = (TextView)findViewById(R.id.tvDiscountLabel);
        //instantiate text watchers
        priceTextWatcher = new PriceTextWatcher(getContext(),txtDiscountAmount,true);
        percentageTextWatcher = new PercentageTextWatcher(getContext(),txtDiscountAmount,false);
        //txtDiscountAmount.addTextChangedListener(priceTextWatcher);

        CreateClickEffectForRoundedBoarder(findViewById(R.id.tvCashDiscount), Enum.DiscountType.cash);
        CreateClickEffectForRoundedBoarder(findViewById(R.id.tvPercentageDiscount), Enum.DiscountType.percentage);
        //findViewById(R.id.tvCashDiscount).callOnClick();
        txtDiscountAmount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_DISCOUNT_AMOUNT_TEXT_SIZE);
        txtDiscountAmount.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tvPercentageSignLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE);
        tvPercentageSignLabel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tvDollarSignLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE);
        tvDollarSignLabel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tvDiscountLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE);
        tvDiscountLabel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        //color
        TextView tv = (TextView)findViewById(R.id.tvColor);
        SetTextLabelProperties(tv);

        colorViews = new View[7];
        View v = findViewById(R.id.colorBlue);
        v.setTag(Enum.DiscountColor.discount_blue);
        colorViews[0]=v;
        v = findViewById(R.id.colorGreen);
        v.setTag(Enum.DiscountColor.discount_green);
        colorViews[1]=v;
        v = findViewById(R.id.colorIndigo);
        v.setTag(Enum.DiscountColor.discount_indigo);
        colorViews[2]=v;
        v = findViewById(R.id.colorPink);
        v.setTag(Enum.DiscountColor.discount_pink);
        colorViews[3]=v;
        v = findViewById(R.id.colorYellow);
        v.setTag(Enum.DiscountColor.discount_yellow);
        colorViews[4]=v;
        v = findViewById(R.id.colorOrange);
        v.setTag(Enum.DiscountColor.discount_orange);
        colorViews[5]=v;
        v = findViewById(R.id.colorBrown);
        v.setTag(Enum.DiscountColor.discount_brown);
        colorViews[6]=v;

        for(int i=0;i<colorViews.length;i++)
        {
            colorViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SetDiscountColor((Enum.DiscountColor) v.getTag(),copiedPromotionObject);
                    for (int j = 0; j < colorViews.length; j++) {
                        View parent = (View) colorViews[j].getParent();
                        if (v != colorViews[j]) {
                            parent.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
                        } else {
                            parent.setBackground(getContext().getResources().getDrawable(R.drawable.draw_holo_blue_border));
                            selectedDiscountColor = (Enum.DiscountColor)v.getTag();
                        }
                        parent.setPadding(2, 2, 2, 2);
                    }

                }
            });
        }
        //promotion trigger
        SetTextLabelProperties((TextView)findViewById(R.id.tvToLimitLabel));
        SetTextLabelProperties((TextView)findViewById(R.id.tvFromAboveLabel));
        //discount label
        //SetTextLabelProperties((TextView)findViewById(R.id.tvDiscountLabel));
        //SetTextLabelProperties((TextView)findViewById(R.id.tvDollarSignLabel));
        //SetTextLabelProperties((TextView)findViewById(R.id.tvPercentageSignLabel));

        imgCancel = (ImageView)findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        imgSave = (ImageView)findViewById(R.id.imgSave);
        imgSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                common.Utility.LogActivity("Save promotion");

                if(ValidateInput())
                {
                    setCancelable(false);

                    UpdatePromotionObjectProperties(copiedPromotionObject);

                    imgCancel.setVisibility(View.GONE);
                    v.setVisibility(View.GONE);
                    ProgressBar pb = new ProgressBar(getContext());
                    pb.setLayoutParams(new RelativeLayout.LayoutParams(common.Utility.DP2Pixel(60,getContext()), ViewGroup.LayoutParams.MATCH_PARENT));
                    RelativeLayout rl = (RelativeLayout) imgCancel.getParent();
                    ((RelativeLayout.LayoutParams)pb.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rl.addView(pb);

                    SavePromotionAsyncTask task = new SavePromotionAsyncTask();
                    originalPromotionObject=copiedPromotionObject;
                    task.execute(originalPromotionObject);




                }
            }
        });
    }

    private void RestoreAfterSaveState()
    {
        RelativeLayout rl = (RelativeLayout) imgCancel.getParent();
        rl.removeViewAt(rl.getChildCount()-1);
        imgSave.setVisibility(View.VISIBLE);
        imgCancel.setVisibility(View.VISIBLE);
        setCancelable(true);
    }
    private void UpdatePromotionObjectProperties(PromotionObject promotionObject)
    {
        promotionObject.SetTitle(txtTitle.getText().toString());



        //promotion activation time and end time
        promotionObject.SetStartDateTime((Long) tvFromDate.getTag());
        promotionObject.SetEndDateTime((Long) tvToDate.getTag());
        promotionObject.SetExpirationDate((Long)tvEndOn.getTag());




        //check user select day of week or day of month
        promotionObject.SetPromotionDateOption(selectedPromotionDateOption);
        if(selectedPromotionDateOption== Enum.PromotionDateOption.day)
        {



            int dayValue =0;
            for(int i=0;i<dayViews.length;i++)
            {
                if(dayViews[i].getTag()!=null) {
                    if(i==0){dayValue+= Enum.Day.Monday.value;}
                    else if(i==1){dayValue+= Enum.Day.Tuesday.value;}
                    else if(i==2){dayValue+= Enum.Day.Wednesday.value;}
                    else if(i==3){dayValue+= Enum.Day.Thursday.value;}
                    else if(i==4){dayValue+= Enum.Day.Friday.value;}
                    else if(i==5){dayValue+= Enum.Day.Saturday.value;}
                    else if(i==6){dayValue+= Enum.Day.Sunday.value;}

                }

            }
            promotionObject.SetOccurDay(dayValue);
        }
        else if(selectedPromotionDateOption== Enum.PromotionDateOption.once)
        {
            /**activation time and end time are same with others**/

        }
        else if(selectedPromotionDateOption== Enum.PromotionDateOption.month)
        {

            int monthValue =0;
            for(int i=0;i<monthViews.length;i++)
            {
                if(monthViews[i].getTag()!=null) {
                    if(i==0){monthValue+= Enum.Month.Jan.value;}
                    else if(i==1){monthValue|= Enum.Month.Feb.value;}
                    else if(i==2){monthValue|= Enum.Month.Mar.value;}
                    else if(i==3){monthValue|= Enum.Month.Apr.value;}
                    else if(i==4){monthValue|= Enum.Month.May.value;}
                    else if(i==5){monthValue|= Enum.Month.Jun.value;}
                    else if(i==6){monthValue|= Enum.Month.Jul.value;}
                    else if(i==7){monthValue|= Enum.Month.Aug.value;}
                    else if(i==8){monthValue|= Enum.Month.Sep.value;}
                    else if(i==9){monthValue|= Enum.Month.Oct.value;}
                    else if(i==10){monthValue|= Enum.Month.Nov.value;}
                    else if(i==11){monthValue|= Enum.Month.Dec.value;}

                }

            }

            promotionObject.SetOccurMonth(monthValue);

            String strSelectedCalendarDays="";
            for(int i=0;i<daysOfMonth.length;i++)
            {
                if(daysOfMonth[i].getTag()!=null)
                {
                    strSelectedCalendarDays+=daysOfMonth[i].getText()+",";
                }
            }

            //remove the last comma
            if(strSelectedCalendarDays.length()>0)strSelectedCalendarDays = strSelectedCalendarDays.substring(0,strSelectedCalendarDays.length()-1);

            promotionObject.SetDayOfMonth(strSelectedCalendarDays);
        }

        if(rdsRepeat[0].isChecked()) {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("Weekly"));
        }
        else if(rdsRepeat[1].isChecked()) {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("TwoWeek"));
        }
        else if(rdsRepeat[2].isChecked()) {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("ThreeWeek"));
        }
        else
        {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("Monthly"));
        }


        //promotion expiration date
        promotionObject.SetExpirationDate((Long) tvEndOn.getTag());



        Enum.PromotionByType selectedPromotionByType = this.selectedPromotionByType;
        promotionObject.SetRule(selectedPromotionByType);
        promotionObject.SetStartingAmount(txtAmountFrom.getText().toString().replaceAll("[$,]", ""));
        promotionObject.SetUpperLimitAmount(txtAmountLimit.getText().toString().replaceAll("[$,]", ""));
        promotionObject.SetUpperLimitFlag(!chkNoUpperLimit.isChecked());
        promotionObject.SetDiscountType(selectedDiscountType);
        double dblValue = Double.parseDouble(
                txtDiscountAmount.getText().toString().replaceAll("[%$,]", ""));
        promotionObject.SetDiscountValue((selectedDiscountType == Enum.DiscountType.cash) ? dblValue*-1 : dblValue / -100f);//divide by 100%

        promotionObject.SetDiscountColor(selectedDiscountColor);
    }
    private void AddRuleItemList(HashMap<Long,Integer>newHM)
    {
        int flRowIndex = copiedPromotionObject.ruleItems.size()*2;
        copiedPromotionObject.ruleItems.add(newHM);
        int listIndex = copiedPromotionObject.ruleItems.size()-1;
        ListRuleItems(listIndex,flRowIndex,copiedPromotionObject.ruleItems.get(listIndex));
    }
    private void UpdateRuleItemList(HashMap<Long,Integer>newHM,int listIndex)
    {


        copiedPromotionObject.ruleItems.add(listIndex,newHM);
        copiedPromotionObject.ruleItems.remove(listIndex+1);
        ListRuleItems(listIndex,(listIndex*2),copiedPromotionObject.ruleItems.get(listIndex));

    }
    private void ListRuleItems(int ruleItemIndex,int flRowIndex,HashMap<Long,Integer> record)
    {
        if(flRowIndex==llItems.getChildCount() || llItems.getChildCount()==0)
        {
            FlowLayout fl = CreateRuleItemRow();
            //fl.setTag(listIndex);
            llItems.addView(fl);

            //create edit and delete item option at the end
            CreateOptionRow();
        }
        FlowLayout fl = (FlowLayout)llItems.getChildAt(flRowIndex);
        CreateItemInFlowLayout(fl,record,flRowIndex);

        //remove empty row
        if(fl.getChildCount()==0) {
            llItems.removeViewAt(flRowIndex);//item row
            llItems.removeViewAt(flRowIndex);//option row
        }


    }
    private void CreateOptionRow()
    {
        int size = common.Utility.DP2Pixel(35,getContext());
        RelativeLayout rl = new RelativeLayout(getContext());
        rl.setPadding(0,0,5,0);
        llItems.addView(rl);

        ImageView imgDelete = new ImageView(getContext());
        imgDelete.setId(dynamicId++);
        imgDelete.setBackground(getContext().getResources().getDrawable(R.drawable.green_border_delete));
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(size,size);
        rllp.setMargins(5,0,0,0);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Remove");
                b.setMessage("Remove the entire row?");
                b.setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RemoveWholeRow(view);
                    }
                });
                b.setNegativeButton("Cancel", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing, allowing the dialog to close
                    }
                });
                b.show();

            }
        });
        rl.addView(imgDelete,rllp);

        ImageView imgEdit = new ImageView(getContext());
        imgEdit.setBackground(getContext().getResources().getDrawable(R.drawable.green_border_edit));
        imgEdit.setId(dynamicId++);
        rllp = new RelativeLayout.LayoutParams(size,size);
        rllp.addRule(RelativeLayout.LEFT_OF,imgDelete.getId());
        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditSelectedRowRuleItems(view);
            }
        });
        rl.addView(imgEdit,rllp);
    }
    private void EditSelectedRowRuleItems(View view)
    {
        int itemListIndex=-1;
        ItemObject io=null;
        for(int i=1;i<llItems.getChildCount();i+=2)
        {
            RelativeLayout rl = (RelativeLayout)llItems.getChildAt(i);
            for(int j=0;j<rl.getChildCount();j++)
            {
                if(rl.getChildAt(j).getId()==view.getId())
                {
                    itemListIndex = ((i+1)/2)-1;//deduct the option row count
                    String strItems = "";
                    HashMap<Long,Integer> map = copiedPromotionObject.ruleItems.get(itemListIndex);
                    for(long key:map.keySet())
                    {
                        io = common.myMenu.GetLatestItem(key);
                        if(io!=null) {
                            strItems += map.get(key) + "|" + key + "|" + io.getParentID() + ",";
                        }
                        else
                        {
                            strItems += map.get(key) + "|" + key + "|" + key + ",";
                        }
                    }
                    if(strItems.length()>0)
                    {
                        //remove the last comma
                        strItems = strItems.substring(0,strItems.length()-1);
                    }
                    SelectPromotionItemRowDialog d = new SelectPromotionItemRowDialog(getContext(), AddPromotionDialog.this,strItems,copiedPromotionObject,itemListIndex,true);
                    d.show();
                    break;
                }
            }
        }

    }
    /*private PromotionObject SavePromotion(PromotionObject promotionObject)
    {


        *//*promotionObject.SetTitle(txtTitle.getText().toString());



        //promotion activation time and end time
        promotionObject.SetStartDateTime((Long) tvFromDate.getTag());
        promotionObject.SetEndDateTime((Long) tvToDate.getTag());
        promotionObject.SetExpirationDate((Long)tvEndOn.getTag());




        //check user select day of week or day of month
        promotionObject.SetPromotionDateOption(selectedPromotionDateOption);
        if(selectedPromotionDateOption== Enum.PromotionDateOption.day)
        {



            int dayValue =0;
            for(int i=0;i<dayViews.length;i++)
            {
                if(dayViews[i].getTag()!=null) {
                    if(i==0){dayValue+= Enum.Day.Monday.value;}
                    else if(i==1){dayValue+= Enum.Day.Tuesday.value;}
                    else if(i==2){dayValue+= Enum.Day.Wednesday.value;}
                    else if(i==3){dayValue+= Enum.Day.Thursday.value;}
                    else if(i==4){dayValue+= Enum.Day.Friday.value;}
                    else if(i==5){dayValue+= Enum.Day.Saturday.value;}
                    else if(i==6){dayValue+= Enum.Day.Sunday.value;}

                }

            }
            promotionObject.SetOccurDay(dayValue);
        }
        else if(selectedPromotionDateOption== Enum.PromotionDateOption.once)
        {
            *//**//**activation time and end time are same with others**//**//*

        }
        else if(selectedPromotionDateOption== Enum.PromotionDateOption.month)
        {

            int monthValue =0;
            for(int i=0;i<monthViews.length;i++)
            {
                if(monthViews[i].getTag()!=null) {
                    if(i==0){monthValue+= Enum.Month.Jan.value;}
                    else if(i==1){monthValue|= Enum.Month.Feb.value;}
                    else if(i==2){monthValue|= Enum.Month.Mar.value;}
                    else if(i==3){monthValue|= Enum.Month.Apr.value;}
                    else if(i==4){monthValue|= Enum.Month.May.value;}
                    else if(i==5){monthValue|= Enum.Month.Jun.value;}
                    else if(i==6){monthValue|= Enum.Month.Jul.value;}
                    else if(i==7){monthValue|= Enum.Month.Aug.value;}
                    else if(i==8){monthValue|= Enum.Month.Sep.value;}
                    else if(i==9){monthValue|= Enum.Month.Oct.value;}
                    else if(i==10){monthValue|= Enum.Month.Nov.value;}
                    else if(i==11){monthValue|= Enum.Month.Dec.value;}

                }

            }

            promotionObject.SetOccurMonth(monthValue);

            String strSelectedCalendarDays="";
            for(int i=0;i<daysOfMonth.length;i++)
            {
                if(daysOfMonth[i].getTag()!=null)
                {
                    strSelectedCalendarDays+=daysOfMonth[i].getText()+",";
                }
            }

            //remove the last comma
            if(strSelectedCalendarDays.length()>0)strSelectedCalendarDays = strSelectedCalendarDays.substring(0,strSelectedCalendarDays.length()-1);

            promotionObject.SetDayOfMonth(strSelectedCalendarDays);
        }

        if(rdsRepeat[0].isChecked()) {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("Weekly"));
        }
        else if(rdsRepeat[1].isChecked()) {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("TwoWeek"));
        }
        else if(rdsRepeat[2].isChecked()) {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("ThreeWeek"));
        }
        else
        {
            promotionObject.SetAlternateOccurrence(Enum.OccurrenceWeek.valueOf("Monthly"));
        }


        //promotion expiration date
        promotionObject.SetExpirationDate((Long) tvEndOn.getTag());



        Enum.PromotionByType selectedPromotionByType = this.selectedPromotionByType;
        promotionObject.SetRule(selectedPromotionByType);
        promotionObject.SetStartingAmount(txtAmountFrom.getText().toString().replaceAll("[$,]", ""));
        promotionObject.SetUpperLimitAmount(txtAmountLimit.getText().toString().replaceAll("[$,]", ""));
        promotionObject.SetUpperLimitFlag(!chkNoUpperLimit.isChecked());
        promotionObject.SetDiscountType(selectedDiscountType);
        double dblValue = Double.parseDouble(
                txtDiscountAmount.getText().toString().replaceAll("[%$,]", ""));
        promotionObject.SetDiscountValue((selectedDiscountType == Enum.DiscountType.cash) ? dblValue : dblValue / 100f);//divide by 100%

        promotionObject.SetDiscountColor(selectedDiscountColor);*//*

        //if(imgCancel.getVisibility()==View.VISIBLE)
        if(blnUpdateMode)
        {
            if(common.myPromotionManager.Update(promotionObject))
            {
                //remove the record from activity and wait for redraw with pass back object
                parent.RemovePromotionFromUIAndCache(promotionObject.GetId());
                //hide the cancel button
                findViewById(R.id.imgCancel).setVisibility(View.INVISIBLE);
                return promotionObject;//update in promotion manager(referencing)
            }
            else
            {
                //revert it if update failed
                common.myPromotionManager.ReloadPromotion(promotionObject.GetId());
                promotionObject= common.myPromotionManager.Get(promotionObject.GetId());
                return promotionObject;
            }
        }
        else {
            if (common.myPromotionManager.Save(promotionObject) > 0) {
                return promotionObject;
            }
        }

        return null;
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private boolean ValidateInput()
    {
        //check title
        String strTitle =txtTitle.getText().toString().trim();
        txtTitle.setText(strTitle);
        if(strTitle.length()==0)
        {
            common.Utility.ShowMessage("Save", "Please provide a subject title for this promotion", getContext(), R.drawable.no_access);
            return false;
        }



        //make sure the user has checked any day of week if is day of week mode
        if(selectedPromotionDateOption== Enum.PromotionDateOption.day) {
            for (int i = 0; i < dayViews.length; i++) {
                if (i == dayViews.length - 1 && dayViews[i].getTag() == null) {
                    common.Utility.ShowMessage("Save", "Please select a day for this promotion to occur", getContext(), R.drawable.no_access);
                    return false;
                } else if (dayViews[i].getTag() != null) {
                    //at least one day has been selected
                    break;
                }
            }
        }
        else  if(selectedPromotionDateOption== Enum.PromotionDateOption.month)
        {
            //check whether user has select at least one month
            for(int i=0;i<monthViews.length;i++)
            {
                if(i==monthViews.length-1 && monthViews[i].getTag()==null)
                {
                    common.Utility.ShowMessage("Save", "Please select a calendar month for this promotion to occur", getContext(), R.drawable.no_access);
                    return false;
                } else if (monthViews[i].getTag() != null) {
                    break;
                }
            }
            //check whether user has select at least one day
            for(int i=0;i<daysOfMonth.length;i++)
            {
                if(i==daysOfMonth.length-1 && daysOfMonth[i].getTag()==null)
                {
                    common.Utility.ShowMessage("Save", "Please select a calendar day for this promotion to occur", getContext(), R.drawable.no_access);
                    return false;
                } else if (daysOfMonth[i].getTag() != null) {
                    break;
                }
            }
        }
        else  if(selectedPromotionDateOption== Enum.PromotionDateOption.once)
        {
            //check start date time and end date time
            long timeStart = (Long)tvFromDate.getTag();
            long timeEnd = (Long)tvToDate.getTag();
            if(timeEnd-timeStart<10000)
            {
                common.Utility.ShowMessage("Save", "Please provide a future end date and time", getContext(), R.drawable.no_access);
                return false;
            }

            //check end time>start time
            Calendar cStart = new GregorianCalendar();
            cStart.setTimeInMillis(timeStart);
            int startMinute = cStart.get(Calendar.HOUR_OF_DAY)*60+cStart.get(Calendar.MINUTE);

            Calendar cEnd = new GregorianCalendar();
            cEnd.setTimeInMillis(timeEnd);
            int endMinute = cEnd.get(Calendar.HOUR_OF_DAY)*60+cEnd.get(Calendar.MINUTE);
            if(endMinute-startMinute<=0)
            {
                common.Utility.ShowMessage("Save", "Please provide a greater end time", getContext(), R.drawable.no_access);
                return false;
            }


          /*  //check the start date is current or in the future but not past
            if(cStart.getTimeInMillis()-Calendar.getInstance().getTimeInMillis()<1000*60*4)
            {
                common.Utility.ShowMessage("Save", "Please provide a start time that's 5 minutes later from now.", getContext(), R.drawable.no_access);
                return false;
            }*/
        }

        //now check the promotion hour, except 'ONCE'
        if(selectedPromotionDateOption!= Enum.PromotionDateOption.once)
        {
            //ignore the date component
            Calendar c1 = new GregorianCalendar();
            c1.setTimeInMillis((Long)tvTimeEnd.getTag());


            Calendar c2 = new GregorianCalendar();
            c2.setTimeInMillis((Long)tvTimeStart.getTag());
            //          Date d2 = new Date(c2.getTimeInMillis());
            c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
            c2.set(Calendar.MONTH,c1.get(Calendar.MONTH));
            c2.set(Calendar.DAY_OF_MONTH,c1.get(Calendar.DAY_OF_MONTH));

            tvTimeStart.setTag(c2.getTimeInMillis());

            if(c2.getTimeInMillis()>=c1.getTimeInMillis())
            {
                common.Utility.ShowMessage("Save", "Please provide a larger promotion end time", getContext(), R.drawable.no_access);
                return false;
            }
        }



        //check event for it to trigger
        if(selectedPromotionByType== Enum.PromotionByType.item)
        {
            //check at least one item
            if(llItems.getChildCount()==0)
            {
                common.Utility.ShowMessage("Save","Please select at least an item for this promotion",getContext(),R.drawable.no_access);
                return false;
            }


        }
        else
        {
            //check is greater than $0.01
            double dblFrom = Double.parseDouble(txtAmountFrom.getText().toString().replaceAll("[$,]","").trim());
            double dblTo = Double.parseDouble(txtAmountLimit.getText().toString().replaceAll("[$,]", "").trim());
            if(dblFrom<=0)
            {
                common.Utility.ShowMessage("Save","Please set a starting amount this promotion",getContext(),R.drawable.no_access);
                return false;
            }
            if(!chkNoUpperLimit.isChecked())
            {
                if(dblFrom>=dblTo)
                {
                    common.Utility.ShowMessage("Save","Starting amount cannot be greater than or equal to limit amount",getContext(),R.drawable.no_access);
                    return false;
                }
            }

        }

        //check discounted value/%
        if(selectedDiscountType== Enum.DiscountType.cash)
        {
            double dblDiscountAmount = Double.parseDouble(txtDiscountAmount.getText().toString().replaceAll("[$,-]", "").trim());
            if(dblDiscountAmount==0)
            {
                common.Utility.ShowMessage("Save","Discount value cannot be zero",getContext(),R.drawable.no_access);
                return false;
            }
        }
        else
        {
            double dblDiscountPercentage = Double.parseDouble(txtDiscountAmount.getText().toString().replaceAll("[%,-]","").trim());
            if(dblDiscountPercentage==0)
            {
                common.Utility.ShowMessage("Save","Discount percentage cannot be zero",getContext(),R.drawable.no_access);
                return false;
            }
        }

        return true;
    }
    private boolean PromotionByTypeClicked(View v)
    {
        if(v==tvSelectedPromotionType)return false;
        v.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));

        if (tvSelectedPromotionType != null) {

            tvSelectedPromotionType.setTextColor(getContext().getResources().getColor(R.color.divider_grey));
            tvSelectedPromotionType.setBackground(null);
        }
        tvSelectedPromotionType = (TextView) v;

        tvSelectedPromotionType.setTextColor(getContext().getResources().getColor(R.color.green));
        return true;
    }
    private void SetDiscountColor(Enum.DiscountColor dc,PromotionObject promotionObject)
    {
        promotionObject.SetDiscountColor(dc);
    }
    private void CreateClickEffectForRoundedBoarder(View v,final Enum.PromotionByType pbt)
    {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View innerView) {

                selectedPromotionByType = pbt;
                if (PromotionByTypeClicked(innerView)) {
                    switch (pbt) {
                        case item:
                            common.Utility.LogActivity("by item clicked");
                            ShowPanel(R.id.ruleItemPanel, Enum.PromotionActivityPanel.input_promotion_by_panel);

                            break;
                        case total:
                            common.Utility.LogActivity("by total clicked");
                            ShowPanel(R.id.ruleAmountPanel, Enum.PromotionActivityPanel.input_promotion_by_panel);

                            break;

                        default:


                    }
                }
            }
        });
    }
    private void DiscountBy(Enum.DiscountType dt)
    {


        if(dt.value == Enum.DiscountType.cash.value)
        {
            if(blnDoNotReset)
            {
               blnDoNotReset=false;
            }
            else
            {
                txtDiscountAmount.setText("0.00");
            }
            txtDiscountAmount.removeTextChangedListener(percentageTextWatcher);

            txtDiscountAmount.addTextChangedListener(priceTextWatcher);
            tvPercentageSignLabel.setVisibility(View.INVISIBLE);
            tvDollarSignLabel.setText("-$");
            //txtDiscountAmount.setText("-$0.00");
            //txtDiscountAmount.setText("0.00");



        }
        else if(dt.value == Enum.DiscountType.percentage.value)
        {
            if(blnDoNotReset)
            {
                blnDoNotReset=false;
            }
            else {
                txtDiscountAmount.setText("00.00");
            }
            txtDiscountAmount.removeTextChangedListener(priceTextWatcher);

            txtDiscountAmount.addTextChangedListener(percentageTextWatcher);
            tvPercentageSignLabel.setVisibility(View.VISIBLE);
            tvDollarSignLabel.setText("-");
            //txtDiscountAmount.setText("-0.00%");
            //txtDiscountAmount.setText("0.00");




        }
        txtDiscountAmount.setSelection(txtDiscountAmount.getText().length());
        copiedPromotionObject.SetDiscountType(dt);
    }
    private boolean DiscountTypeClicked(View v)
    {
        if(v==tvSelectedDiscountType)return false;
        v.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));

        if (tvSelectedDiscountType != null) {

            tvSelectedDiscountType.setTextColor(getContext().getResources().getColor(R.color.divider_grey));
            tvSelectedDiscountType.setBackground(null);
        }
        tvSelectedDiscountType = (TextView) v;
        tvSelectedDiscountType.setTextColor(getContext().getResources().getColor(R.color.green));
        return true;
    }
    private void CreateClickEffectForRoundedBoarder(View v,final Enum.DiscountType dt)
    {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View innerView) {
                selectedDiscountType = dt;
                if (DiscountTypeClicked(innerView)) {
                    switch (dt) {
                        case cash:
                            common.Utility.LogActivity("cash discount clicked");
                            DiscountBy(Enum.DiscountType.cash);
                            break;
                        case percentage:
                            common.Utility.LogActivity("percentage discount clicked");
                            DiscountBy(Enum.DiscountType.percentage);
                            break;
                        default:
                    }
                }
            }
        });
    }

    private String ReturnDateString(int month,int day,int year)
    {
        return ((month > 9) ? month : "0" + month) +
                "/" +
                ((day > 9) ? day : "0" + day) + "/" +
                year;
    }
    private void UpdateTextViewDate(Calendar cal,Enum.CallDateDialogFrom dialogFor)
    {

        String strDate =ReturnDateString(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
        if(dialogFor== Enum.CallDateDialogFrom.endOn || dialogFor== Enum.CallDateDialogFrom.to)
        {
            tvEndOn.setText(Html.fromHtml("<u>" + strDate + "</u>"));
            tvEndOn.setTag(cal.getTimeInMillis());
            tvToDate.setText(Html.fromHtml("<u>" + strDate + "</u>"));
            tvToDate.setTag(cal.getTimeInMillis());

            tvToTime.setTag(cal.getTimeInMillis());
            tvTimeEnd.setTag(cal.getTimeInMillis());

        }
        else if(dialogFor== Enum.CallDateDialogFrom.from)
        {
            tvFromDate.setText(Html.fromHtml("<u>" + strDate + "</u>"));
            tvFromDate.setTag(cal.getTimeInMillis());
            //update tag for starting time
            tvFromTime.setTag(cal.getTimeInMillis());
            tvTimeStart.setTag(cal.getTimeInMillis());
        }
    }
    public void TriggerDiscountByCashClick() {
        findViewById(R.id.tvCashDiscount).callOnClick();
    }
    public void TriggerDiscountByPercentageClick() {
        findViewById(R.id.tvPercentageDiscount).callOnClick();
    }
    private void ShowPromotionDetail()
    {
        blnUpdateMode=true;
        imgCancel.setVisibility(View.VISIBLE);
        //Calendar c = new GregorianCalendar();

        //title
        txtTitle.setText(copiedPromotionObject.GetTitle());
        txtTitle.setSelection(copiedPromotionObject.GetTitle().length());

        //color
        if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_blue)colorViews[0].callOnClick();
        else if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_green)colorViews[1].callOnClick();
        else if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_indigo)colorViews[2].callOnClick();
        else if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_pink)colorViews[3].callOnClick();
        else if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_yellow)colorViews[4].callOnClick();
        else if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_orange)colorViews[5].callOnClick();
        else if(copiedPromotionObject.GetDiscountColor()== Enum.DiscountColor.discount_brown)colorViews[6].callOnClick();


        //promotion date detail


        if(copiedPromotionObject.GetPromotionDateOption()== Enum.PromotionDateOption.day) {
            findViewById(R.id.tvOptionDay).callOnClick();

            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Monday.value)== Enum.Day.Monday.value)
            {
                dayViews[0].callOnClick();
            }
            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Tuesday.value)== Enum.Day.Tuesday.value)
            {
                dayViews[1].callOnClick();
            }
            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Wednesday.value)== Enum.Day.Wednesday.value)
            {
                dayViews[2].callOnClick();
            }
            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Thursday.value)== Enum.Day.Thursday.value)
            {
                dayViews[3].callOnClick();
            }
            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Friday.value)== Enum.Day.Friday.value)
            {
                dayViews[4].callOnClick();
            }
            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Saturday.value)== Enum.Day.Saturday.value)
            {
                dayViews[5].callOnClick();
            }
            if((copiedPromotionObject.GetOccurDay()& Enum.Day.Sunday.value)== Enum.Day.Sunday.value)
            {
                dayViews[6].callOnClick();
            }

            if(copiedPromotionObject.GetOccurrence()==Enum.OccurrenceWeek.Weekly)
            {
                rdsRepeat[0].callOnClick();
            }
            else if(copiedPromotionObject.GetOccurrence()==Enum.OccurrenceWeek.TwoWeek)
            {
                rdsRepeat[1].callOnClick();
            }
            else if(copiedPromotionObject.GetOccurrence()==Enum.OccurrenceWeek.ThreeWeek)
            {
                rdsRepeat[2].callOnClick();
            }
            else if(copiedPromotionObject.GetOccurrence()==Enum.OccurrenceWeek.Monthly)
            {
                rdsRepeat[3].callOnClick();
            }
        }
        else if(copiedPromotionObject.GetPromotionDateOption()== Enum.PromotionDateOption.once)
        {
            findViewById(R.id.tvOptionOnce).callOnClick();

        }
        else if(copiedPromotionObject.GetPromotionDateOption()== Enum.PromotionDateOption.month) {
            findViewById(R.id.tvOptionMonth).callOnClick();
            CheckSelectedMonth(copiedPromotionObject);
            CheckSelectedDate(copiedPromotionObject);
        }

        //expiration date and time
        ShowPromotionDateTime(copiedPromotionObject);

        //promotion triggered by
        //compare with original promotion object, because the properties might be altered by setup procedure
        //copiedPromotionObject.SetRule(originalPromotionObject.GetRule());
        if(copiedPromotionObject.GetRule()== Enum.PromotionByType.item)
        {
            //mark it on UI
            findViewById(R.id.tvOptionByItem).callOnClick();

            for(int i=0;i<copiedPromotionObject.ruleItems.size();i++)
            {
                ListRuleItems(i,i*2,copiedPromotionObject.ruleItems.get(i));
                //remove it if there isn't anymore item inside, deleted item has been removed while calling ListRuleItems()
                if(copiedPromotionObject.ruleItems.get(i).size()==0)
                {
                    copiedPromotionObject.ruleItems.remove(i);
                    i--;
                }
            }

            //compare size see any item removal
            if (originalPromotionObject.ruleItems.size()!=copiedPromotionObject.ruleItems.size())
            {
                blnDismissDialog=false;
            }
            else
            {
                for(int i=0;i<copiedPromotionObject.ruleItems.size();i++)
                {
                    if(copiedPromotionObject.ruleItems.get(i).size()!=originalPromotionObject.ruleItems.get(i).size())
                    {
                        blnDismissDialog = false;
                        break;
                    }
                }
            }


        }
        else {
            findViewById(R.id.tvOptionByTotal).callOnClick();
            txtAmountFrom.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(copiedPromotionObject.GetStartingAmount()));
            txtAmountLimit.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(copiedPromotionObject.GetUpperLimitAmount()));
            chkNoUpperLimit.setChecked(!copiedPromotionObject.GetUpperLimitFlag());
        }

        //discount $/%
        //compare with original promotion object, because the properties might be altered by setup procedure
        //copiedPromotionObject.SetDiscountType(originalPromotionObject.GetDiscountType());
        if(copiedPromotionObject.GetDiscountType()== Enum.DiscountType.cash)
        {
            //findViewById(R.id.tvCashDiscount).callOnClick();
            //txtDiscountAmount.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(copiedPromotionObject.GetDiscountValue())));
            txtDiscountAmount.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(copiedPromotionObject.GetDiscountValue())).replaceAll("[-$]",""));
        }
        else
        {
            //findViewById(R.id.tvPercentageDiscount).callOnClick();
            //String strTemp = copiedPromotionObject.GetDiscountString().replaceAll("[-%]","");
            //txtDiscountAmount.setText(strTemp);
            txtDiscountAmount.setText(copiedPromotionObject.GetDiscountString().replaceAll("[-%]",""));//use formatted string with two zero after floating pt

        }
        //wait for all the UI displaying on screen and trigger save if update needed
        if(blnDismissDialog==false)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    imgSave.callOnClick();
                }
            },100);

        }

    }


    private void CreateItemInFlowLayout(FlowLayout fl, HashMap<Long,Integer>items, final int rowIndex)
    {
        PromotionRuleItemRow row=null;
        ItemObject io=null;
        fl.removeAllViews();
        ArrayList<Long>deletedItems=new ArrayList<Long>();//keeping track of deleted item
        for(long key:items.keySet())
        {
            io = common.myMenu.GetLatestItem(key);
            if(io==null)
            {
                //is by category
                io = new ItemObject(key,"Any",key,"0","",false,0,1);
            }
            row = new PromotionRuleItemRow(getContext());
            if(io!=null) {

                row.SetProperties(io.getName(), items.get(key), key, this, rowIndex);
                fl.addView(row);
            }
            else
            {
                deletedItems.add(key);
            }
        }

        //now remove it one by one
        for(Long id:deletedItems)
        {items.remove(id.longValue());}


    }
    private FlowLayout CreateRuleItemRow()
    {
        FlowLayout fl = new FlowLayout(getContext());
        //fl.setBackgroundColor(Color.RED);
        //ViewGroup.MarginLayoutParams lp  = new ViewGroup.MarginLayoutParams(common.Utility.DP2Pixel(420,getContext()), ViewGroup.LayoutParams.WRAP_CONTENT);
        //fl.setLayoutParams(lp);
        fl.setPadding(3,3,3,3);
        return fl;
    }
    private void CheckSelectedMonth(PromotionObject po)
    {
        if((po.GetOccurMonth()& Enum.Month.Jan.value)==Enum.Month.Jan.value)
            monthViews[0].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Feb.value)==Enum.Month.Feb.value)
            monthViews[1].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Mar.value)==Enum.Month.Mar.value)
            monthViews[2].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Apr.value)==Enum.Month.Apr.value)
            monthViews[3].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.May.value)==Enum.Month.May.value)
            monthViews[4].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Jun.value)==Enum.Month.Jun.value)
            monthViews[5].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Jul.value)==Enum.Month.Jul.value)
            monthViews[6].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Aug.value)==Enum.Month.Aug.value)
            monthViews[7].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Sep.value)==Enum.Month.Sep.value)
            monthViews[8].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Oct.value)==Enum.Month.Oct.value)
            monthViews[9].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Nov.value)==Enum.Month.Nov.value)
            monthViews[10].callOnClick();
        if((po.GetOccurMonth()& Enum.Month.Dec.value)==Enum.Month.Dec.value)
            monthViews[11].callOnClick();
    }
    private void ShowPromotionDateTime(PromotionObject po)
    {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(po.GetStartDateTime());
        String strDate =ReturnDateString(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
        tvFromDate.setTag(po.GetStartDateTime());
        tvFromDate.setText(Html.fromHtml("<u>" + strDate + "</u>"));

        cal.setTimeInMillis(po.GetEndDateTime());
        strDate =ReturnDateString(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
        tvToDate.setTag(po.GetEndDateTime());
        tvToDate.setText(Html.fromHtml("<u>" + strDate + "</u>"));

        tvEndOn.setTag(po.GetExpirationDate());
        tvEndOn.setText(Html.fromHtml("<u>" + strDate + "</u>"));

        SetCurrentTimeForTimeInput(tvTimeStart, new Date(po.GetStartDateTime()));
        tvTimeStart.setTag(po.GetStartDateTime());
        SetCurrentTimeForTimeInput(tvFromTime, new Date(po.GetStartDateTime()));
        tvFromTime.setTag(po.GetStartDateTime());
        SetCurrentTimeForTimeInput(tvTimeEnd, new Date(po.GetEndDateTime()));
        tvTimeEnd.setTag(po.GetEndDateTime());
        SetCurrentTimeForTimeInput(tvToTime, new Date(po.GetEndDateTime()));
        tvToTime.setTag(po.GetEndDateTime());
    }
    private  void CheckSelectedDate(PromotionObject po)
    {
        String[] days = po.GetDayOfMonth().split(",");
        for(int i=0;i<days.length;i++)
        {
            daysOfMonth[Integer.parseInt(days[i])-1].callOnClick();
        }
    }

    @Override
    public void dismiss() {
        //reset dialog flag in parenttxtDiscountAmount
        if(parent!=null)parent.ResetPromotionDialogPopupFlag();
        super.dismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_add_promotion_dialog);
        ConfigureDataInputPanel();
        boolean blnDiscountByCash = true;
        if(copiedPromotionObject.GetId()>0) {
            blnDoNotReset=true;
            //recreate the existing promotion object, because the setup process has altered its initial values
            copiedPromotionObject = new PromotionObject(originalPromotionObject);
            ShowPromotionDetail();
            if(copiedPromotionObject.GetDiscountType()== Enum.DiscountType.percentage)
            {
                blnDiscountByCash=false;
            }
        }
        if(blnDiscountByCash)
        {
            TriggerDiscountByCashClick();
        }
        else
        {
            TriggerDiscountByPercentageClick();
        }
       /* final boolean flagCash =blnDiscountByCash;
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if(flagCash)
                {
                    TriggerDiscountByCashClick();
                }
                else
                {
                    TriggerDiscountByPercentageClick();
                }
            }
        }, 10000);*/

    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if(!blnDateDialogShow)return;
        if(blnDateDialogShow)blnDateDialogShow = false;
        Calendar cal = new GregorianCalendar();//Calendar.getInstance();

        //include time component for storing
        TextView tvTime = (dialogFor== Enum.CallDateDialogFrom.from)?(TextView)findViewById(R.id.tvFromTime):(TextView)findViewById(R.id.tvToTime);
        String strTime = tvTime.getText()+"";
        int hr = Integer.parseInt(strTime.substring(0,2).replace(":",""));
        int minute = Integer.parseInt(strTime.substring(3, 5).trim());
        cal.set(year, month, day
                ,(strTime.contains("PM") && hr !=12)?hr+12:hr,minute,59);

        UpdateTextViewDate(cal,dialogFor);

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        //reset flag
        if(!blnDateDialogShow)return;
        if(blnDateDialogShow)blnDateDialogShow = false;


        //update date tag
        TextView tv =ReturnCurrentDateTextLabel();
        Calendar c = new GregorianCalendar();
        Date date = new Date();
        if(tv!=null) {
            date = new Date((Long) tv.getTag());
        }

        c.setTime(date);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
        //time
        TextView tv2;
        TextView tv3;
        if(dialogFor== Enum.CallDateDialogFrom.day_month_expiration_start_time || Enum.CallDateDialogFrom.from==dialogFor)
        {
            tv3 = tvFromTime;
            tv2 = tvTimeStart;

            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
        }
        else
        {
            tv3 = tvToTime;
            tv2 = tvTimeEnd;

            c.set(Calendar.SECOND,59);
            c.set(Calendar.MILLISECOND,999);
        }

        //set Date
        date = new Date(c.getTimeInMillis());

        if(tv!=null) {
            //save time component in date textview
            tv.setTag(date.getTime());
        }





        tv3.setTag(date.getTime());
        SetCurrentTimeForTimeInput(tv3, date);
        tv2.setTag(date.getTime());
        SetCurrentTimeForTimeInput(tv2, date);

        //tv.setText(Html.fromHtml("<u>" + new SimpleDateFormat("hh:mm aa").format(new Date()) + "</u>"));
        blnDateDialogShow=false;
    }
    void SaveJobCompleted(PromotionObject po)
    {
       /* if(blnRemovePromotionInParentUINCache)
        {
            parent.RemovePromotionFromUIAndCache(originalPromotionObject.GetId());
        }
        if(po!=null)
        {
            parent.Redraw(po);
            Toast.makeText(getContext(), "Promotion saved", Toast.LENGTH_SHORT).show();
        }*/
        parent.ClearPromotionEventUI();
        if(blnDismissDialog) {
            parent.PreparePanel(true);
            dismiss();
        }
        else
        {
            //keeping current dialog open, after auto save
            //make another copied, because original is referencing copied version during save
            copiedPromotionObject = new PromotionObject(originalPromotionObject);
            //reset flag
            blnDismissDialog = true;

            RestoreAfterSaveState();
        }
    }

    private void RemoveWholeRow(View v)
    {
        int rowIndex=-1;
        for(int i=1;i<llItems.getChildCount();i+=2)
        {
            RelativeLayout rl =(RelativeLayout)llItems.getChildAt(i);

            for(int j=0;j<rl.getChildCount();j++) {
                View checkV = rl.getChildAt(j);
                if (checkV.getId() == v.getId()) {
                    rowIndex = i - 1;
                    break;
                }
            }
        }

        int listIndex=(rowIndex+1)/2;
        copiedPromotionObject.ruleItems.remove(listIndex);
        llItems.removeViewAt(rowIndex);//remove the rule item row
        llItems.removeViewAt(rowIndex);//remove the rule item option row
        ReIndexFlowLayoutRow(rowIndex);
    }

    private void ReIndexFlowLayoutRow(int start)
    {
        for(int i=start;i<llItems.getChildCount();i+=2)
        {
            FlowLayout fl = (FlowLayout)llItems.getChildAt(i);
            for(int j=0;j<fl.getChildCount();j++)
            {
                PromotionRuleItemRow row =(PromotionRuleItemRow)fl.getChildAt(j);
                row.UpdateRoleIndex(row.GetRowIndex()-2);
            }
        }
    }
    @Override
    public void UpdateRuleItemGroup(HashMap<Long,Integer>hashMap,int listIndex) {
        UpdateRuleItemList(hashMap,listIndex);
    }

    @Override
    public void AddRuleItemGroup(HashMap<Long,Integer>hashMap) {
        //promotionObject.AddRuleItem(hashMap);
        AddRuleItemList(hashMap);
    }

    @Override
    public void RemoveRuleItem(PromotionRuleItemRow item,int listIndex) {
        //long tempId = item.GetItemId();
        int newUnitCount = copiedPromotionObject.ruleItems.get(listIndex).get(item.GetItemId());
        newUnitCount-=item.GetUnit();

        if(newUnitCount>0)
        {
            //insert new unit count
            copiedPromotionObject.ruleItems.get(listIndex).put(item.GetItemId(),newUnitCount);}
        else
        {
            //remove if the item has been removed completely
            copiedPromotionObject.ruleItems.get(listIndex).remove(item.GetItemId());
        }

        //remove the hash map and row from ui if no other item in it
        if(copiedPromotionObject.ruleItems.get(listIndex).size()==0)
        {
            copiedPromotionObject.ruleItems.remove(listIndex);
            llItems.removeViewAt(item.GetRowIndex());//remove the rule item row
            llItems.removeViewAt(item.GetRowIndex());//remove the rule item option row
            ReIndexFlowLayoutRow(item.GetRowIndex());
        }
        else {
            ((FlowLayout) llItems.getChildAt(listIndex)).removeView(item);
        }
    }


    protected class SavePromotionAsyncTask extends AsyncTask<PromotionObject,Void,PromotionObject>
    {


        @Override
        protected void onPostExecute(PromotionObject promotionObject) {
            super.onPostExecute(promotionObject);
            SaveJobCompleted(promotionObject);
        }

        @Override
        protected PromotionObject doInBackground(PromotionObject... promotionObject) {

            if(blnUpdateMode)
            {
                if(common.myPromotionManager.Update(promotionObject[0]))
                {

                    return promotionObject[0];//update in promotion manager(referencing)
                }
                else
                {
                    //revert it if update failed
                    common.myPromotionManager.ReloadPromotion(promotionObject[0].GetId());
                    promotionObject[0]= common.myPromotionManager.Get(promotionObject[0].GetId());
                    return promotionObject[0];
                }
            }
            else {
                if (common.myPromotionManager.Save(promotionObject[0]) > 0) {
                    return promotionObject[0];
                }
            }
            return null;
        }


    }
}
