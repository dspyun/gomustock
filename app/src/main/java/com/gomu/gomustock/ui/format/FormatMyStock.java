package com.gomu.gomustock.ui.format;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FormatMyStock {
    public String stock_code;
    public String stock_name;
    public int quantity;
    public float buy_price;
    public int cur_price;
    public List<Float> chartdata = new ArrayList<>();

    public ByteBuffer img_buf_1;
    public ByteBuffer img_buf_2;
    public List<FormatChart> chartlist1;
    public List<FormatChart> chartlist2;
}
