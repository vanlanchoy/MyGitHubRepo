package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/9/2015.
 */
public class ServerMessagePopupWindow {
    Context context;
    public ServerMessagePopupWindow(Context c,String strMsg)
    {
        context =c;
        ShowPopup(strMsg);
    }
    private void ShowPopup(String strMsg)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater li = LayoutInflater.from(context);
        View dialogView = li.inflate(R.layout.layout_dialog_server_message_popup_widnow_ui, null);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(dialogView);
        final AlertDialog ad = alertDialogBuilder.create();

        TextView tvMessage = (TextView)dialogView.findViewById(R.id.tvMsg);
        tvMessage.setText(strMsg);
        tvMessage.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);

        TextView tvTitle = (TextView)dialogView.findViewById(R.id.tvTitle);
        tvTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);

        ImageButton imgCancel = (ImageButton)dialogView.findViewById(R.id.imgCancel);
        common.control_events.SetOnTouchImageButtonEffect(imgCancel,R.drawable.green_border_outer_glow_cancel,R.drawable.green_border_cancel);

        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });
        ad.show();
    }
}
