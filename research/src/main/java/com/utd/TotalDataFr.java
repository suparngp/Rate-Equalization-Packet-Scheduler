package com.utd;

/**
 * Created by suparngupta on 3/12/14.
 */
public class TotalDataFr {

    private int flowId;
    private double timestamp;
    private double totalData;
    private int totalPackets;

    public TotalDataFr clone(){
        TotalDataFr clone = new TotalDataFr();
        clone.flowId = this.flowId;
        clone.timestamp = this.timestamp;
        clone.totalData = this.totalData;
        clone.totalPackets = this.totalPackets;
        return clone;
    }

    public int getTotalPackets() {
        return totalPackets;
    }

    public void setTotalPackets(int totalPackets) {
        this.totalPackets = totalPackets;
    }

    public int getFlowId() {
        return flowId;
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public double getTotalData() {
        return totalData;
    }

    public void setTotalData(double totalData) {
        this.totalData = totalData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TotalDataFr)) return false;

        TotalDataFr that = (TotalDataFr) o;

        if (flowId != that.flowId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return flowId;
    }

    @Override
    public String toString() {
        return "TotalDataFr{" +
                "flowId=" + flowId +
                ", timestamp=" + timestamp +
                ", totalData=" + totalData +
                ", totalPackets=" + totalPackets +
                '}';
    }
}
