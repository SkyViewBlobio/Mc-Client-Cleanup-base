package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.gui.OyVeyGui;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Bind;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.InventoryUtilPhob;
import me.alpha432.oyvey.util.ReflectionUtilPhob;
import me.alpha432.oyvey.util.Util;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class XCarryPhob extends Module {
    private static XCarryPhob INSTANCE = new XCarryPhob();
    private final Setting < Boolean > simpleMode = this.register ( new Setting <> ( "Smart" , false ) );
    private final Setting < Bind > autoStore = this.register ( new Setting <> ( "InDuels" , new Bind ( - 1 ) ) );
    private final Setting < Integer > obbySlot = this.register ( new Setting < Object > ( "ObbySlot" , 2 , 1 , 9 , v -> this.autoStore.getValue ( ).getKey ( ) != - 1 ) );
    private final Setting < Integer > slot1 = this.register ( new Setting < Object > ( "Slot1" , 22 , 9 , 44 , v -> this.autoStore.getValue ( ).getKey ( ) != - 1 ) );
    private final Setting < Integer > slot2 = this.register ( new Setting < Object > ( "Slot2" , 23 , 9 , 44 , v -> this.autoStore.getValue ( ).getKey ( ) != - 1 ) );
    private final Setting < Integer > slot3 = this.register ( new Setting < Object > ( "Slot3" , 24 , 9 , 44 , v -> this.autoStore.getValue ( ).getKey ( ) != - 1 ) );
    private final Setting < Integer > tasks = this.register ( new Setting < Object > ( "Actions" , 3 , 1 , 12 , v -> this.autoStore.getValue ( ).getKey ( ) != - 1 ) );
    private final Setting < Boolean > store = this.register ( new Setting <> ( "Keep" , false ) );
    private final Setting < Boolean > shiftClicker = this.register ( new Setting <> ( "ShiftClick" , false ) );
    private final Setting < Boolean > withShift = this.register ( new Setting < Object > ( "WithShift" , Boolean.TRUE , v -> this.shiftClicker.getValue ( ) ) );
    private final Setting < Bind > keyBind = this.register ( new Setting < Object > ( "ShiftBind" , new Bind ( - 1 ) , v -> this.shiftClicker.getValue ( ) ) );
    private final AtomicBoolean guiNeedsClose = new AtomicBoolean ( false );
    private final Queue < InventoryUtilPhob.Task > taskList = new ConcurrentLinkedQueue <> ( );
    private GuiInventory openedGui;
    private boolean guiCloseGuard;
    private boolean autoDuelOn;
    private boolean obbySlotDone;
    private boolean slot1done;
    private boolean slot2done;
    private boolean slot3done;
    private List < Integer > doneSlots = new ArrayList <> ( );


    public XCarryPhob ( ) {
        super ( "XCarry" , "Uses Inv slots that are normally not for keeeping items" , Module.Category.PLAYER , true , false , false );
        this.setInstance ( );
    }

    public static XCarryPhob getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new XCarryPhob();
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
        if ( this.shiftClicker.getValue ( ) && XCarryPhob.mc.currentScreen instanceof GuiInventory ) {
            Slot slot;
            boolean ourBind;
            ourBind = this.keyBind.getValue ( ).getKey ( ) != - 1 && Keyboard.isKeyDown ( this.keyBind.getValue ( ).getKey ( ) ) && ! Keyboard.isKeyDown ( 42 );
            if ( ( Keyboard.isKeyDown ( 42 ) && this.withShift.getValue ( ) || ourBind ) && Mouse.isButtonDown ( 0 ) && ( slot = ( (GuiInventory) XCarryPhob.mc.currentScreen ).getSlotUnderMouse ( ) ) != null && InventoryUtilPhob.getEmptyXCarry() != - 1 ) {
                int slotNumber = slot.slotNumber;
                if ( slotNumber > 4 && ourBind ) {
                    this.taskList.add ( new InventoryUtilPhob.Task ( slotNumber ) );
                    this.taskList.add ( new InventoryUtilPhob.Task ( InventoryUtilPhob.getEmptyXCarry() ) );
                } else if ( slotNumber > 4 && this.withShift.getValue ( ) ) {
                    boolean isHotBarFull = true;
                    boolean isInvFull = true;
                    for (int i : InventoryUtilPhob.findEmptySlots ( false )) {
                        if ( i > 4 && i < 36 ) {
                            isInvFull = false;
                            continue;
                        }
                        if ( i <= 35 || i >= 45 ) continue;
                        isHotBarFull = false;
                    }
                    if ( slotNumber > 35 && slotNumber < 45 ) {
                        if ( isInvFull ) {
                            this.taskList.add ( new InventoryUtilPhob.Task ( slotNumber ) );
                            this.taskList.add ( new InventoryUtilPhob.Task ( InventoryUtilPhob.getEmptyXCarry() ) );
                        }
                    } else if ( isHotBarFull ) {
                        this.taskList.add ( new InventoryUtilPhob.Task ( slotNumber ) );
                        this.taskList.add ( new InventoryUtilPhob.Task ( InventoryUtilPhob.getEmptyXCarry() ) );
                    }
                }
            }
        }
        if ( this.autoDuelOn ) {
            this.doneSlots = new ArrayList <> ( );
            if ( InventoryUtilPhob.getEmptyXCarry() == - 1 || this.obbySlotDone && this.slot1done && this.slot2done && this.slot3done ) {
                this.autoDuelOn = false;
            }
            if ( this.autoDuelOn ) {
                if ( ! this.obbySlotDone && ! XCarryPhob.mc.player.inventory.getStackInSlot ( this.obbySlot.getValue ( ) - 1 ).isEmpty ) {
                    this.addTasks ( 36 + this.obbySlot.getValue ( ) - 1 );
                }
                this.obbySlotDone = true;
                if ( ! this.slot1done && ! XCarryPhob.mc.player.inventoryContainer.inventorySlots.get ( this.slot1.getValue ( ) ).getStack ( ).isEmpty ) {
                    this.addTasks ( this.slot1.getValue ( ) );
                }
                this.slot1done = true;
                if ( ! this.slot2done && ! XCarryPhob.mc.player.inventoryContainer.inventorySlots.get ( this.slot2.getValue ( ) ).getStack ( ).isEmpty ) {
                    this.addTasks ( this.slot2.getValue ( ) );
                }
                this.slot2done = true;
                if ( ! this.slot3done && ! XCarryPhob.mc.player.inventoryContainer.inventorySlots.get ( this.slot3.getValue ( ) ).getStack ( ).isEmpty ) {
                    this.addTasks ( this.slot3.getValue ( ) );
                }
                this.slot3done = true;
            }
        } else {
            this.obbySlotDone = false;
            this.slot1done = false;
            this.slot2done = false;
            this.slot3done = false;
        }
        if ( ! this.taskList.isEmpty ( ) ) {
            for (int i = 0; i < this.tasks.getValue ( ); ++ i) {
                InventoryUtilPhob.Task task = this.taskList.poll ( );
                if ( task == null ) continue;
                task.run ( );
            }
        }
    }

    private
    void addTasks ( int slot ) {
        if ( InventoryUtilPhob.getEmptyXCarry() != - 1 ) {
            int XCarryPhobSlot = InventoryUtilPhob.getEmptyXCarry();
            if ( ! ( ! this.doneSlots.contains ( XCarryPhobSlot ) && InventoryUtilPhob.isSlotEmpty ( XCarryPhobSlot ) || ! this.doneSlots.contains ( ++ XCarryPhobSlot ) && InventoryUtilPhob.isSlotEmpty ( XCarryPhobSlot ) || ! this.doneSlots.contains ( ++ XCarryPhobSlot ) && InventoryUtilPhob.isSlotEmpty ( XCarryPhobSlot ) || ! this.doneSlots.contains ( ++ XCarryPhobSlot ) && InventoryUtilPhob.isSlotEmpty ( XCarryPhobSlot ) ) ) {
                return;
            }
            if ( XCarryPhobSlot > 4 ) {
                return;
            }
            this.doneSlots.add ( XCarryPhobSlot );
            this.taskList.add ( new InventoryUtilPhob.Task ( slot ) );
            this.taskList.add ( new InventoryUtilPhob.Task ( XCarryPhobSlot ) );
            this.taskList.add ( new InventoryUtilPhob.Task ( ) );
        }
    }

    @Override
    public
    void onDisable ( ) {
        if ( fullNullCheck ( ) ) return;
        if ( ! this.simpleMode.getValue ( ) ) {
            this.closeGui ( );
            this.close ( );
        } else {
            XCarryPhob.mc.player.connection.sendPacket ( new CPacketCloseWindow ( XCarryPhob.mc.player.inventoryContainer.windowId ) );
        }
    }

    @Override
    public
    void onLogout ( ) {
        this.onDisable ( );
    }

    @SubscribeEvent
    public void onCloseGuiScreen ( PacketEvent.Send event ) {
        if ( this.simpleMode.getValue ( ) && event.getPacket ( ) instanceof CPacketCloseWindow ) {
            CPacketCloseWindow packet = event.getPacket ( );
            if ( packet.windowId == XCarryPhob.mc.player.inventoryContainer.windowId ) {
                event.setCanceled ( true );
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public
    void onGuiOpen ( GuiOpenEvent event ) {
        if ( ! this.simpleMode.getValue ( ) ) {
            if ( this.guiCloseGuard ) {
                event.setCanceled ( true );
            } else if ( event.getGui ( ) instanceof GuiInventory ) {
                this.openedGui = this.createGuiWrapper ( (GuiInventory) event.getGui ( ) );
                event.setGui ( this.openedGui );
                this.guiNeedsClose.set ( false );
            }
        }
    }

    @SubscribeEvent
    public void onSettingChange ( ClientEvent event ) {
        if ( event.getStage ( ) == 2 && event.getSetting ( ) != null && event.getSetting ( ).getFeature ( ) != null && event.getSetting ( ).getFeature ( ).equals ( this ) ) {
            Setting setting = event.getSetting ( );
            String settingname = event.getSetting ( ).getName ( );
            if ( setting.equals ( this.simpleMode ) && setting.getPlannedValue ( ) != setting.getValue ( ) ) {
                this.disable ( );
            } else if ( settingname.equalsIgnoreCase ( "Store" ) ) {
                event.setCanceled ( true );
                this.autoDuelOn = ! this.autoDuelOn;
                Command.sendMessage ( "<XCarryPhob> \u00a7aAutostoring..." );
            }
        }
    }

    @SubscribeEvent
    public
    void onKeyInput ( InputEvent.KeyInputEvent event ) {
        if ( Keyboard.getEventKeyState ( ) && ! ( XCarryPhob.mc.currentScreen instanceof OyVeyGui) && this.autoStore.getValue ( ).getKey ( ) == Keyboard.getEventKey ( ) ) {
            this.autoDuelOn = ! this.autoDuelOn;
            Command.sendMessage ( "<XCarryPhob> \u00a7aAutostoring..." );
        }
    }

    private
    void close ( ) {
        this.openedGui = null;
        this.guiNeedsClose.set ( false );
        this.guiCloseGuard = false;
    }

    private
    void closeGui ( ) {
        if ( fullNullCheck ( ) ) {
            return;
        }
        if ( this.guiNeedsClose.compareAndSet ( true , false ) && ! XCarryPhob.fullNullCheck ( ) ) {
            this.guiCloseGuard = true;
            XCarryPhob.mc.player.closeScreen ( );
            if ( this.openedGui != null ) {
                this.openedGui.onGuiClosed ( );
                this.openedGui = null;
            }
            this.guiCloseGuard = false;
        }
    }

    private
    GuiInventory createGuiWrapper ( GuiInventory gui ) {
        try {
            GuiInventoryWrapper wrapper = new GuiInventoryWrapper ( );
            ReflectionUtilPhob.copyOf ( gui , wrapper );
            return wrapper;
        } catch ( IllegalAccessException | NoSuchFieldException e ) {
            e.printStackTrace ( );
            return null;
        }
    }

    private
    class GuiInventoryWrapper
            extends GuiInventory {
        GuiInventoryWrapper ( ) {
            super ( Util.mc.player );
        }

        protected
        void keyTyped ( char typedChar , int keyCode ) throws IOException {
            if ( XCarryPhob.this.isEnabled ( ) && ( keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches ( keyCode ) ) ) {
                XCarryPhob.this.guiNeedsClose.set ( true );
                this.mc.displayGuiScreen ( null );
            } else {
                super.keyTyped ( typedChar , keyCode );
            }
        }

        public
        void onGuiClosed ( ) {
            if ( XCarryPhob.this.guiCloseGuard || ! XCarryPhob.this.isEnabled ( ) ) {
                super.onGuiClosed ( );
            }
        }
    }
}
