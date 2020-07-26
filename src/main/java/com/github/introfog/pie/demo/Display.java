package com.github.introfog.pie.demo;

import com.github.introfog.pie.core.Body;
import com.github.introfog.pie.core.shape.Circle;
import com.github.introfog.pie.core.shape.IShape;
import com.github.introfog.pie.core.shape.Polygon;
import com.github.introfog.pie.core.math.MathPIE;
import com.github.introfog.pie.core.math.Vector2f;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

import static com.github.introfog.pie.demo.Main.world;

public class Display extends JPanel implements ActionListener {
    private long previousTime;

    public Display() {
        Timer timer = new Timer(0, this);
        timer.start();
        addMouseListener(new MouseEvents());

        initializeBodies();
        world.setCollisionSolveIterations(10);
        previousTime = System.nanoTime();
    }

    public void paint(Graphics g) {
        long currentTime = System.nanoTime();
        float deltaTime = (currentTime - previousTime) / 1_000_000_000f;
        previousTime = currentTime;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.BLACK);

        g.drawString("FPS: " + (int) (1 / deltaTime), 2, 12);
        g.drawString("Bodies: " + world.getUnmodifiableShapes().size(), 2, 24);
        g.drawString("Version: 1.1-SNAPSHOT", 2, 36);

        world.update(deltaTime);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    private void initializeBodies() {
        Circle circle = new Circle(40f, 220f, 350f, MathPIE.STATIC_BODY_DENSITY, 0.2f);
        world.addShape(circle);

        Vector2f[] vertices = {new Vector2f(20f, -20f), new Vector2f(40f, 20f), new Vector2f(0f, 60f),
                new Vector2f(-60f, 40f), new Vector2f(-40f, 0f), new Vector2f(0f, 0f)};
        Polygon polygon = new Polygon(MathPIE.STATIC_BODY_DENSITY, 0.2f, 470f, 400f, vertices);
        world.addShape(polygon);
    }

    private void draw(Graphics graphics) {
        world.getUnmodifiableShapes().forEach((shape) -> {
            Body body = shape.body;
            if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;

                if (Main.ENABLE_DEBUG_DRAW) {
                    drawAABB(graphics, polygon);
                }

                graphics.setColor(Color.BLUE);
                Vector2f tmpV = new Vector2f();
                Vector2f tmpV2 = new Vector2f();
                for (int i = 0; i < polygon.vertexCount; i++) {
                    tmpV.set(polygon.vertices[i]);
                    polygon.rotateMatrix.mul(tmpV, tmpV);
                    tmpV.add(body.position);

                    tmpV2.set(polygon.vertices[(i + 1) % polygon.vertexCount]);
                    polygon.rotateMatrix.mul(tmpV2, tmpV2);
                    tmpV2.add(body.position);
                    graphics.drawLine((int) tmpV.x, (int) tmpV.y, (int) tmpV2.x,
                            (int) tmpV2.y);
                }

                graphics.drawLine((int) body.position.x, (int) body.position.y, (int) body.position.x,
                        (int) body.position.y);
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;

                if (Main.ENABLE_DEBUG_DRAW) {
                    drawAABB(graphics, circle);
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
        if (Main.ENABLE_DEBUG_DRAW) {
            graphics.setColor(Color.GREEN);
            world.getCollisions().forEach((collision) -> {
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

    private void drawAABB(Graphics graphics, IShape shape) {
        shape.computeAABB();
        graphics.setColor(Color.GRAY);
        graphics.drawRect((int) shape.aabb.min.x, (int) shape.aabb.min.y,
                (int) (shape.aabb.max.x - shape.aabb.min.x),
                (int) (shape.aabb.max.y - shape.aabb.min.y));
    }
}