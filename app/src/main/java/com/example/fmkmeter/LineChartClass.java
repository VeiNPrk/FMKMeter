package com.example.fmkmeter;

import android.content.Context;
import android.os.AsyncTask;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartClass {

    private static LineChart chart = null;
    private static LineData chartData = null;
    private static List<Entry> entries = null;
    private static List<Signal> data;
    private LineChartClass.LoadChartListener mListener;
    private static int startIndex = 0;
    private static int stepChart = 0;
    private static int lastIndex = 0;

    public interface LoadChartListener {
        //void onPreCalculateConclude();
        void onPostLoadChart(/*LineChart chart*/LineData chartData);
    }

    public LineChartClass(Context context, LineChartClass.LoadChartListener listener) {
        chart = new LineChart(context);
        entries = new ArrayList<Entry>();
        data = new ArrayList<Signal>();
        mListener = listener;
        initChart();
    }

    public void setData(List<Signal> data) {
        this.data = data;
    }

    public void setInitSettings(int startIndex, int stepChart) {
        this.startIndex = startIndex*stepChart;
        this.stepChart = stepChart;
        lastIndex = this.startIndex+this.stepChart;
        if(lastIndex>data.size())
            lastIndex=data.size();
    }

    public void loadChart() {
        ChartLoadAsyncTask chartTask = new ChartLoadAsyncTask(mListener);
        chartTask.execute();
    }


    private void initChart() {
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.moveViewToX(0f);
        chart.animateY(500);
        chart.animateY(500);
    }

    private static void initDataChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "LineDataSet"); // add entries to dataset
        chartData = new LineData(dataSet);
    }

    public void removeAllLimitLines(LineChart mchart){
        YAxis leftAxis = mchart.getAxisLeft();
        leftAxis.removeAllLimitLines();
    }
    public void setRedLine(LineChart mchart, int value, String label){
        LimitLine avgLine = new LimitLine(value, label);
        avgLine.setLineWidth(4f);
        avgLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        avgLine.setTextSize(10f);
        YAxis leftAxis = mchart.getAxisLeft();
        /*leftAxis.removeAllLimitLines();*/ // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(avgLine);
    }

    public static class ChartLoadAsyncTask extends AsyncTask<Void, Void, Void> {

        //private SignalDao mAsyncTaskDao;
        private LoadChartListener mListener;

        public ChartLoadAsyncTask(/*SignalDao dao,*/ LoadChartListener listener) {
            //mAsyncTaskDao = dao;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            entries.clear();
            if (data.size() > 0) {
                for (int i = startIndex; i < lastIndex; i++) {
                    entries.add(new Entry(data.get(i).getTime(), data.get(i).getValue()));
                }
            } else
                entries.add(new Entry(1, 0));

            initDataChart(entries);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.onPostLoadChart(chartData);
        }
    }
}
