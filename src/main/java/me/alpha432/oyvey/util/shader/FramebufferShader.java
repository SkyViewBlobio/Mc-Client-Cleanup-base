package me.alpha432.oyvey.util.shader;

import me.alpha432.oyvey.util.OyColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class FramebufferShader extends Shader {
    public Minecraft mc = Minecraft.getMinecraft();

    private static Framebuffer framebuffer;
    protected static int lastScale;
    protected static int lastScaleWidth;
    protected static int lastScaleHeight;

    protected float red, green, blue, alpha = 1f;
    protected float radius = 2f;
    protected float quality = 1f;

    // fields for potential uniforms
    protected boolean animation = true;
    protected int animationSpeed = 1;
    protected float divider = 1f;
    protected float maxSample = 1f;

    private boolean entityShadows;

    public FramebufferShader(String fragmentShader) {
        super(fragmentShader);
    }

    public void setShaderParams(Boolean animation, int animationSpeed, OyColor color) {
        this.animation = animation;
        this.animationSpeed = animationSpeed;

        this.red = color.getRedNorm();
        this.green = color.getGreenNorm();
        this.blue = color.getBlueNorm();
        this.alpha = color.getBlueNorm();
    }

    public void setShaderParams(Boolean animation, int animationSpeed, OyColor color, float radius) {
        this.setShaderParams(animation, animationSpeed, color);
        this.radius = radius;
    }

    public void setShaderParams(Boolean animation, int animationSpeed, OyColor color, float radius, float divider, float maxSample) {
        this.setShaderParams(animation, animationSpeed, color, radius);
        this.divider = divider;
        this.maxSample = maxSample;
    }

    public void startDraw(float partialTicks) {
        GlStateManager.enableAlpha();

        GlStateManager.pushMatrix();
        //GlStateManager.pushAttrib();

        framebuffer = setupFrameBuffer(framebuffer);
        //framebuffer.framebufferClear();

        framebuffer.bindFramebuffer(true);

        entityShadows = mc.gameSettings.entityShadows;
        mc.gameSettings.entityShadows = false;

        //this.mc.entityRenderer.updateCameraAndRender(partialTicks, 0);

        //((IEntityRenderer) mc.entityRenderer).callSetupCameraTransform(partialTicks, 0);
    }

    public void stopDraw() {
        mc.gameSettings.entityShadows = entityShadows;

        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //GL11.glEnable(3042);
        mc.getFramebuffer().bindFramebuffer(true);

        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();

        startShader();
        mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(framebuffer);
        stopShader();

        mc.entityRenderer.disableLightmap();

        //GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public void stopDraw(final OyColor color, final float radius, final float quality, Runnable... shaderOps) {
        mc.gameSettings.entityShadows = entityShadows;
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        mc.getFramebuffer().bindFramebuffer(true);

        red = color.getRed() / 255F;
        green = color.getGreen() / 255F;
        blue = color.getBlue() / 255F;
        alpha = color.getAlpha() / 255F;
        this.radius = radius;
        this.quality = quality;

        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();

        this.startShader();
        mc.entityRenderer.setupOverlayRendering();
        drawFramebuffer(framebuffer);
        this.stopShader();

        mc.entityRenderer.disableLightmap();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
        if (Display.isActive() || Display.isVisible()) {

            if (frameBuffer != null) {
                frameBuffer.framebufferClear();
                ScaledResolution scale = new ScaledResolution(mc);

                int factor = scale.getScaleFactor();
                int factor2 = scale.getScaledWidth();
                int factor3 = scale.getScaledHeight();

                if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
                    frameBuffer.deleteFramebuffer();
                    frameBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
                    frameBuffer.framebufferClear();
                }
                lastScale = factor;
                lastScaleWidth = factor2;
                lastScaleHeight = factor3;
            } else {
                frameBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            }
        } else {
            if (frameBuffer == null) {
                frameBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            }
        }

        return frameBuffer;
    }

    public void drawFramebuffer(final Framebuffer framebuffer) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.framebufferTexture);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2d(0, 1);
        GL11.glVertex2d(0, 0);
        GL11.glTexCoord2d(0, 0);
        GL11.glVertex2d(0, scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1, 0);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1, 1);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), 0);

        /*
        GL11.glTexCoord2d(Double.longBitsToDouble(Double.doubleToLongBits(1.7921236082576344E308) ^ 0x7FEFE69EB44D9FE1L), Double.longBitsToDouble(Double.doubleToLongBits(4.899133169559449) ^ 0x7FE398B65D9806D1L));
        GL11.glVertex2d(Double.longBitsToDouble(Double.doubleToLongBits(3.7307361562967813E307) ^ 0x7FCA9050299687CBL), Double.longBitsToDouble(Double.doubleToLongBits(7.56781900945177E307) ^ 0x7FDAF13C89C9BE29L));
        GL11.glTexCoord2d(Double.longBitsToDouble(Double.doubleToLongBits(1.0409447193540338E308) ^ 0x7FE28788CB57BFECL), Double.longBitsToDouble(Double.doubleToLongBits(4.140164300258766E307) ^ 0x7FCD7A9C5BA7C45BL));
        GL11.glVertex2d(Double.longBitsToDouble(Double.doubleToLongBits(1.3989301333159067E308) ^ 0x7FE8E6DB3F70C542L), (double) scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(Double.longBitsToDouble(Double.doubleToLongBits(52.314008345000495) ^ 0x7FBA28316CEA395FL), Double.longBitsToDouble(Double.doubleToLongBits(1.3534831910786353E308) ^ 0x7FE817C1C68E7C69L));
        GL11.glVertex2d((double) scaledResolution.getScaledWidth(), (double) scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(Double.longBitsToDouble(Double.doubleToLongBits(4.557588341026122) ^ 0x7FE23AF870255A34L), Double.longBitsToDouble(Double.doubleToLongBits(23.337335758793085) ^ 0x7FC7565BA2E3C9A3L));
        GL11.glVertex2d((double) scaledResolution.getScaledWidth(), Double.longBitsToDouble(Double.doubleToLongBits(1.5123382114342209E308) ^ 0x7FEAEBA6CA1CFB74L));
        */

        GL11.glEnd();
        GL20.glUseProgram(0);
    }
}