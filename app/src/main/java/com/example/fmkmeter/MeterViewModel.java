package com.example.fmkmeter;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

public final class MeterViewModel extends AndroidViewModel {

     private MeterContractor.Presenter presenter;
    Repository repository;
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

    MeterContractor.Presenter getPresenter() {
         return this.presenter;
     }
 
     @Override
     protected void onCleared() {
         super.onCleared();
         presenter.onPresenterDestroy();
         presenter = null;
         repository=null;
     }
 }
