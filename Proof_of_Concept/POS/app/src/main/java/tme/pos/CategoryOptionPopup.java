package tme.pos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.InputFilter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 3/3/2015.
 */
public class CategoryOptionPopup {
    Context context;
    View dialogView;
    OnCategoryDialogDismissListener dismissListener;
    float ADD_MENU_ITEM_TITLE_TEXT_SIZE = 30;
    float ADD_MENU_ITEM_MODIFIER_TEXT_SIZE=35;
    float ADD_MENU_ITEM_TEXT_SIZE =20;
    int CATEGORY_NAME_MAX_LENGTH=25;
    String CategoryName;
    long CategoryId;
    public interface OnCategoryDialogDismissListener {
        public void onCategoryDialogCanceled();
        public void onCategorySave(String strName,long Id);
        public void onCategoryDelete(long Id);

    }
    public CategoryOptionPopup(Context c,OnCategoryDialogDismissListener listener,String strCategoryName,long lngCategoryId)
    {
        context =c;
        this.CategoryName = strCategoryName;
        dismissListener = listener;
        this.CategoryId = lngCategoryId;
        LoadApplicationData();
    }
    private void LoadApplicationData()
    {
        ADD_MENU_ITEM_TITLE_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_title);
        ADD_MENU_ITEM_MODIFIER_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_modifier_text);
        ADD_MENU_ITEM_TEXT_SIZE = context.getResources().getDimension(R.dimen.dp_add_menu_item_text);
        CATEGORY_NAME_MAX_LENGTH = Integer.parseInt(context.getResources().getString(R.string.category_item_name_max_char));
    }
    private InputFilter[] CreateMaxLengthFilter(int length)
    {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(length);
        return filters;
    }
    public void ShowPopup(){
        ((MainUIActivity)context).SetPopupShow(true);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((MainUIActivity)context).getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(R.layout.layout_add_category_option_popup_window, null);

        //change label
        TextView tvTitle = (TextView)dialogView.findViewById(R.id.lblWindowTitle);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,common.text_and_length_settings.ADD_MENU_ITEM_TEXT_SIZE);
        tvTitle.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvTitle.setTextColor(context.getResources().getColor(R.color.divider_grey));

        /*TextView tvProperties = (TextView)dialogView.findViewById(R.id.lblProperties);
        tvProperties.setTextSize(TypedValue.COMPLEX_UNIT_DIP, ADD_MENU_ITEM_MODIFIER_TEXT_SIZE);
        tvProperties.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)), Typeface.BOLD);
        tvProperties.setGravity(Gravity.CENTER);*/

        //set para for edit text section
        EditText txtName = (EditText)dialogView.findViewById(R.id.txtCategoryName);
        txtName.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT_SIZE);
        txtName.setHint(context.getResources().getString(R.string.hint_create_category_item_in_popup_window, CATEGORY_NAME_MAX_LENGTH));

        txtName.setFilters(CreateMaxLengthFilter(CATEGORY_NAME_MAX_LENGTH));
        txtName.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)));

        TextView tvItem = (TextView)dialogView.findViewById(R.id.lblCategoryName);
        tvItem.setTextSize(TypedValue.COMPLEX_UNIT_DIP,ADD_MENU_ITEM_TEXT_SIZE);
        tvItem.setTypeface(Typeface.createFromAsset(context.getAssets(), context.getResources().getString(R.string.app_font_family)));



        builder.setView(dialogView);



        final AlertDialog ad = builder.create();

        ImageButton imgSave = (ImageButton)dialogView.findViewById(R.id.imgSave);
        common.control_events.SetOnTouchImageButtonEffect(imgSave,R.drawable.green_border_outer_glow_save,R.drawable.green_border_save);
                imgSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ValidateCategoryName(ad);
                    }
                });


        ImageButton imgDelete = (ImageButton)dialogView.findViewById(R.id.imgDelete);
        common.control_events.SetOnTouchImageButtonEffect(imgDelete,R.drawable.green_border_outer_glow_delete,R.drawable.green_border_delete);
        imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(context);
                b.setTitle("Confirm");
                b.setMessage("Delete current category?");
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                b.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainUIActivity) context).SetTopCategoryContainerBusyFlag(true);//so that top menu container will ignore any scrolling or tapping
                        if (dismissListener != null) dismissListener.onCategoryDelete(CategoryId);
                        dialogInterface.dismiss();

                    }
                });
                b.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        ad.dismiss();
                    }
                });
                b.show();
            }
        });
        ImageButton imgCancel = (ImageButton)dialogView.findViewById(R.id.imgCancel);
        common.control_events.SetOnTouchImageButtonEffect(imgCancel,R.drawable.green_border_outer_glow_cancel,R.drawable.green_border_cancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
            }
        });

        if(CategoryName.length()>0) {
            tvTitle.setText(context.getResources().getString(R.string.label_edit_category_name_title_popup_window));
            txtName.setText(CategoryName);
        }
        else
        {
            RelativeLayout.LayoutParams lpDelete = (RelativeLayout.LayoutParams)imgDelete.getLayoutParams();
            //move imgSave to imgDelete position
            RelativeLayout.LayoutParams lpSave = (RelativeLayout.LayoutParams)imgSave.getLayoutParams();
            lpSave.setMargins(lpDelete.leftMargin,lpDelete.topMargin,lpDelete.rightMargin,lpDelete.bottomMargin);

            //hide delete button
            imgDelete.setVisibility(View.GONE);

        }

        ad.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if(dismissListener!=null)dismissListener.onCategoryDialogCanceled();
                ((MainUIActivity)context).SetPopupShow(false);
            }
        });

        ad.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_Sliding;

        //final View localView = dialogView;
        android.os.Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.show();



            }
        },280);
    }
    private void ValidateCategoryName(AlertDialog ad)
    {
        String strNewName=((EditText)dialogView.findViewById(R.id.txtCategoryName)).getText().toString().trim();
        if(strNewName.length()==0)
        {
            ShowMessageBox("Category Name","Please provide a valid name for this category.",R.drawable.no_access);
            return;
        }
        else if(CategoryName.compareTo(strNewName)==0)
        {
            //ShowMessageBox("Category","nothing to update.");
            //nothing to update
            ad.dismiss();
            return;
        }
        if(dismissListener!=null)dismissListener.onCategorySave(strNewName,CategoryId);
        ad.dismiss();
    }
    private  void ShowMessageBox(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(context);
        messageBox.setTitle(strTitle);
        messageBox.setMessage(strMsg);
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(context.getResources().getDrawable(iconId),context.getResources(),36,36));
        }
        messageBox.show();
    }
}
