package com.gomu.gomustock;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormatChart {
    public String name;
    public int color;
    public float min;
    public float max;
    List<Entry> entries;
    public FormatChart () {

    }
    public void intEntry(List<Integer> data) {
        entries = new ArrayList<>();
        for(int i=0;i<data.size();i++ ) {
            entries.add(new Entry(i, data.get(i)));
        }
        this.max = (float) Collections.max(data);
        this.min = (float) Collections.min(data);
    }
    public void floatEntry(List<Float> data) {
        entries = new ArrayList<>();
        for(int i=0;i<data.size();i++ ) {
            entries.add(new Entry(i, data.get(i)));
        }
        this.max = Collections.max(data);
        this.min = Collections.min(data);
    }
}
