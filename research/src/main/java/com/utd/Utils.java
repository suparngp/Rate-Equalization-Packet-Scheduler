package com.utd;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by suparngupta on 1/25/14.
 */
public class Utils {
    public static void log(Object... messages) {
        System.out.print("Log: ");
        for (int i = 0; i < messages.length; i++) {
            System.out.print(messages[i]);
        }
        System.out.println();
        System.out.println();
    }

    public static void debug(Object... messages) {
//        System.out.print("Debug: ");
//        for (int i = 0; i < messages.length; i++) {
//            System.out.print(messages[i]);
//        }
//        System.out.println();
//        System.out.println();
    }

    public static void error(Object... messages) {
        System.err.print("Error: ");
        for (int i = 0; i < messages.length; i++) {
            System.err.print(messages[i]);
        }
        System.err.println();
        System.out.println();
    }

    public static void dumpCSV(List<Packet> packets, String fileName) {

        ICsvBeanWriter beanWriter = null;
        try {
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            beanWriter = new CsvBeanWriter(new FileWriter(file),
                    CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the bean values to each column (names must match)
            final String[] header = new String[]{"flowId", "packetId", "arrivalTime", "startTime", "finishTime",
                    "length", "transmitted"};
            final CellProcessor[] processors = getProcessors();

            // write the header
            beanWriter.writeHeader(header);

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

    public static CellProcessor[] getProcessors() {
        return new CellProcessor[]{
                null, null, null, null, null, null, null
        };
    }
}
