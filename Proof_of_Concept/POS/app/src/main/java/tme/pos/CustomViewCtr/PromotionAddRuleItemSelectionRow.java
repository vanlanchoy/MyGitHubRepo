package tme.pos.CustomViewCtr;

import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import tme.pos.BusinessLayer.CategoryObject;
import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.IAddPromotionRuleItemRowActivityListener;
import tme.pos.R;
import tme.pos.SpinnerBaseAdapter;
import tme.pos.UnitTextWatcher;

/**
 * Created by vanlanchoy on 5/31/2016.
 */
public class PromotionAddRuleItemSelectionRow extends LinearLayout {
    //int unit=0;
    //long itemId;
    Spinner spCategory;
    Spinner spItem;
    EditText txtUnit;
    float TEXT_SIZE=25;

    ItemObject itemObject;
    ImageView imgDelete;
    ImageView imgAdd;
    boolean blnLoading = false;
    long editItemId;
    long editParentId;
    long lngParentId;
    boolean blnIncludeAnyOption;
    //long editUnitCount;
    IAddPromotionRuleItemRowActivityListener listener;
    public PromotionAddRuleItemSelectionRow(Context c,boolean blnIncludeAnyOption)
    {
        super(c);
        this.blnIncludeAnyOption = blnIncludeAnyOption;
        Instantiate();
    }
    public void SetToIncludeAnySelection(boolean blnFlag) {
        blnIncludeAnyOption = blnFlag;
        if(!blnIncludeAnyOption) {

            SpinnerBaseAdapter<ItemObject> baItems =  new SpinnerBaseAdapter<ItemObject>(CreateItemObjectList(),getContext());
            spItem.setAdapter(baItems);

        }
    }
    public PromotionAddRuleItemSelectionRow(Context c, AttributeSet attributeSet)
    {
        super(c,attributeSet);
        Instantiate();
        TypedArray arry = c.obtainStyledAttributes(attributeSet,R.styleable.MenuItemOptionOrderRow);
        String strHideDeleteFlag = arry.getString(R.styleable.PromotionAddRuleItemSelectionRow_ShowDeleteOption);
        String strHideAddFlag = arry.getString(R.styleable.PromotionAddRuleItemSelectionRow_ShowAddNewRowOption);
        if(strHideDeleteFlag.equalsIgnoreCase("false"))imgDelete.setVisibility(GONE);
        if(strHideAddFlag.equalsIgnoreCase("false"))imgAdd.setVisibility(GONE);
        RelativeLayout.LayoutParams rllp = (RelativeLayout.LayoutParams)imgAdd.getLayoutParams();
        if(imgDelete.getVisibility()==GONE)
        {

            rllp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            //rllp.setMargins(100,0,20,0);

            //((RelativeLayout.LayoutParams)imgAdd.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            //((RelativeLayout.LayoutParams)imgAdd.getLayoutParams()).leftMargin=50;
        }
        else
        {
            rllp.addRule(RelativeLayout.ALIGN_RIGHT,imgDelete.getId());
        }
        imgAdd.setLayoutParams(rllp);
    }
    public void AddListener(IAddPromotionRuleItemRowActivityListener l)
    {
        listener = l;
    }
    public void HideUnitComponent()
    {
        txtUnit.setVisibility(GONE);
        findViewById(R.id.tvUnit).setVisibility(GONE);
        findViewById(R.id.tvX).setVisibility(GONE);
    }
    public void ShowAddOption(boolean blnShow)
    {
        if(!blnShow)
            imgAdd.setVisibility(GONE);
        else
            imgAdd.setVisibility(VISIBLE);
    }
    public void ShowDeleteOption(boolean blnShow)
    {
        if(!blnShow)
            imgDelete.setVisibility(GONE);
        else
            imgDelete.setVisibility(VISIBLE);
    }
    private void SetTextLabelProperties(TextView tv)
    {
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP,TEXT_SIZE);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
    }
    private void LoadCategory()
    {
        CategoryObject[] categoryObjects = common.myMenu.GetCategoryList().toArray(new CategoryObject[0]);
        categoryObjects = (categoryObjects==null)?new CategoryObject[0]:categoryObjects;
        SpinnerBaseAdapter baCategory = new SpinnerBaseAdapter<CategoryObject>( categoryObjects,getContext());
        spCategory.setAdapter(baCategory);

        if(editParentId>-1)
        {
            for(int i=0;i<categoryObjects.length;i++)
            {
                if(categoryObjects[i].getID()==editParentId)
                {
                    spCategory.setSelection(i);
                    break;
                }
            }
            editParentId=-1;//reset
        }
    }
    private ItemObject[] CreateItemObjectList() {

        ItemObject[] itemObjects = common.myMenu.GetCategoryItems(lngParentId,true).toArray(new ItemObject[0]);
        if(itemObjects==null)
        {
            itemObjects=new ItemObject[0];
        }
        else
        {
            //insert 'Any' option into the current list
            int offset = (blnIncludeAnyOption?1:0);
            ItemObject anyIO = new ItemObject(lngParentId,"Any",-1,"0.00","",true,lngParentId,1);//with current category id
            ItemObject[] temp = new ItemObject[itemObjects.length+offset];
            if(blnIncludeAnyOption) {
                temp[0] = anyIO;
            }
            for(int i=0;i<itemObjects.length;i++)temp[i+offset]=itemObjects[i];
            itemObjects = temp;
        }

        return itemObjects;
    }
    private void LoadItem(long ParentId)
    {
        blnLoading = true;
        ItemObject[] itemObjects = common.myMenu.GetCategoryItems(ParentId,true).toArray(new ItemObject[0]);
        if(itemObjects==null)
        {
            itemObjects=new ItemObject[0];
        }
        else
        {
            //insert 'Any' option into the current list
            int offset = (blnIncludeAnyOption?1:0);
            ItemObject anyIO = new ItemObject(ParentId,"Any",-1,"0.00","",true,ParentId,1);//with current category id
            ItemObject[] temp = new ItemObject[itemObjects.length+offset];
            if(blnIncludeAnyOption) {
                temp[0] = anyIO;
            }
            for(int i=0;i<itemObjects.length;i++)temp[i+offset]=itemObjects[i];
            itemObjects = temp;
        }
        //itemObjects = (itemObjects==null)?new ItemObject[0]:itemObjects;
        SpinnerBaseAdapter baItem = new SpinnerBaseAdapter<ItemObject>(itemObjects,getContext());
        spItem.setAdapter(baItem);

        if(editItemId>-1)
        {
            for(int i=0;i<itemObjects.length;i++)
            {
                if(itemObjects[i].getID()==editItemId)
                {
                    spItem.setSelection(i);
                    break;
                }
            }
            editItemId=-1;//reset
        }
        blnLoading = false;
        /*if(editItemId>-1)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SetSelectedItem();
                }
            }, 10);
        }
        else {
            blnLoading = false;
        }*/
    }
    /*private void SetSelectedItem()
    {
        for(int i=0;i<spItem.getAdapter().getCount();i++)
        {
            if(((ItemObject)spItem.getAdapter().getItem(i)).getID()==editItemId)
            {
                //spItem.setSelection(i);
                break;
            }
        }
        editItemId=-1;//reset
        blnLoading = false;
    }*/
    private void Instantiate()
    {
        TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_checkout_panel_fragment_text_size);
        setOrientation(VERTICAL);
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.layout_promotion_add_rule_item_option_ui, this);

        SetTextLabelProperties((TextView) findViewById(R.id.tvX));
        SetTextLabelProperties((TextView) findViewById(R.id.tvUnit));
        spCategory = (Spinner)findViewById(R.id.spCategory);
        LoadCategory();
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CategoryObject c = (CategoryObject) view.findViewById(R.id.textView1).getTag();
                itemObject = null;
                LoadItem(c.getID());

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        txtUnit = (EditText)findViewById(R.id.txtUnit);
        txtUnit.setText("1");
        txtUnit.setSelection(1);
        txtUnit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE);
        txtUnit.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        txtUnit.addTextChangedListener(new UnitTextWatcher(txtUnit,getContext(),null));

       /* //edit more
        if(editUnitCount>-1)
        {
            txtUnit.setText(editUnitCount+"");
        }*/

        spItem = (Spinner)findViewById(R.id.spItem);
        spItem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                itemObject =   (ItemObject) view.findViewById(R.id.textView1).getTag();
                if(itemObject.getID()==-1)itemObject=null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        imgDelete=(ImageView) findViewById(R.id.imgDelete);
        imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                {
                    listener.RemoveRow(PromotionAddRuleItemSelectionRow.this);
                }
            }
        });
        imgAdd=(ImageView) findViewById(R.id.imgAdd);
        imgAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                {
                    listener.AddRow();
                }
            }
        });
    }
    public void SetProperties(final StoreItem si)
    {
        txtUnit.setText(si.UnitOrder+"");
        txtUnit.setSelection(txtUnit.getText().length());
        itemObject = si.item;
        editParentId=itemObject.getParentID();
        editItemId=itemObject.getID();
        if(spCategory.getAdapter().getCount()>0) {
            for (int i = 0; i < spCategory.getAdapter().getCount(); i++) {
                //CategoryObject c = (CategoryObject) view.findViewById(R.id.textView1).getTag();
                CategoryObject c = (CategoryObject) spCategory.getAdapter().getItem(i);
                if (c.getID() == si.item.getParentID()) {
                    spCategory.setSelection(i);
                    //LoadItem(c.getID());
                    break;
                }
            }
            //SetSelectedItem(si);
        }
        else
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(editParentId>-1)SetProperties(si);
                }
            }, 100);
        }


    }
   /* private void SetSelectedItem(final StoreItem si)
    {
        if(blnLoading)
        {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SetSelectedItem(si);
                }
            }, 100);
            return;
        }

        for(int i=0;i<spItem.getAdapter().getCount();i++)
        {

            ItemObject c=(ItemObject)spItem.getAdapter().getItem(i);
            if(c.getID()==si.item.getID()) {
                spItem.setSelection(i);
                break;
            }
        }

    }*/
    public StoreItem GetSelectedItem()
    {
        if(itemObject==null)return null;
        StoreItem si = new StoreItem(itemObject);
        si.UnitOrder=Integer.parseInt(txtUnit.getText()+"");
        return si;
    }
}
