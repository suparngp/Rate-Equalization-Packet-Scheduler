package com.utd;

public class Main {
    public static void main(String[] args) throws Exception{
        float totalCapacity = 10;
        Global.init(totalCapacity);
        FlowGenerator flowGen = new FlowGenerator(2);
        flowGen.run();
        Global.cloneTraffic();
        new GPSScheduler3().run();
        Utils.debug("Traffic for RE: ", Global.queuesMapRE);
        new RateEqScheduler().run();
    }
}
