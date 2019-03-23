package tme.pos.custom_animation;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 11/19/2015.
 */
public class CheckoutDiscountViewSlideDownAnimation extends Animation {
    View currentView;
    int height= 0;
    Context context;
    public CheckoutDiscountViewSlideDownAnimation(View v,Context c,int h)
    {
        currentView = v;
        context = c;
        height = common.Utility.DP2Pixel(h,context);
    }
    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        LinearLayout.LayoutParams lllp = (LinearLayout.LayoutParams)currentView.getLayoutParams();
        lllp.height = (int)(height*interpolatedTime);
        currentView.setLayoutParams(lllp);
    }
}
