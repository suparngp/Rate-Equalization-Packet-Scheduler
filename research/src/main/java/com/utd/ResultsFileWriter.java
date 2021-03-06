package com.utd;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.List;

/**
 * Created by suparngupta on 3/16/14.
 */
public class ResultsFileWriter {

    public synchronized  void dumpCSV(List<Packet> packets, String fileName, boolean create) {

        ICsvBeanWriter beanWriter = null;
        try {
            File file = new File(fileName);
            final String[] header = new String[]{"flowId", "packetId", "arrivalTime", "startTime", "finishTime",
                    "length", "transmitted"};

            if(create){
                if(file.exists())
                    file.delete();

                file.createNewFile();

                beanWriter = new CsvBeanWriter(new FileWriter(file, true),
                        CsvPreference.STANDARD_PREFERENCE);
                // write the header
                beanWriter.writeHeader(header);
            }

            if(beanWriter == null)
            {
                beanWriter = new CsvBeanWriter(new FileWriter(file, !create),
                        CsvPreference.STANDARD_PREFERENCE);
            }

            // the header elements are used to map the bean values to each column (names must match)

            final CellProcessor[] processors = getProcessors();



            // write the beans
            for (final Packet packet : packets) {

                beanWriter.write(packet, header, processors);
            }


            if (beanWriter != null) {
                beanWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void dumpCSV2(Collection<Flow> flows, String fileName, boolean create) {

        System.out.println(fileName);
        ICsvBeanWriter beanWriter = null;
        try {
            File file = new File(fileName);
            final String[] header = new String[]{"flowId", "currentTime", "minimumBandwidth", "allocatedBandwidth"};


            if(create){
                if(file.exists())
                    file.delete();

                file.createNewFile();

                beanWriter = new CsvBeanWriter(new FileWriter(file, true),
                        CsvPreference.STANDARD_PREFERENCE);
                // write the header

                beanWriter.writeHeader(header);
                beanWriter.flush();
                System.out.println("Header Written");
            }

            if(beanWriter == null){
                beanWriter = new CsvBeanWriter(new FileWriter(file, true),
                        CsvPreference.STANDARD_PREFERENCE);
            }
            // the header elements are used to map the bean values to each column (names must match)

            final CellProcessor[] processors = new CellProcessor[]{
                    null, null, null, null
            };



            // write the beans
            for (final Flow flow : flows) {
                beanWriter.write(flow, header, processors);
            }

            if (beanWriter != null) {
                beanWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CellProcessor[] getProcessors() {
        return new CellProcessor[]{
                null, null, null, null, null, null, null
        };
    }




    public synchronized void dumpCSV3(Collection<TotalDataFr> totalDataTracker, String fileName, boolean create) {

        ICsvBeanWriter beanWriter = null;
        try {
            File file = new File(fileName);
            final String[] header = new String[]{"flowId", "timestamp", "totalData", "totalPackets"};


            if(create){
                if(file.exists())
                    file.delete();

                file.createNewFile();

                beanWriter = new CsvBeanWriter(new FileWriter(file, true),
                        CsvPreference.STANDARD_PREFERENCE);
                // write the header

                beanWriter.writeHeader(header);
                beanWriter.flush();
                System.out.println("Header Written");
            }

            if(beanWriter == null){
                beanWriter = new CsvBeanWriter(new FileWriter(file, true),
                        CsvPreference.STANDARD_PREFERENCE);
            }
            // the header elements are used to map the bean values to each column (names must match)

            final CellProcessor[] processors = new CellProcessor[]{
                    null, null, null, null
            };



            // write the beans
            for (final TotalDataFr tdr : totalDataTracker) {
                beanWriter.write(tdr, header, processors);
            }

            if (beanWriter != null) {
                beanWriter.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
