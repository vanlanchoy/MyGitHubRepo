package tme.pos;


import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;

import android.graphics.Color;

import android.graphics.Typeface;

import android.os.Bundle;

import android.os.Handler;
import android.support.v4.view.MotionEventCompat;

import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import tme.pos.BusinessLayer.*;

import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.MenuItemFlipablePicView;
import tme.pos.CustomViewCtr.MenuItemFlippableTableRow;
import tme.pos.CustomViewCtr.MyScrollView;
import tme.pos.CustomViewCtr.TapToAddTextView;
import tme.pos.Interfaces.IItemViewUpdateUnit;
import tme.pos.Interfaces.IMenuItemClickedListener;
import tme.pos.Interfaces.IPageActivityListener;
import tme.pos.Interfaces.IToBeUpdatedInventoryView;


public class PageViewerFragment extends PhotoFeatureFragment//  android.support.v4.app.Fragment
implements MyScrollView.IScrollViewListener,IMenuItemClickedListener
        ,TapToAddTextView.ITappedListener{

    @Override
    public void Tapped() {
        if(pageActivityListener!=null){pageActivityListener.AddNewItem();}

    }


    IPageActivityListener pageActivityListener;
    private static final String Arg_PageIndex = "PageIndex";
    public ArrayList<Pair<Long,TextView>>lstTvUnit;
    private ArrayList<ItemObject>items;
    private String strCurrentPageIndex;

    private int itemPerPage=10;

    private  View FragmentView;





    Enum.ViewMode currentViewMode;


    boolean blnProcessingAddItem;
    int intLastLoadedItemIndex=-1;
    int intPageLastItemIndex=-1;
    boolean blnLoadingItems=false;
    boolean blnIsVisibleToUser = false;

    long lngSelectedCategoryId=-2;
    // TODO: Rename and change types and number of parameters


    public PageViewerFragment()
    {

    }
    public void SetProperties (String index,int intItemPerPage,
                                                 //MainUIActivity mActivity,
                               IPageActivityListener listener,
                               Enum.ViewMode vm,
                               long categoryId

    ) {


        Bundle args = new Bundle();
        args.putString(Arg_PageIndex, index);
        setArguments(args);
        currentViewMode = vm;
        itemPerPage = intItemPerPage;
        pageActivityListener = listener;
        //MainActivity = mActivity;
        blnProcessingAddItem = false;


        items =  common.myMenu.GetCategoryItems();
        lngSelectedCategoryId = categoryId;


        strCurrentPageIndex = getArguments().getString(Arg_PageIndex);
        //wait for oncreateview to execute 1st
        if(FragmentView!=null) {

            //if(blnIsVisibleToUser){CreatePageItems();}
            CreatePageItems();
            FragmentView.setTag(strCurrentPageIndex);
        }


    }


    @Override
    public void onPause() {
        super.onPause();
        //common.Utility.ShowMessage("page viewer fragment", "on pause",getActivity(),R.drawable.message);
       //if(checkPromotionOverTimerHandler!=null)checkPromotionOverTimerHandler.removeCallbacks(null);
    }

    protected  void CreatePageItems()
    {
        //if(checkPromotionOverTimerHandler!=null)checkPromotionOverTimerHandler.removeCallbacks(null);

        //not attached to activity
        if(isAdded()==false || items==null)return;


            if (currentViewMode == Enum.ViewMode.list) {
                CreateListViewModePageItems();
            }
            else
            {
                CreatePicViewModePageItems();
            }





    }

    @Override
    protected void SavedPictureResult(String strSavedPicPath) {


    }




    @Override
    public void onResume() {
        //common.Utility.ShowMessage("","resume",getActivity(),R.drawable.message);
        super.onResume();
        //only run this when is showing promotion item, need to start the timer again
        if(lngSelectedCategoryId==common.text_and_length_settings.PROMOTION_CATEGORY_ID && blnIsVisibleToUser)
        {CreatePageItems();}

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //View v = inflater.inflate(R.layout.layout_fragment_page_viewer, container, false);
        //FragmentView = v;
        FragmentView = inflater.inflate(R.layout.layout_fragment_page_viewer, container, false);
        //tblItems = (TableLayout)FragmentView.findViewById(R.id.tblItems);
        if(getArguments()!=null) {
            //add new menu item image on top right


            strCurrentPageIndex = getArguments().getString(Arg_PageIndex);

            if(strCurrentPageIndex.length()==0) return FragmentView;

            FragmentView.setTag(strCurrentPageIndex);

            CreatePageItems();




        }

        ((MyScrollView)FragmentView.findViewById(R.id.svPageScroll)).SetProperties(this);

        return FragmentView;
    }


    private void LoadPicture(ImageView imageView,String strPath)
    {
        if(strPath.isEmpty()) {
            imageView.setBackground(getResources().getDrawable(R.drawable.photo_not_available));
        }
        else
        {
            File imgFile = new  File(strPath);

            if(imgFile.exists()){

                imageView.setBackground(null);
                imageView.setImageBitmap(DecodeBitmapFile(strPath));




            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            blnIsVisibleToUser=true;
            if(lngSelectedCategoryId==common.text_and_length_settings.PROMOTION_CATEGORY_ID && isAdded())
            {CreatePageItems();}
        } else {
            blnIsVisibleToUser=false;
            //if(checkPromotionOverTimerHandler!=null)checkPromotionOverTimerHandler.removeCallbacks(null);
        }

    }
    private void UpdateInventoryCountInPicViewMode(HashMap<Long,Integer>map)
    {
        TableLayout tblItems = (TableLayout)FragmentView.findViewById(R.id.tblItems);
        MenuItemFlipablePicView v;

        for(int j =0;j<tblItems.getChildCount()-1;j++)//exclude last add new menu item row
        {
            TableRow tr =(TableRow) tblItems.getChildAt(j);
            for(int i=0;i<tr.getChildCount();i++)
            {
                if(tr.getChildAt(i) instanceof MenuItemFlipablePicView)
                {
                    v = (MenuItemFlipablePicView)tr.getChildAt(i);
                    if(map.containsKey(v.GetItemId())) {
                        v.UpdateInventoryCount(map.get(v.GetItemId()));
                    }
                }
            }
        }
    }
    private void UpdateInventoryCountInListViewMode(HashMap<Long,Integer>newlyInsertedQuantity)
    {
        TableLayout tblItems = (TableLayout)FragmentView.findViewById(R.id.tblItems);
        ItemObject io=null;
        long itemId=-1;
        int count=0;
        for(int j =0;j<tblItems.getChildCount()-1;j++)//exclude last add new menu item row
        {
            itemId = (Long)tblItems.getChildAt(j).getTag();

            if(newlyInsertedQuantity.containsKey(itemId))
            {
                    /*
                    found a match, need to update the item inventory and label
                    tblItem->row->ll1(img+ll2)
                    ll2(ll3+ll4)
                    ll3(tv+tvPrice)
                    ll4(imgInventory+tvInventory)
                    ll4 is our target
                    */
                TableRow row = (TableRow)tblItems.getChildAt(j);
                LinearLayout ll1 = (LinearLayout)row.getChildAt(0);
                LinearLayout ll2 = (LinearLayout)ll1.getChildAt(1);
                LinearLayout ll4 = (LinearLayout)ll2.getChildAt(1);
                TextView tvInventoryCount = (TextView)ll4.getChildAt(1);
                if(!common.myMenu.GetLatestItem(itemId).getDoNotTrackFlag()) {
                    count = Integer.parseInt(tvInventoryCount.getText()+"")-newlyInsertedQuantity.get(itemId);
                    //tvInventoryCount.setText(map.get(itemId) + "");
                    tvInventoryCount.setText(count + "");
                    tvInventoryCount.setTextColor((count <= 10) ? Color.RED : Color.BLACK);


                }
            }
        }

    }
    public void UpdateInventoryCount(HashMap<Long,Integer>newlyInsertedQuantity)
    {

        if(currentViewMode== Enum.ViewMode.list)
        {
            UpdateInventoryCountInListViewMode(newlyInsertedQuantity);
        }
        else
        {
            UpdateInventoryCountInPicViewMode(newlyInsertedQuantity);
        }
    }

    private void CreatePicViewModePageItems()
    {


        //boolean blnIsNegate=false;
        lstTvUnit = new ArrayList<Pair<Long,TextView>>();//reset
        int PageIndex = Integer.parseInt(getArguments().getString(Arg_PageIndex));
        final TableLayout tblItems = (TableLayout)FragmentView.findViewById(R.id.tblItems);
        int intStart = (PageIndex* itemPerPage);
        int intEnd = intStart+itemPerPage-1;
        intEnd = (items.size()-1<intEnd)?items.size()-1:intEnd;
        //add initial row 1st
        TableRow tr=new TableRow(getActivity());
        tblItems.addView(tr);
        int count=0;
        for(int i=intStart;i<=intEnd;i++) {
            if(count>=6) {
                count = 0;//reset
                //new row
                tr= new TableRow(getActivity());
                tblItems.addView(tr);
            }


            //add to container
            MenuItemFlipablePicView ll = new MenuItemFlipablePicView(getActivity());//LinearLayout(getActivity());
            ll.SetListener(this);
            ll.SetProperties(items.get(i),common.Utility.GetAtTheMomentItemCount(items.get(i).getID()));


            //set table row layout
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            trlp.setMargins(10,10,15,10);
            tr.addView(ll,trlp);
            count++;
        }

        if(((TableRow)tblItems.getChildAt(0)).getChildCount()==0)tblItems.removeViewAt(0);



        TapToAddTextView tvAddItem = new TapToAddTextView(getActivity());
        tvAddItem.SetListener(this);

        //set table row layout
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(100, 100);
        trlp.setMargins(10,10,15,10);
        if(count>=6) {
            //new row
            tr= new TableRow(getActivity());
            tblItems.addView(tr);
        }

        tr.addView(tvAddItem,trlp);


    }

    private void CreateListViewModePageItems()
    {

        lstTvUnit = new ArrayList<Pair<Long,TextView>>();//reset
        int PageIndex = Integer.parseInt(getArguments().getString(Arg_PageIndex));
        final TableLayout tblItems = (TableLayout)FragmentView.findViewById(R.id.tblItems);
        int intStart = (PageIndex* itemPerPage);
        //int intEnd = intStart+itemPerPage-1;
        intPageLastItemIndex = intStart+itemPerPage-1;
        //load all items if less than defined number
        intLastLoadedItemIndex = items.size()-1>intStart+common.text_and_length_settings.LIST_VIEW_MODE_NUMBER_ITEM_TO_LOAD_PER_BATCH-1?
                intStart+common.text_and_length_settings.LIST_VIEW_MODE_NUMBER_ITEM_TO_LOAD_PER_BATCH-1:items.size()-1;


        CreateListItemRow(intStart,tblItems);




        //add add new menu item row
        TapToAddTextView tvAddItem = new TapToAddTextView(getActivity());
        tvAddItem.SetListener(this);

        TableRow tr = ((TableRow)FragmentView.findViewById(R.id.trAddNewItem));
        tr.addView(tvAddItem);


        //make tap to add button to span two columns
        if(intLastLoadedItemIndex>0)
        {
            ((TableRow.LayoutParams)tvAddItem.getLayoutParams()).span=2;
            ((TableRow.LayoutParams)tvAddItem.getLayoutParams()).column=0;
        }



        TableRow.LayoutParams trlp = new TableRow.LayoutParams(common.Utility.DP2Pixel(common.text_and_length_settings.TAP_TO_ADD_BUTTON_WIDTH,getActivity()), ViewGroup.LayoutParams.WRAP_CONTENT);

        trlp.setMargins(10,0,0,20);

        tvAddItem.setLayoutParams(trlp);


        /*//no add new item option for promotion category
        if(lngSelectedCategoryId==common.text_and_length_settings.PROMOTION_CATEGORY_ID){
            if(tblItems.getChildCount()==1) {
                CreateNoDataInfoForPromotionPage(tblItems);
            }
            tr.setVisibility(View.GONE);
        }
        else
        {
            tr.setVisibility(View.VISIBLE);
        }*/

    }


    private void ContinueLoadListViewModeItem()
    {
        if(blnLoadingItems)return;
        if(intPageLastItemIndex==intLastLoadedItemIndex)return;


        blnLoadingItems = true;
        //boolean blnIsNegate=false;
        //lstTvUnit = new ArrayList<Pair<Long,TextView>>();//reset

        final TableLayout tblItems = (TableLayout)FragmentView.findViewById(R.id.tblItems);
        //1st remove the loading gif
        if(intLastLoadedItemIndex!=intPageLastItemIndex && intLastLoadedItemIndex<items.size()-1){

            tblItems.removeViewAt(tblItems.getChildCount()-2);
        }
        //start at the next item from last loaded index
        int intStart = intLastLoadedItemIndex+1;
        if(intStart>items.size()-1)return;
        //load all items if less than defined number
        intLastLoadedItemIndex = intPageLastItemIndex>intStart+common.text_and_length_settings.LIST_VIEW_MODE_NUMBER_ITEM_TO_LOAD_PER_BATCH-1?
                intStart+common.text_and_length_settings.LIST_VIEW_MODE_NUMBER_ITEM_TO_LOAD_PER_BATCH-1:intPageLastItemIndex;

        intLastLoadedItemIndex=(intLastLoadedItemIndex>items.size()-1)?items.size()-1:intLastLoadedItemIndex;
        CreateListItemRow(intStart,tblItems);


        //reset flag
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                blnLoadingItems = false;
            }
        }, 200);
    }

    private void CreateListItemRow(int intStart,TableLayout tblItems)
    {

        for(int i=intStart;i<=intLastLoadedItemIndex;i++)
        {



            MenuItemFlippableTableRow tr = new MenuItemFlippableTableRow(getActivity(),this,lngSelectedCategoryId,items.get(i).getID(),this);//items,common.myMenu);
            tr.setBackgroundColor(getResources().getColor(R.color.white_green));
            tr.SetProperties(items.get(i));


            TableLayout.LayoutParams tbllp = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            tbllp.setMargins(10, 10, 10, 10);
            tr.setLayoutParams(tbllp);

            tblItems.addView(tr, tblItems.getChildCount() - 1);//add on top of add new item

        }

        //show loading image at the bottom
        if(intPageLastItemIndex>intLastLoadedItemIndex && items.size()-1>intLastLoadedItemIndex)
        {
            ProgressBar pb = new ProgressBar(getActivity());
            TableRow tr = new TableRow(getActivity());
            tr.setBackgroundColor(getResources().getColor(R.color.white_green));
            tr.addView(pb);
            tblItems.addView(tr, tblItems.getChildCount() - 1);
        }


    }
    @Override
    public void onScrollChanged( MyScrollView scrollView,int x, int y, int old_x, int old_y) {
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        //load more item when user starts scrolling down
        if(diff==0 && blnIsVisibleToUser)
        {
            if(currentViewMode== Enum.ViewMode.list)
            {
                ContinueLoadListViewModeItem();
            }
            else
            {
                //already displaying all

            }
        }

    }

   /* @Override
    public int ListItemSingleTapped(long itemId,int initInventoryCount) {
        if(pageActivityListener!=null){return pageActivityListener.AddNewUnitToCart(itemId,1,initInventoryCount);}
        return 0;

    }*/


    @Override
    public int ListItemSingleTapped(long itemId, IToBeUpdatedInventoryView view, int initInventoryCount) {
        int count =0;
        if(pageActivityListener!=null){
            if(pageActivityListener.IsReceiptPanelBusy()) {
                common.Utility.ShowMessage(AppSettings.MESSAGE_APPLICATION_BUSY_TITLE,AppSettings.MESSAGE_APPLICATION_BUSY,getActivity(),R.drawable.exclaimation);
            }
            else {
                count = pageActivityListener.AddNewUnitToCart(itemId, 1, initInventoryCount);
                if (view != null) view.ItemMenuDialogUnitAdded(initInventoryCount - count);
            }
        }
        return count;
    }

    @Override
    public void ListItemDoubleTapped(long itemId, IToBeUpdatedInventoryView view, int initInventoryCount) {
        ShowItemMenuOptionPopup(itemId,view,initInventoryCount);
    }

    @Override
    public void InventoryPopupClicked(long itemId, IItemViewUpdateUnit callback) {

        if(pageActivityListener!=null)
        {
            pageActivityListener.ShowItemInventoryOption(itemId,callback);
        }


    }
    private void ShowItemMenuOptionPopup(long itemId,IToBeUpdatedInventoryView view,int inventoryCount)
    {

        if(pageActivityListener!=null)
        {
            pageActivityListener.ShowItemOptionPopup(itemId,view,inventoryCount);
        }
    }

}
