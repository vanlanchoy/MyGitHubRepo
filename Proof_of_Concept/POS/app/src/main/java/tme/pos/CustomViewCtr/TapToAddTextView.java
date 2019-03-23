package tme.pos.CustomViewCtr;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import tme.pos.BusinessLayer.common;
import tme.pos.R;

/**
 * Created by kchoy on 5/23/2016.
 */
public class TapToAddTextView extends TextView {
    public interface ITappedListener{
        void Tapped();
    }
    ITappedListener listener;
    public TapToAddTextView(Context context)
    {
        super(context);
        Instantiate();
    }
    public TapToAddTextView(Context context, AttributeSet attributeSet)
    {
        super(context,attributeSet);
        Instantiate();
    }
    public void SetListener(ITappedListener l)
    {
        listener = l;
    }
    private void Instantiate()
    {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, common.text_and_length_settings.SP_MENU_ITEM_TEXT_SIZE);
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        setGravity(Gravity.CENTER);
        setBackgroundColor(getResources().getColor(R.color.top_category_item_lost_focus_grey));
        setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
        setText("TAP TO ADD");


        setOnTouchListener(new View.OnTouchListener() {
            boolean blnTap = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = MotionEventCompat.getActionMasked(motionEvent);
                switch (action) {
                    case (MotionEvent.ACTION_DOWN):

                        blnTap = true;
                        setTextColor(getResources().getColor(R.color.light_green));


                        break;
                    case (MotionEvent.ACTION_MOVE):

                        break;
                    case (MotionEvent.ACTION_UP):

                        setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
                        if (blnTap) {

                            if(listener!=null)
                            {
                                listener.Tapped();
                            }

                        }
                        blnTap = false;
                        break;
                    case (MotionEvent.ACTION_CANCEL):

                        setTextColor(getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
                        blnTap = false;
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }
}
