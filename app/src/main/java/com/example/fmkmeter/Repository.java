package com.example.fmkmeter;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    //public static List<Integer> outData = new ArrayList<Integer>();

    private static MutableLiveData<List<Integer>> allData = new MutableLiveData<List<Integer>>();
    private static MutableLiveData<List<Signal>> endData = new MutableLiveData<List<Signal>>();
    private static LiveData<List<Signal>> endLiveData;
    private List<Signal> resultData = null;
    private List<Signal> resultIntegrateData = null;
    private SignalDao mSignalDao;

    Repository(Application application) {
        DBRoomSignal db = App.getInstance().getDatabase();
        //DBRoomSignal db = com.example.fmkmeter.App.getInstance().getDatabase();
        mSignalDao = db.signalDao();
        //endData
        //endData = mSignalDao.getAllSignalls();
        //mAllWords = mWordDao.getAlphabetizedWords();
    }

    public void initAllLiveData() {
        endLiveData = mSignalDao.getAllSignalls();
        Log.d("Repository", "initAllLiveData");
        //List<Signal> list = endLiveData.getValue();
        //endData=mSignalDao.getAllSignalls();
        //setEndData(endLiveData.getValue());
    }

    public static void setEndData(List<Signal> data) {
        endData.postValue(data);
    }

    public static MutableLiveData<List<Integer>> getAllData() {
        return allData;
    }

    public LiveData<List<Signal>> getEndData() {
        return endData;
    }

    public void setResultData(List<Signal> resultData) {
        this.resultData = resultData;
    }

    public void setResultIntegrateData(List<Signal> resultIntegrateData) {
        this.resultIntegrateData = resultIntegrateData;
    }

    public static LiveData<List<Signal>> getEndLiveData() {
        return endLiveData;
    }

    public List<Signal> getResultData() {
        return resultData;
    }

    public List<Signal> getResultIntegrateData() {
        return resultIntegrateData;
    }

    void saveDataToDb(InsertAsyncTaskListener mListener) {
        new InsertAsyncTask(mSignalDao, mListener).execute();
    }

    public interface InsertAsyncTaskListener {
        void onPostInsertExecute();
    }

    public static class InsertAsyncTask extends AsyncTask<Void, Void, Void> {

        private SignalDao mAsyncTaskDao;
        private InsertAsyncTaskListener mListener;

        InsertAsyncTask(SignalDao dao, InsertAsyncTaskListener listener) {
            mAsyncTaskDao = dao;
            mListener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //entries.clear();
            int j = 0;
            List<Integer> data = allData.getValue();
            List<Signal> signalData = new ArrayList<Signal>();
            if (data.size() > 0) {
                for (int i = 0; i < data.size() - 1; i = i + 2) {
                    float bt = (data.get(i) * 256 + data.get(i + 1))/**0.11f*/;
                    signalData.add(new Signal(j, bt));
                    j++;
                }
            } else
                signalData.add(new Signal(1, 0));

            mAsyncTaskDao.deleteAll();
            endData.postValue(new ArrayList<Signal>());
            Log.d("Repository", "endData post1");
            mAsyncTaskDao.insert(signalData);

            endData.postValue(signalData);
            Log.d("Repository", "endData post2");
            try {
                int i = endData.getValue().size();
            } catch (Exception ex) {
                int i = 0;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("Repository", "onPostExecute");
            mListener.onPostInsertExecute();
        }
    }

    void saveResult(Result result) {
        new InsertResultAsyncTask(mSignalDao).execute(result);
    }

    /*public interface InsertAsyncTaskListener {
        void onPostInsertExecute();
    }*/

    public static class InsertResultAsyncTask extends AsyncTask<Result, Void, Void> {

        private SignalDao mAsyncTaskDao;

        //private InsertAsyncTaskListener mListener;
        InsertResultAsyncTask(SignalDao dao) {
            mAsyncTaskDao = dao;
            //mListener = listener;
        }

        @Override
        protected Void doInBackground(Result... voids) {
            // mAsyncTaskDao.insertResult(voids[0]);
            Log.d("Repository", "InsertResultAsyncTask");
            return null;
        }
    }

}
