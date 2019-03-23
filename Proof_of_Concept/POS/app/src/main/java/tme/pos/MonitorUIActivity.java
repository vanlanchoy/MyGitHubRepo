package tme.pos;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import tme.pos.BusinessLayer.ChartManager;
import tme.pos.BusinessLayer.FloorPlanManager;
import tme.pos.BusinessLayer.LockDetails;
import tme.pos.BusinessLayer.Receipt;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 11/16/2016.
 */

public class MonitorUIActivity extends FragmentActivity {
    @Override
    protected void onStart() {


        super.onStart();


    }
    @Override
    protected  void onResume()
    {

        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);


    }

    @Override
    public void onBackPressed() {


        super.onBackPressed();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((POS_Application) getApplication()).setCurrentActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_monitors_ui);
        Refresh();
    }
    private void Refresh() {

        ListCurrentOrdersFromDB();
        ListCurrentCartOrder();
        ListCurrentReceiptArchive();
        ListFloorPlanData();
    }
    private void ListFloorPlanData() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.lyFloor);
        ll.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText("Floor Plan");
        ll.addView(tv);

        LockDetails ld = new FloorPlanManager(this).GetCurrentLockDetailInfo();
        tv = new TextView(this);
        tv.setText("device id ["+ld.DeviceId+"], Version# ["+ld.Version+"], Time ["+ld.LockedDateTime+"]");
        ll.addView(tv);
    }
    private void ListCurrentReceiptArchive() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.lyReceipts);
        ll.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText("Receipts");
        ll.addView(tv);

        ArrayList<Pair<Receipt,String>>rs = new ChartManager(this).GetAllReceipts();
        if(rs.size()==0) {
            tv = new TextView(this);
            tv.setText("No Data");
            ll.addView(tv);
        }
        else {
            for(Pair<Receipt,String> r:rs) {
                tv = new TextView(this);
                tv.setText("Table Label ["+r.first.tableNumber+"], Receipt# ["+r.first.receiptNumber+"], Has Paid ["+r.first.blnHasPaid+"], version ["+r.first.Version+"], Lock by ["+
                r.second+"], Linked ["+r.first.strLinkedReceipts+"], IsActive ["+r.first.blnActive+"]");
                ll.addView(tv);
            }
        }
    }
    private void ListCurrentCartOrder()
    {
        LinearLayout ll = (LinearLayout) findViewById(R.id.lyOrders);
        ll.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText("Cart order");
        ll.addView(tv);

        for(Receipt r:common.myCartManager.GetAllReceipts())
        {
            tv = new TextView(this);
            tv.setText("Table Label ["+r.tableNumber+"], Index ["+r.myCart.receiptIndex+"], Receipt# ["+r.receiptNumber+"], Has Paid ["+r.blnHasPaid+"], version ["+r.Version+"]");
            ll.addView(tv);
        }

        if(ll.getChildCount()==1)
        {
            tv = new TextView(this);
            tv.setText("No Data");
            ll.addView(tv);
        }
    }
    private void ListCurrentOrdersFromDB() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.lyTblOrders);
        ll.removeAllViews();

        TextView tv = new TextView(this);
        tv.setText("Order Saved in DB");
        ll.addView(tv);

        ArrayList<Receipt> rs=common.receiptManager.GetOrdersFromDb("",true);
        if(rs.size()==0) {
            tv = new TextView(this);
            tv.setText("No Data");
            ll.addView(tv);
        }
        else {
            for(Receipt r:rs) {
                tv = new TextView(this);
                tv.setText("Table Label ["+r.tableNumber+"], Index ["+r.myCart.receiptIndex+"], Receipt# ["+r.receiptNumber+"], Has Paid ["+r.blnHasPaid+"], version ["+r.Version+"]");
                ll.addView(tv);
            }
        }
    }
}
