



package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.features.modules.*;
import me.alpha432.oyvey.util.*;
import me.alpha432.oyvey.features.setting.*;
import net.minecraft.network.Packet;
import net.minecraft.util.math.*;
import net.minecraftforge.client.event.*;
import me.alpha432.oyvey.event.events.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.init.*;
import net.minecraft.network.play.server.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.entity.*;
import net.minecraft.network.*;

public class Bypass extends Module
{
    private static Bypass instance;
    private final TimerUtil timer;
    public Setting<Boolean> illegals;
    public Setting<Boolean> secretClose;
    public Setting<Boolean> rotation;
    public Setting<Boolean> elytra;
    public Setting<Boolean> reopen;
    public Setting<Integer> reopen_interval;
    public Setting<Integer> delay;
    public Setting<Boolean> allow_ghost;
    public Setting<Boolean> cancel_close;
    public Setting<Boolean> discreet;
    public Setting<Boolean> packets;
    public Setting<Boolean> limitSwing;
    public Setting<Integer> swingPackets;
    public Setting<Boolean> noLimit;
    int cooldown;
    private float yaw;
    private float pitch;
    private boolean rotate;
    private BlockPos pos;
    private int swingPacket;

    public Bypass() {
        super("-PacketBypass2b",  "G",  Category.Flex,  true,  false,  false);
        this.timer = new TimerUtil();
        this.illegals = (Setting<Boolean>)this.register(new Setting("", false));
        this.secretClose = (Setting<Boolean>)this.register(new Setting("", false,  v -> this.illegals.getValue()));
        this.rotation = (Setting<Boolean>)this.register(new Setting("", false,  v -> this.secretClose.getValue() && this.illegals.getValue()));
        this.elytra = (Setting<Boolean>)this.register(new Setting("", false));
        this.reopen = (Setting<Boolean>)this.register(new Setting("", false,  v -> this.elytra.getValue()));
        this.reopen_interval = (Setting<Integer>)this.register(new Setting("", 1000, 0, 5000,  v -> this.elytra.getValue()));
        this.delay = (Setting<Integer>)this.register(new Setting("", 0, 0, 1000,  v -> this.elytra.getValue()));
        this.allow_ghost = (Setting<Boolean>)this.register(new Setting("GhostBlocks", true,  v -> this.elytra.getValue()));
        this.cancel_close = (Setting<Boolean>)this.register(new Setting("AttackCancel", true,  v -> this.elytra.getValue()));
        this.discreet = (Setting<Boolean>)this.register(new Setting("HiddenPacket", true,  v -> this.elytra.getValue()));
        this.packets = (Setting<Boolean>)this.register(new Setting("5PacketsForSilentCa", false));
        this.limitSwing = (Setting<Boolean>)this.register(new Setting("SwingLimiter", false,  v -> this.packets.getValue()));
        this.swingPackets = (Setting<Integer>)this.register(new Setting("SwingPackets", 1, 0, 100,  v -> this.packets.getValue()));
        this.noLimit = (Setting<Boolean>)this.register(new Setting("NoPressure", false,  v -> this.packets.getValue()));
        Bypass.instance = this;
    }

    public static Bypass getInstance() {
        if (Bypass.instance == null) {
            Bypass.instance = new Bypass();
        }
        return Bypass.instance;
    }

    @Override
    public void onToggle() {
        this.swingPacket = 0;
    }

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent event) {
        if (event.getGui() == null && this.secretClose.getValue() && this.rotation.getValue()) {
            this.pos = new BlockPos(Bypass.mc.player.getPositionVector());
            this.yaw = Bypass.mc.player.rotationYaw;
            this.pitch = Bypass.mc.player.rotationPitch;
            this.rotate = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPacketSend(final PacketEvent.Send event) {
        if (this.illegals.getValue() && this.secretClose.getValue()) {
            if (event.getPacket() instanceof CPacketCloseWindow) {
                event.setCanceled(true);
            }
            else if (event.getPacket() instanceof CPacketPlayer && this.rotation.getValue() && this.rotate) {
                final CPacketPlayer packet = (CPacketPlayer)event.getPacket();
                packet.yaw = this.yaw;
                packet.pitch = this.pitch;
            }
        }
        if (this.packets.getValue() && this.limitSwing.getValue() && event.getPacket() instanceof CPacketAnimation) {
            if (this.swingPacket > this.swingPackets.getValue()) {
                event.setCanceled(true);
            }
            ++this.swingPacket;
        }
    }

    @SubscribeEvent
    public void onIncomingPacket(final PacketEvent.Receive event) {
        if (!fullNullCheck() && this.elytra.getValue()) {
            if (event.getPacket() instanceof SPacketSetSlot) {
                final SPacketSetSlot packet = (SPacketSetSlot)event.getPacket();
                if (packet.getSlot() == 6) {
                    event.setCanceled(true);
                }
                if (!this.allow_ghost.getValue() && packet.getStack().getItem().equals(Items.ELYTRA)) {
                    event.setCanceled(true);
                }
            }
            if (this.cancel_close.getValue() && Bypass.mc.player.isElytraFlying() && event.getPacket() instanceof SPacketEntityMetadata && ((SPacketEntityMetadata)event.getPacket()).getEntityId() == Bypass.mc.player.getEntityId()) {
                event.setCanceled(true);
            }
        }
        if (event.getPacket() instanceof SPacketCloseWindow) {
            this.rotate = false;
        }
    }

    @Override
    public void onTick() {
        if (this.secretClose.getValue() && this.rotation.getValue() && this.rotate && this.pos != null && Bypass.mc.player != null && Bypass.mc.player.getDistanceSq(this.pos) > 400.0) {
            this.rotate = false;
        }
        if (this.elytra.getValue()) {
            if (this.cooldown > 0) {
                --this.cooldown;
            }
            else if (Bypass.mc.player != null && !(Bypass.mc.currentScreen instanceof GuiInventory) && (!Bypass.mc.player.onGround || !this.discreet.getValue())) {
                for (int i = 0; i < 36; ++i) {
                    final ItemStack item = Bypass.mc.player.inventory.getStackInSlot(i);
                    if (item.getItem().equals(Items.ELYTRA)) {
                        Bypass.mc.playerController.windowClick(0,  (i < 9) ? (i + 36) : i,  0,  ClickType.QUICK_MOVE,  (EntityPlayer)Bypass.mc.player);
                        this.cooldown = this.delay.getValue();
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onUpdate() {
        this.swingPacket = 0;
        if (this.elytra.getValue() && this.timer.passedMs(this.reopen_interval.getValue()) && this.reopen.getValue() && !Bypass.mc.player.isElytraFlying() && Bypass.mc.player.fallDistance > 0.0f) {
            Bypass.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)Bypass.mc.player,  CPacketEntityAction.Action.START_FALL_FLYING));
        }
    }
}