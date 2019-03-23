package tme.pos;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.CanSlideLinearLayout;

/**
 * Created by kchoy on 11/19/2015.
 */
public class CheckoutPanelDialog extends Dialog
implements PriceTextWatcher.IPriceTextWatcherListener,
PercentageTextWatcher.IPercentageTextWatcherListener{
    @Override
    public void AfterPercentageTextChanged(String strNewText) {
        //tvDiscountOption.setText(strNewText);
        receipt.SetDiscountValue(Enum.DiscountType.percentage,
                Float.parseFloat(strNewText.replaceAll("[$%,]", "")));
        UpdateReceiptSummaryLabel();
        UpdateAddDiscountLabel(true,strNewText);
    }

    @Override
    public void AfterPriceTextChanged(String strNewText) {
        strNewText = strNewText.replaceAll("[%$,]", "");
        receipt.SetDiscountValue(Enum.DiscountType.cash,
                Float.parseFloat(strNewText));

        UpdateReceiptSummaryLabel();
        UpdateAddDiscountLabel(false,strNewText);

    }
    private void UpdateAddDiscountLabel(boolean blnPercentage,String strNewText)
    {
        strNewText =new BigDecimal(strNewText).setScale(2,BigDecimal.ROUND_HALF_UP).toPlainString();
        tvDiscountOption.setText(DISCOUNT_LABEL + " -" + (blnPercentage?strNewText+"%":"$"+strNewText));
    }
    public interface ICheckoutPanelDialog
    {
        void CheckoutPanelDialogDismissed();
        void PrintBalanceReceipt(Receipt receipt);
        void CheckoutPanelPrintReceipt(Receipt receipt);
    }

    ICheckoutPanelDialog listener;
    Receipt receipt;
    Context context;
    TextView tvTaxOption;
    TextView tvReceiptOption;

    /**gratuity info label**/
    TextView tvGratuityValue ;
    /**tax**/
    TextView tvTaxValue;
    /**discount info label**/
    TextView tvPromotionDiscountValue;
    TextView tvPromotionTitle;
    LinearLayout promotionTitlePanel;
    /**discount info label**/
    TextView tvDiscountValue;
    /**Total**/
    TextView tvFinalTotalValue;
    /**new amount after additional discount**/
    TextView tvNewAmountValue;

    /**discount option **/
    EditText txtDiscountValue;
    TextView tvDiscountOption;
    TextView tvDollarSignLabel;
    TextView tvPercentageSignLabel;

    /**receipt options**/
    TextView tvGratuityOption;
    CheckBox[] receiptOptions;
    //CheckBox chkReceiptOptionPrint;
    //CheckBox chkReceiptOptionEmail;
    EditText txtReceiptOptionEmail;

    /**payment received and balance**/
    EditText txtPayment;
    TextView tvBalance;

    /**Thread for checking promotion money discount**/
    Handler checkCashAmountPromotionHandler;
    Runnable checkCashAmountPromotionThread;

    boolean blnOverwrite=false;
    TextView[] gratuityOptins=new TextView[6];
    TextView[] discountOptins=new TextView[2];
    TextView[] paymentOptions = new TextView[2];
    final static int MAX_SERVER_CHAR_DISPLAY=18;
    final static String DISCOUNT_LABEL="Add discount";

    PercentageTextWatcher percentageTextWatcher;
    PriceTextWatcher priceTextWatcher;
    //final static String DEFAULT_PERCENT_LABEL="0%";
    //final static String DEFAULT_CASH_LABEL="$0.00";

    public CheckoutPanelDialog(Context c,Receipt r,ICheckoutPanelDialog l)
    {
        super(c);
        context = c;
        receipt = r;
        receipt.PaidAmount=0f;
        listener = l;
        checkCashAmountPromotionHandler = new Handler();
        checkCashAmountPromotionThread = new Runnable() {
            @Override
            public void run() {
                CheckPromotionByCashForThisReceipt();
                UpdateReceiptSummaryLabel();
            }
        };

    }
    private void UpdateReceiptOptions()
    {

        boolean blnPercent = true;
        //initialize discount is percentage
        String strValue="00.00";

        if(receipt.GetDiscountType()== Enum.DiscountType.none)
        {

        }
        else if(receipt.GetDiscountType()== Enum.DiscountType.cash)
        {
            blnPercent = false;
            strValue=common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForDiscount())).replace("$","");//.setScale(2,RoundingMode.HALF_UP));
            //mark the discount by cash active
            discountOptins[1].setTextColor(Color.WHITE);
            discountOptins[0].setTextColor(context.getResources().getColor(R.color.green));
            //discountOptins[0].setTextColor(context.getResources().getColor(R.color.lost_shine_green));

            discountOptins[1].setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
            discountOptins[0].setBackground(context.getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
            /*discountOptins[1].setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
            discountOptins[0].setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));*/
        }
        else if(receipt.GetDiscountType()== Enum.DiscountType.percentage)
        {
            blnPercent = true;

            strValue = receipt.GetPercentageValueForDiscount()+"";// + strTemp;
            strValue+=strValue.indexOf(".")+2<strValue.length()-1?"":"0";
        }


        txtDiscountValue.setText(strValue);
        txtDiscountValue.setSelection(strValue.length() - 1);
        //tvDiscountOption.setText("Add discount -" +(blnPercent?"":"$") +strValue+(blnPercent?"%":""));
        tvDiscountOption.setText("Add discount -"  +strValue+(blnPercent?"%":""));
        /* if(receipt.GetPercentageValueForDiscount()>0 && receipt.GetCashValueForDiscount()>0)
        {
            receipt.CashValueForDiscount = receipt.PercentageValueForDiscount * receipt.myCart.getAmount().floatValue();
            receipt.CashValueForDiscount = (new BigDecimal(receipt.CashValueForDiscount)).setScale(2, RoundingMode.CEILING).floatValue();
        }*/


        tvDiscountValue.setText("-" +common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForDiscount())));
        //tvDiscountValue.setText("-" +common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.CashValueForDiscount).setScale(2,RoundingMode.CEILING)));




        //need to update tax amount
        tvTaxValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.GetTaxAmountAfterAmountCashPromotionDiscount()));

        //new to show new amount
        tvNewAmountValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount()));

        //need to update the final total
        tvFinalTotalValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()));

        //initialize tax
        if(receipt.flTaxRate>0) {
            tvTaxOption.setText("Tax @ " + new BigDecimal(receipt.myCart.percentage * 100).setScale(3,BigDecimal.ROUND_HALF_UP).toPlainString() + "%");
        }
        else
        {
            tvTaxOption.setText("Tax Exempted");
            tvTaxOption.setTextColor(context.getResources().getColor(R.color.green));
            tvTaxOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
            /*tvTaxOption.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
            tvTaxOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));*/
        }


        //initialize gratuity
        String strPercentage=receipt.GetGratuityPercentage()+"";
        for(int i=0;i<gratuityOptins.length;i++)
        {
            String strTemp = gratuityOptins[i].getText().toString().replace("%","");
            if(strPercentage.contains(strTemp))
            {
                //gratuityOptins[i].setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
                gratuityOptins[i].setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
                gratuityOptins[i].setTextColor(Color.WHITE);
                tvGratuityOption.setText("Gratuity " + gratuityOptins[i].getText());
                tvGratuityOption.setTextColor(Color.WHITE);
                tvGratuityOption.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_round_corner));
                //tvGratuityOption.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
                //receipt.CashValueForGratuity=receipt.GetAmountAfterAdditionalDiscount().floatValue()*receipt.GetGratuityPercentage() /100f;
                tvGratuityValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity())));


                break;
            }

        }

    }
    private void UpdateReceiptSummaryLabel()
    {


        tvDiscountValue.setText("-" + common.Utility.ConvertBigDecimalToCurrencyFormat((new BigDecimal(receipt.GetCashValueForDiscount()))));

        tvNewAmountValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount()));
        tvGratuityValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity())));
        tvTaxValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.GetTaxAmountAfterAmountCashPromotionDiscount()));
        tvFinalTotalValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()));
        BigDecimal bdTemp = new BigDecimal( Float.parseFloat(txtPayment.getText().toString().replaceAll("[$,-]",""))-receipt.ReturnReceiptFinalTotalAmount().floatValue()).setScale(2,RoundingMode.HALF_UP);
        tvBalance.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(bdTemp));
    }
    private void CheckPromotionByCashForThisReceipt()
    {
        checkCashAmountPromotionHandler.removeCallbacks(checkCashAmountPromotionThread);
        receipt.myCart.SetPromotionByPrice(null);

        //check promotion by cash at this point during check out
        float flAmount = receipt.myCart.getAmount().floatValue();
        ArrayList<PromotionObject> promotions = common.myPromotionManager.GetCurrentPromotionByTotal();
        int indexPromotion=-1;
        PromotionObject promotionObject=null;
        for(int i=0;i<promotions.size();i++)
        {
            if(promotions.get(i).GetStartingAmount().floatValue()<=flAmount) {

                if (promotions.get(i).GetUpperLimitFlag() && promotions.get(i).GetUpperLimitAmount().floatValue() >= flAmount) {

                    indexPromotion = i;
                } else if (!promotions.get(i).GetUpperLimitFlag()) {

                    indexPromotion = i;
                }

                //take this
                if (indexPromotion== i)
                    break;
            }
        }
        if(indexPromotion >-1)
        {
            promotionObject = promotions.get(indexPromotion);
            receipt.myCart.SetPromotionByPrice(promotionObject);
        }
        if(promotionObject!=null)
        {

            promotionTitlePanel.setVisibility(View.VISIBLE);
            tvPromotionTitle.setText(promotionObject.GetTitle());
            tvPromotionDiscountValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.GetPromotionByCashAmount()));



        }
        else
        {
            promotionTitlePanel.setVisibility(View.GONE);
            tvPromotionTitle.setText("");
            tvPromotionDiscountValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(0)));

        }

        //check every minute
        Calendar cCheck= new GregorianCalendar();
        cCheck.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
        cCheck.set(Calendar.SECOND,0);
        cCheck.set(Calendar.MINUTE,cCheck.get(Calendar.MINUTE)+1);

        checkCashAmountPromotionHandler.postDelayed(checkCashAmountPromotionThread,cCheck.getTimeInMillis()-Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public void dismiss() {
        if(listener!=null)listener.CheckoutPanelDialogDismissed();
        super.dismiss();

        //reset promotion to null if user hasn't paid
        if(!receipt.blnHasPaid)receipt.myCart.SetPromotionByPrice(null);

        //cancel the timer
        if(checkCashAmountPromotionHandler!=null)
        checkCashAmountPromotionHandler.removeCallbacks(checkCashAmountPromotionThread);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_checkout_panel_popup_window_ui);

        tvFinalTotalValue = (TextView)findViewById(R.id.tvFinalTotalValue);

        /**gratuity info label**/
        tvGratuityValue = (TextView)findViewById(R.id.tvGratuityValue);

        /**tax**/
        tvTaxValue = (TextView)findViewById(R.id.tvTaxValue);
        /**discount info label**/
        promotionTitlePanel  = (LinearLayout)findViewById(R.id.promotionTitlePanel);
        tvPromotionDiscountValue = (TextView)findViewById(R.id.tvPromotionDiscountValue);
        tvPromotionTitle = (TextView)findViewById(R.id.tvPromotionTitle);
        tvDiscountValue = (TextView)findViewById(R.id.tvDiscountValue);
        tvDollarSignLabel = (TextView)findViewById(R.id.tvDollarSignLabel);
        tvPercentageSignLabel = (TextView)findViewById(R.id.tvPercentageSignLabel);
        tvPercentageSignLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE);
        tvPercentageSignLabel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tvDollarSignLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.DP_DISCOUNT_AMOUNT_LABEL_TEXT_SIZE);
        tvDollarSignLabel.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        /**New amount**/
        tvNewAmountValue = (TextView)findViewById(R.id.tvNewAmountValue);

        /**print receipt**/
        findViewById(R.id.imgPrintReceipt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null)listener.CheckoutPanelPrintReceipt(receipt);
            }
        });
        /**Balance**/
        tvBalance = (TextView)findViewById(R.id.tvBalance);
        findViewById(R.id.imgPaid).setEnabled(!receipt.blnHasPaid);
        findViewById(R.id.imgPaid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check payment received, do not allowed to save if payment is less than total
                if(receipt.PaidAmount-receipt.ReturnReceiptFinalTotalAmount().floatValue()<0)
                {
                    common.Utility.ShowMessage("Checkout","Insufficient fund to pay.",context,R.drawable.no_access);
                    return;
                }

                //queue receipt for printing later
                receipt.printReceiptType = Enum.PrintReceiptType.change_balance;

                //insert receipt number and timestamp, and update properties
                common.Utility.FillInReceiptProperties(receipt,receipt.tableNumber);
                //String strTemp = receipt.ReturnHTMLContentForEmailReceipt();
                if(receiptOptions[1].isChecked()) {
                    if(BluetoothAdapter.getDefaultAdapter()!=null && BluetoothAdapter.getDefaultAdapter().isEnabled() &&
                            common.myPrinterManager.HasConnectedPrinter())
                    {

                        /**in order to get receipt number, receipt has to be paid 1st in order to generate receipt #**/
                        //if(listener!=null)listener.PrintBalanceReceipt(receipt);



                    }
                    else if(BluetoothAdapter.getDefaultAdapter()!=null && !BluetoothAdapter.getDefaultAdapter().isEnabled())
                    {
                        //prompt user to start bluetooth and return
                        ((MainUIActivity) context).StartBluetooth();
                        return;
                    }
                    else if(BluetoothAdapter.getDefaultAdapter()!=null && BluetoothAdapter.getDefaultAdapter().isEnabled())
                    {
                        if(!common.myPrinterManager.HasConnectedPrinter())
                        {
                            ((MainUIActivity) context).PromptToChoosePrinter();
                            return;
                        }
                    }


                }

                if(receiptOptions[0].isChecked()
                        && txtReceiptOptionEmail.getText().length()==0
                        )
                {
                    common.Utility.ShowMessage("Email","Please provide customer email address for receipt.",context,R.drawable.no_access);
                    return;
                }
                //mark paid, pay first only then the receipt number will be available only
                //make a copy
                Receipt copiedReceipt = (Receipt) receipt.clone();
                ((MainUIActivity) context).Paid(receipt);

                copiedReceipt.receiptNumber = receipt.receiptNumber;
                if(receiptOptions[1].isChecked()) {
                    if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled() &&
                            common.myPrinterManager.HasConnectedPrinter()) {
                        copiedReceipt.printReceiptType = Enum.PrintReceiptType.change_balance;
                        //receipt.printReceiptType = Enum.PrintReceiptType.change_balance;
                        if (listener != null) listener.PrintBalanceReceipt(copiedReceipt);


                    }
                }
                //email receipt
                if(receiptOptions[0].isChecked()
                        && txtReceiptOptionEmail.getText().length()>0
                       )
                {
                    ((MainUIActivity) context).SendReceiptEmail(copiedReceipt, txtReceiptOptionEmail.getText()+"");
                }



                dismiss();
            }
        });
        /**exact change**/
        TextView tvExactChange = (TextView)findViewById(R.id.tvExactChange);
        tvExactChange.setText(Html.fromHtml("<font color='#0404B4'><u>Exact</u></font>"));
        tvExactChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtPayment.setText(tvFinalTotalValue.getText());
            }
        });

        /** tax option control**/
        tvTaxOption = (TextView)findViewById(R.id.tvTaxOption);
        tvTaxOption.setText("Tax @ " + (receipt.myCart.percentage * 100) + "%");
        tvTaxOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvTaxOption.getCurrentTextColor() == context.getResources().getColor(R.color.white)) {
                    //text exempt
                    //tvTaxOption.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
                    tvTaxOption.setTextColor(context.getResources().getColor(R.color.green));
                    tvTaxOption.setText("Tax Exempted");
                    tvTaxOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
                    //tvTaxOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));
                    receipt.flTaxRate=0;
                    receipt.myCart.percentage=0;

                } else {
                    //with tax
                    receipt.flTaxRate=common.myAppSettings.GetTaxPercentage();
                    receipt.myCart.percentage=receipt.flTaxRate;
                    tvTaxOption.setTextColor(context.getResources().getColor(R.color.white));
                    tvTaxOption.setText("Tax @ " + (receipt.myCart.percentage * 100) + "%");
                    tvTaxOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
                    //tvTaxOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
                }
                //tvTaxValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.getTaxAmount()));
                tvTaxValue.setText(
                        common.Utility.ConvertBigDecimalToCurrencyFormat(
                                receipt.GetAmountAfterAmountCashPromotionDiscountPlusAdditionalDiscount().multiply(new BigDecimal(receipt.flTaxRate))));
                tvFinalTotalValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.ReturnReceiptFinalTotalAmount()));
                //trigger exact to update balance
                UpdateReceiptSummaryLabel();
            }
        });

        /**Server option control**/
        //reset receipt server
        receipt.server = new Server();
        TextView tvServerOption = (TextView)findViewById(R.id.tvServerOption);
        final CanSlideLinearLayout llServerOptionsPanel = (CanSlideLinearLayout)findViewById(R.id.llServerOptionsPanel);
        tvServerOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(llServerOptionsPanel.getLayoutParams().height==0)
                {
                    llServerOptionsPanel.SlideDown();
                    tvTaxOption.setText(tvTaxOption.getText());//hack to trigger main ui thread to update

                }
            }
        });

        if(common.serverList.GetServers().length==0)
        {
            tvServerOption.setText("Server not available");
            tvServerOption.setOnClickListener(null);
        }
        else {
            LinearLayout lstServer = (LinearLayout)findViewById(R.id.lstServer);
            int count=1;
            for (Server s : common.serverList.GetServers()) {
                TextView tvServer = new TextView(context);
                if(s.Name.length()>20)
                {
                    tvServer.setText(s.Name.substring(0,MAX_SERVER_CHAR_DISPLAY));
                }
                else {
                    tvServer.setText(s.Name);
                }
                tvServer.setGravity(Gravity.CENTER_HORIZONTAL);
                //tvServer.setTag(s.EmployeeId);
                tvServer.setTag(s);
                //tvServer.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
                tvServer.setTextColor(context.getResources().getColor(R.color.green));
                tvServer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
                //tvServer.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
                tvServer.setPadding(0, common.Utility.DP2Pixel(5, context), 0, 0);
                LinearLayout.LayoutParams server_lp =new LinearLayout.LayoutParams(common.Utility.DP2Pixel(280, context)
                        , common.Utility.DP2Pixel(50, context));
                server_lp.gravity=Gravity.CENTER;
                server_lp.topMargin=(count==1?common.Utility.DP2Pixel(5,context):0);
                count++;
                lstServer.addView(tvServer,server_lp );
                LinearLayout line = new LinearLayout(context);
                //line.setBackgroundColor(context.getResources().getColor(R.color.lost_shine_green));
                line.setBackgroundColor(context.getResources().getColor(R.color.green));
                LinearLayout.LayoutParams line_lp =new LinearLayout.LayoutParams(common.Utility.DP2Pixel(280, context), common.Utility.DP2Pixel(1, context));
                lstServer.addView(line,line_lp);
                line_lp.gravity = Gravity.CENTER;
                BindServerClickEvent(tvServer, tvServerOption, llServerOptionsPanel, lstServer);
            }
        }

        /** gratuity option control**/
        tvGratuityOption = (TextView)findViewById(R.id.tvGratuityOption);
        gratuityOptins[0] =(TextView) findViewById(R.id.tv15Percent);
        gratuityOptins[1] =(TextView) findViewById(R.id.tv16Percent);
        gratuityOptins[2] = (TextView)findViewById(R.id.tv17Percent);
        gratuityOptins[3] =(TextView) findViewById(R.id.tv18Percent);
        gratuityOptins[4] =(TextView) findViewById(R.id.tv19Percent);
        gratuityOptins[5] = (TextView)findViewById(R.id.tv20Percent);
        for(int i=0;i<gratuityOptins.length;i++)
        {
            BindGratuitySelectedEvent(gratuityOptins[i],tvGratuityOption,tvGratuityValue);
        }



        tvGratuityOption.setText("Gratuity 0%");
        tvGratuityOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CanSlideLinearLayout llGratuityOptionsPanel = (CanSlideLinearLayout)findViewById(R.id.llGratuityOptionsPanel);
                if(llGratuityOptionsPanel.getLayoutParams().height==0)
                {
                   llGratuityOptionsPanel.SlideDown();
                   tvTaxOption.setText(tvTaxOption.getText());//hack to trigger main ui thread to update

                }
                else
                {
                    llGratuityOptionsPanel.SlideUp();
                }

            }
        });

        /** Additional discount**/
        discountOptins[0]=(TextView)findViewById(R.id.tvDiscountPercent);
        discountOptins[1]=(TextView)findViewById(R.id.tvDiscountCash);
        //final TextView tvDiscountValue = (TextView)findViewById(R.id.tvDiscountValue);
        tvDiscountOption = (TextView)findViewById(R.id.tvDiscountOption);
        final CanSlideLinearLayout llDiscountOptionsPanel = (CanSlideLinearLayout) findViewById(R.id.llDiscountOptionsPanel);
        tvDiscountOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (llDiscountOptionsPanel.getLayoutParams().height == 0) {
                    llDiscountOptionsPanel.SlideDown();
                    tvTaxOption.setText(tvTaxOption.getText());//hack to trigger main ui thread to update

                }
                else
                {
                    common.control_events.HideSoftKeyboard(txtDiscountValue);
                    llDiscountOptionsPanel.SlideUp();
                    //change color if discount value has been provided
                    float temp = Float.parseFloat(txtDiscountValue.getText().toString().replaceAll("[$,%]", ""));
                    ToggleAdditionalDiscountOptionColor(temp!=0);

                }

            }
        });
        tvDiscountOption.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String strEditable = editable.toString().replaceAll("[$%,a-zA-Z]", "");
                ToggleAdditionalDiscountOptionColor(Float.parseFloat(strEditable)!= 0);


            }
        });
        txtDiscountValue=(EditText)findViewById(R.id.txtDiscountValue);
        priceTextWatcher = new PriceTextWatcher(context,txtDiscountValue,false,this);
        percentageTextWatcher = new PercentageTextWatcher(context,txtDiscountValue,false,this);
        for(int i=0;i<discountOptins.length;i++)
        {
            discountOptins[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View view) {
                    DiscountTypeSelectedEvent((TextView) view, tvDiscountOption,txtDiscountValue);
                    UpdateReceiptSummaryLabel();
                }
            });

        }
        txtDiscountValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event==null && actionId != EditorInfo.IME_ACTION_DONE)
                {
                    return false;
                }
                else if (actionId == EditorInfo.IME_ACTION_DONE) {
                    llDiscountOptionsPanel.SlideUp();
                    //ValidateDiscountValue();
                    v.clearFocus();
                    //hide softkeyboard
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            common.control_events.HideSoftKeyboard(tvTaxOption);
                        }
                    }, 200);
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_PERIOD || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_DOT) {
                    return true;
                }
                return false;
            }
        });

        /** print receipt option **/
        receiptOptions = new CheckBox[2];
        receiptOptions[0] = (CheckBox)findViewById(R.id.chkReceiptOptionEmail);
        receiptOptions[1] = (CheckBox)findViewById(R.id.chkReceiptOptionPrint);
        for(int i=0;i<receiptOptions.length;i++) {
            receiptOptions[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckReceiptOptions();
                }
            });
        }
        txtReceiptOptionEmail = (EditText)findViewById(R.id.txtReceiptOptionEmail);
        final CanSlideLinearLayout llReceiptOptionsPanel = (CanSlideLinearLayout) findViewById(R.id.llReceiptOptionsPanel);
        tvReceiptOption = (TextView)findViewById(R.id.tvReceiptOption);
        tvReceiptOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));

        tvReceiptOption.setTextColor(Color.WHITE);
        tvReceiptOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llReceiptOptionsPanel.getLayoutParams().height == 0) {
                    llReceiptOptionsPanel.SlideDown();
                    //scroll to bottom
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((ScrollView) findViewById(R.id.svRightReceiptOptionPanel)).fullScroll(View.FOCUS_DOWN);
                        }
                    }, 600);

                }
                else
                {
                    llReceiptOptionsPanel.SlideUp();
                }
            }
        });
        /**payment type**/
        paymentOptions[0] = (TextView)findViewById(R.id.tvCash);
        paymentOptions[1] = (TextView)findViewById(R.id.tvCreditCard);
        for(int i=0;i<paymentOptions.length;i++)
        {
            paymentOptions[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PaymentOptionClickEvent((TextView) view);
                }
            });

        }

        /**display amount, order does matter**/
        CheckPromotionByCashForThisReceipt();
        ((TextView)findViewById(R.id.tvAmountValue)).setText(common.Utility.ConvertBigDecimalToCurrencyFormat(receipt.myCart.getAmount()));


        /**bind cash payment controls**/
        BindCashCheckoutControl();
        //initialize receipt summary
        UpdateReceiptSummaryLabel();

        //initialize the receipt options, need to wait for page ready
        UpdateReceiptOptions();

        if(receipt.GetDiscountType()== Enum.DiscountType.percentage)
        {
            discountOptins[0].callOnClick();
            //txtDiscountValue.addTextChangedListener(percentageTextWatcher);
        }
        else
        {
            discountOptins[1].callOnClick();
            //txtDiscountValue.addTextChangedListener(priceTextWatcher);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                paymentOptions[0].callOnClick();
                //hide soft keyboard
                common.control_events.HideSoftKeyboard(tvTaxOption);
            }
        }, 200);
    }





    private void ToggleAdditionalDiscountOptionColor(boolean blnOn)
    {

        if(!blnOn)
        {
            tvDiscountOption.setTextColor(context.getResources().getColor(R.color.green));
            ((LinearLayout) tvDiscountOption.getParent()).setBackground(getContext().getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
            tvDiscountOption.setBackground(null);
        }
        else
        {
            tvDiscountOption.setTextColor(Color.WHITE);
            tvDiscountOption.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_round_corner));
        }
    }
    private void CheckReceiptOptions()
    {

        boolean blnPrintPaperReceipt = receiptOptions[1].isChecked();
        boolean blnEmailReceipt=receiptOptions[0].isChecked();
        String strLabel=tvReceiptOption.getText()+"";
        tvReceiptOption.setTextColor(context.getResources().getColor(R.color.white));
        tvReceiptOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
        //tvReceiptOption.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
        if(blnEmailReceipt && blnPrintPaperReceipt) {
            if(common.myAppSettings.GetSendReceiptEmail().length()==0)
            {
                //prompt user to setup email, if hasn't
                receiptOptions[0].setChecked(false);
                common.Utility.ShowMessage("Send Email Receipt","You haven't setup your email address in settings."
                        ,context,R.drawable.no_access);
                blnEmailReceipt=false;
            }
            else {
                strLabel = "Print and email receipt";
            }
        }
        else if(blnEmailReceipt)
        {
            if(common.myAppSettings.GetSendReceiptEmail().length()==0)
            {
                //prompt user to setup email, if hasn't
                receiptOptions[0].setChecked(false);
                common.Utility.ShowMessage("Send Email Receipt","You haven't setup your email address in settings."
                        ,context,R.drawable.no_access);
                blnEmailReceipt=false;
            }
            else {
                strLabel = "Email receipt";
            }

        }
        else if(blnPrintPaperReceipt)
        {
            strLabel="Print receipt";
        }
        else{
            //tvReceiptOption.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
            tvReceiptOption.setTextColor(context.getResources().getColor(R.color.green));
            tvReceiptOption.setBackground(null);
            strLabel="No receipt";
        }

        //last check
        if(blnEmailReceipt ==false && blnPrintPaperReceipt==false)
        {
            //tvReceiptOption.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
            tvReceiptOption.setTextColor(context.getResources().getColor(R.color.green));
            tvReceiptOption.setBackground(null);
            strLabel="No receipt";
        }
        tvReceiptOption.setText(strLabel);

    }
    private void PaymentOptionClickEvent(TextView tv)
    {
        //reset all options
        for(int i=0;i<paymentOptions.length;i++) {
            paymentOptions[i].setTextColor(context.getResources().getColor(R.color.divider_grey));
            paymentOptions[i].setBackground(null);
        }

        //tv.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
        tv.setTextColor(context.getResources().getColor(R.color.green));
        //tv.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));
        tv.setBackground(context.getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));


        if(tv.getText().toString().equalsIgnoreCase("cash"))
        {
            receipt.paymentType= Enum.PaymentType.cash;
        }
        else
        {
            receipt.paymentType= Enum.PaymentType.credit;
        }
    }
    private  void BindServerClickEvent(final TextView tvClicked, final TextView tvDisplay
            ,final CanSlideLinearLayout slider,final LinearLayout lstServer){
            tvClicked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tvClicked.getCurrentTextColor() == Color.WHITE) {
                        tvDisplay.setText("Server");
                        //tvDisplay.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
                        tvDisplay.setTextColor(context.getResources().getColor(R.color.green));
                        tvDisplay.setBackground(null);
                        //tvClicked.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
                        tvClicked.setTextColor(context.getResources().getColor(R.color.green));
                        tvClicked.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                        receipt.server = new Server();
                    } else {
                        //reset selected servant
                        for (int i = 0; i < lstServer.getChildCount(); i = i + 2) {
                            TextView tv = (TextView) lstServer.getChildAt(i);
                            if (tv.getCurrentTextColor() == Color.WHITE) {
                                //tv.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
                                tv.setTextColor(context.getResources().getColor(R.color.green));
                                tv.setBackgroundColor(context.getResources().getColor(R.color.transparent));
                                break;
                            }
                        }

                        tvDisplay.setTextColor(Color.WHITE);
                        String strTemp = "Server " + tvClicked.getText();
                        strTemp = (strTemp.length() > 20) ? strTemp.substring(0, MAX_SERVER_CHAR_DISPLAY) : strTemp;
                        tvDisplay.setText(strTemp);
                        tvDisplay.setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
                        //tvDisplay.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
                        tvClicked.setTextColor(context.getResources().getColor(R.color.white));
                        //tvClicked.setBackgroundColor(context.getResources().getColor(R.color.lost_shine_green));
                        tvClicked.setBackgroundColor(context.getResources().getColor(R.color.green));
                        receipt.server =(Server)tvClicked.getTag();
                    }
                    slider.SlideUp();
                }
            });
    }
    private void BindCashCheckoutControl()
    {
        txtPayment=(EditText)findViewById(R.id.txtPayment);
        txtPayment.addTextChangedListener(new TextWatcher() {
            String strPrevious = "";
            int cursorIndex = 0;
            boolean isEditing = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cursorIndex = after;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isEditing || blnOverwrite) return;
                isEditing = true;

                strPrevious = common.Utility.CheckPaymentMoneyTextChanged(s, strPrevious);

                txtPayment.setText(strPrevious + "");
                txtPayment.setSelection(cursorIndex);

                float flReceived=Float.parseFloat(strPrevious.replaceAll("[$,]", ""));
                if(flReceived<0.009)
                {
                    tvBalance.setText("$0.00");
                }
                else {
                    tvBalance.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal( flReceived-receipt.ReturnReceiptFinalTotalAmount().floatValue())));
                    receipt.paymentType= Enum.PaymentType.cash;

                }
                receipt.PaidAmount = flReceived;
                isEditing = false;
            }
        });
    }
    private boolean IsPercentDiscountMode()
    {
        return (discountOptins[0].getCurrentTextColor()==Color.WHITE)?true:false;
    }

    private void DiscountTypeSelectedEvent(TextView v,TextView tvType,final EditText txtValue)
    {
       int index1=-1,index2=-1;
        boolean blnPercentage = false;
        String strTemp=txtValue.getText().toString();
        if(v.getText().toString().equalsIgnoreCase("%"))
        {

           //percentage clicked
            blnPercentage = true;
            txtDiscountValue.removeTextChangedListener(priceTextWatcher);
            txtDiscountValue.addTextChangedListener(percentageTextWatcher);
            tvPercentageSignLabel.setVisibility(View.VISIBLE);
            tvDollarSignLabel.setText("-");
            strTemp=strTemp.replaceAll("[%$]", "");//+"%";
            index1=0;
            index2=1;


            //receipt.discountType = Enum.DiscountType.percentage;
            if (strTemp.equalsIgnoreCase("0")){//%")) {
                receipt.SetDiscountValue(Enum.DiscountType.cash,0);

            } else {
                receipt.SetDiscountValue(Enum.DiscountType.percentage,Float.parseFloat(strTemp.replaceAll("[%$,]", "")));

            }

        }
        else
        {
            //cash clicked
            blnPercentage = false;
            txtDiscountValue.removeTextChangedListener(percentageTextWatcher);
            txtDiscountValue.addTextChangedListener(priceTextWatcher);
            tvPercentageSignLabel.setVisibility(View.INVISIBLE);
            tvDollarSignLabel.setText("-$");

            strTemp=strTemp.replaceAll("[%$]", "");
            index1=1;
            index2=0;

            receipt.SetDiscountValue(Enum.DiscountType.cash,Float.parseFloat(strTemp.replaceAll("[%$,]", "")));


        }

        UpdateAddDiscountLabel(blnPercentage,strTemp);
        //tvType.setText(DISCOUNT_LABEL + " -" + (blnPercentage?strTemp+"%":"$"+strTemp));


        discountOptins[index1].setTextColor(Color.WHITE);
        discountOptins[index2].setTextColor(context.getResources().getColor(R.color.green));


        discountOptins[index1].setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
        discountOptins[index2].setBackground(context.getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));

    }
    private void DeselectAllGratuityControl()
    {
        for(int i=0;i<gratuityOptins.length;i++)
        {
            gratuityOptins[i].setBackground(context.getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
            gratuityOptins[i].setTextColor(context.getResources().getColor(R.color.green));
            /*gratuityOptins[i].setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));
            gratuityOptins[i].setTextColor(context.getResources().getColor(R.color.lost_shine_green));*/
        }
    }

    private void BindGratuitySelectedEvent(final TextView v,final TextView tvGratuityOption,final TextView tvGratuityValue)
    {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CanSlideLinearLayout panel =(CanSlideLinearLayout) findViewById(R.id.llGratuityOptionsPanel);

                if(v.getCurrentTextColor()==Color.WHITE)
                {
                    //reset if clicking on the selected value
                    tvGratuityOption.setText("Gratuity 0%");
                    tvGratuityValue.setText("$0.00");
                    tvGratuityOption.setTextColor(context.getResources().getColor(R.color.green));
                    ((LinearLayout) tvGratuityOption.getParent()).setBackground(getContext().getResources().getDrawable(R.drawable.drawable_green_checkout_unfilled_round_corner));
                    /*tvGratuityOption.setTextColor(context.getResources().getColor(R.color.lost_shine_green));
                    ((LinearLayout) tvGratuityOption.getParent()).setBackground(getContext().getResources().getDrawable(R.drawable.drawable_checkout_unfilled_round_corner));*/
                    tvGratuityOption.setBackground(null);

                    panel.SlideUp();
                    DeselectAllGratuityControl();
                    receipt.SetGratuityPercentage(0f);
                    //receipt.SetGratuityPercentage(0);


                }
                else
                {

                    receipt.SetGratuityPercentage(Float.parseFloat(v.getText().toString().replace("%", "")));


                    //receipt.CashValueForGratuity=receipt.GetAmountAfterAdditionalDiscount().floatValue()*receipt.GetGratuityPercentage() /100f;
                    DeselectAllGratuityControl();
                    v.setBackground(context.getResources().getDrawable(R.drawable.drawable_round_corner));
                    //v.setBackground(context.getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
                    v.setTextColor(Color.WHITE);
                    tvGratuityOption.setText("Gratuity " + v.getText());
                    tvGratuityOption.setTextColor(Color.WHITE);
                    tvGratuityOption.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_round_corner));
                    //tvGratuityOption.setBackground(getContext().getResources().getDrawable(R.drawable.drawable_checkout_filled_round_corner));
                    tvGratuityValue.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(new BigDecimal(receipt.GetCashValueForGratuity())));
                    panel.SlideUp();
                }
                //
                //update receipt amount
                UpdateReceiptSummaryLabel();
                //UpdateReceiptOptions();
            }
        });
    }
}
