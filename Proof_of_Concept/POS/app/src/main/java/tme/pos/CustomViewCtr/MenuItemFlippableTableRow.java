package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;


import tme.pos.BusinessLayer.*;


import tme.pos.Interfaces.IItemViewUpdateUnit;
import tme.pos.Interfaces.IMenuItemClickedListener;
import tme.pos.Interfaces.IToBeUpdatedInventoryView;
import tme.pos.ItemInventoryOptionDialog;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.POS_Application;
import tme.pos.PageViewerFragment;
import tme.pos.R;


/**
 * Created by kchoy on 2/4/2015.
 */
public class MenuItemFlippableTableRow extends GenericVerticalFlipableTableRow implements IItemViewUpdateUnit
,IToBeUpdatedInventoryView {




    IMenuItemClickedListener listener;


    AppSettings myAppSettings;
    PageViewerFragment menuItemPageFragment;
    long lngSelectedCategoryId=-1;
    long itemId;
    TextView tvPrice;
    TextView tvName;
    ImageView imgView;
    LinearLayout llItemDetails;
    TextView tvInventory;


    public MenuItemFlippableTableRow(Context context
            , PageViewerFragment fragment
            ,long categoryId,long itemId,IMenuItemClickedListener l)//,ArrayList<ItemObject> CategoryItems,
                                     //long categoryId)//},android.support.v4.app.FragmentManager childFragmentMgr)
    {
        super(context);
        lngSelectedCategoryId = categoryId;
        listener =l;
        this.myAppSettings = common.myAppSettings;
        menuItemPageFragment = fragment;
        this.itemId=itemId;
        CreateControl();

    }
    public void SetProperties(ItemObject io)
    {
        setTag(io.getID());
        tvPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(io.getPrice()));
        tvPrice.setTextColor((io.getPrice().doubleValue()<0) ? getResources().getColor(R.color.pink_red) : getResources().getColor(R.color.black));

        tvName.setText(io.getName());
        common.Utility.LoadPicture(imgView,io.getPicturePath(),getContext());

        if(!io.getDoNotTrackFlag())
        {
            ImageView imgInventory = new ImageView(getContext());
            imgInventory.setBackground(getResources().getDrawable(R.drawable.inventory_black));
            LinearLayout.LayoutParams trlp2 = new LinearLayout.LayoutParams(40, 40);
            imgInventory.setLayoutParams(trlp2);

            tvInventory = new TextView(getContext());

            int unitAvailable = common.Utility.GetAtTheMomentItemCount(io.getID());
            unitAvailable = (unitAvailable < 0) ? 0 : unitAvailable;
            UpdateInventory(unitAvailable);
            tvInventory.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            tvInventory.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT,Typeface.BOLD);
            tvInventory.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lpInventory = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lpInventory.gravity = Gravity.CENTER;
            lpInventory.setMargins(10, 0, 0, 0);
            tvInventory.setLayoutParams(lpInventory);

            menuItemPageFragment.lstTvUnit.add(new Pair<Long, TextView>(io.getID(),tvInventory));


            imgInventory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   if(listener!=null)listener.InventoryPopupClicked(itemId,MenuItemFlippableTableRow.this);

                }
            });


            llItemDetails.addView(imgInventory);
            llItemDetails.addView(tvInventory);

        }
        //barcode
        if(io.getBarcode()==0)
        {
            //do nothing
        }
        else {
            TextView tvBarcode = new TextView(getContext());
            tvBarcode.setText("Barcode: " + (io.getBarcode() == 0 ? "N/A" : io.getBarcode()));
            tvBarcode.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lpBarcode = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lpBarcode.gravity = Gravity.CENTER;
            lpBarcode.setMargins(10, 0, 0, 0);
            tvBarcode.setLayoutParams(lpBarcode);
            llItemDetails.addView(tvBarcode);
        }
    }

    private void CreateControl()
    {

        tvPrice = new TextView(getContext());



        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.SP_MENU_ITEM_TEXT_SIZE);
        tvPrice.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvPrice.setBackgroundColor(getResources().getColor(R.color.transparent));


        tvPrice.setGravity(Gravity.RIGHT);
        tvPrice.setBackgroundColor(getResources().getColor(R.color.transparent));
        LinearLayout.LayoutParams lpPrice = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(165f,getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);
        lpPrice.setMargins(0,0,5,0);
        tvPrice.setLayoutParams(lpPrice);

        tvName = new TextView(getContext());


        tvName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.SP_MENU_ITEM_TEXT_SIZE);
        tvName.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT, Typeface.NORMAL);
        tvName.setBackgroundColor(getResources().getColor(R.color.transparent));

        tvName.setLineSpacing(-9.0f,1f);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, common.Utility.DP2Pixel(67,getContext())));
        LinearLayout.LayoutParams lltv= (LinearLayout.LayoutParams)tvName.getLayoutParams();
        lltv.setMargins(5,0,0,0);
        lltv.weight=0.1f;

        imgView = new ImageView(getContext());


        LinearLayout.LayoutParams trlp = new LinearLayout.LayoutParams(100,100);
        imgView.setLayoutParams(trlp);





        LinearLayout ll1 = new LinearLayout(getContext());
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));

        LinearLayout ll2 = new LinearLayout(getContext());
        ll2.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams ll2lp = new LinearLayout.LayoutParams(common.Utility.DP2Pixel(700,getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);
        ll2.setLayoutParams(ll2lp);

        LinearLayout ll3 = new LinearLayout(getContext());
        ll3.setOrientation(LinearLayout.HORIZONTAL);


        llItemDetails = new LinearLayout(getContext());
        llItemDetails.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpll4=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lpll4.gravity=Gravity.BOTTOM;
        llItemDetails.setLayoutParams(lpll4);


        ll3.addView(tvName);
        ll3.addView(tvPrice);
        ll3.setBackgroundColor(getResources().getColor(R.color.very_light_grey));

        ll2.addView(ll3);

        ll2.addView(llItemDetails);
        ll1.addView(imgView);
        ll1.addView(ll2);


        setBackgroundColor(getResources().getColor(R.color.white_green));


        addView(ll1);


    }

    private void UpdateInventory(int count)
    {
        if(tvInventory!=null)
        {
            if(count<=10) {
                tvInventory.setTextColor(Color.RED);
            }
            else
            {
                tvInventory.setTextColor(Color.BLACK);
            }
            tvInventory.setText(count + "");
        }
    }
    @Override
    protected void SingleTapped() {


        if(listener!=null){
            int currentInventoryCount=-1;
            if(tvInventory!=null)
            {
                currentInventoryCount = Integer.parseInt(tvInventory.getText()+"");
            }
            listener.ListItemSingleTapped(itemId,this,currentInventoryCount);
            //int count =listener.ListItemSingleTapped(itemId,currentInventoryCount);
            //UpdateInventory(count);
        }


    }



    @Override
    protected void ShowConfirmation() {
        int currentInventoryCount=-1;
        if(tvInventory!=null)
        {
            currentInventoryCount = Integer.parseInt(tvInventory.getText()+"");
        }
        if(listener!=null){listener.ListItemDoubleTapped(itemId,this,currentInventoryCount);}
        android.os.Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                MenuItemFlippableTableRow.this.setBackgroundColor(getResources().getColor(R.color.white_green));
            }
        },500);






    }
    @Override
    public void ItemMenuDialogUnitAdded(int affectedUnit) {
        if(tvInventory!=null)
        {
            int count = Integer.parseInt(tvInventory.getText()+"")-affectedUnit;
            UpdateInventory(count);
        }

    }

    @Override
    public void InventoryDialogUpdateUnitCount(int unitCount) {
        UpdateInventory(unitCount);
    }



}
