package com.gomu.gomustock;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyUtil {

    public MyUtil() {

    }
    String PATH = Environment.getExternalStorageDirectory().getPath();
    public void shell_copy() {

        Runtime runtime = Runtime.getRuntime();
        Process process;
        String res = "-0-";
        try {
            String cmd = "cp " + PATH + "/download/*.csv" + " " + PATH + "/gomustock/";
            process = runtime.exec(cmd);
            /*
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line ;
            while ((line = br.readLine()) != null) {
                Log.i("test",line);
            }
            */
        } catch (Exception e) {
            e.fillInStackTrace();
            Log.e("Process Manager", "Unable to execute top command");
        }
    }

}
