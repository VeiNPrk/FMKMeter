package com.example.fmkmeter;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineChartClass {

    private static LineChart chart = null;
    private static LineData chartData = null;
    //private static ScatterData minMaxData = null;
    //private static LineData chartIntegrateData = null;
    private List<Entry> entries = null;
    private List<Entry> entriesFirstIntegrate = null;
    private List<Entry> entriesSecondIntegrate = null;
    private List<Entry> entriesMinMax = null;
    private List<Signal> data;
    private List<Signal> integrateFirstData;
    private List<Signal> integrateSecondData;
    int[] indexesMinMax;
    private LineChartClass.LoadChartListener mListener;
    private int startIndex = 0;
    private int stepChart = 0;
    private int lastIndex = 0;
    private boolean mIsVisibleFirstIntegrate = false;

    public interface LoadChartListener {
        //void onPreCalculateConclude();
        void onPostLoadChart(/*LineChart chart*/LineData chartData);
    }

    public LineChartClass(Context context, LineChartClass.LoadChartListener listener/*, boolean isVisibleFirstIntegrate*/) {
        chart = new LineChart(context);

        entries = new ArrayList<Entry>();
        entriesFirstIntegrate = new ArrayList<Entry>();
        entriesSecondIntegrate = new ArrayList<Entry>();
        entriesMinMax = new ArrayList<Entry>();
        data = new ArrayList<Signal>();
        integrateSecondData = new ArrayList<Signal>();
        integrateFirstData = new ArrayList<Signal>();
        mListener = listener;
        initChart();
    }

    public void setVisibleFirstIntegrate(boolean isVisibleFirstIntegrate){
        mIsVisibleFirstIntegrate = isVisibleFirstIntegrate;
    }

    public void setData(List<Signal> data, List<Signal> integrateFirstData, List<Signal> integrateSecondData, int[] indexesMinMax) {
        this.data = data;
        this.integrateFirstData = integrateFirstData;
        this.integrateSecondData=integrateSecondData;
        this.indexesMinMax = indexesMinMax;
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

    private void initDataChart(List<Entry> entries, List<Entry> integrateFirstEntries, List<Entry> integrateSecondEntries, List<Entry> minMaxEntries) {
        String nameDataSetA = "MainData A";
        String nameDataSetV = "Первое инт.V";
        String nameDataSetX = "Второе инт.X";
        if(minMaxEntries.size()>0){
            nameDataSetA = "MainData A/9.8";
            nameDataSetV = "Первое инт.V*10";
            nameDataSetX = "Второе инт.X*10";
        }

        LineDataSet dataSet = new LineDataSet(entries, nameDataSetA); // add entries to dataset
        LineDataSet dataSetSecondIntegrate = new LineDataSet(integrateSecondEntries, nameDataSetX); // add entries to dataset
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        dataSetSecondIntegrate.setColor(Color.BLACK);
        dataSetSecondIntegrate.setCircleColor(Color.BLACK);
        dataSetSecondIntegrate.setLineWidth(2);
        dataSetSecondIntegrate.setCircleRadius(2);
        if(mIsVisibleFirstIntegrate){
            LineDataSet dataSetFirstIntegrate = new LineDataSet(integrateFirstEntries, nameDataSetV); // add entries to dataset
            dataSetFirstIntegrate.setColor(Color.GREEN);
            dataSetFirstIntegrate.setCircleColor(Color.GREEN);
            dataSetFirstIntegrate.setLineWidth(2);
            dataSetFirstIntegrate.setCircleRadius(2);
            dataSets.add(dataSetFirstIntegrate);
        }
        LineDataSet dataSetMinMax = new LineDataSet(minMaxEntries, "MinMax"); // add entries to dataset
        dataSetMinMax.setColor(Color.YELLOW);
        dataSetMinMax.setCircleColor(Color.RED);
        dataSetMinMax.setLineWidth(0);
        //dataSetMinMax.
        dataSetMinMax.setValueTextSize(50);
        dataSetMinMax.setCircleRadius(6);

        /*ScatterDataSet dataSetMinMax = new ScatterDataSet(minMaxEntries, "MinMax"); // add entries to dataset
        dataSetMinMax.setScatterShapeHoleColor(Color.YELLOW);
        dataSetMinMax.setScatterShape(ScatterChart.ScatterShape.CIRCLE);
        dataSetMinMax.setScatterShapeHoleRadius(3f);*/

        dataSets.add(dataSetSecondIntegrate);
        dataSets.add(dataSetMinMax);
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

    public class ChartLoadAsyncTask extends AsyncTask<Void, Void, Void> {

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
                    entries.add(new Entry(data.get(i).getTime(), data.get(i).getValue()/9.8f));
                }
            } else
                entries.add(new Entry(1, 0));

            entriesSecondIntegrate.clear();
            if (integrateSecondData.size() > 0) {
                for (int i = startIndex; i < lastIndex; i++) {
                    entriesSecondIntegrate.add(new Entry(integrateSecondData.get(i).getTime(), integrateSecondData.get(i).getValue()*10));
                }
            } else
                entriesSecondIntegrate.add(new Entry(startIndex, 0));

            if(mIsVisibleFirstIntegrate){
                entriesFirstIntegrate.clear();
                if (integrateFirstData.size() > 0) {
                    for (int i = startIndex; i < lastIndex; i++) {
                        entriesFirstIntegrate.add(new Entry(integrateFirstData.get(i).getTime(), integrateFirstData.get(i).getValue()*10));
                    }
                } else
                    entriesFirstIntegrate.add(new Entry(startIndex, 0));
            }

            entriesMinMax.clear();
            if(indexesMinMax!=null) {
                entriesMinMax.add(new Entry(data.get(indexesMinMax[0]).getTime(), data.get(indexesMinMax[0]).getValue() / 9.8f));
                if (integrateFirstData.size() > 0)
                    entriesMinMax.add(new Entry(integrateFirstData.get(indexesMinMax[1]).getTime(), integrateFirstData.get(indexesMinMax[1]).getValue() * 10));
                if (integrateSecondData.size() > 0) {
                    entriesMinMax.add(new Entry(integrateSecondData.get(indexesMinMax[2]).getTime(), integrateSecondData.get(indexesMinMax[2]).getValue() * 10));
                    entriesMinMax.add(new Entry(integrateSecondData.get(indexesMinMax[3]).getTime(), integrateSecondData.get(indexesMinMax[3]).getValue() * 10));
                }
                if (integrateFirstData.size() > 0)
                    entriesMinMax.add(new Entry(integrateFirstData.get(indexesMinMax[3]).getTime(), integrateFirstData.get(indexesMinMax[3]).getValue() * 10));
            }
            initDataChart(entries, entriesFirstIntegrate, entriesSecondIntegrate, entriesMinMax);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mListener.onPostLoadChart(chartData);
        }
    }
}
