package com.gomu.gomustock.stockengin;

import com.gomu.gomustock.MyDate;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.SellStockDBData;
import com.gomu.gomustock.ui.home.Cache;

import java.util.ArrayList;
import java.util.List;

public class MyBalance {

    int cache = 0;
    public String stock_code ="";
    public List<String> inputDate = new ArrayList<>();
    public List<Integer> inputStockprice = new ArrayList<Integer>();
    public List<Integer> inputBuyQuantity = new ArrayList<Integer>();
    public List<Integer> inputSellQuantity = new ArrayList<Integer>();
    public List<Integer>  outputQuantity = new ArrayList<Integer>();
    public List<Integer>  outputCache = new ArrayList<Integer>();
    public List<Integer>  outputEstim = new ArrayList<Integer>();
    public List<Integer>  outputTotal = new ArrayList<Integer>();

    MyExcel myexcel = new MyExcel();
    List<BuyStockDBData> buystockList = new ArrayList<BuyStockDBData>();
    List<SellStockDBData> sellstockList = new ArrayList<SellStockDBData>();
    int balanace_valid;
    Cache mycache = new Cache();
    public MyBalance(String name) {
        this.stock_code = name;
    }

    public int getCache() {
        mycache = new Cache();
        return mycache.getRemainCache();
    }

    public void prepareDataset(List<BuyStockDBData> buystockList1, List<SellStockDBData> sellstockList1) {
        this.buystockList=buystockList1;
        this.sellstockList=sellstockList1;
        int listsize;
        MyDate mydate = new MyDate();
        List<String>  price = new ArrayList<String>();
       // 날짜는 파일에서 읽어와서 카피한다. 현재>과거 순으로 정열되어 있다.
        inputDate.addAll(myexcel.oa_readItem(stock_code+".xls", "DATE", false));
        if(inputDate.size() <= 0) balanace_valid = -1;

        // buyquantity와 sellquantity 리스트의 모든 element에
        // 매수 매도 데이터가 들어가는 것은 아니다
        // 매도하는 날도 있고 매수하는 날도 있고 아무것도 안하는 날도 있다
        // 그래서 일단 초기화는 0으로 시켜둔다 null로 둘 수는 없으니까
        listsize = inputDate.size();
        init_buffer(listsize);


        // 엑셀에서 읽은 값은 모두 string이다. integer로 바꾼 다음 list에 저장한다
        price.addAll(myexcel.oa_readItem(stock_code+".xls", "CLOSE", false));
        if(price.size() <= 0) balanace_valid = -1;
        listsize = price.size();
        for(int i = 0; i< listsize; i++) {
            // price는 현재>과거 순으로 저정된다
            inputStockprice.set(i,Integer.parseInt(price.get(i)));
        }
        // buylist db에서 매수량을 읽어서 해당날짜의 위치에 집어넣는다.
        loadBuyList(buystockList);
        // selllist db에서 매도량을 읽어서 해당날짜의 위치에 집어넣는다.
        loadSellList(sellstockList);


    }

    public void init_buffer(int listsize) {
        inputBuyQuantity.clear();
        inputSellQuantity.clear();
        outputQuantity.clear();
        outputCache.clear();
        outputEstim.clear();
        inputStockprice.clear();

        listsize = inputDate.size();
        for(int i=0;i<listsize;i++) {
            inputBuyQuantity.add(0);
            inputSellQuantity.add(0);
            outputQuantity.add(0);
            outputCache.add(0);
            outputEstim.add(0);
            outputTotal.add(0);
            inputStockprice.add(0);
        }
    }

    public void loadBuyList(List<BuyStockDBData> buystockList) {
        // 매수일자, 매수액, 매수량을 db에서 불러와서
        // 엑셀 페이블에 저장한다
        // buyquantity와 sellquantity도 불러와야 한다
        // db에서 불러와서 해당날짜 index에 저장해준다
        // buystockList = buystock_db.buystockDao().getAll();
        // buystockList = myportfolio.getBuyList();

        int index=0;
        int value=0;
        // date를 비교해보고 date가 있으면 해당날짜의 매수량을 copy한다
        int size = buystockList.size();
        for(int i=0; i < size ;i++) {
            // 1. inputBuyQuantity는 inputdate의 index 순으로
            //    buystockList의 데이터를 읽어 저짱하기 때문에
            //    현재>과거 순으로 저장된다.
            // 2. 오늘아침에 inputDate파일이 update되지않고
            //    주식을 하나 사면 inputDate에는 오늘날짜가 없어서
            //    아래 index는 -1이 된다. 어떻게 처리할 것이냐? > 일단 continue로 가자
            index = inputDate.indexOf(buystockList.get(i).buy_date);
            if(index == -1) continue;
            inputBuyQuantity.set(index, buystockList.get(i).buy_quantity);
        }
        index = 0; // 디버깅용
    }

    public void loadSellList(List<SellStockDBData> sellstockList) {
        // 매도일자, 매도액, 매도량을 db에서 불러와서
        // 엑셀테이블에 저장한다
        // db에서 불러와서 해당날짜 index에 해당하는 inputarray에 저장해준다

        MyDate mydate = new MyDate();

        //sellstockList = myportfolio.getSellList();
        int index =0;
        // date를 비교해보고 date가 있으면 해당날짜의 매도량을 copy한다
        int size =sellstockList.size();
        for(int i=0; i < size;i++) {
            // 1. inputSellQuantity inputdate의 index 순으로
            //    buystockList의 데이터를 읽어 저짱하기 때문에
            //    현재>과거 순으로 저장된다.
            // 2. 오늘아침에 inputDate파일이 update되지않고
            //    주식을 하나 팔면 inputDate에는 오늘날짜가 없어서
            //    아래 index는 -1이 된다. 어떻게 처리할 것이냐? > 일단 continue로 가자
            index = inputDate.indexOf(sellstockList.get(i).sell_date);
            if(index == -1) continue;
            inputSellQuantity.set(index,sellstockList.get(i).sell_quantity);
        }
        index = 0;
    }

    public void putTodayBuySellData(String today, int buyquan,int sellquan) {

        // 오늘 매매 데이터를 buylist와 selllist에 붙인다.(simulation에서는 안 붙여도 됨)
        // 날짜 inputDate의 index0은 오늘날짜가 들어가 있다(왜냐면 파일로 다운받았으니)
        // 하지만 매매데이터는 폰에서 생성된 정보이고 파일에 포함되어 있지 않다
        // 그래서 오늘 buy sell 수량을 index 0에 각각 넣어 주어야 한다.

        if(inputDate.get(0).equals(today)) {
            int size = inputBuyQuantity.size();
            inputBuyQuantity.set(0, buyquan);
        }

        if(inputDate.get(0).equals(today)) {
            int size2 = inputSellQuantity.size();
            inputSellQuantity.set(0, sellquan);
        }
    }

    public void putTodayPrice(String today, int price) {
        // price는 현재>과거순으로 저장되어 있으니
        // 오늘 price는 0번째에 넣어준다
        if(inputDate.get(0).equals(today)) {
            inputStockprice.set(0, price);
        }
    }

    public void makeBalancedata() {
        // 원금, 매수량/매수액, 매도량/매도액 테이블의 값을 가지고
        // 1년치 일일 잔액, 보유수량, 평가액 변화를 계산해서 엑셀에 저장한다
        // 보유수량 변화량을 계산한다.


        // input 어레이는 최신날짜 > 과거날짜 순으로 정렬되어 있다
        // 하지만 누적계산은 과거날짜 > 최신 순으로 해야 하고
        // 차트배열에서도 과거>현재순으로 정렬된 값이 사용됨으로
        // 과거날짜>최신날짜 순으로 역정렬해준다.
        // 역순 정렬된 데이터로 계산한 후, 반환한다
        List<Integer> inputStockprice_rev = new ArrayList<Integer>();
        List<Integer> inputBuyQuantity_rev = new ArrayList<Integer>();
        List<Integer> inputSellQuantity_rev = new ArrayList<Integer>();
        inputStockprice_rev = myexcel.arrangeRev_int(inputStockprice);
        inputBuyQuantity_rev = myexcel.arrangeRev_int(inputBuyQuantity);
        inputSellQuantity_rev = myexcel.arrangeRev_int(inputSellQuantity);

        int quantity=0;
        int listsize = inputDate.size();
        for(int i=0;i<listsize;i++) {
            quantity = quantity + inputBuyQuantity_rev.get(i);
            quantity = quantity - inputSellQuantity_rev.get(i);
            outputQuantity.set(i, quantity);
            //quantity = 0;
        }
        // 현금변화량을 계산한다
        int j = cache;
        int first_cache = mycache.getFirstCache();
        int cachehistory = 0;
        for(int i=0;i<listsize;i++) {
            cachehistory = cachehistory - inputBuyQuantity_rev.get(i)*inputStockprice_rev.get(i);
            cachehistory = cachehistory + inputSellQuantity_rev.get(i)*inputStockprice_rev.get(i);
            outputCache.set(i, cachehistory);
            //cache = 0;
        }
        // 평가액 변화량을 계산한다
        // 일일보유수량*일일종가
        for(int i=0;i<listsize;i++) {
            outputEstim.set(i, outputQuantity.get(i)*inputStockprice_rev.get(i));
        }

        // 총자산액
        for(int i=0;i<listsize;i++) {
            outputTotal.set(i, outputEstim.get(i) + outputCache.get(i));
        }
        /*
        List<String> temp1 = new ArrayList<>();
        List<String> temp2 = new ArrayList<>();
        List<String> temp3 = new ArrayList<>();
        for(int i=0;i<outputCache.size();i++) {
            temp1.add(Integer.toString(outputCache.get(i)));
            temp2.add(Integer.toString(outputEstim.get(i)));
            temp3.add(Integer.toString(outputTotal.get(i)));
        }
        myexcel.writebalance_test(inputDate,temp1,temp2,temp3);
        */

        int index = 1;
    }

    public String getBalanceinfo() {
        String info="";
        // 잔액, 보유수량, 평가액으로
        // 총자산을 계산해서 스트링으로 리턴한다
        // 리턴값은 balance textview에 뿌려준다
        //
        return info;
    }

    public List<Integer> getRemaincache() {
        // 만덜어진 balance data가 저장된 엑셀에서
        // 잔액을 읽어서 리턴해준다 차트데이터로 쓰인다.
        //List<Integer> temp = new ArrayList<>();
        //temp = arrangeRev(outputCache);
        return outputCache;
    }

    public List<Integer> getEstimStock() {
        // 만덜어진 balance data가 저장된 엑셀에서
        // 잔액을 읽어서 리턴해준다 차트데이터로 쓰인다.

        //List<Integer> temp = new ArrayList<>();
        //temp = arrangeRev(outputEstim);
        return outputEstim;
    }

    public List<Integer> getTotalAsset() {
        // 만덜어진 balance data가 저장된 엑셀에서
        // 총액을 읽어서 리턴해준다. 차트데이터로 쓰인다.
        //List<Integer> temp = new ArrayList<>();
        //temp = arrangeRev(outputTotal);
        return outputTotal;
    }

}
