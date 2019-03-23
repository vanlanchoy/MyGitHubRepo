package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;


import java.util.ArrayList;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.IItemMenuOptionActivityListener;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.MoveItemDialog;
import tme.pos.PageViewerFragment;
import tme.pos.R;

/**
 * Created by vanlanchoy on 10/7/2014.
 */
public class FirstTableRowForMultiIngredients extends TableRow {
    protected GestureDetector myGestureDetector;
    AppSettings myAppSettings;
    FirstTableRowForMultiIngredients currentView;
    //private boolean blnRemoveDialogLaunched=false;
    IItemMenuOptionActivityListener listener;
    //ArrayList<PageViewerFragment> fragments;
    int itemIndexInCart = -1;
    //boolean blnLock;
    Enum.CartItemStatus itemStatus;
    //protected TableLayout prt;
    public FirstTableRowForMultiIngredients(Context context, AttributeSet attrs,int itemIndexInCart,Enum.CartItemStatus cis){//,ArrayList<PageViewerFragment> fragments) {
        super(context, attrs);
        this.itemIndexInCart = itemIndexInCart;
        this.itemStatus = cis;

        Configure();

    }

    public FirstTableRowForMultiIngredients(Context context,IItemMenuOptionActivityListener listener, int itemIndexInCart,Enum.CartItemStatus cis){//},ArrayList<PageViewerFragment> fragments) {
        super(context);
        //this.fragments = fragments;
        this.listener = listener;
        this.itemIndexInCart = itemIndexInCart;
        this.myAppSettings = common.myAppSettings;
        this.itemStatus = cis;
        Configure();

    }

    public void SlideIn(boolean blnSlideRight)
    {
        //currentView = this;
        final TableLayout parentTbl = (TableLayout)currentView.getParent();
        TranslateAnimation movement = new TranslateAnimation(-5000.0f, 0.0f, 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide in to right
            movement = new TranslateAnimation(5000.0f, 0.0f, 0.0f, 0.0f);//move left
        }





        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                parentTbl.setVisibility(VISIBLE);
                parentTbl.setBackgroundResource(R.drawable.draw_table_row_border);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                parentTbl.setBackground(null);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(200);
        movement.setFillAfter(true);



        parentTbl.startAnimation(movement);

    }

    protected void Configure()
    {
        //assigning id
        this.setId(generateViewId());
        currentView = this;
        myGestureDetector = new GestureDetector(getContext(), new MyDoubleTapGestureListener(this));
        //add on touch listener
        if(itemStatus!= Enum.CartItemStatus.free)
        {
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(itemStatus== Enum.CartItemStatus.lock) {//common.Utility.ShowMessage("Options","This receipt has been locked, you can only checkout.",getContext(),R.drawable.no_access);
                    }
                    else
                        {common.Utility.ShowMessage("Options","This receipt has been paid, you cannot modify.",getContext(),R.drawable.no_access);}
                    return false;
                }
            });



        }
        else {
            setOnTouchListener(new OnTouchListener() {


                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    myGestureDetector.onTouchEvent(motionEvent);

                    int action = MotionEventCompat.getActionMasked(motionEvent);
                    //float flOffsetX=0;
                    switch (action) {
                        case (MotionEvent.ACTION_DOWN):

                            break;
                        case (MotionEvent.ACTION_MOVE):


                            break;
                        case (MotionEvent.ACTION_UP):


                            break;
                        case (MotionEvent.ACTION_CANCEL):
                            ((TableLayout) currentView.getParent()).setBackground(null);
                            break;
                        default:
                            break;
                    }


                    return true;
                }
            });

        }
    }

    private void DeleteSelectedItemRow(boolean flgSwipeLeftToDelete)
    {
        final TableLayout prt = (TableLayout)currentView.getParent();
        TranslateAnimation movement = new TranslateAnimation(0.0f, 5000.0f, 0.0f, 0.0f);//move right
        if(flgSwipeLeftToDelete) {
            movement = new TranslateAnimation(0.0f, -5000.0f, 0.0f, 0.0f);//move left
        }
        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                Log.d("Listener", "onAnimationEnd");
                //remove table row and text view that have moved out
                //move other items up to fill the gap

                final MainUIActivity mua = (MainUIActivity) getContext();
                mua.RemoveOrderedItem((StoreItem) prt.getTag());

                //mua.Calculate();
                LinearLayout ll = (LinearLayout) prt.getParent();
                ll.removeView(prt);
                mua.ListOrders(false,null);//call re-list
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //mua.SetReceiptControlBusyFlag(false);
                        mua.UpdateInventoryUnitCount(((StoreItem) prt.getTag()).item.getID());
                    }
                }, 100);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        prt.startAnimation(movement);

    }

/*protected void ShowIsBusyMessage()
{
    AlertDialog.Builder builder = new AlertDialog.Builder((getContext()));
    builder.setTitle("Busy");
    builder.setMessage("System is currently busy, please try again later.");
    builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.dismiss();
        }
    });
    builder.create().show();
}*/
    private void ShowConfirmation()
    {
        final MainUIActivity mua = (MainUIActivity)getContext();

        if(mua.GetIsReceiptControlBusy()||mua.GetIsPopupShown())
        {
            setBackground(null);
            //ShowIsBusyMessage();
            return;
        }

        mua.SetPopupShow(true);
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("What would you like to do?")
                .setTitle("Options");

        // 3. Get the AlertDialog from create()
        //AlertDialog dialog = builder.create();
        // Add the buttons
        builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if(mua.GetIsReceiptControlBusy()) {
                    common.Utility.ShowMessage(AppSettings.MESSAGE_APPLICATION_BUSY_TITLE,AppSettings.MESSAGE_APPLICATION_BUSY,getContext(),R.drawable.exclaimation);
                    return;
                }
                // User clicked OK button
                mua.SetReceiptControlBusyFlag(true);
                DeleteSelectedItemRow(myAppSettings.SwipeLeftToDelete());

                dialog.dismiss();
                //blnRemoveDialogLaunched=false;
            }
        });
        builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                StoreItem si = (StoreItem) ((TableLayout) getParent()).getTag();
                if(si.item.getDoNotTrackFlag()) {
                    //new ItemMenuOptionPopup(getContext(), si, true, fragments);
                    new ItemMenuOptionPopup(getContext(), si, true,0, listener,null,itemIndexInCart);
                }
                else
                {
                    new ItemMenuOptionPopup(getContext(),si,true
                            ,common.inventoryList.GetInventoryCount(si.item.getID())-common.Utility.CalculateOrderedItem(si.item.getID())+si.UnitOrder
                            ,listener,null,itemIndexInCart);
                   /* new ItemMenuOptionPopup(getContext(), si, true, fragments
                            ,common.inventoryList.GetInventoryCount(si.item.getID())-common.Utility.CalculateOrderedItem(si.item.getID()));*/
                }


                dialog.dismiss();
                android.os.Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mua.SetPopupShow(true);
                    }
                }, 200);

            }
        });
        builder.setNegativeButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(mua.GetIsReceiptControlBusy()) {
                    common.Utility.ShowMessage(AppSettings.MESSAGE_APPLICATION_BUSY_TITLE,AppSettings.MESSAGE_APPLICATION_BUSY,getContext(),R.drawable.exclaimation);
                    return;
                }
                common.Utility.LogActivity("move item [" + ((StoreItem)((TableLayout) getParent()).getTag()).item.getName() + "] from receipt "+mua.GetCurrentSubReceiptIndex());
                //change index
                if(mua.GetCurrentSubReceiptIndex()==-1)mua.SetCurrentSubReceiptIndex(0);
                common.Utility.LogActivity("update receipt index to "+mua.GetCurrentSubReceiptIndex());

                MoveItemDialog d = new MoveItemDialog(getContext(),mua);
                d.show();
                dialog.dismiss();
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ((TableLayout) currentView.getParent()).setBackground(null);
                mua.SetPopupShow(false);//set it here only so that it will taking care of user touching outside of the dialog
            }
        });
        builder.show();
    }
    private void SingleTapped()
    {

    }
    private void FlingLeft(){
        MainUIActivity mua = (MainUIActivity)getContext();
        mua.ibtnInvoiceNextPage_Click(null);
    }
    private void FlingRight(){
        final MainUIActivity mua = (MainUIActivity)getContext();
        mua.ibtnInvoicePreviousPage_Click(null);
    }
    private class MyDoubleTapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
        View currentObject;
        public MyDoubleTapGestureListener(View v)
        {
            currentObject = v;
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent event)
        {
            if(event.getAction()==1)
            {
                final MainUIActivity mua = (MainUIActivity)getContext();
                if(mua.GetCurrentSubReceiptIndex()==-1)
                {
                    setBackground(null);
                    common.Utility.ShowMessage("Item", "Please go to the receipt you want to modify this item, currently this is combined view.", getContext(), R.drawable.no_access);

                    return true;
                }
                TableLayout tl = (TableLayout) currentView.getParent();

                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                         R.anim.flip_vertical);
                anim.setTarget(tl);
                anim.setDuration(200);

                anim.start();
                tl.setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                ShowConfirmation();
                //ShowDeleteConfirmation();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            SingleTapped();
            return true;
        }
        @Override
        public boolean onDoubleTap(MotionEvent event)
        {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            if(motionEvent.getX()<motionEvent2.getX())
            {
                FlingRight();
            }
            else
            {
                FlingLeft();
            }
            return true;
        }
    }
}
