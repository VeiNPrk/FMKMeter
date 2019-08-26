package com.example.fmkmeter;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.navigation.Navigation;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ftdi.j2xx.FT_Device;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import java.util.List;

public class ChartPresenter<V extends ChartContractor.View> implements LifecycleObserver, 
     ChartContractor.Presenter<V>, LineChartClass.LoadChartListener, CalculateAsyncTask.CalculateAsyncTaskListener {
    public static final String TAG = "ChartPresenter";
    private Bundle stateBundle;
    private V view;
    private Context context;
    //private D2DeviceUtils deviceUtils;
    private CalculateAsyncTask calculateAsyncTask;
    private boolean isCheckedResult = true;
    private boolean isLoadClicked = false;
    private boolean isLoadResultClicked = false;
    private int indexStepChart = 0;
    private int indexLastStepChart = 0;
    private int countData = 0;
    private int stepChart = 0;
    private int progressStep=0;
    private int delta = 0;
    private int mMinMax=0;
    private int progressDelta=50;
    private String mMessage="";
    private boolean isFirstTime=true;
    Repository repository;
    LineChartClass lineChart = null;
    LifecycleOwner lifecycleOw;
    public void setContext(Context context) {
        this.context = context;
        lineChart = new LineChartClass(context, this);

    }

    public void setRepository(final Repository repository, LifecycleOwner lifecycleOwner, boolean isNewInstance) {
        view.showProgressBar(true);
        lifecycleOw=lifecycleOwner;
        this.repository = repository;
        if(isNewInstance)
            clearFragment();
        if(repository.getEndData().getValue()==null) {
            Log.d("ChartPresenter", "repository.initAllLiveData");
            view.showProgressBar(true);
            repository.initAllLiveData();
        }
        else
            countData = this.repository.getEndData().getValue().size();

        Log.d("ChartPresenter", lifecycleOwner.toString());
        repository.getEndLiveData().observe(lifecycleOwner, new Observer<List<Signal>>() {
            @Override
            public void onChanged(@Nullable List<Signal> data) {
                Log.d("ChartPresenter","EndLiveData onChanged "+data.size());
                if(repository.getEndData().getValue()==null)
                    repository.setEndData(data);
            }
        });


        /*Observer<List<Signal>> obs = ;
        Log.d("ChartPresenter","removeObservers "+obs.toString());*/
        repository.getEndData().observe(lifecycleOwner, new Observer<List<Signal>>() {

            @Override
            public void onChanged(@Nullable List<Signal> data) {
                //Log.d("ChartPresenter", " EndData onChanged " + data.size());
                //if (isFirstTime) {
                //isFirstTime = false;
                countData = data.size();
                Log.d("ChartPresenter", "EndData onChanged " + data.size());
                if (data.size() > 0) {
                    //if (isFirstTime) {
                    isFirstTime = false;
                    mMessage = context.getString(R.string.msg_data_load_complete);
                    Log.d("ChartPresenter", "isFirst "+mMessage);
                    loadDataOnClick();
                    //}
                    //lineChart.setData(data);

                } else {
                    //if (isFirstTime) {
                    isFirstTime = false;
                    mMessage = context.getString(R.string.msg_data_not_found);
                    Log.d("ChartPresenter", "isFirst "+mMessage);
                    //}
                }
                view.showMessageForm(true, mMessage);
                view.showProgressBar(false);
                view.showChartForm(false);
                view.setSizeData(countData);
                //}
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void initViews(){
        Log.d(TAG, "OnLifecycleEvent ON_START");
        view.setSwitch(isCheckedResult);
        if(isCheckedResult) {
            view.setSeekBarDelta(progressDelta);
            if(isLoadResultClicked) {
                int step = repository.getResultData().size();
                if(step>0) {
                    lineChart.setData(repository.getResultData());
                    lineChart.setInitSettings(indexStepChart, step);
                    lineChart.loadChart();
                    lineChart.removeAllLimitLines(view.getChart());
                    //lineChart.setRedLine(view.getChart(), average, "Среднее: "+average);
                    lineChart.setRedLine(view.getChart(), mMinMax, "MINMAX: "+mMinMax);
                    view.setTVIndexChart(indexStepChart, indexLastStepChart);
                    view.setMinMax(mMinMax);
                    view.showNextButton(false);
                    view.showPrewButton(false);
                }
                else{
                    view.showChartForm(false);
                    mMessage=context.getString(R.string.msg_data_result_null);
                    view.showMessageForm(true, mMessage);
                }
            }
        }
        else {
            view.setSeekBarStep(progressStep);
            if(isLoadClicked) {
                int step = stepChart;
                if (countData < step) {
                    step = countData;
                    indexLastStepChart = 1;
                } else {
                    indexLastStepChart = countData / step;
                }
                lineChart.setData(repository.getEndData().getValue());
                lineChart.setInitSettings(indexStepChart, step);
                lineChart.loadChart();
                view.setTVIndexChart(indexStepChart, indexLastStepChart);
                view.showNextButton(true);
                view.showPrewButton(true);
                if (indexStepChart == indexLastStepChart)
                    view.showNextButton(false);
                if (indexStepChart == 0)
                    view.showPrewButton(false);
            }
        }

    }

    @Override
    public void clearFragment(){
        isCheckedResult=false;
        clearElements();
        view.showProgressBar(false);
    }

    private void clearElements(){
        indexStepChart = 0;
        indexLastStepChart = 0;
        view.showMessageForm(true, mMessage);
        view.showChartForm(false);
        view.showNextButton(false);
        view.showPrewButton(false);
        view.setTVIndexChart(indexStepChart, indexLastStepChart);
        isLoadClicked = false;
        isLoadResultClicked = false;
    }

    @Override
    final public V getView() {
        return view;
    }

    @Override
    public void setSwitchChartValue(boolean isChecked) {
        if(isCheckedResult!=isChecked) {
            isCheckedResult = isChecked;
            clearElements();
        }
    }

    @Override
    final public void attachLifecycle(Lifecycle lifecycle) {
        lifecycle.addObserver(this);
    }

    @Override
    final public void detachLifecycle(Lifecycle lifecycle) {
        /*repository.getEndLiveData().removeObservers(lifecycleOw);
        repository.getEndData().removeObservers(lifecycleOw);*/
        Log.d("ChartPresenter","detachLifecycle "+lifecycleOw.toString());
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

    @Override
    public void setSeekBarStep(int progress, int step) {
        //if(stepChart==0){
            stepChart = step;
            //indexStepChart = 0;
            progressStep=progress;
        //}
    }


    @Override
    public void setSeekBarDelta(int progress, int delta) {
        //if(this.delta==0) {
            this.delta = delta;
            progressDelta = progress;
       // }
    }

    @Override
    public void onPresenterDestroy() {
        if (stateBundle != null && !stateBundle.isEmpty()) {
            stateBundle.clear();
        }
    }

    @Override
    public void nextSetOnClick() {
        if (countData > 0 && indexStepChart <= indexLastStepChart - 1) {
            view.showPrewButton(true);
            indexStepChart++;
            lineChart.setInitSettings(indexStepChart, stepChart);
            lineChart.loadChart();
            view.setTVIndexChart(indexStepChart, indexLastStepChart);
            if (indexStepChart == indexLastStepChart)
                view.showNextButton(false);
        }
    }

    @Override
    public void prewSetOnClick() {
        if (countData > 0 && indexStepChart > 0) {
            view.showNextButton(true);
            indexStepChart--;
            lineChart.setInitSettings(indexStepChart, stepChart);
            lineChart.loadChart();
            view.setTVIndexChart(indexStepChart, indexLastStepChart);
            if (indexStepChart == 0)
                view.showPrewButton(false);
        }
    }

    @Override
    public void loadDataOnClick() {
        view.setMinMax(0);
        repository.setResultData(null);
        if(repository.getEndData().getValue()==null) {
            view.showToastMessage(context.getString(R.string.msg_data_not_complete_load));
            return;
        }

        if (!view.getSwitchedResult()) {
            clearElements();
            isLoadClicked=true;
            lineChart.setData(repository.getEndData().getValue());
            view.showProgressBar(true);
            view.showMessageForm(false,null);
            int step = stepChart;
            if (countData < step) {
                step = countData;
                indexLastStepChart = 1;
            } else {
                indexLastStepChart = countData / step;
            }

            view.showNextButton(true);
            lineChart.setInitSettings(indexStepChart, step);
            lineChart.loadChart();
            view.setTVIndexChart(indexStepChart, indexLastStepChart);
        } else {
            isLoadResultClicked=true;
            calculateAsyncTask = new CalculateAsyncTask(this);
            calculateAsyncTask.setData(repository.getEndData().getValue());
            calculateAsyncTask.execute(delta);
        }
    }

    @Override
    public void onPostLoadChart(LineData chartData) {
        view.setChart(chartData);
        view.showChartForm(true);
        view.showMessageForm(false, null);
        view.showProgressBar(false);
    }

    @Override
    public void onPostCalculateConcluded(List<Signal> outData, int minMax, int min, int max, int average) {
        indexStepChart=0;
        repository.setResultData(outData);
        if(outData.size()>0) {
            int step = outData.size();
            indexLastStepChart = 0;
            mMinMax=minMax;
            lineChart.setData(outData);
            lineChart.setInitSettings(indexStepChart, step);
            lineChart.loadChart();
            lineChart.removeAllLimitLines(view.getChart());
            //lineChart.setRedLine(view.getChart(), average, "Среднее: "+average);
            lineChart.setRedLine(view.getChart(), minMax, "MINMAX: "+minMax);
            //lineChart.setRedLine(view.getChart(), max, "MAX: "+max);
            view.showMessageForm(false,null);
            view.showChartForm(true);
            view.setTVIndexChart(indexStepChart, indexLastStepChart);
            view.setMinMax(mMinMax);
            view.showNextButton(false);
            view.showPrewButton(false);
        }
        else{
            view.showChartForm(false);
            mMessage = context.getString(R.string.msg_data_result_null);
            view.showMessageForm(true, mMessage);
        }
    }
}