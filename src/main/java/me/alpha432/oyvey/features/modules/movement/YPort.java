package me.alpha432.oyvey.features.modules.movement;

import net.minecraft.entity.EntityLivingBase;
import me.alpha432.oyvey.util.MotionUtil;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.util.EntityUtil2;
import me.alpha432.oyvey.util.Timer;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.features.modules.Module;

public class YPort extends Module
{
    public Setting<Boolean> useTimer;
    private final Setting<Double> yPortSpeed;
    public Setting<Boolean> stepyport;
    private Timer timer;
    private float stepheight;

    public YPort() {
        super("Yport", "Yports you", Category.MOVEMENT, true, false, false);
        this.useTimer = (Setting<Boolean>)this.register(new Setting("Usetimer", false));
        this.yPortSpeed = (Setting<Double>)this.register(new Setting("Speed", 0.1, 0.0, 1.0));
        this.stepyport = (Setting<Boolean>)this.register(new Setting("Step", true));
        this.timer = new Timer();
        this.stepheight = 2.0f;
    }

    @Override
    public void onDisable() {
        this.timer.reset();
        EntityUtil2.resetTimer();
    }

    @Override
    public void onUpdate() {
        if (YPort.mc.player.isSneaking() || YPort.mc.player.isInWater() || YPort.mc.player.isInLava() || YPort.mc.player.isOnLadder() || OyVey.moduleManager.isModuleEnabled("Strafe")) {
            return;
        }
        if (YPort.mc.player == null || YPort.mc.world == null) {
            this.disable();
            return;
        }
        this.handleYPortSpeed();
        if ((!YPort.mc.player.isOnLadder() || YPort.mc.player.isInWater() || YPort.mc.player.isInLava()) && this.stepyport.getValue()) {
            Step.mc.player.stepHeight = this.stepheight;
            StepTwo.mc.player.stepHeight = this.stepheight;
        }
    }

    @Override
    public void onToggle() {
        Step.mc.player.stepHeight = 0.6f;
        StepTwo.mc.player.stepHeight = 0.6f;
        YPort.mc.player.motionY = -3.0;
    }

    private void handleYPortSpeed() {
        if (!MotionUtil.isMoving(YPort.mc.player) || (YPort.mc.player.isInWater() && YPort.mc.player.isInLava()) || YPort.mc.player.collidedHorizontally) {
            return;
        }
        if (YPort.mc.player.onGround) {
            if (this.useTimer.getValue()) {
                EntityUtil2.setTimer(1.15f);
            }
            YPort.mc.player.jump();
            MotionUtil.setSpeed(YPort.mc.player, MotionUtil.getBaseMoveSpeed() + this.yPortSpeed.getValue());
        }
        else {
            YPort.mc.player.motionY = -1.0;
            EntityUtil2.resetTimer();
        }
    }
}
