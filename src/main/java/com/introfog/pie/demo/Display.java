package com.introfog.pie.demo;

import com.introfog.pie.core.Circle;
import com.introfog.pie.core.Polygon;
import com.introfog.pie.core.World;
import com.introfog.pie.core.collisionDetection.BroadPhase;
import com.introfog.pie.core.math.MathPIE;
import com.introfog.pie.core.math.Vector2f;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JPanel;
import javax.swing.Timer;

public class Display extends JPanel implements ActionListener {
    private float deltaTime;
    private long previousTime;

    private float TIMER = 0.05f;
    private float timer = TIMER;
    private Polygon rectangle;
    private Circle circle;
    private Polygon polygon;
    private float SIZE = 10f;

    private float START_Y_GENERATE_BODY = 100f;
    private float currYNewBody = START_Y_GENERATE_BODY;
    private float currXNewBody = 0f;
    private PrintWriter out;


    private void initializeBodies() {
        circle = new Circle(40f, 220f, 350f, MathPIE.STATIC_BODY_DENSITY, 0.2f);
        World.getInstance().addShape(circle);

        Vector2f[] vertices = {new Vector2f(20f, -20f), new Vector2f(40f, 20f), new Vector2f(0f, 60f),
                new Vector2f(-60f, 40f), new Vector2f(-40f, 0f), new Vector2f(0f, 0f)};
        polygon = new Polygon(MathPIE.STATIC_BODY_DENSITY, 0.2f, 470f, 400f, vertices);
        World.getInstance().addShape(polygon);
    }

    private void testProductivity() {
        if (currYNewBody > (float) Main.WINDOW_HEIGHT - 3 * SIZE) {
            currYNewBody = START_Y_GENERATE_BODY;
        }

        if (timer <= 0) {
            timer = TIMER;
            if (currXNewBody * (SIZE + 1f) + SIZE >= (float) Main.WINDOW_WIDTH) {
                currXNewBody = 0f;
                currYNewBody += SIZE + 1f;
            }

            if (World.getInstance().getAmountBodies() % 2 == 0) {
                rectangle = Polygon
                        .generateRectangle(currXNewBody * (SIZE + 1f) + SIZE / 2f, currYNewBody + SIZE / 2f, SIZE, SIZE,
                                1f,
                                0.2f);
                World.getInstance().addShape(rectangle);
                float dt = deltaTime * 100000;
                dt = Math.round(dt);
                dt /= 100000;
                out.print("Bodies: " + World.getInstance().getAmountBodies() + "\tdt: " + dt);
                out.println("\tMay be collision bodies: " + World.getInstance().amountMayBeCollisionBodies
                                + "\tIntersects oper.:" + BroadPhase.INTERSECTED_COUNTER);
                out.flush();
            } else {
                circle = new Circle(SIZE / 2f, currXNewBody * (SIZE + 1f) + SIZE / 2f, currYNewBody + SIZE / 2f, 0.4f, 0.5f);
                World.getInstance().addShape(circle);
            }
            currXNewBody++;
        }
        timer -= deltaTime;
    }

    private void testBodiesPenetration() {
        if (timer <= 0) {
            timer = TIMER * 10f;

            rectangle = Polygon.generateRectangle(400f, currYNewBody, SIZE, SIZE, 0.4f, 0.5f);
            World.getInstance().addShape(rectangle);
        }
        timer -= deltaTime;
    }

    private void draw(Graphics graphics) {
        World.getInstance().bodies.forEach((body) -> {
            if (body.shape instanceof Polygon) {
                Polygon polygon = (Polygon) body.shape;

                // Рисвание AABB
                if (World.getInstance().onDebugDraw) {
                    polygon.computeAABB();
                    graphics.setColor(Color.GRAY);
                    graphics.drawRect((int) polygon.aabb.min.x, (int) polygon.aabb.min.y,
                            (int) (polygon.aabb.max.x - polygon.aabb.min.x),
                            (int) (polygon.aabb.max.y - polygon.aabb.min.y));
                }

                graphics.setColor(Color.BLUE);

                for (int i = 0; i < polygon.vertexCount; i++) {
                    polygon.tmpV.set(polygon.vertices[i]);
                    polygon.rotateMatrix.mul(polygon.tmpV, polygon.tmpV);
                    polygon.tmpV.add(body.position);

                    polygon.tmpV2.set(polygon.vertices[(i + 1) % polygon.vertexCount]);
                    polygon.rotateMatrix.mul(polygon.tmpV2, polygon.tmpV2);
                    polygon.tmpV2.add(body.position);
                    graphics.drawLine((int) polygon.tmpV.x, (int) polygon.tmpV.y, (int) polygon.tmpV2.x,
                            (int) polygon.tmpV2.y);
                }

                graphics.drawLine((int) body.position.x, (int) body.position.y, (int) body.position.x,
                        (int) body.position.y);
            } else if (body.shape instanceof Circle) {
                Circle circle = (Circle) body.shape;

                // Рисвание AABB
                if (World.getInstance().onDebugDraw) {
                    circle.computeAABB();
                    graphics.setColor(Color.GRAY);
                    graphics.drawRect((int) circle.aabb.min.x, (int) circle.aabb.min.y,
                            (int) (circle.aabb.max.x - circle.aabb.min.x),
                            (int) (circle.aabb.max.y - circle.aabb.min.y));
                }
                graphics.setColor(Color.RED);
                graphics.drawLine((int) body.position.x, (int) body.position.y,
                        (int) body.position.x, (int) body.position.y);
                graphics.drawLine((int) body.position.x, (int) body.position.y,
                        (int) (body.position.x + circle.radius * Math.cos(body.orientation)),
                        (int) (body.position.y + circle.radius * Math.sin(body.orientation)));
                graphics.drawOval((int) (body.position.x - circle.radius), (int) (body.position.y - circle.radius),
                        (int) circle.radius * 2, (int) circle.radius * 2);
            }
        });

        // Рисвание нормалей к точкам касания в коллизии
        if (World.getInstance().onDebugDraw) {
            graphics.setColor(Color.GREEN);
            World.getInstance().collisions.forEach((collision) -> {
                for (int i = 0; i < collision.contactCount; i++) {
                    graphics.drawLine((int) collision.contacts[i].x, (int) collision.contacts[i].y,
                            (int) (collision.contacts[i].x + collision.normal.x * 10),
                            (int) (collision.contacts[i].y + collision.normal.y * 10));
                    graphics.drawLine((int) collision.contacts[i].x + 1, (int) collision.contacts[i].y + 1,
                            (int) (collision.contacts[i].x + collision.normal.x * 10 + 1),
                            (int) (collision.contacts[i].y + collision.normal.y * 10 + 1));
                }
            });
        }
    }


    public Display() {
        Timer timer = new Timer(0, this);
        timer.start();
        addMouseListener(new MouseEvents());

        initializeBodies();
        World.getInstance().iterations = 10;
        previousTime = System.nanoTime();

        try {
            out = new PrintWriter(new FileWriter(".\\tests\\Test something.txt"));
        } catch (IOException e) {
            System.out.println("Error with new FileWriter");
        }
    }

    public void paint(Graphics g) {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - previousTime) / 1_000_000_000f;
        previousTime = currentTime;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.BLACK);

        g.drawString("FPS: " + (int) (1 / deltaTime), 2, 12);
        g.drawString("Bodies: " + World.getInstance().getAmountBodies(), 2, 24);
        g.drawString("Version: 0.2", 2, 36);

        //testProductivity ();
        //testBodiesPenetration ();
        //rectangle.setOrientation (rectangle.body.orientation + 0.001f);

        World.getInstance().update(deltaTime);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}