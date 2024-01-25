package me.alpha432.oyvey.util.shader.shaders;

import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class RedShader extends FramebufferShader {
    private static RedShader INSTANCE;
    protected float time = 0f;

    private RedShader() { super("red.frag");}

    public static RedShader getInstance() {
        if (INSTANCE == null) INSTANCE = new RedShader();
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("time");
        this.setupUniform("resolution");
        //this.setupUniform("texture");
        this.setupUniform("texelSize");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(
                this.getUniform("resolution"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight());
        GL20.glUniform2f(this.getUniform("texelSize"), 1F / mc.displayWidth * (radius * quality), 1F / mc.displayHeight * (radius * quality));
        //GL20.glUniform1i(this.getUniform("texture"), 0);

        if (!this.animation) return;
        if (this.time > 100)
            this.time = 0;
        else
            this.time += 0.05 * this.animationSpeed;
    }
}