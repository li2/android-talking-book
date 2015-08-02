package me.li2.sdcard;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;

import android.os.Environment;

public class SdcardUtil {

    /** 
     * Get inner SDCard path 
     * @return 
     */  
    public String getInnerSDCardPath() {    
        return Environment.getExternalStorageDirectory().getPath();    
    }  
    
    /**
     * Get external SDCard path
     * @return
     */
    public static String getExtSDCardPath() {
        HashSet<String> extSdcardSet = getExternalMounts();
        if (extSdcardSet != null && extSdcardSet.size() > 0) {
            Object[] list = (Object[])extSdcardSet.toArray();
            return (String)list[0];
        }
        return null;
    }
    
    /**
     * Get external SDCard path
     * @return
     * Refer to http://stackoverflow.com/questions/11281010/how-can-i-get-external-sd-card-path-for-android-4-0
     */
    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
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
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }

}
