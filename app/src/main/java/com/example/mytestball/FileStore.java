package com.example.mytestball;

import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.R)
public class FileStore {
    public static final String RECORD_PATH = Environment.getExternalStorageDirectory().getPath() + File.separator+ "getBallSound" + File.separator;
//    public static final String REC_PATH = LOCAL_PATH  + File.separator;
//    public static final String REC_PATH = "/";
    static {
        File recFile = new File(RECORD_PATH);
        if (!recFile.exists()) recFile.mkdirs();
    }

    private FileStore() {
    }

    public static File createFile(String fileName) {
        File realFile = new File(RECORD_PATH + fileName);
        try {
            if (realFile.exists()) {
                realFile.delete();
            }
            realFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return realFile;
    }

}