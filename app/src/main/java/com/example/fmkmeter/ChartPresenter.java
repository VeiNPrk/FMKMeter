package com.example.fmkmeter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.ftdi.j2xx.FT_Device;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.ScatterData;

import java.util.ArrayList;
import java.util.List;

public class ChartPresenter<V extends ChartContractor.View> implements LifecycleObserver,
        ChartContractor.Presenter<V>, LineChartClass.LoadChartListener,
        CalculateAsyncTask.CalculateAsyncTaskListener,
        CalculateAsyncTaskNew.CalculateAsyncTaskNewListener,
        SaveFileDialogFragment.SaveFileDialogListener {
    public static final String TAG = "ChartPresenter";
    private Bundle stateBundle;
    private V view;
    private Context context;
    //private D2DeviceUtils deviceUtils;
    private CalculateAsyncTask calculateAsyncTask;
    private CalculateAsyncTaskNew calculateAsyncTaskNew;
    private boolean isCheckedResult = true;
    private boolean isLoadClicked = false;
    private boolean isLoadResultClicked = false;
    private int indexStepChart = 0;
    private int indexLastStepChart = 0;
    private int countData = 0;
    private int stepChart = 0;
    private int progressStep = 0;
    private int delta = 0;
    private float mMinMax = 0;
    private float mMinMaxSedcondIntegrate = 0;
    private int progressDelta = 50;
    private String mMessage = "";
    private String saveUchastok = "";
    private int saveNIzm = 1;
    private boolean isFirstTime = true;
    private int cntNLast = 2000;
    private boolean tfIntegr = false;
    private static final String KEY_CNT_N_LAST="cnt_n_last";
    private static final String KEY_TF_INTEGR="tf_integr";
    private static final String KEY_TF_VISIBLE_FIRST_INTEGR="tf_visible_first_integr";
    private static final String KEY_TF_USE_NEW_INTEGR="tf_use_new_integr";
    Repository repository;
    LineChartClass lineChart = null;
    LifecycleOwner lifecycleOw;

    public void setContext(Context context) {
        this.context = context;
        lineChart = new LineChartClass(context, this);
    }

    public void setRepository(final Repository repository, LifecycleOwner lifecycleOwner, boolean isNewInstance) {
        view.showProgressBar(true);
        lifecycleOw = lifecycleOwner;
        this.repository = repository;
        if (isNewInstance)
            clearFragment();
        if (repository.getEndData().getValue() == null) {
            Log.d("ChartPresenter", "repository.initAllLiveData");
            view.showProgressBar(true);
            repository.initAllLiveData();
        } else
            countData = this.repository.getEndData().getValue().size();

        Log.d("ChartPresenter", lifecycleOwner.toString());
        repository.getEndLiveData().observe(lifecycleOwner, new Observer<List<Signal>>() {
            @Override
            public void onChanged(@Nullable List<Signal> data) {
                Log.d("ChartPresenter", "EndLiveData onChanged " + data.size());
                if (repository.getEndData().getValue() == null)
                    repository.setEndData(data);
            }
        });

        repository.getEndData().observe(lifecycleOwner, new Observer<List<Signal>>() {

            @Override
            public void onChanged(@Nullable List<Signal> data) {
                countData = data.size();
                Log.d("ChartPresenter", "EndData onChanged " + data.size());
                if (data.size() > 0) {
                    //if (isFirstTime) {
                    isFirstTime = false;
                    mMessage = context.getString(R.string.msg_data_load_complete);
                    Log.d("ChartPresenter", "isFirst " + mMessage);
                    loadDataOnClick();
                } else {
                    isFirstTime = false;
                    mMessage = context.getString(R.string.msg_data_not_found);
                    Log.d("ChartPresenter", "isFirst " + mMessage);
                }
                view.showMessageForm(true, mMessage);
                view.showProgressBar(false);
                view.showChartForm(false);
                view.setSizeData(countData);
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private void initViews() {
        Log.d(TAG, "OnLifecycleEvent ON_START");
        view.setSwitch(isCheckedResult);
        if (isCheckedResult) {
            view.setSeekBarDelta(progressDelta);
            if (isLoadResultClicked) {
                int step = repository.getResultData().size();
                if (step > 0) {
                    lineChart.setData(repository.getResultData(), new ArrayList<Signal>(), new ArrayList<Signal>(), null);
                    lineChart.setInitSettings(indexStepChart, step);
                    lineChart.loadChart();
                    lineChart.removeAllLimitLines(view.getChart());
                    //lineChart.setRedLine(view.getChart(), average, "Среднее: "+average);
                    lineChart.setRedLine(view.getChart(), mMinMax, "MINMAX: " + mMinMax);
                    view.setTVIndexChart(indexStepChart, indexLastStepChart);
                    String[] arrMinMax = getFormatMinMax(repository.getResultData(), new ArrayList<Signal>(), new ArrayList<Signal>(), null, mMinMax, mMinMaxSedcondIntegrate);
                    view.setMinMax(arrMinMax[0], arrMinMax[1]);
                    view.showNextButton(false);
                    view.showPrewButton(false);
                } else {
                    view.showChartForm(false);
                    mMessage = context.getString(R.string.msg_data_result_null);
                    view.showMessageForm(true, mMessage);
                }
            }
        } else {
            view.setSeekBarStep(progressStep);
            if (isLoadClicked) {
                int step = stepChart;
                if (countData < step) {
                    step = countData;
                    indexLastStepChart = 1;
                } else {
                    indexLastStepChart = countData / step;
                }
                lineChart.setData(repository.getEndData().getValue(), new ArrayList<Signal>(), new ArrayList<Signal>(), null);
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
    public void clearFragment() {
        isCheckedResult = false;
        clearElements();
        view.showProgressBar(false);
    }

    private void clearElements() {
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
        if (isCheckedResult != isChecked) {
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
        Log.d("ChartPresenter", "detachLifecycle " + lifecycleOw.toString());
        lifecycle.removeObserver(this);
    }

    @Override
    public void attachView(V view) {
        if(this.view==null)
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
        progressStep = progress;
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
        view.setMinMax("0","0");
        repository.setResultData(null);
        repository.setResultFirstIntegrateData(null);
        repository.setResultSecondIntegrateData(null);
        if (repository.getEndData().getValue() == null) {
            view.showToastMessage(context.getString(R.string.msg_data_not_complete_load));
            return;
        }

        if (!view.getSwitchedResult()) {
            clearElements();
            isLoadClicked = true;
            lineChart.setData(repository.getEndData().getValue(), new ArrayList<Signal>(), new ArrayList<Signal>(), null);
            view.showProgressBar(true);
            view.showMessageForm(false, null);
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
            isLoadResultClicked = true;
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            tfIntegr = sharedPreferences.getBoolean(KEY_TF_INTEGR, false);
            lineChart.setVisibleFirstIntegrate(tfIntegr && sharedPreferences.getBoolean(KEY_TF_VISIBLE_FIRST_INTEGR, false));
            try {
                cntNLast = Integer.parseInt(sharedPreferences.getString(KEY_CNT_N_LAST, "2000"));
            } catch (NumberFormatException nfe) {
                cntNLast = 2000;
            }

            if(tfIntegr && sharedPreferences.getBoolean(KEY_TF_USE_NEW_INTEGR, false)) {
                calculateAsyncTaskNew = new CalculateAsyncTaskNew(this, cntNLast, tfIntegr);
                calculateAsyncTaskNew.setData(repository.getEndData().getValue());
                calculateAsyncTaskNew.execute(delta);
            } else{
                calculateAsyncTask = new CalculateAsyncTask(this, cntNLast, tfIntegr);
                calculateAsyncTask.setData(repository.getEndData().getValue());
                calculateAsyncTask.execute(delta);
            }
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
    public void onPostCalculateConcluded(List<Signal> outData, List<Signal> outFirstIntegrateData, List<Signal> outSecondIntegrateData, float minMax, float minMaxIntegrate, float min, float max, float average) {
        calculateConclude(outData, outFirstIntegrateData, outSecondIntegrateData, minMax, minMaxIntegrate, null/*min, max, average*/);
    }

    @Override
    public void onPostCalculateNewConcluded(List<Signal> outData, List<Signal> outIntegrateFirstData, List<Signal> outIntegrateSecondData, float minMaxValue, float minMaxIntegrateValue, int[] indexesMinMax/*float min, float max, float average*/) {
        calculateConclude(outData, outIntegrateFirstData, outIntegrateSecondData, minMaxValue, minMaxIntegrateValue, indexesMinMax/*min, max, average*/);
    }

    private void calculateConclude(List<Signal> outData, List<Signal> outFirstIntegrateData, List<Signal> outSecondIntegrateData, float minMax, float minMaxSecondIntegrate, int[] indexesMinMax/*float min, float max, float average*/){
        indexStepChart = 0;
        repository.setResultData(outData);
        repository.setResultFirstIntegrateData(outFirstIntegrateData);
        repository.setResultSecondIntegrateData(outSecondIntegrateData);
        if (outData.size() > 0) {
            int step = outData.size();
            indexLastStepChart = 0;
            mMinMax = minMax;
            mMinMaxSedcondIntegrate = minMaxSecondIntegrate;
            lineChart.setData(outData, outFirstIntegrateData, outSecondIntegrateData, indexesMinMax);
            lineChart.setInitSettings(indexStepChart, step);
            lineChart.loadChart();
            lineChart.removeAllLimitLines(view.getChart());
            //lineChart.setRedLine(view.getChart(), average, "Среднее: "+average);
            lineChart.setRedLine(view.getChart(), minMax, "MINMAX: " + minMax);
            //lineChart.setRedLine(view.getChart(), max, "MAX: "+max);
            view.showMessageForm(false, null);
            view.showChartForm(true);
            view.setTVIndexChart(indexStepChart, indexLastStepChart);
            String[] arrMinMax = getFormatMinMax(outData, outFirstIntegrateData, outSecondIntegrateData, indexesMinMax, minMax, minMaxSecondIntegrate);
            view.setMinMax(arrMinMax[0], arrMinMax[1]);
            view.showNextButton(false);
            view.showPrewButton(false);
        } else {
            view.showChartForm(false);
            mMessage = context.getString(R.string.msg_data_result_null);
            view.showMessageForm(true, mMessage);
        }
    }

    private String[] getFormatMinMax(List<Signal> outData, List<Signal> outFirstIntegrateData, List<Signal> outSecondIntegrateData, int[] indexesMinMax, float minMax, float minMaxSecondIntegrate){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String[] minMaxStr = {"", ""};
        if(sharedPreferences.getBoolean(KEY_TF_INTEGR, false) && sharedPreferences.getBoolean(KEY_TF_USE_NEW_INTEGR, false)) {
            if(indexesMinMax!=null) {
                String str = "t1="+outData.get(indexesMinMax[0]).getTime()+", A1="+outData.get(indexesMinMax[0]).getValue()+"; ";
                if (outFirstIntegrateData.size() > 0)
                    str += "t2="+outFirstIntegrateData.get(indexesMinMax[1]).getTime()+", V2="+outFirstIntegrateData.get(indexesMinMax[1]).getValue()+"; ";
                minMaxStr[0] = str;
                str = "";
                if (outSecondIntegrateData.size() > 0) {
                    str += "t3="+outSecondIntegrateData.get(indexesMinMax[2]).getTime()+", X3="+outSecondIntegrateData.get(indexesMinMax[2]).getValue()+"; ";
                    str += "t4="+outSecondIntegrateData.get(indexesMinMax[3]).getTime()+", X4="+outSecondIntegrateData.get(indexesMinMax[3]).getValue()+"; ";
                }
                minMaxStr[1] = str;
            }
        } else{
            minMaxStr[0] = Float.toString(minMax);
            minMaxStr[1] = Float.toString(minMaxSecondIntegrate);
        }
        return minMaxStr;
    }

    private boolean canAccessContacts() {
        return (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(perm));
        } else {
            return false;
        }
    }

    @Override
    public void saveFileOnClick() {
        if (!canAccessContacts()) {
            Log.d("init", "runTimePermissions");
            view.runTimePermissions();
            //initLocation();
        } else {
            view.showDialog();
        }
    }

    @Override
    public DialogFragment getSaveDialog() {
        return SaveFileDialogFragment.newInstance(this, saveUchastok, String.valueOf(saveNIzm));
        //return null;
    }

    @Override
    public void onSaveDialogPositiveClick(String uchastok, String put, String nOpora, String nIzm) {
        String fileName = uchastok + "_" + put + "_" + nOpora + "_" + nIzm + ".txt";
        /*ДЛЯ ОТЛАДКИ
        List<Signal> signalData = new ArrayList<Signal>();
        signalData.add(new Signal(1, 123));
        signalData.add(new Signal(2, 456));
        signalData.add(new Signal(3, 79));*/
        //view.showToastMessage(FileUtils.saveFile(signalData, context, fileName));
        if (repository.getResultData() != null && repository.getResultData().size() > 0) {
            view.showToastMessage(FileUtils.saveFile(repository.getResultData(), repository.getResultFirstIntegrateData(), repository.getResultSecondIntegrateData(), context, fileName));
            saveUchastok = uchastok;
            saveNIzm = Integer.valueOf(nIzm) + 1;
        } else view.showToastMessage(context.getString(R.string.msg_data_not_found));
    }
}