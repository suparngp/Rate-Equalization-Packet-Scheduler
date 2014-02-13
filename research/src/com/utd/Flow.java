package com.utd;

import java.util.Random;
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
    private TSGenerator tsGenerator;
    private float allocatedBandwidth;

    public Flow(int id, float offset){
        this.flowId = id;
        //this.offset = (new Random().nextFloat() * 1000) % 5;
        this.tsGenerator = new TSGenerator();
        this.offset = offset;
    }

    @Override
    public void run(){
        LinkedBlockingQueue<Packet> queue = Global.getQueue(this.flowId);

        //generate a limit of number of packets, this flow will generate. Using small numbers for simplicity.
        int maxPacketCount = 0;
        while(maxPacketCount < 1){
            maxPacketCount = (new Random().nextInt() % 3) * 1;
        }

        //create packets and add to the packet queue
        for(int i = 0; i < maxPacketCount; i++){
            Packet p = new Packet();
            p.setFlowId(this.flowId);
            p.setPacketId(currentPacketId);
            currentPacketId++;
            float ts = offset + tsGenerator.generateTimestamp();
            p.setArrivalTime(ts);
            p.setLength(80);
            p.setStartTime(0);
            p.setFinishTime(0);
            try{
                queue.put(p);
            }

            catch(InterruptedException ie){
                Utils.error(ie);
            }

        }
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

    @Override
    public String toString() {
        return "Flow{" +
                "flowId=" + flowId +
                ", weight=" + weight +
                ", minimumBandwidth=" + minimumBandwidth +
                ", offset=" + offset +
                ", currentPacketId=" + currentPacketId +
                //", tsGenerator=" + tsGenerator +
                ", allocatedBandwidth=" + allocatedBandwidth +
                '}';
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
        Flow clone = new Flow(this.flowId, this.offset);
        clone.allocatedBandwidth = this.allocatedBandwidth;
        clone.currentPacketId = this.currentPacketId;
        clone.flowId = this.flowId;
        clone.minimumBandwidth = this.minimumBandwidth;
        clone.offset = this.offset;
        clone.tsGenerator = this.tsGenerator.clone();
        clone.weight = this.weight;
        return clone;
    }
}
