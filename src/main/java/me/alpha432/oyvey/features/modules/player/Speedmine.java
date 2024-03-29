



package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.modules.*;
import me.alpha432.oyvey.features.setting.*;
import net.minecraft.block.state.*;
import me.alpha432.oyvey.*;
import net.minecraft.network.*;
import java.awt.*;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.math.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import java.util.*;
import net.minecraftforge.fml.common.eventhandler.*;
import me.alpha432.oyvey.event.events.*;
import net.minecraft.block.*;
import me.alpha432.oyvey.util.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

public class Speedmine extends Module
{
    private static Speedmine INSTANCE;
    public final TimerUtil timer;
    private final Setting<Float> range;
    public Setting<Boolean> tweaks;
    public Setting<Mode> mode;
    public Setting<Float> damage;
    public Setting<Boolean> reset;
    public Setting<Boolean> noBreakAnim;
    public Setting<Boolean> noDelay;
    public Setting<Boolean> noSwing;
    public Setting<Boolean> allow;
    public Setting<Boolean> doubleBreak;
    public Setting<Boolean> webSwitch;
    public Setting<Boolean> silentSwitch;
    public Setting<Boolean> render;
    public Setting<Integer> red;
    public Setting<Integer> green;
    public Setting<Integer> blue;
    public Setting<Boolean> box;
    private final Setting<Integer> boxAlpha;
    public Setting<Boolean> outline;
    public final Setting<Float> lineWidth;
    public BlockPos currentPos;
    public IBlockState currentBlockState;
    public float breakTime;
    private boolean isMining;
    private BlockPos lastPos;
    private EnumFacing lastFacing;

    public Speedmine() {
        super("Speedmine (X)",  "Speeds up mining.",  Module.Category.PLAYER,  true,  false,  false);
        this.timer = new TimerUtil();
        this.range = (Setting<Float>)this.register(new Setting("Range", 10.0f, 0.0f, 50.0f));
        this.tweaks = (Setting<Boolean>)this.register(new Setting("Tweaks", true));
        this.mode = (Setting<Mode>)this.register(new Setting("Mode", Mode.PACKET,  v -> this.tweaks.getValue()));
        this.damage = (Setting<Float>)this.register(new Setting("Damage", 0.7f, 0.0f, 1.0f,  v -> this.mode.getValue() == Mode.DAMAGE && this.tweaks.getValue()));
        this.reset = (Setting<Boolean>)this.register(new Setting("Reset", true));
        this.noBreakAnim = (Setting<Boolean>)this.register(new Setting("NoBreakAnim", false));
        this.noDelay = (Setting<Boolean>)this.register(new Setting("NoDelay", false));
        this.noSwing = (Setting<Boolean>)this.register(new Setting("NoSwing", false));
        this.allow = (Setting<Boolean>)this.register(new Setting("AllowMultiTask", false));
        this.doubleBreak = (Setting<Boolean>)this.register(new Setting("DoubleBreak", false));
        this.webSwitch = (Setting<Boolean>)this.register(new Setting("WebSwitch", false));
        this.silentSwitch = (Setting<Boolean>)this.register(new Setting("SilentSwitch", false));
        this.render = (Setting<Boolean>)this.register(new Setting("Render", false));
        this.red = (Setting<Integer>)this.register(new Setting("Red", 125, 0, 255,  v -> this.render.getValue()));
        this.green = (Setting<Integer>)this.register(new Setting("Green", 0, 0, 255,  v -> this.render.getValue()));
        this.blue = (Setting<Integer>)this.register(new Setting("Blue", 255, 0, 255,  v -> this.render.getValue()));
        this.box = (Setting<Boolean>)this.register(new Setting("Box", false,  v -> this.render.getValue()));
        this.boxAlpha = (Setting<Integer>)this.register(new Setting("BoxAlpha", 85, 0, 255,  v -> this.box.getValue() && this.render.getValue()));
        this.outline = (Setting<Boolean>)this.register(new Setting("Outline", true,  v -> this.render.getValue()));
        this.lineWidth = (Setting<Float>)this.register(new Setting("LineWidth", 1.0f, 0.1f, 5.0f,  v -> this.outline.getValue() && this.render.getValue()));
        this.breakTime = -1.0f;
        this.setInstance();
    }

    public static Speedmine getInstance() {
        if (Speedmine.INSTANCE == null) {
            Speedmine.INSTANCE = new Speedmine();
        }
        return Speedmine.INSTANCE;
    }

    private void setInstance() {
        Speedmine.INSTANCE = this;
    }

    public void onTick() {
        if (this.currentPos != null) {
            if (Speedmine.mc.player != null && Speedmine.mc.player.getDistanceSq(this.currentPos) > MathUtil.square(this.range.getValue())) {
                this.currentPos = null;
                this.currentBlockState = null;
                return;
            }
            if (Speedmine.mc.player != null && this.silentSwitch.getValue() && this.timer.passedMs((int)(2000.0f * OyVey.serverManager.getTpsFactor())) && this.getPickSlot() != -1) {
                Speedmine.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(this.getPickSlot()));
            }
            if (Speedmine.mc.player != null && this.silentSwitch.getValue() && this.timer.passedMs((int)(2200.0f * OyVey.serverManager.getTpsFactor()))) {
                final int oldSlot = Speedmine.mc.player.inventory.currentItem;
                Speedmine.mc.player.connection.sendPacket((Packet)new CPacketHeldItemChange(oldSlot));
            }
            if (fullNullCheck()) {
                return;
            }
            if (!Speedmine.mc.world.getBlockState(this.currentPos).equals(this.currentBlockState) || Speedmine.mc.world.getBlockState(this.currentPos).getBlock() == Blocks.AIR) {
                this.currentPos = null;
                this.currentBlockState = null;
            }
            else if (this.webSwitch.getValue() && this.currentBlockState.getBlock() == Blocks.WEB && Speedmine.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
                InventoryUtil.switchToHotbarSlot(ItemSword.class,  false);
            }
        }
    }

    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (this.noDelay.getValue()) {
            Speedmine.mc.playerController.blockHitDelay = 0;
        }
        if (this.isMining && this.lastPos != null && this.lastFacing != null && this.noBreakAnim.getValue()) {
            Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,  this.lastPos,  this.lastFacing));
        }
        if (this.reset.getValue() && Speedmine.mc.gameSettings.keyBindUseItem.isKeyDown() && !this.allow.getValue()) {
            Speedmine.mc.playerController.isHittingBlock = false;
        }
    }

    public void onRender3D(final Render3DEvent render3DEvent) {
        if (this.render.getValue() && this.currentPos != null) {
            final Color color = new Color(this.red.getValue(),  this.green.getValue(),  this.blue.getValue(),  this.boxAlpha.getValue());
            RenderUtil.boxESP(this.currentPos,  color,  this.lineWidth.getValue(),  this.outline.getValue(),  this.box.getValue(),  this.boxAlpha.getValue(),  true);
        }
    }

    @SubscribeEvent
    public void onPacketSend(final PacketEvent.Send event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0) {
            if (this.noSwing.getValue() && event.getPacket() instanceof CPacketAnimation) {
                event.setCanceled(true);
            }
            final CPacketPlayerDigging packet;
            if (this.noBreakAnim.getValue() && event.getPacket() instanceof CPacketPlayerDigging && (packet = (CPacketPlayerDigging)event.getPacket()) != null) {
                packet.getPosition();
                try {
                    for (final Entity entity : Speedmine.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null,  new AxisAlignedBB(packet.getPosition()))) {
                        if (!(entity instanceof EntityEnderCrystal)) {
                            continue;
                        }
                        this.showAnimation();
                        return;
                    }
                }
                catch (Exception ex) {}
                if (packet.getAction().equals((Object)CPacketPlayerDigging.Action.START_DESTROY_BLOCK)) {
                    this.showAnimation(true,  packet.getPosition(),  packet.getFacing());
                }
                if (packet.getAction().equals((Object)CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                    this.showAnimation();
                }
            }
        }
    }

    @SubscribeEvent
    public void onBlockEvent(final BlockEvent event) {
        if (fullNullCheck()) {
            return;
        }
        if (event.getStage() == 3 && Speedmine.mc.world.getBlockState(event.pos).getBlock() instanceof BlockEndPortalFrame) {
            Speedmine.mc.world.getBlockState(event.pos).getBlock().setHardness(50.0f);
        }
        if (event.getStage() == 3 && this.reset.getValue() && Speedmine.mc.playerController.curBlockDamageMP > 0.1f) {
            Speedmine.mc.playerController.isHittingBlock = true;
        }
        if (event.getStage() == 4 && this.tweaks.getValue()) {
            if (BlockUtil.canBreak(event.pos)) {
                if (this.reset.getValue()) {
                    Speedmine.mc.playerController.isHittingBlock = false;
                }
                switch (this.mode.getValue()) {
                    case PACKET: {
                        if (this.currentPos == null) {
                            this.currentPos = event.pos;
                            this.currentBlockState = Speedmine.mc.world.getBlockState(this.currentPos);
                            final ItemStack object = new ItemStack(Items.DIAMOND_PICKAXE);
                            this.breakTime = object.getDestroySpeed(this.currentBlockState) / 3.71f;
                            this.timer.reset();
                        }
                        Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                        Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,  event.pos,  event.facing));
                        Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,  event.pos,  event.facing));
                        event.setCanceled(true);
                        break;
                    }
                    case DAMAGE: {
                        if (Speedmine.mc.playerController.curBlockDamageMP < this.damage.getValue()) {
                            break;
                        }
                        Speedmine.mc.playerController.curBlockDamageMP = 1.0f;
                        break;
                    }
                    case INSTANT: {
                        Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                        Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,  event.pos,  event.facing));
                        Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,  event.pos,  event.facing));
                        Speedmine.mc.playerController.onPlayerDestroyBlock(event.pos);
                        Speedmine.mc.world.setBlockToAir(event.pos);
                        break;
                    }
                }
            }
            final BlockPos above;
            if (this.doubleBreak.getValue() && BlockUtil.canBreak(above = event.pos.add(0,  1,  0)) && Speedmine.mc.player.getDistance((double)above.getX(),  (double)above.getY(),  (double)above.getZ()) <= 5.0) {
                Speedmine.mc.player.swingArm(EnumHand.MAIN_HAND);
                Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK,  above,  event.facing));
                Speedmine.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,  above,  event.facing));
                Speedmine.mc.playerController.onPlayerDestroyBlock(above);
                Speedmine.mc.world.setBlockToAir(above);
            }
        }
    }

    private int getPickSlot() {
        for (int i = 0; i < 9; ++i) {
            if (Speedmine.mc.player.inventory.getStackInSlot(i).getItem() == Items.DIAMOND_PICKAXE) {
                return i;
            }
        }
        return -1;
    }

    private void showAnimation(final boolean isMining,  final BlockPos lastPos,  final EnumFacing lastFacing) {
        this.isMining = isMining;
        this.lastPos = lastPos;
        this.lastFacing = lastFacing;
    }

    public void showAnimation() {
        this.showAnimation(false,  null,  null);
    }

    public String getDisplayInfo() {
        return this.mode.currentEnumName();
    }

    static {
        Speedmine.INSTANCE = new Speedmine();
    }

    public enum Mode
    {
        PACKET,
        DAMAGE,
        INSTANT;
    }
}