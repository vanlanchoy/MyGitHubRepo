package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;

import android.util.AttributeSet;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;

import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by vanlanchoy on 5/30/2016.
 */
public class PromotionSummaryPopup  extends LinearLayout {
    int FIXED_HEIGHT=10;
    int FIXED_WIDTH=10;
    int ANIMATION_STARTING_HEIGHT=10;
    int ANIMATION_STARTING_WIDTH=10;
    int TARGET_HEIGHT=10;
    int TARGET_WIDTH=10;
    int INCREMENT_HEIGHT=10;
    int INCREMENT_WIDTH=10;
    //float touched_X=0f;
    //float touched_y=0f;
    boolean blnExpandAnimation=false;
    boolean blnInverseExpansion=true;
    //int use_this=0;
    boolean blnIsVisible=false;
    boolean blnMonthlyPromotionPopup=false;
    public PromotionSummaryPopup(Context context)
    {
        super(context);
        Instantiate();
    }
    public PromotionSummaryPopup(Context context, AttributeSet attrSet)
    {
        super(context,attrSet);
        Instantiate();
    }
    public void IsPromotionPopupInMonthlyView(boolean blnFlag)
    {
        blnMonthlyPromotionPopup = blnFlag;
    }
    /*public void SetTouchPosition(float x,float y)
    {
        use_this =common.Utility.DP2Pixel( ANIMATION_STARTING_HEIGHT*4,getContext());
        touched_X=x;
        touched_y=y;
        setX(x);
        if(blnInverseExpansion)
            setY(y+use_this);
        else
            setY(y);
    }*/
    private void Instantiate()
    {
        FIXED_HEIGHT = common.Utility.DP2Pixel(500,getContext());
        FIXED_WIDTH = common.Utility.DP2Pixel(400,getContext());

        INCREMENT_HEIGHT=50;
        INCREMENT_WIDTH=50;
    }

    public void SetSize(int height, int width,int increment_height,int increment_width,boolean blnInverseExpansion)
    {
        FIXED_HEIGHT = height;
        FIXED_WIDTH = width;
        INCREMENT_WIDTH = increment_width;
        INCREMENT_HEIGHT = increment_height;
        this.blnInverseExpansion = blnInverseExpansion;
    }
    public void AnimationShow()
    {
        blnExpandAnimation = true;
        if(blnInverseExpansion)
            setVisibility(VISIBLE);
        //save the target height and width
        TARGET_HEIGHT=FIXED_HEIGHT;
        TARGET_WIDTH= FIXED_WIDTH;

        //set current height and width to initial for animation run
        RelativeLayout.LayoutParams rllp=(RelativeLayout.LayoutParams)getLayoutParams();
        rllp.height = ANIMATION_STARTING_HEIGHT;
        rllp.width = ANIMATION_STARTING_WIDTH;
        invalidate();
    }
    public void AnimationHide()
    {
        blnExpandAnimation = false;

        //save the target height and width
        TARGET_HEIGHT=10;
        TARGET_WIDTH= 10;
        setY(getY()-INCREMENT_HEIGHT);
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas)
    {


        RelativeLayout.LayoutParams rllp=null;
        TableRow.LayoutParams trlp=null;
        if(getLayoutParams() instanceof  RelativeLayout.LayoutParams)
        {
            rllp=(RelativeLayout.LayoutParams)getLayoutParams();
        }
        else
        {
            return;

        }
        if(rllp.height==TARGET_HEIGHT && rllp.width==TARGET_WIDTH){
            if(blnInverseExpansion)
                setY(getY()-INCREMENT_HEIGHT+ANIMATION_STARTING_HEIGHT);
            return;
        }
        super.onDraw(canvas);

        if(blnExpandAnimation)
        {
            if(rllp.height<TARGET_HEIGHT || rllp.width<TARGET_WIDTH)
            {


                //adjust when is hasn't reached target only
                if(rllp.height<TARGET_HEIGHT)
                {
                    rllp.height+=INCREMENT_HEIGHT;
                    //when over height size
                    if(rllp.height>TARGET_HEIGHT)
                    {
                        if(!blnIsVisible)
                        ShowChildViews();

                        rllp.height = TARGET_HEIGHT;
                        if(blnInverseExpansion)
                            setY(getY()-INCREMENT_HEIGHT);

                    }
                    else
                    {
                        if(blnInverseExpansion)
                            setY(getY()-INCREMENT_HEIGHT);
                    }
                }



                //adjust when is hasn't reached target only
                if(rllp.width<TARGET_WIDTH)
                {
                    rllp.width+=INCREMENT_WIDTH;
                    if(rllp.width>TARGET_WIDTH)
                    {

                        rllp.width= TARGET_WIDTH;
                    }

                }

                setLayoutParams(rllp);

            }

        }
        else
        {
            if(rllp.height>TARGET_HEIGHT || rllp.width>TARGET_WIDTH)
            {
                rllp.height-=INCREMENT_HEIGHT;
                rllp.width-=INCREMENT_WIDTH;
                if(rllp.width<TARGET_WIDTH)rllp.width=TARGET_WIDTH;
                if(rllp.height<TARGET_HEIGHT)rllp.height=TARGET_HEIGHT;
                setY(getY()+INCREMENT_HEIGHT);
                setLayoutParams(rllp);

            }
            if(rllp.height==TARGET_HEIGHT && rllp.width==TARGET_WIDTH)setVisibility(INVISIBLE);

        }



    }
    private void ShowChildViews()
    {
        setBackgroundColor(Color.WHITE);

        for(int i=0;i<getChildCount();i++)
        {
            if(getChildAt(i).getVisibility()==INVISIBLE)
            getChildAt(i).setVisibility(VISIBLE);


        }

        //order does matter
        if(!blnMonthlyPromotionPopup)
        {

            setBackground(getContext().getResources().getDrawable(R.drawable.draw_green_line_border));
            setPadding(2,0,2,0);
        }
        else
        {

            setBackground(getContext().getResources().getDrawable(R.drawable.draw_black_line_border));
            setPadding(5,5,5,5);
        }

        blnIsVisible=true;
    }
}
