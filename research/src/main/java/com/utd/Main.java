package com.utd;

public class Main {
    public static void main(String[] args) throws Exception{
        float totalCapacity = 165 * 1024 * 1024 / 1000;
        int maxPacketCount = 2;
        float maxPacketLength = 1000;
        int flowsCount = 30;
        Global.init(totalCapacity, maxPacketCount, true, 2, maxPacketLength, flowsCount);
        FlowGenerator flowGen = new FlowGenerator(flowsCount);
        flowGen.scenario6();
        Global.cloneTraffic();
        //new GPSScheduler3(6).start();
        //Utils.log("Traffic for RE: ", Global.queuesMapRE);
        new RateEqScheduler(6).start();
//        Utils.debug("Traffic for VC2: ", Global.queuesMapVC2);
        //new VC2Scheduler2().init().run();
    }
}
