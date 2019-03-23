package tme.pos;


import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import printer.StarMicronics_TSP650II_BTI_Thermal_Printer;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/17/2015.
 */
public class CustomPreferenceHeaderListItemAdapter extends ArrayAdapter<PreferenceActivity.Header> {
    ArrayList<View> views= new ArrayList<View>();
    public CustomPreferenceHeaderListItemAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomPreferenceHeaderListItemAdapter(Context context, int resource, List<PreferenceActivity.Header> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.layout_preference_header_ui, null);

        }
        views.add(v);//add to list

        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
        tvTitle.setTypeface(tvTitle.getTypeface(), Typeface.NORMAL);
        PreferenceActivity.Header h = getItem(position);
        tvTitle.setText(h.title.toString());

        TextView tvSummary = (TextView) v.findViewById(R.id.tvSummary);
        tvSummary.setText(h.summary.toString());
        //tvSummary.setTextSize(TypedValue.COMPLEX_UNIT_DIP,25);
        //tvSummary.setTypeface(tvTitle.getTypeface(), Typeface.BOLD);
        TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);
        ImageView imgStatus = (ImageView)v.findViewById(R.id.imgStatus);
        /*if(position==1)//printer status
        {
            v.setTag("printer");
            tvStatus.setText("Offline");
            tvStatus.setTextColor(getContext().getResources().getColor(R.color.top_category_item_lost_focus_text_grey));
            //imgStatus.setBackground(getContext().getResources().getDrawable(R.drawable.draw_online_rect));
            imgStatus.setBackground(getContext().getResources().getDrawable(R.drawable.draw_offline_rect));

            //call to get printer status
            Handler hdr = new Handler();
            hdr.post(new Runnable() {
                @Override
                public void run() {
                    UpdatePrinterStatus();
                }
            });
        }
        else
        {*/
            v.setTag("");
            tvStatus.setVisibility(View.GONE);
            imgStatus.setVisibility(View.GONE);
            if(position==0)
            {
                //select the 1st by default
                //v.setBackgroundColor(getContext().getResources().getColor(R.color.selected_row_green));
            }

        //}

        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ResetBackgroupColor();
                view.setBackgroundColor(getContext().getResources().getColor(R.color.selected_row_green));
                return false;
            }
        });

        return v;

    }
    private void UpdatePrinterStatus()
    {
        StarMicronics_TSP650II_BTI_Thermal_Printer printer =common.Utility.PrinterGetInstance(getContext());

        for(View v:views)
        {
            if(v.getTag().toString().equalsIgnoreCase("printer"))
            {
                TextView tvStatus = (TextView) v.findViewById(R.id.tvStatus);
                ImageView imgStatus = (ImageView)v.findViewById(R.id.imgStatus);

                tvStatus.setText("Offline");
                imgStatus.setBackground(getContext().getResources().getDrawable(R.drawable.draw_offline_rect));

                if(printer==null) return;

                if(printer.GetStatus().equalsIgnoreCase("connected"))
                {
                    tvStatus.setText("Connected");
                    imgStatus.setBackground(getContext().getResources().getDrawable(R.drawable.draw_online_rect));
                }

            }
        }

    }
    protected void ResetBackgroupColor()
    {
        for(int i=0;i<views.size();i++)
        {
            views.get(i).setBackgroundColor(getContext().getResources().getColor(R.color.transparent));
        }
    }
}
