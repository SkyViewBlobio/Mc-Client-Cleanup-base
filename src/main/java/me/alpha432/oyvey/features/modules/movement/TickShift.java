package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.EntityUtil;

/*
 * Credits
 * - Codex#4562: [rudimentary shit code]
 * - Doogie13: [explaining codex tickshift]
 * - noat (me): [fix codex's code to make it works]
 */
public class TickShift extends Module {
    public TickShift() {
        super("TickShift", "Makes you go Faster", Category.PLAYER, true, false, false);
    }
    Setting<Integer> ticksVal = this.register(new Setting<>("Ticks",18,1,100));
    Setting<Float> timer = this.register(new Setting<>("Timer",1.8f,1f,3f));

    public void onEnable() {
        canTimer = false;
        tick = 0;
    }

    boolean canTimer = false;
    int tick = 0;

    public void onUpdate() {
        if (tick <= 0)  {tick = 0; canTimer = false; mc.timer.tickLength = 50f;}
        if (tick > 0 && EntityUtil.isEntityMoving(mc.player)) {
            tick--;
            mc.timer.tickLength = 50f / timer.getValue();
        }
        if (!EntityUtil.isEntityMoving(mc.player)) tick++;
        if (tick >= ticksVal.getValue()) tick = ticksVal.getValue();
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(tick);
    }
    public void onDisable() {
        mc.timer.tickLength = 50f;
    }
}