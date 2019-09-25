package com.example.fmkmeter;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
@Entity
public class Result {

    @PrimaryKey(autoGenerate = true)
    public long id;
    private int value;
    private long timeCreate;
    private int order;
    private String note;

    public long getTimeCreate() {
        return timeCreate;
    }

    public int getOrder() {
        return order;
    }

    public int getValue() {
        return value;
    }

    public String getNote(){
        return note;
    }

    public Result(int order, int value, String note){
        this.timeCreate= Calendar.getInstance().getTimeInMillis();
        this.value=value;
        this.order=order;
        this.note=note;
    }
}
