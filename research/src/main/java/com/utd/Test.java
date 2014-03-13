package com.utd;

/**
 * Created by suparngupta on 3/2/14.
 */
public class Test {
    public static void main(String[] args){
        Flow flow = new Flow(1, 1048.576f, FlowType.UU_A_PR);

        flow.setupTS();
        for(int i = 0; i < 10; i++){
            System.out.println(flow.getTsGenerator().generateTimestamp());
        }

        ExponentialRandom r = new ExponentialRandom(1, 1048.576);
        System.out.println(r.nextExp());
    }
}
