package com.example.fmkmeter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartClass {

    private static LineChart chart = null;
    private static LineData chartData = null;
    //private static LineData chartIntegrateData = null;
    private static List<Entry> entries = null;
    private static List<Entry> entriesIntegrate = null;
    private static List<Signal> data;
    private static List<Signal> integrateData;
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
        entriesIntegrate = new ArrayList<Entry>();
        data = new ArrayList<Signal>();
        integrateData = new ArrayList<Signal>();
        mListener = listener;
        initChart();
    }

    public void setData(List<Signal> data, List<Signal> integrateData) {

        this.data = data;
        this.integrateData=integrateData;
    }

    public void setInitSettings(int startIndex, int stepChart) {
        this.startIndex = startIndex * stepChart;
        this.stepChart = stepChart;
        lastIndex = this.startIndex + this.stepChart;
        if (lastIndex > data.size())
            lastIndex = data.size();
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

    private static void initDataChart(List<Entry> entries, List<Entry> integrateEntries) {
        LineDataSet dataSet = new LineDataSet(entries, "LineDataSet"); // add entries to dataset
        LineDataSet dataSetIntegrate = new LineDataSet(integrateEntries, "LineDataSetIntegrate"); // add entries to dataset
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        dataSetIntegrate.setColor(Color.BLACK);
        dataSetIntegrate.setCircleColor(Color.BLACK);
        dataSetIntegrate.setLineWidth(2);
        dataSetIntegrate.setCircleRadius(2);
        dataSets.add(dataSetIntegrate);
        chartData = new LineData(dataSets);
        //chartIntegrateData = new LineData(dataSetIntegrate);
    }

    public void removeAllLimitLines(LineChart mchart) {
        YAxis leftAxis = mchart.getAxisLeft();
        leftAxis.removeAllLimitLines();
    }

    public void setRedLine(LineChart mchart, float value, String label) {
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

            entriesIntegrate.clear();
            if (integrateData.size() > 0) {
                for (int i = startIndex; i < lastIndex; i++) {
                    entriesIntegrate.add(new Entry(integrateData.get(i).getTime(), integrateData.get(i).getValue()));
                }
            } else
                entriesIntegrate.add(new Entry(startIndex, 0));

            initDataChart(entries, entriesIntegrate);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.onPostLoadChart(chartData);
        }
    }
}
