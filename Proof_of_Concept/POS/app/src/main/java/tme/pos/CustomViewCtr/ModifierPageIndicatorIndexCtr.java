package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;
import tme.pos.R;

/**
 * Created by kchoy on 2/10/2015.
 */
public class ModifierPageIndicatorIndexCtr extends PageIndicatorIndexCtr {
    //boolean blnFill = true;
    //int PageIndex=0;
    //ViewPager viewPager;

    public ModifierPageIndicatorIndexCtr(Context context,int pageIndex) {
        super(context, pageIndex, null);


    }
    public boolean IsFilled(){return blnFill;}
    @Override
    public void UnfillCircle()
    {
        blnFill = false;
        invalidate();
    }
    @Override
    public void FillCircle()
    {
        blnFill = true;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int color=0;
        if(PageIndex==Enum.MutualGroupColor.white.group)
        {
            color = Enum.MutualGroupColor.white.value;
        }
        else if(PageIndex==Enum.MutualGroupColor.mutual_dark_navy.group)
        {
            color = Enum.MutualGroupColor.mutual_dark_navy.value;
        }
        else if(PageIndex==Enum.MutualGroupColor.mutual_dark_indigo.group)
        {
            color = Enum.MutualGroupColor.mutual_dark_indigo.value;
        }
        else if(PageIndex==Enum.MutualGroupColor.mutual_dark_red.group)
        {
            color = Enum.MutualGroupColor.mutual_dark_red.value;
        }
        else if(PageIndex==Enum.MutualGroupColor.mutual_dark_brown.group)
        {
            color = Enum.MutualGroupColor.mutual_dark_brown.value;
        }
        else if(PageIndex==Enum.MutualGroupColor.mutual_dark_orange.group)
        {
            color = Enum.MutualGroupColor.mutual_dark_orange.value;
        }

        canvas.drawColor(Color.WHITE);

        Paint p = new Paint();
        // smooths
        p.setAntiAlias(true);
        //p.setColor(color);
        //if(color!=Enum.MutualGroupColor.white.value) {
            p.setColor(getResources().getColor(color));
        //}
        //else
        //{
            //p.setColor(Color.WHITE);
        //}
        //p.setColor(getResources().getColor(color));
        if(blnFill)
        {
            //special case for white color
            if(color==Enum.MutualGroupColor.white.value)
            {
                p.setColor(getResources().getColor(R.color.mutual_light_green));
                //p.setStyle(Paint.Style.STROKE);
            }

            p.setStyle(Paint.Style.FILL);
        }
        else
        {
            //special case for white color
            if(color==Enum.MutualGroupColor.white.value)
            {
                DashPathEffect dashPath = new DashPathEffect(new float[]{5,5}, (float)1.0);
                p.setPathEffect(dashPath);
                p.setColor(Color.BLACK);

            }

            p.setStyle(Paint.Style.STROKE);


        }
        p.setStrokeWidth(2.5f);
        // opacity
        //p.setAlpha(0x80); //
        canvas.drawCircle(common.Utility.DP2Pixel(20f, getContext()), common.Utility.DP2Pixel(20f, getContext()), common.Utility.DP2Pixel(10f, getContext()), p);


    }
}
