package com.example.fmkmeter;

import java.util.ArrayList;
import java.util.Random;

public class RandomValueCreator {
    public static void createRandomSignals(){
        ArrayList<Integer> outData = new ArrayList<Integer>();
        int min = 10000;
        int max = 60000;
        int diff = max - min;
        Random random = new Random();
        int cnt = random.nextInt(diff + 1);
        cnt += min;
        for (int i = 0; i < cnt; i++){
            int minV = 50;
            int maxV = 100;
            int diffV = maxV - minV;
            int value =0;
            if(i>10000 && i<10040){
                value = 80+10000-i;
            }
            else {
                Random randomV = new Random();
                value= randomV.nextInt(diffV + 1);
                value += minV;
            }
            outData.add((int) value);
            Repository.getAllData().postValue(outData);
        }
    }
}
