package tme.pos.CustomViewCtr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import tme.pos.PageViewerFragment;

/**
 * Created by vanlanchoy on 10/7/2014.
 */

public class MyScrollView extends ScrollView{
    public interface IScrollViewListener {
        void onScrollChanged(MyScrollView msv,int x, int y, int old_x, int old_y);
    }
    private float xDistance, yDistance, lastX, lastY;
    //PageViewerFragment pvf;
    IScrollViewListener listener;
    public MyScrollView(Context context) {
        super(context);

    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    /*public void SetProperties(PageViewerFragment pageViewer)
    {
        pvf = pageViewer;
    }*/
    public void SetProperties(IScrollViewListener svl)
    {
        listener = svl;
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                lastX = ev.getX();
                lastY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                xDistance += Math.abs(curX - lastX);
                yDistance += Math.abs(curY - lastY);
                lastX = curX;
                lastY = curY;
                if(xDistance > yDistance)
                    return false;
        }

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        //super.onScrollChanged(l, t, oldl, oldt);
        //if(pvf!=null)pvf.onScrollChanged(this,l,t,oldl,oldt);
        if(listener!=null)listener.onScrollChanged(this,l,t,oldl,oldt);
    }
}
