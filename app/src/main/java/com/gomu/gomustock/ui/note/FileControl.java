package com.gomu.gomustock.ui.note;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileControl {

    String INFODIR = Environment.getExternalStorageDirectory().getPath() + "/gomustock/";
    public void copy(File sourceF, File targetF) {

        File[] ff = sourceF.listFiles();
        for (File file : ff) {
            File temp = new File(targetF.getAbsolutePath() + File.separator + file.getName());
            if (file.isDirectory()) {
                temp.mkdir();
                copy(file, temp);
            } else {
                FileInputStream fis = null;
                FileOutputStream fos = null;
                try {
                    fis = new FileInputStream(file);
                    fos = new FileOutputStream(temp);
                    byte[] b = new byte[4096];
                    int cnt = 0;
                    while ((cnt = fis.read(b)) != -1) {
                        fos.write(b, 0, cnt);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                        fos.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }
    }


    public String ReadTextFile(String filename) {

        String temp= null;
        File file = new File(INFODIR, filename);

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String result = "";
            String line;
            while ((line = reader.readLine()) != null) {
                result += line;
                result +=  System.getProperty ("line.separator");
            }
            reader.close();

            temp = result;

        }catch (Exception e){
            e.printStackTrace();
        }
        return temp;
    }

    public void WriteTextFile(String filename, String Contents){
        //FileOutputStream fos=null;

        File file = new File(INFODIR, filename);

        try{
            FileWriter writer = new FileWriter(file, false);
            PrintWriter out = new PrintWriter(writer);
            out.println(Contents);
            out.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
