package tme.pos.CustomViewCtr;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import tme.pos.BusinessLayer.StoreItem;
import tme.pos.BusinessLayer.common;
import tme.pos.CategoryOptionPopup;
import tme.pos.MainUIActivity;
import tme.pos.MyCategoryItemViewBaseAdapter;
import tme.pos.R;

/**
 * Created by vanlanchoy on 11/2/2014.
 */
public class MyGridViewCategoryItemView extends MyCategoryItemView{
    boolean blnDrawborder = false;
    public MyGridViewCategoryItemView(Context context) {
        super(context);
    }

    public MyGridViewCategoryItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyGridViewCategoryItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
   /* public void Drawborder(boolean flag)
    {
        blnDrawborder = flag;
        //invalidate();
    }
    private void DrawBorder(Canvas canvas)
    {

        Rect movingRect = new Rect(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());


            float fl10 = common.Utility.DP2Pixel(10,getContext());
            float fl20 = common.Utility.DP2Pixel(20,getContext());
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);


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









    }*/
    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        //if(blnDrawborder) {
            //DrawBorder(canvas);
        //}
    }
    @Override
    public void ImplementGestureListener()
    {
        setOnTouchListener(new MyCategoryGridViewItemView_OnTouchListener());
        myGestureDetector = new GestureDetector(getContext(),new MyCategoryGridViewItemView_GestureDetector());
        currentView = this;
    }
    @Override
    public void onCategoryDialogCanceled()
    {
        RestoreViewColor();
    }
    @Override
    protected void ShowOptionPopup()
    {
        CategoryOptionPopup cop = new CategoryOptionPopup(getContext(),this,getText()+"",Long.parseLong(currentView.getTag().toString()));
        cop.ShowPopup();
        /*
        int[] btnLocation = new int[2];
        getLocationOnScreen(btnLocation);
        final GridView gvCategory = (GridView)this.getParent();
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
        final View popupView = layoutInflater.inflate(R.layout.layout_category_item_option_popup_menu, null);

        //ShowMessageBox("Grid View popup view","width:"+popupView.getWidth()+", height:"+popupView.getHeight());
        final PopupWindow pWindow = new PopupWindow(
                popupView,DP2Pixel(55*5,getContext()), DP2Pixel(43,getContext()));

        try {


            Button btnDel = (Button) popupView.findViewById(R.id.btnDelCategory);
            btnDel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ShowMessageBox("grid view btn del click","delete");
                    //setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
                    RestoreViewColor();
                    pWindow.dismiss();
                    MainUIActivity mua = (MainUIActivity) getContext();
                    mua.DeleteCategory(currentView);
                }
            });
            Button btnEdit = (Button) popupView.findViewById(R.id.btnEditCategory);
            btnEdit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //ShowMessageBox("click","edit");
                    //setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
                    RestoreViewColor();
                    pWindow.dismiss();
                    MainUIActivity mua = (MainUIActivity) getContext();
                    //mua.ShowEditCategoryNamePopup(currentView);
                    mua.ShowEditCategoryNamePopup(currentView.getText().toString(),currentView.getTag().toString());
                }
            });
            //remove hot list button
            Button btnHot = (Button) popupView.findViewById(R.id.btnHot);
            LinearLayout ll = (LinearLayout) popupView.findViewById(R.id.PopupOptionPanel);
            ll.removeView(btnHot);

            pWindow.setWidth(DP2Pixel(55 * ll.getChildCount(), getContext()));

            pWindow.setTouchInterceptor(new OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_OUTSIDE) {

                        RestoreViewColor();
                        //setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));

                        pWindow.dismiss();

                        return true;

                    }
                    return false;
                }
            });


            //need this line else setTouchInterceptor won't work
            Bitmap bm = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            pWindow.setBackgroundDrawable(new BitmapDrawable(
                    getContext().getResources(),
                    bm
            ));


            popupView.setFocusable(true);


            //pWindow.setFocusable(true);
            //pWindow.setTouchable(true);
            pWindow.setOutsideTouchable(true);
            pWindow.showAtLocation(popupView, Gravity.NO_GRAVITY, btnLocation[0], btnLocation[1] + getHeight());
        }
        catch(Exception ex)
        {
            //ShowErrorMessageBox("ShowOptionPopup",ex);
        }
        */
    }
   /* public void Select(StoreItem si, int ItemIndexOnReceipt)
    {
        //draw border around the item and collapse
        GridView gvParent = (GridView) currentView.getParent();
        for (int i = 0; i < gvParent.getChildCount() - 1; i++)//exclude the last dummy item
        {
            ((MyGridViewCategoryItemView) gvParent.getChildAt(i)).setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
            gvParent.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));

        }
        currentView.setBackground(getContext().getResources().getDrawable(R.drawable.draw_two_round_rect));



        //collapse, draw border and make the item in top category container selected
        MainUIActivity mua = (MainUIActivity) getContext();
        MyTopMenuContainer tmc = (MyTopMenuContainer) mua.findViewById(R.id.CategoryContainer);
        ActivityLinearLayout all = (ActivityLinearLayout) mua.findViewById(R.id.MenuPanel);
        GridView gvCategory = (GridView) mua.findViewById(R.id.gvCategory);
        all.SlideUpCategoryGridView(gvCategory, 20, 20, currentView.getTag() + "");

        mua.ShowPageItemLoading(currentView.getTag().toString(),si,ItemIndexOnReceipt);
        //mua.LoadCategorySubMenuItem(currentView.getTag().toString(),si,ItemIndexOnReceipt);
    }*/
    protected  void RestoreViewColor()
    {
        if(currentView==null)return;

        MyCategoryItemViewBaseAdapter adapter =  (MyCategoryItemViewBaseAdapter)((MyDragAndDropGridView)((MainUIActivity)getContext()).findViewById(R.id.gvCategory)).getAdapter();

        if(!currentView.getTag().toString().equalsIgnoreCase(adapter.GetSelectedCategoryTag())) {
            currentView.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            ((TextView) currentView).setTextColor(getResources().getColor(R.color.green));
            //((TextView) currentView).setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
        }
        else
        {
            currentView.setBackgroundColor(getResources().getColor(R.color.white));
            currentView.setBackground(getResources().getDrawable(R.drawable.draw_two_round_rect));
            ((TextView) currentView).setTextColor(getResources().getColor(R.color.black));
        }
    }
    private void SelectedCategory()
    {
        //draw border around the item and collapse
        GridView gvParent = (GridView) currentView.getParent();
        for (int i = 0; i < gvParent.getChildCount() - 1; i++)//exclude the last dummy item
        {
            ((MyGridViewCategoryItemView) gvParent.getChildAt(i)).setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
            gvParent.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
            //gvParent.getChildAt(i).setBackground(null);
        }
        currentView.setBackground(getContext().getResources().getDrawable(R.drawable.draw_two_round_rect));
        //currentView.setBackground(getContext().getResources().getDrawable(R.drawable.draw_gridview_category_selected_item_border));


        //collapse, draw border and make the item in top category container selected

        MainUIActivity mua = (MainUIActivity) getContext();
        //MyTopMenuContainer tmc = (MyTopMenuContainer) mua.findViewById(R.id.CategoryContainer);
        ActivityLinearLayout all = (ActivityLinearLayout) mua.findViewById(R.id.MenuPanel);
        GridView gvCategory = (GridView) mua.findViewById(R.id.gvCategory);
        all.SlideUpCategoryGridView(gvCategory, 20, 20,currentView.getTag() + "");
    }
    public class MyCategoryGridViewItemView_OnTouchListener implements OnTouchListener
    {
        @Override
        public boolean onTouch(View view,MotionEvent motionEvent)
        {

            int action = MotionEventCompat.getActionMasked(motionEvent);
            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    //((MyCategoryItemView)view).setText("action down");
                    currentView.setBackgroundColor(getResources().getColor(R.color.light_green));
                    break;
                case (MotionEvent.ACTION_MOVE):
//                    Log.d("grid view item","Y:"+motionEvent.getY());
                    //((MyCategoryItemView)view).setText("action move");
                    break;
                case (MotionEvent.ACTION_UP):
                    //((MyCategoryItemView)view).setText("action up");
                    break;
                case (MotionEvent.ACTION_CANCEL):
                    //((MyCategoryItemView)view).setText("action cancel");
                   RestoreViewColor();
                    break;
                default:
                    break;
            }

            myGestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }

    public class MyCategoryGridViewItemView_GestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
    {
        public MyCategoryGridViewItemView_GestureDetector()
        {

        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent motionEvent)
        {



            SelectedCategory();

                return true;

        }

        @Override
        public boolean onDoubleTap(MotionEvent motionEvent) {



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

            ((MyDragAndDropGridView)currentView.getParent()).SetChildDragListener(currentView);


        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
            return false;
        }
    }
}
