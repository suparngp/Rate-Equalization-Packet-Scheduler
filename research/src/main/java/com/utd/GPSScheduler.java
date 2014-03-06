package com.utd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suparngupta on 1/23/14.
 */
public class GPSScheduler {
    private TSGenerator tsGenerator;
    private HashMap<Flow, Packet> currentPackets;
    private List<Flow> flows;
    private HashMap<Flow, LinkedBlockingQueue<Packet>> queuesMap;
    private List<Packet> completed;
    private float lastBreakingPoint = 0;
    private float currentTime = 0;

    float availableCapacity = 0, totalCapacity = Global.totalCapacity, minimumReserved = 0;

    public GPSScheduler(){
        tsGenerator = new TSGenerator();
        currentPackets = new HashMap<>();
        flows = new ArrayList<>();
        queuesMap = new HashMap<>();
        completed = new ArrayList<>();
    }
    public void run() throws Exception{
        while(true){
            HashMap<String, Object> next = Global.getNextPacket();
            if(next == null){
                Utils.log("Simulation Completed");
                Utils.log(completed);
                System.exit(0);
                return;
            }

            //here if this packet belongs to a new flow
            //add this new flow, adjust the weights.
            //this is a breaking point.

            /*
            * However we need to see what has happened before this packet arrived.
            * The only event which can happen is a departure.
            * Or otherwise the queue would remain same.
            * We can check if a departure happened by checking the finishing times of the packets in the
            * currently served packets queue. If a packet has a finishTime < arrivalTime of new packet,
            * Then that packet can simply be removed from current and added to a completed list.
            *
            * For each packet departure,
            * We must enqueue a new packet from the same flow.
            * If no such packet exists then there is abreaking point. We remove that flow and update weights
            * and BW. Otherwise we simply add the packet to the flow's queue.
            * We remove the packets with earliest finishing times first.
            * */
            Flow flow = (Flow)next.get("flow");
            Packet packet = (Packet)next.get("packet");
            //packet.setArrivalTime(tsGenerator.generateTimestamp());


            if(isNewFlow(flow)){
                departPackets(packet.getArrivalTime());
                currentTime = packet.getArrivalTime();
                addFlow(flow, packet);
            }
            else {
                queuesMap.get(flow).put(packet);
                departPackets(packet.getArrivalTime());
                currentTime = packet.getArrivalTime();
            }
        }
    }

    private float computeFinishTime(Packet p){
        Flow flow = getFlow(p.getFlowId());
        float finishingTime = currentTime + (p.getLength() - p.getTransmitted()) / flow.getAllocatedBandwidth();
        return finishingTime;
    }

    public void adjustTransmitted(float currentBreakingPoint){
        for(Flow f: currentPackets.keySet()){
            Packet p = currentPackets.get(f);
            float rem = p.getLength() - p.getTransmitted();
            p.setTransmitted(p.getTransmitted() + (f.getAllocatedBandwidth() * (currentBreakingPoint - lastBreakingPoint)));
        }
    }

    public  void adjustWeights(){
        float min = Float.MAX_VALUE;
        minimumReserved = 0;
        for(int i = 0; i < flows.size(); i++){
            minimumReserved += flows.get(i).getMinimumBandwidth();
        }

        //update available capacity
        availableCapacity = totalCapacity - minimumReserved;

        for(int i = 0; i < flows.size(); i++){
            flows.get(i).setWeight(flows.get(i).getMinimumBandwidth()/minimumReserved);
        }
    }

    public void adjustBandwidth(){
        for(int i = 0; i < flows.size(); i++){
            Flow flow = flows.get(i);
            flow.setAllocatedBandwidth(totalCapacity * flow.getWeight());
        }
    }

    public void adjustFinishingTimes(){
        for(Flow f: currentPackets.keySet()){
            Packet p = currentPackets.get(f);
            p.setFinishTime(p.getArrivalTime() + p.getFinishTime() + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth()));
        }
    }


    public boolean isNewFlow(Flow flow){
        for(int i = 0; i < flows.size(); i++){
            if(flows.get(i).getFlowId() == flow.getFlowId()){
                return false;
            }
        }
        return true;
    }

    public  void addFlow(Flow flow, Packet packet) {
        adjustTransmitted(packet.getArrivalTime());
        availableCapacity -= flow.getMinimumBandwidth();
        flows.add(flow);
        LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();
        queuesMap.put(flow, queue);
        packet.setStartTime(packet.getArrivalTime());
        currentPackets.put(flow, packet);
        lastBreakingPoint = packet.getArrivalTime();
        adjustWeights();
        adjustBandwidth();
        adjustFinishingTimes();
    }

    public  void removeFlow(Flow flow, float arr) {
        for (int i = 0; i < flows.size(); i++) {
            if (flows.get(i).getFlowId() == flow.getFlowId()) {
                adjustTransmitted(arr);
                flows.remove(i);
                queuesMap.remove(flow);
                currentPackets.remove(flow);
                availableCapacity += flow.getMinimumBandwidth();
                adjustWeights();
                adjustBandwidth();
                adjustFinishingTimes();
                break;
            }
        }
    }

    public  Flow getFlow(int id){
        for(int i = 0; i < flows.size(); i++){
            if(flows.get(i).getFlowId() == id){
                return flows.get(i);
            }
        }

        return null;
    }
    /*
    * Remove the packets from the queue one by one before the maxFinishingTime.
    * Packets with earlier finishing times depart first,
    * then weights and bandwidths are updated.
    * */
    private void departPackets(float maxFinishingTime){
        while(true){
            Packet p = null;
            Flow f = null;
            float earliest = Float.MAX_VALUE;
            HashSet<Flow> candidates = new HashSet<>();
            for(Flow flow: currentPackets.keySet()){
                //if this a
                if(currentPackets.get(flow).getFinishTime() < maxFinishingTime
                        && currentPackets.get(flow).getFinishTime() < earliest){
                    f = flow;
                    p = currentPackets.get(f);
                    earliest = p.getFinishTime();
                }
            }

            //no more packets to depart;
            if(p == null){
                Utils.debug("Breaking");
                break;
            }
            currentTime = p.getFinishTime();
            p.setTransmitted(80);
            completed.add(p);
            //now check if this flow has another packet with arrTime < maxFinishingTime
            //If yes, then enqueue the packet, compute the finishing time,
            //if not, then remove the flow, adjust the weights and BW.

            System.out.println(f);
            if(!queuesMap.get(f).isEmpty() && queuesMap.get(f).peek().getArrivalTime() < maxFinishingTime){
                Packet newPacket = queuesMap.get(f).poll();
                float start = Math.max(p.getFinishTime(), newPacket.getArrivalTime());
                computeFinishTime(newPacket);
                newPacket.setStartTime(start);
                currentPackets.put(f, newPacket);
            }

        /*
        * This is a breaking point. Save it and update the times.
        * */
            else{
                removeFlow(f, maxFinishingTime);
                Utils.debug(queuesMap, currentPackets);
                adjustTransmitted(p.getFinishTime());
                adjustWeights();
                adjustBandwidth();
                adjustFinishingTimes();
                lastBreakingPoint = p.getFinishTime();
            }
        }


    }
}
