package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.mail.Store;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.CustomViewCtr.MainLinearLayout;
import tme.pos.CustomViewCtr.MenuItemFlippableTableRow;
import tme.pos.CustomViewCtr.MenuItemOptionOrderRow;
import tme.pos.CustomViewCtr.TapToAddTextView;
import tme.pos.Interfaces.IItemMenuOptionActivityListener;
import tme.pos.Interfaces.IToBeUpdatedInventoryView;

/**
 * Created by vanlanchoy on 2/28/2015.
 */
public class ItemMenuOptionPopup implements MenuItemOptionOrderRow.IitemChangeListener
,UnitTextWatcher.IUnitChangedListener, TapToAddTextView.ITappedListener{


    Context context;
    ArrayList<StoreItem> currentOrderLst;
    ArrayList<StoreItem> originalOrderLst;
    ArrayList<Integer> originalOrderLstIndexes;
    View dialogView;
    //PromotionAwarded pa;
    TextView tvTitle;
    ImageView itemImage;
    ImageView imgInventoryPic;
    ImageButton imgBtnEdit;
    TextView tvBarcode;
    TextView tvInventoryCount;
    Button btnApply;
    TextView tvOrderStatus;
    //AppSettings myAppSettings;
    TextView tvUnit;
    EditText txtUnit;
    LinearLayout llOrderList;
    //TapToAddTextView tvTapToAdd;
    boolean blnIsExistingOrder = false;
    //ItemInventoryOptionDialog.ICallback callback;
    //ArrayList<StoreItem>items;
    HashMap<Long,Integer>  mapInventoryRemainAtMoment;
    HashMap<Long,Integer> mapInitTotalUnitForSI;
    IItemMenuOptionActivityListener itemMenuOptionActivityListener;
    IToBeUpdatedInventoryView viewToCallback;
    PromotionObject promotionObject;
    HashMap<Long,Integer>promotionTotalUnitNeeded;




    //unit order edit text at the top
   @Override
    public void UnitChanged(int newUnit,int oldUnit) {
       //only promotion mode can see this control
       //wipe out the current record and recalculate again
       promotionTotalUnitNeeded.clear();
       HashMap<Long,Integer> tempHM;
       for(int i=0;i<promotionObject.ruleItems.size();i++)
       {
           tempHM = promotionObject.ruleItems.get(i);
           for(long key:tempHM.keySet())
           {
               int addUnit =tempHM.get(key) * newUnit;
               if(promotionTotalUnitNeeded.containsKey(key))
               {
                    promotionTotalUnitNeeded.put(key,promotionTotalUnitNeeded.get(key)+addUnit);
               }
               else
               {
                   promotionTotalUnitNeeded.put(key, addUnit);
               }
           }

       }

       //UpdateHints();
       //UpdateUnit(oldUnit-newUnit);

       /*int count =Integer.parseInt(tvUnit.getText()+"");
        count = count+(oldUnit-newUnit);
        tvUnit.setText(count+"");*/
    }

    @Override
    public void Tapped() {
        int currentUnitCount = Integer.parseInt(tvUnit.getText()+"");
        if(currentUnitCount>=common.text_and_length_settings.UNIT_LIMIT)
        {
            common.Utility.ShowMessage("Order","You have reached unit limit "+common.text_and_length_settings.UNIT_LIMIT,context,R.drawable.no_access);
            return;
        }
        currentUnitCount+=1;
        tvUnit.setText(currentUnitCount+"");

        //update each row item
        UpdateChildRowTotalUnitCount();

        CreateNewItemRow(true);
    }




    @Override
    public void AddModifier(long id) {

    }

    @Override
    public void Delete(MenuItemOptionOrderRow row) {
        /*int count =Integer.parseInt(tvUnit.getText()+"");
        count -=row.GetOrderedUnit();//deduct the removing row
        tvUnit.setText(count+"");*/
        UpdateUnitLabel(-row.GetOrderedUnit());
        if(promotionObject!=null)
        {
            currentOrderLst.remove(row.GetStoreItem());
            UpdateHints();
        }
        llOrderList.removeView(row);
        //UpdateChildRowTotalUnitCount();
    }

    @Override
    public void DeleteModifier(long id) {

    }

    @Override
    public void ItemChanged(long newID,long oldID,int unit,MenuItemOptionOrderRow row)
    {
        //only for promotion combo use
        if(promotionObject==null)return;

        //remove the old id from existing order
        UpdateStoreItemOrderList(newID,oldID,unit,row);

        int count = Integer.parseInt(txtUnit.getText()+"");
        UnitChanged(count,count);
        /*for(int i=0;i<currentOrderLst.size();i++) {
            if(currentOrderLst.get(i).item.getID()==oldID)
            {
                ItemObject io = common.myMenu.GetItem(newID);
                currentOrderLst.get(i).item = io;
                currentOrderLst.get(i).UnitOrder = unit;
            }
        }*/
        UpdateHints();
    }
    //row item edit text
    @Override
    public int UnitUpdate(int newUnit,int oldUnit,long itemId,MenuItemOptionOrderRow row) {

        if(promotionObject!=null) {
            UpdateStoreItemOrderList(itemId,itemId,newUnit,row);
            UpdateHints();
        }
        else {
            UpdateUnitLabel(newUnit - oldUnit);
        }


        return Integer.parseInt(tvUnit.getText()+"");
    }

    @Override
    public void AddNewRow(MenuItemOptionOrderRow clickedRow) {
        StoreItem si = (StoreItem) clickedRow.GetStoreItem().clone();
        si.UnitOrder=1;
        si.modifiers.clear();


        //get clicked row index
        for(int i=0;i<llOrderList.getChildCount();i++)
        {
            MenuItemOptionOrderRow row = (MenuItemOptionOrderRow)llOrderList.getChildAt(i);
            if(row.equals(clickedRow))
            {

                //create spinner row and add new item into current ordered list
                if(promotionObject!=null)
                {
                    currentOrderLst.add(i,si);
                    CreatePromotionItemRow(clickedRow.GetAdapter(),i,si,clickedRow.GetModifierList(),false,true);
                    //will trigger by spinner in new row
                    //UpdateHints();
                }
                else
                {
                    CreateItemRow(si,i,true,false);
                }
                return;
            }
        }

    }



    public ItemMenuOptionPopup(Context c,StoreItem si,boolean blnIsExistingOrder,int remaining
            ,IItemMenuOptionActivityListener l,IToBeUpdatedInventoryView callback
            ,int lstIndex)
    {
        ArrayList<StoreItem>lst = new ArrayList<StoreItem>();
        lst.add(si);
        ArrayList<Integer>lstIndexes = new ArrayList<Integer>();
        lstIndexes.add(lstIndex);
        HashMap<Long,Integer>map  = new HashMap<Long, Integer>();
        map.put(si.item.getID(),remaining);
        Instantiate(c,lst,blnIsExistingOrder,map,l,callback,lstIndexes);
    }
    //for combo item use
    public ItemMenuOptionPopup(Context c, ArrayList<StoreItem>siLst, boolean blnIsExistingOrder, HashMap<Long,Integer> remainingUnitCount,IItemMenuOptionActivityListener l
    ,IToBeUpdatedInventoryView callback,PromotionObject po,ArrayList<Integer>lstIndexes)//, ItemInventoryOptionDialog.ICallback callback)
    {
        promotionObject = po;
        Instantiate(c,siLst,blnIsExistingOrder,remainingUnitCount,l,callback,lstIndexes);
    }
    private ArrayList<StoreItem>CopyStoreItemList(ArrayList<StoreItem>lst)
    {
        ArrayList<StoreItem> newLst = new ArrayList<StoreItem>();
        for(int i=0;i<lst.size();i++)
        {
            newLst.add((StoreItem) lst.get(i).clone());
        }
        return newLst;
    }
    private void Instantiate(Context c, ArrayList<StoreItem>siLst, boolean blnIsExistingOrder, HashMap<Long,Integer> remainingUnitCount, IItemMenuOptionActivityListener l
    ,IToBeUpdatedInventoryView callback,ArrayList<Integer>lstIndexes)
    {
        context = c;
        promotionTotalUnitNeeded = new HashMap<Long, Integer>();
        this.blnIsExistingOrder = blnIsExistingOrder;
        this.mapInventoryRemainAtMoment = remainingUnitCount;
        //this.callback = callback;
        //this.items = siLst;
        this.originalOrderLstIndexes = lstIndexes;
        this.currentOrderLst =CopyStoreItemList(siLst);// new ArrayList<StoreItem>(siLst);
        this.originalOrderLst = siLst;
        mapInitTotalUnitForSI = CalculateTotalUnitNeeded(currentOrderLst);
        viewToCallback = callback;
        itemMenuOptionActivityListener = l;
        /*//subtract initial unit if is existing
        if(blnIsExistingOrder)
        {
            for(Long id:mapInitTotalUnitForSI.keySet())
            {
                mapInventoryRemainAtMoment.put(id,mapInventoryRemainAtMoment.get(id)-mapInitTotalUnitForSI.get(id));

            }
        }*/

        ShowMenuItemPopup();
    }
    private void UpdateStoreItemOrderList(long newID,long oldID,int unit,MenuItemOptionOrderRow row)
    {
        if(currentOrderLst.size()==0)return;
        //remove the old id from existing order
        int targetIndex=-1;
        for(int i=0;i<llOrderList.getChildCount();i++)
        {
            if(llOrderList.getChildAt(i).equals(row))
            {
                targetIndex=i;
            }
        }

        if(targetIndex==-1)return;

        //perform another check make sure is the target item
        if(currentOrderLst.get(targetIndex).item.getID()==oldID)
        {
            //perform only when user change item or simply updating unit count, else just skip this part
            if(newID!=oldID)
            {
               ItemObject io = common.myMenu.GetLatestItem(newID);
               currentOrderLst.get(targetIndex).item = io;
            }
           currentOrderLst.get(targetIndex).UnitOrder = unit;
        }
        /*for(int i=0;i<currentOrderLst.size();i++) {
            if(currentOrderLst.get(i).item.getID()==oldID)
            {
                //perform only when user change item, else just skip this part
                if(newID!=oldID)
                {
                    ItemObject io = common.myMenu.GetItem(newID);
                    currentOrderLst.get(i).item = io;
                }
                currentOrderLst.get(i).UnitOrder = unit;
            }
        }*/
    }
    private void UpdateChildRowTotalUnitCount()
    {
        int currentUnitCount = Integer.parseInt(tvUnit.getText()+"");
        for(int i=0;i<llOrderList.getChildCount();i++) {
            if(llOrderList.getChildAt(i) instanceof MenuItemOptionOrderRow)
                ((MenuItemOptionOrderRow) llOrderList.getChildAt(i)).UpdateTotalOrderedUnitCount(currentUnitCount);
        }
    }
    private void UpdateHints()
    {
        if(promotionTotalUnitNeeded.size()==0)return;
        long id=0;
        boolean blnFound=false;
        HashMap<Long,Boolean>processed = new HashMap<Long, Boolean>();
        HashMap<Integer,ArrayList<Long>>overlapped = new HashMap<Integer, ArrayList<Long>>();
        tvOrderStatus.setText("");//reset
        String strMsg="";
        HashMap<Integer,Boolean>used = new HashMap<Integer, Boolean>();
        //make a copy to calculate
        HashMap<Long,Integer>records = new HashMap<Long, Integer>(promotionTotalUnitNeeded);
        currentOrderLst = GetSelectedItems();
        //find out which category the item belongs to
        for(int i=0;i<currentOrderLst.size();i++)
        {
            id = currentOrderLst.get(i).item.getID();

            /*//booking calculate how many unit is required for current order
            if(!records.containsKey(id))
            {
                //is "Any" item from this category id
                //update id
                id =currentOrderLst.get(i).item.getParentID();

                //records.put(id, records.get(id) - currentOrderLst.get(i).UnitOrder);
            }
            else {
                //records.put(id, records.get(id) - currentOrderLst.get(i).UnitOrder);
            }*/
            //check whether the newly selected item is for 'Any', always check for individual then only 'Any'
            if(!records.containsKey(id))
            {
                //update 'Any' record, with new id
                id = currentOrderLst.get(i).item.getParentID();
                records.put(id, records.get(id) - currentOrderLst.get(i).UnitOrder);
            }
            else
            {
                int newUpdateQuantity =records.get(id) - currentOrderLst.get(i).UnitOrder;
                if(newUpdateQuantity<0)
                {
                    //already fulfilled individual item, try 'Any'
                    if(records.containsKey(currentOrderLst.get(i).item.getParentID()))
                    {
                        //update 'Any' record, with new id
                        id = currentOrderLst.get(i).item.getParentID();
                        records.put(id, records.get(id) - currentOrderLst.get(i).UnitOrder);
                    }
                    else
                    {
                        //force insert into individual since no 'Any'
                        records.put(id, newUpdateQuantity);
                    }
                }
                else
                {
                    //regular record
                    records.put(id, newUpdateQuantity);
                }
                //records.put(id, records.get(id) - currentOrderLst.get(i).UnitOrder);
            }


            //keeping track of multiple items have been selected to fulfill the one item category in promotion, example: 1 (beef/chicken) + soda but user selected both + soda
            for(int j=0;j<promotionObject.ruleItems.size();j++)
            {

                if(promotionObject.ruleItems.get(j).containsKey(id))
                {
                    blnFound = false;
                    if(!overlapped.containsKey(j))overlapped.put(j,new ArrayList<Long>());
                    //add to list
                    for(int k=0;k<overlapped.get(j).size();k++)
                    {
                        if(overlapped.get(j).get(k).longValue()==id)
                        {
                            blnFound=true;
                        }
                    }
                    //insert only once
                    if(!blnFound)
                    overlapped.get(j).add(currentOrderLst.get(i).item.getID());
                }
            }
        }
        //now build the hint
        for(int i=0;i<currentOrderLst.size();i++)
        {
            id = currentOrderLst.get(i).item.getID();
            if(!records.containsKey(id))
            {
                id = currentOrderLst.get(i).item.getParentID();
            }
            //check overlapped
            int index = MultipleItemInSameGroup(id,overlapped);
            if(index>-1)
            {
                if(used.containsKey(index))continue;
                used.put(index,true);
                ArrayList<Long>temp =overlapped.get(index);
                strMsg += "one of (";
                for(int k=0;k<temp.size();k++) {
                    strMsg += common.myMenu.GetLatestItem(temp.get(k)).getName()+"/";
                }
                strMsg=strMsg.substring(0,strMsg.length()-1)+"), ";
            }
            else {
                //check short/over
                if (records.get(id) != 0 && !processed.containsKey(id)) {
                    strMsg += records.get(id) + "X " + currentOrderLst.get(i).item.getName() + ", ";
                    processed.put(id, true);
                }
            }
        }
        if(strMsg.length()>0)
        {
            strMsg="You need "+strMsg;
            strMsg = strMsg.substring(0,strMsg.length()-2);
        }
        tvOrderStatus.setText(strMsg);
    }
    private int MultipleItemInSameGroup(long id,HashMap<Integer,ArrayList<Long>>overlapped)
    {
        for(int i:overlapped.keySet())
        {
            ArrayList<Long> temp = overlapped.get(i);
            for(int j=0;j<temp.size();j++)
            {
                if(temp.get(j).longValue()==id && temp.size()>1)return i;
            }
            //if(overlapped.get(i).size()>1)return i;
        }
        return -1;
    }
  protected Bitmap DecodeBitmapFile(String strPath)
  {
      // First decode with inJustDecodeBounds=true to check dimensions
      final BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;

      // Calculate inSampleSize
      options.inSampleSize =2;// common.Utility.CalculateInSampleSize(options, 100, 100);

      // Decode bitmap with inSampleSize set
      options.inJustDecodeBounds = false;

      return BitmapFactory.decodeFile(strPath, options);
  }
  private void LoadPicture(ImageView imageView,String strPath)
  {
      if(strPath.isEmpty()) {
          imageView.setBackground(context.getResources().getDrawable(R.drawable.photo_not_available));
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
    private void SetupPromotionComboEnvironment()
    {
        imgInventoryPic.setVisibility(View.GONE);
        tvBarcode.setVisibility(View.GONE);
        tvInventoryCount.setVisibility(View.GONE);
        imgBtnEdit.setVisibility(View.GONE);
        //itemImage.setBackgroundColor(context.getResources().getColor(promotionObject.GetDiscountColor().value));
        tvTitle.setText(promotionObject.GetTitle());
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplyUnitOrder();
            }
        });
        tvUnit.setVisibility(View.GONE);
        txtUnit.setText("1");
        txtUnit.setSelection(txtUnit.getText().length());
        txtUnit.addTextChangedListener(new UnitTextWatcher(txtUnit
                ,0
                ,common.text_and_length_settings.TOTAL_PRICE_LIMIT,context,null));

        CreatePromotionItemRows(1);

        UnitChanged(1,1);
        UpdateHints();

    }
    private void ApplyUnitOrder()
    {
        currentOrderLst.clear();
        currentOrderLst = new ArrayList<StoreItem>(originalOrderLst);
        int count = Integer.parseInt(txtUnit.getText()+"");
        tvOrderStatus.setText("");
        CreatePromotionItemRows(count);

        //UnitChanged(count,count);
        //UpdateHints();
    }
    private void SetupRegularOrderEnvironment()
    {
        LoadPicture(itemImage, currentOrderLst.get(0).item.getPicturePath());
        tvTitle.setText(currentOrderLst.get(0).item.getName());
        btnApply.setVisibility(View.GONE);
        tvOrderStatus.setVisibility(View.GONE);
        if(!currentOrderLst.get(0).item.getDoNotTrackFlag()) {
            TextView tvInventoryCount = (TextView) dialogView.findViewById(R.id.tvInventoryCount);
            tvInventoryCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            tvInventoryCount.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.BOLD);
            //regular order consisting just one item inside the map
            int count = mapInventoryRemainAtMoment.get(mapInventoryRemainAtMoment.keySet().iterator().next());
            /*if(blnIsExistingOrder)
            {
                count +=originalOrderLst.get(0).UnitOrder;
            }*/
            tvInventoryCount.setText(count+ "");
            tvInventoryCount.setGravity(Gravity.CENTER);
            tvInventoryCount.setTextColor((count<10)? Color.RED:Color.BLACK);
        }
        else
        {
            /*dialogView.findViewById(R.id.imgInventoryPic).setVisibility(View.GONE);
            dialogView.findViewById(R.id.tvInventoryCount).setVisibility(View.GONE);*/
            imgInventoryPic.setVisibility(View.GONE);
            tvInventoryCount.setVisibility(View.GONE);
        }



        if(currentOrderLst.get(0).item.getBarcode()>0) {
            ((TextView) dialogView.findViewById(R.id.tvBarcode)).setText("Barcode: " + (currentOrderLst.get(0).item.getBarcode() == 0 ? "N/A" : currentOrderLst.get(0).item.getBarcode()));
            ((TextView) dialogView.findViewById(R.id.tvBarcode)).setGravity(Gravity.CENTER);
        }
        else
        {
            //dialogView.findViewById(R.id.tvBarcode).setVisibility(View.GONE);
            tvBarcode.setVisibility(View.GONE);
        }




        tvUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE);
        tvUnit.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);

        txtUnit.setVisibility(View.GONE);

        CreateItemRow(currentOrderLst.get(0),false,true);
        //insert add new row  option
        //CreateTapToAddControl();
    }
    private void CreateItemRow(StoreItem si,int position,boolean blnAllowDelete,boolean blnAllowAddNewRow)
    {
        MenuItemOptionOrderRow defaultRow = new MenuItemOptionOrderRow(context);
        HashMap<Long,ArrayList<ModifierObject>>mos = new HashMap<Long,ArrayList<ModifierObject>>();
        mos.put(-1l,common.myMenu.GetModifiers(Enum.ModifierType.global,-1l));
        mos.put(si.item.getID(),common.myMenu.GetModifiers(Enum.ModifierType.individual,si.item.getID()));
        //mos.add(common.myMenu.GetModifiers(Enum.ModifierType.individual_and_global,si.item.getID()));
        if(!blnAllowDelete){defaultRow.HideDeleteOption();}
        if(!blnAllowAddNewRow){defaultRow.HideAddNewRowOption();}
        defaultRow.SetProperties(this, si
                ,mos
                ,si.UnitOrder);
        llOrderList.addView(defaultRow,position);
        UpdateUnitLabel(si.UnitOrder);


    }
    private void CreatePromotionItemRow(SpinnerBaseAdapter<ItemObject> spinnerAdapter,int insertIndex
    ,StoreItem si,HashMap<Long,ArrayList<ModifierObject>> mos,boolean blnHideDelete,boolean blnHideAdd)
    {
        insertIndex=(insertIndex==-1)?llOrderList.getChildCount():insertIndex;
        MenuItemOptionOrderRow defaultRow = new MenuItemOptionOrderRow(context);
        defaultRow.SetProperties(this, si
                ,mos
                ,-1,spinnerAdapter);
        if(blnHideAdd)defaultRow.HideAddNewRowOption();
        if(blnHideDelete)defaultRow.HideDeleteOption();
        llOrderList.addView(defaultRow,insertIndex);
    }
    private void CreatePromotionItemRows(int intOrder)
    {
        int count=0;
        ItemObject io;
        CategoryObject co;
        ItemObject[] ios;
        llOrderList.removeAllViews();
        SpinnerBaseAdapter<ItemObject> spinnerAdapter;
        HashMap<Long,ArrayList<ModifierObject>>mos = new HashMap<Long,ArrayList<ModifierObject>>();
        //ArrayList<ArrayList<ModifierObject>>mos = new ArrayList<ArrayList<ModifierObject>>();
        //MenuItemOptionOrderRow defaultRow;
        StoreItem si;
        mos.put(-1l,common.myMenu.GetModifiers(Enum.ModifierType.global,-1));
        for(int i=0;i<promotionObject.ruleItems.size();i++)
        {
            HashMap<Long,Integer> map = promotionObject.ruleItems.get(i);
            //need to get all the child item size if the current id is category id
            if(map.size()==1 && common.myMenu.GetCategory(map.keySet().iterator().next())!=null)
            {
                ArrayList<ItemObject> lst = common.myMenu.GetCategoryItems(map.keySet().iterator().next(),true);
                ios = new ItemObject[lst.size()];
                for(int j=0;j<ios.length;j++)
                {
                    ios[j]=lst.get(j);

                    if(mos.containsKey(lst.get(j).getID()))mos.remove(lst.get(j).getID());
                    mos.put(lst.get(j).getID(),common.myMenu.GetModifiers(Enum.ModifierType.individual,lst.get(j).getID()));
                }
            }
            else
            {
                ios = new ItemObject[map.size()];
                count=0;
                for(long key:map.keySet())
                {
                    io = common.myMenu.GetLatestItem(key);
                    if(io==null)
                    {
                        continue;
                       /* if(common.myMenu.GetCategory(key)==null)
                        {
                            //do nothing and let it continue
                        }
                        else
                        {
                            //random pick one from this category
                            ArrayList<ItemObject> lst =common.myMenu.GetCategoryItems(key,true);
                            if(lst.size()>0) {
                                ios[count++] = lst.get(0);
                            }
                        }*/
                    }
                    else {
                        ios[count++] = common.myMenu.GetLatestItem(key);
                    }

                    if(mos.containsKey(key))mos.remove(key);
                    mos.put(key,common.myMenu.GetModifiers(Enum.ModifierType.individual,key));
                }
            }

            if(ios[0]!=null) {
                si = new StoreItem(ios[0]);
                if (!map.containsKey(ios[0].getID())) {
                    si.UnitOrder = map.get(ios[0].getParentID()) * intOrder;
                } else {
                    si.UnitOrder = map.get(ios[0].getID()) * intOrder;
                }
                spinnerAdapter = new SpinnerBaseAdapter<ItemObject>(ios, context, 20);
                CreatePromotionItemRow(spinnerAdapter, -1, si, mos, true, false);
            }
            /*defaultRow = new MenuItemOptionOrderRow(context);
            defaultRow.HideDeleteOption();
            defaultRow.SetProperties(this, si
                    ,common.myMenu.GetModifiers(Enum.ModifierType.individual_and_global,ios[0].getID())
                    ,-1,spinnerAdapter);
            llOrderList.addView(defaultRow);*/
        }

    }
    void UpdateUnitLabel(int toAdd)
    {
        int unitCount =Integer.parseInt(tvUnit.getText()+"");
        unitCount +=toAdd;
        tvUnit.setText(unitCount+"");
    }
    private void CreateItemRow(StoreItem si,boolean blnAllowDelete,boolean blnAllowAddNewRow)
    {

        CreateItemRow(si,llOrderList.getChildCount(),blnAllowDelete,blnAllowAddNewRow);


    }

    private void CreateNewItemRow(boolean blnAllowDelete)
    {
        HashMap<Long,ArrayList<ModifierObject>>mos = new HashMap<Long, ArrayList<ModifierObject>>();
        mos.put(-1l,common.myMenu.GetModifiers(Enum.ModifierType.global,-1l));
        mos.put(currentOrderLst.get(0).item.getID(),common.myMenu.GetModifiers(Enum.ModifierType.individual,currentOrderLst.get(0).item.getID()));
        MenuItemOptionOrderRow defaultRow = new MenuItemOptionOrderRow(context);
        if(!blnAllowDelete)
        {defaultRow.HideDeleteOption();}
        StoreItem siCloned = (StoreItem)currentOrderLst.get(0).clone();
        siCloned.modifiers.clear();//clear all existing modifier in order to start clean
        defaultRow.SetProperties(this,siCloned,mos,1);
        defaultRow.UpdateTotalOrderedUnitCount(Integer.parseInt(tvUnit.getText()+""));
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lllp.topMargin=common.Utility.DP2Pixel(20,context);
        llOrderList.addView(defaultRow,llOrderList.getChildCount()-1,lllp);//insert a position before tap to add


    }
    private void ShowMenuItemPopup()
    {




        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Get the layout inflater
        LayoutInflater inflater = ((MainUIActivity)context).getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(R.layout.layout_menu_item_option_popup_window, null);
        //construct modifier items
        imgInventoryPic=(ImageView)dialogView.findViewById(R.id.imgInventoryPic);
        tvBarcode=(TextView)dialogView.findViewById(R.id.tvBarcode);
        tvInventoryCount=(TextView)dialogView.findViewById(R.id.tvInventoryCount);
        tvOrderStatus = (TextView)dialogView.findViewById(R.id.tvOrderStatus);
        imgBtnEdit = (ImageButton)dialogView.findViewById(R.id.imgEdit);
        //ShowModifiers(dialogView);
        llOrderList =((LinearLayout)dialogView.findViewById(R.id.llOrderList));
        itemImage = (ImageView)dialogView.findViewById(R.id.imgItemPic);
        btnApply = (Button)dialogView.findViewById(R.id.btnApply);
        tvTitle = (TextView)dialogView.findViewById(R.id.tvTitle);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE);
        tvTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.NORMAL);

        tvUnit = (TextView)dialogView.findViewById(R.id.tvUnit);
        tvUnit.setText("0");
        txtUnit = (EditText) dialogView.findViewById(R.id.txtUnit);
        txtUnit.setText("0");
        if(promotionObject==null) {
            SetupRegularOrderEnvironment();
        }
        else
        {
            SetupPromotionComboEnvironment();
        }









        builder.setView(dialogView);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(itemMenuOptionActivityListener!=null){itemMenuOptionActivityListener.DialogDismissed();}
                //((MainUIActivity)context).SetPopupShow(false);
            }
        });


        final AlertDialog ad = builder.create();

        common.control_events.SetOnTouchImageButtonEffect(imgBtnEdit,R.drawable.green_border_outer_glow_edit,R.drawable.green_border_edit);
        imgBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(itemMenuOptionActivityListener!=null){
                    itemMenuOptionActivityListener.SetDialogPopupFlag(false);
                    itemMenuOptionActivityListener.EditItem(currentOrderLst.get(0).item);
                }
                //((MainUIActivity)context).SetPopupShow(false);//reset the flag, edit menu option will set flag again later
                //((MainUIActivity)context).imgBtnAddNewMenuItem_Click(null, true, currentOrderLst.get(0).item);
                ad.dismiss();
            }
        });
        ImageButton imgBtnDismissDialog = (ImageButton)dialogView.findViewById(R.id.imgBtnCancel);
        common.control_events.SetOnTouchImageButtonEffect(imgBtnDismissDialog,R.drawable.green_border_outer_glow_cancel,R.drawable.green_border_cancel);
        imgBtnDismissDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        ImageButton imgBtnOK = (ImageButton)dialogView.findViewById(R.id.imgBtnOK);
        common.control_events.SetOnTouchImageButtonEffect(imgBtnOK,R.drawable.green_border_outer_glow_ok,R.drawable.green_border_ok);
        imgBtnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((MainUIActivity) context).GetCurrentSubReceiptIndex() == -1)
                {
                    common.Utility.ShowMessage("Add item","Add to 'All' receipt is prohibited",context,R.drawable.no_access);
                    return;
                }

                if(((MainUIActivity) context).GetCurrentCart().blnIsLock)
                {
                    common.Utility.ShowMessage("Add", "Cannot add item to this receipt once it/split receipt has been paid.", context, R.drawable.no_access);
                    return;
                }
                if(!HasSufficientStock())
                {
                    return;
                }

                AddToCart();

                ad.dismiss();
            }
        });

      /*  //set layout height according to the item text length
        //using two line if more than 29
        //ScrollView svReceipt = (ScrollView)dialogView.findViewById(R.id.svReceipt);
        if(tvTitle.getText().length()>29){

            //svReceipt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,700));
            svReceipt.getLayoutParams().height=MainUIActivity.DP2Pixel(context.getResources().getDimension(R.dimen.dp_receipt_panel_height_in_menu_item_option_popup_window_when_title_use_two_lines),
                    context);
        }
        else
        {
            //svReceipt.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,600));
            svReceipt.getLayoutParams().height=MainUIActivity.DP2Pixel(context.getResources().getDimension(R.dimen.dp_receipt_panel_height_in_menu_item_option_popup_window_when_title_use_one_line),
                    context);
        }*/

        //order does matter
        ad.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_Sliding;

        //final View localView = dialogView;
        android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.show();

            }
        },280);




    }
    private void UpdateOrders()
    {
        currentOrderLst.clear();
        MenuItemOptionOrderRow row;
        for(int i=0;i<llOrderList.getChildCount();i++)
        {

                row = (MenuItemOptionOrderRow)llOrderList.getChildAt(i);
                currentOrderLst.add(row.GetStoreItem());

        }
    }

    private boolean HasSufficientStock()
    {
        boolean blnFound=false;
        UpdateOrders();
        HashMap<Long,Integer> tempMap = CalculateTotalUnitNeeded(currentOrderLst);
        if(!blnIsExistingOrder)
        {
            //invoke by single/double tapped or barcode search to add new

            //deduct those newly added and not in the previous map
            for(long key:tempMap.keySet())
            {
                    mapInventoryRemainAtMoment.put(key,common.Utility.GetAtTheMomentItemCount(key));
                    int count =mapInventoryRemainAtMoment.get(key);
                    count -= tempMap.get(key);
                    mapInventoryRemainAtMoment.put(key,count);


            }

        }
        else
        {
            //edit from main receipt item panel
            Long[]keys = mapInitTotalUnitForSI.keySet().toArray(new Long[mapInitTotalUnitForSI.keySet().size()]);
            for(int i=keys.length-1;i>-1;i--)
            {
                blnFound=false;
                for(StoreItem si:currentOrderLst)
                {
                    if(keys[i].longValue()==si.item.getID())
                    {
                        blnFound = true;
                        break;
                    }

                }

                if(!blnFound)
                {
                    int unit =mapInitTotalUnitForSI.get(keys[i]);
                    mapInventoryRemainAtMoment.put(keys[i],mapInventoryRemainAtMoment.get(keys[i])+unit);

                    //and remove it from the initial map
                    mapInitTotalUnitForSI.remove(keys[i]);
                }
            }

            //readjust stock availability
            //comparing initial unit with new unit
            for(long key:mapInitTotalUnitForSI.keySet())
            {
                int count =mapInitTotalUnitForSI.get(key)-tempMap.get(key);//exclude original existing unit if is editing receipt item
                count += mapInventoryRemainAtMoment.get(key);
                mapInventoryRemainAtMoment.put(key,count);
            }

            //now deduct those newly added and not in the previous map
            for(long key:tempMap.keySet())
            {
                if(!mapInitTotalUnitForSI.containsKey(key))
                {
                    int count =mapInventoryRemainAtMoment.get(key);
                    count -= tempMap.get(key);
                    mapInventoryRemainAtMoment.put(key,count);

                }
            }
        }


        //update existing map
        mapInitTotalUnitForSI = tempMap;


        //check stock
        for(long key:mapInventoryRemainAtMoment.keySet())
        {
            if(mapInventoryRemainAtMoment.get(key)<0) {

                for(StoreItem si:currentOrderLst)
                {
                    if(si.item.getID()==key && !si.item.getDoNotTrackFlag())
                    {
                        common.Utility.ShowMessage("Add", "Insufficient stock for item "+si.item.getName(), context, R.drawable.no_access);
                        return false;
                    }
                }

            }

        }

       return true;
    }
    //group by item id to get a total count for each item
    private HashMap<Long,Integer>CalculateTotalUnitNeeded(ArrayList<StoreItem>lst)
    {
        HashMap<Long,Integer> tempMap = new HashMap<Long, Integer>();

        for(StoreItem si:lst) {

            if(!tempMap.containsKey(si.item.getID()))
            {
                tempMap.put(si.item.getID(),0);
            }

            tempMap.put(si.item.getID(),tempMap.get(si.item.getID())+si.UnitOrder);
        }

        return tempMap;
    }


    private ArrayList<StoreItem>GetSelectedItems()
    {
        ArrayList<StoreItem>lst = new ArrayList<StoreItem>();
        for(int i=0;i<llOrderList.getChildCount();i++)
        {
            lst.add(((MenuItemOptionOrderRow)llOrderList.getChildAt(i)).GetStoreItem());
        }

        return lst;
    }
    private void AddToCart()
    {

        if(itemMenuOptionActivityListener!=null) {
            if(itemMenuOptionActivityListener.IsReceiptPanelBusy()) {
                common.Utility.ShowMessage(AppSettings.MESSAGE_APPLICATION_BUSY_TITLE,AppSettings.MESSAGE_APPLICATION_BUSY,context,R.drawable.exclaimation);
                return;
            }
        }
        if(blnIsExistingOrder)
        {
            //return the receipt row index together to update the unit count
            if(itemMenuOptionActivityListener!=null)
            {
                itemMenuOptionActivityListener.UpdateCurrentCart(GetSelectedItems(),originalOrderLstIndexes);
            }
        }
        else
        {
            //add new entry to receipt
            if(itemMenuOptionActivityListener!=null)
            {
                itemMenuOptionActivityListener.InsertNewItemIntoCurrentCart(GetSelectedItems(),viewToCallback);
            }
        }


    }






}
