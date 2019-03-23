package tme.pos.BusinessLayer;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;

import tme.pos.R;

/**
 * Created by vanlanchoy on 4/12/2015.
 */
public class Control_Events {
    Context context;
    static String strDebugEnvironmentDir = "/sdcard/TMePOS";
    public Control_Events(Context c)
    {
        context =c;
    }
    public  void HideSoftKeyboard(View v)
    {
        InputMethodManager inputManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void CreateClickEffectForRoundedborder(final View v)
    {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v2, MotionEvent event) {

                int colorFrom = common.myAppSettings.context.getResources().getColor(R.color.transparent);
                int colorTo = common.myAppSettings.context.getResources().getColor(R.color.selected_row_green);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        v2.setBackgroundColor((Integer) animator.getAnimatedValue());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                v2.setBackground(common.myAppSettings.context.getResources().getDrawable(R.drawable.draw_two_round_rect));
                            }
                        }, 100);
                    }

                });
                colorAnimation.setDuration(100);
                colorAnimation.start();

                return false;
            }
        });
    }
    public static void CreateClickEffect(final View v)
    {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v2, MotionEvent event) {

                int colorFrom =common.myAppSettings.context.getResources().getColor(R.color.transparent);
                int colorTo = common.myAppSettings.context.getResources().getColor(R.color.selected_row_green);
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        v.setBackgroundColor((Integer)animator.getAnimatedValue());
                    }

                });
                colorAnimation.setDuration(100);
                colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
                colorAnimation.setRepeatCount(1);
                colorAnimation.start();
                return false;
            }
        });
    }
    public void SetOnTouchImageButtonEffect(final ImageButton btn,final int down,final int up)
    {
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    btn.setBackground(context.getResources().getDrawable(down));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    btn.setBackground(context.getResources().getDrawable(up));
                }
                return false;
            }
        });
    }
    public void SetOnTouchEffect(Button btn)
    {
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //ShowMessage("On Touch", motionEvent.getAction() + "");
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    ((TextView) view).setTextColor(context.getResources().getColor(R.color.white));
                    view.setBackgroundColor(context.getResources().getColor(R.color.green));
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    ((TextView) view).setTextColor(context.getResources().getColor(R.color.light_green));
                    view.setBackgroundColor(context.getResources().getColor(R.color.top_category_item_lost_focus_grey));
                }

                return false;
            }
        });
    }
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(context.getResources().getDrawable(iconId),context.getResources(),36,36));
        }
        messageBox.show();
    }
}
