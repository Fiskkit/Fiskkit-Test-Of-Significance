package com.fiskkit.tos.darkbot.util;

/**
 * Created by Fabled on 9/10/2014.
 */
public class TimeTracker {

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime() {
        this.time = System.nanoTime();
    }
}
