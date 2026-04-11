package handlers;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class Explosion {

    public double  posX;
    public double posY;
    public int lifeFrames;
    int maxRadius;
    int maxRadiusSmall;
    boolean isSmall; //For later

    public Explosion(double posX, double posY, boolean isSmall) {

        this.posX = posX;
        this.posY = posY;
        this.isSmall = isSmall;
        this.maxRadius = 45;
        this.maxRadiusSmall = 28;
        this.lifeFrames = isSmall ? maxRadiusSmall : maxRadius;

    }

    public double outerRadius() {

        return isSmall ? (maxRadiusSmall - lifeFrames) * 5.5 : (maxRadius - lifeFrames) * 8.5;

    }

    double midRadius() {

        return isSmall ? Math.max(0, ((maxRadiusSmall * 0.80) - lifeFrames) * 4.5) : Math.max(0, ((maxRadius * 0.85) - lifeFrames) * 7.0);

    }

    double coreRadius() {

        return isSmall ? Math.max(0, ((maxRadiusSmall * 0.60) - lifeFrames) * 3.5) : Math.max(0, ((maxRadius * 0.66) - lifeFrames) * 5.0);

    }

    float outerAlpha() {

        return lifeFrames / (float) (isSmall ? maxRadiusSmall : maxRadius);

    }

    float midAlpha() {

        return isSmall ? Math.max(0, (lifeFrames - 4) / (float) (maxRadiusSmall * 0.80)) : Math.max(0, (lifeFrames - 7) / (float) (maxRadius * 0.85));

    }

    float coreAlpha() {

        return isSmall ? Math.max(0, (lifeFrames - 9) / (float) (maxRadiusSmall * 0.60)) : Math.max(0, (lifeFrames - 15) / (float) (maxRadius * 0.66));

    }

    public static class ExplosionParticle {

        public double posX, posY, velocityX, velocityY;
        public int lifeFrames;
        public int maxLifeFrames;
        public float drawRadius;
        public Color color;

        public ExplosionParticle(double spawnX, double spawnY, double angle, double speed, Color color) {

            this.posX = spawnX;
            this.posY = spawnY;
            this.velocityX = Math.cos(angle) * speed;
            this.velocityY = Math.sin(angle) * speed;
            this.lifeFrames = 40 + new Random().nextInt(20);
            this.maxLifeFrames = this.lifeFrames;
            this.drawRadius = (float) (1.5 + new Random().nextDouble() * 3);
            this.color = color;

        }

    }

    public static void drawExplosion(Graphics2D g2, Explosion boom) {

        double gradientRadius = boom.outerRadius() * 0.55;

        if (gradientRadius > 1) {

            float[] stops  = { 0f, 0.35f, 0.7f, 1f };
            Color[] colors = {

                    new Color(255, 255, 240, (int)(180 * boom.outerAlpha())),
                    new Color(255, 180,  60, (int)(120 * boom.outerAlpha())),
                    new Color(200,  60,  20, (int)( 60 * boom.outerAlpha())),
                    new Color(  0,   0,   0, 0)

            };

            try {

                g2.setPaint(new RadialGradientPaint((float) boom.posX, (float) boom.posY, (float) gradientRadius, stops, colors));

                g2.fill(new Ellipse2D.Double(boom.posX-gradientRadius, boom.posY - gradientRadius, gradientRadius * 2, gradientRadius * 2));

            } catch (Exception ignored) {

                //Later

            }

        }

        double outerR = boom.outerRadius();
        if (outerR > 1) {

            float alpha = boom.outerAlpha();
            g2.setColor(new Color(255, 220, 120, (int) (200 * alpha)));
            g2.setStroke(new BasicStroke(3.5f * alpha));
            g2.draw(new Ellipse2D.Double(boom.posX - outerR, boom.posY - outerR, outerR * 2, outerR * 2));

        }

        double midR = boom.midRadius();
        if (midR > 1)   {

            float alpha = boom.midAlpha();
            g2.setColor(new Color(255, 140, 60, (int) (180 * alpha)));
            g2.setStroke(new BasicStroke(2.5f * alpha));
            g2.draw(new Ellipse2D.Double(boom.posX - midR, boom.posY - midR, midR * 2, midR * 2));

        }

        double coreR = boom.coreRadius();
        if (coreR > 1)  {

            float alpha = boom.coreAlpha();
            g2.setColor(new Color(255, 255, 255, (int) (230 * alpha)));
            g2.setStroke(new BasicStroke(5f * alpha));
            g2.draw(new Ellipse2D.Double(boom.posX - coreR, boom.posY - coreR, coreR * 2, coreR * 2));

        }

    }

}