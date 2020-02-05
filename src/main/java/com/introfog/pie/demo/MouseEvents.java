package com.introfog.pie.demo;

import com.introfog.pie.core.shape.Circle;
import com.introfog.pie.core.shape.Polygon;

import java.applet.Applet;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static com.introfog.pie.demo.Main.world;

public class MouseEvents extends Applet implements MouseListener {
    @Override
    public void init() {
        addMouseListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        float mouseX = me.getX();
        float mouseY = me.getY();

        if (me.getButton() == MouseEvent.BUTTON1) {
            Circle circle;
            float rand = (float) Math.random();
            circle = new Circle(rand * 20f + 5f, mouseX, mouseY, 1f, 0.2f);
            world.addShape(circle);
        } else if (me.getButton() == MouseEvent.BUTTON3) {
            Polygon rectangle;
            float rand = (float) Math.random();
            float height = rand * 80f + 20f;
            rand = (float) Math.random();
            float width = rand * 80f + 20f;
            rectangle = Polygon.generateRectangle(mouseX, mouseY, width, height, 1f, 0.2f);
            rectangle.setOrientation((float) Math.PI / 6f);
            world.addShape(rectangle);
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }
}
