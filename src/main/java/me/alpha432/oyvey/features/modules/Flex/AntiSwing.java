package me.alpha432.oyvey.features.modules.Flex;

import io.netty.buffer.Unpooled;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public
class AntiSwing
        extends Module {
    public
    AntiSwing ( ) {
        super ( "-AntiSwing" , "wont swing your hands" , Category.Flex , true , false , false );
    }

    @SubscribeEvent
    public
    void onPacketSend ( PacketEvent.Send event ) {
        CPacketCustomPayload packet;
        if ( event.getPacket ( ) instanceof FMLProxyPacket && ! mc.isSingleplayer ( ) ) {
            event.setCanceled ( true );
        }
        if ( event.getPacket ( ) instanceof CPacketCustomPayload && ( packet = event.getPacket ( ) ).getChannelName ( ).equals ( "MC|Brand" ) ) {
            packet.data = new PacketBuffer ( Unpooled.buffer ( ) ).writeString ( "vanilla" );
        }
    }
}
