package com.example.fmkmeter;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

public final class ChartViewModel<V extends ChartContractor.View, P extends ChartContractor.Presenter<V>> extends AndroidViewModel {

     private P presenter;
     Repository repository;
     public ChartViewModel(@NonNull Application application) {
          super(application);
          repository = App.getInstance().getRepository();
      }

     void setPresenter(P presenter) {
          if (this.presenter == null) {
               this.presenter = presenter;
               this.presenter.setContext(getApplication());
          }
     }

     P getPresenter() {
          return this.presenter;
     }

     public Repository getRepository(){
          return repository;
      }

     @Override
     protected void onCleared() {
          super.onCleared();
          presenter.onPresenterDestroy();
          presenter = null;
          repository=null;
     }
}
