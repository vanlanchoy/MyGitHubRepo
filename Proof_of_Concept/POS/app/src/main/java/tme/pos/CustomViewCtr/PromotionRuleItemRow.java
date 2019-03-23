package tme.pos.CustomViewCtr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import tme.pos.BusinessLayer.PromotionObject;
import tme.pos.Interfaces.IPromotionRuleItemRowListener;
import tme.pos.R;

/**
 * Created by vanlanchoy on 6/2/2016.
 */
public class PromotionRuleItemRow extends LinearLayout {
    long itemId;
    int unit;
    int rowIndex;
    TextView tvTitle;
    TextView tvRemove;
    IPromotionRuleItemRowListener listener;
    public PromotionRuleItemRow(Context c)
    {
        super(c);
        Instantiate();
    }
    public PromotionRuleItemRow(Context c, AttributeSet attributeSet)
    {
        super(c,attributeSet);
        Instantiate();
    }
    private void Instantiate()
    {
        setBackgroundColor(getResources().getColor(R.color.very_light_grey2));
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_selected_rule_item_view, this);
        tvTitle = (TextView)findViewById(R.id.tvTitle);
        tvRemove = (TextView)findViewById(R.id.tvRemove);
        tvRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null)listener.RemoveRuleItem(PromotionRuleItemRow.this,rowIndex/2);//need list index not row index in the data structure, have to deduct option row)
            }
        });
    }
    public long GetItemId(){return itemId;}
    public int GetUnit(){return unit;}
    public void SetProperties(String strItemName,int unit,long itemId
            ,IPromotionRuleItemRowListener l,int rowIndex)
    {
        this.listener = l;
        this.itemId = itemId;
        this.unit = unit;
        this.rowIndex = rowIndex;
        if(strItemName.length()>43)
            strItemName = strItemName.substring(0,40)+"...";
        tvTitle.setText(unit+"X "+strItemName);
    }
    public void UpdateRoleIndex(int index)
    {
        rowIndex = index;
    }
    public int GetRowIndex(){return rowIndex;}

}
