package me.alpha432.oyvey.util.shader.shaders;

import me.alpha432.oyvey.util.shader.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class ItemShader extends FramebufferShader {
    private static ItemShader INSTANCE;
    protected float time = 0f;

    private ItemShader() { super("item.frag");}

    public static ItemShader getInstance() {
        if (INSTANCE == null) INSTANCE = new ItemShader();
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("time");
        this.setupUniform("dimensions");

        this.setupUniform("texture");
        this.setupUniform("image");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");

        this.setupUniform("blur");
        this.setupUniform("minAlpha");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(
                this.getUniform("dimensions"),
                (float) new ScaledResolution(mc).getScaledWidth(),
                (float) new ScaledResolution(mc).getScaledHeight());

        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform1i(this.getUniform("image"), 0);
        GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
        GL20.glUniform1f(this.getUniform("radius"), this.radius);
        GL20.glUniform1f(this.getUniform("divider"), this.divider);
        GL20.glUniform1f(this.getUniform("maxSample"), this.maxSample);

        GL20.glUniform1i(this.getUniform("blur"), 1);
        GL20.glUniform1f(this.getUniform("minAlpha"), 1f);

        if (!this.animation) return;
        if (this.time > 100)
            this.time = 0;
        else
            this.time += 0.001 * this.animationSpeed;

        /* old crap TODO: implement images

        //if (ItemChams.getInstance().useGif.getValue()) {
        if (false) {
            //EfficientTexture texture = ITEM_CHAMS.get().gif.getValue().getDynamicTexture();
            //GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture != null ? texture.getGlTextureId() : 0);
        } else {
            List<EfficientTexture> images = FileManager.getInstance().getImageList();
            int id = 0;

            if (images.isEmpty())
                id = 0;
            else
                id = images.get(0).getGlTextureId();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
            //GL11.glBindTexture(GL11.GL_TEXTURE_2D,
            //      ITEM_CHAMS.get().image.getValue().getTexture() != null ? ITEM_CHAMS.get().image.getValue().getTexture().getGlTextureId() : 0);
        }
        GL20.glUniform1i(getUniform("image"), 8);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL20.glUniform1f(getUniform("imageMix"), imageMix);
        GL20.glUniform1i(getUniform("useImage"), useImage ? 1 : 0);

         */
    }
}
