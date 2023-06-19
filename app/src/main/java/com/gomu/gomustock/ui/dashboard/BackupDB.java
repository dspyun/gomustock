package com.gomu.gomustock.ui.dashboard;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class BackupDB {


    private String URL_APP_DB = "/data/com.gomu.myroom/databases/database";
    private String URL_APP_SHM = "/data/com.gomu.myroom/databases/database-shm";
    private String URL_APP_WAL = "/data/com.gomu.myroom/databases/database-wal";
    private String URL_APP_MYDB = "/data/com.gomu.myroom/databases/example";
    private String URL_LOCAL_DB = "/Download/database";
    private String URL_LOCAL_SHM = "/Download/database-shm";
    private String URL_LOCAL_WAL = "/Download/database-wal";
    private String URL_LOCAL_MYDB = "/Download/example";
    private static String APPPATH = Environment.getDataDirectory().getPath();
    private static String EXRPATH = Environment.getExternalStorageDirectory().getPath();

    // 백업 파일 생성
    public String createBackupFile(Context context2) {
        try {
            backupEachFile( URL_APP_SHM,  URL_LOCAL_SHM);
            backupEachFile( URL_APP_WAL,  URL_LOCAL_WAL);
            backupEachFile( URL_APP_DB,  URL_LOCAL_DB);
            //backupEachFile( URL_APP_MYDB,  URL_LOCAL_MYDB);
            return "ok";
        } catch (Exception e) {
            Toast.makeText(context2, "backup fail", Toast.LENGTH_LONG);
            //Log.e(TAG, "backup failed");
            e.printStackTrace();
            return "fail";
        }
    }

    public void mycopy(String srcpath, String dstpath) {

        String myapppath = APPPATH + srcpath;
        String myextpath = EXRPATH + dstpath;
        File srcfile = new File(myapppath);
        File dstfile = new File(myextpath);

        try {
            filecopy(srcfile, dstfile);
        } catch (Exception e) {
           // Toast.makeText(context,"new backup fail", Toast.LENGTH_LONG);
            //Log.e(TAG, "backup failed");
            e.printStackTrace();
        }
    }

    public static void filecopy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    private String backupEachFile(String from, String to) throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {
            File currentDB = new File(data, from);
            File backupDB = new File(sd, to);

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());

            src.close();
            dst.close();
            //Toast.makeText(getContext(), getContext().getString(R.string.backup_success), Toast.LENGTH_LONG).show();
            //Log.i(TAG, from + "backup success");
            //cancel();
            return "ok";

        }
        else {
            //Toast.makeText(getContext(), getContext().getString(R.string.backup_authorization_failed), Toast.LENGTH_LONG).show();
            return "fail";
        }
    }
    public void test(Context context) {
        String srcpath = EXRPATH + "/Download/test.txt";
        FileWriter test;

        try {
            test = new FileWriter(srcpath);
            test.write("test test");
            test.close();
            Toast.makeText(context,"test file write ok", Toast.LENGTH_LONG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
