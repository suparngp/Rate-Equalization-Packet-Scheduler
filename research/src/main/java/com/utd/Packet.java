package com.utd;

/**
 * Defines a packet.
 * Created by suparngupta on 1/23/14.
 */
public class Packet implements Comparable, Cloneable{
    private int flowId;
    private float arrivalTime;
    private float startTime;
    private float finishTime;
    private int packetId;
    private float length;
    private float transmitted;

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public float getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(float arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public float getStartTime() {
        return startTime;
    }

    public void setStartTime(float startTime) {
        this.startTime = startTime;
    }

    public float getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(float finishTime) {
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "flowId=" + flowId +
                ", arrivalTime=" + arrivalTime +
                ", startTime=" + startTime +
                ", finishTime=" + finishTime +
                ", packetId=" + packetId +
                ", length=" + length +
                ", transmitted=" + transmitted +
                '}';
    }

    public int getPacketId() {
        return packetId;
    }

    public void setPacketId(int packetId) {
        this.packetId = packetId;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getTransmitted() {
        return transmitted;
    }

    public void setTransmitted(float transmitted) {
        this.transmitted = transmitted;
    }

    @Override
    public int compareTo(Object o) {
        Packet p = (Packet)o;
        if(this.getFinishTime() < p.getFinishTime()){
            return -1;
        }
        else if(this.getFinishTime() > p.getFinishTime()){
            return 1;
        }
        else{
            return 0;
        }
    }

    public Packet clone(){

        Packet clone = new Packet();
        clone.arrivalTime = this.arrivalTime;
        clone.finishTime = this.finishTime;
        clone.flowId = this.flowId;
        clone.length = this.length;
        clone.packetId = this.packetId;
        clone.startTime = this.startTime;
        clone.transmitted = this.transmitted;
        return clone;
    }
}
