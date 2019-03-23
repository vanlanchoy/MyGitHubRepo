package tme.pos;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Store;

import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 2/24/2016.
 */
public class MoveItemDialog extends Dialog {
    float ADD_MENU_ITEM_TITLE_TEXT_SIZE = 30;
    float ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = 35;
    float ADD_MENU_ITEM_TEXT_SIZE = 20;
    MainUIActivity mua;
    ScrollView svReceipt;
    long lngScrollInterval = 100;
    int intReceiptWidth = 180;
    int intReceiptHeight = 180;
    List<Receipt> receipts;
    TextView tvShadow;
    boolean blnScrolling = false;
    TableLayout tblReceiptContainer;
    String strReceiptMessage = "";
    int intWaterMarkSize = 35;
    RelativeLayout dialogPanel;
    TableLayout tblItems;
    //boolean blnActionUpOrMove;
    int intCurrentReceiptIndex;
    TextView tvTitle;
    boolean blnSameReceipt = false;
    int intCurrentSelectedItemCount = 0;
    int intScrollViewWidth = 0;
    boolean blnDragging = false;
    boolean blnActionUp=false;
    Handler handler = new Handler();
    Runnable checkDraggingThread;
    String strModifierSpaces="    ";
    int intReceiptUIMaxRow=9;
    String strTableId;
    public MoveItemDialog(Context context, MainUIActivity mua) {
        super(context);
        this.mua = mua;
        receipts = common.myCartManager.GetReceipts(mua.GetCurrentTableId());
        intCurrentReceiptIndex = mua.GetCurrentSubReceiptIndex();
        strTableId = mua.GetCurrentTableId();
        LoadApplicationData();
    }

    protected void CreateReceiptPanel() {
        tblReceiptContainer = (TableLayout) findViewById(R.id.tblReceiptContainer);
        tblReceiptContainer.removeAllViews();
        TableRow trContainerRow = new TableRow(getContext());
        TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tllp.bottomMargin = 30;
        tblReceiptContainer.addView(trContainerRow, tllp);
        int count = 0;
        for (int i = 0; i < receipts.size(); i++) {
            if (count >= 3) {
                count = 0;//reset
                //new row
                trContainerRow = new TableRow(getContext());
                tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tllp.bottomMargin = 30;
                tblReceiptContainer.addView(trContainerRow, tllp);
            }

            TableRow.LayoutParams trlp = new TableRow.LayoutParams(intReceiptWidth, intReceiptHeight);
            trlp.setMargins(10, 0, 5, 0);

            //final TableLayout tbl = CreateReceiptUI(receipts.get(i).myCart.GetItems(), (i == intCurrentReceiptIndex) ? R.color.half_transparent_dark_grey : R.color.black);
            //final TableLayout tbl = CreateReceiptUI(receipts.get(i).myCart.GetItems(), R.color.black);
            final TableLayout tbl = CreateReceiptUI(CombineItems(receipts.get(i).myCart.GetItems()), R.color.black);
            tbl.setTag(i);//store receipt index
            //tbl.setBackgroundColor(Color.GREEN);
            //order does matter!!
            if(intCurrentReceiptIndex==i)tbl.setBackground(getContext().getResources().getDrawable(R.drawable.draw_black_line_border));
            tbl.setPadding(5,5,5,5);
            //wrap it in relative layout and apply label
            RelativeLayout rl = new RelativeLayout(getContext());
            RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(intReceiptWidth, intReceiptHeight);

            rl.addView(tbl, rllp);

            TextView tvLabel = new TextView(getContext());
            tvLabel.setText("#" + (i + 1));
            tvLabel.setTextColor(getContext().getResources().getColor(R.color.transparent_grey_50_percent));
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, intWaterMarkSize);
            tvLabel.setGravity(Gravity.CENTER);

            rl.addView(tvLabel, rllp);
            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intCurrentReceiptIndex = (Integer)tbl.getTag();
                    //view.setBackground(getContext().getResources().getDrawable(R.drawable.draw_black_line_border));
                    mua.SetCurrentSubReceiptIndex(intCurrentReceiptIndex);
                    CreateReceiptPanel();
                    ListAvailableItems(intCurrentReceiptIndex);
                }
            });
            trContainerRow.addView(rl, trlp);
            count++;
        }

        if (count >= 3) {

            //new row
            trContainerRow = new TableRow(getContext());
            tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tllp.bottomMargin = 30;
            tblReceiptContainer.addView(trContainerRow, tllp);
        }
        //add create new receipt option
        RelativeLayout rl = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(intReceiptWidth, intReceiptHeight);
        rl.addView(CreateSplitReceiptUI(), rllp);
        rl.setBackground(getContext().getResources().getDrawable(R.drawable.draw_dash_boarder));
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(intReceiptWidth, intReceiptHeight);
        trlp.setMargins(10, 0, 5, 0);
        trContainerRow.addView(rl, trlp);
    }

    private void HitTestReceipt(float x, float y) {
        blnSameReceipt = false;
        RelativeLayout rl = GetSelectedReceiptObject(x, y);
        if (rl == null) return;
        View v = rl.getChildAt(0);
        if (v instanceof TableLayout) {
            v.setBackground(getContext().getResources().getDrawable(R.drawable.draw_selected_receipt_border));
            //v.setBackground(getContext().getResources().getDrawable(R.drawable.draw_black_line_border));
            v.setPadding(5, 5, 5, 5);
            int receiptIndex = (Integer) v.getTag();
            blnSameReceipt = (receiptIndex == intCurrentReceiptIndex) ? true : false;

        } else if (v instanceof TextView) {
            //is add new receipt label
            ((TextView) v).setTextColor(getContext().getResources().getColor(R.color.green));
        }


    }

    private RelativeLayout GetSelectedReceiptObject(float x, float y) {
        int[] location = new int[2];
        RelativeLayout selectedRL = null;
        for (int i = 0; i < tblReceiptContainer.getChildCount(); i++) {
            TableRow tr = (TableRow) tblReceiptContainer.getChildAt(i);
            for (int j = 0; j < tr.getChildCount(); j++) {
                RelativeLayout rl = (RelativeLayout) tr.getChildAt(j);
                View v = rl.getChildAt(0);
                //v.getLocationInWindow(location);
                v.getLocationOnScreen(location);
                float startX = location[0];
                float startY = location[1];
                float endX = location[0] + v.getWidth();
                float endY = location[1] + v.getHeight();
                //strReceiptMessage="receipt startX: "+startX+", endX: "+endX+", startY: "+ startY+", endY: "+endY;
                if (x > startX && x < endX && y > startY && y < endY) {

                    selectedRL = rl;
                } else {
                    if (v instanceof TableLayout) {
                        v.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
                        v.setPadding(5, 5, 5, 5);
                    } else if (v instanceof TextView) {
                        //is add new receipt label
                        ((TextView) v).setTextColor(getContext().getResources().getColor(R.color.black));
                    }
                }

            }

        }

        return selectedRL;
    }

    private TextView CreateSplitReceiptUI() {
        TextView tv = new TextView(getContext());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, intWaterMarkSize);
        tv.setGravity(Gravity.CENTER);
        tv.setText("New");
        return tv;
    }

    private TableLayout CreateReceiptUI(ArrayList<StoreItem> si, int textColor) {
        TableRow tr;
        TextView tv;
        TableLayout tblReceipt = new TableLayout(getContext());
        //tblReceipt.setPadding(5, 5, 5, 5);
        tblReceipt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //strReceiptMessage = "receipt X:" + motionEvent.getX() + ", Y:" + motionEvent.getY();
                return false;
            }
        });
        tblReceipt.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
        for (int i = 0; i < si.size(); i++) {

            //max display 9 rows
            if(intReceiptUIMaxRow==tblReceipt.getChildCount())
            {

                ((TextView)((TableRow)tblReceipt.getChildAt(tblReceipt.getChildCount()-1)).getChildAt(0)).setText("......");

                ((TextView)((TableRow)tblReceipt.getChildAt(tblReceipt.getChildCount()-1)).getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);

                TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                trlp.setMargins(0,-10,0,0);
                ((TextView)((TableRow)tblReceipt.getChildAt(tblReceipt.getChildCount()-1)).getChildAt(0)).setLayoutParams(trlp);

                return tblReceipt;
            }

            tr = new TableRow(getContext());

            //tr.setBackgroundColor(Color.RED);
            tv = new TextView(getContext());
            String strText =si.get(i).UnitOrder + "X " + si.get(i).item.getName();
            tv.setText(strText.length()>25?strText.substring(0,23)+"...":strText);
            tv.setTextColor(getContext().getResources().getColor(textColor));
            tr.addView(tv);
            tblReceipt.addView(tr);


            for(int j=0;j<si.get(i).modifiers.size();j++)
            {
                //max display 9 rows
                if(intReceiptUIMaxRow==tblReceipt.getChildCount())
                {
                    ((TextView)((TableRow)tblReceipt.getChildAt(tblReceipt.getChildCount()-1)).getChildAt(0)).setText("......");

                    ((TextView)((TableRow)tblReceipt.getChildAt(tblReceipt.getChildCount()-1)).getChildAt(0)).setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);

                    TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    trlp.setMargins(0,-10,0,0);
                    ((TextView)((TableRow)tblReceipt.getChildAt(tblReceipt.getChildCount()-1)).getChildAt(0)).setLayoutParams(trlp);


                    return tblReceipt;
                }

                tr = new TableRow(getContext());

                tv = new TextView(getContext());
                strText =strModifierSpaces+ si.get(i).modifiers.get(j).getName();
                tv.setText(strText.length()>25?strText.substring(0,23)+"...":strText);
                tv.setTextColor(getContext().getResources().getColor(textColor));
                tr.addView(tv);
                tblReceipt.addView(tr);


            }
        }

        return tblReceipt;
    }

    protected void InsertMoveItems(RelativeLayout rl) {
        if (rl == null) return;
        View v = rl.getChildAt(0);
        if (v instanceof TextView) {
            ((TextView) v).setTextColor(Color.BLACK);
            CreateNewReceipt();
        } else if (v instanceof TableLayout) {
            v.setBackground(getContext().getResources().getDrawable(R.drawable.draw_border));
            //v.setPadding(5, 5, 5, 5);
            DropIntoReceipt((TableLayout) v);
        }
        //allow listing empty receipt
        ListAvailableItems(intCurrentReceiptIndex);
        RemoveEmptyReceipt();//remove empty receipt after listing
        CreateReceiptPanel();
    }

    private void DropIntoReceipt(TableLayout tbl) {
        //dropped into receipt index
        int droppedIndex = (Integer) tbl.getTag();

        if (droppedIndex == intCurrentReceiptIndex) return;//do nothing if is the same receipt

        ArrayList<StoreItem> selectedItems = GetSelectedItems();
        Receipt fromReceipt = receipts.get(intCurrentReceiptIndex);
        Receipt toReceipt = receipts.get(droppedIndex);
        ArrayList<StoreItem>fromItems = fromReceipt.myCart.GetItems();
        ArrayList<StoreItem>toItems = toReceipt.myCart.GetItems();
        for (int j = 0; j < selectedItems.size(); j++) {

            for (int i = fromItems.size() - 1; i > -1; i--) {
                if (fromItems.get(i).IsSameOrderedItemExcludeUnitCount(selectedItems.get(j))) {
                    StoreItem toSI = null;
                    //check whether the current item type is already on the receipt
                    for (int k = 0; k < toItems.size(); k++) {
                        if (toItems.get(k).IsSameOrderedItemExcludeUnitCount(selectedItems.get(j))) {

                            toSI = toItems.get(k);
                            break;
                        }
                    }

                    //deduct the quantity
                    fromItems.get(i).UnitOrder-=1;
                    //selectedItems.get(j).UnitOrder -= 1;
                    if (toSI != null) {
                        //merge
                        toSI.UnitOrder += 1;


                    } else {
                        //insert new
                        StoreItem tempSi = (StoreItem) selectedItems.get(j).clone();
                        tempSi.UnitOrder = 1;
                        toReceipt.myCart.GetItems().add(tempSi);
                    }

                    //remove it from receipt if unit count is zero
                    if (fromReceipt.myCart.GetItems().get(i).UnitOrder == 0) {
                        fromReceipt.myCart.GetItems().remove(i);
                        intCurrentReceiptIndex=0;//reset to 1st receipt if all the items have been moved
                    }
                }
            }
        }
    }


    private void CreateNewReceipt() {

        int count = GetSelectItemCount();
        if (receipts.get(intCurrentReceiptIndex).myCart.GetTotalUnitCount() == count
                && receipts.size() == 1) {
            //do nothing if there is only one receipt and user wants to move ALL the items to new receipt
            return;
        }

        //create new receipt
        Receipt r = common.Utility.CreateNewReceiptObject(strTableId);
        common.Utility.FillInReceiptProperties(r, mua.GetCurrentTableLabel());
        common.myCartManager.AddNewReceipt(r, strTableId);

        ArrayList<StoreItem> selectedItems = GetSelectedItems();
        Receipt fromReceipt = receipts.get(intCurrentReceiptIndex);
        for (int j = 0; j < selectedItems.size(); j++) {
            for (int i = 0; i < fromReceipt.myCart.GetItems().size(); i++) {
                if (selectedItems.get(j).IsSameOrderedItemExcludeUnitCount(fromReceipt.myCart.GetItems().get(i))) {
                    StoreItem cloneSI = (StoreItem) fromReceipt.myCart.GetItems().get(i).clone();
                    cloneSI.UnitOrder = 1;
                    r.myCart.GetItems().add(cloneSI);

                    fromReceipt.myCart.GetItems().get(i).UnitOrder -= 1;
                    if (fromReceipt.myCart.GetItems().get(i).UnitOrder == 0) {
                        fromReceipt.myCart.GetItems().remove(i);
                    }

                    break;
                }
            }
        }
        //ListAvailableItems(intCurrentReceiptIndex);
        //CreateReceiptPanel();
    }

    private void RemoveEmptyReceipt() {
        for (int i = receipts.size() - 1; i >= 0; i--) {
            if (receipts.get(i).myCart.GetItems().size() == 0) receipts.remove(i);
        }

    }
    private ArrayList<StoreItem> CombineItems(ArrayList<StoreItem> lst)
    {
        ArrayList<StoreItem> combines = new ArrayList<StoreItem>();
        boolean blnFound = false;
        for(int i=0;i<lst.size();i++)
        {
            blnFound = false;
            if(combines.size()==0)
            {
                combines.add((StoreItem) lst.get(i).clone());
            }
            else
            {
                for(int j=0;j<combines.size();j++)
                {
                    blnFound = combines.get(j).IsSameOrderedItemExcludeUnitCount(lst.get(i));
                    if(blnFound){
                        //combine with existing
                        combines.get(j).UnitOrder+=lst.get(i).UnitOrder;
                        break;
                    }
                }
                if(!blnFound)
                {
                    combines.add((StoreItem)lst.get(i).clone());
                }
            }
        }
        return combines;
    }
    private void ListAvailableItems(int receiptIndex) {
        tblItems.removeAllViews();
        Receipt receipt = receipts.get(receiptIndex);
        ArrayList<StoreItem>combine = CombineItems(receipt.myCart.GetItems());
        //for (int i = 0; i < receipt.myCart.GetItems().size(); i++) {
        for (int i = 0; i < combine.size(); i++) {

            TableLayout tblDetail = new TableLayout(getContext());
            TableRow trDetail = new TableRow(getContext());
            TextView tvItem = new TextView(getContext());
            //tvItem.setText(receipt.myCart.GetItems().get(i).UnitOrder + "X " + receipt.myCart.GetItems().get(i).item.getName());
            tvItem.setText(combine.get(i).UnitOrder + "X " +combine.get(i).item.getName());
            //tvItem.setBackgroundColor(Color.RED);
            tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
            tvItem.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.BOLD);
            TableRow.LayoutParams trlpItem = new TableRow.LayoutParams(intScrollViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            trDetail.addView(tvItem, trlpItem);

            //trDetail.setBackgroundColor(Color.CYAN);
            TableLayout.LayoutParams tllpDetail = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tblDetail.addView(trDetail, tllpDetail);

            //for (int j = 0; j < receipt.myCart.GetItems().get(i).modifiers.size(); j++) {
            for (int j = 0; j < combine.get(i).modifiers.size(); j++) {
                trDetail = new TableRow(getContext());
                tvItem = new TextView(getContext());
                tvItem.setText(strModifierSpaces + combine.get(i).modifiers.get(j).getName());
                //tvItem.setText(strModifierSpaces + receipt.myCart.GetItems().get(i).modifiers.get(j).getName());

                tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                tvItem.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.BOLD);
                trlpItem = new TableRow.LayoutParams(intScrollViewWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                trDetail.addView(tvItem, trlpItem);
                tblDetail.addView(trDetail);
            }

            TableRow tr = new TableRow(getContext());
            TableRow.LayoutParams trlp = new TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tr.addView(tblDetail, trlp);
            tr.setPadding(5, 0,0, 10);
            //tr.setTag(receipt.myCart.GetItems().get(i));
            tr.setTag(combine.get(i));
            TableLayout.LayoutParams tllp = new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


            tblItems.addView(tr, tllp);


        }
    }

    private int GetSelectItemCount() {
        int count = 0;
        for (int i = 0; i < tblItems.getChildCount(); i++) {
            Drawable drawable = tblItems.getChildAt(i).getBackground();
            if (drawable != null) count++;
        }

        return count;
    }

    private ArrayList<StoreItem> GetSelectedItems() {
        ArrayList<StoreItem> SIs = new ArrayList<StoreItem>();
        for (int i = 0; i < tblItems.getChildCount(); i++) {
            Drawable drawable = tblItems.getChildAt(i).getBackground();
            if (drawable != null) SIs.add((StoreItem) tblItems.getChildAt(i).getTag());
        }
        return SIs;
    }

    protected void CreateDragShadow(float window_x, float window_y) {
        if (tvShadow == null) {
            tvShadow = new TextView(getContext());
            tvShadow.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE + 20);
            tvShadow.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.BOLD);
            tvShadow.setTextColor(Color.GRAY);
            tvShadow.setVisibility(View.GONE);
            dialogPanel.addView(tvShadow);
        }
        if (tvShadow.getVisibility() == View.GONE) {
            intCurrentSelectedItemCount = GetSelectItemCount();
            if (intCurrentSelectedItemCount > 0) {
                tvShadow.setVisibility(View.VISIBLE);

            }
        }

        if (blnSameReceipt) {
            tvShadow.setText("");
            tvShadow.setBackground(getContext().getResources().getDrawable(R.drawable.no_access));
        } else {
            tvShadow.setText("+" + intCurrentSelectedItemCount);
            tvShadow.setBackground(null);
        }


        tvShadow.setX((window_x + 600) - (blnSameReceipt ? 100 : 50));

        tvShadow.setY(window_y);// - (blnSameReceipt ? 100 : 50));
    }

    private TableRow GetClickedReceiptItem(float x, float y) {
        int[] location = new int[2];
        TableRow selectedTR = null;
        for (int i = 0; i < tblItems.getChildCount(); i++) {
            TableRow tr = (TableRow) tblItems.getChildAt(i);
            //tr.getLocationOnScreen(location);
            tr.getLocationInWindow(location);
            if (location[0] <= x && (location[0] + tr.getWidth()) >= x
                    && location[1] <= y && (location[1] + tr.getHeight()) >= y) {
                selectedTR = tr;
                break;
            }
        }
        return selectedTR;
    }

    private void HitTestReceiptItem(float x, float y) {
        TableRow tr = null;
        tr = GetClickedReceiptItem(x, y);
        if (tr == null) return;
        if (tr.getBackground() != null) {
            //deselect
            tr.setBackground(null);
        } else {
            //select
            tr.setBackgroundColor(getContext().getResources().getColor(R.color.selected_row_green));
        }

    }

    private void SetupDraggingMode(boolean blnFlag) {
        int color = R.color.black;
        if (blnFlag) {
            color = R.color.top_category_item_lost_focus_grey;
        }

        for (int i = 0; i < tblItems.getChildCount(); i++) {
            TableRow tr = (TableRow) tblItems.getChildAt(i);

            if (tr.getBackground() != null) {
                TableLayout tblDetail = (TableLayout) tr.getChildAt(0);
                for(int j=0;j<tblDetail.getChildCount();j++) {
                    TableRow trDetail = (TableRow) tblDetail.getChildAt(j);
                    ((TextView) trDetail.getChildAt(0)).setTextColor(getContext().getResources().getColor(color));
                }
            }
        }
    }

    @Override
    public void dismiss() {
        common.Utility.LogActivity("move item dialog dismiss");

        Enum.DBOperationResult result = common.receiptManager.SaveOrdersIntoDB(common.myCartManager.GetReceipts(strTableId),true);
        if(result== Enum.DBOperationResult.Failed || result== Enum.DBOperationResult.TryLater)
        {
            common.Utility.ShowMessage("Receipt","Failed to update receipt, please try again later",getContext(),R.drawable.no_access);
            mua.ReloadLastStateAndListOrders();
        }
        else if(result== Enum.DBOperationResult.VersionOutOfDate) {

            common.Utility.ShowMessage("Receipt","your receipt is out of date, refreshing now.",getContext(),R.drawable.no_access);
            mua.ReloadLastStateAndListOrders();
        }
        else if(result== Enum.DBOperationResult.Success) {
            mua.DisplayCurrentCartItem();
        }


        super.dismiss();


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkDraggingThread = new Runnable() {
            @Override
            public void run() {

                if(!blnActionUp){// && GetSelectItemCount()>0) {
                    //SetupDraggingMode(true);
                    blnDragging = true;
                }
            }
        };

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_move_item_popup_window_ui);
        //final TextView tvTitle = (TextView)findViewById(R.id.lblTitle);
        tvTitle = (TextView)findViewById(R.id.lblTitle);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE);
        tvTitle.setTypeface(Typeface.createFromAsset(getContext().getAssets(), getContext().getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);

        tblItems = (TableLayout)findViewById(R.id.tblItems);
        final ScrollView svItem = (ScrollView)findViewById(R.id.svItems);
        intScrollViewWidth = svItem.getLayoutParams().width;
        svReceipt = (ScrollView)findViewById(R.id.svReceipt);
        dialogPanel =(RelativeLayout)findViewById(R.id.dialogPanel);

        svItem.setOnTouchListener(new View.OnTouchListener() {
            float x = 0;
            float y = 0;
            float last_x = 0;
            float last_y = 0;

            @Override
            public boolean onTouch(View view, final MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                last_x = x;
                last_y = y;
                x = event.getRawX();
                y = event.getRawY();
                if (action == MotionEvent.ACTION_DOWN) {
                    //user click on receipt item table
                    //if (event.getX() >= 615 && event.getY() >= 110) {
                    blnActionUp = false;
                    //set timer for select, time is 0.5 second
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (blnActionUp) {
                                //tvTitle.setText("Item clicked");
                                HitTestReceiptItem(event.getX(), event.getY());//absolute for actiondown
                            }
                        }
                    }, 300);
                    //set timer for dragging, time 1 second
                    handler.postDelayed(checkDraggingThread, 500);

                    //}
                } else if (action == MotionEvent.ACTION_UP) {
                    handler.removeCallbacks(checkDraggingThread);
                    if (blnDragging) {
                        InsertMoveItems(GetSelectedReceiptObject(x, y));
                        SetupDraggingMode(false);
                    }
                    if (tvShadow != null) {
                        tvShadow.setVisibility(View.GONE);
                    }
                    blnActionUp = true;
                    blnDragging = false;
                    blnScrolling = false;
                } else if (action == MotionEvent.ACTION_MOVE) {

                    //tvTitle.setText("dragging x: " + x + ", y: " + y);
                    //dragging
                    if (blnDragging) {
                        if (x < 800 && GetSelectItemCount() > 0) {
                            //moving items to another receipt
                            SetupDraggingMode(true);
                            HitTestReceipt(x, y);
                            CreateDragShadow(event.getX(), event.getY());
                            if (tvShadow != null && tvShadow.getVisibility() == View.GONE) {

                                tvShadow.setVisibility(View.VISIBLE);
                            }
                        } else {
                            //scrolling in receipt items
                            SetupDraggingMode(false);
                            if (x > 800) {
                                svItem.scrollBy(0, Math.round(last_y - y));
                            }
                            if (tvShadow != null && tvShadow.getVisibility() == View.VISIBLE) {
                                tvShadow.setVisibility(View.GONE);
                            }
                        }

                    }

                    //scrolling part
                    if (x < 800 && y > 675 && y < 700 && blnDragging) {

                        if (blnScrolling) return true;
                        blnScrolling = true;
                        ScrollDown();

                    } else if (x < 800 && y < 200 && y > 160 && blnDragging) {
                        if (blnScrolling) return true;
                        blnScrolling = true;
                        ScrollUp();

                    } else {
                        blnScrolling = false;
                    }

                }
                return true;
            }
        });


        CreateReceiptPanel();
        ListAvailableItems(intCurrentReceiptIndex);



    }
    private void ScrollUp()
    {

        svReceipt.scrollBy(0, -10);
        if(blnScrolling) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScrollUp();
                }
            },lngScrollInterval);
        }


    }
    private void ScrollDown()
    {

        svReceipt.scrollBy(0, 10);
        if(blnScrolling) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ScrollDown();
                }
            }, lngScrollInterval);
        }


    }
    private void LoadApplicationData()
    {
        ADD_MENU_ITEM_TITLE_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_add_menu_item_title);
        ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_add_menu_item_modifier_text);
        ADD_MENU_ITEM_TEXT_SIZE = getContext().getResources().getDimension(R.dimen.dp_add_menu_item_text);
    }
}
