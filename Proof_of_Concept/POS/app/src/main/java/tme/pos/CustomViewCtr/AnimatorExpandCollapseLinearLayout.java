package tme.pos.CustomViewCtr;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 6/11/2015.
 */
public class AnimatorExpandCollapseLinearLayout extends LinearLayout {
    boolean blnExpand = false;
    public AnimatorExpandCollapseLinearLayout(Context context) {
        super(context);
    }

    public AnimatorExpandCollapseLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatorExpandCollapseLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        if(blnExpand)
        {

        }
        else
        {

        }
    }
    public void Collapse()
    {
        //ShowMessage("collapse",blnExpand+", layout height "+getLayoutParams().height);
        blnExpand=false;
        this.setBackgroundColor(Color.WHITE);
        if(getLayoutParams().height<=-200)
        {
            //ShowMessage("collapse","done");
            getLayoutParams().height = 0;
            return;
        }
        else
        {
            //ShowMessage("collapse",getLayoutParams().height+"");
            getLayoutParams().height-=20;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Collapse();
                    invalidate();
                }
            },1000);

        }




    }
    public void Expand()
    {
        blnExpand=true;
        if(getLayoutParams().height>=getMeasuredHeight())
        {
            getLayoutParams().height = getMeasuredHeight();
            return;
        }
        else
        {
            getLayoutParams().height+=10f;

        }
    }
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(getContext());
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
        messageBox.show();
    }
}
