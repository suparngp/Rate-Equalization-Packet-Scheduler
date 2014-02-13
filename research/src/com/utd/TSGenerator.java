package com.utd;

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
}
