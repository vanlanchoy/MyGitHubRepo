package tme.pos.CustomViewCtr;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.ModifierOptionDialog;
import tme.pos.R;
import tme.pos.SpinnerBaseAdapter;
import tme.pos.UnitTextWatcher;

/**
 * Created by vanlanchoy on 5/22/2016.
 */
public class MenuItemOptionOrderRow extends LinearLayout implements UnitTextWatcher.IUnitChangedListener,
        ModifierOptionDialog.IModifierChangedListener{
    ImageView imgDelete;
    ImageView imgAdd;
    TextView tvItemName;
    EditText txtUnit;
    Spinner spItemName;
    int intTotalOrderedUnit=0;
    FlowLayout modifierContainer;
    IitemChangeListener listener;
    LinearLayout llTapToAddContainer;
    StoreItem orderedItem;
    HashMap<Long,ArrayList<ModifierObject>>allModifiers;
    SpinnerBaseAdapter<ItemObject>adapterItemName;
    int paddingSize;
    int marginRight;
    int previousCount;

    public interface IitemChangeListener
    {
        void Delete(MenuItemOptionOrderRow row);
        int UnitUpdate(int newUnit,int oldUnit,long itemId,MenuItemOptionOrderRow row);
        void AddNewRow(MenuItemOptionOrderRow clickedRow);
        void ItemChanged(long newID,long oldID,int unit,MenuItemOptionOrderRow row);
        void AddModifier(long id);
        void DeleteModifier(long id);
    }

    @Override
    public void UnitChanged(int newUnit,int oldUnit) {
        previousCount = oldUnit;
        if((intTotalOrderedUnit-previousCount+newUnit)>common.text_and_length_settings.UNIT_LIMIT)
        {
            common.Utility.ShowMessage("Order","You have reached unit limit "+common.text_and_length_settings.UNIT_LIMIT,getContext(),R.drawable.no_access);
            //txtUnit.setText(previousCount+"");
            txtUnit.setText(oldUnit+"");
            return;
        }
        UpdateReceiptItemUnit(newUnit);
    }

    @Override
    public void UpdateModifiers(ArrayList<ModifierObject> modifiers) {
        //overwrite selected modifiers
        orderedItem.modifiers = modifiers;
        ListSelectedModifiers();
    }


    public MenuItemOptionOrderRow(Context context)
    {
        super(context);
        Instantiate();
    }
    public MenuItemOptionOrderRow(Context context, AttributeSet attributeSet)
    {
        super(context,attributeSet);
        TypedArray arry = context.obtainStyledAttributes(attributeSet,R.styleable.MenuItemOptionOrderRow);
        String strShowDeleteFlag = arry.getString(R.styleable.MenuItemOptionOrderRow_ShowDeleteOption);
        String strShowAddNewRowFlag = arry.getString(R.styleable.MenuItemOptionOrderRow_ShowAddNewRowOption);
        Instantiate();

        if(strShowDeleteFlag.equalsIgnoreCase("false"))imgDelete.setVisibility(GONE);
        if(strShowAddNewRowFlag.equalsIgnoreCase("false"))imgAdd.setVisibility(GONE);

        arry.recycle();
    }
    public SpinnerBaseAdapter<ItemObject>GetAdapter(){return adapterItemName;}
    public HashMap<Long,ArrayList<ModifierObject>> GetModifierList(){return allModifiers;}
    private void ListSelectedModifiers()
    {
        modifierContainer.removeAllViews();
        for(ModifierObject mo:orderedItem.modifiers)
        {
            modifierContainer.addView(CreateModifierUI(mo));
        }
        modifierContainer.addView(llTapToAddContainer);
    }
    private LinearLayout CreateModifierUI(final ModifierObject mo)
    {
        final LinearLayout ll = new LinearLayout(getContext());
        ViewGroup.MarginLayoutParams vglp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vglp.setMargins(0,0,marginRight,0);
        ll.setPadding(paddingSize,0,paddingSize,0);
        ll.setBackgroundColor(getContext().getResources().getColor(R.color.very_light_grey2));
        ll.setOrientation(HORIZONTAL);
        TextView tvName = new TextView(getContext());
        tvName.setText(mo.getName());
        tvName.setTextColor(getContext().getResources().getColor(common.Utility.GetModifierGroupColor(mo.getMutualGroup())));
        tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lllp.setMargins(0,0,marginRight,0);
        ll.addView(tvName,lllp);
        ll.setLayoutParams(vglp);
        TextView tvRemove = new TextView(getContext());
        tvRemove.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        tvRemove.setText("X");
        tvRemove.setTextColor(getContext().getResources().getColor(R.color.green));
        LinearLayout.LayoutParams lllp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lllp2.setMargins(0,0,marginRight,0);
        ll.addView(tvRemove,lllp2);
        tvRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                orderedItem.modifiers.remove(mo);
                modifierContainer.removeView(ll);
            }
        });
        return ll;
    }
    public int GetOrderedUnit(){return previousCount;}
    public void SetProperties(IitemChangeListener listener, StoreItem si
            , HashMap<Long,ArrayList<ModifierObject>> allModifiers, int originalUnit, SpinnerBaseAdapter<ItemObject>adapter)
    {
        this.listener = listener;
        orderedItem = si;
        this.allModifiers = allModifiers;
        previousCount = originalUnit;
        ListSelectedModifiers();
        txtUnit.setText(si.UnitOrder+"");
        //txtUnit.setSelection(0);
        txtUnit.setSelection(txtUnit.getText().length());
        txtUnit.addTextChangedListener(new UnitTextWatcher(txtUnit,orderedItem.item.getPrice().floatValue(),common.text_and_length_settings.TOTAL_PRICE_LIMIT,getContext(),this));

        tvItemName.setVisibility(GONE);
        spItemName.setAdapter(adapter);
        spItemName.setSelection(0);
        spItemName.setVisibility(VISIBLE);
        adapterItemName = adapter;
    }
    public void SetProperties(IitemChangeListener listener,StoreItem si
            ,HashMap<Long,ArrayList<ModifierObject>>allModifiers,int originalUnit)
    {
        this.listener = listener;
        orderedItem = si;
        this.allModifiers = allModifiers;
        previousCount = originalUnit;
        ListSelectedModifiers();
        txtUnit.setText(si.UnitOrder+"");
        txtUnit.addTextChangedListener(new UnitTextWatcher(txtUnit,orderedItem.item.getPrice().floatValue(),common.text_and_length_settings.TOTAL_PRICE_LIMIT,getContext(),this));
        //txtUnit.setSelection(0);
        txtUnit.setSelection(txtUnit.getText().length());
        tvItemName.setText(si.item.getName());
    }
    private void ShowModifierPopup()
    {
        HashMap<Long,ArrayList<ModifierObject>>mos = new HashMap<Long,ArrayList<ModifierObject>>();
        ModifierOptionDialog dialog;

        /*for(int i=0;i<allModifiers.size();i++)
        {
            ArrayList<ModifierObject>availableMos = allModifiers.get(i);
            for(ModifierObject mo:availableMos)
            {
                if(mo.getParentID()==common.text_and_length_settings.GLOBAL_MODIFIER_PARENT_ID)
                {
                    mos.add(mo);
                }
                else if(mo.getParentID()==orderedItem.item.getID())
                {
                    mos.add(mo);
                }
            }
        }*/
        if(allModifiers.containsKey(-1l))
        {
            mos.put(-1l,allModifiers.get(-1l));
        }
        else
        {
            mos.put(-1l,new ArrayList<ModifierObject>());
        }
        if(allModifiers.containsKey(orderedItem.item.getID()))
        {
            mos.put(orderedItem.item.getID(),allModifiers.get(orderedItem.item.getID()));
        }
        else
        {
            mos.put(orderedItem.item.getID(),new ArrayList<ModifierObject>());
        }
        if(spItemName.getVisibility()!=View.VISIBLE)
        {
            //use default
            dialog = new ModifierOptionDialog(getContext(),orderedItem.modifiers,mos,this);
            //dialog = new ModifierOptionDialog(getContext(),orderedItem.modifiers,allModifiers.get(0),this);
        }
        else
        {
            dialog = new ModifierOptionDialog(getContext(),orderedItem.modifiers,
                    mos,this);
           /* dialog = new ModifierOptionDialog(getContext(),orderedItem.modifiers,
                    allModifiers.get(spItemName.getSelectedItemPosition()),this);*/
        }

        dialog.show();
    }
    private void Instantiate()
    {
        paddingSize = common.Utility.DP2Pixel(3,getContext());
        marginRight = common.Utility.DP2Pixel(5,getContext());
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_menu_item_option_order_row, this);
        imgDelete=(ImageView) findViewById(R.id.imgDelete);
        imgAdd=(ImageView) findViewById(R.id.imgAdd);
        spItemName=(Spinner)findViewById(R.id.spItemName);
        spItemName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long oldId = orderedItem.item.getID();
                long newId = ((ItemObject)spItemName.getAdapter().getItem(position)).getID();
                orderedItem.item = common.myMenu.GetLatestItem(newId);
                if(listener!=null)
                {
                    if(oldId!=newId)
                    {
                        orderedItem.modifiers.clear();
                        ListSelectedModifiers();
                    }
                    listener.ItemChanged(newId,oldId,orderedItem.UnitOrder,MenuItemOptionOrderRow.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        txtUnit = (EditText)findViewById(R.id.txtUnit);

        modifierContainer = (FlowLayout) findViewById(R.id.modifierContainer);
        modifierContainer.setPadding(3,0,0,0);
        llTapToAddContainer = (LinearLayout)findViewById(R.id.llTapToAddContainer);
        tvItemName = (TextView)findViewById(R.id.tvItemName);
        //show modifier popup
        findViewById(R.id.tvAddModifier).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowModifierPopup();
            }
        });
         txtUnit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    EditText txtUnit = (EditText) view;
                    if (txtUnit.getText().length() == 0) txtUnit.getText().append("1");
                    if (txtUnit.getText().toString().equals("0")) txtUnit.getText().append("1");
                    UpdateReceiptItemUnit(Integer.parseInt(txtUnit.getText()+""));
                }
            }
        });
        txtUnit.setSelection(txtUnit.getText().length());
        //delete current row
        imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                {
                    listener.Delete(MenuItemOptionOrderRow.this);
                }
            }
        });

        //add new row with same type of item
        imgAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!=null)
                {
                    listener.AddNewRow(MenuItemOptionOrderRow.this);
                }
            }
        });

    }
    private void UpdateReceiptItemUnit(int unit)
    {
        if(listener!=null) {
            long itemId=-1;
            if(spItemName.getAdapter()==null)
            {
                itemId=orderedItem.item.getID();
            }
            else
            {
                itemId =((ItemObject)spItemName.getSelectedItem()).getID();
            }

            intTotalOrderedUnit = listener.UnitUpdate(unit,previousCount,itemId,MenuItemOptionOrderRow.this);
        }
        orderedItem.UnitOrder=unit;
        previousCount = unit;
    }
    public StoreItem GetStoreItem(){return orderedItem;}
    public void UpdateTotalOrderedUnitCount(int newTotal)
    {
        intTotalOrderedUnit = newTotal;
    }
    public void HideDeleteOption()
    {
        imgDelete.setVisibility(GONE);
    }
    public void HideAddNewRowOption()
    {
        imgAdd.setVisibility(GONE);
    }
}
