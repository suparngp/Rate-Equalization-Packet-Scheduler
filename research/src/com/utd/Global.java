package com.utd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Globals class defines the singleton entities common to the system.
 * Like session queues, list of available sessions.
 * Created by suparngupta on 1/23/14.
 */
public class Global {

    static HashMap<Integer, LinkedBlockingQueue<Packet>> queuesMapGPS = new HashMap<>();
    static HashMap<Integer, LinkedBlockingQueue<Packet>> queuesMapRE = new HashMap<>();
    static ArrayList<Flow> flowsGPS = new ArrayList<>();
    static ArrayList<Flow> flowsRE = new ArrayList<>();
    static float totalCapacity = 0;
    static float availableCapacity = 0;
    static float usedCapacity = 0;
    static float minimumReserved = 0;
    public static void init(float tc) {
        totalCapacity = tc;
        availableCapacity = totalCapacity;
        usedCapacity = 0;


    }

    /*
    * Gets the queue for the provided flow id;
    *
    * */
    public static LinkedBlockingQueue<Packet> getQueue(int flowId) {
        return queuesMapGPS.get(flowId);
    }


    public static void addFlow(Flow flow) {
        availableCapacity -= flow.getMinimumBandwidth();
        flowsGPS.add(flow);
        LinkedBlockingQueue<Packet> queue = new LinkedBlockingQueue<>();
        queuesMapGPS.put(flow.getFlowId(), queue);
    }

    public static void removeFlow(int id) {
        for (int i = 0; i < flowsGPS.size(); i++) {
            if (flowsGPS.get(i).getFlowId() == id) {
                flowsGPS.remove(i);
                queuesMapGPS.remove(id);
                availableCapacity += flowsGPS.get(i).getMinimumBandwidth();
                break;
            }
        }
    }

    public static boolean packetsExist(){
        for(int key: queuesMapGPS.keySet()){
            if(queuesMapGPS.get(key).size() != 0){
                return false;
            }
        }

        return true;
    }

    public static Flow getFlow(int id){
        for(int i = 0; i < flowsGPS.size(); i++){
            if(flowsGPS.get(i).getFlowId() == id){
                return flowsGPS.get(i);
            }
        }

        return null;
    }


    public static void adjustWeights(){
        float min = Float.MAX_VALUE;
        minimumReserved = 0;
        for(int i = 0; i < flowsGPS.size(); i++){
            minimumReserved += flowsGPS.get(i).getMinimumBandwidth();
        }

        //update available capacity
        availableCapacity = totalCapacity - minimumReserved;

        for(int i = 0; i < flowsGPS.size(); i++){
            flowsGPS.get(i).setWeight(flowsGPS.get(i).getMinimumBandwidth()/minimumReserved);
        }
    }

    public static void adjustBandwidth(){
        for(int i = 0; i < flowsGPS.size(); i++){
            Flow flow = flowsGPS.get(i);
            flow.setAllocatedBandwidth(totalCapacity * flow.getWeight());
        }
    }

    public static HashMap<String,Object> getNextPacket(){
        LinkedBlockingQueue<Packet> queue;
        Flow flow = null;
        float latest = Float.MAX_VALUE;
        Packet packet = null;
        Integer index = 0;
        Utils.debug(queuesMapGPS.keySet());
        for(int i: queuesMapGPS.keySet()){
            if(queuesMapGPS.get(i).peek().getArrivalTime() < latest){
                flow = flowsGPS.get(i - 1);
                index = i;
                queue = queuesMapGPS.get(i);
                packet = queue.peek();
                latest = packet.getArrivalTime();
            }
        }

        //we have the latest packet now.
        //remove the packet and return the packet and flow.
        if(packet == null){
            return null;
        }
        queuesMapGPS.get(index).poll();
        if(queuesMapGPS.get(index).size() == 0){
            queuesMapGPS.remove(index);
        }
        HashMap<String, Object> result = new HashMap<>();
        result.put("flow", flow);
        result.put("packet", packet);
        return result;
    }

    public static Object[] peekPacket(){
        LinkedBlockingQueue<Packet> queue;
        Flow flow = null;
        float latest = Float.MAX_VALUE;
        Packet packet = null;
        Integer index = 0;
        //Utils.debug(queuesMapGPS.keySet());
        for(int i: queuesMapGPS.keySet()){
            if(queuesMapGPS.get(i).peek().getArrivalTime() < latest){
                flow = flowsGPS.get(i - 1);
                queue = queuesMapGPS.get(i);
                packet = queue.peek();
                latest = packet.getArrivalTime();
            }
        }
        if(packet == null){
            return null;
        }
        Object[] result = {flow, packet};
        return result;
    }

    public static Object[] pollPacket(){
        LinkedBlockingQueue<Packet> queue;
        Flow flow = null;
        float latest = Float.MAX_VALUE;
        Packet packet = null;
        Integer index = 0;
        //Utils.debug(queuesMapGPS.keySet());
        for(int i: queuesMapGPS.keySet()){
            if(queuesMapGPS.get(i).size() == 0)
                continue;

            if(queuesMapGPS.get(i).peek().getArrivalTime() < latest){
                flow = flowsGPS.get(i - 1);
                index = i;
                queue = queuesMapGPS.get(i);
                packet = queue.peek();
                latest = packet.getArrivalTime();
            }
        }

        if(packet == null){
            return null;
        }
        queuesMapGPS.get(index).poll();
        Object[] result = {flow, packet};
        return result;
    }

    public static Object[] pollPacketRE(){
        LinkedBlockingQueue<Packet> queue;
        Flow flow = null;
        float latest = Float.MAX_VALUE;
        Packet packet = null;
        Integer index = 0;
        //Utils.debug(queuesMapGPS.keySet());
        for(int i: queuesMapRE.keySet()){
            if(queuesMapRE.get(i).size() == 0)
                continue;

            if(queuesMapRE.get(i).peek().getArrivalTime() < latest){
                flow = flowsRE.get(i - 1);
                index = i;
                queue = queuesMapRE.get(i);
                packet = queue.peek();
                latest = packet.getArrivalTime();
            }
        }

        if(packet == null){
            return null;
        }
        queuesMapRE.get(index).poll();
        Object[] result = {flow, packet};
        return result;
    }


    public static void cloneTraffic() throws Exception{
        //clone the flows
        for(Flow f: flowsGPS){
            Flow flow = f.clone();
            flowsRE.add(flow);
            LinkedBlockingQueue<Packet> q = new LinkedBlockingQueue<>();
            for(Packet p: queuesMapGPS.get(f.getFlowId())){
                Packet packet = p.clone();
                q.put(packet);
            }
            queuesMapRE.put(flow.getFlowId(), q);
        }
    }
}
