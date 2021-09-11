package com.example.fmkmeter;

import android.app.Application;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public final class MeterViewModel extends AndroidViewModel {

    private MeterContractor.Presenter presenter;
    private Repository repository;
    private static MutableLiveData<Long> timerStartData = new MutableLiveData<Long>();
    private static MutableLiveData<Long> timerMeasurementData = new MutableLiveData<Long>();
    public boolean timerStartIsFinish = true;
    public boolean timerMeasurmentIsFinish = true;
    public boolean isClickStart = false;

    public MeterViewModel(@NonNull Application application) {
        super(application);
        repository = App.getInstance().getRepository();
    }

    void setPresenter(MeterContractor.Presenter presenter) {
         if (this.presenter == null) {
             this.presenter = presenter;
             this.presenter.setContext(getApplication());
             //this.presenter.setRepository(repository);
         }
     }

    public Repository getRepository(){
        return repository;
    }

    public MutableLiveData<Long> getTimerStartData(){ return timerStartData; }

    public MutableLiveData<Long> getTimerMeasurementData(){ return timerMeasurementData; }

    MeterContractor.Presenter getPresenter() {
        if(presenter == null) {
            presenter = new MeterPresenter(getApplication(), repository);
            //presenter.se
        }
        return this.presenter;
    }

    public CountDownTimer getStartTimer(int sec){
        return new CountDownTimer(sec * 1000 + 600, 200) {
            public void onTick(long millisUntilFinished) {
                timerStartIsFinish = false;
                timerStartData.postValue(millisUntilFinished / 1000);
                Log.d("TIMER",millisUntilFinished / 1000 + " с.");
            }
            public void onFinish() {
                timerStartIsFinish = true;
                timerStartData.postValue(0L);
                Log.d("TIMER","done!");
            }
        };
    }

    public CountDownTimer getMeasurementTimer(int sec){
        return new CountDownTimer(sec * 1000 + 600, 200) {
            public void onTick(long millisUntilFinished) {
                timerMeasurmentIsFinish = false;
                timerMeasurementData.postValue(millisUntilFinished / 1000);
                Log.d("TIMER",millisUntilFinished / 1000 + " с.");
            }
            public void onFinish() {
                timerMeasurmentIsFinish = true;
                timerMeasurementData.postValue(0L);
                Log.d("TIMER","done!");
            }
        };
    }
 
     @Override
     protected void onCleared() {
         super.onCleared();
         presenter.onPresenterDestroy();
         presenter = null;
         repository=null;
     }
 }
