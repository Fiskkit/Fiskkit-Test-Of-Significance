package com.fiskkit.tos.darkbot;

import java.util.TimerTask;

import com.fiskkit.tos.darkbot.util.TimeTracker;

/**
 * Created by Fabled on 9/9/2014.
 */
public class ToSTask extends TimerTask {
    TimeTracker time;

    public ToSTask() {
    }

    public ToSTask(TimeTracker t) {
        this.time = t;
    }

    @Override
    public void run() {
        time.setTime();
        System.out.println("Task complete");
        //System.exit(0);
    }
}
