package com.utd;

public class Main {
    public static void main(String[] args) throws Exception{
        float totalCapacity = 10;
        int maxPacketCount = 2;
        float maxPacketLength = 10;
        Global.init(totalCapacity, maxPacketCount, true, 2, maxPacketLength);
        FlowGenerator flowGen = new FlowGenerator(4);
        flowGen.run();
        Global.cloneTraffic();
        new GPSScheduler3().run();
        Utils.debug("Traffic for RE: ", Global.queuesMapRE);
        new RateEqScheduler().run();
    }
}
