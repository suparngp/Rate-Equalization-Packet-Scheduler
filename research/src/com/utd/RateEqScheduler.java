package com.utd;

import com.sun.org.apache.xerces.internal.util.Status;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The original Rate Equalization Scheduler
 * Created by suparngupta on 2/12/14.
 */
public class RateEqScheduler {
    HashMap<Flow, LinkedBlockingQueue<Packet>> queues = new HashMap<>();
    List<Packet> completed = new ArrayList<>();
    List<Float> breakingPoints = new ArrayList<>();
    float currentTime = 0;

    public void run() {

        //add the first packet to the queue.
        Object[] next = Global.pollPacketRE();
        if (next == null) {
            Utils.error("No traffic available to start the simulation");
            System.exit(0);
        }
        //add this new flow.
        //((Packet)next[1]).setStartTime(((Packet)next[1]).getArrivalTime());

        addFlow((Flow) next[0], (Packet) next[1]);
        adjustRates();

        next = Global.pollPacketRE();
        while (next != null) {
            Flow f = (Flow) next[0];
            Packet p = (Packet) next[1];

            //clean up the queues till this arrival.
            //check if this packet adds a new flow.
            //if yes, then add this flow, adjust the rates
            //else simply add this packet to the flow's queue

            cleanup(p.getArrivalTime());
            if (queues.keySet().contains(f)) {
                currentTime = p.getArrivalTime();
                addPacket(f, p);
            } else {
                addFlow(f, p);
                adjustRates();
            }
            next = Global.pollPacketRE();
        }
        Utils.log("No New packets, ending the simulation");
        cleanup(Float.MAX_VALUE);
        Utils.log("The completed Packets are: \n", completed);
        Collections.sort(breakingPoints);
        Utils.log("The breaking points are: \n", breakingPoints);
        System.exit(0);
    }

    public void addFlow(Flow flow, Packet packet) {
        LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
        try {
            currentTime = packet.getArrivalTime();
            packet.setStartTime(currentTime);
            q.put(packet);
            queues.put(flow, q);
        } catch (Exception e) {
            Utils.error("Simulation Failed, Queues overflowed", e);
        }
    }

    public void adjustRates() {
        Utils.debug("At time: ", currentTime);
        Utils.debug("Before adjusting rates are ", queues.keySet());
        Set<Flow> flows = queues.keySet();
        float total = 0;

        //if system has no flows then return
        if (flows.size() == 0) {
            Utils.debug("After adjusting rates are ", queues.keySet());
            breakingPoints.add(currentTime);
            return;
        }

        //if system has only one flow then set the allocated bandwidth to total capacity
        else if (flows.size() == 1) {
            flows.iterator().next().setAllocatedBandwidth(Global.totalCapacity);
            Utils.debug("After adjusting rates are ", queues.keySet());
            breakingPoints.add(currentTime);
            return;
        }

        //else set the allocated bandwidth to minimum bandwidth first.
        for (Flow f : queues.keySet()) {
            f.setAllocatedBandwidth(f.getMinimumBandwidth());
        }

        //get the total bandwidth that is being used.
        total = calculateTotalBandwidth();


        //Add code to adjust the rates... according to the Rate Eq Algo.

        //compute the difference between used capacity and total capacity
        float extraBandwidth = Global.totalCapacity - total;

        //sort the flows according to the minimum bandwidth reserved.
        List<Flow> flowList = new ArrayList<>();
        flowList.addAll(flows);
        Collections.sort(flowList, new FlowComparator());

        //index points to the flow which has least allocation such that allocated rate = reserved rate
        int index = 1;

        Utils.debug("Extra Bandwidth ", extraBandwidth);


        //while extra bandwidth is there and index is within the flowList range.
        while (extraBandwidth > 0 && index < flowList.size()) {

            //prev is the flow which has least allocation such that allocated rate = reserved rate


            //least is the flow which belongs to the group with equal allocated B/W and highest minimum reserved bandwidth


            //difference between the allocated B/Ws
            float difference = 0;

            Utils.debug("Difference ", difference);
            Utils.debug("Index ", index);

            //if the difference is 0 then it means that prev can be brought down in the common pool
            // so we first find the flow which has a positive difference and then
            // share the extra Bandwidth among all the  flows which are there in the common pool.
            while (difference == 0 && index < flowList.size()) {
                Flow prev = flowList.get(index);
                Flow least = flowList.get(index - 1);
                difference = prev.getAllocatedBandwidth() - least.getAllocatedBandwidth();
                index++;
            }

            if(difference == 0 && index == flowList.size()){
                float share = extraBandwidth / index;
                for (int i = 0; i < index; i++) {
                    flowList.get(i).setAllocatedBandwidth(flowList.get(i).getAllocatedBandwidth() + share);
                    extraBandwidth -= share;
                }
            }
            //if the extra bandwidth can bring all the flows including the one at index in the common pool do that
            else if (extraBandwidth >= (index * difference)) {
                for (int i = 0; i < index; i++) {
                    flowList.get(i).setAllocatedBandwidth(flowList.get(i).getAllocatedBandwidth() + difference);
                    extraBandwidth -= difference;
                }
                index++;
            }

            //otherwise just split the B/W equally among others.
            else {
                float share = extraBandwidth / index;
                for (int i = 0; i < index; i++) {
                    flowList.get(i).setAllocatedBandwidth(flowList.get(i).getAllocatedBandwidth() + share);
                    extraBandwidth -= share;
                }
            }
        }

        Utils.debug("Final allocation: ", flowList);

        //all the changes we made to the flows were done in a local copy flowList.
        //now we need to copy the allocated bandwidth to actual flows.
        //the reason is we cannot update the keys in the HashMap in Java directly.

        for (Flow f : flowList) {
            for (Flow f2 : flows) {
                if (f2.getFlowId() == f.getFlowId()) {
                    f2.setAllocatedBandwidth(f.getAllocatedBandwidth());
                }
            }

        }

        Utils.debug("After adjusting rates are ", queues.keySet());
        breakingPoints.add(currentTime);
    }

    public void cleanup(float limit) {

        //find if any queue will become empty before this limit.
        //compute the finishing times of each queue
        while (true) {
            Flow flow = null;
            float earliest = Float.MAX_VALUE;
            for (Flow f : queues.keySet()) {
                LinkedBlockingQueue<Packet> q = queues.get(f);
                float totalRem = 0;
                for (Packet p : q) {
                    totalRem += (p.getLength() - p.getTransmitted());
                }

                float finishingTime = currentTime + (totalRem / f.getAllocatedBandwidth());

                //if this is a breaking point
                if (finishingTime < limit && finishingTime < earliest) {
                    flow = f;
                    earliest = finishingTime;
                }
            }

            Utils.debug("Earliest finishing queue before ", limit, " is ", flow, " at ", earliest);
            //no queues will become empty by this limit
            //so simply clear the queues till this limit
            if (flow == null) {
                cleanupQueues(limit);
                currentTime = limit;
                break;
            } else {
                cleanupQueues(earliest);
                currentTime = earliest;
                queues.remove(flow);
                adjustRates();

            }
        }

    }

    public void cleanupQueues(float limit) {
        for (Flow f : queues.keySet()) {
            float lastFinishingTime = 0;
            while (true) {
                Packet p = queues.get(f).peek();
                if(p == null)
                    continue;
                float finishingTime = 0;
                if (lastFinishingTime == 0) {
                    finishingTime = currentTime + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth());
                } else {
                    finishingTime = lastFinishingTime + ((p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth());
                }

                Utils.debug("finshing time == limit ", finishingTime == limit);
                //this packet can leave
                if (finishingTime <= limit) {
                    p = queues.get(f).poll();
                    p.setTransmitted(p.getLength());
                    p.setFinishTime(finishingTime);
                    completed.add(p);
                    //get the next packet
                    p = queues.get(f).peek();
                    if (p == null) {
                        break;
                    }
                    p.setStartTime(finishingTime);
                    lastFinishingTime = finishingTime;
                } else {
                    p.setTransmitted(p.getTransmitted() + (limit - currentTime) * f.getAllocatedBandwidth());
                    p.setFinishTime(limit + (p.getLength() - p.getTransmitted()) / f.getAllocatedBandwidth());
                    break;
                }
            }
        }

        Utils.debug("At time ", limit, " after cleaning up the queues, ", queues);
    }

    public void addPacket(Flow f, Packet p) {
        try {
            queues.get(f).put(p);
        } catch (Exception e) {
            Utils.error("Queues Overflowed, Simulation Failed");
        }
    }

    public float calculateTotalBandwidth() {
        //get the total bandwidth that is being used.
        float total = 0;
        for (Flow f : queues.keySet()) {
            total += f.getMinimumBandwidth();
        }
        return total;
    }
}
