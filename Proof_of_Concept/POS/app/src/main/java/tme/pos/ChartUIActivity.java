package tme.pos;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.Graph;
import tme.pos.CustomViewCtr.PageIndicatorIndexCtr;
import tme.pos.CustomViewCtr.PieChart;

/**
 * Created by kchoy on 5/4/2015.
 */
public class ChartUIActivity extends FragmentActivity implements DatePickerDialog.OnDateSetListener {
    TextView tvSelected;
    boolean blnLoading = false;
    boolean blnDateDialogShow = false;
    int START_YEAR=2015;
    LinearLayout selectedPanel;
    ChartManager cm = new ChartManager(this);
    PieChart pcBestSelling;
    @Override
    protected void onStart() {


        super.onStart();


    }
    @Override
    protected  void onResume()
    {
        Log.d("Chart activity Info", "on resume");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);


    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();

    }
    private void CreateClickEffectForRoundedborder(View v,final Enum.ChartType ct)
    {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View innerView) {
                if (SubjectTitleClick(innerView)) {
                    switch (ct) {
                        case yearly:
                            ShowYearlyChart();
                            break;
                        case monthly:
                            ShowMonthlyChart();
                            break;
                        case daily:
                            ShowDailyChart();
                            break;
                        default:
                            ShowReceiptLookup();

                    }
                }
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedIntanceState)
    {
        ((POS_Application)getApplication()).setCurrentActivity(this);
        super.onCreate(savedIntanceState);
        setContentView(R.layout.layout_chart_ui);


        //day chart
        common.control_events.CreateClickEffect(findViewById(R.id.tvShowDaily));

        //TextView tvBestSellingTitle = (TextView)findViewById(R.id.tvBestSellingTitle);
        //tvBestSellingTitle.setText(Html.fromHtml("Best selling item for this month:"));

        //TextView tvGratuityTitle = (TextView)findViewById(R.id.tvGratuityTitle);
        //tvGratuityTitle.setText(Html.fromHtml("Gratuity data for this month:"));




        //yearly
        common.control_events.CreateClickEffect(findViewById(R.id.tvShowYearly));

        TextView tvYearly =(TextView) this.findViewById(R.id.tvYearly);
        tvSelected = tvYearly;
        SetTextSizeAndTypeFace(tvYearly, 26);//common.text_and_length_settings.CHART_OPTIONS_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedborder(tvYearly, Enum.ChartType.yearly);

        BindReceiptChartControls();


        //month
        common.control_events.CreateClickEffect(findViewById(R.id.tvShowMonthly));
        TextView tvMonthly =(TextView) this.findViewById(R.id.tvMonthly);
        SetTextSizeAndTypeFace(tvMonthly,26);// common.text_and_length_settings.CHART_OPTIONS_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedborder(tvMonthly, Enum.ChartType.monthly);


        TextView tvDaily =(TextView) this.findViewById(R.id.tvDaily);
        SetTextSizeAndTypeFace(tvDaily,26);// common.text_and_length_settings.CHART_OPTIONS_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedborder(tvDaily, Enum.ChartType.daily);


        TextView tvReceiptLookup =(TextView) this.findViewById(R.id.tvReceiptLookup);
        SetTextSizeAndTypeFace(tvReceiptLookup, 26);// common.text_and_length_settings.CHART_OPTIONS_TITLE_TEXT_SIZE);
        CreateClickEffectForRoundedborder(tvReceiptLookup, Enum.ChartType.receipt);

        //repositioning controls
        findViewById(R.id.imgCancelStamp).setY(common.Utility.DP2Pixel(120, this));
        findViewById(R.id.imgCancelStamp).setX(common.Utility.DP2Pixel(830, this));
        findViewById(R.id.llReceiptOptions).setX(common.Utility.DP2Pixel(550,this));

        //pcBestSelling=(PieChart)this.findViewById(R.id.pcBestSelling);
        //pcBestSelling.setWillNotDraw(false);

        //simulate selected subject
        // Obtain MotionEvent object
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 0.0f;
        float y = 0.0f;
// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

// Dispatch touch event to view
        tvYearly.dispatchTouchEvent(motionEvent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ShowYearlyChart();
                common.control_events.HideSoftKeyboard(findViewById(R.id.npStartYear));
            }
        }, 500);


    }
    private boolean SubjectTitleClick(View v)
    {
        if (blnLoading) return false;
        if(v==tvSelected)return false;
        v.setBackground(getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
        //v.setBackground(getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));
        //v.setBackground(getResources().getDrawable(R.drawable.draw_two_round_rect));
        if (tvSelected != null) {
            //tvSelected.setTextColor(getResources().getColor(R.color.green));
            tvSelected.setTextColor(getResources().getColor(R.color.divider_grey));
            tvSelected.setBackground(null);
        }
        tvSelected = (TextView) v;
        //tvSelected.setTextColor(getResources().getColor(R.color.black));
        //tvSelected.setTextColor(getResources().getColor(R.color.lost_shine_green));
        tvSelected.setTextColor(getResources().getColor(R.color.green));
        return true;
    }
    private void SetTextSizeAndTypeFace(TextView tv,float textSize) {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
    }
    private void ShowPanelAnimation(final int ShowPanelId)
    {
        final float flPanelWidth = 1250;
        TranslateAnimation movementSlideOut = new TranslateAnimation(0.0f,common.Utility.DP2Pixel(flPanelWidth, this),  0.0f, 0.0f);//move left
        movementSlideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                selectedPanel.setVisibility(View.GONE);
                selectedPanel = (LinearLayout) findViewById(ShowPanelId);
                selectedPanel.setVisibility(View.VISIBLE);

                TranslateAnimation movementSlideIn = new TranslateAnimation(-flPanelWidth, 0.0f, 0.0f, 0.0f);//move right


                movementSlideIn.setDuration(200);
                movementSlideIn.setFillAfter(true);
                selectedPanel.startAnimation(movementSlideIn);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movementSlideOut.setDuration(200);
        selectedPanel.startAnimation(movementSlideOut);
    }
    private void ShowPanel(int ShowPanelId)
    {
        if(selectedPanel!=null) {

            ShowPanelAnimation(ShowPanelId);
            //selectedPanel.setVisibility(View.GONE);
        }
        else {
            selectedPanel = (LinearLayout) findViewById(ShowPanelId);
            selectedPanel.setVisibility(View.VISIBLE);
        }
    }
    private String[] ConstructYearList(int Start)
    {
        int CurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        int duration = (CurrentYear-Start)+1;
        String[] options = new String[duration];
        for(int i=0;i<duration;i++)
        {
            options[i] = (Start+i)+"";
        }
        return options;
    }
    private void ShowYearlyChart()
    {
        //if(selectedPanel!=null)selectedPanel.setVisibility(View.GONE);
        //selectedPanel = (LinearLayout)findViewById(R.id.llYearlyPanel);
        ShowPanel(R.id.llYearlyPanel);
        final int CurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        final NumberPicker npStartYear = (NumberPicker)findViewById(R.id.npStartYear);
        npStartYear.setMinValue(START_YEAR);
        npStartYear.setMaxValue(CurrentYear);
        //npStartYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        npStartYear.setDisplayedValues(ConstructYearList(START_YEAR));
        npStartYear.setWrapSelectorWheel(false);


        final TextView tvYearlyResult = (TextView)findViewById(R.id.tvYearlyResult);
        tvYearlyResult.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.LABEL_TEXT_SIZE);
        TextView tvShowYearly=(TextView)findViewById(R.id.tvShowYearly);
        tvShowYearly.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        findViewById(R.id.tvShowYearly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnLoading) return;
                blnLoading = true;
                tvYearlyResult.setText("Showing result begin year " + CurrentYear);

                ShowYearlyChartLoadingStage();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ConstructYearlyChart(npStartYear.getValue());
                    }
                });

            }
        });

    }
    private void ShowYearlyChartLoadingStage()
    {
        findViewById(R.id.imgYearlyBarIcon).setVisibility(View.GONE);
        findViewById(R.id.llYearlyBars).setVisibility(View.VISIBLE);
        findViewById(R.id.llYearlyLabels).setVisibility(View.VISIBLE);
        ShowLoadingStage((LinearLayout) findViewById(R.id.llYearlyLabels), (LinearLayout) findViewById(R.id.llYearlyBars), 1210, 420, false);
        /*
        float flWidth = 1210;
        float flHeight = 420;
        //remove any bar and label
        LinearLayout llYearlyBars = (LinearLayout)findViewById(R.id.llYearlyBars);
        llYearlyBars.removeAllViews();
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(flWidth,this), common.Utility.DP2Pixel(flHeight,this));
        llYearlyBars.setLayoutParams(lllp);

        LinearLayout llYearlyLabels = (LinearLayout)findViewById(R.id.llYearlyLabels);
        llYearlyLabels.removeAllViews();

        //show loading
        ProgressBar pb = new ProgressBar(this);
        TextView tvLoading = new TextView(this);
        tvLoading.setText("Loading");
        tvLoading.setY(common.Utility.DP2Pixel((flHeight / 2) + 20, this));
        tvLoading.setX(common.Utility.DP2Pixel((flWidth / 2) - 50, this));
        //tvLoading.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        llYearlyBars.addView(pb);
        pb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams)pb.getLayoutParams()).gravity = Gravity.CENTER;
        pb.setX(common.Utility.DP2Pixel(flWidth / 2, this));
        llYearlyBars.addView(tvLoading);
*/
    }
    private void ShowMonthlyChartLoadingStage()
    {
        /*findViewById(R.id.llMonthlyGraph1).setVisibility(View.VISIBLE);
        ((LinearLayout)findViewById(R.id.llMonthlyBars2)).removeAllViews();
        findViewById(R.id.llMonthlyGraph2).setVisibility(View.VISIBLE);
        findViewById(R.id.imgMonthlyBarIcon).setVisibility(View.GONE);
        ShowLoadingStage((LinearLayout) findViewById(R.id.llMonthlyLabels1), (LinearLayout) findViewById(R.id.llMonthlyBars1), 1210, 300,false);*/
    }
    private void ShowLoadingStage(LinearLayout labelPanel,LinearLayout barPanel,float flWidth,float flHeight,boolean blnReceipt)
    {
        //float flWidth = 1210;
        //float flHeight = 420;
        //remove any bar and label

        //show loading
        ProgressBar pb = new ProgressBar(this);
        TextView tvLoading = new TextView(this);
        tvLoading.setText("Loading");
        //tvLoading.setBackgroundColor(Color.RED);

        //clear the panel for progress bar icon
        barPanel.removeAllViews();

        if(blnReceipt)
        {
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            barPanel.setLayoutParams(trlp);
            trlp.leftMargin=common.Utility.DP2Pixel(250,this);
            trlp.topMargin=common.Utility.DP2Pixel(50,this);
            tvLoading.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        else {
            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(flWidth, this), common.Utility.DP2Pixel(flHeight, this));
            barPanel.setLayoutParams(lllp);

            //display the icon in middle
            tvLoading.setY(common.Utility.DP2Pixel((flHeight / 2) + 20, this));
            tvLoading.setX(common.Utility.DP2Pixel((flWidth / 2) - 50, this));

            pb.setX(common.Utility.DP2Pixel(flWidth / 2, this));
        }







        barPanel.addView(pb);//add progress bar
        pb.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams)pb.getLayoutParams()).gravity = Gravity.CENTER;

        barPanel.addView(tvLoading);//add loading sentence

        if(!blnReceipt) {//receipt panel doesn't have label panel
            labelPanel.removeAllViews();
        }
    }
    private TextView CreateBarLabel(String strValue,float flWidth,float flMarginLeft)
    {
        //float flWidth=70;
        //float flMarginLeft=50;
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(flWidth, this), LinearLayout.LayoutParams.WRAP_CONTENT);
        lllp.leftMargin=common.Utility.DP2Pixel(flMarginLeft,this);
        tv.setLayoutParams(lllp);
        tv.setText(strValue);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
    }
    private TextView CreateYearlyBarLabel(String strYear)
    {

        float flWidth=70;
        float flMarginLeft=50;

        return CreateBarLabel(strYear,flWidth,flMarginLeft);
        /*
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(flWidth, this), LinearLayout.LayoutParams.WRAP_CONTENT);
        lllp.leftMargin=common.Utility.DP2Pixel(flMarginLeft,this);
        tv.setLayoutParams(lllp);
        tv.setText(strYear);
        tv.setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        return tv;
        */
    }
    private TextView CreateMonthlyBarLabel(String strValue)
    {
        float flWidth=120;
        float flMarginLeft=50;
        return CreateBarLabel(strValue,flWidth,flMarginLeft);
    }
    private RelativeLayout CreateBar(float value,boolean blnLast,float flBarHeight,
                                     float flRelativeWidth,float flRLMarginLeft)
    {
        //float flRelativeWidth=70;
        //float flRLMarginLeft=50;
        float flRLMarginRight=10;
        RelativeLayout rl = new RelativeLayout(this);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(flRelativeWidth,this), LinearLayout.LayoutParams.MATCH_PARENT);
        lllp.leftMargin=common.Utility.DP2Pixel(flRLMarginLeft,this);
        rl.setLayoutParams(lllp);
        if(blnLast)lllp.rightMargin=common.Utility.DP2Pixel(flRLMarginRight,this);

        LinearLayout ll = new LinearLayout(this);
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll.setOrientation(LinearLayout.VERTICAL);
        rl.addView(ll,rllp);

        //value label
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams tvlp =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvlp.gravity = Gravity.CENTER;
        tv.setLayoutParams(tvlp);
        //tv.setGravity(Gravity.CENTER);
        //((LinearLayout)tv.getLayoutParams()).setGravity(Gravity.CENTER);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(value)));
        ll.addView(tv);

        //bar object
        LinearLayout llBar = new LinearLayout(this);
        llBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, common.Utility.DP2Pixel(flBarHeight, this)));
        llBar.setBackgroundColor(getResources().getColor(R.color.wallet_holo_blue_light));
        ll.addView(llBar);

        return rl;
    }
    private RelativeLayout CreateYearlyBar(float value,boolean blnLast,float flBarHeight)
    {
        float flRelativeWidth=70;
        float flRLMarginLeft=50;
        return CreateBar(value, blnLast, flBarHeight, flRelativeWidth, flRLMarginLeft);

    }
    private void ConstructYearlyChart(int intStart)
    {
        float flBarPanelHeight = 400;

        ArrayList<Pair<Integer,Float>> data= cm.GetYearlyData(intStart, intStart + 9);

        //stop the loading sentence
        LinearLayout llYearlyBars = (LinearLayout)findViewById(R.id.llYearlyBars);
        llYearlyBars.removeAllViews();
        llYearlyBars.getLayoutParams().height = common.Utility.DP2Pixel(flBarPanelHeight,this);

        //get lowest and highest value
        float flHi=0,flLow=-1;
        for(int i=0;i<data.size();i++)
        {
            if(data.get(i).second>flHi)flHi = data.get(i).second;
            if(data.get(i).second<flLow)flLow = data.get(i).second;
        }
        //year label
        for(int i=0;i<data.size();i++)
        {
            final RelativeLayout rl=CreateYearlyBar(data.get(i).second, (i == data.size() - 1) ? true : false, CalculateBarHeight(300, data.get(i).second, flHi));
            rl.setVisibility(View.INVISIBLE);
            llYearlyBars.addView(rl);
            ((LinearLayout) findViewById(R.id.llYearlyLabels)).addView(CreateYearlyBarLabel(data.get(i).first + ""));
            final TranslateAnimation movementRise = new TranslateAnimation(0.0f,0.0f,common.Utility.DP2Pixel(flBarPanelHeight, this),  0.0f);//rise
            movementRise.setDuration(1000);
            movementRise.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    rl.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    rl.startAnimation(movementRise);
                }
            },100*i);

        }

        blnLoading = false;
    }
    private float CalculateBarHeight(float flMaxHeight,float value,float maxValue)
    {
        if(maxValue==0)return 0;
        float ratio = (value/maxValue);
        return ratio*flMaxHeight;
    }

    private void ShowMonthlyChart()
    {
        ShowPanel(R.id.llMonthlyPanel);

        int CurrentYear = Calendar.getInstance().get(Calendar.YEAR);
        final NumberPicker npMonthly = (NumberPicker)findViewById(R.id.npMonthly);
        npMonthly.setMinValue(START_YEAR);
        npMonthly.setMaxValue(CurrentYear);
        npMonthly.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        final TextView tvMonthlyResult = (TextView)findViewById(R.id.tvMonthlyResult);
        tvMonthlyResult.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.LABEL_TEXT_SIZE);
        TextView tvShowMonthly=(TextView)findViewById(R.id.tvShowMonthly);
        tvShowMonthly.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        findViewById(R.id.tvShowMonthly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnLoading) return;
                blnLoading = true;
                tvMonthlyResult.setText("Showing result for year "+npMonthly.getValue());
                ShowMonthlyChartLoadingStage();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ConstructMonthlyChart(npMonthly.getValue());
                    }
                });

            }
        });

    }
    private void ConstructMonthlyChart(int year)
    {
        Graph monthlyGraph = (Graph)findViewById(R.id.monthlyGraph);
        ArrayList<Pair<Integer,Float>> data= cm.GetMonthlyData(year);
        monthlyGraph.Draw(data);
        blnLoading = false;
    }


    private void ShowDailyChart()
    {
        ShowPanel(R.id.llDailyPanel);
        final NumberPicker npDailyMonth = (NumberPicker)findViewById(R.id.npDailyMonth);
        npDailyMonth.setDisplayedValues(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"});
        npDailyMonth.setMinValue(0);
        npDailyMonth.setMaxValue(11);
        npDailyMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final NumberPicker npDailyYear = (NumberPicker)findViewById(R.id.npDailyYear);

        npDailyYear.setMinValue(START_YEAR);
        npDailyYear.setMaxValue(Calendar.getInstance().get(Calendar.YEAR));
        npDailyYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);


        TextView tvShowDaily=(TextView)findViewById(R.id.tvShowDaily);
        tvShowDaily.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        findViewById(R.id.tvShowDaily).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnLoading) return;
                blnLoading = true;

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        ConstructDailyChart(npDailyYear.getValue(), ConvertMonthStringToInt(npDailyMonth.getDisplayedValues()[npDailyMonth.getValue()].toLowerCase()));

                    }
                });

            }
        });


    }
    private int ConvertMonthStringToInt(String strMonth)
    {
        if(strMonth.equalsIgnoreCase("jan"))
        {
            return Calendar.JANUARY;
        }
        else if(strMonth.equalsIgnoreCase("feb"))
        {
            return Calendar.FEBRUARY;
        }
        else if(strMonth.equalsIgnoreCase("mar"))
        {
            return Calendar.MARCH;
        }
        else if(strMonth.equalsIgnoreCase("apr"))
        {
            return Calendar.APRIL;
        }
        else if(strMonth.equalsIgnoreCase("may"))
        {
            return Calendar.MAY;
        }
        else if(strMonth.equalsIgnoreCase("jun"))
        {
            return Calendar.JUNE;
        }
        else if(strMonth.equalsIgnoreCase("jul"))
        {
            return Calendar.JULY;
        }
        else if(strMonth.equalsIgnoreCase("aug"))
        {
            return Calendar.AUGUST;
        }
        else if(strMonth.equalsIgnoreCase("sept"))
        {
            return Calendar.SEPTEMBER;
        }
        else if(strMonth.equalsIgnoreCase("oct"))
        {
            return Calendar.OCTOBER;
        }
        else if(strMonth.equalsIgnoreCase("nov"))
        {
            return Calendar.NOVEMBER;
        }
        else
        {
            return Calendar.DECEMBER;
        }
    }
    private String ConvertCalendarMonth2String(int calendarMonth)
    {
        switch (calendarMonth)
        {
            case Calendar.JANUARY:
                return "Jan";
            case Calendar.FEBRUARY:
                return "Feb";
            case Calendar.MARCH:
                return "Mar";
            case Calendar.APRIL:
                return "Apr";
            case Calendar.MAY:
                return "May";
            case Calendar.JUNE:
                return "June";
            case Calendar.JULY:
                return "July";
            case Calendar.AUGUST:
                return "Aug";
            case Calendar.SEPTEMBER:
                return "Sept";
            case Calendar.OCTOBER:
                return "Oct";
            case Calendar.NOVEMBER:
                return "Nov";
            default:
                return "Dec";
        }
    }
    private void ConstructDailyChart(int year,int calendarMonth)
    {

        final DailyChartModel data =cm.GetDailySalesData(year, calendarMonth);
        ViewPager pager = (ViewPager) findViewById(R.id.DailyChartPager);
        final LinearLayout llPageIndicator = (LinearLayout)findViewById(R.id.llPageIndicator);
        FragmentStatePagerAdapter chartPagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch(position)
                {
                    case 0:
                        DailySalesChartFragment dailySales = new DailySalesChartFragment();
                        dailySales.Draw(data.DailySalesData);
                        return dailySales;

                    case 1:
                        BestSellingItemPieChartFragment bestSelling =new BestSellingItemPieChartFragment();
                        bestSelling.Draw(data.BestSellingData);
                        return bestSelling;
                    case 2:
                        break;
                    default:
                        break;
                }

                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
        pager.setAdapter(chartPagerAdapter);
        ViewPager.SimpleOnPageChangeListener listener = new ViewPager.SimpleOnPageChangeListener() {
            int intSelectedPageIndex = -1;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                intSelectedPageIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    //LinearLayout ll = (LinearLayout)mua.findViewById(R.id.llPageIndicator);
                    for (int i = 0; i < llPageIndicator.getChildCount(); i++) {
                        if (llPageIndicator.getChildAt(i) instanceof PageIndicatorIndexCtr) {
                            PageIndicatorIndexCtr child = (PageIndicatorIndexCtr) llPageIndicator.getChildAt(i);
                            if (Integer.parseInt(child.getTag().toString()) == intSelectedPageIndex) {
                                child.FillCircle();
                            } else {
                                child.UnfillCircle();
                            }
                        }
                    }

                }
            }
        };
        pager.setOnPageChangeListener(listener);
        listener.onPageSelected(0);
        pager.setPageTransformer(true, new ZoomOutPageTransformer());
        CreateDailyChartPageIndicator(2,pager);



        TextView tvDailySearchCriteria = (TextView)findViewById(R.id.tvDailySearchCriteria);
        tvDailySearchCriteria.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.LABEL_TEXT_SIZE);
        //tvDailySearchCriteria.setTypeface(null, Typeface.BOLD);
        tvDailySearchCriteria.setText("Showing result for "+ConvertCalendarMonth2String(calendarMonth)+" "+year);


        //get max value
        float maxValue=0;
        float value=0;
        for(int i=0;i<data.DailySalesData.size();i++)
        {
            value = data.DailySalesData.get(i).second;
            if(maxValue<value)maxValue=value;
        }


        findViewById(R.id.imgDailyBarIcon).setVisibility(View.GONE);
        blnLoading = false;
    }
    private void CreateDailyChartPageIndicator(int count,ViewPager pager) {


        int height=40;
        int width=40;
        final LinearLayout llPageIndicator = (LinearLayout) findViewById(R.id.llPageIndicator);
        llPageIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {

            PageIndicatorIndexCtr indicator = new PageIndicatorIndexCtr(this, i, pager);

            indicator.setLayoutParams(new LinearLayout.LayoutParams(common.Utility.DP2Pixel(width, this), common.Utility.DP2Pixel(height, this)));
            indicator.setTag(i);
            llPageIndicator.addView(indicator);
            if (i > 0) {
                indicator.UnfillCircle();
            } else {
                indicator.FillCircle();
            }


        }
    }



    private void ShowReceiptLookup()
    {
        ShowPanel(R.id.llReceiptPanel);

    }
    private void UpdateTableList(String strReceiptNum,boolean isActive)
    {
        TableLayout tbl = (TableLayout)findViewById(R.id.tblReceiptList);
        for(int i=0;i<tbl.getChildCount();i++)
        {
            TableRow tr = (TableRow)tbl.getChildAt(i);
            TextView tvReceiptNum=(TextView)tr.getChildAt(1);
            String strCompare = tvReceiptNum.getText().toString().substring(1,tvReceiptNum.getText().length()-1);
            if(strReceiptNum.equalsIgnoreCase(strCompare))
            {
                findViewById(R.id.imgCancelReceipt).setVisibility(((isActive) ? View.VISIBLE : View.GONE));
                findViewById(R.id.imgActiveReceipt).setVisibility(((isActive) ? View.GONE : View.VISIBLE));
                findViewById(R.id.imgCancelStamp).setVisibility(((isActive) ? View.GONE : View.VISIBLE));

                tvReceiptNum.setText(Html.fromHtml("<font color='" + ((isActive)?"#50C108":"#ff0000") + "'><u>" + tvReceiptNum.getText().toString() + "</u></font> "));


                return;
            }
        }
    }
    private void BindReceiptChartControls()
    {
        final TextView tvSearchReceiptCriteria = (TextView)findViewById(R.id.tvSearchReceiptCriteria);
        tvSearchReceiptCriteria.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.LABEL_TEXT_SIZE);

        final Context context = this;

        //print icon
        common.control_events.CreateClickEffect(findViewById(R.id.imgPrintReceipt));
        TableLayout tblReceiptList = (TableLayout)findViewById(R.id.tblReceiptList);
        tblReceiptList.removeAllViews();
        tblReceiptList.addView(CreateNoRecordTableRow());
        common.control_events.CreateClickEffect(findViewById(R.id.tvSelectedReceiptDate));
        common.control_events.CreateClickEffect(findViewById(R.id.tvShowReceipt));
        common.control_events.CreateClickEffect(findViewById(R.id.imgSearchReceipt));



        //cancel receipt
        common.control_events.CreateClickEffect(findViewById(R.id.imgCancelReceipt));
        findViewById(R.id.imgCancelReceipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Receipt r = (Receipt) findViewById(R.id.llReceiptGUIPanel).getTag();
                r = cm.GetReceipt(r.receiptNumber);
                ///check whether is still partial paid on this paid receipt
                ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(r.myCart.tableId);
               /* for(Receipt rCompare:receipts) {
                    if(rCompare.receiptNumber.compareToIgnoreCase(r.receiptNumber)==0)
                    {
                        r = rCompare;
                        break;
                    }
                    *//*if(rCompare.receiptNumber==r.receiptNumber) {
                        common.Utility.ShowMessage("Cancel","You cannot cancel this receipt unless all its correlated part have been paid.",context,R.drawable.no_access);
                        return;
                    }*//*
                }*/

                //allow cancel if pass the check
                Enum.DBOperationResult result = cm.UpdateReceiptActiveStatus(false, r, r.receiptDateTime.getTimeInMillis() + "",receipts);
                if(result== Enum.DBOperationResult.Success){
                    UpdateTableList(r.receiptNumber,false);
                    r.blnActive=false;

                    //do the by date search again if successful in order to remove the affected receipts
                    if(receipts.size()>1) {
                        //Enum.DBOperationResult saveResult = common.receiptManager.SaveOrdersIntoDB(receipts,true);
                        SearchReceiptByDate();
                    }
                }
                //common.Utility.ShowMessage("Cancel", result + "", context, R.drawable.message);
            }
        });




        //Active receipt
        common.control_events.CreateClickEffect(findViewById(R.id.imgActiveReceipt));
        findViewById(R.id.imgActiveReceipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Receipt r = (Receipt) findViewById(R.id.llReceiptGUIPanel).getTag();
                Enum.DBOperationResult result = cm.UpdateReceiptActiveStatus(true, r, r.receiptDateTime.getTimeInMillis() + "",common.myCartManager.GetReceipts(r.myCart.tableId));
                if (result == Enum.DBOperationResult.Success) {
                    UpdateTableList(r.receiptNumber, true);
                    r.blnActive = true;
                }
                //common.Utility.ShowMessage("Active",result+"",context,R.drawable.message);
            }
        });

        findViewById(R.id.tvSelectedReceiptDate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set flag preventing double dialog box for date picker on screen
                if (blnLoading) return;
                blnLoading = true;
                ShowDatePickerDialog();
            }
        });

        TextView tvShowReceipt=(TextView)findViewById(R.id.tvShowReceipt);
        tvShowReceipt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE);
        findViewById(R.id.tvShowReceipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnLoading) return;
                blnLoading = true;

                tvSearchReceiptCriteria.setText("Showing result for " + ((TextView) findViewById(R.id.tvSelectedReceiptDate)).getText());
                ShowSearchReceiptListLoadingStage();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        SearchReceiptByDate();
                    }
                });


            }
        });
        findViewById(R.id.imgSearchReceipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blnLoading) return;
                final String strKeyword=(((EditText) findViewById(R.id.txtSearchReceipt)).getText() + "").trim();
                if(strKeyword.length()<0)
                {
                    common.Utility.ShowMessage("Lookup","Please provide at least one characters for searching.",context,R.drawable.no_access);
                    return;
                }

                blnLoading = true;
                tvSearchReceiptCriteria.setText("Showing result for \"" + ((EditText) findViewById(R.id.txtSearchReceipt)).getText() + "\"");
                ShowSearchReceiptListLoadingStage();
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        SearchReceiptByKeyword(strKeyword);
                    }
                });
            }
        });
        SetCurrentDateForDateInput();
    }
    private void SearchReceiptByKeyword(String strKeyword)
    {

        ArrayList<Pair<String,String[]>> data =cm.SearchReceipt(strKeyword);
        ConstructReceiptListTable(data);
        blnLoading=false;
    }
    private void ShowSearchReceiptListLoadingStage()
    {
        TableLayout tblReceiptList = ((TableLayout) findViewById(R.id.tblReceiptList));
        tblReceiptList.removeAllViews();
        TableRow tr = new TableRow(this);
        //tr.setBackgroundColor(Color.BLUE);
        TableLayout.LayoutParams tllp  = new TableLayout.LayoutParams(common.Utility.DP2Pixel(590,this),common.Utility.DP2Pixel(1210,this));
        tblReceiptList.addView(tr, tllp);
        LinearLayout llProgressBar = new LinearLayout(this);
        llProgressBar.setOrientation(LinearLayout.VERTICAL);
        //llProgressBar.setBackgroundColor(Color.GREEN);
        //llProgressBar.setGravity(Gravity.CENTER_HORIZONTAL);

        tr.addView(llProgressBar);
        ShowLoadingStage(null, llProgressBar, 590, 1210, true);
    }
    private void SearchReceiptByDate()
    {

        long value = Calendar.getInstance().getTimeInMillis();

        if(findViewById(R.id.tvSelectedReceiptDate).getTag()!=null)
        {
            value =Long.parseLong(findViewById(R.id.tvSelectedReceiptDate).getTag().toString());
        }

        Calendar cal = new GregorianCalendar();//Calendar.getInstance();
        cal.setTimeInMillis(value);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        value = cal.getTimeInMillis();

        //String strTemp =common.Utility.ReturnDateString(cal,false);
        cal.add(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        /*cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);*/

        //strTemp =common.Utility.ReturnDateString(cal,false);
        ArrayList<Pair<String,String[]>>data =cm.GetReceipt(value, cal.getTimeInMillis());
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
        findViewById(R.id.llReceiptOptions).setVisibility(View.VISIBLE);

        findViewById(R.id.imgCancelStamp).setVisibility(((!isActive)?View.VISIBLE:View.GONE));
        findViewById(R.id.imgActiveReceipt).setVisibility(((isActive)?View.GONE:View.VISIBLE));
        findViewById(R.id.imgCancelReceipt).setVisibility(((!isActive)?View.GONE:View.VISIBLE));

    }
    private void ResetReceiptPanel()
    {
        //hide receipt gui panel
        findViewById(R.id.llReceiptGUIPanel).setVisibility(View.GONE);
        //clear the result list
        TableLayout tblReceiptList = (TableLayout)findViewById(R.id.tblReceiptList);
        tblReceiptList.removeAllViews();
        //show receipt icon
        findViewById(R.id.ReceiptIcon).setVisibility(View.VISIBLE);

        //receipt cancel status stamp
        findViewById(R.id.imgCancelStamp).setVisibility(View.INVISIBLE);

        //hide receipt options and cancel stamp
        findViewById(R.id.llReceiptOptions).setVisibility(View.GONE);
    }
    private void ReceiptRowClicked(TableLayout tbl,String strReceiptNum,TableRow tr)
    {
        if(blnLoading)return;
        blnLoading = true;
        for(int i =0;i<tbl.getChildCount();i++)
        {

            tbl.getChildAt(i).setBackgroundColor(Color.WHITE);
        }
        tr.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
        DisplayReceipt(strReceiptNum);
        blnLoading = false;

    }
    private void ConstructReceiptListTable(ArrayList<Pair<String,String[]>> data)
    {

        ResetReceiptPanel();

        final TableLayout tblReceiptList = (TableLayout)findViewById(R.id.tblReceiptList);


        int index=1;
        //int count=3;

        //while(count-->-1) {
            for (Pair<String, String[]> p : data) {
                final TableRow tr = new TableRow(this);
                tr.setPadding(0,10,0,(int)10*-1);
                TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tllp.setMargins(0,0,0,10);
                TextView tvIndex = new TextView(this);
                tvIndex.setText(index++ + ".");
                tvIndex.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
                tvIndex.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
                TableRow.LayoutParams trlp1 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                trlp1.gravity = Gravity.LEFT;

                //Receipt number
                final String strReceiptNum = p.first;
                TextView tvReceiptNum = new TextView(this);
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
                        ReceiptRowClicked(tblReceiptList,strReceiptNum,tr);
                      /*  for(int i =0;i<tblReceiptList.getChildCount();i++)
                        {
                            TableRow r = (TableRow)tblReceiptList.getChildAt(i);
                            tblReceiptList.getChildAt(i).setBackgroundColor(Color.WHITE);
                        }
                        tr.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                        DisplayReceipt(strReceiptNum);*/
                    }
                });

                TableRow.LayoutParams trlp2 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                trlp2.bottomMargin = common.Utility.DP2Pixel(20, this);

                //timestamp
                TextView tvTime = new TextView(this);
                tvTime.setText(Html.fromHtml("  " + p.second[1].substring(p.second[1].indexOf(" "))));
                tvTime.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
                tvTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);



                //total
                TextView tvTotal = new TextView(this);
                tvTotal.setText(Html.fromHtml("  " + common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(p.second[2]))));
                tvTotal.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
                tvTotal.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.INVENTORY_POPUP_WINDOW_TEXT_SIZE);
                TableRow.LayoutParams trlp3 = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                trlp3.leftMargin = common.Utility.DP2Pixel(20, this);

                tr.addView(tvIndex, trlp1);
                tr.addView(tvReceiptNum, trlp2);
                tr.addView(tvTime);
                tr.addView(tvTotal,trlp3);
                tr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ReceiptRowClicked(tblReceiptList,strReceiptNum,tr);

                    }
                });
                //TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //tllp.bottomMargin=3;
                tblReceiptList.addView(tr,tllp);
            }
        //}
        if(tblReceiptList.getChildCount()==0)
        {
            tblReceiptList.addView(CreateNoRecordTableRow());
        }
    }
    public TextView CreateItemNameTextView(int Unit,String strName,Typeface tf,int intMaxChar)
    {
        //create table column for item name
        TextView txtName = new TextView(this);
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
        TextView txtUnit = new TextView(this);
        txtUnit.setTypeface(tf);
        txtUnit.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, common.text_and_length_settings.INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT));
        txtUnit.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price));
        //txtUnit.setText("x" + Integer.toString(intCount));
        txtUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_RECEIPT_ITEM_MENU_TEXT_SIZE);
        //txtUnit.setBackgroundColor(Color.CYAN);
        txtUnit.setGravity(Gravity.RIGHT);
        txtUnit.setPadding(common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW,common.text_and_length_settings.LEFT_RIGHT_PADDING_RECEIPT_ROW,
                common.text_and_length_settings.TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtUnit;
    }
    public TextView CreateTotalPriceTextView(int unit,BigDecimal price,Typeface tf)
    {
        //create table column for price
        TextView txtPrice = new TextView(this);
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
        TextView txtName = new TextView(this);
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
        TextView txtPrice = new TextView(this);
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
        TextView txtPrice = new TextView(this);
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
    private void UpdateReceiptSummary(Receipt receipt)
    {
        //Receipt receipt = common.myCartManager.GetReceipt(strReceiptId,intMiniReceiptIndex);

        //table label
        ((TextView)findViewById(R.id.tvTableLabel)).setText(receipt.tableNumber.length()==0?"":receipt.tableNumber);

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

        //promotion
        TextView tvPromotionDiscountValue = (TextView)findViewById(R.id.tvPromotionDiscountValue);
        tvPromotionDiscountValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.GetPromotionByCashAmount()));

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
        Receipt receipt = cm.GetReceipt(strReceiptNum);

        if(receipt==null)
        {
            common.Utility.ShowMessage("Load Receipt","Couldn't load selected receipt, data not available.",this, R.drawable.exclaimation);
        }
        else
        {
            //save selected receipt object
            findViewById(R.id.llReceiptGUIPanel).setTag(receipt);

            ShowReceiptPanel(receipt.blnActive);
            UpdateReceiptSummary(receipt);


            //for(StoreItem si:receipt.myCart.GetItems())
            TextView tvItemName;
            TextView tvUnit;
            TextView tvPrice;
            for(CartDisplayItem cdi:receipt.myCart.GetDisplayCartItemList())
            {
                //create table column for item name
                if(cdi.cit== Enum.CartItemType.PromotionAwarded)
                {
                    String strUnit="";
                    int intShareBy =cdi.pa.ShareByHowManyReceipts();
                    if(intShareBy==1)
                    {
                        //strUnit = (promotionAwarded.unit>1)?promotionAwarded.unit+"x ":"";
                        strUnit = cdi.pa.unit+"x ";
                    }
                    else
                    {
                        //strUnit="("+promotionAwarded.unit+"/"+intShareBy+")x ";
                        strUnit="("+cdi.pa.unit+"/"+intShareBy+")x ";
                    }

                    tvItemName = CreateItemNameTextView(1,
                            cdi.pa.promotionObject.GetTitle(),
                            common.text_and_length_settings.TYPE_FACE_ABEL_FONT,
                            common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH);
                    tvItemName.setText(strUnit+cdi.pa.promotionObject.GetTitle());
                    tvItemName.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);

                    tvItemName.setTextColor(getResources().getColor(R.color.dark_grey));
                    //create table column for unit
                    tvUnit = CreateUnitPriceTextView(new BigDecimal(0), common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
                    if(cdi.pa.promotionObject.GetDiscountType()== Enum.DiscountType.cash) {
                        tvUnit.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(cdi.pa.GetItemsTotalPriceForThisComboBeforeDiscount()));
                    }
                    else
                    {
                        tvUnit.setText(String.format("%.2f",cdi.pa.promotionObject.GetDiscountValue()*100f)+"%");
                    }
                    tvUnit.setTextColor(getResources().getColor(R.color.dark_grey));
                    //create table column for price
                    BigDecimal bdDiscount = cdi.pa.GetTotalDiscountAwarded((receipt.myCart.receiptIndex==cdi.pa.GetSharedReceiptIndex().get(0))?true:false,receipt.myCart.receiptIndex);
                    tvPrice = CreateTotalPriceTextView(1, bdDiscount, common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
                    //BigDecimal bdDiscount = cdi.pa.GetTotalDiscountAwarded((receipt.myCart.receiptIndex==cdi.pa.GetSharedReceiptIndex().get(0))?true:false,receipt.myCart.receiptIndex);
                    tvPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(bdDiscount));
                    tvPrice.setTextColor(getResources().getColor(R.color.dark_grey));

                    TableRow trItem = new TableRow(this);
                    trItem.addView(tvItemName);
                    trItem.addView(tvUnit);
                    trItem.addView(tvPrice);
                    tblReceipt.addView(trItem);
                }
                else
                {
                    tvItemName = CreateItemNameTextView(cdi.si.UnitOrder,
                            cdi.si.item.getName(),
                            common.text_and_length_settings.TYPE_FACE_ABEL_FONT,
                            common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH);

                    //create table column for unit price
                    tvUnit = CreateUnitPriceTextView(cdi.si.item.getPrice(), common.text_and_length_settings.TYPE_FACE_ABEL_FONT);


                    //create table column for price
                    tvPrice = CreateTotalPriceTextView(cdi.si.UnitOrder, cdi.si.item.getPrice(), common.text_and_length_settings.TYPE_FACE_ABEL_FONT);

                    TableRow trItem = new TableRow(this);
                    trItem.addView(tvItemName);
                    trItem.addView(tvUnit);
                    trItem.addView(tvPrice);
                    tblReceipt.addView(trItem);
                    for(ModifierObject mo: cdi.si.modifiers)
                    {
                        //create table column for Item
                        TextView tvSubName = CreateSubItemNameTextView(mo.getName(),
                                common.text_and_length_settings.TYPE_FACE_ABEL_FONT,
                                common.text_and_length_settings.SUB_ITEM_NAME_MAX_LENGTH, cdi.si.UnitOrder);
                        tvSubName.setPadding(20,0,0,0);
                        tvSubName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f));

                        ((TableRow.LayoutParams)tvSubName.getLayoutParams()).setMargins(0, common.text_and_length_settings.MODIFIER_TABLE_RECEIPT_ROW_NAME_TOP_MARGIN, 0, 0);


                        //create table column for unit price
                        TextView tvSubUnitPrice =  CreateSubUnitPriceTextView(mo.getPrice(),
                                common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
                        tvSubUnitPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f));


                        //create table column for price
                        TextView tvSubTotalPrice =  CreateSubTotalPriceTextView(mo.getPrice(),
                                common.text_and_length_settings.TYPE_FACE_ABEL_FONT, cdi.si.UnitOrder);


                        tvSubTotalPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f));
                        TableRow trSubItem = new TableRow(this);

                        trSubItem.addView(tvSubName);
                        trSubItem.addView(tvSubUnitPrice);
                        trSubItem.addView(tvSubTotalPrice);
                        tblReceipt.addView(trSubItem);
                    }
                }

            }
        }
    }
    private TableRow CreateNoRecordTableRow()
    {
        TableRow tr = new TableRow(this);
        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);
        tr.setLayoutParams(tllp);
        tr.setGravity(Gravity.CENTER);
        //tllp.gravity=Gravity.CENTER_HORIZONTAL;
        //tr.setBackgroundColor(Color.BLUE);
        TextView tv = new TextView(this);
        tv.setText("No Data");
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.CHECKOUT_PANEL_FRAGMENT_TEXT_SIZE + 5);
        tv.setTextColor(getResources().getColor(R.color.common_signin_btn_light_text_disabled));
        //TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        //tv.setGravity(Gravity.CENTER);
        //tv.setBackgroundColor(Color.GREEN);
        //trlp.gravity = Gravity.CENTER;
        //tv.setLayoutParams(trlp);

        //tv.setWidth(500);
        tr.addView(tv);
        return tr;
    }

    protected void ShowDatePickerDialog()
    {
        blnDateDialogShow = true;
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, this, year, month, day).show();
    }
    private void SetCurrentDateForDateInput()
    {
        final Calendar c = Calendar.getInstance();
        int month = (c.get(Calendar.MONTH)+1);
        int day = c.get(Calendar.DAY_OF_MONTH);

        ((TextView)findViewById(R.id.tvSelectedReceiptDate)).setText(Html.fromHtml("<u>"+ReturnDateString(month, day, c.get(Calendar.YEAR))+"</u>"));

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
        Calendar cal = new GregorianCalendar();//Calendar.getInstance();
        cal.set(year, month, day,0,0,0);

        ((TextView) findViewById(R.id.tvSelectedReceiptDate)).setText(Html.fromHtml("<u>" + ReturnDateString(month + 1, day, year) + "</u>"));
        ((TextView) findViewById(R.id.tvSelectedReceiptDate)).setTag(cal.getTimeInMillis());

        //reset flag
        blnLoading=false;

    }
}
