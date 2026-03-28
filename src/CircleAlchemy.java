import obj.Circle;
import obj.Triangle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.HashSet;

import static obj.Circle.*;
import static obj.Triangle.drawTriangle;

/**
 * Balls
 * @author Feathers
 * @version 1.0.1
 */

static class CircleAlchemy extends JPanel {

    /**
     * This will list every active Circle in the map.
     */
    private final ArrayList<Circle> circles = new ArrayList<>();

    /**
     * This will list every Triangle in the map.
     */
    private final ArrayList<Triangle> triangles = new ArrayList<>();

    /**
     * This will be used for RNG.
     */
    private final Random random = new Random();

    /**
     * This is the width of the map, and it'll be set to the current screen's width.
     */
    private final int worldWidth;

    /**
     * This is the height of the map, and it'll be set to the current screen's height.
     */
    private final int worldHeight;

    CircleAlchemy() {

        //Take the current screen's resolution and set it as the size of the map.
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        worldWidth = screen.width;
        worldHeight = screen.height;

        //Start the world with 800 circles at random positions and radius from 8 to 14 pixels.
        for (int i = 0; i < 800; i++)
            spawnCircle(circles, random, worldHeight, worldWidth, random.nextInt(worldWidth), random.nextInt(worldHeight),
                    random.nextInt(7) + 8, false);

        for (int i = 0; i < 5; i++) triangles.add(new Triangle(random.nextInt(worldWidth), random.nextInt(worldHeight)));

        //60 fps game loop.
        new Timer(16, _ -> {

            update();
            repaint();

        }).start();

    }

    private void update() {

        HashSet<Circle>   absorbedCircles = new HashSet<>();
        ArrayList<Circle> newSplinters    = new ArrayList<>();

        //Triangles with their respective logic
        for (Triangle triangle : triangles) {

            triangle.heading    += (random.nextDouble() * 0.2) - 0.1;
            triangle.colorPhase += 0.005;

            triangle.velocityX = Math.cos(triangle.heading) * triangle.movementSpeed;
            triangle.velocityY = Math.sin(triangle.heading) * triangle.movementSpeed;

            triangle.posX = (triangle.posX + triangle.velocityX + worldWidth) % worldWidth;
            triangle.posY = (triangle.posY + triangle.velocityY + worldHeight) % worldHeight;

            for (Circle circle : circles) {

                if (circle.isSplinter || circle.popCountdown >= 0) continue;

                double deltaX      = wrappedDelta(triangle.posX, circle.posX, worldWidth);
                double deltaY      = wrappedDelta(triangle.posY, circle.posY, worldHeight);
                double touchDist = circle.radius + triangle.collisionRadius;

                if (deltaX * deltaX + deltaY * deltaY < touchDist * touchDist) {

                    circle.popCountdown = 0;

                }

            }

        }

        //Merging logic
        for (int i = 0; i < circles.size(); i++) {

            Circle circleA = circles.get(i);
            if (absorbedCircles.contains(circleA) || circleA.popCountdown >= 0) continue;

            for (int j = i + 1; j < circles.size(); j++) {

                Circle circleB = circles.get(j);
                if (absorbedCircles.contains(circleB) || circleB.popCountdown >= 0) continue;
                if (circleA.isSplinter && circleB.isSplinter) continue;

                double deltaX      = wrappedDelta(circleB.posX, circleA.posX, worldWidth);
                double deltaY      = wrappedDelta(circleB.posY, circleA.posY, worldHeight);
                double touchRadius = circleA.radius + circleB.radius;

                if (deltaX * deltaX + deltaY * deltaY < touchRadius * touchRadius) {

                    mergeCircles(circleA, circleB, absorbedCircles, deltaX, deltaY);

                }

            }

        }

        circles.removeAll(absorbedCircles);

        for (Circle circle : circles) {

            if (circle.pendingMass > 0) {

                circle.velocityX   = circle.pendingVelocityX / circle.pendingMass;
                circle.velocityY   = circle.pendingVelocityY / circle.pendingMass;
                circle.pendingMass = 0;

            }

        }

        //Move all the circles around in a fixed velocity and direction.
        for (Circle circle : circles) {

            circle.posX += circle.velocityX;
            circle.posY += circle.velocityY;

            if (circle.isSpawning) {

                //Prevent toroidal wrapping if the circle just spawned, so they don't appear awkwardly and suddenly on multiple edges.
                if (circle.posX > 0 && circle.posX < worldWidth && circle.posY > 0 && circle.posY < worldHeight)
                    circle.isSpawning = false;

            } else {

                //Toroidal Wrapping
                circle.posX = (circle.posX + worldWidth) % worldWidth;
                circle.posY = (circle.posY + worldHeight) % worldHeight;

            }

            //This should apply the gravity effect for the splinters
            if (circle.isSplinter) {

                applySplinterGravity(circle);
                circle.velocityX *= 0.99;
                circle.velocityY *= 0.99;

            }

            //A little animation of a squish, due to the multiplication it will ease back to normal.
            circle.morphScaleX += (1.0 - circle.morphScaleX) * 0.1;
            circle.morphScaleY += (1.0 - circle.morphScaleY) * 0.1;

            //Same but with a glow.
            circle.glowIntensity *= 0.98f;

        }

        //This will make it so when a circle gets too big, it'll pop into a bunch of splinter circles.
        handleCirclePops(circles, random, newSplinters);
        circles.addAll(newSplinters);

        //Maintain a minimum population of at least 800 circles.
        if (circles.size() <= 800) spawnCircleFromBorder(circles, random, worldHeight, worldWidth);

    }

    /**
     * This will make sure that collision will work properly through screen seams.
     */
    private double wrappedDelta(double to, double from, int axisSize) {

        double delta = to - from;
        if (delta > axisSize / 2.0) delta -= axisSize;
        else if (delta < -axisSize / 2.0) delta += axisSize;
        return delta;

    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(5, 5, 10));
        g2.fillRect(0, 0, getWidth(), getHeight());

        //Draw each circle with up to 8 toroidal ghost copies for the toroidal wrapping logic
        for (Circle circle : circles) {

            if (circle.isSpawning) {

                drawCircle(worldHeight, worldWidth, g2, circle, circle.posX, circle.posY);

            } else {

                for (int xOffset = -1; xOffset <= 1; xOffset++)

                    for (int yOffset = -1; yOffset <= 1; yOffset++)
                        drawCircle(worldHeight, worldWidth, g2, circle,
                                circle.posX + xOffset * worldWidth,
                                circle.posY + yOffset * worldHeight);

            }

        }

        for (Triangle triangle : triangles) {

            for (int xOffset = -1; xOffset <= 1; xOffset++)

                for (int yOffset = -1; yOffset <= 1; yOffset++)

                    drawTriangle(g2, triangle,
                            triangle.posX + xOffset * worldWidth,
                            triangle.posY + yOffset * worldHeight,
                            worldWidth, worldHeight);

        }

    }

    /**
     * The primary function of this is to give the splinters the ability to recognize gravity to then later have the speed altered every frame to home into other nearby circles.
     */
    private void applySplinterGravity(Circle splinter) {

        for (Circle other : circles) {

            if (other.isSplinter || other.isSpawning) continue;

            double deltaX = wrappedDelta(other.posX, splinter.posX, worldWidth);
            double deltaY = wrappedDelta(other.posY, splinter.posY, worldHeight);
            double distSq = deltaX * deltaX + deltaY * deltaY;

            if (distSq < 40000 && distSq > 10) {

                double dist = Math.sqrt(distSq);
                splinter.velocityX += (deltaX / dist) * (other.radius * 0.25 / dist);
                splinter.velocityY += (deltaY / dist) * (other.radius * 0.25 / dist);

            }

        }

    }

}

void main() {

    JFrame frame = new JFrame();
    frame.setUndecorated(true);
    frame.add(new CircleAlchemy());

    frame.addKeyListener(new KeyAdapter() {

        public void keyPressed(KeyEvent e) {

            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);

        }

    });

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    frame.setVisible(true);

}
