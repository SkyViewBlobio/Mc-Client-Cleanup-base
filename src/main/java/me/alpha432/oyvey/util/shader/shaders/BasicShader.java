package me.alpha432.oyvey.util.shader.shaders;

import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class BasicShader extends FramebufferShader {
    private static BasicShader INSTANCE;
    private float time = 0;
    private float timeMult = 0.1f;
    private int timeLimit = 10000;

    private BasicShader(String fragmentShader) {
        super(fragmentShader);
    }

    private BasicShader(String fragmentShader, float timeMult) {
        super(fragmentShader);
        this.timeMult = timeMult;
    }

    public static FramebufferShader getInstance(String fragmentShader) {
        if (INSTANCE == null || !INSTANCE.fragmentShader.equals(fragmentShader))
            INSTANCE = new BasicShader(fragmentShader);
        return INSTANCE;
    }

    public static FramebufferShader getInstance(String fragmentShader, float timeMult) {
        if (INSTANCE == null || !INSTANCE.fragmentShader.equals(fragmentShader))
            INSTANCE = new BasicShader(fragmentShader, timeMult);
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("time");
        this.setupUniform("resolution");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(this.getUniform("resolution"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight() );

        if (!this.animation) return;
        if (this.time > this.timeLimit)
            this.time = 0;
        else
            this.time += this.timeMult * this.animationSpeed;

        //arcsin(sin(x*0.1))*50+100
        //this.time = (float) Math.asin( Math.sin(this.time*0.1) )*50+100;
    }
}