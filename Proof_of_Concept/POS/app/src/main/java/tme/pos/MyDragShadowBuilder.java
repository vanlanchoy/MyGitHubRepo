package tme.pos;

import android.graphics.Point;
import android.util.Log;
import android.view.View;

/**
 * Created by vanlanchoy on 3/4/2015.
 */
public class MyDragShadowBuilder extends View.DragShadowBuilder {
    public MyDragShadowBuilder(View v)
    {
        super(v);
    }
    @Override
    public void onProvideShadowMetrics (Point size, Point touch) {
        super.onProvideShadowMetrics(size,touch);
        //Log.d("onProvideShadowMetrics","Y:"+touch.y);
    }
}
