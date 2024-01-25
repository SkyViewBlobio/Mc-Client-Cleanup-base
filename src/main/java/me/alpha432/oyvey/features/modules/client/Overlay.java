package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.Feature;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.util.Util;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class Overlay extends Module {
    public static final ResourceLocation mark = new ResourceLocation("textures/aa12.png");
    public Setting<Integer> imageX;
    public Setting<Integer> imageY;
    public Setting<Integer> imageWidth;
    public Setting<Integer> imageHeight;
    private int color;

    public Overlay() {
        super("Heaven", "Xulu+ Logo", Category.CLIENT, false, false, false);
        this.imageX = this.register(new Setting("x", 0, 0, 300));
        this.imageY = this.register(new Setting("y", 0, 0, 300));
        this.imageWidth = this.register(new Setting("width", 154, 0, 1600));
        this.imageHeight = this.register(new Setting("height", 105, 0, 900));

    }

    public void renderLogo() {
        int width = (Integer) this.imageWidth.getValue();
        int height = (Integer) this.imageHeight.getValue();
        int x = (Integer) this.imageX.getValue();
        int y = (Integer) this.imageY.getValue();
        Util.mc.renderEngine.bindTexture(mark);
        GlStateManager.color(255.0F, 255.0F, 255.0F);
        Gui.drawScaledCustomSizeModalRect(x - 2, y - 36, 7.0F, 7.0F, width - 7, height - 7, width, height, (float) width, (float) height);
    }

    public void onRender2D(Render2DEvent event) {
        if (!Feature.fullNullCheck()) {
            int width = this.renderer.scaledWidth;
            int height = this.renderer.scaledHeight;
            this.color = ColorUtil.toRGBA((Integer) ClickGui.getInstance().red.getValue(), (Integer) ClickGui.getInstance().green.getValue(), (Integer) ClickGui.getInstance().blue.getValue());
            if ((Boolean) this.enabled.getValue()) {
                this.renderLogo();
            }

        }

    }
}