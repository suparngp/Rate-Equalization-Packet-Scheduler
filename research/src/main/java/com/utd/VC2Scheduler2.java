package com.utd;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suparngupta on 3/7/14.
 */
public class VC2Scheduler2 {

    HashMap<Flow, LinkedBlockingQueue<Packet>>groupA = new HashMap<>();
    List<Flow> groupB = new ArrayList<>();
    HashMap<Flow, Float> virtualClocks = new HashMap<>();
    private float currentTime;
    private List<Packet> completed = new ArrayList<>();
    HashMap<Flow, Integer> packetCount = new HashMap<>();
    ResultsFileWriter output = new ResultsFileWriter();

    public VC2Scheduler2 init(){
        double usedCapacity = 0;
        for(int i = 0; i < Global.flowsVC2.size(); i++){
            LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
            Flow f = Global.flowsVC2.get(i);
            f.setAllocatedBandwidth(f.getMinimumBandwidth());
            groupA.put(f, q);
            packetCount.put(f, 0);
            usedCapacity += f.getMinimumBandwidth();
        }
        Object[] next;
        for(int i = 0; i < groupA.size(); i++){
            next = Global.pollPacketVC2();
            Flow f = (Flow)next[0];
            Packet p = (Packet)next[1];
            addPacket(f, p);
            groupB.add(f);
            virtualClocks.put(f, p.getArrivalTime());
        }

        if(usedCapacity < Global.totalCapacity){
            float extra = (float)(Global.totalCapacity - usedCapacity);
            Flow padding = new Flow(-1, extra, FlowType.PADDING);
            LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
            groupA.put(padding, q);
            virtualClocks.put(padding, 1 / padding.getMinimumBandwidth());
        }

        //add one packet for each Flow.


        System.out.println(groupA);
        System.out.println(groupB);
        return this;
    }

    public void run(){
        while(currentTime < Global.timeLimit){
            Object[] next = Global.pollPacketVC2();
            if(next == null){
                Utils.error("Incoming traffic is empty. Error in simulation");
                System.exit(0);
            }

            Flow f = (Flow)next[0];
            Packet p = (Packet)next[1];

            cleanUp(p.getArrivalTime());

            if(!groupB.contains(f)){
//                System.out.println("Group B does not have this flow" + f);
//                System.out.println(virtualClocks.get(f));
//                System.out.println(getMinimumVCFromB());
                float currentVC = Math.max(getMinimumVCFromB(), virtualClocks.get(f));
                virtualClocks.put(f, currentVC);
                groupB.add(f);
            }
            addPacket(f, p);
        }

        System.out.println("VC2 Simulation completed");
        System.out.println(completed.size());
        processCompleted();
        //System.out.println(virtualClocks);
    }

    public void addPacket(Flow f, Packet p){
        try{
            groupA.get(f).put(p);
        }

        catch(Exception e){
            Utils.error("Unable to add packet to queue", e);
        }
    }

    public Flow getNextFlow(){
        Flow flow = null;
        double vc = Double.MAX_VALUE;
        for(Flow f: virtualClocks.keySet()){
            if(/*virtualClocks.get(f) <= currentTime &&*/ virtualClocks.get(f) < vc){
                flow = f;
                vc = virtualClocks.get(f);
            }
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

    public Flow getFromGroupB(){
        Flow flow = null;
        int max = Integer.MAX_VALUE;
        for(Flow f: groupB){
            if(packetCount.get(f) < max){
                max = packetCount.get(f);
                flow = f;
            }
        }
        return flow;
    }

    public void cleanUp(double limit){
        while(currentTime < limit){
            if(groupB.size() < 10){
                System.out.println("Greedy flows become emoty");
                System.exit(0);
            }
            Flow f = null;
            while (f == null){
                f = getNextFlow();
                if(f == null){
                    currentTime = getMinimumVCFromA();
                }
            }

            if(groupA.get(f).isEmpty()){
                updateVC(f);
                f = getFromGroupB();
                System.out.println("Giving extra to " + f.getFlowId());
            }

            if(groupA.get(f) == null){
                System.out.println(groupA.keySet());
                System.out.println(groupB);
                System.out.println(f);
            }
            Packet p = groupA.get(f).poll();
            float finishingTime = currentTime + 1 / Global.totalCapacity;
            p.setStartTime(currentTime);
            p.setFinishTime(finishingTime);
            completed.add(p);
            packetCount.put(f, packetCount.get(f) + 1);
            p = f.createPacket();
            Global.addToVC2(p, f);
            if(packetCount.get(f) - getMinPacketCountFromB() < 250000)
                packetCount.put(f, packetCount.get(f) + 1);
            updateVC(f);
            if(groupA.get(f).isEmpty()){
                boolean remove = groupB.remove(f);
                if(!remove){
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

    public float getMinimumVCFromB(){
        double vc = Double.MAX_VALUE;
        for(Flow f: groupB){
            if(virtualClocks.get(f) < vc){
                vc = virtualClocks.get(f);
            }
        }

        return (float)vc;
    }

    public float getMinimumVCFromA(){
        double vc = Double.MAX_VALUE;
        for(Flow f: groupA.keySet()){
            if(virtualClocks.get(f) < vc){
                vc = virtualClocks.get(f);
            }
        }

        return (float)vc;
    }

    public void processCompleted(){

        double current = 0;
        HashMap<Integer, TotalDataFr> totalDataTracker = new HashMap<>();
        double incre = 0.02;
        String fileName = "vc2-total.csv";

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
        while (!completed.isEmpty()){
            for(int flowId: totalDataTracker.keySet()){
                totalDataTracker.get(flowId).setTimestamp(current);
            }
            HashSet<TotalDataFr> set = new HashSet<>();
            for(index = 0; index < completed.size(); index++){
                Packet p = completed.get(index);
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
            completed.removeAll(removed);
            removed.clear();
            current += incre;

        }
    }

    public int getMinPacketCountFromB(){
        int min = Integer.MAX_VALUE;
        for(Flow f: groupB){
            if(packetCount.get(f) < min){
                min = packetCount.get(f);
            }
        }
        return min;
    }
}
