package com.example.fmkmeter;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.fmkmeter.utils.D2DeviceUtils;
import com.example.fmkmeter.utils.SharedPreferenceUtils;
import com.ftdi.j2xx.FT_Device;

import java.util.List;

public class MeterPresenter<V extends MeterContractor.View> implements LifecycleObserver, MeterContractor.Presenter<V>, ReaderAsyncTask.ReaderAsyncTaskListener, Repository.InsertAsyncTaskListener {
    public static final String TAG = "MeterPresenter";
    private Bundle stateBundle;
    private V view;
    private Context context;
    private D2DeviceUtils deviceUtils;
    ReaderAsyncTask readerAsyncTask;
    private boolean isSingle = false;
    private boolean isIzmStart = false;
    private boolean isAutoStart = false;
    private boolean isInsertDone = false;
    private boolean isInsertStart = false;
    private Repository repository;
    LifecycleOwner ow1;

    public MeterPresenter(Context context, Repository repository){
        this.context = context;
        this.repository = repository;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    public void setLifecycleOwner(/*Repository repository, */LifecycleOwner lifecycleOwner) {
        ow1 = lifecycleOwner;
        //this.repository = repository;
        this.repository.getAllData().observe(lifecycleOwner, new Observer<List<Integer>>() {
            @Override
            public void onChanged(@Nullable List<Integer> data) {
                view.updateCounter(data.size());
                //textView.setText(value)
            }
        });
        this.repository.getEndData().observe(lifecycleOwner, new Observer<List<Signal>>() {
            @Override
            public void onChanged(@Nullable List<Signal> data) {
                Log.d("MeterPresenter", "EndData onChanged");
                view.updateResult(data.size());
                if (isInsertDone) {
                    view.showProgressBar(false);
                    isInsertDone = false;
                    isInsertStart = false;
                }
            }
        });
        if (isInsertStart)
            view.showProgressBar(true);

        if (deviceUtils == null)
            deviceUtils = new D2DeviceUtils(context);
    }

    @Override
    final public V getView() {
        return view;
    }

    @Override
    final public void attachLifecycle(Lifecycle lifecycle) {
        lifecycle.addObserver(this);
    }

    @Override
    final public void detachLifecycle(Lifecycle lifecycle) {
        lifecycle.removeObserver(this);
    }

    @Override
    public void attachView(V view) {
        this.view = view;
        //deviceUtils = new D2DeviceUtils(context);
    }

    @Override
    final public void detachView() {
        view = null;

    }

    @Override
    final public boolean isViewAttached() {
        return view != null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void viewOnStart() {
        view.izmIsStart(isIzmStart);
    }

    /*
        @Override
        final public Bundle getStateBundle() {
            return stateBundle == null ?
                    stateBundle = new Bundle() : stateBundle;
        }
        */
    @Override
    public void onPresenterDestroy() {
        if (stateBundle != null && !stateBundle.isEmpty()) {
            stateBundle.clear();
        }
        if (deviceUtils.getDevice() != null) {
            deviceUtils.getDevice().close();
            deviceUtils = null;
        }
    }

    @Override
    public DialogFragment getInfoDialog() {
        return InfoDialogFragment.newInstance(deviceUtils.getDeviceNode());
        //dialog.show(getSupportFragmentManager(), InfoDialogFragment.TAG);
        //return null;
    }

    @Override
    public void refreshOnClick() {
        getListDevices();
        /*if(getListDevices().size()>0)
            view.selectSpnDevice(0);*/
    }

    @Override
    public List<String> getListDevices() {
        return deviceUtils.getDeviceList();
    }

    @Override
    public void spnDevicesItemOnClick(int position) {
        try {
            if (!deviceUtils.isOpened()) {
                view.showToast(deviceUtils.connectDevice(position));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void singlIzmOnClick() {
        Log.d(TAG, "SingleIzm");
        if (!deviceUtils.isOpened()) {
            view.showToast(context.getString(R.string.msg_device_not_open));
            return;
        }
        deviceUtils.restartDevice();
        deviceUtils.setSettings();
        isSingle = true;
        //read_thread.start();
        try {
            deviceUtils.singleIzm();
            StartThreadRead(deviceUtils.getDevice(), true);
        } catch (Exception ex) {
            //binding.tvRead.setText(ex.toString());
            //Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG);
        }
    }

    public boolean deviceIsOpened(){
        return deviceUtils.isOpened();
    }

    @Override
    public void startIzmOnClick() {
        Log.d(TAG, "startIzmOnClick");
        //Для отладки
        if (!deviceUtils.isOpened()) {
            view.showToast(context.getString(R.string.msg_device_not_open));
            return;
        }
        //Log.d(TAG, "StartIzm");
        StopThreadRead();

        deviceUtils.restartDevice();
        deviceUtils.setSettings();

        try {
            deviceUtils.startIzm();
            isIzmStart = true;
            view.izmIsStart(isIzmStart);
            StartThreadRead(deviceUtils.getDevice(), false);
        } catch (Exception ex) {
            //binding.tvRead.setText(ex.toString());
        }
    }

    @Override
    public void finishIzmOnClick(boolean isAutoFinish) {
        Log.d(TAG, "FinishIzm");
        try {
            StopThreadRead();
            isIzmStart = false;
            if(!isAutoFinish)
                view.izmIsStart(isIzmStart);
            if (!deviceUtils.isOpened()) {
                view.showToast(context.getString(R.string.msg_device_not_open));
                return;
            }
            deviceUtils.finishIzm();
            isInsertStart = true;
            view.showProgressBar(true);
            //repository.saveDataToDb();
        } catch (Exception ex) {
            //binding.tvRead.setText(ex.toString());
        }
    }

    @Override
    public void startCalculate(CalculateAsyncTask.CalculateAsyncTaskListener calculateAsyncTaskListener, CalculateAsyncTaskNew.CalculateAsyncTaskNewListener calculateAsyncTaskNewListener) {
        boolean tfIntegr = SharedPreferenceUtils.getIsIntegrate(context);
        int cntNLast = 2000;
        try {
            cntNLast = SharedPreferenceUtils.getCntNLast(context);
        } catch (NumberFormatException nfe) {
            cntNLast = 2000;
        }
        if(tfIntegr && SharedPreferenceUtils.getIsUseNewIntegrate(context)) {
            CalculateAsyncTaskNew calculateAsyncTaskNew = new CalculateAsyncTaskNew(calculateAsyncTaskNewListener, cntNLast, tfIntegr);
            calculateAsyncTaskNew.setData(repository.getEndData().getValue());
            calculateAsyncTaskNew.execute(0);
        } else{
            CalculateAsyncTask calculateAsyncTask = new CalculateAsyncTask(calculateAsyncTaskListener, cntNLast, tfIntegr);
            calculateAsyncTask.setData(repository.getEndData().getValue());
            calculateAsyncTask.execute(0);
        }
    }

    @Override
    public void startIzmOnClickTest() {
        Log.d(TAG, "StartIzm");
        try {
            isIzmStart = true;
            view.izmIsStart(isIzmStart);
            RandomValueCreator.createRandomSignals();

        } catch (Exception ex) {

        }
    }

    @Override
    public void finishIzmOnClickTest() {
        repository.saveDataToDb(new TestData().getTestSignals(), this);
    }

    @Override
    public void connectDevice(int deviceIndex) {

    }

    private void StartThreadRead(/*boolean bReadThreadGoing,*/FT_Device dev, boolean isSingle) {
        readerAsyncTask = new ReaderAsyncTask(dev, isSingle);
        readerAsyncTask.setListener(this);
        readerAsyncTask.execute();
    }

    private void StopThreadRead() {
        if (readerAsyncTask == null) return;
        Log.d(TAG, "cancel result: " + readerAsyncTask.cancel(false));
    }

    @Override
    public void onProgressConclude() {

    }

    @Override
    public void onPostExecuteConcluded(boolean isSingle) {
        if (isSingle)
            repository.saveDataToDb(this);
    }

    @Override
    public void onCanceled() {
        repository.saveDataToDb(this);
    }

    @Override
    public void onPostInsertExecute() {
        Log.d("MeterPresenter", "onPostInsertExecute");
        isInsertDone = true;
        repository.initAllLiveData();
        view.showProgressBar(false);
        isInsertDone = false;
        isInsertStart = false;
        try {
            int i = repository.getEndData().getValue().size();
        } catch (Exception ex) {
            int i = 0;
        }

        view.goToResult();
        /*this.repository.getEndData().observe(ow1, new Observer<List<Signal>>() {
            @Override
            public void onChanged(@Nullable List<Signal> data) {
                view.updateResult(data.size());
                if(isInsertDone){
                    view.showProgressBar(false);
                    isInsertDone=false;
                }
            }
        });*/
    }
}