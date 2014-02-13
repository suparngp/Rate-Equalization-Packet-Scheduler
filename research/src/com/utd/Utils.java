package com.utd;

/**
 * Created by suparngupta on 1/25/14.
 */
public class Utils {
    public static void log(Object... messages){
        System.out.print("Log: ");
        for(int i = 0; i < messages.length; i++){
            System.out.print(messages[i]);
        }
        System.out.println();
        System.out.println();
    }

    public static void debug(Object... messages){
        System.out.print("Debug: ");
        for(int i = 0; i < messages.length; i++){
            System.out.print(messages[i]);
        }
        System.out.println();
        System.out.println();
    }

    public static void error(Object... messages){
        System.err.print("Error: ");
        for(int i = 0; i < messages.length; i++){
            System.err.print(messages[i]);
        }
        System.err.println();
        System.out.println();
    }
}
