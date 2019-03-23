package tme.pos.CustomViewCtr;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TableRow;

import tme.pos.AddNewMenuItemFragment;
import tme.pos.R;

/**
 * Created by kchoy on 2/4/2015.
 */
public class ModifierFlippableTableRow extends GenericVerticalFlipableTableRow {
    private boolean blnRemoveDialogLaunched=false;
    private AddNewMenuItemFragment parentFragment;
    public ModifierFlippableTableRow(Context context, AddNewMenuItemFragment fragment)
    {
        super(context);
        parentFragment = fragment;
    }
    public ModifierFlippableTableRow(Context context, AttributeSet attrs, AddNewMenuItemFragment fragment)
    {
       super(context,attrs);
        parentFragment = fragment;

    }
    @Override
    protected void SingleTapped()
    {

    }
    @Override
    protected void ShowConfirmation()
    {
        ShowDeleteConfirmation();
    }
    protected void ShowDeleteConfirmation()
    {
        if(blnRemoveDialogLaunched)return;
        blnRemoveDialogLaunched = true;
        final TableRow tr = this;
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 2. Chain together various setter methods to set the dialog characteristics

        builder.setMessage(Html.fromHtml(getResources().getString(R.string.to_remove_item_sentence) + " or <i><b>REMOVE ALL</b></i> ?"))
                .setTitle(R.string.label_alert_popup_title_for_delete_ordered_item);


        // 3. Get the AlertDialog from create()

        // Add the buttons
        builder.setPositiveButton("Selected", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button

                ((AddNewMenuItemFragment)parentFragment).RemoveModifierRow(tr);
                //DeleteSelectedItemRow(blnSwipeLeftToDelete);
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                setBackground(null);
                dialog.dismiss();

            }
        });
        builder.setNeutralButton("All",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                ((AddNewMenuItemFragment)parentFragment).RemoveAllModifierRows();

            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                blnRemoveDialogLaunched=false;
                setBackground(null);
            }
        });
        builder.show();
    }

}
