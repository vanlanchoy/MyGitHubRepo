package tme.pos.CustomViewCtr;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import tme.pos.BusinessLayer.common;
import tme.pos.custom_animation.CheckoutDiscountViewSlideDownAnimation;
import tme.pos.custom_animation.CheckoutDiscountViewSlideUpAnimation;

/**
 * Created by kchoy on 11/19/2015.
 */
public class CanSlideLinearLayout extends LinearLayout {

    int height=150;
    public CanSlideLinearLayout(Context c)
    {
        super(c);

    }
    public CanSlideLinearLayout(Context c,AttributeSet attSet)
    {
        super(c,attSet);

    }
    public void SlideUp()
    {
        CheckoutDiscountViewSlideUpAnimation slideUp = new CheckoutDiscountViewSlideUpAnimation(this, getContext(),150);
        slideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {


            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //need this dummy line in order to let the animation run

                LinearLayout.LayoutParams lllp =(LinearLayout.LayoutParams) getLayoutParams();
                lllp.height=0;
                lllp.bottomMargin= 0;
                setLayoutParams(lllp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideUp.setDuration(500);
        startAnimation(slideUp);
        invalidate();
    }
    public void SlideDown()
    {
        CheckoutDiscountViewSlideDownAnimation slideDown = new CheckoutDiscountViewSlideDownAnimation(this, getContext(),150);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {


            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //need this dummy line in order to let the animation run

                LinearLayout.LayoutParams lllp =(LinearLayout.LayoutParams) getLayoutParams();
                lllp.bottomMargin= common.Utility.DP2Pixel(10,getContext());
                setLayoutParams(lllp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        slideDown.setDuration(500);
        startAnimation(slideDown);
        invalidate();
    }
}
