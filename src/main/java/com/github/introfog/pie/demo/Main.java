package com.github.introfog.pie.demo;

import com.github.introfog.pie.core.Context;
import com.github.introfog.pie.core.World;
import com.github.introfog.pie.core.collisions.broadphase.AabbTreeMethod;
import com.github.introfog.pie.core.collisions.broadphase.SpatialHashingMethod;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Main {
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 700;
    public static final boolean ENABLE_DEBUG_DRAW = true;
    public static final boolean ENABLE_AABB_TREE_DRAW = false;

    public static World world;

    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        if (ENABLE_AABB_TREE_DRAW) {
            world = new World(new Context().setBroadPhaseMethod(new AabbTreeMethod()));
        } else {
            world = new World(new Context().setBroadPhaseMethod(new SpatialHashingMethod()));
        }

        JFrame frame = new JFrame("Demo PIE");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.add(new Display());
        frame.setVisible(true);
    }
}