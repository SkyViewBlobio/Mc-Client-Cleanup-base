package me.alpha432.oyvey.features.modules.client;

import me.alpha432.oyvey.util.RenderUtil;
import me.alpha432.oyvey.util.ColorUtil;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.Render2DEvent;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Timer;
import me.alpha432.oyvey.features.modules.Module;

public class Csgo extends Module {

    Timer delayTimer;
    public Setting<Integer> X;
    public Setting<Integer> Y;
    public Setting<Integer> delay;
    public Setting<Integer> saturation;
    public Setting<Integer> brightness;
    public float hue;
    public int red;
    public int green;
    public int blue;
    private String message;

    public Csgo() {
        super("Xulu+Watermark", "Nice Screen Extras", Category.CLIENT, true, false, false);
        this.delayTimer = new Timer();
        this.X = (Setting<Integer>)this.register(new Setting("watermarkx", 0, 0, 300));
        this.Y = (Setting<Integer>)this.register(new Setting("watermarky", 0, 0, 300));
        this.delay = (Setting<Integer>)this.register(new Setting("delay", 240, 0, 600));
        this.saturation = (Setting<Integer>)this.register(new Setting("saturation", 127, 1, 255));
        this.brightness = (Setting<Integer>)this.register(new Setting("brightness", 100, 0, 255));
        this.red = 1;
        this.green = 1;
        this.blue = 1;
        this.message = "";
    }

    @Override
    public void onRender2D(final Render2DEvent event) {
        this.drawCsgoWatermark();
    }

    public void drawCsgoWatermark() {
        final int padding = 5;
        this.message = "Xulu+ v"+OyVey.MODVER+" | " + Csgo.mc.player.getName() + " | " + OyVey.serverManager.getPing() + "Ms";
        final Integer textWidth = Csgo.mc.fontRenderer.getStringWidth(this.message);
        final Integer textHeight = Csgo.mc.fontRenderer.FONT_HEIGHT;
        RenderUtil.drawRectangleCorrectly(this.X.getValue(), this.Y.getValue(), textWidth + 8, textHeight + 4, ColorUtil.toRGBA(0, 0, 0, 150));
        RenderUtil.drawRectangleCorrectly(this.X.getValue(), this.Y.getValue(), textWidth + 8, 2, ColorUtil.toRGBA(((Integer)(ClickGui.getInstance()).red.getValue()).intValue(), ((Integer)(ClickGui.getInstance()).green.getValue()).intValue(), ((Integer)(ClickGui.getInstance()).blue.getValue()).intValue()));
        Csgo.mc.fontRenderer.drawString(this.message, (float)(this.X.getValue() + 3), (float)(this.Y.getValue() + 3), ColorUtil.toRGBA(255, 255, 255, 255), false);
    }
}