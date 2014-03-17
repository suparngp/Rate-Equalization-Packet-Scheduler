package com.utd;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The original Rate Equalization Scheduler
 * Created by suparngupta on 2/12/14.
 */
public class RateEqScheduler extends Thread {
    HashMap<Flow, LinkedBlockingQueue<Packet>> queues = new HashMap<>();
    List<Packet> completed = new ArrayList<>();
    List<Float> breakingPoints = new ArrayList<>();
    float currentTime = 0;
    HashSet<Flow> trackedFlows = new HashSet<>();
    HashMap<Float, HashSet<Flow>> tracker = new HashMap<>();

    ResultsFileWriter output = new ResultsFileWriter();
    private int scenario = -1;

    public RateEqScheduler(int scenario){
        this.scenario = scenario;
    }

    public void run() {

        trackedFlows.addAll(Global.flowsRE);
        boolean create = true;
        output.dumpCSV2(queues.keySet(), "rate-eq-bw-" + this.scenario + ".csv", true);

        //add the first packet to the queue.
        Object[] next = null;
        for(int i = 0; i < Global.flowsRE.size(); i++){
            next = Global.pollPacketRE();

            if (next == null) {
                Utils.error("No traffic available to start the simulation");
                System.exit(0);
            }

            //add this new flow.
            //((Packet)next[1]).setStartTime(((Packet)next[1]).getArrivalTime());

            addFlow((Flow) next[0], (Packet) next[1]);

        }

        adjustRates();
        next = Global.pollPacketRE();
        while (currentTime < Global.timeLimit && next != null) {
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

//            if(completed.size() > 100){
//                float finishingTime = 0;
//                List<Packet> packetizedCompleted = new ArrayList<>();
//                for(Packet p1: completed){
//                    Packet clone = p1.clone();
//                    float startTime = Math.max(finishingTime, p1.getArrivalTime());
//                    finishingTime = startTime + p1.getLength() / Global.totalCapacity;
//                    p1.setStartTime(startTime);
//                    p1.setFinishTime(finishingTime);
//                    packetizedCompleted.add(p1);
//                }
//                output.dumpCSV(packetizedCompleted, "rate-eq.csv", create);
//                create = false;
//                packetizedCompleted.clear();
//                completed.clear();
//            }

            next = Global.pollPacketRE();
        }
        Utils.log("No New packets, ending the simulation");
        cleanup(Float.MAX_VALUE);
        Utils.debug("The completed Packets are: \n", completed);
        Collections.sort(breakingPoints);
        Utils.debug("The breaking points are: \n", breakingPoints);

        Utils.log("In packetized version the completed sequence will be");
        processCompleted();
//        System.exit(0);
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

        for(Flow f: trackedFlows){
            f.setCurrentTime(currentTime);
            if(!queues.keySet().contains(f)){
                f.setAllocatedBandwidth(0);
            }
        }

        Utils.log("At time: ", currentTime);
        Utils.log("Before adjusting rates are ", queues.keySet());
        Set<Flow> flows = queues.keySet();
        float total = 0;

        //if system has no flows then return
        if (flows.size() == 0) {
            Utils.log("After adjusting rates are ", queues.keySet());
            breakingPoints.add(currentTime);
            return;
        }

        //if system has only one flow then set the allocated bandwidth to total capacity
        else if (flows.size() == 1) {
            Flow fl  = flows.iterator().next();
            fl.setAllocatedBandwidth(Global.totalCapacity);

            for(Flow f: trackedFlows){
                if(f.getFlowId() == fl.getFlowId()){
                    f.setAllocatedBandwidth(fl.getAllocatedBandwidth());
                    break;
                }
            }
            HashSet<Flow> set = tracker.get(currentTime);
            if(set == null){
                set = new HashSet<>();
            }
            set.clear();
            for(Flow f: trackedFlows){
                Flow clone = f.clone();
                set.add(clone);
            }

            tracker.put(currentTime, set);

            Utils.log("After adjusting rates are ", queues.keySet());
            //output.dumpCSV2(trackedFlows, "rate-eq-bw.csv", false);
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

        Utils.log(flowList);
        //index points to the flow which has least allocation such that allocated rate = reserved rate
        int index = 1;

        Utils.log("Extra Bandwidth ", extraBandwidth);


        //while extra bandwidth is there and index is within the flowList range.
        while (Math.floor(extraBandwidth) > 0) {

            //System.out.println("Stuck Here " + extraBandwidth);
            index = 1;
            //prev is the flow which has least allocation such that allocated rate = reserved rate


            //least is the flow which belongs to the group with equal allocated B/W and highest minimum reserved bandwidth


            //difference between the allocated B/Ws
            float difference = 0;



            //if the difference is 0 then it means that prev can be brought down in the common pool
            // so we first find the flow which has a positive difference and then
            // share the extra Bandwidth among all the  flows which are there in the common pool.
            while (difference == 0 && index < flowList.size()) {
                Flow prev = flowList.get(index);
                Flow least = flowList.get(index - 1);
                difference = prev.getAllocatedBandwidth() - least.getAllocatedBandwidth();
                index++;
            }

            index--;
            Utils.log("Difference ", difference);
            Utils.log("Index ", index);

            if(difference == 0){
                float share = extraBandwidth / flowList.size();
                for (int i = 0; i < flowList.size(); i++) {
                    flowList.get(i).setAllocatedBandwidth(flowList.get(i).getAllocatedBandwidth() + share);
                    extraBandwidth -= share;
                }
                break;
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

        for(Flow f: trackedFlows){
            for(Flow fl: flowList){
                if(fl.getFlowId() == f.getFlowId()){
                    f.setAllocatedBandwidth(fl.getAllocatedBandwidth());
                    break;
                }
            }
        }

        HashSet<Flow> set = tracker.get(currentTime);
        if(set == null){
            set = new HashSet<>();
        }
        set.clear();
        for(Flow f: trackedFlows){
            Flow clone = f.clone();
            set.add(clone);
        }
        tracker.put(currentTime, set);
        //System.out.println(tracker);
        //output.dumpCSV2(trackedFlows, "rate-eq-bw.csv", false);
        Utils.log("Final allocation: ", flowList);
        //System.out.println(flowList);
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

        Utils.log("After adjusting rates are ", queues.keySet());
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
                    totalRem += 1;
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
                    break;
                float finishingTime = 0;
                if (lastFinishingTime == 0) {
                    finishingTime = currentTime + ((p.getLength() - p.getTransmitted()) / (f.getAllocatedBandwidth() * Global.maxPacketLength));
                } else {
                    finishingTime = lastFinishingTime + ((p.getLength() - p.getTransmitted()) / (f.getAllocatedBandwidth() * Global.maxPacketLength));
                }

                Utils.debug("finshing time == limit ", finishingTime == limit);
                //this packet can leave
                if (finishingTime <= limit) {
                    p = queues.get(f).poll();
                    p.setTransmitted(p.getLength());
                    p.setFinishTime(finishingTime);
                    completed.add(p);

                    //add a new packet of the same flow
                    Packet newP = f.createPacket();
                    Global.addToRE(newP, f);


//                    addPacket(f, p);
                    //get the next packet
                    p = queues.get(f).peek();
                    if (p == null) {
                        break;
                    }
                    p.setStartTime(finishingTime);
                    lastFinishingTime = finishingTime;
                } else {
                    p.setTransmitted(p.getTransmitted() + (limit - currentTime) * f.getAllocatedBandwidth() * Global.maxPacketLength);
                    p.setFinishTime(limit + (p.getLength() - p.getTransmitted()) / (f.getAllocatedBandwidth() * Global.maxPacketLength));
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

    public void processCompleted(){
        List<Packet> packetizedCompleted = new ArrayList<>();
        float finishingTime = 0;
        for(Packet p: completed){
            Packet clone = p.clone();
            float startTime = Math.max(finishingTime, clone.getArrivalTime());
            finishingTime = startTime + 1 / Global.totalCapacity;
            //System.out.println(finishingTime);
            clone.setStartTime(startTime);
            clone.setFinishTime(finishingTime);
            packetizedCompleted.add(clone);
        }
        Utils.log(packetizedCompleted.size());
        output.dumpCSV(packetizedCompleted, "rate-eq-" + this.scenario + ".csv", true);
        List<Float> tss = new ArrayList<>();
        tss.addAll(tracker.keySet());
        Collections.sort(tss);
        //Utils.log(tss);
        for(Float ts: tss){
            Utils.log(ts, tracker.get(ts));
            output.dumpCSV2(tracker.get(ts), "rate-eq-bw-" + this.scenario + ".csv", false);
        }





        double current = 0;
        HashMap<Integer, TotalDataFr> totalDataTracker = new HashMap<>();
        double incre = 0.02;
        String fileName = "rate-eq-total-" + this.scenario + ".csv";

        int index = 0;
        List<Packet> removed = new ArrayList<>();
        HashMap<Double, HashSet<TotalDataFr>> totalTracker = new HashMap<>();
        boolean create = true;
        for(Flow f: Global.flowsRE){
            TotalDataFr fr = new TotalDataFr();
            fr.setFlowId(f.getFlowId());
            fr.setTimestamp(0);
            fr.setTotalData(0);
            fr.setTotalPackets(0);
            totalDataTracker.put(f.getFlowId(), fr);
        }
        while (!packetizedCompleted.isEmpty()){
            for(int flowId: totalDataTracker.keySet()){
                totalDataTracker.get(flowId).setTimestamp(current);
            }
            HashSet<TotalDataFr> set = new HashSet<>();
            for(index = 0; index < packetizedCompleted.size(); index++){
                Packet p = packetizedCompleted.get(index);
                if(p.getFinishTime() < current){
                    removed.add(p);
                    TotalDataFr frt = totalDataTracker.get(p.getFlowId());
                    frt.setFlowId(p.getFlowId());
                    frt.setTimestamp(current);
                    frt.setTotalPackets(frt.getTotalPackets() + 1);
                    frt.setTotalData(frt.getTotalData() + Global.maxPacketLength);
                    totalDataTracker.put(p.getFlowId(), frt);


                }
                else{
                    break;
                }
            }

            for(TotalDataFr ftr: totalDataTracker.values()){
                TotalDataFr clone = ftr.clone();
                set.add(clone);
            }

            //System.out.println(set);
            totalTracker.put(current, set);
            output.dumpCSV3(set, fileName, create);
            if(create)
                create = false;
            set.clear();
            packetizedCompleted.removeAll(removed);
            removed.clear();
            current += incre;

        }
    }
}
