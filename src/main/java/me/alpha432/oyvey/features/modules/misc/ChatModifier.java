package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
//import me.alpha432.oyvey.features.modules.client.Management;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.XuluTextSex;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public
class ChatModifier
        extends Module {
    private static ChatModifier INSTANCE = new ChatModifier ( );
    private final Timer timer = new Timer ( );
    public Setting < Suffix > suffix = this.register ( new Setting <> ( "Suffix" , Suffix.Ax3Mode , "Is The Current Suffix." ) );
    public Setting < String > customSuffix = this.register ( new Setting <> ( "" , " | Xulu+ Owns Yo Ass " , v -> this.suffix.getValue ( ) == Suffix.Ax3Mode ) );
    public Setting < Boolean > clean = this.register ( new Setting <> ( "Clean" , Boolean.TRUE , "Cleans your chat" ) );
    public Setting < Boolean > infinite = this.register ( new Setting <> ( "Infinite" , Boolean.FALSE , "Makes your chat infinite." ) );
    //public Setting < Boolean > qNotification = this.register ( new Setting < Object > ( "QNotification" , Boolean.FALSE , v -> this.autoQMain.getValue ( ) ) );
    //public Setting < Integer > qDelay = this.register ( new Setting < Object > ( "QDelay" , 9 , 1 , 90 , v -> this.autoQMain.getValue ( ) ) );
    //public Setting < XuluTextSex.Color > timeStamps = this.register ( new Setting <> ( "Time" , XuluTextSex.Color.NONE ) );
    public Setting < Boolean > disability = this.register ( new Setting <> ( "" , false ) );

    public
    ChatModifier ( ) {
        super ( "Chat Modifier (X)" , "Modifies your chat" , Module.Category.MISC , true , false , false );
        this.setInstance ( );
    }

    public static
    ChatModifier getInstance ( ) {
        if ( INSTANCE == null ) {
            INSTANCE = new ChatModifier ( );
        }
        return INSTANCE;
    }

    private
    void setInstance ( ) {
        INSTANCE = this;

        if ( this.disability.getValue ( ) ) {
            ChatModifier.mc.player.sendChatMessage(XuluTextSex.disability);
            this.disability.setValue(false);
        }}

    @SubscribeEvent
    public
    void onPacketSend ( PacketEvent.Send event ) {
        if ( event.getStage ( ) == 0 && event.getPacket ( ) instanceof CPacketChatMessage ) {
            CPacketChatMessage packet = event.getPacket();
            String s = packet.getMessage();
            if (s.startsWith("/")) {
                return;
            }
            switch (this.suffix.getValue()) {

                case Ax3Mode: {
                    s = s + this.customSuffix.getValue ( );
                    break;
                }
            }
            if ( s.length ( ) >= 256 ) {
                s = s.substring ( 0 , 256 );
            }
            packet.message = s;
        }
    }

    @SubscribeEvent
    public
    void onChatPacketReceive ( PacketEvent.Receive event ) {
        if ( event.getStage ( ) == 0 ) {
            event.getPacket ( );
        }// empty if block
    }

    public
    enum Suffix {
        NONE,
        Ax3Mode

    }
}
