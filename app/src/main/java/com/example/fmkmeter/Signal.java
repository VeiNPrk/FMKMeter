package com.example.fmkmeter;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Signal {

    @PrimaryKey(autoGenerate = true)
    public long id;

    private int time;

    public int getTime() {
        return time;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value=value;
    }

    private int value;

    public Signal(int time, int value){
        this.time=time;
        this.value=value;
    }
}
