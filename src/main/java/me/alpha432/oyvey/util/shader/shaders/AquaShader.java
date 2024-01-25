package me.alpha432.oyvey.util.shader.shaders;

import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class AquaShader extends FramebufferShader {
    private static AquaShader INSTANCE;
    public float time = 0;

    private AquaShader() {
        super("aqua.frag");
    }

    public static FramebufferShader getInstance() {
        if (INSTANCE == null) INSTANCE = new AquaShader();
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("resolution");
        this.setupUniform("time");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(this.getUniform("resolution"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight() );

        if (!this.animation) return;
        if (this.time > 100)
            this.time = 0;
        else
            this.time += 0.01 * this.animationSpeed;
        //this.time += Float.intBitsToFloat(Float.floatToIntBits(1015.0615f) ^ 0x7F395856) * (float)RenderUtill.deltaTime;
    }
}