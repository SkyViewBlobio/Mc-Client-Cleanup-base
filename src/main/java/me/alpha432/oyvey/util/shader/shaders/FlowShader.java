package me.alpha432.oyvey.util.shader.shaders;

import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class FlowShader extends FramebufferShader {
    public static FlowShader INSTANCE;
    protected float time = 0f;

    private FlowShader() {
        super("flow.frag");
    }

    public static FlowShader getInstance() {
        if (INSTANCE == null) INSTANCE = new FlowShader();
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("resolution");
        this.setupUniform("time");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform2f(
                this.getUniform("resolution"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight());

        GL20.glUniform1f(this.getUniform("time"), this.time);

        if (!this.animation) return;
        if (this.time > 100)
            this.time = 0;
        else
            this.time += 0.001 * this.animationSpeed;
    }
}