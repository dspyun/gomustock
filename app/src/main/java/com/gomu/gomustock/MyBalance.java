package com.gomu.gomustock;

import com.gomu.gomustock.portfolio.BuyStockDBData;
import com.gomu.gomustock.portfolio.Cache;
import com.gomu.gomustock.ui.home.Portfolio;
import com.gomu.gomustock.portfolio.SellStockDBData;

import java.util.ArrayList;
import java.util.List;

public class MyBalance {

    // 일단 1500만원 넣어준다
    int cache = 20000000;
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
//    int listsize;
    Portfolio myportfolio; //= new Portfolio();
    List<BuyStockDBData> buystockList = new ArrayList<BuyStockDBData>();
    List<SellStockDBData> sellstockList = new ArrayList<SellStockDBData>();
    int balanace_valid;
    public MyBalance(String name) {
        this.stock_code = name;
        // 순서바꾸면 안됨. 차례로 데이터 들어가고 계산되어야 함
        cache = getCache();
    }

    public int getCache() {
        Cache mycache = new Cache();
        return mycache.getRemainCache();
    }

    public void prepareDataset(List<BuyStockDBData> buystockList, List<SellStockDBData> sellstockList) {
        this.buystockList=buystockList;
        this.sellstockList=sellstockList;
        int listsize;
        MyDate mydate = new MyDate();
        List<String>  temp = new ArrayList<String>();
       // 날짜는 파일에서 읽어와서 카피한다.
        inputDate.addAll(myexcel.oa_readItem(stock_code+".xls", "DATE", false));
        if(inputDate.size() <= 0) balanace_valid = -1;

        // buyquantity와 sellquantity 리스트의 모든 element에
        // 매수 매도 데이터가 들어가는 것은 아니다
        // 매도하는 날도 있고 매수하는 날도 있고 아무것도 안하는 날도 있다
        // 그래서 일단 초기화는 0으로 시켜둔다 null로 둘 수는 없으니까

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
        // 엑셀에서 읽은 값은 모두 string이다. integer로 바꾼 다음 list에 저장한다
        temp.addAll(myexcel.oa_readItem(stock_code+".xls", "CLOSE", false));
        if(temp.size() <= 0) balanace_valid = -1;
        listsize = temp.size();
        for(int i = 0; i< listsize; i++) {
            inputStockprice.set(i,Integer.parseInt(temp.get(i)));
        }
        // buylist db에서 매수량을 읽어서 해당날짜의 위치에 집어넣는다.
        loadBuyList(buystockList);
        // selllist db에서 매도량을 읽어서 해당날짜의 위치에 집어넣는다.
        loadSellList(sellstockList);
    }

    public void loadBuyList(List<BuyStockDBData> buystockList) {
        // 매수일자, 매수액, 매수량을 db에서 불러와서
        // 엑셀 페이블에 저장한다
        // buyquantity와 sellquantity도 불러와야 한다
        // db에서 불러와서 해당날짜 index에 저장해준다
        //buystockList = buystock_db.buystockDao().getAll();


        //buystockList = myportfolio.getBuyList();
        int index=0;
        // date를 비교해보고 date가 있으면 해당날짜의 매수량을 copy한다
        for(int i=0; i < buystockList.size();i++) {
            index = inputDate.indexOf(buystockList.get(i).buy_date);
            inputBuyQuantity.set(index,buystockList.get(i).buy_quantity);
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
        for(int i=0; i < sellstockList.size();i++) {
            index = inputDate.indexOf(sellstockList.get(i).sell_date);
            inputSellQuantity.set(index,sellstockList.get(i).sell_quantity);
        }
        index = 0;
    }

    public void makeBalancedata() {
        // 원금, 매수량/매수액, 매도량/매도액 테이블의 값을 가지고
        // 1년치 일일 잔액, 보유수량, 평가액 변화를 계산해서 엑셀에 저장한다
        // 보유수량 변화량을 계산한다.
        int quantity=0;
        int listsize = inputDate.size();
        for(int i=0;i<listsize;i++) {
            quantity = quantity + inputBuyQuantity.get(i);
            quantity = quantity - inputSellQuantity.get(i);
            outputQuantity.set(i, quantity);
            quantity = 0;
        }
        // 현금변화량을 계산한다
        for(int i=0;i<listsize;i++) {
            cache = cache - inputBuyQuantity.get(i)*inputStockprice.get(i);
            cache = cache + inputSellQuantity.get(i)*inputStockprice.get(i);
            outputCache.set(i, cache);
            cache = 0;
        }
        // 평가액 변화량을 계산한다
        // 일일보유수량*일일종가
        for(int i=0;i<listsize;i++) {
            outputEstim.set(i, outputQuantity.get(i)*inputStockprice.get(i));
        }

        // 총자산액
        for(int i=0;i<listsize;i++) {
            outputTotal.set(i, outputEstim.get(i) + outputCache.get(i));
        }

        MyExcel myexcel = new MyExcel();

        List<String> temp1 = new ArrayList<>();
        List<String> temp2 = new ArrayList<>();
        List<String> temp3 = new ArrayList<>();
        for(int i=0;i<outputCache.size();i++) {
            temp1.add(Integer.toString(outputCache.get(i)));
            temp2.add(Integer.toString(outputEstim.get(i)));
            temp3.add(Integer.toString(outputTotal.get(i)));
        }
        //myexcel.writebalance_test(inputDate,temp1,temp2,temp3);
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
        /*
        if(balanace_valid == -1) {
            for(int i =0;i<60;i++) {
                outputCache.add(0);
            }
        }

         */
        return outputCache;
    }

    public List<Integer> getEstimStock() {
        // 만덜어진 balance data가 저장된 엑셀에서
        // 잔액을 읽어서 리턴해준다 차트데이터로 쓰인다.
        /*
        if(balanace_valid == -1) {
            for(int i =0;i<60;i++) {
                outputEstim.add(0);
            }
        }

         */

        List<Integer> estim_rev = new ArrayList<>();
        for(int i=outputEstim.size()-1;i>=0;i--) {
            estim_rev.add(outputEstim.get(i));
        }
        return estim_rev;
        //return outputEstim;
    }

    public List<Integer> getTotalAsset() {
        // 만덜어진 balance data가 저장된 엑셀에서
        // 총액을 읽어서 리턴해준다. 차트데이터로 쓰인다.
        /*
        if(balanace_valid == -1) {
            for (int i = 0; i < 60; i++) {
                outputTotal.add(0);
            }
        }
         */
        return outputTotal;
    }

    public List<Integer> plusRemaincache(List<Integer> input) {
        List<Integer> sum = new ArrayList<Integer>();
        for(int i =0;i<input.size();i++) {
            sum.add(outputCache.get(i)+input.get(i));
        }
        return sum;
    }
    public List<Integer> plusEstimStock(List<Integer> input) {
        List<Integer> sum = new ArrayList<Integer>();
        for(int i =0;i<input.size();i++) {
            sum.add(outputEstim.get(i)+input.get(i));
        }
        return sum;
    }
}
