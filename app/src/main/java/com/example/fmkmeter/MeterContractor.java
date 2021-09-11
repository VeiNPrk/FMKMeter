package com.example.fmkmeter;

import android.content.Context;

import androidx.lifecycle.Lifecycle;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.LifecycleOwner;

import java.util.List;

public interface MeterContractor {
    interface View{
        void showDialog(DialogFragment dialog);
        
        void showToast(String message);

        void updateCounter(int cnt);

        void updateResult(int cnt);

        void showProgressBar(boolean tf);

        void selectSpnDevice(int index);

        void goToResult();

        void izmIsStart(boolean isStart);
    } 

    interface Presenter<V extends MeterContractor.View>{

        void setContext(Context context);

        void setLifecycleOwner(/*Repository repository, */LifecycleOwner lifecycleOwner);

        void attachLifecycle(Lifecycle lifecycle);

        void detachLifecycle(Lifecycle lifecycle);

        void attachView(V view);

        void detachView();

        V getView();

        boolean deviceIsOpened();

        boolean isViewAttached();

        void onPresenterDestroy();

        DialogFragment getInfoDialog();

        void refreshOnClick();

        List<String> getListDevices();

        void spnDevicesItemOnClick(int position);

        void singlIzmOnClick();

        void startIzmOnClick();

        void finishIzmOnClick();

        void startIzmOnClickTest();

        void finishIzmOnClickTest();

        void connectDevice(int deviceIndex);
    }
}
