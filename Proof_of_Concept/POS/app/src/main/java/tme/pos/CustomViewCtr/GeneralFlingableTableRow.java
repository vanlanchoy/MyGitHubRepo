package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;

import tme.pos.AddNewMenuItemFragment;
import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by vanlanchoy on 1/27/2015.
 */
public class GeneralFlingableTableRow extends TableRow {
    private boolean blnRemoveDialogLaunched=false;
    protected GestureDetector myGestureDetector;
    AddNewMenuItemFragment parentFragment;
    AppSettings myAppSettings;
    public GeneralFlingableTableRow(Context context,AddNewMenuItemFragment fragment)
    {
        super(context);
        parentFragment = fragment;
        this.myAppSettings = common.myAppSettings;
        Configure();
    }
    public GeneralFlingableTableRow(Context context, AttributeSet attrs,AddNewMenuItemFragment fragment)
    {
        super(context,attrs);
        parentFragment = fragment;
        this.myAppSettings = common.myAppSettings;
        Configure();
    }

    protected void Configure()
    {
        //assigning id
        this.setId(generateViewId());
        final int Id = this.getId();
        final TableRow tr = this;
        //define gesture detector
        ConfigureGestureDetector();

        //add on touch listener
        setOnTouchListener(new OnTouchListener() {
            float flInitialX=0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //FirstTableRowForMultiIngredients tr = (FirstTableRowForMultiIngredients)view;
                //TableLayout tbl = (TableLayout)tr.getParent();
                // tbl.setLayoutParams(new TableLayout.LayoutParams(currentView.getWidth(),TableLayout.LayoutParams.WRAP_CONTENT));
                int action = MotionEventCompat.getActionMasked(motionEvent);
                float flOffsetX=0;
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        flInitialX = motionEvent.getX();

                        setBackgroundResource(R.drawable.draw_table_row_border);

                        break;
                    case (MotionEvent.ACTION_MOVE):




                        flOffsetX = motionEvent.getX()-flInitialX;
                        setLeft(getLeft()+
                                Math.round(
                                        MainUIActivity.Pixel2DP(flOffsetX,getContext())
                                ));
                        setRight(getRight() +
                                Math.round(
                                        MainUIActivity.Pixel2DP(flOffsetX, getContext())
                                ));


                        setBackgroundResource(R.drawable.draw_table_row_border);

                        break;
                    case (MotionEvent.ACTION_UP):
                        setBackground(null);

                        break;
                    case (MotionEvent.ACTION_CANCEL):
                        setBackground(null);
                        break;
                    default:
                        break;
                }






                //trigger delete if the view is half invisible
                int QuaSize =(getRight()-getLeft())/4;

                if(myAppSettings.SwipeLeftToDelete() && action==motionEvent.ACTION_MOVE)
                {
                    //swipe left to delete situation
                    if((QuaSize*-1)>=getLeft()) {
                        //to delete
                        ShowDeleteConfirmation("", myAppSettings.SwipeLeftToDelete());
                    }
                    else if(QuaSize<=getLeft())
                    {
                        //to edit
                        //ShowMessageBox("SwipeLeft right","Edit order!");
                    }

                }
                else if(!myAppSettings.SwipeLeftToDelete() && action==motionEvent.ACTION_MOVE)
                {
                    //swipe right to delete
                    if(QuaSize<=getLeft())
                    {
                        ShowDeleteConfirmation("", myAppSettings.SwipeLeftToDelete());
                    }
                    else  if((QuaSize*-1)>=getLeft())
                    {
                        //ShowMessageBox("SwipeLeft left","Edit order!");
                    }
                }

                else {
                    //motionEvent.setSource(tbl.getId());
                    myGestureDetector.onTouchEvent(motionEvent);
                }
                return true;
            }
        });
    }
    protected void ConfigureGestureDetector()
    {
        myGestureDetector = new GestureDetector(getContext(),new GestureDetector.SimpleOnGestureListener() {
            private float flingMin=0;//because the row will follow the finger movement so we can only get very small offset for X (0.xxx)
            private float velocityMin=50;


            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {
                Log.d("Listener", "onScroll");




                return false;
            }

            @Override
            public boolean onDown(MotionEvent e1)
            {
                //mainTextView.setText("on down");
                Log.d("Listener","onDown");
                return true;
            }

            @Override
            public boolean onFling(final MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
            {


                    if(myAppSettings.SwipeLeftToDelete())
                    {
                        if(e2.getX()<=e1.getX())
                        {
                            ShowDeleteConfirmation("",myAppSettings.SwipeLeftToDelete());
                        }
                        else
                        {
                            common.Utility.ShowMessage("fling right", "Edit order!", getContext(), R.drawable.message);
                        }
                    }
                    else
                    {
                        if(e2.getX()>e1.getX())
                        {

                            ShowDeleteConfirmation("",myAppSettings.SwipeLeftToDelete());
                        }
                        else
                        {
                            common.Utility.ShowMessage("fling left","Edit order!",getContext(),R.drawable.message);
                        }
                    }






                return false;
            }
        });
    }
    /*protected  boolean SwipeLeftToDelete()
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean blnRightHanded = sp.getBoolean(MainUIActivity.PREFERRENCE_LEFT_HANDED_SETTING_KEY,true);
        return !blnRightHanded;

    }*/
    protected   void ShowMessageBox(String strTitle,String strMsg,int iconId)
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
    protected void ShowDeleteConfirmation(String strOrderedItem, final boolean blnSwipeLeftToDelete)
    {
        if(blnRemoveDialogLaunched)return;
        blnRemoveDialogLaunched = true;
        final TableRow tr = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 2. Chain together various setter methods to set the dialog characteristics
        if(strOrderedItem.length()>0) {
            builder.setMessage(Html.fromHtml(getResources().getString(R.string.to_remove_item_sentence) + " \"<i>" + strOrderedItem + "</i>\"?"))
                    .setTitle(R.string.label_alert_popup_title_for_delete_ordered_item);
        }
        else
        {
            builder.setMessage(Html.fromHtml(getResources().getString(R.string.to_remove_item_sentence) + "?"))
                    .setTitle(R.string.label_alert_popup_title_for_delete_ordered_item);
        }

        // 3. Get the AlertDialog from create()

        // Add the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                parentFragment.RemoveModifierRow(tr);
                //DeleteSelectedItemRow(blnSwipeLeftToDelete);
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();

            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                blnRemoveDialogLaunched=false;
            }
        });
        builder.show();
    }
}
