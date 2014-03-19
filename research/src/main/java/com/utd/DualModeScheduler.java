package com.utd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class to simulate Dual Mode Scheduler
 * Created by suparngupta on 3/7/14.
 */
public class DualModeScheduler {

    //    map to hold flows and respective queues.
    private HashMap<Flow, LinkedBlockingQueue<Packet>> groupA = new HashMap<>();

    //list of flows in group B
    private List<Flow> groupB = new ArrayList<>();

    //Map to hold the virtual clocks
    private HashMap<Flow, Float> virtualClocks = new HashMap<>();

    //current time of the scehuler
    private float currentTime;

    //list to hold completed packets
    private List<Packet> completed = new ArrayList<>();

    //map to maintain the transmitted packet count for each flow
    private HashMap<Flow, Integer> packetCount = new HashMap<>();

    //output file writer
    private ResultsFileWriter output = new ResultsFileWriter();

    //scenario number
    private int scenario;

    private File dump;
    BufferedWriter br;

    float last = 0;
    int count = 0;
    /*
    * Initialize the scheduler with the scenarion number.
    * */
    public DualModeScheduler(int scenario) {
        this.scenario = scenario;
        dump = new File("dump");
        if(dump.exists()){
            dump.delete();
        }
        try{
            dump.createNewFile();
            br = new BufferedWriter(new FileWriter(dump, true));
        }
        catch(Exception e){
            e.printStackTrace();;
        }

    }


    /**
     * Configures the scheduler with the initial configuration.
     */
    public DualModeScheduler init() {
        double usedCapacity = 0;

        //assign the minimum capacity to each flow.
        for (int i = 0; i < Global.flowsVC2.size(); i++) {
            LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
            Flow f = Global.flowsVC2.get(i);
            f.setAllocatedBandwidth(f.getMinimumBandwidth());
            groupA.put(f, q);
            packetCount.put(f, 0);
            usedCapacity += f.getMinimumBandwidth();
        }

        //add the initial t = 0 to the flow queues
        Object[] next;
        for (int i = 0; i < groupA.size(); i++) {
            next = Global.pollPacketVC2();
            Flow f = (Flow) next[0];
            Packet p = (Packet) next[1];
            addPacket(f, p);
            groupB.add(f);
            virtualClocks.put(f, p.getArrivalTime());
        }


        //of there is unused capacity, create a padding flow with flow ID -1
        if (usedCapacity < Global.totalCapacity) {
            float extra = (float) (Global.totalCapacity - usedCapacity);
            Flow padding = new Flow(-1, extra, FlowType.PADDING);
            LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
            groupA.put(padding, q);
            virtualClocks.put(padding, 0.0f);
        }

        return this;
    }

    /*
    * Driver method to run the scheduler.
    * */
    public void run() {

        //run the scheduler till current time reaches a limit.
        while (currentTime < Global.timeLimit) {
            Object[] next = Global.pollPacketVC2();
            if (next == null) {
                Utils.error("Incoming traffic is empty. Error in simulation");
                System.exit(0);
            }

            Flow f = (Flow) next[0];
            Packet p = (Packet) next[1];

            cleanUp(p.getArrivalTime());

            //if the new packet introduces a new flow, then add it to group B as well.
            if (!groupB.contains(f)) {
                float currentVC = Math.max(getMinimumVCFromB(), virtualClocks.get(f));
                //int round = Math.max(getMinPacketCountFromB(), packetCount.get(f));
                //packetCount.put(f, round);
                virtualClocks.put(f, currentVC);
                groupB.add(f);
            }
            addPacket(f, p);
        }

        System.out.println("VC2 Simulation completed");
        System.out.println(completed.size());
        processCompleted();
        try{
//            for(Flow f: groupA.keySet()){
//                if(f.getFlowId() == 20){
//                    System.out.println(groupA.get(f));
//                    break;
//                }
//            }
            System.out.println(last);
            System.out.println(count);
            br.close();
        }

        catch (Exception e){
            e.printStackTrace();
        }
        //System.out.println(virtualClocks);
    }

    public void addPacket(Flow f, Packet p) {
        try {
            groupA.get(f).put(p);
        } catch (Exception e) {
            Utils.error("Unable to add packet to queue", e);
        }
    }

    public Flow getNextFlow() {
        Flow flow = null;
        double vc = Double.MAX_VALUE;
        for (Flow f : virtualClocks.keySet()) {
            if (/*virtualClocks.get(f) <= currentTime &&*/ virtualClocks.get(f) < vc) {
                flow = f;
                vc = virtualClocks.get(f);
            }
        }
        //System.out.println("Returning flow " + flow.getFlowId());
        try{
            br.append(String.valueOf(flow.getFlowId()));
            br.newLine();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return flow;
    }

//    public Flow getFromGroupB() {
//        Flow flow = null;
//        double vc = Double.MAX_VALUE;
//        for(Flow f: groupB){
//            if(virtualClocks.get(f) < vc){
//                flow = f;
//                vc = virtualClocks.get(f);
//            }
//        }
//        return flow;
//    }

    public Flow getFromGroupB() {
        Flow flow = null;
        int max = Integer.MAX_VALUE;
        for (Flow f : groupB) {
            if (packetCount.get(f) < max) {
                max = packetCount.get(f);
                flow = f;
            }
        }
        return flow;
    }

    public void cleanUp(double limit) {
        while (currentTime < limit) {
            if (groupB.size() < 10) {
                System.out.println("Greedy flows become emoty");
                System.exit(0);
            }


            Flow f = null;
            while (f == null) {
                f = getNextFlow();
                if (f == null) {
                    currentTime = getMinimumVCFromA();
                }
            }

            boolean fromB = false;
            if (groupA.get(f).isEmpty()) {
                updateVC(f);
                f = getFromGroupB();
                fromB = true;
                System.out.println("Giving extra to " + f.getFlowId());
                //System.out.println(packetCount);
            }

            if (groupA.get(f) == null) {
                System.out.println(groupA.keySet());
                System.out.println(groupB);
                System.out.println(f);
            }

            Packet p = groupA.get(f).poll();
            float finishingTime = currentTime + 1 / Global.totalCapacity;
            p.setStartTime(currentTime);
            p.setFinishTime(finishingTime);
            if(p.getFlowId() == 20){
                last = p.getArrivalTime();
                count ++;
            }
            completed.add(p);
            //packetCount.put(f, packetCount.get(f) + 1);
            //p = f.createPacket();
            addNewPackets(f, finishingTime);
            //Global.addToVC2(p, f);

            if(fromB){
                int finalCount = Math.min(packetCount.get(f) + 1, getMinPacketCountFromB() + 10);
                packetCount.put(f, finalCount);
            }

            else{
                packetCount.put(f, packetCount.get(f) + 1);
            }
//            if (packetCount.get(f) - getMinPacketCountFromB() < 100)
//                packetCount.put(f, packetCount.get(f) + 1);

            updateVC(f);
            if (groupA.get(f).isEmpty()) {
                boolean remove = groupB.remove(f);
                if (!remove) {
                    System.out.println("Flow not in group B impossible");
                    System.exit(0);
                }
            }
            currentTime = finishingTime;
        }
    }

    public void updateVC(Flow flow) {
        virtualClocks.put(flow, (virtualClocks.get(flow) + 1 / flow.getMinimumBandwidth()));
    }

    public float getMinimumVCFromB() {
        double vc = Double.MAX_VALUE;
        for (Flow f : groupB) {
            if (virtualClocks.get(f) < vc) {
                vc = virtualClocks.get(f);
            }
        }

        return (float) vc;
    }

    public float getMinimumVCFromA() {
        double vc = Double.MAX_VALUE;
        for (Flow f : groupA.keySet()) {
            if (virtualClocks.get(f) < vc) {
                vc = virtualClocks.get(f);
            }
        }

        return (float) vc;
    }

    public void processCompleted() {

        double current = 0;
        HashMap<Integer, TotalDataFr> totalDataTracker = new HashMap<>();
        double incre = 0.02;
        String fileName = "vc2-total-" + this.scenario + ".csv";

        int index = 0;
        List<Packet> removed = new ArrayList<>();
        HashMap<Double, HashSet<TotalDataFr>> totalTracker = new HashMap<>();
        boolean create = true;
        for (Flow f : Global.flowsRE) {
            TotalDataFr fr = new TotalDataFr();
            fr.setFlowId(f.getFlowId());
            fr.setTimestamp(0);
            fr.setTotalData(0);
            fr.setTotalPackets(0);
            totalDataTracker.put(f.getFlowId(), fr);
        }
        while (!completed.isEmpty()) {
            for (int flowId : totalDataTracker.keySet()) {
                totalDataTracker.get(flowId).setTimestamp(current);
            }
            HashSet<TotalDataFr> set = new HashSet<>();
            for (index = 0; index < completed.size(); index++) {
                Packet p = completed.get(index);
                if (p.getFinishTime() < current) {
                    removed.add(p);
                    TotalDataFr frt = totalDataTracker.get(p.getFlowId());
                    frt.setFlowId(p.getFlowId());
                    frt.setTimestamp(current);
                    frt.setTotalPackets(frt.getTotalPackets() + 1);
                    frt.setTotalData(frt.getTotalData() + Global.maxPacketLength);
                    totalDataTracker.put(p.getFlowId(), frt);


                } else {
                    break;
                }
            }

            for (TotalDataFr ftr : totalDataTracker.values()) {
                TotalDataFr clone = ftr.clone();
                set.add(clone);
            }

            //System.out.println(set);
            totalTracker.put(current, set);
            output.dumpCSV3(set, fileName, create);
            if (create)
                create = false;
            set.clear();
            completed.removeAll(removed);
            removed.clear();
            current += incre;

        }
    }

    public int getMinPacketCountFromB() {
        int min = Integer.MAX_VALUE;
        for (Flow f : groupB) {
            if (packetCount.get(f) < min) {
                min = packetCount.get(f);
            }
        }
        return min;
    }

    public void addNewPackets(Flow f, double time) {

        while(true){
            if(Global.queuesMapVC2.get(f.getFlowId()) == null && Global.queuesMapVC2.get(f.getFlowId()).peek().getArrivalTime() <= time){
                try{
                    groupA.get(f).put(Global.queuesMapVC2.get(f.getFlowId()).poll());
                }
                catch(Exception e){
                    System.out.println("Unable to add new packets on departure");
                    e.printStackTrace();
                }
            }

            else{
                break;
            }
        }


//        while(true){
//            Packet p = f.createPacket();
//            if(p.getArrivalTime() > time){
//                Global.addToVC2(p, f);
//                break;
//            }
//
//            else{
//                try{
//                    groupA.get(f).put(p);
//                }
//                catch(Exception e){
//                    System.out.println("Unable to add new packets on departure");
//                    e.printStackTrace();
//                }
//
//            }
//        }

//        try {
//            Packet p = f.createPacket();
//            groupA.get(f).put(p);
//        } catch (Exception e) {
//            System.out.println("Unable to add new packets on departure");
//            e.printStackTrace();
//        }
    }
}
