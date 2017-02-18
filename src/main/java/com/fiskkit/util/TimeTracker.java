package com.fiskkit.util;

/**
 * Holds time values to be injected into Netty Server.
 */
public class TimeTracker {
    private long time;

    public TimeTracker(){}

    public long getTime() {
        return time;
    }

    public void setTime() {
        this.time = System.nanoTime();
    }
}
