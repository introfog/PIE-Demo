package com.introfog.pie.demo;

import com.introfog.pie.core.World;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Main {
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 700;
    public static final boolean ENABLE_DEBUG_DRAW = false;

    public static World world = new World();

    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        JFrame frame = new JFrame("Demo PIE");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.add(new Display());
        frame.setVisible(true);
    }
}