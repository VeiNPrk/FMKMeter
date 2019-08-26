package com.example.fmkmeter;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CalculateAsyncTask extends AsyncTask<Integer, Void, Void> {

    public static final String TAG ="CalculateAsyncTask";
    private CalculateAsyncTask.CalculateAsyncTaskListener mListener;
    private List<Signal> data = null;
    private List<Signal> outData = null;
    //double delta = 1.1;
    int minMax=0;
    int min=0;
    int max=0;
    int deltaA = 0;
    int N = 2000;
    int kRes = 7;
    int delta = 0;
    public interface CalculateAsyncTaskListener {
        void onPostCalculateConcluded(List<Signal> outData, int minMaxValue, int min, int max, int average);
    }

    public CalculateAsyncTask(CalculateAsyncTaskListener listener) {
        //mAsyncTaskDao = dao;
        mListener = listener;
    }

    final public void setData(List<Signal> _data) {
        data = _data;
    }

    private boolean findCriticalTreshold(int iA, int deltaA, int delta){
        boolean tf=false;
        if(delta<=0)
            tf=iA</*deltaA+*/delta;
        else
            tf=iA>/*deltaA+*/delta;
        return tf;
    }

    private boolean findLastCriticalTreshold(int iA, int delta){
        boolean tf=false;
        if(delta<=0)
            tf=iA>delta;
        else
            tf=iA<delta;
        return tf;
    }

    private int findMinMax(List<Signal> data, int delta, int lastIndex){
        int minMax=-delta;
        min=deltaA;
        max=deltaA;
        for(int i = 0; i < lastIndex; i++){

            if (data.get(i).getValue() < min)
                min = data.get(i).getValue();
            if (data.get(i).getValue() > max)
                max = data.get(i).getValue();

            if(delta<0) {
                if (data.get(i).getValue() < minMax)
                    minMax = data.get(i).getValue();
            }
            else
                if(data.get(i).getValue()>minMax)
                    minMax=data.get(i).getValue();
        }
        return minMax;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        outData = new ArrayList<Signal>();
        if(data.size()<2000)
            N=data.size();
    }

    @Override
    protected Void doInBackground(Integer... params) {
        //entries.clear();
        delta = params[0];
        int findIndex=-1;
        int firstFindIndex=-1;
        int lastFindIndex=-1;
        int j=0;
        int summN=0;
        int k=0;
        for(int i = 0; i < N; i++){
            summN+=data.get(i).getValue();
        }
        deltaA = summN/N;

        for(int i = 0; i < data.size(); i++){
            int val = data.get(i).getValue()-deltaA;
            data.get(i).setValue(val);
        }

        Log.d(TAG, "deltaA="+deltaA+" delta="+delta);

        while(j<data.size() && findIndex<0){
            if(findCriticalTreshold(data.get(j).getValue(), deltaA, delta)){
                if(k==0)
                    firstFindIndex=j;
                k++;
                if(k==kRes)
                    findIndex=firstFindIndex;
            }
            else
                k=0;

            j++;
        }
        k=0;
        while(j<data.size() && lastFindIndex<0){
            if(findLastCriticalTreshold(data.get(j).getValue(), delta)){
                if(k==0)
                    firstFindIndex=j;
                k++;
                if(k==kRes)
                    lastFindIndex=firstFindIndex;
            }
            else
                k=0;

            j++;
        }

        if(findIndex>-1){
            int startIndex = findIndex-N;
            int finalIndex = findIndex+N;
            if(startIndex<0)
                startIndex=0;
            if(finalIndex>data.size())
                finalIndex=data.size();

            if(lastFindIndex<0 || lastFindIndex>data.size())
                lastFindIndex=finalIndex;

            lastFindIndex-=startIndex;


            for(int i = startIndex; i < finalIndex; i++)
                outData.add(data.get(i));

            minMax = findMinMax(outData, delta, lastFindIndex);
            /*if(delta>0)
                minMax-=deltaA;
            else
                minMax=deltaA-minMax;*/
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null)
            mListener.onPostCalculateConcluded(outData, minMax, min, max, deltaA);
    }
}
