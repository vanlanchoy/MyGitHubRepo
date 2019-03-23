package tme.pos.CustomViewCtr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vanlanchoy on 5/22/2016.
 * reference https://nishantvnair.wordpress.com/2010/09/28/flowlayout-in-android/
 */
public class FlowLayout extends ViewGroup {


    int currentLine;
    int horizontalSpacing =10;
    int verticalSpacing=10;
    public FlowLayout(Context context)
    {
        super(context);
    }
    public FlowLayout(Context context, AttributeSet attributeSet)
    {
        super(context,attributeSet);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int count = getChildCount();

        View v;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int measureWidth;

        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }


        for (int i = 0; i < count; i++) {
            v = getChildAt(i);
            if (v.getVisibility() != GONE) {

                v.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
                measureWidth = v.getMeasuredWidth();
                currentLine = Math.max(currentLine, v.getMeasuredHeight() + verticalSpacing);

                if (paddingLeft + measureWidth > width) {
                    paddingLeft = getPaddingLeft();
                    paddingTop += currentLine;
                }

                paddingLeft += measureWidth + horizontalSpacing;
            }
        }


        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = paddingTop + currentLine;

        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (paddingTop + currentLine < height) {
                height = paddingTop + currentLine;
            }
        }
        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //skip if no child present
        if(getChildCount()==0)return;

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        //int childCount = getChildCount();

        //ViewGroup.LayoutParams lp;
        View v;
        ArrayList<View>list =new ArrayList<View>();

        for(int i=0;i<getChildCount();i++)
        {
            list.add(getChildAt(i));

        }
        int width=r-1;
        int currentWidth=0;
        boolean blnRemoved=false;
        while(list.size()>0)
        {

            currentWidth=0;
            blnRemoved = true;
            while(blnRemoved) {
                blnRemoved = false;
                for (int i = list.size() - 1; i > -1; i--) {
                    if (list.get(i).getMeasuredWidth()+getPaddingLeft()+currentWidth < width) {

                        v = list.remove(i);

                        v.layout(paddingLeft+currentWidth, paddingTop, currentWidth + v.getMeasuredWidth(), paddingTop + v.getMeasuredHeight());
                        currentWidth += v.getMeasuredWidth()+getPaddingLeft();
                        blnRemoved = true;
                    }
                }
            }
            paddingTop+=currentLine;
        }
        /*for(int i=0;i<childCount;i++)
        {
            v = getChildAt(i);
            if(v.getVisibility()!=GONE)
            {
                lp = v.getLayoutParams();
                //if(paddingLeft+lp.width>r-1)
                if(paddingLeft+v.getMeasuredWidth()>r-1)
                {
                    paddingLeft = getPaddingLeft();
                    paddingTop += currentLine;
                }
                v.layout(paddingLeft,paddingTop,paddingLeft+v.getMeasuredWidth(),paddingTop+v.getMeasuredHeight());
                paddingLeft+=v.getMeasuredWidth()+horizontalSpacing;
            }

        }*/
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1); // default of 1px spacing
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return true;
        }
        return false;
    }
    public static class LayoutParams extends ViewGroup.LayoutParams {


        public LayoutParams(int width, int height) {
            super(0,0);

        }



    }
}
