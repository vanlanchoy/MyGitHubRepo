package tme.pos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.PromotionObject;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.PromotionAddRuleItemSelectionRow;
import tme.pos.Interfaces.IAddPromotionRuleItemRowActivityListener;
import tme.pos.Interfaces.ICreateCustomListActivityListener;
import tme.pos.Interfaces.ICreatePromotionActivityListener;

/**
 * Created by kchoy on 3/8/2016.
 */
public class SelectPromotionItemRowDialog extends Dialog implements IAddPromotionRuleItemRowActivityListener {

    ICreatePromotionActivityListener listener;
    ICreateCustomListActivityListener customListListener;
    int customPageIndex;
    ArrayList<Long>currentPageList;
    PromotionObject promotionObject;
    LinearLayout llParent;
    boolean blnEditMode;
    int listIndex=-1;
    String strItemString="";
    String strTitle;
    EditText txtTitle;
    TextView tvMsg;
    boolean blnIncludeAnySelection;
    public SelectPromotionItemRowDialog(Context c, ICreateCustomListActivityListener l, ArrayList<Long>lst,int pageIndex,String strTitle,boolean blnIncludeAnySelection)
    {
        super(c);
        this.customListListener = l;
        customPageIndex = pageIndex;
        currentPageList = lst;
        this.strTitle = strTitle;
        this.blnIncludeAnySelection = blnIncludeAnySelection;
    }
    public SelectPromotionItemRowDialog(Context c, ICreatePromotionActivityListener l, PromotionObject po,boolean blnIncludeAnySelection
            )
    {
        super(c);
        Configure(l,"",po,blnIncludeAnySelection);
    }
    public SelectPromotionItemRowDialog(Context c, ICreatePromotionActivityListener l, String strItems
            , PromotionObject po,int listIndex,boolean blnIncludeAnySelection)
    {
        super(c);
        blnEditMode = true;
        this.listIndex = listIndex;
        Configure(l, strItems,po,blnIncludeAnySelection);
    }
    private void Configure(ICreatePromotionActivityListener l,String strItems,PromotionObject po,boolean blnIncludeAnySelection)
    {


        this.listener =l;
        this.promotionObject = po;
        this.strItemString = strItems;
        this.blnIncludeAnySelection = blnIncludeAnySelection;

    }
    void EditSelectedRuleItems()
    {
        /**unit|id|parent,unit|id|parent**/
        ItemObject io;
        StoreItem si;
        long itemId;
        if(strItemString.trim().length()>0) {
            PromotionAddRuleItemSelectionRow row = null;
            String[] items = strItemString.split(",");
            for (int i = 0; i < items.length; i++) {
                if (i > 0) {//use the default row 1st
                    row = new PromotionAddRuleItemSelectionRow(getContext(),blnIncludeAnySelection);
                    row.ShowAddOption(false);
                    row.AddListener(this);
                    llParent.addView(row, llParent.getChildCount() - 1);
                }
                else{
                    row = (PromotionAddRuleItemSelectionRow)llParent.getChildAt(i);
                }
                String[] values = items[i].split("\\|");
                itemId =Long.parseLong(values[1]);
                io = common.myMenu.GetLatestItem(itemId);
                if(io==null)
                {
                    io = new ItemObject(itemId,"Any",itemId,"0","",false,0,1);
                }
                si = new StoreItem(io);
                si.UnitOrder = Integer.parseInt(values[0]);
                //}

                row.SetProperties(si);
            }
        }
    }

    void EditCustomList()
    {
        //hide dialog message
        tvMsg.setVisibility(View.GONE);
        //show edit tile
        txtTitle.setVisibility(View.VISIBLE);
        txtTitle.setText(strTitle);
        txtTitle.setSelection(strTitle.length());
        ItemObject io;
        StoreItem si;
        PromotionAddRuleItemSelectionRow row = null;
            for (int i = 0; i < currentPageList.size(); i++) {
                if (i > 0) {//use the default row 1st
                    row = new PromotionAddRuleItemSelectionRow(getContext(),blnIncludeAnySelection);
                    row.ShowAddOption(false);
                    row.AddListener(this);
                    llParent.addView(row, llParent.getChildCount() - 1);
                }
                else{
                    row = (PromotionAddRuleItemSelectionRow)llParent.getChildAt(i);
                }

                io = common.myMenu.GetLatestItem(currentPageList.get(i));
                si = new StoreItem(io);
                si.UnitOrder = 1;
                row.SetProperties(si);
                row.HideUnitComponent();
            }

        if(currentPageList.size()==0)
        {
            //update the default row to hide unit component
            row =(PromotionAddRuleItemSelectionRow) llParent.getChildAt(0);
            row.HideUnitComponent();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_select_promotion_item_dialog_ui);

        llParent =  (LinearLayout)findViewById(R.id.llParent);
        PromotionAddRuleItemSelectionRow row = ((PromotionAddRuleItemSelectionRow)llParent.getChildAt(0));
        row.AddListener(this);
        row.SetToIncludeAnySelection(true);


        //dialog message
        tvMsg = (TextView)findViewById(R.id.tvMsg);

        //title
        txtTitle = (EditText) findViewById(R.id.txtTitle);

        //ok button
        final TextView tv = (TextView)findViewById(R.id.tvOK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(IsItemExistOnList())
                {
                    return;
                }*/
                tv.setVisibility(View.INVISIBLE);
                AddItem();
                dismiss();
            }
        });

        if(customListListener== null)
        {
            //for promotion select rule item
            EditSelectedRuleItems();
        }
        else
        {
            //for main activity custom list
            EditCustomList();
        }
        //tv.setTypeface(null,Typeface.BOLD);
    }
    private boolean IsItemExistOnList()
    {
        if(promotionObject==null)return false;
        for(int i=0;i<promotionObject.ruleItems.size();i++)
        {
            if(i!=listIndex)//skip one self
            {
                for(int j=0;j<llParent.getChildCount();j++)
                {
                    if (llParent.getChildAt(j) instanceof PromotionAddRuleItemSelectionRow) {
                        PromotionAddRuleItemSelectionRow row = (PromotionAddRuleItemSelectionRow) llParent.getChildAt(j);
                        StoreItem si = row.GetSelectedItem();
                        if(si==null)continue;
                        if(promotionObject.ruleItems.get(i).containsKey(si.item.getID()))
                        {
                            common.Utility.ShowMessage("Add","Item "+si.item.getName()+ " already existed on the list",getContext(),R.drawable.no_access);
                            return true;
                        }
                    }
                }
            }
        }
            return false;
    }
    private void AddItem()
    {
        HashMap<Long,Integer>hashMap = new HashMap<Long, Integer>();
        HashMap<Long,Boolean>customListHM = new HashMap<Long, Boolean>();
        //ArrayList<Long> newCustomList = new ArrayList<Long>();
        for(int i=0;i<llParent.getChildCount();i++)
        {
            if(llParent.getChildAt(i) instanceof PromotionAddRuleItemSelectionRow) {
                PromotionAddRuleItemSelectionRow row = (PromotionAddRuleItemSelectionRow) llParent.getChildAt(i);
                StoreItem si = row.GetSelectedItem();
                if(si==null)continue;
                //newCustomList.add(si.item.getID());
                customListHM.put(si.item.getID(),true);
                if (!hashMap.containsKey(si.item.getID())) {
                    hashMap.put(si.item.getID(), 0);
                }
                hashMap.put(si.item.getID(), hashMap.get(si.item.getID()) + si.UnitOrder);
            }
        }
        if(hashMap.size()==0)return;

        if(listener!=null) {
            if (blnEditMode) {
                listener.UpdateRuleItemGroup(hashMap,listIndex);
            } else {
                listener.AddRuleItemGroup(hashMap);
            }
        }
        else if(customListListener!=null)
        {

            String strNewTitle =txtTitle.getText().length()>0?txtTitle.getText().toString():"List "+customPageIndex;
            customListListener.UpdateList(new ArrayList<Long>(customListHM.keySet()),strNewTitle,customPageIndex);
        }
    }

    @Override
    public void dismiss() {
        common.control_events.HideSoftKeyboard(findViewById(R.id.tvOK));
        super.dismiss();
    }
    @Override
    public void RemoveRow(PromotionAddRuleItemSelectionRow row) {
        llParent.removeView(row);
    }

    @Override
    public void AddRow() {
        PromotionAddRuleItemSelectionRow row = new PromotionAddRuleItemSelectionRow(getContext(),blnIncludeAnySelection);
        row.ShowDeleteOption(true);
        row.ShowAddOption(false);
        row.AddListener(SelectPromotionItemRowDialog.this);
        if(customListListener!=null)
        {
            row.HideUnitComponent();
        }
        llParent.addView(row,llParent.getChildCount()-1);
    }
}
