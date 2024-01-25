package me.alpha432.oyvey.features.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.UpdateWalkingPlayerEvent;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.*;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import me.alpha432.oyvey.features.modules.Module;

public class BedBombPhob extends Module {
    private final Setting < Boolean > server = this.register ( new Setting <> ( "ServerPingPacket" , false ) );
    private final Setting < Boolean > place = this.register ( new Setting <> ( "Place" , false ) );
    private final Setting < Integer > placeDelay = this.register ( new Setting < Object > ( "Delays" , 50 , 0 , 500 , v -> this.place.getValue ( ) ) );
    private final Setting < Float > placeRange = this.register ( new Setting < Object > ( "Ranges" , 6.0f , 1.0f , 10.0f , v -> this.place.getValue ( ) ) );
    private final Setting < Boolean > extraPacket = this.register ( new Setting < Object > ( "PacketForce" , Boolean.FALSE , v -> this.place.getValue ( ) ) );
    private final Setting < Boolean > packet = this.register ( new Setting < Object > ( "Packet" , Boolean.FALSE , v -> this.place.getValue ( ) ) );
    private final Setting < Boolean > explode = this.register ( new Setting <> ( "Break" , true ) );
    private final Setting < BreakLogic > breakMode = this.register ( new Setting < Object > ( "BreakMode" , BreakLogic.ALL , v -> this.explode.getValue ( ) ) );
    private final Setting < Integer > breakDelay = this.register ( new Setting < Object > ( "Delays" , 50 , 0 , 500 , v -> this.explode.getValue ( ) ) );
    private final Setting < Float > breakRange = this.register ( new Setting < Object > ( "Ranges" , 6.0f , 1.0f , 10.0f , v -> this.explode.getValue ( ) ) );
    private final Setting < Float > minDamage = this.register ( new Setting < Object > ( "MinDMG" , 5.0f , 1.0f , 36.0f , v -> this.explode.getValue ( ) ) );
    private final Setting < Float > range = this.register ( new Setting < Object > ( "ERange" , 10.0f , 1.0f , 12.0f , v -> this.explode.getValue ( ) ) );
    private final Setting < Boolean > suicide = this.register ( new Setting < Object > ( "Suicide" , Boolean.FALSE , v -> this.explode.getValue ( ) ) );
    private final Setting < Boolean > removeTiles = this.register ( new Setting <> ( "RemoveFire" , false ) );
    private final Setting < Boolean > rotate = this.register ( new Setting <> ( "RotationsPassed" , false ) );
    private final Setting < Boolean > oneDot15 = this.register ( new Setting <> ( "1.15" , false ) );
    private final Setting < Logic > logic = this.register ( new Setting < Object > ( "Logic" , Logic.BREAKPLACE , v -> this.place.getValue ( ) && this.explode.getValue ( ) ) );
    private final Setting < Boolean > craft = this.register ( new Setting <> ( "CraftBed" , false ) );
    private final Setting < Boolean > placeCraftingTable = this.register ( new Setting < Object > ( "PlaceTable" , Boolean.FALSE , v -> this.craft.getValue ( ) ) );
    private final Setting < Boolean > openCraftingTable = this.register ( new Setting < Object > ( "OpenTable" , Boolean.FALSE , v -> this.craft.getValue ( ) ) );
    private final Setting < Boolean > craftTable = this.register ( new Setting < Object > ( "CraftTable" , Boolean.FALSE , v -> this.craft.getValue ( ) ) );
    private final Setting < Float > tableRange = this.register ( new Setting < Object > ( "TableRange" , 6.0f , 1.0f , 10.0f , v -> this.craft.getValue ( ) ) );
    private final Setting < Integer > craftDelay = this.register ( new Setting < Object > ( "CraftDelay" , 4 , 1 , 10 , v -> this.craft.getValue ( ) ) );
    private final Setting < Integer > tableSlot = this.register ( new Setting < Object > ( "TableSlot" , 8 , 0 , 8 , v -> this.craft.getValue ( ) ) );
    private final Setting < Boolean > sslot = this.register ( new Setting <> ( "S-Slot" , false ) );
    private final Timer breakTimer = new Timer ( );
    private final Timer placeTimer = new Timer ( );
    private final Timer craftTimer = new Timer ( );
    private final AtomicDouble yaw = new AtomicDouble ( - 1.0 );
    private final AtomicDouble pitch = new AtomicDouble ( - 1.0 );
    private final AtomicBoolean shouldRotate = new AtomicBoolean ( false );
    private final int lastCraftStage = - 1;
    private EntityPlayer target;
    private boolean sendRotationPacket;
    private boolean one;
    private boolean two;
    private boolean three;
    private boolean four;
    private boolean five;
    private boolean six;
    private BlockPos maxPos;
    private boolean shouldCraft;
    private int craftStage;
    private int bedSlot = - 1;
    private BlockPos finalPos;
    private EnumFacing finalFacing;

    public BedBombPhob ( ) {
        super ( "YippieMeta" , "The YippieMod" , Module.Category.COMBAT , true , false , false );
    }

    @Override
    public
    void onEnable ( ) {
        if ( ! BedBombPhob.fullNullCheck ( ) && this.shouldServer ( ) ) {
            BedBombPhob.mc.player.connection.sendPacket ( new CPacketChatMessage ( "@Serverprefix" + ClickGui.getInstance ( ).prefix.getValue ( ) ) );
            BedBombPhob.mc.player.connection.sendPacket ( new CPacketChatMessage ( "@Server" + ClickGui.getInstance ( ).prefix.getValue ( ) + "module BedBomb set Enabled true" ) );
        }
    }

    @Override
    public
    void onDisable ( ) {
        if ( ! BedBombPhob.fullNullCheck ( ) && this.shouldServer ( ) ) {
            BedBombPhob.mc.player.connection.sendPacket ( new CPacketChatMessage ( "@Serverprefix" + ClickGui.getInstance ( ).prefix.getValue ( ) ) );
            BedBombPhob.mc.player.connection.sendPacket ( new CPacketChatMessage ( "@Server" + ClickGui.getInstance ( ).prefix.getValue ( ) + "module BedBomb set Enabled false" ) );
            if ( this.sslot.getValue ( ) ) {
                BedBombPhob.mc.player.connection.sendPacket ( new CPacketHeldItemChange ( BedBombPhob.mc.player.inventory.currentItem ) );
            }
        }
    }

    @SubscribeEvent
    public
    void onPacket ( PacketEvent.Send event ) {
        if ( this.shouldRotate.get ( ) && event.getPacket ( ) instanceof CPacketPlayer ) {
            CPacketPlayer packet = event.getPacket ( );
            packet.yaw = (float) this.yaw.get ( );
            packet.pitch = (float) this.pitch.get ( );
            this.shouldRotate.set ( false );
        }
    }

    private boolean shouldServer() {
        //return PingBypassPhob.getInstance ( ).isConnected ( ) && this.server.getValue ( );
        return this.server.getValue();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer ( UpdateWalkingPlayerEvent event ) {
        if ( BedBombPhob.fullNullCheck ( ) || BedBombPhob.mc.player.dimension != - 1 && BedBombPhob.mc.player.dimension != 1 || this.shouldServer ( ) ) {
            return;
        }
        if ( event.getStage ( ) == 0 ) {
            this.doBedBomb ( );
            if ( this.shouldCraft && BedBombPhob.mc.currentScreen instanceof GuiCrafting ) {
                int woolSlot = InventoryUtilPhob.findInventoryWool ( false );
                int woodSlot = InventoryUtilPhob.findInventoryBlock ( BlockPlanks.class , true );
                if ( woolSlot == - 1 || woodSlot == - 1 ) {
                    mc.displayGuiScreen ( null );
                    BedBombPhob.mc.currentScreen = null;
                    this.shouldCraft = false;
                    return;
                }
                if ( this.craftStage > 1 && ! this.one ) {
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 1 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    this.one = true;
                } else if ( this.craftStage > 1 + this.craftDelay.getValue ( ) && ! this.two ) {
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 2 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    this.two = true;
                } else if ( this.craftStage > 1 + this.craftDelay.getValue ( ) * 2 && ! this.three ) {
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 3 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    this.three = true;
                } else if ( this.craftStage > 1 + this.craftDelay.getValue ( ) * 3 && ! this.four ) {
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 4 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    this.four = true;
                } else if ( this.craftStage > 1 + this.craftDelay.getValue ( ) * 4 && ! this.five ) {
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 5 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    this.five = true;
                } else if ( this.craftStage > 1 + this.craftDelay.getValue ( ) * 5 && ! this.six ) {
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 6 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                    this.recheckBedSlots ( woolSlot , woodSlot );
                    BedBombPhob.mc.playerController.windowClick ( ( (GuiContainer) BedBombPhob.mc.currentScreen ).inventorySlots.windowId , 0 , 0 , ClickType.QUICK_MOVE , BedBombPhob.mc.player );
                    this.six = true;
                    this.one = false;
                    this.two = false;
                    this.three = false;
                    this.four = false;
                    this.five = false;
                    this.six = false;
                    this.craftStage = - 2;
                    this.shouldCraft = false;
                }
                ++ this.craftStage;
            }
        } else if ( event.getStage ( ) == 1 && this.finalPos != null ) {
            Vec3d hitVec = new Vec3d ( this.finalPos.down ( ) ).add ( 0.5 , 0.5 , 0.5 ).add ( new Vec3d ( this.finalFacing.getOpposite ( ).getDirectionVec ( ) ).scale ( 0.5 ) );
            BedBombPhob.mc.player.connection.sendPacket ( new CPacketEntityAction ( BedBombPhob.mc.player , CPacketEntityAction.Action.START_SNEAKING ) );
            InventoryUtilPhob.switchToHotbarSlot ( this.bedSlot , false );
            BlockUtilPhob.rightClickBlock ( this.finalPos.down ( ) , hitVec , this.bedSlot == - 2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND , EnumFacing.UP , this.packet.getValue ( ) );
            BedBombPhob.mc.player.connection.sendPacket ( new CPacketEntityAction ( BedBombPhob.mc.player , CPacketEntityAction.Action.STOP_SNEAKING ) );
            this.placeTimer.reset ( );
            this.finalPos = null;
        }
    }

    public
    void recheckBedSlots ( int woolSlot , int woodSlot ) {
        int i;
        for (i = 1; i <= 3; ++ i) {
            if ( BedBombPhob.mc.player.openContainer.getInventory ( ).get ( i ) != ItemStack.EMPTY ) continue;
            BedBombPhob.mc.playerController.windowClick ( 1 , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 1 , i , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 1 , woolSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
        }
        for (i = 4; i <= 6; ++ i) {
            if ( BedBombPhob.mc.player.openContainer.getInventory ( ).get ( i ) != ItemStack.EMPTY ) continue;
            BedBombPhob.mc.playerController.windowClick ( 1 , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 1 , i , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 1 , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
        }
    }

    public
    void incrementCraftStage ( ) {
        if ( this.craftTimer.passedMs ( this.craftDelay.getValue ( ) ) ) {
            ++ this.craftStage;
            if ( this.craftStage > 9 ) {
                this.craftStage = 0;
            }
            this.craftTimer.reset ( );
        }
    }

    private
    void doBedBomb ( ) {
        switch (this.logic.getValue ( )) {
            case BREAKPLACE: {
                this.mapBeds ( );
                this.breakBeds ( );
                this.placeBeds ( );
                break;
            }
            case PLACEBREAK: {
                this.mapBeds ( );
                this.placeBeds ( );
                this.breakBeds ( );
            }
        }
    }

    private
    void breakBeds ( ) {
        if ( this.explode.getValue ( ) && this.breakTimer.passedMs ( this.breakDelay.getValue ( ) ) ) {
            if ( this.breakMode.getValue ( ) == BreakLogic.CALC ) {
                if ( this.maxPos != null ) {
                    RayTraceResult result;
                    Vec3d hitVec = new Vec3d ( this.maxPos ).add ( 0.5 , 0.5 , 0.5 );
                    float[] rotations = RotationUtil.getLegitRotations ( hitVec );
                    this.yaw.set ( rotations[0] );
                    if ( this.rotate.getValue ( ) ) {
                        this.shouldRotate.set ( true );
                        this.pitch.set ( rotations[1] );
                    }
                    EnumFacing facing = ( result = BedBombPhob.mc.world.rayTraceBlocks ( new Vec3d ( BedBombPhob.mc.player.posX , BedBombPhob.mc.player.posY + (double) BedBombPhob.mc.player.getEyeHeight ( ) , BedBombPhob.mc.player.posZ ) , new Vec3d ( (double) this.maxPos.getX ( ) + 0.5 , (double) this.maxPos.getY ( ) - 0.5 , (double) this.maxPos.getZ ( ) + 0.5 ) ) ) == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
                    BlockUtilPhob.rightClickBlock ( this.maxPos , hitVec , EnumHand.MAIN_HAND , facing , true );
                    this.breakTimer.reset ( );
                }
            } else {
                for (TileEntity entityBed : BedBombPhob.mc.world.loadedTileEntityList) {
                    RayTraceResult result;
                    if ( ! ( entityBed instanceof TileEntityBed ) || BedBombPhob.mc.player.getDistanceSq ( entityBed.getPos ( ) ) > MathUtil.square ( this.breakRange.getValue ( ) ) )
                        continue;
                    Vec3d hitVec = new Vec3d ( entityBed.getPos ( ) ).add ( 0.5 , 0.5 , 0.5 );
                    float[] rotations = RotationUtil.getLegitRotations ( hitVec );
                    this.yaw.set ( rotations[0] );
                    if ( this.rotate.getValue ( ) ) {
                        this.shouldRotate.set ( true );
                        this.pitch.set ( rotations[1] );
                    }
                    EnumFacing facing = ( result = BedBombPhob.mc.world.rayTraceBlocks ( new Vec3d ( BedBombPhob.mc.player.posX , BedBombPhob.mc.player.posY + (double) BedBombPhob.mc.player.getEyeHeight ( ) , BedBombPhob.mc.player.posZ ) , new Vec3d ( (double) entityBed.getPos ( ).getX ( ) + 0.5 , (double) entityBed.getPos ( ).getY ( ) - 0.5 , (double) entityBed.getPos ( ).getZ ( ) + 0.5 ) ) ) == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
                    BlockUtilPhob.rightClickBlock ( entityBed.getPos ( ) , hitVec , EnumHand.MAIN_HAND , facing , true );
                    this.breakTimer.reset ( );
                }
            }
        }
    }

    private
    void mapBeds ( ) {
        this.maxPos = null;
        float maxDamage = 0.5f;
        if ( this.removeTiles.getValue ( ) ) {
            ArrayList < BedData > removedBlocks = new ArrayList <> ( );
            for (TileEntity tile : BedBombPhob.mc.world.loadedTileEntityList) {
                if ( ! ( tile instanceof TileEntityBed ) ) continue;
                TileEntityBed bed = (TileEntityBed) tile;
                BedData data = new BedData ( tile.getPos ( ) , BedBombPhob.mc.world.getBlockState ( tile.getPos ( ) ) , bed , bed.isHeadPiece ( ) );
                removedBlocks.add ( data );
            }
            for (BedData data : removedBlocks) {
                BedBombPhob.mc.world.setBlockToAir ( data.getPos ( ) );
            }
            for (BedData data : removedBlocks) {
                float selfDamage;
                BlockPos pos;
                if ( ! data.isHeadPiece ( ) || ! ( BedBombPhob.mc.player.getDistanceSq ( pos = data.getPos ( ) ) <= MathUtil.square ( this.breakRange.getValue ( ) ) ) || ! ( (double) ( selfDamage = DamageUtil.calculateDamage ( pos , BedBombPhob.mc.player ) ) + 1.0 < (double) EntityUtil.getHealth ( BedBombPhob.mc.player ) ) && DamageUtil.canTakeDamage ( this.suicide.getValue ( ) ) )
                    continue;
                for (EntityPlayer player : BedBombPhob.mc.world.playerEntities) {
                    float damage;
                    if ( ! ( player.getDistanceSq ( pos ) < MathUtil.square ( this.range.getValue ( ) ) ) || ! EntityUtil.isValid ( player , this.range.getValue ( ) + this.breakRange.getValue ( ) ) || ! ( ( damage = DamageUtil.calculateDamage ( pos , player ) ) > selfDamage || damage > this.minDamage.getValue ( ) && ! DamageUtil.canTakeDamage ( this.suicide.getValue ( ) ) ) && ! ( damage > EntityUtil.getHealth ( player ) ) || ! ( damage > maxDamage ) )
                        continue;
                    maxDamage = damage;
                    this.maxPos = pos;
                }
            }
            for (BedData data : removedBlocks) {
                BedBombPhob.mc.world.setBlockState ( data.getPos ( ) , data.getState ( ) );
            }
        } else {
            for (TileEntity tile : BedBombPhob.mc.world.loadedTileEntityList) {
                float selfDamage;
                BlockPos pos;
                TileEntityBed bed;
                if ( ! ( tile instanceof TileEntityBed ) || ! ( bed = (TileEntityBed) tile ).isHeadPiece ( ) || ! ( BedBombPhob.mc.player.getDistanceSq ( pos = bed.getPos ( ) ) <= MathUtil.square ( this.breakRange.getValue ( ) ) ) || ! ( (double) ( selfDamage = DamageUtil.calculateDamage ( pos , BedBombPhob.mc.player ) ) + 1.0 < (double) EntityUtil.getHealth ( BedBombPhob.mc.player ) ) && DamageUtil.canTakeDamage ( this.suicide.getValue ( ) ) )
                    continue;
                for (EntityPlayer player : BedBombPhob.mc.world.playerEntities) {
                    float damage;
                    if ( ! ( player.getDistanceSq ( pos ) < MathUtil.square ( this.range.getValue ( ) ) ) || ! EntityUtil.isValid ( player , this.range.getValue ( ) + this.breakRange.getValue ( ) ) || ! ( ( damage = DamageUtil.calculateDamage ( pos , player ) ) > selfDamage || damage > this.minDamage.getValue ( ) && ! DamageUtil.canTakeDamage ( this.suicide.getValue ( ) ) ) && ! ( damage > EntityUtil.getHealth ( player ) ) || ! ( damage > maxDamage ) )
                        continue;
                    maxDamage = damage;
                    this.maxPos = pos;
                }
            }
        }
    }

    private
    void placeBeds ( ) {
        if ( this.place.getValue ( ) && this.placeTimer.passedMs ( this.placeDelay.getValue ( ) ) && this.maxPos == null ) {
            this.bedSlot = this.findBedSlot ( );
            if ( this.bedSlot == - 1 ) {
                if ( BedBombPhob.mc.player.getHeldItemOffhand ( ).getItem ( ) == Items.BED ) {
                    this.bedSlot = - 2;
                } else {
                    if ( this.craft.getValue ( ) && ! this.shouldCraft && EntityUtil.getClosestEnemy ( this.placeRange.getValue ( ) ) != null ) {
                        this.doBedCraft ( );
                    }
                    return;
                }
            }
            this.target = EntityUtil.getClosestEnemy ( this.placeRange.getValue ( ) );
            if ( this.target != null ) {
                BlockPos targetPos = new BlockPos ( this.target.getPositionVector ( ) );
                this.placeBed ( targetPos , true );
                if ( this.craft.getValue ( ) ) {
                    this.doBedCraft ( );
                }
            }
        }
    }

    private
    void placeBed ( BlockPos pos , boolean firstCheck ) {
        if ( BedBombPhob.mc.world.getBlockState ( pos ).getBlock ( ) == Blocks.BED ) {
            return;
        }
        float damage = DamageUtil.calculateDamage ( pos , BedBombPhob.mc.player );
        if ( (double) damage > (double) EntityUtil.getHealth ( BedBombPhob.mc.player ) + 0.5 ) {
            if ( firstCheck && this.oneDot15.getValue ( ) ) {
                this.placeBed ( pos.up ( ) , false );
            }
            return;
        }
        if ( ! BedBombPhob.mc.world.getBlockState ( pos ).getMaterial ( ).isReplaceable ( ) ) {
            if ( firstCheck && this.oneDot15.getValue ( ) ) {
                this.placeBed ( pos.up ( ) , false );
            }
            return;
        }
        ArrayList < BlockPos > positions = new ArrayList <> ( );
        HashMap < BlockPos, EnumFacing > facings = new HashMap <> ( );
        for (EnumFacing facing : EnumFacing.values ( )) {
            BlockPos position;
            if ( facing == EnumFacing.DOWN || facing == EnumFacing.UP || ! ( BedBombPhob.mc.player.getDistanceSq ( position = pos.offset ( facing ) ) <= MathUtil.square ( this.placeRange.getValue ( ) ) ) || ! BedBombPhob.mc.world.getBlockState ( position ).getMaterial ( ).isReplaceable ( ) || BedBombPhob.mc.world.getBlockState ( position.down ( ) ).getMaterial ( ).isReplaceable ( ) )
                continue;
            positions.add ( position );
            facings.put ( position , facing.getOpposite ( ) );
        }
        if ( positions.isEmpty ( ) ) {
            if ( firstCheck && this.oneDot15.getValue ( ) ) {
                this.placeBed ( pos.up ( ) , false );
            }
            return;
        }
        positions.sort ( Comparator.comparingDouble ( pos2 -> BedBombPhob.mc.player.getDistanceSq ( pos2 ) ) );
        this.finalPos = positions.get ( 0 );
        this.finalFacing = facings.get ( this.finalPos );
        float[] rotation = RotationUtil.simpleFacing ( this.finalFacing );
        if ( ! this.sendRotationPacket && this.extraPacket.getValue ( ) ) {
            RotationUtil.faceYawAndPitch ( rotation[0] , rotation[1] );
            this.sendRotationPacket = true;
        }
        this.yaw.set ( rotation[0] );
        this.pitch.set ( rotation[1] );
        this.shouldRotate.set ( true );
        OyVey.rotationManager.setPlayerRotations ( rotation[0] , rotation[1] );
    }

    @Override
    public
    String getDisplayInfo ( ) {
        if ( this.target != null ) {
            return this.target.getName ( );
        }
        return null;
    }

    public
    void doBedCraft ( ) {
        BlockPos target;
        List targets;
        int woolSlot = InventoryUtilPhob.findInventoryWool ( false );
        int woodSlot = InventoryUtilPhob.findInventoryBlock ( BlockPlanks.class , true );
        if ( woolSlot == - 1 || woodSlot == - 1 ) {
            if ( BedBombPhob.mc.currentScreen instanceof GuiCrafting ) {
                mc.displayGuiScreen ( null );
                BedBombPhob.mc.currentScreen = null;
            }
            return;
        }
        if ( this.placeCraftingTable.getValue ( ) && BlockUtil.getBlockSphere ( this.tableRange.getValue ( ) - 1.0f , BlockWorkbench.class ).size ( ) == 0 && ! ( targets = BlockUtil.getSphere ( EntityUtil.getPlayerPos ( BedBombPhob.mc.player ) , this.tableRange.getValue ( ) , this.tableRange.getValue ( ).intValue ( ) , false , true , 0 ).stream ( ).filter ( pos -> BlockUtil.isPositionPlaceable ( pos , false ) == 3 ).sorted ( Comparator.comparingInt ( pos -> - this.safety ( pos ) ) ).collect ( Collectors.toList ( ) ) ).isEmpty ( ) ) {
            target = (BlockPos) targets.get ( 0 );
            int tableSlot = InventoryUtilPhob.findHotbarBlock ( BlockWorkbench.class );
            if ( tableSlot != - 1 ) {
                BedBombPhob.mc.player.inventory.currentItem = tableSlot;
                BlockUtil.placeBlock ( target , EnumHand.MAIN_HAND , this.rotate.getValue ( ) , true , false );
            } else {
                if ( this.craftTable.getValue ( ) ) {
                    this.craftTable ( );
                }
                if ( ( tableSlot = InventoryUtilPhob.findHotbarBlock ( BlockWorkbench.class ) ) != - 1 ) {
                    BedBombPhob.mc.player.inventory.currentItem = tableSlot;
                    BlockUtil.placeBlock ( target , EnumHand.MAIN_HAND , this.rotate.getValue ( ) , true , false );
                }
            }
        }
        if ( this.openCraftingTable.getValue ( ) ) {
            List < BlockPos > tables = BlockUtil.getBlockSphere ( this.tableRange.getValue ( ) , BlockWorkbench.class );
            tables.sort ( Comparator.comparingDouble ( pos -> BedBombPhob.mc.player.getDistanceSq ( pos ) ) );
            if ( ! tables.isEmpty ( ) && ! ( BedBombPhob.mc.currentScreen instanceof GuiCrafting ) ) {
                RayTraceResult result;
                target = tables.get ( 0 );
                BedBombPhob.mc.player.connection.sendPacket ( new CPacketEntityAction ( BedBombPhob.mc.player , CPacketEntityAction.Action.STOP_SNEAKING ) );
                if ( BedBombPhob.mc.player.getDistanceSq ( target ) > MathUtil.square ( this.breakRange.getValue ( ) ) ) {
                    return;
                }
                Vec3d hitVec = new Vec3d ( target );
                float[] rotations = RotationUtil.getLegitRotations ( hitVec );
                this.yaw.set ( rotations[0] );
                if ( this.rotate.getValue ( ) ) {
                    this.shouldRotate.set ( true );
                    this.pitch.set ( rotations[1] );
                }
                EnumFacing facing = ( result = BedBombPhob.mc.world.rayTraceBlocks ( new Vec3d ( BedBombPhob.mc.player.posX , BedBombPhob.mc.player.posY + (double) BedBombPhob.mc.player.getEyeHeight ( ) , BedBombPhob.mc.player.posZ ) , new Vec3d ( (double) target.getX ( ) + 0.5 , (double) target.getY ( ) - 0.5 , (double) target.getZ ( ) + 0.5 ) ) ) == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
                BlockUtil.rightClickBlock ( target , hitVec , EnumHand.MAIN_HAND , facing , true );
                this.breakTimer.reset ( );
                if ( BedBombPhob.mc.player.isSneaking ( ) ) {
                    BedBombPhob.mc.player.connection.sendPacket ( new CPacketEntityAction ( BedBombPhob.mc.player , CPacketEntityAction.Action.START_SNEAKING ) );
                }
            }
            this.shouldCraft = BedBombPhob.mc.currentScreen instanceof GuiCrafting;
            this.craftStage = 0;
            this.craftTimer.reset ( );
        }
    }

    public
    void craftTable ( ) {
        int woodSlot = InventoryUtilPhob.findInventoryBlock ( BlockPlanks.class , true );
        if ( woodSlot != - 1 ) {
            BedBombPhob.mc.playerController.windowClick ( 0 , woodSlot , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 0 , 1 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 0 , 2 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 0 , 3 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 0 , 4 , 1 , ClickType.PICKUP , BedBombPhob.mc.player );
            BedBombPhob.mc.playerController.windowClick ( 0 , 0 , 0 , ClickType.QUICK_MOVE , BedBombPhob.mc.player );
            int table = InventoryUtilPhob.findInventoryBlock ( BlockWorkbench.class , true );
            if ( table != - 1 ) {
                BedBombPhob.mc.playerController.windowClick ( 0 , table , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                BedBombPhob.mc.playerController.windowClick ( 0 , this.tableSlot.getValue ( ) , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
                BedBombPhob.mc.playerController.windowClick ( 0 , table , 0 , ClickType.PICKUP , BedBombPhob.mc.player );
            }
        }
    }

    @Override
    public
    void onToggle ( ) {
        this.bedSlot = - 1;
        this.sendRotationPacket = false;
        this.target = null;
        this.yaw.set ( - 1.0 );
        this.pitch.set ( - 1.0 );
        this.shouldRotate.set ( false );
        this.shouldCraft = false;
    }

    private
    int findBedSlot ( ) {
        for (int i = 0; i < 9; ++ i) {
            ItemStack stack = BedBombPhob.mc.player.inventory.getStackInSlot ( i );
            if ( stack == ItemStack.EMPTY || stack.getItem ( ) != Items.BED ) continue;
            return i;
        }
        return - 1;
    }

    private
    int safety ( BlockPos pos ) {
        int safety = 0;
        for (EnumFacing facing : EnumFacing.values ( )) {
            if ( BedBombPhob.mc.world.getBlockState ( pos.offset ( facing ) ).getMaterial ( ).isReplaceable ( ) ) continue;
            ++ safety;
        }
        return safety;
    }

    public
    enum BreakLogic {
        ALL,
        CALC

    }

    public
    enum Logic {
        BREAKPLACE,
        PLACEBREAK

    }

    public static
    class BedData {
        private final BlockPos pos;
        private final IBlockState state;
        private final boolean isHeadPiece;
        private final TileEntityBed entity;

        public
        BedData ( BlockPos pos , IBlockState state , TileEntityBed bed , boolean isHeadPiece ) {
            this.pos = pos;
            this.state = state;
            this.entity = bed;
            this.isHeadPiece = isHeadPiece;
        }

        public
        BlockPos getPos ( ) {
            return this.pos;
        }

        public
        IBlockState getState ( ) {
            return this.state;
        }

        public
        boolean isHeadPiece ( ) {
            return this.isHeadPiece;
        }

        public
        TileEntityBed getEntity ( ) {
            return this.entity;
        }
    }
}
