package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import tme.pos.BusinessLayer.CategoryObject;
import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.MyCategoryItemViewBaseAdapter;
import tme.pos.MyDragShadowBuilder;
import tme.pos.R;

/**
 * Created by kchoy on 11/7/2014.
 */
public class MyDragAndDropGridView extends GridView{

    public MyDragAndDropGridView(Context context) {
        super(context);

    }

    public MyDragAndDropGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public MyDragAndDropGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }


    protected void SetChildViewDragListener(final View pressedChild)
    {
        final MyCategoryItemViewBaseAdapter adapter =  (MyCategoryItemViewBaseAdapter)this.getAdapter();
        for(int i=0;i<this.getChildCount()-1;i++)//exclude last dummy child
        {
            //unset all
            ((MyGridViewCategoryItemView)this.getChildAt(i)).setOnDragListener(null);
            //((MyGridViewCategoryItemView)this.getChildAt(i)).Drawborder(true);
        }
        for(int i=0;i<this.getChildCount()-1;i++)//exclude last one dummy children
        {
            final MyGridViewCategoryItemView top_view = (MyGridViewCategoryItemView)this.getChildAt(i);
            if(pressedChild.getTag()==top_view.getTag()) continue;//skip if is currently dragging child

            //Log.d("set listener",top_view.getTag()+"");
            top_view.setOnDragListener(new OnDragListener() {
                @Override
                public boolean onDrag(View view, DragEvent dragEvent) {
                    //ShowMessage("on drag","");
                    //MyDragAndDropGridView parent =((MyDragAndDropGridView)view.getParent());
                    switch (dragEvent.getAction()) {
                        case DragEvent.ACTION_DRAG_STARTED:
                            //((MyCategoryItemView)view).setText("started");
                            //Log.d("started", "" + view.getTag().toString());
                            break;
                        case DragEvent.ACTION_DRAG_LOCATION:
                            //((MyCategoryItemView)view).setText("drag location");
/*
                            Log.d("location", "drag event Y:" + dragEvent.getY());

                            Log.d("grid view ","SCroll Y:"+parent.getScrollY());
                            Log.d("grid view ","Y:"+parent.getY());
                            Log.d("grid view  ","bottom:"+parent.getBottom());
                            Rect rec = new Rect();
                            ((LinearLayout)parent.getParent()).getDrawingRect(rec);
                            Log.d("grid view linear layout ","rec bottom:"+rec.top);
                            Log.d("grid view linear layout ","rec bottom:"+rec.bottom);
                            */
                            break;
                        case DragEvent.ACTION_DRAG_ENTERED:

                            //((MyCategoryItemView)view).setText("enter");
                            //ShowMessage("entered",""+view.getTag().toString()+", supported: "+dragEvent.getResult());
                            view.setTop(view.getTop()-15);
                            view.setBottom(view.getBottom()-15);
                            /*
                            Log.d("view ","getY:"+view.getY());
                            Log.d("view ","getBottom:"+view.getBottom());
                            Log.d("view ","ScrollY:"+view.getScrollY());
                            parent.scrollTo(0,Math.round(view.getBottom()));
                            */
                            //parent.scrollTo(0,Math.round(dragEvent.getY()));
                            //view.animate().translationYBy(30);
                            //view.animate().scaleX(1.2f);
                            //view.animate().scaleY(1.2f);
                            //Log.d("entered", "" + view.getTag().toString());

                            break;
                        case DragEvent.ACTION_DRAG_EXITED:
                            //((MyCategoryItemView)view).setText("exited");
                            //view.animate().translationYBy(-30);
                            view.setTop(view.getTop()+15);
                            view.setBottom(view.getBottom()+15);
                            //view.animate().scaleX(1.0f);
                            //view.animate().scaleY(1.0f);
                            //ShowMessage("exited",""+view.getTag().toString()+", supported: "+dragEvent.getResult());
                            //Log.d("exited", "" + view.getTag().toString());
                            break;
                        case DragEvent.ACTION_DROP:
                            //((MyCategoryItemView)view).setText("drop");
                            //View localView = (View)dragEvent.getLocalState();
                            //ShowMessage("dropped", "" + view.getTag().toString() + ", supported: " + dragEvent.getResult());
                            //Log.d("drop","view: "+view.getTag().toString()+", localview: "+localView.getTag().toString());
                            //Log.d("drop", "view: " + view.getTag().toString());
                            MoveChildPosition(pressedChild, view);
                        case DragEvent.ACTION_DRAG_ENDED:

                            //((MyCategoryItemView)view).setText("end");
                            if(!pressedChild.getTag().toString().equalsIgnoreCase(adapter.GetSelectedCategoryTag())) {
                                pressedChild.setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
                                ((TextView) pressedChild).setTextColor(getResources().getColor(R.color.green));
                                //((TextView) pressedChild).setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
                            }
                            else
                            {
                                pressedChild.setBackgroundColor(getResources().getColor(R.color.white));
                                pressedChild.setBackground(getResources().getDrawable(R.drawable.draw_two_round_rect));
                                ((TextView) pressedChild).setTextColor(getResources().getColor(R.color.black));
                            }
                            //Log.d("ended", "" + view.getTag().toString());
                            break;

                        default:
                            //ShowMessage("drag and drop","default");
                            //((MyCategoryItemView)view).setText("default");
                            return false;
                    }
                    return true;
                }
            });
        }
    }
    private void MoveChildPosition(View draggedChild,View child)
    {
        //save the new order
        MainUIActivity mua = (MainUIActivity) getContext();
        common.myMenu.MoveCategory(draggedChild.getTag().toString(),
                child.getTag().toString());


        //move the GUI
        MyCategoryItemViewBaseAdapter adapter = (MyCategoryItemViewBaseAdapter)this.getAdapter();
        ArrayList<CategoryObject>cList =adapter.GetCategories();

        int FromIndex = -1;
        int ToIndex = -1;
        int count=0;
        for (int i = 0; i < cList.size()-1; i++) {//exclude dummy category view
            if (cList.get(i).getID()==Long.parseLong(draggedChild.getTag().toString()) ) {
                FromIndex = i;
                count++;
            } else if (cList.get(i).getID()==Long.parseLong(child.getTag().toString())) {
                ToIndex = i;
                count++;
            }
            if(count>1)break;
        }
        CategoryObject coFrom = cList.get(FromIndex);
        //CategoryObject coTo = cList.get(ToIndex);
        cList.remove(coFrom);
        cList.add(ToIndex,coFrom);


        this.setAdapter(new MyCategoryItemViewBaseAdapter(getContext(),cList,adapter.GetSelectedCategoryTag()));

        //move the category view in top menu container as well
        MyTopMenuContainer tmc = (MyTopMenuContainer)((MainUIActivity) getContext()).findViewById(R.id.CategoryContainer);
        tmc.MoveCategory(draggedChild.getTag().toString(),child.getTag().toString());
    }
    private void SwapChildPosition(View child_1,View child_2)
    {
        //try {


            //save the new order
            MainUIActivity mua = (MainUIActivity) getContext();
            common.myMenu.MoveCategory(child_1.getTag().toString(),
                    child_2.getTag().toString());


            //move the GUI
            MyCategoryItemViewBaseAdapter adapter = (MyCategoryItemViewBaseAdapter)this.getAdapter();
            ArrayList<CategoryObject>cList =adapter.GetCategories();

            int FromIndex = -1;
            int ToIndex = -1;
            int count=0;
            for (int i = 0; i < cList.size()-1; i++) {//exclude dummy category view
                if (cList.get(i).getID()==Long.parseLong(child_1.getTag().toString()) ) {
                    FromIndex = i;
                    count++;
                } else if (cList.get(i).getID()==Long.parseLong(child_2.getTag().toString())) {
                    ToIndex = i;
                    count++;
                }
                if(count>1)break;
            }
            CategoryObject coFrom = cList.get(FromIndex);
            CategoryObject coTo = cList.get(ToIndex);
            cList.remove(coFrom);
            cList.remove(coTo);

            if (FromIndex < ToIndex) {
                cList.add(FromIndex,coTo);
                cList.add(ToIndex,coFrom);
                //cList.add(ToIndex - 1, coFrom);
            } else {
                cList.add(ToIndex,coFrom);
                cList.add(FromIndex,coTo);
                //cList.add(ToIndex, coFrom);
            }
            this.setAdapter(new MyCategoryItemViewBaseAdapter(getContext(),cList,adapter.GetSelectedCategoryTag()));

            //move the category view in top menu container as well
            MyTopMenuContainer tmc = (MyTopMenuContainer)((MainUIActivity) getContext()).findViewById(R.id.CategoryContainer);
            tmc.MoveCategory(child_1.getTag().toString(),child_2.getTag().toString());

        //}
        //catch (Exception ex)
        //{
            //ShowErrorMessageBox("MoveChildPosition",ex);
        //}
    }

    public void SetChildDragListener(View pressedChild)
    {

        SetChildViewDragListener(pressedChild);


        //try {


            ClipData.Item item = new ClipData.Item(pressedChild.getTag().toString());
            ClipData data = new ClipData(pressedChild.getTag().toString(), new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
            pressedChild.startDrag(data, new MyDragShadowBuilder(pressedChild), null, 0);

        //}
        //catch(Exception ex)
        //{
            //ShowErrorMessageBox("long clicked",ex);
        //}
    }

   /* public void ShowMessage(String strTitle,String strMsg,int iconId)
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
    }*/
//    protected void ShowErrorMessageBox(String strMethodName,Exception ex)
//    {
//        Log.d("EXCEPTION: " + strMethodName, "" + ex.getMessage());
//
//        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
//        messageBox.setTitle(strMethodName);
//        messageBox.setMessage(ex.getMessage());
//        messageBox.setCancelable(false);
//        messageBox.setNeutralButton("OK", null);
//        messageBox.show();
//    }
}
