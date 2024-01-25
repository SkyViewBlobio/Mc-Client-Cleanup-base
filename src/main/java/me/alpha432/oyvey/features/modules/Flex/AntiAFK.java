



package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.features.modules.*;
import me.alpha432.oyvey.features.setting.*;
import java.util.*;
import net.minecraft.network.*;
import net.minecraft.inventory.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

public class AntiAFK extends Module
{
    private final Random random;
    private final Setting<Boolean> swing;
    private final Setting<Boolean> turn;
    private final Setting<Boolean> jump;
    private final Setting<Boolean> sneak;
    private final Setting<Boolean> interact;
    private final Setting<Boolean> tabcomplete;
    private final Setting<Boolean> msgs;
    private final Setting<Boolean> stats;
    private final Setting<Boolean> window;
    private final Setting<Boolean> swap;
    private final Setting<Boolean> dig;
    private final Setting<Boolean> move;

    public AntiAFK() {
        super("-NoDisconnect",  "Attempts to stop the server from kicking u when ur afk.",  Category.Flex,  true,  false,  false);
        this.swing = (Setting<Boolean>)this.register(new Setting("Swing", true));
        this.turn = (Setting<Boolean>)this.register(new Setting("Turn", true));
        this.jump = (Setting<Boolean>)this.register(new Setting("Jump", true));
        this.sneak = (Setting<Boolean>)this.register(new Setting("Sneak", true));
        this.interact = (Setting<Boolean>)this.register(new Setting("InteractBlock", false));
        this.tabcomplete = (Setting<Boolean>)this.register(new Setting("TabComplete", true));
        this.msgs = (Setting<Boolean>)this.register(new Setting("ChatMsgs", true));
        this.stats = (Setting<Boolean>)this.register(new Setting("Stats", true));
        this.window = (Setting<Boolean>)this.register(new Setting("WindowClick", true));
        this.swap = (Setting<Boolean>)this.register(new Setting("ItemSwap", true));
        this.dig = (Setting<Boolean>)this.register(new Setting("HitBlock", true));
        this.move = (Setting<Boolean>)this.register(new Setting("Move", true));
        this.random = new Random();
    }

    @Override
    public void onUpdate() {
        if (AntiAFK.mc.player.ticksExisted % 45 == 0 && this.swing.getValue()) {
            AntiAFK.mc.player.swingArm(EnumHand.MAIN_HAND);
        }
        if (AntiAFK.mc.player.ticksExisted % 20 == 0 && this.turn.getValue()) {
            AntiAFK.mc.player.rotationYaw = (float)(this.random.nextInt(360) - 180);
        }
        if (AntiAFK.mc.player.ticksExisted % 60 == 0 && this.jump.getValue() && AntiAFK.mc.player.onGround) {
            AntiAFK.mc.player.jump();
        }
        if (AntiAFK.mc.player.ticksExisted % 50 == 0 && this.sneak.getValue() && !AntiAFK.mc.player.isSneaking()) {
            AntiAFK.mc.player.movementInput.sneak = true;
        }
        if (AntiAFK.mc.player.ticksExisted % 52.5 == 0.0 && this.sneak.getValue() && AntiAFK.mc.player.isSneaking()) {
            AntiAFK.mc.player.movementInput.sneak = false;
        }
        if (AntiAFK.mc.player.ticksExisted % 30 == 0 && this.interact.getValue()) {
            final BlockPos blockPos = AntiAFK.mc.objectMouseOver.getBlockPos();
            if (!AntiAFK.mc.world.isAirBlock(blockPos)) {
                AntiAFK.mc.playerController.clickBlock(blockPos,  AntiAFK.mc.objectMouseOver.sideHit);
            }
        }
        if (AntiAFK.mc.player.ticksExisted % 80 == 0 && this.tabcomplete.getValue() && !AntiAFK.mc.player.isDead) {
            AntiAFK.mc.player.connection.sendPacket((Packet)new CPacketTabComplete("/" + UUID.randomUUID().toString().replace('-',  'v'),  AntiAFK.mc.player.getPosition(),  false));
        }
        if (AntiAFK.mc.player.ticksExisted % 200 == 0 && this.msgs.getValue() && !AntiAFK.mc.player.isDead) {
            AntiAFK.mc.player.sendChatMessage("Xulu Anti Disconnect " + this.random.nextInt());
        }
        if (AntiAFK.mc.player.ticksExisted % 300 == 0 && this.stats.getValue() && !AntiAFK.mc.player.isDead) {
            AntiAFK.mc.player.sendChatMessage("/stats");
        }
        if (AntiAFK.mc.player.ticksExisted % 125 == 0 && this.window.getValue() && !AntiAFK.mc.player.isDead) {
            AntiAFK.mc.player.connection.sendPacket((Packet)new CPacketClickWindow(1,  1,  1,  ClickType.CLONE,  new ItemStack(Blocks.OBSIDIAN),  (short)1));
        }
        if (AntiAFK.mc.player.ticksExisted % 70 == 0 && this.swap.getValue() && !AntiAFK.mc.player.isDead) {
            AntiAFK.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS,  AntiAFK.mc.player.getPosition(),  EnumFacing.DOWN));
        }
        if (AntiAFK.mc.player.ticksExisted % 50 == 0 && this.dig.getValue()) {
            AntiAFK.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,  AntiAFK.mc.player.getPosition(),  EnumFacing.DOWN));
        }
        if (AntiAFK.mc.player.ticksExisted % 150 == 0 && this.move.getValue()) {
            AntiAFK.mc.gameSettings.keyBindForward.pressed = true;
            AntiAFK.mc.gameSettings.keyBindBack.pressed = true;
            AntiAFK.mc.gameSettings.keyBindRight.pressed = true;
            AntiAFK.mc.gameSettings.keyBindLeft.pressed = true;
        }
    }
}