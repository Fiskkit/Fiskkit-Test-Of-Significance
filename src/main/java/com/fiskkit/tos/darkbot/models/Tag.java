package com.fiskkit.tos.darkbot.models;

/**
 * Created by Fabled on 7/12/2014.
 */
public class Tag {
    private int id;
    private int count;

    public Tag(int id, int count) {
        this.id = id;
        this.count = count;
    }

    public int getId() {

        return id;
    }

    public int getCount() {
        return count;
    }
}
