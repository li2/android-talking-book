package me.li2.sdcard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.net.Uri;

public class FileOperateUtil {
	
	public static ArrayList<String> loadAssetsFileToStringList(Context context, String fileName){
        try {            
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            ArrayList<String>result = new ArrayList<String>();
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals("")) {
                    continue;
                }
                result.add(line);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	
	public static String loadAssetsFileToString(Context context, String fileName) {
        String result = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            result = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return result;
    }
	
    public static ArrayList<String> loadExtFileToStringList(Uri fileUri) {
        ArrayList<String> result = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            File file = new File(fileUri.getPath());
            reader = new BufferedReader(new FileReader(file));
            
            String line = null;           
            try {
                while ((line = reader.readLine()) != null) {
                    if(line.trim().equals("")) {
                        continue;
                    }
                    result.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            // Ignore this one, it happens when starting fresh.
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    
    public static String loadExtFileToString(Uri fileUri) {
        String result = null;
        try {
            File file = new File(fileUri.getPath());
            InputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            result = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return result;
    }	
}
