package com.utd;

/**
 * Time stamp generator for flowsGPS
 * Generating the constant timestamps
 * Created by suparngupta on 1/23/14.
 */
public class TSGenerator implements Cloneable {
    private Flow flow;
    ExponentialRandom random;
    double current = 0;
    double last = 0;
    double incre = 0;
    double factor;
    public TSGenerator(Flow flow, double factor) {
        this.flow = flow;
        FlowType type = flow.getFlowType();
        this.factor = factor;

        double rate = flow.getMinimumBandwidth() * factor;

        switch (type) {

            case UU_A_PR:
                random = new ExponentialRandom(flow.getFlowId(), rate);
                break;
            case UU_B_PR:
                random = new ExponentialRandom(flow.getFlowId(), rate);
                break;
            case UU_A_CR:
                incre = 1 / rate;
                break;
            case UU_B_CR:
                incre = 1 / rate;
                break;
            case UA_A_PR:
                random = new ExponentialRandom(flow.getFlowId(), rate);
                break;
            case UA_A_CR:
                incre = 1 / rate;
                break;
            case UU_UA_B_PR:
                random = new ExponentialRandom(flow.getFlowId(), rate);
                break;
            case UU_UA_B_CR:
                incre = 1 / rate;
                break;
            case GREEDY:
                incre = 1 / (Global.totalCapacity / 2);
                break;
            case PADDING:
                break;
        }
    }

    @Override
    public TSGenerator clone() {
        TSGenerator clone = new TSGenerator(flow, factor);

        return clone;
    }
//
//    public double[] generatePoissonTS(double rate, double limit, double ratio){
//        rate *= ratio;
//        double[] ts = RandomNumberGenerator.generateExpRVArray(rate);
//        int counter = 0;
//        double current = ts[0];
//        for(double t: ts){
//            if(current > limit){
//                break;
//            }
//            counter++;
//            current += ts[counter];
//        }
//        double[] result = new double[counter];
//        current = 0;
//        int i = -1;
//        while(i+ 1 < counter){
//            result[i + 1] = current;
//            i++;
//            current += ts[i];
//        }
//        return result;
//    }
//
//    public double[] generateConstantRateTS(double rate, double limit, double ratio){
//        rate *= ratio;
//
//        double increment = 1 / rate;
//        ArrayList<Double> ts = new ArrayList<>();
//        double current = 0;
//        while(current < limit){
//            ts.add(current);
//            current += increment;
//        }
//        double[] result = new double[ts.size()];
//
//        for(int i = 0; i < result.length; i++){
//            result[i] = ts.get(i);
//        }
//        return result;
//    }
//
//    public double[] generateGreedyTS(double rate, double limit, double ratio){
//
//        return generateConstantRateTS(rate, limit, ratio);
//    }



    public double generateTimestamp() {
        FlowType type = flow.getFlowType();
        last = current;
        switch (type){

            case UU_A_PR:
                current += random.nextExp();
                break;
            case UU_B_PR:
                current += random.nextExp();
                break;
            case UU_A_CR:
                current += incre;
                break;
            case UU_B_CR:
                current += incre;
                break;
            case UA_A_PR:
                current += random.nextExp();
                break;
            case UA_A_CR:
                current += incre;
                break;
            case UU_UA_B_PR:
                current += random.nextExp();
                break;
            case UU_UA_B_CR:
                current += incre;
                break;
            case GREEDY:
                current += incre;
                break;
            case PADDING:
                break;


        }
        return last;
    }
}
