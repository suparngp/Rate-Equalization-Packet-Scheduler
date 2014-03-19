package com.utd;

/**
 * From the implementation point of view, we need an entity which generates flowsGPS at random times.
 * Now we are not performing something in real time. We generate time stamps based on a certain distribution.
 * So to simulate the fact that the flowsGPS can come in at anytime, we can generate flowsGPS, and add a random offset
 * to the packet arrival times. So it will look like the flowsGPS came in after these many seconds.
 * Created by suparngupta on 1/25/14.
 */
public class FlowGenerator extends Thread{

    private int maxFlows = 50;
    private int flowId = 1;
    private float maxReservation = 0;

    public FlowGenerator(int maxFlows){
        this.maxFlows = maxFlows;
        this.maxReservation = Global.totalCapacity / 5;
    }

//    public void run(){
//        Random random = new Random();
//        float min = Float.MAX_VALUE;
//        for(int i = 0; i < this.maxFlows; i++){
//            float capacity = -1;
//
//            if(Global.isEqualCapacity){
//                capacity = Global.equalCapacity;
//            }
//            else{
//                while(capacity <= 0){
//                    capacity = (random.nextFloat() * 100) % this.maxReservation;
//                    int temp = (int)(capacity * 100.0);
//                    capacity = (float)((float)temp / 100.0);
//                }
//            }
//
//            if(capacity <= Global.availableCapacity){
//                Utils.debug("Capacity allowed " + capacity);
//                float delay = random.nextFloat() % 10.0f;
//                int temp = (int)(delay * 100.0);
//                delay = (float)((float)temp / 100.0);
//                Flow flow = new Flow(flowId, delay);
//                flowId++;
//                flow.setMinimumBandwidth(capacity);
//                flow.setAllocatedBandwidth(capacity);
//                Global.addFlow(flow);
//                //Global.adjustWeights();
//                //Global.adjustBandwidth();
//                Global.availableCapacity -= capacity;
//                flow.run();
//            }
//
//            else{
//                Utils.debug("Capacity not allowed " + capacity);
//            }
//        }
//        Utils.log(Global.flowsGPS);
//        Utils.debug(Global.queuesMapGPS);
//    }


    public void scenario1(){
        for(int i = 1; i <= 10; i++){
            float bw = i * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UA_A_PR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 11; i <= 20; i++){
            float bw = (i - 10) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.GREEDY);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        //Utils.log(Global.flowsGPS);
      Utils.log(Global.queuesMapGPS);
    }

    public void scenario2(){
        for(int i = 1; i <= 10; i++){
            float bw = i * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_A_CR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 11; i <= 20; i++){
            float bw = (i - 10) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.GREEDY);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        Utils.log(Global.flowsGPS);
        Utils.debug(Global.queuesMapGPS);
    }

    public void scenario3(){
        for(int i = 1; i <= 10; i++){
            float bw = i * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_A_PR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 11; i <= 20; i++){
            float bw = (i - 10) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_B_PR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 21; i <= 30; i++){
            float bw = (i - 20) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.GREEDY);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        //Utils.error("Remaning BW: ", Global.totalCapacity);
        Utils.log(Global.flowsGPS);
        Utils.debug(Global.queuesMapGPS);
    }

    public void scenario4(){
        for(int i = 1; i <= 10; i++){
            float bw = i * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_A_CR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 11; i <= 20; i++){
            float bw = (i - 10) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_B_CR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 21; i <= 30; i++){
            float bw = (i - 20) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.GREEDY);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        Utils.log(Global.flowsGPS);
        Utils.debug(Global.queuesMapGPS);
    }

    public void scenario5(){
        for(int i = 1; i <= 10; i++){
            float bw = i * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_UA_B_PR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 11; i <= 20; i++){
            float bw = (i - 10) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.GREEDY);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        Utils.log(Global.flowsGPS);
        Utils.debug(Global.queuesMapGPS);
    }

    public void scenario6(){
        for(int i = 1; i <= 10; i++){
            float bw = i * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.UU_UA_B_CR);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        for(int i = 11; i <= 20; i++){
            float bw = (i - 10) * 1024 * 1024 / Global.maxPacketLength;
            Flow flow = new Flow(i, bw, FlowType.GREEDY);
            flow.setMinimumBandwidth(bw);
            flow.setAllocatedBandwidth(bw);
            Global.addFlow(flow);
            Global.availableCapacity -= bw;
            flow.run();
        }

        Utils.log(Global.flowsGPS);
        Utils.debug(Global.queuesMapGPS);
    }
}
