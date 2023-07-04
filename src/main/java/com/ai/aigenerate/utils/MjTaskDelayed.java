package com.ai.aigenerate.utils;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public class MjTaskDelayed implements Delayed {

    private String taskId;
    private long start = System.currentTimeMillis();
    private long time ;
    public MjTaskDelayed(String taskId, long time) {
        this.taskId = taskId;
        this.time = time;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long delay = (start+time) - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    public void resetDelay(long time) {
        this.time = time;
        this.start = System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        MjTaskDelayed o1 = (MjTaskDelayed) o;
        return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
    }
}
