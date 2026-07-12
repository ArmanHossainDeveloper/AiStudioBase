package arman.common.io;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileUtil {

    ///File file;
String sdCard = "/sdcard";

    /*public FileUtil(String filePath){
        file = new File(filePath);
    }
    */

    public String getAbsoluteFilePath(Uri uri){
        if (uri == null || !"content".equals(uri.getScheme())) return "";

        String contentPath = uri.getPath();
        if (contentPath == null) return "";
        int checkPoint = 0;
        //String path = contentPath;
        while (checkPoint != -1){
            String path = contentPath.substring(checkPoint);
            Log.e("getAbsoluteFilePath", "Path: " + path);
            java.io.File file = new java.io.File(sdCard + path);
            if (file.exists()){
                Log.e("getAbsoluteFilePath", "Found path: " + path);
                return path;
            }

            checkPoint = contentPath.indexOf("/", ++checkPoint);
        }
        return "";
    }


    public void read(){

    }

    public void write(){

    }

    public void delete(){

    }

    public void rename(){

    }

    public void copy(){

    }

    public void move(){

    }

    public boolean saveFile(File file, byte[] content){
        if (file == null) return false;
        try (OutputStream output = new FileOutputStream(file)) {
            output.write(content);
            return true;
        } catch (IOException ignored) {}
        return false;
    }

}
