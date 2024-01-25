package me.alpha432.oyvey.util;

import me.alpha432.oyvey.features.modules.client.ClickGui;

import java.awt.*;

public class ColorUtil {
    public static int toARGB(int r, int g, int b, int a) {
        return new Color(r, g, b, a).getRGB();
    }

    public static int toRGBA(int r, int g, int b) {
        return ColorUtil.toRGBA(r, g, b, 255);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + b + (a << 24);
    }

    public static int toRGBA(float r, float g, float b, float a) {
        return ColorUtil.toRGBA((int) (r * 255.0f), (int) (g * 255.0f), (int) (b * 255.0f), (int) (a * 255.0f));
    }

    public static Color rainbow(int delay) {
        double rainbowState = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / 20.0);
        return Color.getHSBColor((float) ((rainbowState %= 360.0) / 360.0), ClickGui.getInstance().rainbowSaturation.getValue().floatValue() / 255.0f, ClickGui.getInstance().rainbowBrightness.getValue().floatValue() / 255.0f);
    }

    public static int toRGBA(float[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return ColorUtil.toRGBA(colors[0], colors[1], colors[2], colors[3]);
    }

    public static int toRGBA(double[] colors) {
        if (colors.length != 4) {
            throw new IllegalArgumentException("colors[] must have a length of 4!");
        }
        return ColorUtil.toRGBA((float) colors[0], (float) colors[1], (float) colors[2], (float) colors[3]);
    }

    public static int toRGBA(Color color) {
        return ColorUtil.toRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static Color releasedDynamicRainbow(final int delay, int alpha) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState %= 360.0;

        Color c = Color.getHSBColor((float) (rainbowState / 360.0), 1f, 1f);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static Color getSinState(final Color c1, final double delay, final int a, final type t) {
        double sineState;
        sineState = Math.sin(2400 - System.currentTimeMillis() / delay) * Math.sin(2400 - System.currentTimeMillis() / delay);
        float[] hsb = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
        Color c = null;
        switch (t) {
            case HUE:
                sineState /= hsb[0];
                sineState = Math.min(1.0, sineState);
                c = Color.getHSBColor((float) sineState, 1f, 1f);
                break;
            case SATURATION:
                sineState /= hsb[1];
                sineState = Math.min(1.0, sineState);
                c = Color.getHSBColor(hsb[0], (float) sineState, 1f);
                break;
            case BRIGHTNESS:
                sineState /= hsb[2];
                sineState = Math.min(1.0, sineState);
                c = Color.getHSBColor(hsb[0], 1f, (float) sineState);
                break;
            case SPECIAL:
                sineState /= hsb[1];
                sineState = Math.min(1.0, sineState);
                c = Color.getHSBColor(hsb[0], 1f, (float) sineState);
                break;
        }
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public static Color getSinState(final Color c1, final Color c2, final double delay, final int a) {
        double sineState;
        sineState = Math.sin(2400 - System.currentTimeMillis() / delay) * Math.sin(2400 - System.currentTimeMillis() / delay);
        float[] hsb = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);
        Color c;
        sineState /= hsb[0];
        sineState *= sineState/hsb2[0];
        sineState = Math.min(1.0, sineState);
        c = Color.getHSBColor((float) sineState, 1f, 1f);

        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    public enum type {
        HUE,
        SATURATION,
        BRIGHTNESS,
        SPECIAL
    }
}