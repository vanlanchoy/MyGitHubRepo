package tme.pos.CustomViewCtr;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import tme.pos.BusinessLayer.MyMenu;
import tme.pos.BusinessLayer.common;
import tme.pos.CategoryOptionPopup;
import tme.pos.ItemMenuOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.R;


/**
 * Created by vanlanchoy on 10/13/2014.
 */
public class AddNewCategoryItemView extends MyCategoryItemView {
    boolean blnTap=false;
    int MAX_CATEGORY_ITEM=50;

    public AddNewCategoryItemView(Context context) {
        super(context);
        if (!isInEditMode()) {
            Configure();

        }
        Initialize();
    }

    public AddNewCategoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            Configure();
        }
        Initialize();
    }

    public AddNewCategoryItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            Configure();
        }
        Initialize();
    }
    public void Initialize()
    {
        setTag("");
        //max category item
        MAX_CATEGORY_ITEM = Integer.parseInt(getResources().getString(R.string.max_category_item));
    }
    public void ResetTapFlag()
    {
        blnTap = false;
    }
    protected  void ShowMessageBox(String strTitle,String strMsg,int iconId)
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
    @Override
    public void ImplementGestureListener()
    {
        final AddNewCategoryItemView tvCurrentTapToAdd = this;
        //setOnTouchListener(new MyCategoryItemView_OnTouchListener());
        setOnTouchListener(new OnTouchListener() {
            //boolean blnTap = false;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = MotionEventCompat.getActionMasked(motionEvent);
                MainUIActivity mua = ((MainUIActivity)getContext());
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):
                        if(mua.GetIsTopCategoryContainerBusy()){
                            ShowIsBusyMessage();
                            return false;
                        }
                        if(mua.GetIsPopupShown())return false;

                        if(blnTap)return false;
                        blnTap = true;
                        currentView.setTextColor(getResources().getColor(R.color.light_green));



                        break;
                    case (MotionEvent.ACTION_MOVE):
                        Log.d("add new","action move");
                        //blnTap = false;
                        break;
                    case (MotionEvent.ACTION_UP):
                        Log.d("add new","action up");
                        currentView.setTextColor(getResources().getColor(R.color.add_new_category_item_text_grey));
                        if(blnTap)
                        {
                            //if(((MainUIActivity)getContext()).myMenu.GetCategoryList().size()>=MAX_CATEGORY_ITEM)
                            if(common.myMenu.GetCategoryList().size()>=MAX_CATEGORY_ITEM)
                            {
                                common.Utility.ShowMessage("Add Category", "You have reached maximum <b><i>" + MAX_CATEGORY_ITEM + "<i></b> categories, please remove a category in order to add new.",
                                        getContext(),R.drawable.no_access);
                                blnTap = false;
                            }
                            else {
                                mua.SetPopupShow(true);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //MainUIActivity mua = (MainUIActivity) getContext();
                                        CategoryOptionPopup cPopup = new CategoryOptionPopup(getContext(), currentView, "", -1);
                                        cPopup.ShowPopup();
                                        blnTap = false;
                                        //MainUIActivity mua = (MainUIActivity)getContext();
                                        //mua.AddNewCategory(tvCurrentTapToAdd);
                                    }
                                }, 150);
                            }
                        }
                        //blnTap = false;
                        break;
                    case (MotionEvent.ACTION_CANCEL):
                        Log.d("add new","action cancel");
                        currentView.setTextColor(getResources().getColor(R.color.add_new_category_item_text_grey));
                        blnTap = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        //myGestureDetector = new GestureDetector(getContext(),new MyCategoryItemView_GestureDetector());
        currentView = this;
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        //draw some border at four corner
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);

        //paint.setStrokeWidth(4f);
        paint.setStrokeWidth(common.Utility.DP2Pixel(4,getContext()));
        paint.setColor(getResources().getColor(R.color.add_new_category_item_text_grey));
        //canvas.drawRect(0,0,this.getWidth(),this.getHeight(),paint);

        //paint.setColor(Color.BLACK);
        //int lineLength=20;
        int lineLength=Math.round(common.Utility.DP2Pixel(20,getContext()));
        Point pTopLeft1 = new Point(0,0);
        Point pTopLeft2 = new Point(lineLength,0);
        canvas.drawLine(pTopLeft1.x,pTopLeft1.y,pTopLeft2.x,pTopLeft2.y,paint);

        Point pTopLeft3 = new Point(0,0);
        Point pTopLeft4 = new Point(0,lineLength);
        canvas.drawLine(pTopLeft3.x,pTopLeft3.y,pTopLeft4.x,pTopLeft4.y,paint);

        Point pBottomLeft1 = new Point(0,getHeight());
        Point pBottomLeft2 = new Point(0,getHeight()-lineLength);
        canvas.drawLine(pBottomLeft1.x,pBottomLeft1.y,pBottomLeft2.x,pBottomLeft2.y,paint);

        Point pBottomLeft3 = new Point(0,getHeight());
        Point pBottomLeft4 = new Point(lineLength,getHeight());
        canvas.drawLine(pBottomLeft3.x,pBottomLeft3.y,pBottomLeft4.x,pBottomLeft4.y,paint);

        Point pTopRight1 = new Point(getWidth()-lineLength,0);
        Point pTopRight2 = new Point(getWidth(),0);
        canvas.drawLine(pTopRight1.x,pTopRight1.y,pTopRight2.x,pTopRight2.y,paint);

        Point pTopRight3 = new Point(getWidth(),lineLength);
        Point pTopRight4 = new Point(getWidth(),0);
        canvas.drawLine(pTopRight3.x,pTopRight3.y,pTopRight4.x,pTopRight4.y,paint);

        Point pBottomRight1 = new Point(getWidth(),getHeight());
        Point pBottomRight2 = new Point(getWidth(),getHeight()-lineLength);
        canvas.drawLine(pBottomRight1.x,pBottomRight1.y,pBottomRight2.x,pBottomRight2.y,paint);

        Point pBottomRight3 = new Point(getWidth(),getHeight());
        Point pBottomRight4 = new Point(getWidth()- lineLength,getHeight());
        canvas.drawLine(pBottomRight3.x,pBottomRight3.y,pBottomRight4.x,pBottomRight4.y,paint);
    }
//    public class MyCategoryItemView_GestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
//    {
//        public MyCategoryItemView_GestureDetector()
//        {
//
//        }
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
//
//            return false;
//        }
//
//        @Override
//        public boolean onDoubleTap(MotionEvent motionEvent) {
//            return false;
//        }
//
//        @Override
//        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
//            return false;
//        }
//
//        @Override
//        public boolean onDown(MotionEvent motionEvent) {
//           return false;
//        }
//
//        @Override
//        public void onShowPress(MotionEvent motionEvent) {
//
//        }
//
//        @Override
//        public boolean onSingleTapUp(MotionEvent motionEvent) {
//            MainUIActivity mua = (MainUIActivity)getContext();
//
//            mua.AddNewCategory();
//            return true;
//        }
//
//        @Override
//        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
//            return false;
//        }
//
//        @Override
//        public void onLongPress(MotionEvent motionEvent) {
//
//        }
//
//        @Override
//        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
//            return false;
//        }
//    }
}
