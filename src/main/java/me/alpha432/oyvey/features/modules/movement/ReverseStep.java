package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class ReverseStep extends Module {

    private final Setting<Integer> speed = this.register(new Setting<>("Speed", 0, 0, 20));

    public ReverseStep() {
        super("ReverseStep", "Speeds up downwards motion", Category.MOVEMENT, true, false, false);
    }

    @Override
    public void onUpdate() {
        if (mc.player.isInLava() || mc.player.isInWater()) {
            return;
        }
        if (mc.player.onGround) {
            mc.player.motionY -= speed.getValue();
        }
    }
}