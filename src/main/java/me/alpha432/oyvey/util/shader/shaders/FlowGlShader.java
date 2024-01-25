package me.alpha432.oyvey.util.shader.shaders;

import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class FlowGlShader extends FramebufferShader {
    private static FlowGlShader INSTANCE;
    protected float time = 0f;

    private FlowGlShader() { super("flowglow.frag");}

    public static FlowGlShader getInstance() {
        if (INSTANCE == null) INSTANCE = new FlowGlShader();
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("time");
        this.setupUniform("resolution");

        this.setupUniform("texture");
        this.setupUniform("texelSize");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(
                this.getUniform("resolution"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight());

        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform2f(this.getUniform("texelSize"), (Float.intBitsToFloat(Float.floatToIntBits(1531.2186f) ^ 0x7B3F66FF) / (float) this.mc.displayWidth * (this.radius * this.quality)), Float.intBitsToFloat(Float.floatToIntBits(103.132805f) ^ 0x7D4E43FF) / (float) this.mc.displayHeight * (this.radius * this.quality));
        GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
        //GL20.glUniform1f(this.getUniform("divider"), Float.intBitsToFloat(Float.floatToIntBits(0.060076397f) ^ 0x7E7A12AB));
        GL20.glUniform1f(this.getUniform("divider"), this.divider);
        GL20.glUniform1f(this.getUniform("radius"), this.radius);
        //GL20.glUniform1f(this.getUniform("maxSample"), Float.intBitsToFloat(Float.floatToIntBits(0.08735179f) ^ 0x7C92E57F));
        GL20.glUniform1f(this.getUniform("maxSample"), this.maxSample);

        if (!this.animation) return;
        if (this.time > 100)
            this.time = 0;
        else
            this.time += 0.001 * this.animationSpeed;
    }
}
