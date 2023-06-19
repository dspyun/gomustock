package com.gomu.gomustock;

import android.os.Environment;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvValidationException;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MyCSV {

    private String MYDIR = Environment.getExternalStorageDirectory().getPath() + "/gomustock/";
    private String DOWNLOAD = Environment.getExternalStorageDirectory().getPath() + "/download/";

    public MyCSV() {

    }


    public void readCsvData(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path));
            String[] nextLine;
            while (true) {

                    if (!((nextLine = reader.readNext()) != null)) break;

                    for (int i = 0; i< nextLine.length; i++) {
                        System.out.print(nextLine[i] + " ");
                    }
                    System.out.println();

            }
        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public ArrayList<String> read30(String filename) {

        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        ArrayList<String> dataarry = new ArrayList<String>();

        /*
        Boolean value = file_check(MYDIR + filename+".csv");
        if(value != true) filename = filename +".csv";
        else filename = filename+".csv";
        */
        String PathFile = MYDIR+filename;;
        //readCsvData(PathFile);

        try {
            //CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            CSVReader csvReader = new CSVReader(new FileReader(filename));
            String[] header = csvReader.peek();
            //System.out.println(String.join(",", header));
            //System.out.println("-----------------");
            csvReader.skip(1);

            for(int i=0;i<20; i++)
            {
                String[] temp = csvReader.readNext();
                dataarry.add(String.valueOf(temp));
            }

        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        return dataarry;
    }


    public String fileconveter() {
        String myfilename = "000660.csv";
        String outFilename="";
        try {
            /*
            File newFile = new File(DOWNLOAD+"000600.csv");
            FileOutputStream output = new FileOutputStream(newFile);

            FileInputStream input = new FileInputStream(DOWNLOAD+"000660.KS.csv");
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(reader);
            // 한글 깨짐 현상 해결

            int ch;

            while ((ch = in.read()) != -1) {
                //하나씩 받아오고 출력시킴!!
                output.write((char) ch);
            }
            output.close();
            in.close();
            */

            outFilename = DOWNLOAD+"000660.KS.csv";
            String s;

            BufferedReader in = new BufferedReader(new FileReader(DOWNLOAD+"000660.KS.csv"));
            //기존파일에 엎어쓴다.
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilename),"UTF-8"));

            while ((s = in.readLine()) != null) {
                out.write(s);
                out.newLine();
            }

            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFilename;
    }
    public Boolean file_check(String filename) {

        String PathFile = filename;
        Boolean return_flag=false;

        try {
            java.io.File file1 = new java.io.File(PathFile);
            // 1. check if the file exists or not
            boolean isExists = file1.exists();

            if (isExists) {
                return_flag = true;
            } else {
                return_flag = false;
            }
        } catch (Exception e) {

        }
        return return_flag;
    }


    public class YahooData {
        @CsvBindByName
        private String date;
        @CsvBindByName
        private String open;
        @CsvBindByName
        private String high;
        @CsvBindByName
        private String low;
        @CsvBindByName
        private String close;
        @CsvBindByName
        private String adjclose;
        @CsvBindByName
        private String volume;

        public String getDate() {
            return date;
        }

        public String getClose() {
            return close;
        }
    }



    public ArrayList<Float> normalization(ArrayList<Float> input) {
        ArrayList<Float> temp = new ArrayList<Float>();
        ArrayList<Float> normalization = new ArrayList<Float>();
        Float max, min, low, value;
        temp = input;
        Collections.sort(temp); // 오름차순으로 정렬 후, 최대값, 최소값 저장
        max = temp.get(0);
        min = temp.get(temp.size()-1);
        low = max - min;
        for(int i=0;i<temp.size();i++) {
            value = (input.get(i) - min)/low;
            normalization.add(value);
        }
        return normalization;
    }

    public ArrayList<Float> standardization_cal(ArrayList<Float> input) {
        ArrayList<Float> temp = new ArrayList<Float>();
        ArrayList<Float> standardization = new ArrayList<Float>();
        double ave=0, stddev=0, sum=0, value=0;


        temp = input;
        for(int i=0;i<temp.size();i++) {
            sum += temp.get(i);
        }
        ave = sum/temp.size();

        for(int i=0;i<temp.size();i++) {
            sum += (temp.get(i)-ave)*(temp.get(i)-ave);
        }
        stddev = Math.sqrt(sum/temp.size());

        for(int i=0;i<temp.size();i++) {
            value = (input.get(i) - ave)/stddev;
            standardization.add((float)value);
        }
        return standardization;
    }

    public ArrayList<Float> standardization_lib(ArrayList<Float> input) {
        float average, value;
        double stddev;
        ArrayList<Float> standardization = new ArrayList<Float>();

        Mean m = new Mean(); // 이것이 math3 라이브러리에서 평균을 구해주는 객체이다.
        for (int i = 0; i < input.size(); i++) {
            m.increment(input.get(i));//자료를 넣고
        }
        average = (float) m.getResult();

        Variance v = new Variance();
        for (int i = 0; i < input.size(); i++) {
            v.increment(input.get(i));//자료를 넣고
        }
        stddev = Math.sqrt((float)v.getResult());

        for(int i=0;i<input.size();i++) {
            value = (input.get(i) - average)/(float)stddev;
            standardization.add((float)value);
        }
        return standardization;
    }
}
