package tme.pos;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vanlanchoy on 10/31/2015.
 */
public class MyStringBaseAdapter extends ArrayAdapter<String> {
    List<String> lstReceipt;




    public MyStringBaseAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
lstReceipt = objects;
    }



  /*  @Override
    public int getCount() {
        return lstReceipt.size();
    }

    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }


    @Override
    public long getItemId(int position) {
        String[] values = lstReceipt.get(position).split("||");

        return Long.parseLong(values[0]);
    }
*/
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            TextView listItem = (TextView)inflater.inflate(R.layout.layout_device_name_ui, parent, false);
            final String[] values = lstReceipt.get(position).split("\\|");
        String strTableNum = (values.length>3)?values[3]:"";
        //String[] values = getItem(position).split("||");
            listItem.setText((position + 1)+". "+values[1]
                    + "\n"
                    + "Total: "+values[2]
                    +"      "+((strTableNum.length()>0)? "Table#: " +strTableNum:""));

            listItem.setTag(values[0]);
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lstReceipt.remove(position);
                ((ReceiptQueueActivity)getContext()).RemoveSelectedItem(position,values[0]);
                notifyDataSetChanged();
            }
        });
            return listItem;

    }
    /*public void Remove(int position)
    {
        lstReceipt.remove(position);
    }*/
}
