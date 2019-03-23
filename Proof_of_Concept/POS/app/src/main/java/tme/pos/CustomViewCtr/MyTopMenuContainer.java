package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;



import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by kchoy on 10/10/2014.
 */
public class MyTopMenuContainer extends LinearLayout {

    private Rect currentRect,movingRect,newRect;
    private int swapTargetX1=-1,swapTargetX2=-1,swapTargetIndex=-1;
    private long previousSelectedId=-1;
    public MyCategoryItemView selectedChildItem;
    private MyCategoryItemView swapChildItem1,swapChildItem2;
    private Runnable drawThread;
    private Handler drawHandle = new Handler();
    private final int FRAME_RATE = 0;
    final int delta = 10;
    public boolean blnPopupWindowAppear=false;
    public MyTopMenuContainer(Context context) {

        super(context);
        Configure();
    }

    public MyTopMenuContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            Configure();
        }
    }

    public MyTopMenuContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            Configure();
        }
    }
protected void Configure() {


    setOnTouchListener(new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = MotionEventCompat.getActionMasked(motionEvent);
            float flInitialY=0f;

            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    //flInitialY= motionEvent.getRawY();


                    break;
                case (MotionEvent.ACTION_MOVE):

                    break;
                case (MotionEvent.ACTION_UP):
                    /*if( flInitialY<25 ){
                        ShowMessageBox("Initial raw y",""+flInitialY);
                        MainUIActivity mua = (MainUIActivity)getContext();
                        if(!mua.getActionBar().isShowing()){mua.getActionBar().show();}
                    }*/
                    break;
                case (MotionEvent.ACTION_CANCEL):


                    break;
                default:
                    break;
            }
            return true;
        }
    });
}
    public void ParentScrollStop()
    {
        MyHorizontalScrollView myParent = (MyHorizontalScrollView)getParent();
        Rect r = new Rect();
        myParent.getDrawingRect(r);


        //check current selected child 1st if any
        if(selectedChildItem!=null)
        {
            int middle=(selectedChildItem.getLeft()+selectedChildItem.getRight())/2;

            if(r.left< middle && r.right>middle)
            {
                //still visible
            }
            else
            {
                DeselectCurrentItem();
                movingRect=null;
                currentRect=null;
                invalidate();
            }

        }

    }
    public void DeselectCurrentItem()
    {
        if (selectedChildItem != null) {

            //selectedChildItem.setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
            selectedChildItem.setTextColor(getResources().getColor(R.color.green));
            selectedChildItem.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            selectedChildItem.setTypeface(selectedChildItem.getTypeface(), Typeface.NORMAL);
            selectedChildItem = null;
            ((MainUIActivity)getContext()).HideViewPager();
        }
    }
    private void MakeSelectedChildVisibleOnScreen(MyCategoryItemView newSelectedChild)
    {
        MyHorizontalScrollView myParent = (MyHorizontalScrollView)getParent();
        Rect displaying_r = new Rect();
        myParent.getDrawingRect(displaying_r);
        if(newSelectedChild.getLeft()< displaying_r.left)
        {
            //scroll to left
            //ShowMessageBox("scroll to left", "newSelected child left: " + newSelectedChild.getLeft() + ", current left: " + displaying_r.left);
            myParent.scrollTo(newSelectedChild.getLeft() - 30, 0);
            //myParent.scrollTo(242,0);
        }
        else if(newSelectedChild.getRight()> displaying_r.right)
        {
            //scroll to right
            //ShowMessageBox("scroll to right","newSelected child left: "+newSelectedChild.getLeft()+", current left: "+displaying_r.left);
            myParent.scrollTo(displaying_r.left+( newSelectedChild.getRight()-displaying_r.right)+30,0);
        }

        //call invalidate after scroll to selected item in order to show border
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               invalidate();

            }
        },100);
    }

    public void DrawChildBorder(Rect r,final MyCategoryItemView newSelectedChild)
    {

                if(movingRect!=null)return;//ignore if the transition still in progress
                if(currentRect!=null && r.left==currentRect.left)return;//ignore if the new item is currently selected

                //make child completely visible on screen
                //MakeSelectedChildVisibleOnScreen(newSelectedChild);

                newRect=r;
                if(selectedChildItem!=null) {
                    long ID1 = Long.parseLong(selectedChildItem.getTag().toString());
                    long ID2 = Long.parseLong(newSelectedChild.getTag().toString());
                    if (Long.parseLong(selectedChildItem.getTag().toString()) !=  Long.parseLong(newSelectedChild.getTag().toString())) {
                        DeselectCurrentItem();
                    }
                }
                //save selected item
                selectedChildItem = newSelectedChild;

            selectedChildItem.setTextColor(Color.BLACK);
            selectedChildItem.setTypeface(selectedChildItem.getTypeface(), Typeface.BOLD);
            selectedChildItem.setBackgroundColor(Color.WHITE);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MakeSelectedChildVisibleOnScreen(newSelectedChild);

                }
            },100);






    }
   @Override
    protected void onDraw(Canvas canvas)
   {
       //draw the existing border
       super.onDraw(canvas);
       try {

            if(swapChildItem1!=null && swapChildItem2!=null)
            {
                if(swapChildItem1.getLeft()!=swapTargetX1)
                {


                    int offset = Math.abs(swapChildItem1.getLeft()-swapTargetX1);
                    if(offset<20)
                    {
                        swapChildItem1.setLeft(swapTargetX1);
                        swapChildItem2.setLeft(swapTargetX2);
                    }
                    else {
                        //item 1 is moving to left
                        swapChildItem1.setLeft(swapChildItem1.getLeft() - 20);
                        //item 2 is moving to right
                        swapChildItem2.setLeft(swapChildItem2.getLeft() + 20);
                    }


                    invalidate();
                }
                else
                {

                    swapTargetX1=-1;
                    swapTargetX2=-2;

                    //exchange layout position
                    this.removeView(swapChildItem1);
                    this.addView(swapChildItem1,swapTargetIndex-2);
                    this.removeViewAt(swapTargetIndex-1);
                    this.addView(swapChildItem2,swapTargetIndex);


                    //no item has been selected yet, if during initial launch
                    if(previousSelectedId!=-1) {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < getChildCount(); i++) {
                                    if(getChildAt(i) instanceof MyCategoryItemView){
                                    //if (i % 2 == 0) {
                                        if (previousSelectedId == Long.parseLong(getChildAt(i).getTag().toString()))
                                        {
                                            MyCategoryItemView item = (MyCategoryItemView) getChildAt(i);
                                            //ShowMessageBox("redraw selected item",item.getText()+"");
                                            DrawChildBorder(new Rect(item.getLeft(), item.getTop(), item.getRight(), item.getBottom()), item);
                                            previousSelectedId = -1;
                                            selectedChildItem = item;
                                            break;
                                        }
                                    }
                                }
                            }
                        };
                        Handler h = new Handler();
                        h.postDelayed(r, 500);
                    }


                    swapChildItem1=null;
                    swapChildItem2=null;
                }
            }

            if(newRect==null && currentRect!=null && movingRect==null)
            {

                //other area trigger redraw on screen
                movingRect = new Rect(currentRect.left,currentRect.top,currentRect.right,currentRect.bottom);
                DrawBorder(canvas);
                movingRect = null;
            }
            else if (newRect != null) {
                //update before drawing
                UpdateMovingRectPosition();

                //now draw the tip of the triangle
                DrawBorder(canvas);

                if(movingRect.left!=newRect.left) {

                    invalidate();
                }
                else
                {
                    //complete the transition. stop the thread

                    currentRect = new Rect(newRect.left,newRect.top,newRect.right,newRect.bottom);
                    movingRect = null;
                    newRect = null;
                    ((MainUIActivity)getContext()).SetTopCategoryContainerBusyFlag(false);

                   //this.setBackground(new BitmapDrawable(getResources(),getViewBitmap(this)));

                }
            }
        }
        catch(Exception ex)
        {
            ShowErrorMessageBox("onDraw",ex);
        }

   }

    private void UpdateMovingRectPosition()
    {

        if(currentRect==null)
        {
            currentRect = new Rect(newRect.left-1, newRect.top, newRect.right-1, newRect.bottom);
        }
        if(movingRect==null)
        {
            movingRect = new Rect(currentRect.left, currentRect.top, currentRect.right, currentRect.bottom);
        }


        if(movingRect.left!=newRect.left)
        {
            //update the next moving position




            //get next position to draw
            if (newRect.left > movingRect.left) {
                //move to right

                movingRect.left=movingRect.left+(Math.abs(newRect.left-movingRect.left)/2);//movingRect.left+delta;
                movingRect.right=movingRect.right+(Math.abs(newRect.right-movingRect.right)/2);//movingRect.right+delta;


            } else {
                //move to left

                movingRect.left=movingRect.left-(Math.abs(movingRect.left-newRect.left)/2);//movingRect.left-delta;
                movingRect.right=movingRect.right-(Math.abs(movingRect.right-newRect.right)/2);//movingRect.right-delta;


            }

            if(Math.abs(movingRect.left-newRect.left)<delta)
            {
                movingRect.left = newRect.left;
                movingRect.right = newRect.right;
            }




        }
        else
        {

            //complete the transition. stop the thread

            currentRect = new Rect(newRect.left,newRect.top,newRect.right,newRect.bottom);
            movingRect = null;
            newRect = null;

        }

    }
    private void DrawBorder(Canvas canvas)
    {



        if(movingRect!=null) {
            float fl10 = common.Utility.DP2Pixel(10,getContext());
            float fl20 = common.Utility.DP2Pixel(20,getContext());
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            //gradient color
//            Shader textShader=new LinearGradient(0, 5, 5, 10,
//                    new int[]{Color.GREEN,Color.BLUE},
//                    new float[]{0, 1}, Shader.TileMode.CLAMP);
//
//
//
//             Shader textShader2=new LinearGradient(0, 0, 0,  fl10,
//                    Color.GREEN,Color.BLUE,
//                    Shader.TileMode.REPEAT);
            //paint.setShader(textShader2);
            paint.setColor(getResources().getColor(R.color.light_green));
            paint.setStrokeWidth(common.Utility.DP2Pixel(6,getContext()));
            paint.setStyle(Paint.Style.STROKE);
            float fl8 = common.Utility.DP2Pixel(8,getContext());
            float fl12 = common.Utility.DP2Pixel(12,getContext());
            canvas.drawRoundRect(new RectF(movingRect.left - fl8, movingRect.top - fl8, movingRect.right + fl12, movingRect.bottom + fl12), fl20, fl20, paint);

            //draw outer round rect
            paint.setColor(Color.GREEN);
            //paint.setStrokeWidth(6);
            paint.setStrokeWidth(common.Utility.DP2Pixel(6,getContext()));
            paint.setStyle(Paint.Style.STROKE);
            //canvas.drawRoundRect(new RectF(movingRect.left - 10, movingRect.top - 10, movingRect.right + 10, movingRect.bottom + 10), 20, 20, paint);

            canvas.drawRoundRect(new RectF(movingRect.left - fl10, movingRect.top - fl10, movingRect.right + fl10, movingRect.bottom + fl10), fl20, fl20, paint);



            //draw outer round rect
            paint.setColor(Color.WHITE);

            paint.setStyle(Paint.Style.STROKE);



            float fl2 = common.Utility.DP2Pixel(2,getContext());
            canvas.drawRoundRect(new RectF(movingRect.left + fl2, movingRect.top + fl2, movingRect.right - fl2, movingRect.bottom - fl2), fl20, fl20, paint);

            //draw bottom triangle

            paint.setStrokeWidth(common.Utility.DP2Pixel(4,getContext()));
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            int Middle=(movingRect.right+movingRect.left)/2;

            int intMargin= Math.round(fl10);
            int x1 = Middle-intMargin;
            int x2 = Middle+intMargin;
            int y1 = movingRect.bottom+Math.round(fl10);
            int y2 = y1+common.Utility.DP2Pixel(7,getContext());

            Point a = new Point(x1,y1);
            Point b = new Point(x2,y1);
            Point c = new Point(Middle,y2);

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(a.x, a.y);
            path.lineTo(b.x, b.y);
            path.lineTo(c.x, c.y);

            path.close();
            canvas.drawPath(path,paint);

            paint.setColor(getResources().getColor(R.color.light_green));
            //paint.setColor(getResources().getColor(R.color.mutual_dark_red));
            paint.setStrokeWidth(common.Utility.DP2Pixel(2,getContext()));
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(a.x,a.y+(common.Utility.DP2Pixel(7,getContext())/2),c.x,c.y+common.Utility.DP2Pixel(2,getContext()),paint);
            canvas.drawLine(b.x,a.y+(common.Utility.DP2Pixel(7,getContext())/2),c.x,c.y+common.Utility.DP2Pixel(2,getContext()),paint);








        }
    }

    protected void ShowErrorMessageBox(String strMethodName,Exception ex)
    {
        Log.d("EXCEPTION: " + strMethodName,  ""+ex.getMessage()+", Cause "+ex.getCause().toString());

        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strMethodName);
        messageBox.setMessage(ex.getMessage()+", Cause "+ex.getCause().toString());
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        messageBox.show();
    }
    public void SwapRight(MyCategoryItemView item1)
    {


            //get the current item right child to pass it in SwapLeft()
            int targetIndex = -1;
            for (int i = 0; i < getChildCount(); i++) {
                //divider child in between
                if (getChildAt(i) instanceof MyCategoryItemView) {
                    //if(i % 2==0)
                    //{
                    if (Long.parseLong(item1.getTag().toString()) == Long.parseLong(getChildAt(i).getTag().toString())) {
                        targetIndex = i;
                        break;
                    }
                }

            }

            if (targetIndex == -1) return;


            //the right child is not the last dummy child
            if (targetIndex + 2 != getChildCount() - 1) {
                SwapLeft((MyCategoryItemView) getChildAt(targetIndex + 2));
            }



    }
    public void SwapLeft(MyCategoryItemView item1)
    {

            long lnTemp = previousSelectedId;
            if (selectedChildItem != null) {

                previousSelectedId = Long.parseLong(selectedChildItem.getTag().toString());
                if(lnTemp!=previousSelectedId) {
                    //DeselectCurrentItem();
                }


            }
            swapTargetIndex = -1;
            for (int i = 0; i < getChildCount(); i++) {
                //divider child in between
                if(getChildAt(i) instanceof MyCategoryItemView){
                //if (i % 2 == 0) {

                    if (Long.parseLong(item1.getTag().toString()) == Long.parseLong(getChildAt(i).getTag().toString())) {
                        swapTargetIndex = i;
                        swapChildItem1 = (MyCategoryItemView) getChildAt(i);
                        break;
                    }
                }
            }

            //do nothing if already the left most
            if (swapTargetIndex < 1) {
                swapTargetIndex = -1;

                return;
            }

            //get the previous item
            swapChildItem2 = (MyCategoryItemView) getChildAt(swapTargetIndex - 2);
            swapTargetX1 = swapChildItem2.getLeft();
            swapTargetX2 = item1.getLeft();
           /* Log.d("swap left","child 1: "+swapChildItem1.getTag().toString()+" , child 2: "+swapChildItem2.getTag().toString());
            ((MainUIActivity)getContext()).myMenu.SwapCategory(swapChildItem1.getTag().toString(),swapChildItem2.getTag().toString());*/
            //ShowMessageBox("swap left", "child 1 index:" + swapTargetIndex + ", child 2 index:" + (swapTargetIndex - 2));
            invalidate();




    }
    public void DeleteCategory(final MyCategoryItemView item)
    {
        int index = -1;
        View divider = null;
        final MyTopMenuContainer tmc = this;
        for(int i =0;i<getChildCount();i++)
        {
            if(getChildAt(i) instanceof MyCategoryItemView){
            //if(i % 2==0) {
                if (getChildAt(i).getTag().toString().equalsIgnoreCase(item.getTag().toString())) {
                    index = i;

                    break;
                }
            }
        }
        if(index>-1)
        {
            final int k = index;
            //deselect the selected item if is the same
            if(selectedChildItem!=null) {
                if (item.getTag().toString().equalsIgnoreCase(selectedChildItem.getTag().toString())) {
                    DeselectCurrentItem();
                    currentRect=null;
                }
            }

            TranslateAnimation movement = new TranslateAnimation(0.0f, 0.0f, 0.0f, -5000.0f);//move up
            movement.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    removeViewAt(k+1);
                    removeViewAt(k);//remove the divider after as well

                    //redraw the category border
                    if(selectedChildItem!=null)
                    {
                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                DrawChildBorder(new Rect(selectedChildItem.getLeft(), selectedChildItem.getTop(), selectedChildItem.getRight(), selectedChildItem.getBottom()), selectedChildItem);
                            }
                        },0);
                    }


                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            movement.setDuration(500);
            movement.setFillAfter(true);



            item.startAnimation(movement);

        }
    }
    public void MoveCategory(String key1,String key2)
    {
        try {


            int FromIndex = -1;
            int ToIndex = -1;
            int count = 0;
            for (int i = 0; i < this.getChildCount() - 1; i++) {//exclude dummy category view
                if(this.getChildAt(i) instanceof MyCategoryItemView){
                //if (i % 2 == 0) {
                    if (this.getChildAt(i).getTag().toString().compareTo(key1) == 0) {
                        FromIndex = i;
                        count++;
                    } else if (this.getChildAt(i).getTag().toString().compareTo(key2) == 0) {
                        ToIndex = i;
                        count++;
                    }
                    if (count > 1) break;
                }
            }

            //View v1 = this.getChildAt(FromIndex);
            //this.removeView(v1);

            View vFrom = this.getChildAt(FromIndex);
            View vTo = this.getChildAt(ToIndex);
            this.removeView(vTo);
            this.removeView(vFrom);
            if (FromIndex < ToIndex) {


                this.addView(vTo,FromIndex);
                this.addView(vFrom,ToIndex);



                //this.addView(v1, ToIndex - 1);
            } else {


                this.addView(vFrom,ToIndex);
                this.addView(vTo,FromIndex);
                //this.addView(v1, ToIndex);
            }

        }
        catch(Exception ex)
        {
            ShowErrorMessageBox("Move Category",ex);
        }

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
}
