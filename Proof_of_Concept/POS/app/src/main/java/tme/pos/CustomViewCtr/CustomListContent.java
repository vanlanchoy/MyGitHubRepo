package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import tme.pos.BusinessLayer.Duple;
import tme.pos.BusinessLayer.ItemObject;
import tme.pos.BusinessLayer.common;
import tme.pos.Interfaces.ICustomListActivityListener;
import tme.pos.Interfaces.IOptionPopupItemListener;
import tme.pos.Interfaces.IPageActivityListener;

import tme.pos.R;

/**
 * Created by vanlanchoy on 6/18/2016.
 */
public class CustomListContent extends LinearLayout {
    //IPageActivityListener pageActivityListener;
    IOptionPopupItemListener popupItemListener;
    LinearLayout ll;
    int currentIndex=0;
    ICustomListActivityListener pageActivityListener;
    TextView tvTitle;
    public CustomListContent(Context c, IOptionPopupItemListener l2)
    {
        super(c);
        Instantiate(l2);
    }
    public CustomListContent(Context c, AttributeSet attributeSet, IOptionPopupItemListener l2)
    {
        super(c,attributeSet);
        Instantiate(l2);
    }
    public void SetListener(ICustomListActivityListener l)//,float last_x,float last_y)
    {
        pageActivityListener =l;


    }
    private void Instantiate(IOptionPopupItemListener l2)
    {
        setOrientation(VERTICAL);
        popupItemListener = l2;


        //page title and edit button
        tvTitle = new TextView(getContext());
        tvTitle.setText("List 1");
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,25);
        tvTitle.setGravity(Gravity.CENTER);
        //tvTitle.setBackgroundColor(Color.RED);
        addView(tvTitle,lllp);

        //scroll view for page content
        ScrollView sv = new ScrollView(getContext());
        lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, common.Utility.DP2Pixel(400,getContext()));
        //sv.setBackgroundColor(Color.BLUE);
        addView(sv,lllp);


        ll = new LinearLayout(getContext());
        ll.setOrientation(VERTICAL);
        //ll.setBackgroundColor(Color.BLUE);
        sv.addView(ll);
        GotoPage(1,false);
    }
    public void ClearPage(int pageIndex)
    {
        common.customListManager.Delete(pageIndex);
        GotoPage(pageIndex,false);
    }
    public void GotoPage(int index,boolean blnShowAnimation)
    {
        if(blnShowAnimation)
        {
            SlideOut((currentIndex<index)?false:true,ll,index);
        }
        else
        {
            currentIndex = index;
            ll.removeAllViews();
            Duple<String,ArrayList<Long>>record = common.customListManager.GetCustomList(index);
            if(record==null)
            {
                tvTitle.setText("List "+index);
            }
            else {
                tvTitle.setText(record.GetFirst());
                int origSize = record.GetSecond().size();
                //check for invalid/deleted item
                for(int i=record.GetSecond().size()-1;i>-1;i--)
                {
                    ItemObject io = common.myMenu.GetLatestItem(record.GetSecond().get(i));
                    if(io==null)
                    {
                        record.GetSecond().remove(i);
                    }
                }

                //help user to update if the size doesn't match
                if(origSize!=record.GetSecond().size())
                {
                    common.customListManager.Update(index,record.GetFirst(),record.GetSecond());
                }

                for(int i=0;i<record.GetSecond().size();i++)
                {
                    final ItemObject io = common.myMenu.GetLatestItem(record.GetSecond().get(i));

                    GenericFlipableTextView tv = new GenericFlipableTextView(getContext()) {
                        @Override
                        protected void SingleTapped() {
                            if(pageActivityListener!=null){
                                pageActivityListener.CustomList_AddNewUnitToCart(io.getID());
                            }
                        }

                        @Override
                        protected void ShowConfirmation() {
                            if(pageActivityListener!=null)
                            {
                                pageActivityListener.CustomList_ShowItemOptionPopup(io.getID());
                            }
                        }
                    };
                    tv.setText(io.getName());
                    tv.setPadding(3,3,3,3);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SP_MENU_ITEM_TEXT_SIZE);
                    ll.addView(tv);
                }
            }


        }
    }
    public void SlideOut(boolean blnSlideRight,View v,final int index)
    {

        TranslateAnimation movement = new TranslateAnimation(0,
                common.Utility.DP2Pixel(1000,getContext()), 0.0f, 0.0f);//move right
        if(!blnSlideRight)
        {
            //slide to left
            movement = new TranslateAnimation(0f
                    ,common.Utility.DP2Pixel(-1000,getContext()), 0.0f, 0.0f);//move left
        }





        movement.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                GotoPage(index,false);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        movement.setDuration(500);
        movement.setFillAfter(false);



        v.startAnimation(movement);

    }
}
