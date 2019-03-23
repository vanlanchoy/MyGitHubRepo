package tme.pos;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

import tme.pos.BusinessLayer.*;
import tme.pos.BusinessLayer.Enum;


/**
 * Created by kchoy on 12/10/2014.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
{
    int NUM_PAGES =0;

    ArrayList<PageViewerFragment> pages;

    public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm,
                                   int num_pages,

                                   ArrayList<PageViewerFragment> pages
                                   )
    {
        super(fm);
        NUM_PAGES = num_pages;

        this.pages = pages;


    }
    @Override
    public Fragment getItem(int i) {


        return pages.get(i);
    }



    @Override
    public int getCount()
    {
        return NUM_PAGES;
    }

}
