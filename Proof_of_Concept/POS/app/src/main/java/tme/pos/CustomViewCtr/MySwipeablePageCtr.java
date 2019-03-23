package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.POS_Application;
import tme.pos.R;

/**
 * Created by kchoy on 2/12/2015.
 */
public class MySwipeablePageCtr extends HorizontalScrollView {
    private ArrayList<ModifierObject>modifiers;
    private View ItemOptionPopupView;
    private int intCurrentPageIndex;
    private int intNextPageIndex;
    private int intTotalPages;
    private int intPageWidth;
    private boolean blnTouchAnimationRunning;
    private boolean blnIsBusy;
    private boolean blnFlingToLeft;
    private int intInitialX;
    private float SP_INACTIVE_MODIFIER_PAGE_TEXT_SIZE;
    private float SP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE;
    private ArrayList<ModifierObject>SelectedModifiers;
    //private MenuItemFlippableTableRow SelectedItemRowForThisAlert;
    private long lngDelay = 1000;
    int MODIFIER_PAGE_COUNT=6;
    ItemMenuOptionPopup popupClass;
    public MySwipeablePageCtr(Context context)
    {
        super(context);


    }

    public MySwipeablePageCtr(Context context, AttributeSet attrs) {
        super(context, attrs);

    }
    public MySwipeablePageCtr(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void GoToGroupPage(int intGroup)
    {
        if(blnIsBusy)return;
        blnIsBusy = true;
        Log.d("Go To Group Page","group "+intGroup);
        if(blnTouchAnimationRunning){//||blnFlingAnimation) {
            Log.d("return ","blnTouchAnimationRunning "+blnTouchAnimationRunning);//+", blnFlingAnimation"+blnFlingAnimation);
            return;
        }
        if(this.intPageWidth==-1)
        {
            CalculatePageWidth();
        }

        //blnTouchAnimationRunning = true;
        //intInitialX = getScrollX();
        intNextPageIndex = intGroup;
        setScrollX((intNextPageIndex*intPageWidth)-(intNextPageIndex*0));
        SetTextColor(R.color.black);
        blnIsBusy=false;


        //FlingAnimation();
        ////reset flag when scrolling is done
       // CheckIsScrollingStop();

    }
    public void ChangeModifierCategory(boolean blnGlobal)
    {
        if(blnIsBusy)return;
        blnIsBusy = true;
        //remove all pages
        LinearLayout pageContainer = (LinearLayout)this.getChildAt(0);
        pageContainer.removeAllViews();

//ShowMessageBox("modifier size",modifiers.size()+"");
        //get a list of individual modifiers
        Map records = new HashMap();
        records.put(0,new ArrayList<ModifierObject>());
        records.put(1,new ArrayList<ModifierObject>());
        records.put(2,new ArrayList<ModifierObject>());
        records.put(3,new ArrayList<ModifierObject>());
        records.put(4,new ArrayList<ModifierObject>());
        records.put(5,new ArrayList<ModifierObject>());
        for(int i=0;i<MODIFIER_PAGE_COUNT;i++)
        {
            for(int j=0;j<modifiers.size();j++)
            {
                ModifierObject mo = modifiers.get(j);
                if(mo.getMutualGroup()==i &&
                        ((blnGlobal && mo.getParentID()==-1)||(!blnGlobal && mo.getParentID()!=-1 ))
                        )
                {
                    ((ArrayList<ModifierObject>)records.get(i)).add(mo);
                }
            }
        }

        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(0));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(1));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(2));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(3));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(4));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(5));

        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                blnIsBusy = false;
            }
        },lngDelay);

    }
    public void SetupComponent(ArrayList<ModifierObject>modifiers, int intPages,View ItemOptionPopupView,
                               ItemMenuOptionPopup popupClass,ArrayList<ModifierObject>SelectedModifiers)
    {
        blnIsBusy = true;
        this.popupClass = popupClass;
        this.modifiers = modifiers;
        this.intCurrentPageIndex = 0;
        this.intNextPageIndex =this.intCurrentPageIndex;
        this.intTotalPages =intPages;
        this.intPageWidth=-1;
        this.blnTouchAnimationRunning = false;
        this.blnFlingToLeft = false;
        this.intInitialX=-1;
        this.ItemOptionPopupView = ItemOptionPopupView;
        this.SP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE = getResources().getDimension(R.dimen.dp_menu_item_popup_active_page_modifier_text_size);
        this.SP_INACTIVE_MODIFIER_PAGE_TEXT_SIZE = getResources().getDimension(R.dimen.dp_menu_item_popup_inactive_page_modifier_text_size);
        this.SelectedModifiers = SelectedModifiers;

        this.MODIFIER_PAGE_COUNT = Integer.parseInt(POS_Application.getInstance().getResources().getString((R.string.modifier_group_page_count)));
        //Log.d("initialize next page index",this.intNextPageIndex+"");

        //hierachy:parent horizontal scrollview->linear layout->scroll view->table
        //create ONE horizontal pages container
        LinearLayout pageContainer = new LinearLayout(getContext());
        pageContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        pageContainer.setLayoutParams(lllp);
        this.addView(pageContainer);

        //get a list of individual modifiers
        Map records = new HashMap();
        records.put(0,new ArrayList<ModifierObject>());
        records.put(1,new ArrayList<ModifierObject>());
        records.put(2,new ArrayList<ModifierObject>());
        records.put(3,new ArrayList<ModifierObject>());
        records.put(4,new ArrayList<ModifierObject>());
        records.put(5,new ArrayList<ModifierObject>());
        for(int i=0;i<6;i++)
        {
            for(int j=0;j<modifiers.size();j++)
            {
                ModifierObject mo = modifiers.get(j);
                if(mo.getMutualGroup()==i && mo.getParentID()!=-1)
                {
                    ((ArrayList<ModifierObject>)records.get(i)).add(mo);
                }
            }
        }

        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(0));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(1));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(2));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(3));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(4));
        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(5));



        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                blnIsBusy = false;
            }
        },lngDelay);

    }
//    public void SetupComponent(ArrayList<ModifierObject>modifiers, int intPages,View ItemOptionPopupView,MenuItemFlippableTableRow tableRow)
//    {
//        blnIsBusy = true;
//
//        this.modifiers = modifiers;
//        this.intCurrentPageIndex = 0;
//        this.intNextPageIndex =this.intCurrentPageIndex;
//        this.intTotalPages =intPages;
//        this.intPageWidth=-1;
//        this.blnTouchAnimationRunning = false;
//        this.blnFlingToLeft = false;
//        this.intInitialX=-1;
//        this.ItemOptionPopupView = ItemOptionPopupView;
//        this.SP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE = getResources().getDimension(R.dimen.sp_menu_item_popup_active_page_modifier_text_size);
//        this.SP_INACTIVE_MODIFIER_PAGE_TEXT_SIZE = getResources().getDimension(R.dimen.sp_menu_item_popup_inactive_page_modifier_text_size);
//        this.SelectedModifiers = new ArrayList<ModifierObject>();
//        this.SelectedItemRowForThisAlert = tableRow;
//        this.MODIFIER_PAGE_COUNT = Integer.parseInt(POS_Application.getInstance().getResources().getString((R.string.modifier_group_page_count)));
//        Log.d("initialize next page index",this.intNextPageIndex+"");
//
//        //hierachy:parent horizontal scrollview->linear layout->scroll view->table
//        //create ONE horizontal pages container
//        LinearLayout pageContainer = new LinearLayout(getContext());
//        pageContainer.setOrientation(LinearLayout.HORIZONTAL);
//        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        pageContainer.setLayoutParams(lllp);
//        this.addView(pageContainer);
//
//        //get a list of individual modifiers
//        Map records = new HashMap();
//        records.put(0,new ArrayList<ModifierObject>());
//        records.put(1,new ArrayList<ModifierObject>());
//        records.put(2,new ArrayList<ModifierObject>());
//        records.put(3,new ArrayList<ModifierObject>());
//        records.put(4,new ArrayList<ModifierObject>());
//        records.put(5,new ArrayList<ModifierObject>());
//        for(int i=0;i<6;i++)
//        {
//            for(int j=0;j<modifiers.size();j++)
//            {
//                ModifierObject mo = modifiers.get(j);
//                if(mo.getMutualGroup()==i && mo.getParentID()!=-1)
//                {
//                    ((ArrayList<ModifierObject>)records.get(i)).add(mo);
//                }
//            }
//        }
//
//        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(0));
//        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(1));
//        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(2));
//        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(3));
//        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(4));
//        CreateNewPage(pageContainer,(ArrayList<ModifierObject>)records.get(5));
//
//
//
//        Handler h = new Handler();
//        h.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                blnIsBusy = false;
//            }
//        },lngDelay);
//
//    }
   private void CreateNewPage(LinearLayout pageContainer,ArrayList<ModifierObject>PageModifierList)
   {




       //create scroll able page
       ScrollView page1sv = new ScrollView(getContext());
       page1sv.setVerticalScrollBarEnabled(true);
       ScrollView.LayoutParams svlp = new ScrollView.LayoutParams(MainUIActivity.DP2Pixel(760f,getContext()), LayoutParams.MATCH_PARENT);
       page1sv.setLayoutParams(svlp);

       //create table as page
       TableLayout page1 = new TableLayout(getContext());
       TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
       tllp.setMargins(10,0,10,10);
       page1.setPadding(0,10,0,0);
       //page1.setLayoutParams(tllp);


       for( int i=0;i<PageModifierList.size();i++)
       {
           ModifierObject mo = PageModifierList.get(i);
           final int ii =i;
           //create dummy row
           TableRow tr = new TableRow(getContext());
           //if(pageNum==1)
            tr.setBackgroundColor(getResources().getColor(Enum.MutualGroupColor.values()[mo.getMutualGroup()].value));
           //else
            //tr.setBackgroundColor(getResources().getColor(R.color.mutual_dark_orange));

           //text view
           final TextView tvName = new TextView(getContext());
           tvName.setText(mo.getName());
           tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE);
           tvName.setTextAlignment(TEXT_ALIGNMENT_CENTER);

           tvName.setGravity(Gravity.LEFT);
           tvName.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View view) {
                   ModifierClicked(tvName);
               }
           });

           TableRow.LayoutParams trlp = new TableRow.LayoutParams(0,TableRow.LayoutParams.WRAP_CONTENT);
           trlp.weight=0.1f;

           //price
           TextView tvPrice = new TextView(getContext());
           tvPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(mo.getPrice()));
           tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP,SP_ACTIVE_MODIFIER_PAGE_TEXT_SIZE);
           tvPrice.setGravity(Gravity.RIGHT);
           tvPrice.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View view) {
                   ModifierClicked(tvName);
               }
           });

           tr.addView(tvName, trlp);
           tr.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
           tr.addView(tvPrice, new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
           tr.setTag(mo);
           tr.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View view) {
                   ModifierClicked(tvName);
               }
           });

           page1.addView(tr,tllp);

           //highlight the row if selected
           for(int jj=0;jj<SelectedModifiers.size();jj++) {
               //if (SelectedModifiers.contains(mo)) {
               if(SelectedModifiers.get(jj).getID()==mo.getID())
               {
                   tvPrice.setTextColor(getResources().getColor(R.color.white));
                   tvName.setTextColor(getResources().getColor(R.color.white));
                   if (mo.getMutualGroup() != 0) {
                       tr.setBackgroundColor(getResources().getColor(Enum.MutualGroupColor.values()[mo.getMutualGroup()].value));
                   } else {
                       tr.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                   }

               }
           }
       }

       page1sv.addView(page1);

       pageContainer.addView(page1sv);

   }
   /* private String ConvertBigDecimalToCurrencyFormat(BigDecimal price)
    {
        String strTemp= NumberFormat.getCurrencyInstance(java.util.Locale.US).format(price);
        if(strTemp.contains("("))
        {
            //negative value
            strTemp = strTemp.replace("(","");
            strTemp = strTemp.replace(")","");
            strTemp ="-"+strTemp;
        }


        return strTemp;
    }*/
    public void ResetModifierRowAndUpdateSelectedList(ModifierObject RemovedMo)
    {
        LinearLayout pageContainer = (LinearLayout)getChildAt(0);
        for(int i=0;i<pageContainer.getChildCount();i++)
        {
            ScrollView svPage = (ScrollView)pageContainer.getChildAt(i);
            TableLayout tblPage = (TableLayout)svPage.getChildAt(0);
            for(int j =0;j<tblPage.getChildCount();j++)
            {
                ModifierObject mo = (ModifierObject)tblPage.getChildAt(j).getTag();
                if(mo.getID()==RemovedMo.getID())
                {
                    ModifierClicked(((TableRow)tblPage.getChildAt(j)).getChildAt(0));
                    break;
                }
            }
        }
    }
    private void ModifierClicked(View v)
    {

        TextView tv =(TextView)v;
        //ShowMessageBox("Clicked",tv.getText()+"");
        TableRow tr = (TableRow)tv.getParent();
        TableLayout tbl = (TableLayout)tr.getParent();
        ModifierObject mo = (ModifierObject) tr.getTag();
        ModifierObject RemovedMo=null,NewMo=null;

        for(int i =0;i<SelectedModifiers.size();i++)
        {
            if(SelectedModifiers.get(i).getID()==mo.getID())
            {
                SelectedModifiers.remove(i);
                //store as remove object
                RemovedMo=mo;
                //reset the selected color

                //deselect the current modifier
                ((TextView)tr.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                ((TextView)tr.getChildAt(1)).setTextColor(getResources().getColor(R.color.black));

                tr.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            }
        }

        if(mo.getMutualGroup()!=0)
        {
            //current modifier is in mutual group
            //only allowed one item to be selected in each group

            if(RemovedMo!=null)
            {
                //store as remove object
                RemovedMo=mo;
                //reset the selected color

                //deselect the current modifier
                ((TextView)tr.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                ((TextView)tr.getChildAt(1)).setTextColor(getResources().getColor(R.color.black));

                tr.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            }
            else
            {

                //deselect all other currently selected row if any in UI
                for(int i=0;i<tbl.getChildCount();i++)
                {
                    TableRow tempTR=(TableRow)tbl.getChildAt(i);
                    //ModifierObject localMo = (ModifierObject)tempTR.getTag();

                    //if(((TextView)tempTR.getChildAt(0)).getCurrentTextColor()==getResources().getColor(R.color.white))
                    //{
                        ((TextView)tempTR.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                        ((TextView)tempTR.getChildAt(1)).setTextColor(getResources().getColor(R.color.black));
                        tempTR.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
                        //break;
                    //}
                }
                //remove any modifier that is in this same group from the selected list
                for(int i=0;i<SelectedModifiers.size();i++)
                {
                    if(SelectedModifiers.get(i).getMutualGroup()==mo.getMutualGroup()&&
                            mo.getParentID()==SelectedModifiers.get(i).getParentID())
                    {
                        //store removed mo
                        RemovedMo=SelectedModifiers.get(i);
                        SelectedModifiers.remove(i);
                        break;
                    }
                }


                //select the current row
                ((TextView)tr.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                ((TextView)tr.getChildAt(1)).setTextColor(getResources().getColor(R.color.white));

                tr.setBackgroundColor(getResources().getColor(Enum.MutualGroupColor.values()[mo.getMutualGroup()].value));

                //add to selected list
                SelectedModifiers.add(mo);

                //store new added mo
                NewMo = mo;
            }


        }
        else
        {
            //if(tv.getCurrentTextColor()==getResources().getColor(R.color.white))
            if(RemovedMo!=null)
            {
                //store removed mo
                RemovedMo=mo;
                //deselect current row
                ((TextView)tr.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                ((TextView)tr.getChildAt(1)).setTextColor(getResources().getColor(R.color.black));

                tr.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            }
            else
            {
                //select current row
                ((TextView)tr.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                ((TextView)tr.getChildAt(1)).setTextColor(getResources().getColor(R.color.white));

                tr.setBackgroundColor(getResources().getColor(R.color.selected_row_green));

                SelectedModifiers.add(mo);

                //store new added mo
                NewMo = mo;
            }
        }

        //popupClass.UpdateReceiptModifier(RemovedMo,NewMo);
        //SelectedItemRowForThisAlert.UpdateReceiptModifier(RemovedMo,NewMo);
    }
    private void ReportSelectedList()
    {
        String strValue="";
        for(ModifierObject mo:SelectedModifiers)
        {
            strValue+=mo.getName() +"<br/>";
        }
        common.Utility.ShowMessage("Selected items",strValue+"",getContext(),R.drawable.message);
    }
    private void CalculatePageWidth()
    {
        LinearLayout pageContainer = (LinearLayout)getChildAt(0);
        ScrollView Sv_page1 = (ScrollView)pageContainer.getChildAt(0);
        ScrollView Sv_page2 = (ScrollView)pageContainer.getChildAt(1);
        int[] location_Page1 = new int[2];
        Sv_page1.getLocationInWindow(location_Page1);
        int[] location_Page2 = new int[2];
        Sv_page2.getLocationInWindow(location_Page2);
        this.intPageWidth = location_Page2[0]-location_Page1[0];
    }
    private void ShowNextIndexPage()
    {

        //get current X
        int currentPageX =intCurrentPageIndex*this.intPageWidth;
        int nextPageX = intNextPageIndex*this.intPageWidth;

        if(this.intCurrentPageIndex<this.intNextPageIndex)
        {
            //Log.d("this.intCurrentPageIndex<this.intNextPageIndex","scroll x: "+getScrollX()+", currentPage X: "+currentPageX+", nextPage X: "+nextPageX);
            //show next page
            //check if current page x value has move right 60%+
            //else retain at current page
            int delta = Math.abs(nextPageX-getScrollX());
            double ratio = (delta*1.0)/(intPageWidth*1.0);
            //less than 60%, retain
            if(ratio>0.4)
            {
                Log.d("scroll percentage",ratio+", retain");
                intNextPageIndex = intCurrentPageIndex;
            }else{Log.d("scroll percentage",ratio+", to next page");}
        }
        else if(this.intCurrentPageIndex>this.intNextPageIndex)
        {
            //Log.d("this.intCurrentPageIndex>this.intNextPageIndex","scroll x: "+getScrollX()+", currentPage X: "+currentPageX+", nextPage X: "+nextPageX);
            //show previous page
            //check if next page x value has move left 60%+
            //else retain at current page
            int delta = Math.abs(currentPageX-getScrollX());
            double ratio = (delta*1.0)/(intPageWidth*1.0);

            //less than 60%, retain
            if(ratio>0.6)
            {Log.d("scroll percentage",ratio+", to previous page");

            }else{
                Log.d("scroll percentage",ratio+", retain");
                intNextPageIndex = intCurrentPageIndex;
            }
        }

        Log.d("Show next page index","touch animation set to true");
        this.blnTouchAnimationRunning = true;
        Log.d("current page index "+intCurrentPageIndex,"x is "+intCurrentPageIndex*intPageWidth);
        Log.d("next page index "+intNextPageIndex,"x is "+intNextPageIndex*intPageWidth);
        for(int i=0;i<intTotalPages;i++)
        {
            Log.d("page #"+i,"x is "+i*intPageWidth);
        }
        //save initial x
        intInitialX = getScrollX();

        //call fling animation
        //wait and see whether is a fling action
        //check flag after 200 milliseconds
        android.os.Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                //restore back to current page if is not fling action, else let
                //fling method handle it
                FlingAnimation();
                //if (!blnFlingAnimation) FlingAnimation();
            }
        }, 150);




        CheckIsScrollingStop();



    }
    private void CheckIsScrollingStop()
    {
        android.os.Handler h = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Log.d("initial X "+intInitialX,"current X "+getScrollX());
                if(intInitialX==getScrollX())
                {
                    ScrollingStopped();
                }
                else
                {
                    //check again later
                    intInitialX = getScrollX();
                    CheckIsScrollingStop();
                }
            }
        };
        h.postDelayed(r,200);
    }
    private void ScrollingStopped()
    {
        SetTextColor(R.color.black);
        blnTouchAnimationRunning = false;
        intCurrentPageIndex = intNextPageIndex;
        //update page indicator
        LinearLayout PageIndicatorContainer = (LinearLayout)ItemOptionPopupView.findViewById(R.id.llPageIndicator);
        for(int i=0;i<PageIndicatorContainer.getChildCount();i++)
        {
            if(intNextPageIndex==i)
            {
                ((ModifierPageIndicatorIndexCtr)PageIndicatorContainer.getChildAt(i)).FillCircle();
            }
            else
            {
                ((ModifierPageIndicatorIndexCtr)PageIndicatorContainer.getChildAt(i)).UnfillCircle();
            }
        }


        Log.d("animation running done ","is now false");
    }
    private void ReportLocation()
    {
        //test reporting page 1 location
        LinearLayout pageContainer = (LinearLayout)getChildAt(0);
        ScrollView Sv_page1 = (ScrollView)pageContainer.getChildAt(0);
        ScrollView Sv_page2 = (ScrollView)pageContainer.getChildAt(1);
        //Rect r = new Rect();

        int[] location = new int[2];
        Sv_page1.getLocationInWindow(location);
        //Log.d("sv page 1 on .getLocationInWindow() ","X: "+location[0]+", Y: "+location[1]);
        Sv_page2.getLocationInWindow(location);
        //Log.d("sv page 2 on .getLocationInWindow() ","X: "+location[0]+", Y: "+location[1]);
    }

    private void PageScrollingToRight()
    {
        blnFlingToLeft= true;
        SetTextColor(R.color.divider_grey);
        //SetInactiveTextSize();
    }
    private void PageScrollingToLeft(){
        blnFlingToLeft = false;
        SetTextColor(R.color.divider_grey);
        //SetInactiveTextSize();
    }
   /* private void SetInactiveTextSize()
    {
        //restore the text size
        TableLayout tbl = (TableLayout)((ScrollView)((LinearLayout)getChildAt(0)).getChildAt(intCurrentPageIndex)).getChildAt(0);
        for(int i=0;i>tbl.getChildCount();i++)
        {
            ((TextView)((TableRow)tbl.getChildAt(i)).getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP,SP_INACTIVE_MODIFIER_PAGE_TEXT_SIZE);
            ((TextView)((TableRow)tbl.getChildAt(i)).getChildAt(1)).setTextSize(TypedValue.COMPLEX_UNIT_DIP,10f);//SP_INACTIVE_MODIFIER_PAGE_TEXT_SIZE);
        }
    }*/
    private void SetTextColor(int intColor)
    {
        LinearLayout pageContainer = (LinearLayout)getChildAt(0);
        ScrollView Sv_page1 = (ScrollView)pageContainer.getChildAt(this.intCurrentPageIndex);
        if(Sv_page1==null)return;
        TableLayout tbl = (TableLayout)Sv_page1.getChildAt(0);
        SetChildTextColor(tbl,intColor);
        ScrollView Sv_page2 = (ScrollView)pageContainer.getChildAt(this.intNextPageIndex);
        if(Sv_page2==null)return;
        tbl = (TableLayout)Sv_page2.getChildAt(0);
        SetChildTextColor(tbl,intColor);
    }
    private void SetChildTextColor(TableLayout tbl,int intColor)
    {
        for(int i=0;i<tbl.getChildCount();i++)
        {
            TableRow tr = (TableRow)tbl.getChildAt(i);
            if(((TextView)tr.getChildAt(0)).getCurrentTextColor()!=getResources().getColor(R.color.white))
            {
                ((TextView)tr.getChildAt(0)).setTextColor(getResources().getColor(intColor));
                ((TextView)tr.getChildAt(1)).setTextColor(getResources().getColor(intColor));
            }

        }
    }
    public void FlingAnimation()
    {
        if(this.intPageWidth==-1)
        {
            CalculatePageWidth();
        }
        smoothScrollTo(intNextPageIndex*intPageWidth,getScrollY());

    }
    private  void ShowMessageBox(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
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
    @Override
    public void fling(int velocityX) {
        //ShowMessageBox("fling","velocityX "+velocityX);
        //only allow abs(velocity) greater than 10000 to trigger
        if(Math.abs(velocityX)<1500)return;
        //blnFlingAnimation = true;
        Log.d("fling","velocityX "+velocityX);
        Log.d("fling","blnTouchAnimationRunning "+blnTouchAnimationRunning+", intCurrentPageIndex "+intCurrentPageIndex+
                ", intNextPageIndex "+intNextPageIndex);
        super.fling(0);

        //ignore fling, restoring move by user if next page didn't achieve 60% of visibility
        if(blnTouchAnimationRunning && intCurrentPageIndex!=intNextPageIndex) {
            Log.d("fling","return");
            return;
        }

        //fling execution
        if(blnFlingToLeft)
        {
            intNextPageIndex = intCurrentPageIndex+1;
        }
        else
        {
            intNextPageIndex = intCurrentPageIndex-1;
        }
        //return already at 1st page or at the last page
        if(intNextPageIndex<0 ||intNextPageIndex>intTotalPages-1)return;

        //already has fling animation pending to execute, so ignore
        //if(blnTouchAnimationRunning)return;


        FlingAnimation();



    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.d("onScrollChanged","new X: "+l+", old X: "+oldl );



        //calculate page width
        if(this.intPageWidth==-1)
        {
            CalculatePageWidth();
        }

        //X-axis
        if(l>oldl) {
            if (this.intNextPageIndex == this.intCurrentPageIndex) {
                Log.d("onScrollChanged","intNextPageIndex++");
                intNextPageIndex++;
            }
            if (this.intNextPageIndex > (intTotalPages - 1)) {
                Log.d("onScrollChanged","intNextPageIndex = this.intTotalPAges-1");
                this.intNextPageIndex = this.intTotalPages - 1;
            }
            PageScrollingToRight();
            Log.d("scrolling", "scrolled to right, X: " + l+", current page #"+intCurrentPageIndex+", next page #"+this.intNextPageIndex);
        }
        else if(l<oldl) {
            if (this.intNextPageIndex == this.intCurrentPageIndex) {
                Log.d("onScrollChanged l<oldl","intNextPageIndex--");
                intNextPageIndex--;
            }
            if (this.intNextPageIndex < 0) {
                //Log.d("onScrollChanged intNextPageIndex<0","intNextPageIndex=0");
                this.intNextPageIndex = 0;

            }
            PageScrollingToLeft();
            Log.d("scrolling", "scrolled to left, X: " + l+", current page #"+intCurrentPageIndex+", next page #"+this.intNextPageIndex);
        }
        else
            Log.d("scrolling","not moving, X: "+l);


    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //skill if the scroll to next page animation still running
        if(this.blnTouchAnimationRunning){
            Log.d("animation running","true");
            return false;
        }
        Log.d("animation running","false");
        int action = ev.getAction();


        switch(action)
        {
            case MotionEvent.ACTION_DOWN:
                Log.d("page container","down");
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("page container","move");
                break;
            case MotionEvent.ACTION_UP:
                Log.d("page container","up");
                ShowNextIndexPage();

                break;
            case MotionEvent.ACTION_OUTSIDE:
                Log.d("page container","outside");
                break;
        }


        return super.onTouchEvent(ev);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {


        int action = MotionEventCompat.getActionMasked(motionEvent);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if(blnIsBusy||blnTouchAnimationRunning) {
                    //ShowMessageBox("Busy", "System is currently busy.");
                    return false;
                }
                //if(blnTouchAnimationRunning ){Log.d("animation running","true");return false;}
                //if(blnIsBusy ){Log.d("page is busy","true");return false;}
                //if(blnFlingAnimation ){Log.d("fling animation running","true");return false;}
                Log.d("on intercept","down");

                break;
            case (MotionEvent.ACTION_MOVE):

                break;
            case (MotionEvent.ACTION_UP):
                break;

            case (MotionEvent.ACTION_CANCEL):
                break;

            default:
                break;
        }
        //return false;

        return super.onInterceptTouchEvent(motionEvent);

    }
}
