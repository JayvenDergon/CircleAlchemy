package handlers;

import java.awt.*;

public class OkLabControl {

    public static Color oklabToRgb(double lightnessL, double chromaA, double chromaB) {

        double lIntermediate = lightnessL + 0.3963377774 * chromaA + 0.2158037573 * chromaB;
        double mIntermediate = lightnessL - 0.1055613458 * chromaA - 0.0638541728 * chromaB;
        double sIntermediate = lightnessL - 0.0894841775 * chromaA - 1.2914855480 * chromaB;

        double lCone = lIntermediate * lIntermediate * lIntermediate;
        double mCone = mIntermediate * mIntermediate * mIntermediate;
        double sCone = sIntermediate * sIntermediate * sIntermediate;

        double linearRed   =  4.0767416621 * lCone - 3.3077115913 * mCone + 0.2309699292 * sCone;
        double linearGreen = -1.2684380046 * lCone + 2.6097574011 * mCone - 0.3413193965 * sCone;
        double linearBlue  = -0.0041960863 * lCone - 0.7034186147 * mCone + 1.7076127010 * sCone;

        return new Color(clampTo255(applyGamma(linearRed)), clampTo255(applyGamma(linearGreen)), clampTo255(applyGamma(linearBlue)));

    }

    private static double applyGamma(double v) {

        return v <= 0.0031308 ? 12.92 * v : 1.055 * Math.pow(v, 1.0 / 2.4) - 0.055;

    }

    private static int clampTo255(double v) {

        return (int) (Math.max(0, Math.min(1, v)) * 255);

    }

}
