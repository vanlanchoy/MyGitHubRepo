package tme.pos.BusinessLayer;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by kchoy on 4/29/2015.
 */
public class FloorPlan implements Serializable {
    public interface FloorPlanEventListener {
        public void OnSave();
    }

    ArrayList<Duple<float[],float[]>> floorPlanObjects = new ArrayList<Duple<float[],float[]>>();
    ArrayList<float[]>floorPlanLines = new ArrayList<float[]>();
    ArrayList<float[]>floorPlanArces = new ArrayList<float[]>();
    ArrayList<Duple<float[],Duple<String,String>>>floorPlanTable = new ArrayList<Duple<float[],Duple<String,String>>>();
    //transient  String strFloorPlanBackgroundPhoto = "";
    String strFilename="FloorPlan.txt";
    transient Context context;//mark to be excluded in serialization
    String strErrorMsg="";
    transient FloorPlanEventListener listener;
    //private static final long serialVersionUID = 1L;
    public void SetBackgroundPhotoFilePath(String strFilePath)
    {
        common.myAppSettings.SetFloorPlanBackgroundPic(strFilePath);
        //strFloorPlanBackgroundPhoto = strFilePath;
    }
    public void SetLineObjects(ArrayList<float[]> newObj)
    {
        floorPlanLines = newObj;
    }
    public void SetArcObjects(ArrayList<float[]> newObj)
    {
        floorPlanArces = newObj;
    }
    public void SetScribbleObjects(ArrayList<Duple<float[],float[]>> newObj){
        floorPlanObjects = newObj;
    }
    public void SetTableObjects(ArrayList<Duple<float[],Duple<String,String>>> newObj){
        floorPlanTable = newObj;
    }
    public String GetBackgroundPhotoFilePath()
    {
        return common.myAppSettings.GetFloorPlanBackgroundPic();
        //return strFloorPlanBackgroundPhoto;
    }
    public ArrayList<Duple<float[],float[]>> GetScribbleObjects()
    {
        return floorPlanObjects;
    }
    public ArrayList<float[]> GetLineObjects()
    {
        return floorPlanLines;
    }
    public ArrayList<float[]> GetArcObjects()
    {
        return floorPlanArces;
    }
    public ArrayList<Duple<float[],Duple<String,String>>> GetTableObjects()
    {
        return floorPlanTable;
    }

    public FloorPlan(Context c,FloorPlanEventListener l){context=c; listener=l;}
    public Duple[] GetTableLabels()
    {
        ArrayList<Duple> temp = new ArrayList<Duple>();
        for(Duple<float[],Duple<String,String>> d:floorPlanTable)
        {
            if(d!=null)
            temp.add(d.GetSecond());
        }
        //Duple[] strArry=new Duple[temp.size()];
        return temp.toArray(new Duple[temp.size()]);
    }
    /*public boolean LoadFloorPlan()
    {
        try
        {
            FileInputStream fis = context.openFileInput(strFilename);
            ObjectInputStream is = new ObjectInputStream(fis);
            FloorPlan floorPlan = (FloorPlan) is.readObject();

            is.close();
            fis.close();
            //assign to current properties
            floorPlanObjects = floorPlan.floorPlanObjects;
            floorPlanLines = floorPlan.floorPlanLines;
            floorPlanArces = floorPlan.floorPlanArces;
            floorPlanTable = floorPlan.floorPlanTable;
            //strFloorPlanBackgroundPhoto = floorPlan.strFloorPlanBackgroundPhoto;
            return true;
        }
        catch(EOFException ex)
        {
            //ignore, end of file
            strErrorMsg="";
            return true;
        }
        catch(FileNotFoundException ex)
        {
            //ignore
            strErrorMsg="";
            return true;
        }
        catch(InvalidClassException ex)
        {

            strErrorMsg = ex.getMessage();
            if(strErrorMsg.contains("Incompatible class (SUID):"))
            {
                //delete the incompatible previously saved file
                DeleteSavedFile();
                strErrorMsg="";
            }
            return true;
        }
        catch(Exception ex)
        {
            if(ex.getCause()!=null)
                strErrorMsg = ex.getCause().getMessage();
            else
                strErrorMsg = ex.getMessage();

            return false;
        }
    }*/
    public boolean LoadFloorPlan()
    {
        try
        {

            FloorPlanManager fpm = new FloorPlanManager(context);
            String strObj = new FloorPlanManager(context).LoadFloorPlanData();
            //ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(strObj.getBytes()));
            //FloorPlan floorPlan = (FloorPlan) is.readObject();
            if(!(strObj==null || strObj.length()==0)) {
                FloorPlan floorPlan = (FloorPlan) fpm.StringToObject(strObj);
                //is.close();

                //assign to current properties
                floorPlanObjects = floorPlan.floorPlanObjects;
                floorPlanLines = floorPlan.floorPlanLines;
                floorPlanArces = floorPlan.floorPlanArces;
                floorPlanTable = floorPlan.floorPlanTable;
            }
            //strFloorPlanBackgroundPhoto = floorPlan.strFloorPlanBackgroundPhoto;
            return true;
        }

        catch(Exception ex)
        {
            if(ex.getCause()!=null)
                strErrorMsg = ex.getCause().getMessage();
            else
                strErrorMsg = ex.getMessage();

            return false;
        }
    }
    private void DeleteSavedFile()
    {
        File dir = context.getFilesDir();
        File file = new File(dir, strFilename);
        boolean deleted = file.delete();
    }
    public boolean Save() {
        try {
            strErrorMsg="";
            Enum.DBOperationResult result = new FloorPlanManager(context).Save(this);
            if(result!= Enum.DBOperationResult.Success) {
                strErrorMsg = "Failed to save floor plan data";
                common.Utility.LogActivity("Failed to save floor plan data");
                return false;
            }
            if(listener!=null)listener.OnSave();
            return true;
        }
        catch (Exception ex)
        {
            if(ex.getCause()!=null)
                strErrorMsg = ex.getCause().getMessage();
            else
                strErrorMsg = ex.getMessage();

            return false;
        }
    }
   /* public boolean Save()
    {

        try {
            FileOutputStream fos = context.openFileOutput(strFilename, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
            strErrorMsg="";
            if(listener!=null)listener.OnSave();
            return true;
        }
        catch (Exception ex)
        {
            if(ex.getCause()!=null)
                strErrorMsg = ex.getCause().getMessage();
            else
                strErrorMsg = ex.getMessage();

            return false;
        }
    }*/
    public String GetErrorMessage()
    {
        return strErrorMsg;
    }
}
