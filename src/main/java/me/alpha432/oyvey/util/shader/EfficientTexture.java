package me.alpha432.oyvey.util.shader;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class EfficientTexture extends AbstractTexture
{
    private int[] textureData;
    private final int width;
    private final int height;

    public EfficientTexture(BufferedImage bufferedImage) {
        this(bufferedImage.getWidth(), bufferedImage.getHeight());
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), this.textureData, 0, bufferedImage.getWidth());
        this.updateEfficientTexture();
    }

    public EfficientTexture(int textureWidth, int textureHeight) {
        this.width = textureWidth;
        this.height = textureHeight;
        this.textureData = new int[textureWidth * textureHeight];
        TextureUtil.allocateTexture(this.getGlTextureId(), textureWidth, textureHeight);
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
    }

    private void updateEfficientTexture() {
        TextureUtil.uploadTexture(this.getGlTextureId(), this.textureData, this.width, this.height);
        textureData = new int[0];
    }
}