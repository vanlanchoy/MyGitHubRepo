package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.IItemViewUpdateUnit;
import tme.pos.Interfaces.IMenuItemClickedListener;
import tme.pos.Interfaces.IToBeUpdatedInventoryView;
import tme.pos.ItemInventoryOptionDialog;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.MyDoubleTapGestureListener;
import tme.pos.R;

/**
 * Created by kchoy on 5/26/2016.
 */
public class MenuItemFlipablePicView extends LinearLayout implements IItemViewUpdateUnit
        ,IToBeUpdatedInventoryView {
    IMenuItemClickedListener listener;
    long itemId;
    int inventoryCount;
    public MenuItemFlipablePicView(Context c)
    {
        super(c);
    }
    public void SetListener(IMenuItemClickedListener l)
    {
        listener = l;
    }
    public void SetProperties(ItemObject io, int inventoryCount)
    {
        this.inventoryCount = inventoryCount;
        itemId = io.getID();
        //construct item name label
        TextView tv = new TextView(getContext());
        //limit to 30 chars
        String strName = (io.getName().length()>30)?io.getName().substring(0,27)+"...":io.getName();
        tv.setText(strName);


        //construct image
        ImageView imgView = new ImageView(getContext());
        common.Utility.LoadPicture(imgView, io.getPicturePath(),getContext());

        //construct price label
        TextView tvPrice = new TextView(getContext());
        tvPrice.setText(common.Utility.ConvertBigDecimalToCurrencyFormat(io.getPrice()));
        tvPrice.setTextColor((io.getPrice().doubleValue()<0) ? getResources().getColor(R.color.pink_red) : getResources().getColor(R.color.black));

        //add to container

        setOrientation(LinearLayout.VERTICAL);
        setTag(io.getID());
        final GestureDetector gd = new GestureDetector(getContext(), new MyDoubleTapGestureListener(this));
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gd.onTouchEvent(motionEvent);
                int action = MotionEventCompat.getActionMasked(motionEvent);

                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        setBackgroundColor(getResources().getColor(R.color.selected_row_green));

                        break;
                    case (MotionEvent.ACTION_MOVE):



                        break;
                    case (MotionEvent.ACTION_UP):
                        setBackgroundColor(getResources().getColor(R.color.white));

                        break;
                    case (MotionEvent.ACTION_CANCEL):
                        setBackgroundColor(getResources().getColor(R.color.white));

                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(100,100);
        imgView.setLayoutParams(lllp);
        addView(imgView);
        addView(tv);
        addView(tvPrice);
    }
    public void UpdateInventoryCount(int newInventoryCount){inventoryCount = newInventoryCount;}
    public long GetItemId(){return itemId;}

    @Override
    public void ItemMenuDialogUnitAdded(int affectedUnit) {
        UpdateInventoryCount(inventoryCount-affectedUnit);
    }

    @Override
    public void InventoryDialogUpdateUnitCount(int unitCount) {
        UpdateInventoryCount(unitCount);
    }

    private class MyDoubleTapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
        View currentObject;
        //int itemIndex;
        public MyDoubleTapGestureListener(View v)
        {
            currentObject = v;
            //itemIndex = (Integer)v.getTag();
        }
        @Override
        public boolean onDoubleTapEvent(MotionEvent event)
        {
            if(event.getAction()==1)
            {

                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                        R.anim.flip_vertical);
                anim.setTarget(currentObject);
                anim.setDuration(300);
                anim.start();

            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(listener!=null)
            {
                listener.ListItemSingleTapped(itemId,MenuItemFlipablePicView.this,inventoryCount);//not using table row and no inventory inf
            }

            return true;

        }
        @Override
        public boolean onDoubleTap(MotionEvent event)
        {
            if(listener!=null)
            {
                listener.ListItemDoubleTapped(itemId,MenuItemFlipablePicView.this,inventoryCount);//not using table row and no inventory info
            }

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
            return false;
        }
    }
}
