package com.example.fmkmeter;

import android.os.AsyncTask;
import android.util.Log;

import com.ftdi.j2xx.FT_Device;

import java.util.ArrayList;
import java.util.List;

public class ReaderAsyncTask extends AsyncTask<Void, Void, Integer> {

    public static final String TAG="ReaderAsyncTask";
    FT_Device ftDev = null;
    int iavailable = 0;
    boolean bReadThreadGoing=true;
    boolean isSingle = false;
    boolean isSinglePost = false;
    byte[] readData;
    List<Integer> outData;
    public interface ReaderAsyncTaskListener {
        void onProgressConclude();
        void onPostExecuteConcluded(boolean isSingle);
        void onCanceled();
    }

    private ReaderAsyncTaskListener mListener;

    public ReaderAsyncTask(FT_Device dev, boolean single){
        ftDev = dev;
        isSingle=single;
        isSinglePost=isSingle;
        readData = new byte[MainActivity_old.readLength];
        //Repository.outData.clear();
        outData = new ArrayList<Integer>();
    }

    final public void setListener(ReaderAsyncTaskListener listener) {
        mListener = listener;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        //int singleK=0;
        outData.clear();
        try {
            while(true == bReadThreadGoing && !isCancelled())
            {
                Log.d(TAG, "Cancel="+isCancelled());
                Thread.sleep(MainActivity_old.DELAY_READ_THREAD);
                synchronized(ftDev)
                {
                    iavailable = ftDev.getQueueStatus();
                    Log.d("iavailable", "iavailable="+iavailable);

                    if (iavailable > 0) {
                        if(iavailable > MainActivity_old.readLength){
                            iavailable = MainActivity_old.readLength;
                        }

                        ftDev.read(readData, iavailable);
                        /*for (int i = 0; i < iavailable-1; i=i+2) {
                            int bt = readData[i] * 256 + readData[i + 1];
                            Repository.outData.add(bt);
                        }*/
                        for (int i = 0; i < iavailable; i++){
                            /*Repository.*/outData.add((int) readData[i]);
                            Repository.getAllData().postValue(/*Repository.*/outData);
                        }
                    }
                    //singleK++;
                    if(isSingle /*&& singleK>10*/){
                        isSingle=false;
                        bReadThreadGoing=false;
                    }
                }
                publishProgress();
            }
            publishProgress();
        } catch (InterruptedException e) {
            Log.d("tread", "Thread Interupt");
            publishProgress();
        }
        return /*Repository.*/outData.size();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        if (mListener != null)
            mListener.onProgressConclude();
    }

    @Override
    protected void onPostExecute(Integer aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null){
            mListener.onProgressConclude();
            mListener.onPostExecuteConcluded(isSinglePost);
        }
    }

    @Override
    protected void onCancelled(Integer aVoid) {
        super.onCancelled(aVoid);
        bReadThreadGoing=false;
        Log.d(TAG, "Cancel");
        if (mListener != null) {
            mListener.onProgressConclude();
            mListener.onCanceled();
        }

    }
}
