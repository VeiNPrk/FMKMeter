package com.example.fmkmeter;

import android.app.Application;
import androidx.room.Room;

public class App extends Application {

    public static App instance;

    private DBRoomSignal database;
    private Repository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, DBRoomSignal.class, "database")
                .build();
        repository = new Repository(this);
    }

    public static App getInstance() {
        return instance;
    }

    public DBRoomSignal getDatabase() {
        return database;
    }

    public Repository getRepository(){
        return repository;
    }
}
