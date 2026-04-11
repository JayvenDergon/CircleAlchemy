package obj;

import handlers.OkLabControl;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class Triangle {

    public double posX, posY, velocityX, velocityY, heading;
    public double movementSpeed = 4;
    public double collisionRadius = 16;
    public double colorPhase;

    public Triangle(double posX, double posY) {

        this.posX       = posX;
        this.posY       = posY;
        this.heading    = new Random().nextDouble() * Math.PI * 2;
        this.colorPhase = new Random().nextDouble() * 100;

    }

    /**
    * Responsible for rendering triangles, it works more or less the same way it goes for Circles.
    */
    public static void drawTriangle(Graphics2D g2, Triangle triangle, double tx, double ty, int worldWidth, int worldHeight) {

        if (tx + triangle.collisionRadius < 0 || tx - triangle.collisionRadius > worldWidth || ty + triangle.collisionRadius < 0 || ty - triangle.collisionRadius > worldHeight) return;

        AffineTransform savedTransform = g2.getTransform();
        g2.translate(tx, ty);
        g2.rotate(triangle.heading + Math.PI /2);

        double lightness = 0.75 + Math.sin(triangle.colorPhase * 2) * 0.05;
        g2.setColor(OkLabControl.oklabToRgb(lightness, Math.cos(triangle.colorPhase) * 0.15, Math.sin(triangle.colorPhase) * 0.15));

        double radius = triangle.collisionRadius;
        int[] verticesX = { 0, (int) (radius * Math.sqrt(3) / 2), (int) (-radius * Math.sqrt(3) / 2)};
        int[] verticesY = { (int) -radius, (int) (radius / 2), (int) (radius / 2) };
        g2.fillPolygon(verticesX, verticesY, 3);
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawPolygon(verticesX, verticesY, 3);

        g2.setTransform(savedTransform);

    }

    public static class RespawnTimer {

        public int framesRemaining;

        public RespawnTimer(int initialFrames) {

            this.framesRemaining = initialFrames;

        }

    }

}