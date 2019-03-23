package tme.pos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import tme.pos.BusinessLayer.common;

/**
 * Created by vanlan on 2/18/2016.
 */
public class BarcodeSearchDialog extends Dialog {
    float ADD_MENU_ITEM_TITLE_TEXT_SIZE = 30;
    float ADD_MENU_ITEM_MODIFIER_TEXT_SIZE=35;
    float ADD_MENU_ITEM_TEXT_SIZE =20;
    MainUIActivity mua;

    public BarcodeSearchDialog(Context context,MainUIActivity mua)
    {
        super(context);
        this.mua = mua;
        LoadApplicationData();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_search_by_barcode_popup_window_ui);

        TextView tvTitle = (TextView)findViewById(R.id.lblTitle);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE);
        tvTitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);

        TextView tvBarcode = (TextView)findViewById(R.id.lblBarcode);
        tvBarcode.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT_SIZE);
        tvBarcode.setTypeface(Typeface.createFromAsset(getContext().getAssets(), tvBarcode.getResources().getString(R.string.app_font_family)));

        final EditText txtBarcode = (EditText)findViewById(R.id.txtBarcode);
        txtBarcode.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_TEXT_SIZE);
        txtBarcode.setFilters(common.Utility.CreateMaxLengthFilter(common.text_and_length_settings.MAX_BARCODE_LENGTH));

        TextView tvSearchBarcode=(TextView)findViewById(R.id.tvSearchBarcode);
        tvSearchBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strBarcode =txtBarcode.getText()+"";
                strBarcode = strBarcode.trim();
                if(strBarcode.length()==0)
                {
                    common.Utility.ShowMessage("Search","Please provide a barcode to search",getContext(),R.drawable.no_access);
                    return;
                }
                mua.SearchItemByBarcode(strBarcode, BarcodeSearchDialog.this);
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
            }
        });
    }
    private void LoadApplicationData()
    {
        ADD_MENU_ITEM_TITLE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_add_menu_item_title);
        ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_add_menu_item_modifier_text);
        ADD_MENU_ITEM_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_add_menu_item_text);

    }
}