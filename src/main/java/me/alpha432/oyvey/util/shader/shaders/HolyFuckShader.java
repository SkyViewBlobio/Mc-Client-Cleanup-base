package me.alpha432.oyvey.util.shader.shaders;

import net.minecraft.util.Tuple;
import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class HolyFuckShader extends FramebufferShader {
    private static HolyFuckShader INSTANCE;
    protected float time = 0f;

    private HolyFuckShader() {
        super("holyfuck.frag");
    }

    public static HolyFuckShader getInstance() {
        if (INSTANCE == null) INSTANCE = new HolyFuckShader();
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("time");
        this.setupUniform("resolution");

        this.setupUniform("speed");
        this.setupUniform("shift");

        this.setupUniform("color");
        this.setupUniform("radius");
        this.setupUniform("quality");
        this.setupUniform("divider");
        this.setupUniform("texelSize");
        this.setupUniform("maxSample");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(
                this.getUniform("resolution"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight());

        GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
        GL20.glUniform1f(this.getUniform("radius"), this.radius);
        GL20.glUniform1f(this.getUniform("quality"), this.quality);
        GL20.glUniform1f(this.getUniform("divider"), this.divider);
        GL20.glUniform2f(this.getUniform("speed"), (float) this.animationSpeed, (float) this.animationSpeed);
        GL20.glUniform1f(this.getUniform("shift"), 1f);
        GL20.glUniform1f(this.getUniform("maxSample"), this.maxSample);

        if (!this.animation) return;
        if (this.time > 100)
            this.time = 0;
        else
            this.time += 0.01f * this.animationSpeed;
    }
}