package com.utd;

/**
 * Main Class to drive the program.
 * Configuration parameters which can be setup are:
 * 1. total capacity of the output channel.
 * 2. Maximum packet length assuming constant packet size.
 * 3. Maximum number of flows in the system.
 * */
public class Main {
    public static void main(String[] args) throws Exception{

        //configuration parameters
        float totalCapacity = 165 * 1024 * 1024 / 1000;
        float maxPacketLength = 1000;
        int flowsCount = 30;

        //initialize the system.
        Global.init(totalCapacity, maxPacketLength, flowsCount);

        //generate all the flows
        FlowGenerator flowGen = new FlowGenerator(flowsCount);

        //select the simulation scenario
        flowGen.scenario6();

        //clone the initial traffic for all the three algorithms.
        Global.cloneTraffic();

        //Run the WFQ scehduler
//        new WFQScheduler2(6).start();

        // Run the Rate equalization scheduler
        new RateEqScheduler2(6).start();

        //Run the DualModeScheduler Scehduler
        //new DualModeScheduler(6).init().start();
    }
}
