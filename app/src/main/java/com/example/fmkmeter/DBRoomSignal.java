package com.example.fmkmeter;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {Signal.class}, version = 1)
public abstract class DBRoomSignal extends RoomDatabase {
         public abstract SignalDao signalDao();
}

