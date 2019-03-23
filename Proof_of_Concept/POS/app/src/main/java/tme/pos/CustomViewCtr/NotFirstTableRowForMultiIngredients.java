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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import java.util.ArrayList;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.ModifierObject;
import tme.pos.BusinessLayer.RegularOrderItemProperties;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.IItemMenuOptionActivityListener;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.MoveItemDialog;
import tme.pos.PageViewerFragment;
import tme.pos.R;

/**
 * Created by kchoy on 10/8/2014.
 */
public class NotFirstTableRowForMultiIngredients extends TableRow {
    protected GestureDetector myGestureDetector;

    NotFirstTableRowForMultiIngredients currentRow = this;
    final int intTblRowPadding;//,intTextViewHeight;
    private boolean blnRemoveDialogLaunched=false;
    //final MainUIActivity mainActivity;
    final int intSubMaxChar;
    StoreItem si;
    RegularOrderItemProperties roip;
    AppSettings myAppSettings;
    //ArrayList<PageViewerFragment> fragments;
    IItemMenuOptionActivityListener listener;
    int itemIndexInCart=-1;
    Enum.CartItemStatus itemStatus;
    public NotFirstTableRowForMultiIngredients(Context context, AttributeSet attrs,int intTblRowPadding,StoreItem si,
                                               RegularOrderItemProperties roip,int intSubMaxChar
                                               ,int itemIndexInCart,Enum.CartItemStatus cis){//},ArrayList<PageViewerFragment> fragments) {
        super(context, attrs);
        this.intTblRowPadding = intTblRowPadding;
        //this.intTextViewHeight =intTextViewHeight;
        //this.mainActivity=mainActivity;
        this.si = si;
        this.roip = roip;
        this.intSubMaxChar = intSubMaxChar;
        this.myAppSettings = common.myAppSettings;
        this.itemIndexInCart = itemIndexInCart;
        this.itemStatus = cis;
        Configure();

    }

    public NotFirstTableRowForMultiIngredients(Context context, int intTblRowPadding
            , StoreItem si, RegularOrderItemProperties roip, int intSubMaxChar
            ,IItemMenuOptionActivityListener listener, int itemIndexInCart,Enum.CartItemStatus cis){//}, ArrayList<PageViewerFragment> fragments) {
        super(context);
        this.intTblRowPadding = intTblRowPadding;
        //this.intTextViewHeight =intTextViewHeight;
        //this.mainActivity=mainActivity;
        this.si = si;
        this.roip = roip;
        this.intSubMaxChar = intSubMaxChar;
        this.myAppSettings = common.myAppSettings;
        this.listener = listener;
        this.itemIndexInCart = itemIndexInCart;
        this.itemStatus = cis;
        Configure();

    }

    private void Configure() {
        //assigning id
        this.setId(generateViewId());
        currentRow = this;
        myGestureDetector = new GestureDetector(getContext(), new MyDoubleTapGestureListener(this));
        //add on touch listener
        if(itemStatus!= Enum.CartItemStatus.free)
        {
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(itemStatus== Enum.CartItemStatus.lock) {
                        //common.Utility.ShowMessage("Options", "This receipt has been locked, you can only checkout.", getContext(), R.drawable.no_access);
                    }
                    else
                        common.Utility.ShowMessage("Options","This receipt has been paid, you cannot modify.",getContext(),R.drawable.no_access);
                    return false;
                }
            });



        }
        else {
            setOnTouchListener(new OnTouchListener() {
                float flInitialX = 0;

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    myGestureDetector.onTouchEvent(motionEvent);

                    int action = MotionEventCompat.getActionMasked(motionEvent);
                    float flOffsetX = 0;
                    switch (action) {
                        case (MotionEvent.ACTION_DOWN):


                            break;
                        case (MotionEvent.ACTION_MOVE):


                            break;
                        case (MotionEvent.ACTION_UP):


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

  /*  protected void ShowErrorMessageBox(String strMethodName,Exception ex)
    {
        Log.d("EXCEPTION: " + strMethodName,  ""+ex.getMessage());

        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strMethodName);
        messageBox.setMessage(ex.getMessage());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }*/

    private void ShowConfirmation()
    {
        final MainUIActivity mua = (MainUIActivity)getContext();

        if(mua.GetIsPopupShown()||mua.GetIsReceiptControlBusy())
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
                /*mua.GoToSelectCategory((StoreItem2)((TableLayout)getParent()).getTag(),
                        ((LinearLayout)((TableLayout)getParent()).getParent()).indexOfChild(((TableLayout)getParent())));*/

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
                //new ItemMenuOptionPopup(getContext(), si, true, menuItemPageFragment);//getContext(),(StoreItem2)((TableLayout)getParent()).getTag(),true);
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
                common.Utility.LogActivity("move item [" + si.item.getName() + "] from receipt "+mua.GetCurrentSubReceiptIndex());
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
    private void DeleteSelectedItemRow(boolean flgSwipeLeftToDelete) {
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

                //Log.d("Listener", "onAnimationEnd");
                //remove table row and text view that have moved out

                TextView tv = (TextView) currentRow.getChildAt(1);
                TableLayout prt = (TableLayout) currentRow.getParent();
                ModifierObject mo1 = (ModifierObject) tv.getTag();
                final MainUIActivity mua = (MainUIActivity) getContext();
                mua.RemoveOrderedModifier(si,mo1);//check for every split receipt and perform a removal operation


                /*//check if there is still other modifier listing under the same item
                MyCart mc = mua.GetCurrentCartCopied(true);
                for (StoreItem2 tempSi : mc.items) {
                    if (tempSi.IsSameOrderedItemExcludeUnitCount(si)) {
                        si = tempSi;
                        break;
                    }
                }*/
                for (int i = 0; i < si.modifiers.size(); i++) {

                    ModifierObject mo2 = si.modifiers.get(i);


                    if (mo1.getID() == mo2.getID()) {


                        si.modifiers.remove(i);
                        break;
                    }
                }
                prt.removeView(currentRow);

                //check if the new update item matching any existing ordered item and perform a merge on the receipt
                mua.ListOrders(false,null);

                //mua.Calculate();
               /* Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mua.SetReceiptControlBusyFlag(false);
                        //mua.SetPopupShow(false);
                    }
                }, 100);*/
                //Toast.makeText(getContext(), "Total updated.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(1000);
        movement.setFillAfter(true);



        currentRow.startAnimation(movement);
    }
    private void FlingLeft(){
        MainUIActivity mua = (MainUIActivity)getContext();
        mua.ibtnInvoiceNextPage_Click(null);
    }
    private void FlingRight(){
        final MainUIActivity mua = (MainUIActivity)getContext();
        mua.ibtnInvoicePreviousPage_Click(null);
    }
    private  void ShowMessageBox(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strTitle);
        messageBox.setMessage(strMsg);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
        messageBox.show();
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
                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                        R.anim.flip_vertical);
                anim.setTarget(currentObject);
                anim.setDuration(400);
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
