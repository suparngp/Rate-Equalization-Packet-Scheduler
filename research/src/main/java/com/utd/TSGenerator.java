package com.utd;

import java.util.ArrayList;
import java.util.Random;

/**
 * Time stamp generator for flowsGPS
 * Generating the constant timestamps
 * Created by suparngupta on 1/23/14.
 */
public class TSGenerator implements Cloneable{
    private float currentTimestamp = 0;
    private Random random = new Random();

    public float generateTimestamp() {
        currentTimestamp += 1;
        return currentTimestamp;
    }

    @Override
    public TSGenerator clone(){
        TSGenerator clone = new TSGenerator();
        clone.currentTimestamp = this.currentTimestamp;
        clone.random = this.random;
        return clone;
    }


    public double[] generatePoissonTS(double rate, double limit, double ratio){
        rate *= ratio;
        double[] ts = RandomNumberGenerator.generateExpRVArray(rate);
        int counter = 0;
        double current = ts[0];
        for(double t: ts){
            if(current > limit){
                break;
            }
            counter++;
            current += ts[counter];
        }
        double[] result = new double[counter];
        current = 0;
        int i = -1;
        while(i+ 1 < counter){
            result[i + 1] = current;
            i++;
            current += ts[i];
        }
        return result;
    }

    public double[] generateConstantRateTS(double rate, double limit, double ratio){
        rate *= ratio;

        double increment = 1 / rate;
        ArrayList<Double> ts = new ArrayList<>();
        double current = 0;
        while(current < limit){
            ts.add(current);
            current += increment;
        }
        double[] result = new double[ts.size()];

        for(int i = 0; i < result.length; i++){
            result[i] = ts.get(i);
        }
        return result;
    }

    public double[] generateGreedyTS(double rate, double limit, double ratio){

        return generateConstantRateTS(rate, limit, ratio);
    }

}
