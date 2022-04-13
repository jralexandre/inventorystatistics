package br.com.azimusb.inventorystatistics.helpers;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {

    private final Queue<Long> window = new LinkedList<Long>();
    private final int period;
    private Long sum = 0L;

    public MovingAverage(int period) {
        assert period > 0 : "Period must be a positive integer";
        this.period = period;
    }

    public void add(Long num) {
        sum += num;
        window.add(num);
        if (window.size() > period) {
            sum -= window.remove();
        }
    }

    public double getAverage() {
        if (window.isEmpty()) return 0.0;
        return (double)sum / window.size();
    }

    public void clear() {
        window.clear();
        sum = 0L;
    }
}
