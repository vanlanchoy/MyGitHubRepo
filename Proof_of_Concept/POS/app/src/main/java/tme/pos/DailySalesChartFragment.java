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

import tme.pos.BusinessLayer.DailyChartModel;
import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.Graph;

/**
 * Created by kchoy on 12/14/2015.
 */
public class DailySalesChartFragment extends Fragment {
    ArrayList<Pair<Integer,Float>> data;
    public DailySalesChartFragment()
    {

    }
    public void Draw(ArrayList<Pair<Integer,Float>> model)
    {
        if(model==null || model.size()==0)return;
        data = model;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View FragmentView = inflater.inflate(R.layout.layout_fragment_graph_ui, container, false);
        final Graph graph = (Graph)FragmentView;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                graph.Draw(data);
            }
        }, 500);

        TextView tv = new TextView(getActivity());
        ((RelativeLayout)FragmentView).addView(tv, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
        tv.setTypeface(common.text_and_length_settings.TYPE_FACE_ABEL_FONT);
        tv.setText("Sales graph");
        //((RelativeLayout)FragmentView).addView(tv);

        return FragmentView;
    }
}
