package com.utd;

/**
 * Created by suparngupta on 3/2/14.
 */
public class Test {
    public static void main(String[] args){
        Flow flow = new Flow(1, 10485.76f, FlowType.UU_A_PR);

        flow.setupTS();

        double ts = flow.getTsGenerator().generateTimestamp();
        int counter = 1;

        while(ts <= 0.28){
            ts = flow.getTsGenerator().generateTimestamp();
            counter++;
        }

        System.out.println(counter);
//        for(int i = 0; i < 10; i++){
//            System.out.println(flow.getTsGenerator().generateTimestamp());
//        }
//
//        ExponentialRandom r = new ExponentialRandom(1, 1048.576);
//        System.out.println(r.nextExp());
    }
}
