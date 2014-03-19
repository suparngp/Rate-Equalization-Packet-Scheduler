package com.utd;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suparngupta on 1/23/14.
 */
public class Flow extends Thread implements Cloneable{
    private int flowId;
    private float weight;
    private float minimumBandwidth;
    private float offset;
    private int currentPacketId = 1;

    private double currentTime;
    public TSGenerator getTsGenerator() {
        return tsGenerator;
    }

    public void setTsGenerator(TSGenerator tsGenerator) {
        this.tsGenerator = tsGenerator;
    }

    public int getCurrentPacketId() {
        return currentPacketId;
    }

    public void setCurrentPacketId(int currentPacketId) {
        this.currentPacketId = currentPacketId;
    }

    private TSGenerator tsGenerator;
    private float allocatedBandwidth;
    private float virtualClock = 0;
    private FlowType flowType;


    public Flow(int id, float minimumBw, FlowType flowType){
        this.flowId = id;
        //this.offset = (new Random().nextFloat() * 1000) % 5;

        this.offset = 0 ;
        this.flowType = flowType;
        this.minimumBandwidth = minimumBw;

    }


    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getMinimumBandwidth() {
        return minimumBandwidth;
    }

    public void setMinimumBandwidth(float minimumBandwidth) {
        this.minimumBandwidth = minimumBandwidth;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }


    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public float getAllocatedBandwidth() {
        return allocatedBandwidth;
    }

    public void setAllocatedBandwidth(float allocatedBandwidth) {
        this.allocatedBandwidth = allocatedBandwidth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Flow flow = (Flow) o;

        if (flowId != flow.flowId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return flowId;
    }

    @Override
    public Flow clone(){
        Flow clone = new Flow(this.flowId, this.offset, this.flowType);
        clone.allocatedBandwidth = this.allocatedBandwidth;
        clone.currentPacketId = this.currentPacketId;
        clone.flowId = this.flowId;
        clone.minimumBandwidth = this.minimumBandwidth;
        clone.offset = this.offset;
        clone.tsGenerator = this.tsGenerator.clone();
        clone.weight = this.weight;
        clone.virtualClock = this.virtualClock;
        clone.flowType = this.flowType;
        clone.currentTime = this.currentTime;
        return clone;
    }

    public float getVirtualClock() {
        return virtualClock;
    }

    public void setVirtualClock(float virtualClock) {
        this.virtualClock = virtualClock;
    }


    @Override
    public String toString() {
        return "Flow{" +
                "flowId=" + flowId +
                ", weight=" + weight +
                ", minimumBandwidth=" + minimumBandwidth +
                ", offset=" + offset +
                ", currentPacketId=" + currentPacketId +
                ", allocatedBandwidth=" + allocatedBandwidth +
                ", virtualClock=" + virtualClock +
                ", flowType=" + flowType +
                '}';
    }

    @Override
    public void run(){
//        LinkedBlockingQueue<Packet> queue = Global.getQueue(this.flowId);
//
//        double[] timestamps = null;
//
//        double rate = minimumBandwidth;
//
//        //if flow belongs to UA, group A and needs poisson arrival
//        switch(flowType){
//            case UA_A_PR:
//                timestamps = tsGenerator.generatePoissonTS(rate, Global.timeLimit, 1);
//
//                break;
//            case UA_A_CR:
//                timestamps = tsGenerator.generateConstantRateTS(rate, Global.timeLimit, 1);
//                break;
//            case UU_A_CR:
//                timestamps = tsGenerator.generateConstantRateTS(rate, Global.timeLimit, 1);
//                break;
//            case UU_A_PR:
//                timestamps = tsGenerator.generatePoissonTS(rate, Global.timeLimit, 1);
//                break;
//            case UU_B_CR:
//                timestamps = tsGenerator.generateConstantRateTS(rate, Global.timeLimit, 0.5);
//                break;
//            case UU_B_PR:
//                timestamps = tsGenerator.generatePoissonTS(rate, Global.timeLimit, 0.5);
//                break;
//            case UU_UA_B_PR:
//                timestamps = tsGenerator.generatePoissonTS(rate, Global.timeLimit, 0.5);
//                break;
//            case UU_UA_B_CR:
//                timestamps = tsGenerator.generateConstantRateTS(rate, Global.timeLimit, 0.5);
//                break;
//            case GREEDY:
//                timestamps = tsGenerator.generateGreedyTS(rate, Global.timeLimit, 10);
//                break;
//        }
//
//        Utils.log("Timestamps", timestamps.length);
//
//        //generate a limit of number of packets, this flow will generate. Using small numbers for simplicity.
////        int maxPacketCount = 0;
////        while(maxPacketCount < 1){
////            maxPacketCount = (new Random().nextInt() % Global.maxPacketCount) * 1;
////        }
//
//        for(double t : timestamps){
//            float ts = (float)t;
//            Packet p = new Packet();
//            p.setFlowId(this.flowId);
//            p.setPacketId(currentPacketId);
//            currentPacketId++;
//            p.setArrivalTime(ts);
//            p.setLength(Global.maxPacketLength);
//            p.setStartTime(0);
//            p.setFinishTime(0);
//            try{
//                queue.put(p);
//            }
//
//            catch(InterruptedException ie){
//                Utils.error(ie);
//            }
//        }
//
//
////        //create packets and add to the packet queue
////        for(int i = 0; i < maxPacketCount; i++){
////            Packet p = new Packet();
////            p.setFlowId(this.flowId);
////            p.setPacketId(currentPacketId);
////            currentPacketId++;
////            float ts = offset + tsGenerator.generateTimestamp();
////            p.setArrivalTime(ts);
////            p.setLength(Global.maxPacketLength);
////            p.setStartTime(0);
////            p.setFinishTime(0);
////            try{
////                queue.put(p);
////            }
////
////            catch(InterruptedException ie){
////                Utils.error(ie);
////            }
////
////        }


        //if flow belongs to UA, group A and needs poisson arrival

        setupTS();


        LinkedBlockingQueue<Packet> queue = Global.getQueue(this.flowId);
        for(int i = 0; i < 4000; i++){
            try{
                Packet p = createPacket();
                queue.put(p);
            }

            catch(Exception e){
                Utils.error("unable to generate initial packets", e);
            }

        }

    }


    public Packet createPacket(){
        Packet p = new Packet();
        p.setFlowId(this.flowId);
        p.setPacketId(currentPacketId);
        currentPacketId++;
        float ts = (float)this.tsGenerator.generateTimestamp();
        p.setArrivalTime(ts);
        p.setLength(Global.maxPacketLength);
        p.setStartTime(0);
        p.setFinishTime(0);
        return p;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(double currentTime) {
        this.currentTime = currentTime;
    }

    public void setupTS(){
        switch(flowType){
            case UA_A_PR:
                this.tsGenerator = new TSGenerator(this, 1);

                break;
            case UA_A_CR:
                this.tsGenerator = new TSGenerator(this, 1);
                break;
            case UU_A_CR:
                this.tsGenerator = new TSGenerator(this, 1);
                break;
            case UU_A_PR:
                this.tsGenerator = new TSGenerator(this, 1);
                break;
            case UU_B_CR:
                this.tsGenerator = new TSGenerator(this, 0.5);
                break;
            case UU_B_PR:
                this.tsGenerator = new TSGenerator(this, 0.5);
                break;
            case UU_UA_B_PR:
                this.tsGenerator = new TSGenerator(this, 0.5);
                break;
            case UU_UA_B_CR:
                this.tsGenerator = new TSGenerator(this, 0.5);
                break;
            case GREEDY:
                this.tsGenerator = new TSGenerator(this, 1);
                break;
        }

    }

}
