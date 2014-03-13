package com.utd;

import java.util.Random;

/**
 * An exponential Random Number Generator
 * Created by suparngupta on 3/6/14.
 */
public class ExponentialRandom {
    private Random random;
    private double lambda;

    public ExponentialRandom(long seed, double lambda){
        this.random = new Random(seed + 21474836);
        this.lambda = lambda;
    }


    public double nextExp(){
        return -(1.0 / lambda) * Math.log(random.nextDouble());
    }
}
