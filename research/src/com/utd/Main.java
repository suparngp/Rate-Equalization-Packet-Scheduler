package com.utd;

public class Main {
    public static void main(String[] args) throws Exception{
        float totalCapacity = 10;
        int maxPacketCount = 2;
        float maxPacketLength = 10;
        int flowsCount = 4;
        Global.init(totalCapacity, maxPacketCount, true, 2, maxPacketLength, flowsCount);
        FlowGenerator flowGen = new FlowGenerator(flowsCount);
        flowGen.run();
        Global.cloneTraffic();
        new GPSScheduler3().run();
        Utils.debug("Traffic for RE: ", Global.queuesMapRE);
        new RateEqScheduler().run();
        Utils.debug("Traffic for VC2: ", Global.queuesMapVC2);
        new VC2Scheduler().run();
    }
}
