package com.example.fmkmeter;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CalculateAsyncTaskNew extends AsyncTask<Integer, Void, Void> {

    public static final String TAG = "CalculateAsyncTask";
    private CalculateAsyncTaskNew.CalculateAsyncTaskNewListener mListener;
    private List<Signal> data = null;
    private List<Signal> outData = null;
    private List<Signal> outIntegrateFirstData = null;
    private List<Signal> outIntegrateSecondData = null;
    //double delta = 1.1;
    float minMax = 0;
    int[] indexesMinMax = {0, 0, 0, 0};
    float minMaxSecondIntegrate = 0;
    float min = 0;
    float max = 0;
    float deltaA = 0;
    int N = 2000;
    int NLast = 2000;
    int kRes = 7;
    int delta = 0;
    float dt=0.000012f;
    boolean tfIntegrate = false;
    //float koef=0.11f;

    public interface CalculateAsyncTaskNewListener {
        void onPostCalculateNewConcluded(List<Signal> outData, List<Signal> outIntegrateFirstData, List<Signal> outIntegrateSecondData, float minMaxValue, float minMaxIntegrateValue, int[] indexesMinMax/*float min, float max, float average*/);
    }

    public CalculateAsyncTaskNew(CalculateAsyncTaskNewListener listener, int cntN, boolean tf) {
        mListener = listener;
        NLast = cntN;
        tfIntegrate = tf;
    }

    final public void setData(List<Signal> _data) {
        data = _data;
    }

    private boolean findCriticalTreshold(float iA, /*float deltaA,*/ int delta) {
        boolean tf = false;
        if (delta <= 0)
            tf = iA </*deltaA+*/delta;
        else
            tf = iA >/*deltaA+*/delta;
        return tf;
    }

    private boolean findLastCriticalTreshold(float iA, float delta) {
        boolean tf = false;
        if (delta <= 0)
            tf = iA > delta;
        else
            tf = iA < delta;
        return tf;
    }

    private float findMinMax(List<Signal> data, float delta, int lastIndex) {
        float minMax = -delta;
        min = deltaA;
        max = deltaA;
        for (int i = 0; i < lastIndex; i++) {

            if (data.get(i).getValue() < min)
                min = data.get(i).getValue();
            if (data.get(i).getValue() > max)
                max = data.get(i).getValue();

            if (delta < 0) {
                if (data.get(i).getValue() < minMax)
                    minMax = data.get(i).getValue();
            } else if (data.get(i).getValue() > minMax)
                minMax = data.get(i).getValue();
        }
        return minMax;
    }
    private float findMinMaxIntegrate(List<Signal> data, float delta, int lastIndex) {
        float minMax = data.get(0).getValue();
        for (int i = 0; i < lastIndex; i++) {
            if (delta < 0) {
                if (data.get(i).getValue() < minMax)
                    minMax = data.get(i).getValue();
            } else if (data.get(i).getValue() > minMax)
                minMax = data.get(i).getValue();
        }
        return minMax/1000;
    }

    private void findIndexesMinMax(List<Signal> data, List<Signal> firstIntegrateData){
        float porog = -50;
        int indexPorog = 0;
        while(indexPorog<data.size() && data.get(indexPorog).getValue() > porog)
            indexPorog++;

        int index = indexPorog;
        float min = data.get(index).getValue();
        int minIndex = -1;
        for (int i = index; i < data.size(); i++) {
            if(data.get(i).getValue() < min){
                min = data.get(i).getValue();
                minIndex = i;
            }
        }
        if(minIndex>-1)
            indexesMinMax[0] = minIndex;
        while(index<data.size()-1 && data.get(index).getValue() * data.get(index+1).getValue() > 0)
            index++;

        indexesMinMax[1] = index;
        while(index<firstIntegrateData.size()-1 && firstIntegrateData.get(index).getValue() * firstIntegrateData.get(index+1).getValue() > 0)
            index++;

        indexesMinMax[2] = index;
        while(index<data.size()-1 && data.get(index).getValue() * data.get(index+1).getValue() > 0)
            index++;

        indexesMinMax[3] = index;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        outData = new ArrayList<Signal>();
        outIntegrateFirstData = new ArrayList<Signal>();
        outIntegrateSecondData = new ArrayList<Signal>();
        if (data.size() < 2000)
            N = data.size();
    }

    @Override
    protected Void doInBackground(Integer... params) {
        //entries.clear();
        delta = params[0];
        int findIndex = -1;
        int firstFindIndex = -1;
        int lastFindIndex = -1;
        int j = 0;
        float summN = 0;
        int k = 0;
        for (int i = 0; i < N; i++) {
            summN += data.get(i).getValue();
        }
        deltaA = summN / N;
/*НА время тестирования с внешними данными*/
        for (int i = 0; i < data.size(); i++) {
            float val = data.get(i).getValue() - deltaA;
            data.get(i).setValue(val);
        }

        Log.d(TAG, "deltaA=" + deltaA + " delta=" + delta);

        while (j < data.size() && findIndex < 0) {
            if (findCriticalTreshold(data.get(j).getValue(), /*deltaA,*/ delta)) {
                if (k == 0)
                    firstFindIndex = j;
                k++;
                if (k == kRes)
                    findIndex = firstFindIndex;
            } else
                k = 0;

            j++;
        }
        k = 0;
        while (j < data.size() && lastFindIndex < 0) {
            if (findLastCriticalTreshold(data.get(j).getValue(), delta)) {
                if (k == 0)
                    firstFindIndex = j;
                k++;
                if (k == kRes)
                    lastFindIndex = firstFindIndex;
            } else
                k = 0;

            j++;
        }

        if (findIndex > -1) {
            int startIndex = findIndex - N;
            int finalIndex = findIndex + NLast;
            if (startIndex < 0)
                startIndex = 0;
            if (finalIndex > data.size())
                finalIndex = data.size();

            if (lastFindIndex < 0 || lastFindIndex > data.size())
                lastFindIndex = finalIndex;

            lastFindIndex -= startIndex;

            for (int i = startIndex; i < finalIndex; i++)
                outData.add(new Signal(data.get(i).getTime(), (data.get(i).getValue() * 9.8f / 450)));

            if(tfIntegrate) {
                outIntegrateFirstData = doIntegrateFirst(outData);
                outIntegrateSecondData = doIntegrateSecond(outIntegrateFirstData);
                for (int i = 0; i < outData.size(); i++) {
                    float /*val = outData.get(i).getValue();
                    outData.get(i).setValue(val);
                    val = outIntegrateFirstData.get(i).getValue();
                    outIntegrateFirstData.get(i).setValue(val);*/
                    val = outIntegrateSecondData.get(i).getValue()*1000;
                    outIntegrateSecondData.get(i).setValue(val);
                }
                findIndexesMinMax(outData, outIntegrateFirstData);
                minMaxSecondIntegrate = findMinMaxIntegrate(outIntegrateSecondData, delta, lastFindIndex);
            }
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
            mListener.onPostCalculateNewConcluded(outData, outIntegrateFirstData, outIntegrateSecondData, minMax, minMaxSecondIntegrate, indexesMinMax/*min, max, deltaA*/);
    }

    private List<Signal> doIntegrateFirst(List<Signal> data){
        List<Signal> integrateData = data;
        List<Signal> integrateOutData = new ArrayList<Signal>();
        float summN=0;
        int integrateN=N/2;
        float deltaIntegrate = 0;

        float v0 = 0;
        integrateOutData.clear();
        integrateOutData.add(new Signal(integrateData.get(0).getTime(), v0));
        for (int i = 0; i < integrateData.size()-1; i++) {
            float val = integrateOutData.get(i).getValue() + integrateData.get(i+1).getValue()/42000;
            integrateOutData.add(new Signal(integrateData.get(i+1).getTime(), val));
        }

        for (int i = 0; i < integrateN; i++) {
            summN += integrateOutData.get(i).getValue();
        }
        deltaIntegrate = summN / integrateN;

        for (int i = 0; i < integrateOutData.size(); i++) {
            float val = integrateOutData.get(i).getValue() - deltaIntegrate;
            integrateOutData.get(i).setValue(val);
        }
        return integrateOutData;
    }

    private List<Signal> doIntegrateSecond(List<Signal> data){
        List<Signal> integrateData = data;
        List<Signal> integrateOutData = new ArrayList<Signal>();
        //integrateData=data;
        /*float summN=0;
        int integrateN=N/2;
        float deltaIntegrate = 0;
        for (int i = 0; i < integrateN; i++) {
            summN += integrateData.get(i).getValue();
        }
        deltaIntegrate = summN / integrateN;

        for (int i = 0; i < integrateData.size(); i++) {
            float val = integrateData.get(i).getValue() - deltaIntegrate;
            integrateData.get(i).setValue(val);
        }*/
        float v0 = 0;
        integrateOutData.clear();
        integrateOutData.add(new Signal(integrateData.get(0).getTime(), v0));
        for (int i = 0; i < integrateData.size()-1; i++) {
            float val = integrateOutData.get(i).getValue() + integrateData.get(i+1).getValue()/42000/**dt/**dt/**1000*/;
            integrateOutData.add(new Signal(integrateData.get(i+1).getTime(), val));
        }
        /*for (int i = 0; i < integrateOutData.size(); i++) {
            float val = integrateOutData.get(i).getValue()*dt*dt*1000000;
            integrateOutData.get(i).setValue(val);
        }*/
        return integrateOutData;
    }

}
