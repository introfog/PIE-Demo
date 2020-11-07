package com.github.introfog.pie.demo;

import com.github.introfog.pie.core.Body;
import com.github.introfog.pie.core.Context;

import com.github.introfog.pie.core.collisions.broadphase.AabbTreeMethod;
import com.github.introfog.pie.core.collisions.broadphase.aabbtree.AabbTreeNode;
import com.github.introfog.pie.core.math.MathPie;
import com.github.introfog.pie.core.shape.Aabb;
import com.github.introfog.pie.core.shape.Circle;
import com.github.introfog.pie.core.shape.Polygon;
import com.github.introfog.pie.core.math.Vector2f;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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
        try {
            draw(g);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    private void initializeBodies() {
        Circle circle = new Circle(40f, 220f, 350f, MathPie.STATIC_BODY_DENSITY, 0.2f);
        world.addShape(circle);

        List<Vector2f> vertices = new ArrayList<>();
        vertices.add(new Vector2f(20f, -20f));
        vertices.add(new Vector2f(40f, 20f));
        vertices.add(new Vector2f(0f, 60f));
        vertices.add(new Vector2f(-60f, 40f));
        vertices.add(new Vector2f(-40f, 0f));
        vertices.add(new Vector2f(0f, 0f));
        Polygon polygon = new Polygon(MathPie.STATIC_BODY_DENSITY, 0.2f, 470f, 400f, vertices);
        world.addShape(polygon);
    }

    private void drawNode(Graphics graphics, AabbTreeNode node) throws NoSuchFieldException, IllegalAccessException {
        Field field = node.getClass().getDeclaredField("aabb");
        field.setAccessible(true);
        Aabb aabb = (Aabb) field.get(node);
        drawAABB(graphics, aabb);
        field = node.getClass().getDeclaredField("children");
        field.setAccessible(true);
        AabbTreeNode[] children = (AabbTreeNode[]) field.get(node);
        if (children[0] != null) {
            drawNode(graphics, children[0]);
            drawNode(graphics, children[1]);
        }
    }

    private void draw(Graphics graphics) throws NoSuchFieldException, IllegalAccessException {
        if (Main.ENABLE_AABB_TREE_DRAW) {
            Field field = world.getClass().getDeclaredField("context");
            field.setAccessible(true);
            Context context = (Context) field.get(world);
            AabbTreeMethod method = (AabbTreeMethod) context.getBroadPhaseMethod();
            field = method.getClass().getDeclaredField("root");
            field.setAccessible(true);
            AabbTreeNode root = (AabbTreeNode) field.get(method);
            drawNode(graphics, root);
        }

        world.getUnmodifiableShapes().forEach((shape) -> {
            shape.computeAabb();
            Body body = shape.body;
            if (shape instanceof Polygon) {
                Polygon polygon = (Polygon) shape;

                if (Main.ENABLE_DEBUG_DRAW) {
                    drawAABB(graphics, polygon.aabb);
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
                    drawAABB(graphics, circle.aabb);
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
            world.getManifolds().forEach((collision) -> {
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

    private void drawAABB(Graphics graphics, Aabb aabb) {
        graphics.setColor(Color.GRAY);
        graphics.drawRect((int) aabb.min.x, (int) aabb.min.y,
                (int) (aabb.max.x - aabb.min.x),
                (int) (aabb.max.y - aabb.min.y));
    }
}