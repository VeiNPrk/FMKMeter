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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value=value;
    }

    private float value;

    public Signal(int time, float value){
        this.time=time;
        this.value=value;
    }
}
