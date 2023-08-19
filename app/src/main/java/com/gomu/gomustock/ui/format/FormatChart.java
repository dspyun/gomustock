package com.gomu.gomustock.ui.format;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormatChart {
    public String name;
    public int color;
    public float min;
    public float max;
    public float last;
    public List<Entry> entries;
    public FormatChart () {

    }
    public void intEntry(List<Integer> data) {
        entries = new ArrayList<>();
        int size = data.size();
        for(int i=0;i<size;i++ ) {
            entries.add(new Entry(i, data.get(i)));
        }
        this.max = (float) Collections.max(data);
        this.min = (float) Collections.min(data);
        this.last = data.get(size-1);
    }
    public void floatEntry(List<Float> data) {
        entries = new ArrayList<>();
        int size = data.size();
        for(int i=0;i<size;i++ ) {
            entries.add(new Entry(i, data.get(i)));
        }
        this.max = Collections.max(data);
        this.min = Collections.min(data);
        this.last = data.get(size-1);
    }
}
