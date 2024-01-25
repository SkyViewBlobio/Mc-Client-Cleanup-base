package me.alpha432.oyvey.util;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class OyColor extends Color {

    public OyColor(int r, int g, int b) {
        super(r, g, b);
    }

    public OyColor(int rgb) {
        super(rgb);
    }

    public OyColor(int rgba, boolean hasAlpha) {
        super(rgba, hasAlpha);
    }

    public OyColor(int r, int g, int b, int a) {
        super(r, g, b, a);
    }

    public OyColor(Color color) {
        super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public OyColor(Color color, int alpha) {
        super(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static OyColor fromHSB(float hue, float saturation, float brightness) {
        return new OyColor(Color.getHSBColor(hue, saturation, brightness));
    }

    public float getHue() {
        return RGBtoHSB(getRed(), getGreen(), getBlue(), null)[0];
    }

    public float getSaturation() {
        return RGBtoHSB(getRed(), getGreen(), getBlue(), null)[1];
    }

    public float getBrightness() {
        return RGBtoHSB(getRed(), getGreen(), getBlue(), null)[2];
    }

    public float getRedNorm(){ return getRed() / 255f; }
    public float getGreenNorm(){ return getGreen() / 255f; }
    public float getBlueNorm(){ return getBlue() / 255f; }
    public float getAlphaNorm(){ return getAlpha() / 255f; }

    public void glColor() {
        GlStateManager.color(getRedNorm(), getGreenNorm(), getBlueNorm(), getAlphaNorm());
    }
}
