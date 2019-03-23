package tme.pos;

import android.support.v4.view.ViewPager;
import android.widget.LinearLayout;

import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.PageIndicatorIndexCtr;

/**
 * Created by kchoy on 12/12/2014.
 */
public class SubMenuItemPageChangeListener extends ViewPager.SimpleOnPageChangeListener {

    int intSelectedPageIndex = -1;
    MainUIActivity mua;
    LinearLayout llPageIndicator;
    public SubMenuItemPageChangeListener(LinearLayout PageIndicator,MainUIActivity context) {
        super();
        mua = context;
        llPageIndicator = PageIndicator;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        super.onPageSelected(position);
        intSelectedPageIndex = position;
        SelectPageIndicator();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        super.onPageScrollStateChanged(state);
        if(state==ViewPager.SCROLL_STATE_IDLE )
        {

            //slow to update indicator

        }
        else if(state==ViewPager.SCROLL_STATE_SETTLING)
        {
            //not updating indicator

        }
        else if(state==ViewPager.SCROLL_STATE_DRAGGING)
        {

        }
    }
    protected void SelectPageIndicator()
    {
        for(int i = 0;i<llPageIndicator.getChildCount();i++)
        {
            if(llPageIndicator.getChildAt(i) instanceof PageIndicatorIndexCtr)
            {
                PageIndicatorIndexCtr child = (PageIndicatorIndexCtr)llPageIndicator.getChildAt(i);
                if(Integer.parseInt(child.getTag().toString())==intSelectedPageIndex)
                {
                    child.FillCircle();
                }
                else
                {
                    child.UnfillCircle();
                }
            }
        }
    }
}
