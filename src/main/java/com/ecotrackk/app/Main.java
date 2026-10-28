
package com.ecotrackk.app;

import com.ecotrackk.util.JPAUtil;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Starting EcoTrackk...");

            new ConsoleMenu().start();

        } catch (Exception e) {
            System.out.println("Application error!");
            e.printStackTrace();

        } finally {
            JPAUtil.shutdown();
        }
    }
}
