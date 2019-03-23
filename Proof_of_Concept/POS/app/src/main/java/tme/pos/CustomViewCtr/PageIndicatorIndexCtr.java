package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import tme.pos.BusinessLayer.common;
import tme.pos.MainUIActivity;
import tme.pos.PageViewerFragment;
import tme.pos.R;


/**
 * TODO: document your custom view class.
 */
public class PageIndicatorIndexCtr extends View {

    boolean blnFill = true;
    int PageIndex=0;
    ViewPager viewPager;

    public PageIndicatorIndexCtr(Context context,int pageIndex,ViewPager pager) {
        super(context);
        Configure(pageIndex,pager);
    }

    public PageIndicatorIndexCtr(Context context, AttributeSet attrs,int pageIndex,ViewPager pager) {
        super(context, attrs);
        Configure(pageIndex,pager);
    }

    public PageIndicatorIndexCtr(Context context, AttributeSet attrs, int defStyle,int pageIndex,ViewPager pager) {
        super(context, attrs, defStyle);
        Configure(pageIndex, pager);
    }

    protected void Configure(final int pageIndex,ViewPager pager)
    {
        PageIndex = pageIndex;
        viewPager = pager;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainUIActivity)getContext()).StartSpinnerBuiltInThread();
                viewPager.setCurrentItem(pageIndex, true);


            }
        });
    }
    public void UnfillCircle()
    {
        blnFill = false;
        invalidate();
    }
    public void FillCircle()
    {
        blnFill = true;
        invalidate();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.WHITE);
        Paint p = new Paint();
        // smooths
        p.setAntiAlias(true);
        //p.setColor(Color.LTGRAY);
        p.setColor(getResources().getColor(R.color.light_green));
        if(blnFill)
        {
            p.setStyle(Paint.Style.FILL);
        }
        else
        {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2.5f);
        }

        // opacity
        //p.setAlpha(0x80); //
        canvas.drawCircle(common.Utility.DP2Pixel(20f,getContext()), common.Utility.DP2Pixel(20f,getContext()), common.Utility.DP2Pixel(10f,getContext()), p);


    }


}
