package me.alpha432.oyvey.features.modules.Flex;

import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AntiLog4j extends Module {

    public AntiLog4j() {
        super("-Log4j Protect", "Makes log4j exploit disappear", Category.Flex, true, false, false);
    }

    @SubscribeEvent(priority= EventPriority.HIGHEST)
    public void onPacketRecieve(PacketEvent.Receive event) {
        String text;
        if (event.getPacket() instanceof SPacketChat && ((text = ((SPacketChat)event.getPacket()).getChatComponent().getUnformattedText()).contains("${") || text.contains("$<") || text.contains("$:-") || text.contains("jndi:ldap"))) {
            Command.sendMessage("§d[Log4j Protect] §dBlocked message: " + text);
            event.setCanceled(true);
        }
    }
}