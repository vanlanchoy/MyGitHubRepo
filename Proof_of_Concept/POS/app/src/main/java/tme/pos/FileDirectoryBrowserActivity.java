package tme.pos;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.security.KeyRep;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

import tme.pos.BusinessLayer.AppSettings;
import tme.pos.BusinessLayer.common;

/**
 * Created by kchoy on 10/28/2015.
 */
public class FileDirectoryBrowserActivity extends Activity {
    LinearLayout llBreadCrumb;
    ListView lvContent;
    ListView lvLevel;
    ArrayAdapter<String> adpContent;
    ArrayAdapter<String> adpLevel;
    final static String strFolderPrefix="(Folder) ";
    final static  String ROOT ="root";
    final static  String INTERNAL_STORAGE_NAME ="Internal Storage";
    String strFilePath="";
    int intLogoWidth=250;
    int intLogoHeight=250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.layout_directory_browser_ui);

        setTitle("Choose a logo");
        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        //insert the top level
        lvLevel = (ListView) findViewById(R.id.lvLevel);
        adpLevel = new ArrayAdapter<String>(this,R.layout.layout_device_name_ui);
        //adpLevel.add(ROOT);
        lvLevel.setAdapter(adpLevel);
        lvLevel.setOnItemClickListener(levelClickListener);

       /* for(File f:getExternalCacheDirs())
        {
            if(f.isDirectory())
            {
                adpLevel.add("(cache) "+f.getAbsolutePath());
            }
        }
        for(File f:getExternalFilesDirs(null))

        {
            if(f.isDirectory())
            {
                adpLevel.add("(FileDir) "+f.getAbsolutePath());
            }
        }*/

        //insert root directory into the list
        adpContent = new ArrayAdapter<String>(this,R.layout.layout_device_name_ui);
        lvContent = (ListView) findViewById(R.id.lvContent);
        lvContent.setAdapter(adpContent);
        lvContent.setOnItemClickListener(itemClickListener);

        ShowFolderContent(ROOT);
    }

    private void ShowFolderContent(String strFolderPath)
    {
        adpContent.clear();
        if(strFolderPath.equalsIgnoreCase(ROOT))
        {
            ShowRootFolderContent();
           /* //internal storage
            adpContent.add(Environment.getExternalStorageDirectory().getName());
            //external storage

            String strName =System.getenv("SECONDARY_STORAGE");
            if(strName!=null) {
                int startIndex = strName.lastIndexOf("/");
                if(startIndex>-1) {
                    strName = strName.substring(startIndex + 1);
                }
                adpContent.add(strName);
            }*/





           /* final String state = Environment.getExternalStorageState();

            if ( Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state) ) {  // we can read the External Storage...
                //Retrieve the primary External Storage:
                final File primaryExternalStorage = Environment.getExternalStorageDirectory();

                //Retrieve the External Storages root directory:
                final String externalStorageRootDir;
                if ( (externalStorageRootDir = primaryExternalStorage.getParent()) == null ) {  // no parent...
                    adpLevel.add(primaryExternalStorage + "\n");
                }
                else {
                    final File externalStorageRoot = new File( externalStorageRootDir );
                    final File[] files = externalStorageRoot.listFiles();

                    for ( final File file : files ) {
                        if ( file.isDirectory() && file.canRead() && (file.listFiles().length > 0) ) {  // it is a real directory (not a USB drive)...
                           adpLevel.add(file.getAbsolutePath());
                        }
                    }
                }
            }*/
        }
        else
        {
            File f = new File(strFolderPath);
            //Toast.makeText(FileDirectoryBrowserActivity.this, "total item is "+f.listFiles().length, Toast.LENGTH_LONG).show();
            for(File childFile:f.listFiles())
            {
                String strPrefix = "";
                if(childFile.isDirectory()){
                    strPrefix=strFolderPrefix;
                }
                else
                {
                    if(!(childFile.getName().toLowerCase().indexOf(".jpg")>0
                            ||childFile.getName().toLowerCase().indexOf(".jpeg")>0
                    ||childFile.getName().toLowerCase().indexOf(".png")>0 ))continue;
                }
                String strName =childFile.getAbsolutePath();
                if(strName!=null) {
                    int startIndex = strName.lastIndexOf("/");
                    if(startIndex>-1) {
                        strName = strName.substring(startIndex + 1);
                    }
                    adpContent.add(strPrefix+strName);
                }
            }
        }
        adpLevel.add(strFolderPath);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void ShowRootFolderContent()
    {
        adpContent.clear();
        //primary storage
        adpContent.add(INTERNAL_STORAGE_NAME);

        //other removable storage
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                if(!IsFolderExist(part))
                                adpContent.add(part);

                    }
                }
            }
        }


    }
    private boolean IsFolderExist(String strFolderName)
    {
        for(int i=0;i<adpContent.getCount();i++)
        {
            if(adpContent.getItem(i).equalsIgnoreCase(strFolderName))return true;
        }
        return false;
    }
    private AdapterView.OnItemClickListener levelClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            for(int j =adpLevel.getCount()-1;j>i;j--)
            {
                adpLevel.remove(adpLevel.getItem(j));
            }
            String strSelectedFolder =adpLevel.getItem(adpLevel.getCount()-1);
            //remove it, the next method will add it back to the list
            adpLevel.remove(strSelectedFolder);
            ShowFolderContent(strSelectedFolder);
        }
    };
    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int i, long l) {
            // Get the device MAC address, which is the last 17 chars in the View
            String strClickedItem = ((TextView) v).getText().toString();
            String strCurrentFolder=adpLevel.getItem(adpLevel.getCount()-1);
            //special case if is from ROOT folder
            if(strCurrentFolder.equalsIgnoreCase("root"))
            {
                if(INTERNAL_STORAGE_NAME.equalsIgnoreCase(strClickedItem))
                {
                    ShowFolderContent(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
                else
                {
                    ShowFolderContent(strClickedItem);
                    //ShowFolderContent(System.getenv("SECONDARY_STORAGE"));
                }
            }
            else {
                //replace the folder prefix if any
                strClickedItem = strClickedItem.replace(strFolderPrefix, "");
                strClickedItem = adpLevel.getItem(adpLevel.getCount() - 1) +"/"+ strClickedItem;
                File f = new File(strClickedItem);
                if (f.isDirectory()) {
                    //further display the content inside this folder
                    //get current path
                    //Toast.makeText(FileDirectoryBrowserActivity.this, "this is a directory", Toast.LENGTH_LONG).show();

                    ShowFolderContent(f.getAbsolutePath());
                } else {
                    //is a jpeg/jpg/png file
                    //Toast.makeText(FileDirectoryBrowserActivity.this, f.getAbsolutePath()+" is a file", Toast.LENGTH_LONG).show();
                    strFilePath = f.getAbsolutePath();
                    //return the file path
                    if(strFilePath.length()>0)
                    {
                        //check image size keep it in defined height and weight
                        Bitmap b = BitmapFactory.decodeFile(strFilePath);
                        if(b.getHeight()>intLogoHeight || b.getWidth()>intLogoWidth)
                        {
                            Toast.makeText(FileDirectoryBrowserActivity.this, "Allowed file maximum dimension is "+intLogoWidth+"x"+intLogoHeight+", please choose a smaller logo.", Toast.LENGTH_LONG).show();
                            return;
                        }

                            Intent intent = new Intent();
                            intent.putExtra(AppSettings.EXTRA_LOGO_PATH, strFilePath.toString());

                            setResult(Activity.RESULT_OK, intent);

                    }
                    finish();
                }
            }
        }
    };
}
