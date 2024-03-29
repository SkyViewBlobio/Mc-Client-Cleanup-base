package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

// TODO: fix thing
public
class NoSlowDown
        extends Module {
    private static final KeyBinding[] keys = {NoSlowDown.mc.gameSettings.keyBindForward , NoSlowDown.mc.gameSettings.keyBindBack , NoSlowDown.mc.gameSettings.keyBindLeft , NoSlowDown.mc.gameSettings.keyBindRight , NoSlowDown.mc.gameSettings.keyBindJump , NoSlowDown.mc.gameSettings.keyBindSprint};
    private static NoSlowDown INSTANCE = new NoSlowDown ( );
    public final Setting< Double > webHorizontalFactor = this.register ( new Setting <> ( "WebMoved" , 2.0 , 0.0 , 100.0 ) );
    public final Setting < Double > webVerticalFactor = this.register ( new Setting <> ( "WebVertical" , 2.0 , 0.0 , 100.0 ) );
    public Setting < Boolean > guiMove = this.register ( new Setting <> ( "Gui" , true ) );
    public Setting < Boolean > noSlow = this.register ( new Setting <> ( "NoSlow" , true ) );
    public Setting < Boolean > soulSand = this.register ( new Setting <> ( "SoulSand" , true ) );
    public Setting < Boolean > strict = this.register ( new Setting <> ( "Strict(2b)" , false ) );
    public Setting < Boolean > sneakPacket = this.register ( new Setting <> ( "SneakPacket" , false ) );
    public Setting < Boolean > endPortal = this.register ( new Setting <> ( "EndSpoofed" , false ) );
    public Setting < Boolean > webs = this.register ( new Setting <> ( "Webs" , false ) );
    private boolean sneaking;

    public
    NoSlowDown ( ) {
        super ( "NoSlow" , "Prevents you from getting slowed down." , Module.Category.MOVEMENT , true , false , false );
        this.setInstance ( );
    }

    public static
    NoSlowDown getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new NoSlowDown ( );
        }
        return INSTANCE;
    }

    private
    void setInstance ( ) {
        INSTANCE = this;
    }

    @Override
    public
    void onUpdate ( ) {
        if ( this.guiMove.getValue ( ) ) {
            {
                for (KeyBinding bind : keys) {
                    KeyBinding.setKeyBindState ( bind.getKeyCode ( ) , Keyboard.isKeyDown ( bind.getKeyCode ( ) ) );
                }
            }
            if ( NoSlowDown.mc.currentScreen == null ) {
                for (KeyBinding bind : keys) {
                    if ( Keyboard.isKeyDown ( bind.getKeyCode ( ) ) ) continue;
                    KeyBinding.setKeyBindState ( bind.getKeyCode ( ) , false );
                }
            }
        }
        if ( this.webs.getValue ( ) && OyVey.moduleManager.getModuleByClass ( Flight.class ).isDisabled ( ) && OyVey.moduleManager.getModuleByClass ( Phase.class ).isDisabled ( ) && NoSlowDown.mc.player.isInWeb ) {
            NoSlowDown.mc.player.motionX *= this.webHorizontalFactor.getValue ( );
            NoSlowDown.mc.player.motionZ *= this.webHorizontalFactor.getValue ( );
            NoSlowDown.mc.player.motionY *= this.webVerticalFactor.getValue ( );
        }
        NoSlowDown.mc.player.getActiveItemStack ( ).getItem ( );
        if ( this.sneaking && ! NoSlowDown.mc.player.isHandActive ( ) && this.sneakPacket.getValue ( ) ) {
            NoSlowDown.mc.player.connection.sendPacket ( new CPacketEntityAction ( NoSlowDown.mc.player , CPacketEntityAction.Action.STOP_SNEAKING ) );
            this.sneaking = false;
        }
    }

    @SubscribeEvent
    public
    void onUseItem ( PlayerInteractEvent.RightClickItem event ) {
        Item item = NoSlowDown.mc.player.getHeldItem ( event.getHand ( ) ).getItem ( );
        if ( ( item instanceof ItemFood || item instanceof ItemBow || item instanceof ItemPotion && this.sneakPacket.getValue ( ) ) && ! this.sneaking ) {
            NoSlowDown.mc.player.connection.sendPacket ( new CPacketEntityAction ( NoSlowDown.mc.player , CPacketEntityAction.Action.START_SNEAKING ) );
            this.sneaking = true;
        }
    }

    @SubscribeEvent
    public
    void onInput ( InputUpdateEvent event ) {
        if ( this.noSlow.getValue ( ) && NoSlowDown.mc.player.isHandActive ( ) && ! NoSlowDown.mc.player.isRiding ( ) ) {
            event.getMovementInput ( ).moveStrafe *= 5.0f;
            event.getMovementInput ( ).moveForward *= 5.0f;
        }
    }
    @SubscribeEvent
    public
    void onPacket ( PacketEvent.Send event ) {
        if ( event.getPacket ( ) instanceof CPacketPlayer && this.strict.getValue ( ) && this.noSlow.getValue ( ) && NoSlowDown.mc.player.isHandActive ( ) && ! NoSlowDown.mc.player.isRiding ( ) ) {
            NoSlowDown.mc.player.connection.sendPacket ( new CPacketPlayerDigging ( CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK , new BlockPos ( Math.floor ( NoSlowDown.mc.player.posX ) , Math.floor ( NoSlowDown.mc.player.posY ) , Math.floor ( NoSlowDown.mc.player.posZ ) ) , EnumFacing.DOWN ) );
        }
    }
}
