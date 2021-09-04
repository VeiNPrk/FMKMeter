package com.example.fmkmeter;

import android.content.Context;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

public interface ChartContractor {
     interface View{
          void showProgressBar(boolean tf);

          void showNextButton(boolean tf);

          void showPrewButton(boolean tf);

          void showChartForm(boolean tf);

          void setChart(LineData data);

          void setSizeData(int countData);

          void setTVIndexChart(int indexChart, int lastIndexChart);

         void setMinMax(String minMax, String minMaxIntegrate);

          boolean getSwitchedResult();

          void showMessageForm(boolean tf, String msg);

          void showToastMessage(String msg);

          void setSeekBarStep(int progress);

          void setSeekBarDelta(int progress);

          void setSwitch(boolean isChecked);

         LineChart getChart();

         void showDialog();

         void runTimePermissions();
     } 

     interface Presenter<V extends ChartContractor.View>{

          void setContext(Context context);

          void setRepository(Repository repository, LifecycleOwner lifecycleOwner, boolean isNewInstance);

          void attachLifecycle(Lifecycle lifecycle);

          void detachLifecycle(Lifecycle lifecycle);
  
          void attachView(V view);
  
          void detachView();

          void clearFragment();

          V getView();

          void setSwitchChartValue(boolean isChecked);
  
          boolean isViewAttached();

          void setSeekBarStep(int progress, int step);

          void setSeekBarDelta(int progress, int delta);
  
          void onPresenterDestroy();

          void nextSetOnClick();

          void prewSetOnClick();

          void loadDataOnClick();

          void saveFileOnClick();

         DialogFragment getSaveDialog();

     }
}
