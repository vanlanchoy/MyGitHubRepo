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


import java.util.ArrayList;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.PromotionAwarded;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.IItemMenuOptionActivityListener;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.MoveItemDialog;
import tme.pos.PageViewerFragment;
import tme.pos.R;

/**
 * Created by kchoy on 10/23/2014.
 */
public class RegularOrderedItemRow extends TableLayout {
    protected TableLayout currentView;
    protected GestureDetector myGestureDetector;
    AppSettings myAppSettings;
    //ArrayList<PageViewerFragment> fragments;
    IItemMenuOptionActivityListener listener;
    int itemIndexInCart=-1;
    Enum.CartItemStatus itemStatus;
    //private boolean blnRemoveDialogLaunched=false;

    public RegularOrderedItemRow(Context context,IItemMenuOptionActivityListener listener, int itemIndexInCart,Enum.CartItemStatus cis){//},ArrayList<PageViewerFragment> fragments) {
        super(context);
        this.myAppSettings = common.myAppSettings;
        //this.fragments = fragments;
        this.listener = listener;
        this.itemIndexInCart = itemIndexInCart;
        this.itemStatus = cis;
        Configure();

    }

    public RegularOrderedItemRow(Context context, AttributeSet attrs, int itemIndexInCart,Enum.CartItemStatus cis){//},ArrayList<PageViewerFragment> fragments) {
        super(context, attrs);
        this.myAppSettings = common.myAppSettings;
        this.itemIndexInCart = itemIndexInCart;
        this.itemStatus = cis;
        Configure();

    }
    public void SlideIn(boolean blnSlideRight)
    {
        currentView = this;
        TranslateAnimation movement = new TranslateAnimation(-5000.0f, 0.0f, 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide in to right
            movement = new TranslateAnimation(5000.0f, 0.0f, 0.0f, 0.0f);//move left
        }





        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                currentView.setVisibility(VISIBLE);
                currentView.setBackgroundResource(R.drawable.draw_table_row_border);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                    currentView.setBackground(null);


            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(200);
        movement.setFillAfter(true);



        currentView.startAnimation(movement);

    }
    protected void Configure()
    {
        currentView = this;
        myGestureDetector = new GestureDetector(getContext(), new MyDoubleTapGestureListener(this));
        //add on touch listener
        if(itemStatus!= Enum.CartItemStatus.free)
        {
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                   /* if(itemStatus== Enum.CartItemStatus.lock) {
                        common.Utility.ShowMessage("Options", "This receipt has been locked, you can only checkout.", getContext(), R.drawable.no_access);
                    }
                    else
                        common.Utility.ShowMessage("Options","This receipt has been paid, you cannot modify.",getContext(),R.drawable.no_access);*/
                    return false;
                }
            });



        }
        else {
            setOnTouchListener(new OnTouchListener() {
                //float flInitialX=0;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    myGestureDetector.onTouchEvent(motionEvent);

                    int action = MotionEventCompat.getActionMasked(motionEvent);
                    //float flOffsetX=0;
                    switch (action) {
                        case (MotionEvent.ACTION_DOWN):
                        /*
                        flInitialX = motionEvent.getX();

                        //setBackgroundResource(R.drawable.draw_table_row_border);
                        //flip_vertical
                        ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                                R.anim.flip_vertical);
                        anim.setTarget(tr);
                        anim.setDuration(400);
                        anim.start();
*/
                            break;
                        case (MotionEvent.ACTION_MOVE):


                            break;
                        case (MotionEvent.ACTION_UP):

                            //setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                            //ShowDeleteConfirmation();
                            break;
                        case (MotionEvent.ACTION_CANCEL):
                            setBackground(null);
                            break;
                        default:
                            break;
                    }


                    return true;
                }
            });
        }

    }


   /* protected void ShowErrorMessageBox(String strMethodName,Exception ex)
    {
        Log.d("EXCEPTION: " + strMethodName, "" + ex.getMessage());

        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strMethodName);
        messageBox.setMessage(ex.getMessage());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }*/
    /*protected  boolean SwipeLeftToDelete()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean blnRightHanded = sp.getBoolean(MainUIActivity.PREFERRENCE_LEFT_HANDED_SETTING_KEY,true);
        return !blnRightHanded;

    }*/


    private void DeleteSelectedItemRow(boolean flgSwipeLeftToDelete)
    {
        TranslateAnimation movement = new TranslateAnimation(0.0f, 5000.0f, 0.0f, 0.0f);//move right
        if(flgSwipeLeftToDelete) {
            movement = new TranslateAnimation(0.0f, -5000.0f, 0.0f, 0.0f);//move left
        }
        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {


                //remove table row and text view that have moved out
                //move other items up to fill the gap
                final MainUIActivity mua = (MainUIActivity)getContext();
                mua.RemoveOrderedItem((StoreItem)currentView.getTag());
               /* MyCart mc = mua.GetCurrentCartCopied(true);
                    for(StoreItem2 si: mc.items)
                    {
                        if(si.IsSameOrderedItemExcludeUnitCount((StoreItem2)currentView.getTag()))
                        {
                            mc.items.remove(si);
                            break;
                        }
                    }*/
                    //mua.GetCurrentCartCopied(true).items.remove(currentView.getTag());
                //mua.Calculate();
                LinearLayout ll = (LinearLayout)currentView.getParent();
                ll.removeView(currentView);
                mua.ListOrders(false,null);//call relist
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //mua.SetReceiptControlBusyFlag(false);
                        mua.UpdateInventoryUnitCount(((StoreItem)currentView.getTag()).item.getID());
                    }
                },100);

                //Toast.makeText(getContext(),"Total updated.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        currentView.startAnimation(movement);

    }
   /* private void ShowRemoveComboItemDialog()
    {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("This item is part of a promotion set, do you want to remove other items that come with this set as well?")
                .setTitle("Options");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.show();
    }*/

    private void ShowConfirmation()
    {

        final MainUIActivity mua = (MainUIActivity)getContext();
        if(mua.GetCurrentSubReceiptIndex()==-1)
        {
            setBackground(null);
            common.Utility.ShowMessage("Item", "Please go to the receipt you want to modify this item, currently this is combined view.", getContext(), R.drawable.no_access);

            return;
        }
        if( mua.GetIsPopupShown()||mua.GetIsReceiptControlBusy())
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
                    common.Utility.LogActivity("remove item [" + ((StoreItem) getTag()).item.getName() + "] from receipt " + mua.GetCurrentSubReceiptIndex());
                    mua.SetReceiptControlBusyFlag(true);
                    //mua.UpdateInventoryUnitCount(((StoreItem2)getTag()).item.getID());
                    DeleteSelectedItemRow(myAppSettings.SwipeLeftToDelete());
                //}
                dialog.dismiss();
                //blnRemoveDialogLaunched=false;
            }
        });
        builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                //create this class will show popup
                common.Utility.LogActivity("edit item ["+((StoreItem) getTag()).item.getName()+"] from receipt "+mua.GetCurrentSubReceiptIndex());
                StoreItem si = (StoreItem) getTag();
                if(si.item.getDoNotTrackFlag()) {
                    new ItemMenuOptionPopup(getContext(), si, true,0, listener,null,itemIndexInCart);// fragments);
                }
                else
                {
                    new ItemMenuOptionPopup(getContext(),si,true
                            ,common.inventoryList.GetInventoryCount(si.item.getID())-common.Utility.CalculateOrderedItem(si.item.getID())+si.UnitOrder
                    ,listener,null,itemIndexInCart);
                    /*new ItemMenuOptionPopup(getContext(), si, true, fragments
                            ,common.inventoryList.GetInventoryCount(si.item.getID())-common.Utility.CalculateOrderedItem(si.item.getID())+si.UnitOrder);*/
                }


                dialog.dismiss();
                android.os.Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mua.SetPopupShow(true);
                    }
                }, 200);
                //blnRemoveDialogLaunched=false;
            }
        });
        builder.setNegativeButton("Move", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(mua.GetIsReceiptControlBusy()) {
                    common.Utility.ShowMessage(AppSettings.MESSAGE_APPLICATION_BUSY_TITLE,AppSettings.MESSAGE_APPLICATION_BUSY,getContext(),R.drawable.exclaimation);
                    return;
                }
                // User cancelled the dialog
                common.Utility.LogActivity("move item [" + ((StoreItem) getTag()).item.getName() + "] from receipt "+mua.GetCurrentSubReceiptIndex());
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
                setBackground(null);
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

                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                        R.anim.flip_vertical);
                anim.setTarget(currentObject);
                anim.setDuration(200);
                anim.start();
                setBackgroundColor(getResources().getColor(R.color.selected_row_green));
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
