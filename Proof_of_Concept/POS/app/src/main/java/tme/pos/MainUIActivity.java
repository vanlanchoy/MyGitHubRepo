package tme.pos;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;


import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.util.Log;
import android.app.AlertDialog;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import printer.PrinterManager;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.*;
import android.util.*;
import android.content.Context;
import tme.pos.BusinessLayer.*;
import tme.pos.DataAccessLayer.Schema;
import tme.pos.Interfaces.ICreateCustomListActivityListener;
import tme.pos.Interfaces.ICustomListActivityListener;
import tme.pos.Interfaces.IOptionPopupAnimationCompleteListener;
import tme.pos.Interfaces.IDialogActivityListener;
import tme.pos.Interfaces.IItemMenuOptionActivityListener;
import tme.pos.Interfaces.IItemViewUpdateUnit;
import tme.pos.Interfaces.IOptionPopupItemListener;
import tme.pos.Interfaces.IPageActivityListener;
import tme.pos.Interfaces.IPromotionMenuContentUpdateListener;
import tme.pos.Interfaces.IToBeUpdatedInventoryView;
import tme.pos.SendEmail.SendAsyncEmail;
import tme.pos.WebService.TMe_POS_WS;
import tme.pos.TMe_POS_WS_Receiver.OnTMePOSServerListener;
import android.widget.Toast;

import com.google.android.gms.analytics.ecommerce.Promotion;


public class MainUIActivity extends FragmentActivity implements OnTMePOSServerListener,
        FloorPlan.FloorPlanEventListener,AdapterView.OnItemSelectedListener,
        FloorPlanCtr.FloorPlanCtrTableTouchedListener
        ,PhotoFeatureFragment.OnFragmentInteractionListener
,IPageActivityListener,IDialogActivityListener
    ,IItemMenuOptionActivityListener
        ,IPromotionMenuContentUpdateListener
    ,IOptionPopupAnimationCompleteListener
    ,IOptionPopupItemListener
    ,ICreateCustomListActivityListener
        ,ICustomListActivityListener
    ,LockScreenUnlockPasswordConfirmationDialog.ILockScreenUserRespondListerner
    ,CheckoutPanelDialog.ICheckoutPanelDialog
    ,TaxRateOptionPopup.ITaxRateChangedListener
    ,MyScrollView.IScrollViewListener
{


    static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private BluetoothAdapter mBluetoothAdapter = null;
    public AppSettings myAppSettings;
    private TMe_POS_WS_Receiver serverMsgReceiver;
    public static String PREFERENCE_LEFT_HANDED_SETTING_KEY = "tme_pos_app_pref_right_handed";//need this during initial startup
    String HINT_MODIFIER_NAME = "";
    String HINT_MODIFIER_PRICE = "";

    int CATEGORY_NAME_MAX_LENGTH = 10;

    int SUB_ITEM_NAME_MAX_LENGTH = 5;
    int INVOICE_ITEM_PER_PAGE = 10;

    int CURRENT_SUB_RECEIPT_INDEX = 0;
    int PAGE_INDICATOR_CIRCLE_VIEW_WIDTH = 0;
    int PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT = 0;

    int MAX_ITEM_PER_CATEGORY = 50;
    int MAX_MODIFIER_ITEM = 10;
    int MAX_CATEGORY_ITEM = 50;
    int LEFT_RIGHT_PADDING_RECEIPT_ROW = 10;
    int TOP_DOWN_PADDING_RECEIPT_ROW = 10;
    int TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW = 0;


    float DP_RECEIPT_ITEM_MENU_TEXT_SIZE = 22;
    float DP_RECEIPT_MODIFIER_TEXT_SIZE = 20;
    float ADD_MENU_ITEM_TITLE_TEXT_SIZE = 30;
    float ADD_MENU_ITEM_TEXT_SIZE = 30;
    float TEXT_SIZE = 24;
    float ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = 35;
    float SP_MENU_ITEM_TEXT_SIZE = 40;
    float DP_SERVER_NOTIFICATION_TEXT_SIZE = 40;

    float INVOICE_ITEM_NAME_WIDTH_WEIGHT = 0F;
    float INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT = 0F;
    float INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT = 0F;
    float INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT = 0F;
    float INVOICE_SUB_ITEM_UNIT_WIDTH_WEIGHT = 0F;
    float INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT = 0F;


    //boolean blnListingOrder= false;
    boolean LOADING_FLAG = false;
    boolean INITIAL_FLAG = true;
    boolean ACTION_BAR_SHOWN = false;
    boolean blnReceiptControlBusy = false;
    boolean blnTopCategoryContainerControlBusy = false;
    boolean blnCheckPromotionAfterPromotionFragmentDismiss=false;
    boolean blnPopupShow = false;
    boolean blnPromotionComboTopSellPopupShow=false;
    boolean blnBluetoothAvailable = false;
    Typeface FONT_ABEL;
    LinearLayoutWithExpansionAnimation llOptionPopup;
    android.os.Handler THREAD_HANDLER;
    PopupWindow POPUP_WINDOW;
    ListOrderAsyncTask listOrderAsyncTask;

    CheckoutPanelDialog checkoutPanelDialog;
    public ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private long SelectedCategoryId = -1;
    ImageView imgCloseOptionPopup;
    Intent TmeServerNotificationIntent;
    Intent TmeServerSendPasswordResetLinkIntent;
    Intent TmeServerRegisterDeviceIntent;
    Intent TmeServerUpdateGeoLocation;
    FloorPlanCtr fpCtr;
    Spinner drpTable;
    LinearLayout UndoBar;
    ItemInventoryOptionDialog itemInventoryOptionDialog;
    protected GestureDetector ReceiptScrollViewGestureDetector;
    //int flGridViewOriginalHeight;
    int lastLoadedOrderedItemIndex;
    Enum.ViewMode currentViewMode;
    Handler threadUndoBar;
    Handler threadCheckPromotion;
    Runnable runCheckPromotion;
    Runnable runHideUndoBar;
    Runnable runScrollTo;
    IntentFilter serverMsgListenerFilter;
    ArrayList<StoreItem>lastInsertedItemsToReceipt;
    MyCart currentOrderedItemCart;
    HashMap<String, ArrayList<PromotionObject>> currentTablePromotions;

    private Html.ImageGetter imgGetter = new Html.ImageGetter() {

        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            //if(imageNumber == 1) {
            drawable = getResources().getDrawable(R.drawable.green_select_promotion);
            drawable.setBounds(0, 0, 30,30);
            //++imageNumber;
           /* } else drawable = getResources().getDrawable(R.raw.a);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());*/

            return drawable;
        }
    };

    public MainUIActivity() {
        super();

        serverMsgListenerFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_UPDATE_GEOLOCATION);

    }
   /* @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        common.myCartManager = (MyCartManager) savedInstanceState.getSerializable("JsonMyCartManager");
    }



    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if(common.myCartManager==null)return;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();
        Gson gson = gsonBuilder.setPrettyPrinting().create();
        String jsonString = gson.toJson(common.myCartManager,MyCartManager.class);
        state.putSerializable("JsonMyCartManager", jsonString);
    }
*/


    @Override
    protected void onDestroy() {
        common.Utility.LogActivity("Main activity on destroy");
        super.onDestroy();
        //unregister
        if(common.myPrinterManager!=null)
            this.unregisterReceiver(common.myPrinterManager);

        if(serverMsgReceiver!=null)
            this.unregisterReceiver(serverMsgReceiver);

        if(common.PosCenter!=null)
        common.PosCenter.UnRegisterService();

    }

    @Override
    protected void onStop() {
        common.Utility.LogActivity("Main activity on stop");
        super.onStop();
        common.myLocationService.Disconnect();

        threadCheckPromotion.removeCallbacks(runCheckPromotion);
        //Stop the analytics tracking
        //GoogleAnalytics.getInstance(this).reportActivityStop(this);

    }

    @Override
    protected void onResume() {
        common.Utility.LogActivity("Main activity on resume");
        super.onResume();
        ((POS_Application) getApplication()).setCurrentActivity(this);
        //common.myPrinterManager.StartBluetoothPrinter();
        StartLoadSequenceThread();
        StartLocationServiceThread();

        if(blnCheckPromotionAfterPromotionFragmentDismiss)
        {
            blnCheckPromotionAfterPromotionFragmentDismiss=false;
            ListOrders(false,null);
            //Calculate();
        }
        //trigger callback
        if(itemInventoryOptionDialog!=null)
        {
            itemInventoryOptionDialog.ReloadSupplierList();
            itemInventoryOptionDialog = null;
        }

        //start timer to remove invalid promotion
        if(common.myPromotionManager!=null) {

            threadCheckPromotion.postDelayed(runCheckPromotion, common.myPromotionManager.GetTimeForPromotionRefresh(true));
        }
    }

    @Override
    protected void onPause() {
        common.Utility.LogActivity("Main on pause");
        CloseAnyNonDialogPopup();
        super.onPause();
        common.myLocationService.StopLocationService();

    }

    @Override
    protected void onStart() {
        /*ORDER DOES MATTER!!!*/
        common.Utility.LogActivity("Main activity Info on start");
        super.onStart();




        //Get an Analytics tracker to report app starts & uncaught exceptions etc.
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);

    }
    private void StartLocationServiceThread()
    {

        if (common.myLocationService != null) {
            common.myLocationService.ResumeLocationService();
        }
        else
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    StartLoadSequenceThread();
                }
            }, 5000);
        }
    }
    private void StartLoadSequenceThread()
    {
        if (INITIAL_FLAG) {
            INITIAL_FLAG = false;
            THREAD_HANDLER = new android.os.Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    LoadApplicationDataProcedure();
                }
            };

            THREAD_HANDLER.postDelayed(r, 2000);

            //InitializeControlBindings();

        } else {

            //re-adjust menu panel, make sure checkout panel is fit perfectly at the bottom
            ReadjustAllPanelHeight();
          /*  new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    common.Utility.ShowMessage("on resume","start load sequence thread",MainUIActivity.this,R.drawable.message);
                    ReadjustAllPanelHeight();
                }
            }, 100);*/

            }
    }

    public void DismissGeneralLoadingPage()
    {
        //close the loading popup when is done
        if (POPUP_WINDOW != null) {
            POPUP_WINDOW.dismiss();
            POPUP_WINDOW = null;
            LOADING_FLAG = false;
            MainLinearLayout all = (MainLinearLayout) findViewById(R.id.ActivityPanel);
            all.setAlpha(1f);
        }
    }

    private void LoadApplicationDataProcedure() {
        //start loading progress bar
        LOADING_FLAG = true;

        //StartSpinner();

        //StartSpinnerBuiltInThread();
        if (THREAD_HANDLER == null) THREAD_HANDLER = new android.os.Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                //load application settings
                LoadApplicationSettings();


                //re-adjust menu panel, make sure checkout panel is fit perfectly at the bottom
                ReadjustAllPanelHeight();
                //ReadjustMenuPanelComponentSizes();

                //control bindings
                BindCheckoutControl(false);

                //load view info
                LoadViewData();

                //load preferences
                LoadPreferences();

                //start communicating with server
                StartServices();

                //load restaurant table list if any
                LoadRestaurantTableList();


                LOADING_FLAG = false;

                //close the loading popup when is done
                if (POPUP_WINDOW != null) {
                    POPUP_WINDOW.dismiss();
                    POPUP_WINDOW = null;

                    MainLinearLayout all = (MainLinearLayout) findViewById(R.id.ActivityPanel);
                    all.setAlpha(1f);
                    //getActionBar().hide();
                    ReadjustAllPanelHeight();
                    CheckCrashStatus();
                    ReloadLastState();
                    if (myAppSettings.GetAppIsLockedFlag()) LockScreen();
                }
            }
        };

        THREAD_HANDLER.post(r);


    }
    private void RefreshReceipts(String strTableId) {
        common.receiptManager.RefreshReceipts(strTableId);
        CURRENT_SUB_RECEIPT_INDEX=0;
        ListOrders(false,null);
    }
    private boolean IsTableExist(String strTableId)
    {
        for(int i=0;i<drpTable.getAdapter().getCount();i++)
        {
            Duple<String, Duple<String, Boolean>> d =(Duple<String, Duple<String, Boolean>>)drpTable.getAdapter().getItem(i);
            if(strTableId.compareToIgnoreCase(d.GetFirst())==0)return true;
        }

        return false;
    }
    private void ReloadLastState()
    {
        //common.receiptManager.DeleteAllOrders();
        ArrayList<Receipt> receipts = common.receiptManager.GetOrdersFromDB();
        HashMap<Integer,Receipt>receiptCollected = new HashMap<Integer, Receipt>();
        String strCurrentTableId;
        boolean blnAssignedTableId=false;

        while(receipts.size()>0) {
            receiptCollected.clear();
            strCurrentTableId="";
            blnAssignedTableId = false;
            if(!IsTableExist(receipts.get(0).myCart.tableId))
            {
                receipts.remove(0);
                continue;
            }

            for (int i = receipts.size() - 1; i >= 0; i--) {
                Receipt r = receipts.get(i);
                if(!blnAssignedTableId) {
                    blnAssignedTableId = true;
                    strCurrentTableId = r.myCart.tableId;
                }

                if(strCurrentTableId.compareToIgnoreCase(r.myCart.tableId)==0)
                {
                    receipts.remove(i);
                    receiptCollected.put(r.myCart.receiptIndex,r);
                }

            }

            //now insert the collected receipts into the cart manager class
            int intTotalReceiptCount = receiptCollected.keySet().size();
            ArrayList<Receipt> receiptsFromCartManager = common.myCartManager.GetReceipts(strCurrentTableId);
            for(int i=0;i<intTotalReceiptCount;i++)
            {
                if(i==0)
                {
                    receiptsFromCartManager.clear();
                }
                 receiptsFromCartManager.add(receiptCollected.get(i));
            }
        }

        /*MyCartManager mcm = common.Utility.LoadCartManager();
        if(mcm!=null) {
            common.myCartManager =mcm;
            common.myCartManager.RemoveEmptySplitReceipt();
            //update tax
            SaveTax(common.myAppSettings.GetTaxPercentage()+"");
            //set table label 'Default' as selected
            drpTable.setSelection(0);
            ListOrders(false,null);

        }*/
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //do nothing
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        CURRENT_SUB_RECEIPT_INDEX=0;
        DisplayCurrentCartItem();
        view.setBackgroundColor(getResources().getColor(R.color.white));
        ((TextView) (((LinearLayout) view).getChildAt(0))).setTextColor(getResources().getColor(R.color.black));
        //Calculate();
    }

    private void CheckCrashStatus() {
        boolean flag = common.myAppSettings.GetApplicationCrashFlag();
        if (flag) {
            ShowMessage("Status", "Application just recovered from a crash", R.drawable.message);
            /*common.myCartManager = common.Utility.ReloadLastStateFile(this);
            common.myCartManager.RemoveEmptySplitReceipt();
            ListOrders(false);*/
        } else {
            //delete the crash file if existed
            common.Utility.DeleteFile(getString(R.string.crash_filename),this);
        }
    }

    public void OnSave() {
        LoadRestaurantTableList();
    }
    public void imgCurrentPromotion_Click(View v)
    {
        if(blnPromotionComboTopSellPopupShow){CloseAnyNonDialogPopup();return;}
        blnPromotionComboTopSellPopupShow=true;
        llOptionPopup.setVisibility(View.VISIBLE);
        int[] position = new int[2];
        findViewById(R.id.imgCurrentPromotion).getLocationOnScreen(position);
        llOptionPopup.SetTouchPosition(position[0],position[1]);
        PromotionMenuItemContent content = new PromotionMenuItemContent(this,this);
        llOptionPopup.SetListener(this);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        content.SetListener(this);
        findViewById(R.id.llCustomListPageIndex).setVisibility(View.GONE);
        TextView tvOptionPopupTitle=(TextView)findViewById(R.id.tvOptionPopupTitle);
        tvOptionPopupTitle.setText("Promotion");
        TextView tvPromotionNextCheckTime = (TextView)findViewById(R.id.tvPromotionNextCheckTime);
        tvPromotionNextCheckTime.setVisibility(View.VISIBLE);
        tvPromotionNextCheckTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16f);
        tvPromotionNextCheckTime.setText("Next check is " +
        common.Utility.ReturnDateTimeString(Calendar.getInstance().getTimeInMillis() + common.myPromotionManager.GetTimeForPromotionRefresh(false)));
        tvOptionPopupTitle.setVisibility(View.VISIBLE);

        findViewById(R.id.imgClearCustomPage).setVisibility(View.GONE);
        findViewById(R.id.imgEditCustomPageTitle).setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.imgShortcutPanelLogo)).setImageResource(R.drawable.select_promotion);



        ((LinearLayout)llOptionPopup.getChildAt(2)).removeAllViews();
        ((LinearLayout)llOptionPopup.getChildAt(2)).addView(content,lllp);

        llOptionPopup.AnimationShow();
    }
    public void imgCustomList_Click(View v)
    {
        final int totalPage=5;
        final CustomListContent content = new CustomListContent(this,this);
        final ImageView imgEditCustomPageTitle = (ImageView)findViewById(R.id.imgEditCustomPageTitle);
        final LinearLayout llCustomListPageIndex = (LinearLayout)findViewById(R.id.llCustomListPageIndex);
        LinearLayout.LayoutParams lllp;

        if(blnPromotionComboTopSellPopupShow){CloseAnyNonDialogPopup();return;}
        blnPromotionComboTopSellPopupShow=true;
        llOptionPopup.setVisibility(View.VISIBLE);
        int[] position = new int[2];
        findViewById(R.id.imgCustomList).getLocationOnScreen(position);
        llOptionPopup.SetTouchPosition(position[0],position[1]);
        llOptionPopup.SetListener(this);

        TextView tvOptionPopupTitle=(TextView)findViewById(R.id.tvOptionPopupTitle);
        tvOptionPopupTitle.setVisibility(View.GONE);


        llCustomListPageIndex.setVisibility(View.VISIBLE);
        llCustomListPageIndex.removeAllViews();
        for(int i=0;i<totalPage;i++)
        {
            TextView tvPage = new TextView(this);
            tvPage.setText(""+(i+1));
            tvPage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,28);
            if(i!=0){tvPage.setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));}
            if(i==0){tvPage.setTextColor(getResources().getColor(R.color.green));tvPage.setTag(i+1);}
            lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lllp.setMargins(10,0,0,0);
            llCustomListPageIndex.addView(tvPage,lllp);
            tvPage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView tv;
                    if(v.getTag()==null)
                    {
                        for(int i=0;i<llCustomListPageIndex.getChildCount();i++)
                        {
                            tv = (TextView)llCustomListPageIndex.getChildAt(i);
                            tv.setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
                            tv.setTag(null);
                        }
                    }

                    tv = (TextView)v;
                    int newIndex = Integer.parseInt(tv.getText()+"");
                    tv.setTextColor(getResources().getColor(R.color.green));
                    content.GotoPage(newIndex,true);
                    imgEditCustomPageTitle.setTag(newIndex);

                }
            });
        }

        imgEditCustomPageTitle.setTag(1);
        imgEditCustomPageTitle.setVisibility(View.VISIBLE);
        imgEditCustomPageTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pageIndexFromTag = (Integer) imgEditCustomPageTitle.getTag();
                SelectPromotionItemRowDialog dialog = new
                        SelectPromotionItemRowDialog(MainUIActivity.this,MainUIActivity.this
                ,common.customListManager.GetCustomList(pageIndexFromTag).GetSecond()
                        ,pageIndexFromTag
                        ,common.customListManager.GetCustomList(pageIndexFromTag).GetFirst()
                        ,false);
                dialog.show();
            }
        });
        ((ImageView)findViewById(R.id.imgShortcutPanelLogo)).setImageResource(R.drawable.select_shortcut);
        ImageView imgClearCustomPage =(ImageView) findViewById(R.id.imgClearCustomPage);
        imgClearCustomPage.setVisibility(View.VISIBLE);
        imgClearCustomPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for( int i=0;i<llCustomListPageIndex.getChildCount();i++)
                {

                    if(llCustomListPageIndex.getChildAt(i).getTag()!=null)
                    {
                        final int k = i;
                        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainUIActivity.this);
                        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int index) {
                                content.ClearPage(k+1);
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog alert = dialog.create();
                        alert.setTitle("Delete");
                        alert.setMessage("Confirm to delete this custom list page?");
                        alert.setCancelable(false);
                        alert.show();

                        break;
                    }
                }
            }
        });
        ((LinearLayout)llOptionPopup.getChildAt(2)).removeAllViews();
        lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        content.SetListener(this);
        ((LinearLayout)llOptionPopup.getChildAt(2)).addView(content,lllp);

        llOptionPopup.AnimationShow();
    }
    private void LoadRestaurantTableList() {
        drpTable = (Spinner) findViewById(R.id.drpTable);
        drpTable.setOnItemSelectedListener(this);
        //duple<table id, table label>
        Duple<String, String>[] temp = common.floorPlan.GetTableLabels();
        Duple[] tableList = new Duple[temp.length + 1];
        //adding default selection
        //duple<table id, <table label, occupied status>
        tableList[0] = new Duple("", new Duple("Default", true));
        for (int i = 0; i < temp.length; i++) {
            //ShowMessage("construct spinner item",temp[i].GetFirst());
            tableList[i + 1] = new Duple<String, Duple<String, Boolean>>(
                    temp[i].GetFirst(),
                    new Duple<String, Boolean>(temp[i].GetSecond(),
                            (common.myCartManager.GetCart(temp[i].GetFirst(), 0).GetItems().size() > 0) ? true : false));
        }
        //drpTable.setAdapter(new SpinnerBaseAdapter<Duple>(tableList,this));
        drpTable.setAdapter(new TableLabelSpinnerAdapter<Duple<String, Duple<String, Boolean>>>(tableList, this));

    }
    private void StartBluetoothDeviceListener()
    {
        common.myPrinterManager = (common.myPrinterManager==null)?new PrinterManager(this):common.myPrinterManager;
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(common.myPrinterManager, filter1);
        this.registerReceiver(common.myPrinterManager, filter2);
        this.registerReceiver(common.myPrinterManager, filter3);
    }
    public void UpdateGeoLocationService(String strLatitude, String strLongitude) {

        serverMsgListenerFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        registerReceiver(serverMsgReceiver, serverMsgListenerFilter);//start listening

        //start service to check GPS location
        TmeServerUpdateGeoLocation = new Intent(this, TMe_POS_WS.class);
        TmeServerUpdateGeoLocation.setAction(TMe_POS_WS_Receiver.ACTION_UPDATE_GEOLOCATION);
        TmeServerUpdateGeoLocation.putExtra(TMe_POS_WS.PROFILE_ID, myAppSettings.GetDeviceRegisterId());
        TmeServerUpdateGeoLocation.putExtra(TMe_POS_WS.HASHED_PASSWORD, myAppSettings.GetHashedPassword());
        TmeServerUpdateGeoLocation.putExtra(TMe_POS_WS.LATITUDE, strLatitude);
        TmeServerUpdateGeoLocation.putExtra(TMe_POS_WS.LONGITUDE, strLongitude);

        startService(TmeServerUpdateGeoLocation);//start connecting to server for notification
    }
    public void SendResetPasswordLink()
    {
        TmeServerSendPasswordResetLinkIntent = new Intent(this, TMe_POS_WS.class);
        TmeServerSendPasswordResetLinkIntent.setAction(TMe_POS_WS_Receiver.ACTION_SEND_PASSWORD_RESET_LINK);
        startService(TmeServerSendPasswordResetLinkIntent);
    }
    private void StartGetServerNotificationService() {
        //instantiate server notification listener
        IntentFilter serverMsgListenerFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_GET_SERVER_NOTIFICATION);
        serverMsgListenerFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        registerReceiver(serverMsgReceiver, serverMsgListenerFilter);//start listening

        //start service to check for TMe server notification
        TmeServerNotificationIntent = new Intent(this, TMe_POS_WS.class);
        TmeServerNotificationIntent.setAction(TMe_POS_WS_Receiver.ACTION_GET_SERVER_NOTIFICATION);
        startService(TmeServerNotificationIntent);//start connecting to server for notification
    }

   /* private void StartGetExpirationDateService() {
        //instantiate expiration date listener
        IntentFilter serverMsgListenerFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_GET_EXPIRATION_DATE);
        serverMsgListenerFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        registerReceiver(serverMsgReceiver, serverMsgListenerFilter);//start listening

        //start service
        TmeServerExpirationDateIntent = new Intent(this, TMe_POS_WS.class);
        TmeServerExpirationDateIntent.putExtra(TMe_POS_WS.PROFILE_ID_1, myAppSettings.GetDeviceRegisterId1());
        TmeServerExpirationDateIntent.putExtra(TMe_POS_WS.PROFILE_ID_2, myAppSettings.GetDeviceRegisterId2());
        TmeServerExpirationDateIntent.setAction(TMe_POS_WS_Receiver.ACTION_GET_EXPIRATION_DATE);
        startService(TmeServerExpirationDateIntent);//start connecting to server for notification
    }*/
    public void RegisterDevice()
    {
        //start service
        TmeServerRegisterDeviceIntent = new Intent(this, TMe_POS_WS.class);
        TmeServerRegisterDeviceIntent.setAction(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
        startService(TmeServerRegisterDeviceIntent);
    }
    /*private void StartRegisterDeviceService() {
        //instantiate expiration date listener
        IntentFilter serverMsgListenerFilter = new IntentFilter(TMe_POS_WS_Receiver.ACTION_REGISTER_DEVICE);
        serverMsgListenerFilter.addCategory(Intent.CATEGORY_DEFAULT);
        serverMsgReceiver = new TMe_POS_WS_Receiver(this);
        registerReceiver(serverMsgReceiver, serverMsgListenerFilter);//start listening

        RegisterDevice();

    }*/

    private void StartServices() {


        //register the device if there is no ids being found and with internet present only
        if(common.Utility.IsConnectedToNetwork(this)) {
            //check if there is any new server msg
            StartGetServerNotificationService();
           /* if (myAppSettings.GetDeviceRegisterId1().length() == 0) {

                StartRegisterDeviceService();
            } else {
                //else check for new expiration date
                StartGetExpirationDateService();
            }*/
        }


        //start bluetooth listener services
        StartBluetoothDeviceListener();

        //RetrieveLastBackupDate();
    }

    private void LoadApplicationSettings() {
        //character length
        CATEGORY_NAME_MAX_LENGTH = Integer.parseInt(getResources().getString(R.string.category_item_name_max_char));

        SUB_ITEM_NAME_MAX_LENGTH = Integer.parseInt(getResources().getString(R.string.sub_item_name_max_char));

        //invoice item listing per page
        INVOICE_ITEM_PER_PAGE = Integer.parseInt(getResources().getString(R.string.invoice_item_per_page));

        //text size
        TEXT_SIZE = getResources().getDimension(R.dimen.add_category_edit_text_size);
        ADD_MENU_ITEM_TITLE_TEXT_SIZE = getResources().getDimension(R.dimen.dp_add_menu_item_title);
        ADD_MENU_ITEM_TEXT_SIZE = getResources().getDimension(R.dimen.dp_add_menu_item_text);
        SP_MENU_ITEM_TEXT_SIZE = getResources().getDimension(R.dimen.dp_menu_item_text_size);
        ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = getResources().getDimension(R.dimen.dp_add_menu_item_modifier_text);
        DP_SERVER_NOTIFICATION_TEXT_SIZE = getResources().getDimension(R.dimen.dp_notification_text_size);

        //layout params
        INVOICE_ITEM_NAME_WIDTH_WEIGHT = Float.parseFloat(getResources().getString(R.string.invoice_item_name_width_weight));
        INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT = Float.parseFloat(getResources().getString(R.string.invoice_sub_item_name_width_weight));
        INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT = Float.parseFloat(getResources().getString(R.string.invoice_item_unit_price_width_weight));
        INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT = Float.parseFloat(getResources().getString(R.string.invoice_sub_item_price_width_weight));
        INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT = Float.parseFloat(getResources().getString(R.string.invoice_item_total_price_width_weight));
        INVOICE_SUB_ITEM_UNIT_WIDTH_WEIGHT = Float.parseFloat(getResources().getString(R.string.invoice_sub_item_unit_width_weight));

        FONT_ABEL = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.app_font_family));

        //page indicator circle
        PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT = Integer.parseInt(getResources().getString(R.string.page_indicator_circle_view_height));
        PAGE_INDICATOR_CIRCLE_VIEW_WIDTH = Integer.parseInt(getResources().getString(R.string.page_indicator_circle_view_width));

        //max category item
        MAX_CATEGORY_ITEM = Integer.parseInt(getResources().getString(R.string.max_category_item));
        //max item per category
        MAX_ITEM_PER_CATEGORY = Integer.parseInt(getResources().getString(R.string.max_item_per_category));

        //max modifier item allow per category and global shared
        MAX_MODIFIER_ITEM = Integer.parseInt(getResources().getString(R.string.max_modifier_item));
        //hint modifier name
        HINT_MODIFIER_NAME = getResources().getString(R.string.hint_create_modifier_item_in_popup_window);
        //hint modifier price
        HINT_MODIFIER_PRICE = getResources().getString(R.string.label_default_price);

        //padding
        LEFT_RIGHT_PADDING_RECEIPT_ROW = MainUIActivity.DP2Pixel(Float.parseFloat(getResources().getString(R.string.dp_left_right_padding_size_for_row_item)), this);
        TOP_DOWN_PADDING_RECEIPT_ROW = MainUIActivity.DP2Pixel(Float.parseFloat(getResources().getString(R.string.dp_top_bottom_padding_size_for_row_item)), this);
        TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW = DP2Pixel(Float.parseFloat(getString(R.string.dp_top_bottom_padding_size_for_row_sub_item)), this);
        //text size
        DP_RECEIPT_ITEM_MENU_TEXT_SIZE = Float.parseFloat(getResources().getString(R.string.sp_text_size_for_row_item));
        DP_RECEIPT_MODIFIER_TEXT_SIZE = Float.parseFloat(getResources().getString(R.string.sp_text_size_for_row_sub_item));
    }
    private void RestoreDatabase()
    {
        BackupManager bm = new BackupManager(this,null);
        ArrayList<Pair<String,String>> pairs = new ArrayList<Pair<String, String>>();
        //category
        pairs.add(new Pair<String, String>(Schema.DataTable_Category.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Category.TABLE_NAME+".csv"));
        //inventory
        pairs.add(new Pair<String, String>(Schema.DataTable_Inventory.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Inventory.TABLE_NAME+".csv"));
        //item
        pairs.add(new Pair<String, String>(Schema.DataTable_Item.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Item.TABLE_NAME+".csv"));
        //item and modifier update log
        pairs.add(new Pair<String, String>(Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_ItemAndModifierUpdateLog.TABLE_NAME+".csv"));
        //modifier
        pairs.add(new Pair<String, String>(Schema.DataTable_Modifier.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Modifier.TABLE_NAME+".csv"));
        //receipt
        pairs.add(new Pair<String, String>(Schema.DataTable_Receipt.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Receipt.TABLE_NAME+".csv"));
        //supplier
        pairs.add(new Pair<String, String>(Schema.DataTable_Supplier.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Supplier.TABLE_NAME+".csv"));
        //promotion
        pairs.add(new Pair<String, String>(Schema.DataTable_Promotion.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Promotion.TABLE_NAME+".csv"));
        //promotion update log
        pairs.add(new Pair<String, String>(Schema.DataTable_PromotionUpdateLog.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_PromotionUpdateLog.TABLE_NAME+".csv"));
        //server
        pairs.add(new Pair<String, String>(Schema.DataTable_Server.TABLE_NAME,common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION+Schema.DataTable_Server.TABLE_NAME+".csv"));

        bm.RestoreDatabase(pairs);
        bm.CleanUpTempFiles();
    }
    /*private void BackupDatabase()
    {
        try {

             new BackupManager(this).ZipFiles(common.myAppSettings.FILE_EXPORT_LOCATION, "temp.zip");


        }
        catch (Exception ex)
        {
            common.Utility.LogActivity(ex.getMessage());
            common.Utility.ShowMessage("Zip files","Failed to files",this,R.drawable.exclaimation);
        }
    }*/
    public void LoadViewData() {
        float flTax = myAppSettings.GetTaxPercentage();

        SetTaxPercentageLabel(new DecimalFormat("#0.000").format(flTax * 100f) + "");


        //UnzipBackupFile();
        //RestoreDatabase();
        //common.floorPlan.LoadFloorPlan();//need to wait for unzipping

        //myMenu = new MyMenu(this);
        //create fake data
        //myMenu.CreateTestMenuInDB("Beverages");

        //create category item on top
        AddItemCategory(common.myMenu.GetCategoryList());


        ExportAllTableDataToCSV();
        //BackupDatabase();
    }
    private void UnzipBackupFile()
    {
        try {
            new BackupManager(this,null).UnzipFile(common.myAppSettings.FILE_EXPORT_PRIVATE_LOCATION, "temp.zip.gzip");
        }
        catch(Exception ex)
        {
            common.Utility.LogActivity(ex.getMessage());
            common.Utility.ShowMessage("Unzip",ex.getMessage(),this,R.drawable.exclaimation);
        }
    }

    private void ExportAllTableDataToCSV()
    {
        BackupManager bm = new BackupManager(this,null);
        bm.ExportPaymentTypeRecords();
        bm.ExportCategoryRecords();
        bm.ExportItemRecords();
        bm.ExportModifierRecords();
        bm.ExportServerRecords();
        bm.ExportReceiptRecords();
        bm.ExportSupplierRecords();
        bm.ExportInventoryRecords();
        bm.ExportItemModifierUpdateRecords();
        bm.ExportPromotionRecords();
        bm.ExportCustomListRecords();
        bm.ExportPromotionChangeLogRecords();
        bm.ExportOrdersRecord();
        bm.ExportReceiptCountDataRecord();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the contacts-related task you need to do.
                    common.myLocationService.SetPermission(true);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    common.myLocationService.SetPermission(false);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    public void LoadData() {
        myAppSettings = new AppSettings(this);
        common.myAppSettings = myAppSettings;
        common.companyProfile = new CompanyProfile();
        common.UpdateCompanyProfile();//need app settings class and company profile class
        common.floorPlan = new FloorPlan(this, this);
        common.floorPlan.LoadFloorPlan();
        common.text_and_length_settings = new TextLengthAndSize(this);
        common.control_events = new Control_Events(this);
        common.serverList = new ServerList(this);
        common.supplierList = new SupplierList(this);
        common.receiptManager = new ReceiptManager(this);
        common.myPrinterManager = new PrinterManager(this);
        common.myCartManager=new MyCartManager();//common.Utility.LoadLastSavedState(this);
        common.myLocationService = new MyLocationService(this);
        common.inventoryList = new InventoryList(this);
        common.mathLib = new MathLib();
        common.myMenu = new MyMenu(this);
        common.myPromotionManager = new MyPromotionManager(this);
        common.PosCenter = new ServerCommunicator(this);
        common.customListManager = new CustomListManager(this);
    }

    public void HideViewPager() {
        //findViewById(R.id.imgAddNewMenuItem).setVisibility(View.INVISIBLE);
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.MenuItemPager).setVisibility(View.INVISIBLE);
                ((LinearLayout) findViewById(R.id.llPageIndicator)).removeAllViews();
                findViewById(R.id.llItemViewMode).setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        findViewById(R.id.MenuItemSelectionPanel).startAnimation(slide);


    }

    //public void LoadCategorySubMenuItem( long categoryId,long itemId)
    public void LoadCategorySubMenuItem( ArrayList<ItemObject>items,long itemId)
    {

        //ArrayList<ItemObject> items = common.myMenu.GetCategoryItems(categoryId, true);

        int PageCount = 1;
        int PageIndex = 0;

        if (items.size() > 0) {
            PageCount = (int) Math.ceil(Double.parseDouble(items.size() + "") /
                    (currentViewMode== Enum.ViewMode.list?common.text_and_length_settings.ITEM_PER_PAGE_TO_DISPLAY_LIST_MODE:
                            common.text_and_length_settings.ITEM_PER_PAGE_TO_DISPLAY_PIC_MODE));
        }

        if (itemId <0 ) {
            PageIndex = 0;
        } else {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getID() == itemId) {

                    PageIndex = i /
                            (currentViewMode== Enum.ViewMode.list?common.text_and_length_settings.ITEM_PER_PAGE_TO_DISPLAY_LIST_MODE:
                                    common.text_and_length_settings.ITEM_PER_PAGE_TO_DISPLAY_PIC_MODE);//+1 starting from zero index
                    break;
                }
            }
        }
        final SubMenuItemPageChangeListener pageChangeListener = new SubMenuItemPageChangeListener((LinearLayout) findViewById(R.id.llPageIndicator),this);

        mPager = (ViewPager) findViewById(R.id.MenuItemPager);

        mPager.setOffscreenPageLimit(5);//default or smallest value is 1



        mPagerAdapter = new ScreenSlidePagerAdapter(
                getSupportFragmentManager(),
                PageCount,
                CreatePageFragments(PageCount)
               );

        mPager.setAdapter(mPagerAdapter);

        mPager.setOnPageChangeListener(pageChangeListener);

        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        CreateSubMenuItemPageIndicator(PageCount);
        mPager.setVisibility(View.VISIBLE);


        pageChangeListener.onPageSelected(PageIndex);


        //SelectedCategoryId = categoryId;


        LinearLayout misp = (LinearLayout) this.findViewById(R.id.MenuItemSelectionPanel);
        Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_fast);
        misp.startAnimation(slide);

        HidePageItemLoading();

    }
    protected void ShowSearchBarcodeResult(ItemObject io) {

        SetPopupShow(true);


        //create this class will show popup
        StoreItem si = new StoreItem(io);
        si.UnitOrder=1;
        new ItemMenuOptionPopup(this,si,false,common.Utility.GetAtTheMomentItemCount(io.getID()),this,null,-1);







    }
    public ArrayList<PageViewerFragment>CreatePageFragments(int intTotalPages)
    {
        ArrayList<PageViewerFragment>pages = new ArrayList<PageViewerFragment>();
        for(int i=0;i<intTotalPages;i++)
        {
            PageViewerFragment page = new PageViewerFragment();

            page.SetProperties(i + "",
                    (currentViewMode== Enum.ViewMode.list?common.text_and_length_settings.ITEM_PER_PAGE_TO_DISPLAY_LIST_MODE:
                            common.text_and_length_settings.ITEM_PER_PAGE_TO_DISPLAY_PIC_MODE)
                    , this, currentViewMode,SelectedCategoryId);
            pages.add(page);
        }
        return pages;
    }

    public void UpdateInventoryUnitCount(long itemId)//alternate way to keep track of stock availability, CheckoutMoreOptionDialog.Paid()
    {
        //skip update if there isn't menuItemPageFrament object, menu item isn't been displayed
        //scenario #1 recover from crash

        if(mPager==null || SelectedCategoryId==common.text_and_length_settings.PROMOTION_CATEGORY_ID)return;
        //remove existing from memory and recalculate again
        common.inventoryList.RemoveInventoryCount(itemId);

        int unitAvailable = common.inventoryList.GetInventoryCount(itemId);
        unitAvailable -= common.Utility.CalculateOrderedItem(itemId);//menuItemPageFragment.CalculateOrderedItem(itemId);
        unitAvailable = (unitAvailable < 0) ? 0 : unitAvailable;
        //View v = PageViewerFragment.FragmentView;
        ScreenSlidePagerAdapter screenSlidePagerAdapter = ((ScreenSlidePagerAdapter)mPager.getAdapter());
        for(int i=0;i<screenSlidePagerAdapter.getCount();i++)
        {
            PageViewerFragment pageViewerFragment = (PageViewerFragment)screenSlidePagerAdapter.getItem(i);
            for (int j = 0; j < pageViewerFragment.lstTvUnit.size(); j++) {
                if (pageViewerFragment.lstTvUnit.get(j).first == itemId) {
                    pageViewerFragment.lstTvUnit.get(j).second.setText("" + unitAvailable);
                    return;
                }
            }
        }


    }

    private void SetTaxPercentageLabel(String strTaxPercentage) {
        TextView tv = (TextView) findViewById(R.id.lblTaxPercentage);
        tv.setText(Html.fromHtml("Tax @ <font color='#50C108'><u>" + strTaxPercentage + "%</u></font>:"));
    }

    //for create new or update global modifier
    public void SaveGlobalModifiers(ArrayList<ModifierObject> InactiveList, ArrayList<ModifierObject> NewList,boolean blnReloadReceipt) {
        //if operation successful perform removing affected selected cart item
        if (common.myMenu.UpdateModifiers(InactiveList, NewList)) {

            //perform removal by checking deleted modifier list
            ArrayList<Long> ids1 = new ArrayList<Long>();
            if (InactiveList.size() > 0) {

                for (ModifierObject mo : InactiveList) {
                    ids1.add(mo.getID());
                }


            }

            //perform removal by checking updated modifier list
            ArrayList<Long> ids2 = new ArrayList<Long>();
            if (NewList.size() > 0) {

                for (ModifierObject mo : NewList) {
                    ids2.add(mo.getID());
                }

                //RemoveAffectedCartItem(new ArrayList<Long>(), ids);
            }
            if(ids1.size()>0 && ids2.size()>0)
            {
                RemoveAffectedCartItem(new ArrayList<Long>(), ids1,false);
                UpdateAffectedCartItem(new ArrayList<Long>(), ids2,blnReloadReceipt);
            }
            else if(ids1.size()==0 && ids2.size()>0)
            {

                UpdateAffectedCartItem(new ArrayList<Long>(), ids2,blnReloadReceipt);
            }
            else if(ids1.size()>0 && ids2.size()==0)
            {
                RemoveAffectedCartItem(new ArrayList<Long>(), ids1,blnReloadReceipt);
            }
            //RemoveAffectedCartItem(new ArrayList<Long>(), ids1);
            //UpdateAffectedCartItem(new ArrayList<Long>(), ids2);

            if(blnReloadReceipt) {
                PerformMergeSameItemInAllReceipts();
                DismissAddNewItemFragment();
                Toast.makeText(this, "Modifiers saved.", Toast.LENGTH_SHORT).show();
            }
        }


    }
    /*public void SaveIndividualModifiers(ArrayList<ModifierObject>InactiveList,ArrayList<ModifierObject>NewList)
    {
        myMenu.UpdateModifiers(InactiveList,NewList);
    }*/
    public void HidePageItemLoading()
    {
        findViewById(R.id.pbItemPageLoading).setVisibility(View.GONE);
        findViewById(R.id.MenuItemPager).setVisibility(View.VISIBLE);
        findViewById(R.id.llPageIndicator).setVisibility(View.VISIBLE);
        findViewById(R.id.llItemViewMode).setVisibility(View.VISIBLE);
    }
    public void ShowPageItemLoading(final String CategoryId, final StoreItem si)//, final int ItemIndexOnReceipt)
    {
        CloseAnyNonDialogPopup();
        SelectedCategoryId = Long.parseLong(CategoryId);
        findViewById(R.id.pbItemPageLoading).setVisibility(View.VISIBLE);
        findViewById(R.id.llPageIndicator).setVisibility(View.INVISIBLE);
        findViewById(R.id.MenuItemPager).setVisibility(View.INVISIBLE);
        findViewById(R.id.llItemViewMode).setVisibility(View.INVISIBLE);
       /* if(si!=null)
            LoadCategorySubMenuItem(SelectedCategoryId, si.item.getID());
        else
            LoadCategorySubMenuItem(SelectedCategoryId, -1l);*/

        Duple<Long,Long> duple=new Duple<Long, Long>(SelectedCategoryId,-1l);
        LoadMenuItemPageAsyncTask task = new LoadMenuItemPageAsyncTask();
        if(si!=null) {
            duple=new Duple<Long, Long>(SelectedCategoryId,si.item.getID());
            task.execute(duple);
        }
        else
        {
            task.execute(duple);}


    }
    //only updating individual modifier will trigger this method, deletion will have another method to handle
    //so updating item need to pass to UpdateAffectedCartItem() in order to update receipt item
    //do not pass item ids to RemoveAffectedCartItem() else will get remove
    public void UpdateItemAndModifiers(ItemObject item, long lngItemId, long lngCategoryId
            , ArrayList<ModifierObject> InactiveModifiers, ArrayList<ModifierObject> updatedModifiers)
    {

        common.myMenu.UpdateItemAndModifiers(item, lngItemId, InactiveModifiers, updatedModifiers);


        ArrayList<Long>itemIds = new ArrayList<Long>();
        if(item!=null) {
            itemIds.add(item.getID());
        }

        if (InactiveModifiers.size() > 0 ||
                updatedModifiers.size() > 0 ||
                item != null)//update modifier or item will trigger this part to check cart item
        {

            ArrayList<Long> DeletedModifierIds = new ArrayList<Long>();
            ArrayList<Long> UpdatedModifierIds = new ArrayList<Long>();
            ArrayList<Long> ItemIds = new ArrayList<Long>();
            if (item != null && item.getName().length() > 0) ItemIds.add(item.getID());

            for (ModifierObject mo : InactiveModifiers) {
                DeletedModifierIds.add(mo.getID());
            }

            //RemoveAffectedCartItem(new ArrayList<Long>(), DeletedModifierIds);

            for (ModifierObject mo : updatedModifiers) {
                UpdatedModifierIds.add(mo.getID());
            }
            //UpdateAffectedCartItem(ItemIds,UpdatedModifierIds);

            if(DeletedModifierIds.size()>0 && UpdatedModifierIds.size()>0)
            {
                RemoveAffectedCartItem(new ArrayList<Long>(), DeletedModifierIds,false);
                UpdateAffectedCartItem(itemIds, UpdatedModifierIds,true);
            }
            else if(DeletedModifierIds.size()==0 && UpdatedModifierIds.size()>0)
            {

                UpdateAffectedCartItem(itemIds, UpdatedModifierIds,true);
            }
            else if(DeletedModifierIds.size()>0 && UpdatedModifierIds.size()==0)
            {
                RemoveAffectedCartItem(new ArrayList<Long>(), DeletedModifierIds,true);
            }
            else
            {
                //update item object
                UpdateAffectedCartItem(itemIds, new ArrayList<Long>(), true);
            }
        }

        PerformMergeSameItemInAllReceipts();
        DismissAddNewItemFragment();

        //don't load category if the current selected category id is not the same
        //as updated category id(from cart item)
        final MyTopMenuContainer tmc = (MyTopMenuContainer) findViewById(R.id.CategoryContainer);
        if (tmc.selectedChildItem != null) {
            //long SelectedCategoryId = (Long)tmc.selectedChildItem.getTag();
            if (SelectedCategoryId == lngCategoryId) {
                ShowPageItemLoading(SelectedCategoryId + "", null);
                /*//delay showing the new loaded items page, else page won't show
                android.os.Handler h = new android.os.Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ShowPageItemLoading(SelectedCategoryId + "", null, -1);//here
                        //LoadCategorySubMenuItem(SelectedCategoryId + "", null, -1);//here

                    }
                }, 2000);*/

            }
        }

        //LoadCategorySubMenuItem(lngParentId + "",null,-1);
        Toast.makeText(this, "Item saved.", Toast.LENGTH_SHORT).show();
    }

    //create a new item and its modifier
    public boolean SaveNewItemAndModifiers(final ItemObject io, ArrayList<ModifierObject> modifiers)//String strNewItemName,BigDecimal bgPrice,String strPicturePath,final long lgCategoryId,ArrayList<ModifierObject>modifiers)
    {
        long newItemId = common.myMenu.AddItem(io.getName(), io.getPrice(), io.getParentID(),
                io.getPicturePath(),io.getDoNotTrackFlag()
        ,io.getBarcode());//strNewItemName,bgPrice,lgCategoryId,strPicturePath);
        if (newItemId > -1) {

            DismissAddNewItemFragment();


            //insert newly inserted item id as modifier parent id
            for (int i = 0; i < modifiers.size(); i++) {
                //exclude the one that is global
                if (modifiers.get(i).getParentID() != -1)
                    modifiers.get(i).setParentID(newItemId);
            }

            //save modifier if success
            if (common.myMenu.UpdateModifiers(new ArrayList<ModifierObject>(), modifiers)) {
                ShowPageItemLoading(io.getParentID()+"",null);
               /* //delay showing the new loaded items page, else page won't show
                android.os.Handler h = new android.os.Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ShowPageItemLoading(io.getParentID()+"",null,-1);


                    }
                }, 1800);*/
            } else {
                common.Utility.ShowMessage("Add new modifier", "There is a problem while creating new modifier, please try again later.", this, R.drawable.exclaimation);
            }
            Toast.makeText(this, "New item added.", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            common.Utility.ShowMessage("Add menu item", "There is a problem while creating new item, please try again later.", this, R.drawable.exclaimation);
            return false;
        }
    }

    public void DismissAddNewItemFragment() {
//pop the fragment

        //reshow the frameLayout
        final View v = findViewById(R.id.AddNewMenuItemFragmentPlaceholder);
        final MyTopMenuContainer tmc = (MyTopMenuContainer) findViewById(R.id.CategoryContainer);
        if (v == null) {
            //ShowMessage("not found","");
        } else {
            //FrameLayout fl = (FrameLayout)findViewById(R.id.AddNewMenuItemFragmentPlaceholder);

            v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            Animation slide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
            slide.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    getSupportFragmentManager().popBackStack();
                    v.setVisibility(View.GONE);
                    ShowPageItemLoading(SelectedCategoryId+"",null);
                    SetPopupShow(false);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(slide);

        }

    }

    @Override
    public void onBackPressed() {

        if (fpCtr != null) {
            //remove floor plan control
            RemoveFloorPlan();
        }
        else if(getSupportFragmentManager().getBackStackEntryCount()==0)
        {
            //common.Utility.SaveReceiptsObjectIntoJson(this);
            this.finish();
        }
        /*else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //ask user to save before killing and exiting the program
            //if there is item in any receipt
            if(common.myCartManager.AtLeastOneItemInAnyReceipt()) {
                AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
                messageBox.setTitle("Save");
                messageBox.setMessage("Save current receipts before exit?");
                messageBox.setCancelable(false);
                messageBox.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        common.Utility.SaveCartsBeforeExit(MainUIActivity.this);
                        MainUIActivity.this.finish();
                    }
                });
                messageBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainUIActivity.this.finish();
                    }
                });
                messageBox.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.question), getResources(), 36, 36));

                messageBox.show();
            }
            else
            {
                this.finish();
            }


        } */
        else {
            DismissAddNewItemFragment();

        }
    }

    private void RemoveFloorPlan() {
        if (fpCtr == null) return;
        fpCtr.SlideOut(!myAppSettings.SwipeLeftToDelete());
        fpCtr = null;

    }
    private void RemoveLastOrderedItems()
    {
        if(lastInsertedItemsToReceipt.size()>0)
        {
            String strTableId = GetCurrentTableId();
            Enum.GetLockResult getLockResult =common.receiptManager.GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
            if(getLockResult== Enum.GetLockResult.TryLater || getLockResult== Enum.GetLockResult.RecordCountMismatch || getLockResult== Enum.GetLockResult.VersionOutOfDate) {
                common.Utility.ShowMessage("Delete","Someone is modifying the receipt, please try again later.",this,R.drawable.no_access);
                RefreshReceipts(strTableId);
                return;
            }

            for(int i=0;i<lastInsertedItemsToReceipt.size();i++)
            {

                RemoveItemFromCurrentCart(lastInsertedItemsToReceipt.get(i));
                UpdateInventoryUnitCount(lastInsertedItemsToReceipt.get(i).item.getID());

            }
            lastInsertedItemsToReceipt.clear();


            ListOrders(false,null);

           //common.Utility.LogActivity("current cart table id is ["+GetCurrentReceipt().myCart.tableId+"]");
            //ArrayList<Receipt> tempR = common.myCartManager.GetReceipts(GetCurrentReceipt().myCart.tableId);
            //common.Utility.LogActivity("receipt count for this table id is "+tempR.size());
            Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(GetCurrentTableId()),false);

            //unlock records
            common.receiptManager.UnLockRecords(strTableId);

            if(result!= Enum.DBOperationResult.Success) {
                common.Utility.ShowMessage("Receipt","Failed to update receipt, please try again later",this,R.drawable.no_access);
                ReloadLastState();
                ListOrders(false,null);
            }
        }


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
       ((POS_Application) getApplication()).setCurrentActivity(this);



        //check for Bluetooth enabled
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            //Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            blnBluetoothAvailable=false;
        }
        else
        {
            blnBluetoothAvailable=true;
        }



        setContentView(R.layout.layout_activity_main_ui);

        ((ImageView) findViewById(R.id.imgInvoicePreviousPage)).setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.left_arrow), getResources(), 36, 36));
        ((ImageView) findViewById(R.id.imgInvoiceNextPage)).setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.right_arrow), getResources(), 36, 36));
        ReceiptScrollViewGestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){


           // @Override
            //public boolean onSingleTapConfirmed(MotionEvent e) {

            //    return onSingleTapConfirmed(e);
            //}

           // @Override
            //public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //    return super.onScroll(e1, e2, distanceX, distanceY);
            //}

            @Override
            public boolean onFling(final MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(e1==null || e2==null)return false;
                //redisplay the revolver receipt
                //float differY=Math.abs(e1.getY()-e2.getY());
                float differX=Math.abs(e1.getX()-e2.getX());
                //if(differY>30)return false;
                if(differX<50)return false;
                if(Math.abs(velocityX)<2200)return false;
                //common.Utility.ShowMessage("test","velocity x "+velocityX,MainUIActivity.this,R.drawable.message);
                if(e1.getX()<e2.getX())
                {
                    //previous
                    ibtnInvoicePreviousPage_Click(null);

                }
                else
                {
                    //next
                    ibtnInvoiceNextPage_Click(null);

                }
                return true;//mark consumed
            }
        });

        MyScrollView msv =(MyScrollView) findViewById(R.id.svOrderedItem);
        msv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ReceiptScrollViewGestureDetector.onTouchEvent(event);
                return false;
            }
        });
        msv.SetProperties(this);

        //listener to any touch activity
        //findViewById(R.id.rlActivityPanel)
        imgCloseOptionPopup = (ImageView)findViewById(R.id.imgCloseOptionPopup);
        imgCloseOptionPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(blnPromotionComboTopSellPopupShow)
                {
                    if(((LinearLayout)llOptionPopup.getChildAt(2)).getChildAt(0) instanceof PromotionMenuItemContent) {
                        PromotionMenuItemContent content = (PromotionMenuItemContent) ((LinearLayout) llOptionPopup.getChildAt(2)).getChildAt(0);
                        content.StopRefreshTimer();
                    }

                    llOptionPopup.AnimationHide();

                }
            }
        });
        //barcode search
        findViewById(R.id.imgSearchByBarcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseAnyNonDialogPopup();
                BarcodeSearchDialog dialog = new BarcodeSearchDialog(MainUIActivity.this,MainUIActivity.this);
                dialog.show();
            }
        });

        ///undo bar
        UndoBar = (LinearLayout)findViewById(R.id.UndoBar);
        threadUndoBar = new Handler();
        runHideUndoBar = new Runnable() {
            @Override
            public void run() {
                HideUndoBar();
            }
        };

        ((TextView)findViewById(R.id.tvUndo)).setText(Html.fromHtml("<u>UNDO</u>"));
        findViewById(R.id.tvUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(threadUndoBar!=null)
                {
                    threadUndoBar.removeCallbacks(runHideUndoBar);
                }
                HideUndoBar();
                RemoveLastOrderedItems();
            }
        });

        findViewById(R.id.tvHideUndoBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HideUndoBar();
            }
        });
        HideUndoBar();

        //popup windows title banner
        //need to capture the touch even else the pageview behind will consume
        findViewById(R.id.llOptionPopupBanner).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //promotion item panel button
        llOptionPopup=(LinearLayoutWithExpansionAnimation)findViewById(R.id.llOptionPopup);

        //option side bar
        View v =findViewById(R.id.optionChart);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("user click chart settings");
                LaunchIntent(R.id.action_chart_action_bar);
            }
        });
        common.control_events.CreateClickEffect(v);

        v =findViewById(R.id.optionFloorPlan);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("user click floor plan settings");
                LaunchIntent(R.id.action_floor_plan_action_bar);
            }
        });
        common.control_events.CreateClickEffect(v);


        v =findViewById(R.id.optionManagement);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("user click management settings");
                LaunchIntent(R.id.action_resource_management_action_bar);
            }
        });
        common.control_events.CreateClickEffect(v);

        v =findViewById(R.id.optionLock);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                common.Utility.LogActivity("user lock application");
                LaunchIntent(R.id.action_lock_action_bar);
            }
        });
        common.control_events.CreateClickEffect(v);

        v =findViewById(R.id.optionSettings);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("user click application settings");
                LaunchIntent(R.id.action_settings);
            }
        });
        common.control_events.CreateClickEffect(v);

        v =findViewById(R.id.optionPromotion);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("user click promotion settings");
                LaunchIntent(R.id.action_promotion);
            }
        });
        common.control_events.CreateClickEffect(v);

        v =findViewById(R.id.optionMonitor);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.Utility.LogActivity("user click monitor settings");
                LaunchIntent(R.id.action_monitor);
            }
        });
        common.control_events.CreateClickEffect(v);
        //OPTION BAR
        final RelativeLayout optionBar =(RelativeLayout)findViewById(R.id.OptionBar);
        optionBar.setVisibility(View.GONE);
        findViewById(R.id.optionHide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HideRightSideOptionBar();
            }
        });

        findViewById(R.id.imgOptions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CloseAnyNonDialogPopup();
                ShowRightSideOptionBar();
            }
        });


        currentViewMode = Enum.ViewMode.list;
        findViewById(R.id.imgViewListMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseAnyNonDialogPopup();
                ChangeViewMode(Enum.ViewMode.list);
            }
        });
        findViewById(R.id.imgViewPicMode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseAnyNonDialogPopup();
                ChangeViewMode(Enum.ViewMode.pic);
            }
        });

        common.control_events.CreateClickEffect(findViewById(R.id.imgOptions));
        common.control_events.CreateClickEffect(findViewById(R.id.imgFloorPlan));
        common.control_events.CreateClickEffect(findViewById(R.id.imgInvoiceNextPage));
        common.control_events.CreateClickEffect(findViewById(R.id.imgInvoicePreviousPage));
        common.control_events.CreateClickEffect(findViewById(R.id.imgCheckout));
        //common.control_events.CreateClickEffect(findViewById(R.id.imgPrint));
        common.control_events.CreateClickEffect(findViewById(R.id.imgDrawer));
        ImageView imgClearInvoice = (ImageView) findViewById(R.id.imgClearInvoice);
        common.control_events.CreateClickEffect(imgClearInvoice);

        drpTable = (Spinner) findViewById(R.id.drpTable);
        //findViewById(R.id.imgPrint).setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        PrintReceipt();
        //    }
        //});

        findViewById(R.id.imgDrawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                common.myPrinterManager.OpenDrawer();
            }
        });
        //check promotion
        threadCheckPromotion = new Handler();
        runCheckPromotion = new Runnable()
        {
            @Override
            public void run()
            {
                common.Utility.LogActivity("Checking promotion at current time");
                threadCheckPromotion.removeCallbacks(runCheckPromotion);

                if(!blnReceiptControlBusy)
                {
                    blnReceiptControlBusy = true;//set flag

                    //check is there any promotion changes

                    boolean blnSame=false;
                    Duple<Boolean,MyCart> results = GetListingCartAndPaidStatusAlsoUpdateCurrentPageIndex();
                    MyCart myCart=results.GetSecond();
                    if(myCart.blnIsLock || results.GetFirst()) {
                        //already partial paid or locked
                        common.Utility.LogActivity("receipt has been partially paid/locked");
                    }
                    else {
                        ArrayList<PromotionObject> dbPromotions= CombineCombosAndItems(CURRENT_SUB_RECEIPT_INDEX,myCart);
                        currentTablePromotions =(currentTablePromotions==null)?new HashMap<String, ArrayList<PromotionObject>>():currentTablePromotions;
                        ArrayList<PromotionObject> storedPromotions = currentTablePromotions.get(GetCurrentTableId());
                        if(dbPromotions.size()>0 && storedPromotions==null) {
                            blnSame = false;
                        }
                        else if(dbPromotions.size()==storedPromotions.size())
                        {
                            //do compare
                            for(int i=0;i<dbPromotions.size();i++) {
                                blnSame =false;
                                for(int j=0;j<storedPromotions.size();j++) {
                                    if(dbPromotions.get(i).compareTo(storedPromotions.get(j))==0) {
                                        blnSame = true;
                                        break;
                                    }
                                }
                                if(!blnSame)break;
                            }
                        }
                        else {
                            blnSame=false;
                        }

                        if(checkoutPanelDialog!=null && !blnSame)
                        {
                            common.Utility.ShowMessage("Promotion","Promotion list has refreshed, please checkout again to get the updated price.",MainUIActivity.this,R.drawable.message);
                            checkoutPanelDialog.dismiss();
                        }

                        if(!blnSame) {
                            common.Utility.LogActivity("Refreshing receipt items due to promotion changes");
                            ListOrders(false, null);
                        }
                        else {
                            common.Utility.LogActivity("No promotion changes");
                        }
                    }

                }
                else
                {
                    //do nothing
                    common.Utility.LogActivity("abandon check promotion because receipt control is currently busy");
                }

                common.Utility.LogActivity("check promotion again after T "+common.myPromotionManager.GetTimeForPromotionRefresh(true));
                threadCheckPromotion.postDelayed(runCheckPromotion,common.myPromotionManager.GetTimeForPromotionRefresh(true));
            }
        };

        THREAD_HANDLER = new android.os.Handler();


        THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {


                getActionBar().hide();


            }
        });


        //instantiate global objects

        THREAD_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                LoadData();
            }
        });

        //LoadData();
        StartSpinnerBuiltInThread();



    }

    public void StartBluetooth()
    {
        if(blnBluetoothAvailable) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, common.myAppSettings.REQUEST_ENABLE_BT);

            }
        }
    }
    public void PromptToChoosePrinter()
    {
        //prompt user to choose printer
        SelectBluetoothDevice();
    }
    @Override
    public void PrintBalanceReceipt(Receipt r)
    {
        if(blnBluetoothAvailable) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, common.myAppSettings.REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
                if(!common.myPrinterManager.HasConnectedPrinter())
                {
                    //prompt user to choose printer
                    SelectBluetoothDevice();
                }
                else
                {
                    //Toast.makeText(this, "(Main Activity)HasConnectedPrinter ", Toast.LENGTH_SHORT).show();

                    //print receipt
                    if(common.myPrinterManager.PrintReceipt(r)==false)
                    {
                        //prompt for select printer device
                        SelectBluetoothDevice();
                    }
                }

            }
        }
    }

    @Override
    public void CheckoutPanelPrintReceipt(Receipt receipt) {
        PrintReceipt(receipt);
    }

    public void Paid(Receipt receipt)
    {
    String strTableId = GetCurrentTableId();
        //first to get lock on all record having the same table id for this operation
        Enum.GetLockResult result =common.receiptManager.GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
        if(result== Enum.GetLockResult.TryLater || result== Enum.GetLockResult.RecordCountMismatch || result== Enum.GetLockResult.VersionOutOfDate) {
            common.Utility.ShowMessage("Delete","Someone is modifying the receipt, please try again later.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
            return;
        }
        ArrayList<Receipt>receipts = common.myCartManager.GetReceipts(strTableId);
        /*//construct linked receipt string only this is the last receipt to be paid

        int intUnpaidCount =0;
        for(Receipt r:receipts)
        {
            if(!r.blnHasPaid)intUnpaidCount++;
        }
        String strLinkReceipts="";
        for(Receipt r:receipts) {
            if(r.receiptNumber.length()>0)
            strLinkReceipts+=r.receiptNumber+",";
        }
        strLinkReceipts =(strLinkReceipts.length()>0)?strLinkReceipts.substring(0,strLinkReceipts.length()-1):strLinkReceipts;
        receipt.strLinkedReceipts = strLinkReceipts;*/

        long id =common.receiptManager.SaveReceipt(receipt,receipts);
        if(id>0)
        {
            receipt.blnHasPaid = true;
            //set lock flag
            //ArrayList<Receipt> receipts =common.myCartManager.GetReceipts(strTableId);
            for(int i=0;i<receipts.size();i++)
            {
                receipts.get(i).myCart.blnIsLock=true;

            }

            common.Utility.ShowMessage("Paid","Your receipt #"+receipt.receiptNumber,this,R.drawable.message);
        }
        else {
            common.Utility.ShowMessage("Paid","Failed to checkout please try again later.",this,R.drawable.no_access);
        }



        //check is there any unpaid receipt still

            boolean blnNotPaidYet = false;
            //ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(strTableId);
            for(Receipt r:receipts)
            {
                if(!r.blnHasPaid)
                {
                    blnNotPaidYet = true;
                    break;
                }
            }

            if(!blnNotPaidYet)
            {
                //remove all receipts under this table id
                common.myCartManager.RemoveReceipts(strTableId);

            }





        //allow trigger asynchronous display 1st
        ListOrders(false,null);

        //save the new receipt structures
        Enum.DBOperationResult operationResult =common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),false);
        if(operationResult!= Enum.DBOperationResult.Success) {
            common.Utility.ShowMessage("Delete","Failed to clear all items, please try again later.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
            return;
        }

        //unlock records
        common.receiptManager.UnLockRecords(strTableId);
    }
    private void PrintReceipt(Receipt r)
    {
        if(blnBluetoothAvailable) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, common.myAppSettings.REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
                if(!common.myPrinterManager.HasConnectedPrinter())
                {
                    //prompt user to choose printer
                    SelectBluetoothDevice();
                }
                else
                {


                    //print receipt
                    //assign receipt number
                    common.receiptManager.AssignReceiptNumber(r);
                    r.printReceiptType = Enum.PrintReceiptType.total;
                    if(common.myPrinterManager.PrintReceipt(r)==false)
                    {
                        //prompt for select printer device
                        SelectBluetoothDevice();
                    }
                }

            }
        }
    }
    private void CreateSubMenuItemPageIndicator(int count) {

        //final int pageIndex = 0;
        final LinearLayout llPageIndicator = (LinearLayout) findViewById(R.id.llPageIndicator);

        llPageIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {

            PageIndicatorIndexCtr indicator = new PageIndicatorIndexCtr(this, i, mPager);
            //indicator.setLayoutParams(new LinearLayout.LayoutParams(PAGE_INDICATOR_CIRCLE_VIEW_WIDTH, PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT));
            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(DP2Pixel(PAGE_INDICATOR_CIRCLE_VIEW_WIDTH, this), DP2Pixel(PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT, this));
            lllp.setMargins(0,-5,0,0);
            indicator.setLayoutParams(lllp);
            //indicator.setLayoutParams(new LinearLayout.LayoutParams(DP2Pixel(PAGE_INDICATOR_CIRCLE_VIEW_WIDTH, this), DP2Pixel(PAGE_INDICATOR_CIRCLE_VIEW_HEIGHT, this)));
            indicator.setTag(i);
            llPageIndicator.addView(indicator);
            if (i > 0) {
                indicator.UnfillCircle();
            } else {
                indicator.FillCircle();
            }


        }
    }
    public boolean IsBarCodeExists(String strBarcode,long itemId)
    {
        ItemObject io = common.myMenu.SearchItemByBarcode(strBarcode);
        //if from the same item
        if(io==null || io.getID()==itemId)
        {
            return false;
        }
        else
        {
            return true;
        }

    }
    public boolean SearchItemByBarcode(String strBarcode,Dialog dialog)
    {
        common.Utility.LogActivity("search item by barcode "+strBarcode);
        ItemObject io = common.myMenu.SearchItemByBarcode(strBarcode);
        if(dialog!=null) {
            dialog.dismiss();
        }

        if(io==null)
        {
            //make sure is not from add new item triggering this message
            if(dialog!=null) {
                common.Utility.ShowMessage("Search", "No item associated with this barcode", this, R.drawable.message);
            }
            return false;
        }
        //make sure is not from add new item triggering this message
        if(dialog!=null) {
            ShowSearchBarcodeResult(io);
        }
        return true;

    }

    public void StartSpinnerBuiltInThread() {
        if(LOADING_FLAG)return;
        LOADING_FLAG = true;
        //final ActivityLinearLayout all = (ActivityLinearLayout) findViewById(R.id.ActivityPanel);
        final MainLinearLayout all = (MainLinearLayout) findViewById(R.id.ActivityPanel);
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.layout_loading_spinner_popup_window, null);
        popupView.post(new Runnable() {
            @Override
            public void run() {

                POPUP_WINDOW = new PopupWindow(
                        popupView, all.getWidth(), all.getHeight());


                //all.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
                POPUP_WINDOW.setFocusable(false);
                POPUP_WINDOW.setTouchable(true);
                POPUP_WINDOW.setOutsideTouchable(true);
                if (LOADING_FLAG) {
                    all.setAlpha(.1f);
                    POPUP_WINDOW.showAtLocation(popupView, Gravity.NO_GRAVITY, 0, 0);
                }
            }
        });
    }
/*private void StartSpinner()
{
    try {
        //final ActivityLinearLayout all = (ActivityLinearLayout) findViewById(R.id.ActivityPanel);
        final MainLinearLayout all = (MainLinearLayout) findViewById(R.id.ActivityPanel);
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.layout_loading_spinner_popup_window, null);
        POPUP_WINDOW = new PopupWindow(
                popupView, all.getWidth(), all.getHeight());

        all.setAlpha(.1f);
        //all.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
        POPUP_WINDOW.setFocusable(false);
        POPUP_WINDOW.setTouchable(true);
        POPUP_WINDOW.setOutsideTouchable(true);
        POPUP_WINDOW.showAtLocation(popupView, Gravity.NO_GRAVITY, 0, 0);


    }
    catch(Exception ex)
    {
        //ShowErrorMessageBox("StartSpinner",ex);
    }
}*/

    public void RemoveOrderedModifier(StoreItem targetSI, ModifierObject targetMO)
    {
        ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(((Duple<String, Duple<String, Boolean>>) drpTable.getSelectedItem()).GetFirst());
        String strTableId=GetCurrentTableId();
        for(int i=receipts.size()-1;i>-1;i--)
        {
            ArrayList<StoreItem>items = receipts.get(i).myCart.GetItems();
            for(int j=items.size()-1;j>-1;j--)
            {
                StoreItem foundSI =items.get(j);
                if(foundSI.IsSameOrderedItemExcludeUnitCount(targetSI))
                {
                    //further check for modifier
                    for(int k=0;k<foundSI.modifiers.size();k++)
                    {
                        if(foundSI.modifiers.get(k).getID()==targetMO.getID())
                        {
                            foundSI.modifiers.remove(k);
                            PerformMergeSameItemInAllReceipts();

                            Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),true);
                            if(result== Enum.DBOperationResult.VersionOutOfDate || result== Enum.DBOperationResult.TryLater || result== Enum.DBOperationResult.Failed) {
                                common.Utility.ShowMessage("Receipt","Your receipt version is out of date, refreshing now.",this,R.drawable.no_access);
                                RefreshReceipts(strTableId);
                            }
                            return;
                        }
                    }

                }
            }


        }



    }
    private void RemoveItemFromCurrentCart(StoreItem targetSI)
    {
        ArrayList<StoreItem>items=GetCurrentCart().GetItems();
        Receipt receipt =GetCurrentReceipt();
        for(int j=items.size()-1;j>-1;j--)
        {
            if(items.get(j).IsSameOrderedItemExcludeUnitCount(targetSI))
            {
                if(items.get(j).UnitOrder>targetSI.UnitOrder)
                {
                    items.get(j).UnitOrder-=targetSI.UnitOrder;
                }
                else {
                    receipt.myCart.RemoveStoreItem(j);
                }
                break;
            }
        }

        //check whether the receipt is empty after removing the current item
        if(receipt.myCart.GetItems().size()==0)
        {
            common.myCartManager.RemoveEmptySplitReceipt();

        }
    }
    private void SortDescending(ArrayList<Integer>lst)
    {
        boolean blnProcess= true;
        while(blnProcess)
        {
            blnProcess = false;
            for(int i=0;i<lst.size()-1;i++)
            {
                if(lst.get(i)<lst.get(i+1))
                {
                    lst.add(i,lst.get(i+1));
                    lst.remove(i+2);
                    blnProcess= true;
                }
            }
        }
    }
    private void RemoveItemFromOrderedList(ArrayList<Integer>removeLst,HashMap<Long, Integer> diffMap)
    {
        SortDescending(removeLst);
        ArrayList<CartDisplayItem>displayLst=GetCurrentCart().GetDisplayCartItemList();
        ArrayList<StoreItem>lst=GetCurrentCart().GetItems();
        for(int i=0;i<removeLst.size();i++)
        {
            CartDisplayItem d = displayLst.get(removeLst.get(i));
            if(d.cit== Enum.CartItemType.StoreItem)
            {
                StoreItem tempSI = d.si;
                //collecting original unit count
                if(!diffMap.containsKey(tempSI.item.getID()))
                {
                    diffMap.put(tempSI.item.getID(),0);
                }
                diffMap.put(tempSI.item.getID(),diffMap.get(tempSI.item.getID())+tempSI.UnitOrder);

                while(tempSI.UnitOrder>0)
                {
                    for(int j=lst.size()-1;j>-1;j--)
                    {
                        if(tempSI.IsSameOrderedItemExcludeUnitCount(lst.get(j)))
                        {
                            if(tempSI.UnitOrder>lst.get(j).UnitOrder)
                            {
                                tempSI.UnitOrder-=lst.get(j).UnitOrder;
                                lst.remove(j);
                            }
                            else if(tempSI.UnitOrder<lst.get(j).UnitOrder)
                            {
                                lst.get(j).UnitOrder-=tempSI.UnitOrder;
                                tempSI.UnitOrder=0;
                            }
                            else
                            {
                                //same
                                lst.remove(j);
                                tempSI.UnitOrder=0;
                            }

                        }
                    }
                }
            }
        }


    }
    private void RemoveItemFromCurrentCart(long itemId)
    {
        ArrayList<StoreItem>lst=GetCurrentCart().GetItems();
        for(int i=lst.size()-1;i>-1;i--)
        {
            if(lst.get(i).item.getID()==itemId)
            {
                lst.remove(i);
            }
        }

       /* //remove this sub receipt if is empty
        if(lst.size()==0)
        {
            common.myCartManager.RemoveEmptySplitReceipt();
        }*/
    }
    public void RemoveOrderedItem(StoreItem targetSI)
    {
        ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(((Duple<String, Duple<String, Boolean>>) drpTable.getSelectedItem()).GetFirst());
        Receipt receipt = receipts.get(CURRENT_SUB_RECEIPT_INDEX);
        String strTableId = GetCurrentTableId();

        for(int j=receipt.myCart.GetItems().size()-1;j>-1;j--)
        {
            if(receipt.myCart.GetItems().get(j).IsSameOrderedItemExcludeUnitCount(targetSI))
            {
                if(targetSI.UnitOrder>receipt.myCart.GetItems().get(j).UnitOrder)
                {
                    targetSI.UnitOrder-=receipt.myCart.GetItems().get(j).UnitOrder;
                    receipt.myCart.RemoveStoreItem(j);
                }
                else if(targetSI.UnitOrder<receipt.myCart.GetItems().get(j).UnitOrder)
                {
                    receipt.myCart.GetItems().get(j).UnitOrder-=targetSI.UnitOrder;
                    targetSI.UnitOrder=0;
                }
                else
                {
                    targetSI.UnitOrder=0;
                    receipt.myCart.RemoveStoreItem(j);
                }

                if(targetSI.UnitOrder==0)
                {break;}
            }
        }

        Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),true);
        if(result== Enum.DBOperationResult.VersionOutOfDate || result== Enum.DBOperationResult.Failed || result== Enum.DBOperationResult.TryLater) {
            common.Utility.ShowMessage("Receipt","Your receipt version is out of date, refreshing now.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
        }
        //common.receiptManager.SaveOrderIntoDB(receipt);


    }

   /* public void EditExistingCheckCurrentCart( StoreItem currentSI) {
        //update receipt discount
        Receipt r =GetCurrentReceipt();
        //reset if the discount value is greater than the amount
        if(r.myCart.getAmount().floatValue()-r.GetCashValueForDiscount()<=0)
        {
            r.SetDiscountValue(Enum.DiscountType.cash, 0);

        }

        UpdateReceipt(currentSI);
    }*/

    /*private void UpdateReceipt(StoreItem targetSI) {
       *//*if (intTargetCartIndex > -1) {
            RelistUpdatedOrder(intTargetCartIndex);
        } else {
            ListOrders(true);
        }*//*

        ListOrders(true,targetSI);

        //Calculate();
    }*/
    private boolean CheckUnitLimitation(StoreItem si)
    {
        int unitCount =0;
        int totalUnitCount=0;
        boolean blnResult=true;
        ArrayList<StoreItem>items;
        //check  a series of receipt under the same table id, don't allowed to exceed unit count 999
        //else misaligned on UI and on paper receipt
        ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(GetCurrentTableId());
        for(Receipt r:receipts) {
            items = r.myCart.GetItems();
            for(StoreItem tempSi:items) {
                totalUnitCount += tempSi.UnitOrder;
                if(tempSi.item.getID()==si.item.getID())
                    unitCount+=tempSi.UnitOrder;
            }
        }

      /*  MyCart myCart = GetCurrentCart();
       items = myCart.GetItems();
        for (int a = 0; a < items.size(); a++)
        {
            StoreItem cartItem = items.get(a);
            //keeping count of total unit count
            totalUnitCount += cartItem.UnitOrder;
            if (si.IsSameOrderedItemExcludeUnitCount(cartItem)) {
                unitCount += cartItem.UnitOrder;
            }
        }

        */
        unitCount+=si.UnitOrder;
        totalUnitCount +=si.UnitOrder;

        //check total ordered unit cannot exceed application limit
        if(totalUnitCount>AppSettings.TOTAL_UNIT_LIMIT)
        {
            common.Utility.ShowMessage("Limit","You have exceeded the "+AppSettings.TOTAL_UNIT_LIMIT+" total unit in this receipt.",this,R.drawable.no_access);
            blnResult = false;
        }
        if(unitCount>AppSettings.UNIT_LIMIT)
        {
            common.Utility.ShowMessage("Limit","You have exceeded the "+AppSettings.UNIT_LIMIT+" unit for this item.",this,R.drawable.no_access);
            return false;
        }
        return blnResult;
    }
    private StoreItem InsertIntoExistingCart(StoreItem si) {

        //int unitCount =0;
        //int totalUnitCount=0;


        MyCart myCart = GetCurrentCart();// receipts.get(0).myCart;
        //ArrayList<StoreItem>items = myCart.GetItems();
        /*for (int a = 0; a < items.size(); a++)
        {
            StoreItem cartItem = items.get(a);
            //keeping count of total unit count
            totalUnitCount += cartItem.UnitOrder;
            if (si.IsSameOrderedItemExcludeUnitCount(cartItem)) {
                unitCount += cartItem.UnitOrder;
            }
        }

        unitCount+=si.UnitOrder;
        totalUnitCount +=si.UnitOrder;

        //check total ordered unit cannot exceed application limit
        if(totalUnitCount>AppSettings.TOTAL_UNIT_LIMIT)
        {
            common.Utility.ShowMessage("Limit","You have exceeded the "+AppSettings.TOTAL_UNIT_LIMIT+" unit in this receipt.",this,R.drawable.no_access);
            return null;
        }
        if(unitCount>AppSettings.UNIT_LIMIT)
        {
            common.Utility.ShowMessage("Limit","You have exceeded the "+AppSettings.UNIT_LIMIT+" unit for this item.",this,R.drawable.no_access);
            return null;
        }*/

        myCart.AddStoreItem(si);
        return si;


    }
    public int GetCurrentSubReceiptIndex()
    {
        return CURRENT_SUB_RECEIPT_INDEX;
    }
    public void SetCurrentSubReceiptIndex(int newIndex)
    {
        CURRENT_SUB_RECEIPT_INDEX = newIndex;
    }
    public boolean AddToCart(ArrayList<StoreItem>items,boolean blnShowUndoBar)
    {

        //check limitation before insertion
        for(int i=0;i<items.size();i++)
        {
            if(!CheckUnitLimitation(items.get(i)))
            {
                /*HashMap<Long,Boolean>record = new HashMap<Long, Boolean>();
                //refresh the page view inventory count with given item id
                for(int j=0;j<items.size();j++) {
                    if(!record.containsKey(items.get(i).item.getID()))
                    {
                        record.put(items.get(i).item.getID(),true);
                        UpdateInventoryUnitCount(items.get(i).item.getID());
                    }

                }*/
                return false;
            }
        }

        for(int i=0;i<items.size();i++) {
            InsertIntoExistingCart(items.get(i));
        }



        lastInsertedItemsToReceipt = items;
        ListOrders(true,items);

        /**save to db**/
        //revert if failed
        //Enum.DBOperationResult result = common.receiptManager.SaveOrderIntoDB(GetCurrentReceipt());
        Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(GetCurrentReceipt().myCart.tableId),true);
        AddToCartResult(result,blnShowUndoBar);
        return true;
       /* if(result== Enum.DBOperationResult.Failed || result== Enum.DBOperationResult.TryLater)
        {

            common.Utility.ShowMessage("Receipt","Failed to update receipt, please try again later",this,R.drawable.no_access);
        }
        else if(result== Enum.DBOperationResult.VersionOutOfDate) {

            common.Utility.ShowMessage("Receipt","your receive is out of date, refreshing now.",this,R.drawable.no_access);
        }

        if(result!= Enum.DBOperationResult.Success) {
            blnShowUndoBar = false;
            RemoveLastOrderedItems();
        }

        if(blnShowUndoBar)
        {TriggerToShowUndoBar();}*/
    }
    public void AddToCart(StoreItem si,boolean blnShowUndoBar) {

        common.Utility.LogActivity("add item id "+si.item.getID()+" to receipt");

        //check limitation before insertion
        if(!CheckUnitLimitation(si))return;

        //increase existing order count if find a match in current cart item
        StoreItem targetSI = InsertIntoExistingCart(si);

        //exceeded limitation
        if(targetSI==null)return;

        ArrayList<StoreItem>sis = new ArrayList<StoreItem>();
        sis.add(targetSI);
        ListOrders(true,sis);


        lastInsertedItemsToReceipt = new ArrayList<StoreItem>();
        lastInsertedItemsToReceipt.add(si);

        /**save to db**/
        //revert if failed
        //Enum.DBOperationResult result =common.receiptManager.SaveOrderIntoDB(GetCurrentReceipt());
        Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(GetCurrentReceipt().myCart.tableId),true);
        AddToCartResult(result,blnShowUndoBar);
       /* if(common.receiptManager.SaveOrderIntoDB(GetCurrentReceipt())==0)
        {
            blnShowUndoBar = false;
            RemoveLastOrderedItems();
            common.Utility.ShowMessage("Receipt","Failed to update receipt, please try again later",this,R.drawable.no_access);
        }

        if(blnShowUndoBar)
        {TriggerToShowUndoBar();}*/
    }
    public void ReloadLastStateAndListOrders() {
        ReloadLastState();
        ListOrders(false,null);
    }
    private void AddToCartResult(Enum.DBOperationResult result,boolean blnShowUndoBar) {

        if(result== Enum.DBOperationResult.Failed || result== Enum.DBOperationResult.TryLater)
        {

            common.Utility.ShowMessage("Receipt","Failed to update receipt, please try again later",this,R.drawable.no_access);
        }
        else if(result== Enum.DBOperationResult.VersionOutOfDate) {

            common.Utility.ShowMessage("Receipt","your receipt is out of date, refreshing now.",this,R.drawable.no_access);
            ReloadLastState();
            ListOrders(false,null);
        }

        if(result!= Enum.DBOperationResult.Success) {
            blnShowUndoBar = false;
            RemoveLastOrderedItems();
        }

        if(blnShowUndoBar)
        {TriggerToShowUndoBar();}
    }
    private void TriggerToShowUndoBar()
    {
        ShowUndoBar();
        threadUndoBar.postDelayed(runHideUndoBar, 5000);
    }
    private MyCart GetCombineCartCopied( String strTableId) {
        //construct a temporary receipt with combining all the sub receipt together for display
        //MyCart mc = new MyCart(myAppSettings.GetTaxPercentage(),"-1",0);
        MyCart mc = new MyCart(myAppSettings.GetTaxPercentage(),strTableId,0);
        ArrayList<Receipt> copiedReceiptList = new ArrayList<Receipt>();
        ArrayList<Receipt>currentReceiptList = common.myCartManager.GetReceipts(strTableId);
        for(Receipt r:currentReceiptList)
        {
            copiedReceiptList.add((Receipt)r.clone());
        }
        boolean blnSame = false;
        for (int i = 0; i < copiedReceiptList.size(); i++) {

            MyCart tempCart = copiedReceiptList.get(i).myCart;
            ArrayList<StoreItem>tempItems = tempCart.GetItems();
            for (int j = 0; j < tempItems.size(); j++) {
                blnSame = false;
                StoreItem tempSI = tempItems.get(j);
                ArrayList<StoreItem>items = mc.GetItems();
                for (int k = 0; k < items.size(); k++) {
                    StoreItem storedSI = items.get(k);
                    if (storedSI.item.getID() == tempSI.item.getID() && storedSI.modifiers.size() == tempSI.modifiers.size()) {
                        blnSame = true;
                        for (int l = 0; l < storedSI.modifiers.size(); l++) {
                            if (storedSI.modifiers.get(l).getID() != tempSI.modifiers.get(l).getID()) {
                                blnSame = false;
                                break;
                            }
                        }

                        if (blnSame) {
                            mc.UpdateUnitOrder(k,tempSI.UnitOrder);
                        }
                    }
                }
                if (!blnSame) {
                    //create new object
                    mc.AddStoreItem(((StoreItem) tempCart.GetItems().get(j).clone()));
                }
            }


        }




        return mc;
    }

    public MyCart GetCurrentCartCopied() {

        return GetCombineCartCopied(((Duple<String, Duple<String, Boolean>>) drpTable.getSelectedItem()).GetFirst());

    }

    public MyCart GetCurrentCart() {

        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {
            return GetCurrentCartCopied();
        }
        else
        {

            ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(GetCurrentTableId());
            return receipts.get(CURRENT_SUB_RECEIPT_INDEX).myCart;

        }

    }

   /* private int ReturnLastInvoicePageIndex() {

        return (int) Math.ceil((float) GetCurrentCartCopied(false).items.size() / (float) INVOICE_ITEM_PER_PAGE);
    }*/

    private void AddItemCategory(CategoryObject co) {
        final MyTopMenuContainer tmc = (MyTopMenuContainer) findViewById(R.id.CategoryContainer);
        MyCategoryItemView civ = CreateCategoryItemObject(co, tmc);
        boolean blnLoadItem=false;
        int insertCategoryIndex=0;
        int insertDividerIndex=1;
        for(int i=tmc.getChildCount()-1;i>-1;i--)
        {
            if( tmc.getChildAt(i) instanceof MyCategoryItemView && Long.parseLong(tmc.getChildAt(i).getTag().toString())==common.text_and_length_settings.PROMOTION_CATEGORY_ID)
            {
                if(tmc.getChildCount()==3)//current has Promotion and Tap to add option on display with divider in between
                {

                }
                else
                {
                    insertCategoryIndex=tmc.getChildCount()-3;
                    insertDividerIndex =tmc.getChildCount() - 2;
                }
                break;
            }
        }

        tmc.addView(civ,insertCategoryIndex);
        tmc.addView(CreateCategoryItemDivider(tmc), insertDividerIndex);
       /* if (tmc.getChildCount() == 0) {
            tmc.addView(civ);
            tmc.addView(CreateCategoryItemDivider(tmc));
        } else {*/
        //tmc.addView(civ, tmc.getChildCount() - 1);//reserving two spot for promotion and tap to add category views
        //tmc.addView(CreateCategoryItemDivider(tmc), tmc.getChildCount() - 1);
        //}

        if (findViewById(R.id.gvCategory).getVisibility() == View.VISIBLE) {
            AddCategoryItemInGridView();
        }

        if(SelectedCategoryId==-1){
            SelectedCategoryId =co.getID();
            blnLoadItem=true;
        }
        if(SelectedCategoryId!=common.text_and_length_settings.TAP_TO_ADD_CATEGORY_ID) {
            for(int i=tmc.getChildCount()-1;i>-1;i--)
            {
                if( tmc.getChildAt(i) instanceof MyCategoryItemView && Long.parseLong(tmc.getChildAt(i).getTag().toString())==SelectedCategoryId)
                {
                    final MyCategoryItemView child =(MyCategoryItemView)tmc.getChildAt(i);
                    final boolean blnLoad = blnLoadItem;
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    tmc.DrawChildBorder(new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom()), child);
                                    if(blnLoad)ShowPageItemLoading(SelectedCategoryId+"",null);
                                }
                            },500);

                    break;
                }
            }

        }

    }

    private void AddItemCategory(ArrayList<CategoryObject> items) {
        try {
            MyTopMenuContainer tmc = (MyTopMenuContainer) findViewById(R.id.CategoryContainer);
            //int width =0;
            for (int i = 0; i < items.size(); i++) {
                MyCategoryItemView civ = CreateCategoryItemObject(items.get(i), tmc);
                if (i == 0) {//odd number

                    tmc.addView(civ);
                } else {
                    //add a divider before adding item

                    tmc.addView(CreateCategoryItemDivider(tmc));
                    tmc.addView(civ);
                }

            }

            //add a dummy place holder for add category button
            //add a divider before adding item
            if (tmc.getChildCount() > 0) {
                tmc.addView(CreateCategoryItemDivider(tmc));
            }

           /* //promotion category
            MyCategoryItemView promotionCategory = CreateCategoryItemObject(new CategoryObject(common.text_and_length_settings.PROMOTION_CATEGORY_ID,"Promotion"), tmc);
            promotionCategory.setTag(common.text_and_length_settings.PROMOTION_CATEGORY_ID);
            tmc.addView(promotionCategory);
            tmc.addView(CreateCategoryItemDivider(tmc));*/


            tmc.addView(CreateDummyCategoryObject("TAP TO ADD", tmc));


        } catch (Exception ex) {
            //ShowErrorMessageBox("AddItemCategory",ex);
        }
    }

    private LinearLayout CreateCategoryItemDivider(MyTopMenuContainer parent) {
        return (LinearLayout) getLayoutInflater().inflate(R.layout.layout_item_category_divider, parent, false);
    }

    private AddNewCategoryItemView CreateDummyCategoryObject(String str, MyTopMenuContainer parent) {
        String strContent = str;
        strContent = (strContent.length() > CATEGORY_NAME_MAX_LENGTH) ? strContent.substring(0, CATEGORY_NAME_MAX_LENGTH) : strContent;

        AddNewCategoryItemView catItem = (AddNewCategoryItemView) getLayoutInflater().inflate(R.layout.layout_add_item_category_ui,
                parent, false);
        catItem.setText(strContent);
        catItem.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        catItem.setTag(common.text_and_length_settings.TAP_TO_ADD_CATEGORY_ID);
        return catItem;
    }

    private MyCategoryItemView CreateCategoryItemObject(CategoryObject co, MyTopMenuContainer parent) {
        String strContent = co.getName();
        strContent = (strContent.length() > CATEGORY_NAME_MAX_LENGTH) ? strContent.substring(0, CATEGORY_NAME_MAX_LENGTH) : strContent;
        MyCategoryItemView catItem = (MyCategoryItemView) getLayoutInflater().inflate(R.layout.layout_item_category_ui, parent, false);
        catItem.setText(strContent);
        catItem.setTag(co.getID());
        catItem.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.app_font_family)));
        catItem.setTextColor(getResources().getColor(R.color.green));
        return catItem;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_ui, menu);
        return true;
    }

    public void LaunchSupplierManagementWindow() {
        LaunchWindow("supplier");
       /* Intent intentServer = new Intent(this,ResourceManagementFragmentActivity.class);
        intentServer.putExtra("panel","supplier");
        Bundle b = new Bundle();
        b.putString("panel","supplier");
        startActivityForResult(intentServer, 0,b);*/
    }

    public void LaunchServerManagementWindow() {
        LaunchWindow("server");

    }
    private void LaunchAddSupplierWindow()
    {
        Intent intentServer = new Intent(this, ResourceManagementFragmentActivity.class);
        intentServer.putExtra("panel", "supplier");
        intentServer.putExtra("show_add", "add supplier");
        //Bundle b = new Bundle();
        //b.putString("panel","supplier");
        startActivityForResult(intentServer, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);

    }
    private void LaunchWindow(String strWindowName) {

        Intent intentServer = new Intent(this, ResourceManagementFragmentActivity.class);
        intentServer.putExtra("panel", strWindowName);
        //Bundle b = new Bundle();
        //b.putString("panel","supplier");
        startActivityForResult(intentServer, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
    }
    private boolean LaunchIntent(int intentId)
    {
        CloseAnyNonDialogPopup();

        Intent intent;
        switch (intentId) {
            case (R.id.action_settings):
                Intent r = new Intent(this, SettingsPreferenceActivity.class);
                startActivityForResult(r, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);

                break;
            case (R.id.action_close_action_bar):
                getActionBar().hide();
                ReadjustAllPanelHeight();
                //ReadjustMenuPanelComponentSizes();
                //ReadjustCheckoutFragmentPopup();
                break;
            case (R.id.action_resource_management_action_bar):
                intent = new Intent(this, ResourceManagementFragmentActivity.class);// ServerUIActivity.class);
                intent.putExtra("panel", "");
                startActivityForResult(intent, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case (R.id.action_floor_plan_action_bar):
                if (fpCtr != null)//hide the floor plan screen if the user tend to launch floor plan editor
                {
                    //remove floor plan control
                    RemoveFloorPlan();
                }
                intent = new Intent(this, FloorPlanUIActivity.class);
                startActivityForResult(intent, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case (R.id.action_chart_action_bar):
                blnCheckPromotionAfterPromotionFragmentDismiss = true;
                intent = new Intent(this, ChartUIActivity.class);
                startActivityForResult(intent, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case(R.id.action_promotion):
                blnCheckPromotionAfterPromotionFragmentDismiss=true;
                intent = new Intent(this, PromotionUIActivity.class);
                startActivityForResult(intent, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case (R.id.action_lock_action_bar):

                LockApplication();
                break;
            case(R.id.action_monitor):
                intent = new Intent(this, MonitorUIActivity.class);
                startActivityForResult(intent, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            default:

        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return LaunchIntent(item.getItemId());
        /*switch (item.getItemId()) {
            case (R.id.action_settings):
                Intent r = new Intent(this, SettingsPreferenceActivity.class);
                startActivityForResult(r, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);

                break;
            case (R.id.action_close_action_bar):
                getActionBar().hide();
                ReadjustAllPanelHeight();
                //ReadjustMenuPanelComponentSizes();
                //ReadjustCheckoutFragmentPopup();
                break;
            case (R.id.action_resource_management_action_bar):
                Intent intentResource = new Intent(this, ResourceManagementFragmentActivity.class);// ServerUIActivity.class);
                intentResource.putExtra("panel", "");
                startActivityForResult(intentResource, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case (R.id.action_floor_plan_action_bar):
                if (fpCtr != null)//hide the floor plan screen if the user tend to launch floor plan editor
                {
                    //remove floor plan control
                    RemoveFloorPlan();
                }
                Intent intentFloorPlan = new Intent(this, FloorPlanUIActivity.class);
                startActivityForResult(intentFloorPlan, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case (R.id.action_chart_action_bar):
                Intent intentChart = new Intent(this, ChartUIActivity.class);
                startActivityForResult(intentChart, common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID);
                break;
            case (R.id.action_lock_action_bar):

                LockApplication();
                break;
            default:
                return super.onOptionsItemSelected(item);

        }

        return true;*/
    }

    private Receipt GetCurrentReceipt()
    {
        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {
            MyCart cart =GetCombineCartCopied(GetCurrentTableId());
            Receipt r = common.Utility.CreateNewReceiptObject("-1");
            common.Utility.FillInReceiptProperties(r, GetCurrentTableLabel());
            r.myCart = cart;
            return r;
        }
        return common.myCartManager.GetReceipt(GetCurrentTableId(), CURRENT_SUB_RECEIPT_INDEX);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {


            if(common.myAppSettings.LAUNCH_WINDOW_ACTIVITY_ID==requestCode) {
                LoadPreferences();
                //super.onActivityResult(requestCode, resultCode, data);
            }
            else if(common.myAppSettings.REQUEST_ENABLE_BT==requestCode) {
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    SelectBluetoothDevice();
                } else {

                    //do nothing, let the system prompt for bluetooth activation again
                    common.Utility.ShowMessage("Print Receipt","Please turn on Bluetooth to connect to printer.",this,R.drawable.exclaimation);
                }
            }
            else if(common.myAppSettings.REQUEST_CONNECT_DEVICE==requestCode) {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    if (data == null) return;

                    String strAddress = data.getExtras().getString(AppSettings.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strAddress);
                    String strBrand = data.getExtras().getString(AppSettings.PRINTER_BRAND);
                    String strModel = data.getExtras().getString(AppSettings.PRINTER_MODEL);
                    String strPaperWidth = data.getExtras().getString(AppSettings.PRINTER_PAPER_WIDTH);
                    Toast.makeText(this, "(Main Activity)request connection to " + device.getName(), Toast.LENGTH_SHORT).show();
                    //pass in the info to connect to printer
                    common.myPrinterManager.ConnectSelectedPrinter(Integer.parseInt(strPaperWidth.substring(0, 2))
                            , strBrand, strModel, device);

                    //queue receipt for printing later
                    //common.myPrinterManager.QueueReceipt(GetCurrentReceipt());


                }
            }
        else if(AppSettings.INTENT_RECEIPT_QUEUE== requestCode)
            {
                if (resultCode == Activity.RESULT_OK) {


                    Bundle b = data.getExtras();
                    if(b!=null){
                        if(b.containsKey(AppSettings.EXTRA_DELETED_RECEIPT_INDEX))
                        {

                            common.myPrinterManager.RemovedSelectedReceipt(b.getIntArray(AppSettings.EXTRA_DELETED_RECEIPT_INDEX));


                        }

                    }

                }
                common.myPrinterManager.ResumePrintingTask();
            }
        else
            {
                super.onActivityResult(requestCode, resultCode, data);
            }


    }


    private void SelectBluetoothDevice()
    {
        if(mBluetoothAdapter.isEnabled() && blnBluetoothAvailable) {
            //select a printer device
            startActivityForResult(new Intent(MainUIActivity.this, DeviceListActivity.class), common.myAppSettings.REQUEST_CONNECT_DEVICE);
        }
        else
        {
            //bluetooth is still turning on, try to prompt user to select device later
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SelectBluetoothDevice();
                }
            }, 500);
        }
    }
    public void SendReceiptEmail(Receipt r,String strCustomerEmailAddr)
    {
        new SendAsyncEmail(myAppSettings.GetSendReceiptEmail(),
                myAppSettings.GetCompanyProfile_CompanyName()+"- Receipt #"+r.receiptNumber ,
               r.ReturnHTMLContentForEmailReceipt(),
                strCustomerEmailAddr,
                this, this)
                .execute();
    }

    private void LockApplication() {

        //myAppSettings.SaveLoginEmail("");
        //myAppSettings.SaveHashedMethod("");
        //myAppSettings.SaveHashedPassword(common.Utility.HashPassword(5,"12345678",this));
        if (myAppSettings.GetLoginEmail().length() > 0 &&
                myAppSettings.GetHashedPassword().length() > 0 &&
                myAppSettings.GetHashedMethod().length()>0)

        {
            //prompt user to confirm registration password
            new LockScreenUnlockPasswordConfirmationDialog(this,this).show();
        } else {
            common.Utility.ShowMessage("Lock Screen", "You haven't registered your device with us in order to activate this feature.", this, R.drawable.no_access);
        }
    }

    private void UpdateCategoryName(String strId, String strNewName) {

        CategoryObject co = common.myMenu.GetCategory(Long.parseLong(strId));
        if (co != null) {

            co.setName(strNewName);
            MyTopMenuContainer tmc = (MyTopMenuContainer) this.findViewById(R.id.CategoryContainer);

            for (int i = 0; i < tmc.getChildCount(); i++) {
                if (tmc.getChildAt(i) instanceof MyCategoryItemView) {

                    if (tmc.getChildAt(i).getTag().toString().equalsIgnoreCase(strId)) {

                        ((TextView) tmc.getChildAt(i)).setText(strNewName);
                        break;
                    }
                }
            }

        } else {
            ShowMessage("Edit Category Name", "Failed to update name, please try again later.", R.drawable.exclaimation);
        }

    }

    private InputFilter[] CreateMaxLengthFilter(int length) {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(length);
        return filters;
    }


    private void UpdateAffectedCartItem(ArrayList<Long> AffectedItemIds, ArrayList<Long> AffectedModifierIds,boolean blnReloadReceipt)
    {
        //reload the modifier table to get the update version
        if(AffectedItemIds.size()>0) {
            common.myMenu.ReloadModifierTable();
        }

        //iterate the table list to check each cart for the affected item
        Duple<String, String>[] temp = common.floorPlan.GetTableLabels();

        //insert default table as well
        ArrayList<Duple<String, String>> temp2 = new ArrayList<Duple<String, String>>();
        temp2.add(new Duple<String, String>("", "Default"));
        for (int i = 0; i < temp.length; i++) {
            temp2.add(temp[i]);

        }
        temp = temp2.toArray(new Duple[temp2.size()]);
        boolean blnShowAffectedMessage = false;
        for (int k = 0; k < temp.length; k++) {
            ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(temp[k].GetFirst());
            for(int a = 0;a<receipts.size();a++)
            {
                MyCart myCart = receipts.get(a).myCart;
                ArrayList<StoreItem>items = myCart.GetItems();
                int initialSize = items.size();

                //check item list
                for (int i = items.size() - 1; i >= 0; i--) {
                    StoreItem si = items.get(i);
                    for (long id : AffectedItemIds) {
                        if (si.item.getID() == id) {

                            si.item = common.myMenu.GetLatestItem(si.item.getID());

                            break;
                        }
                    }
                }

                //check modifier list
                for (long id : AffectedModifierIds) {
                    for (int i = items.size() - 1; i >= 0; i--) {
                        StoreItem si = items.get(i);
                        for (int j =  si.modifiers.size()-1;j>=0 ;j--) {
                            if (si.modifiers.get(j).getID() == id) {
                                ModifierObject mo =common.myMenu.GetModifier(si.modifiers.get(j).getID());
                                //remove the item if mutual group has been changed
                                if(si.modifiers.get(j).getMutualGroup()!=mo.getMutualGroup())
                                {
                                    si.modifiers.remove(j);
                                }
                                else {
                                    //ModifierObject mo =myMenu.GetModifier(si.modifiers.get(j).getID());
                                    si.modifiers.remove(j);
                                    si.modifiers.add(j, mo);
                                }
                                blnShowAffectedMessage=true;

                                break;
                            }
                        }
                    }
                }


                if (initialSize != items.size()) {
                    blnShowAffectedMessage = true;

                }

            }



        }
        if (blnShowAffectedMessage) {
            ShowMessage("Cart Item", "Affected cart item(s) has/have been updated.", R.drawable.message);
            //Toast.makeText(this,"Affected cart item(s) has/have been updated.",Toast.LENGTH_SHORT);
        }

        //common.Utility.SaveReceiptsObjectIntoJson(this);

        if(blnReloadReceipt) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ListOrders(false,null);
                    //Calculate();
                }
            }, 2500);
        }

    }
    private void RemoveAffectedCartItem(ArrayList<Long> AffectedItemIds, ArrayList<Long> AffectedModifierIds,boolean blnReloadReceipt) {

        //iterate the table list to check each cart for the affected item
        Duple<String, String>[] temp = common.floorPlan.GetTableLabels();
        //insert default table as well
        ArrayList<Duple<String, String>> temp2 = new ArrayList<Duple<String, String>>();
        temp2.add(new Duple<String, String>("", "Default"));
        for (int i = 0; i < temp.length; i++) {
            temp2.add(temp[i]);

        }
        temp = temp2.toArray(new Duple[temp2.size()]);
        boolean blnShowAffectedMessage = false;
        for (int k = 0; k < temp.length; k++) {
            ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(temp[k].GetFirst());
            for(int a = 0;a<receipts.size();a++)
            {
                MyCart myCart = receipts.get(a).myCart;
                ArrayList<StoreItem>items =myCart.GetItems();
                int initialSize = items.size();

                //check item list
                for (int i = items.size() - 1; i >= 0; i--) {
                    StoreItem si = items.get(i);
                    for (long id : AffectedItemIds) {
                        if (si.item.getID() == id) {
                            myCart.RemoveStoreItem(si);
                            //myCart.items.remove(si);
                            break;
                        }
                    }
                }

                //check modifier list
                for (long id : AffectedModifierIds) {
                    for (int i = items.size() - 1; i >= 0; i--) {
                        StoreItem si = items.get(i);
                        for (int j = 0; j < si.modifiers.size(); j++) {
                            if (si.modifiers.get(j).getID() == id) {
                                si.modifiers.remove(j);


                                break;
                            }
                        }
                    }
                }


                if (initialSize != items.size()) {
                    blnShowAffectedMessage = true;

                }

            }



        }
        if (blnShowAffectedMessage) {
            ShowMessage("Cart Item", "Affected cart item(s) has/have been removed.", R.drawable.message);
        }

        if(blnReloadReceipt) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ListOrders(false,null);
                    //Calculate();
                }
            }, 2500);

        }

        //common.Utility.SaveReceiptsObjectIntoJson(this);
    }

    public void DeleteItem(long lngItemId) {
        boolean blnReloadItemList = false;
        ArrayList<ItemObject> Items = common.myMenu.GetCategoryItems();
        for (ItemObject io : Items) {
            if (io.getID() == lngItemId) {

                //need to reload the list
                blnReloadItemList = true;
                break;
            }
        }

        if (common.myMenu.DeleteItem(lngItemId) > 0) {
            //ShowMessage("Delete","Delete item return 1, selected category id "+SelectedCategoryId);
            //reload
            if (blnReloadItemList) {
                ShowPageItemLoading(SelectedCategoryId+"",null);
                /** tap to add button won't show immediately but will respond to click**/
               /*
                android.os.Handler h = new android.os.Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ShowPageItemLoading(SelectedCategoryId+"",null);
                        //LoadCategorySubMenuItem(SelectedCategoryId + "", null, -1);
                    }
                }, 1000);*/

            }

            //check cart
            ArrayList<Long> ItemIds = new ArrayList<Long>();
            ItemIds.add(lngItemId);
            RemoveAffectedCartItem(ItemIds, new ArrayList<Long>(),true);
            PerformMergeSameItemInAllReceipts();
            Toast.makeText(this, "Item deleted.", Toast.LENGTH_SHORT).show();


        }

        DismissAddNewItemFragment();
    }



    private void UncheckAllReceiptOptions(CheckBox[] checkBoxes)
    {
        for(CheckBox chk:checkBoxes)chk.setChecked(false);
    }
    private  void SetupPrintReceiptOption(Dialog dialog)
    {
        final CheckBox chkPrint = (CheckBox) dialog.findViewById(R.id.chkPrintReceiptOption);
        final CheckBox chkEmail = (CheckBox) dialog.findViewById(R.id.chkEmailReceiptOption);
        final CheckBox chkNoReceipt = (CheckBox) dialog.findViewById(R.id.chkNoReceiptOption);
        final CheckBox[] chks = new CheckBox[]{chkPrint,chkEmail,chkNoReceipt};
        boolean blnEmailSetup = ((myAppSettings.GetSendReceiptEmail().length()==0)?false:true);
        //((LinearLayout)dialog.findViewById(R.id.llEmailReceiptOption)).setEnabled(blnEmailSetup);
        chkEmail.setEnabled(blnEmailSetup);
        EditText txtCustomerEmail = (EditText)dialog.findViewById(R.id.txtCustomerEmail);
        txtCustomerEmail.setEnabled(blnEmailSetup);
        if(!blnEmailSetup)txtCustomerEmail.setHint("(Not yet setup)");
        chkPrint.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UncheckAllReceiptOptions(chks);
                ((CheckBox) v).setChecked(true);
                return true;
            }
        });
        chkEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UncheckAllReceiptOptions(chks);
                ((CheckBox) v).setChecked(true);
                return true;
            }
        });
        chkNoReceipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UncheckAllReceiptOptions(chks);
                ((CheckBox) v).setChecked(true);
                return true;
            }
        });
    }
    private boolean CheckSufficientFund(Dialog dialog,BigDecimal bdAmount)
    {
        String strFund=((EditText)dialog.findViewById(R.id.txtAmountReceive)).getText()+"";
        if(strFund.length()>0)
        {
            bdAmount = bdAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
            BigDecimal bdFund = new BigDecimal(strFund.replaceAll("[$,]",""));
            if(bdFund.floatValue()>=bdAmount.floatValue())
            {
                return true;
            }
        }
        return false;
    }
    public void HideUndoBar()
    {
        if(UndoBar.getVisibility()==View.GONE)return;

        if(threadUndoBar!=null)
        {
            threadUndoBar.removeCallbacks(runHideUndoBar);
        }
        TranslateAnimation movementRight = new TranslateAnimation(0f,0f,0,ConvertToPixel(50));
        movementRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                UndoBar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movementRight.setDuration(500);
        UndoBar.startAnimation(movementRight);
    }
    public void ShowUndoBar()
    {


        final TranslateAnimation movementLeft = new TranslateAnimation(0,0,ConvertToPixel(50),0);
        movementLeft.setDuration(500);
        movementLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                UndoBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        UndoBar.startAnimation(movementLeft);
    }
    public void HideRightSideOptionBar()
    {
        final RelativeLayout optionBar=(RelativeLayout)findViewById(R.id.OptionBar);
        TranslateAnimation movementRight = new TranslateAnimation(optionBar.getX(),optionBar.getX()+20,0.0f,0.0f);
        movementRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                optionBar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movementRight.setDuration(500);
        optionBar.startAnimation(movementRight);
    }
    public void ShowRightSideOptionBar()
    {
        final RelativeLayout optionBar=(RelativeLayout)findViewById(R.id.OptionBar);


        final TranslateAnimation movementLeft = new TranslateAnimation(ConvertToPixel(60),0,0.0f,0.0f);
        movementLeft.setDuration(500);
        movementLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                optionBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



        optionBar.startAnimation(movementLeft);
    }
    private void CombineAllSplitReceipts()
    {
        String strTableId = GetCurrentTableId();
        Enum.GetLockResult getLockResult =common.receiptManager.GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
        if(getLockResult== Enum.GetLockResult.TryLater || getLockResult== Enum.GetLockResult.RecordCountMismatch || getLockResult== Enum.GetLockResult.VersionOutOfDate) {
            common.Utility.ShowMessage("Delete","Someone is modifying the receipt, please try again later.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
            return;
        }
        //combine all in cart
        MyCart combinedCart = GetCombineCartCopied(strTableId);
        //remove all the split receipt
        common.myCartManager.RemoveReceipts(strTableId);
        //assign the combined cart to the 1st receipt
        common.myCartManager.GetReceipt(strTableId,0).myCart = combinedCart;
        //common.Utility.SaveReceiptsObjectIntoJson(this);
        CURRENT_SUB_RECEIPT_INDEX=-1;
        ibtnInvoiceNextPage_Click(null);



        Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(GetCurrentTableId()),false);

        //unlock records
        common.receiptManager.UnLockRecords(strTableId);

        if(result!= Enum.DBOperationResult.Success) {
            common.Utility.ShowMessage("Receipt","Failed to update receipt, please try again later",this,R.drawable.no_access);
            ReloadLastState();
            ListOrders(false,null);
        }

        ShowCheckoutPanel();
    }
    private void ShowCheckoutPanel()
    {
        //this will wait until the combine all receipts process completion and do a recalculation
        //before the GetCurrentReceipt grab the before discount total
        //while(blnListingOrder){
        while(blnReceiptControlBusy){
           new Handler().postDelayed(new Runnable() {
               @Override
               public void run() {
                   ShowCheckoutPanel();
               }
           }, 100);
            return;
        }

        if(GetCurrentReceipt().ReturnReceiptTotalAmountWithoutGratuityAndAmountPromotionDiscount().floatValue()>common.myAppSettings.AMOUNT_LIMIT)
        {
            common.Utility.ShowMessage("Checkout","Receipt amount cannot be greater than one million.",this,R.drawable.no_access);
            return;
        }
        if(GetIsPopupShown())return;
        SetPopupShow(true);
        checkoutPanelDialog = new CheckoutPanelDialog(this,GetCurrentReceipt(),this);
        checkoutPanelDialog.show();

    }
    public void CheckoutOption()
    {

        if(GetCurrentCart().GetItems().size()==0) {
            ShowMessage("Checkout", "Cart is empty.", R.drawable.no_access);
            return;
        }
        if(GetCurrentCart().blnIsLock) {
            ShowCheckoutPanel();
            return;
        }
        if(common.myCartManager.GetReceipts(GetCurrentTableId()).size()>1 && !GetCurrentCart().blnIsLock)
        {
            //prompt user about removing all the receipts
            AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
            messageBox.setTitle("Checkout");
            messageBox.setMessage(Html.fromHtml("Combining all split receipts and check out under one?"));
            messageBox.setCancelable(true);
            messageBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CombineAllSplitReceipts();
                }
            });
            messageBox.setNegativeButton("Cancel", null);
            messageBox.setNeutralButton("NO",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ShowCheckoutPanel();
                }
            });
            messageBox.show();
            return;
        }


        ShowCheckoutPanel();

    }









    public void DeleteCategory(MyCategoryItemView item)
    {
        ArrayList<Long>AffectedItemIds = new ArrayList<Long>();
        if (common.myMenu.DeleteCategory(item, AffectedItemIds)>0)
        {
            //check cart item
            RemoveAffectedCartItem(AffectedItemIds, new ArrayList<Long>(),true);
            PerformMergeSameItemInAllReceipts();
            MyTopMenuContainer tmc = (MyTopMenuContainer)findViewById(R.id.CategoryContainer);
            tmc.DeleteCategory(item);
            //ShowMessage(SelectedCategoryId+"",item.getTag()+"");
            //hide menu item fragment
            if(SelectedCategoryId== Long.parseLong(item.getTag().toString()))
            {
                SelectedCategoryId=-1;


                HideViewPager();
            }

            Toast.makeText(this,"Category deleted.",Toast.LENGTH_SHORT).show();
        }
        if (findViewById(R.id.gvCategory).getVisibility()==View.VISIBLE)
        {
            AddCategoryItemInGridView();
        }


        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SetTopCategoryContainerBusyFlag(false);
                SetPopupShow(false);
            }
        }, 100);

    }

    public void CreateCategory(String strText)
    {
        long newID = common.myMenu.AddCategory(strText);
        if(newID==-1)
        {
            ShowMessage("Add New Category","Failed to add new category, please try again later.",R.drawable.exclaimation);
        }
        else
        {
            //add to the top menu bar
            AddItemCategory(new CategoryObject(newID, strText));


        }
    }

    protected void LoadPreferences()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean blnRightHanded =sp.getBoolean(PREFERENCE_LEFT_HANDED_SETTING_KEY,true);
        AppSettings.PanelDesign pd = (blnRightHanded)? AppSettings.PanelDesign.Right_Handed: AppSettings.PanelDesign.Left_Handed;
        SwitchSide(pd);
    }
    private TableLayout CreateReceiptItem(StoreItem si,int itemIndexInCart,Enum.CartItemStatus cis)
    {

        //23 characters max for item name
        Typeface tf = Typeface.createFromAsset(getAssets(), getResources().getString(R.string.app_font_family));
        //text size for regular item
        float flTextSize =Float.parseFloat(getResources().getString(R.string.sp_text_size_for_row_item));//DP2Pixel(Float.parseFloat(getResources().getString(R.string.sp_text_size_for_row_item)),this); //22f;
        //padding for regular item
        int intTopDownPadding=DP2Pixel(Float.parseFloat(getResources().getString(R.string.dp_top_bottom_padding_size_for_row_item)),this);// 15;
        //padding for regular item
        int intLeftRightPadding=DP2Pixel(Float.parseFloat(getResources().getString(R.string.dp_left_right_padding_size_for_row_item)),this);//2;

        //int intMaxChar =INVOICE_ITEM_NAME_MAX_LENGTH;// Integer.parseInt(getResources().getString(R.string.max_char_item_name));
        //row padding for regular item row
        //int intTblRowPaddingSize = DP2Pixel(Float.parseFloat(getString(R.string.dp_padding_size_for_row)),this);
        //text size for sub item name
        float flSubTextSize = Float.parseFloat(getString(R.string.sp_text_size_for_row_sub_item));//DP2Pixel(Float.parseFloat(getString(R.string.sp_text_size_for_row_sub_item)),this);
        //maximum character for sub item name
        int intSubMaxChar = SUB_ITEM_NAME_MAX_LENGTH;// Integer.parseInt(getResources().getString(R.string.max_char_sub_item_name));
        //padding for sub item text view
        int intSubTopDownPadding = DP2Pixel(Float.parseFloat(getString(R.string.dp_top_bottom_padding_size_for_row_sub_item)),this);
        //item text view height
        float flRowItemTxtViewHeight = Float.parseFloat(getResources().getString(R.string.item_name_view_dp_height));
        int intTextViewHeight=DP2Pixel(flRowItemTxtViewHeight,this);// 75;
        //sub item text view height
        float flRowSubItemTxtViewHeight = Float.parseFloat(getResources().getString(R.string.sub_item_name_view_dp_height));
        int intSubTextViewHeight=DP2Pixel(flRowSubItemTxtViewHeight,this);// 55;
        //sub price top margin to align with sub item name
        int intSubPriceMargin =  DP2Pixel(Float.parseFloat(getString(R.string.dp_top_margin_for_sub_item_price)),this);
        //sub price top margin to align with sub item name
        int intSubNameMargin =  DP2Pixel(Float.parseFloat(getString(R.string.dp_top_margin_for_sub_item_name)),this);
        //sub item row height
        int intSubItemRowMinHeight = DP2Pixel(Float.parseFloat(getString(R.string.sub_item_row_dp_min_height)),this);
        TableLayout trContentTable;
        if(si.modifiers.size()>0)
        {
            trContentTable = CreateOrderedItemWithSubItem(si, tf, common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH, flTextSize, intLeftRightPadding,
                    intTopDownPadding, common.text_and_length_settings.RECEIPT_DISPLAY_TABLE_ROW_PADDING, flSubTextSize, intSubMaxChar,
                    intSubTopDownPadding, intTextViewHeight, intSubTextViewHeight,
                    intSubPriceMargin, intSubNameMargin, intSubItemRowMinHeight,itemIndexInCart,cis);

        }
        else
        {
            trContentTable = CreateOrderedItem(si, tf, common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH
                    ,common.text_and_length_settings.RECEIPT_DISPLAY_TABLE_ROW_PADDING,itemIndexInCart,cis);
        }

        return trContentTable;
    }
   /* private void CreateOrderedItemRow(StoreItem si, int intVisible)
    {



            TableLayout trContentTable = CreateReceiptItem(si);


            if(trContentTable!=null)
            {
                LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);


                trContentTable.setVisibility(intVisible);

                ly.addView(trContentTable);
            }



    }*/
    public TextView CreatePromotionDiscountedUnitTextView(Typeface tf,PromotionAwarded promotionAwarded)
    {
        //create table column for price
        TextView txtPrice = new TextView(this);
        txtPrice.setTypeface(tf);
        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT));
        //String strUnit = promotionAwarded.unit+"/"+ promotionAwarded.ShareByHowManyReceipts();
        if(promotionAwarded.promotionObject.GetDiscountType()== Enum.DiscountType.cash) {
            txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(promotionAwarded.GetItemsTotalPriceForThisComboBeforeDiscount()));
        }
        else
        {
            txtPrice.setText(String.format("%.2f",promotionAwarded.promotionObject.GetDiscountValue()*100f)+"%");
        }
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW,LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtPrice;
    }
    public TextView CreatePromotionDiscountTotalPriceTextView(PromotionAwarded pa,Typeface tf)
    {
        //create table column for price
        //double discountPrice=0f;
        TextView txtPrice = new TextView(this);

        txtPrice.setTypeface(tf);
        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT));

        BigDecimal bdDiscount = pa.GetTotalDiscountAwarded((CURRENT_SUB_RECEIPT_INDEX==pa.GetSharedReceiptIndex().get(0))?true:false,CURRENT_SUB_RECEIPT_INDEX);
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(bdDiscount));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW,LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtPrice;
    }
    public TextView CreateTotalPriceTextView(int unit,BigDecimal price,Typeface tf)
    {
        //create table column for price
        TextView txtPrice = new TextView(this);
        txtPrice.setTypeface(tf);
        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, INVOICE_ITEM_TOTAL_PRICE_WIDTH_WEIGHT));
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price.multiply(new BigDecimal(unit))));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW,LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtPrice;
    }
    public TextView CreateUnitPriceTextView(BigDecimal price,Typeface tf)
    {
        //create table column for unit
        TextView txtUnit = new TextView(this);
        txtUnit.setTypeface(tf);
        txtUnit.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, INVOICE_ITEM_UNIT_PRICE_WIDTH_WEIGHT));
        txtUnit.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price));
        //txtUnit.setText("x" + Integer.toString(intCount));
        txtUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);
        //txtUnit.setBackgroundColor(Color.CYAN);
        txtUnit.setGravity(Gravity.RIGHT);
        txtUnit.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW,LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_ROW);
        return txtUnit;
    }
    public TextView CreatePromotionTitleTextView(String strTitle,Typeface tf,int intMaxChar,Html.ImageGetter imgGetter)
    {
        //create table column for promotion title
        TextView txtName = new TextView(this);
        txtName.setTypeface(tf);
        txtName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, INVOICE_ITEM_NAME_WIDTH_WEIGHT));
        txtName.setText(
                Html.fromHtml(
                        ((strTitle.length() > intMaxChar) ? strTitle.substring(0, intMaxChar) : strTitle)
                        ,imgGetter
                        ,null
                )
        );

        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtName.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW, TOP_DOWN_PADDING_RECEIPT_ROW, LEFT_RIGHT_PADDING_RECEIPT_ROW, TOP_DOWN_PADDING_RECEIPT_ROW);

        return txtName;

    }
    public TextView CreateItemNameTextView(int Unit,String strName,Typeface tf,int intMaxChar)
    {
        //create table column for item name
        TextView txtName = new TextView(this);
        txtName.setTypeface(tf);
        txtName.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, INVOICE_ITEM_NAME_WIDTH_WEIGHT));
        txtName.setText(Unit + "x " + ((strName.length() > intMaxChar) ? strName.substring(0, intMaxChar) : strName));

        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtName.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW, TOP_DOWN_PADDING_RECEIPT_ROW, LEFT_RIGHT_PADDING_RECEIPT_ROW, TOP_DOWN_PADDING_RECEIPT_ROW);
        //txtName.setBackgroundColor(Color.YELLOW);
        return txtName;
    }
    public TextView CreateSubItemNameTextView(String strName,Typeface tf,int intMaxChar,int intUnit)
    {


        //create table column for item name
        TextView txtName = new TextView(this);
        txtName.setTypeface(tf);
        txtName.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, INVOICE_SUB_ITEM_NAME_WIDTH_WEIGHT));

        txtName.setText((strName.length() > intMaxChar) ? strName.substring(0, intMaxChar) : strName);
        txtName.setText(intUnit + "x " + txtName.getText());
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtName.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW, TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW, LEFT_RIGHT_PADDING_RECEIPT_ROW, TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW);
        //txtName.setBackgroundColor(Color.YELLOW);
        return txtName;
    }
    public TextView CreateSubUnitPriceTextView(BigDecimal price,Typeface tf)
    {

        //create table column for price
        TextView txtPrice = new TextView(this);
        txtPrice.setTypeface(tf);

        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT));
        //txtPrice.setText(NumberFormat.getCurrencyInstance(java.util.Locale.US).format(price));
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW,LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW);
        //txtPrice.setBackgroundColor(Color.RED);
        return txtPrice;
    }
    public TextView CreateSubTotalPriceTextView(BigDecimal price,Typeface tf,int intUnit)

    {

        //create table column for price
        TextView txtPrice = new TextView(this);
        txtPrice.setTypeface(tf);

        txtPrice.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, INVOICE_SUB_ITEM_PRICE_WIDTH_WEIGHT));
        txtPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(price.multiply(new BigDecimal(intUnit))));
        //txtPrice.setText(NumberFormat.getCurrencyInstance(java.util.Locale.US).format(price.multiply(new BigDecimal(intUnit))));
        txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_RECEIPT_ITEM_MENU_TEXT_SIZE);

        txtPrice.setGravity(Gravity.RIGHT);
        txtPrice.setPadding(LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW,LEFT_RIGHT_PADDING_RECEIPT_ROW,TOP_DOWN_PADDING_RECEIPT_MODIFIER_ROW);
        //txtPrice.setBackgroundColor(Color.GREEN);
        return txtPrice;
    }
    private TableLayout CreateOrderedItemWithSubItem(StoreItem si, Typeface tf, int intMaxChar, float flTextSize,
                                                     int intLeftRightPadding, int intTopDownPadding, int intTblRowPadding,
                                                     float flSubTextSize, int intSubMaxChar, int intSubTopDownPadding,
                                                     int intTextViewHeight, int intSubTextViewHeight,
                                                     int intSubPriceMargin,
                                                     int intSubNameMargin, int intSubItemRowMinHeight,int itemIndexInCart,Enum.CartItemStatus cis)
    {
        try
        {




            //create regular item table properties, will need swap a regular one with the current one when there is no sub ingredient
            RegularOrderItemProperties roip = new RegularOrderItemProperties(si,tf,intMaxChar,flTextSize,intLeftRightPadding,intTopDownPadding,intTblRowPadding);

            //create table column for item name
            TextView txtName =CreateItemNameTextView(si.UnitOrder, si.item.getName(), tf, intMaxChar);
            //txtName.setHeight(intTextViewHeight);

            //create table column for unit price
            TextView txtUnit = CreateUnitPriceTextView(si.item.getPrice(),tf);//txtUnit.setGravity(Gravity.CENTER);
            //txtUnit.setHeight(intTextViewHeight);

            //create table column for price
            TextView txtPrice = CreateTotalPriceTextView(si.UnitOrder,si.item.getPrice(),tf);
            //txtPrice.setHeight(intTextViewHeight);


            //create table
            final TableLayout trContentTable = new TableLayout(this);
            trContentTable.setId(View.generateViewId());


            //create table row layout
            TableRow.LayoutParams tableRowParams=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);

            tableRowParams.setMargins(0, 0, 0, 0);

            //create table row
            final FirstTableRowForMultiIngredients tr = new FirstTableRowForMultiIngredients(this,this,itemIndexInCart,cis);//(mPager==null)?null:((ScreenSlidePagerAdapter)mPager.getAdapter()).pages);
            tr.setPadding(intTblRowPadding, intTblRowPadding, intTblRowPadding, 0);


            tr.setLayoutParams(tableRowParams);

            //adding textviews
            tr.addView(txtName, 0);
            tr.addView(txtUnit,1);
            tr.addView(txtPrice, 2);



            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

            tableParams.setMargins(0,0,0,0);
            trContentTable.addView(tr,tableParams);
            trContentTable.setTag(si);






            for(int i=0;i<si.modifiers.size();i++)
            {
                //create table column for Item
                TextView txtSubName =CreateSubItemNameTextView(si.modifiers.get(i).getName(), tf, intSubMaxChar, si.UnitOrder);
                txtSubName.setTag(si.modifiers.get(i));
                ((TableRow.LayoutParams)txtSubName.getLayoutParams()).setMargins(0,intSubNameMargin,0,0);


                //create table column for unit
                TextView txtSubUnitPrice = CreateSubUnitPriceTextView(si.modifiers.get(i).getPrice(),tf);
                TableRow.LayoutParams subUnitPriceLP=(TableRow.LayoutParams)txtSubUnitPrice.getLayoutParams();
                subUnitPriceLP.setMargins(0,intSubPriceMargin,0,0);
                //txtSubUnit.setBackgroundColor(Color.MAGENTA);

                //create table column for price
                TextView txtSubTotalPrice = CreateSubTotalPriceTextView(si.modifiers.get(i).getPrice(), tf, si.UnitOrder);
                TableRow.LayoutParams subPriceLP=(TableRow.LayoutParams)txtSubTotalPrice.getLayoutParams();
                subPriceLP.setMargins(0,intSubPriceMargin,0,0);




                final NotFirstTableRowForMultiIngredients trSub = new NotFirstTableRowForMultiIngredients(this,intTblRowPadding,si,roip,intSubMaxChar,this,itemIndexInCart,cis);//(mPager==null)?null:((ScreenSlidePagerAdapter)mPager.getAdapter()).pages);
                trSub.setId(View.generateViewId());
                LinearLayout placeHolder = new LinearLayout(this);
                placeHolder.setLayoutParams(new TableRow.LayoutParams(20, TableRow.LayoutParams.MATCH_PARENT));
                trSub.addView(placeHolder);
                trSub.addView(txtSubName);
                trSub.addView(txtSubUnitPrice);
                trSub.addView(txtSubTotalPrice);


                trSub.setMinimumHeight(intSubItemRowMinHeight);



                trSub.setPadding(intTblRowPadding,intTblRowPadding,intTblRowPadding,0);



                trContentTable.addView(trSub,tableParams);

            }


            return trContentTable;
        }
        catch (Exception ex)
        {
            //ShowErrorMessageBox("CreateItem",ex);
        }

        return null;
    }
    /*public TableLayout CreateOrderedItem(RegularOrderItemProperties roip)
    {

        return CreateOrderedItem(roip.si, roip.tf, roip.intMaxChar,  roip.intTblRowPadding);
    }*/

     private TableLayout CreatePromotionItem(PromotionAwarded promotionAwarded, Typeface tf, int intMaxChar, int intTblRowPadding)
    {

        //double intShareBy = promotionAwarded.unit*promotionAwarded.promotionObject.GetDiscountValue()/ promotionAwarded.ShareByHowManyReceipts();
        //create table column for item name
        String strUnit="";
        int intShareBy = promotionAwarded.ShareByHowManyReceipts();
        if(intShareBy==1)
        {

            strUnit = promotionAwarded.unit+"x ";
        }
        else
        {

            strUnit="("+promotionAwarded.unit+"/"+intShareBy+")x ";
        }

        TextView txtName =CreatePromotionTitleTextView(strUnit
                +promotionAwarded.promotionObject.GetTitle()+"",tf,intMaxChar,imgGetter);

        txtName.setTextColor(getResources().getColor(R.color.dark_grey));
        //create table column for unit
        TextView txtUnit = CreatePromotionDiscountedUnitTextView( tf, promotionAwarded);
        txtUnit.setTextColor(getResources().getColor(R.color.dark_grey));
        //create table column for price
        TextView txtPrice = CreatePromotionDiscountTotalPriceTextView(promotionAwarded, tf);
        txtPrice.setTextColor(getResources().getColor(R.color.dark_grey));

        //create table
        TableLayout trContentTable = new TableLayout(this);
        trContentTable.setId(View.generateViewId());
        trContentTable.setTag(promotionAwarded);
        trContentTable.setBackgroundColor(getResources().getColor(R.color.very_light_grey2));

        //create table row
        TableRow.LayoutParams tableRowParams=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);


        TableRow tr = new TableRow(this);
        tr.setPadding(intTblRowPadding, intTblRowPadding, intTblRowPadding, intTblRowPadding);


        tr.setLayoutParams(tableRowParams);

        //adding text view
        //ImageView star = new ImageView(this);
        //star.setBackground(getResources().getDrawable(R.drawable.green_select_promotion));
        //tr.addView(star,new TableRow.LayoutParams(30,30));
        tr.addView(txtName);
        tr.addView(txtUnit);
        tr.addView(txtPrice);

        trContentTable.addView(tr);


        trContentTable.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));




        return trContentTable;

    }


    private TableLayout CreateOrderedItem(StoreItem si, Typeface tf, int intMaxChar, int intTblRowPadding,int itemIndexInCart,Enum.CartItemStatus cis)
    {


            //create table column for item name
            TextView txtName =CreateItemNameTextView(si.UnitOrder,si.item.getName(),tf,intMaxChar);
            //create table column for unit
            TextView txtUnit = CreateUnitPriceTextView(si.item.getPrice(),tf);//txtUnit.setGravity(Gravity.CENTER);
            //create table column for price
            TextView txtPrice = CreateTotalPriceTextView(si.UnitOrder, si.item.getPrice(), tf);//txtPrice.setBackgroundColor(Color.GREEN);

            //create table
            //final TableLayout trContentTable = new TableLayout(this);
            RegularOrderedItemRow trContentTable = new RegularOrderedItemRow(this,this,itemIndexInCart,cis);//(mPager==null)?null: ((ScreenSlidePagerAdapter)mPager.getAdapter()).pages);//menuItemPageFragment);
            trContentTable.setId(View.generateViewId());
            trContentTable.setTag(si);
            //trContentTable.setBackgroundResource(R.drawable.draw_table_row_border);

            //create table row
            TableRow.LayoutParams tableRowParams=new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            //tableRowParams.setMargins(20,20,20,20);

            TableRow tr = new TableRow(this);
            tr.setPadding(intTblRowPadding, intTblRowPadding, intTblRowPadding, intTblRowPadding);

            //tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.setLayoutParams(tableRowParams);

            //adding textviews
            tr.addView(txtName,0);
            tr.addView(txtUnit,1);
            tr.addView(txtPrice,2);

            trContentTable.addView(tr);


            trContentTable.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));




            return trContentTable;

    }
    /*protected void SetOnTouchClearInvoiceEffect(final ImageButton btn)
    {
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn.setBackground(getResources().getDrawable(R.drawable.green_outer_glow_delete_36));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn.setBackground(getResources().getDrawable(R.drawable.green_delete_36));
                }
                return false;
            }
        });
    }*/
    private void RemoveAllSplitReceipts(View view)
    {

        String strTableId = GetCurrentTableId();
        //first to get lock on all record having the same table id for this operation
        Enum.GetLockResult result =common.receiptManager.GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
        if(result== Enum.GetLockResult.TryLater || result== Enum.GetLockResult.RecordCountMismatch || result== Enum.GetLockResult.VersionOutOfDate) {
            common.Utility.ShowMessage("Delete","Someone is modifying the receipt, please try again later.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
            return;
        }

        LinearLayout ll = (LinearLayout)findViewById(R.id.llOrderedItem);
        ll.removeAllViews();
        ArrayList<Long>lstItemId = new ArrayList<Long>();
        HashMap<Long,Integer>removedQuantity = new HashMap<Long, Integer>();
        StoreItem si;
        ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(strTableId);
        for(int i=receipts.size()-1;i>-1;i--)
        {
            //reset flag
            receipts.get(i).blnHasPaid=false;
            receipts.get(i).myCart.blnIsLock = false;
            if(receipts.get(i).myCart.GetItems().size()>0)
            {
                for(int j=0;j<receipts.get(i).myCart.GetItems().size();j++) {
                    si = receipts.get(i).myCart.GetItems().get(j);
                    lstItemId.add(receipts.get(i).myCart.GetItems().get(j).item.getID());
                    if(!removedQuantity.containsKey(si.item.getID()))removedQuantity.put(si.item.getID(),0);
                    removedQuantity.put(si.item.getID(),-si.UnitOrder-removedQuantity.get(si.item.getID()));//refill is negative sign
                }
            }

        }

        //remove all the split receipt
        common.myCartManager.RemoveReceipts(strTableId);
        ListOrders(false,null);
        //save the new receipt structures
        Enum.DBOperationResult operationResult =common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),false);
        if(operationResult!= Enum.DBOperationResult.Success) {
            common.Utility.ShowMessage("Delete","Failed to clear all items, please try again later.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
            return;
        }

        //unlock records
        common.receiptManager.UnLockRecords(strTableId);
        //Calculate();




       /* //update inventory
        int size =GetCurrentReceipt().myCart.GetItems().size();
        for(int i=0;i<size;i++) {
            UpdateInventoryUnitCount(GetCurrentReceipt().myCart.GetItems().get(i).item.getID());
        }*/


        if(view!=null) {
            Toast.makeText(this, "Cleared.", Toast.LENGTH_SHORT).show();
            UpdateAffectedItemInventories(removedQuantity);//re-add it back if is clear by removing
        }
        else
            Toast.makeText(this,"Paid.",Toast.LENGTH_SHORT).show();
    }

    private void RemoveAllCurrentReceiptItems(View view)
    {
         String strTableId = GetCurrentTableId();
        //first to get lock on all record having the same table id for this operation
        Enum.GetLockResult result =common.receiptManager.GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
        if(result== Enum.GetLockResult.TryLater || result== Enum.GetLockResult.RecordCountMismatch || result== Enum.GetLockResult.VersionOutOfDate) {
            common.Utility.ShowMessage("Delete","Someone is modifying the receipt, please try again later.",this,R.drawable.no_access);
            RefreshReceipts(strTableId);
            return;
        }
        //need to store the item ids before clearing it, to update the inventory count later
        //cannot get an accurate count before removing the items on the list

        Receipt receipt = GetCurrentReceipt();
        ArrayList<Long>lstItemId = new ArrayList<Long>();
        HashMap<Long,Integer>removedQuantity = new HashMap<Long, Integer>();
        StoreItem si;
        for(int j=0;j<receipt.myCart.GetItems().size();j++) {
            si = receipt.myCart.GetItems().get(j);
            lstItemId.add(si.item.getID());
            if(!removedQuantity.containsKey(si.item.getID()))removedQuantity.put(si.item.getID(),0);
            removedQuantity.put(si.item.getID(),-si.UnitOrder-removedQuantity.get(si.item.getID()));//refill is negative sign
        }

        //delete all in memory before clearing the items in db
        GetCurrentReceipt().myCart.RemoveAllStoreItems();
        //common.receiptManager.SaveOrderIntoDB(GetCurrentReceipt());
        LinearLayout ll = (LinearLayout)findViewById(R.id.llOrderedItem);
        ll.removeAllViews();

        //reset flag
        receipt.myCart.blnIsLock = false;
        receipt.blnHasPaid = false;

        int totalReceiptCount=common.myCartManager.GetReceipts(strTableId).size();
        if(receipt.myCart.receiptIndex==0 && totalReceiptCount>1)
        {
            //unlock records, then let the method trigger lock again when go to next page
            //this will perform save operation as well
            common.receiptManager.UnLockRecords(GetCurrentCart().tableId);
            ibtnInvoiceNextPage_Click(null);
        }
        else {
            //receipt.Version=1;//only reset it when you paid every single receipt for this table
            ListOrders(false, null);

            //save the new receipt structures
            Enum.DBOperationResult operationResult =common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),false);
            if(operationResult!= Enum.DBOperationResult.Success) {
                common.Utility.ShowMessage("Delete","Failed to clear all items, please try again later.",this,R.drawable.no_access);
                RefreshReceipts(strTableId);
                return;
            }

            //unlock records
            common.receiptManager.UnLockRecords(strTableId);
        }





        if(view!=null) {
            Toast.makeText(this, "Cleared.", Toast.LENGTH_SHORT).show();
            UpdateAffectedItemInventories(removedQuantity);//re-add it back if is clear by removing
        }
        else {
            Toast.makeText(this, "Paid.", Toast.LENGTH_SHORT).show();
        }
    }
    public void btnRemoveAllOrders_Click(final View view)
    {
        HideUndoBar();
        CloseAnyNonDialogPopup();

        boolean blnHasPaid = false;
        ArrayList<Receipt> receipts =common.myCartManager.GetReceipts(GetCurrentTableId());
        for(int i=0;i<receipts.size();i++)
        {
            if(receipts.get(i).blnHasPaid)
            {
                blnHasPaid = true;
                break;
            }
        }
        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {

            if(blnHasPaid)
            {
                common.Utility.ShowMessage("Remove all items","You cannot remove until all split receipts have been paid.",MainUIActivity.this,R.drawable.no_access);
                return;
            }
            //prompt user about removing all the receipts
            AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
            messageBox.setTitle("Remove all receipts?");
            messageBox.setMessage(Html.fromHtml("Do you want to remove all the split receipts?"));
            messageBox.setCancelable(true);
            messageBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    RemoveAllSplitReceipts(view);
                }
            });
            messageBox.setNegativeButton("Cancel", null);
            messageBox.show();
            return;
        }
        else {
            //prompt user before deleting
            if (GetCurrentReceipt().myCart.GetItems().size() > 0) {
                if(blnHasPaid)
                {
                    common.Utility.ShowMessage("Remove all items","You cannot remove until all split receipts have been paid.",MainUIActivity.this,R.drawable.no_access);
                }
                else
                {
                    AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
                    messageBox.setTitle("Remove all items?");
                    messageBox.setMessage(Html.fromHtml("Do you want to remove all the items?"));
                    messageBox.setCancelable(true);
                    messageBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            RemoveAllCurrentReceiptItems(view);
                        }
                    });
                    messageBox.setNegativeButton("Cancel", null);
                    messageBox.show();
                    return;
                }

            }


        }



    }


    public void Calculate()
    {
        MyCart tempMyCart;
        boolean blnHasPaid = false;
        String strReceiptNumber="";
        if(CURRENT_SUB_RECEIPT_INDEX==-1) {
            tempMyCart = GetCurrentCartCopied();
            tempMyCart.receiptIndex=-1;
            ArrayList<Receipt> copiedReceiptList = new ArrayList<Receipt>();
            ArrayList<Receipt>currentReceiptList = common.myCartManager.GetReceipts(GetCurrentTableId());
            for(Receipt r:currentReceiptList)
            {
                copiedReceiptList.add((Receipt)r.clone());
            }
            //ArrayList<PromotionAwarded> promotionAwardedOffered = common.myPromotionManager.CheckDiscountByItemPromotions(common.myCartManager.GetReceipts(tempMyCart.tableId), -1,tempMyCart);
            ArrayList<PromotionAwarded> promotionAwardedOffered = common.myPromotionManager.CheckDiscountByItemPromotions(copiedReceiptList, -1,tempMyCart);
            for(PromotionAwarded pa:promotionAwardedOffered)
            {
                tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(pa,null, Enum.CartItemType.PromotionAwarded));
            }
            //String strTemp ="";
            //tempMyCart.GetDisplayCartItemList().add(promotionAwardedOffered);
        }
        else
        {
            Receipt tempR = GetCurrentReceipt();
            tempMyCart = GetCurrentCart();
            blnHasPaid = tempR.blnHasPaid;
            strReceiptNumber=(tempR.receiptNumber.length()>0)?" #"+tempR.receiptNumber:"";
        }
        TextView txtAmount = (TextView)findViewById(R.id.txtAmount);
        TextView txtTax = (TextView)findViewById(R.id.txtTax);
        TextView txtTotal = (TextView)findViewById(R.id.txtTotal);

        txtAmount.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(tempMyCart.getAmount()));
        txtTax.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(tempMyCart.getTaxAmount()));
        ImageView imgReceiptStatus = (ImageView)findViewById(R.id.imgReceiptStatus);

        if(blnHasPaid)
        {
            imgReceiptStatus.setVisibility(View.VISIBLE);
            imgReceiptStatus.setBackground(this.getResources().getDrawable(R.drawable.ok));
        }
        else if(tempMyCart.blnIsLock)
        {
            imgReceiptStatus.setVisibility(View.VISIBLE);
            imgReceiptStatus.setBackground(this.getResources().getDrawable(R.drawable.lock));
        }
        else
        {
            imgReceiptStatus.setVisibility(View.GONE);
            imgReceiptStatus.setBackground(null);
        }
        txtTotal.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(tempMyCart.getTotal())
                +strReceiptNumber);


        Toast.makeText(getBaseContext(),"Total updated.",Toast.LENGTH_SHORT).show();
    }

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

    public static int DP2Pixel(float dp,Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (metrics.density * dp + 0.5f);
    }


    public static float Pixel2DP(float px,Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dp =(float)(px/metrics.density);
        return dp;
    }

    public void SwitchSide(AppSettings.PanelDesign pd)
    {
        LinearLayout InvoicePanel = (LinearLayout)findViewById(R.id.InvoicePanel);
        LinearLayout MenuPanel = (LinearLayout)findViewById(R.id.MenuPanel);
        LinearLayout ParentPanel = (LinearLayout) findViewById(R.id.ActivityPanel2);
        if(pd== AppSettings.PanelDesign.Right_Handed)
        {
            //Menu->divider->invoice
            ParentPanel.removeView(InvoicePanel);
            ParentPanel.removeView(MenuPanel);
            ParentPanel.addView(MenuPanel,0);
            ParentPanel.addView(InvoicePanel);
        }
        else
        {
            //invoice->divider->Menu (original layout)
            ParentPanel.removeView(InvoicePanel);
            ParentPanel.removeView(MenuPanel);
            ParentPanel.addView(MenuPanel);
            ParentPanel.addView(InvoicePanel,0);
        }
    }

    @Override
    public void OnTableTouched(String strTableId) {
        //ShowMessage("table selected",strTableId);
        for(int i=0;i<drpTable.getAdapter().getCount();i++) {
            //String strLoop = ((String)((LinearLayout)(drpTable.getChildAt(i))).getChildAt(0).getTag());
            String strLoop = ((String)(drpTable.getAdapter().getView(i,null,null)).getTag());
            //String strLoop = ((TextView)((LinearLayout)(drpTable.getAdapter().getView(i,null,null))).getChildAt(0)).getText()+"";
            //ShowMessage("compare","loop="+strLoop+" vs selected="+strTableId);
            if(strLoop.equalsIgnoreCase(strTableId))
            {
                drpTable.setSelection(i,true);
                break;
            }
        }
        RemoveFloorPlan();
        //((LinearLayout)findViewById(R.id.ActivityPanel)).removeView(fpCtr);
        //fpCtr=null;

    }
    @Override
    public boolean IsReceiptPanelBusy(){return GetIsReceiptControlBusy();}
    @Override
    public int AddNewUnitToCart(long itemId, int unit,int initUnitCount) {
        //doesn't allowed add if is combined receipt view
        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {
            common.Utility.ShowMessage("Add", "Please go to the receipt you want to add this item, currently this is combined view.", this, R.drawable.no_access);
            return initUnitCount;
        }
        if(blnPromotionComboTopSellPopupShow)
        {
            CloseAnyNonDialogPopup();
            return initUnitCount;
        }
        if(GetCurrentCart().blnIsLock)
        {
            common.Utility.ShowMessage("Add", "Cannot add item to this receipt once it/split receipt has been paid.", this, R.drawable.no_access);
            return initUnitCount;
        }
        ItemObject io = common.myMenu.GetLatestItem(itemId);
        if(!io.getDoNotTrackFlag()) {
            int count = common.Utility.GetAtTheMomentItemCount(itemId);
            if (count - unit<0)
            {
                common.Utility.ShowMessage("Inventory", io.getName()+" is currently out of stock.", this, R.drawable.no_access);
                return initUnitCount;
            }
        }
        StoreItem si =new StoreItem(io);
        si.UnitOrder=1;
        AddToCart(si,true);
        return initUnitCount-unit;
    }

    @Override
    public void ShowItemOptionPopup(long itemId,IToBeUpdatedInventoryView view,int inventoryCount) {
        if(GetIsPopupShown()){

            return;
        }
        SetPopupShow(true);
        StoreItem si = new StoreItem(common.myMenu.GetLatestItem(itemId));

        /**do not check inventory here,  this will restricted user for updating item properties**/
       /* //check if there is inventory tracking only
        if(inventoryCount<1 && !si.item.getDoNotTrackFlag())
        {
            common.Utility.ShowMessage("Inventory", si.item.getName()+" is currently out of stock.", this, R.drawable.no_access);
            SetPopupShow(false);
            return;
        }*/

        si.UnitOrder=1;
        new ItemMenuOptionPopup(this,si,false,inventoryCount,this,view,-1);
    }

    @Override
    public void AddNewItem() {
        imgBtnAddNewMenuItem_Click(null,false,null);
    }

    @Override
    public int ShowItemInventoryOption(long itemId, IItemViewUpdateUnit callback) {
        ItemInventoryOptionDialog dialog = new ItemInventoryOptionDialog(this,itemId,this,callback);
        dialog.show();
        return 0;

    }

    @Override
    public void LaunchSupplierOption(ItemInventoryOptionDialog dialog) {
        itemInventoryOptionDialog = dialog;
        LaunchAddSupplierWindow();
        //LaunchSupplierManagementWindow();
    }

    @Override
    public void UpdateCurrentCart(ArrayList<StoreItem> lst,ArrayList<Integer>originalOrderLstIndexes) {
        //remove any existing same item, and collect their original unit order
        HashMap<Long, Integer> diffMap = new HashMap<Long, Integer>();
        RemoveItemFromOrderedList(originalOrderLstIndexes,diffMap);

        //calculate the differences
        for(int i=0;i<lst.size();i++)
        {
            if(diffMap.containsKey(lst.get(i).item.getID()))
            {
                diffMap.put(lst.get(i).item.getID(),(diffMap.get(lst.get(i).item.getID())-lst.get(i).UnitOrder)*-1);
            }
        }
        AddToCart(lst,false);

        UpdateAffectedItemInventories(diffMap);

    }

    @Override
    public void InsertNewItemIntoCurrentCart(ArrayList<StoreItem> lst,IToBeUpdatedInventoryView callbackView) {
        if(blnReceiptControlBusy)return;
        if(!AddToCart(lst,true))return;

        if(callbackView!=null)
        {
            //from single item row/pic click
            callbackView.ItemMenuDialogUnitAdded(lst.get(0).UnitOrder);
        }
        else {
            //from promotion or barcode search
            HashMap<Long, Integer> map = new HashMap<Long, Integer>();
            for (StoreItem si : lst) {
                if (!map.containsKey(si.item.getID())) {
                    map.put(si.item.getID(), 0);
                }
                map.put(si.item.getID(), map.get(si.item.getID()) + si.UnitOrder);
            }
            UpdateAffectedItemInventories(map);
        }
    }

    @Override
    public void DialogDismissed() {
        SetPopupShow(false);
    }

    @Override
    public void EditItem(ItemObject io) {
        imgBtnAddNewMenuItem_Click(null,true,io);
    }

    @Override
    public void SetDialogPopupFlag(boolean blnIsShow) {
        CloseAnyNonDialogPopup();
        blnPopupShow = blnIsShow;
    }

    public void UpdateAffectedItemInventories(HashMap<Long,Integer>newlyInsertedMap)
    {
        ArrayList<PageViewerFragment>fragments =(mPagerAdapter!=null)?((ScreenSlidePagerAdapter)mPagerAdapter).pages:null;
        if(fragments==null)return;

            for(int i=0;i<fragments.size();i++)
            {

                fragments.get(i).UpdateInventoryCount(newlyInsertedMap);


            }



    }

    @Override
    public void ContentUpdated() {
        //llOptionPopup.AnimationShow();
        llOptionPopup.setY(llOptionPopup.lastY);
        llOptionPopup.setX(llOptionPopup.lastX);
    }

    @Override
    public void ShrinkAnimationCompleted() {

        blnPromotionComboTopSellPopupShow = false;
    }

    @Override
    public void PromotionItemSingleTap(long promotionId) {
        //imgCloseOptionPopup.callOnClick();
        CloseAnyNonDialogPopup();
        HashMap<Long,Integer>inventoryCount = new HashMap<Long, Integer>();
        ArrayList<StoreItem>lst = new ArrayList<StoreItem>();
        StoreItem si;
        boolean blnMoreThanOneItemInAGroup=false;
        int count=0;
        PromotionObject po = common.myPromotionManager.Get(promotionId);
        //single tap will launch edit item panel if more than one item in one group
        for(int i=0;i<po.ruleItems.size();i++)
        {
            if(po.ruleItems.get(i).size()>0)
            {
                blnMoreThanOneItemInAGroup = true;
                break;
            }
        }

        //only containing one item in each group
        if(!blnMoreThanOneItemInAGroup) {
            HashMap<Long, Integer> temp = null;
            long key = -1;
            for (int i = 0; i < po.ruleItems.size(); i++) {
                temp = po.ruleItems.get(i);
                key = temp.keySet().iterator().next();
                si = new StoreItem(common.myMenu.GetLatestItem(key));
                si.UnitOrder = temp.get(key);
                lst.add(si);
                count = common.Utility.GetAtTheMomentItemCount(key);
                if (count <= 0 && !si.item.getDoNotTrackFlag()) {
                    common.Utility.ShowMessage("Add", "Insufficient stock for item " + si.item.getName(), this, R.drawable.no_access);
                    return;
                }
                inventoryCount.put(key, count);
            }
            InsertNewItemIntoCurrentCart(lst,null);
        }
        else
        {
            //trigger double tap
            PromotionItemDoubleTap(promotionId);
        }
        /*for(long key:po.ruleItems.keySet())
        {
            si = new StoreItem(common.myMenu.GetItem(key));
            si.UnitOrder = po.ruleItems.get(key);
            lst.add(si);
            count = common.Utility.GetAtTheMomentItemCount(key);
            if(count<=0 && !si.item.getDoNotTrackFlag())
            {
               common.Utility.ShowMessage("Add","Insufficient stock for item "+si.item.getName(),this,R.drawable.no_access);
               return;
            }
            inventoryCount.put(key,count);
        }*/


    }

    @Override
    public void PromotionItemDoubleTap(long promotionId) {
        //imgCloseOptionPopup.callOnClick();
        CloseAnyNonDialogPopup();
        HashMap<Long,Integer>inventoryCount = new HashMap<Long, Integer>();
        ArrayList<StoreItem>lst = new ArrayList<StoreItem>();
        StoreItem si;
        HashMap<Long,Integer>temp=null;
        PromotionObject po = common.myPromotionManager.Get(promotionId);
        boolean blnAdded=false;
        ItemObject io = null;
        CategoryObject co = null;
        for(int i=0;i<po.ruleItems.size();i++)
        {
            blnAdded=false;//reset
            temp = po.ruleItems.get(i);
            for(long key:temp.keySet())
            {
                io = common.myMenu.GetLatestItem(key);
                /**the item has been deleted**/
                if(io==null)
                {
                    //secondary check whether is a category object id for 'Any'
                    co = common.myMenu.GetCategory(key);
                    if(co==null) {
                        common.Utility.LogActivity("item " + key + " is no longer existed");
                        common.Utility.ShowMessage("Promotion", "One of the item has been deleted from the menu, please update your promotion items.", this, R.drawable.exclaimation);
                        return;
                    }
                    else
                    {
                        si = new StoreItem(new ItemObject(key,"Any",key,"0","",true,0,1));
                    }
                }
                else {
                    si = new StoreItem(common.myMenu.GetLatestItem(key));
                }
                si.UnitOrder = temp.get(key);
                if(!blnAdded)
                {
                    lst.add(si);//only take the 1st one in each group
                    blnAdded=true;
                }

                inventoryCount.put(key,common.Utility.GetAtTheMomentItemCount(key));
            }
        }
       ArrayList<Integer> tempIndexes = new ArrayList<Integer>();
        tempIndexes.add(-1);
        new ItemMenuOptionPopup(this,lst,false,inventoryCount,this,null,po,tempIndexes);
    }

    @Override
    public void ComboItemSingleTap(long comboId) {
        //imgCloseOptionPopup.callOnClick();
        CloseAnyNonDialogPopup();
    }

    @Override
    public void ComboItemDoubleTap(long comboId) {
        //imgCloseOptionPopup.callOnClick();
        CloseAnyNonDialogPopup();
    }

    @Override
    public void UpdateList(ArrayList<Long> list, String strTitle,int pageIndex) {
        common.customListManager.Update(pageIndex,strTitle,list);
        if(llOptionPopup!=null)
        {
            if(((LinearLayout)llOptionPopup.getChildAt(2)).getChildAt(0) instanceof CustomListContent)
            {
                ((CustomListContent)((LinearLayout)llOptionPopup.getChildAt(2)).getChildAt(0)).GotoPage(pageIndex,false);
            }
        }
    }

    @Override
    public void CustomList_AddNewUnitToCart(long itemId) {
        //doesn't allowed add if is combined receipt view
        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {
            common.Utility.ShowMessage("Add", "Please go to the receipt you want to add this item, currently this is combined view.", this, R.drawable.no_access);
            return;
        }

        ItemObject io = common.myMenu.GetLatestItem(itemId);
        if(!io.getDoNotTrackFlag()) {
            int count = common.Utility.GetAtTheMomentItemCount(itemId);
            if (count - 1<0)
            {
                common.Utility.ShowMessage("Inventory", io.getName()+" is currently out of stock.", this, R.drawable.no_access);
                return;
            }
        }
        StoreItem si =new StoreItem(io);
        si.UnitOrder=1;
        AddToCart(si,true);
        UpdateInventoryUnitCount(itemId);
    }

    @Override
    public void CustomList_ShowItemOptionPopup(long itemId) {
        if(GetIsPopupShown()){

            return;
        }
        SetPopupShow(true);
        StoreItem si = new StoreItem(common.myMenu.GetLatestItem(itemId));


        si.UnitOrder=1;
        new ItemMenuOptionPopup(this,si,false,common.Utility.GetAtTheMomentItemCount(si.item.getID()),this,null,-1);
    }
    @Override
    public void CheckoutPanelDialogDismissed()
    {
        //reset
        checkoutPanelDialog=null;
        SetPopupShow(false);
    }
    @Override
    public void LockScreen() {
        //final Activity activity =  this;
//set flag to lock application
        myAppSettings.SetAppIsLockedFlag(true);
        final MainLinearLayout all = (MainLinearLayout) findViewById(R.id.ActivityPanel);
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.layout_dialog_unlock_application_popup_window_ui, null);

        //readjust the lock image to make if position at y = 1/3
        ((LinearLayout.LayoutParams) popupView.findViewById(R.id.rlPanel).getLayoutParams()).topMargin = Math.round(all.getHeight() * 0.3f);

         popupView.findViewById(R.id.tvEmailUnlockPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!common.Utility.IsConnectedToNetwork(MainUIActivity.this))
                {
                    common.Utility.LogActivity("no internet");
                    common.Utility.ShowMessage("Internet","You must fist connect to the internet for this operation",MainUIActivity.this,R.drawable.no_access);

                    return;
                }


                    new SendAsyncEmail(myAppSettings.GetLockScreenPasswordEmail(),
                            "lock screen", "your device " + Build.SERIAL +
                            " password is " + myAppSettings.GetLockScreenPassword(),
                            myAppSettings.GetLockScreenPasswordEmail(),
                            popupView.getContext(), MainUIActivity.this)
                            .execute();
                    common.Utility.ShowMessage("Send Email", "An email with reset link has being sent to "+myAppSettings.GetLoginEmail()+".", MainUIActivity.this, R.drawable.message);

            }
        });
       /* ((TextView) popupView.findViewById(R.id.tvEmailUnlockPassword)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return true;
            }
        });*/

        ((EditText) popupView.findViewById(R.id.txtUnlockPassword)).addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                String strHashed = common.Utility.HashPassword(Integer.parseInt(common.myAppSettings.GetHashedMethod()),s.toString(),MainUIActivity.this);

                if (strHashed.equalsIgnoreCase(myAppSettings.GetHashedPassword())) {
                    common.control_events.HideSoftKeyboard(popupView);

                    ((ImageView) popupView.findViewById(R.id.imgLock)).setImageResource(
                            R.drawable.unlock_green);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myAppSettings.SetAppIsLockedFlag(false);
                            POPUP_WINDOW.setAnimationStyle(R.anim.slide_up_fast);
                            POPUP_WINDOW.dismiss();
                            all.setAlpha(1.0f);
                        }
                    }, 100);

                }

            }
        });
        POPUP_WINDOW = new PopupWindow(popupView, all.getWidth(), all.getHeight(), true);
        all.setAlpha(.1f);
        POPUP_WINDOW.setAnimationStyle(R.anim.slide_down_fast);
        POPUP_WINDOW.showAtLocation(popupView, Gravity.NO_GRAVITY, 0, 0);
    }

    @Override
    public void UpdatedRate(String strNewTaxRate) {
        SaveTax(strNewTaxRate);
        SetPopupShow(false);
    }

    @Override
    public void Dismiss() {
        SetPopupShow(false);
    }

    @Override
    public void onScrollChanged(MyScrollView myScrollView, int x, int y, int old_x, int old_y) {
        if(blnReceiptControlBusy)return;
        blnReceiptControlBusy=true;
        View view = myScrollView.getChildAt(myScrollView.getChildCount() - 1);
        int diff = (view.getBottom() - (myScrollView.getHeight() + myScrollView.getScrollY()));
        //load more item when user starts scrolling down
        if(diff==0 )
        {
            ContinueLoadItemOrder();
        }
        blnReceiptControlBusy=false;
    }

    private void ContinueLoadItemOrder()
    {
        LinearLayout ly = (LinearLayout)findViewById(R.id.llOrderedItem);
        //boolean blnIncludeLoadingGif=false;
        boolean blnHasPaid=false;
        TableLayout row;
        CartDisplayItem cdi;
        if(CURRENT_SUB_RECEIPT_INDEX==-1){
            blnHasPaid = false;
        }
        else
        {
            blnHasPaid=GetCurrentReceipt().blnHasPaid;
        }
        //calculate range to load
        int startIndex =lastLoadedOrderedItemIndex+1;
        int lastIndex =startIndex+AppSettings.ORDER_ITEM_PER_LOAD;
        if(lastIndex>currentOrderedItemCart.GetDisplayCartItemList().size())
        {
            lastIndex = currentOrderedItemCart.GetDisplayCartItemList().size();
        }

        if(startIndex>=lastIndex)return;

        //update last loaded index
        lastLoadedOrderedItemIndex = lastIndex-1;

       /* else
        {
            blnIncludeLoadingGif = true;
        }*/
        for(int i=startIndex;i<lastIndex;i++)
        {


            cdi=currentOrderedItemCart.GetDisplayCartItemList().get(i);
            if(cdi.cit== Enum.CartItemType.StoreItem)
            {
                row=CreateReceiptItem(cdi.si,i,blnHasPaid? Enum.CartItemStatus.paid:(currentOrderedItemCart.blnIsLock? Enum.CartItemStatus.lock: Enum.CartItemStatus.free));

            }
            else
            {
                row=CreatePromotionItem( cdi.pa, common.text_and_length_settings.TYPE_FACE_ABEL_FONT
                        , common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH
                        , common.text_and_length_settings.RECEIPT_DISPLAY_TABLE_ROW_PADDING);
            }


            if(startIndex==0)
            {
                //insert at last
                ly.addView(row);
            }
            else
            {
                //insert at second last
                ly.addView(row,ly.getChildCount()-1);
            }
        }

        //remove the last loading gif if loaded all or add loding gif
        if(startIndex==0 && lastIndex!=currentOrderedItemCart.GetDisplayCartItemList().size())
        {
            //add loading gif
            ProgressBar pb = new ProgressBar(this);
            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(getResources().getColor(R.color.white_green));
            tr.addView(pb);
            ly.addView(tr);
        }
        else if(lastIndex==currentOrderedItemCart.GetDisplayCartItemList().size() &&
                startIndex>0 && ly.getChildCount()>0)
        {
            //remove it
            ly.removeViewAt(ly.getChildCount()-1);
        }




    }
    public static class SettingsActivity extends PreferenceActivity
    {
        @Override
        public void onBuildHeaders(List<Header> target) {
            super.onBuildHeaders(target);
            loadHeadersFromResource(R.xml.headers_preference,target);
        }
    }
    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle saveInstanceState)
        {
            super.onCreate(saveInstanceState);

            //load the preference from an xml resource
            addPreferencesFromResource(R.xml.headers_preference);
            //addPreferencesFromResource(R.xml.preference);
        }
    }

    private int ConvertToPixel(int value)
    {
        return common.Utility.DP2Pixel(value, this);
    }
    public void SlideOut(boolean blnSlideRight,View v)
    {
        blnReceiptControlBusy = true;
        TranslateAnimation movement = new TranslateAnimation(0,ConvertToPixel((int)2000.0f), 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide to left
            movement = new TranslateAnimation(0f,ConvertToPixel((int)-2000.0f), 0.0f, 0.0f);//move left
        }





        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ListOrders(false,null);
                //Calculate();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(200);
        movement.setFillAfter(false);


        UpdateReceiptLabelIndex();
        v.startAnimation(movement);

    }
    public String GetCurrentTableId()
    {
        return ((Duple<String, Duple<String, Boolean>>) drpTable.getSelectedItem()).GetFirst();
    }
    public String GetCurrentTableLabel()
    {
        return  ((Duple<String, Duple<String, Boolean>>) drpTable.getSelectedItem()).GetSecond().GetFirst();
    }
    Enum.RemoveEmptyReceiptResult RemoveEmptyReceiptBeforeNextPaging()
    {
        /**this method will check for empty receipt and have it remove from current
        receipt list, and also readjust the receipt index**/
        MyCart currentCart=GetCurrentCart();
        String strTableId = currentCart.tableId;
        //boolean blnWriteToDB = true;
        Enum.RemoveEmptyReceiptResult result = Enum.RemoveEmptyReceiptResult.NoUpdateNeeded;
        if(currentCart.GetItems().isEmpty())
        {
            ArrayList<Receipt> receipts =common.myCartManager.GetReceipts(strTableId);
            //remove this split receipt if there is other in the list else keep it
            //so that don't have to create a new object later
            if(receipts.size()>1) {
                //write to DB only when this is not the last receipt and version still 1
                if(receipts.size()-1==CURRENT_SUB_RECEIPT_INDEX && receipts.get(CURRENT_SUB_RECEIPT_INDEX).Version==1)//last sub receipt
                {
                    //no need to update database, simply removing last unused sub receipt
                    //blnWriteToDB = false;
                    result= Enum.RemoveEmptyReceiptResult.NoUpdateNeeded;

                    common.myCartManager.RemoveReceipt(GetCurrentTableId(),CURRENT_SUB_RECEIPT_INDEX);
                    CURRENT_SUB_RECEIPT_INDEX--;
                }
                else
                {
                    //get lock and update the database
                    Enum.GetLockResult getLockResult =common.receiptManager.GetLocks(Schema.DataTable_Orders.TABLE_NAME,common.myCartManager.GetReceipts(strTableId));
                    if(getLockResult== Enum.GetLockResult.TryLater || getLockResult== Enum.GetLockResult.RecordCountMismatch || getLockResult== Enum.GetLockResult.VersionOutOfDate) {
                        common.Utility.ShowMessage("Refresh","Your receipt version is out of date, refreshing now.",this,R.drawable.no_access);
                        RefreshReceipts(strTableId);
                        return Enum.RemoveEmptyReceiptResult.VersionOutOfDate;
                    }

                    common.myCartManager.RemoveReceipt(GetCurrentTableId(),CURRENT_SUB_RECEIPT_INDEX);
                    CURRENT_SUB_RECEIPT_INDEX--;

                    //save the new receipt structure after removing empty receipt
                    Enum.DBOperationResult operationResult =common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),false);
                    if(operationResult== Enum.DBOperationResult.Success) {
                        result = Enum.RemoveEmptyReceiptResult.HasRemovedEmpty;
                    }
                    else if(operationResult== Enum.DBOperationResult.Failed) {
                        result = Enum.RemoveEmptyReceiptResult.WriteToDBFailed;
                    }
                    else {
                        result = Enum.RemoveEmptyReceiptResult.VersionOutOfDate;
                    }
                    common.receiptManager.UnLockRecords(strTableId);
                }




                /*if(blnWriteToDB) {

                    Enum.DBOperationResult operationResult =common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),true);

                    if (operationResult != Enum.DBOperationResult.Success)
                    {return Enum.RemoveEmptyReceiptResult.WriteToDBFailed;}
                    else
                    {result = Enum.RemoveEmptyReceiptResult.HasRemovedEmpty;}
                }
                else {
                    result = Enum.RemoveEmptyReceiptResult.HasRemovedEmpty;
                }*/
            }

        }

        return result;
    }
    public void ibtnInvoiceNextPage_Click(View view)
    {
        if(blnReceiptControlBusy)return;
        HideUndoBar();
        String strTableId=GetCurrentTableId();
        //does not allowed to go next page if the current receipt is locked and last
        if(CURRENT_SUB_RECEIPT_INDEX==common.myCartManager.GetReceipts(strTableId).size()-1 && GetCurrentCart().blnIsLock)
        {
            //common.Utility.ShowMessage("Split Receipt","Cannot add new split receipt once part of this receipt has been paid.",this,R.drawable.no_access);
            return;
        }

        //do not allow to go to next page if the current receipt is last and empty
        if(CURRENT_SUB_RECEIPT_INDEX==common.myCartManager.GetReceipts(strTableId).size()-1
                && GetCurrentCart().GetItems().size()==0)
        {
            return;
        }

        //boolean blnDeletedFlag = false;
        Enum.RemoveEmptyReceiptResult result = Enum.RemoveEmptyReceiptResult.NoUpdateNeeded;
        int originalReceiptIndex = CURRENT_SUB_RECEIPT_INDEX;
        if(CURRENT_SUB_RECEIPT_INDEX>-1) {//if is not showing combined receipt only
            if (GetCurrentCart().GetItems().isEmpty()) {
                //blnDeletedFlag = RemoveEmptyReceiptBeforeNextPaging();
                result = RemoveEmptyReceiptBeforeNextPaging();

            }
        }

        ArrayList<Receipt> receipts =common.myCartManager.GetReceipts(strTableId);
        //if(!blnDeletedFlag)
        if(result== Enum.RemoveEmptyReceiptResult.NoUpdateNeeded)
        {
            //normal flow
            if(receipts.size()==1 && CURRENT_SUB_RECEIPT_INDEX==-1)
            {
                CURRENT_SUB_RECEIPT_INDEX++;
            }
            else if(CURRENT_SUB_RECEIPT_INDEX==receipts.size()-1 && GetCurrentCart().GetItems().size()>0)
            {
                //allow split
                //split another receipt
                Receipt r = common.Utility.CreateNewReceiptObject(strTableId);
                r.Version = receipts.size();
                common.Utility.FillInReceiptProperties(r,strTableId);
                common.myCartManager.AddNewReceipt(r,strTableId);

                CURRENT_SUB_RECEIPT_INDEX++;
            }
            else if( receipts.size()==1 && CURRENT_SUB_RECEIPT_INDEX==0)
            {
                //stay at current page
                return;
            }
            else if(receipts.size()==(CURRENT_SUB_RECEIPT_INDEX+1) && GetCurrentCart().GetItems().size()==0)
            {
                //last page + empty cart, stay at current page
                return;
            }

            else
            {
                CURRENT_SUB_RECEIPT_INDEX++;
            }
        }
        else
        {
            //show the next page if available after removing current receipt
            //else will fall back to previous receipt
            if(receipts.size()-1>=originalReceiptIndex)
            {
                CURRENT_SUB_RECEIPT_INDEX=originalReceiptIndex;
            }
        }


        SlideOut(false, findViewById(R.id.svOrderedItem));

    }
    public void ibtnInvoicePreviousPage_Click(View view)
    {
        if(blnReceiptControlBusy)return;
        HideUndoBar();
        //boolean blnDeletedFlag = false;
        Enum.RemoveEmptyReceiptResult result = Enum.RemoveEmptyReceiptResult.NoUpdateNeeded;
        String strTableId = GetCurrentTableId();
        int totalReceiptCount=common.myCartManager.GetReceipts(strTableId).size();
        if(GetCurrentCart().GetItems().isEmpty() && totalReceiptCount>1) {
            result= RemoveEmptyReceiptBeforeNextPaging();
            if(!(result== Enum.RemoveEmptyReceiptResult.NoUpdateNeeded || result==Enum.RemoveEmptyReceiptResult.HasRemovedEmpty)){
                common.Utility.ShowMessage("Receipt","Your receipt version is out of date, refreshing now.",this,R.drawable.no_access);
                ReloadLastState();
                ListOrders(false,null);
                return;
            }
            totalReceiptCount=(result== Enum.RemoveEmptyReceiptResult.HasRemovedEmpty)?totalReceiptCount-1:totalReceiptCount;
        }

        //if(!blnDeletedFlag)
        if(result== Enum.RemoveEmptyReceiptResult.NoUpdateNeeded)
        {
            //normal flow
            if(CURRENT_SUB_RECEIPT_INDEX==0 && totalReceiptCount>1)
            {
                //combine all split receipt under current table id into one view
                CURRENT_SUB_RECEIPT_INDEX=-1;

            }
            else if(CURRENT_SUB_RECEIPT_INDEX==-1)
            {
                //stay at current page
                return;
            }
            else
            {
                CURRENT_SUB_RECEIPT_INDEX--;
            }
        }
        else
        {
            int receiptCount = common.myCartManager.GetReceipts(strTableId).size();
            if(CURRENT_SUB_RECEIPT_INDEX==-1 && receiptCount==0)
            {
                //the one and only receipt on display is currently empty and user hit previous
                //create a new receipt instance and set the receipt index to 0
                Receipt r = common.Utility.CreateNewReceiptObject(strTableId);
                common.Utility.FillInReceiptProperties(r,strTableId);
                common.myCartManager.GetReceipts(strTableId).add(r);
                CURRENT_SUB_RECEIPT_INDEX=0;

            }
            else
            {
               //do nothing
            }
        }

        SlideOut(true, findViewById(R.id.svOrderedItem));


    }
    protected  void ChangeViewMode(Enum.ViewMode vm)
    {
        if(currentViewMode==vm)return;
        common.Utility.LogActivity("Change view mode "+vm.name());
        currentViewMode = vm;


        if(vm== Enum.ViewMode.list)
        {
            ((ImageView)findViewById(R.id.imgViewPicMode)).setBackground(getResources().getDrawable(R.drawable.view_pic_unselected));
            ((ImageView)findViewById(R.id.imgViewListMode)).setBackground(getResources().getDrawable(R.drawable.view_list_selected));
        }
        else
        {
            ((ImageView)findViewById(R.id.imgViewPicMode)).setBackground(getResources().getDrawable(R.drawable.view_pic_selected));
            ((ImageView)findViewById(R.id.imgViewListMode)).setBackground(getResources().getDrawable(R.drawable.view_list_unselected));
        }

        if(mPager==null)return;//user hasn't clicked on any category yet

        ShowPageItemLoading(SelectedCategoryId+"",null);


    }

    public void lblTaxPercentage_Click(View view)
    {
        //show percentage input screen


        if(GetIsPopupShown())return;

        TaxRateOptionPopup taxPopup = new TaxRateOptionPopup(this,this);
        taxPopup.ShowPopup(myAppSettings.GetTaxPercentage());



    }
    public void SaveTax(String strNewTaxRate)
    {
        MyCart myCart;
        float newTaxRate = new BigDecimal(strNewTaxRate).floatValue();///100f;
        HashMap<String,ArrayList<Receipt>> receiptHM = common.myCartManager.Receipts;
        for(String key:receiptHM.keySet())
        {
            ArrayList<Receipt> receipts = receiptHM.get(key);
            for(Receipt r:receipts)
            {
                if(!r.blnHasPaid)//do not update the paid receipt but locked receipt
                {
                    r.myCart.percentage =newTaxRate;
                    r.flTaxRate = newTaxRate;
                }
            }
        }
        if(CURRENT_SUB_RECEIPT_INDEX==-1) {
            myCart = GetCurrentCartCopied();
            myCart.percentage = newTaxRate;
        }
       /* else
        {
            myCart = GetCurrentCart();
        }*/

       // myCart.percentage = (new BigDecimal(strNewTaxRate).floatValue()/100);
        Calculate();
        myAppSettings.SavePercentage(Float.toString(newTaxRate));

        SetTaxPercentageLabel(new DecimalFormat("#0.000").format(newTaxRate * 100f) + "");
    }

  /*  public void RelistUpdatedOrder(int intCartIndex)
    {
        //get if the updated item is currently on display

            //look for the targeted receipt row on ui
        MyCart myCart;
        int intTargetReceiptRow=-1;
        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {
            myCart = GetCurrentCartCopied();
        }
        else
        {
            myCart = GetCurrentCart();
        }
            StoreItem siUpdated = myCart.GetItems().get(intCartIndex);
            boolean blnFound = false;
            LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);
            for(int i=0;i<ly.getChildCount();i++)
            {

                StoreItem siFromReceiptRow = (StoreItem)ly.getChildAt(i).getTag();
                intTargetReceiptRow = i;
                if(siUpdated.item.getID()==siFromReceiptRow.item.getID() &&
                        siUpdated.modifiers.size()==siFromReceiptRow.modifiers.size())
                {
                    blnFound=true;

                    for(int j=0;j<siUpdated.modifiers.size();j++)
                    {
                        if(siUpdated.modifiers.get(j).getID()==siFromReceiptRow.modifiers.get(j).getID())
                        {

                            blnFound = true;
                        }
                        else{blnFound = false; break;}
                    }
                    if(blnFound)break;
                }
            }

            if(blnFound)
            {
                //remove existing row
                //and create new one
                TableLayout tlReceipt = CreateReceiptItem(myCart.GetItems().get(intCartIndex),intCartIndex);
                ly.removeViewAt(intTargetReceiptRow);
                ly.addView(tlReceipt,intTargetReceiptRow);
                //show animation
                if(tlReceipt.getChildCount()>1)
                {

                    ((FirstTableRowForMultiIngredients)tlReceipt.getChildAt(0)).SlideIn(!myAppSettings.SwipeLeftToDelete());
                }
                else
                {

                    ((RegularOrderedItemRow)tlReceipt).SlideIn(!myAppSettings.SwipeLeftToDelete());
                }
            }
        //}

        UpdateCartItemCountLabel(myCart);
        //ListOrders(false);
    }*/
    public void DisplayCurrentCartItem()
    {
        //CURRENT_INVOICE_PAGE_INDEX =1;
        //CURRENT_SUB_RECEIPT_INDEX=0;
        UpdateReceiptLabelIndex();
        ListOrders(false,null);
        //Calculate();
    }

    private void ConstructDisplayOrder(MyCart myCart, PromotionAwarded pa,long itemId,int receiptIndex,int cartIndex)
    {
        //deduct
        int unitNeeded =pa.collectedItems.get(itemId).get(receiptIndex);
        int available = myCart.GetItems().get(cartIndex).UnitOrder;
        if(unitNeeded>available)
        {
            unitNeeded-=available;
            //myCart.GetItems().get(cartIndex).UnitOrder=0;
            pa.collectedItems.get(itemId).put(receiptIndex,unitNeeded);
            //add to display queue
            myCart.GetDisplayCartItemList().add(new CartDisplayItem(null,myCart.GetItems().get(cartIndex), Enum.CartItemType.StoreItem));
            //myCart.GetDisplayCartItemList().add(new Duple<Enum.CartItemType, Object>(Enum.CartItemType.StoreItem,myCart.GetItems().get(cartIndex)));
        }
        else
        {

            myCart.GetItems().get(cartIndex).UnitOrder-=unitNeeded;
            pa.collectedItems.get(itemId).put(receiptIndex,0);
            StoreItem cloneSI =(StoreItem) myCart.GetItems().get(cartIndex).clone();
            cloneSI.UnitOrder = unitNeeded;
            //add cloned to display queue
            myCart.GetDisplayCartItemList().add(new CartDisplayItem(null,cloneSI, Enum.CartItemType.StoreItem));
            //myCart.GetDisplayCartItemList().add(new Duple<Enum.CartItemType, Object>(Enum.CartItemType.StoreItem,cloneSI));
        }


        //remove after add to display queue else will trigger infinite loop at caller level
        //myCart.GetItems().remove(cartIndex);

        //remove after when done with current receipt
        if(pa.collectedItems.get(itemId).get(receiptIndex)==0)
        {pa.collectedItems.get(itemId).remove(receiptIndex);}

        //remove the entry as well if no item require in there
        if(pa.collectedItems.get(itemId).size()==0)
        {
            pa.collectedItems.remove(itemId);
        }

        //check whether is completed promotion object
        if(pa.collectedItems.size()==0)
        {
            //add to display queue
            myCart.GetDisplayCartItemList().add(new CartDisplayItem(pa,null, Enum.CartItemType.PromotionAwarded));
            //myCart.GetDisplayCartItemList().add(new Duple<Enum.CartItemType, Object>(Enum.CartItemType.PromotionObject,pa));
        }
        //check for second item needed for this promotion within the same receipt
        for(long key:pa.collectedItems.keySet())
        {
            if(pa.collectedItems.get(key).containsKey(receiptIndex))
            {
                for(int i=0;i<myCart.GetItems().size();i++)
                {
                    if(myCart.GetItems().get(i).item.getID()==key)
                    {
                        ConstructDisplayOrder(myCart,pa,key,receiptIndex,i);
                        break;
                    }
                }

            }
        }


    }
    /*private boolean ContainValue(ArrayList<Integer>lst,int valueToCheck)
    {
        for(int i=0;i<lst.size();i++)
        {
            if(lst.get(i)==valueToCheck)
                return true;
        }

        return false;
    }*/
    private  HashMap<Long,HashMap<Integer,Integer>> MakeCopied(HashMap<Long,HashMap<Integer,Integer>> lst)
    {
        HashMap<Long,HashMap<Integer,Integer>> newLst = new HashMap<Long,HashMap<Integer,Integer>>();
        for(long key:lst.keySet())
        {
            newLst.put(key,new HashMap<Integer, Integer>());
            for(int key2:lst.get(key).keySet())
            {
                newLst.get(key).put(key2,lst.get(key).get(key2).intValue());
            }
        }
        return newLst;
    }
    private void CollectDisplayItemsAndPromotionAwareded(MyCart tempMyCart,ArrayList<PromotionAwarded>promotionAwardedOffered,int receiptIndex)
    {
        StoreItem si;
        int unitNeeded;
        PromotionAwarded pa;
        StoreItem clonedSI;
        HashMap<Long,HashMap<Integer,Integer>>collectedItems;
        ArrayList<Long> tempKeyset = new ArrayList<Long>();

        for (int j = 0; j < promotionAwardedOffered.size(); j++) {
            pa = promotionAwardedOffered.get(j);
            collectedItems = MakeCopied(pa.collectedItems);
            //make a copy to loop through in order to prevent concurrent access
            tempKeyset.clear();
            for (long itemId : collectedItems.keySet()) {
                tempKeyset.add(itemId);
            }

            for (int a = 0; a < tempKeyset.size(); a++) {

                if (collectedItems.get(tempKeyset.get(a)).containsKey(receiptIndex)) {

                    unitNeeded = collectedItems.get(tempKeyset.get(a)).get(receiptIndex);
                    //search for target store item

                    for (int i = 0; i < tempMyCart.GetItems().size(); i++) {
                        si = tempMyCart.GetItems().get(i);

                        //prevent fulfilled item to enter, will be remove after my cart for loop

                        if (tempKeyset.get(a) == si.item.getID()) {
                            if (si.UnitOrder > unitNeeded) {
                                clonedSI = (StoreItem) si.clone();
                                clonedSI.UnitOrder = unitNeeded;
                                si.UnitOrder -= unitNeeded;
                                tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(null, clonedSI, Enum.CartItemType.StoreItem));

                                collectedItems.get(tempKeyset.get(a)).remove(receiptIndex);
                                if (collectedItems.get(tempKeyset.get(a)).size() == 0) {
                                    collectedItems.remove(tempKeyset.get(a));
                                }

                                break;
                            } else if (si.UnitOrder == unitNeeded) {
                                collectedItems.get(tempKeyset.get(a)).remove(receiptIndex);
                                tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(null, si, Enum.CartItemType.StoreItem));

                                tempMyCart.GetItems().remove(i);
                                i--;
                                if (collectedItems.get(tempKeyset.get(a)).size() == 0) {
                                    collectedItems.remove(tempKeyset.get(a));
                                }


                                break;
                            } else {
                                unitNeeded -= si.UnitOrder;

                                collectedItems.get(tempKeyset.get(a)).put(receiptIndex, unitNeeded);
                                tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(null, si, Enum.CartItemType.StoreItem));


                                tempMyCart.GetItems().remove(i);
                                i--;
                            }
                        }

                    }


                    //under two circumstances this promotion will be added to the display queue
                    //#1 the size is empty
                    //#2 no more item from the current receipt index
                    if (collectedItems.size() == 0 || !ItemNeededToCollect(receiptIndex, collectedItems)) {
                        //re-add the entry back to the collected items list for share by how many receipt count later
                        tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(pa, null, Enum.CartItemType.PromotionAwarded));

                    }
                }


            }



        }
    }
    private ArrayList<PromotionObject> CombineCombosAndItems(int receiptIndex,MyCart myCart) {
        common.Utility.LogActivity("combining combos and items");


        /**collect all promotions by passing a complete combined receipt**/
        //ArrayList<PromotionAwarded> promotionAwardedOffered = common.myPromotionManager.CheckDiscountByItemPromotions(common.myCartManager.GetReceipts(myCart.tableId), receiptIndex,myCart);

        ArrayList<Receipt> receipts = common.myCartManager.GetReceipts(myCart.tableId);

        //clear display queue
        for(int i=0;i<receipts.size();i++)
            receipts.get(i).myCart.GetDisplayCartItemList().clear();


        if(receiptIndex==-1) {
            //combined receipt
            Receipt receipt = common.Utility.CreateNewReceiptObject(myCart.tableId);
            receipt.myCart = myCart;
            //receipts.clear();
            receipts = new ArrayList<Receipt>();
            receipts.add(receipt);
        }

        PromotionObject[] completedPromotions = common.myPromotionManager.GetCompletedPromotionsForCurrentReceipts(receipts);


        //filter out promotion by item only
        ArrayList<PromotionObject> promotionsByItem = common.myPromotionManager.FilterOutCashValueCombo(completedPromotions);

        //sort by highest discount value
        common.myPromotionManager.SortHighestDiscountFirstForItemPromotion(promotionsByItem);

        ArrayList<Receipt>copiedReceipts = new ArrayList<Receipt>();

        //make a copied
        common.myPromotionManager.CopiedReceiptList(copiedReceipts,receipts);

        ArrayList<PromotionAwarded> promotionAwardedOffered =  common.myPromotionManager.CollectReceiptPromotions(promotionsByItem,copiedReceipts);



        //make another copied
        copiedReceipts.clear();
        common.myPromotionManager.CopiedReceiptList(copiedReceipts,receipts);
        if(receiptIndex==-1)
        {


            MyCart tempMyCart = copiedReceipts.get(0).myCart;

            CollectDisplayItemsAndPromotionAwareded(tempMyCart,promotionAwardedOffered,0);

            while (tempMyCart.GetItems().size() > 0) {
                tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(null, tempMyCart.GetItems().remove(0), Enum.CartItemType.StoreItem));

            }

            myCart.GetDisplayCartItemList().clear();
            myCart.GetDisplayCartItemList().addAll(tempMyCart.GetDisplayCartItemList());
        }
        else
        {


            for(int b = 0;b<copiedReceipts.size();b++) {
                //display the promotion item 1st
                MyCart tempMyCart = copiedReceipts.get(b).myCart;

                CollectDisplayItemsAndPromotionAwareded(tempMyCart,promotionAwardedOffered,b);

                //insert non promotion item or leftover
                //if(myCart.GetItems().size()>0) {
                while (tempMyCart.GetItems().size() > 0) {
                    tempMyCart.GetDisplayCartItemList().add(new CartDisplayItem(null, tempMyCart.GetItems().remove(0), Enum.CartItemType.StoreItem));

                }

                receipts.get(b).myCart.GetDisplayCartItemList().clear();
                receipts.get(b).myCart.GetDisplayCartItemList().addAll(tempMyCart.GetDisplayCartItemList());

                if(receiptIndex==b) {
                    myCart.GetDisplayCartItemList().clear();
                    myCart.GetDisplayCartItemList().addAll(tempMyCart.GetDisplayCartItemList());
                }

            }
        }
        return promotionsByItem;
    }
    public boolean ItemNeededToCollect(int receiptIndex,HashMap<Long,HashMap<Integer,Integer>>collectedItems)
    {
        for(long itemId:collectedItems.keySet())
        {
            HashMap<Integer,Integer> record = collectedItems.get(itemId);
            if(record.containsKey(receiptIndex)) {
                return true;
            }
        }

        return false;
    }
    private void CreateAddOrderItemAnimation(HashMap<Long,Integer>targetRowIndexes,
                                             HashMap<Long,Integer>itemIDWithUnitRecord,
                                             final boolean blnAnimationAdd)
    {
        //hide related promotion rows, and show it later
        LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);

        for(long itemId:targetRowIndexes.keySet()) {

            //increase the unit count in existing display item list, the list didn't get
            // updated because is not re-checking promotion in this case
            TableLayout tbl = (TableLayout) ly.getChildAt(targetRowIndexes.get(itemId));
            TableRow row = (TableRow) tbl.getChildAt(0);
            TextView tv = (TextView) row.getChildAt(0);
            String strTemp = tv.getText() + "";
            int index = strTemp.indexOf("x ");
            tv.setText(itemIDWithUnitRecord.get(itemId) + "x " + strTemp.substring(index + 1));

            final MyScrollView msv = ((MyScrollView) findViewById(R.id.svOrderedItem));
            final int rowIndexToRunAnimation = targetRowIndexes.get(itemId);

            //final int animatedTargetRowIndex = targetRowIndex;
            msv.post(new Runnable() {

                @Override
                public void run() {


                    if (blnAnimationAdd) {
                        LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);
                        TableLayout tl = ((TableLayout) ly.getChildAt(rowIndexToRunAnimation));//ly.getChildCount() - 1));
                        if (tl == null) return;


                        if (tl.getChildCount() > 1) {

                            ((FirstTableRowForMultiIngredients) tl.getChildAt(0)).SlideIn(!myAppSettings.SwipeLeftToDelete());
                        } else {

                            ((RegularOrderedItemRow) tl).SlideIn(!myAppSettings.SwipeLeftToDelete());
                        }
                    }
                }
            });
        }
    }
    private Duple<Boolean,MyCart> GetListingCartAndPaidStatusAlsoUpdateCurrentPageIndex() {
        boolean blnHasPaid;
        MyCart myCart = null;
        if(CURRENT_SUB_RECEIPT_INDEX==-1){
            myCart =GetCurrentCartCopied();
            blnHasPaid = false;
        }
        else
        {
            //check receipt index because for example not all the tables have split receipt
            ArrayList<Receipt> receipts =common.myCartManager.GetReceipts(GetCurrentTableId());
            CURRENT_SUB_RECEIPT_INDEX = (CURRENT_SUB_RECEIPT_INDEX>receipts.size()-1)?0:CURRENT_SUB_RECEIPT_INDEX;
            myCart = (MyCart) receipts.get(CURRENT_SUB_RECEIPT_INDEX).myCart.clone();//common.myCartManager.GetCart(strTableId,CURRENT_SUB_RECEIPT_INDEX);
            blnHasPaid=receipts.get(CURRENT_SUB_RECEIPT_INDEX).blnHasPaid;
        }

        return new Duple<Boolean, MyCart>(blnHasPaid,myCart);
    }
    public void ListOrders(final boolean blnAnimationAdd, ArrayList<StoreItem> targetSIs)
    {

        boolean blnHasPaid;
        int newUnitCount=0;
        HashMap<Long,Integer>itemIDWithUnitRecord = new HashMap<Long, Integer>();
        String strTableId = GetCurrentTableId();


        common.Utility.LogActivity("List order page index "+CURRENT_SUB_RECEIPT_INDEX+ " of ");


        final LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);
        Duple<Boolean,MyCart> objects = GetListingCartAndPaidStatusAlsoUpdateCurrentPageIndex();
        blnHasPaid = objects.GetFirst();
        MyCart myCart=objects.GetSecond();

        //check whether the new added store item has promotion at the moment else just add it to UI instantly
        //without triggering async to re-check
        if((targetSIs!=null && !common.myPromotionManager.IsItemHasPromotionAtTheMoment(targetSIs)) )
        {
            //clear promotion record
            if(currentTablePromotions!=null)
            {currentTablePromotions.remove(strTableId);}

            ArrayList<TableLayout> receiptUIRow = new ArrayList<TableLayout>();
            for(int i=0;i<ly.getChildCount();i++) {
                View v = ly.getChildAt(i);
                if(v.getTag()!=null)receiptUIRow.add((TableLayout) v);//only table layout has tag a.k.a non loading gif row
            }






                //add to existing display cart item list
                for(int a = 0;a<targetSIs.size();a++) {
                    //newUnitCount=0;
                    for (int i = 0; i < myCart.GetDisplayCartItemList().size(); i++) {
                        if (myCart.GetDisplayCartItemList().get(i).cit == Enum.CartItemType.StoreItem)
                            if (myCart.GetDisplayCartItemList().get(i).si.IsSameOrderedItemExcludeUnitCount(targetSIs.get(a))) {
                                myCart.GetDisplayCartItemList().get(i).si.UnitOrder += targetSIs.get(a).UnitOrder;
                                newUnitCount = myCart.GetDisplayCartItemList().get(i).si.UnitOrder;
                                itemIDWithUnitRecord.put(targetSIs.get(a).item.getID(),newUnitCount);
                                break;
                            }
                    }
                }


            //loop through the list to find the table ui to perform merge existing item animation
            HashMap<Long,Integer>targetRowIndexes = GetAnimationRowIndex(targetSIs);



            //hide related promotion rows, and show it later
            if(targetRowIndexes.size()>0)
            {
                CreateAddOrderItemAnimation(targetRowIndexes,itemIDWithUnitRecord,blnAnimationAdd);


                //display total item
                UpdateReceiptLabels(myCart);

                BindCheckoutControl(blnHasPaid);
                Calculate();

                //blnListingOrder=false;//reset flag*/
                blnReceiptControlBusy = false;//reset flag
            }
            else
            {
                ListOrderAsync(blnAnimationAdd,targetSIs);

            }



        }
        else
        {
            ListOrderAsync(blnAnimationAdd,targetSIs);

        }


    }
    private void ListOrderAsync(boolean blnAnimationAdd,ArrayList<StoreItem>targetSIs)
    {
        common.Utility.LogActivity("Creating list order async");
        //clear the list 1st
        LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);
        ly.removeAllViews();
        //insert loading gif
        ProgressBar pb = new ProgressBar(this);
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(getResources().getColor(R.color.white_green));
        tr.addView(pb);
        ly.addView(tr);

        //now call the thread to handle backend data
        if(listOrderAsyncTask!=null) {
            //cancel any previous task
            listOrderAsyncTask.cancel(false);

        }

        listOrderAsyncTask = new ListOrderAsyncTask();
        listOrderAsyncTask.execute(new Object[]{blnAnimationAdd, targetSIs});
    }

    private void MergeDisplayPromotionItems(MyCart myCart)
    {
        boolean blnFound = true;
        ArrayList<CartDisplayItem>lst = myCart.GetDisplayCartItemList();

        PromotionAwarded pa1,pa2;
        StoreItem si1,si2;
        boolean blnMerged =false;
        int lastPAIndex = -1;
        while(blnFound)
        {
            blnFound = false;

            for(int i=lst.size()-1;i>-1;i--)
            {
                if(lst.get(i).cit== Enum.CartItemType.PromotionAwarded)
                {
                    if(lastPAIndex<0)
                    {
                        lastPAIndex=i;//start keeping track
                    }
                    else
                    {
                        //compare and see is same type
                        pa1 = lst.get(lastPAIndex).pa;
                        pa2 = lst.get(i).pa;
                        if(pa1.promotionObject.GetId()==pa2.promotionObject.GetId() &&
                                (pa1.ShareByHowManyReceipts()==1 && pa2.ShareByHowManyReceipts()==1)//not partial promotion
                                )
                        {
                            pa2.unit+=pa1.unit;
                            //copy promotion awarded #1 collected item over if is discount by pecentage
                            if(pa2.promotionObject.GetDiscountType()== Enum.DiscountType.percentage)
                            {
                                for(long itemId:pa1.collectedItems.keySet())
                                {
                                    if(pa2.collectedItems.containsKey(itemId))
                                    {
                                        for(int receiptIndex:pa1.collectedItems.get(itemId).keySet())
                                        {
                                            if(pa2.collectedItems.get(itemId).containsKey(receiptIndex))
                                            {
                                                pa2.collectedItems.get(itemId).put(receiptIndex,pa2.collectedItems.get(itemId).get(receiptIndex)+pa1.collectedItems.get(itemId).get(receiptIndex));
                                            }
                                            else
                                            {
                                                pa2.collectedItems.get(itemId).put(receiptIndex,pa1.collectedItems.get(itemId).get(receiptIndex));
                                            }
                                        }
                                    }
                                    else
                                    {
                                        pa2.collectedItems.put(itemId,pa1.collectedItems.get(itemId));
                                    }
                                }
                            }
                            lst.remove(lastPAIndex--);
                            int stopIndex=-1;
                            //check for next promotion object index
                            for(int j=i-1;j>-1;j--)
                            {

                                if(lst.get(j).cit== Enum.CartItemType.PromotionAwarded)
                                {
                                    stopIndex=j;
                                    break;
                                }
                            }
                            //start merging
                            while(lastPAIndex!=i) {
                                blnMerged=false;//reset
                                si1 =  lst.get(lastPAIndex).si;
                                for (int j = i - 1; j > stopIndex; j--) {
                                    si2 =(StoreItem)lst.get(j).si;
                                    //if(si1.item.getID()==si2.item.getID())
                                    if(si1.IsSameOrderedItemExcludeUnitCount(si2))
                                    {
                                        si2.UnitOrder+=si1.UnitOrder;
                                        lst.remove(lastPAIndex--);
                                        blnMerged=true;
                                        break;
                                    }
                                }
                                if(!blnMerged)
                                {
                                    //didn't find a match so just insert directly
                                    //insert above current promotion object at index i
                                    lst.add(i,lst.remove(lastPAIndex));
                                    //increase every index by 1, keeping same last PA Index value
                                    i = i+1;

                                }
                            }
                        }
                        else
                        {
                            //increase i and use current promotion item to check again next loop
                            i++;
                            //reset
                            lastPAIndex=-1;
                        }
                    }
                }

            }
        }
    }
    private void MergeQueuedDisplayItems(MyCart myCart)
    {
        //merge promotion item 1st
        boolean blnFound =true;
        ArrayList<CartDisplayItem>lst;
        CartDisplayItem d1,d2;
        //Duple<Enum.CartItemType,Object> d1;
        //Duple<Enum.CartItemType,Object> d2;
        StoreItem si1;
        StoreItem si2;
        lst = myCart.GetDisplayCartItemList();
        while(blnFound) {
            blnFound = false;
            for (int i = lst.size() - 1; i > 0; i--) {
                d1 = lst.get(i);
                if(d1.cit== Enum.CartItemType.StoreItem)
                {
                    si1 = d1.si;
                    for(int j=i-1;j>-1;j--)
                    {
                        d2 = lst.get(j);
                        //stop merging after encounter promotion item
                        if(d2.cit== Enum.CartItemType.PromotionAwarded)
                        {
                            break;
                        }
                        else
                        {
                            si2 = d2.si;
                            if(si1.IsSameOrderedItemExcludeUnitCount(si2))
                            {
                                si2.UnitOrder+=si1.UnitOrder;
                                lst.remove(i);
                                blnFound = true;
                                i=j;
                                break;
                            }
                        }
                    }
                }
            }
        }




    }

    private HashMap<Long,Integer> GetAnimationRowIndex(ArrayList<StoreItem> targetSIs)
    {
        HashMap<Long,Integer>RowIndexes = new HashMap<Long, Integer>();
        if(targetSIs==null)return RowIndexes;

        LinearLayout ly = (LinearLayout)findViewById(R.id.llOrderedItem);
        TableLayout tbl;
        //TableRow row;
        StoreItem currentSI;
        for(int a=0;a<targetSIs.size();a++)
        {
            for (int i = ly.getChildCount() - 1; i > -1; i--) {
                if (ly.getChildAt(i) instanceof TableLayout) {
                    tbl = (TableLayout) ly.getChildAt(i);
                    if (tbl.getTag() instanceof StoreItem)
                    //if(row.getTag() instanceof StoreItem)
                    {
                        currentSI = (StoreItem) tbl.getTag();
                        if (currentSI.IsSameOrderedItemExcludeUnitCount(targetSIs.get(a)))
                        {
                            RowIndexes.put(targetSIs.get(a).item.getID(),i);
                            break;
                        }
                    }
                }
                else {
                    //no item on display yet table row is showing loading gif
                }

            }
        }
        return RowIndexes;
    }
    private void UpdateCartItemCountLabel(MyCart myCart)
    {

        TextView tvCount = (TextView)findViewById(R.id.txtItemCount);
        //tvCount.setText(common.myCartManager.GetReceipt(GetCurrentTableId(),CURRENT_SUB_RECEIPT_INDEX).myCart.GetTotalUnitCount()+" item(s)");
        tvCount.setText(myCart.GetTotalUnitCount()+" item(s)");

    }
    private void UpdateReceiptLabelIndex()
    {
        //display total pages
        TextView tvPages = (TextView)findViewById(R.id.txtInvoicePage);
        if(CURRENT_SUB_RECEIPT_INDEX==-1)
        {
            tvPages.setText("All");
        }
        else
        {
            tvPages.setText((CURRENT_SUB_RECEIPT_INDEX+1) + "/" + common.myCartManager.GetReceipts(GetCurrentTableId()).size());

        }
    }
    private void UpdateReceiptLabels(MyCart myCart)
    {
        UpdateCartItemCountLabel(myCart);

        UpdateReceiptLabelIndex();

    }
    private void AddCategoryItemInGridView()
    {
        //draw border on selected item in grid view  if there is a selected item in top menu catainer
        MyTopMenuContainer tmc = (MyTopMenuContainer)findViewById(R.id.CategoryContainer);
        if(tmc.selectedChildItem!=null)
        {
            AddCategoryItemIntoGridView(tmc.selectedChildItem.getTag().toString());

        }
        else
        {
            AddCategoryItemIntoGridView("");
        }
    }
    public void ShowCategoryInGridView()
    {
        try
        {
            MyHorizontalScrollView HScrollView = (MyHorizontalScrollView) findViewById(R.id.MyTopMenuContainerScrollbar);
            GridView gvCategory = (GridView) findViewById(R.id.gvCategory);
            gvCategory.getLayoutParams().height = HScrollView.getLayoutParams().height;

            gvCategory.setVisibility(View.VISIBLE);
            HScrollView.setVisibility(View.GONE);

            AddCategoryItemInGridView();

        }
        catch (Exception ex)
        {
            //ShowErrorMessageBox("ShowCategoryInGridView",ex);
        }
    }
    public void HideCategoryInGridView()
    {
        MyHorizontalScrollView HScrollView = (MyHorizontalScrollView) findViewById(R.id.MyTopMenuContainerScrollbar);
        GridView gvCategory = (GridView)findViewById(R.id.gvCategory);
        gvCategory.setVisibility(View.GONE);
        HScrollView.setVisibility(View.VISIBLE);

    }
    private void AddCategoryItemIntoGridView(String strTag)
    {

        GridView gvCategory = (GridView)findViewById(R.id.gvCategory);


        ArrayList<CategoryObject> categoryObjects =(ArrayList<CategoryObject>)common.myMenu.GetCategoryList().clone();

        //add option
        categoryObjects.add(new CategoryObject(common.text_and_length_settings.TAP_TO_ADD_CATEGORY_ID, "TAP TO ADD"));
        gvCategory.setAdapter(new MyCategoryItemViewBaseAdapter(this, categoryObjects, strTag));




    }

    public void BindCheckoutControl(boolean blnHasPaid)
    {
        ((ImageView)findViewById(R.id.imgCheckout)).setOnClickListener(null);
        if(!blnHasPaid) {
            ((ImageView) findViewById(R.id.imgCheckout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GetCurrentSubReceiptIndex() == -1) {
                        common.Utility.ShowMessage("Checkout", "Cannot checkout All receipt.", MainUIActivity.this, R.drawable.no_access);
                        return;
                    }
                    CloseAnyNonDialogPopup();
                    CheckoutOption();
                }
            });
        }
        else
        {
            ((ImageView) findViewById(R.id.imgCheckout)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (GetCurrentSubReceiptIndex() == -1) {
                        common.Utility.ShowMessage("Checkout", "Cannot checkout All receipt.", MainUIActivity.this, R.drawable.no_access);
                        return;
                    }
                    common.Utility.ShowMessage("Checkout","This receipt has been paid.",MainUIActivity.this,R.drawable.no_access);
                }
            });
        }
    }

    public float GetActionBarHeight()
    {
        float ActionBarHeight=0;
        final android.content.res.TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });

        ActionBarHeight = DP2Pixel(styledAttributes.getDimension(0, 0),this)-11;


        styledAttributes.recycle();
        return ActionBarHeight;
    }
    public void ReadjustAllPanelHeight()
    {
        ReadjustMenuPanelComponentSizes();
        //ReadjustCheckoutFragmentPopup();
    }
    private void ReadjustMenuPanelComponentSizes()
    {

        //get MenuPanel height
        int topBorderOffset=0;
        //ActivityLinearLayout MenuPanel = (ActivityLinearLayout)findViewById(R.id.MenuPanel);
        //MyHorizontalScrollView MyTopMenuContainerScrollbar = (MyHorizontalScrollView)findViewById(R.id.MyTopMenuContainerScrollbar);
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.rlSummation);
        LinearLayout CheckoutPanel = (LinearLayout)findViewById(R.id.CheckoutPanel);
        GridView gvCategory = (GridView) findViewById(R.id.gvCategory);
        CheckoutPanel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, rl.getHeight()+topBorderOffset));
        LinearLayout llShortcut = (LinearLayout)findViewById(R.id.llShortcut);
        if(mPager==null) {
            mPager = (ViewPager)findViewById(R.id.MenuItemPager);
        }
        LinearLayout MenuItemSelectionPanel = (LinearLayout)findViewById(R.id.MenuItemSelectionPanel);



                    MenuItemSelectionPanel.getLayoutParams().height=common.Utility.DP2Pixel(565,this);//449,this);//height;
                    mPager.getLayoutParams().height = MenuItemSelectionPanel.getLayoutParams().height-llShortcut.getMeasuredHeight();






    }
    public void CloseAnyNonDialogPopup()
    {
        imgCloseOptionPopup.callOnClick();
    }
    public void PerformMergeSameItemInAllReceipts()
    {
        common.myCartManager.MergeSameItem();
    }
    public void imgBtnSelectFloorPlanTable_Click(View view)
    {
        CloseAnyNonDialogPopup();

        //need to take out the tool bar and action bar heights else table icon will not be align with
        //the design in floor plan mode
        int h = findViewById(R.id.ActivityPanel).getHeight()-Math.round(GetActionBarHeight())
                -common.Utility.DP2Pixel(60 + 10, this)
                +((getActionBar().isShowing())?Math.round(GetActionBarHeight())://add height if action bar visible
        -common.Utility.DP2Pixel(11, this));//minus offset if action bar invisible

        fpCtr = new FloorPlanCtr(this);
        //fpCtr.setLayoutParams();

        fpCtr.LoadSaved();
        fpCtr.SetFloorPlanMode(Enum.FloorPlanMode.select);
        fpCtr.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((LinearLayout)findViewById(R.id.ActivityPanel)).addView(fpCtr, 0,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                        //h));
                        fpCtr.setBackgroundColor(getResources().getColor(R.color.add_new_category_item_text_grey));
        if(common.floorPlan.GetBackgroundPhotoFilePath().length()>0)
        {
            fpCtr.setBackground(new BitmapDrawable(getResources(), common.floorPlan.GetBackgroundPhotoFilePath()));

        }
        fpCtr.SetTableTouchedListener(this);
        fpCtr.SlideIn(!myAppSettings.SwipeLeftToDelete());
    }

    public void imgBtnAddNewMenuItem_Click(View view,boolean blnEdit,ItemObject itemObject)
    {
        common.Utility.LogActivity("create new menu item");

        HideRightSideOptionBar();
        if(GetIsPopupShown()){return;}
        if(!blnEdit && MAX_ITEM_PER_CATEGORY<=common.myMenu.GetCategoryItems(SelectedCategoryId,false).size())
        {
            ShowMessage("Add Category Item","You have reached maximum <b><i>"+MAX_ITEM_PER_CATEGORY+"<i></b> items, please remove an item in order to add new.",R.drawable.no_access);
            return;
        }
        //ShowMessage("item file path",itemObject.getPicturePath());
        SetPopupShow(true);
        FrameLayout fl = (FrameLayout)findViewById(R.id.AddNewMenuItemFragmentPlaceholder);

        fl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        fl.setVisibility(View.VISIBLE);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        //ft.setCustomAnimations(R.anim.slide_up,R.anim.slide_down,R.anim.slide_up,R.anim.slide_down);
        ft.setCustomAnimations(R.anim.slide_up,-1);//,R.anim.slide_down,R.anim.slide_up,R.anim.slide_down);

        //// Replace the container with the new fragment
        //AddNewMenuItemFragment fra = new AddNewMenuItemFragment();

        long lngCategoryId = SelectedCategoryId;
        Enum.ModifierType mt = Enum.ModifierType.global;
        long lngItemId = common.text_and_length_settings.NEW_MODIFIER_PARENT_ID;
        ItemObject io = null;
        if(blnEdit)
        {
            mt = Enum.ModifierType.individual_and_global;
            lngCategoryId = itemObject.getParentID();
            lngItemId = itemObject.getID();
            io = itemObject;
        }

            ft.replace(R.id.AddNewMenuItemFragmentPlaceholder,
                    new AddNewMenuItemFragment().newInstance(SelectedCategoryId,
                            common.text_and_length_settings.INVOICE_ITEM_NAME_MAX_LENGTH,
                            ADD_MENU_ITEM_TITLE_TEXT_SIZE,
                            ADD_MENU_ITEM_TEXT_SIZE,
                            common.myMenu.GetCategory(lngCategoryId).getName(),
                            ADD_MENU_ITEM_MODIFIER_TEXT_SIZE,
                            this,
                            getResources().getString(R.string.label_create_menu_item_modifier_name_popup_window),
                            MAX_MODIFIER_ITEM,
                            SUB_ITEM_NAME_MAX_LENGTH,
                            HINT_MODIFIER_NAME,
                            HINT_MODIFIER_PRICE,
                            getResources().getString(R.string.label_create_menu_item_modifier_price_popup_window),
                            Integer.parseInt(getResources().getString(R.string.modifier_color_view_width_height_size)),
                            common.myMenu.GetModifiers(mt, lngItemId),
                            io,null),
                    AppSettings.TAG_ADD_NEW_MENU_ITEM_FRAGMENT
                    );

        // or ft.add(R.id.your_placeholder, new FooFragment());
        // Execute the changes specified
        ft.addToBackStack(null).commit();
    }
    public void SetPopupShow(boolean blnFlag){blnPopupShow = blnFlag;CloseAnyNonDialogPopup();}
    public boolean GetIsPopupShown(){return blnPopupShow;}
    public void SetReceiptControlBusyFlag(boolean blnFlag)
    {
        blnReceiptControlBusy = blnFlag;
    }
    public boolean GetIsReceiptControlBusy()
    {
        return blnReceiptControlBusy;
    }
    public void SetTopCategoryContainerBusyFlag(boolean blnFlag)
    {
        Log.d("set flag",blnFlag+"");
        blnTopCategoryContainerControlBusy = blnFlag;
    }
    public boolean GetIsTopCategoryContainerBusy()
    {
        return blnTopCategoryContainerControlBusy;
    }
    public void UpdateCategoryName(String strCategoryName,long lngId)
    {
        final MyHorizontalScrollView HScrollView = (MyHorizontalScrollView)findViewById(R.id.MyTopMenuContainerScrollbar);
        final GridView gvCategory = (GridView)findViewById(R.id.gvCategory);
        final MyTopMenuContainer tmc = (MyTopMenuContainer)findViewById(R.id.CategoryContainer);
        int  rowCount = common.myMenu.UpdateCategoryName(strCategoryName, lngId+"");
        if(rowCount<1)
        {
            ShowMessage("Edit Category Name","Failed to update category name, please try again later.",R.drawable.exclaimation);
        }
        else {
            UpdateCategoryName(lngId + "", strCategoryName);
            //add to the top menu bar
            if (HScrollView.getVisibility() == View.GONE) {
                //add to expanded category panel
                String strSelectedTag = "";
                if (tmc.selectedChildItem != null) {
                    strSelectedTag = tmc.selectedChildItem.getTag().toString();
                }

                gvCategory.setAdapter(new MyCategoryItemViewBaseAdapter(gvCategory.getContext(), common.myMenu.GetCategoryList(), strSelectedTag));
            }
        }
        Toast.makeText(getBaseContext(),"Category updated.",Toast.LENGTH_SHORT).show();
        SetPopupShow(false);
    }

    //PhotoFeatureFragment
    @Override
    public void onFragmentInteraction(Uri uri)
    {

    }

    /**TMe_POS_Register**/
    @Override
    public void onRegisterDevice(String strResult,String strEmail,String strHashMethod)
    {
        /*if(strProfileId2.equalsIgnoreCase("-1"))
        {
            ShowMessage("Register Device","There is an error while registering your device to server, please try again later or choose another device, we apologize for the inconvenience.",R.drawable.exclaimation);

        }
        else
        {
            ShowMessage("Device profile","device id: "+strProfileId1+", GUID: "+strProfileId2,R.drawable.message);
            myAppSettings.SaveDeviceRegisteredIds(strProfileId1,strProfileId2);

            //get expiration date if success
            StartGetExpirationDateService();
        }
     */
    }



    /**TMe_POS_Expiration_DATE**/
    @Override
    public void onExpirationDateReceive(String strDate)
    {
        //error
        if(strDate.compareTo("1/1/2000")==0) {
            if(!common.Utility.IsConnectedToNetwork(this))
            {
               //no internet connection
            }
        }
        else if(strDate.compareTo("1/1/1900")==0)
        {
            //couldn't locate device on server, haven't register


        }
        else {
            //update each time
            myAppSettings.SaveExpirationDate(strDate);
        }
        ShowMessage("Expiration Date",strDate,R.drawable.message);
    }
    /**TMe_POS_Geo location**/
    @Override
    public void onUpdateGeoLocation(String RowAffected) {
        //ShowMessage("Geo Location","Updated status = "+RowAffected);
    }
    /**TMe_POS_NOTIFICATION_RECEIVER**/
    @Override
    public void onNotificationReceive(final String strMsg,String strMsgId) {

        //check with preference store value see if is already shown before
        String strSavedCode = myAppSettings.GetServerMessageCode();

        myAppSettings.SaveServerMessage(strMsg,strMsgId);
        int IncomingMsgCode = Integer.parseInt(strMsgId);

        if(IncomingMsgCode==-1)return;//don't display error code

        int SavedMsgCode=(strSavedCode.length()>0)?Integer.parseInt(strSavedCode):-1000;//assign some number if is empty code
        if(IncomingMsgCode==SavedMsgCode)return;//is the same msg, skip it

        if(strMsg.length()==0)return;//is empty string, skip it

        final Context c = this;
        final RelativeLayout rlNotification = (RelativeLayout)findViewById(R.id.rlNotification);
        rlNotification.setVisibility(View.VISIBLE);
        final TextView tvNotification = (TextView)findViewById(R.id.tvNotification);
        tvNotification.setText("New Message");
        tvNotification.setTypeface(Typeface.createFromAsset(getAssets(), getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvNotification.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DP_SERVER_NOTIFICATION_TEXT_SIZE);
        tvNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlNotification.setVisibility(View.GONE);

                //show popup windows
                //ServerMessagePopupWindow popupWindow =
                new ServerMessagePopupWindow(c,strMsg);
            }
        });
        ImageButton imgDismiss = (ImageButton)findViewById(R.id.imgDismiss);
        imgDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlNotification.setVisibility(View.GONE);
            }
        });

        new CountDownTimer(5000,300){
            int[] colors = new int[]{R.color.glow_green_1,R.color.glow_green_2,
                    R.color.glow_green_3,R.color.glow_green_4,R.color.glow_green_5};
            int counter = 0;
            public void onTick(long milisUntilFinished)
            {
                tvNotification.setTextColor(getResources().getColor(colors[counter++]));
                if(counter>=5)counter=0;
            }
            public void onFinish() {
                tvNotification.setTextColor(getResources().getColor(R.color.green));
            }
        }.start();

    }

    /*private boolean IsRegisterDue()
    {
        boolean blnFlag = true;
        if(common.myAppSettings.GetDeviceRegisterId2().length()==0)
        {
            if(common.myAppSettings.GetAppInitialRunDate().length()==0)
            {

                common.myAppSettings.SaveAppInitialRunDate(Calendar.getInstance().getTimeInMillis()+"");
                blnFlag = false;
            }
            else
            {

                long dayPassed =Math.abs(Calendar.getInstance().getTimeInMillis()-Long.parseLong(common.myAppSettings.GetAppInitialRunDate()));
                dayPassed = dayPassed/(24*60*60*1000);
                if(dayPassed<4)blnFlag=false;

            }
        }else{blnFlag=false;}
        return blnFlag;
    }*/
    protected class ListOrderAsyncTask extends AsyncTask<Object,Void,Object[]>
    {
        private boolean blnCancel=false;
        @Override
        protected void onCancelled()
        {
            blnCancel = true;
            blnReceiptControlBusy=false;
            common.Utility.LogActivity("Cancelling list order async task");
        }
        @Override
        protected Object[] doInBackground(Object... objects) {


            boolean blnHasPaid;
            String strTableID = GetCurrentTableId();
            //if(currentOrderedItemCart==null)currentOrderedItems = new ArrayList<CartDisplayItem>();
            lastLoadedOrderedItemIndex=-1;
            ArrayList<StoreItem> storedItems =new ArrayList<StoreItem>();
            //stop if task cancelled
            if(blnCancel)return null;


            Duple<Boolean,MyCart> results = GetListingCartAndPaidStatusAlsoUpdateCurrentPageIndex();
            blnHasPaid = results.GetFirst();
            MyCart myCart=results.GetSecond();

           /* if(CURRENT_SUB_RECEIPT_INDEX==-1){
                myCart =GetCurrentCartCopied();
                blnHasPaid = false;
            }
            else
            {
                //check receipt index because for example not all the tables have split receipt
                ArrayList<Receipt> receipts =common.myCartManager.GetReceipts(strTableID);
                CURRENT_SUB_RECEIPT_INDEX = (CURRENT_SUB_RECEIPT_INDEX>receipts.size()-1)?0:CURRENT_SUB_RECEIPT_INDEX;
                myCart = (MyCart) receipts.get(CURRENT_SUB_RECEIPT_INDEX).myCart.clone();//common.myCartManager.GetCart(strTableId,CURRENT_SUB_RECEIPT_INDEX);
                blnHasPaid=receipts.get(CURRENT_SUB_RECEIPT_INDEX).blnHasPaid;
            }*/

            //stop if task cancelled
            if(blnCancel)return null;

        /*    if((blnHasPaid || myCart.blnIsLock) && myCart.GetItems().size()>0)
            {
                common.Utility.LogActivity("Table #"+strTableID+", Receipt #"+CURRENT_SUB_RECEIPT_INDEX+" status is paid or locked, redisplay existing item");
                //just redisplay whatever is storing in the list, do not query new
                if(myCart.GetItems().size()>0 && myCart.GetDisplayCartItemList().size()==0)
                {
                    for(int i=0;i<myCart.GetItems().size();i++)
                    {
                        //stop if task cancelled
                        if(blnCancel)return null;

                        myCart.GetDisplayCartItemList().add(new CartDisplayItem(null,myCart.GetItems().get(i), Enum.CartItemType.StoreItem));
                    }
                }
            }
            else
            {*/
                common.Utility.LogActivity("Table #"+strTableID+", Receipt #"+CURRENT_SUB_RECEIPT_INDEX+" querying promotions");
                //clear the displaying list
                myCart.GetDisplayCartItemList().clear();


                for(StoreItem si:myCart.GetItems())
                {
                    //stop if task cancelled
                    if(blnCancel)return null;

                    storedItems.add((StoreItem) si.clone());
                }


                //stop if task cancelled
                if(blnCancel)return null;
                ArrayList<PromotionObject> inUsedPromotions= CombineCombosAndItems(CURRENT_SUB_RECEIPT_INDEX,myCart);

                //update promotion record
                currentTablePromotions =(currentTablePromotions==null)?new HashMap<String, ArrayList<PromotionObject>>():currentTablePromotions;
                currentTablePromotions.put(strTableID,inUsedPromotions);


                //stop if task cancelled
                if(blnCancel)return null;
                MergeQueuedDisplayItems(myCart);

                //stop if task cancelled
                if(blnCancel)return null;
                MergeDisplayPromotionItems(myCart);

                //update if is not combined receipt
                if(CURRENT_SUB_RECEIPT_INDEX>-1) {
                    common.myCartManager.GetReceipts(GetCurrentTableId()).get(CURRENT_SUB_RECEIPT_INDEX).myCart.GetDisplayCartItemList().clear();
                    common.myCartManager.GetReceipts(GetCurrentTableId()).get(CURRENT_SUB_RECEIPT_INDEX).myCart.GetDisplayCartItemList().addAll(myCart.GetDisplayCartItemList());
                }

                myCart.SetItems(storedItems);
            //}



            //stop if task cancelled
            if(blnCancel)return null;

            currentOrderedItemCart = myCart;


            Object[] newObjectArry = new Object[objects.length+3];
            for(int i=0;i<objects.length;i++)
                newObjectArry[i] = objects[i];

            newObjectArry[objects.length] = myCart;
            newObjectArry[objects.length+1] = blnHasPaid;
            //newObjectArry[objects.length+2]=receiptUIRow;
            return newObjectArry;
        }



        @Override
        protected void onPostExecute(Object[] objects) {

            common.Utility.LogActivity("Table #"+GetCurrentTableId()+", Receipt #"+CURRENT_SUB_RECEIPT_INDEX+" listing receipt items");
            //stop if task cancelled
            if (blnCancel) return;


            final HashMap<Long, Integer> targetRowIndexes;

            //removing the initial loading gif from list
            final LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);
            ly.removeAllViews();

            //stop if task cancelled
            if (blnCancel) return;

            ContinueLoadItemOrder();

            //stop if task cancelled
            if (blnCancel) return;


            final boolean blnAnimationAdd = (Boolean) objects[0];
            ArrayList<StoreItem> targetSIs = (ArrayList<StoreItem>) objects[1];
            MyCart myCart = (MyCart) objects[2];
            boolean blnHasPaid = (Boolean) objects[3];


            //loop through the list to find the table ui to perform animation
            //stop if task cancelled
            if (blnCancel) return;
            targetRowIndexes = GetAnimationRowIndex(targetSIs);


            for (Long ids : targetRowIndexes.keySet()) {
                for (int i = 0; i < ly.getChildCount(); i++) {

                    //stop if task cancelled
                    if (blnCancel) return;

                    if (blnAnimationAdd && targetRowIndexes.get(ids) == i) {
                        ly.getChildAt(i).setVisibility(View.INVISIBLE);
                        final int index = i;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (ly.getChildCount() - 1 >= index)
                                    ly.getChildAt(index).setVisibility(View.VISIBLE);
                            }
                        }, 100);
                    }
                }
            }


            final MyScrollView msv = ((MyScrollView) findViewById(R.id.svOrderedItem));
            int largestIndex=-1;
            for(Integer rowIndex:targetRowIndexes.values())
            {
                largestIndex = largestIndex<rowIndex?rowIndex:largestIndex;
            }
            /*for(Long id:targetRowIndexes.keySet())
            {
                final int rowIndexToRunAnimation = targetRowIndexes.get(id);*/


                if (runScrollTo == null)
                {
                    final int animatedTargetRowIndex = largestIndex;
                    runScrollTo = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            //stop if task cancelled
                            if (blnCancel) return;
                            LinearLayout ly = (LinearLayout) findViewById(R.id.llOrderedItem);

                            //calculate the percentages and scroll down the target position
                            int totalHeight = 0;
                            for (int i = 0; i <= animatedTargetRowIndex; i++)
                            {
                                if (blnCancel) return;
                                totalHeight += ly.getChildAt(i).getMeasuredHeight();
                            }
                            msv.scrollTo(0, totalHeight);// msv.getBottom());

                            if (blnAnimationAdd)
                            {

                                TableLayout tl = null;
                                //make every new added item slide out
                                for(Integer rowIndex:targetRowIndexes.values()) {
                                    View v = ly.getChildAt(rowIndex);
                                    if (v instanceof TableLayout) {
                                        tl = (TableLayout) v;
                                    }

                                    if (tl.getChildCount() > 1) {

                                        ((FirstTableRowForMultiIngredients) tl.getChildAt(0)).SlideIn(!myAppSettings.SwipeLeftToDelete());
                                    } else {

                                        ((RegularOrderedItemRow) tl).SlideIn(!myAppSettings.SwipeLeftToDelete());
                                    }
                                }

                            }
                        }
                    };
                //}


            }



            msv.postDelayed(runScrollTo, 100);

            //stop if task cancelled
            if (blnCancel) return;

            //display total item
            UpdateReceiptLabels(myCart);

            //stop if task cancelled
            if (blnCancel) return;

            BindCheckoutControl(blnHasPaid);

            //stop if task cancelled
            if (blnCancel) return;
            Calculate();

            blnReceiptControlBusy = false;//reset flag
        }
    }
    protected class LoadMenuItemPageAsyncTask extends AsyncTask<Duple<Long,Long>,Void,Duple<ArrayList<ItemObject>,Long>>
    {


        @Override
        protected Duple<ArrayList<ItemObject>,Long> doInBackground(Duple<Long,Long>... duples) {
            ArrayList<ItemObject> items = common.myMenu.GetCategoryItems(duples[0].GetFirst(), true);
            return new Duple<ArrayList<tme.pos.BusinessLayer.ItemObject>,Long>(items,duples[0].GetSecond());
        }

        @Override
        protected void onPostExecute(Duple<ArrayList<ItemObject>,Long> duple) {
            super.onPostExecute(duple);
            //ShowPageItemLoading(Id + "", null, -1);
            LoadCategorySubMenuItem(duple.GetFirst(),duple.GetSecond());
        }


    }
}
