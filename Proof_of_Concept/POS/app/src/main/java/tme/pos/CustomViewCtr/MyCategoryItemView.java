package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.CategoryOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.R;

/**
 * Created by kchoy on 10/10/2014.
 */
public class MyCategoryItemView extends TextView implements CategoryOptionPopup.OnCategoryDialogDismissListener{

    protected GestureDetector myGestureDetector;
    MyCategoryItemView currentView;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public MyCategoryItemView(Context context) {
        super(context);
        if(!isInEditMode()) {
            Configure();
        }
    }

    public MyCategoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()) {
            Configure();
        }
    }

    public MyCategoryItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(!isInEditMode()) {
            Configure();
        }
    }
    protected void Configure()
    {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(210,80);
        lp.setMargins(20,20,20,20);
        this.setLayoutParams(lp);
        ImplementGestureListener();
        this.setOnFocusChangeListener(new MyCategoryItemView_OnFocusChangeListener());

    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

    }

    protected void ShowIsBusyMessage()
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
    }

    protected void ShowOptionPopup()
    {


        CategoryOptionPopup cop = new CategoryOptionPopup(getContext(),this,getText()+"",Long.parseLong(currentView.getTag().toString()));
        cop.ShowPopup();

    }
    public void ImplementGestureListener()
    {
        setOnTouchListener(new MyCategoryItemView_OnTouchListener());
        myGestureDetector = new GestureDetector(getContext(),new MyCategoryItemView_GestureDetector());
        currentView = this;
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
    public static int DP2Pixel(float dp,Context context)
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (metrics.density * dp + 0.5f);
    }



    @Override
    public void onCategoryDialogCanceled() {
        if(this.getParent() instanceof MyTopMenuContainer) {
            MyTopMenuContainer tmc = (MyTopMenuContainer) this.getParent();

            if (tmc.selectedChildItem == null) {
                setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            } else if (tmc.selectedChildItem.getTag().toString().equalsIgnoreCase(currentView.getTag().toString())) {
                setBackgroundColor(getResources().getColor(R.color.white));
            } else {
                setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            }
        }
        else if(this.getParent() instanceof MyDragAndDropGridView)
        {
            //MyDragAndDropGridView gv = (MyDragAndDropGridView) this.getParent();
            //gv.selectedChildItem
        }
        ((MainUIActivity)getContext()).SetPopupShow(false);
    }

    @Override
    public void onCategorySave(String strName,long Id){
        if(Id>-1)
        {
            //update
            ((MainUIActivity)getContext()).UpdateCategoryName(strName, Id);
        }
        else
        {
            //create new
            ((MainUIActivity)getContext()).CreateCategory(strName);
        }
    }

    @Override
    public void onCategoryDelete(long CategoryId) {
        ((MainUIActivity)getContext()).DeleteCategory(currentView);
    }



    public class MyCategoryItemView_OnTouchListener implements OnTouchListener
    {
        @Override
        public boolean onTouch(View view,MotionEvent motionEvent)
        {

            //ShowErrorMessageBox("My Category item view onTouch",new Exception());
            if(((MainUIActivity)getContext()).GetIsTopCategoryContainerBusy())
            {
                ShowIsBusyMessage();
                return false;
            }
            myGestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }
    public class MyCategoryItemView_OnFocusChangeListener implements OnFocusChangeListener{
        @Override
        public void onFocusChange(View view, boolean b) {
            //ShowMessageBox("focus changed", "Item view boolean " + b);
        }

    }
    public void SelectCategoryItemView(StoreItem si, int ItemIndexOnReceipt )
    {
        common.Utility.LogActivity("Category ["+getText()+"] selected");
        MyTopMenuContainer tmc = (MyTopMenuContainer) currentView.getParent();
        //ShowMessageBox("item","selected new item, flag is not "+tmc.blnPopupWindowAppear);
        tmc.DrawChildBorder(new Rect(currentView.getLeft(), currentView.getTop(), currentView.getRight(), currentView.getBottom()), currentView);
        //.DrawSelectedTriangle(location[0], location[0] + getLayoutParams().width, location[1] + getLayoutParams().height);
        MainUIActivity mua = (MainUIActivity) getContext();

        mua.ShowPageItemLoading(currentView.getTag().toString(),si);
        //mua.LoadCategorySubMenuItem(currentView.getTag().toString(),si,ItemIndexOnReceipt);
    }
    public class MyCategoryItemView_GestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
    {
        public MyCategoryItemView_GestureDetector()
        {

        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
           //try {
            //LinearLayout prt = (LinearLayout) currentView.getParent();
            //if(prt.getLayoutTransition().isChangingLayout() ||prt.getLayoutTransition().isRunning())
            if(((MainUIActivity)getContext()).GetIsTopCategoryContainerBusy())
            {
                //Log.d("tap","show");
                //ShowIsBusyMessage();
                return false;
            }
            //Log.d("tap","selected");

               SelectCategoryItemView(null, -1);


            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {

            if(Long.parseLong(currentView.getTag().toString())==common.text_and_length_settings.PROMOTION_CATEGORY_ID)return false;
            if(((MainUIActivity)getContext()).GetIsTopCategoryContainerBusy())return false;

                ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(),
                        R.anim.flip_vertical);
                anim.setTarget(currentView);
                anim.setDuration(400);
                anim.start();
                setBackgroundColor(getResources().getColor(R.color.selected_row_green));
                ShowOptionPopup();

            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent motionEvent) {
            return false;
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
