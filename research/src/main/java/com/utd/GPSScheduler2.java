package com.utd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suparngupta on 2/3/14.
 */
public class GPSScheduler2 {
    private TSGenerator tsGenerator;
    private HashMap<Flow, Packet> currentPackets;
    private List<Flow> flows;
    private HashMap<Flow, LinkedBlockingQueue<Packet>> queuesMap;
    private List<Packet> completed;
    private float lastBreakingPoint = 0;
    private float currentTime = 0;
    float availableCapacity = 0, totalCapacity = Global.totalCapacity, minimumReserved = 0;

    public void run() {

        while (true) {


            //generate a packet
            HashMap<String, Object> next = Global.getNextPacket();
            Flow f = (Flow) next.get("flow");
            Packet p = (Packet) next.get("packet");

            if (!flows.contains(f)) {
                currentTime = p.getArrivalTime();
            }

        }

    }

    public void cleanPackets(float limit) {
        //while (currentTime < limit) {
        //pick the packet with least finishing time.
        Flow flow = null;
        Packet packet = null;
        float least = Float.MAX_VALUE;
        for (Flow f : currentPackets.keySet()) {
            Packet p = currentPackets.get(f);
            if (p.getFinishTime() < least) {
                packet = p;
                flow = f;
                least = p.getFinishTime();
            }
        }


        //here we have the least packet.
        //set the finishing times for other packets.
//                float duration = least - currentTime;
//                for(Flow f: currentPackets.keySet()){
//                    Packet p = currentPackets.get(f);
//                    p.setFinishTime(currentTime + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth()));
//                    p.setTransmitted(p.getTransmitted() + (f.getAllocatedBandwidth() * duration));
//                }
        computeFinishingTimes(least);

        //set current time to least finishing time.
        currentTime = least;

        //do one more pass to remove the packets from the queue.
        for (Flow f : currentPackets.keySet()) {
            Packet p = currentPackets.get(f);
            if (p.getFinishTime() == currentTime) {
                completed.add(p);
                currentPackets.put(f, null);
            }
        }

        //pick the candidates for enqueuing the next packet.
        //Time should be advanced to the earliest packet arriving for the empty queue.
        least = Float.MAX_VALUE;

        //while (true) {
        // boolean updated = false;
        for (Flow f : queuesMap.keySet()) {
            if (currentPackets.get(f) != null) {
                continue;
            }
            //updated = true;
            Packet p = queuesMap.get(f).peek();
            if (p.getArrivalTime() < least) {
                least = p.getArrivalTime();
                flow = f;
                packet = p;
            }
        }

//            if (!updated) {
//                break;
//            }
        //at this point we have the packet which arrived earliest.
        //check if this flow is a new flow.
        //if it is a new flow, then add the packet, adjust weights and compute the timestamps.
        if (!currentPackets.keySet().contains(flow)) {
            currentPackets.put(flow, packet);
            adjustRate();
            computeFinishingTimes(currentTime);
        }

        //this means this flow is not new.
        //check if this packet arrived before the currentTime.
        else if (packet.getArrivalTime() < currentTime) {
            packet = queuesMap.get(flow).poll();
            currentPackets.put(flow, packet);
        }

        //else this is a breaking point.
        //adjust the flow rates, remove the flowsGPS from the currentQueue.

        else {
            adjustRate();
            computeFinishingTimes(currentTime);
        }
        //}
        //}
    }

    public void adjustRate() {
        List<Flow> removed = new ArrayList<>();
        for (Flow f : currentPackets.keySet()) {
            if (currentPackets.get(f) != null) {
                continue;
            }

            removed.add(f);
        }

        for (Flow f : removed) {
            currentPackets.remove(f);
        }

        //get the total sum of reservation rates, least reservation rate and total number of flowsGPS.
        float totalCount = currentPackets.size();
        float least = Float.MAX_VALUE;
        float totalReservation = 0;
        for (Flow f : currentPackets.keySet()) {
            totalReservation += f.getMinimumBandwidth();
            if (f.getMinimumBandwidth() < least) {
                least = f.getMinimumBandwidth();
            }
        }

        //set the weights.
        for (Flow f : currentPackets.keySet()) {
            f.setWeight(f.getMinimumBandwidth() / totalReservation);
            f.setAllocatedBandwidth(f.getWeight() * totalCapacity);
        }
    }

    public void computeFinishingTimes(float limit) {
        float duration = limit - currentTime;
        for (Flow f : currentPackets.keySet()) {
            Packet p = currentPackets.get(f);
            p.setTransmitted(p.getTransmitted() + (f.getAllocatedBandwidth() * duration));
            p.setFinishTime(limit + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth()));
        }
    }

    public void runSimulation(){

        Flow flow = null;
        Packet packet = null;
        float least = Float.MAX_VALUE;
        for (Flow f : currentPackets.keySet()) {
            Packet p = currentPackets.get(f);
            if (p.getFinishTime() < least) {
                packet = p;
                flow = f;
                least = p.getFinishTime();
            }
        }

        for (Flow f : queuesMap.keySet()) {
            if (currentPackets.get(f) != null) {
                continue;
            }
            Packet p = queuesMap.get(f).peek();
            if (p.getArrivalTime() < least) {
                least = p.getArrivalTime();
                flow = f;
                packet = p;
            }
        }
        


    }
}
