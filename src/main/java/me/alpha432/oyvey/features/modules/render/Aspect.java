package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.event.events.PerspectiveEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Util;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Aspect extends Module {

    private Setting<Double> aspect;

    public Aspect() {
        super("Aspect", "Changes the res without the FOV", Category.RENDER, true, false, false);
        this.aspect = (Setting<Double>)this.register(new Setting("Ratio", (Aspect.mc.displayWidth / (double)Aspect.mc.displayHeight), 0.0, 3.0));
    }

    @SubscribeEvent
    public void onPerspectiveEvent(final PerspectiveEvent event) {
        event.setAspect(this.aspect.getValue().floatValue());
    }
}
