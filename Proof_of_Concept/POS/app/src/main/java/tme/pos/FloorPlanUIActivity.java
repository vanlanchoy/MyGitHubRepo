package tme.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.common;
import tme.pos.CustomViewCtr.FloorPlanCtr;

/**
 * Created by vanlanchoy on 4/19/2015.
 */
public class FloorPlanUIActivity extends Activity {

    LinearLayout mainPanel;
    Enum.FloorPlanMode floorPlanMode;
    TextView tvMode;
    ImageButton imgBtnArc;
    ImageButton imgBtnScribble;
    ImageButton imgBtnMove;
    ImageButton imgBtnEraser;
    ImageButton imgBtnLine;
    ImageButton imgBtnTable;
    ImageButton imgBtnRotate;
    ImageButton imgBtnCopy;
    ImageButton imgBtnPhoto;
    FloorPlanCtr floorCtr;
    ImageButton[] imgBtns;

    protected static String TemporaryCameraPictureFileName="temp.jpg";
    @Override
    protected void onStart() {


        super.onStart();


    }
    @Override
    protected  void onResume()
    {
        Log.d("FloorPlan activity Info", "on resume");
        super.onResume();
        ((POS_Application)getApplication()).setCurrentActivity(this);


    }

    @Override
    public void onBackPressed() {


        if(floorCtr==null){ super.onBackPressed();}
        else if(floorCtr.IsTableInEditingMode()){
            //won't hit here, there table will be removed after hitting back button, and will only hit this method again on second back
            common.Utility.ShowMessage("Save","You have unsaved table, please provide a table label and save before exit.",this,R.drawable.no_access);
            moveTaskToBack(true);
        }
        else{
            if(floorCtr.IsDirty()) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        floorCtr.Save();
                        finish();
                    }
                } );
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                AlertDialog alert = dialog.create();
                alert.setTitle("Floor Plan");
                alert.setMessage("Save before exit?");
                alert.setCancelable(false);
                alert.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.question), getResources(), 36, 36));
                alert.show();
            }
            else
            {
                super.onBackPressed();
            }
        }


        //
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == this.RESULT_OK)
        {
            String strUniquePicName = System.currentTimeMillis() + ".jpg";
            Bitmap bitmap=null;


            if (requestCode == tme.pos.BusinessLayer.Enum.ChoosePhotoFrom.gallery.value)
            {
                Uri selectedImageUri = data.getData();



                //compress file
                bitmap = DecodeBitmapFile(getPath(selectedImageUri));






            }
            else if(requestCode == Enum.ChoosePhotoFrom.camera.value)
            {

                File f = new File(Environment.getExternalStorageDirectory()
                        .toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals(TemporaryCameraPictureFileName)) {
                        f = temp;
                        break;
                    }
                }



                bitmap = DecodeBitmapFile(f.getAbsolutePath());



            }

            if(bitmap!=null)
            {
                String strSavedImageUri = SaveCompressedPic(strUniquePicName,bitmap);
                SavedPictureResult(strSavedImageUri);


            }
        }
    }
    @Override
    protected void onCreate(Bundle savedIntanceState)
    {
        ((POS_Application)getApplication()).setCurrentActivity(this);
        super.onCreate(savedIntanceState);
        setContentView(R.layout.layout_floor_plan_ui);
        final Context context = this;
        mainPanel = (LinearLayout) findViewById(R.id.llMain);

        floorCtr = (FloorPlanCtr)findViewById(R.id.llFloorPlan);
        floorCtr.LoadSaved();

        if(common.floorPlan.GetBackgroundPhotoFilePath().length()>0) {
            floorCtr.setBackground(new BitmapDrawable(getResources(),
                    DecodeBitmapFile(
                            common.floorPlan.GetBackgroundPhotoFilePath())));
        }
        ImageButton imgBtnClear = (ImageButton)findViewById(R.id.imgBtnClear);
        common.control_events.SetOnTouchImageButtonEffect(imgBtnClear,R.drawable.green_border_outer_glow_delete,R.drawable.green_border_delete);

        imgBtnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        floorCtr.Clear();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog alert = dialog.create();
                alert.setTitle("Floor Plan");
                alert.setMessage("Delete all objects?");
                alert.setCancelable(false);
                alert.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.question), getResources(), 36, 36));
                alert.show();

            }
        });

        ImageButton imgBtnSave = (ImageButton)findViewById(R.id.imgBtnSave);
        common.control_events.SetOnTouchImageButtonEffect(imgBtnSave, R.drawable.green_border_outer_glow_save, R.drawable.green_border_save);
        imgBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                floorCtr.Save();
            }
        });

        tvMode = (TextView)findViewById(R.id.tvMode);
        floorCtr.SetMessageControl(tvMode);

        imgBtns = new ImageButton[9];

        imgBtnArc = (ImageButton)findViewById(R.id.imgBtnArc);
        imgBtnArc.setTag(Enum.FloorPlanMode.arc);
        imgBtns[0] = imgBtnArc;

        imgBtnMove= (ImageButton)findViewById(R.id.imgBtnMove);
        imgBtnMove.setTag(Enum.FloorPlanMode.move);
        imgBtns[1] = imgBtnMove;

        imgBtnEraser = (ImageButton)findViewById(R.id.imgBtnEraser);
        imgBtnEraser.setTag(Enum.FloorPlanMode.eraser);
        imgBtns[2] = imgBtnEraser;

        imgBtnScribble = (ImageButton)findViewById(R.id.imgBtnScribble);
        imgBtnScribble.setTag(Enum.FloorPlanMode.scribble);
        imgBtns[3] = imgBtnScribble;



        imgBtnRotate = (ImageButton)findViewById(R.id.imgBtnRotate);
        imgBtnRotate.setTag(Enum.FloorPlanMode.rotate);
        imgBtns[4] = imgBtnRotate;

        imgBtnLine = (ImageButton)findViewById(R.id.imgBtnLine);
        imgBtnLine.setTag(Enum.FloorPlanMode.line);
        imgBtns[5] = imgBtnLine;

        imgBtnTable = (ImageButton)findViewById(R.id.imgBtnTable);
        imgBtnTable.setTag(Enum.FloorPlanMode.table);
        imgBtns[6] = imgBtnTable;

        imgBtnCopy = (ImageButton)findViewById(R.id.imgBtnCopy);
        imgBtnCopy.setTag(Enum.FloorPlanMode.copy);
        imgBtns[7] = imgBtnCopy;

        imgBtnPhoto = (ImageButton)findViewById(R.id.imgBtnCamera);
        imgBtnPhoto.setTag(Enum.FloorPlanMode.photo);
        imgBtns[8] = imgBtnPhoto;

        final FloorPlanUIActivity myClass = this;
        for(int i=0;i<imgBtns.length;i++)
        {

            imgBtns[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if(floorCtr.IsTableInEditingMode())
                    {
                        floorCtr.SaveTableLabel();
                        return;
                    }
                    //reset all images
                    ResetImgBtn();


                    if((Enum.FloorPlanMode)view.getTag()== Enum.FloorPlanMode.photo)
                    {
                        SelectPhoto();
                        ResetImgBtn();
                        return;
                    }

                    //if the new selected mode is same as current mode then reset to none
                    if(floorPlanMode==(Enum.FloorPlanMode)view.getTag())
                    {
                        floorPlanMode= Enum.FloorPlanMode.none;
                    }
                    else {
                        floorPlanMode = (Enum.FloorPlanMode) view.getTag();
                        ShowSelectedImage(floorPlanMode);
                    }

                    floorCtr.SetFloorPlanMode(floorPlanMode);
                    floorCtr.SetParentCtr(myClass);
                }
            });
        }

        //imgBtnScribble.callOnClick();
        floorPlanMode= Enum.FloorPlanMode.none;
        floorCtr.SetFloorPlanMode(floorPlanMode);
        floorCtr.SetParentCtr(this);
    }
    private void SetSelectedImageButton(ImageButton imgBtn,int drawableId)
    {
        imgBtn.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(drawableId), getResources(),60,60));
        imgBtn.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
    }
    public void SetFloorPlanMode(Enum.FloorPlanMode mode)
    {
        switch (mode)
        {
            case none:
                for(int i=0;i<imgBtns.length;i++)
                {
                    if((Enum.FloorPlanMode)imgBtns[i].getTag()==floorPlanMode)
                    {
                        imgBtns[i].callOnClick();
                        return;
                    }
                }
                break;
            default:

        }
    }


    protected  void SavedPictureResult(final String strSavedFileFullPath)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(strSavedFileFullPath, options);
        floorCtr.setBackground(new BitmapDrawable(getResources(), bitmap));
        floorCtr.setTag(strSavedFileFullPath);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //floorCtr.setBackground(null);
                DeleteExistingItemPic(common.floorPlan.GetBackgroundPhotoFilePath());
                common.floorPlan.SetBackgroundPhotoFilePath(strSavedFileFullPath);
                common.floorPlan.Save();//save 1st without updating other objects

            }
        });
        builder.setNegativeButton("Discard", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteExistingItemPic(strSavedFileFullPath);
                if(common.floorPlan.GetBackgroundPhotoFilePath().length()>0) {
                    floorCtr.setBackground(new BitmapDrawable(getResources(), common.floorPlan.GetBackgroundPhotoFilePath()));
                }else{floorCtr.setBackgroundColor(getResources().getColor(R.color.add_new_category_item_text_grey));}
            }
        });
        builder.setTitle("Save Photo?");
        builder.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.question), getResources(), 36, 36));
        builder.show();
    }
    protected void ShowSelectedImage(Enum.FloorPlanMode mode)
    {
        switch (mode)
        {
            case arc:
                SetSelectedImageButton(imgBtnArc,R.drawable.arc);
                //imgBtnArc.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.arc), getResources(),60,60));
                //imgBtnArc.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case scribble:
                SetSelectedImageButton(imgBtnScribble, R.drawable.scribble);
                //imgBtnScribble.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.scribble), getResources(),60,60));
                //imgBtnScribble.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case line:
                SetSelectedImageButton(imgBtnLine, R.drawable.line);
                //imgBtnLine.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.line), getResources(),60,60));
                //imgBtnLine.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case eraser:
                SetSelectedImageButton(imgBtnEraser, R.drawable.eraser);
                //imgBtnEraser.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.eraser), getResources(),60,60));
                //imgBtnEraser.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case move:
                SetSelectedImageButton(imgBtnMove, R.drawable.move);
                //imgBtnMove.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.move), getResources(),60,60));
                //imgBtnMove.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case rotate:
                SetSelectedImageButton(imgBtnRotate, R.drawable.rotate);
                //imgBtnRotate.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.rotate), getResources(),60,60));
                //imgBtnRotate.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case table:
                SetSelectedImageButton(imgBtnTable, R.drawable.table);
                //imgBtnTable.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.table), getResources(),60,60));
                //imgBtnTable.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case copy:
                SetSelectedImageButton(imgBtnCopy, R.drawable.copy);
                //imgBtnCopy.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.copy), getResources(),60,60));
                //imgBtnCopy.setBackground(getResources().getDrawable(R.drawable.draw_green_line_border));
                break;
            case photo:
                SetSelectedImageButton(imgBtnPhoto, R.drawable.camera);

            default:
        }
    }

    protected  void ResetImgBtn()
    {
        imgBtnArc.setBackground(null);
        imgBtnScribble.setBackground(null);
        imgBtnLine.setBackground(null);
        imgBtnEraser.setBackground(null);
        imgBtnMove.setBackground(null);
        imgBtnRotate.setBackground(null);
        imgBtnTable.setBackground(null);
        imgBtnCopy.setBackground(null);

        imgBtnArc.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.arc_green), getResources(),60,60));
        imgBtnScribble.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.scribble_green), getResources(),60,60));
        imgBtnLine.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.line_green), getResources(),60,60));
        imgBtnEraser.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.eraser_green), getResources(), 60, 60));
        imgBtnMove.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.move_green), getResources(), 60, 60));
        imgBtnRotate.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.rotate_green), getResources(), 60, 60));
        imgBtnTable.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.table_green), getResources(), 60, 60));
        imgBtnCopy.setImageDrawable(common.Utility.ResizeDrawable(getResources().getDrawable(R.drawable.copy_green), getResources(), 60, 60));

    }
    public void ShowMessage(String strTitle,String strMsg,int iconId)
    {
        AlertDialog.Builder messageBox = new AlertDialog.Builder(this);
        messageBox.setTitle(strTitle);
        messageBox.setMessage(Html.fromHtml(strMsg));
        messageBox.setCancelable(false);
        messageBox.setNeutralButton("OK", null);
        if(iconId>-1)
        {
            messageBox.setIcon(common.Utility.ResizeDrawable(getResources().getDrawable(iconId),getResources(),36,36));
        }
        messageBox.show();
    }
    public  void SelectPhoto()
    {
        ArrayList<String> items2 = new ArrayList<String>();

        items2.add("Take Photo");
        items2.add("Choose From Library");
        if( common.floorPlan.GetBackgroundPhotoFilePath().length()>0)items2.add("Remove Background Photo");
        items2.add("Cancel");


        CharSequence[] tempArray = new CharSequence[items2.size()];
        tempArray = items2.toArray(tempArray);
        final CharSequence[] items = tempArray;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment
                            .getExternalStorageDirectory(), TemporaryCameraPictureFileName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, Enum.ChoosePhotoFrom.camera.value);
                } else if (items[item].equals("Choose From Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"), Enum.ChoosePhotoFrom.gallery.value);
                } else if (items[item].equals("Remove Background Photo")) {
                    DeleteExistingItemPic(common.floorPlan.GetBackgroundPhotoFilePath());
                    common.floorPlan.SetBackgroundPhotoFilePath("");
                    //delete current displaying pic if any
                    if(floorCtr.getTag()!=null)
                    {
                        DeleteExistingItemPic((String)floorCtr.getTag());
                    }
                    floorCtr.setBackgroundColor(getResources().getColor(R.color.add_new_category_item_text_grey));

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }
    protected void DeleteExistingItemPic(String strPath)
    {
        if(strPath==null)return;
        if(strPath.isEmpty())return;
        File f = new File(strPath);
        if(f.exists())
        {f.delete();}



    }
    protected String SaveCompressedPic(String strFilePath,Bitmap bitmap)
    {
        File destination = new File(getFilesDir(),strFilePath);//Environment.getExternalStorageDirectory(),strFilePath);
        String strCompressedFilePath="";

        FileOutputStream fo;

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

            //save path to database
            return destination.getAbsolutePath();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strCompressedFilePath;
    }
    protected Bitmap DecodeBitmapFile(String strPath)
    {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        options.inSampleSize =2;// common.Utility.CalculateInSampleSize(options, 100, 100);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(strPath, options);
    }
    protected String getPath(Uri uri)
    {

        if( uri == null ) {
            return null;
        }

        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        return uri.getPath();
    }
}
