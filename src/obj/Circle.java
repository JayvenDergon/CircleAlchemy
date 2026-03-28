package obj;

import handlers.OkLabControl;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * A circle object containing the data for everything related with the circles.
 */
public class Circle {

    public double posX;
    public double posY; //Position
    public double radius; //Size
    public double velocityX;
    public double velocityY; //Movement

    //Will be used for merging effects
    public double morphScaleX      = 1;
    public double morphScaleY      = 1;
    public double drawAngle        = 0; //For a directional effect when merging
    public float glowIntensity     = 0f; //Effects for merging
    public int popCountdown        = -1;
    public boolean isSpawning      = false; //For spawning logic (No toroidal wrapping when first spawning).
    public double pendingVelocityX = 0;
    public double pendingVelocityY = 0;
    public double pendingMass      = 0;

    public boolean isSplinter;
    public Color color; //Color of the circle.

    //Constructor
    public Circle(double posX, double posY, double radius, double velocityX, double velocityY, Color color, boolean isSplinter) {

        this.posX       = posX;
        this.posY       = posY;
        this.radius     = radius;
        this.velocityX  = velocityX;
        this.velocityY  = velocityY;
        this.color      = color;
        this.isSplinter = isSplinter;

    }

    public Circle (double posX, double posY, double radius, double velocityX, double velocityY, Color color) {

        this(posX, posY, radius, velocityX, velocityY, color, false);

    }

    /**
     * Creates new circles, then promptly added to the world.
     * All circles start moving inwards into the map so they don't go out of bounds.
     */
    public static void spawnCircle(ArrayList<Circle> circles, Random random,
                                    int worldHeight, int worldWidth,
                                    double spawnX, double spawnY,
                                    double radius, boolean fromEdge) {

        double velocityX = (random.nextDouble() * 6) - 3;
        double velocityY = (random.nextDouble() * 6) - 3;

        if (fromEdge) {

            if      (spawnX < 0)           velocityX =  Math.abs(velocityX) + 0.6; //Left

            else if (spawnX > worldWidth)  velocityX = -Math.abs(velocityX) - 0.6; //Right

            if      (spawnY < 0)           velocityY =  Math.abs(velocityY) + 0.6; //Bottom

            else if (spawnY > worldHeight) velocityY = -Math.abs(velocityY) - 0.6; //Top

        }

        Color randomColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        Circle newCircle = new Circle(spawnX, spawnY, radius, velocityX, velocityY, randomColor);
        newCircle.isSpawning = fromEdge;
        circles.add(newCircle);

    }

    /**
     * Spawns one circle from a random screen edge.
     * Called whenever the population drops below 800 as seen in the if function in update().
     */
    public static void spawnCircleFromBorder(ArrayList<Circle> circles, Random random,
                                              int worldHeight, int worldWidth) {

        //Radius of the Circle.
        double newRadius = random.nextInt(6) + 8;
        //This will tell the program from which edge the circle should spawn at.
        int    edge      = random.nextInt(4);
        double spawnX, spawnY;

        switch (edge) {

            case 0  -> { spawnX = -newRadius;             spawnY = random.nextInt(worldHeight); } //Left

            case 1  -> { spawnX = worldWidth + newRadius; spawnY = random.nextInt(worldHeight); } //Right

            case 2  -> { spawnX = random.nextInt(worldWidth); spawnY = worldHeight + newRadius; } //Bottom

            default -> { spawnX = random.nextInt(worldWidth); spawnY = -newRadius;              } //Top
        }

        spawnCircle(circles, random, worldHeight, worldWidth, spawnX, spawnY, newRadius, true);
    }

    /**
     * Draws one circle (or if possibly, one of the toroidal ghosts) at the given screen position.
     * No rendering if outside the plane (For the ghosts in particular that are not within the plane yet).
     */
    public static void drawCircle(int worldHeight, int worldWidth,
                                  Graphics2D g2, Circle c,
                                  double tx, double ty) {

        if (tx + c.radius < 0 || tx - c.radius > worldWidth || ty + c.radius < 0 || ty - c.radius > worldHeight) return;

        double jitterX = (c.popCountdown > 0) ? (Math.random() * 6) - 3 : 0;
        double jitterY = (c.popCountdown > 0) ? (Math.random() * 6) - 3 : 0;

        AffineTransform savedTransform = g2.getTransform();
        g2.translate(tx + jitterX, ty + jitterY);

        if (c.glowIntensity > 0.01) {

            float glowRadius = (float) c.radius * 1.5f;
            g2.setPaint(new RadialGradientPaint(0, 0, glowRadius,
                    new float[]{ 0f, 0.5f, 1f },
                    new Color[]{
                            new Color(255, 255, 255, (int) (160 * c.glowIntensity)),
                            new Color(c.color.getRed(), c.color.getGreen(), c.color.getBlue(), (int) (80 * c.glowIntensity)),
                            new Color(0, 0, 0, 0)
                    }));

            g2.fill(new Ellipse2D.Double(-glowRadius, -glowRadius, 2 * glowRadius, 2 * glowRadius));

        }

        g2.rotate(c.drawAngle);
        g2.setColor(c.color);
        double drawWidth = c.radius * 2 * c.morphScaleX;
        double drawHeight = c.radius * 2 * c.morphScaleY;
        g2.fill(new Ellipse2D.Double(-drawWidth / 2, -drawHeight / 2, drawWidth, drawHeight));

        g2.setTransform(savedTransform);

    }

    /**
     * Takes 2 circles that overlap and merges them. The larger one will always absorb the smaller one.
     * This will conserve momentum by combining the velocity of both circles relative to their masses.
     * This will also blend their colors and have a visual effect for a nice little detail.
     */
    public static void mergeCircles(Circle circleA, Circle circleB,
                                    HashSet<Circle> absorbedSet,
                                    double collisionDeltaX, double collisionDeltaY) {

        double survivorMass, consumedMass, totalMass, colorWeight, stretchAmount;

        Circle survivor = (circleA.radius >= circleB.radius) ? circleA : circleB;
        Circle consumed = (circleA.radius >= circleB.radius) ? circleB : circleA;

        survivorMass = survivor.radius * survivor.radius;
        consumedMass = consumed.radius * consumed.radius;
        totalMass    = survivorMass + consumedMass;

        //This will make sure that the velocity of both objects is added (or subtracted) proportional to their mass to conserved momentum.
        if (survivor.pendingMass == 0) {

            survivor.pendingVelocityX = survivor.velocityX * survivorMass;
            survivor.pendingVelocityY = survivor.velocityY * survivorMass;
            survivor.pendingMass      = survivorMass;

        }

        survivor.pendingVelocityX += consumed.velocityX * consumedMass;
        survivor.pendingVelocityY += consumed.velocityY * consumedMass;
        survivor.pendingMass      += consumedMass;

        //Color blending function.
        colorWeight    = consumedMass / totalMass;
        survivor.color = blendColors(survivor.color, consumed.color, colorWeight);

        //A little squishing effect for whenever a merge happens. This is directional relative to the position of the merging. + Glow
        survivor.drawAngle   = Math.atan2(collisionDeltaY, collisionDeltaX);
        stretchAmount        = (consumedMass / totalMass) / 0.5;
        survivor.morphScaleX = 1.0 + stretchAmount;
        survivor.morphScaleY = 1.0 - (stretchAmount * 0.4);
        survivor.glowIntensity = 0.75f;

        //Get new radius.
        survivor.radius = Math.sqrt(totalMass);

        //Add consumed circle to Set that will be then removed.
        absorbedSet.add(consumed);

    }

    /**
     * A linear interpolation of the colors of the 2 circles that merge.
     */
    public static Color blendColors(Color colorA, Color colorB, double weight) {

        double[] labA = OkLabControl.rgbToOklab(colorA);
        double[] labB = OkLabControl.rgbToOklab(colorB);

        double l = labA[0] * (1 - weight) + labB[0] * weight;
        double a = labA[1] * (1 - weight) + labB[1] * weight;
        double b = labA[2] * (1 - weight) + labB[2] * weight;

        return OkLabControl.oklabToRgb(l, a, b);

    }

    public static void handleCirclePops(ArrayList<Circle> circles, Random random, ArrayList<Circle> splinters) {

        Iterator<Circle> iterator = circles.iterator();

        while (iterator.hasNext()) {

            Circle circle = iterator.next();

            //If the circle is too large, start the timer.
            if (!circle.isSplinter && circle.radius > 50 && circle.popCountdown == -1) {

                circle.popCountdown = 15;

            }

            if (circle.popCountdown > 0) {

                circle.popCountdown--;

            } else if (circle.popCountdown == 0) {

                for (int i = 0; i < 15; i++) {

                    double splinterAngle = random.nextDouble() * Math.PI * 2;
                    double splinterSpeed = random.nextDouble() * 4 + 2;
                    splinters.add(new Circle(
                            circle.posX, circle.posY,
                            random.nextDouble() * 2 + 1,
                            Math.cos(splinterAngle) * splinterSpeed + circle.velocityX,
                            Math.sin(splinterAngle) * splinterSpeed + circle.velocityY,
                            circle.color, true
                    ));

                }

                iterator.remove();

            }

        }

    }

}