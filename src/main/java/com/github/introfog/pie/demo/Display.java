package com.github.introfog.pie.demo;

import com.github.introfog.pie.core.Body;
import com.github.introfog.pie.core.collisions.broadphase.AbstractBroadPhase;
import com.github.introfog.pie.core.collisions.broadphase.BruteForceMethod;
import com.github.introfog.pie.core.shape.Circle;
import com.github.introfog.pie.core.shape.IShape;
import com.github.introfog.pie.core.shape.Polygon;
import com.github.introfog.pie.core.math.MathPIE;
import com.github.introfog.pie.core.math.Vector2f;
import com.github.introfog.pie.core.util.ShapeIOUtil;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

import static com.github.introfog.pie.demo.Main.world;

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

    public Display() throws IOException {
        Timer timer = new Timer(0, this);
        timer.start();
        addMouseListener(new MouseEvents());

        initializeBodies();
        //initializeShapesForTest();
        world.setCollisionSolveIterations(10);
        previousTime = System.nanoTime();

//        try {
//            //out = new PrintWriter(new FileWriter("./target/test/com/github/introfog/pie/demo/Test something.txt"));
//            out = new PrintWriter(new FileWriter(".\\Test something.txt"));
//        } catch (IOException e) {
//            System.out.println("Error with new FileWriter: " + e.getMessage());
//        }
    }

    public void paint(Graphics g) {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - previousTime) / 1_000_000_000f;
        previousTime = currentTime;

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.BLACK);

        g.drawString("FPS: " + (int) (1 / deltaTime), 2, 12);
        g.drawString("Bodies: " + world.getUnmodifiableShapes().size(), 2, 24);
        g.drawString("Version: 1.1-SNAPSHOT", 2, 36);

        //testProductivity ();
        //testBodiesPenetration ();
        //rectangle.setOrientation (rectangle.body.orientation + 0.001f);

        world.update(deltaTime);
        draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    private void initializeShapesForTest() throws IOException {
        List<IShape> shapes = new ArrayList<>();
        final float RADIUS = 5;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                shapes.add(new Circle(RADIUS, 50f + i * (2 * RADIUS - 1), 50f + j * (2 * RADIUS - 1), MathPIE.STATIC_BODY_DENSITY, 0.2f));
            }
        }
        ShapeIOUtil.writeShapesToFile(shapes, ".\\Core\\src\\test\\resource\\com\\introfog\\pie\\core\\2500shapes_simple collision.json");

        AbstractBroadPhase broadPhase = new BruteForceMethod();
        broadPhase.setShapes(shapes);
        ShapeIOUtil.writeShapePairsToFile(broadPhase.calculateAabbCollisions(),
                ".\\Core\\src\\test\\resource\\com\\introfog\\pie\\core\\2500shapes_simple collision_answer.json");
        world.setShapes(shapes);
    }

    private void initializeBodies() {
        circle = new Circle(40f, 220f, 350f, MathPIE.STATIC_BODY_DENSITY, 0.2f);
        world.addShape(circle);

        Vector2f[] vertices = {new Vector2f(20f, -20f), new Vector2f(40f, 20f), new Vector2f(0f, 60f),
                new Vector2f(-60f, 40f), new Vector2f(-40f, 0f), new Vector2f(0f, 0f)};
        polygon = new Polygon(MathPIE.STATIC_BODY_DENSITY, 0.2f, 470f, 400f, vertices);
        world.addShape(polygon);
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

            if (world.getUnmodifiableShapes().size() % 2 == 0) {
                rectangle = Polygon
                        .generateRectangle(currXNewBody * (SIZE + 1f) + SIZE / 2f, currYNewBody + SIZE / 2f, SIZE, SIZE,
                                1f,
                                0.2f);
                world.addShape(rectangle);
                float dt = deltaTime * 100000;
                dt = Math.round(dt);
                dt /= 100000;
                out.print("Bodies: " + world.getUnmodifiableShapes().size() + "\tdt: " + dt);
                out.println("\tMay be collision bodies: \tIntersects oper.: some integer");
                out.flush();
            } else {
                circle = new Circle(SIZE / 2f, currXNewBody * (SIZE + 1f) + SIZE / 2f, currYNewBody + SIZE / 2f, 0.4f, 0.5f);
                world.addShape(circle);
            }
            currXNewBody++;
        }
        timer -= deltaTime;
    }

    private void testBodiesPenetration() {
        if (timer <= 0) {
            timer = TIMER * 10f;

            rectangle = Polygon.generateRectangle(400f, currYNewBody, SIZE, SIZE, 0.4f, 0.5f);
            world.addShape(rectangle);
        }
        timer -= deltaTime;
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