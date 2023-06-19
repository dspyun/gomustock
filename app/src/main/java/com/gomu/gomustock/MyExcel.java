package com.gomu.gomustock;


import static java.lang.Boolean.TRUE;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/*
jxl dependency추가
1. app > open module setting
2. dependency > + 클릭
3. jxl을 검색해서 net.sourceforge 버전을 추가한다
 */
/* 내장메모리 경로 얻는 방법
String root path =  Environment.getExternalStorageDirectory().getAbsolutePath();
또는 String root_path =  Environment.getExternalStorageDirectory().getPath();
is = getBaseContext().getResources().getAssets().open("test.xls");
is = FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/download/" + "test.xls");
*/

public class MyExcel extends MyStat{

    private String ExcelFile;
    private int colTotal, rowTotal;
    String oldfilename=null, oldline=null, oldcol=null;
    private String latest_filename, latest_line;
    private List<String> column00, column01, column02, column03;
    private String STOCKDIR = Environment.getExternalStorageDirectory().getPath() + "/gomustock/";;
    private String DOWNLOAD = Environment.getExternalStorageDirectory().getPath() + "/download/";
    private ArrayList<String> initInfo;

    public MyExcel(String filename) {
        ExcelFile=filename;
        init_maxline();
        column00 = readColumn(filename,0);
        column01 = readColumn(filename,1);
        column02 = readColumn(filename,2);
    }

    public MyExcel() {
        //initInfo = readInitFile();
    }

    @Override
    protected void finalize() {

    }

      /* sheet의 최대 라인수를 읽어서 전역변수에 저장한다. */

    public void init_maxline () {
        InputStream is1 = null;
        Workbook wb1 = null;
        int first_col=0;
        String PathFile = ExcelFile;

        try {
            is1 = new FileInputStream(PathFile);
            wb1 = Workbook.getWorkbook(is1);
            Sheet sheet = wb1.getSheet(0);   // 시트 불러오기

            colTotal = sheet.getColumns();    // 전체 컬럼
            rowTotal = sheet.getColumn(first_col).length; // 라인은 첫째 컬럼 최대치를 기준으론 한다.
            wb1.close();
            is1.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb1.close();
            //is1.close();
        }

    }

    public List<String> readColumn(String excelfile, int col) {

        InputStream is=null;
        Workbook wb=null;
        String contents=null;
        String PathFile = excelfile;
        List<String> mArrayBuffer = new ArrayList<String>();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    // 현재 컬럼의 내용을 추가한다.
                    for(int i=0; i < sheet.getColumn(col).length-1; i++) {
                        contents = sheet.getCell(col, i).getContents();
                        mArrayBuffer.add(contents);
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        return mArrayBuffer;
    }

    public int getMaxRow() {
        return rowTotal;
    }
    public int getMaxColumn() {
        return colTotal;
    }

    public String readCell( int col1, int line1) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = ExcelFile;

        line = line1; col=col1;
        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    contents1 = sheet.getCell(col, line).getContents();
                    //colTotal = sheet.getColumns();    // 전체 컬럼
                    //rowTotal = sheet.getColumn(0).length; // 라인은 첫째 컬럼 최대치를 기준으론 한다.
                    //return contents1;
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        return contents1;
    }



    public void writeprice( String filename, List<FormatOHLCV> history) {

        WritableSheet writablesheet;
        String PathFile = STOCKDIR+filename+".xls";;
        java.io.File file1 = new java.io.File(PathFile);
        try {
            // 오픈한 파일은 엑셀파일로 바꾸고
            WritableWorkbook workbook = Workbook.createWorkbook(file1);
            //Toast.makeText(getActivity(), " workbook open ok", Toast.LENGTH_SHORT).show();

            if(workbook != null) {
                //Toast.makeText(getContext(), " write ready ", Toast.LENGTH_SHORT).show();
                workbook.createSheet("sheet1", 0);
                writablesheet = workbook.getSheet(0);
                //Toast.makeText(getContext(), " sheet open ok", Toast.LENGTH_SHORT).show();

                if(writablesheet != null) {
                    for(int row =0;row<history.size();row++) {
                        writablesheet.addCell(new Label(0, row, history.get(row).date));
                        writablesheet.addCell(new Label(1, row, history.get(row).open));
                        writablesheet.addCell(new Label(2, row, history.get(row).high));
                        writablesheet.addCell(new Label(3, row, history.get(row).low));
                        writablesheet.addCell(new Label(4, row, history.get(row).close));
                        writablesheet.addCell(new Label(5, row, history.get(row).volume));
                    }
                }
            }
            workbook.write();
            workbook.close();
            //Toast.makeText(getContext(), "init excel write ok", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //Toast.makeText(getContext(), "io error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            //Toast.makeText(getContext(), "write error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {

        }
    }

    public void writefogninfo(String filename, List<String> fogn, List<String> agency) {

        WritableSheet writablesheet;
        String PathFile = STOCKDIR+filename+"fogn.xls";;
        java.io.File file1 = new java.io.File(PathFile);
        try {
            // 오픈한 파일은 엑셀파일로 바꾸고
            WritableWorkbook workbook = Workbook.createWorkbook(file1);
            //Toast.makeText(getActivity(), " workbook open ok", Toast.LENGTH_SHORT).show();

            if(workbook != null) {
                //Toast.makeText(getContext(), " write ready ", Toast.LENGTH_SHORT).show();
                workbook.createSheet("sheet2", 0);
                writablesheet = workbook.getSheet(0);
                //Toast.makeText(getContext(), " sheet open ok", Toast.LENGTH_SHORT).show();

                if(writablesheet != null) {
                    for(int row =0;row<fogn.size();row++) {
                        // 0번은 날짜 입력하는 컬럼으로 남겨둔다. 현재 미구현
                        writablesheet.addCell(new Label(1, row, fogn.get(row)));
                        writablesheet.addCell(new Label(2, row, agency.get(row)));
                    }
                }
            }
            workbook.write();
            workbook.close();
            //Toast.makeText(getContext(), "init excel write ok", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //Toast.makeText(getContext(), "io error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            //Toast.makeText(getContext(), "write error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {

        }
    }

    public int getTagColumn(String tag) {
        // default 값은  종가는읽게 6으로 리턴
        int column=0;
        if(tag.equals("DATE")) column = 0;
        else if(tag.equals("OPEN")) column = 1;
        else if(tag.equals("HIGH")) column = 2;
        else if(tag.equals("LOW")) column = 3;
        else if(tag.equals("CLOSE")) column = 4;
        else if(tag.equals("VOLUME")) column = 5;
        else column = 4;

        return column;
    }

    public List<String> oa_readItem(String filename, String tag, boolean header) {

        int column = getTagColumn(tag);
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+filename;;
        List<String> pricebuffer = new ArrayList<String>();


        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 1;
                    for(int i=start;i<sheet.getColumn(column).length;i++) {
                        // formatOA class의 구조로 저장된다
                        // 종가는 6번째 컬럼의 값
                        pricebuffer.add(sheet.getCell(column, i).getContents());
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }

        return pricebuffer;
    }

    public List<String> oa_readPrice60(String filename, boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+filename;;
        List<String> mArrayBuffer = new ArrayList<String>();
        List<String> mArrayBuffer_rev = new ArrayList<String>();
        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 1;
                    for(int i=start;i<sheet.getColumn(6).length;i++) {
                        // formatOA class의 구조로 저장된다
                        // 종가는 6번째 컬럼의 값
                        mArrayBuffer.add(sheet.getCell(6, i).getContents());
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        /*
        for(int i =mArrayBuffer.size()-1;i>=0;i--) {
            mArrayBuffer_rev.add(mArrayBuffer.get(i));
        }
        return mArrayBuffer_rev;

         */
        return mArrayBuffer;
    }

    public List<String> oa_readIndex60(String filename, boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+filename;;
        List<String> mArrayBuffer = new ArrayList<String>();
        List<String> mArrayBuffer_rev = new ArrayList<String>();
        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 1;
                    for(int i=start;i<sheet.getColumn(6).length;i++) {
                        // formatOA class의 구조로 저장된다
                        // 종가는 6번째 컬럼의 값
                        mArrayBuffer.add(sheet.getCell(6, i).getContents());
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        /*
        for(int i =mArrayBuffer.size()-1;i>=0;i--) {
            mArrayBuffer_rev.add(mArrayBuffer.get(i));
        }
        return mArrayBuffer_rev;
         */
        return mArrayBuffer;
    }

    private List<String> getDir(String dirPath) {

        List<String> dirname = new ArrayList<String>();
        List<String>  filename = new ArrayList<String>();
        List<String>   path = new ArrayList<String>();

        java.io.File dir = new java.io.File(dirPath);
        java.io.File[] fileslist = dir.listFiles();

        if (dirPath != STOCKDIR) {
            filename.add(dirPath);
            path.add(dirPath);
            filename.add("../");
            path.add(dir.getParent());
        }

        for (int i=0; i<fileslist.length-1;i++) {
            java.io.File file = fileslist[i];
            path.add(file.getParentFile().getName());

            if (file.isDirectory()) {
                dirname.add(file.getName() + "/");
            } else {
                filename.add(file.getName());
            }
        }

        // dirname 뒤에 파일이름을 넣어준다
        for(int i=0;i<filename.size()-1;i++) {
            dirname.add(filename.get(i));
        }
        // path도 리턴 시키는 방법을 찾아볼 것
        //Toast.makeText(getContext(), Integer.toString(j), Toast.LENGTH_SHORT).show()
        return dirname;
    }

    public List<Float> yf_readkospi30() {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+"^KS11.cvs";;
        List<Float> kospiarray = new ArrayList<Float>();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    for(int i=1;i<30;i++) {
                        contents1= sheet.getCell(4, i).getContents();
                        kospiarray.add(Float.valueOf(contents1));
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        return kospiarray;
    }

    public List<Float> yf_readsam30() {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+"005930"+".KS.csv";
        List<Float> kospiarray = new ArrayList<Float>();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    for(int i=1;i<30;i++) {
                        contents1= sheet.getCell(4, i).getContents();
                        kospiarray.add(Float.valueOf(contents1));
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        return kospiarray;
    }



    public String find_stockno(String name) {
        InputStream is=null;
        Workbook wb=null;
        String stock_name=null;
        String stock_no=null;
        int line, col;
        String PathFile = STOCKDIR+"stocktable.xls";;
        List<String> STOCK_NO = new ArrayList<String>();
        List<String> STOCK_NAME = new ArrayList<String>();
        List<String> ETF_NO = new ArrayList<String>();
        List<String> ETF_NAME = new ArrayList<String>();
        if(name.equals("코스피")) return "^KS11";
        else if(name.equals("환율")) return "KRW=X";

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    for(int i =0 ; i<sheet.getColumn(0).length ;i++) {
                        STOCK_NO.add(sheet.getCell(1, i).getContents());
                        STOCK_NAME.add(sheet.getCell(3, i).getContents());
                    }
                    for(int i =0 ; i<sheet.getColumn(1).length ;i++) {
                        ETF_NO.add(sheet.getCell(1, i).getContents());
                        ETF_NAME.add(sheet.getCell(3, i).getContents());
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        int index = STOCK_NAME.indexOf(name);
        if(index == -1 ) {
            index = ETF_NAME.indexOf(name);
        }
        if(index == -1) return "";
        return STOCK_NO.get(index);
    }

    public String find_stockname(String number) {
        InputStream is=null;
        Workbook wb=null;
        String stock_name=null;
        String stock_no=null;
        int line, col;
        String PathFile = STOCKDIR+"stocktable.xls";;
        List<String> STOCK_NO = new ArrayList<String>();
        List<String> STOCK_NAME = new ArrayList<String>();
        List<String> ETF_NO = new ArrayList<String>();
        List<String> ETF_NAME = new ArrayList<String>();
        if(number.equals("^KS11")) return "코스피";
        else if(number.equals("KRW=X")) return "환율";

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    for(int i =0 ; i<sheet.getColumn(0).length ;i++) {
                        STOCK_NO.add(sheet.getCell(1, i).getContents());
                        STOCK_NAME.add(sheet.getCell(3, i).getContents());
                    }
                    for(int i =0 ; i<sheet.getColumn(1).length ;i++) {
                        ETF_NO.add(sheet.getCell(1, i).getContents());
                        ETF_NAME.add(sheet.getCell(3, i).getContents());
                    }

                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }
        int index = STOCK_NO.indexOf(number);
        if(index == -1 ) {
            index = ETF_NO.indexOf(number);
        }
        if(index == -1) return "";
        return STOCK_NAME.get(index);
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

    public List<String> ReadCsv(String filename) {
            //반환용 리스트
        String PathFile = STOCKDIR+filename;;
        java.io.File file = new java.io.File(PathFile);
        List<String> kospiarray = new ArrayList<String>();

        try {
            int ch;
            //BufferedReader reader = new BufferedReader(new FileReader(PathFile));
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr);
            //BufferedReader reader = Files.newBufferedReader(Paths.get(PathFile));
            String line;
            for(int i=0;(line = reader.readLine()) != null;i++) {
                kospiarray.add(line);
            }
            line += "";
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
        return kospiarray;
    }

    public List<FormatTestData> readtestset(String filename, boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+filename;;
        List<FormatTestData> testdatalist = new ArrayList<FormatTestData>();
        FormatTestData testdata = new FormatTestData();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 0;
                    for(int i=start;i<sheet.getColumn(0).length;i++) {
                        // formatOA class의 구조로 저장된다
                        // 종가는 6번째 컬럼의 값
                        testdata = new FormatTestData();
                        testdata.date = sheet.getCell(0, i).getContents();
                        testdata.price = sheet.getCell(1, i).getContents();
                        testdata.buy_quantity = sheet.getCell(2, i).getContents();
                        testdata.sell_quantity = sheet.getCell(3, i).getContents();
                        testdatalist.add(testdata);
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }

        return testdatalist;
    }
    public List<FormatTestData> readtestbuy(String filename, boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+filename;;
        List<FormatTestData> testdatalist = new ArrayList<FormatTestData>();
        FormatTestData testdata = new FormatTestData();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 0;
                    for(int i=start;i<sheet.getColumn(0).length;i++) {
                        // formatOA class의 구조로 저장된다
                        // 종가는 6번째 컬럼의 값
                        testdata = new FormatTestData();
                        testdata.date = sheet.getCell(0, i).getContents();
                        testdata.price = sheet.getCell(1, i).getContents();
                        testdata.buy_quantity = sheet.getCell(2, i).getContents();
                        testdatalist.add(testdata);
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }

        return testdatalist;
    }


    public List<FormatTestData> readtestsell(String filename, boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+filename;;
        List<FormatTestData> testdatalist = new ArrayList<FormatTestData>();
        FormatTestData testdata = new FormatTestData();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 0;
                    for(int i=start;i<sheet.getColumn(0).length;i++) {
                        // sell quantity를 읽는ㄴ다
                        testdata = new FormatTestData();
                        testdata.date = sheet.getCell(0, i).getContents();
                        testdata.price = sheet.getCell(1, i).getContents();
                        testdata.sell_quantity = sheet.getCell(3, i).getContents();
                        testdatalist.add(testdata);
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } finally {
            //wb.close();
            //is.close();
        }

        return testdatalist;
    }

    public void write_testdata(String code, List<String> date ,List<String> price,List<Integer> buy, List<Integer> sell) {

        String filename = code+"_testset";
        WritableSheet writablesheet;
        String PathFile = STOCKDIR+filename+".xls";;
        java.io.File file1 = new java.io.File(PathFile);
        try {
            // 오픈한 파일은 엑셀파일로 바꾸고
            WritableWorkbook workbook = Workbook.createWorkbook(file1);
            //Toast.makeText(getActivity(), " workbook open ok", Toast.LENGTH_SHORT).show();

            if(workbook != null) {
                //Toast.makeText(getContext(), " write ready ", Toast.LENGTH_SHORT).show();
                workbook.createSheet("sheet1", 0);
                writablesheet = workbook.getSheet(0);
                //Toast.makeText(getContext(), " sheet open ok", Toast.LENGTH_SHORT).show();

                if(writablesheet != null) {
                    // header 때문에 1부터 시작해야 한다
                    for(int row =0;row < buy.size();row++) {
                        writablesheet.addCell(new Label(0, row, date.get(row)));
                        writablesheet.addCell(new Label(1, row, price.get(row)));
                        writablesheet.addCell(new Label(2, row, String.valueOf(buy.get(row))));
                        writablesheet.addCell(new Label(3, row, String.valueOf(sell.get(row))));
                    }
                }
            }
            workbook.write();
            workbook.close();
            //Toast.makeText(getContext(), "init excel write ok", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //Toast.makeText(getContext(), "io error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            //Toast.makeText(getContext(), "write error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {

        }
    }


    public List<FormatTestData> removeHeader(List<FormatTestData> input) {
        List<FormatTestData> temp = new ArrayList<FormatTestData>();

        for(int i = 0;i<input.size()-1; i++ ) {
            temp.add(input.get(i+1));
        }
        return temp;
    }

    public void writestockinfo(List<FormatStockInfo> information) {

        WritableSheet writablesheet;
        String PathFile = STOCKDIR+"stockinfo"+".xls";;
        java.io.File file1 = new java.io.File(PathFile);
        try {
            // 오픈한 파일은 엑셀파일로 바꾸고
            WritableWorkbook workbook = Workbook.createWorkbook(file1);
            //Toast.makeText(getActivity(), " workbook open ok", Toast.LENGTH_SHORT).show();

            if(workbook != null) {
                //Toast.makeText(getContext(), " write ready ", Toast.LENGTH_SHORT).show();
                workbook.createSheet("sheet1", 0);
                writablesheet = workbook.getSheet(0);
                //Toast.makeText(getContext(), " sheet open ok", Toast.LENGTH_SHORT).show();

                if(writablesheet != null) {
                    for(int row =0;row<information.size();row++) {
                        writablesheet.addCell(new Label(0, row, information.get(row).stock_code));
                        writablesheet.addCell(new Label(1, row, information.get(row).stock_name));
                        writablesheet.addCell(new Label(2, row, information.get(row).per));
                        writablesheet.addCell(new Label(3, row, information.get(row).per12));
                        writablesheet.addCell(new Label(4, row, information.get(row).area_per));
                        writablesheet.addCell(new Label(5, row, information.get(row).pbr));
                        writablesheet.addCell(new Label(6, row, information.get(row).div_rate));
                        writablesheet.addCell(new Label(7, row, information.get(row).fogn_rate));
                        writablesheet.addCell(new Label(8, row, information.get(row).beta));
                        writablesheet.addCell(new Label(9, row, information.get(row).op_profit));
                        writablesheet.addCell(new Label(10, row, information.get(row).cur_price));
                        writablesheet.addCell(new Label(11, row, information.get(row).score));
                        writablesheet.addCell(new Label(12, row, information.get(row).desc));
                    }
                }
            }
            workbook.write();
            workbook.close();
            //Toast.makeText(getContext(), "init excel write ok", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //Toast.makeText(getContext(), "io error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            //Toast.makeText(getContext(), "write error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {

        }
    }


    public List<FormatStockInfo> readStockinfo(boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+"stockinfo"+".xls";;
        List<FormatStockInfo> mArrayBuffer = new ArrayList<FormatStockInfo>();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 1;
                    for(int i=start;i<sheet.getColumn(0).length;i++) {
                        FormatStockInfo temp = new FormatStockInfo();
                        temp.stock_code = sheet.getCell(0, i).getContents();
                        temp.stock_name = sheet.getCell(1, i).getContents();
                        temp.per = sheet.getCell(2, i).getContents();
                        temp.per12 = sheet.getCell(3, i).getContents();
                        temp.area_per = sheet.getCell(4, i).getContents();
                        temp.pbr = sheet.getCell(5, i).getContents();
                        temp.div_rate = sheet.getCell(6, i).getContents();
                        temp.fogn_rate = sheet.getCell(7, i).getContents();
                        temp.beta = sheet.getCell(8, i).getContents();
                        temp.op_profit = sheet.getCell(9, i).getContents();
                        temp.cur_price = sheet.getCell(10, i).getContents();
                        temp.score = sheet.getCell(11, i).getContents();
                        temp.desc = sheet.getCell(12, i).getContents();
                        mArrayBuffer.add(temp);
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return mArrayBuffer;
    }

    public List<String> readFogninfo(String stock_code, String group, boolean header) {
        InputStream is=null;
        Workbook wb=null;
        String contents1=null;
        int line, col;
        String PathFile = STOCKDIR+stock_code+"fogn.xls";;
        if(group.equals("FOGN")) col = 1;
        else if(group.equals("AGENCY")) col = 2;
        else col = 1;

        List<String> Buffer = new ArrayList<>();
        List<String> Buffer_rev = new ArrayList<>();

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    // line1, col1에서 contents를 읽는다.
                    int start = 0;
                    if(header != TRUE) start = 1;
                    //for(int i=start;i<sheet.getColumn(col).length;i++) {
                    for(int i=start;i<20;i++) {
                        Buffer.add(sheet.getCell(col, i).getContents());
                    }
                    // 주가의 데이터 순서와 맞춰준다
                    // 과거의 데이터를 시작으로 해서 최신 데이터가 가장 끝에 오도록 재정렬 해준다
                    // 저장할 때 역순으로 먼저 저장해도 되지만
                    // 100개를 역순으로 저장하고 20개를 읽으면 20개에 최신 데이터가 포함되지 않는다
                    // 최과거 데이터가 0번이기 때문에 최과거 기준으로 20개가 읽히므로
                    // 그것을 방지하기 위해 읽는 곳에서 데이터를 역순으로 배열시킨다
                    for(int j =Buffer.size()-1;j>=0;j--) {
                        Buffer_rev.add(Buffer.get(j));
                    }
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return Buffer_rev;
    }

    public String readTreemap() {

        StringBuilder  sb = new StringBuilder();
        String PathFile = STOCKDIR+"treemap.xls";;
        InputStream is=null;
        Workbook wb=null;
        String spc10="          ";
        String spc4="    ";

        try {
            is =  new FileInputStream(PathFile);
            wb = Workbook.getWorkbook(is);
            if(wb != null) {
                Sheet sheet = wb.getSheet(0);   // 시트 불러오기
                if(sheet != null) {
                    for(int i=0;i<sheet.getColumn(0).length;i++) {
                        sb.append(spc10 +"[");
                        sb.append("'"+sheet.getCell(0, i).getContents()+"',"+spc4); // name
                        sb.append("'"+sheet.getCell(1, i).getContents()+"',"+spc4); // area
                        sb.append(sheet.getCell(2, i).getContents()+","+spc4); // volume
                        sb.append(sheet.getCell(3, i).getContents()); // change
                        sb.append("],\n");
                    }
                    sb.append(spc10+"]);\n");
                }
            }
            wb.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void writehtml(String html_string) {

        String PathFile = "file:///android_asset/" + "treemap" + ".html";
        ;
        try {
            File file = new File(PathFile); // File객체 생성
            FileWriter fw = new FileWriter(file, false);
            fw.write(html_string);
            fw.close();
        } catch (IOException e) {
            //Toast.makeText(getContext(), "io error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {

        }
    }
}