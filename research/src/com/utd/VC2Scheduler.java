package com.utd;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by suparngupta on 2/20/14.
 */
public class VC2Scheduler {
    HashMap<Flow, LinkedBlockingQueue<Packet>> queues = new HashMap<>();
    HashMap<Flow, Integer> counter = new HashMap<>();
    List<Packet> completed = new ArrayList<>();
    List<Float> breakingPoints = new ArrayList<>();
    float currentTime = 0;
    HashMap<Flow, Float> groupAVC = new HashMap<>();

    public VC2Scheduler() {
        float usedBandwidth = 0;
        for (int i = 0; i < Global.flowsVC2.size(); i++) {
            Global.flowsVC2.get(i).setAllocatedBandwidth(Global.flowsVC2.get(i).getMinimumBandwidth());
            Global.flowsVC2.get(i).setWeight(0);
            LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
            queues.put(Global.flowsVC2.get(i), q);
            groupAVC.put(Global.flowsVC2.get(i), 0.0f);
            counter.put(Global.flowsVC2.get(i), 0);
            usedBandwidth += Global.flowsVC2.get(i).getMinimumBandwidth();
        }

        if(Global.totalCapacity - usedBandwidth > 0){
            Flow padding = new Flow(-1, -1);
            padding.setMinimumBandwidth(Global.totalCapacity - usedBandwidth);
            padding.setVirtualClock(0);
            queues.put(padding, new LinkedBlockingQueue<Packet>());
            groupAVC.put(padding, 0.0f);
            counter.put(padding, 0);
        }

        Utils.log("After adjusting rates, the final flows pattern is ", queues);
    }

    public void updateVC(Flow flow) {
        groupAVC.put(flow, (groupAVC.get(flow) + 1 / flow.getMinimumBandwidth()));
    }

    public Flow getNextCandidate() {
        float min = Float.MAX_VALUE;
        Flow flow = null;
        for (Flow f : groupAVC.keySet()) {
            if (groupAVC.get(f) < min) {
                min = groupAVC.get(f);
                flow = f;
            }
        }
        return flow;
    }

    public void run() {
        //add the first packet to the queue.
        Object[] next = Global.pollPacketVC2();
        if (next == null) {
            Utils.error("No traffic available to start the simulation");
            System.exit(0);
        }
        Flow flow = (Flow) next[0];
        Packet packet = (Packet) next[1];
        currentTime = packet.getArrivalTime();
        addPacket(packet, flow);

        while (true) {
            next = Global.pollPacketVC2();

            if(next == null){
                Utils.log("Traffic completed, Cleaning up queues");
                cleanUp(Float.MAX_VALUE);
                Utils.log("Simulation Completed. Exiting...");
                Utils.log(completed);
                System.exit(0);
            }

            flow = (Flow)next[0];
            packet = (Packet)next[1];
            cleanUp(packet.getArrivalTime());
            addPacket(packet, flow);
        }
    }


    public Flow getFromGroupB() {
        int min = Integer.MAX_VALUE;
        Flow flow = null;
        for (Flow f : counter.keySet()) {
            if (counter.get(f) < min && queues.get(f).size() != 0) {
                min = counter.get(f);
                flow = f;
            }
        }
        return flow;
    }

    public void cleanUp(float limit) {
        while (currentTime < limit) {
            Flow candidate = getNextCandidate();
            Packet p = null;
            Flow flow = candidate;
            if (queues.get(candidate).size() == 0) {
                //forward the packet from somewhere else.
                flow = getFromGroupB();
                if (flow == null) {
                    Utils.log("There no flow in Group B as well. Advancing current time to packet's arrival time");
                    currentTime = limit;
                    break;
                }
                p = queues.get(flow).poll();
                updateVC(flow);
            } else {
                p = queues.get(candidate).poll();
            }

            p.setStartTime(currentTime);
            p.setTransmitted(p.getLength());
            p.setFinishTime(currentTime + p.getLength() / Global.totalCapacity);
            currentTime = p.getFinishTime();
            completed.add(p);
            counter.put(flow, counter.get(flow) + 1);
            if(flow != candidate){
                updateVC(flow);
            }
            updateVC(candidate);
        }
    }

    public void addPacket(Packet p, Flow f) {
        try {
            queues.get(f).put(p);
        } catch (Exception e) {
            e.printStackTrace();
            Utils.error("Unable to add packet to the queue. Simulation Failed. Exiting...", e);
            System.exit(0);
        }
    }
}
