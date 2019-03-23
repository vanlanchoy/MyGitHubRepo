package tme.pos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;


import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


import tme.pos.BusinessLayer.Enum;
import tme.pos.BusinessLayer.common;


public abstract class PhotoFeatureFragment extends android.support.v4.app.Fragment {

      private OnFragmentInteractionListener mListener;


    protected static String TemporaryCameraPictureFileName="temp.jpg";

    protected  abstract void SavedPictureResult(String strSavedPicPath);

    public PhotoFeatureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
    public void DeleteExistingItemPic(String strPath)
    {
        if(strPath==null)return;
        if(strPath.isEmpty())return;
        File f = new File(strPath);
        if(f.exists())
        {f.delete();}



    }
    protected   void SelectPhoto()
    {
        final CharSequence[] items = { "Take Photo", "Choose From Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (resultCode == getActivity().RESULT_OK)
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





    protected String SaveCompressedPic(String strFilePath,Bitmap bitmap)
    {
        File destination = new File(getActivity().getFilesDir(),strFilePath);//Environment.getExternalStorageDirectory(),strFilePath);
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
    public Bitmap DecodeBitmapFile(String strPath)
    {
       /* // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Calculate inSampleSize
        options.inSampleSize =4;//2;// common.Utility.CalculateInSampleSize(options, 100, 100);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(strPath, options);*/
        return common.Utility.DecodeBitmapFile(strPath);
    }
    protected String getPath(Uri uri)
    {

        if( uri == null ) {
            return null;
        }

        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        return uri.getPath();
    }





}
