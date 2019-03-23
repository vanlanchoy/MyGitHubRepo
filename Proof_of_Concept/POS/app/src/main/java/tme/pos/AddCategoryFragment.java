package tme.pos;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/3/2015.
 */
public class AddCategoryFragment extends DialogFragment{
    static AddCategoryFragment newInstance(int num) {
        AddCategoryFragment f = new AddCategoryFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.layout_add_category_option_popup_window, container, false);


        ImageButton imgCancel = (ImageButton)v.findViewById(R.id.imgCancelAddNew);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return v;
    }
}
