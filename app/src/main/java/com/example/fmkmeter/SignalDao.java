package com.example.fmkmeter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SignalDao {

    // LiveData is a data holder class that can be observed within a given lifecycle.
    // Always holds/caches latest version of data. Notifies its active observers when the
    // data has changed. Since we are getting all the contents of the database,
    // we are notified whenever any of the database contents have changed.
    @Query("SELECT * from signal ORDER BY time ASC")
    LiveData<List<Signal>> getAllSignalls();

    /*@Query("SELECT * from result ORDER BY timeCreate ASC")
    LiveData<List<Result>> getAllResults();*/

    @Query("SELECT * FROM signal WHERE id=:id")
    LiveData<List<Signal>> findRepositoriesForUser(final int id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<Signal> signals);

    /*@Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertResult(Result result);*/

    @Query("DELETE FROM signal")
    void deleteAll();
}
