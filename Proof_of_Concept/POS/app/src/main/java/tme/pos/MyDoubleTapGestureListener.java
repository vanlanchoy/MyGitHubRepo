package tme.pos;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by kchoy on 5/19/2015.
 */
public class MyDoubleTapGestureListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener{
    public interface OnFlippableListener {
        public void ShowConfirmation();
        public void OnSingleTapped();
        public void OnDoubleTapped();
    }
    View currentObject;
    Context context;
    OnFlippableListener listener;
    public MyDoubleTapGestureListener(View v,Context c,OnFlippableListener listener )
    {
        currentObject = v;
        context = c;
        this.listener = listener;
    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent event)
    {
        if(event.getAction()==1)
        {

            ObjectAnimator anim = (ObjectAnimator) AnimatorInflater.loadAnimator(context,
                    R.anim.flip_vertical);
            anim.setTarget(currentObject);
            anim.setDuration(300);
            anim.start();
            currentObject.setBackgroundColor(context.getResources().getColor(R.color.selected_row_green));
            if(listener!=null)listener.ShowConfirmation();
            //ShowDeleteConfirmation();
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if(listener!=null)listener.OnSingleTapped();
        return true;
    }
    @Override
    public boolean onDoubleTap(MotionEvent event)
    {
        if(listener!=null)listener.OnDoubleTapped();
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float v, float v2) {
        return false;
    }
}
