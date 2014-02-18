package com.utd;

import com.sun.org.apache.xerces.internal.util.Status;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suparngupta on 2/12/14.
 */
public class GPSScheduler3 {
    HashMap<Flow, LinkedBlockingQueue<Packet>> queues = new HashMap<>();
    SortedSet<Packet> completed = new ConcurrentSkipListSet<>();
    List<Float> breakingPoints = new ArrayList<>();
    float currentTime = 0;

    public void run(){

        //add the first packet to the queue.
        Object[] next = Global.pollPacket();
        if(next == null){
            Utils.error("No traffic available to start the simulation");
            System.exit(0);
        }
        //add this new flow.
        //((Packet)next[1]).setStartTime(((Packet)next[1]).getArrivalTime());

        addFlow((Flow) next[0], (Packet) next[1]);
        adjustRates();

        next = Global.pollPacket();
        while(next != null){
            Flow f = (Flow)next[0];
            Packet p = (Packet)next[1];

           //clean up the queues till this arrival.
            //check if this packet adds a new flow.
            //if yes, then add this flow, adjust the rates
            //else simply add this packet to the flow's queue

            cleanup(p.getArrivalTime());
            if(queues.keySet().contains(f)){
               currentTime = p.getArrivalTime();
               addPacket(f, p);
            }

            else{
                addFlow(f, p);
                adjustRates();
            }
            next = Global.pollPacket();
        }
        Utils.log("No New packets, ending the simulation");
        cleanup(Float.MAX_VALUE);
        Utils.log("The completed Packets are: \n", completed);
        Utils.log("The breaking points are: \n", breakingPoints);
        Utils.log("........WFQ GPS Simulation Completed.........");
    }

    public void addFlow(Flow flow, Packet packet){
        LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
        try{
            currentTime = packet.getArrivalTime();
            packet.setStartTime(currentTime);
            q.put(packet);
            queues.put(flow, q);
        }

        catch(Exception e){
            Utils.error("Simulation Failed, Queues overflowed", e);
        }
    }

    public void adjustRates(){
        Utils.debug("At time: ", currentTime);
        Utils.debug("Before adjusting rates are ", queues.keySet());
        Set<Flow> flows = queues.keySet();
        float total = 0;
        for(Flow f: flows){
            total += f.getMinimumBandwidth();
        }

        Set<Flow> updated = new HashSet<>();
        for(Flow f: queues.keySet()){
            f.setWeight(f.getMinimumBandwidth() / total);
            f.setAllocatedBandwidth(f.getWeight() * Global.totalCapacity);
        }

        Utils.debug("After adjusting rates are ", queues.keySet());
        breakingPoints.add(currentTime);
    }

    public void cleanup(float limit){

        //find if any queue will become empty before this limit.
        //compute the finishing times of each queue
        while(true){
            Flow flow = null;
            float earliest = Float.MAX_VALUE;
            for(Flow f: queues.keySet()){
                LinkedBlockingQueue<Packet> q = queues.get(f);
                float totalRem = 0;
                for(Packet p: q){
                    totalRem += (p.getLength() - p.getTransmitted());
                }

                float finishingTime = currentTime + (totalRem / f.getAllocatedBandwidth());

                //if this is a breaking point
                if(finishingTime < limit && finishingTime < earliest){
                    flow = f;
                    earliest = finishingTime;
                }
            }

            Utils.debug("Earliest finishing queue before ", limit, " is ", flow , " at ", earliest);
            //no queues will become empty by this limit
            //so simply clear the queues till this limit
            if(flow == null){
                cleanupQueues(limit);
                currentTime = limit;
                break;
            }

            else{
                cleanupQueues(earliest);
                currentTime = earliest;
                queues.remove(flow);
                adjustRates();

            }
        }

    }

    public void cleanupQueues(float limit){
        for(Flow f: queues.keySet()){
            float lastFinishingTime = 0;
            while(true){
                Packet p = queues.get(f).peek();
                if(p == null){
                    continue;
                }
                float finishingTime = 0;
                if(lastFinishingTime == 0){
                    finishingTime = currentTime + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth());
                }
                else{
                    finishingTime = lastFinishingTime + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth());
                }

                Utils.debug("finshing time == limit ", finishingTime == limit);
                //this packet can leave
                if(finishingTime <= limit){
                    p = queues.get(f).poll();
                    p.setTransmitted(p.getLength());
                    p.setFinishTime(finishingTime);
                    completed.add(p);
                    //get the next packet
                    p = queues.get(f).peek();
                    if(p == null){
                        break;
                    }
                    p.setStartTime(finishingTime);
                    lastFinishingTime = finishingTime;
                }
                else{
                    if(lastFinishingTime == 0){
                        p.setTransmitted(p.getTransmitted() + (limit - currentTime) * f.getAllocatedBandwidth());
                    }
                    else{
                        p.setTransmitted(p.getTransmitted() + (limit - lastFinishingTime) * f.getAllocatedBandwidth());
                    }

                    p.setFinishTime(limit + (p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth());
                    break;
                }
            }
        }

        Utils.debug("At time ", limit, " after cleaning up the queues, ", queues);
    }

    public void addPacket(Flow f, Packet p){
        try{
            queues.get(f).put(p);
        }

        catch(Exception e){
            Utils.error("Queues Overflowed, Simulation Failed");
        }
    }
}
