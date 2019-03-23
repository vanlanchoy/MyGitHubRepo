package tme.pos;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.PieChart;

/**
 * Created by kchoy on 12/14/2015.
 */
public class BestSellingItemPieChartFragment extends Fragment {
    PieChart pieChart;
    ArrayList<Pair<String,Integer>> data;
    public BestSellingItemPieChartFragment()
    {
        data=new ArrayList<Pair<String, Integer>>();
    }
    public void Draw(ArrayList<Pair<String,Integer>> BestSellingData)
    {
        data = BestSellingData;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View FragmentView = inflater.inflate(R.layout.layout_fragment_piechart_ui, container, false);
        pieChart = (PieChart)FragmentView;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pieChart.Draw(data);
            }
        }, 500);


        TextView tv = new TextView(getActivity());
        ((RelativeLayout)FragmentView).addView(tv, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        //tv.setGravity(Gravity.CENTER);
        //tv.setBackgroundColor(Color.BLUE);
        /*String strLabel="No data";
        if(data.size()>0)
        {*/
          String  strLabel="Top 5 best selling item by quantity";
        //}

        tv.setText(strLabel);


        return FragmentView;
    }
}
