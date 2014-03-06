package com.utd;

/**
 * Defines a random number generator. Uses the code from previous class assignments.
 * 
 * @author Suparn Gupta
 */
public class RandomNumberGenerator {
    

    /**
     * Generates an array of exponential random numbers.
     * All the numbers need to be generated at once because the array is deterministic and not truly random.
     * @param param
     * @return the exponentialRandomNumbersArray
     */
    public static double[] generateExpRVArray(double param){
        double m = 2147483647;
        double k = 16807;
        double[] r = new double[1000000];
        double[] s = new double[1000000];
        s[0] = 1234;
        r[0] = (s[0] / m);
        for(int i = 1; i < 1000000; i++){
            s[i] = ((k * s[i - 1])% m);
            r[i] = s[i] / m;
        }
        
        double[] y = new double[1000000];
        double lambda = param;
        for(int i = 0; i < 1000000; i++){
            y[i] = -(1.0 / lambda) * Math.log(r[i]);
        }
        
        return y;
    }
}
