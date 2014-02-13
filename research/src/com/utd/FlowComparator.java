package com.utd;

import java.util.Comparator;

/**
 * Created by suparngupta on 2/12/14.
 */
public class FlowComparator implements Comparator<Flow>{
    @Override
    public int compare(Flow o1, Flow o2) {
        if(o1.getMinimumBandwidth() < o2.getMinimumBandwidth()){
            return -1;
        }
        else if(o1.getMinimumBandwidth() > o2.getMinimumBandwidth()){
            return 1;
        }
        else{
            return 0;
        }
    }
}
