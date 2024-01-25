package me.alpha432.oyvey.features.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Timestamps extends Module {
    public Timestamps() {
        super("ChatTime", "Prefixes chat messages with the time", Category.CLIENT, true, false, false);
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");
        String strDate = dateFormatter.format(date);
        TextComponentString time = new TextComponentString(ChatFormatting.LIGHT_PURPLE + "[" + ChatFormatting.DARK_PURPLE + strDate + ChatFormatting.LIGHT_PURPLE + "]" + ChatFormatting.RESET + " ");
        event.setMessage(time.appendSibling(event.getMessage()));
    }
}