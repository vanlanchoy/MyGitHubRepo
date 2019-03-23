package tme.pos.custom_animation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;

import tme.pos.R;

/**
 * Created by kchoy on 5/24/2016.
 */
public class SwipeRight extends TranslateAnimation {

    public SwipeRight(final ViewGroup v, final Context context)
    {
        super(0.0f, 5000.0f, 0.0f, 0.0f);
        setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {

                return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
            }
        });

        setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setBackgroundColor(context.getResources().getColor(R.color.selected_row_green));
                v.getChildAt(0).setBackgroundColor(context.getResources().getColor(R.color.transparent));


            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setBackground(null);

                v.setBackgroundColor(context.getResources().getColor(R.color.white_green));



            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        setDuration(500);
    }
}